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

import org.openide.util.Lookup;

/**
 * This class is just holder for parameters of Single Copy Refactoring. 
 * Refactoring itself is implemented in plugins
 * @see org.netbeans.modules.refactoring.spi.RefactoringPlugin
 * @see org.netbeans.modules.refactoring.spi.RefactoringPluginFactory
 * @see AbstractRefactoring
 * @see RefactoringSession
 * @author Jan Becicka
 */
public final class SingleCopyRefactoring extends AbstractRefactoring {

    private Lookup target;
    private String newName;

    /**
     * Creates a new instance of SingleCopyRefactoring.
     * Single Copy Refactoring implementations currently understand following types:
     * <table border="1">
     *   <tr><th>Module</th><th>Types the Module Understands</th><th>Implementation</th></tr>
     *   <tr><td>Refactoring API (Default impl.)</td><td>FileObject</td><td>Does file copy</td></tr>
     *   <tr><td>Java Refactoring</td><td><ul>
     *                                    <li>{@link FileObject}(s) with content type text/x-java (class copy)
     *                                    </ul>
     *                              <td>Updates name, package declaration and import statements</td></tr>
     * </table>
     * @param objectToCopy Object to be copied stored into Lookup
     */
    public SingleCopyRefactoring (Lookup objectToCopy) {
        super(objectToCopy);
    }

    /**
     * Target for copying.
     * Single Copy Refactoring implementations currently understand following types:
     * <table border="1">
     *   <tr><th>Module</th><th>Types the Module Understands</th><th>Implementation</th></tr>
     *   <tr><td>Refactoring API (Default impl.)</td><td>{@link java.net.URL}</td>
     *        <td>Creates directory corresponding to specified URL (if does not 
     *            exist) and copies all FileObjects into this folder.</td></tr>
     *   <tr><td>Java Refactoring</td><td>{@link java.net.URL}</td><td>Updates name, package declaration and import statements</td></tr>
     * </table>
     * @param target
     */
    public void setTarget(Lookup target) {
        this.target = target;
    }
    
    /**
     * Target for copying
     * @see #setTarget
     * @return target
     */
    public Lookup getTarget() {
        return target;
    }
    
    /**
     * getter for new name of copied file
     * @return value String value
     */
    public String getNewName() {
        return newName;
    }
    
    /**
     * setter for new name of copied file
     * @param newName new value
     */
    public void setNewName(String newName) {
        this.newName = newName;
    }
}

