package com.intellij.awesomeKt.util

import com.intellij.CommonBundle
import com.intellij.awesomeKt.configurable.AkSettings
import com.intellij.notification.NotificationGroup
import com.intellij.notification.NotificationListener
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.util.*

/**
 * Created by Roger™
 */
class AkIntelliJUtil {

    companion object {

        private val NOTIFICATION_TOOLWINDOW_GROUP = NotificationGroup.toolWindowGroup(Constants.PLUGIN_NAME, Constants.TOOL_WINDOW_NAME, false)

        fun successBalloon(project: Project?, content: String, listener: NotificationListener? = null, title: String = "") {
            NOTIFICATION_TOOLWINDOW_GROUP.createNotification(title, content, NotificationType.INFORMATION, listener).notify(project)
        }

        fun warnBalloon(project: Project?, content: String, listener: NotificationListener? = null, title: String = "") {
            NOTIFICATION_TOOLWINDOW_GROUP.createNotification(title, content, NotificationType.WARNING, listener).notify(project)
        }

        fun errorBalloon(project: Project?, content: String, listener: NotificationListener? = null, title: String = "") {
            NOTIFICATION_TOOLWINDOW_GROUP.createNotification(title, content, NotificationType.ERROR, listener).notify(project)
        }

        fun message(key: String, vararg params: Any): String {
            val filename = "messages.lang-${AkSettings.instance.lang.locale}"
            return CommonBundle.message(ResourceBundle.getBundle(filename, UTF8Control()), key, params)
        }
    }

    class UTF8Control : ResourceBundle.Control() {

        @Throws(IllegalAccessException::class, InstantiationException::class, IOException::class)
        override fun newBundle(baseName: String, locale: Locale, format: String, loader: ClassLoader, reload: Boolean): ResourceBundle? {
            val bundleName = toBundleName(baseName, locale)
            val resourceName = toResourceName(bundleName, "properties")
            var bundle: ResourceBundle? = null
            var stream: InputStream? = null
            if (reload) {
                val url = loader.getResource(resourceName)
                if (url != null) {
                    val connection = url.openConnection()
                    if (connection != null) {
                        connection.useCaches = false
                        stream = connection.getInputStream()
                    }
                }
            } else {
                stream = loader.getResourceAsStream(resourceName)
            }
            if (stream != null) {
                try {
                    bundle = PropertyResourceBundle(InputStreamReader(stream, "UTF-8"))
                } finally {
                    stream.close()
                }
            }
            return bundle
        }
    }
}