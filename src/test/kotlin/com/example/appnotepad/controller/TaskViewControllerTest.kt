package com.example.appnotepad.controller

import com.example.appnotepad.dto.TaskDTO
import com.example.appnotepad.repository.postgres
import org.assertj.core.api.Assertions.assertThat
import org.flywaydb.core.Flyway
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.ui.ModelMap
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@SpringBootTest
@Testcontainers
internal class TaskViewControllerTest {

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
    private lateinit var taskViewController: TaskViewController

    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate

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
    fun getTasksBySortAndFilter() {
        val model = ModelMap()
        val page = 0
        val field = "name"
        val result = taskViewController.getTasksBySortAndFilter(model, page, field, null, null, null, null)
        assertEquals("view", result)

        assertThat(model).containsKey("tasks")
        assertThat(model).containsKey("totalPages")
        assertThat(model).containsKey("page")
        assertThat(model).containsKey("field")

        val tasksAttribute = model["tasks"] as List<TaskDTO>
        assertThat(tasksAttribute).isNotEmpty
        assertThat(tasksAttribute.size).isEqualTo(10)

        val totalPagesAttribute = model["totalPages"] as Int
        assertThat(totalPagesAttribute).isEqualTo(2)

        val pageAttribute = model["page"] as Int
        assertThat(pageAttribute).isEqualTo(page)

        val fieldAttribute = model["field"] as String
        assertThat(fieldAttribute).isEqualTo(field)

    }

    @Test
    fun getTasksBySortAndFilterWithFragment() {
        val model = ModelMap()
        val page = 0
        val field = "name"
        val result = taskViewController.getTasksBySortAndFilter(model, page, field, null, null, null, "hx-req")
        assertEquals("tasksTableFragment", result)

        assertThat(model).containsKey("tasks")
        assertThat(model).containsKey("totalPages")
        assertThat(model).containsKey("page")
        assertThat(model).containsKey("field")

        val tasksAttribute = model["tasks"] as List<TaskDTO>
        assertThat(tasksAttribute).isNotEmpty
        assertThat(tasksAttribute.size).isEqualTo(10)

        val totalPagesAttribute = model["totalPages"] as Int
        assertThat(totalPagesAttribute).isEqualTo(2)

        val pageAttribute = model["page"] as Int
        assertThat(pageAttribute).isEqualTo(page)

        val fieldAttribute = model["field"] as String
        assertThat(fieldAttribute).isEqualTo(field)
    }

}