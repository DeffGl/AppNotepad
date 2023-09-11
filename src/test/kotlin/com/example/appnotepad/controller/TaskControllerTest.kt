package com.example.appnotepad.controller


import com.example.appnotepad.entity.Task
import com.example.appnotepad.entity.enum.Status
import com.example.appnotepad.repository.TaskRepository
import com.example.appnotepad.repository.postgres
import io.restassured.http.ContentType
import io.restassured.module.mockmvc.RestAssuredMockMvc
import io.restassured.module.mockmvc.RestAssuredMockMvc.given
import org.flywaydb.core.Flyway
import org.hamcrest.CoreMatchers.equalTo
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.util.*

@SpringBootTest
@Testcontainers
internal class TaskControllerTest {

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

    @Autowired
    private lateinit var webApplicationContext: WebApplicationContext

    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate

    @Autowired
    private lateinit var taskRepository: TaskRepository

    @BeforeEach
    fun setup() {
        container.start()

        val flyway = Flyway.configure()
            .dataSource(container.jdbcUrl, container.username, container.password)
            .locations("classpath:db/migration")
            .load()
        flyway.migrate()

        RestAssuredMockMvc.mockMvc(MockMvcBuilders.standaloneSetup(TaskController::class, TaskExceptionController::class).build())
        RestAssuredMockMvc.webAppContextSetup(webApplicationContext)
    }

    @AfterEach
    fun cleanup() {
        jdbcTemplate.execute("TRUNCATE TABLE tasks")
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
    fun createTask() {
        val requestBody = mapOf(
            "name" to "testTask",
            "description" to "testDesc"
        )
        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
            .`when`()
            .post("/api/tasks")
            .then()
            .log().ifValidationFails()
            .statusCode(HttpStatus.OK.value())
            .body("name", equalTo(requestBody["name"]))
            .body("description", equalTo(requestBody["description"]))
    }


    @Test
    fun createTask_testTaskNotSaveDbException(){
        val task = Task(null, "testTask", "testDesc")
        taskRepository.save(task)

        val requestBody = mapOf(
            "name" to task.name,
            "description" to task.description
        )
        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
            .`when`()
            .post("/api/tasks")
            .then()
            .log().ifValidationFails()
            .statusCode(HttpStatus.BAD_REQUEST.value())
    }

    @Test
    fun updateTask() {
        val task = Task(null, "testTask", "testDesc")
        val savedTask = taskRepository.save(task)
        val id = savedTask.id

        val requestBodyForUpdate = mapOf(
            "name" to "updateTestTask",
            "description" to "updateTestDesc"
        )

        given()
            .contentType(ContentType.JSON)
            .body(requestBodyForUpdate)
            .`when`()
            .put("/api/tasks/${id}")
            .then()
            .log().ifValidationFails()
            .statusCode(HttpStatus.OK.value())
            .body("id", equalTo(id.toString()))
            .body("name", equalTo(requestBodyForUpdate["name"]))
            .body("description", equalTo(requestBodyForUpdate["description"]))

    }


    @Test
    fun updateTask_testTaskNotUpdatedException() {
        val task = Task(null, "testTask", "testDesc", status = Status.COMPLETED)
        val savedTask = taskRepository.save(task)
        val id = savedTask.id

        val requestBodyForUpdate = mapOf(
            "name" to "updateTestTask",
            "description" to "updateTestDesc"
        )

        given()
            .contentType(ContentType.JSON)
            .body(requestBodyForUpdate)
            .`when`()
            .put("/api/tasks/${id}")
            .then()
            .log().ifValidationFails()
            .statusCode(HttpStatus.BAD_REQUEST.value())
    }

    @Test
    fun updateTask_testTaskNotFoundException() {
        val requestBodyForUpdate = mapOf(
            "name" to "updateTestTask",
            "description" to "updateTestDesc"
        )

        given()
            .contentType(ContentType.JSON)
            .body(requestBodyForUpdate)
            .`when`()
            .put("/api/tasks/${UUID.randomUUID()}") //передаем несуществующий id
            .then()
            .log().ifValidationFails()
            .statusCode(HttpStatus.NOT_FOUND.value())
    }

    @Test
    fun updateTask_testTaskNotSaveDbException() {
        val task = Task(null, "testTask", "testDesc")
        val taskForUpdate = Task(null, "testTaskForUpdate", "testDescForUpdate")

        taskRepository.save(task)
        val savedTaskForUpdate = taskRepository.save(taskForUpdate)

        val id = savedTaskForUpdate.id

        val requestBodyForUpdate = mapOf(
            "name" to task.name,
            "description" to task.description
        )

        given()
            .contentType(ContentType.JSON)
            .body(requestBodyForUpdate)
            .`when`()
            .put("/api/tasks/${id}")
            .then()
            .log().ifValidationFails()
            .statusCode(HttpStatus.BAD_REQUEST.value())
    }

    @Test
    fun updateStatus() {
        val task = Task(null, "testTask", "testDesc")
        val savedTask = taskRepository.save(task)
        val id = savedTask.id

        given()
            .queryParam("status", Status.COMPLETED)
            .`when`()
            .patch("/api/tasks/${id}")
            .then()
            .log().ifValidationFails()
            .statusCode(HttpStatus.OK.value())
            .body("id", equalTo(id.toString()))
            .body("name", equalTo(task.name))
            .body("description", equalTo(task.description))
            .body("status", equalTo(Status.COMPLETED.status))
    }

    @Test
    fun updateStatus_testIllegalArgumentException() {
        val task = Task(null, "testTask", "testDesc")
        val savedTask = taskRepository.save(task)
        val id = savedTask.id

        given()
            .queryParam("status", "Status") //передаем некорректный статус
            .`when`()
            .patch("/api/tasks/${id}")
            .then()
            .log().ifValidationFails()
            .statusCode(HttpStatus.BAD_REQUEST.value())
    }

    @Test
    fun deleteTask() {
        val task = Task(null, "testTask", "testDesc")
        val savedTask = taskRepository.save(task)
        val id = savedTask.id
        given()
            .`when`()
            .delete("/api/tasks/${id}")
            .then()
            .log().ifValidationFails()
            .statusCode(HttpStatus.OK.value())

        assertTrue(taskRepository.findById(id!!).isEmpty)
    }

    @Test
    fun deleteTask_testTaskNotDeleteException() {
        val task = Task(null, "testTask", "testDesc", status = Status.COMPLETED)
        val savedTask = taskRepository.save(task)
        val id = savedTask.id

        given()
            .`when`()
            .delete("/api/tasks/${id}")
            .then()
            .log().ifValidationFails()
            .statusCode(HttpStatus.BAD_REQUEST.value())

    }
}