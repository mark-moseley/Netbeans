/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.maven.api.archetype;

import java.util.List;

/**
 * For simple cases, use layer based registration instead:
 * 
     <p>
       "Projects/org-netbeans-modules-maven/Archetypes" folder contains fileobjects
       that represent archetypes. The archetypes are defined by the following file attributes:
     </p>
       <table>
           <tbody>
               <tr><td>groupId</td><td>mandatory</td><td></td></tr>
               <tr><td>artifactId</td><td>mandatory</td><td></td></tr>
               <tr><td>version</td><td>mandatory</td><td></td></tr>
               <tr><td>repository</td><td>optional</td><td>url of the archetype's repository</td></tr>
               <tr><td>nameBundleKey</td><td>optional</td><td>key in bundle file that holds localized name</td></tr>
               <tr><td>descriptionBundleKey</td><td>optional</td><td>key in bundle file that holds localized description</td></tr>
           </tbody>
       </table>
     <p>
 *<strike>
 * Componentized provider of list of available archetypes.
 * It is used in New Maven project wizard to populate the list of available archetypes.
 * The providers are expected to be registered using {@link org.openide.util.lookup.ServiceProvider}.
 * There are 3 default implementations registered: One lists 1 basic archetype
 * (simple and the other lists all archetypes it find in local and remote repository indexes.
 * </strike>
 * </p>
 * @author mkleint
 */
public interface ArchetypeProvider {

    /**
     * return Archetype instances known to this provider. Is called once per
     * New Maven Project wizard invokation.
     * @return list of archetypes
     */
    List<Archetype> getArchetypes();
}
