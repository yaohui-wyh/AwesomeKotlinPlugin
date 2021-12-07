package com.intellij.awesomeKt.action

import com.intellij.awesomeKt.util.AkDataKeys
import com.intellij.awesomeKt.util.AkIntelliJUtil
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.vcs.CheckoutProvider
import com.intellij.openapi.vcs.CheckoutProviderEx
import com.intellij.openapi.vcs.ProjectLevelVcsManager
import com.intellij.ui.AppIcon

/**
 * Created by Roger™
 */
class VcsCheckoutAction : LanguageAwareAction(
    AkIntelliJUtil.message("VcsCheckoutAction.text"),
    AkIntelliJUtil.message("VcsCheckoutAction.description"),
    AllIcons.Actions.CheckOut
) {
    override fun actionPerformed(e: AnActionEvent) {
        e.getData(AkDataKeys.tableItem)?.let { link ->
            if ((link.github.isNullOrBlank() && link.bitbucket.isNullOrBlank()) || link.href.isNullOrBlank()) return
            CheckoutProvider.EXTENSION_POINT_NAME.extensions.forEach { provider ->
                if (provider is CheckoutProviderEx && provider.vcsId.equals("git", true)) {
                    val project = ProjectManager.getInstance().defaultProject
                    val listener = ProjectLevelVcsManager.getInstance(project).compositeCheckoutListener
                    AppIcon.getInstance().requestAttention(null, true)
                    provider.doCheckout(project, listener, link.href)
                    return
                }
            }
        }
    }

    override fun update(e: AnActionEvent) {
        super.update(e)
        e.presentation.isEnabledAndVisible = false
        e.getData(AkDataKeys.tableItem)?.let {
            if ((!it.github.isNullOrBlank() || !it.bitbucket.isNullOrBlank()) && !it.href.isNullOrBlank()) {
                e.presentation.isEnabledAndVisible = true
            }
        }
    }
}