package com.intellij.awesomeKt.action

import com.intellij.awesomeKt.util.AkIntelliJUtil
import com.intellij.awesomeKt.util.Constants
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.options.ShowSettingsUtil

/**
 * Created by Roger™
 */
class SettingsAction : LanguageAwareAction(
        AkIntelliJUtil.message("SettingsAction.text"),
        AkIntelliJUtil.message("SettingsAction.description"),
        AllIcons.General.Settings
) {
    override fun actionPerformed(e: AnActionEvent) {
        ShowSettingsUtil.getInstance().showSettingsDialog(e.project, Constants.Plugins.name)
    }
}