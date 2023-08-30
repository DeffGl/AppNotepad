package com.example.appnotepad.controller

import com.example.appnotepad.service.TaskService
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/tasks")
class TaskViewController(val taskService: TaskService) {

    @GetMapping
    fun viewTasks(model: Model): String {
        model.addAttribute("tasks", taskService.findAllTasks())
        return "view"
    }
}