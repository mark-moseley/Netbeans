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
package org.netbeans.jellytools.modules.freeform.actions;

import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.actions.Action;

/** Used to call "Redeploy Project" popup menu item on project's root node.
 * @see Action
 * @see org.netbeans.jellytools.modules.freeform.nodes.FreeformProjectNode
 * @author Martin.Schovanek@sun.com
 */
public class RedeployFreeformAction extends Action {

    private static final String redeployProject = Bundle.getStringTrimmed(
            "org.netbeans.modules.ant.freeform.Bundle",
            "CMD_redeploy");

    /**
     * creates new RunFreeformAction instance
     */    
    public RedeployFreeformAction() {
        super(null, redeployProject);
    }
}
