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

package org.netbeans.spi.project;

import java.util.Set;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;

/**
 * Optional ability of projects which may have a list of "subprojects".
 * The exact interpretation of this term is at the discretion of the project,
 * but typically subprojects would be "built" as part of this project or somehow
 * used in it as dependencies; or they may just be contained or agglomerated in
 * it somehow.
 * @see Project#getLookup
 * @see <a href="@org-netbeans-modules-project-ant@/org/netbeans/spi/project/support/ant/ReferenceHelper.html#createSubprojectProvider()"><code>ReferenceHelper.createSubprojectProvider</code></a>
 * @author Jesse Glick
 */
public interface SubprojectProvider {
    
    /**
     * Get a set of projects which this project can be considered to depend upon somehow.
     * This information is likely to be used only for UI purposes.
     * Only direct subprojects need be listed, not all recursive subprojects.
     * There may be no direct or indirect cycles in the project dependency graph
     * but it may be a DAG, i.e. two projects may both depend on the same subproject.
     * @return an immutable and unchanging set of {@link Project}s
     * @see org.netbeans.api.project.ProjectUtils#hasSubprojectCycles
     */
    Set<? extends Project> getSubprojects();
    
    /**
     * Add a listener to changes in the set of subprojects.
     * @param listener a listener to add
     */
    void addChangeListener(ChangeListener listener);
    
    /**
     * Remove a listener to changes in the set of subprojects.
     * @param listener a listener to remove
     */
    void removeChangeListener(ChangeListener listener);
    
}
