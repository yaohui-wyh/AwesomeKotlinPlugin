package link.kotlin.scripts.model

import link.kotlin.scripts.LinkType
import link.kotlin.scripts.TargetType

/**
 * Content of this file should be same with
 * https://github.com/KotlinBy/awesome-kotlin/blob/master/src/main/kotlin/link/kotlin/scripts/model/Link.kt
 */
data class Link(
        val name: String,
        val href: String = "",
        val desc: String = "",
        val type: LinkType = LinkType.none,
        val platforms: Array<TargetType>,
        val tags: Array<String> = arrayOf(),
        val whitelisted: Boolean = false,
        var star: Int? = null,
        var update: String? = null
)