package com.intellij.awesomeKt.component

import com.intellij.awesomeKt.configurable.AwesomeKtSettings
import com.intellij.awesomeKt.util.Constants
import com.intellij.openapi.components.ApplicationComponent

/**
 * Created by Roger™
 */
class AwesomeKtComponent : ApplicationComponent {
    override fun getComponentName() = Constants.COMPONENT_NAME

    override fun disposeComponent() {}

    override fun initComponent() {
        AwesomeKtSettings.instance.init()
    }
}