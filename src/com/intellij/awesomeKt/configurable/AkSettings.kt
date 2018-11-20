package com.intellij.awesomeKt.configurable

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage

/**
 * Created by Roger™
 */
@State(name = "AwesomeKotlinSettings", storages = [(Storage("awesome-kotlin/settings.xml"))])
class AkSettings : PersistentStateComponent<AkSettings.State> {
    private var myState = State()

    companion object {
        val instance: AkSettings
            get() = ServiceManager.getService(AkSettings::class.java)
    }

    var lang: LanguageItem
        get() = myState.lang
        set(lang) {
            myState.lang = lang
        }

    var contentSource: ContentSource
        get() = myState.contentSource
        set(source) {
            myState.contentSource = source
        }

    var customContentSourceList: MutableList<String>
        get() = myState.customContentSourceList
        set(list) {
            myState.customContentSourceList = list
        }

    override fun getState() = myState

    override fun loadState(state: State) {
        myState = state
    }

    fun init() {
        // TODO
    }

    class State {
        var lang: LanguageItem = LanguageItem.CHINESE
        var contentSource: ContentSource = ContentSource.PLUGIN
        var customContentSourceList: MutableList<String> = mutableListOf()
    }
}

enum class LanguageItem(val locale: String, val messageKey: String) {
    CHINESE("zh", "Config.language.zh"),
    ENGLISH("en", "Config.language.en");
}

enum class ContentSource {
    PLUGIN,
    GITHUB,
    CUSTOM
}

val githubPrefix = "https://raw.githubusercontent.com/KotlinBy/awesome-kotlin/master/src/main/resources/links/"
val githubContentList = listOf(
        "Links.kts",
        "Libraries.kts",
        "Projects.kts",
        "Android.kts",
        "JavaScript.kts",
        "Native.kts",
        "UserGroups.kts",
        "Archive.kts"
)
