/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
/*
 * InstanceTargetNode.java
 *
 * Created on December 7, 2003, 9:11 PM
 */

package org.netbeans.modules.j2ee.deployment.impl.ui;

import org.netbeans.modules.j2ee.deployment.impl.ui.actions.SetAsDefaultServerAction;
import org.netbeans.modules.j2ee.deployment.impl.ServerInstance;
import org.netbeans.modules.j2ee.deployment.impl.ServerTarget;
import org.openide.util.HelpCtx;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.AbstractNode;
import org.openide.util.actions.SystemAction;
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;

/**
 * A node for an admin instance that is also a target server.
 *
 * @author  nn136682
 */
public class InstanceTargetXNode extends FilterXNode {
    private ServerTarget instanceTarget;
    private ServerInstance instance;
    
    public InstanceTargetXNode(Node instanceNode, ServerInstance instance) {
        this(instanceNode, Node.EMPTY, instance);
    }
    
    public InstanceTargetXNode(Node instanceNode, Node xnode, ServerInstance instance) {
        super(instanceNode, xnode, true, true);
        this.instance = instance;
        this.setChildren(new InstanceTargetChildren(xnode, instance));
    }
    
    private ServerTarget getServerTarget() {
        if (instanceTarget != null) 
            return instanceTarget;
        instanceTarget = instance.getCoTarget();
        return instanceTarget;
    }

    public Node getDelegateTargetNode() {
        if (xnode != null && xnode != Node.EMPTY)
            return xnode;
        ServerTarget st = getServerTarget();
        if (st == null)
            return xnode;
        Node tn = instance.getServer().getNodeProvider().createTargetNode(st);
        if (tn != null) 
            xnode = tn;
        return xnode;
    }
    
    public static class InstanceTargetChildren extends Children {
        ServerInstance instance;
        ServerTarget target;
        
        public InstanceTargetChildren(Node original, ServerInstance instance) {
            super(original);
            this.instance = instance;
        }
        protected void addNotify() {
            super.addNotify();
            if (original == Node.EMPTY) {
                Node newOriginal = ((InstanceTargetXNode) getNode()).getDelegateTargetNode();
                if (newOriginal != null && newOriginal != Node.EMPTY)
                    this.changeOriginal(newOriginal);
            }
        }
        public void updateKeys() {
            addNotify();
        }
    }
    
    public javax.swing.Action[] getActions(boolean context) {
        List actions = new ArrayList();
        actions.addAll(Arrays.asList(getOriginal().getActions(context)));
        //if (instance.isRunning()) {
        //    actions.addAll(Arrays.asList(getDelegateTargetNode().getActions(context)));
        //} else {
            actions.add(SystemAction.get(SetAsDefaultServerAction.class));
        //}
        
        return (javax.swing.Action[]) actions.toArray(new javax.swing.Action[actions.size()]);
    }
    
    public PropertySet[] getPropertySets() {
        List ret = new ArrayList();
        ret.addAll(Arrays.asList(getOriginal().getPropertySets()));
        ret.addAll(Arrays.asList(getDelegateTargetNode().getPropertySets()));
        return (PropertySet[]) ret.toArray(new PropertySet[ret.size()]);
    }
    
    public org.openide.nodes.Node.Cookie getCookie(Class type) {
        Node tn = getDelegateTargetNode();
        org.openide.nodes.Node.Cookie c = null;
        if (tn != null)
            c = tn.getCookie(type);
        if (c == null) 
            c = super.getCookie(type);
        return c;
    }
    
    public void handleRefresh() {
        ((InstanceTargetChildren)getChildren()).updateKeys();
    }
}

