package com.intellij.awesomeKt.action

import com.intellij.awesomeKt.util.AKIntelliJUtil
import com.intellij.awesomeKt.util.Constants
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.options.ShowSettingsUtil

/**
 * Created by Roger™
 */
class SettingsAction : LanguageAwareAction(
        AKIntelliJUtil.message("SettingsAction.text"),
        AKIntelliJUtil.message("SettingsAction.description"),
        AllIcons.General.SecondaryGroup
) {
    override fun actionPerformed(e: AnActionEvent?) {
        ShowSettingsUtil.getInstance().showSettingsDialog(e?.project, Constants.PLUGIN_NAME)
    }
}