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

package org.netbeans.api.project.ant;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ant.AntArtifactProvider;
import org.netbeans.spi.project.ant.AntArtifactQueryImplementation;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;

/**
 * Find out how to create a certain build artifact by calling some Ant script.
 * @see AntArtifactQueryImplementation
 * @author Jesse Glick
 */
public class AntArtifactQuery {
    
    private AntArtifactQuery() {}
    
    /**
     * Try to find an Ant artifact object corresponding to a given file on disk.
     * The file need not currently exist for the query to succeed.
     * All registered {@link AntArtifactQueryImplementation} providers are asked.
     * @param file a file which might be built by some Ant target
     * @return an Ant artifact object describing it, or null if it is not recognized
     */
    public static AntArtifact findArtifactFromFile(File file) {
        if (!file.equals(FileUtil.normalizeFile(file))) {
            throw new IllegalArgumentException("Parameter file was not "+  // NOI18N
                "normalized. Was "+file+" instead of "+FileUtil.normalizeFile(file));  // NOI18N
        }
        for (AntArtifactQueryImplementation aaqi : Lookup.getDefault().lookupAll(AntArtifactQueryImplementation.class)) {
            AntArtifact artifact = aaqi.findArtifact(file);
            if (artifact != null) {
                return artifact;
            }
        }
        return null;
    }
    
    /**
     * Try to find a particular build artifact according to the Ant target producing it.
     * @param p a project (should have {@link AntArtifactProvider} in lookup
     *          in order for this query to work)
     * @param id a desired {@link AntArtifact#getID ID}
     * @return an artifact produced by that project with the specified target,
     *         or null if none such can be found
     */
    public static AntArtifact findArtifactByID(Project p, String id) {
        AntArtifactProvider prov = p.getLookup().lookup(AntArtifactProvider.class);
        if (prov == null) {
            return null;
        }
        for (AntArtifact artifact : prov.getBuildArtifacts()) {
            if (artifact.getID().equals(id)) {
                return artifact;
            }
        }
        return null;
    }
    
    /**
     * Try to find build artifacts of a certain type in a project.
     * @param p a project (should have {@link AntArtifactProvider} in lookup
     *          in order for this query to work)
     * @param type a desired {@link AntArtifact#getType artifact type}
     * @return all artifacts of the specified type produced by that project
     *         (may be an empty list)
     */
    public static AntArtifact[] findArtifactsByType(Project p, String type) {
        AntArtifactProvider prov = p.getLookup().lookup(AntArtifactProvider.class);
        if (prov == null) {
            return new AntArtifact[0];
        }
        AntArtifact[] artifacts = prov.getBuildArtifacts();
        List<AntArtifact> l = new ArrayList<AntArtifact>(artifacts.length);
        for (AntArtifact aa : artifacts) {
            if (aa.getType().equals(type)) {
                l.add(aa);
            }
        }
        return l.toArray(new AntArtifact[l.size()]);
    }
    
}
