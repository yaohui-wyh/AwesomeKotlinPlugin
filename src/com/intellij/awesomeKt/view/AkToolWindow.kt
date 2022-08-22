package com.intellij.awesomeKt.view

import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory

/**
 * Created by Roger™
 */
class AkToolWindow : ToolWindowFactory, DumbAware {

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val contentFactory = toolWindow.contentManager.factory
        val content = AkToolWindowContent(project)
        val contentObj = contentFactory.createContent(content.createToolWindow(), "", false)
        toolWindow.contentManager.addContent(contentObj)
        Disposer.register(project, contentObj)
    }
}