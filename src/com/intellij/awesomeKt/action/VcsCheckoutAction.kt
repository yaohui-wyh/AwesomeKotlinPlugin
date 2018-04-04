package com.intellij.awesomeKt.action

import com.intellij.awesomeKt.util.IdeUtil
import com.intellij.awesomeKt.util.MyDataKeys
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.vcs.CheckoutProvider
import com.intellij.openapi.vcs.CheckoutProviderEx
import com.intellij.openapi.vcs.ProjectLevelVcsManager
import com.intellij.ui.AppIcon

/**
 * Created by Roger™
 */
class VcsCheckoutAction : DumbAwareAction(
        IdeUtil.message("VcsCheckoutAction.text"),
        IdeUtil.message("VcsCheckoutAction.description"),
        AllIcons.Actions.CheckOut
) {
    override fun actionPerformed(e: AnActionEvent?) {
        e?.getData(MyDataKeys.tableItem)?.let { link ->
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

    override fun update(e: AnActionEvent?) {
        e?.presentation?.isEnabledAndVisible = e?.getData(MyDataKeys.tableItem) != null
        e?.presentation?.let { p ->
            p.text = IdeUtil.message("VcsCheckoutAction.text")
            p.description = IdeUtil.message("VcsCheckoutAction.description")
        }
    }
}