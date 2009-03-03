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

package org.netbeans.modules.hudson.impl;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;
import javax.swing.SwingUtilities;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.hudson.api.HudsonChangeAdapter;
import org.netbeans.modules.hudson.api.HudsonChangeListener;
import org.netbeans.modules.hudson.api.HudsonInstance;
import org.netbeans.modules.hudson.api.HudsonJob;
import org.netbeans.modules.hudson.api.HudsonJob.Color;
import org.netbeans.modules.hudson.api.HudsonVersion;
import org.netbeans.modules.hudson.api.HudsonView;
import static org.netbeans.modules.hudson.constants.HudsonInstanceConstants.*;
import org.netbeans.modules.hudson.ui.HudsonJobView;
import org.netbeans.modules.hudson.ui.interfaces.OpenableInBrowser;
import org.netbeans.modules.hudson.ui.notification.ProblemNotificationController;
import org.netbeans.modules.hudson.util.Utilities;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;

/**
 * Implementation of the HudsonInstacne
 *
 * @author Michal Mocnak
 */
public class HudsonInstanceImpl implements HudsonInstance, OpenableInBrowser {
    
    private HudsonInstanceProperties properties;
    private HudsonConnector connector;
    
    private HudsonVersion version;
    private boolean connected;
    private boolean terminated;
    
    private final Synchronization synchronization;
    private Semaphore semaphore;
    
    private Collection<HudsonJob> jobs = new ArrayList<HudsonJob>();
    private Collection<HudsonView> views = new ArrayList<HudsonView>();
    private final Collection<HudsonChangeListener> listeners = new ArrayList<HudsonChangeListener>();
    private ProblemNotificationController problemNotificationController;
    /**
     * Must be kept here, not in {@link HudsonJobImpl}, because that is transient
     * and this should persist across refreshes.
     */
    private final Map<String,JobWorkspaceFileSystem> workspaces = new HashMap<String,JobWorkspaceFileSystem>();
    
