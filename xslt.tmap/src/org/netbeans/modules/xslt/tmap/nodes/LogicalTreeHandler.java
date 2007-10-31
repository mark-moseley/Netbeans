/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.xslt.tmap.nodes;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import javax.swing.tree.TreeSelectionModel;
import org.netbeans.modules.xml.xam.ComponentEvent;
import org.netbeans.modules.xml.xam.ComponentListener;
import org.netbeans.modules.xslt.tmap.model.api.TMapComponent;
import org.netbeans.modules.xslt.tmap.model.api.TMapModel;
import org.netbeans.modules.xslt.tmap.navigator.TMapNavigatorController;
import org.openide.ErrorManager;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.BeanTreeView;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.windows.TopComponent;

/**
 *
 * @author Vitaly Bychkov
 * @version 1.0
 */
public class LogicalTreeHandler implements PropertyChangeListener, ComponentListener {

    private static final long serialVersionUID = 1L;
    private TMapModel myModel;
    //context lookup
    private Lookup myContextLookup;
    
    private BeanTreeView myBeanTreeView;
    private ExplorerManager myExplorerManager;

    public LogicalTreeHandler(ExplorerManager explorerManager,
            TMapModel tModel,
            Lookup contextLookup) 
    {
        myModel = tModel;
        myExplorerManager = explorerManager;
        myExplorerManager.addPropertyChangeListener(this);
        
        myContextLookup = contextLookup;
        
        myBeanTreeView = createBeanTreeView();
        
        //add TopComponent Active Node changes listener :
        TopComponent.getRegistry().addPropertyChangeListener(this);
        myModel.addComponentListener((ComponentListener)this);
    }

    private BeanTreeView createBeanTreeView() {
        BeanTreeView beanTreeView = new BeanTreeView();
        beanTreeView.setRootVisible(true);
        beanTreeView.setEnabled(true);
        beanTreeView.setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        beanTreeView.setDefaultActionAllowed(true);
//        doTreeNodeSelectionByActiveNode();
        
        return beanTreeView;
    }
    
    public void removeListeners() {
        if (myExplorerManager != null) {
            myExplorerManager.removePropertyChangeListener(this);
        }
        TopComponent.getRegistry().removePropertyChangeListener(this);
        if (myModel != null) {
            myModel.removeComponentListener(this);
        }
        myModel = null;
        myExplorerManager = null;
        
    }

    public BeanTreeView getBeanTreeView() {
        return myBeanTreeView;
    }

    public void propertyChange(PropertyChangeEvent evt) {
//        System.out.println("propertyChange: "+evt);
        
        
        String propertyName = evt.getPropertyName();
        TopComponent navigatorTopComponent 
                = TMapNavigatorController.getNavigatorTC();
//        System.out.println("propertyChange name: "+propertyName);
        
        if (propertyName.equals(TopComponent.Registry.PROP_ACTIVATED)) {
            if (TopComponent.getRegistry().getActivated() == navigatorTopComponent) {
//                addUndoManager();
//                triggerValidation();
            }
        }
        else if (propertyName.equals(TopComponent.Registry.PROP_ACTIVATED_NODES)) {
           if (TopComponent.getRegistry().getActivated() != navigatorTopComponent) {
               doTreeNodeSelectionByActiveNode();
           }
           return;
            
        } else if (propertyName.equals(ExplorerManager.PROP_SELECTED_NODES)) {
            if (navigatorTopComponent == null) {
                return;
            }
            // NAVIGATOR SELECTED NODES SETTED AS ACTIVE NODES
            //navigatorTopComponent.setActivatedNodes(new Node[] {});
            navigatorTopComponent.setActivatedNodes((Node[])evt.getNewValue());
        } else if (propertyName.equals(ExplorerManager.PROP_ROOT_CONTEXT)) {
            //EVENT FOR PROPERTY PROP_ROOT_CONTEXT
            doTreeNodeSelectionByActiveNode();
        } else if (propertyName.equals(TopComponent.Registry.PROP_OPENED)) {
//            System.out.println("the set of the opened topComponent were changed");
            TMapNavigatorController.activateLogicalPanel();
        } /***/else if (propertyName.equals(TopComponent.Registry.PROP_CURRENT_NODES)) {
//            System.out.println("the set of currently selected nodes were changed");
            TMapNavigatorController.activateLogicalPanel();
        }               
        

    }

    public void valueChanged(ComponentEvent evt) {
//        System.out.println("valueChanged: "+evt);
    }

    public void childrenAdded(ComponentEvent evt) {
//        System.out.println("childrenAdded: "+evt);
    }

    public void childrenDeleted(ComponentEvent evt) {
//        System.out.println("childrenDeleted: "+evt);
    }
    
    protected void doTreeNodeSelectionByActiveNode() {
        Node[] nodes = TopComponent.getRegistry().getActivatedNodes();
        if (nodes == null || nodes.length == 0) {
            return;
        }
        
            for (Node node : nodes) {
                if (node instanceof TMapComponentNode) {
                    doTreeNodeSelection((TMapComponentNode)node);
                    break;
                }
            }

        
//        for (Node elem : nodes) {
//            if (elem instanceof BpelNode 
//                    && ((BpelNode)elem).getReference() instanceof BpelEntity
//                    && !(org.netbeans.modules.bpel.editors.api.utils.Util.isNavigatorShowableNodeType(((BpelNode)elem).getNodeType()))) 
//            {
//                elem = org.netbeans.modules.bpel.editors.api.utils.Util.getClosestNavigatorNode(
//                        (BpelEntity)((BpelNode)elem).getReference(),
//                        elem.getLookup());
//            }
//
//            if (!(elem instanceof BpelNode)
//            || ((BpelNode)elem).getNodeType().equals(NodeType.SCHEMA_ELEMENT)
//            || !(((BpelNode)elem).getReference() instanceof BpelEntity)) 
//            {
//                continue;
//            }
//            
//            BpelEntity refBpelEntityObj = BpelEntity.class.cast(
//                    ((BpelNode)elem).getReference());
//            if (refBpelEntityObj != null) {
//                doTreeNodeSelection((BpelNode)elem);
//            }
//            //just one node can be selected in navigator bpel logical View
//            break;
//        }
    }
    
    private void doTreeNodeSelection(TMapComponentNode tMapNode) {
        
        try {
//         myBeanTreeView.expandAll();
            Node node2sel = findTMapNode(myExplorerManager.getRootContext()
            ,tMapNode.getComponentRef());
            if (node2sel == null) {
                return;
            }
            
            myExplorerManager.setSelectedNodes(new Node[] {node2sel});
        } catch (PropertyVetoException ex) {
            ErrorManager.getDefault().notify(ex);
        }
    }

    private Node findTMapNode(Node parentNode, TMapComponent reference) {
        if (parentNode == null || reference == null 
                || !(parentNode instanceof TMapComponentNode)) 
        {
            return null;
        }
        
        if (reference.equals(((TMapComponentNode)parentNode).getComponentRef())) {
            return (TMapComponentNode)parentNode;
        }
        
        Children child = parentNode.getChildren();
        if (child == null || child.equals( Children.LEAF)) {
            return null;
        }
        
        Node[] nodes = child.getNodes();
        if (nodes == null || nodes.length < 1) {
            return null;
        }
        
        for (Node node : nodes) {
            Node tmpNode = findTMapNode(node, reference);
            if (tmpNode != null) {
                return tmpNode;
            }
        }

        return null;
    }
}
