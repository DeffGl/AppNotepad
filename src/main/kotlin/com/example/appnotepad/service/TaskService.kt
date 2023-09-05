package com.example.appnotepad.service

import com.example.appnotepad.dto.TaskDTO
import com.example.appnotepad.entity.Task
import com.example.appnotepad.entity.enum.Status
import org.springframework.data.domain.Page
import java.util.*

interface TaskService {
    fun createTask(createdTask: Task): Task
    fun updateTask(id: UUID, updatedTask: Task): Task
    fun updateStatus(id: UUID, newStatus: Status): Task?
    fun deleteTask(id: UUID): String
    fun findTask(id: UUID): Task
    fun copyTask(id: UUID, updatedTask: Task): Task
    fun saveTask(task: Task, action: (Task) -> Task): Task
    fun findAllFilteredTasks(page: Int, field: String, name: String?, description: String?, status: String?): Page<TaskDTO>
}