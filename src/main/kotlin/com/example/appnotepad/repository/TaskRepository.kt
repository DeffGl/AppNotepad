package com.example.appnotepad.repository

import com.example.appnotepad.dto.TaskDTO
import com.example.appnotepad.entity.Task
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface TaskRepository : CrudRepository<Task, UUID> {
    fun findAll(pageable: Pageable): Page<TaskDTO>
}