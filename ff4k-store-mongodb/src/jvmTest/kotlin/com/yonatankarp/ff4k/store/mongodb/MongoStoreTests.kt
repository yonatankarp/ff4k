package com.yonatankarp.ff4k.store.mongodb

import com.mongodb.kotlin.client.coroutine.MongoClient
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import com.yonatankarp.ff4k.Feature
import com.yonatankarp.ff4k.FeatureStore
import com.yonatankarp.ff4k.PropertyStore
import com.yonatankarp.ff4k.test.contract.FeatureStoreContractTest
import com.yonatankarp.ff4k.test.contract.PropertyStoreContractTest
import io.kotest.core.annotation.Condition
import io.kotest.core.annotation.EnabledIf
import io.kotest.core.spec.Spec
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import org.testcontainers.DockerClientFactory
import org.testcontainers.mongodb.MongoDBContainer
import org.testcontainers.utility.DockerImageName
import java.util.UUID
import kotlin.reflect.KClass

class DockerAvailable : Condition {
    override fun evaluate(kclass: KClass<out Spec>): Boolean = DockerClientFactory.instance().isDockerAvailable
}

// Official images come from the ECR Public mirror so tests do not depend on Docker Hub availability or rate limits.
private fun mirrored(
    image: String,
    original: String,
): DockerImageName = DockerImageName.parse("public.ecr.aws/docker/library/$image").asCompatibleSubstituteFor(original)

// Result caches a failed start too, so one unavailable image fails every test at once instead of retrying per test.
private val mongo: Result<MongoDatabase> by lazy {
    runCatching {
        val container = MongoDBContainer(mirrored("mongo:7", "mongo")).apply { start() }
        MongoClient.create(container.connectionString).getDatabase("ff4k")
    }
}

/** A collection name no other store uses, so every store starts empty without touching other data. */
private fun freshCollection(): String = "ff4k_${UUID.randomUUID()}"

@EnabledIf(DockerAvailable::class)
class MongoFeatureStoreTest : FeatureStoreContractTest() {
    override suspend fun createStore(): FeatureStore = MongoFeatureStore(mongo.getOrThrow(), collection = freshCollection())
}

@EnabledIf(DockerAvailable::class)
class MongoPropertyStoreTest : PropertyStoreContractTest() {
    override suspend fun createStore(): PropertyStore = MongoPropertyStore(mongo.getOrThrow(), collection = freshCollection())
}

@EnabledIf(DockerAvailable::class)
class MongoFeatureStoreRaceTest :
    FunSpec({
        test("an update does not overwrite a feature deleted and recreated while it ran") {
            val store = MongoFeatureStore(mongo.getOrThrow(), collection = freshCollection())
            store.put(Feature("f", description = "original"))
            var calls = 0

            val updated = store.update("f") { feature ->
                // between the read and the write, another caller recreates the feature
                if (calls++ == 0) {
                    runBlocking {
                        store.delete("f")
                        store.put(Feature("f", description = "recreated"))
                    }
                }
                feature.copy(enabled = true)
            }

            updated shouldBe Feature("f", enabled = true, description = "recreated")
            store.get("f") shouldBe updated
        }
    })
