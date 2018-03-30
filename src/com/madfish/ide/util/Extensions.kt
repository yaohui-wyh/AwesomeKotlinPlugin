package com.madfish.ide.util

import com.intellij.openapi.diagnostic.Logger
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

fun LocalDateTime?.toStr(pattern: String? = "yyyy-MM-dd HH:mm:ss"): String {
    this?.let { return it.format(DateTimeFormatter.ofPattern(pattern)) }
    return ""
}

fun Logger.d(message: String, t: Throwable? = null) {
    if (PodcastDebug.isInternal) {
        this.warn(message, t)
    }
}
