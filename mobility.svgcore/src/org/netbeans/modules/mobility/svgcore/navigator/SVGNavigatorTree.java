/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * SVGNavigatorTreeModel.java
 * Created on May 26, 2007, 7:51 PM
 */

package org.netbeans.modules.mobility.svgcore.navigator;

import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.Iterator;
import java.util.List;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import org.netbeans.modules.editor.structure.api.DocumentElement;
import org.netbeans.modules.mobility.svgcore.SVGDataObject;
import org.netbeans.modules.mobility.svgcore.model.SVGFileModel;

/**
 *
 * @author Pavel Benes
 */
public final class SVGNavigatorTree extends JTree {
    public static byte VISIBILITY_DIRECT     = 1;
    public static byte VISIBILITY_UNDIRECT   = 2;
    public static byte VISIBILITY_NO         = 3;
    
    static boolean showAttributes     = true;
    static boolean showIdOnly         = false;
    static boolean showAnimationsOnly = false;  
    
    private final SVGDataObject     m_dObj;
    private final DefaultTreeModel  m_treeModel;    
    private       boolean           m_firstPaint;
    private       String            m_selectedId = null;

    public SVGNavigatorTree(SVGDataObject dObj) throws Exception {
        super();
        m_dObj = dObj;
        
        m_firstPaint = true;
        setShowsRootHandles(true);
        setRootVisible(false);
        putClientProperty("JTree.lineStyle", "Angled");  //NOI18N       
        
        DocumentElement rootElement = dObj.getModel().getModel().getRootElement();
        SVGNavigatorNode rootTna = new SVGNavigatorNode(rootElement, this, null, VISIBILITY_DIRECT);

        m_treeModel = new DefaultTreeModel(rootTna);
        setModel(m_treeModel);   
        
        setCellRenderer(new SVGNavigatorTreeCellRenderer());
    }
    
    public DefaultTreeModel getTreeModel() {
        return m_treeModel;
    }
    
    public SVGDataObject getDataObject() {
        return m_dObj;
    }
    
    String getSelectedId() {
        return m_selectedId;
    }
    
    public void filterChanged() {
        SVGNavigatorNode root = (SVGNavigatorNode) treeModel.getRoot();
        root.refresh();
        
        //make sure that root's children are visible
        if (root.getChildCount() > 0) {
            TreePath path = new TreePath( root);
            if ( isCollapsed(path)) {
                expandPath(path);
            }            
        }
        repaint();
    }
    
    /** Overriden to calculate correct row height before first paint */
    public void paint(Graphics g) {
        if (m_firstPaint) {
            int height = g.getFontMetrics(getFont()).getHeight();
            setRowHeight(height + 2);
            m_firstPaint = false;
        }
        super.paint(g);
    }
    
    public static boolean isTreeElement(DocumentElement de) {
        return SVGFileModel.isTagElement(de);
    }
    
    public void selectNode( String elemId, DocumentElement de) {
        m_selectedId = elemId;
        if (de == null) {
            de = m_dObj.getModel().getElementById(elemId);
        }

        if (de != null) {
            SVGNavigatorNode rootNode = (SVGNavigatorNode) treeModel.getRoot();
            SVGNavigatorNode node = rootNode.findNode(de);
            if ( node == null) {
                List<DocumentElement> parents = SVGFileModel.getParents(de);
                int parentIndex = parents.size() - 1;
                assert parentIndex >= 0 : "The element must have at least one parent";
                node = rootNode.findNode( parents.get(parentIndex));
                assert node != null : "Tree node not found";
                
                while( parentIndex > 0) {
                    SVGNavigatorNode childNode = node.getChildByElemenent(parents.get(--parentIndex));
                    if ( childNode != null) {
                        node = childNode;
                    } else {
                        break;
                    }
                }
            }
            
            TreePath treePath = node.getNodePath();
            makeVisible(treePath);
            setSelectionPath(treePath);
            Rectangle rect = getPathBounds(treePath);
            scrollRectToVisible(rect);
            repaint();
        } 
    }
    
    byte checkVisibility(DocumentElement docElem, boolean deepCheck) {
        boolean isVisible = isVisible(docElem);
        
        if ( !isVisible && deepCheck) {
            // check if node should be visible because of its visible children
            for ( Iterator i = docElem.getChildren().iterator(); i.hasNext(); ) {  
                DocumentElement chde = (DocumentElement)i.next();    
                  
                if( isTreeElement(docElem) ) {
                    if ( checkVisibility(chde, true) != VISIBILITY_NO) {
                        return VISIBILITY_UNDIRECT;
                    }
                }                
            }
        }
        return isVisible ? VISIBILITY_DIRECT : VISIBILITY_NO;
    }    
    
    private static boolean isVisible(DocumentElement docElem) {
        if (showIdOnly) {
            String id = SVGFileModel.getIdAttribute(docElem);

            if (id == null || id.length() == 0) {
                return false;
            }
        }

        if (showAnimationsOnly && !SVGFileModel.isAnimation(docElem)) {
            return false;
        }

        return true;
    }
}
