package com.yonatankarp.ff4k.store.redis

import com.yonatankarp.ff4k.FeatureStore
import com.yonatankarp.ff4k.PropertyStore
import com.yonatankarp.ff4k.test.contract.FeatureStoreContractTest
import com.yonatankarp.ff4k.test.contract.PropertyStoreContractTest
import io.kotest.core.annotation.Condition
import io.kotest.core.annotation.EnabledIf
import io.kotest.core.spec.Spec
import io.lettuce.core.RedisClient
import io.lettuce.core.api.StatefulRedisConnection
import org.testcontainers.DockerClientFactory
import org.testcontainers.containers.GenericContainer
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
private val redis: Result<StatefulRedisConnection<String, String>> by lazy {
    runCatching {
        val container = GenericContainer(mirrored("redis:7-alpine", "redis")).withExposedPorts(6379).apply { start() }
        RedisClient.create("redis://${container.host}:${container.getMappedPort(6379)}").connect()
    }
}

// A fresh hash key per store keeps every store empty without flushing the shared server.
private fun uniqueKey(prefix: String) = "$prefix:${UUID.randomUUID()}"

@EnabledIf(DockerAvailable::class)
class RedisFeatureStoreTest : FeatureStoreContractTest() {
    override suspend fun createStore(): FeatureStore = RedisFeatureStore(redis.getOrThrow(), uniqueKey("ff4k:features"))
}

@EnabledIf(DockerAvailable::class)
class RedisPropertyStoreTest : PropertyStoreContractTest() {
    override suspend fun createStore(): PropertyStore = RedisPropertyStore(redis.getOrThrow(), uniqueKey("ff4k:properties"))
}
