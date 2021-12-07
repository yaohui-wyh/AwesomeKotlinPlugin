package com.intellij.awesomeKt.app

import com.intellij.openapi.components.ServiceManager
import link.kotlin.scripts.Category
import link.kotlin.scripts.pluginBundleLinks

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