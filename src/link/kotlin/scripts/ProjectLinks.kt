package link.kotlin.scripts

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.intellij.awesomeKt.configurable.AkData
import com.intellij.awesomeKt.util.KotlinScriptCompiler
import com.intellij.awesomeKt.util.d
import com.intellij.openapi.diagnostic.Logger
import com.intellij.util.ResourceUtil
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
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

        private val dispatcher = Dispatcher().apply {
            maxRequests = 200
            maxRequestsPerHost = 100
        }

        private val cacheInterceptor = Interceptor { chain ->
            val request = chain.request()
            val response = chain.proceed(request)
            response.newBuilder()
                    .removeHeader("Pragma")
                    .removeHeader("Cache-Control")
                    .header("Cache-Control", "max-age=" + 20 * 60)
                    .build()
        }

        private val okHttpClient: OkHttpClient = OkHttpClient
                .Builder()
                .addNetworkInterceptor(cacheInterceptor)
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .dispatcher(dispatcher)
                .build()

        private val mapper = jacksonObjectMapper()

        fun search(text: String): List<Category> {
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

        private fun linksFromFile(basePath: String, path: String): Category? {
            return try {
                logger.d("Parsing kts File [$basePath/$path]...")
                val text = ResourceUtil.loadText(ResourceUtil.getResource(this::class.java, basePath, path))
                KotlinScriptCompiler.execute(text)
            } catch (ex: Exception) {
                logger.error("Error while processing file [$basePath/$path]", ex)
                null
            }
        }

        private suspend fun fetchKtsFile(url: String): Response? {
            logger.d("Fetching $url...")
            return try {
                val request = Request.Builder().url(url).build()
                okHttpClient.newCall(request).await()
            } catch (ex: Exception) {
                null
            }
        }

        suspend fun linksFromGithub(): List<Category> {
            val deferredList = githubContentList.map {
                GlobalScope.async {
                    fetchKtsFile(githubPrefix + it)?.body()?.string().orEmpty()
                }
            }
            // KotlinScriptCompiler should invoke in sync
            return deferredList.mapNotNull { linksFromKtsScript(it.await()) }
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