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

package org.netbeans.modules.java.freeform;

import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.swing.Icon;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.ant.freeform.spi.ProjectNature;
import org.netbeans.modules.ant.freeform.spi.TargetDescriptor;
import org.netbeans.spi.java.project.support.ui.PackageView;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.PathMatcher;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * General hook for registration of the Java nature for freeform projects.
 * @author David Konecny
 */
public class JavaProjectNature implements ProjectNature {

    public static final String NS_JAVA_1 = "http://www.netbeans.org/ns/freeform-project-java/1"; // NOI18N
    public static final String NS_JAVA_2 = "http://www.netbeans.org/ns/freeform-project-java/2"; // NOI18N
    public static final String EL_JAVA = "java-data"; // NOI18N
    public static final String STYLE_PACKAGES = "packages"; // NOI18N
    
    
    public JavaProjectNature() {}
    
    public List<TargetDescriptor> getExtraTargets(Project project, AntProjectHelper projectHelper, PropertyEvaluator projectEvaluator, AuxiliaryConfiguration aux) {
        return new ArrayList<TargetDescriptor>();
    }

    public Set<String> getSourceFolderViewStyles() {
        return Collections.singleton(STYLE_PACKAGES);
    }
    
    public org.openide.nodes.Node createSourceFolderView(Project project, final FileObject folder, final String includes,
            final String excludes, String style, final String name, final String displayName) throws IllegalArgumentException {
        if (style.equals(STYLE_PACKAGES)) {
            return PackageView.createPackageView(new SourceGroup() {
                PathMatcher matcher = new PathMatcher(includes, excludes, FileUtil.toFile(folder));
                public FileObject getRootFolder() {
                    return folder;
                }
                public String getName() {
                    return name;
                }
                public String getDisplayName() {
                    if (displayName != null) {
                        return displayName;
                    } else {
                        // Don't use folder.getNodeDelegate().getDisplayName() since we are not listening to changes anyway.
                        return folder.getNameExt();
                    }
                }
                public Icon getIcon(boolean opened) {
                    return null;
                }
                public boolean contains(FileObject file) throws IllegalArgumentException {
                    String path = FileUtil.getRelativePath(folder, file);
                    if (path == null) {
                        throw new IllegalArgumentException(file + " not in " + folder);
                    }
                    if (file.isFolder()) {
                        path += "/"; // NOI18N
                    }
                    return matcher.matches(path, true);
                }
                public void addPropertyChangeListener(PropertyChangeListener listener) {}
                public void removePropertyChangeListener(PropertyChangeListener listener) {}
            });
        } else {
            throw new IllegalArgumentException();
        }
    }

    public org.openide.nodes.Node findSourceFolderViewPath(Project project, org.openide.nodes.Node root, Object target) {
        return PackageView.findPath(root, target);
    }

    





    
}
