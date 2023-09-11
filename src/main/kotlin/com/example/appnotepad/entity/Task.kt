package com.example.appnotepad.entity

import com.example.appnotepad.entity.enum.Status
import io.swagger.v3.oas.annotations.media.Schema
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime
import java.util.*

@Table("tasks")
data class Task(
    @Id
    @Schema(readOnly = true)
    val id: UUID? = null,
    val name: String,
    val description: String? = null,
    @Schema(readOnly = true)
    val createdDate: LocalDateTime = LocalDateTime.now(),
    @Schema(readOnly = true)
    val modifiedDate: LocalDateTime? = null,
    @Schema(readOnly = true)
    val status: Status = Status.INCOMPLETE
    )
