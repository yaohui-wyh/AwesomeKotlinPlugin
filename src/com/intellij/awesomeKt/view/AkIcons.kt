package com.intellij.awesomeKt.view

import com.intellij.openapi.util.IconLoader

/**
 * Created by Roger™
 */
interface AkIcons {

    companion object {
        val KOTLIN = IconLoader.getIcon("/images/kotlin.png", AkIcons::class.java)
        val KOTLIN_ICON = IconLoader.getIcon("/images/kotlinIcon.png", AkIcons::class.java)
        val GITHUB_ICON = IconLoader.getIcon("/images/github.png", AkIcons::class.java)
        val BITBUCKET_ICON = IconLoader.getIcon("/images/bitbucket.png", AkIcons::class.java)
        val STAR = IconLoader.getIcon("/images/star.png", AkIcons::class.java)
        val CHANGES = IconLoader.getIcon("/images/changes.png", AkIcons::class.java)
        val CREATED = IconLoader.getIcon("/images/created.png", AkIcons::class.java)
    }
}