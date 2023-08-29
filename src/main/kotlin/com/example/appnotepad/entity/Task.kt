package com.example.appnotepad.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime
import java.util.*

@Table("tasks")
data class Task(
    @Id
    val id: UUID? = null,
    val name: String,
    val description: String? = null,
    val createdDate: LocalDateTime = LocalDateTime.now(),
    val modifiedDate: LocalDateTime? = null,
    val completed: Boolean = false
    )