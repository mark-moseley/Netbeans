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

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.spi.project.ProjectFactory;
import org.netbeans.spi.project.support.ant.AntBasedProjectType;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.MultiFileSystem;
import org.openide.filesystems.XMLFileSystem;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ServiceProvider;
import org.xml.sax.SAXException;

/**
 *
 * @author Jirka Rechtacek
 */
@ServiceProvider(service=FileSystem.class)
public class FoDFileSystem extends MultiFileSystem 
implements Runnable, ChangeListener, LookupListener {
    private static FoDFileSystem INSTANCE;
    final static Logger LOG = Logger.getLogger (FoDFileSystem.class.getPackage().getName());
    private static RequestProcessor RP = new RequestProcessor("Ergonomics"); // NOI18N
    private RequestProcessor.Task refresh = RP.create(this, true);
    private Lookup.Result<ProjectFactory> factories;
    private Lookup.Result<?> ants;
    private boolean forcedRefresh;
    private boolean warmUp;

    public FoDFileSystem() {
        assert INSTANCE == null;
        INSTANCE = this;
        setPropagateMasks(true);
        FeatureManager.getInstance().addChangeListener(this);
        refresh();
    }

    public static synchronized FoDFileSystem getInstance() {
        if (INSTANCE == null) {
            while (INSTANCE == null) {
                INSTANCE = Lookup.getDefault().lookup(FoDFileSystem.class);
            }
        }
        return INSTANCE;
    }

    public void refresh() {
        refresh.schedule(0);
        refresh.waitFinished();
    }
    public void refreshForce() {
        forcedRefresh = true;
        refresh.schedule(0);
        refresh.waitFinished();
    }

    public void waitFinished() {
        refresh.waitFinished();
    }

    private FileSystem def;
    private FileSystem getDefaultLayer() {
        if (def == null) {
            try {
                if (FeatureInfo.doParseXML()) {
                    def = new XMLFileSystem(FoDFileSystem.class.getResource("default.xml"));
                    return def;
                }
                def = FileUtil.createMemoryFileSystem();
            } catch (SAXException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return def;
    }
    
    public void run() {
        if (RP.isRequestProcessorThread()) {
            refreshInRP();
        } else {
            LOG.fine("Warmup starting..."); // NOI18N
            for (Runnable r : Lookups.forPath("WarmUp").lookupAll(Runnable.class)) {
                r.run();
            }
            LOG.fine("Warmup done."); // NOI18N
        }
    }

    private void refreshInRP() {
        boolean empty = true;

        LOG.fine("collecting layers"); // NOI18N
        List<FileSystem> delegate = new ArrayList<FileSystem>();
        for (FeatureInfo info : FeatureManager.features()) {
            if (!info.isPresent()) {
                continue;
            }
            if (!info.isEnabled()) {
                LOG.finest("adding feature " + info.clusterName); // NOI18N
                delegate.add(info.getXMLFileSystem());
            } else {
                empty = false;
            }
        }
        if (empty && noAdditionalProjects()) {
            LOG.fine("adding default layer"); // NOI18N
            delegate.add(0, getDefaultLayer());
        }
        if (forcedRefresh) {
            forcedRefresh = false;
            LOG.log(Level.INFO, "Forced refresh. Setting delegates to empty"); // NOI18N
            setDelegates();
            LOG.log(Level.INFO, "New delegates count: {0}", delegate.size()); // NOI18N
            LOG.log(Level.INFO, "{0}", delegate); // NOI18N
        }
        LOG.log(Level.FINE, "delegating to {0} layers", delegate.size()); // NOI18N
        LOG.log(Level.FINEST, "{0}", delegate); // NOI18N
        setDelegates(delegate.toArray(new FileSystem[0]));
        LOG.fine("done");
        FeatureManager.dumpModules();
        if (warmUp) {
            warmUp = false;
            RequestProcessor.getDefault().post(this);
        }
    }

    public FeatureInfo whichProvides(FileObject template) {
        String path = template.getPath();
        for (FeatureInfo info : FeatureManager.features()) {
            FileSystem fs = info.getXMLFileSystem();
            if (fs.findResource(path) != null) {
                return info;
            }
        }
        return null;
    }
    
    public URL getDelegateFileSystem(FileObject template) {
        String path = template.getPath();
        for (FeatureInfo info : FeatureManager.features()) {
            FileSystem fs = info.getXMLFileSystem();
            if (fs.findResource(path) != null) {
                return info.getLayerURL();
            }
        }
        return null;
    }

    public void stateChanged(ChangeEvent e) {
        warmUp = true;
        refresh.schedule(500);
    }

    public void resultChanged(LookupEvent ev) {
        warmUp = true;
        refresh.schedule(500);
    }

    private boolean noAdditionalProjects() {
        if (factories == null) {
            factories = Lookup.getDefault().lookupResult(ProjectFactory.class);
            factories.addLookupListener(this);
            
            ants = Lookup.getDefault().lookupResult(AntBasedProjectType.class);
            ants.addLookupListener(this);
        }

        for (ProjectFactory pf : factories.allInstances()) {
            if (pf.getClass().getName().contains("AntBasedProjectFactorySingleton")) { // NOI18N
                continue;
            }
            if (pf.getClass().getName().startsWith("org.netbeans.modules.ide.ergonomics")) { // NOI18N
                continue;
            }
            return false;
        }
        return ants.allItems().isEmpty();
    }
}
