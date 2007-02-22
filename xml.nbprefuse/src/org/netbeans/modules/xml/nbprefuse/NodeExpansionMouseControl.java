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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/*
 * NodeExpansionMouseControl.java
 *
 * Created on January 17, 2006, 12:50 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.nbprefuse;


import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.Iterator;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import org.netbeans.modules.xml.nbprefuse.util.GraphUtilities;
import org.openide.util.NbBundle;
import prefuse.Visualization;
import prefuse.controls.ControlAdapter;
import prefuse.visual.AggregateItem;
import prefuse.visual.NodeItem;
import prefuse.visual.VisualItem;

/**
 *
 * @author Jeri Lockhart
 */
public final class NodeExpansionMouseControl extends ControlAdapter {
    
    private JPopupMenu popup;
    private VisualItem vItem;
    private JMenuItem menuItem;
    
    public NodeExpansionMouseControl(final Visualization viz, final String activity) {
        popup = new JPopupMenu();
        menuItem = new JMenuItem();
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){
                if (vItem != null && vItem instanceof VisualItem){
                    NodeItem fileNode = null;
                    if (vItem.canGetBoolean(AnalysisConstants.IS_FILE_GROUP_AGGREGATE) &&
                            vItem.getBoolean(AnalysisConstants.IS_FILE_GROUP_AGGREGATE)){
                        
                        fileNode = findFileNode(vItem.getInt(AnalysisConstants.ID),
                                AggregateItem.class.cast(vItem));
                    } else {
                        fileNode = NodeItem.class.cast(vItem);
                    }
                    if (fileNode.canGetBoolean(AnalysisConstants.IS_FILE_NODE) &&
                            fileNode.getBoolean(AnalysisConstants.IS_FILE_NODE)){
                        GraphUtilities.expandCollapseFileNode(fileNode);
                        if (activity != null){
                            viz.run(activity);
                        }
                    }
                }
            }
        });
        popup.add(menuItem);
        
    }
    
    public void itemReleased(VisualItem gi, MouseEvent e) {
        
        super.itemReleased(gi, e);
        vItem = gi;
        maybeShowPopup(e);
    }
    
    public void itemPressed(VisualItem gi, MouseEvent e) {
        
        super.itemPressed(gi, e);
        vItem = gi;
        maybeShowPopup(e);
    }
    
    
    
    private void maybeShowPopup(MouseEvent e) {
        if (e.isPopupTrigger()) {
            NodeItem fileNode = null;
            if (vItem.canGetBoolean(AnalysisConstants.IS_FILE_GROUP_AGGREGATE) &&
                    vItem.getBoolean(AnalysisConstants.IS_FILE_GROUP_AGGREGATE)){
                fileNode = findFileNode(vItem.getInt(AnalysisConstants.ID),
                        AggregateItem.class.cast(vItem));
            }
            if (vItem.canGetBoolean(AnalysisConstants.IS_FILE_NODE) &&
                    vItem.getBoolean(AnalysisConstants.IS_FILE_NODE)){
                fileNode = NodeItem.class.cast(vItem);
            }
            if (fileNode == null){
                return;
            }
            boolean isExpanded = fileNode.getBoolean(AnalysisConstants.IS_EXPANDED);
            menuItem.setText(NbBundle.getMessage(NodeExpansionMouseControl.class,(isExpanded?"LBL_collapse":"LBL_expand")));
            popup.show(e.getComponent(),
                    e.getX(), e.getY());
        }
    }
    
    /**
     * Finds the schema File Node in the AggregateItem
     * The AggregateItem contains the schema file node and
     *  the SchemaComponent nodes for the file
     *
     */
    private NodeItem findFileNode(final int aggregateItemFileGroup, final VisualItem item ) {
        NodeItem fileNode = null;
        AggregateItem agIt = AggregateItem.class.cast(item);
        Iterator agItems = agIt.items();
        while(agItems.hasNext()){
            VisualItem agItem = (VisualItem)agItems.next();
            int fileGroup = -1;
            if (agItem.canGetInt(AnalysisConstants.FILE_NODE_FILE_GROUP)) {
                fileGroup = agItem.getInt(AnalysisConstants.FILE_NODE_FILE_GROUP);
                if (fileGroup == aggregateItemFileGroup &&
                        agItem.canGetBoolean(AnalysisConstants.IS_EXPANDED)){
                    fileNode =  NodeItem.class.cast(agItem);
                    break;
                }
            }
        }
        return fileNode;
    }
    
}


