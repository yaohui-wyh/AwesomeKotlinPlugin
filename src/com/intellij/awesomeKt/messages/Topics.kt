package com.intellij.awesomeKt.messages

import com.intellij.util.messages.Topic
import link.kotlin.scripts.Link

val AWESOME_KOTLIN_VIEW_TOPIC: Topic<TableViewListener> = Topic.create("AwesomeKt.View.Topic", TableViewListener::class.java)

interface TableViewListener {

    fun onLinkItemClicked(link: Link?)
}