/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.j2ee.ejbjar;

import org.netbeans.modules.j2ee.api.ejbjar.EjbJar;
import org.netbeans.modules.j2ee.spi.ejbjar.EjbJarImplementation;

/* This class provides access to the {@link EjbJar}'s private constructor 
 * from outside in the way that this class is implemented by an inner class of 
 * {@link EjbJar} and the instance is set into the {@link DEFAULT}.
 */
public abstract class EjbJarAccessor {

    public static EjbJarAccessor DEFAULT;
    
    // force loading of EjbJar class. That will set DEFAULT variable.
    static {
        Object o = EjbJar.class;
    }
    
    public abstract EjbJar createEjbJar(EjbJarImplementation spiWebmodule);

    public abstract EjbJarImplementation getEjbJarImplementation (EjbJar wm);

}
