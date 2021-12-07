package com.intellij.awesomeKt.view

import com.intellij.awesomeKt.action.RefreshAction
import com.intellij.awesomeKt.action.SettingsAction
import com.intellij.awesomeKt.action.VcsCheckoutAction
import com.intellij.awesomeKt.action.ViewReadmeAction
import com.intellij.awesomeKt.app.AkData
import com.intellij.awesomeKt.messages.AWESOME_KOTLIN_REFRESH_TOPIC
import com.intellij.awesomeKt.messages.AWESOME_KOTLIN_VIEW_TOPIC
import com.intellij.awesomeKt.messages.RefreshItemsListener
import com.intellij.awesomeKt.messages.TableViewListener
import com.intellij.awesomeKt.util.AkDataKeys
import com.intellij.awesomeKt.util.AkIntelliJUtil
import com.intellij.awesomeKt.util.Constants
import com.intellij.awesomeKt.util.ProjectLinks
import com.intellij.icons.AllIcons
import com.intellij.ide.BrowserUtil
import com.intellij.ide.CommonActionsManager
import com.intellij.ide.DataManager
import com.intellij.ide.TreeExpander
import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.progress.PerformInBackgroundOption
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
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
import link.kotlin.scripts.dsl.Category
import link.kotlin.scripts.model.Link
import net.miginfocom.swing.MigLayout
import java.awt.BorderLayout
import java.awt.Component
import java.awt.Font
import java.awt.event.MouseEvent
import javax.swing.*
import javax.swing.event.DocumentEvent
import javax.swing.text.BadLocationException
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.TreePath
import javax.swing.tree.TreeSelectionModel

/**
 * Created by Roger™
 */
class AkToolWindowContent(val project: Project) : DataProvider {

    private var rootPanel: JPanel = JPanel(BorderLayout())
    private var myTree: Tree = Tree()
    private var myDetailPanel: JPanel = JPanel(VerticalFlowLayout(VerticalFlowLayout.LEFT, 0, 2, true, false))
    private var currentLink: Link? = null

    init {
        setTreeView()
        subscribeEvents()
        DataManager.registerDataProvider(rootPanel, this)
    }

