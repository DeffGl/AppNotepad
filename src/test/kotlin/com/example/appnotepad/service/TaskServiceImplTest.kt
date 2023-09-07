package com.example.appnotepad.service

import com.example.appnotepad.dto.TaskDTO
import com.example.appnotepad.entity.Task
import com.example.appnotepad.entity.enum.Status
import com.example.appnotepad.repository.TaskRepository
import com.example.appnotepad.repository.postgres
import com.example.appnotepad.util.exceptions.TaskNotDeleteException
import com.example.appnotepad.util.exceptions.TaskNotFoundException
import com.example.appnotepad.util.exceptions.TaskNotSaveDbException
import com.example.appnotepad.util.exceptions.TaskNotUpdatedException
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.relational.core.conversion.DbActionExecutionException
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@Testcontainers
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
internal class TaskServiceImplTest {



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
    private lateinit var taskRepository: TaskRepository

    @Autowired
    private lateinit var taskService: TaskService

    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate

    @ParameterizedTest(name = "{0} - name: {1}, description: {2}, status: {3}")
    @CsvSource(
        "findCompletedTasksByNameAndDescription, Task, Description for task, completed",
        "findIncompleteTasksByNameAndDescription, Task, Description for task, incomplete"
    )
    fun findAllTasksByStatus(testName: String, inputName: String?, inputDescription: String?, status: String?) {
        val field = "name"
        val pageable = PageRequest.of(0, 10, Sort.by("name"))
        val completedTaskByNameAndDescription = taskRepository.findAllByNameContainingIgnoreCaseAndDescriptionContainingIgnoreCaseAndStatusEquals(inputName,
            inputDescription, if (status == "completed") Status.COMPLETED else Status.INCOMPLETE, pageable).map { mapToTaskDTO(it) }
        val foundAllTasks = taskService.findAllFilteredTasks(0, field, inputName, inputDescription, status)

        assertTrue(isPagesEqual(completedTaskByNameAndDescription, foundAllTasks))
    }


    @ParameterizedTest(name = "{0} - name: {1}, description: {2}, status: {3}")
    @CsvSource(
        "findAllTasksByDescription, '', Description for task, ",
        "findAllTasksByDescriptionAndEmptyStatus, '', Description for task, ''",
        "findAllTasksByDescriptionAndStatusAll, '', Description for task, all"
    )
    fun testFindAllTasksByDescription(testName: String, inputName: String?, inputDescription: String?, status: String?){
        val field = "name"
        val pageable = PageRequest.of(0, 10, Sort.by(field))
        val allTaskByDescription = taskRepository.findAllByNameContainingIgnoreCaseAndDescriptionContainingIgnoreCase("", "Description for task", pageable).map { mapToTaskDTO(it) }
        val foundAllTasks = taskService.findAllFilteredTasks(0, field, inputName, inputDescription, status)

        assertTrue(isPagesEqual(allTaskByDescription, foundAllTasks))
    }

    @ParameterizedTest(name = "{0} - name: {1}, description: {2}, status: {3}")
    @CsvSource(
        "findAllTasksByName, Task, '', ",
        "findAllTasksByNameAndEmptyStatus, Task, '', ''",
        "findAllTasksByNameAndStatusAll, Task, '', all"
    )
    fun testFindAllTasksByName(testName: String, inputName: String?, inputDescription: String?, status: String?){
        val field = "name"
        val pageable = PageRequest.of(0, 10, Sort.by(field))
        val allTaskByName = taskRepository.findAllByNameContainingIgnoreCaseAndDescriptionContainingIgnoreCase("Task", "", pageable).map { mapToTaskDTO(it) }
        val foundAllTasks = taskService.findAllFilteredTasks(0, field, inputName, inputDescription, status)

        assertTrue(isPagesEqual(allTaskByName, foundAllTasks))
    }

