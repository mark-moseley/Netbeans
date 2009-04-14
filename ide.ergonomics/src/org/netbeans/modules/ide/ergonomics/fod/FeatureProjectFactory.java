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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.io.InputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import javax.swing.Icon;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.xml.parsers.DocumentBuilder;
import org.netbeans.api.autoupdate.UpdateElement;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.spi.project.ProjectFactory;
import org.netbeans.spi.project.ProjectState;
import org.netbeans.spi.project.SubprojectProvider;
import org.netbeans.spi.project.support.LookupProviderSupport;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.netbeans.spi.project.ui.support.UILookupMergerSupport;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;
import org.openide.util.lookup.ServiceProvider;
import org.w3c.dom.Document;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.netbeans.api.project.ui.OpenProjects;
import org.openide.filesystems.FileUtil;
import org.xml.sax.SAXException;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>, Jirka Rechtacek <jrechtacek@netbeans.org>
 */
@ServiceProvider(service=ProjectFactory.class, position=30000)
public class FeatureProjectFactory implements ProjectFactory, PropertyChangeListener {

    public FeatureProjectFactory() {
        OpenProjects.getDefault().addPropertyChangeListener(this);
    }

    final static class Data {
        private final boolean deepCheck;
        private final FileObject dir;
        private Map<String,String> data;
        private Map<String,Document> doms;

        public Data(FileObject dir, boolean deepCheck) {
            this.deepCheck = deepCheck;
            this.dir = dir;
        }

        Document dom(String relative) {
            Document doc = doms == null ? null : doms.get(relative);
            if (doc != null) {
                return doc;
            }
            FileObject fo = dir.getFileObject(relative);
            if (fo == null) {
                return null;
            }
            File f = FileUtil.toFile(fo);
            try {
                DocumentBuilder b = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                if (f != null) {
                    doc = b.parse(f);
                } else {
                    InputStream is = fo.getInputStream();
                    doc = b.parse(is);
                }
                if (doms == null) {
                    doms = new HashMap<String,Document>();
                }
                doms.put(relative, doc);
                return doc;
            } catch (ParserConfigurationException parserConfigurationException) {
                Exceptions.printStackTrace(parserConfigurationException);
            } catch (SAXException sAXException) {
                Exceptions.printStackTrace(sAXException);
            } catch (IOException iOException) {
                Exceptions.printStackTrace(iOException);
            }
            return null;
        }

        final boolean hasFile(String relative) {
            return dir.getFileObject(relative) != null;
        }

        final boolean isDeepCheck() {
            return deepCheck;
        }

        @Override
        public String toString() {
            return dir.getPath();
        }

        final synchronized String is(String relative) {
            FileObject prj = dir.getFileObject(relative);
            if (prj == null) {
                return null;
            }

            String content = data == null ? null : data.get(relative);
            if (content != null) {
                return content;
            }

            byte[] arr = new byte[4000];
            int len;
            InputStream is = null;
            try {
                is = prj.getInputStream();
                len = is.read(arr);
                if (len >= 0) {
                    content = new String(arr, 0, len, "UTF-8");
                }
            } catch (IOException ex) {
                FoDFileSystem.LOG.log(Level.FINEST, "exception while reading " + prj, ex); // NOI18N
                len = -1;
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }
            FoDFileSystem.LOG.log(Level.FINEST, "    read {0} bytes", len); // NOI18N
            if (len == -1) {
                return null;
            }

            if (data == null) {
                data = new HashMap<String,String>();
            }

            data.put(relative, content);
            return content;
        }
    }


    public boolean isProject(FileObject projectDirectory) {
        Data d = new Data(projectDirectory, false);

        for (FeatureInfo info : FeatureManager.features()) {
            if (!info.isEnabled() && (info.isProject(d) == 1)) {
                return true;
            }
        }
        return false;
    }

    public Project loadProject(FileObject projectDirectory, ProjectState state) throws IOException {
        Data d = new Data(projectDirectory, true);
        
        FeatureInfo lead = null;
        List<FeatureInfo> additional = new ArrayList<FeatureInfo>();
        int notEnabled = 0;
        for (FeatureInfo info : FeatureManager.features()) {
            switch (info.isProject(d)) {
                case 0: break;
                case 1:
                    lead = info;
                    if (!info.isEnabled()) {
                        notEnabled++;
                    }
                break;
                case 2:
                    additional.add(info);
                    if (!info.isEnabled()) {
                        notEnabled++;
                    }
                    break;
                default: assert false;
            }
        }
        if (lead == null || notEnabled == 0) {
            return null;
        }

        return new FeatureNonProject(projectDirectory, lead, state, additional);
    }

