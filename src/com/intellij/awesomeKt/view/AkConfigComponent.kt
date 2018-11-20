package com.intellij.awesomeKt.view

import com.intellij.awesomeKt.configurable.AKSettings
import com.intellij.awesomeKt.configurable.LanguageItem
import com.intellij.awesomeKt.util.AkIntelliJUtil
import com.intellij.awesomeKt.util.Constants
import com.intellij.icons.AllIcons
import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.HyperlinkLabel
import com.intellij.ui.IdeBorderFactory
import com.intellij.ui.ListCellRendererWrapper
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBRadioButton
import com.intellij.util.ui.UIUtil
import java.awt.FlowLayout
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Insets
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import javax.swing.*

/**
 * Created by Roger™
 */
class AkConfigComponent {

    val mainPanel = JPanel()
    private val comboBox = ComboBox(LanguageItem.values())

    init {
        mainPanel.layout = GridBagLayout()
        mainPanel.add(buildSettingsPanel(), GridBagConstraints(0, 0, 1, 1, 1.0, 0.0, GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, Insets(0, 0, 0, 0), 0, 0))
        mainPanel.add(buildUpdatePanel(), GridBagConstraints(0, 1, 1, 1, 1.0, 0.0, GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, Insets(0, 0, 0, 0), 0, 0))
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

        comboBox.selectedItem = AKSettings.instance.lang
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

        issueLabel.setHtmlText("${AkIntelliJUtil.message("Config.feedback")}: <a href=\"#feedback\">Github Issue</a>")
        issueLabel.setHyperlinkTarget(Constants.BUG_REPORTER_WEB_URL)

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

    private fun buildUpdatePanel(): JPanel {
        val updatePanel = JPanel()
        updatePanel.layout = BoxLayout(updatePanel, BoxLayout.Y_AXIS)
        updatePanel.border = IdeBorderFactory.createTitledBorder(AkIntelliJUtil.message("Config.updateContent"), true)

        val btnGroupPanel = JPanel()
        btnGroupPanel.layout = FlowLayout(FlowLayout.LEFT, 0, 5)

        val btnGroup = ButtonGroup()
        val actionListener = ActionListener {
        }
        val pluginVersionBtn = JBRadioButton(AkIntelliJUtil.message("Config.updateContent.pluginVersion"), true)
        pluginVersionBtn.addActionListener(actionListener)
        btnGroup.add(pluginVersionBtn)
        val githubVersionBtn = JBRadioButton(AkIntelliJUtil.message("Config.updateContent.githubVersion"), false)
        githubVersionBtn.addActionListener(actionListener)
        btnGroup.add(githubVersionBtn)
        val customUrlBtn = JBRadioButton(AkIntelliJUtil.message("Config.updateContent.customUrl"), false)
        customUrlBtn.addActionListener(actionListener)
        btnGroup.add(customUrlBtn)

        btnGroupPanel.add(pluginVersionBtn)
        btnGroupPanel.add(githubVersionBtn)
        btnGroupPanel.add(customUrlBtn)

        updatePanel.add(btnGroupPanel)

        return updatePanel
    }

    private fun setRateLabel(parent: JPanel): HyperlinkLabel {
        val label = HyperlinkLabel()
        parent.add(label)

        label.setHtmlText(" <a href=\"#rate\">${AkIntelliJUtil.message("Config.rate")}</a>")
        label.font = UIUtil.getLabelFont(UIUtil.FontSize.SMALL)
        label.setIcon(AllIcons.Toolwindows.ToolWindowFavorites)
        label.setHyperlinkTarget(Constants.PLUGIN_RATE_URL)
        return label
    }

    fun isModified(): Boolean {
        return comboBox.selectedItem != AKSettings.instance.lang
    }

    fun reset() {
        comboBox.selectedItem = AKSettings.instance.lang
    }

    fun apply() {
        AKSettings.instance.lang = comboBox.selectedItem as LanguageItem
    }
}

