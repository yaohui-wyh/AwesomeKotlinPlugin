package link.kotlin.scripts

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.intellij.awesomeKt.app.AkData
import com.intellij.awesomeKt.util.KotlinScriptCompiler
import com.intellij.awesomeKt.util.d
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.diagnostic.Logger
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import link.kotlin.scripts.model.GitHubLink
import link.kotlin.scripts.model.GitHubReadme
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

    private val logger = Logger.getInstance(this::class.java)
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
                        if (linksMatch.isNotEmpty()) Subcategory(
                            subcategory.id,
                            subcategory.name,
                            linksMatch.toMutableList()
                        ) else null
                    }
                }
                if (subCategoriesMatch.isNotEmpty()) Category(
                    category.id,
                    category.name,
                    subCategoriesMatch.toMutableList()
                ) else null
            }
        }
    }

    fun linksFromPlugin(): List<CategoryKtsResult> =
        pluginBundleLinks.map { CategoryKtsResult(success = true, category = it) }

    fun parseKtsFile(url: String, text: String): CategoryKtsResult {
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

    suspend fun fetchKtsFiles(urls: List<String>): List<KtsFilePair> {
        val list = urls.map {
            GlobalScope.async {
                logger.d("Fetching $it...")
                try {
                    val request = Request.Builder().url(it).build()
                    okHttpClient.newCall(request).await()
                } catch (ex: Exception) {
                    logger.d("Error while processing ktsFile $it", ex)
                    null
                }
            }
        }
        return list.mapIndexed { idx, deferred ->
            try {
                val response = deferred.await()
                val text = response?.body?.string().orEmpty()
                response?.close()
                KtsFilePair(urls[idx], text)
            } catch (ex: Exception) {
                KtsFilePair(urls[idx], "")
            }
        }
    }

    suspend fun getGithubStarCount(link: Link): GitHubLink {
        val githubLink = GitHubLink(link = link)
        logger.d("Querying GitHub Api for ${link.name}...")
        val request = Request.Builder()
            .url("https://api.github.com/repos/${link.name}")
            .header("Accept", "application/vnd.github.preview")
            .build()
        try {
            val response = okHttpClient.newCall(request).await()
            val json = mapper.readTree(response.body?.string().orEmpty())
            response.close()

            githubLink.link?.star = json["stargazers_count"]?.asInt(0)
            githubLink.link?.update = parseDate(json["pushed_at"]?.textValue())
            githubLink.createdAt = parseDate(json["created_at"]?.textValue())
            githubLink.forkCount = json["forks"]?.asInt(0) ?: 0
            githubLink.watchCount = json["subscribers_count"]?.asInt(0) ?: 0
            githubLink.openIssueCount = json["open_issues"]?.asInt(0) ?: 0
            githubLink.homepage = json["homepage"]?.textValue()?.trim().orEmpty()
        } catch (ex: Exception) {
            logger.d("Error while getting Github info for ${link.name}", ex)
        }
        return githubLink
    }

    fun getGithubReadme(link: Link): GitHubReadme {
        logger.d("Querying GitHub readme for ${link.name}...")
        val request = Request.Builder()
            .url("https://api.github.com/repos/${link.name}/readme")
            .header("Accept", "application/vnd.github.preview")
            .build()
        val ret = GitHubReadme()
        try {
            val response = okHttpClient.newCall(request).execute()
            val json = mapper.readTree(response.body?.string().orEmpty())
            response.close()

            ret.content = json["content"]?.textValue().orEmpty()
            ret.name = json["name"]?.textValue().orEmpty()
            ret.url = json["url"]?.textValue().orEmpty()
            ret.size = json["size"]?.asInt(0) ?: 0
        } catch (ex: Exception) {
            logger.d("Error while getting Github info for ${link.name}", ex)
        }
        return ret
    }

    companion object {
        val instance: ProjectLinks = ServiceManager.getService(ProjectLinks::class.java)
    }
}

data class KtsFilePair(val url: String, val text: String) {
    fun shortName() = url.split("/").lastOrNull().orEmpty()
}

data class CategoryKtsResult(
    var url: String = "",
    var success: Boolean = false,
    var category: Category? = null,
    var errMessage: String = ""
)

fun parseDate(date: String?): String {
    if (date.isNullOrBlank()) return ""
    val formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
    return LocalDateTime.ofInstant(Instant.parse(date), ZoneId.of("UTC")).format(formatter)
}

suspend fun Call.await(): Response = suspendCoroutine { cont: Continuation<Response> ->
    this.enqueue(object : Callback {
        override fun onResponse(call: Call, response: Response) {
            cont.resume(response)
        }

        override fun onFailure(call: Call, e: IOException) {
            cont.resumeWithException(e)
        }
    })
}

const val githubPrefix = "https://raw.githubusercontent.com/KotlinBy/awesome-kotlin/master/src/main/resources/links/"
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
val pluginBundleLinks = listOf(
    AkLibraries,
    AkProjects,
    AkAndroid,
    AkJavaScript,
    AkNative,
    AkLinks,
    AkUserGroups,
    AkArchive
)
