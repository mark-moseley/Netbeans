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
package org.netbeans.modules.j2ee.jboss4;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.MissingResourceException;
import javax.management.ObjectName;
import org.netbeans.modules.j2ee.dd.api.application.Application;
import org.netbeans.modules.j2ee.dd.api.application.DDProvider;
import org.netbeans.modules.j2ee.dd.api.application.Module;
import org.netbeans.modules.j2ee.jboss4.config.gen.JbossWeb;
import org.netbeans.modules.j2ee.jboss4.ide.ui.JBPluginProperties;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.status.ProgressEvent;
import javax.enterprise.deploy.spi.status.ProgressListener;
import javax.enterprise.deploy.spi.status.ProgressObject;
import javax.enterprise.deploy.spi.exceptions.OperationUnsupportedException;
import javax.enterprise.deploy.spi.status.ClientConfiguration;
import javax.enterprise.deploy.spi.status.DeploymentStatus;
import org.netbeans.modules.j2ee.jboss4.nodes.Util;
import org.openide.filesystems.JarFileSystem;
import org.openide.util.RequestProcessor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.netbeans.modules.j2ee.jboss4.ide.JBDeploymentStatus;
import javax.enterprise.deploy.shared.ActionType;
import javax.enterprise.deploy.shared.CommandType;
import javax.enterprise.deploy.shared.StateType;
import javax.management.MBeanServerConnection;

import org.openide.util.NbBundle;

/**
 *
 * @author Ivan Sidorkin
 */
public class JBDeployer implements ProgressObject, Runnable {
    /** timeout for waiting for URL connection */
    private static final int TIMEOUT = 60000;

    private static final int POLLING_INTERVAL = 1000;

    private static final Logger LOGGER = Logger.getLogger(JBDeployer.class.getName());

    private final JBDeploymentManager dm;

    private File file;
    private String uri;
    private JBTargetModuleID mainModuleID;

    /** Creates a new instance of JBDeployer */
    public JBDeployer(String serverUri, JBDeploymentManager dm) {
        uri = serverUri;
        this.dm = dm;
    }


    public ProgressObject deploy(Target[] target, File file, File file2, String host, int port) {
        //PENDING: distribute to all targets!
        mainModuleID = new JBTargetModuleID(target[0], file.getName());

        try {
            String server_url = "http://" + host + ":" + port; // NOI18N

            if (file.getName().endsWith(".war")) {
                mainModuleID.setContextURL(server_url + JbossWeb.createGraph(file2).getContextRoot());
            } else if (file.getName().endsWith(".ear")) {
                JarFileSystem jfs = new JarFileSystem();
                jfs.setJarFile(file);
                FileObject appXml = jfs.getRoot().getFileObject("META-INF/application.xml");
                if (appXml != null) {
                    Application ear = DDProvider.getDefault().getDDRoot(appXml);
                    Module[] modules = ear.getModule();
                    for (int i = 0; i < modules.length; i++) {
                        JBTargetModuleID mod_id = new JBTargetModuleID(target[0]);
                        if (modules[i].getWeb() != null) {
                            mod_id.setContextURL(server_url + modules[i].getWeb().getContextRoot());
                        }
                        mainModuleID.addChild(mod_id);
                    }
                } else {
                    // Java EE 5
                    for (FileObject child : jfs.getRoot().getChildren()) {
                        if (child.hasExt("war") || child.hasExt("jar")) { // NOI18N
                            JBTargetModuleID mod_id = new JBTargetModuleID(target[0]);

                            if (child.hasExt("war")) { // NOI18N
                                String contextRoot = child.getName();
                                ZipInputStream zis = new ZipInputStream(child.getInputStream());
                                try {

                                    ZipEntry entry = null;
                                    while ((entry = zis.getNextEntry()) != null) {
                                        if ("WEB-INF/jboss-web.xml".equals(entry.getName())) { // NOI18N
                                            String ddContextRoot =
                                                    JbossWeb.createGraph(new ZipEntryInputStream(zis)).getContextRoot();
                                            if (ddContextRoot != null) {
                                                contextRoot = ddContextRoot;
                                            }
                                            break;
                                        }
                                    }
                                } catch (IOException ex) {
                                    LOGGER.log(Level.INFO, "Error reading context-root", ex); // NOI18N
                                } finally {
                                    zis.close();
                                }

                                mod_id.setContextURL(server_url + contextRoot);
                            }
                            mainModuleID.addChild(mod_id);
                        }
                    }
                }
            }

        } catch(Exception e) {
            LOGGER.log(Level.INFO, null, e);
        }

        this.file = file;
        fireHandleProgressEvent(null, new JBDeploymentStatus(ActionType.EXECUTE, CommandType.DISTRIBUTE, StateType.RUNNING, NbBundle.getMessage(JBDeployer.class, "MSG_DEPLOYING", file.getAbsolutePath())));
        RequestProcessor.getDefault().post(this, 0, Thread.NORM_PRIORITY);
        return this;
    }

