package com.example.appnotepad.actuator

import com.example.appnotepad.actuator.metrics.TaskMetrics
import org.springframework.boot.actuate.endpoint.annotation.Endpoint
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation
import org.springframework.stereotype.Component

@Component
@Endpoint(id = "task-metrics")
class TaskMetricsEndpoint {
    private var createdTasks = 0
    private var modifiedTasks = 0
    private var deletedTasks = 0

    fun incrementCreatedTasks() {
        createdTasks++
    }

    fun incrementModifiedTasks() {
        modifiedTasks++
    }

    fun incrementDeletedTasks() {
        deletedTasks++
    }

    @ReadOperation
    fun getTaskMetrics(): TaskMetrics {
        return TaskMetrics(createdTasks, modifiedTasks, deletedTasks)
    }

}