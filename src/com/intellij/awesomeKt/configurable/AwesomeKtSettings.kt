package com.intellij.awesomeKt.configurable

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.runBlocking
import link.kotlin.scripts.DefaultStarsGenerator
import link.kotlin.scripts.Links
import link.kotlin.scripts.ProjectLinks

/**
 * Created by Roger™
 */
@State(name = "awesomeKtSettings", storages = [(Storage("awesomeKt/settings.xml"))])
class AwesomeKtSettings : PersistentStateComponent<AwesomeKtSettings.State> {
    private var myState = State()

    companion object {
        val instance: AwesomeKtSettings
            get() = ServiceManager.getService(AwesomeKtSettings::class.java)
    }

    var lang: LanguageItem
        get() = myState.lang
        set(lang) {
            myState.lang = lang
        }

    override fun getState() = myState

    override fun loadState(state: State) {
        myState = state
    }

    fun init() {
        async {
            ProjectLinks.categories = DefaultStarsGenerator().generate(ProjectLinks.categories).toMutableList()
        }
    }

    class State {
        var lang: LanguageItem = LanguageItem.CHINESE
    }
}

enum class LanguageItem(val locale: String, val messageKey: String) {
    CHINESE("zh", "Config.language.zh"),
    ENGLISH("en", "Config.language.en");
}
