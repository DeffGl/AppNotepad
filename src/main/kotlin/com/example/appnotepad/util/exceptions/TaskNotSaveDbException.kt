package com.example.appnotepad.util.exceptions

open class TaskNotSaveDbException: Exception {
    constructor(message: String) : super(message)
    constructor(message: String, cause: Throwable) : super(message, cause)
}