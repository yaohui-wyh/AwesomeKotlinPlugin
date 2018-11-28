package com.intellij.awesomeKt.util

import com.intellij.awesomeKt.configurable.AkSettings
import com.intellij.awesomeKt.configurable.ContentSource
import com.intellij.ide.plugins.PluginManager
import com.intellij.openapi.application.ApplicationInfo
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.ServiceManager.getService
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.extensions.PluginId
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import kotlin.reflect.full.memberProperties

/**
 * Created by Roger™
 */
class TrackingManager {

    private val logger = Logger.getInstance(this::class.java)
    private val usageList = mutableListOf<TrackingAction>()
    private val usageSet = mutableSetOf<TrackingAction>()
    private val uuid by lazy { AkSettings.instance.uuid }
    private lateinit var ideVersion: String
    private lateinit var pluginVersion: String
    private val client = OkHttpClient.Builder().build()

    init {
        ApplicationManager.getApplication().executeOnPooledThread {
            try {
                pluginVersion = PluginManager.getPlugin(PluginId.getId(Constants.Plugins.id))?.version.orEmpty()
                val buildNumber = ApplicationInfo.getInstance().build
                ideVersion = buildNumber.productCode.toLowerCase() + buildNumber.baselineVersion
            } catch (ex: Exception) {
                logger.d("invalid plugin / ide version", ex)
            }
        }
    }

    fun reportUsage(item: TrackingAction, uniq: Boolean = false) {
        if (uniq) {
            usageSet.add(item)
        } else {
            usageList.add(item)
        }
    }

    fun reportConfig() {
        getConfig()?.let { usageSet.add(it) }
    }

    fun doReport() {
        val items = mutableListOf<TrackingAction>()
        items.addAll(usageList)
        items.addAll(usageSet)
        if (items.isNotEmpty()) {
            usageList.clear()
            usageSet.clear()
            bulkReport(items)
        }
    }

    private fun getConfig(): TrackingAction? {
        return when (AkSettings.instance.contentSource) {
            ContentSource.GITHUB -> TrackingAction.CONTENT_SOURCE_GITHUB
            ContentSource.CUSTOM -> TrackingAction.CONTENT_SOURCE_CUSTOM
            else -> null
        }
    }

    private fun isReportAvailable(): Boolean {
        if (!this::pluginVersion.isInitialized || pluginVersion.isBlank()) return false
        if (!this::ideVersion.isInitialized || ideVersion.isBlank()) return false
        if (uuid.isBlank()) return false
        return true
    }

    private fun bulkReport(items: List<TrackingAction>) {
        if (items.isEmpty() || !isReportAvailable()) return
        val gaList = mutableListOf<GAItem>()
        gaList.addAll(items.filter { it.groupBy }.groupBy { it.reportName }.map {
            GAItem(tid = gaTrackingId, cid = uuid, ea = it.key, ec = ideVersion, el = pluginVersion, ev = it.value.size)
        })
        gaList.addAll(items.filter { !it.groupBy }.map {
            GAItem(tid = gaTrackingId, cid = uuid, ea = it.reportName, ec = ideVersion, el = pluginVersion)
        })
        ApplicationManager.getApplication().executeOnPooledThread { batchReportToGa(gaList) }
    }

    private fun batchReportToGa(items: List<GAItem>) {
        try {
            val body = RequestBody.create(null, items.joinToString("\n") { it.toBodyString() })
            val request = Request.Builder().url(gaBatchHost).post(body).build()
            val response = client.newCall(request).execute()
            response.close()
        } catch (ex: Exception) {
            logger.d("")
        }
    }

    companion object {
        private const val gaBatchHost = "https://www.google-analytics.com/batch"
        private const val gaTrackingId = "UA-134663830-1"
        val instance: TrackingManager = getService(TrackingManager::class.java)
    }
}

data class GAItem(
        /** Version */
        val v: Int = 1,

        /** GA Tracking ID */
        val tid: String,

        /** Anonymous Client ID */
        val cid: String,

        /** Event hit type */
        val t: String = "event",

        /**
         * Event Action. Required
         * DataTrackingConst.Actions, e.g. ar
         */
        val ea: String,

        /**
         * Event Category. Required.
         * IDE Version, e.g. iu183
         */
        val ec: String,

        /**
         * Event label
         * Plugin Version, e.g. 1.1.0
         */
        val el: String,

        /**
         * Event Value
         * Action Counts, for high-frequency events
         */
        val ev: Int = 1
) {

    fun toBodyString() = this.javaClass.kotlin.memberProperties.joinToString("&") { "${it.name}=${it.get(this)}" }
}

enum class TrackingAction(val reportName: String, val displayName: String, val groupBy: Boolean = false) {
    SEARCH("ase", "搜索"),
    CHECKOUT("ac", "签出工程", true),

    // UiActions
    CLICK_ITEM("uci", "查看条目", true),
    VISIT_GITHUB("uvg", "访问仓库 GitHub 地址", true),

    // Configs
    CONTENT_SOURCE_GITHUB("ccsg", "配置项 - GitHub"),
    CONTENT_SOURCE_CUSTOM("ccsc", "配置项 - Custom"),

    // IDE
    IDE_START("is", "IDE 启动");
}