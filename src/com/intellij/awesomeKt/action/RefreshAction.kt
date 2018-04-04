package com.intellij.awesomeKt.action

import com.intellij.awesomeKt.util.IdeUtil
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAwareAction

/**
 * Created by Roger™
 */
class RefreshAction : DumbAwareAction(
        IdeUtil.message("RefreshAction.text"),
        IdeUtil.message("RefreshAction.description"),
        AllIcons.Actions.Refresh
) {
    override fun actionPerformed(e: AnActionEvent?) {

    }

    override fun update(e: AnActionEvent?) {
        e?.presentation?.let { p ->
            p.text = IdeUtil.message("RefreshAction.text")
            p.description = IdeUtil.message("RefreshAction.description")
        }
    }
}