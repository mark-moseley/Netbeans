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
package org.netbeans.modules.bpel.nodes.actions;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.nodes.BpelNode;
import org.netbeans.modules.xml.xam.ui.XAMUtils;
import org.openide.ErrorManager;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.actions.NodeAction;

/**
 *
 * @author Vitaly Bychkov
 *
 */
public abstract class BpelNodeAction extends NodeAction {

    public BpelNodeAction() {
        name = getBundleName();
    }

    protected abstract String getBundleName();
    
    public abstract ActionType getType();
    
    protected abstract void performAction(BpelEntity[] bpelEntities);
    
    protected boolean enable(BpelEntity[] bpelEntities) {
        if (bpelEntities == null) return false;
        if (bpelEntities.length != 1) return false;
        if (bpelEntities[0] == null) return false;
                
        BpelModel bpelModel = bpelEntities[0].getBpelModel();
        
        if (bpelModel == null) return false;
        
        boolean readonly = !XAMUtils.isWritable(bpelModel);
        
        if (readonly && isChangeAction()) {
            return false;
        }
        
        return true;
    }
    
    protected boolean asynchronous() {
        return false;
    }
    
    public void performAction(Node[] nodes) {
        final BpelEntity[] bpelEntities = getBpelEntities(nodes);
        
        if (!enable(bpelEntities)) {
            return;
        }
        BpelModel model = getBpelModel(nodes[0]);
        if (model == null) {
            return;
        }
        try {
            model.invoke(new Callable<Object>() {
                public Object call() {
                    performAction(bpelEntities);
                    return null; 
                }
            }, this);
        } catch (Exception e) {
            ErrorManager.getDefault().notify(e);
        }
    }
    
    
    public boolean enable(final Node[] nodes) {
        if (nodes == null || nodes.length < 1) {
            return false;
        }
        for (Node node : nodes) {
            if (!(node instanceof BpelNode)) {
                return false;
            }
        }
        
        BpelModel model = getBpelModel(nodes[0]);
        // model == null in case dead element
        if (model == null) {
            return false;
        }
        boolean isEnable = false;
        if (model.isIntransaction()) {
            return enable(getBpelEntities(nodes));
        }
        try {
            return model.invoke(new Callable<Boolean>() {
                public Boolean call() throws Exception {
                    return new Boolean(enable(getBpelEntities(nodes)));
                }
            }, this);
        } catch (Exception ex) {
            ErrorManager.getDefault().notify(ex);
        }
        
        return false;
    }
    
    public String getName() {
        return name;
    }
    
    
    public boolean isChangeAction() {
        return true;
    }
    
    
    public BpelModel getBpelModel(Node node) {
        BpelModel bpelModel = (BpelModel)node.getLookup().lookup(BpelModel.class);
        if (bpelModel == null && node instanceof BpelNode) {
            Object ref = ((BpelNode)node).getReference();
            if (ref instanceof BpelEntity) {
                bpelModel = ((BpelEntity)ref).getBpelModel();
            }
        }
        return bpelModel;
    }
    
    public BpelModel getBpelModel(BpelEntity entity) {
        return entity == null ? null : entity.getBpelModel();
    }

    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
    
    protected static final BpelEntity[] getBpelEntities(Node[] nodes) {
        List<BpelEntity> entities = new ArrayList<BpelEntity>();
        
        Object tmpRefObj = null;
        for (Node node : nodes) {
            if (node instanceof BpelNode
                && (tmpRefObj = ((BpelNode)node).getReference()) instanceof BpelEntity) {
                entities.add((BpelEntity)tmpRefObj);
            }
        }
        
        BpelEntity[] entitiesArray = entities.size() < 1 ? null
            : new BpelEntity[entities.size()];
        entitiesArray = entitiesArray == null ?
            null
            : entities.toArray(entitiesArray);
        
        return entitiesArray;
    }
    
    private String name;
}
