package com.intellij.awesomeKt.util

import com.intellij.openapi.actionSystem.DataKey
import link.kotlin.scripts.model.Link

/**
 * Created by Roger™
 */
object AkDataKeys {

    val tableItem: DataKey<Link> = DataKey.create<Link>("AwesomeKt.TreeItem")
}
