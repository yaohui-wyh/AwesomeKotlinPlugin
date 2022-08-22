package com.intellij.awesomeKt.app

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage

/**
 * Created by Roger™
 */
@State(name = "AwesomeKotlinSettings", storages = [(Storage("awesome-kotlin/settings.xml"))])
@Service(Service.Level.APP)
class AkSettings : PersistentStateComponent<AkSettings.State> {
    private var myState = State()

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

    class State {
        var lang: LanguageItem = LanguageItem.ENGLISH
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