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

package org.netbeans.modules.java.editor.codegen.ui;

import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.List;
import javax.swing.InputMap;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import org.openide.explorer.view.BeanTreeView;
import org.openide.explorer.view.NodeTreeModel;
import org.openide.explorer.view.Visualizer;
import org.openide.nodes.Node;

/**
 *
 * @author Petr Hrebejk
 */
public class CheckTreeView extends BeanTreeView  {
    
    private NodeTreeModel nodeTreeModel;
    
    /** Creates a new instance of CheckTreeView */
    public CheckTreeView() {
        
        setFocusable( false );
        
        CheckListener l = new CheckListener();
        tree.addMouseListener( l );
        tree.addKeyListener( l );

        CheckRenderer check = new CheckRenderer();
        tree.setCellRenderer( check );
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        
        tree.setShowsRootHandles(false);
        
        InputMap input = tree.getInputMap( JTree.WHEN_FOCUSED );
        if( null != input )
            input.remove( KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0) );
        
        setBorder( UIManager.getBorder("ScrollPane.border") );
    }
    
    @Override
    protected NodeTreeModel createModel() {
        nodeTreeModel = super.createModel();
        return nodeTreeModel;
    }

    public void expandRow( int row ) {        
        tree.expandRow(row);
    }
    
    public boolean getScrollsOnExpand() {
        return tree.getScrollsOnExpand();
    }
    
    public void setScrollsOnExpand( boolean scrolls ) {
        tree.setScrollsOnExpand( scrolls );
    }
    
    @Override
    protected void showPath(TreePath path) {
        tree.expandPath(path);
        showPathWithoutExpansion(path);
    }
    
    @Override
    protected void showSelection(TreePath[] treePaths) {
        tree.getSelectionModel().setSelectionPaths(treePaths);

        if (treePaths.length == 1) {
            showPathWithoutExpansion(treePaths[0]);
        }
    }
    
    private void showPathWithoutExpansion(TreePath path) {
        Rectangle rect = tree.getPathBounds(path);

        if (rect != null && getWidth() > 0 && getHeight() > 0 ) {
            tree.scrollRectToVisible(rect);
        }
    }
    
    class CheckListener implements MouseListener, KeyListener {

        // MouseListener -------------------------------------------------------
        
        public void mouseClicked(MouseEvent e) {
            if( !e.isPopupTrigger() ) {
                TreePath path = tree.getPathForLocation(e.getPoint().x, e.getPoint().y);
                toggle( path );            
            }
        }

        public void keyTyped(KeyEvent e) {}

        public void keyReleased(KeyEvent e) {}

        public void mouseEntered(MouseEvent e) {}

        public void mouseExited(MouseEvent e) {}

        public void mousePressed(MouseEvent e) {}

        public void mouseReleased(MouseEvent e) {}

        // Key Listener --------------------------------------------------------
        
        public void keyPressed(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_SPACE ) {
                
                if ( e.getSource() instanceof JTree ) {
                    JTree tree = (JTree) e.getSource();
                    TreePath path = tree.getSelectionPath();

                    if ( toggle( path )) {
                        e.consume();
                    }
                }
            }
        }
        
        // Private methods -----------------------------------------------------
        
        private boolean toggle( TreePath treePath ) {
            
            if( treePath == null )
                return false;
            
            Node node = Visualizer.findNode( treePath.getLastPathComponent() );
            if( node == null )
                return false ;

            ElementNode.Description description = node.getLookup().lookup(ElementNode.Description.class);
            if (description != null ) {
                if( description.isSelectable()  ) {
                    description.setSelected( !description.isSelected() );
                    return true;
                } else {
                    boolean newState = !description.isSelected();
                    description.setSelected(newState);
                    toggleChildren( description.getSubs(), newState );
                }
            }
            
            return false;
        }
        
        private void toggleChildren( List<ElementNode.Description> children, boolean newState ) {
            if( null == children )
                return;
            for( ElementNode.Description d : children ) {
                d.setSelected( newState );
                toggleChildren( d.getSubs(), newState );
            }
        }
    }
    
}
