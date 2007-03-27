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

package org.openide.actions;

import org.openide.util.actions.CallbackSystemAction;

/**
 * Test behavior of CutAction together with cloning.
 */
public class CutActionTest extends AbstractCallbackActionTestHidden {
    public CutActionTest(String name) {
        super(name);
    }

    protected Class<? extends CallbackSystemAction> actionClass() {
        return CutAction.class;
    }
    
    protected String actionKey () {
        return javax.swing.text.DefaultEditorKit.cutAction;
    }
}
