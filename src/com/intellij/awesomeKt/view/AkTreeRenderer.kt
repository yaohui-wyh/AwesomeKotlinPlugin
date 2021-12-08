package com.intellij.awesomeKt.view

import com.intellij.ui.JBDefaultTreeCellRenderer
import com.intellij.ui.components.JBLabel
import com.intellij.util.ui.JBUI
import link.kotlin.scripts.Category
import link.kotlin.scripts.LinkType
import link.kotlin.scripts.Subcategory
import link.kotlin.scripts.model.Link
import java.awt.Component
import java.awt.Font
import javax.swing.JLabel
import javax.swing.JTree
import javax.swing.tree.DefaultMutableTreeNode

/**
 * Created by Roger™
 */
class AkTreeRenderer(tree: JTree) : JBDefaultTreeCellRenderer(tree) {

    override fun getTreeCellRendererComponent(
        tree: JTree?, value: Any?, sel: Boolean, expanded: Boolean,
        leaf: Boolean, row: Int, hasFocus: Boolean
    ): Component {
        super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus)
        if (value is DefaultMutableTreeNode) {
            val label = JBLabel()
            val obj = value.userObject
            when (obj) {
                is Category -> {
                    label.text = obj.name
                    label.font = Font(JLabel().font.fontName, Font.BOLD, JBUI.scale(14))
                    label.icon = AkIcons.KOTLIN_ICON
                    label.border = JBUI.Borders.empty(8, 0, 4, 0)
                }
                is Subcategory -> {
                    label.text = obj.name
                    label.font = Font(JLabel().font.fontName, Font.BOLD, JBUI.scale(13))
                    label.border = JBUI.Borders.empty(4, 0, 4, 0)
                }
                is Link -> {
                    when (obj.type) {
                        LinkType.github -> label.icon = AkIcons.GITHUB_ICON
                        LinkType.bitbucket -> label.icon = AkIcons.BITBUCKET_ICON
                        else -> {
                        }
                    }
                    label.text = obj.name
                    label.font = Font(JLabel().font.fontName, Font.PLAIN, JBUI.scale(12))
                    label.border = JBUI.Borders.empty(2, 0, 3, 0)
                }
            }
            return label
        }
        return this
    }
}