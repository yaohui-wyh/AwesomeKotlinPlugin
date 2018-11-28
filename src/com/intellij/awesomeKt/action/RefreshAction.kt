package com.intellij.awesomeKt.action

import com.intellij.awesomeKt.messages.AWESOME_KOTLIN_REFRESH_TOPIC
import com.intellij.awesomeKt.util.AkIntelliJUtil
import com.intellij.awesomeKt.util.Constants
import com.intellij.icons.AllIcons
import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager

/**
 * Created by Roger™
 */
class RefreshAction : LanguageAwareAction(
        AkIntelliJUtil.message("RefreshAction.text"),
        AkIntelliJUtil.message("RefreshAction.description"),
        AllIcons.Actions.Refresh
) {
    override fun actionPerformed(e: AnActionEvent?) {
        ApplicationManager.getApplication().messageBus.syncPublisher(AWESOME_KOTLIN_REFRESH_TOPIC).onRefresh()
    }

    override fun update(e: AnActionEvent?) {
        e?.presentation?.isEnabled = !PropertiesComponent.getInstance().getBoolean(Constants.Properties.refreshBtnBusyKey, false)
    }
}