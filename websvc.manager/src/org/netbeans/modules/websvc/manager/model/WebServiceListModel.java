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
package org.netbeans.modules.websvc.manager.model;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.netbeans.modules.websvc.manager.WebServiceManager;
import org.netbeans.modules.websvc.manager.WebServicePersistenceManager;
import org.netbeans.modules.websvc.saas.util.WsdlUtil;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.Repository;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor.Task;
import org.openide.util.WeakListeners;

/**
 * A model to keep track of web service data and their group
 * Nodes are created using this model
 * @author Winston Prakash
 */
public class WebServiceListModel {

    public static final String DEFAULT_GROUP = "default"; // NOI18N

    private static Random serviceRandom = new Random(System.currentTimeMillis());
    private static Random serviceGroupRandom = new Random(System.currentTimeMillis());
    public static boolean MODEL_DIRTY_FLAG = false;
    Set<WebServiceListModelListener> listeners = new HashSet<WebServiceListModelListener>();
    /**
     * Fix for Bug#: 5039378
     * Netbeans can potentially use multiple threads to maintain a Node's data model.
     *- David Botterill 5/6/2004
     */
    private List<WebServiceData> webServices = Collections.synchronizedList(new ArrayList<WebServiceData>());
    private List<WebServiceGroup> webServiceGroups = Collections.synchronizedList(new ArrayList<WebServiceGroup>());
    // To maintain the display names for the webservice/port
    private Set uniqueDisplayNames = Collections.synchronizedSet(new HashSet());
    private List<String> partnerServices = new ArrayList<String>();
    private static WebServiceListModel websvcNodeModel = new WebServiceListModel();
    private boolean initialized = false;
    private RestFolderListener partnerServiceListener;

    private WebServiceListModel() {
    }

    public static WebServiceListModel getInstance() {
        return websvcNodeModel;
    }

    /**
     * For unit test use
     */
    public static void resetInstance() {
        websvcNodeModel = new WebServiceListModel();
    }

    public void addWebServiceListModelListener(WebServiceListModelListener listener) {
        listeners.add(listener);
    }

    public void removeWebServiceListModelListener(WebServiceListModelListener listener) {
        listeners.remove(listener);
    }

    public List<String> getPartnerServices() {
        return partnerServices;
    }

    private static boolean containsKey(List list, String key) {
        synchronized (list) {
            for (Object o : list) {
                if (o instanceof WebServiceData) {
                    WebServiceData wsData = (WebServiceData) o;

                    if (wsData.getId().equals(key)) {
                        return true;
                    }
                } else if (o instanceof WebServiceGroup) {
                    WebServiceGroup wsGroup = (WebServiceGroup) o;

                    if (wsGroup.getId().equals(key)) {
                        return true;
                    }
                }
            }
            return false;
        }
    }

    /** Get a unique Id for the webservice data
     *  Unique Id is "webservice" + a random number.
     */
    public String getUniqueWebServiceId() {
        initialize();
        String uniqueId = "webservice" + serviceRandom.nextLong();

        while (containsKey(webServices, uniqueId)) {
            uniqueId = "webservice" + serviceRandom.nextLong();
        }
        return uniqueId;
    }

    /** Get a unique Id for the webservice data group
     *  Unique Id is "webserviceGroup" + a random number.
     */
    public String getUniqueWebServiceGroupId() {
        initialize();
        String uniqueId = "webserviceGroup" + serviceGroupRandom.nextLong();
        while (containsKey(webServiceGroups, uniqueId)) {
            uniqueId = "webserviceGroup" + serviceGroupRandom.nextLong();
        }
        return uniqueId;
    }

    /** Add the webservice data with a unique Id */
    public void addWebService(WebServiceData webService) {
        initialize();
        if (!webServices.contains(webService)) {
            WebServiceListModel.setDirty(true);
            webServices.add(webService);
        }
    }

    /** Get the webservice data based on unique Id */
    public WebServiceData getWebService(String webServiceId) {
        synchronized (webServices) {
            initialize();
            for (WebServiceData wsData : webServices) {
                if (wsData.getId().equals(webServiceId)) {
                    return wsData;
                }
            }
        }
        return null;
    }

    /** Get the webservice data from the model */
    public void removeWebService(String webServiceId) {
        initialize();
        WebServiceData wsData = getWebService(webServiceId);
        if (wsData == null) {
            return;
        }
        WebServiceGroup group = getWebServiceGroup(wsData.getGroupId());
        WebServiceListModel.setDirty(true);
        if (group != null) {
            group.remove(webServiceId);
        }
        webServices.remove(wsData);
    }

    /** Check if the model contains the webservice data*/
    public boolean webServiceExists(WebServiceData webService) {
        initialize();
        return containsKey(webServices, webService.getId());
    }

