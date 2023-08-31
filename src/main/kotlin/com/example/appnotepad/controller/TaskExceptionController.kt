package com.example.appnotepad.controller

import com.example.appnotepad.util.ErrorMessage
import com.example.appnotepad.util.exceptions.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.WebRequest
import java.rmi.UnexpectedException
import java.time.LocalDateTime

@RestControllerAdvice
class TaskExceptionController {

    var log: Logger = LoggerFactory.getLogger(this::class.java)

    @ExceptionHandler
    fun handleTaskNotFoundException (ex: TaskNotFoundException, request: WebRequest): ResponseEntity<ErrorMessage>{
        log.error(ex.message)
        val errorMessage = ErrorMessage(LocalDateTime.now(),
            HttpStatus.NOT_FOUND.value(),
            ex.message.toString(),
            request.getDescription(false)
        )
        return ResponseEntity(errorMessage, HttpStatus.NOT_FOUND)
    }

    @ExceptionHandler(TaskNotSaveDbException::class, TaskNotUpdatedException::class)
    fun handleTaskSaveDbException(ex: TaskNotSaveDbException, request: WebRequest): ResponseEntity<ErrorMessage>{
        if(ex.cause?.message == null) log.error(ex.message) else log.error("${ex.message}: ${ex.cause?.message}")
        val errorMessage = ErrorMessage(LocalDateTime.now(),
            HttpStatus.BAD_REQUEST.value(),
            ex.message.toString(),
            request.getDescription(false)
        )
        return ResponseEntity(errorMessage, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler
    fun handleTaskNotDeleteException(ex: TaskNotDeleteException, request: WebRequest): ResponseEntity<ErrorMessage>{
        log.error("${ex.message}")
        val errorMessage = ErrorMessage(LocalDateTime.now(),
            HttpStatus.BAD_REQUEST.value(),
            ex.message,
            request.getDescription(false)
        )
        return ResponseEntity(errorMessage, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler
    fun handleIllegalArgumentException(ex: IllegalArgumentException, request: WebRequest): ResponseEntity<ErrorMessage>{
        log.error("${ex.message}")
        val errorMessage = ErrorMessage(LocalDateTime.now(),
            HttpStatus.BAD_REQUEST.value(),
            "Некорректно введен статус задачи",
            request.getDescription(false)
        )
        return ResponseEntity(errorMessage, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler
    fun handleUnexpectedException(ex: UnexpectedException, request: WebRequest): ResponseEntity<ErrorMessage>{
        log.error("${ex.message}: ${ex.cause?.message}")
        val errorMessage = ErrorMessage(LocalDateTime.now(),
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            ex.message,
            request.getDescription(false)
        )
        return ResponseEntity(errorMessage, HttpStatus.INTERNAL_SERVER_ERROR)
    }



}