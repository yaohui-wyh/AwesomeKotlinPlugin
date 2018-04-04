package com.intellij.awesomeKt.view

import com.intellij.ui.IdeBorderFactory
import com.intellij.ui.JBDefaultTreeCellRenderer
import com.intellij.ui.components.JBLabel
import com.intellij.util.ui.JBUI
import link.kotlin.scripts.Category
import link.kotlin.scripts.Link
import link.kotlin.scripts.LinkType
import link.kotlin.scripts.Subcategory
import java.awt.Component
import java.awt.Dimension
import java.awt.Font
import javax.swing.JLabel
import javax.swing.JTree
import javax.swing.tree.DefaultMutableTreeNode

/**
 * Created by Roger™
 */
class MyTreeRenderer(tree: JTree) : JBDefaultTreeCellRenderer(tree) {

    override fun getTreeCellRendererComponent(tree: JTree?, value: Any?, sel: Boolean, expanded: Boolean,
                                              leaf: Boolean, row: Int, hasFocus: Boolean): Component {
        super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus)
        if (value is DefaultMutableTreeNode) {
            val label = JBLabel()
            val obj = value.userObject
            when (obj) {
                is Category -> {
                    label.text = obj.name
                    label.font = Font(JLabel().font.fontName, Font.BOLD, JBUI.scale(14))
                    label.icon = MyIcons.KOTLIN_ICON
                    label.border = IdeBorderFactory.createEmptyBorder(8, 0, 4, 0)
                }
                is Subcategory -> {
                    label.text = obj.name
                    label.font = Font(JLabel().font.fontName, Font.BOLD, JBUI.scale(13))
                    label.border = IdeBorderFactory.createEmptyBorder(4, 0, 4, 0)
                }
                is Link -> {
                    when (obj.type) {
                        LinkType.github -> label.icon = MyIcons.GITHUB_ICON
                        LinkType.bitbucket -> label.icon = MyIcons.BITBUCKET_ICON
                        else -> {
                        }
                    }
                    label.text = obj.name
                    label.font = Font(JLabel().font.fontName, Font.PLAIN, JBUI.scale(12))
                    label.border = IdeBorderFactory.createEmptyBorder(2, 0, 3, 0)
                }
            }
            return label
        }
        return this
    }
}