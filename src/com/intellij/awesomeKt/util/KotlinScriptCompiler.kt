package com.intellij.awesomeKt.util

import com.intellij.openapi.diagnostic.Logger
import org.jetbrains.kotlin.cli.common.repl.KotlinJsr223JvmScriptEngineFactoryBase
import org.jetbrains.kotlin.cli.common.repl.ScriptArgsWithTypes
import org.jetbrains.kotlin.script.jsr223.KotlinJsr223JvmDaemonCompileScriptEngine
import org.jetbrains.kotlin.script.jsr223.KotlinStandardJsr223ScriptTemplate
import javax.script.Bindings
import javax.script.ScriptContext
import javax.script.ScriptEngine
import kotlin.script.experimental.jvm.util.KotlinJars
import kotlin.script.experimental.jvm.util.scriptCompilationClasspathFromContextOrStlib

/**
 * Created by Roger™
 */
object KotlinScriptCompiler {

    private val scriptEngine by lazy { MyScriptEngineFactory().scriptEngine }
    private val logger = Logger.getInstance(this::class.java)

    @Suppress("UNCHECKED_CAST")
    fun <T> execute(script: String): T? {
        return try {
            scriptEngine.eval(script) as? T
        } catch (ex: ClassCastException) {
            logger.d("Script eval ClassCastException", ex)
            null
        } catch (ex: ThreadDeath) {
            logger.d("Script eval ThreadDead", ex)
            null
        }
    }
}

class MyScriptEngineFactory : KotlinJsr223JvmScriptEngineFactoryBase() {
    override fun getScriptEngine(): ScriptEngine {
        Thread.currentThread().contextClassLoader = this::class.java.classLoader
        return KotlinJsr223JvmDaemonCompileScriptEngine(
                this,
                KotlinJars.compilerClasspath,
                scriptCompilationClasspathFromContextOrStlib("kotlin-script-util-1.3.21.jar", wholeClasspath = true),
                KotlinStandardJsr223ScriptTemplate::class.qualifiedName!!,
                { ctx, types -> ScriptArgsWithTypes(arrayOf(ctx.getBindings(ScriptContext.ENGINE_SCOPE)), types ?: emptyArray()) },
                arrayOf(Bindings::class)
        )
    }
}