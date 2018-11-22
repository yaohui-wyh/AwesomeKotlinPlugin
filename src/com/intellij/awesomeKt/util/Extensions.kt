package com.intellij.awesomeKt.util

import com.intellij.openapi.diagnostic.Logger

fun Logger.d(message: String, t: Throwable? = null) {
    if (AwesomeKtDebug.isInternal) {
        this.warn("[AwesomeKt Debug] $message", t)
    }
}

object AwesomeKtDebug {

    val isInternal: Boolean = System.getProperty("AwesomeKotlin.is.internal") == "true"
}