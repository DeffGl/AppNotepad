package com.example.appnotepad.service

import com.example.appnotepad.actuator.TaskMetricsEndpoint
import com.example.appnotepad.dto.TaskDTO
import com.example.appnotepad.entity.Task
import com.example.appnotepad.entity.enum.Status
import com.example.appnotepad.repository.TaskRepository
import com.example.appnotepad.util.exceptions.TaskNotDeleteException
import com.example.appnotepad.util.exceptions.TaskNotFoundException
import com.example.appnotepad.util.exceptions.TaskNotSaveDbException
import com.example.appnotepad.util.exceptions.TaskNotUpdatedException
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.relational.core.conversion.DbActionExecutionException
import org.springframework.stereotype.Service
import java.rmi.UnexpectedException
import java.time.LocalDateTime
import java.util.*

@Service
class TaskServiceImpl(private val taskRepository: TaskRepository, private val taskMetricsEndpoint: TaskMetricsEndpoint) : TaskService {

    override fun findAllFilteredTasks(page: Int, field: String, name: String?, description: String?, status: String?): Page<TaskDTO> {
        val pageable: Pageable = PageRequest.of(page, 10, Sort.by(field))
        return if (name.isNullOrBlank() && description.isNullOrBlank() && (status.isNullOrBlank() || status == "all"))
            taskRepository.findAll(pageable)
        else if (status == "all") taskRepository.findAllByNameContainingIgnoreCaseAndDescriptionContainingIgnoreCase(
            name,
            description,
            pageable)
        else taskRepository.findAllByNameContainingIgnoreCaseAndDescriptionContainingIgnoreCaseAndStatusEquals(
            name,
            description,
            if (status == "completed") Status.COMPLETED else Status.INCOMPLETE,
            pageable)
    }


    override fun createTask(createdTask: Task): Task {
        val task: Task = saveTask(createdTask) { task ->
            taskRepository.save(task)
        }
        taskMetricsEndpoint.incrementCreatedTasks()
        return task
    }

    override fun updateTask(id: UUID, updatedTask: Task): Task {
        val newTask = copyTask(id, updatedTask)
        if (newTask.status == Status.COMPLETED) throw TaskNotUpdatedException("Нельзя редактировать выполненную задачу")
        val task: Task = saveTask(newTask) { task ->
            taskRepository.save(task)
        }
        taskMetricsEndpoint.incrementModifiedTasks()
        return task
    }

    override fun updateStatus(id: UUID, newStatus: Status): Task? {
        val newTask = findTask(id).copy(
            status = newStatus
        )
        return taskRepository.save(newTask)
    }

    override fun deleteTask(id: UUID): String {
        if (findTask(id).status == Status.COMPLETED) throw TaskNotDeleteException("Нельзя удалить выполненную задачу")
        taskRepository.deleteById(id)
        taskMetricsEndpoint.incrementDeletedTasks()
        return "Задача успешно удалена"
    }

    override fun findTask(id: UUID): Task {
        return taskRepository.findById(id).orElseThrow {
            throw TaskNotFoundException("Задача не была найдена с id: $id")
        }
    }

    override fun copyTask(id: UUID, updatedTask: Task): Task {
        return findTask(id).copy(
            name = updatedTask.name,
            description = updatedTask.description,
            modifiedDate = LocalDateTime.now()
        )
    }

    override fun saveTask(task: Task, action: (Task) -> Task): Task {
        try {
            return action(task)
        } catch (ex: DbActionExecutionException) {
            throw TaskNotSaveDbException(
                "Не удалось сохранить/отредактировать задачу. Возможно задача с таким именем уже существует",
                ex
            );
        } catch (ex: Exception) {
            throw UnexpectedException("Произошла непредвиденная ошибка при редактировании задачи", ex)
        }
    }
}