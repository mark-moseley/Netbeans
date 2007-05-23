/*
 * ClassMemberPanelUi.java
 *
 * Created on November 8, 2006, 4:03 PM
 */

package org.netbeans.modules.java.navigation;

import java.awt.BorderLayout;
import java.awt.Rectangle;
import javax.swing.Action;
import javax.swing.KeyStroke;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.beans.PropertyVetoException;
import javax.lang.model.element.Element;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.KeyStroke;
import javax.swing.event.ChangeEvent;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.modules.java.navigation.ClassMemberFilters;
import org.netbeans.modules.java.navigation.ElementNode;
import org.netbeans.modules.java.navigation.ElementNode.Description;
import org.netbeans.modules.java.navigation.ElementNode.Description;
import org.netbeans.modules.java.navigation.actions.FilterSubmenuAction;
import org.netbeans.modules.java.navigation.actions.SortActionSupport.SortByNameAction;
import org.netbeans.modules.java.navigation.actions.SortActionSupport.SortBySourceAction;
import org.netbeans.modules.java.navigation.base.FiltersManager;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.netbeans.modules.java.navigation.base.TapPanel;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.BeanTreeView;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

/**
 *
 * @author  phrebejk
 */
public class ClassMemberPanelUI extends javax.swing.JPanel
        implements ExplorerManager.Provider, FiltersManager.FilterChangeListener {
    
    private ExplorerManager manager = new ExplorerManager();
    private MyBeanTreeView elementView;
    private TapPanel filtersPanel;
    private Lookup lookup = null; // XXX may need better lookup
    private ClassMemberFilters filters;
    
    private Action[] actions; // General actions for the panel
    
    private static final Rectangle ZERO = new Rectangle(0,0,1,1);

    
    /** Creates new form ClassMemberPanelUi */
    public ClassMemberPanelUI() {
                      
        initComponents();
        
        // Tree view of the elements
        elementView = createBeanTreeView();        
        add(elementView, BorderLayout.CENTER);
               
        // filters
        filtersPanel = new TapPanel();
        filtersPanel.setOrientation(TapPanel.DOWN);
        // tooltip
        KeyStroke toggleKey = KeyStroke.getKeyStroke(KeyEvent.VK_T,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
        String keyText = Utilities.keyToString(toggleKey);
        filtersPanel.setToolTipText(NbBundle.getMessage(ClassMemberPanelUI.class, "TIP_TapPanel", keyText));
        
        filters = new ClassMemberFilters( this );
        filters.getInstance().hookChangeListener(this);
        JComponent buttons = filters.getComponent();
        buttons.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 0));
        filtersPanel.add(buttons);
        
        actions = new Action[] {            
            new SortByNameAction( filters ),
            new SortBySourceAction( filters ),
            null,
            new FilterSubmenuAction(filters.getInstance())            
        };
        
        add(filtersPanel, BorderLayout.SOUTH);
        
        manager.setRootContext(ElementNode.getWaitNode());
        
    }

    @Override
    public boolean requestFocusInWindow() {
        boolean result = super.requestFocusInWindow();
        elementView.requestFocusInWindow();
        return result;
    }
    
    public org.openide.util.Lookup getLookup() {
        // XXX Check for chenge of FileObject
        return lookup;
    }
    
    public org.netbeans.modules.java.navigation.ElementScanningTask getTask() {
        
        return new ElementScanningTask(this);
        
    }
    
    
    public void showWaitNode() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
               elementView.setRootVisible(true);
               manager.setRootContext(ElementNode.getWaitNode());
            } 
        });
    }
    
    public void selectElementNode( ElementHandle<Element> eh ) {
        ElementNode root = getRootNode();
        if ( root == null ) {
            return;
        }
        ElementNode node = root.getNodeForElement(eh);
        try {
            manager.setSelectedNodes(new Node[]{ node == null ? getRootNode() : node });
        } catch (PropertyVetoException propertyVetoException) {
            Exceptions.printStackTrace(propertyVetoException);
        }
//        SwingUtilities.invokeLater(new Runnable() {
//            public void run() {
//                
//            }
//            
//        });
    }

    public void refresh( final Description description ) {
        
        final ElementNode rootNode = getRootNode();
        
        if ( rootNode != null && rootNode.getDescritption().fileObject.equals( description.fileObject) ) {
            // update
            //System.out.println("UPDATE ======" + description.fileObject.getName() );
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    rootNode.updateRecursively( description );
                }
            } );            
        } 
        else {
            //System.out.println("REFRES =====" + description.fileObject.getName() );
            // New fileobject => refresh completely
            SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    elementView.setRootVisible(false);        
                    manager.setRootContext(new ElementNode( description ) );
                    boolean scrollOnExpand = elementView.getScrollOnExpand();
                    elementView.setScrollOnExpand( false );
                    elementView.expandAll();
                    elementView.setScrollOnExpand( scrollOnExpand );
                }
            } );
            
        }
    }
    
    public void sort() {
        ElementNode root = getRootNode();
        if( null != root )
            root.refreshRecursively();
    }
    
    public ClassMemberFilters getFilters() {
        return filters;
    }
    
    public void expandNode( Node n ) {
        elementView.expandNode(n);
    }
    
    public Action[] getActions() {
        return actions;
    }
    
    public FileObject getFileObject() {
        return getRootNode().getDescritption().fileObject;
    }
    
    // FilterChangeListener ----------------------------------------------------
    
    public void filterStateChanged(ChangeEvent e) {
        ElementNode root = getRootNode();
        
        if ( root != null ) {
            root.refreshRecursively();
        }
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {

        setLayout(new java.awt.BorderLayout());
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
    
    // Private methods ---------------------------------------------------------
    
    private ElementNode getRootNode() {
        
        Node n = manager.getRootContext();
        if ( n instanceof ElementNode ) {
            return (ElementNode)n;
        }
        else {
            return null;
        }
    }
    
    private MyBeanTreeView createBeanTreeView() {
//        ActionMap map = getActionMap();
//        map.put(DefaultEditorKit.copyAction, ExplorerUtils.actionCopy(manager));
//        map.put(DefaultEditorKit.cutAction, ExplorerUtils.actionCut(manager));
//        map.put(DefaultEditorKit.pasteAction, ExplorerUtils.actionPaste(manager));
//        map.put("delete", new DelegatingAction(ActionProvider.COMMAND_DELETE, ExplorerUtils.actionDelete(manager, true)));
//        
        
        MyBeanTreeView btv = new MyBeanTreeView();    // Add the BeanTreeView        
//      btv.setDragSource (true);        
//      btv.setRootVisible(false);        
//      associateLookup( ExplorerUtils.createLookup(manager, map) );        
        return btv;
        
    }
    
    
    // ExplorerManager.Provider imlementation ----------------------------------
    
    public ExplorerManager getExplorerManager() {
        return manager;
    }
    
    
    private static class MyBeanTreeView extends BeanTreeView {
        public boolean getScrollOnExpand() {
            return tree.getScrollsOnExpand();
}
        
        public void setScrollOnExpand( boolean scroll ) {
            this.tree.setScrollsOnExpand( scroll );
        }
    }
}
