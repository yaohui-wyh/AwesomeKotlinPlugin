package link.kotlin.scripts

data class Link(
        val name: String = "",
        val href: String = "",
        val desc: String = "",
        val type: LinkType = LinkType.none,
        val tags: Array<String> = arrayOf(),
        val whitelisted: Boolean = false,
        var star: Int = 0,
        var update: String = ""
)

enum class LinkType {
    none,
    github,
    bitbucket,
    blog,
    kug,
    article,
    video,
    slides,
    webinar
}

fun LinkType.toView() = when (this) {
    LinkType.article -> "Articles, Blog Posts"
    LinkType.video -> "Videos"
    LinkType.slides -> "Slides"
    LinkType.webinar -> "Webinars"
    else -> ""
}

data class Subcategory(
        val id: String = "",
        val name: String = "",
        val links: MutableList<Link> = mutableListOf()
) {
    operator fun Link.unaryPlus() = links.add(this)
}

data class Category(
        val id: String = "",
        val name: String = "",
        val subcategories: MutableList<Subcategory> = mutableListOf()
) {
    operator fun Subcategory.unaryPlus() = subcategories.add(this)
}


fun category(name: String, config: Category.() -> Unit): Category {
    return Category(name = name, subcategories = mutableListOf()).apply {
        config(this)
    }
}

fun Category.subcategory(name: String, config: Subcategory.() -> Unit) {
    val subcategory = Subcategory(name = name, links = mutableListOf())
    config(subcategory)
    this.subcategories.add(subcategory)
}

fun Subcategory.link(config: LinkBuilder.() -> Unit) {
    val linkBuilder = LinkBuilder()
    config(linkBuilder)
    this.links.add(linkBuilder.toLink())
}

class LinkBuilder {
    var name: String = ""
    var href: String = ""
    var desc: String = ""
    var type: LinkType = LinkType.none
    var tags: Array<String> = arrayOf()

    var whitelisted: Boolean = false
    fun toLink(): Link {
        return Link(
                name = name,
                href = href,
                desc = desc,
                type = type,
                tags = tags,
                whitelisted = whitelisted
        )
    }
}

object Tags {
    inline operator fun <reified K> get(vararg items: K) = arrayOf(*items)
}

typealias Links = List<Category>