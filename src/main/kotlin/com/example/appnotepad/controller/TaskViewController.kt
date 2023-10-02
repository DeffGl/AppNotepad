package com.example.appnotepad.controller

import com.example.appnotepad.dto.TaskDTO
import com.example.appnotepad.service.TaskService
import org.springframework.data.domain.Page
import org.springframework.stereotype.Controller
import org.springframework.ui.ModelMap
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam

@Controller
@RequestMapping("/tasks")
class TaskViewController(private val taskService: TaskService) {

    @GetMapping()
    fun getTasksBySortAndFilter(
        model: ModelMap,
        @RequestParam(required = false, defaultValue = "0") page: Int,
        @RequestParam(required = false, defaultValue = "name") field: String,
        @RequestParam(required = false) name: String? = null,
        @RequestParam(required = false) description: String? = null,
        @RequestParam(required = false) status: String? = null,
        @RequestHeader(name = "HX-Request", required = false) hxRequestHeader: String?
    ): String {

        val taskPage: Page<TaskDTO> = taskService.findAllFilteredTasks(page, field, name, description, status)
        model.addAttribute("tasks", taskPage.content)
        model.addAttribute("totalPages", taskPage.totalPages)
        model.addAttribute("page", page)
        model.addAttribute("field", field)
        return if (hxRequestHeader != null) {
            "tasksTableFragment"
        } else {
            "view"
        }
    }
}