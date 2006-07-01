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

package org.netbeans.core.multiview;

import org.netbeans.core.api.multiview.MultiViewHandler;
import org.netbeans.core.api.multiview.MultiViewPerspective;
import org.netbeans.core.spi.multiview.MultiViewDescription;
import org.netbeans.core.spi.multiview.MultiViewElement;

/**
 * @author  mkleint
 */
public abstract class Accessor {

    protected static Accessor DEFAULT = null;

    static {
        // invokes static initializer of Item.class
        // that will assign value to the DEFAULT field above
        Class c = MultiViewPerspective.class;
        try {
            Class.forName(c.getName(), true, c.getClassLoader());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }    
    
    public abstract MultiViewPerspective createPerspective(MultiViewDescription desc);
    
//    public abstract MultiViewPerspectiveComponent createPersComponent(MultiViewElement elem);
    
    public abstract MultiViewHandler createHandler(MultiViewHandlerDelegate delegate);
    
//    public abstract MultiViewElement extractElement(MultiViewPerspectiveComponent comp);
    
    public abstract MultiViewDescription extractDescription(MultiViewPerspective perspective);
    
}
