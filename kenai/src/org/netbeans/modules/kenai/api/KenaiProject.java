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

package org.netbeans.modules.kenai.api;

import java.net.MalformedURLException;
import java.net.URL;
import org.netbeans.modules.kenai.FeatureData;
import org.netbeans.modules.kenai.ProjectData;

/**
 * IDE-side representation of a Kenai project.
 *
 * @author Maros Sandor
 * @author Jan Becicka
 */
public final class KenaiProject {

    private String    name;

    private URL       href;

    private ProjectData     data;
    
    private KenaiProjectFeature[] features;

    /**
     * When detailed properties of this project has been fetched.
     */
    private long        detailsTimestamp;

    /**
     * I assume that this constructor does NOT provide full project information. If it does then
     * call fillInfo() just after the object is created.
     *
     * @param p
     */
    KenaiProject(ProjectData p) {
        this.name = p.name;
        try {
            this.href = new URL(p.href);
        } catch (MalformedURLException ex) {
            throw new IllegalArgumentException(ex);
        }
        this.data = p;
    }

    public String getName() {
        return name;
    }

    public URL getWebLocation() {
        return href;
    }

    public String getDisplayName() {
        return data.display_name;
    }

    public String getDescription() {
        fetchDetailsIfNotAvailable();
        return data.description;
    }

    public String[] getTags() {
        return new String[0];
    }

    public synchronized KenaiProjectFeature[] getFeatures() {
        if (features==null) {
            features=new KenaiProjectFeature[data.features.length];
            int i=0;
            for (FeatureData feature:data.features) {
                features[i++] = new KenaiProjectFeature(feature);
            }
        }
        return features;
    }

    /**
     * Creates new feateru for this project
     * @param projectName
     * @param name
     * @param display_name
     * @param description
     * @param service
     * @param url
     * @param repository_url
     * @param browse_url
     * @return
     * @throws org.netbeans.modules.kenai.api.KenaiException
     */
    KenaiProjectFeature createProjectFeature(
            String name,
            String display_name,
            String description,
            String service,
            String url,
            String repository_url,
            String browse_url
            ) throws KenaiException {
        KenaiProjectFeature feature = Kenai.getDefault().createProjectFeature(getName(), name, display_name, description, service, url, repository_url, browse_url);
        refresh();
        return feature;
    }

    void fillInfo(ProjectData prj) {
        detailsTimestamp = System.currentTimeMillis();
    }

    ProjectData getData() {
        return data;
    }

    private void fetchDetailsIfNotAvailable() {
        if (detailsTimestamp > 0) return;

//        try {
//            ProjectData prj = kenai.getDetails(name);
//            fillInfo(prj);
//        } catch (KenaiException kenaiException) {
//            Utils.logError(this, kenaiException);
//        }
    }

    private void refresh() throws KenaiException {
        this.data = Kenai.getDefault().getDetails(getName());

        this.name = data.name;
        try {
            this.href = new URL(data.href);
        } catch (MalformedURLException ex) {
            throw new IllegalArgumentException(ex);
        }
        features=null;
    }
}
