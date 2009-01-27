/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.maven.spi.nodes;

import java.awt.Image;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.maven.indexer.api.NBVersionInfo;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;

/**
 *
 * @author Dafe Simonek
 */
public final class MavenNodeFactory {

    private static final String DELIMITER = " : ";

    private MavenNodeFactory() {
    }

    public static VersionNode createVersionNode (NBVersionInfo versionInfo, boolean fromMng) {
        return new VersionNode(versionInfo, fromMng);
    }

    public static ArtifactNode createArtifactNode (String name, final List<NBVersionInfo> list) {
        return new ArtifactNode(name, list);
    }

    public static class VersionNode extends AbstractNode {

        private NBVersionInfo nbvi;
        private boolean fromDepMng;

        /** Creates a new instance of VersionNode */
        public VersionNode(NBVersionInfo versionInfo, boolean fromDepMng) {
            super(Children.LEAF);

            this.nbvi = versionInfo;
            this.fromDepMng = fromDepMng;

            setName(versionInfo.getVersion());

            StringBuilder sb = new StringBuilder();
            if (fromDepMng) {
                sb.append(nbvi.getGroupId());
                sb.append(DELIMITER);
                sb.append(nbvi.getArtifactId());
        sb.append(DELIMITER);
            } else {
                sb.append(nbvi.getVersion());
            }
            sb.append(" [ ");
            sb.append(nbvi.getType());
            String classifier = nbvi.getClassifier();
            if (classifier != null) {
                sb.append(",");
                sb.append(classifier);
            }
            sb.append(" ] ");
            String repo = nbvi.getRepoId();
            if (repo != null) {
                sb.append(" - ");
                sb.append(repo);
            }

            setDisplayName(sb.toString());

            setIconBaseWithExtension("org/netbeans/modules/maven/resources/DependencyJar.gif"); //NOI18N

        }

        /*@Override
        public java.awt.Image getIcon(int param) {
            java.awt.Image retValue = super.getIcon(param);
            if (hasJavadoc) {
                retValue = ImageUtilities.mergeImages(retValue,
                        ImageUtilities.loadImage("org/netbeans/modules/maven/repository/DependencyJavadocIncluded.png"),//NOI18N
                        12, 12);
            }
            if (hasSources) {
                retValue = ImageUtilities.mergeImages(retValue,
                        ImageUtilities.loadImage("org/netbeans/modules/maven/repository/DependencySrcIncluded.png"),//NOI18N
                        12, 8);
            }
            return retValue;

        }*/

        public NBVersionInfo getNBVersionInfo() {
            return nbvi;
        }

        @Override
        public String getShortDescription() {
            return nbvi.toString();
        }
    }

    public static class ArtifactNode extends AbstractNode {

        private List<NBVersionInfo> versionInfos;

        public ArtifactNode(String name, final List<NBVersionInfo> list) {
            super(new Children.Keys<NBVersionInfo>() {
                @Override
                protected Node[] createNodes(NBVersionInfo arg0) {
                    return new Node[]{new VersionNode(arg0, false)};
                }

                @Override
                protected void addNotify() {
                    setKeys(list);
                }
            });
            this.versionInfos = list;
            setName(name);
            setDisplayName(name);
        }

        @Override
        public Image getIcon(int arg0) {
            Image badge = ImageUtilities.loadImage("org/netbeans/modules/maven/resources/ArtifactBadge.png", true); //NOI18N

            return badge;
        }

        @Override
        public Image getOpenedIcon(int arg0) {
            return getIcon(arg0);
        }

        public List<NBVersionInfo> getVersionInfos() {
            return new ArrayList<NBVersionInfo>(versionInfos);
        }
    }

}
