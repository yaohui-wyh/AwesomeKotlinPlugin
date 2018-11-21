package com.intellij.awesomeKt.view

import com.intellij.awesomeKt.action.RefreshAction
import com.intellij.awesomeKt.action.SettingsAction
import com.intellij.awesomeKt.action.VcsCheckoutAction
import com.intellij.awesomeKt.action.WebAction
import com.intellij.awesomeKt.configurable.AkData
import com.intellij.awesomeKt.configurable.AkSettings
import com.intellij.awesomeKt.configurable.ContentSource
import com.intellij.awesomeKt.messages.AWESOME_KOTLIN_REFRESH_TOPIC
import com.intellij.awesomeKt.messages.AWESOME_KOTLIN_VIEW_TOPIC
import com.intellij.awesomeKt.messages.RefreshItemsListener
import com.intellij.awesomeKt.messages.TableViewListener
import com.intellij.awesomeKt.util.AkDataKeys
import com.intellij.awesomeKt.util.AkIntelliJUtil
import com.intellij.awesomeKt.util.Constants
import com.intellij.ide.BrowserUtil
import com.intellij.ide.CommonActionsManager
import com.intellij.ide.DataManager
import com.intellij.ide.TreeExpander
import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.DataProvider
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.application.ApplicationManager
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
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import link.kotlin.scripts.Category
import link.kotlin.scripts.LinkType
import link.kotlin.scripts.ProjectLinks
import link.kotlin.scripts.model.Link
import net.miginfocom.swing.MigLayout
import java.awt.BorderLayout
import java.awt.Component
import java.awt.Font
import javax.swing.BoxLayout
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.event.DocumentEvent
import javax.swing.text.BadLocationException
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.TreeSelectionModel

/**
 * Created by Roger™
 */
class AkToolWindowContent : DataProvider {

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

    private fun subscribeEvents() {
        val busConnection = myProject.messageBus.connect(myProject)
        busConnection.subscribe(AWESOME_KOTLIN_VIEW_TOPIC, object : TableViewListener {
            override fun onLinkItemClicked(link: Link?) {
                currentLink = link
                myDetailPanel.removeAll()
                link?.let {
                    val linkLabel = HoverHyperlinkLabel(it.name)
                    if (link.href.isNotBlank()) {
                        linkLabel.addHyperlinkListener { BrowserUtil.browse(link.href) }
                    }
                    linkLabel.font = Font(JLabel().font.fontName, Font.BOLD, JBUI.scale(13))
                    linkLabel.border = IdeBorderFactory.createEmptyBorder(5, 0, 5, 0)
                    myDetailPanel.add(linkLabel)

                    val desc = AkHtmlPanel()
                    desc.text = it.desc
                    desc.background = UIUtil.getTableBackground()
                    myDetailPanel.add(desc)

                    setGithubDetail(link)
                }
                myDetailPanel.repaint()
                myDetailPanel.revalidate()
            }
        })

        busConnection.subscribe(AWESOME_KOTLIN_REFRESH_TOPIC, object : RefreshItemsListener {
            override fun onRefresh() {
                if (AkSettings.instance.contentSource == ContentSource.PLUGIN) {
                    AkData.instance.links = ProjectLinks.linksFromPlugin()
                    myTree.model = setTreeModel(AkData.instance.links)
                }
                if (AkSettings.instance.contentSource == ContentSource.GITHUB) {
                    myTree.setPaintBusy(true)
                    PropertiesComponent.getInstance().setValue(Constants.propRefreshBtnBusy, true)
                    GlobalScope.launch {
                        val links = ProjectLinks.linksFromGithub()
                        AkData.instance.links = ProjectLinks.linksFromPlugin()
                        ApplicationManager.getApplication().invokeLater {
                            myTree.model = setTreeModel(links)
                            myTree.setPaintBusy(false)
                            PropertiesComponent.getInstance().setValue(Constants.propRefreshBtnBusy, false)
                        }
                    }
                }
            }
        })
    }

    private fun setGithubDetail(link: Link) {
        if (link.type == LinkType.github) {
            GlobalScope.launch {
                val ret = ProjectLinks.getGithubStarCount(link)
                ApplicationManager.getApplication().invokeLater {
                    val panel = JPanel()
                    panel.border = IdeBorderFactory.createEmptyBorder(10, 0, 0, 0)
                    panel.layout = BoxLayout(panel, BoxLayout.Y_AXIS)
                    if (ret.star != null && ret.star!! > 0) {
                        val starLabel = JBLabel("Star ${ret.star.toString()}")
                        starLabel.icon = AkIcons.STAR
                        starLabel.foreground = AkUISettings.instance.passedColor
                        panel.add(starLabel)
                    }

                    if (!ret.update.isNullOrBlank()) {
                        val updateLabel = JBLabel("Last update ${ret.update}")
                        updateLabel.icon = AkIcons.CHANGES
                        updateLabel.foreground = AkUISettings.instance.passedColor
                        panel.add(updateLabel)
                    }

                    if (panel.componentCount > 0) {
                        panel.background = UIUtil.getTableBackground()
                        myDetailPanel.add(panel)
                        myDetailPanel.repaint()
                        myDetailPanel.revalidate()
                    }
                }
            }
        }
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

    private fun setTreeView() {
        val model = setTreeModel(AkData.instance.links)
        myTree.model = model
        myTree.isRootVisible = false
        myTree.selectionModel.selectionMode = TreeSelectionModel.SINGLE_TREE_SELECTION
        myTree.cellRenderer = AkTreeRenderer(myTree)
        myTree.rowHeight = 0
        myTree.emptyText.text = AkIntelliJUtil.message("View.emptyTable")
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
        group.add(RefreshAction())
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
        if (AkDataKeys.tableItem.`is`(dataId)) {
            return currentLink
        }
        return null
    }
}