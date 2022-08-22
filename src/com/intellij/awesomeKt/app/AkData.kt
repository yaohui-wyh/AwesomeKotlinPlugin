package com.intellij.awesomeKt.app

import com.intellij.awesomeKt.util.pluginBundleLinks
import com.intellij.openapi.components.Service
import link.kotlin.scripts.dsl.Category

/**
 * Created by Roger™
 */
@Service(Service.Level.APP)
class AkData {
    var links: List<Category> = pluginBundleLinks
}