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
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.Collection;
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
import javax.swing.text.JTextComponent;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.EditorUI;
import org.netbeans.editor.Utilities;
import org.netbeans.editor.ext.ExtEditorUI;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.editor.structure.api.DocumentElement;
import org.netbeans.modules.editor.structure.api.DocumentModel;
import org.netbeans.modules.editor.structure.api.DocumentModelException;
import org.openide.ErrorManager;
import org.openide.cookies.EditorCookie;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.Lookup.Template;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.windows.TopComponent;


/** XML Navigator UI component containing a tree of XML elements.
 *
 * @author Marek Fukala
 * @version 1.0
 */
public class NavigatorContent extends JPanel   {
    
    //suppose we always have only one instance of the navigator panel at one time
    //so using the static fields is OK. TheeNodeAdapter is reading these two
    //fields and change it's look accordingly
    static boolean showAttributes = true;
    static boolean showContent = true;
    
    private JPanel active = null;
    private final JPanel emptyPanel;
    
    public NavigatorContent() {
        setLayout(new BorderLayout());
        //init empty panel
        emptyPanel = new JPanel();
        emptyPanel.setBackground(Color.WHITE);
        emptyPanel.setLayout(new BorderLayout());
        JLabel waitLabel = new JLabel(NbBundle.getMessage(NavigatorContent.class, "LBL_Wait"));
        waitLabel.setHorizontalAlignment(SwingConstants.CENTER);
        waitLabel.setForeground(Color.GRAY);
        emptyPanel.add(waitLabel, BorderLayout.CENTER);
    }
    
    public void navigate(final BaseDocument bdoc) {
        //called from AWT thread
        showWaitPanel();
        
        //get the model and create the new UI on background
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                //get document model for the file
                try {
                    DocumentModel model = DocumentModel.getDocumentModel(bdoc);
                    if(model != null) {
                        JPanel panel = new NavigatorContentPanel(model);
                        showUI(panel);
                    } else System.out.println("model is null!!!!");
                }catch(DocumentModelException dme) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, dme);
                }
            }
        });
    }
    
    private void showUI(final JPanel ui) {
        //generate the component on background
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                removeAll();
                add(ui, BorderLayout.CENTER);
            }
        });
    }
    
    private void showWaitPanel() {
        removeAll();
        add(emptyPanel, BorderLayout.CENTER);
        repaint();
    }
    
    private class NavigatorContentPanel extends JPanel implements FiltersManager.FilterChangeListener {
        
        private JTree tree;
        private FiltersManager filters;
        
        public NavigatorContentPanel(DocumentModel dm) {
            setLayout(new BorderLayout());
            
            //create the JTree pane
            tree = new PatchedJTree();
            TreeModel model = createTreeModel(dm);
            tree.setModel(model);
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
            
            JScrollPane treeView = new JScrollPane(tree);
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
                        selectElementInPane(panes[0], selected, false);
                    } else if(selectLineOnly) {
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
    
}

