/*
 * Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License Version
 * 1.0 (the "License"). You may not use this file except in compliance with
 * the License. A copy of the License is available at http://www.sun.com/
 * 
 * The Original Code is the Jemmy library.
 * The Initial Developer of the Original Code is Alexandre Iline.
 * All Rights Reserved.
 * 
 * Contributor(s): Alexandre Iline.
 * 
 * $Id$ $Revision$ $Date$
 * 
 */

package org.netbeans.jemmy.drivers;

import org.netbeans.jemmy.operators.ComponentOperator;

abstract public class SupportiveDriver implements Driver {
    private Class[] supported;
    public SupportiveDriver(Class[] supported) {
	this.supported = supported;
    }
    public void checkSupported(ComponentOperator oper) {
	UnsupportedOperatorException.checkSupported(getClass(), supported, oper.getClass());
    }
    public Class[] getSupported() {
	return(supported);
    }
}
