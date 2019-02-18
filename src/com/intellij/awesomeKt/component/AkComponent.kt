package com.intellij.awesomeKt.component

import com.intellij.awesomeKt.util.Constants
import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.components.ApplicationComponent

/**
 * Created by Roger™
 */
class AkComponent : ApplicationComponent {
    override fun getComponentName() = Constants.Components.appName

    override fun disposeComponent() {}

    override fun initComponent() {
        resetProps()
    }

    private fun resetProps() {
        val prop = PropertiesComponent.getInstance()
        prop.setValue(Constants.Properties.refreshBtnBusyKey, false)
        prop.setValue(Constants.Properties.viewReadmeBtnBusyKey, false)
    }
}