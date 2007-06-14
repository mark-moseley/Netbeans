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

package org.netbeans.modules.xml.wsdl.refactoring;

import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.MoveRefactoring;
import org.netbeans.modules.refactoring.api.RenameRefactoring;
import org.netbeans.modules.refactoring.api.SafeDeleteRefactoring;
import org.netbeans.modules.refactoring.api.WhereUsedQuery;
import org.netbeans.modules.refactoring.spi.RefactoringPlugin;
import org.netbeans.modules.refactoring.spi.RefactoringPluginFactory;


/**
 *
 * @author Sonali Kochar
 */
public class WSDLRefactoringsFactory implements RefactoringPluginFactory {
   
    public RefactoringPlugin createInstance(AbstractRefactoring refactoring) {
        if (refactoring instanceof WhereUsedQuery) {
            return new WSDLWhereUsedRefactoringPlugin( (WhereUsedQuery)refactoring);
        } else if (refactoring instanceof RenameRefactoring) {
            return new WSDLRenameRefactoringPlugin( (RenameRefactoring)refactoring);
        } else if(refactoring instanceof SafeDeleteRefactoring) {
            return new WSDLSafeDeleteRefactoringPlugin( (SafeDeleteRefactoring)refactoring);
        } else if(refactoring instanceof MoveRefactoring) {
            return new WSDLMoveRefactoringPlugin( (MoveRefactoring)refactoring);
        }
        return null;
    }
    
}