    public ProgressObject redeploy (TargetModuleID module_id[], File file, File file2) {
        //PENDING: distribute all modules!
        this.file = file;
        this.mainModuleID = (JBTargetModuleID) module_id[0];
        fireHandleProgressEvent(null, new JBDeploymentStatus(ActionType.EXECUTE, CommandType.DISTRIBUTE, StateType.RUNNING, NbBundle.getMessage(JBDeployer.class, "MSG_DEPLOYING", file.getAbsolutePath())));
        RequestProcessor.getDefault().post(this, 0, Thread.NORM_PRIORITY);
        return this;
    }

    public void run() {

        String deployDir = InstanceProperties.getInstanceProperties(uri).getProperty(JBPluginProperties.PROPERTY_DEPLOY_DIR);
        FileObject foIn = FileUtil.toFileObject(file);
        FileObject foDestDir = FileUtil.toFileObject(new File(deployDir));
        String fileName = file.getName();

        File toDeploy = new File(deployDir + File.separator + fileName);
        if (toDeploy.exists()) {
            toDeploy.delete();
        }

        fileName = fileName.substring(0, fileName.lastIndexOf('.'));
        String msg = NbBundle.getMessage(JBDeployer.class, "MSG_DEPLOYING", file.getAbsolutePath());
        fireHandleProgressEvent(null, new JBDeploymentStatus(ActionType.EXECUTE, CommandType.DISTRIBUTE, StateType.RUNNING, msg));


        try {
            Long previousDeployTime = getDeploymentTime(toDeploy);

            FileUtil.copyFile(foIn, foDestDir, fileName); // copy version
            TargetModuleID moduleID = mainModuleID;
            String webUrl = mainModuleID.getWebURL();
            if (webUrl == null) {
                TargetModuleID[] ch = mainModuleID.getChildTargetModuleID();
                if (ch != null) {
                    for (int i = 0; i < ch.length; i++) {
                        webUrl = ch [i].getWebURL();
                        if (webUrl != null) {
                            moduleID = ch[i];
                            break;
                        }
                    }
                }

            }
            if (webUrl != null) {
                URL url = new URL(webUrl);
                String waitingMsg = NbBundle.getMessage(JBDeployer.class, "MSG_Waiting_For_Url", url);
                fireHandleProgressEvent(null, new JBDeploymentStatus(ActionType.EXECUTE, CommandType.DISTRIBUTE, StateType.RUNNING, waitingMsg));

                //wait until the url becomes active
                boolean ready = waitForUrlReady(moduleID, toDeploy, previousDeployTime, TIMEOUT);
                if (!ready) {
                    LOGGER.log(Level.INFO, "URL wait timeouted after " + TIMEOUT); // NOI18N
                }
            }
        } catch (MalformedURLException ex) {
            LOGGER.log(Level.INFO, null, ex);
            fireHandleProgressEvent(null, new JBDeploymentStatus(ActionType.EXECUTE, CommandType.DISTRIBUTE, StateType.FAILED, "Failed"));
        } catch (MissingResourceException ex) {
            LOGGER.log(Level.INFO, null, ex);
            fireHandleProgressEvent(null, new JBDeploymentStatus(ActionType.EXECUTE, CommandType.DISTRIBUTE, StateType.FAILED, "Failed"));
        } catch (IOException ex) {
            LOGGER.log(Level.INFO, null, ex);
            fireHandleProgressEvent(null, new JBDeploymentStatus(ActionType.EXECUTE, CommandType.DISTRIBUTE, StateType.FAILED, "Failed"));
        } catch (InterruptedException ex) {
            LOGGER.log(Level.INFO, null, ex);
            // allow the thread to exit
        }

        fireHandleProgressEvent(null, new JBDeploymentStatus(ActionType.EXECUTE, CommandType.DISTRIBUTE, StateType.COMPLETED, "Applicaton Deployed"));
    }

