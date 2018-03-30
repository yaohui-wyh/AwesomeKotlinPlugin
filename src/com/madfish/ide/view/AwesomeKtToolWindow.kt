package com.madfish.ide.view

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory

/**
 * Created by Roger™
 */
class AwesomeKtToolWindow : ToolWindowFactory, DumbAware {

    private lateinit var toolWindow: ToolWindow
    private val logger = Logger.getInstance(this::class.java)
    private val contentFactory = ContentFactory.SERVICE.getInstance()

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        this.toolWindow = toolWindow
    }

}
