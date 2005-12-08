/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.xml.text.navigator;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.WeakHashMap;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.EditorUI;
import org.netbeans.editor.ext.ExtEditorUI;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.editor.structure.api.DocumentElement;
import org.netbeans.modules.editor.structure.api.DocumentModel;
import org.netbeans.modules.editor.structure.api.DocumentModelException;
import org.openide.ErrorManager;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.EditorCookie.Observable;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.text.CloneableEditorSupport;
import org.openide.text.DataEditorSupport;
import org.openide.util.Lookup.Template;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.UserQuestionException;
import org.openide.util.Utilities;
import org.openide.windows.TopComponent;


/** XML Navigator UI component containing a tree of XML elements.
 *
 * @author Marek Fukala
 * @version 1.0
 */
public class NavigatorContent extends JPanel implements PropertyChangeListener  {
    
    private static final boolean DEBUG = false;
    private static NavigatorContent navigatorContentInstance = null;
    
    
    public static synchronized NavigatorContent getDefault() {
        if(navigatorContentInstance == null)
            navigatorContentInstance = new NavigatorContent();
        return navigatorContentInstance;
    }
    
    //suppose we always have only one instance of the navigator panel at one time
    //so using the static fields is OK. TheeNodeAdapter is reading these two
    //fields and change it's look accordingly
    static boolean showAttributes = true;
    static boolean showContent = true;
    
    private JPanel active = null;
    private final JPanel emptyPanel;
    
    private JLabel msgLabel;
    
    private DataObject peerDO = null;
    
    private WeakHashMap uiCache = new WeakHashMap();
    
    private boolean editorOpened = false;
    
    private NavigatorContent() {
        setLayout(new BorderLayout());
        //init empty panel
        setBackground(Color.WHITE);
        emptyPanel = new JPanel();
        emptyPanel.setBackground(Color.WHITE);
        emptyPanel.setLayout(new BorderLayout());
        msgLabel = new JLabel();
        emptyPanel.add(msgLabel, BorderLayout.CENTER);
    }
    
    public void navigate(DataObject d) {
        if(peerDO != null && peerDO != d) {
            //release the original document (see closeDocument() javadoc)
            closeDocument(peerDO);
        }
        
        EditorCookie ec = (EditorCookie)d.getCookie(EditorCookie.class);
        if(ec == null) {
            ErrorManager.getDefault().log(ErrorManager.WARNING, "The DataObject " + d.getName() + "(class=" + d.getClass().getName() + ") has no EditorCookie!?");
        } else {
            try {
                if(DEBUG) System.out.println("[xml navigator] navigating to DATAOBJECT " + d.hashCode());
                //test if the document is opened in editor
                BaseDocument bdoc = (BaseDocument)ec.openDocument();
                //create & show UI
                if(bdoc != null) {
                    //there is something we can navigate in
                    navigate(d, bdoc);
                    //remember the peer dataobject to be able the call EditorCookie.close() when closing navigator
                    this.peerDO = d;
                    //check if the editor for the DO has an opened pane
                    editorOpened = ec.getOpenedPanes() != null && ec.getOpenedPanes().length > 0;
                }
                
            }catch(UserQuestionException uqe) {
                //do not open a question dialog when the document is just loaded into the navigator
                showDocumentTooLarge();
            }catch(IOException e) {
                ErrorManager.getDefault().notify(e);
            }
        }
    }
    
