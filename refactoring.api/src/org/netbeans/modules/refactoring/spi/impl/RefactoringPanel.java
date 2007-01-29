/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.refactoring.spi.impl;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.ref.WeakReference;
import java.util.*;
import java.util.Collection;
import javax.swing.*;
import javax.swing.ImageIcon;
import javax.swing.border.EmptyBorder;
import javax.swing.text.Position;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.project.*;
import org.netbeans.modules.refactoring.api.*;
import org.netbeans.modules.refactoring.api.impl.APIAccessor;
import org.netbeans.modules.refactoring.spi.RefactoringElementImplementation;
import org.netbeans.modules.refactoring.spi.ui.RefactoringCustomUI;
import org.netbeans.modules.refactoring.spi.ui.RefactoringUI;
import org.netbeans.modules.refactoring.spi.ui.TreeElement;
import org.netbeans.modules.refactoring.spi.ui.TreeElementFactory;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.LifecycleManager;
import org.openide.text.CloneableEditorSupport;
import org.openide.text.PositionBounds;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;
import org.openide.windows.TopComponent;

/**
 * Panel for showing proposed changes (refactoring elements) of any refactoring.
 *
 * @author  Pavel Flaska, Martin Matula
 */
public class RefactoringPanel extends JPanel implements InvalidationListener {
    
    // PRIVATE FIELDS
    /* tree contains elements which will be changed by refactoring action */
    private transient JTree tree = null;
    /* toolbar button causing refresh of the data */
    private transient JButton refreshButton = null;
    /* button lying in the toolbar allows expansion of all nodes in a tree */
    private transient JToggleButton expandButton = null;
    private JToolBar toolBar = null;

    private transient JButton refactorButton = null;
    private transient JButton cancelButton = null;
    private transient ButtonL buttonListener = null;
    private transient JButton rerunButton = null;

    private final RefactoringUI ui;
    private final boolean isQuery;
    
    private transient boolean isVisible = false;
    private transient RefactoringSession session = null;
    private transient ParametersPanel parametersPanel = null;
    private transient JScrollPane scrollPane = null; 
    private transient JPanel southPanel;
    public JSplitPane splitPane;
    private JPanel left;
    private Action callback = null;
    
    private static final int MAX_ROWS = 50;
    
    private transient JToggleButton logicalViewButton = null;
    private transient JToggleButton physicalViewButton = null;
    private transient JToggleButton customViewButton = null;
    private transient ProgressListener progressListener;

    private transient JButton prevMatch = null;
    private transient JButton nextMatch = null;
    private WeakReference refCallerTC;
    private boolean inited = false;

    
    static Image PACKAGE_BADGE = Utilities.loadImage( "org/netbeans/spi/java/project/support/ui/packageBadge.gif" ); // NOI18N
    
    public RefactoringPanel(RefactoringUI ui) {
        this(ui,null);
    }
    
    public RefactoringPanel(RefactoringUI ui, TopComponent caller) {
        if (caller!=null)
            refCallerTC = new WeakReference(caller);
        this.ui = ui;
        this.isQuery = ui.isQuery();
        refresh(true);
    }
    
    public RefactoringPanel(RefactoringUI ui, RefactoringSession session, Action callback) {
        this.session = session;
        this.ui = ui;
        this.isQuery = ui.isQuery();
        this.callback = callback;
        initialize();
        refresh(false);
    }
    
