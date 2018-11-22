package com.intellij.awesomeKt.configurable

import com.intellij.openapi.components.ServiceManager
import link.kotlin.scripts.Category
import link.kotlin.scripts.ProjectLinks

/**
 * Created by Roger™
 */
class AkData {

    var links: List<Category> = ProjectLinks.pluginBundleLinks

    companion object {
        val instance: AkData
            get() = ServiceManager.getService(AkData::class.java)
    }
}