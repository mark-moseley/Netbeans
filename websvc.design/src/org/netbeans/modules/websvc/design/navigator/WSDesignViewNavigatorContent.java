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

/*
 * WSDesignViewNavigatorContent.java
 *
 * Created on April 9, 2007, 5:31 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.websvc.design.navigator;

import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;
import org.netbeans.modules.websvc.design.javamodel.MethodModel;
import org.netbeans.modules.websvc.design.javamodel.ServiceModel;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.BeanTreeView;
import org.openide.explorer.view.TreeView;
import org.openide.loaders.DataObject;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

/**
 *
 * @author rico
 */
public class WSDesignViewNavigatorContent extends JPanel
        implements ExplorerManager.Provider, PropertyChangeListener{
    
    /** Explorer manager for the tree view. */
    private ExplorerManager explorerManager;
    /** Our schema component node tree view. */
    private TreeView treeView;
    
    
    /** Creates a new instance of WSDesignViewNavigatorContent */
    public WSDesignViewNavigatorContent() {
        setLayout(new BorderLayout());
        explorerManager = new ExplorerManager();
        treeView = new BeanTreeView();
        explorerManager.addPropertyChangeListener(this);
    }
    
    public void propertyChange(PropertyChangeEvent arg0) {
        
    }
    
    public ExplorerManager getExplorerManager() {
        return explorerManager;
    }
    
    public void navigate(DataObject implClass){
        add(treeView, BorderLayout.CENTER);
        AbstractNode root = new AbstractNode(new WSChildren(implClass));
        root.setName("Operations");
        getExplorerManager().setRootContext(root);
        revalidate();
        repaint();
    }
    
    public class WSChildren extends Children.Keys{
        DataObject implClass;
        public WSChildren(DataObject implClass){
            this.implClass = implClass;
        }
        protected Node[] createNodes(Object key) {
            if(key instanceof MethodModel){
                MethodModel m = (MethodModel)key;
                AbstractNode n = new AbstractNode(Children.LEAF);
                n.setName(m.getOperationName());
                return new Node[] {n};
            }
            return new Node[0];
        }
        
        protected void addNotify() {
            updateKeys();
        }
        
        private void updateKeys(){
            List keys = new ArrayList();
            if(implClass != null){
                ServiceModel model = ServiceModel.getServiceModel(implClass.getPrimaryFile());
                keys = model.getOperations();
            }
            this.setKeys(keys);
        }
    }
    
}
