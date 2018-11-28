package com.intellij.awesomeKt.component

import com.intellij.awesomeKt.configurable.AkSettings
import com.intellij.awesomeKt.util.Constants
import com.intellij.awesomeKt.util.TrackingAction
import com.intellij.awesomeKt.util.TrackingManager
import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.components.ApplicationComponent
import org.apache.commons.lang.RandomStringUtils

/**
 * Created by Roger™
 */
class AkComponent : ApplicationComponent {
    override fun getComponentName() = Constants.Components.appName

    override fun disposeComponent() {}

    override fun initComponent() {
        PropertiesComponent.getInstance().setValue(Constants.Properties.refreshBtnBusyKey, false)

        val settings = AkSettings.instance
        if (settings.uuid.isBlank()) {
            settings.uuid = RandomStringUtils.randomAlphanumeric(8)
        }
        val tracking = TrackingManager.instance
        tracking.reportUsage(TrackingAction.IDE_START)
        tracking.reportConfig()
    }
}