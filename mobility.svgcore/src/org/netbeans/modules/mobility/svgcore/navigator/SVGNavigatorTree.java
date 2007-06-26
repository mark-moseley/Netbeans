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
import java.util.Iterator;
import javax.swing.JTree;
import javax.swing.text.AttributeSet;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import org.netbeans.modules.editor.structure.api.DocumentElement;
import org.netbeans.modules.mobility.svgcore.SVGDataObject;
import org.netbeans.modules.mobility.svgcore.model.SVGFileModel;

/**
 *
 * @author Pavel Benes
 */
public class SVGNavigatorTree extends JTree {
    public static byte VISIBILITY_DIRECT     = 1;
    public static byte VISIBILITY_UNDIRECT   = 2;
    public static byte VISIBILITY_NO         = 3;
    
    static boolean showAttributes     = true;
    static boolean showIdOnly         = false;
    static boolean showAnimationsOnly = false;  
    
    private final SVGDataObject         dObj;
    private       DefaultTreeModel      treeModel;
    
    private boolean firstPaint;

    public SVGNavigatorTree(SVGDataObject dObj) throws Exception {
        super();
        this.dObj = dObj;
        
        firstPaint = true;
        setShowsRootHandles(true);
        setRootVisible(false);
        setCellRenderer(new SVGNavigatorTreeCellRenderer());
        putClientProperty("JTree.lineStyle", "Angled");        
        initialize();
    }
    
    void initialize() throws Exception {
        treeModel = new DefaultTreeModel(null);
        setModel(treeModel);         
        DocumentElement rootElement = dObj.getModel()._getModel().getRootElement();
        SVGNavigatorNode rootTna = new SVGNavigatorNode(rootElement, this, null, VISIBILITY_DIRECT);
        treeModel.setRoot(rootTna);  
    }

    public DefaultTreeModel getTreeModel() {
        return treeModel;
    }
    
    public SVGDataObject getDataObject() {
        return this.dObj;
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
        if (firstPaint) {
            int height = g.getFontMetrics(getFont()).getHeight();
            setRowHeight(height + 2);
            firstPaint = false;
        }
        super.paint(g);
    }
    
    public static boolean isTreeElement(DocumentElement de) {
        return de.getType().equals(SVGNavigatorNode.XML_TAG) ||
               de.getType().equals(SVGNavigatorNode.XML_EMPTY_TAG);
    }
    
    public void selectNode( String id) {
        DocumentElement de = dObj.getModel().getElementById(id);
        if (de != null) {
            SVGNavigatorNode node = ((SVGNavigatorNode) treeModel.getRoot()).findNode(de);
            if ( node != null) {
                TreePath treePath = node.getNodePath();
                makeVisible(treePath);
                setSelectionPath(treePath);
                repaint();
            } else {
                //TODO expand navigator node representing the element
                System.out.println("Node not expanded");
            }
        } else {
            System.err.println("Element not found");
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
            AttributeSet attrs     = docElem.getAttributes();
            String       attrValue = (String) attrs.getAttribute("id");

            if (attrValue == null || attrValue.length() == 0) {
                return false;
            }
        }

        if (showAnimationsOnly && !SVGFileModel.isAnimation(docElem)) {
            return false;
        }

        return true;
    }
}
