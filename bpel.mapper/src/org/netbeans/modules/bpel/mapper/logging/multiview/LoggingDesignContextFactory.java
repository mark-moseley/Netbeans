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

package org.netbeans.modules.bpel.mapper.logging.multiview;

import org.netbeans.modules.bpel.mapper.multiview.BpelDesignContext;
import org.netbeans.modules.bpel.mapper.multiview.BpelDesignContextImpl;
import org.netbeans.modules.bpel.mapper.multiview.DesignContextFactory;
import org.netbeans.modules.bpel.model.api.Assign;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.model.api.Copy;
import org.netbeans.modules.bpel.model.api.ExtensibleElements;
import org.netbeans.modules.bpel.model.ext.logging.api.Alert;
import org.netbeans.modules.bpel.model.ext.logging.api.Log;
import org.netbeans.modules.bpel.model.ext.logging.api.Trace;
import org.netbeans.modules.soa.ui.nodes.InstanceRef;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.windows.TopComponent;

/**
 * @author Vitaly Bychkov
 */
public class LoggingDesignContextFactory implements DesignContextFactory {

    private static final LoggingDesignContextFactory INSTANCE = new LoggingDesignContextFactory();
    private final ContextCreator[] contextCreators;
            
    private LoggingDesignContextFactory() {
        contextCreators = new ContextCreator[] {
            new LoggingContextCreatorImpl(), 
            new EmptyContextCreator()};
    }
    
    public static LoggingDesignContextFactory getInstance() {
        return INSTANCE;
    }
    
    public BpelDesignContext createBpelDesignContext(
                    BpelEntity selectedEntity, Node node, Lookup lookup) 
    {
        if (selectedEntity == null || node == null || lookup == null) {
            return null;
        }
        
        BpelDesignContext context = null;
        
        assert contextCreators != null;
        for (ContextCreator contextCreator : contextCreators) {
            if (contextCreator.accepted(selectedEntity)) {
                context = contextCreator.create(selectedEntity, node, lookup);
                break;
            }
        }

        return context;
    }
    
    public BpelDesignContext getActivatedContext(BpelModel currentBpelModel) {
        if (currentBpelModel == null) {
            return null;
        }
        
        Node[] nodes = TopComponent.getRegistry().getActivatedNodes();
        if (nodes == null || nodes.length != 1) {
            return null;
        }
        BpelEntity bpelEntity = null;
        if (nodes[0] instanceof InstanceRef) {
            Object entity = ((InstanceRef) nodes[0]).getReference();
            if (entity instanceof BpelEntity 
                    && currentBpelModel.equals(((BpelEntity)entity).getBpelModel()))
            {
                bpelEntity = (BpelEntity)entity;
            }
        } else {
            return null;
        }
        
        Lookup lookup = nodes[0].getLookup();
        BpelDesignContext bpelContext = 
                createBpelDesignContext(bpelEntity, nodes[0], lookup);
        return bpelContext;
    }

    private class LoggingContextCreatorImpl implements ContextCreator {

        /**
         * 
         * @param selectedEntity - the selected bpel entity to show mapper
         * @return true if selected Entity is Assign or Assign bpel descendant - Copy, From or To
         */
        public boolean accepted(BpelEntity selectedEntity) {
            return selectedEntity instanceof ExtensibleElements 
                    || selectedEntity instanceof Trace
                    || selectedEntity instanceof Log
                    || selectedEntity instanceof Alert;
        }

        public BpelDesignContext create(BpelEntity selectedEntity, Node node, Lookup lookup) {
            if (!accepted(selectedEntity)) {
                return null;
            }
            //
            BpelDesignContext context =  null;
            Class<? extends BpelEntity> entityType = selectedEntity.getElementType();
            if (entityType == Trace.class) {
                BpelEntity parent = selectedEntity.getParent();
                if (parent != null && parent.getElementType() == Copy.class) {
                    BpelEntity nextParent = parent.getParent();
                    if (nextParent != null && 
                            nextParent.getElementType() == Assign.class) {
                        context = new BpelDesignContextImpl(nextParent, 
                                parent, selectedEntity, node, lookup);
                    }
                }
            } else if (entityType == Log.class || entityType == Alert.class) {
                BpelEntity parent = selectedEntity.getParent();
                if (parent != null && parent.getElementType() == Trace.class) {
                    BpelEntity nextParent = parent.getParent();
                    if (nextParent instanceof ExtensibleElements) {
                        context = new BpelDesignContextImpl(nextParent, 
                                parent, selectedEntity, node, lookup);
                    }
                    
                    context = new BpelDesignContextImpl(parent, 
                            selectedEntity, selectedEntity, node, lookup);
                }
            } else {
                context = new BpelDesignContextImpl(selectedEntity, 
                        selectedEntity, selectedEntity, node, lookup);
            } 
            //
            return context;
        }
    }
    
}
