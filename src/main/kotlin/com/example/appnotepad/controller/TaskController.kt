package com.example.appnotepad.controller

import com.example.appnotepad.entity.Task
import com.example.appnotepad.entity.enum.Status
import com.example.appnotepad.service.TaskService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/tasks")
class TaskController(private val taskService: TaskService) {
    @PostMapping
    fun createTask(@RequestBody task: Task): ResponseEntity<Task> {
        return ResponseEntity.ok(taskService.createTask(task))
    }

    @PutMapping("/{id}")
    fun updateTask(@PathVariable id: UUID, @RequestBody task: Task): ResponseEntity<Task> {
        return ResponseEntity.ok(taskService.updateTask(id, task))
    }

    @PatchMapping("/{id}")
    fun updateStatus(@PathVariable id: UUID, @RequestParam status: Status): ResponseEntity<Task> {
        return ResponseEntity.ok(taskService.updateStatus(id, status))
    }

    @DeleteMapping("/{id}")
    fun deleteTask(@PathVariable id: UUID): ResponseEntity<String> {
        return ResponseEntity.ok(taskService.deleteTask(id))
    }

}