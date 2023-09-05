package com.example.appnotepad.controller

import com.example.appnotepad.dto.TaskDTO
import com.example.appnotepad.service.TaskServiceImpl
import jakarta.servlet.http.HttpServletRequest
import org.springframework.data.domain.Page
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam

@Controller
@RequestMapping("/tasks")
class TaskViewController(private val taskServiceImpl: TaskServiceImpl) {

    @GetMapping()
    fun getUpdateFragmentAndFilter(
        model: Model,
        @RequestParam(required = false, defaultValue = "0") page: Int,
        @RequestParam(required = false, defaultValue = "name") field: String,
        @RequestParam(required = false) name: String? = null,
        @RequestParam(required = false) description: String? = null,
        @RequestParam(required = false) status: String? = null,
        @RequestBody(required = false) fieldd: String?,
        request: HttpServletRequest
    ): String {

        val taskPage: Page<TaskDTO> = taskServiceImpl.findAllFilteredTasks(page, field, name, description, status)
        model.addAttribute("tasks", taskPage.content)
        model.addAttribute("totalPages", taskPage.totalPages)
        model.addAttribute("page", page)
        model.addAttribute("field", field)
        return if (request.getHeader("HX-Request") != null) {
            "tasksTableFragment"
        } else {
            "view"
        }
    }

}