package com.example.appnotepad.dto

import com.example.appnotepad.entity.enum.Status
import java.time.LocalDateTime

data class TaskDTO (val name: String,
                    val description: String?,
                    val createdDate: LocalDateTime,
                    val modifiedDate: LocalDateTime?,
                    val status: Status
)