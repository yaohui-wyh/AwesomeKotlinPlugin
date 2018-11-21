package link.kotlin.scripts

import com.intellij.awesomeKt.configurable.AkData
import com.intellij.awesomeKt.util.KotlinScriptCompiler
import com.intellij.awesomeKt.util.d
import com.intellij.openapi.diagnostic.Logger
import com.intellij.util.ResourceUtil
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import link.kotlin.scripts.resources.links.*
import okhttp3.*
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class ProjectLinks {

    companion object {

        private val logger = Logger.getInstance(this::class.java)

        private val dispatcher = Dispatcher().apply {
            maxRequests = 200
            maxRequestsPerHost = 100
        }
        private val okHttpClient: OkHttpClient = OkHttpClient
                .Builder()
                .readTimeout(15, TimeUnit.SECONDS)
                .dispatcher(dispatcher)
                .build()

        fun search(text: String): List<Category> {
            return AkData.instance.links.mapNotNull {
                if (it.name.contains(text, true)) {
                    it
                } else {
                    val subCategoriesMatch = it.subcategories.mapNotNull {
                        if (it.name.contains(text, true)) {
                            it
                        } else {
                            val linksMatch = it.links.filter {
                                val sources = mutableListOf(it.name, it.desc, it.href)
                                sources.addAll(it.tags)
                                sources.any { it.contains(text, true) }
                            }
                            if (linksMatch.isNotEmpty()) Subcategory(it.id, it.name, linksMatch.toMutableList()) else null
                        }
                    }
                    if (subCategoriesMatch.isNotEmpty()) Category(it.id, it.name, subCategoriesMatch.toMutableList()) else null
                }
            }
        }

        fun linksFromPlugin(): List<Category> {
            return listOf(AkLibraries, AkProjects, AkAndroid, AkJavaScript, AkNative, AkLinks, AkUserGroups, AkArchive)
        }

        private fun linksFromKtsScript(text: String): Category? {
            if (text.isEmpty()) {
                logger.d("Invalid ktsText")
                return null
            }
            return try {
                logger.d("Parsing kts text...")
                KotlinScriptCompiler.execute<Category>(text)
            } catch (ex: Exception) {
                logger.warn("Error while processing text", ex)
                null
            }
        }

        private fun linksFromFile(path: String): Category? {
            return try {
                val text = ResourceUtil.loadText(ResourceUtil.getResource(this::class.java, "/link/kotlin/scripts/resources/links/", path))
                KotlinScriptCompiler.execute(text)
            } catch (e: Exception) {
                logger.error("Error while processing file $path.", e)
                null
            }
        }

        private suspend fun fetchKtsFile(url: String): Response {
            return suspendCoroutine { cont ->
                logger.d("Fetching $url...")
                val request = Request.Builder().url(url).build()
                okHttpClient.newCall(request).enqueue(object : Callback {
                    override fun onFailure(call: Call, ex: IOException) { cont.resumeWithException(ex) }
                    override fun onResponse(call: Call, response: Response) { cont.resume(response) }
                })
            }
        }

        suspend fun linksFromGithub(): List<Category> {
            val deferredList = githubContentList.map {
                GlobalScope.async {
                    fetchKtsFile(githubPrefix + it).body()?.string().orEmpty()
                }
            }
            // KotlinScriptCompiler should invoke in sync
            return deferredList.mapNotNull { linksFromKtsScript(it.await()) }
        }
    }
}

val githubContentList = listOf(
        "Libraries.kts",
        "Projects.kts",
        "Android.kts",
        "JavaScript.kts",
        "Native.kts",
        "Links.kts",
        "UserGroups.kts",
        "Archive.kts"
)
const val githubPrefix = "https://raw.githubusercontent.com/KotlinBy/awesome-kotlin/master/src/main/resources/links/"