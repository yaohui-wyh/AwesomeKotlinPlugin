package com.intellij.awesomeKt.messages

import com.intellij.util.messages.Topic
import link.kotlin.scripts.model.Link

val AWESOME_KOTLIN_REFRESH_TOPIC: Topic<RefreshItemsListener> = Topic.create("AwesomeKt.Refresh.Topic", RefreshItemsListener::class.java)

val AWESOME_KOTLIN_VIEW_TOPIC: Topic<TableViewListener> = Topic.create("AwesomeKt.View.Topic", TableViewListener::class.java)

interface RefreshItemsListener {
    fun onRefresh()
}

interface TableViewListener {
    fun onLinkItemClicked(link: Link?)
}