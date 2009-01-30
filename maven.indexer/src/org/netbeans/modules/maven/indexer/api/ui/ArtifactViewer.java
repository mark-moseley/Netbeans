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

package org.netbeans.modules.maven.indexer.api.ui;

import java.util.List;
import java.util.logging.Logger;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.netbeans.api.project.Project;
import org.netbeans.core.api.multiview.MultiViewHandler;
import org.netbeans.core.api.multiview.MultiViewPerspective;
import org.netbeans.core.api.multiview.MultiViews;
import org.netbeans.modules.maven.indexer.api.NBVersionInfo;
import org.netbeans.modules.maven.indexer.spi.ui.ArtifactViewerFactory;
import org.openide.util.Lookup;
import org.openide.windows.TopComponent;

/**
 * a factory class for creation of artifact view components.
 * @author mkleint
 */
public final class ArtifactViewer {

    public static final String HINT_ARTIFACT = "art"; //NOI18N
    public static final String HINT_PROJECT = "prj"; //NOI18N
    public static final String HINT_DEPENDENCIES = "dep"; //NOI18N
    public static final String HINT_GRAPH = "grf"; //NOI18N



    private ArtifactViewer() {
    }

    /**
     * Shows detailed view component with information about the given artifact.
     * @param info
     */
    public static void showArtifactViewer(NBVersionInfo info, String panelHint) {
        showArtifactViewer(null, info, null, null, panelHint);

    }
    /**
     * Shows detailed view component with information about the given artifact.
     * @param info
     */
    public static void showArtifactViewer(NBVersionInfo info) {
        showArtifactViewer(null, info, null, null, null);
    }

    /**
     * Shows detailed view component with information about the given artifact.
     * @param info
     */
    public static void showArtifactViewer(Artifact artifact, List<ArtifactRepository> repos, String panelHint) {
        showArtifactViewer(null, null, artifact, repos, panelHint);
    }

    /**
     * Shows detailed view component with information about the given artifact.
     * @param info
     */
    public static void showArtifactViewer(Artifact artifact, List<ArtifactRepository> repos) {
        showArtifactViewer(null, null, artifact, repos, null);
    }

    /**
     * Shows detailed view component with information about the given project.
     * @param project
     * @param panelHint
     */
    public static void showArtifactViewer(Project project, String panelHint) {
        showArtifactViewer(project, null, null, null, panelHint);
    }

    private static void showArtifactViewer(Project project, NBVersionInfo info, Artifact artifact, List<ArtifactRepository> repos, String panelHint) {
        ArtifactViewerFactory fact = Lookup.getDefault().lookup(ArtifactViewerFactory.class);
        if (fact == null) {
            Logger.getLogger(ArtifactViewer.class.getName()).info("No implementation of ArtifactViewerFactory available.");
            return;
        }
        TopComponent tc;
        if (project != null) {
            tc = fact.createTopComponent(project);
        } else if (info != null) {
            tc = fact.createTopComponent(info);
        } else {
            tc = fact.createTopComponent(artifact, repos);
        }
        tc.open();
        tc.requestActive();
        if (panelHint != null) {
            MultiViewHandler hand = MultiViews.findMultiViewHandler(tc);
            if (hand == null) {
                return;
            }
            for (MultiViewPerspective pers : hand.getPerspectives()) {
                if (panelHint.equals(pers.preferredID())) {
                    hand.requestVisible(pers);
                    return;
                }
            }
        }
    }
}

