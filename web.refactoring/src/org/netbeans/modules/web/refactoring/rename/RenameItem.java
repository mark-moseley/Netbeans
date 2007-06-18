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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.refactoring.rename;

/**
 * Encapsulates the new and old name.
 * TODO: needs a better name
 *
 * @author Erno Mononen
 */
class RenameItem {
    
    private final String oldFqn;
    private final String newFqn;
    
    public RenameItem(String newFqn, String oldFqn) {
        this.newFqn = newFqn;
        this.oldFqn = oldFqn;
    }
    
    public String getNewFqn() {
        return newFqn;
    }
    
    public String getOldFqn() {
        return oldFqn;
    }
    
}
