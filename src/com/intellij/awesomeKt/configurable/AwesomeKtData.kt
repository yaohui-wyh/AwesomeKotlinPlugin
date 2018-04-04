package com.intellij.awesomeKt.configurable

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage

/**
 * Created by Roger™
 */
@State(name = "awesomeKtData", storages = [(Storage("awesomeKt/data.xml"))])
class AwesomeKtData(
) : PersistentStateComponent<AwesomeKtData.State> {
    private var myState = State()

    companion object {
        val instance: AwesomeKtData
            get() = ServiceManager.getService(AwesomeKtData::class.java)
    }

    override fun getState() = myState

    override fun loadState(state: State) {
        myState = state
    }

    class State {
    }
}
