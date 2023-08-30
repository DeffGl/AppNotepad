package com.example.appnotepad.service

import com.example.appnotepad.dto.TaskDTO
import com.example.appnotepad.entity.Task
import com.example.appnotepad.entity.enum.Status
import com.example.appnotepad.repository.TaskRepository
import com.example.appnotepad.util.exceptions.*
import org.springframework.data.relational.core.conversion.DbActionExecutionException
import org.springframework.stereotype.Service
import java.rmi.UnexpectedException
import java.time.LocalDateTime
import java.util.*

@Service
class TaskService(val taskRepository: TaskRepository) {

    fun findAllTasks(): List<TaskDTO> = taskRepository.findAll().toList().map { task ->
        TaskDTO(
            task.name,
            task.description,
            task.createdDate.toLocalDate(),
            task.modifiedDate?.toLocalDate(),
            if (task.status == Status.COMPLETED) Status.COMPLETED.status else Status.INCOMPLETE.status
        )
    }

    fun createTask(createdTask: Task): Task {
        return saveTask(createdTask){
                task -> taskRepository.save(task)
        }
    }

    fun updateTask(id: UUID, updatedTask: Task): Task {
        val newTask = copyTask(id, updatedTask)
        if (newTask.status == Status.COMPLETED) throw TaskNotUpdatedException("Нельзя редактировать выполненную задачу")
        return saveTask(newTask){
            task -> taskRepository.save(task)
        }
    }

    fun updateStatus(id: UUID, newStatus: Status): Task? {
        val newTask = findTask(id).copy(
            status = newStatus
        )
        return taskRepository.save(newTask)
    }

    fun deleteTask(id: UUID): String {
        if (findTask(id).status == Status.COMPLETED) throw TaskNotDeleteException("Нельзя удалить выполненную задачу")
        taskRepository.deleteById(id)
        return "Задача успешно удалена"
    }

    fun findTask(id: UUID): Task {
        val task: Optional<Task> = taskRepository.findById(id)
        if (task.isEmpty) {
            throw TaskNotFoundException("Задача не найдена с id: $id")
        }
        return task.get()
    }

    private fun copyTask(id: UUID, updatedTask: Task): Task{
        return findTask(id).copy(
            name = updatedTask.name,
            description = updatedTask.description,
            modifiedDate = LocalDateTime.now()
        )
    }

    private fun saveTask(task: Task, action: (Task) -> Task): Task{
        try {
            return action(task)
        } catch (ex: DbActionExecutionException) {
            throw TaskNotSaveDbException("Не удалось сохранить/отредактировать задачу. Возможно задача с таким именем уже существует", ex);
        } catch (ex: Exception){
            throw UnexpectedException("Произошла непредвиденная ошибка при редактировании задачи", ex)
        }
    }
}