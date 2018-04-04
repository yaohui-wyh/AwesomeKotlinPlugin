package link.kotlin.scripts

import AndroidCategories
import JavascriptCategories
import LibraryCategories
import LinkCategories
import NativeCategories
import ProjectCategories
import UserGroupCategories

private val files = listOf(
        "Links.kt",
        "Libraries.kt",
        "Projects.kt",
        "Android.kt",
        "JavaScript.kt",
        "Native.kt",
        "UserGroups.kt"
)

object ProjectLinks {

    var categories = mutableListOf(
            LinkCategories,
            LibraryCategories,
            ProjectCategories,
            AndroidCategories,
            JavascriptCategories,
            NativeCategories,
            UserGroupCategories)

    fun search(text: String): List<Category> {
        return categories.mapNotNull {
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

//    val links by lazy { files.mapNotNull(this::linksFromFile) }
//
//    private fun linksFromFile(path: String): Category? {
//        try {
//            val text = ResourceUtil.loadText(ResourceUtil.getResource(this::class.java, "/link/kotlin/scripts/resources/", path))
//            return KotlinScriptCompiler.execute(text)
//        } catch (e: Exception) {
//            logger.error("Error while processing file $path.", e)
//            throw e
//        }
//    }

}
