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
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Link

        if (name != other.name) return false
        if (href != other.href) return false
        if (desc != other.desc) return false
        if (type != other.type) return false
        if (!platforms.contentEquals(other.platforms)) return false
        if (!tags.contentEquals(other.tags)) return false
        if (whitelisted != other.whitelisted) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + href.hashCode()
        result = 31 * result + desc.hashCode()
        result = 31 * result + type.hashCode()
        result = 31 * result + platforms.contentHashCode()
        result = 31 * result + tags.contentHashCode()
        result = 31 * result + whitelisted.hashCode()
        return result
    }
}