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

package org.netbeans.modules.refactoring.api;

/**
 *
 * @author Jan Becicka
 */
public class SingleCopyRefactoring extends AbstractRefactoring {

    private Object object;
    private Object target;
    private String newName;

    public SingleCopyRefactoring (Object objectToCopy) {
        this.object = objectToCopy;
    }

    public Object getRefactoredObject() {
        return object;
    }

    public void setTarget(Object target) {
        this.target = target;
    }
    
    public Object getTarget() {
        return target;
    }
    
    public String getNewName() {
        return newName;
    }
    
    public void setNewName(String newName) {
        this.newName = newName;
    }
}

