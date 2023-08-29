package com.example.appnotepad.repository

import com.example.appnotepad.entity.Task
import org.springframework.data.repository.CrudRepository
import java.util.*

interface TaskRepository : CrudRepository<Task, UUID> {
}