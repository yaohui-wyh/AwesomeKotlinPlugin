package com.intellij.awesomeKt.view

import com.intellij.awesomeKt.configurable.AkSettings
import com.intellij.awesomeKt.configurable.ContentSource
import com.intellij.awesomeKt.configurable.LanguageItem
import com.intellij.awesomeKt.messages.AWESOME_KOTLIN_REFRESH_TOPIC
import com.intellij.awesomeKt.util.AkIntelliJUtil
import com.intellij.awesomeKt.util.Constants
import com.intellij.icons.AllIcons
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.VerticalFlowLayout
import com.intellij.openapi.ui.ex.MultiLineLabel
import com.intellij.ui.HyperlinkLabel
import com.intellij.ui.IdeBorderFactory
import com.intellij.ui.ListCellRendererWrapper
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBRadioButton
import com.intellij.util.ui.UIUtil
import link.kotlin.scripts.githubContentList
import link.kotlin.scripts.githubPrefix
import java.awt.FlowLayout
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Insets
import javax.swing.*

/**
 * Created by Roger™
 */
class AkConfigComponent {

    val mainPanel = JPanel()
    private val comboBox = ComboBox(LanguageItem.values())
    private val radioBtnGroup = ButtonGroup()
    private val fromPluginBtn = JBRadioButton(AkIntelliJUtil.message("Config.updateContent.fromPlugin"))
    private val fromGithubBtn = JBRadioButton(AkIntelliJUtil.message("Config.updateContent.fromGithub"))
    private val fromCustomUrlBtn = JBRadioButton(AkIntelliJUtil.message("Config.updateContent.fromCustomUrl"))
    private val contentListPanel = AkContentSourceListPanel()
    private val contentHintPanel = MultiLineLabel()

    init {
        mainPanel.layout = GridBagLayout()
        mainPanel.add(buildSettingsPanel(), GridBagConstraints(0, 0, 1, 1, 1.0, 0.0, GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, Insets(0, 0, 0, 0), 0, 0))
        mainPanel.add(buildContentSourcePanel(), GridBagConstraints(0, 1, 1, 1, 1.0, 0.0, GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, Insets(0, 0, 0, 0), 0, 0))
        mainPanel.add(buildAboutPanel(), GridBagConstraints(0, 2, 1, 1, 1.0, 1.0, GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, Insets(0, 0, 0, 0), 0, 0))
    }

    private fun buildSettingsPanel(): JPanel {
        val settingsPanel = JPanel()
        settingsPanel.layout = BoxLayout(settingsPanel, BoxLayout.Y_AXIS)
        settingsPanel.border = IdeBorderFactory.createTitledBorder(AkIntelliJUtil.message("Config.settings"), true)

        val languagePanel = JPanel(FlowLayout(FlowLayout.LEFT, 0, 5))
        val languageLabel = JBLabel(AkIntelliJUtil.message("Config.language"))

        languageLabel.border = IdeBorderFactory.createEmptyBorder(0, 0, 0, 15)
        languagePanel.add(languageLabel)
        languagePanel.add(comboBox)

        comboBox.selectedItem = AkSettings.instance.lang
        comboBox.renderer = object : ListCellRendererWrapper<LanguageItem>() {
            override fun customize(list: JList<*>?, item: LanguageItem?, index: Int, selected: Boolean, hasFocus: Boolean) {
                setText(AkIntelliJUtil.message(item?.messageKey.orEmpty()))
            }
        }

        settingsPanel.add(languagePanel)
        return settingsPanel
    }

    private fun buildAboutPanel(): JPanel {
        val aboutPanel = JPanel()
        aboutPanel.layout = BoxLayout(aboutPanel, BoxLayout.Y_AXIS)
        aboutPanel.border = IdeBorderFactory.createTitledBorder(AkIntelliJUtil.message("Config.about"), true)

        // ========= Info Panel ============
        val infoPanel = JPanel(FlowLayout(FlowLayout.LEFT, 0, 5))

        val issueLabel = HyperlinkLabel()
        infoPanel.add(issueLabel)

        issueLabel.setHtmlText("${AkIntelliJUtil.message("Config.feedback")}: <a href=\"#feedback\">Github Issue</a>, ")
        issueLabel.setHyperlinkTarget(Constants.Urls.issues)

        val akWebLabel = HyperlinkLabel()
        infoPanel.add(akWebLabel)

        akWebLabel.setHtmlText("${AkIntelliJUtil.message("Config.visitWeb")}: <a href=\"#visit\">KotlinBy/awesome-kotlin</a>")
        akWebLabel.setHyperlinkTarget(Constants.Urls.awesomeKtRepo)

        // ========= Extra Panel ============
        val extraPanel = JPanel(FlowLayout(FlowLayout.LEFT, 0, 5))

        val feedbackLabel = JBLabel(AkIntelliJUtil.message("Config.feedbackText"), UIUtil.ComponentStyle.SMALL, UIUtil.FontColor.BRIGHTER)
        feedbackLabel.border = IdeBorderFactory.createEmptyBorder(0, 2, 0, 8)
        extraPanel.add(feedbackLabel)
        setRateLabel(extraPanel)

        aboutPanel.add(infoPanel)
        aboutPanel.add(extraPanel)

        return aboutPanel
    }

