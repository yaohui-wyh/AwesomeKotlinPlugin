package link.kotlin.scripts

import com.intellij.awesomeKt.util.KotlinScriptCompiler
import com.intellij.openapi.diagnostic.Logger
import com.intellij.util.ResourceUtil

private val files = listOf(
        "Links.kts",
        "Libraries.kts",
        "Projects.kts",
        "Android.kts",
        "JavaScript.kts",
        "Native.kts",
        "UserGroups.kts"
)

class ProjectLinks {

    companion object {

        private val logger = Logger.getInstance(this::class.java)
        val links by lazy { files.mapNotNull(this::linksFromFile) }

        fun search(text: String): List<Category> {
            return links.mapNotNull {
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

        private fun linksFromFile(path: String): Category? {
            return try {
                val text = ResourceUtil.loadText(ResourceUtil.getResource(this::class.java, "/scripts/", path))
                KotlinScriptCompiler.execute(text)
            } catch (e: Exception) {
                logger.error("Error while processing file $path.", e)
                null
            }
        }
    }
}