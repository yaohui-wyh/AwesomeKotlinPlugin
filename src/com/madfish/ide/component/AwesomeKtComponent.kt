package com.madfish.ide.component

import com.intellij.openapi.components.ApplicationComponent
import com.madfish.ide.configurable.AwesomeKtSettings
import com.madfish.ide.util.Constants

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