package com.example.appnotepad.repository

import com.example.appnotepad.entity.Task
import com.example.appnotepad.entity.enum.Status
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface TaskRepository : CrudRepository<Task, UUID> {
    fun findAll(pageable: Pageable): Page<Task>
    fun findAllByNameContainingIgnoreCaseAndDescriptionContainingIgnoreCaseAndStatusEquals(name: String?, description: String?, status: Status?, pageable: Pageable): Page<Task>
    fun findAllByNameContainingIgnoreCaseAndDescriptionContainingIgnoreCase(name: String?, description: String?, pageable: Pageable): Page<Task>
}