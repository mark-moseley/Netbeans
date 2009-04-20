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
package org.netbeans.modules.php.project.ui.logicalview;

import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.php.project.PhpProject;
import org.netbeans.modules.php.project.ProjectPropertiesSupport;
import org.netbeans.modules.php.project.util.PhpProjectUtils;
import org.netbeans.spi.project.ui.support.NodeFactory;
import org.netbeans.spi.project.ui.support.NodeList;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.nodes.Node;
import org.openide.util.ChangeSupport;

/**
 *
 * @author Radek Matous
 */
@NodeFactory.Registration(projectType="org-netbeans-modules-php-project", position=100)
public class SourcesNodeFactory implements NodeFactory {
    private static final Logger LOGGER = Logger.getLogger(SourcesNodeFactory.class.getName());

    /** Creates a new instance of SourcesNodeFactory */
    public SourcesNodeFactory() {
    }

    public NodeList createNodes(Project p) {
        PhpProject prj = p.getLookup().lookup(PhpProject.class);
        return new SourceChildrenList(prj);
    }

    private static class SourceChildrenList implements NodeList<SourceGroup>, ChangeListener {

        private java.util.Map<SourceGroup, PropertyChangeListener> groupsListeners;
        private final PhpProject project;
        private final ChangeSupport changeSupport = new ChangeSupport(this);

        public SourceChildrenList(PhpProject project) {
            this.project = project;
        }

        private Sources getSources() {
            return ProjectUtils.getSources(project);
        }

        public void addNotify() {
            getSources().addChangeListener(this);
        }

        public void removeNotify() {
            getSources().removeChangeListener(this);
        }

        public void stateChanged(ChangeEvent e) {
            // #132877 - discussed with tomas zezula
            SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    fireChange();
                }
            });
        }

        private void fireChange() {
            changeSupport.fireChange();
        }

        private DataFolder getFolder(FileObject fileObject) {
            if (fileObject != null && fileObject.isValid()) {
                try {
                    DataFolder dataFolder = DataFolder.findFolder(fileObject);
                    return dataFolder;
                } catch (Exception ex) {
                    LOGGER.log(Level.INFO, null, ex);
                }
            }
            return null;
        }

        public List<SourceGroup> keys() {
            // parse SG
            // update SG listeners
            // XXX check if this is necessary
            final SourceGroup[] sourceGroups = PhpProjectUtils.getSourceGroups(project);
            final SourceGroup[] groups = new SourceGroup[sourceGroups.length];
            System.arraycopy(sourceGroups, 0, groups, 0, sourceGroups.length);

            List<SourceGroup> keysList = new ArrayList<SourceGroup>(groups.length);
            //Set<FileObject> roots = new HashSet<FileObject>();
            FileObject fileObject = null;
            for (int i = 0; i < groups.length; i++) {
                fileObject = groups[i].getRootFolder();
                DataFolder srcDir = getFolder(fileObject);

                if (srcDir != null) {
                    keysList.add(groups[i]);
                }
            //roots.add(fileObject);
            }
            return keysList;
        // Seems that we do not need to implement FileStatusListener
        // to listen to source groups root folders changes.
        // look at RubyLogicalViewRootNode for example.
        //updateSourceRootsListeners(roots);
        }

        public void addChangeListener(ChangeListener l) {
            changeSupport.addChangeListener(l);
        }

        public void removeChangeListener(ChangeListener l) {
            changeSupport.removeChangeListener(l);
        }

        public Node node(SourceGroup key) {
            Node node = null;
            if (key != null) {
                FileObject rootFolder = key.getRootFolder();
                DataFolder folder = getFolder(rootFolder);
                if (folder != null) {
                    boolean isTest = !folder.getPrimaryFile().equals(ProjectPropertiesSupport.getSourcesDirectory(project));
                    node = new SrcNode(project, folder, new PhpSourcesFilter(project, rootFolder), key.getDisplayName(), isTest);
                }
            }
            return node;
        }
    }
}