    public void saveProject(Project project) throws IOException, ClassCastException {
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if (OpenProjects.PROPERTY_OPEN_PROJECTS.equals(evt.getPropertyName())) {
            final List<FeatureInfo> additional = new ArrayList<FeatureInfo>();
            FeatureInfo f = null;
            for (Project p : OpenProjects.getDefault().getOpenProjects()) {
                Data d = new Data(p.getProjectDirectory(), true);

                for (FeatureInfo info : FeatureManager.features()) {
                    switch (info.isProject(d)) {
                        case 0: break;
                        case 1: 
                            f = info;
                            break;
                        case 2:
                            f = info;
                            additional.add(info);
                            break;
                        default: assert false;
                    }
                }
            }

            if (f != null && !additional.isEmpty()) {
                final FeatureInfo finalF = f;
                final FeatureInfo[] addF = additional.toArray(new FeatureInfo[0]);
                class Enable implements Runnable {
                    private boolean success;
                    public void run() {
                        FeatureManager.logUI("ERGO_PROJECT_OPEN", finalF.clusterName);
                        FindComponentModules findModules = new FindComponentModules(finalF, addF);
                        Collection<UpdateElement> toInstall = findModules.getModulesForInstall();
                        Collection<UpdateElement> toEnable = findModules.getModulesForEnable();
                        if (toInstall != null && !toInstall.isEmpty()) {
                            ModulesInstaller installer = new ModulesInstaller(toInstall, findModules);
                            installer.getInstallTask().waitFinished();
                            success = true;
                        } else if (toEnable != null && !toEnable.isEmpty()) {
                            ModulesActivator enabler = new ModulesActivator(toEnable, findModules);
                            enabler.getEnableTask().waitFinished();
                            success = true;
                        } else if (toEnable == null || toInstall == null) {
                            success = true;
                        } else if (toEnable.isEmpty() && toInstall.isEmpty()) {
                            success = true;
                        }
                    }
                }
                Enable en = new Enable();
                RequestProcessor.getDefault ().post (en, 0, Thread.NORM_PRIORITY).waitFinished ();
            }
        }
    }

    private static final class FeatureNonProject 
    implements Project, ChangeListener {
        private final FeatureDelegate delegate;
        private final FeatureInfo info;
        private final FeatureInfo[] additional;
        private final Lookup lookup;
        private ProjectState state;
        private String error;
        private final ChangeListener weakL;

        public FeatureNonProject(
            FileObject dir, FeatureInfo info,
            ProjectState state, List<FeatureInfo> additional
        ) {
            this.delegate = new FeatureDelegate(dir, this);
            this.info = info;
            this.additional = additional.toArray(new FeatureInfo[0]);
            this.lookup = Lookups.proxy(delegate);
            this.state = state;
            this.weakL = WeakListeners.change(this, FeatureManager.getInstance());
            FeatureManager.getInstance().addChangeListener(weakL);
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

        public void stateChanged(ChangeEvent e) {
            if (info.isEnabled()) {
                switchToReal();
            }
        }
        final void switchToReal() {
            ProjectState s = null;
            synchronized (this) {
                s = state;
                state = null;
            }
            if (s != null) {
                try {
                    s.notifyDeleted();
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

        private final class FeatureOpenHook extends ProjectOpenedHook
        implements Runnable, ProgressMonitor {
            @Override
            protected void projectOpened() {
                if (state == null) {
                    return;
                }
                RequestProcessor.getDefault ().post (this, 0, Thread.NORM_PRIORITY).waitFinished ();
                if (error == null) {
                    switchToReal();
                    // make sure support for projects we depend on are also enabled
                    SubprojectProvider sp = getLookup().lookup(SubprojectProvider.class);
                    if (sp != null) {
                        for (Project subP : sp.getSubprojects()) {
                            FeatureNonProject toOpen;
                            toOpen = subP.getLookup().lookup(FeatureNonProject.class);
                            if (toOpen != null) {
                                toOpen.delegate.hook.projectOpened();
                            }
                        }
                    }
                } else {
                    delegate.associate(new BrokenProject(getProjectDirectory(), error));
                }
            }

            @Override
            protected void projectClosed() {
            }

            public void run() {
                FeatureManager.logUI("ERGO_PROJECT_OPEN", info.clusterName);
                error = null;
                FindComponentModules findModules = new FindComponentModules(info, additional);
                Collection<UpdateElement> toInstall = findModules.getModulesForInstall ();
                Collection<UpdateElement> toEnable = findModules.getModulesForEnable ();
                if (toInstall != null && ! toInstall.isEmpty ()) {
                    ModulesInstaller installer = new ModulesInstaller(toInstall, findModules, this);
                    installer.getInstallTask ().waitFinished ();
                } else if (toEnable != null && ! toEnable.isEmpty ()) {
                    ModulesActivator enabler = new ModulesActivator (toEnable, findModules, this);
                    enabler.getEnableTask ().waitFinished ();
                }
            }

            public void onDownload(ProgressHandle progressHandle) {
            }

            public void onValidate(ProgressHandle progressHandle) {
            }

            public void onInstall(ProgressHandle progressHandle) {
            }

            public void onEnable(ProgressHandle progressHandle) {
            }

            public void onError(String message) {
                error = message;
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
        private final FeatureNonProject.FeatureOpenHook hook;


        public FeatureDelegate(FileObject dir, FeatureNonProject feature) {
            this.dir = dir;
            this.hook = feature.new FeatureOpenHook();
            ic.add(UILookupMergerSupport.createProjectOpenHookMerger(hook));
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
            return ImageUtilities.loadImageIcon(
                "org/netbeans/modules/ide/ergonomics/fod/project.png" // NOI18N
            , false);
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
            if (p == null) {
                delegate = Lookup.EMPTY;
                return;
            }
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
