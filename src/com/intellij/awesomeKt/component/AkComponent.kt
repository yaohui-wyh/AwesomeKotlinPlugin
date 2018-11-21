package com.intellij.awesomeKt.component

import com.intellij.awesomeKt.configurable.AkSettings
import com.intellij.awesomeKt.util.Constants
import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.components.ApplicationComponent

/**
 * Created by Roger™
 */
class AkComponent : ApplicationComponent {
    override fun getComponentName() = Constants.COMPONENT_NAME

    override fun disposeComponent() {}

    override fun initComponent() {
        AkSettings.instance.init()
        PropertiesComponent.getInstance().setValue(Constants.propRefreshBtnBusy, false)
    }
}