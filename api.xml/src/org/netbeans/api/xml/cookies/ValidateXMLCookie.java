/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.api.xml.cookies;

import org.openide.nodes.Node;

/**
 * Validate XML entity for semantics correctness. It must not have any UI.
 *
 * @author  Petr Kuzel
 * @deprecated XML tools API candidate
 */
public interface ValidateXMLCookie extends Node.Cookie {

    /**
     * Validate XML entity for semantics correctness.
     * @param report optional listener (<code>null</code> allowed)
     *               giving judgement details
     * @return true if validity check passes
     */
    boolean validateXML(ProcessorListener l);

}
