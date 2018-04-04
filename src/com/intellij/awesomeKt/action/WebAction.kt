package com.intellij.awesomeKt.action

import com.intellij.awesomeKt.util.Constants
import com.intellij.awesomeKt.util.IdeUtil
import com.intellij.awesomeKt.view.MyIcons
import com.intellij.ide.BrowserUtil
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAwareAction

/**
 * Created by Roger™
 */
class WebAction : DumbAwareAction(
        IdeUtil.message("WebAction.text"),
        IdeUtil.message("WebAction.description"),
        MyIcons.KOTLIN
) {
    override fun actionPerformed(e: AnActionEvent?) {
        BrowserUtil.browse(Constants.WEB_URL)
    }

    override fun update(e: AnActionEvent?) {
        e?.presentation?.let { p ->
            p.text = IdeUtil.message("WebAction.text")
            p.description = IdeUtil.message("WebAction.description")
        }
    }
}