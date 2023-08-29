package com.example.appnotepad.controller

import com.example.appnotepad.dto.TaskDTO
import com.example.appnotepad.service.TaskService
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping

@Controller
class TaskViewController(val taskService: TaskService) {


    @GetMapping("/")
    fun view(model: Model): String {
        model.addAttribute("tasks", taskService.findTasks())
        return "view"
    }
}