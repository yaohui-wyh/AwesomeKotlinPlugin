package com.intellij.awesomeKt.view

import com.intellij.awesomeKt.util.AkIntelliJUtil
import com.intellij.openapi.ui.InputValidator
import com.intellij.openapi.ui.Messages
import com.intellij.ui.ToolbarDecorator
import com.intellij.ui.components.JBList
import com.intellij.util.ui.JBDimension
import java.awt.BorderLayout
import javax.swing.DefaultListModel
import javax.swing.JPanel
import javax.swing.ListSelectionModel

/**
 * Created by Roger™
 */
class AkContentSourceListPanel(myItems: List<String> = listOf()) : JPanel(BorderLayout()) {

    private var model = DefaultListModel<String>()
    private val list = JBList(model)

    var items: List<String>
        get() = model.elements().toList()
        set(listItems) {
            model.removeAllElements()
            listItems.forEach { model.addElement(it) }
        }

    init {
        items = myItems
        list.setEmptyText(AkIntelliJUtil.message("Config.updateContent.emptyList"))
        list.selectionMode = ListSelectionModel.SINGLE_SELECTION
        val decorator = ToolbarDecorator.createDecorator(list)
                .setAddAction {
                    val url = Messages.showInputDialog(
                            AkIntelliJUtil.message("Config.updateContent.addDialog.message"),
                            AkIntelliJUtil.message("Config.updateContent.addActionName"),
                            null,
                            "",
                            object : InputValidator {
                                override fun checkInput(inputString: String?) = !inputString.isNullOrBlank()
                                override fun canClose(inputString: String?) = true
                            }
                    )
                    if (!url.isNullOrBlank()) {
                        model.addElement(url)
                    }
                }
                .setAddActionName(AkIntelliJUtil.message("Config.updateContent.addActionName"))
                .setRemoveAction {
                    model.remove(list.selectedIndex)
                }
                .setRemoveActionName(AkIntelliJUtil.message("Config.updateContent.removeActionName"))
                .disableUpDownActions()

        add(decorator.createPanel(), BorderLayout.CENTER)
        preferredSize = JBDimension(-1, 120)
    }

    fun toggleEditable(enable: Boolean) {
        list.isEnabled = enable
    }
}