package com.intellij.awesomeKt.view

import com.intellij.awesomeKt.action.SettingsAction
import com.intellij.awesomeKt.action.VcsCheckoutAction
import com.intellij.awesomeKt.action.WebAction
import com.intellij.awesomeKt.messages.AWESOME_KOTLIN_VIEW_TOPIC
import com.intellij.awesomeKt.messages.TableViewListener
import com.intellij.awesomeKt.util.MyDataKeys
import com.intellij.icons.AllIcons
import com.intellij.ide.CommonActionsManager
import com.intellij.ide.DataManager
import com.intellij.ide.TreeExpander
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.DataProvider
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.openapi.ui.VerticalFlowLayout
import com.intellij.ui.*
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.panels.Wrapper
import com.intellij.ui.treeStructure.Tree
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import com.intellij.util.ui.table.ComponentsListFocusTraversalPolicy
import com.intellij.util.ui.tree.TreeUtil
import link.kotlin.scripts.Category
import link.kotlin.scripts.Link
import link.kotlin.scripts.ProjectLinks
import net.miginfocom.swing.MigLayout
import java.awt.BorderLayout
import java.awt.Component
import java.awt.FlowLayout
import java.awt.Font
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.SwingConstants
import javax.swing.event.DocumentEvent
import javax.swing.text.BadLocationException
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.TreeSelectionModel

/**
 * Created by Roger™
 */
class AwesomeKtToolWindowContent: DataProvider {

    private var rootPanel: JPanel = JPanel(BorderLayout())
    private var myTree: Tree = Tree()
    private var myDetailPanel: JPanel = JPanel(VerticalFlowLayout(VerticalFlowLayout.LEFT, 0, 2, true, false))
    private lateinit var myProject: Project
    private var currentLink: Link? = null

    fun init(project: Project) {
        myProject = project
        setTreeView()
        subscribeEvents()
        DataManager.registerDataProvider(rootPanel, this)
    }

    fun subscribeEvents() {
        val busConnection = myProject.messageBus.connect(myProject)
        busConnection.subscribe(AWESOME_KOTLIN_VIEW_TOPIC, object : TableViewListener {
            override fun onLinkItemClicked(link: Link?) {
                currentLink = link
                myDetailPanel.removeAll()
                link?.let {
                    val headerPanel = JPanel(FlowLayout(FlowLayout.LEFT, 0, 0))
                    val linkLabel = HyperlinkLabel()
                    headerPanel.add(linkLabel)
                    linkLabel.setHtmlText("<a href=\"#link\"><b>${it.name}</b></a>")
                    if (it.href.isNotBlank()) {
                        linkLabel.setHyperlinkTarget(it.href)
                    }
                    linkLabel.font = Font(JLabel().font.fontName, Font.BOLD, JBUI.scale(13))

                    if (it.star > 0) {
                        val starLabel = JBLabel(it.star.toString())
                        starLabel.icon = AllIcons.Ide.Rating
                        starLabel.horizontalTextPosition = SwingConstants.LEFT
                        starLabel.border = IdeBorderFactory.createEmptyBorder(0, 8, 0, 15)
                        headerPanel.add(starLabel)
                    }

                    headerPanel.border = IdeBorderFactory.createEmptyBorder(5, 2, 5, 0)
                    headerPanel.background = UIUtil.getLabelBackground()
                    myDetailPanel.add(headerPanel)

                    if (it.update.isNotBlank()) {
                        val updateLabel = JBLabel("Last update: ${it.update}")
                        updateLabel.foreground = MyUISettings.instance.passedColor
                        updateLabel.border = IdeBorderFactory.createEmptyBorder(0, 2, 0, 0)
                        myDetailPanel.add(updateLabel)
                    }

                    val desc = MyHtmlPanel()
                    desc.text = it.desc
                    desc.background = UIUtil.getTableBackground()
                    desc.border = IdeBorderFactory.createEmptyBorder(0, 2, 0, 0)
                    myDetailPanel.add(desc)
                }
                myDetailPanel.repaint()
                myDetailPanel.revalidate()
            }
        })
    }

