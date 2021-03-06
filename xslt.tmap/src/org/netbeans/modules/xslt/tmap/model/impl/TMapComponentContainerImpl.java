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
package org.netbeans.modules.xslt.tmap.model.impl;

import java.util.List;
import org.netbeans.modules.xslt.tmap.model.api.TMapComponent;
import org.netbeans.modules.xslt.tmap.model.api.TMapComponentContainer;
import org.w3c.dom.Element;

/**
 *
 * @author Vitaly Bychkov
 * @version 1.0
 */
public abstract class TMapComponentContainerImpl 
        extends TMapComponentAbstract
        implements  TMapComponentContainer 
{
    public TMapComponentContainerImpl(TMapModelImpl model, Element e) {
        super(model, e);
    }

    public TMapComponentContainerImpl(TMapModelImpl model, TMapComponents type) {
        super(model, createNewElement(type, model));
    }

    public <T extends TMapComponent> int indexOf(Class<T> type, T component) {
//        readLock();
//        try {
            List<T> list = getChildren( type );
            return list.indexOf( component );
//        }
//        finally {
//            readUnlock();
//        }
    }

    public <T extends TMapComponent> void remove(T component) {
        removeChild(((TMapComponentAbstract)component).getComponentName(), 
                component);
    }
    
    
    
    
    
    
    
    
    
    
    
}
