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

package org.netbeans.modules.j2ee.spi.ejbjar;

import org.netbeans.modules.j2ee.api.ejbjar.EjbJar;
import org.netbeans.modules.j2ee.ejbjar.EjbJarAccessor;

/**
 * Most general way to create {@link EjbJar} instances.
 * You are not permitted to create them directly; instead you implement
 * {@link EjbJarImplementation} and use this factory.
 *
 * @author  Pavel Buzek
 */
public final class EjbJarFactory {

    private EjbJarFactory () {
    }

    /**
     * Create API webmodule instance for the given SPI webmodule.
     * @param spiWebmodule instance of SPI webmodule
     * @return instance of API webmodule
     */
    public static EjbJar createEjbJar(EjbJarImplementation spiWebmodule) {
        return EjbJarAccessor.DEFAULT.createEjbJar (spiWebmodule);
    }

}
