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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.compapp.test.ui.actions;

import org.netbeans.modules.compapp.projects.jbi.api.JbiProjectConstants;
import org.openide.*;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * Action to debug a test case.
 *
 * @author jqian
 */
public class TestCaseDebugAction extends AbstractTestCaseExecutionAction {
    
    protected boolean enable(Node[] activatedNodes) {
        return (activatedNodes.length == 1) &&
                (activatedNodes[0].getCookie(TestcaseCookie.class) != null) &&
                super.enable(activatedNodes);
    }
      
    public String getName() {
        return NbBundle.getMessage(TestCaseDebugAction.class, 
                "LBL_TestCaseDebugAction_Name");  // NOI18N
    }
    
    protected String getAntTargetName() {
        return JbiProjectConstants.COMMAND_DEBUG_SINGLE;
    }    
            
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
}
