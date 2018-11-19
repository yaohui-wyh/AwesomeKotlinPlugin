package com.intellij.awesomeKt.action

import com.intellij.awesomeKt.util.AkIntelliJUtil
import com.intellij.awesomeKt.util.Constants
import com.intellij.awesomeKt.view.AkIcons
import com.intellij.ide.BrowserUtil
import com.intellij.openapi.actionSystem.AnActionEvent

/**
 * Created by Roger™
 */
class WebAction : LanguageAwareAction(
        AkIntelliJUtil.message("WebAction.text"),
        AkIntelliJUtil.message("WebAction.description"),
        AkIcons.KOTLIN
) {
    override fun actionPerformed(e: AnActionEvent?) {
        BrowserUtil.browse(Constants.WEB_URL)
    }
}