    public void navigate(final DataObject documentDO, final BaseDocument bdoc) {
        if(DEBUG) System.out.println("[xml navigator] navigating to DOCUMENT " + bdoc.hashCode());
        //called from AWT thread
        showScanningPanel();
        
        //try to find the UI in the UIcache
        final JPanel cachedPanel;
        WeakReference panelWR = (WeakReference)uiCache.get(documentDO);
        if(panelWR != null) {
            NavigatorContentPanel cp = (NavigatorContentPanel)panelWR.get();
            if(cp != null) {
                if(DEBUG) System.out.println("panel is cached");
                //test if the document associated with the panel is the same we got now
                cachedPanel = bdoc == cp.getDocument() ? cp : null;
                if(cachedPanel == null) {
                    if(DEBUG) System.out.println("but the document is different - creating a new UI...");
                    if(DEBUG) System.out.println("the cached document : " + cp.getDocument());
                    
                    //remove the old mapping from the cache
                    uiCache.remove(documentDO);
                }
            } else
                cachedPanel = null;
        } else
            cachedPanel = null;
        
        //get the model and create the new UI on background
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                //get document model for the file
                try {
                    final DocumentModel model;
                    if(cachedPanel == null)
                        model = DocumentModel.getDocumentModel(bdoc);
                    else
                        model = null; //if the panel is cached it holds a refs to the model - not need to init it again
                    
                    //I need to lock the model for update since during the model
                    //update the UI is updated synchronously in AWT (current thread)
                    if(model != null) {
                        model.readLock();
                        try {
                            SwingUtilities.invokeAndWait(new Runnable() {
                                public void run() {
                                    showWaitPanel();
                                    JPanel panel = null;
                                    if(cachedPanel == null) {
                                        //cache the newly created panel
                                        panel = new NavigatorContentPanel(model);
                                        //use the document dataobject as a key since the document itself is very easily discarded and hence
                                        //harly usable as a key of the WeakHashMap
                                        DataObject documentDO = NbEditorUtilities.getDataObject(bdoc);
                                        if(documentDO != null)
                                            uiCache.put(documentDO, new WeakReference(panel));
                                        if(DEBUG) System.out.println("[xml navigator] panel created");
                                        
                                        //start to listen to the document property changes - we need to get know when the document is being closed
                                        ((EditorCookie.Observable)documentDO.getCookie(EditorCookie.class)).addPropertyChangeListener(NavigatorContent.this);
                                    } else {
                                        panel = cachedPanel;
                                        if(DEBUG) System.out.println("[xml navigator] panel gotten from cache");
                                    }
                                    
                                    //paint the navigator UI
                                    removeAll();
                                    add(panel, BorderLayout.CENTER);
                                    revalidate();
                                    //panel.revalidate();
                                    repaint();
                                }
                            });
                        }catch(InterruptedException ie) {
                            ErrorManager.getDefault().notify(ErrorManager.WARNING, ie);
                        }catch(InvocationTargetException ite) {
                            ErrorManager.getDefault().notify(ErrorManager.ERROR, ite);
                        }finally {
                            model.readUnlock();
                        }
                    } else {
                        //model is null => show message
                        showCannotNavigate();
                    }
                }catch(DocumentModelException dme) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, dme);
                }
            }
        });
    }
    
    public void release() {
        removeAll();
        repaint();
        
        closeDocument(peerDO);
    }
    
    /** A hacky fix for XMLSyncSupport - I need to call EditorCookie.close when the navigator
     * is deactivated and there is not view pane for the navigated document. Then a the synchronization
     * support releases a strong reference to NbEditorDocument. */
    private void closeDocument(DataObject dobj) {
        if(dobj != null) {
            EditorCookie ec = (EditorCookie)peerDO.getCookie(EditorCookie.class);
            if(ec != null) {
                JEditorPane panes[] = ec.getOpenedPanes();
                //call EC.close() if there isn't any pane and the editor was opened
                if((panes == null || panes.length == 0)) {
                    ((EditorCookie.Observable)ec).removePropertyChangeListener(this);
                    
                    if(editorOpened) {
                        ec.close();
                        if(DEBUG) System.out.println("document instance for dataobject " + dobj.hashCode() + " closed.");
                    }
                }
                editorOpened = false;
            }
        }
    }
    
    public void showDocumentTooLarge() {
        removeAll();
        msgLabel.setForeground(Color.GRAY);
        msgLabel.setText(NbBundle.getMessage(NavigatorContent.class, "LBL_TooLarge"));
        msgLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(emptyPanel, BorderLayout.CENTER);
        repaint();
    }
    
    public void showCannotNavigate() {
        removeAll();
        msgLabel.setForeground(Color.GRAY);
        msgLabel.setText(NbBundle.getMessage(NavigatorContent.class, "LBL_CannotNavigate"));
        msgLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(emptyPanel, BorderLayout.CENTER);
        repaint();
    }
    
    private void showScanningPanel() {
        removeAll();
        msgLabel.setIcon(WAIT_ICON);
        msgLabel.setHorizontalAlignment(SwingConstants.LEFT);
        msgLabel.setForeground(Color.BLACK);
        msgLabel.setText(NbBundle.getMessage(NavigatorContent.class, "LBL_Scan"));
        add(emptyPanel, BorderLayout.NORTH);
        repaint();
    }
    
    
    private void showWaitPanel() {
        removeAll();
        msgLabel.setIcon(null);
        msgLabel.setForeground(Color.GRAY);
        msgLabel.setHorizontalAlignment(SwingConstants.LEFT);
        msgLabel.setText(NbBundle.getMessage(NavigatorContent.class, "LBL_Wait"));
        add(emptyPanel, BorderLayout.NORTH);
        repaint();
    }
    
    public void propertyChange(PropertyChangeEvent evt) {
        if(evt.getPropertyName() == EditorCookie.Observable.PROP_DOCUMENT) {
            if(evt.getNewValue() == null) {
                final DataObject dobj = ((DataEditorSupport)evt.getSource()).getDataObject();
                if(dobj != null) {
                    editorOpened = false;
                    //document is being closed
                    if(DEBUG) System.out.println("document has been closed for DO: " + dobj.hashCode());
                    
                    //remove the property change listener from the DataObject's EditorSupport
                    EditorCookie ec = (EditorCookie)dobj.getCookie(EditorCookie.class);
                    if(ec != null)
                        ((EditorCookie.Observable)ec).removePropertyChangeListener(this);
                    
                    //and navigate the document again (must be called asynchronously
                    //otherwise the ClonableEditorSupport locks itself (new call to CES from CES.propertyChange))
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            if(dobj.isValid()) navigate(dobj);
                        }
                    });
                }
            } else {
                //a new pane created
                editorOpened = true;
            }
        }
    }
    
    private class NavigatorContentPanel extends JPanel implements FiltersManager.FilterChangeListener {
        
        private JTree tree;
        private FiltersManager filters;
        private Document doc;
        
        public NavigatorContentPanel(DocumentModel dm) {
            this.doc = dm.getDocument();
            
            setLayout(new BorderLayout());
            //create the JTree pane
            tree = new PatchedJTree();
            TreeModel model = createTreeModel(dm);
            tree.setModel(model);
            //tree.setLargeModel(true);
            tree.setShowsRootHandles(true);
            tree.setRootVisible(false);
            tree.setCellRenderer(new NavigatorTreeCellRenderer());
            tree.putClientProperty("JTree.lineStyle", "Angled");
            ToolTipManager.sharedInstance().registerComponent(tree);
            
            MouseListener ml = new MouseAdapter() {
                public void mousePressed(MouseEvent e) {
                    int selRow = tree.getRowForLocation(e.getX(), e.getY());
                    if(selRow != -1) {
                        TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());
                        TreeNodeAdapter tna = (TreeNodeAdapter)selPath.getLastPathComponent();
                        if(e.getClickCount() == 2)
                            openAndFocusElement(tna, false);
                        
                        if(e.getClickCount() == 1)
                            openAndFocusElement(tna, true); //select active line only
                        
                    }
                }
            };
            tree.addMouseListener(ml);
            
            final TreeSelectionModel selectionModel = new DefaultTreeSelectionModel();
            selectionModel.setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
            tree.setSelectionModel(selectionModel);
            tree.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "open"); // NOI18N
            tree.getActionMap().put("open", new AbstractAction() { // NOI18N
                public void actionPerformed(ActionEvent e) {
                    TreePath selPath = selectionModel.getLeadSelectionPath();
                    TreeNodeAdapter tna = (TreeNodeAdapter)selPath.getLastPathComponent();
                    openAndFocusElement(tna, false);
                }
            });
            
            JScrollPane treeView = new JScrollPane(tree);
            treeView.setBorder(BorderFactory.createEmptyBorder());
            treeView.setViewportBorder(BorderFactory.createEmptyBorder());
            
            add(treeView, BorderLayout.CENTER);
            
            //create the TapPanel
            TapPanel filtersPanel = new TapPanel();
            JLabel filtersLbl = new JLabel(NbBundle.getMessage(NavigatorContent.class, "LBL_Filter")); //NOI18N
            filtersLbl.setBorder(new EmptyBorder(0, 5, 5, 0));
            filtersPanel.add(filtersLbl);
            filtersPanel.setOrientation(TapPanel.DOWN);
            // tooltip
            KeyStroke toggleKey = KeyStroke.getKeyStroke(KeyEvent.VK_T,
                    Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
            String keyText = org.openide.util.Utilities.keyToString(toggleKey);
            filtersPanel.setToolTipText(NbBundle.getMessage(NavigatorContent.class, "TIP_TapPanel", keyText));
            
            //create FiltersManager
            filters = createFilters();
            //listen to filters changes
            filters.hookChangeListener(this);
            
            filtersPanel.add(filters.getComponent());
            
            add(filtersPanel, BorderLayout.SOUTH);
            
            //add popup menu mouse listener
            MouseListener pmml = new MouseAdapter() {
                public void mousePressed(MouseEvent e) {
                    if(e.getClickCount() == 1 && e.getModifiers() == MouseEvent.BUTTON3_MASK) {
                        //show popup
                        JPopupMenu pm = new JPopupMenu();
                        JMenuItem[] items = new FilterActions(filters).createMenuItems();
                        //add filter actions
                        for(int i = 0; i < items.length; i++) pm.add(items[i]);
                        pm.pack();
                        pm.show(tree, e.getX(), e.getY());
                    }
                }
            };
            tree.addMouseListener(pmml);
            
            //expand all root elements which are tags
            TreeNode rootNode = (TreeNode)model.getRoot();
            for(int i = 0; i < rootNode.getChildCount(); i++) {
                TreeNode node = rootNode.getChildAt(i);
                if(node.getChildCount() > 0)
                    tree.expandPath(new TreePath(new TreeNode[]{rootNode, node}));
            }
        }
        
        public Document getDocument() {
            return this.doc;
        }
        
        private void openAndFocusElement(final TreeNodeAdapter selected, final boolean selectLineOnly) {
            BaseDocument bdoc = (BaseDocument)selected.getDocumentElement().getDocument();
            DataObject dobj = NbEditorUtilities.getDataObject(bdoc);
            if(dobj == null) return ;
            
            final EditorCookie.Observable ec = (EditorCookie.Observable)dobj.getCookie(EditorCookie.Observable.class);
            if(ec == null) return ;
            
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    JEditorPane[] panes = ec.getOpenedPanes();
                    if (panes != null && panes.length > 0) {
                        // editor already opened, so just select
                        selectElementInPane(panes[0], selected, !selectLineOnly);
                    } else if(!selectLineOnly) {
                        // editor not opened yet
                        ec.open();
                        try {
                            ec.openDocument(); //wait to editor to open
                            panes = ec.getOpenedPanes();
                            if (panes != null && panes.length > 0) {
                                selectElementInPane(panes[0], selected, true);
                            }
                        }catch(IOException ioe) {
                            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ioe);
                        }
                    }
                }
            });
        }
        
        private void selectElementInPane(final JEditorPane pane, final TreeNodeAdapter tna, final boolean focus) {
            RequestProcessor.getDefault().post(new Runnable() {
                public void run() {
                    pane.setCaretPosition(tna.getDocumentElement().getStartOffset());
                }
            });
            if(focus) {
                // try to activate outer TopComponent
                Container temp = pane;
                while (!(temp instanceof TopComponent)) {
                    temp = temp.getParent();
                }
                ((TopComponent) temp).requestActive();
            }
        }
        
        private TreeModel createTreeModel(DocumentModel dm) {
            DocumentElement rootElement = dm.getRootElement();
            DefaultTreeModel dtm = new DefaultTreeModel(null);
            TreeNodeAdapter rootTna = new TreeNodeAdapter(rootElement, dtm, tree, null);
            dtm.setRoot(rootTna);
            
            return dtm;
        }
        
        /** Creates filter descriptions and filters itself */
        private FiltersManager createFilters() {
            FiltersDescription desc = new FiltersDescription();
            
            desc.addFilter(ATTRIBUTES_FILTER,
                    NbBundle.getMessage(NavigatorContent.class, "LBL_ShowAttributes"),     //NOI18N
                    NbBundle.getMessage(NavigatorContent.class, "LBL_ShowAttributesTip"),     //NOI18N
                    showAttributes,
                    new ImageIcon(org.openide.util.Utilities.loadImage("org/netbeans/modules/xml/text/navigator/resources/a.png")), //NOI18N
                    null
                    );
            desc.addFilter(CONTENT_FILTER,
                    NbBundle.getMessage(NavigatorContent.class, "LBL_ShowContent"),     //NOI18N
                    NbBundle.getMessage(NavigatorContent.class, "LBL_ShowContentTip"),     //NOI18N
                    showContent,
                    new ImageIcon(org.openide.util.Utilities.loadImage("org/netbeans/modules/xml/text/navigator/resources/content.png")), //NOI18N
                    null
                    );
            
            return FiltersDescription.createManager(desc);
        }
        
        
        public void filterStateChanged(ChangeEvent e) {
            showAttributes = filters.isSelected(ATTRIBUTES_FILTER);
            showContent = filters.isSelected(CONTENT_FILTER);
            
            tree.repaint();
        }
        
        private class PatchedJTree extends JTree {
            
            private boolean firstPaint;
            
            public PatchedJTree() {
                super();
                firstPaint = true;
            }
            
            /** Overriden to calculate correct row height before first paint */
            public void paint(Graphics g) {
                if (firstPaint) {
                    int height = g.getFontMetrics(getFont()).getHeight();
                    setRowHeight(height + 2);
                    firstPaint = false;
                }
                super.paint(g);
            }
            
        }
        
        public static final String ATTRIBUTES_FILTER = "attrs";
        public static final String CONTENT_FILTER = "content";
        
    }
    
    private static final Icon WAIT_ICON = new ImageIcon( Utilities.loadImage(
            "org/netbeans/modules/xml/text/navigator/resources/wait.gif" ) ); //NOI18N
}

