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

package org.netbeans.modules.palette.ui;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import org.netbeans.modules.palette.DefaultSettings;
import org.openide.explorer.view.Visualizer;
import org.openide.nodes.Node;

/**
 * This listener controls click and double click on the CheckNodes. In addition
 * to it provides support for keyboard node checking/unchecking and opening
 * document.
 *
 * todo (#pf): Improve behaviour and comments.
 *
 * @author  Pavel Flaska, S. Aubrecht
 */
class CheckListener implements MouseListener, KeyListener {

    DefaultSettings settings;
    
    public CheckListener( DefaultSettings settings ) {
        this.settings = settings;
    }
    
    public void mouseClicked(MouseEvent e) {
        // todo (#pf): we need to solve problem between click and double
        // click - click should be possible only on the check box area
        // and double click should be bordered by title text.
        // we need a test how to detect where the mouse pointer is
        JTree tree = (JTree) e.getSource();
        Point p = e.getPoint();
        int x = e.getX();
        int y = e.getY();
        int row = tree.getRowForLocation(x, y);
        TreePath path = tree.getPathForRow(row);

        // if path exists and mouse is clicked exactly once
        if( null == path )
            return;
        
        Node node = Visualizer.findNode( path.getLastPathComponent() );
        if( null == node )
            return;
        
        Rectangle chRect = CheckRenderer.getCheckBoxRectangle();
        Rectangle rowRect = tree.getPathBounds(path);
        chRect.setLocation(chRect.x + rowRect.x, chRect.y + rowRect.y);
        if (e.getClickCount() == 1 && chRect.contains(p)) {
            boolean isSelected = settings.isNodeVisible( node );
            settings.setNodeVisible( node, !isSelected );
            tree.repaint();
        }
    }
    
    public void keyTyped(KeyEvent e) {
    }
    
    public void keyReleased(KeyEvent e) {
    }
    
    public void mouseEntered(MouseEvent e) {
    }
    
    public void mouseExited(MouseEvent e) {
    }
    
    public void mousePressed(MouseEvent e) {
    }
    
    public void mouseReleased(MouseEvent e) {
    }
    
    public void keyPressed(KeyEvent e) {
        if (e.getKeyChar() == ' ') {
            JTree tree = (JTree) e.getSource();
            TreePath path = tree.getSelectionPath();
            if( null == path )
                return;

            Node node = Visualizer.findNode( path.getLastPathComponent() );
            if( null == node )
                return;
            
            boolean isSelected = settings.isNodeVisible( node );
            settings.setNodeVisible( node, !isSelected );
            tree.repaint();
            
            e.consume();
        }
    }
} // end CheckNodeListener