    /** Get a unique display name */
    public String getUniqueDisplayName(String name) {
        initialize();
        String displayName = name;
        for (int i = 1; uniqueDisplayNames.contains(displayName); i++) {
            displayName = name + Integer.toString(i);
        }

        return displayName;
    }

    public boolean isDisplayNameUnique(String name) {
        return !uniqueDisplayNames.contains(name);
    }

    /** Get all the webservice data added to this model*/
    public List<WebServiceData> getWebServiceSet() {
        initialize();
        return webServices;
    }

    /** Add a webservice group to the model*/
    public void addWebServiceGroup(WebServiceGroup group) {
        initialize();
        if (!webServiceGroups.contains(group)) {
            WebServiceListModel.setDirty(true);
            webServiceGroups.add(group);

            for (WebServiceListModelListener listener : listeners) {
                WebServiceListModelEvent evt = new WebServiceListModelEvent(group.getId());
                listener.webServiceGroupAdded(evt);
            }
        }
    }

    /** Remove the webservice group from the model*/
    public void removeWebServiceGroup(String groupId) {
        initialize();
        WebServiceGroup group = getWebServiceGroup(groupId);
        if (group != null) {
            WebServiceListModel.setDirty(true);
            /**
             * Fix bug:
             * We need to get an array of the web services instead of using the Iterator because a
             * Set iterator is fail-safe and will throw a ConcurrentModificationException if you're using
             * it and the set is modified.
             * - David Botterill 5/6/2004.
             */
            String[] webserviceIds = (String[]) getWebServiceGroup(groupId).getWebServiceIds().toArray(new String[0]);
            for (int ii = 0; null != webserviceIds && ii < webserviceIds.length; ii++) {
                WebServiceManager.getInstance().removeWebService(getWebService(webserviceIds[ii]));
            }
            webServiceGroups.remove(group);
            Iterator iter = listeners.iterator();
            while (iter.hasNext()) {
                WebServiceListModelEvent evt = new WebServiceListModelEvent(groupId);
                ((WebServiceListModelListener) iter.next()).webServiceGroupRemoved(evt);
            }
        }
    }

    /** Get a webservice group by its Id*/
    public WebServiceGroup getWebServiceGroup(String groupId) {
        synchronized (webServiceGroups) {
            initialize();
            for (WebServiceGroup wsGroup : webServiceGroups) {
                if (wsGroup.getId().equals(groupId)) {
                    return wsGroup;
                }
            }
        }
        return null;
    }

    public WebServiceData findWebServiceData(String wsdlUrl, String serviceName) {
        return findWebServiceData(wsdlUrl, serviceName, true);
    }

    public WebServiceData findWebServiceData(String wsdlUrl, String serviceName, boolean strict) {
        // RESOLVE: Look up only by wsdlUrl since we don't know the serviceName
        // until the data is loaded.  In the future, we should implement
        // look up by wsdlUrl and the group name.  For now, we will assume that
        // each service is uniquely identitied by its url.
        for (WebServiceData wsd : getWebServiceSet()) {
            if (wsdlUrl.equals(wsd.getOriginalWsdl())) {
                return wsd;
            }
        }

//        WebServiceData target = null;
//        for (WebServiceData wsd : getWebServiceSet()) {
//            if (wsdlUrl.equals(wsd.getOriginalWsdl())) {
//                target = wsd;
//            }
//            if (serviceName.equals(wsd.getName())) {
//                return wsd;
//            }
//        }
//
//        if (! strict && target != null) {
//                WebServiceData clone = new WebServiceData(target);
//                clone.setName(serviceName);
//                return clone;
//        }

        return null;
    }

    public WebServiceData getWebServiceData(String wsdlUrl, String serviceName) {
        return getWebServiceData(wsdlUrl, serviceName, true);
    }

    public WebServiceData getWebServiceData(String wsdlUrl, String serviceName, boolean synchronous) {
        final WebServiceData target = findWebServiceData(wsdlUrl, serviceName, false);
        if (target != null && !target.isReady()) {
            Runnable run = new Runnable() {

                public void run() {
                    try {
                        System.out.println("adding web service");
                        WebServiceManager.getInstance().addWebService(target, true);
                    } catch (IOException ex) {
                        Logger.global.log(Level.INFO, ex.getLocalizedMessage(), ex);
                    }
                }
            };
            Task t = WebServiceManager.getInstance().getRequestProcessor().post(run);
            if (synchronous) {
                t.waitFinished();
            }
        }
        return target;
    }

    public List<WebServiceGroup> getWebServiceGroupSet() {
        initialize();
        return webServiceGroups;
    }

    public static void setDirty(boolean inDirtyFlag) {
        WebServiceListModel.MODEL_DIRTY_FLAG = inDirtyFlag;
    }