    private fun subscribeEvents() {
        val busConnection = project.messageBus.connect(project)
        busConnection.subscribe(AWESOME_KOTLIN_VIEW_TOPIC, object : TableViewListener {
            override fun onLinkItemClicked(link: Link?) {
                currentLink = link
                myDetailPanel.removeAll()
                link?.let {
                    val linkLabel = HoverHyperlinkLabel(it.name)
                    if (!link.href.isNullOrBlank()) {
                        linkLabel.addHyperlinkListener {
                            BrowserUtil.browse(link.href.trim())
                        }
                    }
                    linkLabel.font = Font(JLabel().font.fontName, Font.BOLD, JBUI.scale(13))
                    linkLabel.border = JBUI.Borders.empty(5, 0, 5, 0)
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
                if (PropertiesComponent.getInstance().getBoolean(Constants.Properties.refreshBtnBusyKey, false)) return
                PropertiesComponent.getInstance().setValue(Constants.Properties.refreshBtnBusyKey, true)

                ApplicationManager.getApplication().invokeLater { myTree.setPaintBusy(true) }
                ProgressManager.getInstance().run(object : Task.Backgroundable(
                    project,
                    "Update Content...",
                    false,
                    PerformInBackgroundOption.ALWAYS_BACKGROUND
                ) {
                    override fun run(indicator: ProgressIndicator) {
                        indicator.fraction = 0.1
                        indicator.text = "Fetching links..."
                        val results = ProjectLinks.instance.linksFromPlugin().toMutableList()
                        AkData.instance.links = results.mapNotNull { it.category }
                        ApplicationManager.getApplication().invokeLater {
                            if (results.all { it.success }) {
                                AkIntelliJUtil.successNotification(project, "Update Success", null)
                            } else {
                                val title =
                                    "Update Finished: ${results.count { it.success }} success, ${results.count { !it.success }} fail"
                                val errorContent = results.filter { !it.success }.joinToString("\n") {
                                    "${it.url} : ${it.errMessage}"
                                }
                                AkIntelliJUtil.errorNotification(project, errorContent, null, title)
                            }
                            myTree.model = setTreeModel(AkData.instance.links)
                            myTree.setPaintBusy(false)
                            PropertiesComponent.getInstance().setValue(Constants.Properties.refreshBtnBusyKey, false)
                        }
                    }
                })
            }
        })
    }

    private fun setGithubDetail(link: Link) {
        if (!link.github.isNullOrBlank()) {
            val panel = JPanel()
            panel.border = JBUI.Borders.empty(10, 0, 0, 0)
            panel.layout = BoxLayout(panel, BoxLayout.Y_AXIS)
            panel.background = UIUtil.getTableBackground()

            val hintLabel = JBLabel("Updating repository details...")
            hintLabel.icon = AllIcons.Process.Step_1
            hintLabel.foreground = UIUtil.getLabelFontColor(UIUtil.FontColor.BRIGHTER)
            panel.add(hintLabel)
            myDetailPanel.add(panel)
            myDetailPanel.repaint()
            myDetailPanel.revalidate()

            ProgressManager.getInstance().run(object : Task.Backgroundable(
                project,
                "Fetching GitHub Stars...",
                true,
                ALWAYS_BACKGROUND
            ) {
                override fun run(indicator: ProgressIndicator) {
                    try {
                        val gitHubLink = ProjectLinks.instance.getGithubStarCount(link)
                        if (gitHubLink.link != currentLink) return

                        ApplicationManager.getApplication().invokeAndWait({
                            panel.remove(hintLabel)

                            if (gitHubLink.homepage.isNotBlank()) {
                                val homepage = HyperlinkLabel(gitHubLink.homepage)
                                homepage.setHyperlinkTarget(gitHubLink.homepage)
                                homepage.alignmentX = Component.LEFT_ALIGNMENT
                                panel.add(homepage)
                            }

                            gitHubLink.link?.star?.let {
                                val starLabel =
                                    JBLabel("Star $it, Fork ${gitHubLink.forkCount}, Watch ${gitHubLink.watchCount}")
                                starLabel.icon = AkIcons.STAR
                                starLabel.foreground = UIUtil.getLabelFontColor(UIUtil.FontColor.BRIGHTER)
                                starLabel.alignmentX = Component.LEFT_ALIGNMENT
                                panel.add(starLabel)
                            }

                            gitHubLink.link?.update?.let {
                                val updateLabel = JBLabel("Updated on $it", SwingConstants.LEFT)
                                updateLabel.icon = AkIcons.CHANGES
                                updateLabel.foreground = UIUtil.getLabelFontColor(UIUtil.FontColor.BRIGHTER)
                                updateLabel.alignmentX = Component.LEFT_ALIGNMENT
                                panel.add(updateLabel)
                            }

                            if (gitHubLink.createdAt.isNotBlank()) {
                                val createLabel = JBLabel("Created on ${gitHubLink.createdAt}", SwingConstants.LEFT)
                                createLabel.icon = AkIcons.CREATED
                                createLabel.foreground = UIUtil.getLabelFontColor(UIUtil.FontColor.BRIGHTER)
                                createLabel.alignmentX = Component.LEFT_ALIGNMENT
                                panel.add(createLabel)
                            }

                            if (panel.componentCount > 0) {
                                myDetailPanel.add(panel)
                                myDetailPanel.repaint()
                                myDetailPanel.revalidate()
                            }
                        }, ModalityState.stateForComponent(myDetailPanel))
                    } catch (ex: RuntimeException) {
                        AkIntelliJUtil.errorNotification(project, ex.message.orEmpty())
                    }

                }
            })

        } else {
            val hrefLabel = HyperlinkLabel(link.href?.trim().orEmpty())
            hrefLabel.setHyperlinkTarget(link.href?.trim().orEmpty())
            hrefLabel.alignmentX = Component.LEFT_ALIGNMENT
            myDetailPanel.add(hrefLabel)
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
        myTree.toolTipText = "Double click to show project's README"
        myTree.cellRenderer = AkTreeRenderer()
        myTree.rowHeight = 0
        model.root?.let {
            TreeUtil.expand(myTree, 0)
        }
        SmartExpander.installOn(myTree)
        myTree.addTreeSelectionListener {
            val item = (myTree.lastSelectedPathComponent as? DefaultMutableTreeNode)?.userObject
            if (item is Link) {
                project.messageBus.syncPublisher(AWESOME_KOTLIN_VIEW_TOPIC).onLinkItemClicked(item)
            }
        }
        val actionGroups = DefaultActionGroup()
        actionGroups.addAction(VcsCheckoutAction())
        actionGroups.addAction(ViewReadmeAction())
        PopupHandler.installPopupHandler(myTree, actionGroups, ActionPlaces.UNKNOWN, ActionManager.getInstance())

        object : DoubleClickListener() {
            override fun onDoubleClick(event: MouseEvent): Boolean {
                val item = (myTree.lastSelectedPathComponent as? DefaultMutableTreeNode)?.userObject
                if (item is Link) {
                    val action = ViewReadmeAction()
                    action.actionPerformed(
                        AnActionEvent.createFromAnAction(
                            action,
                            event,
                            ActionPlaces.UNKNOWN,
                            DataManager.getInstance().getDataContext(myTree)
                        )
                    )
                }
                return true
            }
        }.installOn(myTree)
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

        myDetailPanel.border = JBUI.Borders.empty(15, 10, 20, 10)
        myDetailPanel.background = UIUtil.getTableBackground()

        val splitter = OnePixelSplitter(true, 0.75f)
        splitter.firstComponent = rootPanel
        splitter.secondComponent = myDetailPanel

        panel.toolbar = buildCategoryToolbar()
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
        group.add(SettingsAction())
        val toolbar = ActionManager.getInstance().createActionToolbar(ActionPlaces.TOOLBAR, group, true)
        toolbar.setTargetComponent(rootPanel)

        val searchField = object : SearchTextField(true) {}
        searchField.addDocumentListener(object : DocumentAdapter() {
            override fun textChanged(e: DocumentEvent) {
                val searchText = getText(e)
                val selectedNode = myTree.selectionPath?.lastPathComponent as? DefaultMutableTreeNode
                myTree.model = setTreeModel(ProjectLinks.instance.search(searchText))
                if (searchText.isNotBlank()) {
                    TreeUtil.expandAll(myTree)
                } else {
                    // clear selection event triggered
                    // FIXME: keep the selection path
                    selectedNode?.let { myTree.makeVisible(TreePath(it.path).parentPath) }
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

    override fun getData(dataId: String): Any? {
        if (AkDataKeys.tableItem.`is`(dataId)) {
            return currentLink
        }
        return null
    }
}