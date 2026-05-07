package com.yonatankarp.ff4k.store.jdbc

import com.mysql.cj.jdbc.MysqlDataSource
import com.yonatankarp.ff4k.core.Feature
import com.yonatankarp.ff4k.property.PropertyInt
import com.yonatankarp.ff4k.property.PropertyString
import com.yonatankarp.ff4k.strategy.AlwaysTrueFlippingStrategy
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.maps.shouldContainKey
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.Dispatchers
import org.postgresql.ds.PGSimpleDataSource
import org.testcontainers.mysql.MySQLContainer
import org.testcontainers.postgresql.PostgreSQLContainer
import javax.sql.DataSource

class SupportedDialectFeatureStoreIntegrationTest : FunSpec({

    val databases = listOf(
        SupportedJdbcDatabase.PostgreSQL,
        SupportedJdbcDatabase.MySQL,
    )

    afterSpec {
        databases.forEach { it.close() }
    }

    databases.forEach { database ->
        context(database.displayName) {
            test("persists feature state through the auto-detected JDBC dialect") {
                val store = jdbcFeatureStore(
                    dataSource = database.dataSource(),
                    ioDispatcher = Dispatchers.IO.limitedParallelism(1),
                )
                store.clear()

                val feature = Feature(
                    uid = "checkout-rollout",
                    isEnabled = true,
                    group = "payments",
                    description = "Checkout rollout for ${database.displayName}",
                    permissions = setOf("ADMIN", "SUPPORT"),
                    flippingStrategy = AlwaysTrueFlippingStrategy,
                    customProperties = mapOf(
                        "maxAttempts" to PropertyInt("maxAttempts", 3),
                        "region" to PropertyString("region", "EU"),
                    ),
                )

                store += feature

                val loaded = store["checkout-rollout"]
                loaded.shouldNotBeNull()
                loaded.uid shouldBe feature.uid
                loaded.isEnabled.shouldBeTrue()
                loaded.group shouldBe "payments"
                loaded.description shouldBe "Checkout rollout for ${database.displayName}"
                loaded.permissions shouldContainExactlyInAnyOrder listOf("ADMIN", "SUPPORT")
                loaded.flippingStrategy shouldBe AlwaysTrueFlippingStrategy
                loaded.customProperties shouldContainKey "maxAttempts"
                loaded.customProperties["maxAttempts"].shouldBeInstanceOf<PropertyInt>()
                (loaded.customProperties["maxAttempts"] as PropertyInt).value shouldBe 3
                loaded.customProperties["region"].shouldBeInstanceOf<PropertyString>()
                (loaded.customProperties["region"] as PropertyString).value shouldBe "EU"
            }

            test("keeps group operations isolated between feature groups") {
                val store = jdbcFeatureStore(
                    dataSource = database.dataSource(),
                    ioDispatcher = Dispatchers.IO.limitedParallelism(1),
                )
                store.clear()

                store += Feature("payment-a", isEnabled = true, group = "payments")
                store += Feature("payment-b", isEnabled = true, group = "payments")
                store += Feature("search-a", isEnabled = true, group = "search")

                store.disableGroup("payments")

                val paymentFeatures = store.getGroup("payments")
                paymentFeatures.size shouldBe 2
                paymentFeatures.values.forEach { it.isEnabled.shouldBeFalse() }

                val searchFeature = store["search-a"]
                searchFeature.shouldNotBeNull()
                searchFeature.isEnabled.shouldBeTrue()
                store.containsGroup("payments").shouldBeTrue()
                store.containsGroup("missing").shouldBeFalse()
            }

            test("updates existing features with createOrUpdate") {
                val store = jdbcFeatureStore(
                    dataSource = database.dataSource(),
                    ioDispatcher = Dispatchers.IO.limitedParallelism(1),
                )
                store.clear()

                store.createOrUpdate(Feature("kill-switch", isEnabled = false, description = "Initial"))
                store.createOrUpdate(Feature("kill-switch", isEnabled = true, description = "Updated"))

                val loaded = store["kill-switch"]
                loaded.shouldNotBeNull()
                loaded.isEnabled.shouldBeTrue()
                loaded.description shouldBe "Updated"
                store.count() shouldBe 1
            }
        }
    }
})

private sealed class SupportedJdbcDatabase(val displayName: String) {
    abstract fun dataSource(): DataSource

    abstract fun close()

    object PostgreSQL : SupportedJdbcDatabase("PostgreSQL") {
        @Volatile
        private var container: PostgreSQLContainer? = null

        override fun dataSource(): DataSource {
            val postgres = container ?: synchronized(this) {
                container ?: PostgreSQLContainer("postgres:14-alpine")
                    .apply { start() }
                    .also { container = it }
            }

            return PGSimpleDataSource().apply {
                setUrl(postgres.jdbcUrl)
                user = postgres.username
                password = postgres.password
            }
        }

        override fun close() {
            container?.stop()
            container = null
        }
    }

    object MySQL : SupportedJdbcDatabase("MySQL") {
        @Volatile
        private var container: MySQLContainer? = null

        override fun dataSource(): DataSource {
            val mysql = container ?: synchronized(this) {
                container ?: MySQLContainer("mysql:8.4")
                    .apply { start() }
                    .also { container = it }
            }

            return MysqlDataSource().apply {
                setUrl(mysql.jdbcUrl)
                user = mysql.username
                password = mysql.password
            }
        }

        override fun close() {
            container?.stop()
            container = null
        }
    }
}