    private fun getContentBtn(src: ContentSource): JBRadioButton {
        return when (src) {
            ContentSource.PLUGIN -> fromPluginBtn
            ContentSource.GITHUB -> fromGithubBtn
            ContentSource.CUSTOM -> fromCustomUrlBtn
        }
    }

    private fun getContentSrc(btnModel: ButtonModel): ContentSource {
        return when (btnModel) {
            fromPluginBtn.model -> ContentSource.PLUGIN
            fromGithubBtn.model -> ContentSource.GITHUB
            fromCustomUrlBtn.model -> ContentSource.CUSTOM
            else -> ContentSource.PLUGIN
        }
    }

    private fun buildContentSourcePanel(): JPanel {
        val contentPanel = JPanel()
        contentPanel.layout = GridBagLayout()
        contentPanel.border = IdeBorderFactory.createTitledBorder(AkIntelliJUtil.message("Config.updateContent"), true)

        // ========= Radio Buttons ============
        val btnGroupPanel = JPanel(VerticalFlowLayout(VerticalFlowLayout.LEFT, 0, 2, true, false))

        radioBtnGroup.add(fromPluginBtn)
        radioBtnGroup.add(fromGithubBtn)
        radioBtnGroup.add(fromCustomUrlBtn)

        btnGroupPanel.add(fromPluginBtn)
        btnGroupPanel.add(fromGithubBtn)
        btnGroupPanel.add(fromCustomUrlBtn)

        fromPluginBtn.addActionListener { toggleRadioSelection() }
        fromGithubBtn.addActionListener { toggleRadioSelection() }
        fromCustomUrlBtn.addActionListener { toggleRadioSelection() }

        contentPanel.add(btnGroupPanel, GridBagConstraints(0, 0, 0, 1, 1.0, 0.0, GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, Insets(0, 0, 0, 0), 0, 0))
        contentPanel.add(contentListPanel, GridBagConstraints(0, 1, 1, 1, 1.0, 0.0, GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, Insets(8, 10, 0, 0), 0, 0))
        contentPanel.add(contentHintPanel, GridBagConstraints(0, 2, 0, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.WEST, Insets(10, 10, 0, 0), 0, 0))

        toggleRadioSelection()
        return contentPanel
    }

    private fun setRateLabel(parent: JPanel): HyperlinkLabel {
        val label = HyperlinkLabel()
        parent.add(label)

        label.setHtmlText(" <a href=\"#rate\">${AkIntelliJUtil.message("Config.rate")}</a>")
        label.font = UIUtil.getLabelFont(UIUtil.FontSize.SMALL)
        label.setIcon(AllIcons.Toolwindows.ToolWindowFavorites)
        label.setHyperlinkTarget(Constants.Urls.ratePlugin)
        return label
    }

    private fun toggleRadioSelection() {
        contentListPanel.isVisible = !fromPluginBtn.isSelected
        contentHintPanel.isVisible = !fromPluginBtn.isSelected
        if (fromGithubBtn.isSelected) {
            contentListPanel.items = githubContentList
            contentListPanel.toggleEditable(false)
            contentHintPanel.text = AkIntelliJUtil.message("Config.updateContent.fromGithub.hint", githubPrefix + "Links.kts", AkIntelliJUtil.message("Config.feedback"))
        }
        if (fromCustomUrlBtn.isSelected) {
            contentListPanel.items = AkSettings.instance.customContentSourceList
            contentListPanel.toggleEditable(true)
            contentHintPanel.text = AkIntelliJUtil.message("Config.updateContent.fromCustomUrl.hint")
        }
    }

    fun isModified(): Boolean {
        val contentSrcSelection = getContentSrc(radioBtnGroup.selection)
        return comboBox.selectedItem != AkSettings.instance.lang ||
                contentSrcSelection != AkSettings.instance.contentSource ||
                (contentSrcSelection == ContentSource.CUSTOM && contentListPanel.items != AkSettings.instance.customContentSourceList)
    }

    // Reset will be called at init
    fun reset() {
        comboBox.selectedItem = AkSettings.instance.lang
        radioBtnGroup.setSelected(getContentBtn(AkSettings.instance.contentSource).model, true)
        toggleRadioSelection()
    }

    fun apply() {
        val contentSrcSelection = getContentSrc(radioBtnGroup.selection)
        AkSettings.instance.lang = comboBox.selectedItem as LanguageItem
        AkSettings.instance.contentSource = getContentSrc(radioBtnGroup.selection)
        if (contentSrcSelection == ContentSource.CUSTOM) {
            AkSettings.instance.customContentSourceList = contentListPanel.items.toMutableList()
        }
        // Trigger refresh
        ApplicationManager.getApplication().messageBus.syncPublisher(AWESOME_KOTLIN_REFRESH_TOPIC).onRefresh()
    }
}