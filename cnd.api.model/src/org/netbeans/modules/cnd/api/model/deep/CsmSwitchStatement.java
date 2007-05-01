/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

package org.netbeans.modules.cnd.api.model.deep;

import org.netbeans.modules.cnd.api.model.CsmScope;
import java.util.List;

/**
 * Represents switch statement;
 * getCodeBlock().getStatements() returns the list of the statements;
 *
 * TODO: perhaps it isn't worth to subclass CsmCompoundStatement and we'd better
 * add a separate member getStatements().
 *
 * TODO: perhaps we should provide some higher level of service
 * for determining the groups of statements for each case
 *
 * @author Vladimir Kvashin
 */
public interface CsmSwitchStatement extends CsmScope  {

    /** gets switch condition */
    CsmCondition getCondition();
    
    /** gets swithc body */
    CsmStatement getBody();
    
}
