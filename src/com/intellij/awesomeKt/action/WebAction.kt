package com.intellij.awesomeKt.action

import com.intellij.awesomeKt.util.AKIntelliJUtil
import com.intellij.awesomeKt.util.Constants
import com.intellij.awesomeKt.view.AKIcons
import com.intellij.ide.BrowserUtil
import com.intellij.openapi.actionSystem.AnActionEvent

/**
 * Created by Roger™
 */
class WebAction : LanguageAwareAction(
        AKIntelliJUtil.message("WebAction.text"),
        AKIntelliJUtil.message("WebAction.description"),
        AKIcons.KOTLIN
) {
    override fun actionPerformed(e: AnActionEvent?) {
        BrowserUtil.browse(Constants.WEB_URL)
    }
}