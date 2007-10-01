/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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