    fun setTreeModel(links: List<Category>): DefaultTreeModel {
        val root = DefaultMutableTreeNode()
        links.forEach { category ->
            val cNode = DefaultMutableTreeNode(category)
            category.subcategories.forEach { subCategory ->
                val subCNode = DefaultMutableTreeNode(subCategory)
                cNode.add(subCNode)
                subCategory.links.forEach { link ->
                    val linkNode = DefaultMutableTreeNode(link)
                    subCNode.add(linkNode)
                }
            }
            root.add(cNode)
        }
        return DefaultTreeModel(root)
    }

    fun setTreeView() {
        val model = setTreeModel(ProjectLinks.categories)
        myTree.model = model
        myTree.isRootVisible = false
        myTree.selectionModel.selectionMode = TreeSelectionModel.SINGLE_TREE_SELECTION
        myTree.cellRenderer = MyTreeRenderer(myTree)
        myTree.rowHeight = 0
        UIUtil.setLineStyleAngled(myTree)
        model.root?.let {
            TreeUtil.expand(myTree, 0)
        }
        SmartExpander.installOn(myTree)
        myTree.addTreeSelectionListener {
            val nodeObj = (it.path.lastPathComponent as DefaultMutableTreeNode).userObject
            val item = nodeObj as? Link
            myProject.messageBus.syncPublisher(AWESOME_KOTLIN_VIEW_TOPIC).onLinkItemClicked(item)
        }
    }

    fun createToolWindow(): SimpleToolWindowPanel {
        val panel = SimpleToolWindowPanel(true, true)
        val pane = ScrollPaneFactory.createScrollPane(myTree, SideBorder.TOP)
        rootPanel.add(pane, BorderLayout.CENTER)

        // Avoid default focus on SearchField
        rootPanel.isFocusCycleRoot = true
        rootPanel.focusTraversalPolicy = object : ComponentsListFocusTraversalPolicy() {
            override fun getOrderedComponents(): List<Component> {
                return listOf(myTree)
            }
        }

        myDetailPanel.border = IdeBorderFactory.createEmptyBorder(15, 10, 20, 10)
        myDetailPanel.background = UIUtil.getTableBackground()

        val splitter = OnePixelSplitter(true, 0.75f)
        splitter.firstComponent = rootPanel
        splitter.secondComponent = myDetailPanel

        panel.setToolbar(buildCategoryToolbar())
        panel.setContent(splitter)

        return panel
    }

    private fun buildCategoryToolbar(): JComponent {
        val actionManager = CommonActionsManager.getInstance()
        val collapseAction = actionManager.createCollapseAllAction(object : TreeExpander {
            override fun collapseAll() {
                TreeUtil.collapseAll(myTree, 1)
            }

            override fun canExpand() = true
            override fun expandAll() {}
            override fun canCollapse() = true
        }, myTree)
        val group = DefaultActionGroup()
        group.add(VcsCheckoutAction())
        group.addSeparator()
        group.add(collapseAction)
        group.add(WebAction())
        group.add(SettingsAction())
        val toolbar = ActionManager.getInstance().createActionToolbar(ActionPlaces.TOOLBAR, group, true)
        toolbar.setTargetComponent(rootPanel)

        val searchField = object : SearchTextField(true) {}
        searchField.addDocumentListener(object : DocumentAdapter() {
            override fun textChanged(e: DocumentEvent?) {
                e?.let {
                    val searchText = getText(e)
                    myTree.model = setTreeModel(ProjectLinks.search(searchText))
                    if (searchText.isNotBlank()) {
                        TreeUtil.expandAll(myTree)
                    }
                }
            }

            private fun getText(e: DocumentEvent): String {
                return try {
                    e.document.getText(0, e.document.length).trim()
                } catch (ex: BadLocationException) {
                    ""
                }
            }
        })
        val wrapper = Wrapper(searchField)
        wrapper.setVerticalSizeReferent(toolbar.component)
        wrapper.border = JBUI.Borders.emptyLeft(5)

        val panel = JPanel(MigLayout("ins 0, fill", "[left]0[left, fill]push[right]", "center"))
        panel.add(wrapper)
        panel.add(toolbar.component)

        return panel
    }

    override fun getData(dataId: String?): Any? {
        if (MyDataKeys.tableItem.`is`(dataId)) {
            return currentLink
        }
        return null
    }
}