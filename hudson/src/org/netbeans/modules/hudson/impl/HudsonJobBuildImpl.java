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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.hudson.impl;

import java.util.ArrayList;
import org.netbeans.modules.hudson.api.HudsonJob.Color;
import org.netbeans.modules.hudson.api.HudsonMavenModuleBuild;
import org.netbeans.modules.hudson.impl.HudsonJobImpl.HudsonMavenModule;
import org.netbeans.modules.hudson.spi.HudsonJobChangeItem;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.hudson.api.HudsonJob;
import org.netbeans.modules.hudson.api.HudsonJobBuild;
import org.netbeans.modules.hudson.constants.HudsonXmlApiConstants;
import org.netbeans.modules.hudson.spi.HudsonSCM;
import org.netbeans.modules.hudson.ui.interfaces.OpenableInBrowser;
import org.openide.filesystems.FileSystem;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.w3c.dom.Document;

public class HudsonJobBuildImpl implements HudsonJobBuild, OpenableInBrowser {

    private final HudsonJobImpl job;
    private final int build;
    private final boolean building;
    private final Result result;
    private final HudsonConnector connector;

    HudsonJobBuildImpl(HudsonConnector connector, HudsonJobImpl job, int build, boolean building, Result result) {
        this.connector = connector;
        this.job = job;
        this.build = build;
        this.building = building;
        this.result = result;
    }
    
    public HudsonJob getJob() {
        return job;
    }

    public int getNumber() {
        return build;
    }

    public String getUrl() {
        return job.getUrl() + build + "/"; // NOI18N
    }

    public @Override String toString() {
        return getUrl();
    }

    public boolean isBuilding() {
        return building;
    }
    
    public Result getResult() {
        return result;
    }

    private Collection<? extends HudsonJobChangeItem> changes;
    public Collection<? extends HudsonJobChangeItem> getChanges() {
        if (changes == null) {
            Document changeSet = connector.getDocument(getUrl() +
                    HudsonXmlApiConstants.XML_API_URL + "?xpath=/*/changeSet"); // NOI18N
            if (changeSet != null) {
                for (HudsonSCM scm : Lookup.getDefault().lookupAll(HudsonSCM.class)) {
                    changes = scm.parseChangeSet(job, changeSet.getDocumentElement());
                    if (changes != null) {
                        break;
                    }
                }
            }
            if (changes == null) {
                changes = Collections.emptyList();
            }
        }
        return changes;
    }

    public FileSystem getArtifacts() {
        return job.getInstance().getArtifacts(this);
    }

    public Collection<? extends HudsonMavenModuleBuild> getMavenModules() {
        List<HudsonMavenModuleBuildImpl> modules = new ArrayList<HudsonMavenModuleBuildImpl>();
        for (HudsonJobImpl.HudsonMavenModule module : job.mavenModules) {
            modules.add(new HudsonMavenModuleBuildImpl(module));
        }
        return modules;
    }

    public String getDisplayName() {
        return NbBundle.getMessage(HudsonJobBuildImpl.class, "HudsonJobBuildImpl.display_name", job.getDisplayName(), getNumber());
    }

    private final class HudsonMavenModuleBuildImpl implements HudsonMavenModuleBuild, OpenableInBrowser {

        private final HudsonJobImpl.HudsonMavenModule module;

        HudsonMavenModuleBuildImpl(HudsonMavenModule module) {
            this.module = module;
        }

        public String getName() {
            return module.name;
        }

        public String getDisplayName() {
            return module.displayName;
        }

        public Color getColor() {
            return module.color;
        }

        public String getUrl() {
            return module.url + build + "/"; // NOI18N
        }

        public HudsonJobBuild getBuild() {
            return HudsonJobBuildImpl.this;
        }

        public FileSystem getArtifacts() {
            return job.getInstance().getArtifacts(this);
        }

        public @Override String toString() {
            return getUrl();
        }

        public String getBuildDisplayName() {
            return NbBundle.getMessage(HudsonJobBuildImpl.class, "HudsonJobBuildImpl.display_name", getDisplayName(), getNumber());
        }

    }

}
