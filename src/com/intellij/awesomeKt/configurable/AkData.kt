package com.intellij.awesomeKt.configurable

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage

/**
 * Created by Roger™
 */
@State(name = "AwesomeKotlinData", storages = [(Storage("awesome-kotlin/data.xml"))])
class AkData(
) : PersistentStateComponent<AkData.State> {
    private var myState = State()

    companion object {
        val instance: AkData
            get() = ServiceManager.getService(AkData::class.java)
    }

    override fun getState() = myState

    override fun loadState(state: State) {
        myState = state
    }

    class State {
    }
}