    @ParameterizedTest(name = "{0} - name: {1}, description: {2}, status: {3}")
    @CsvSource(
        "findAllTasksByNameAndDescription, Task, Description for task, ",
        "findAllTasksByNameAndDescriptionAndEmptyStatus, Task, Description for task, ''",
        "findAllTasksByNameAndDescriptionAndStatusAll, Task, Description for task, all"
    )
    fun testFindAllTasksByNameAndDescription(testName: String, inputName: String?, inputDescription: String?, status: String?){
        val field = "name"
        val pageable = PageRequest.of(0, 10, Sort.by(field))
        val allTaskByNameAndDescription = taskRepository.findAllByNameContainingIgnoreCaseAndDescriptionContainingIgnoreCase("Task", "Description for task", pageable).map { mapToTaskDTO(it) }
        val foundAllTasks = taskService.findAllFilteredTasks(0, field, inputName, inputDescription, status)

        assertTrue(isPagesEqual(allTaskByNameAndDescription, foundAllTasks))
    }



    @ParameterizedTest(name = "{0} - name: {1}, description: {2}, status: {3}")
    @CsvSource(
        "findAllTasks, , , ",
        "findAllTasksByBlanksArgs, '', '', ''",
        "findAllTasksByStatusAll, , , all",
        "findAllTasksByBlankArgsAndStatusAll, '', '', all"
    )
    fun testFindAllTasks(testName: String, inputName: String?, inputDescription: String?, status: String?){
        val field = "name"
        val pageable = PageRequest.of(0, 10, Sort.by(field))
        val allTasks = taskRepository.findAll(pageable).map { mapToTaskDTO(it) }
        val foundAllTasks = taskService.findAllFilteredTasks(0, field, inputName, inputDescription, status)

        assertTrue(isPagesEqual(allTasks, foundAllTasks))
    }

    @ParameterizedTest(name = "{0} - name: {1}, description: {2}, status: {3}")
    @CsvSource(
        "findByNameAndStatusAll, Task, , all",
        "findByNameAndEmptyStatus, Task, , ''",
        "findByNameAndStatusCompleted, Task, , completed",
        "findByNameAndStatusIncomplete, Task, , incomplete",
        "findByDescriptionAndStatusAll, , Description for task, all",
        "findByDescriptionAndEmptyStatus, , Description for task, ''",
        "findByDescriptionAndStatusCompleted, , Description for task, completed",
        "findByDescriptionAndStatusIncomplete, , Description for task, incomplete",
        "findByStatusCompleted, , , completed",
        "findByStatusIncomplete, , , incomplete"
    )
    fun findAllFilteredTasks_testIllegalArgumentException(testName: String, inputName: String?, inputDescription: String?, status: String?){
        assertThrows(IllegalArgumentException::class.java) {
            val field = "name"
            taskService.findAllFilteredTasks(0, field, inputName, inputDescription, status)
        }
    }

    fun isPagesEqual(expected: Page<TaskDTO>, actual: Page<TaskDTO>): Boolean {
        if (expected.totalElements != actual.totalElements ||
            expected.totalPages != actual.totalPages ||
            expected.number != actual.number ||
            expected.size != actual.size ||
            expected.content.size != actual.content.size) {
            return false
        }

        for (i in expected.content.indices) {
            val expectedTask = expected.content[i]
            val actualTask = actual.content[i]
            if (expectedTask.name != actualTask.name ||
                expectedTask.description != actualTask.description ||
                expectedTask.createdDate != actualTask.createdDate ||
                expectedTask.modifiedDate != actualTask.modifiedDate ||
                expectedTask.status != actualTask.status) {
                return false
            }
        }
        return true
    }

    fun mapToTaskDTO(task: Task): TaskDTO {
        return TaskDTO(
            task.name,
            task.description,
            task.createdDate,
            task.modifiedDate,
            task.status
        )
    }

