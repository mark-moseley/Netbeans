/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.tax;

/**
 * @author  Libor Kramolis
 * @version 0.1
 */
public class NotSupportedException extends TreeException {
        
    /** Serial Version UID */
    private static final long serialVersionUID =8614780080672026461L;
    
    //
    // init
    //

    /**
     */
    public NotSupportedException (String msg) {
        super (msg);
    }

}
