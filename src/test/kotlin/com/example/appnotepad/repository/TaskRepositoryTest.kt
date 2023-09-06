package com.example.appnotepad.repository

import com.example.appnotepad.entity.Task
import org.flywaydb.core.Flyway
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.data.relational.core.conversion.DbActionExecutionException
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.JdbcDatabaseContainer
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName

fun postgres(imageName: String, opts: JdbcDatabaseContainer<Nothing>.() -> Unit) = PostgreSQLContainer<Nothing>(
    DockerImageName.parse(imageName)
).apply(opts)

@Testcontainers
@DataJdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
internal class TaskRepositoryTest {


    @Autowired
    private lateinit var taskRepository: TaskRepository

    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate

    companion object {
        @Container
        val container = postgres("postgres:13-alpine") {
            withDatabaseName("notepadDB")
            withUsername("postgres")
            withPassword("1234")
        }

        @JvmStatic
        @DynamicPropertySource
        fun dataSourceConfig(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", container::getJdbcUrl)
            registry.add("spring.datasource.password", container::getPassword)
            registry.add("spring.datasource.username", container::getUsername)
        }
    }

    @BeforeEach
    fun setup() {
        container.start()

        val flyway = Flyway.configure()
            .dataSource(container.jdbcUrl, container.username, container.password)
            .locations("classpath:db/migration")
            .load()
        flyway.migrate()
    }

    @Test
    fun containerIsRunning() {
        assertTrue(container.isRunning)
    }

    @Test
    fun testTableExist() {
        val tableExist = jdbcTemplate.queryForObject(
            "SELECT EXISTS (SELECT table_name FROM information_schema.tables WHERE table_name = 'tasks')",
            Boolean::class.java
        )
        assertEquals(true, tableExist)
    }

    @Test
    fun migrationIsSuccessful() {
        val countMigrations = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM flyway_schema_history",
            Int::class.java
        )
        assertNotNull(countMigrations)
        assertEquals(countMigrations, 2)
    }

    @Test
    fun createAndReadTask() {
        val nameTask = "TestTask"
        val task = Task(null, nameTask)
        val receivedTask = taskRepository.save(task)

        val savedTask = taskRepository.findById(receivedTask.id!!)
        assertEquals(nameTask, savedTask.get().name)
    }

    @Test
    fun updateTask() {
        val nameTask = "TestTask"
        val task = Task(null, nameTask, "Original task")
        val receivedTask = taskRepository.save(task)

        val newDescription = "Updated Task"
        val updatedTask = taskRepository.findById(receivedTask.id!!).get().copy(description = newDescription)

        val savedTask = taskRepository.save(updatedTask)
        assertEquals(receivedTask.id, savedTask.id)
        assertEquals(newDescription, savedTask.description)
    }


    @Test
    fun deleteTask() {
        val task = Task(null, "TaskToDelete")
        val savedTask = taskRepository.save(task)

        taskRepository.deleteById(savedTask.id!!)

        val deletedTask = taskRepository.findById(savedTask.id!!)
        assertEquals(false, deletedTask.isPresent)

    }

    @Test
    fun saveTaskWithDuplicateName() {
        val uniqueName = "UniqueName"
        val taskWithUniqueName = Task(null, uniqueName)
        taskRepository.save(taskWithUniqueName)

        val taskWithDuplicateName = Task(null, uniqueName)

        assertThrows(DbActionExecutionException::class.java) {
            taskRepository.save(taskWithDuplicateName)
        }
    }

/*

    @Test
    fun findAll() {
        val pageable = PageRequest.of(0, 10, Sort.by("name"))
        val tasks = taskRepository.findAll().toList().stream().sorted { o1, o2 -> o1.name.compareTo(o2.name) }.toList()
        val page = taskRepository.findAll(pageable)

        assertEquals(10, page.content.size)
        assertEquals(page.content[0].name, tasks[0].name)

    }
*/


}