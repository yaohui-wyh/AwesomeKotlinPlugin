package com.intellij.awesomeKt.app

import com.intellij.openapi.components.ServiceManager
import link.kotlin.scripts.Category
import com.intellij.awesomeKt.util.pluginBundleLinks

/**
 * Created by Roger™
 */
class AkData {

    var links: List<Category> = pluginBundleLinks

    companion object {
        val instance: AkData
            get() = ServiceManager.getService(AkData::class.java)
    }
}