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

package org.netbeans.core.execution;

import java.security.PermissionCollection;
import java.security.Permission;
import java.util.Enumeration;

import org.openide.windows.InputOutput;

/** Every running process is represented by several objects in the ide whether
* or not it is executed as a thread or standalone process. The representation
* of a process should be marked by the IOPermissionCollection that gives possibility
* to such process to do its System.out/in operations through the ide.
*
* @author Ales Novak
*/
final class IOPermissionCollection extends PermissionCollection implements java.io.Serializable {

    /** InputOutput for this collection */
    private InputOutput io;
    /** Delegated PermissionCollection. */
    private PermissionCollection delegated;
    /** TaskThreadGroup ref or null */
    final TaskThreadGroup grp;

    static final long serialVersionUID =2046381622544740109L;
    /** Constructs new ExecutionIOPermission. */
    protected IOPermissionCollection(InputOutput io, PermissionCollection delegated, TaskThreadGroup grp) {
        this.io = io;
        this.delegated = delegated;
        this.grp = grp;
    }

    /** Standard implies method see java.security.Permission.
    * @param p a Permission
    */
    public boolean implies(Permission p) {
        return delegated.implies(p);
    }
    /** @return Enumeration of all Permissions in this collection. */
    public Enumeration<Permission> elements() {
        return delegated.elements();
    }
    /** @param perm a Permission to add. */
    public void add(Permission perm) {
        delegated.add(perm);
    }

    /** @return "" */ // NOI18N
    public InputOutput getIO() {
        return io;
    }
    /** Sets new io for this PermissionCollection */
    public void setIO(InputOutput io) {
        this.io = io;
    }

    public String toString() {
        return delegated.toString();
    }
}
