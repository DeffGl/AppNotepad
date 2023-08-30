package com.example.appnotepad.dto

import java.time.LocalDate

data class TaskDTO (val name: String,
                    val description: String?,
                    val createdDate: LocalDate,
                    val modifiedDate: LocalDate?,
                    val status: String
)