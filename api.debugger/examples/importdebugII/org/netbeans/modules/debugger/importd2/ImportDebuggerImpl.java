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

package org.netbeans.modules.debugger.importd2;

import org.openide.debugger.Debugger;

import org.netbeans.modules.debugger.*;


/**
 * This class represents JPDA Debugger Implementation.
 *
 * @author Jan Jancura
 */
public class ImportDebuggerImpl extends DebuggerImpl {

    static ImportDebugger impl;


    /**
     * Returns displayable name of JPDA debugger.
     *
     * @return displayable name of JPDA debugger
     */
    public  String getDisplayName () {
        return ImportDebugger.getLocString ("CTL_Import_Debugger");
    }

    /**
     * Returns a new instance of Debugger.
     */
    public AbstractDebugger createDebugger () {
        if (impl == null) impl = new ImportDebugger ();
        return new org.netbeans.modules.debugger.support.DelegatingDebugger (impl);
    }
}

