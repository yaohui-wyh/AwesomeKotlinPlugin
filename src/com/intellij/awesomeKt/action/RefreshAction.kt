package com.intellij.awesomeKt.action

import com.intellij.awesomeKt.messages.AWESOME_KOTLIN_REFRESH_TOPIC
import com.intellij.awesomeKt.util.AkIntelliJUtil
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnActionEvent

/**
 * Created by Roger™
 */
class RefreshAction : LanguageAwareAction(
        AkIntelliJUtil.message("RefreshAction.text"),
        AkIntelliJUtil.message("RefreshAction.description"),
        AllIcons.Actions.Refresh
) {
    override fun actionPerformed(e: AnActionEvent?) {
        e?.project?.messageBus?.syncPublisher(AWESOME_KOTLIN_REFRESH_TOPIC)?.onRefresh()
    }
}