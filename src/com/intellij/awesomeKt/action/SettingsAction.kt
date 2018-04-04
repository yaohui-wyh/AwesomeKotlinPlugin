package com.intellij.awesomeKt.action

import com.intellij.awesomeKt.util.Constants
import com.intellij.awesomeKt.util.IdeUtil
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.project.DumbAwareAction

/**
 * Created by Roger™
 */
class SettingsAction : DumbAwareAction(
        IdeUtil.message("SettingsAction.text"),
        IdeUtil.message("SettingsAction.description"),
        AllIcons.General.SecondaryGroup
) {
    override fun actionPerformed(e: AnActionEvent?) {
        ShowSettingsUtil.getInstance().showSettingsDialog(e?.project, Constants.PLUGIN_NAME)
    }

    override fun update(e: AnActionEvent?) {
        e?.presentation?.let { p ->
            p.text = IdeUtil.message("SettingsAction.text")
            p.description = IdeUtil.message("SettingsAction.description")
        }
    }
}