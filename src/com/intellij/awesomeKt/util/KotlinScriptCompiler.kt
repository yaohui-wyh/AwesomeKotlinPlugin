package com.intellij.awesomeKt.util

import org.jetbrains.kotlin.cli.common.repl.KotlinJsr223JvmScriptEngineFactoryBase
import org.jetbrains.kotlin.cli.common.repl.ScriptArgsWithTypes
import org.jetbrains.kotlin.script.jsr223.KotlinJsr223JvmLocalScriptEngine
import org.jetbrains.kotlin.script.jsr223.KotlinStandardJsr223ScriptTemplate
import javax.script.Bindings
import javax.script.ScriptContext
import javax.script.ScriptEngine
import kotlin.script.experimental.jvm.util.scriptCompilationClasspathFromContextOrStlib


/**
 * Created by Roger™
 */
val myClassLoader: ClassLoader = KotlinScriptCompiler.javaClass.classLoader

object KotlinScriptCompiler {

    private val scriptEngine = MyScriptEngineFactory().scriptEngine

    @Suppress("UNCHECKED_CAST")
    fun <T> execute(script: String): T? {
        return try {
            Thread.currentThread().contextClassLoader = myClassLoader
            scriptEngine.eval(script) as? T
        } catch (ex: ClassCastException) {
            null
        }
    }
}

class MyScriptEngineFactory : KotlinJsr223JvmScriptEngineFactoryBase() {
    override fun getScriptEngine(): ScriptEngine {
        return KotlinJsr223JvmLocalScriptEngine(
                this,
                scriptCompilationClasspathFromContextOrStlib(classLoader = myClassLoader, wholeClasspath = true),
                KotlinStandardJsr223ScriptTemplate::class.qualifiedName!!,
                { ctx, types ->
                    ScriptArgsWithTypes(arrayOf(ctx.getBindings(ScriptContext.ENGINE_SCOPE)), types ?: emptyArray())
                },
                arrayOf(Bindings::class)
        )
    }
}