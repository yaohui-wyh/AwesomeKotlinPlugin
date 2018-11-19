package com.intellij.awesomeKt.view

import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory

/**
 * Created by Roger™
 */
class AkToolWindow : ToolWindowFactory, DumbAware {

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val contentFactory = ContentFactory.SERVICE.getInstance()
        val content = AkToolWindowContent()
        content.init(project)
        val contentObj = contentFactory.createContent(content.createToolWindow(), "", false)
        toolWindow.contentManager.addContent(contentObj)
        Disposer.register(project, contentObj)
    }
}