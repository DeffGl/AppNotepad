package com.example.appnotepad

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class AppNotepadApplication

fun main(args: Array<String>) {
    runApplication<AppNotepadApplication>(*args)
}
