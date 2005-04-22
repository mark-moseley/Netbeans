/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.openide.nodes;

import java.lang.ref.*;
import java.util.*;
import org.openide.ErrorManager;
import junit.framework.*;
import org.netbeans.junit.*;

public class ChildrenFilterAsKeysTest extends ChildrenKeysTest {
    
    public ChildrenFilterAsKeysTest(java.lang.String testName) {
        super(testName);
    }


    protected Node createNode (Children ch) {
        return new FilterNode (new AbstractNode (ch));
    }
    
}
