package com.intellij.awesomeKt.action

import com.intellij.awesomeKt.util.*
import com.intellij.awesomeKt.util.Constants.Properties.viewReadmeBtnBusyKey
import com.intellij.icons.AllIcons
import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.fileEditor.impl.FileEditorManagerImpl
import com.intellij.openapi.progress.PerformInBackgroundOption
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.util.io.FileUtilRt
import com.intellij.openapi.vfs.LocalFileSystem
import org.apache.commons.codec.binary.Base64
import java.io.File
import java.nio.charset.Charset

/**
 * Created by Roger™
 */
class ViewReadmeAction : LanguageAwareAction(
    AkIntelliJUtil.message("ViewReadmeAction.text"),
    AkIntelliJUtil.message("ViewReadmeAction.description"),
    AllIcons.Actions.PreviewDetails
) {

    private val logger = Logger.getInstance(this::class.java)

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val link = e.getData(AkDataKeys.tableItem) ?: return
        if ((link.github.isNullOrBlank() && link.bitbucket.isNullOrBlank()) || link.href.isNullOrBlank()) return

        val prop = PropertiesComponent.getInstance()
        ProgressManager.getInstance().run(object :
            Task.Backgroundable(project, "Fetching README...", false, PerformInBackgroundOption.ALWAYS_BACKGROUND) {
            override fun run(indicator: ProgressIndicator) {
                prop.setValue(viewReadmeBtnBusyKey, true)
                val readme = ProjectLinks.instance.getGithubReadme(link)
                if (!readme.isValid()) {
                    prop.setValue(viewReadmeBtnBusyKey, false)
                    AkIntelliJUtil.errorNotification(project, "Invalid README for ${link.name}")
                    return
                }
                try {
                    val tmpFile = buildTempFile()
                    val contentText = Base64.decodeBase64(readme.content).toString(Charset.defaultCharset())
                    val appManager = ApplicationManager.getApplication()
                    val editorManager = (FileEditorManagerImpl.getInstance(project) as FileEditorManagerImpl)
                    LocalFileSystem.getInstance().refreshAndFindFileByIoFile(tmpFile)?.let { vfs ->
                        appManager.invokeLater {
                            editorManager.closeFile(vfs, true, true)
                            appManager.runWriteAction {
                                vfs.setBinaryContent(contentText.toByteArray())
                                appManager.invokeLater {
                                    editorManager.openFileInNewWindow(vfs)
                                    prop.setValue(viewReadmeBtnBusyKey, false)
                                }
                            }
                        }
                    }
                } catch (ex: Exception) {
                    logger.d("View Readme Exception", ex)
                    prop.setValue(viewReadmeBtnBusyKey, false)
                }
            }
        })
    }

    private fun buildTempFile(): File {
        val prop = PropertiesComponent.getInstance()
        prop.getValue(Constants.Properties.tempFilePathKey)?.let { tmpPath ->
            if (tmpPath.isNotBlank()) {
                val prevFile = File(tmpPath)
                if (prevFile.exists() && prevFile.canWrite()) {
                    return prevFile
                }
            }
        }
        val tempDir = FileUtilRt.createTempDirectory("awesomeKt", null)
        val file = FileUtil.createTempFile(tempDir, "README", ".md", true, false)
        prop.setValue(Constants.Properties.tempFilePathKey, file.absolutePath)
        return file
    }

    override fun update(e: AnActionEvent) {
        super.update(e)
        e.presentation.isEnabledAndVisible = false
        val link = e.getData(AkDataKeys.tableItem) ?: return
        val prop = PropertiesComponent.getInstance().getBoolean(viewReadmeBtnBusyKey, false)
        if (!link.github.isNullOrBlank() && !link.href.isNullOrBlank() && !prop) {
            e.presentation.isEnabledAndVisible = true
        }
    }
}