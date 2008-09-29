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

package org.netbeans.modules.j2ee.deployment.impl;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.j2ee.deployment.plugins.api.*;
import org.netbeans.modules.j2ee.deployment.common.api.*;
import org.netbeans.modules.j2ee.deployment.devmodules.api.*;
import javax.enterprise.deploy.spi.*;
import javax.enterprise.deploy.shared.*;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.ArtifactListener.Artifact;
import org.openide.util.NbBundle;
import org.netbeans.modules.j2ee.deployment.execution.DeploymentTarget;
import org.netbeans.modules.j2ee.deployment.plugins.spi.IncrementalDeployment;

/**
 *
 * @author  nn136682
 */
public class ServerFileDistributor extends ServerProgress {
    private final ServerInstance instance;
    private final DeploymentTarget dtarget;
    private final IncrementalDeployment incremental;

    // valued by RootedEntry's
    private Iterator rootModuleFiles;
    // keyed by child module URL, valued by collection of RootedEntry's
    private Map childModuleFiles;
    // keyed by child module URL, valued by collection of J2eeModule's
    private Map childModuleMap;

    private static final Logger LOGGER  = Logger.getLogger(ServerFileDistributor.class.getName());

    /** Creates a new instance of ServerFileDistributor */
    public ServerFileDistributor(ServerInstance instance, DeploymentTarget dtarget){
        super(instance);
        this.instance = instance;
        this.dtarget = dtarget;
        incremental = instance.getIncrementalDeployment ();

        //init contents
        try {
            J2eeModule source = dtarget.getModule();
            rootModuleFiles = source.getArchiveContents();
            if (source instanceof J2eeApplication) {
                childModuleFiles = new HashMap();
                childModuleMap = new HashMap();
                J2eeModule[] childModules = ((J2eeApplication)source).getModules();
                for (int i=0; i<childModules.length; i++) {
                    Iterator contents = childModules[i].getArchiveContents();
                    if (contents != null)
                        childModuleFiles.put(childModules[i].getUrl(), contents);
                    childModuleMap.put(childModules[i].getUrl(), childModules[i]);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    static Map j2eeTypeMap = null;
    static synchronized List getDescriptorPath(J2eeModule module) {
        if (j2eeTypeMap == null) {
            j2eeTypeMap = new HashMap();
            j2eeTypeMap.put(J2eeModule.EJB, Arrays.asList(new String[]{J2eeModule.EJBJAR_XML, J2eeModule.EJBSERVICES_XML}));
            j2eeTypeMap.put(J2eeModule.WAR, Arrays.asList(new String[]{J2eeModule.WEB_XML, J2eeModule.WEBSERVICES_XML}));
            j2eeTypeMap.put(J2eeModule.CLIENT, Arrays.asList(new String[]{J2eeModule.CLIENT_XML}));
            j2eeTypeMap.put(J2eeModule.CONN, Arrays.asList(new String[]{J2eeModule.CONNECTOR_XML}));
            j2eeTypeMap.put(J2eeModule.EAR, Arrays.asList(new String[]{J2eeModule.APP_XML}));
        }
        return (List) j2eeTypeMap.get(module.getModuleType());
    }

    private J2eeModule getJ2eeModule(TargetModuleID target) {
        if (target.getParentTargetModuleID() == null)
            return dtarget.getModule();
        else {
            String moduleUrl = incremental.getModuleUrl(target);
            return (J2eeModule) childModuleMap.get(moduleUrl);
        }
    }

    private AppChanges createModuleChangeDescriptor(TargetModuleID target) {
        J2eeModule module = getJ2eeModule(target);
        List descriptorRelativePaths = getDescriptorPath(module);

        ModuleType moduleType = (ModuleType) module.getModuleType ();
        List serverDescriptorRelativePaths = Arrays.asList(instance.getServer().getDeploymentPlanFiles(moduleType));
        return new AppChanges(descriptorRelativePaths, serverDescriptorRelativePaths, moduleType);
    }

    public AppChangeDescriptor distribute(TargetModule targetModule, ModuleChangeReporter mcr) throws IOException {
        long lastDeployTime = targetModule.getTimestamp();
        TargetModuleID[] childModules = targetModule.getChildTargetModuleID();
        AppChanges changes = new AppChanges();
        File destDir = null;

            //PENDING: whether module need to be stop first
            for (int i=0; childModules != null && i<childModules.length; i++) {
                // need to get the ModuleUrl for the child, not the root app... DUH
                String url = incremental.getModuleUrl(childModules[i]);
                destDir = incremental.getDirectoryForModule(childModules[i]);
                Iterator source = (Iterator) childModuleFiles.get(url);
                if (destDir == null)
                    changes.record(_distribute(childModules[i], lastDeployTime));
                else if (null != source)
                    // original code assumed 1-to-1 correspondence between
                    //   J2eeModule objects and TargetModuleID objects that are
                    //   are children of a deployed EAR...
                    // That assumption is not valid
                    changes.record(_distribute(source, destDir, childModules[i], lastDeployTime));
            }

            //PENDING: whether ordering of copying matters
            destDir = incremental.getDirectoryForModule(targetModule.delegate());
            if (destDir == null)
                changes.record(_distribute(targetModule.delegate(), lastDeployTime));
            else
                changes.record(_distribute(rootModuleFiles, destDir, targetModule.delegate(), lastDeployTime));

            if (mcr != null)
                changes.record(mcr, lastDeployTime);

            setStatusDistributeCompleted(NbBundle.getMessage(
                ServerFileDistributor.class, "MSG_DoneIncrementalDeploy", targetModule.getModuleID()));

        return changes;
    }

    public DeploymentChangeDescriptor distributeOnSave(TargetModule targetModule, ModuleChangeReporter mcr,
            Iterable<Artifact> artifacts) throws IOException {

        long lastDeployTime = targetModule.getTimestamp();
        TargetModuleID[] childModules = targetModule.getChildTargetModuleID();
        AppChanges changes = new AppChanges();
        File destDir = null;

        //PENDING: whether module need to be stop first
        for (int i = 0; childModules != null && i < childModules.length; i++) {
            // need to get the ModuleUrl for the child, not the root app... DUH
            String url = incremental.getModuleUrl(childModules[i]);
            destDir = incremental.getDirectoryForModule(childModules[i]);

            if (destDir == null) {
                changes.record(_distributeOnSave(childModules[i], artifacts));
            } else {
                Iterator source = (Iterator) childModuleFiles.get(url);
                if (source != null) {
                    // original code assumed 1-to-1 correspondence between
                    //   J2eeModule objects and TargetModuleID objects that are
                    //   are children of a deployed EAR...
                    // That assumption is not valid
                    changes.record(_distributeOnSave(destDir, childModules[i], artifacts));
                }
            }
        }

        //PENDING: whether ordering of copying matters
        destDir = incremental.getDirectoryForModule(targetModule.delegate());
        if (destDir == null) {
            changes.record(_distributeOnSave(targetModule.delegate(), artifacts));
        } else {
            changes.record(_distributeOnSave(destDir, targetModule.delegate(), artifacts));
        }

        if (mcr != null) {
            changes.record(mcr, lastDeployTime);
        }

        setStatusDistributeCompleted(NbBundle.getMessage(
            ServerFileDistributor.class, "MSG_DoneIncrementalDeploy", targetModule.getModuleID()));

        return Accessor.getDefault().newDescriptor(changes);
    }

    public AppChanges _test_distribute(Iterator source, File destDir, TargetModuleID target, long lastDeployTime) throws IOException {
        return _distribute(source, destDir, target, lastDeployTime);
    }

    private AppChanges _distribute(TargetModuleID target,  long lastDeployTime) throws IOException {
        AppChanges mc = createModuleChangeDescriptor(target);
        setStatusDistributeRunning(NbBundle.getMessage(
        ServerFileDistributor.class, "MSG_RunningIncrementalDeploy", target));

        Iterator content = getJ2eeModule(target).getArchiveContents ();

        Date lastDeployed = new Date(lastDeployTime);
        while (content.hasNext ()) {
            J2eeModule.RootedEntry re = (J2eeModule.RootedEntry) content.next ();
            FileObject file = re.getFileObject ();
            if (file.isFolder())
                continue;
            //jar file are created externally and timestamp may not be refreshed
            file.refresh ();
            if (file.lastModified().after(lastDeployed)) {
                String relativePath = re.getRelativePath ();
                // FIXME destdir
                mc.record(relativePath);
            }
        }

        return mc;
    }

    // files are already there
    private AppChanges _distributeOnSave(TargetModuleID target, Iterable<Artifact> artifacts) throws IOException {
        AppChanges mc = createModuleChangeDescriptor(target);
        setStatusDistributeRunning(NbBundle.getMessage(
            ServerFileDistributor.class, "MSG_RunningIncrementalDeploy", target));

        FileObject contentDirectory = getJ2eeModule(target).getContentDirectory();
        assert contentDirectory != null;
        File destDir = FileUtil.toFile(contentDirectory);
        assert destDir != null;

        for (Artifact artifact : artifacts) {
            File fsFile = artifact.getFile();
            FileObject file = FileUtil.toFileObject(FileUtil.normalizeFile(fsFile));
            if (file != null && !file.isFolder()) {
                String relative = FileUtil.getRelativePath(contentDirectory, file);
                if (relative != null) {
                    mc.record(destDir, relative);
                }
            }
        }

        return mc;
    }

    private AppChanges _distribute(Iterator source, File destDir, TargetModuleID target, long lastDeployTime) throws IOException {
        AppChanges mc = createModuleChangeDescriptor(target);
        if (source == null) {
            Logger.getLogger("global").log(Level.SEVERE, "There is no contents for " + target); //NOI18N
            throw new IOException(NbBundle.getMessage(ServerFileDistributor.class, "MSG_NoContents", target));
        }
        setStatusDistributeRunning(NbBundle.getMessage(ServerFileDistributor.class, "MSG_RunningIncrementalDeploy", target));
        try {
            //get relative-path-key map from FDL
            File dir = incremental.getDirectoryForModule(target);
            // mkdirs()/toFileObject is not tolerated any more
            FileObject destRoot = FileUtil.createFolder(destDir);

            // create target FOs map keyed by relative paths
            java.util.Enumeration destFiles = destRoot.getChildren(true);
            Map destMap = new HashMap();
            int rootPathLen = destRoot.getPath().length();
            for (; destFiles.hasMoreElements(); ) {
                FileObject destFO = (FileObject) destFiles.nextElement();
                destMap.put(destFO.getPath().substring(rootPathLen + 1), destFO);
            }

            // iterate through source changes
            for (Iterator j=source; j.hasNext();) {
                J2eeModule.RootedEntry entry = (J2eeModule.RootedEntry) j.next();
                String relativePath = entry.getRelativePath();
                FileObject sourceFO = entry.getFileObject();
                FileObject targetFO = (FileObject) destMap.get(relativePath);
                if (sourceFO.isFolder()) {
                    destMap.remove(relativePath);
                    continue;
                }
                // refactor to make the finally easier to write and read in the
                // future.
                createOrReplace(sourceFO,targetFO,destRoot,relativePath,mc,destMap, true, lastDeployTime);
            }

            ModuleType moduleType = (ModuleType) dtarget.getModule ().getModuleType ();
            String[] rPaths = instance.getServer().getDeploymentPlanFiles(moduleType);

            // copying serverconfiguration files if changed
            File configFile = dtarget.getConfigurationFile();
            if (rPaths == null || rPaths.length == 0)
                return mc;

            File[] paths = new File[rPaths.length];
            for (int n=0; n<rPaths.length; n++) {
                paths[n] = new File(FileUtil.toFile(destRoot), rPaths[n]);
                if (paths[n].exists() && paths[n].lastModified() > configFile.lastModified())
                    // FIXME destdir
                    mc.record(rPaths[n]);
            }

            return mc;

        } catch (Exception e) {
            String msg = NbBundle.getMessage(ServerFileDistributor.class, "MSG_IncrementalDeployFailed", e);
            setStatusDistributeFailed(msg);
            throw new RuntimeException(msg,e);
        }
    }

    private AppChanges _distributeOnSave(File destDir, TargetModuleID target, Iterable<Artifact> artifacts) throws IOException {
        AppChanges mc = createModuleChangeDescriptor(target);

        setStatusDistributeRunning(NbBundle.getMessage(ServerFileDistributor.class, "MSG_RunningIncrementalDeploy", target));
        try {
            // mkdirs()/toFileObject is not tolerated any more
            FileObject destRoot = FileUtil.createFolder(destDir);

            // create target FOs map keyed by relative paths
            java.util.Enumeration destFiles = destRoot.getChildren(true);
            Map destMap = new HashMap();
            int rootPathLen = destRoot.getPath().length();
            for (; destFiles.hasMoreElements(); ) {
                FileObject destFO = (FileObject) destFiles.nextElement();
                destMap.put(destFO.getPath().substring(rootPathLen + 1), destFO);
            }

            FileObject contentDirectory = getJ2eeModule(target).getContentDirectory();
            assert contentDirectory != null;

            for (Artifact artifact : artifacts) {
                File fsFile = artifact.getFile();
                File altDistPath = artifact.getDistributionPath();

                FileObject file = FileUtil.toFileObject(FileUtil.normalizeFile(fsFile));

                FileObject checkFile = null;
                if (altDistPath != null) {
                    checkFile = FileUtil.toFileObject(FileUtil.normalizeFile(altDistPath));
                } else {
                    checkFile = file;
                }

                if (checkFile != null && file != null) {
                    String relative = FileUtil.getRelativePath(contentDirectory, checkFile);
                    if (relative != null) {
                        FileObject targetFO = (FileObject) destMap.get(relative);
                        if (file.isFolder()) {
                            destMap.remove(relative);
                            continue;
                        }

                        // FIXME timestamp
                        createOrReplace(file, targetFO, destRoot, relative, mc, destMap, false, 0);
                    }
                }
            }

            ModuleType moduleType = (ModuleType) dtarget.getModule ().getModuleType ();
            String[] rPaths = instance.getServer().getDeploymentPlanFiles(moduleType);

            // copying serverconfiguration files if changed
            File configFile = dtarget.getConfigurationFile();
            if (rPaths == null || rPaths.length == 0)
                return mc;

            File[] paths = new File[rPaths.length];
            for (int n=0; n<rPaths.length; n++) {
                File dest = FileUtil.toFile(destRoot);
                assert dest != null;
                paths[n] = new File(dest, rPaths[n]);
                if (paths[n].exists() && paths[n].lastModified() > configFile.lastModified())
                    mc.record(dest, rPaths[n]);
            }

            return mc;

        } catch (Exception e) {
            String msg = NbBundle.getMessage(ServerFileDistributor.class, "MSG_IncrementalDeployFailed", e);
            setStatusDistributeFailed(msg);
            throw new RuntimeException(msg,e);
        }
    }

    private static void createOrReplace(FileObject sourceFO, FileObject targetFO,
            FileObject destRoot, String relativePath, AppChanges mc, Map destMap, boolean checkTimeStamps,
            long lastDeployTime) throws IOException {

        FileObject destFolder;
        OutputStream destStream = null;
        InputStream sourceStream = null;
        File dest = FileUtil.toFile(destRoot);

        Date ldDate = new Date(lastDeployTime);
        try {
            // double check that the target does not exist... 107526
            //   the destMap seems to be incomplete....
            if (null == targetFO) {
                targetFO = destRoot.getFileObject(relativePath);
            }
            if (targetFO == null) {
                destFolder = findOrCreateParentFolder(destRoot, relativePath);
            } else {
                // remove from map to form of to-remove-target-list
                destMap.remove(relativePath);

                // for web app changes... since the 'copy' was already done by
                // the build target.
                if (targetFO.equals(sourceFO) && targetFO.lastModified().after(ldDate)) {
                    mc.record(dest, relativePath);
                }

                //check timestamp
                if (checkTimeStamps) {
                    if (!sourceFO.lastModified().after(targetFO.lastModified())) {
                        return;
                    }
                }
                if (targetFO.equals(sourceFO)) {
                    // do not write a file onto itself...
                    mc.record(dest, relativePath);
                    return;
                }
                destFolder = targetFO.getParent();

                // we need to rewrite the content of the file here... thanks,
                //   to windows file locking.
                destStream = targetFO.getOutputStream();

            }
            mc.record(dest, relativePath);

            if (null == destStream) {
                FileUtil.copyFile(sourceFO, destFolder, sourceFO.getName());
            } else {
                // this is where we need to push the content into the file....
                sourceStream = sourceFO.getInputStream();
                FileUtil.copy(sourceStream, destStream);
            }
        } finally {
            if (null != sourceStream) {
                try {
                    sourceStream.close();
                } catch (IOException ioe) {
                    LOGGER.log(Level.WARNING, null, ioe);
                }
            }
            if (null != destStream) {
                try {
                    destStream.close();
                } catch (IOException ioe) {
                    LOGGER.log(Level.WARNING, null, ioe);
                }
            }
        }
    }


    /**
     * Find or create parent folder of a file given its root and its relative path.
     * The target file does not need to exist.
     *
     * @param dest FileObject for the root of the target file
     * @param relativePath relative path of the target file
     * @return the FileObject for the parent folder target file.
     * @throws java.io.IOException
     */
    public static FileObject findOrCreateParentFolder(FileObject dest, String relativePath) throws IOException {
        File parentRelativePath = (new File(relativePath)).getParentFile();
        if (parentRelativePath == null)
            return dest;

        FileObject folder = FileUtil.createFolder(dest, parentRelativePath.getPath());
        if (folder.isData()) {
            Logger.getLogger(ServerFileDistributor.class.getName()).finer("found file "+
                    folder.getPath()+"when a folder was expecetd");
            folder = null;
        }

        return folder;
    }

    //ServerProgress methods
    private void setStatusDistributeRunning(String message) {
        notify(createRunningProgressEvent(CommandType.DISTRIBUTE, message));
    }
    private void setStatusDistributeFailed(String message) {
        notify(createFailedProgressEvent(CommandType.DISTRIBUTE, message));
    }
    private void setStatusDistributeCompleted(String message) {
        notify(createCompletedProgressEvent(CommandType.DISTRIBUTE, message));
    }


    public static final class AppChanges implements AppChangeDescriptor {

        private boolean descriptorChanged = false;
        private boolean serverDescriptorChanged = false;
        private boolean classesChanged = false;
        private boolean manifestChanged = false;
        private boolean ejbsChanged = false;
        private List changedEjbs = Collections.EMPTY_LIST;
        private ModuleType moduleType = null;
        private List changedFiles = new ArrayList();
        private List descriptorRelativePaths;
        private List serverDescriptorRelativePaths;

        AppChanges() {
            super();
        }

        AppChanges(List descriptorRelativePaths, List serverDescriptorRelativePaths, ModuleType moduleType) {
            this.descriptorRelativePaths = descriptorRelativePaths;
            this.serverDescriptorRelativePaths = serverDescriptorRelativePaths;
            this.moduleType = moduleType;
        }
        private void record(AppChanges changes) {
            if (!descriptorChanged) {
                descriptorChanged = changes.descriptorChanged();
            }
            if (!serverDescriptorChanged) {
                serverDescriptorChanged = changes.serverDescriptorChanged();
            }
            if (!classesChanged) {
                classesChanged = changes.classesChanged();
            }
            if (!manifestChanged) {
                manifestChanged = changes.manifestChanged();
            }
            if (!ejbsChanged) {
                ejbsChanged = changes.ejbsChanged();
            }
            List ejbs = Arrays.asList(changes.getChangedEjbs());
            if (ejbs.size() > 0) {
                changedEjbs.addAll(ejbs);
            }
            changedFiles.addAll(changes.changedFiles);
        }

        /**
         *
         * @param relativePath
         * @deprecated use {@link #record(java.io.File, java.lang.String)}
         */
        private void record(String relativePath) {
            record(null, relativePath);
        }

        private void record(File destDir, String relativePath) {
            if (destDir != null) {
                changedFiles.add(new File(destDir, relativePath));
            } else {
                changedFiles.add(new File(relativePath));
            }

            if (!classesChanged) {
                boolean classes = (!moduleType.equals(ModuleType.WAR)
                        && !relativePath.startsWith("META-INF")) // NOI18N
                            || relativePath.startsWith("WEB-INF/classes/"); // NOI18N

                if (moduleType.equals(ModuleType.EAR)) {
                    classes = false;
                }

                boolean importantLib = !moduleType.equals(ModuleType.WAR)
                        || relativePath.startsWith("WEB-INF/lib/"); // NOI18N
                boolean libs = importantLib
                        && (relativePath.endsWith(".jar") // NOI18N
                        || relativePath.endsWith(".zip")); // NOI18N
                if (classes || libs) {
                    classesChanged = true;
                    return;
                }
            }
            if (!descriptorChanged
                    && (((descriptorRelativePaths != null
                            && descriptorRelativePaths.contains(relativePath))
                                || (relativePath.startsWith("WEB-INF") // NOI18N
                                    && (relativePath.endsWith(".tld") // NOI18N
                                        || relativePath.endsWith(".xml") // NOI18N
                                        || relativePath.endsWith(".dtd")))))) { // NOI18N

                descriptorChanged = true;
                return;
            }
            if (!serverDescriptorChanged
                    && serverDescriptorRelativePaths != null
                    && serverDescriptorRelativePaths.contains(relativePath)) {

                serverDescriptorChanged = true;
                return;
            }
            if (!manifestChanged && relativePath.equals("META-INF/MANIFEST.MF")) { // NOI18N
                manifestChanged = true;
                return;
            }
        }

        private void record(ModuleChangeReporter mcr, long since) {
            EjbChangeDescriptor ecd = mcr.getEjbChanges(since);
            ejbsChanged = ecd.ejbsChanged();
            String[] ejbs = ecd.getChangedEjbs();
            if (ejbs != null && ejbs.length > 0) {
                changedEjbs.addAll(Arrays.asList(ejbs));
            }
            if (!manifestChanged) {
                manifestChanged = mcr.isManifestChanged(since);
            }
        }

        public boolean classesChanged() {
            return classesChanged;
        }

        public boolean descriptorChanged() {
            return descriptorChanged;
        }

        public boolean manifestChanged() {
            return manifestChanged;
        }

        public boolean serverDescriptorChanged() {
            return serverDescriptorChanged;
        }

        public boolean ejbsChanged() {
            return ejbsChanged;
        }

        public String[] getChangedEjbs() {
            return (String[]) changedEjbs.toArray(new String[]{});
        }

        public File[] getChangedFiles() {
            return (File[]) changedFiles.toArray(new File[changedFiles.size()]);
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append(super.toString());
            builder.append(" ["); // NOI18N
            for (File file : getChangedFiles()) {
                builder.append(file.getAbsolutePath()).append(", ");
            }
            if (getChangedFiles().length > 0) {
                builder.setLength(builder.length() - 2);
            }
            builder.append("], "); // NOI18N

            builder.append("classesChanged=").append(classesChanged()); // NOI18N
            builder.append(", ");
            builder.append("descriptorChanged=").append(this.descriptorChanged()); // NOI18N
            builder.append(", ");
            builder.append("ejbsChanged=").append(this.ejbsChanged()); // NOI18N
            builder.append(", ");
            builder.append("manifestChanged=").append(this.manifestChanged()); // NOI18N
            builder.append(", ");
            builder.append("serverDescriptorChanged=").append(this.serverDescriptorChanged()); // NOI18N

            return builder.toString();
        }
    }

    public static abstract class Accessor {

        private static volatile Accessor accessor;

        public static void setDefault(Accessor accessor) {
            if (Accessor.accessor != null) {
                throw new IllegalStateException("Already initialized accessor");
            }
            Accessor.accessor = accessor;
        }

        public static Accessor getDefault() {
            if (accessor != null) {
                return accessor;
            }

            // invokes static initializer of DeploymentChangeDescriptor.class
            // that will assign value to the DEFAULT field above
            Class c = DeploymentChangeDescriptor.class;
            try {
                Class.forName(c.getName(), true, c.getClassLoader());
            } catch (ClassNotFoundException ex) {
                assert false : ex;
            }
            assert accessor != null : "The accessor field must be initialized";
            return accessor;
        }

        /** Accessor to constructor */
        public abstract DeploymentChangeDescriptor newDescriptor(AppChanges desc);

    }
}
