package com.example.appnotepad.entity.enum

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue
import java.lang.IllegalArgumentException

enum class Status() {
    COMPLETED,
    INCOMPLETE;

    companion object {

        @JvmStatic
        @JsonCreator
        fun fromString(status: String): Status {
            return values().find { it.toString() == status } ?: throw IllegalArgumentException("Некорректно введенный статус задачи: $status")
        }
    }

    override fun toString(): String {
        return when (this) {
            COMPLETED -> "COMPLETED"
            INCOMPLETE -> "INCOMPLETE"
        }
    }
}