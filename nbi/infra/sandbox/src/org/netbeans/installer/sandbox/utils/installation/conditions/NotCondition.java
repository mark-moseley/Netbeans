/*
 * The contents of this file are subject to the terms of the Common Development and
 * Distribution License (the License). You may not use this file except in compliance
 * with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html or
 * http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file and
 * include the License file at http://www.netbeans.org/cddl.txt. If applicable, add
 * the following below the CDDL Header, with the fields enclosed by brackets []
 * replaced by your own identifying information:
 * 
 *     "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original Software
 * is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun Microsystems, Inc. All
 * Rights Reserved.
 */



package org.netbeans.installer.sandbox.utils.installation.conditions;

import org.netbeans.installer.sandbox.utils.installation.InstallationFileObject;

/**
 *
 * @author Dmitry Lipin
 */
public class NotCondition  extends LogicalCondition implements FileCondition {
     public NotCondition(FileCondition ... conds) {
        setConditions(conds);
    }
    
    public boolean accept(InstallationFileObject fo) {
        boolean result = false;
        FileCondition [] conds = getConditions();
        if(conds.length>0) {
            return !conds[0].accept(fo);
        }
        return true;
    }
    
    public String getName() {
        return "NOT";
    }
    public LogicalCondition clone() {
        return new NotCondition();
    }
}
