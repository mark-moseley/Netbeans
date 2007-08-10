/*
 * The contents of this file are subject to the terms of the Common
 * Development and Distribution License (the License). You may not use this 
 * file except in compliance with the License.  You can obtain a copy of the
 *  License at http://www.netbeans.org/cddl.html
 *
 * When distributing Covered Code, include this CDDL Header Notice in each
 * file and include the License. If applicable, add the following below the
 * CDDL Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Copyright 2006 Sun Microsystems, Inc. All Rights Reserved
 *
 */

package org.netbeans.modules.etl.ui.palette;

import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author nithya
 */
public class OperatorNode extends AbstractNode {
    
    private Operator model;
    
    /**
     * Creates a new instance of InstrumentNode
     * @param key 
     */
    public OperatorNode(Operator key) {
        super(Children.LEAF, Lookups.fixed( new Object[] {key} ) );
        this.model = key;
        setIconBaseWithExtension(key.getImage());
        setName(key.getName());
    }
    
    /**
     * 
     * @return model
     */
    public Operator getOperator() {
        return this.model;
    }    
}