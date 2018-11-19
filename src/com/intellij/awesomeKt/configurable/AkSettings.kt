package com.intellij.awesomeKt.configurable

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage

/**
 * Created by Roger™
 */
@State(name = "AwesomeKotlinSettings", storages = [(Storage("awesome-kotlin/settings.xml"))])
class AKSettings : PersistentStateComponent<AKSettings.State> {
    private var myState = State()

    companion object {
        val instance: AKSettings
            get() = ServiceManager.getService(AKSettings::class.java)
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
        // TODO
    }

    class State {
        var lang: LanguageItem = LanguageItem.CHINESE
    }
}

enum class LanguageItem(val locale: String, val messageKey: String) {
    CHINESE("zh", "Config.language.zh"),
    ENGLISH("en", "Config.language.en");
}
