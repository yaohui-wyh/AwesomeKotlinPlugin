package com.madfish.ide.configurable

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage

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
    }

    class State {
        var lang: LanguageItem = LanguageItem.CHINESE
    }
}

enum class LanguageItem(val locale: String, val messageKey: String) {
    CHINESE("zh", "Config.language.zh"),
    ENGLISH("en", "Config.language.en");
}
