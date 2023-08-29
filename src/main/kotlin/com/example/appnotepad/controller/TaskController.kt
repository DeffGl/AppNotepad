package com.example.appnotepad.controller

import com.example.appnotepad.entity.Task
import com.example.appnotepad.service.TaskService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/tasks")
class TaskController(val taskService: TaskService) {
    @PostMapping("/create")
    fun createTask(@RequestBody task: Task): Task{
        return taskService.createTask(task)
    }
    @PutMapping("/{id}")
    fun updateTask(@RequestParam id: Long, @RequestBody task: Task){
        println("Привет: $id ${task.name}")
    }
    /*@PatchMapping
    @DeleteMapping*/
}