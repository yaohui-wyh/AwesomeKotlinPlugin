package com.intellij.awesomeKt.util

import com.intellij.openapi.diagnostic.Logger
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

fun LocalDateTime?.toStr(pattern: String? = "yyyy-MM-dd HH:mm:ss"): String {
    this?.let { return it.format(DateTimeFormatter.ofPattern(pattern)) }
    return ""
}

fun Logger.d(message: String, t: Throwable? = null) {
    if (AwesomeKtDebug.isInternal) {
        this.warn(message, t)
    }
}

object AwesomeKtDebug {

    val isInternal: Boolean = System.getProperty("AwesomeKotlin.is.internal") == "true"
}