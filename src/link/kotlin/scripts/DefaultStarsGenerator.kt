package link.kotlin.scripts

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.intellij.openapi.diagnostic.Logger
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.newFixedThreadPoolContext
import okhttp3.*
import java.io.IOException
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.concurrent.TimeUnit
import kotlin.coroutines.experimental.Continuation
import kotlin.coroutines.experimental.suspendCoroutine

class DefaultStarsGenerator {

    companion object {
        private val logger = Logger.getInstance(this::class.java)
        private val applicationContext = newFixedThreadPoolContext(4, "KL")
        private val dispatcher = Dispatcher().apply {
            maxRequests = 200
            maxRequestsPerHost = 100
        }
        private val okHttpClient: OkHttpClient = OkHttpClient
                .Builder()
                .readTimeout(15, TimeUnit.SECONDS)
                .dispatcher(dispatcher)
                .build()
        private val mapper = jacksonObjectMapper()
    }

    suspend fun generate(links: Links): List<Category> {
        val deferredCategories = links.map { category ->
            async(applicationContext) { processCategory(category) }
        }
        return deferredCategories.map { it.await() }
    }

    private suspend fun processCategory(category: Category): Category {
        val deferredSubcategories = category.subcategories.map { subcategory ->
            async(applicationContext) { processSubcategory(subcategory) }
        }
        val processed = deferredSubcategories.map { it.await() }.toMutableList()
        return category.copy(subcategories = processed)
    }

    private suspend fun processSubcategory(subcategory: Subcategory): Subcategory {
        return subcategory.copy(links = subcategory.links.map { link ->
            when (link.type) {
                LinkType.github -> {
                    logger.info("Fetching star count from github: ${link.name}.")
                    try {
                        val response = getGithubStarCount(okHttpClient, link.name)
                        val json = mapper.readTree(response.body()?.string() ?: "")
                        val stargazersCount = json["stargazers_count"]?.asInt() ?: 0
                        val pushedAt = json["pushed_at"]?.asText().orEmpty()

                        if (pushedAt.isNotEmpty()) {
                            link.star = stargazersCount
                            link.update = parseInstant(pushedAt).format(formatter)
                        }
                    } catch (e: Exception) {
                        logger.info("Error while fetching data for '${link.name}'.", e)
                    }
                    link
                }
                LinkType.bitbucket -> {
                    logger.info("Fetching star count from bitbucket: ${link.name}.")
                    try {
                        val response = getBitbucketStarCount(okHttpClient, link.name)
                        val stars = mapper.readValue<BitbucketResponse>(response.body()?.string() ?: "")
                        link.star = stars.size
                    } catch (e: Exception) {
                        logger.info("Error while fetching data for '${link.name}'.", e)
                    }
                    link
                }
                else -> link
            }
        }.toMutableList())
    }
}


private suspend fun getGithubStarCount(client: OkHttpClient, name: String): Response {
    val request = Request.Builder()
            .url("https://api.github.com/repos/$name")
            .header("User-Agent", "Awesome-Kotlin-List")
            .header("Accept", "application/vnd.github.preview")
            .build()
    return client.newCall(request).await()
}

private suspend fun getBitbucketStarCount(client: OkHttpClient, name: String): Response {
    val request = Request.Builder()
            .url("https://api.bitbucket.org/2.0/repositories/$name/watchers")
            .header("User-Agent", "Awesome-Kotlin-List")
            .build()
    return client.newCall(request).await()
}

@JsonIgnoreProperties(ignoreUnknown = true)
class BitbucketResponse(
        val size: Int
)

fun parseInstant(date: String): LocalDateTime {
    return LocalDateTime.ofInstant(Instant.parse(date), ZoneId.of("UTC"))
}

internal val formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)

suspend fun Call.await(): Response = suspendCoroutine { cont: Continuation<Response> ->
    this.enqueue(object : Callback {
        override fun onResponse(call: Call, response: Response) {
            cont.resume(response)
        }

        override fun onFailure(call: Call, exception: IOException) {
            cont.resumeWithException(OkHttpException(call.request(), exception))
        }
    })
}

class OkHttpException(val request: Request, val exception: Exception) : RuntimeException(exception)