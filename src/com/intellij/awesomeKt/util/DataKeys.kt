package com.intellij.awesomeKt.util

import com.intellij.openapi.actionSystem.DataKey
import link.kotlin.scripts.Link

/**
 * Created by Roger™
 */
object MyDataKeys {

    val tableItem: DataKey<Link> = DataKey.create<Link>("AwesomeKotlin.TreeItem")
}
