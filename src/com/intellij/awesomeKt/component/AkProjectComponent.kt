package com.intellij.awesomeKt.component

import com.intellij.awesomeKt.util.Constants
import com.intellij.openapi.components.ProjectComponent

/**
 * Created by Roger™
 */
class AkProjectComponent : ProjectComponent {

    override fun getComponentName() = Constants.Components.projectName

    override fun disposeComponent() {}

    override fun projectClosed() {}

    override fun initComponent() {}

    override fun projectOpened() {}
}