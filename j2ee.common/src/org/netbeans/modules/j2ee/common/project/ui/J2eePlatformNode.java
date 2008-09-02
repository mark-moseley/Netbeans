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

package org.netbeans.modules.j2ee.common.project.ui;


import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.netbeans.api.project.Project;
import org.openide.nodes.Children;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Node;
import org.openide.util.WeakListeners;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.j2ee.common.project.classpath.ClassPathSupport;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.InstanceListener;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.java.project.support.ui.PackageView;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.actions.SystemAction;

/**
 * J2eePlatformNode represents the J2EE platform in the logical view.
 * Listens on the {@link PropertyEvaluator} for change of
 * the ant property holding the platform name.
 * @see J2eePlatform
 * @author Andrei Badea
 */
class J2eePlatformNode extends AbstractNode implements PropertyChangeListener, InstanceListener {

    private static final String ARCHIVE_ICON = "org/netbeans/modules/j2ee/common/project/ui/resources/jar.gif"; //NOI18N
    private static final String DEFAULT_ICON = "org/netbeans/modules/j2ee/common/project/ui/resources/j2eeServer.gif"; //NOI18N
    private static final String BROKEN_PROJECT_BADGE = "org/netbeans/modules/j2ee/common/project/ui/resources/brokenProjectBadge.gif"; //NOI18N
    
    private static final Icon icon = new ImageIcon(Utilities.loadImage(ARCHIVE_ICON));
    
    private static final Image brokenIcon = Utilities.mergeImages(
            Utilities.loadImage(DEFAULT_ICON),
            Utilities.loadImage(BROKEN_PROJECT_BADGE), 
            8, 0);

    private final PropertyEvaluator evaluator;
    private final String platformPropName;
    private J2eePlatform platformCache;
    
    private final PropertyChangeListener platformListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent evt) {
            if (J2eePlatform.PROP_DISPLAY_NAME.equals(evt.getPropertyName())) {
                fireNameChange((String)evt.getOldValue(), (String)evt.getNewValue());
                fireDisplayNameChange((String)evt.getOldValue(), (String)evt.getNewValue());
            }
            if (J2eePlatform.PROP_CLASSPATH.equals(evt.getPropertyName())) {
                postAddNotify();
            }
        }
    };
    private PropertyChangeListener weakPlatformListener;

    private J2eePlatformNode(Project project, PropertyEvaluator evaluator, String platformPropName, ClassPathSupport cs) {
        super(new PlatformContentChildren(cs));
        this.evaluator = evaluator;
        this.platformPropName = platformPropName;
        evaluator.addPropertyChangeListener(WeakListeners.propertyChange(this, evaluator));
        
        J2eeModuleProvider moduleProvider = (J2eeModuleProvider)project.getLookup().lookup(J2eeModuleProvider.class);
        moduleProvider.addInstanceListener(
                (InstanceListener)WeakListeners.create(InstanceListener.class, this, moduleProvider));
    }
    
    public static J2eePlatformNode create(Project project, PropertyEvaluator evaluator, String platformPropName, ClassPathSupport cs) {
        return new J2eePlatformNode(project, evaluator, platformPropName, cs);
    }

    public String getName () {
        return this.getDisplayName();
    }
    
    public String getDisplayName() {
        return "";
    }
    
    public String getHtmlDisplayName() {
        if (getPlatform() != null)
            return getPlatform().getDisplayName();
        else 
            return NbBundle.getMessage(J2eePlatformNode.class, "LBL_J2eeServerMissing");
    }
    
    public Image getIcon(int type) {
        Image result = null;
        if (getPlatform() != null) {
            result = getPlatform().getIcon();
        }
        return result != null ? result : brokenIcon;
    }
    
    public Image getOpenedIcon(int type) {
        return getIcon(type);
    }

    public boolean canCopy() {
        return false;
    }
    
    public Action[] getActions(boolean context) {
        return new SystemAction[0];
    }

    public void propertyChange(PropertyChangeEvent evt) {
        //The caller holds ProjectManager.mutex() read lock
        
        if (platformPropName.equals(evt.getPropertyName())) {
            refresh();
        }
    }
    
    private void refresh() {
        if (platformCache != null)
            platformCache.removePropertyChangeListener(weakPlatformListener);

        platformCache = null;

        this.fireNameChange(null, null);
        this.fireDisplayNameChange(null, null);
        this.fireIconChange();
        
        // The caller may hold ProjectManager.mutex() read lock (i.e., the propertyChange() method)
        postAddNotify();
    }
    
    public void instanceAdded(String serverInstanceID) {
        refresh();
    }
    
    public void instanceRemoved(String serverInstanceID) {
        refresh();
    }
    
    public void changeDefaultInstance(String oldServerInstanceID, String newServerInstanceID) {
    }

    private void postAddNotify() {
        LibrariesNode.rp.post (new Runnable () {
            public void run () {
                ((PlatformContentChildren)getChildren()).addNotify ();
            }
        });
    }

    private J2eePlatform getPlatform () {
        if (platformCache == null) {
            String j2eePlatformInstanceId = this.evaluator.getProperty(this.platformPropName);
            if (j2eePlatformInstanceId != null) {
                platformCache = Deployment.getDefault().getJ2eePlatform(j2eePlatformInstanceId);
            }
            if (platformCache != null) {
                weakPlatformListener = WeakListeners.propertyChange(platformListener, platformCache);
                platformCache.addPropertyChangeListener(weakPlatformListener);
                // the platform has likely changed, so force the node to display the new platform's icon
                this.fireIconChange();
            }
        }
        return platformCache;
    }

    private static class PlatformContentChildren extends Children.Keys<SourceGroup> {

        private ClassPathSupport cs;
        PlatformContentChildren (ClassPathSupport cs) {
            this.cs = cs;
        }

        @Override
        protected void addNotify() {
            this.setKeys (this.getKeys());
        }

        @Override
        protected void removeNotify() {
            this.setKeys(Collections.<SourceGroup>emptySet());
        }

        protected Node[] createNodes(SourceGroup sg) {
            return new Node[] {ActionFilterNode.create(PackageView.createPackageView(sg), null, null, null, null, null, null)};
        }

        private List<SourceGroup> getKeys () {
            List<SourceGroup> result;
            
            J2eePlatform j2eePlatform = ((J2eePlatformNode)this.getNode()).getPlatform();
            if (j2eePlatform != null) {
                File[] classpathEntries = j2eePlatform.getClasspathEntries();
                result = new ArrayList<SourceGroup>(classpathEntries.length);
                for (int i = 0; i < classpathEntries.length; i++) {
                    FileObject file = FileUtil.toFileObject(classpathEntries[i]);
                    if (file != null) {
                        FileObject archiveFile = FileUtil.getArchiveRoot(file);
                        if (archiveFile != null) {
                            result.add(new LibrariesSourceGroup(archiveFile, file.getNameExt(), icon, icon));
                        }
                    }
                }
            } else {
                result = Collections.<SourceGroup>emptyList();
            }
            
            return result;
        }
    }
}
