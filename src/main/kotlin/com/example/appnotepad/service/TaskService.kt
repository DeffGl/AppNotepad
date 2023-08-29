package com.example.appnotepad.service

import com.example.appnotepad.dto.TaskDTO
import com.example.appnotepad.entity.Task
import com.example.appnotepad.entity.enum.Status
import com.example.appnotepad.repository.TaskRepository
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.*

@Service
class TaskService(val taskRepository: TaskRepository) {
    fun findTasks(): List<TaskDTO> = taskRepository.findAll().toList().map { task ->
        TaskDTO(
            task.name,
            task.description,
            task.createdDate.toLocalDate(),
            task.modifiedDate?.toLocalDate(),
            if (task.completed) Status.COMPLETED.value else Status.INCOMPLETE.value
        )
    }

    fun createTask(createdTask: Task): Task {
        return taskRepository.save(createdTask)
    }
}