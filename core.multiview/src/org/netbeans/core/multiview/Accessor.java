/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
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
        Object o = MultiViewPerspective.class;
    }    
    
    public abstract MultiViewPerspective createPerspective(MultiViewDescription desc);
    
//    public abstract MultiViewPerspectiveComponent createPersComponent(MultiViewElement elem);
    
    public abstract MultiViewHandler createHandler(MultiViewHandlerDelegate delegate);
    
//    public abstract MultiViewElement extractElement(MultiViewPerspectiveComponent comp);
    
    public abstract MultiViewDescription extractDescription(MultiViewPerspective perspective);
    
}