    /**
     * Waits until the url is ready. As a first attemp tries to ask the
     * deployer whether the application with given deploymentUrl is already
     * started. As a fallback it asks the jboss for the MBean of the
     * warfile (name of the war is expected to be <code>moduleID.getModuleID()</code>).
     */
    private boolean waitForUrlReady(TargetModuleID moduleID, File deployedFile,
            Long previousDeploymentTime, long timeout) throws InterruptedException {

        if (Thread.currentThread().isInterrupted()) {
            throw new InterruptedException("Interrupted on wait enter"); // NOI18N
        }

        for (int i = 0, limit = (int) timeout / POLLING_INTERVAL;
                i < limit && !isApplicationReady(deployedFile, moduleID.getModuleID(), previousDeploymentTime, i == 0); i++) {

            Thread.sleep(POLLING_INTERVAL);
        }

        return isApplicationReady(deployedFile, moduleID.getModuleID(), previousDeploymentTime, false);
    }

    private Long getDeploymentTime(File fileToDeploy) {
        assert fileToDeploy != null;

        try {
            // jboss does not escape url special characters
            Object info = dm.invokeMBeanOperation(new ObjectName("jboss.system:service=MainDeployer"), // NOI18N
                    "getDeployment", new Object[] {fileToDeploy.getCanonicalFile().toURL()}, new String[] {"java.net.URL"}); // NOI18N
            if (info == null) {
                info = dm.invokeMBeanOperation(new ObjectName("jboss.system:service=MainDeployer"), // NOI18N
                    "getDeployment", new Object[] {fileToDeploy.toURL()}, new String[] {"java.net.URL"}); // NOI18N
            }
            if (info == null) {
                return Long.MIN_VALUE;
            }

            Class infoClass = info.getClass();
            return infoClass.getDeclaredField("lastDeployed").getLong(info); // NOI18N
        } catch (Exception ex) {
            // pass through, return MIN_VALUE
            LOGGER.log(Level.FINE, null, ex);
        }
        return null;
    }

