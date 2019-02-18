package com.intellij.awesomeKt.util

import com.intellij.openapi.diagnostic.Logger
import org.jetbrains.kotlin.cli.common.repl.KotlinJsr223JvmScriptEngineFactoryBase
import org.jetbrains.kotlin.cli.common.repl.ScriptArgsWithTypes
import org.jetbrains.kotlin.script.jsr223.KotlinJsr223JvmLocalScriptEngine
import org.jetbrains.kotlin.script.jsr223.KotlinStandardJsr223ScriptTemplate
import javax.script.Bindings
import javax.script.ScriptContext
import javax.script.ScriptEngine
import kotlin.script.experimental.jvm.util.scriptCompilationClasspathFromContextOrNull

/**
 * Created by Roger™
 */
object KotlinScriptCompiler {

    private val logger = Logger.getInstance(this::class.java)

    @Suppress("UNCHECKED_CAST")
    fun <T> execute(script: String): T? {
        return try {
            MyScriptEngineFactory().scriptEngine.eval(script) as? T
        } catch (ex: ClassCastException) {
            logger.d("Script eval ClassCastException", ex)
            null
        }
    }
}

class MyScriptEngineFactory : KotlinJsr223JvmScriptEngineFactoryBase() {
    override fun getScriptEngine(): ScriptEngine {
        Thread.currentThread().contextClassLoader = this::class.java.classLoader
        val classPath = scriptCompilationClasspathFromContextOrNull("kotlin-script-util.jar", wholeClasspath = true).orEmpty()
        return KotlinJsr223JvmLocalScriptEngine(
                this,
                classPath,
                KotlinStandardJsr223ScriptTemplate::class.qualifiedName!!,
                { ctx, types -> ScriptArgsWithTypes(arrayOf(ctx.getBindings(ScriptContext.ENGINE_SCOPE)), types ?: emptyArray()) },
                arrayOf(Bindings::class)
        )
    }
}