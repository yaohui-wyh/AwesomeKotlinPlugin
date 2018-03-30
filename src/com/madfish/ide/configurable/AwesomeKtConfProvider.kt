package com.madfish.ide.configurable

import com.intellij.openapi.options.Configurable
import com.intellij.openapi.options.ConfigurableProvider
import com.madfish.ide.util.Constants
import com.madfish.ide.view.AwesomeKtConfigComponent
import javax.swing.JComponent

/**
 * Created by Roger™
 */
class AwesomeKtConfProvider : ConfigurableProvider() {
    override fun createConfigurable() = AwesomeKtConfigurable()
}

class AwesomeKtConfigurable : Configurable {

    private val component = AwesomeKtConfigComponent()

    override fun disposeUIResources() {}

    override fun reset() {
        component.reset()
    }

    override fun getHelpTopic() = ""

    override fun isModified() = component.isModified()

    override fun getDisplayName() = Constants.PLUGIN_NAME

    override fun apply() {
        component.apply()
    }

    override fun createComponent(): JComponent? = component.mainPanel
}
