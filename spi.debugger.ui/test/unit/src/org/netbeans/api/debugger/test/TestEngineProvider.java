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

package org.netbeans.api.debugger.test;

import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.Session;
import org.netbeans.spi.debugger.DebuggerEngineProvider;
import org.netbeans.spi.debugger.ContextProvider;


/**
 * Represents a test debugger plug-in - one Debugger Implementation.
 *
 * @author Maros Sandor
 */
public class TestEngineProvider extends DebuggerEngineProvider {

    private DebuggerEngine.Destructor   destructor;
    private Session                     session;

    public TestEngineProvider (ContextProvider s) {
        session = (Session) s.lookupFirst(null, Session.class);
    }
    
    public String [] getLanguages () {
        return new String[] { "Basic" };
    }

    public String getEngineTypeID () {
        return TestDebugger.ENGINE_ID;
    }
    
    public Object[] getServices () {
        return new Object [0];
    }
    
    public void setDestructor (DebuggerEngine.Destructor desctuctor) {
        this.destructor = desctuctor;
    }
    
    public DebuggerEngine.Destructor getDestructor () {
        return destructor;
    }
    
    public Session getSession () {
        return session;
    }
}

