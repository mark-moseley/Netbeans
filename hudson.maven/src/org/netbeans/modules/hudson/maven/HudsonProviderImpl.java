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

package org.netbeans.modules.hudson.maven;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.event.ChangeListener;
import org.apache.maven.model.CiManagement;
import org.netbeans.api.project.Project;
import org.netbeans.modules.hudson.spi.ProjectHudsonProvider;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.util.ChangeSupport;

/**
 *
 * @author mkleint
 */
@ProjectServiceProvider(service=ProjectHudsonProvider.class, projectType="org-netbeans-modules-maven")
public class HudsonProviderImpl implements ProjectHudsonProvider, PropertyChangeListener {

    private final Project project;
    private boolean associated;
    private final ChangeSupport cs = new ChangeSupport(this);

    public HudsonProviderImpl(Project project) {
        this.project = project;
        checkHudson();
        NbMavenProject.addPropertyChangeListener(project, this);
    }

    private CiManagement getCIManag() {
        NbMavenProject prj = project.getLookup().lookup(NbMavenProject.class);
        CiManagement cim = prj.getMavenProject().getCiManagement();
        return cim;

    }

    public boolean isAssociated() {
        return associated;
    }

    public String getServerUrl() {
        CiManagement mag = getCIManag();
        if (mag != null) {
            String url = mag.getUrl();
            int index = url.indexOf("/job/");
            if (index > 0) {
                url = url.substring(0, index + 1);
            }
            return url;
        }
        return "http://localhost/";
    }

    public String getJobName() {
        CiManagement mag = getCIManag();
        if (mag != null) {
            String url = mag.getUrl();
            int index = url.indexOf("/job/");
            if (index > 0) {
                url = url.substring(index + "/job/".length());
            }
            if (url.endsWith("/")) {
                url = url.substring(0, url.length() - 1);
            }
            return url;
        }
        return "";
    }

    public void addChangeListener(ChangeListener l) {
        cs.addChangeListener(l);
    }

    public void removeChangeListener(ChangeListener l) {
        cs.removeChangeListener(l);
    }

    public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
        if (NbMavenProject.PROP_PROJECT.equals(propertyChangeEvent.getPropertyName())) {
            boolean old = associated;
            checkHudson();
            if (old != associated) {
                cs.fireChange();
            }
        }
    }

    private void checkHudson() {
        CiManagement cim = getCIManag();
        associated = cim != null && cim.getSystem() != null && "hudson".equalsIgnoreCase(cim.getSystem());
    }

}
