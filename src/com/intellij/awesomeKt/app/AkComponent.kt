package com.intellij.awesomeKt.app

import com.intellij.awesomeKt.util.Constants
import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.components.Service

/**
 * Created by Roger™
 */
@Service
class AkComponent {
    init {
        resetProps()
    }

    private fun resetProps() {
        val prop = PropertiesComponent.getInstance()
        prop.setValue(Constants.Properties.refreshBtnBusyKey, false)
        prop.setValue(Constants.Properties.viewReadmeBtnBusyKey, false)
    }
}