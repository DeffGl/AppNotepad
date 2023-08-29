package com.example.appnotepad.dto

import com.example.appnotepad.entity.enum.Status
import java.time.LocalDate

data class TaskDTO (val name: String,
                    val description: String?,
                    val createdDate: LocalDate,
                    val modifiedDate: LocalDate?,
                    val completed: String
)