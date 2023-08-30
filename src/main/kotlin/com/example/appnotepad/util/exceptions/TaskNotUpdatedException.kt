package com.example.appnotepad.util.exceptions

class TaskNotUpdatedException : TaskNotSaveDbException {
    constructor(message: String) : super(message)
    constructor(message: String, cause: Throwable) : super(message, cause)
}