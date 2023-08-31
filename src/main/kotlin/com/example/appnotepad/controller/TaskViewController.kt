package com.example.appnotepad.controller

import com.example.appnotepad.dto.TaskDTO
import com.example.appnotepad.service.TaskServiceImpl
import org.springframework.data.domain.Page
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam

@Controller
@RequestMapping("/tasks")
class TaskViewController(private val taskServiceImpl: TaskServiceImpl) {

    @GetMapping
    fun viewTasks(model: Model, @RequestParam page: Int): String {
        val taskPage: Page<TaskDTO> = taskServiceImpl.findAllTasks(page)
        model.addAttribute("tasks", taskPage.content)
        model.addAttribute("totalPages", taskPage.totalPages)
        model.addAttribute("page", page)
        return "view"
    }
}