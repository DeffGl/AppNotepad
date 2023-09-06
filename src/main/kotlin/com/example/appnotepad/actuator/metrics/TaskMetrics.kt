package com.example.appnotepad.actuator.metrics

data class TaskMetrics(
    val createdTasks: Int,
    val modifiedTasks: Int,
    val deletedTasks: Int
)