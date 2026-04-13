package com.frisboo.corebanking.persistence.postgres.testing

import com.frisboo.corebanking.persistence.core.constants.POSTGRESQL_LATEST_IMAGE
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.v1.jdbc.Database
import org.testcontainers.containers.PostgreSQLContainer
import javax.sql.DataSource

data class PostgreSQLPoolProps(
    val maximumPoolSize: Int = 10,
    val minimumIdle: Int = 2,
)

class PostgreSQLTestFixture(
    postgreSQLImage: String = POSTGRESQL_LATEST_IMAGE,
    private val databaseName: String = "testdb",
    private val username: String = "postgres",
    private val password: String = "postgres",
    private val poolProps: PostgreSQLPoolProps = PostgreSQLPoolProps(),
) {

    private val container: PostgreSQLContainer<*> = PostgreSQLContainer(postgreSQLImage).apply {
        withDatabaseName(databaseName)
        withUsername(username)
        withPassword(password)
        withReuse(true)
    }

    /**
     * Mutex to ensure thread-safe operations when starting and stopping the container.
     */
    private val mutex = Mutex()

    @Volatile
    private var started: Boolean = false

    /**
     * HikariDataSource instance that will be initialized after the container starts.
     * It will be used to manage database connections for testing purposes.
     */
    private lateinit var dataSource: HikariDataSource

    /**
     * Provides the JDBC URL for connecting to the PostgreSQL database.
     * This URL is obtained from the Testcontainers PostgreSQL container instance.
     */
    val jdbcUrl: String get() = container.jdbcUrl

    fun getDataSource(): DataSource {
        require(started) { "Fixture must be started before accessing the DataSource" }
        return dataSource
    }

    /**
     * Starts the PostgreSQL container and initializes the HikariDataSource with the container's connection details.
     */
    suspend fun start() = mutex.withLock {
        if (started) return@withLock

        container.start()
        dataSource = HikariDataSource(
            HikariConfig().apply {
                jdbcUrl = container.jdbcUrl
                username = container.username
                password = container.password
                maximumPoolSize = poolProps.maximumPoolSize
                minimumIdle = poolProps.minimumIdle
            },
        )
        Database.connect(dataSource)
        started = true
    }

    /**
     * Stops the PostgreSQL container and closes the HikariDataSource.
     */
    suspend fun stop() = mutex.withLock {
        if (!started) return@withLock

        dataSource.close()
        container.stop()
        started = false
    }

    /**
     * Runs Flyway migrations against the PostgreSQL database using the provided migration script locations.
     *
     * @param locations Vararg of migration script locations to be applied to the database.
     */
    fun migrate(vararg locations: String) {
        check(started) { "Fixture must be started before migration" }
        require(locations.isNotEmpty()) { "Migration locations must not be empty" }

        Flyway.configure()
            .dataSource(dataSource)
            .locations(*locations)
            .load()
            .migrate()
    }
}