    @Test
    fun createTaskAnd_testTaskNotSaveDbException() {
        val taskName = "Task"
        val task  = Task(null, taskName)
        val taskWithDuplicateName = Task(null, taskName)

        val createdTask = taskService.createTask(task)
        val foundTask = taskRepository.findById(createdTask.id!!)

        assertEquals(task.name, foundTask.get().name)
        assertThrows(TaskNotSaveDbException::class.java){
            taskService.createTask(taskWithDuplicateName)
        }
    }

    @Test
    fun updateTask() {
        val task  = Task(null, "Task", "Original Description")
        val newDescription = "Updated Description"

        val createdTask = taskRepository.save(task)
        val updatedTask = taskRepository.findById(createdTask.id!!).get().copy(description = newDescription)
        val savedTask = taskService.updateTask(updatedTask.id!!, updatedTask)

        assertEquals(createdTask.id, savedTask.id)
        assertEquals(newDescription, savedTask.description)
    }

    @Test
    fun updateTask_testTaskNotUpdatedException(){
        val task  = Task(null, "Task", "Original Description", status = Status.COMPLETED)
        val newDescription = "Updated Description"

        val createdTask = taskRepository.save(task)
        val updatedTask = taskRepository.findById(createdTask.id!!).get().copy(description = newDescription)
        assertThrows(TaskNotUpdatedException::class.java){
           taskService.updateTask(updatedTask.id!!, updatedTask)
        }

    }

    @Test
    fun updateStatus() {
        val status = Status.INCOMPLETE
        val task  = Task(null, "Task", "Original Description", status = status)

        val createdTask = taskRepository.save(task)
        val updatedTask = taskService.updateStatus(createdTask.id!!, Status.COMPLETED)

        assertNotEquals(status, updatedTask?.status)
    }

    @Test
    fun deleteTask() {
        val task = Task(null, "TaskToDelete")
        val savedTask = taskRepository.save(task)

        taskService.deleteTask(savedTask.id!!)

        val deletedTask = taskRepository.findById(savedTask.id!!)
        assertEquals(false, deletedTask.isPresent)

    }

    @Test
    fun deleteTask_testTaskNotDeleteException(){
        val task  = Task(null, "Task", "Original Description", status = Status.COMPLETED)
        val savedTask = taskRepository.save(task)

        assertThrows(TaskNotDeleteException::class.java){
            taskService.deleteTask(savedTask.id!!)
        }
    }

    @Test
    fun findTask() {
        val task  = Task(null, "Task")
        val savedTask = taskRepository.save(task)

        val foundTask = taskService.findTask(savedTask.id!!)
        assertEquals(savedTask.id, foundTask.id)
    }

    @Test
    fun findTask_testTaskNotFoundException(){
        val task  = Task(null, "Task")
        val savedTask = taskRepository.save(task)
        taskRepository.deleteById(savedTask.id!!)

        assertThrows(TaskNotFoundException::class.java){
            taskService.findTask(savedTask.id!!)
        }
    }

    @Test
    fun copyTask() {
        val task = Task(null, "Task")
        val savedTask = taskRepository.save(task)

        val updatedName = "updatedName"
        val updatedDescription = "updatedDescription"
        val updatedTask = Task (savedTask.id, updatedName, updatedDescription, savedTask.createdDate, savedTask.modifiedDate, savedTask.status)
        val copiedTask = taskService.copyTask(savedTask.id!!, updatedTask)

        assertTrue(isTasksEqual(updatedTask, copiedTask))
    }

    fun isTasksEqual(expected: Task, actual: Task): Boolean{
        if (expected.id != actual.id ||
            expected.name != actual.name ||
            expected.description != actual .description||
            expected.createdDate != actual.createdDate ||
            expected.status != actual.status){
            return false
        }
        return true
    }

    @Test
    fun saveTask() {
        val task = Task(null, "Task")
        val savedTask = taskService.saveTask(task){
            taskRepository.save(task)
        }
        val foundTask = taskRepository.findById(savedTask.id!!)
        assertEquals(savedTask.id, foundTask.get().id)
        assertEquals(savedTask.name, foundTask.get().name)
    }
}