    private boolean isApplicationReady(File deployedFile, String warName, Long previouslyDeployed,
            boolean initial) throws InterruptedException {

        assert deployedFile != null;
        assert warName != null;

        if (initial && previouslyDeployed == null) {
            // safety wait - avoids hitting previous content
            Thread.sleep(2000);
        }

        ClassLoader orig = Thread.currentThread().getContextClassLoader();
        // Try JMX deployer first.
        try {
            // jboss does not escape url special characters
            Object info = dm.invokeMBeanOperation(new ObjectName("jboss.system:service=MainDeployer"), // NOI18N
                    "getDeployment", new Object[] {deployedFile.getCanonicalFile().toURL()} , new String[] {"java.net.URL"}); //NOI18N
            if (info == null) {
                info = dm.invokeMBeanOperation(new ObjectName("jboss.system:service=MainDeployer"), // NOI18N
                    "getDeployment", new Object[] {deployedFile.toURL()}, new String[] {"java.net.URL"}); // NOI18N
            }

            if (info != null) {
                Thread.currentThread().setContextClassLoader(info.getClass().getClassLoader());
                Class infoClass = info.getClass();
                long lastDeployed = infoClass.getDeclaredField("lastDeployed").getLong(info); // NOI18N
                Object state = infoClass.getDeclaredField("state").get(info); // NOI18N
                Object requiredState = state.getClass().getDeclaredField("STARTED").get(null); // NOI18N
                return requiredState.equals(state)
                        && (previouslyDeployed == null || previouslyDeployed.longValue() != lastDeployed);
            }
        } catch (Exception ex) {
            // pass through, try the old way
            LOGGER.log(Level.INFO, null, ex);
        } finally {
            Thread.currentThread().setContextClassLoader(orig);
        }

        // We will try the old way (in fact this does not work for EAR).
        orig = Thread.currentThread().getContextClassLoader();
        try {
            ObjectName searchPattern = new ObjectName("jboss.web.deployment:war=" + warName + ",*"); // NOI18N
            
            MBeanServerConnection server = Util.getRMIServer(dm);
            Thread.currentThread().setContextClassLoader(server.getClass().getClassLoader());
            
            return !server.queryMBeans(searchPattern, null).isEmpty();
        } catch (Exception ex) {
            // pass through, try the old way
            LOGGER.log(Level.INFO, null, ex);
        } finally {
            Thread.currentThread().setContextClassLoader(orig);
        }

        return false;
    }

    private static class ZipEntryInputStream extends InputStream {
        private final ZipInputStream zis;

        public ZipEntryInputStream(ZipInputStream zis) {
            this.zis = zis;
        }

        @Override
        public int available() throws IOException {
            return zis.available();
        }

        @Override
        public void close() throws IOException {
            zis.closeEntry();
        }

        @Override
        public int read() throws IOException {
            if (available() > 0) {
                return zis.read();
            }
            return -1;
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            return zis.read(b, off, len);
        }

        @Override
        public long skip(long n) throws IOException {
            return zis.skip(n);
        }
    }

    // ----------  Implementation of ProgressObject interface
    private List<ProgressListener> listeners = new CopyOnWriteArrayList<ProgressListener>();

    private DeploymentStatus deploymentStatus;

    public void addProgressListener(ProgressListener pl) {
        listeners.add(pl);
    }

    public void removeProgressListener(ProgressListener pl) {
        listeners.remove(pl);
    }

    public void stop() throws OperationUnsupportedException {
        throw new OperationUnsupportedException("Stop is not supported"); // NOI18N
    }

    public boolean isStopSupported() {
        return false;
    }

    public void cancel() throws OperationUnsupportedException {
        throw new OperationUnsupportedException("Cancel is not supported"); // NOI18N
    }

    public boolean isCancelSupported() {
        return false;
    }

    public ClientConfiguration getClientConfiguration(TargetModuleID targetModuleID) {
        return null;
    }

    public TargetModuleID[] getResultTargetModuleIDs() {
        return new TargetModuleID[] {mainModuleID};
    }

    public DeploymentStatus getDeploymentStatus() {
        synchronized (this) {
            return deploymentStatus;
        }
    }

    /** Report event to any registered listeners. */
    public void fireHandleProgressEvent(TargetModuleID targetModuleID, DeploymentStatus deploymentStatus) {
        ProgressEvent evt = new ProgressEvent(this, targetModuleID, deploymentStatus);

        synchronized (this) {
            this.deploymentStatus = deploymentStatus;
        }

        for (ProgressListener listener : listeners) {
            listener.handleProgressEvent(evt);
        }
    }

}



