//package com.intellij.awesomeKt.util
//
//import org.jetbrains.kotlin.script.jsr223.KotlinJsr223JvmLocalScriptEngineFactory
//
///**
// * Created by Roger™
// */
//object KotlinScriptCompiler {
//
//    private val scriptEngine = KotlinJsr223JvmLocalScriptEngineFactory().scriptEngine
//
//    @Suppress("UNCHECKED_CAST")
//    fun <T> execute(script: String): T? {
//        return scriptEngine.eval(script) as T?
//    }
//}