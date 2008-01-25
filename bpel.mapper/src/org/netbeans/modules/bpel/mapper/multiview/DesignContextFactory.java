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
package org.netbeans.modules.bpel.mapper.multiview;

import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

/**
 * @author Vitaly Bychkov
 */
public interface DesignContextFactory {

    BpelDesignContext createBpelDesignContext(
            BpelEntity selectedEntity, Node node, Lookup lookup);
    
    BpelDesignContext getActivatedContext(BpelModel currentBpelModel);

    public interface ContextCreator {
        boolean accepted(BpelEntity selectedEntity);
        BpelDesignContext create(BpelEntity selectedEntity, Node node, Lookup lookup);
    }
    
    public class EmptyContextCreator implements ContextCreator {

        /**
         * @param selectedEntity - the selected bpel entity to show mapper
         */
        public boolean accepted(BpelEntity selectedEntity) {
            return selectedEntity != null;
        }

        public BpelDesignContext create(BpelEntity selectedEntity, Node node, Lookup lookup) {
            if (!accepted(selectedEntity)) {
                return null;
            }
            return new BpelDesignContextImpl(null, null, selectedEntity, node, lookup);
        }
    }
    
}