    private HudsonInstanceImpl(HudsonInstanceProperties properties) {
        this.properties = properties;
        this.connector = new HudsonConnector(this);
        this.synchronization = new Synchronization();
        this.semaphore = new Semaphore(1, true);
        this.terminated = false;
        
        // Start synchronization
        synchronization.start();
        
        // Add property listener for synchronization
        this.properties.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getPropertyName().equals(INSTANCE_SYNC))
                    if (!synchronization.isRunning())
                        synchronization.start();
            }
        });
        
        // For listeners purposes
        final HudsonInstance instance = this;
        
        // Add content change listener to update HudsonJobViews in cache and
        // to notify all failed jobs
        addHudsonChangeListener(new HudsonChangeAdapter() {
            @Override
            public void contentChanged() {
                // Failed jobs collection
                final Collection<HudsonJob> failedJobs = new ArrayList<HudsonJob>();
                
                // Collect failed jobs ans update jobs view
                for (final HudsonJob job : getJobs()) {
                    if (job.getColor().equals(Color.red) || job.getColor().equals(Color.red_anime))
                        failedJobs.add(job);
                    
                    Utilities.invokeInAWTThread(new Runnable() {
                        public void run() {
                            // Updates jobs views in the cache
                            HudsonJobView.getInstanceFromCache(job);
                        }
                    }, true);
                }

                if (problemNotificationController == null) {
                    problemNotificationController = new ProblemNotificationController(instance);
                }
                problemNotificationController.updateNotifications();

                // When job detail is opened and job was removed, close view
                for (final HudsonJobView v : HudsonJobView.getCachedInstances()) {
                    if (instance.equals(v.getJob().getLookup().lookup(HudsonInstance.class))) {
                        if (!getJobs().contains(v.getJob())) {
                            SwingUtilities.invokeLater(new Runnable() {
                                public void run() {
                                    if (v.isOpened())
                                        v.close();
                                }
                            });
                        }
                    }
                }
            }
        });
    }

    public boolean isPersisted() {
        return !(properties instanceof ProjectHIP);
    }

    public void makePersistent() {
        if (isPersisted()) {
            return;
        }
        String name = properties.get(INSTANCE_NAME);
        String url = properties.get(INSTANCE_URL);
        String sync = properties.get(INSTANCE_SYNC);

        HudsonInstanceProperties newProps = new HudsonInstanceProperties(name, url, sync);
        //just in case there are also other properties.
        for (Map.Entry<String,String> ent : properties.entrySet()) {
            newProps.put(ent.getKey(), ent.getValue());
        }
        
        //reassign listeners
        List<PropertyChangeListener> list = ((ProjectHIP)properties).getCurrentListeners();
        for (PropertyChangeListener listener : list) {
            newProps.addPropertyChangeListener(listener);
            properties.removePropertyChangeListener(listener);
        }
        properties = newProps;

        //will this make the propes to get persisted reliably?
        properties.put(INSTANCE_URL, url);
        fireContentChanges();
    }
    
    /**
     *
     * @param name
     * @param url
     * @return
     */
    public static HudsonInstanceImpl createHudsonInstance(String name, String url) {
        return createHudsonInstance(new HudsonInstanceProperties(name, url));
    }
    
    /**
     *
     * @param name
     * @param url
     * @return
     */
    public static HudsonInstanceImpl createHudsonInstance(String name, String url, String sync) {
        return createHudsonInstance(new HudsonInstanceProperties(name, url, sync));
    }
    
    /**
     *
     * @param name
     * @param url
     * @return
     */
    public static HudsonInstanceImpl createHudsonInstance(HudsonInstanceProperties properties) {
        HudsonInstanceImpl instance = new HudsonInstanceImpl(properties);
        
        if (null == HudsonManagerImpl.getInstance().addInstance(instance))
            return null;
        
        return instance;
    }
    
    public void terminate() {
        // Clear all
        synchronization.stop();
        terminated = true;
        connected = false;
        version = null;
        jobs.clear();
        views.clear();
        
        // Fire changes
        fireStateChanges();
        fireContentChanges();
    }
    
    public HudsonConnector getConnector() {
        return connector;
    }
    
    public HudsonVersion getVersion() {
        return version;
    }
    
    public boolean isConnected() {
        return connected;
    }
    
    public HudsonInstanceProperties getProperties() {
        return properties;
    }
    
    public String getName() {
        return getProperties().get(INSTANCE_NAME);
    }
    
    public String getUrl() {
        String url = getProperties().get(INSTANCE_URL);
        assert url.endsWith("/") : url;
        return url;
    }
    
    public synchronized Collection<HudsonJob> getJobs() {
        return jobs;
    }

    public synchronized Collection<HudsonJob> getPreferredJobs() {
        Collection<HudsonJob> prefs = new ArrayList<HudsonJob>();
        Collection<HudsonJob> all = getJobs();
        String prop = getProperties().get(INSTANCE_PREF_JOBS);
        if (prop != null && prop.trim().length() > 0) {
            String[] ids = prop.trim().split("\\|");
            List<String> idsList = Arrays.asList(ids);
            for (HudsonJob jb : all) {
                if (idsList.contains(jb.getName())) {
                    prefs.add(jb);
                }
            }
        }
        return prefs;
    }
    
    public synchronized Collection<HudsonView> getViews() {
        return views;
    }
    
    public synchronized void setViews(Collection<HudsonView> views) {
        this.views = views;
    }
    
    public synchronized void synchronize() {
        if (semaphore.tryAcquire()) {
            final ProgressHandle handle = ProgressHandleFactory.createHandle(
                    NbBundle.getMessage(HudsonInstanceImpl.class, "MSG_Synchronizing", getName()));
            
            handle.start();
            
            RequestProcessor.getDefault().post(new Runnable() {
                public void run() {
                    try {
                        // Get actual views
                        Collection<HudsonView> oldViews = getViews();
                        
                        // Retrieve jobs
                        Collection<HudsonJob> retrieved = getConnector().getAllJobs();
                        
                        // Exit when instance is terminated
                        if (terminated)
                            return;
                        
                        // Set connected and version
                        connected = getConnector().isConnected();
                        version = getConnector().getHudsonVersion();
                        
                        // Update state
                        fireStateChanges();
                        
                        for (JobWorkspaceFileSystem fs : workspaces.values()) {
                            fs.refreshAll();
                        }

                        // Sort retrieved list
                        Collections.sort(Arrays.asList(retrieved.toArray(new HudsonJob[] {})));
                        
                        // When there are no changes return and do not fire changes
                        if (getJobs().equals(retrieved) && oldViews.equals(getViews()))
                            return;
                        
                        // Update jobs
                        jobs = retrieved;

                        // Fire all changes
                        fireContentChanges();
                    } finally {
                        handle.finish();
                        
                        // Release synchronization lock
                        semaphore.release();
                    }
                }
            });
        }
    }
    
    public void addHudsonChangeListener(HudsonChangeListener l) {
        listeners.add(l);
    }
    
    public void removeHudsonChangeListener(HudsonChangeListener l) {
        listeners.remove(l);
    }
    
    protected void fireStateChanges() {
        ArrayList<HudsonChangeListener> tempList;
        
        synchronized (listeners) {
            tempList = new ArrayList<HudsonChangeListener>(listeners);
        }
        
        for (HudsonChangeListener l : tempList) {
            l.stateChanged();
        }
    }
    
    protected void fireContentChanges() {
        ArrayList<HudsonChangeListener> tempList;
        
        synchronized (listeners) {
            tempList = new ArrayList<HudsonChangeListener>(listeners);
        }
        
        for (HudsonChangeListener l : tempList) {
            l.contentChanged();
        }
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof HudsonInstance))
            return false;
        
        final HudsonInstance other = (HudsonInstance) obj;
        
        if (getUrl() != other.getUrl() &&
                (getUrl() == null || !getUrl().equals(other.getUrl())))
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        return getUrl() == null ? 445 : getUrl().hashCode();
    }

    public @Override String toString() {
        return "HudsonInstanceImpl[" + getUrl() + "]"; // NOI18N
    }

    public int compareTo(HudsonInstance o) {
        return getName().compareTo(o.getName());
    }

    /* access from HudsonJobImpl */ FileSystem getRemoteWorkspace(HudsonJob job) {
        synchronized (workspaces) {
            String name = job.getName();
            if (!workspaces.containsKey(name)) {
                try {
                    workspaces.put(name, new JobWorkspaceFileSystem(job));
                } catch (MalformedURLException ex) {
                    Exceptions.printStackTrace(ex);
                    return FileUtil.createMemoryFileSystem();
                }
            }
            return workspaces.get(name);
        }
    }

    
    private class Synchronization implements Runnable {
        
        private Task task;
        private Semaphore lock = new Semaphore(1);
        private RequestProcessor processor = new RequestProcessor(getUrl(), 1, true);
        
        public synchronized void start() {
            if (lock.tryAcquire())
                task = processor.post(this);
        }
        
        public synchronized void stop() {
            task.cancel();
        }
        
        public synchronized void terminate() {
            processor.stop();
        }
        
        public synchronized boolean isRunning() {
            return lock.availablePermits() == 0;
        }
        
        public void run() {
            long pause;
            
            try {
                do {
                    // Synchronize
                    synchronize();
                    
                    // Refresh wait time
                    String s = getProperties().get(INSTANCE_SYNC);
                    pause = Integer.parseInt(s) * 60 * 1000;
                    
                    // Wait for the specified amount of time
                    Thread.sleep(pause);
                } while (pause > 0);
            } catch (InterruptedException e) {
                // Synchronization is stopped or terminated
            } finally {
                // release lock
                lock.release();
            }
        }
    }
}