    public static void checkEventThread() {
        if (!SwingUtilities.isEventDispatchThread()) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, new IllegalStateException("This must happen in event thread!")); //NOI18N
        }
    }
    
    /* initializes all the ui */
    private void initialize() {
        if (inited)
            return ;
        checkEventThread();
        setFocusCycleRoot(true);
        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        left = new JPanel();
        splitPane.setLeftComponent(left);
        left.setLayout(new BorderLayout());
        setLayout(new BorderLayout());
        add(splitPane, BorderLayout.CENTER);
        if (!isQuery) {
            splitPane.setRightComponent(new JLabel("<Preview not Available>", SwingConstants.CENTER));
        }
        // add panel with buttons
        JButton[] buttons = getButtons();
        //if (buttons.length != 0) {
            // there will be at least one button on panel
            southPanel = new JPanel(new GridBagLayout());
            for (int i = 0; i < buttons.length; i++) {
                GridBagConstraints c = new GridBagConstraints();
                c.gridy = 0;
                c.insets = new Insets(5, 5, 5, 0);
                southPanel.add(buttons[i], c);
            }
            JPanel pp = new JPanel(new BorderLayout());
            GridBagConstraints c = new GridBagConstraints();
            c.gridy = 0;
            c.insets = new Insets(5, 5, 5, 5);
            c.weightx = 1;
            c.fill = c.HORIZONTAL;
            southPanel.add(pp, c);
            
            if (!isQuery|| callback != null) {
                left.add(southPanel, BorderLayout.SOUTH);
            }
        //}
        // put the toolbar to the panel. If the getToolBar() returns null,
        // suppose the toolbar does not exist.
        JToolBar toolBar = getToolBar();
        if (toolBar != null)
            left.add(toolBar, BorderLayout.WEST);
        validate();
        inited=true;
    }

    /**
     * Returns the toolbar. In this default implementation, toolbar is
     * oriented vertically in the west and contains 'expand tree' toggle
     * button and refresh button.
     * Override this method and return null if you do not want toolbar
     * in your panel.
     * 
     * @return  toolBar with actions for refactoring panel
     */
    private JToolBar getToolBar() {
        checkEventThread();
        refreshButton = new JButton(
            new ImageIcon(Utilities.loadImage(
            "org/netbeans/modules/refactoring/api/resources/refresh.png")) // NOI18N
        );
        Dimension dim = new Dimension(24, 24);
        refreshButton.setMaximumSize(dim);
        refreshButton.setMinimumSize(dim);
        refreshButton.setPreferredSize(dim);
        refreshButton.setToolTipText(
            NbBundle.getMessage(RefactoringPanel.class, "HINT_refresh") // NOI18N
        );
        refreshButton.setMnemonic(
            NbBundle.getMessage(RefactoringPanel.class, "MNEM_refresh").charAt(0)
        );
        refreshButton.addActionListener(getButtonListener());
        // expand button settings
        expandButton = new JToggleButton(
            new ImageIcon(Utilities.loadImage(
            "org/netbeans/modules/refactoring/api/resources/expandTree.png")) // NOI18N
        );
        expandButton.setSelectedIcon(
            new ImageIcon(Utilities.loadImage(
            "org/netbeans/modules/refactoring/api/resources/colapseTree.png")) // NOI18N
        );
        expandButton.setMaximumSize(dim);
        expandButton.setMinimumSize(dim);
        expandButton.setPreferredSize(dim);
        expandButton.setSelected(true);
        expandButton.setToolTipText(
            NbBundle.getMessage(RefactoringPanel.class, "HINT_expandAll") // NOI18N
        );
        expandButton.setMnemonic(
            NbBundle.getMessage(RefactoringPanel.class, "MNEM_expandAll").charAt(0) // NOI18N
        );
        expandButton.addActionListener(getButtonListener());
        // create toolbar
        toolBar = new JToolBar(JToolBar.VERTICAL);
        toolBar.setFloatable(false); 
        
        logicalViewButton = new JToggleButton(
            new ImageIcon(Utilities.loadImage(
            "org/netbeans/modules/refactoring/api/resources/logical_view.png")) // NOI18N
        );
        
        logicalViewButton.setMaximumSize(dim);
        logicalViewButton.setMinimumSize(dim);
        logicalViewButton.setPreferredSize(dim);
        logicalViewButton.setSelected(currentView==LOGICAL);
        logicalViewButton.setToolTipText(
            NbBundle.getMessage(RefactoringPanel.class, "HINT_logicalView") // NOI18N
        );
        logicalViewButton.setMnemonic(
            NbBundle.getMessage(RefactoringPanel.class, "MNEM_logicalView").charAt(0) // NOI18N
        );
        logicalViewButton.addActionListener(getButtonListener());

        physicalViewButton = new JToggleButton(
            new ImageIcon(Utilities.loadImage(
            "org/netbeans/modules/refactoring/api/resources/file_view.png")) // NOI18N
        );
        
        physicalViewButton.setMaximumSize(dim);
        physicalViewButton.setMinimumSize(dim);
        physicalViewButton.setPreferredSize(dim);
        physicalViewButton.setSelected(currentView==PHYSICAL);
        physicalViewButton.setToolTipText(
            NbBundle.getMessage(RefactoringPanel.class, "HINT_physicalView") // NOI18N
        );
        physicalViewButton.setMnemonic(
            NbBundle.getMessage(RefactoringPanel.class, "MNEM_physicalView").charAt(0) // NOI18N
        );
        physicalViewButton.addActionListener(getButtonListener());

        
        if (ui instanceof RefactoringCustomUI) {
            customViewButton = new JToggleButton(((RefactoringCustomUI)ui).getCustomIcon());
            customViewButton.setMaximumSize(dim);
            customViewButton.setMinimumSize(dim);
            customViewButton.setPreferredSize(dim);
            customViewButton.setSelected(currentView==GRAPHICAL);
            customViewButton.setToolTipText(((RefactoringCustomUI)ui).getCustomToolTip());
            customViewButton.addActionListener(getButtonListener());
        }
        
        nextMatch = new JButton(
            new ImageIcon(Utilities.loadImage(
            "org/netbeans/modules/refactoring/api/resources/nextmatch.png")) // NOI18N
        );
        
        nextMatch.setMaximumSize(dim);
        nextMatch.setMinimumSize(dim);
        nextMatch.setPreferredSize(dim);
        nextMatch.setToolTipText(
            NbBundle.getMessage(RefactoringPanel.class, "HINT_nextMatch") // NOI18N
        );
        nextMatch.addActionListener(getButtonListener());

        prevMatch = new JButton(
            new ImageIcon(Utilities.loadImage(
            "org/netbeans/modules/refactoring/api/resources/prevmatch.png")) // NOI18N
        );
        
        prevMatch.setMaximumSize(dim);
        prevMatch.setMinimumSize(dim);
        prevMatch.setPreferredSize(dim);
        prevMatch.setToolTipText(
            NbBundle.getMessage(RefactoringPanel.class, "HINT_prevMatch") // NOI18N
        );
        prevMatch.addActionListener(getButtonListener());
        
        toolBar.add(refreshButton);
        toolBar.add(expandButton);
        toolBar.add(logicalViewButton);
        toolBar.add(physicalViewButton);
        if (ui instanceof RefactoringCustomUI) {
            toolBar.add(customViewButton);
        }
        toolBar.add(prevMatch);
        toolBar.add(nextMatch);
        
        return toolBar;
    }
    
    /**
     * Returns array of available buttons. Initially, it returns only
     * basic "do refactoring/cancel refactoring" button. Override this method, 
     * if you want to provide any other buttons with different action to be 
     * performed.
     *
     * @return  array of avasilable buttons.
     */
    private JButton[] getButtons() {
        checkEventThread();
        if (isQuery) {
            refactorButton = null;
            if (callback==null) {
                return new JButton[] {};
            } else {
                rerunButton = new JButton((String) callback.getValue(callback.NAME)); // NOI18N
                rerunButton.addActionListener(getButtonListener());
                return new JButton[] {rerunButton};
            }
        } else {
            refactorButton = new JButton(NbBundle.getMessage(RefactoringPanel.class, "LBL_DoRefactor")); // NOI18N
            refactorButton.setToolTipText(NbBundle.getMessage(RefactoringPanel.class, "HINT_DoRefactor")); // NOI18N
            refactorButton.setMnemonic(NbBundle.getMessage(RefactoringPanel.class, "MNEM_DoRefactor").charAt(0));
            refactorButton.addActionListener(getButtonListener());
            cancelButton = new JButton(NbBundle.getMessage(RefactoringPanel.class, "LBL_CancelRefactor")); // NOI18N
            cancelButton.setMnemonic(NbBundle.getMessage(RefactoringPanel.class, "MNEM_CancelRefactor").charAt(0));
            cancelButton.setToolTipText(NbBundle.getMessage(RefactoringPanel.class, "HINT_CancelRefactor")); // NOI18N
            cancelButton.addActionListener(getButtonListener());
            return new JButton[] {refactorButton, cancelButton};
        }
    }
    
    private static final byte LOGICAL = 0;
    private static final byte PHYSICAL = 1;
    private static final byte GRAPHICAL = 2;
    
    private byte currentView = PHYSICAL;

    void switchToLogicalView() {
        logicalViewButton.setSelected(true);
        if (currentView == LOGICAL)
            return ;
        currentView = LOGICAL;
        physicalViewButton.setSelected(false);
        if (customViewButton!=null) {
            customViewButton.setSelected(false);
            prevMatch.setEnabled(true);
            nextMatch.setEnabled(true);
            expandButton.setEnabled(true);
        }
        refresh(false);
    }
    
    void switchToPhysicalView() {
        physicalViewButton.setSelected(true);
        if (currentView == PHYSICAL)
            return ;
        currentView = PHYSICAL;
        logicalViewButton.setSelected(false);
        if (customViewButton!=null) {
            customViewButton.setSelected(false);
            prevMatch.setEnabled(true);
            nextMatch.setEnabled(true);
            expandButton.setEnabled(true);
        }
        refresh(false);
    }

    void switchToCustomView() {
        customViewButton.setSelected(true);
        if (currentView == GRAPHICAL)
            return ;
        currentView = GRAPHICAL;
        logicalViewButton.setSelected(false);
        physicalViewButton.setSelected(false);
        prevMatch.setEnabled(false);
        nextMatch.setEnabled(false);
        expandButton.setEnabled(false);
        refresh(false);
    }
    
    private CheckNode createNode(TreeElement representedObject, HashMap nodes, CheckNode root) {
        //checkEventThread();
        boolean isLogical = currentView == LOGICAL;
        
        CheckNode node = null;
        if (representedObject instanceof SourceGroup) {
            //workaround for issue 52541
            node = (CheckNode) nodes.get(((SourceGroup) representedObject).getRootFolder());
        } else {
            node = (CheckNode) nodes.get(representedObject);
        }
        if (node != null) {
            return node;
        }
        
        TreeElement parent = representedObject.getParent(isLogical);
        String displayName = representedObject.getText(isLogical);
        Icon icon = representedObject.getIcon();
        
        boolean cannotEnable = false;
        
        node = new CheckNode(representedObject, displayName, icon);
        if (cannotEnable)
            node.setDisabled();
        CheckNode parentNode = parent == null ? root : createNode(parent, nodes, root);
        parentNode.add(node);
        
        if (representedObject instanceof SourceGroup) {
            //workaround for issue 52541
            nodes.put(((SourceGroup) representedObject).getRootFolder(), node);
        } else {
            nodes.put(representedObject, node);
        }
        return node;
    }
    
    private static final String getString(String key) {
        return NbBundle.getMessage(RefactoringPanel.class, key);
    }

    /**
     * Overrides default ExplorerPanel behaviour. Does nothing now.
     */
    protected void updateTitle() {
    }
    
    /**
     * Method is responsible for making changes in sources.
     */
    private void refactor() {
        checkEventThread();
        disableComponents(RefactoringPanel.this);
        progressListener = new ProgressL();
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                try {
                    session.addProgressListener(progressListener);
                    session.doRefactoring(true);
                } finally {
                    session.removeProgressListener(progressListener);
                    progressListener = null;
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            RefactoringPanel.this.close();
                        }
                    });
                }
            }
        });
    }

    /**
     * Cancel refactor action. This default implementation is closing window
     * only. It can return result state. In this implementation it returns
     * everytime 0.
     *
     * @return  result of cancel operation. Zero represent successful cancel.
     */
    private int cancel() {
        checkEventThread();
        this.close();
        return 0;
    }
    
    void close() {
        if (isQuery) {
            RefactoringPanelContainer.getUsagesComponent().removePanel(this);
        } else {
            RefactoringPanelContainer.getRefactoringComponent().removePanel(this);
        }
        closeNotify();
    }
    
    
    /*
     * Initializes button listener. The subclasses must not need this listener.
     * This is the reason of lazy initialization.
     */
    private ButtonL getButtonListener() {
        if (buttonListener == null)
            buttonListener = new ButtonL();
        
        return buttonListener;
    }

    RequestProcessor rp = new RequestProcessor();

    /* expandAll nodes in the tree */
    public void expandAll() { 
        checkEventThread();
        final Cursor old = getCursor();
        expandButton.setEnabled(false);
        scrollPane.getViewport().remove(tree);
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        rp.post(new Runnable() {
            public void run() {
                int row = 0;
                while (row < tree.getRowCount()) {
                    tree.expandRow(row);
                    row++;
                }
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        scrollPane.setViewportView(tree);
                        setCursor(old);
                        expandButton.setEnabled(true);
                        expandButton.setToolTipText(
                            NbBundle.getMessage(RefactoringPanel.class, "HINT_collapseAll") // NOI18N
                        );
                        requestFocus();
                    }
                });
            }});
    } 

    /* collapseAll nodes in the tree */
    public void collapseAll() {
        checkEventThread();
        expandButton.setEnabled(false);
        final Cursor old = getCursor();
        scrollPane.getViewport().remove(tree);
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        rp.post(new Runnable() {
            public void run() {
                int row = tree.getRowCount() - 1;
                while (row > 0) {
                    tree.collapseRow(row);
                    row--;
                }
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        scrollPane.setViewportView(tree);
                        setCursor(old);
                        expandButton.setEnabled(true);
                        expandButton.setToolTipText(
                            NbBundle.getMessage(RefactoringPanel.class, "HINT_expandAll") // NOI18N
                        );
                        requestFocus();
                    }
                });
            }});
    }
    
    public void invalidateObject() {
        if (isQuery) {
            return;
        }
        Runnable invalidate = new Runnable() {
            public void run() {
                setRefactoringEnabled(false, false);
            }
        };
        if (SwingUtilities.isEventDispatchThread()) {
            invalidate.run();
        } else {
            SwingUtilities.invokeLater(invalidate);
        }
    }
    private void refresh(final boolean showParametersPanel) {
        checkEventThread();
        if (!isQuery)
            LifecycleManager.getDefault().saveAll();
        
        if (showParametersPanel) {
            // create parameters panel for refactoring
            if (parametersPanel == null) {
                parametersPanel = new ParametersPanel(ui);
            }
            // show parameters dialog
            RefactoringSession tempSession = parametersPanel.showDialog();
            // if no elements were returned, action was either cancelled or preview
            // was skipped -> finish
            if (tempSession == null) {
                return;
            }
            
            session = tempSession;
        }
        
        initialize();

        final String description = ui.getDescription();
        setToolTipText("<html>" + description + "</html>"); // NOI18N
        final Collection elements = session.getRefactoringElements();
        setName(ui.getName());
        Component c=null;
        if (ui instanceof RefactoringCustomUI) {
            c = ((RefactoringCustomUI) ui).getCustomComponent(getImplCollection(elements));
            this.left.remove(c);
        }
        if (currentView == GRAPHICAL) {
            assert ui instanceof RefactoringCustomUI;
            assert c != null;
            RefactoringCustomUI cui = (RefactoringCustomUI) ui;
            this.left.remove(scrollPane);
            this.left.add(c, BorderLayout.CENTER);
            tree=null;
            this.repaint();
        } else {
            
            final ProgressHandle progressHandle = ProgressHandleFactory.createHandle(NbBundle.getMessage(RefactoringPanel.class, isQuery ? "LBL_PreparingUsagesTree":"LBL_PreparingRefactoringTree"));
            
            RequestProcessor.getDefault().post(new Runnable() {
                public void run() {
                    HashSet editorSupports = new HashSet();
                    int errorsNum = 0;
                    if (!isQuery) {
                        for (Iterator iter = elements.iterator(); iter.hasNext(); ) {
                            RefactoringElement elem = (RefactoringElement) iter.next();
                            if (elem.getStatus() == RefactoringElement.GUARDED || elem.getStatus() == RefactoringElement.READ_ONLY) {
                                errorsNum++;
                            }
                        }
                    }
                    int occurencesNum = elements.size();
                    StringBuffer errorsDesc = new StringBuffer();
                    errorsDesc.append(" [" + occurencesNum); // NOI18N
                    errorsDesc.append(' ');
                    errorsDesc.append(occurencesNum == 1 ?
                        NbBundle.getMessage(RefactoringPanel.class, "LBL_Occurence") :
                        NbBundle.getMessage(RefactoringPanel.class, "LBL_Occurences")
                        );
                    if (errorsNum > 0) {
                        errorsDesc.append(',');
                        errorsDesc.append(' ');
                        errorsDesc.append("<font color=#CC0000>" + errorsNum); // NOI18N
                        errorsDesc.append(' ');
                        errorsDesc.append(errorsNum == 1 ?
                            NbBundle.getMessage(RefactoringPanel.class, "LBL_Error") :
                            NbBundle.getMessage(RefactoringPanel.class, "LBL_Errors")
                            );
                        errorsDesc.append("</font>"); // NOI18N
                    }
                    errorsDesc.append(']');
                    final CheckNode root = new CheckNode(null, description + errorsDesc.toString(), new ImageIcon(Utilities.loadImage("org/netbeans/modules/refactoring/api/resources/" + (isQuery ? "findusages.png" : "refactoring.gif"))));
                    HashMap nodes = new HashMap();
                    
                    final Cursor old = getCursor();
                    setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                    
                    progressHandle.start(elements.size()/10);
                    int i=0;
                    try {
                        //[retouche]                    JavaModel.getJavaRepository().beginTrans(false);
                        try {
                            // ui.getRefactoring().setClassPath();
                            for (Iterator it = elements.iterator(); it.hasNext();i++) {
                                RefactoringElement e = (RefactoringElement) it.next();
                                createNode(TreeElementFactory.getTreeElement(e), nodes, root);
                                PositionBounds pb = e.getPosition();
                                if (pb != null) {
                                    CloneableEditorSupport ces = pb.getBegin().getCloneableEditorSupport();
                                    editorSupports.add(ces);
                                }
                                
                                if (i % 10 == 0)
                                    progressHandle.progress(i/10);
                            }
                        } finally {
                            //[retouche]                        JavaModel.getJavaRepository().endTrans();
                        }
                        UndoManager.getDefault().watch(editorSupports, RefactoringPanel.this);
                    } catch (RuntimeException t) {
                        TreeElementFactory.cleanUp();
                        throw t;
                    } catch (Error e) {
                        TreeElementFactory.cleanUp();
                        throw e;
                    } finally {
                        progressHandle.finish();
                        setCursor(old);
                    }
                    
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            if (tree == null) {
                                // add panel with appropriate content
                                tree = new JTree(root) {
                                    public TreePath getNextMatch(String prefix, int startingRow, Position.Bias bias) {
                                        try {
                                            return super.getNextMatch(prefix,startingRow, bias);
                                        } catch (NullPointerException e) {
                                            //Work around for 43112
                                        }
                                        return null;
                                    }
                                };
                                ToolTipManager.sharedInstance().registerComponent(tree);
                                tree.setCellRenderer(new CheckRenderer(isQuery));
                                String s = NbBundle.getMessage(RefactoringPanel.class, "ACSD_usagesTree"); // NOI18N
                                tree.getAccessibleContext().setAccessibleDescription(s);
                                tree.getAccessibleContext().setAccessibleName(s);
                                CheckNodeListener l = new CheckNodeListener(isQuery);
                                tree.addMouseListener(l);
                                tree.addKeyListener(l);
                                tree.setToggleClickCount(0);
                                scrollPane = new JScrollPane(tree);
                                RefactoringPanel.this.left.add(scrollPane, BorderLayout.CENTER);
                                RefactoringPanel.this.validate();
                            } else {
                                tree.setModel(new DefaultTreeModel(root));
                            }
                            tree.setRowHeight((int) ((CheckRenderer) tree.getCellRenderer()).getPreferredSize().getHeight());
                            
                            if (showParametersPanel) {
                                splitPane.setDividerLocation(0.3);
                                if (elements.size() < MAX_ROWS) {
                                    expandAll();
                                    selectNextUsage();
                                } else
                                    expandButton.setSelected(false);
                            } else {
                                if (expandButton.isSelected()) {
                                    expandAll();
                                    selectNextUsage();
                                } else
                                    expandButton.setSelected(false);
                            }
                            
                            tree.setSelectionRow(0);
                            requestFocus();
                            setRefactoringEnabled(true, true);
                        }
                    });
                }
            });
        }
        if (!isVisible) {
            // dock it into output window area and display
            RefactoringPanelContainer cont = isQuery ? RefactoringPanelContainer.getUsagesComponent() : RefactoringPanelContainer.getRefactoringComponent();
            cont.open();
            cont.requestActive();
            cont.addPanel(this);
            isVisible = true;
        }
        setRefactoringEnabled(false, true);
    }
    
    private Collection<RefactoringElementImplementation> getImplCollection(Collection<RefactoringElement> elements) {
        Collection<RefactoringElementImplementation> result = new ArrayList();
        for (RefactoringElement e:elements) {
            result.add(APIAccessor.DEFAULT.getRefactoringElementImplementation(e));
        }
        return Collections.unmodifiableCollection(result);
        
    }
    
    public void requestFocus() {
        super.requestFocus();
        if (refactorButton != null) {
            refactorButton.requestFocus();
        } else {
            if (tree!=null)
                tree.requestFocus();
        }
    }
    
    void setRefactoringEnabled(boolean enabled, boolean isRefreshing) {
        checkEventThread();
        if (tree != null) {
            if (!enabled) {
                CheckNode c = (CheckNode) tree.getModel().getRoot();
                if (!isRefreshing) {
                    c.setNeedsRefresh();
                } else {
                    c.setDisabled();
                }
                tree.setModel(new DefaultTreeModel(c, false));
            }
//            tree.validate();
            tree.setEnabled(enabled);
            if (refactorButton != null) {
                refactorButton.setEnabled(enabled);
            }
        }
    }

    // disables all components in a given container
    private static void disableComponents(Container c) {
        checkEventThread();
        Component children[] = c.getComponents();
        for (int i = 0; i < children.length; i++) {
            if (children[i].isEnabled()) {
                children[i].setEnabled(false);
            }
            if (children[i] instanceof Container) {
                disableComponents((Container) children[i]);
            }
        }
    }
    
    void selectNextUsage() {
        selectNextPrev(true);
    }
    
    void selectPrevUsage() {
        selectNextPrev(false);
    }
    
    private void selectNextPrev(final boolean next) {
        int newRow = getSelectedRow();
        int maxcount = tree.getRowCount();
        CheckNode node;
        do {
            if (next) {
                newRow++;
                if (newRow>=maxcount)
                    newRow = 0;
            } else {
                newRow--;
                if (newRow<0)
                    newRow = maxcount-1;
            }
            TreePath path = tree.getPathForRow(newRow);
            node = (CheckNode) path.getLastPathComponent();
            if (!node.isLeaf()) {
                tree.expandRow(newRow);
                maxcount = tree.getRowCount();
            }
        }
        while (!node.isLeaf());
        tree.setSelectionRow(newRow);
        tree.scrollRowToVisible(newRow);
        if (isQuery) {
            CheckNodeListener.findInSource(node);
        } else {
            CheckNodeListener.openDiff(node);
        }
    }
    
    private int getSelectedRow() {
        int[] rows = tree.getSelectionRows();
        if (rows == null || rows.length == 0)
            return 0;
        return rows[0];
    }

    ////////////////////////////////////////////////////////////////////////////
    // INNER CLASSES
    ////////////////////////////////////////////////////////////////////////////

    private class ButtonL implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            Object o = event.getSource();
            // Cancel button pressed, remove refactoring panel
            if (o == cancelButton) {
                cancel();
            } else if (o == refactorButton) {
                refactor();
            } else if (o == rerunButton) {
                close();
                callback.actionPerformed(event);
            }
            // expandAll button selected/deselected
            else if (o == expandButton && tree != null) {
                if (expandButton.isSelected())
                    expandAll();
                else
                    collapseAll();
            } else if (o == refreshButton) {
                refresh(true);
            } else if (o == physicalViewButton) {
                switchToPhysicalView();
            } else if (o == logicalViewButton) {
                switchToLogicalView();
            } else if (o == customViewButton) {
                switchToCustomView();
            } else if (o == nextMatch) {
                selectNextUsage();
            } else if (o == prevMatch) {
                selectPrevUsage();
            }
        }
    } // end ButtonL
    ////////////////////////////////////////////////////////////////////////////

    private static String normalize(String input) {
        int size = input.length();
        char[] c = new char[size];
        input.getChars(0, size, c, 0);
        boolean wb = false;
        int pos = 0;
        char[] nc = new char[size];
        
        for (int i = 0; i < size; i++) {
            if (Character.isWhitespace(c[i])) {
                if (!wb) {
                    nc[pos++] = ' ';

                    wb = true;
                }
            }
            else {
                nc[pos++] = c[i];
                wb = false;
            }
        }
        return new String(nc, 0, pos);
    }

    /** Processes returned problems from refactoring operations and notifies
     * user (in case of non-fatal problems gives user a chance to continue or cancel).
     * @param problem Problems returned from a refactoring operation.
     * @return <code>true</code> if no fatal problems were found and user decided
     * to continue in case of non-fatal problems; <code>false</code> if there was at
     * least one fatal problem or at least one non-fatal problem in response to which
     * user decided to cancel the operation.
     */
    /* public static boolean confirmProblems(Problem problem) {
        while (problem != null) {
            int result;
            if (problem.isFatal()) {
                JOptionPane.showMessageDialog(null, problem.getMessage(), NbBundle.getMessage(ParametersPanel.class, "LBL_Error"), JOptionPane.ERROR_MESSAGE);
                return false;
            } else {
                if (JOptionPane.showConfirmDialog(
                    null, 
                    problem.getMessage() + ' ' + NbBundle.getMessage(ParametersPanel.class, "QST_Continue"),
                    NbBundle.getMessage(ParametersPanel.class, "LBL_Warning"),
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
                ) != JOptionPane.YES_OPTION) {
                    return false;
                }
            }
            problem = problem.getNext();
        }
        return true;
    } */
    
    protected void closeNotify() {
        UndoWatcher.stopWatching(this);
        if (tree!=null) {
            ToolTipManager.sharedInstance().unregisterComponent(tree);
            scrollPane.getViewport().remove(tree);
        }
        scrollPane.setViewport(null);
        if (refCallerTC != null) {
            TopComponent tc = (TopComponent) refCallerTC.get();
            if (tc != null && tc.isShowing()) {
                tc.requestActive();
            }
        }
        TreeElementFactory.cleanUp();
        //super.closeNotify();
    }

    private static class ProgressL implements ProgressListener {
        
        private ProgressHandle handle;
        private Dialog d;
        private int counter;
        
        public void start(final ProgressEvent event) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    counter = 0;
                    final String lab = NbBundle.getMessage(RefactoringPanel.class, "LBL_RefactorProgressLabel");
                    handle = ProgressHandleFactory.createHandle(lab);
                    JComponent progress = ProgressHandleFactory.createProgressComponent(handle);
                    JPanel component = new JPanel();
                    component.setLayout(new BorderLayout());
                    component.setBorder(new EmptyBorder(12,12,11,11));
                    JLabel label = new JLabel(lab);
                    label.setBorder(new EmptyBorder(0, 0, 6, 0));
                    component.add(label, BorderLayout.NORTH);
                    component.add(progress, BorderLayout.CENTER);
                    DialogDescriptor desc = new DialogDescriptor(component, NbBundle.getMessage(RefactoringPanel.class, "LBL_RefactoringInProgress"), true, new Object[]{}, null, 0, null, null);
                    desc.setLeaf(true);
                    d = DialogDisplayer.getDefault().createDialog(desc);
                    ((JDialog) d).setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
                    
                    handle.start(event.getCount());
                    d.show();
                }
            });
        }
        
        public void step(ProgressEvent event) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    try {
                        handle.progress(++counter);
                    } catch (Throwable e) {
                        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
                    }
                }
            });
        }
        
        public void stop(ProgressEvent event) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    handle.finish();
                    d.hide();
                }
            });
        }
    }
} // end Refactor Panel
