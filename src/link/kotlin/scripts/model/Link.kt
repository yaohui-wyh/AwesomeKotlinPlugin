package link.kotlin.scripts.model

import link.kotlin.scripts.dsl.PlatformType

data class Link(
    val name: String? = null,
    val github: String? = null,
    val bitbucket: String? = null,
    val kug: String? = null,
    val href: String? = null,
    val desc: String? = null,
    val platforms: List<PlatformType> = emptyList(),
    val tags: List<String> = emptyList(),
    val star: Int? = null,
    val update: String? = null,
    val archived: Boolean = false,
    val unsupported: Boolean = false,
    val awesome: Boolean = false
)

data class GitHubLink(
    var link: Link? = null,
    var createdAt: String = "",
    var homepage: String = "",
    var forkCount: Int = 0,
    var watchCount: Int = 0,
    var openIssueCount: Int = 0
)

data class GitHubReadme(
    var name: String = "",
    var size: Int = 0,
    var url: String = "",
    var content: String = ""
) {
    fun isValid() = name.isNotBlank() && size > 0 && url.isNotBlank() && content.isNotBlank()
}