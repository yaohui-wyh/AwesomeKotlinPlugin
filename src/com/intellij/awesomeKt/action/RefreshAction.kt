package com.intellij.awesomeKt.action

import com.intellij.awesomeKt.util.AKIntelliJUtil
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAwareAction

/**
 * Created by Roger™
 */
class RefreshAction : DumbAwareAction(
        AKIntelliJUtil.message("RefreshAction.text"),
        AKIntelliJUtil.message("RefreshAction.description"),
        AllIcons.Actions.Refresh
) {
    override fun actionPerformed(e: AnActionEvent?) {

    }

    override fun update(e: AnActionEvent?) {
        e?.presentation?.let { p ->
            p.text = AKIntelliJUtil.message("RefreshAction.text")
            p.description = AKIntelliJUtil.message("RefreshAction.description")
        }
    }
}