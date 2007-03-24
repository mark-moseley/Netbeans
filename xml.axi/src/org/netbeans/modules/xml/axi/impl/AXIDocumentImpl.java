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
 * AXIDocumentImpl.java
 *
 * Created on May 10, 2006, 1:06 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.axi.impl;

import java.util.HashMap;
import org.netbeans.modules.xml.axi.AXIComponent;
import org.netbeans.modules.xml.axi.AXIDocument;
import org.netbeans.modules.xml.axi.AXIModel;
import org.netbeans.modules.xml.schema.model.SchemaComponent;

/**
 * AXIDocument implementation.
 *
 * @author Samaresh (Samaresh.Panda@Sun.Com)
 */
public final class AXIDocumentImpl extends AXIDocument {
    
    /**
     * Creates a new instance of AXIDocumentImpl
     */
    public AXIDocumentImpl(AXIModel model, SchemaComponent schemaComponent) {
        super(model, schemaComponent);
    }
    
    public AXIComponent findChild(SchemaComponent child) {
        //first try from cache, if not found, lookup children
        AXIComponent axiChild = globalChildrenCache.get(child);
        if(axiChild != null)
            return axiChild;
        for(AXIComponent c : getChildren()) {
            if(c.getPeer() == child) {
                addToCache(c);
                return c;
            }
        }
        return null;        
    }

    public void addToCache(AXIComponent child) {
        if(child.getPeer() == null)
            return;
        globalChildrenCache.put(child.getPeer(), child);
    }
    
    public void removeFromCache(AXIComponent child) {
        globalChildrenCache.remove(child.getPeer());
    }
    
    private HashMap<SchemaComponent, AXIComponent> globalChildrenCache =
            new HashMap<SchemaComponent, AXIComponent>();
}
