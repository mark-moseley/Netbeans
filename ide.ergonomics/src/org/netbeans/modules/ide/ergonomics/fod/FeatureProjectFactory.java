/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.ide.ergonomics.fod;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.util.Collection;
import javax.swing.Icon;
import org.netbeans.api.autoupdate.UpdateElement;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.spi.project.ProjectFactory;
import org.netbeans.spi.project.ProjectState;
import org.netbeans.spi.project.support.LookupProviderSupport;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.netbeans.spi.project.ui.support.UILookupMergerSupport;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>, Jirka Rechtacek <jrechtacek@netbeans.org>
 */
@ServiceProvider(service=ProjectFactory.class, position=30000)
public class FeatureProjectFactory implements ProjectFactory {

    public boolean isProject(FileObject projectDirectory) {
        for (FeatureInfo info : Feature2LayerMapping.features()) {
            if (info.isProject(projectDirectory, false)) {
                return true;
            }
        }
        return false;
    }

    public Project loadProject(FileObject projectDirectory, ProjectState state) throws IOException {
        for (FeatureInfo info : Feature2LayerMapping.features()) {
            if (info.isProject(projectDirectory, true)) {
                return new FeatureNonProject(projectDirectory, info, state);
            }
        }
        return null;
    }

    public void saveProject(Project project) throws IOException, ClassCastException {
    }


    private static final class FeatureNonProject implements Project {
        private final FeatureDelegate delegate;
        private final FeatureInfo info;
        private final Lookup lookup;
        private final ProjectState state;
        private boolean success = false;

        public FeatureNonProject(FileObject dir, FeatureInfo info, ProjectState state) {
            this.delegate = new FeatureDelegate(dir, this);
            this.info = info;
            this.lookup = Lookups.proxy(delegate);
            this.state = state;
        }
        
        public FileObject getProjectDirectory() {
            return delegate.dir;
        }

        public Lookup getLookup() {
            return lookup;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Project) {
                return ((Project)obj).getProjectDirectory().equals(getProjectDirectory());
            }
            return false;
        }

        @Override
        public int hashCode() {
            return getProjectDirectory().hashCode();
        }

        private final class FeatureOpenHook extends ProjectOpenedHook
        implements Runnable {
            @Override
            protected void projectOpened() {
                RequestProcessor.getDefault ().post (this, 0, Thread.NORM_PRIORITY).waitFinished ();
                if (success) {
                    try {
                        state.notifyDeleted();
                        Project p = ProjectManager.getDefault().findProject(getProjectDirectory());
                        if (p == FeatureNonProject.this) {
                            throw new IllegalStateException("New project shall be found! " + p); // NOI18N
                        }
                        delegate.associate(p);
                    } catch (Exception ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }

            @Override
            protected void projectClosed() {
            }

            public void run() {
                Feature2LayerMapping.logUI("ERGO_PROJECT_OPEN", info.clusterName);
                FindComponentModules findModules = new FindComponentModules(info);
                Collection<UpdateElement> toInstall = findModules.getModulesForInstall ();
                Collection<UpdateElement> toEnable = findModules.getModulesForEnable ();
                if (toInstall != null && ! toInstall.isEmpty ()) {
                    ModulesInstaller installer = new ModulesInstaller(toInstall, findModules);
                    installer.getInstallTask ().waitFinished ();
                    success = true;
                } else if (toEnable != null && ! toEnable.isEmpty ()) {
                    ModulesActivator enabler = new ModulesActivator (toEnable, findModules);
                    enabler.getEnableTask ().waitFinished ();
                    success = true;
                } else if (toEnable.isEmpty() && toInstall.isEmpty()) {
                    success = true;
                }
            }
        } // end of FeatureOpenHook
    } // end of FeatureNonProject
    private static final class FeatureDelegate 
    implements Lookup.Provider, ProjectInformation {
        private final FileObject dir;
        private final PropertyChangeSupport support;
        Lookup delegate;
        private final InstanceContent ic = new InstanceContent();
        private final Lookup hooks = new AbstractLookup(ic);


        public FeatureDelegate(FileObject dir, FeatureNonProject feature) {
            this.dir = dir;
            ic.add(UILookupMergerSupport.createProjectOpenHookMerger(feature.new FeatureOpenHook()));
            this.delegate = new ProxyLookup(
                Lookups.fixed(feature, this),
                LookupProviderSupport.createCompositeLookup(
                    hooks, "../nonsence" // NOI18N
                )
            );
            this.support = new PropertyChangeSupport(this);
        }

        public Lookup getLookup() {
            return delegate;
        }


        public String getName() {
            ProjectInformation info = delegate.lookup(ProjectInformation.class);
            if (info != this) {
                return info.getName();
            }
            return dir.getNameExt();
        }

        public String getDisplayName() {
            ProjectInformation info = delegate.lookup(ProjectInformation.class);
            if (info != this) {
                return info.getDisplayName();
            }
            return getName();
        }

        public Icon getIcon() {
            ProjectInformation info = delegate.lookup(ProjectInformation.class);
            if (info != this) {
                return info.getIcon();
            }
            return ImageUtilities.image2Icon(
                ImageUtilities.loadImage("org/netbeans/modules/ide/ergonomics/fod/project.png") // NOI18N
            );
        }

        public Project getProject() {
            return delegate.lookup(Project.class);
        }

        public void addPropertyChangeListener(PropertyChangeListener listener) {
            support.addPropertyChangeListener(listener);
        }

        public void removePropertyChangeListener(PropertyChangeListener listener) {
            support.removePropertyChangeListener(listener);
        }

        final void associate(Project p) {
            assert dir.equals(p.getProjectDirectory());
            ProjectInformation info = p.getLookup().lookup(ProjectInformation.class);
            if (info != null) {
                for (PropertyChangeListener l : support.getPropertyChangeListeners()) {
                    info.addPropertyChangeListener(l);
                }
            }
            delegate = p.getLookup();
            for (ProjectOpenedHook h : p.getLookup().lookupAll(ProjectOpenedHook.class)) {
                ic.add(h);
            }
            support.firePropertyChange(null, null, null);
        }
    }
}