    public static boolean isDirty() {
        return WebServiceListModel.MODEL_DIRTY_FLAG;
    }

    private synchronized void initialize() {
        if (!initialized) {
            initialized = true;
            WebServicePersistenceManager manager = new WebServicePersistenceManager();
            if (!WsdlUtil.hasProcessedImport()) {
                manager.setImported(false);
                manager.load();
                WsdlUtil.markImportProcessed();
            } else {
                manager.load();
            }

            // TODO doesn't do anything useful yet
            partnerServiceListener = new RestFolderListener();
            FileSystem sfs = Repository.getDefault().getDefaultFileSystem();
            FileObject restFolder = sfs.findResource("RestComponents"); // NOI18N

            if (restFolder != null) {
                restFolder.addFileChangeListener(WeakListeners.create(FileChangeListener.class, partnerServiceListener, restFolder));
            }

            for (WebServiceGroup wsGroup : webServiceGroups) {
                if (wsGroup.getId().equals(DEFAULT_GROUP)) {
                    for (WebServiceGroupListener defaultGroupListener : defaultGroupListeners) {
                        wsGroup.addWebServiceGroupListener(defaultGroupListener);
                    }
                    defaultGroupListeners = null;
                    return;
                }
            }

            // Generate the default group on initialization if it doesn't exist
            WebServiceGroup defaultGroup = new WebServiceGroup(WebServiceListModel.DEFAULT_GROUP);
            webServiceGroups.add(defaultGroup);
            for (WebServiceGroupListener defaultGroupListener : defaultGroupListeners) {
                defaultGroup.addWebServiceGroupListener(defaultGroupListener);
            }
            defaultGroupListeners = null;
        }
    }
    private List<WebServiceGroupListener> defaultGroupListeners = new ArrayList<WebServiceGroupListener>();

    public void addDefaultGroupListener(WebServiceGroupListener listener) {
        synchronized (webServiceGroups) {
            for (WebServiceGroup wsGroup : webServiceGroups) {
                if (wsGroup.getId().equals(DEFAULT_GROUP)) {
                    wsGroup.addWebServiceGroupListener(listener);
                    return;
                }
            }

            if (!defaultGroupListeners.contains(listener)) {
                defaultGroupListeners.add(listener);
            }
        }
    }

    public boolean isInitialized() {
        return initialized;
    }

    public WebServiceData addWebService(final String wsdl, final String packageName, final String groupId) {
        final WebServiceData wsData = new WebServiceData(wsdl, groupId);
        wsData.setPackageName(packageName);
        wsData.setResolved(false);

        // Run the add W/S asynchronously
        Runnable addWsRunnable = new Runnable() {

            public void run() {
                try {
                    WebServiceManager.getInstance().addWebService(wsData, true);
                } catch (IOException ex) {
                    handleException(ex);
                }
            }
        };
        WebServiceManager.getInstance().getRequestProcessor().post(addWsRunnable);
        return wsData;
    }

    public void refreshWebService(final WebServiceData wsData) {
        // Run the add W/S asynchronously
        Runnable addWsRunnable = new Runnable() {

            public void run() {
                boolean addError = false;
                Exception exc = null;
                try {
                    WebServiceManager.getInstance().refreshWebService(wsData);
                } catch (IOException ex) {
                    handleException(ex);
                }
            }
        };
        WebServiceManager.getInstance().getRequestProcessor().post(addWsRunnable);
    }

    private void handleException(final Exception exception) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                if (exception instanceof FileNotFoundException) {
                    String errorMessage = NbBundle.getMessage(WebServiceListModel.class, "INVALID_URL");
                    NotifyDescriptor d = new NotifyDescriptor.Message(errorMessage);
                    DialogDisplayer.getDefault().notify(d);
                } else {
                    String cause = (exception != null) ? exception.getLocalizedMessage() : null;
                    String excString = (exception != null) ? exception.getClass().getName() + " - " + cause : null;

                    String errorMessage = NbBundle.getMessage(WebServiceListModel.class, "WS_ADD_ERROR") + "\n\n" + excString; // NOI18N

                    NotifyDescriptor d = new NotifyDescriptor.Message(errorMessage);
                    DialogDisplayer.getDefault().notify(d);
                }
            }
        });
    }

    private static final class RestFolderListener implements FileChangeListener {

        public void fileFolderCreated(FileEvent fe) {
            //new WebServicePersistenceManager().loadPartnerServices();
        }

        public void fileDataCreated(FileEvent fe) {
        }

        public void fileChanged(FileEvent fe) {
        }

        public void fileDeleted(FileEvent fe) {
        }

        public void fileRenamed(FileRenameEvent fe) {
        }

        public void fileAttributeChanged(FileAttributeEvent fe) {
        }
    }
}
