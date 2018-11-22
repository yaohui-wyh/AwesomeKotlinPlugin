package link.kotlin.scripts

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.intellij.awesomeKt.configurable.AkData
import com.intellij.awesomeKt.configurable.AkSettings
import com.intellij.awesomeKt.util.KotlinScriptCompiler
import com.intellij.awesomeKt.util.d
import com.intellij.openapi.diagnostic.Logger
import com.intellij.util.ResourceUtil
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.newFixedThreadPoolContext
import link.kotlin.scripts.model.Link
import link.kotlin.scripts.resources.links.*
import okhttp3.*
import java.io.IOException
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.concurrent.TimeUnit
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class ProjectLinks {

    companion object {

        private val logger = Logger.getInstance(this::class.java)
        private val applicationContext = newFixedThreadPoolContext(4, "KL")
        private val mapper = jacksonObjectMapper()
        private val dispatcher = Dispatcher().apply {
            maxRequests = 50
            maxRequestsPerHost = 10
        }
        private val cacheInterceptor = Interceptor { chain ->
            val request = chain.request()
            val response = chain.proceed(request)
            response.newBuilder()
                    .removeHeader("Pragma")
                    .removeHeader("Cache-Control")
                    .header("Cache-Control", "max-age=" + 30 * 60)
                    .build()
        }
        private val okHttpClient: OkHttpClient = OkHttpClient
                .Builder()
                .addNetworkInterceptor(cacheInterceptor)
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .dispatcher(dispatcher)
                .build()
        val pluginBundleLinks = listOf(AkLibraries, AkProjects, AkAndroid, AkJavaScript, AkNative, AkLinks, AkUserGroups, AkArchive)

        fun search(text: String): List<Category> {
            // TODO: Change to coroutines
            return AkData.instance.links.mapNotNull { category ->
                if (category.name.contains(text, true)) {
                    category
                } else {
                    val subCategoriesMatch = category.subcategories.mapNotNull { subcategory ->
                        if (subcategory.name.contains(text, true)) {
                            subcategory
                        } else {
                            val linksMatch = subcategory.links.filter {
                                val sources = mutableListOf(it.name, it.desc, it.href)
                                sources.addAll(it.tags)
                                sources.any { s -> s.contains(text, true) }
                            }
                            if (linksMatch.isNotEmpty()) Subcategory(subcategory.id, subcategory.name, linksMatch.toMutableList()) else null
                        }
                    }
                    if (subCategoriesMatch.isNotEmpty()) Category(category.id, category.name, subCategoriesMatch.toMutableList()) else null
                }
            }
        }

        fun linksFromPlugin(): List<CategoryKtsResult> {
            return pluginBundleLinks.map { CategoryKtsResult(success = true, category = it) }
        }

        private fun linksFromKtsScript(url: String, text: String): CategoryKtsResult {
            val result = CategoryKtsResult(url)
            if (text.isEmpty()) {
                logger.d("Invalid ktsText")
                result.success = false
                result.errMessage = "Error fetching KotlinScript, please check network connection"
            } else {
                try {
                    logger.d("Parsing kts text...")
                    val category = KotlinScriptCompiler.execute<Category>(text)
                    if (category != null) {
                        result.success = true
                        result.category = category
                    } else {
                        result.success = false
                        result.errMessage = "Error parsing Kotlin Script"
                    }
                } catch (ex: Exception) {
                    logger.d("Error while processing text", ex)
                    result.success = false
                    result.errMessage = "Error parsing Kotlin Script, ${ex.message}"
                }
            }
            return result
        }

        private fun linksFromFile(basePath: String, path: String): Category? {
            return try {
                logger.d("Parsing kts File [$basePath/$path]...")
                val text = ResourceUtil.loadText(ResourceUtil.getResource(this::class.java, basePath, path))
                KotlinScriptCompiler.execute(text)
            } catch (ex: Exception) {
                logger.d("Error while processing file [$basePath/$path]", ex)
                null
            }
        }

        private suspend fun fetchKtsFile(url: String): Response? {
            logger.d("Fetching $url...")
            return try {
                val request = Request.Builder().url(url).build()
                okHttpClient.newCall(request).await()
            } catch (ex: Exception) {
                logger.d("Error while processing ktsFile $url", ex)
                null
            }
        }

        private suspend fun linksFromUrls(urls: List<String>): List<CategoryKtsResult> {
            val deferredList = urls.map {
                GlobalScope.async(applicationContext) {
                    fetchKtsFile(it)?.body()?.string().orEmpty()
                }
            }
            return deferredList.withIndex().map {
                val url = urls[it.index]
                val text = it.value.await()
                linksFromKtsScript(url, text)
            }
        }

        suspend fun linksFromCustomUrls(): List<CategoryKtsResult> {
            logger.d("Fetching links from custom Urls")
            val urls = AkSettings.instance.customContentSourceList
            return linksFromUrls(urls)
        }

        suspend fun linksFromGithub(): List<CategoryKtsResult> {
            logger.d("Fetching links from Github")
            val urls = githubContentList.map { githubPrefix + it }
            return linksFromUrls(urls)
        }

        suspend fun getGithubStarCount(link: Link): Link {
            logger.d("Querying GitHub Api for ${link.name}...")
            val request = Request.Builder()
                    .url("https://api.github.com/repos/${link.name}")
                    .header("Accept", "application/vnd.github.preview")
                    .build()
            try {
                val res = okHttpClient.newCall(request).await()
                val json = mapper.readTree(res.body()?.string().orEmpty())
                val stargazersCount = json["stargazers_count"]?.asInt() ?: 0
                val pushedAt = json["pushed_at"]?.asText().orEmpty()

                if (pushedAt.isNotEmpty()) {
                    link.star = stargazersCount
                    link.update = parseInstant(pushedAt).format(formatter)
                }
            } catch (ex: Exception) {
                logger.d("Error while getting Github info for ${link.name}", ex)
            }
            return link
        }
    }
}

data class CategoryKtsResult(
        var url: String = "",
        var success: Boolean = false,
        var category: Category? = null,
        var errMessage: String = ""
)

val formatter: DateTimeFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)

fun parseInstant(date: String): LocalDateTime {
    return LocalDateTime.ofInstant(Instant.parse(date), ZoneId.of("UTC"))
}

suspend fun Call.await(): Response = suspendCoroutine { cont: Continuation<Response> ->
    this.enqueue(object : Callback {
        override fun onResponse(call: Call, response: Response) {
            cont.resume(response)
        }

        override fun onFailure(call: Call, ex: IOException) {
            cont.resumeWithException(ex)
        }
    })
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