package com.example.appnotepad.util

import java.time.LocalDateTime

class ErrorMessage(var timestamp: LocalDateTime, var status: Int, var message: String?, var path: String)