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

package org.netbeans.modules.j2ee.deployment.impl;

import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.shared.ModuleType;
import org.openide.util.NbBundle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author  nn136682
 */
public class TargetModule implements TargetModuleID, java.io.Serializable {

    private static final long serialVersionUID = 69446832504L;

    private final String id;
    private final String instanceUrl;
    private final String targetName;
    private final long timestamp;
    private final String contentDirectory;
    private final String contextRoot;
    private transient TargetModuleID delegate;
    public static final TargetModuleID[] EMPTY_TMID_ARRAY = new TargetModuleID[0];
    
    /** Creates a new instance of TargetModule */
    public TargetModule(String id, String url, long timestamp, String contentDir, String contextRoot, TargetModuleID delegate) {
        this(id, url, delegate.getTarget().getName(), timestamp, contentDir, contextRoot);
        this.delegate = delegate;
    }
    
    public TargetModule(String id, String url, String targetName, long timestamp, String contentDir, String contextRoot) {
        if (id == null || url == null || targetName == null || timestamp < 0) {
            java.util.List args = Arrays.asList(new Object[] { id, url, targetName, Long.valueOf(timestamp)});
            throw new IllegalArgumentException(NbBundle.getMessage(TargetModule.class, "MSG_BadTargetModuleAttributes", args));
        }
        this.id = id;
        this.instanceUrl = url;
        this.targetName = targetName;
        this.timestamp = timestamp;
        this.contentDirectory = contentDir;
        this.contextRoot = (contextRoot == null) ? "" : contextRoot;
    }

    /* wrapper for map/set operation only */
    public TargetModule(String id, TargetModuleID delegate) {
        this(id, "someurl", 0, null, null,delegate);
    }
    
    public String getId() { return id; }
    public String getInstanceUrl() { return instanceUrl; }
    public String getTargetName() { 
        if (delegate != null) 
            return delegate.getTarget().getName();
        else
            return targetName;
    }
    public long getTimestamp() { return timestamp; }
    //public void setTimestamp(long ts) { this.timestamp = ts; }
    public String getContentDirectory() {
        return contentDirectory;
    }
    public String getContextRoot() {
        return contextRoot;
    }
    
    public static class List implements java.io.Serializable {
        private static final long serialVersionUID = 69446832514L;
        private TargetModule [] targetModules;
        public List(TargetModule[] targetModules) {
            this.targetModules = targetModules;
        }
        public List(TargetModule tm) {
            this.targetModules = new TargetModule[] { tm };
        }
        public TargetModule[] getTargetModules() {
            return targetModules;
        }
    }

    public Target findTarget() {
        ServerInstance instance = ServerRegistry.getInstance().getServerInstance(instanceUrl);
        return instance.getServerTarget(targetName).getTarget();
    }
    
    //Delegate to TargetModuleID
    public void initDelegate(ModuleType type) {
        if (delegate == null) {
            ServerInstance instance = ServerRegistry.getInstance().getServerInstance(instanceUrl);
            DeploymentManager dm = instance.getDeploymentManager();
            Target target = findTarget();

            try {
                TargetModuleID[] tmIDs = dm.getAvailableModules(type, new Target[] {target});
                for (int i=0; i<tmIDs.length; i++) {
                    if (id.equals(tmIDs[i].toString())) {
                        delegate = tmIDs[i];
                        break;
                    }
                }
            } catch (Exception e) {
                Logger.getLogger("global").log(Level.INFO, null, e);
            }
        }
    }
    
    public void initDelegate(TargetModuleID delegate) { 
        this.delegate = delegate; 
    }
    
    public static TargetModuleID[] toTargetModuleID(TargetModule[] targetModules) {
        if (targetModules == null) return new TargetModuleID[0];
        TargetModuleID [] ret = new TargetModuleID[targetModules.length];
        for (int i=0; i<ret.length; i++) {
            ret[i] = targetModules[i].delegate();
        }
        return ret;
    }

    public static Target[] toTarget(TargetModule[] targetModules) {
        if (targetModules == null) return new Target[0];
        Target[] ret = new Target[targetModules.length];
        for (int i=0; i<ret.length; i++) {
            ret[i] = targetModules[i].delegate().getTarget();
        }
        return ret;
    }
    
    public TargetModuleID delegate() {
        if (delegate == null) {
            throw new IllegalStateException("Delegate is not set yet"); //NOI18N
        }
        return delegate;
    }
    public javax.enterprise.deploy.spi.TargetModuleID[] getChildTargetModuleID() {
        return delegate().getChildTargetModuleID();
    }
    public String getModuleID() {
        return delegate().getModuleID();
    }
    public javax.enterprise.deploy.spi.TargetModuleID getParentTargetModuleID() {
        return delegate().getParentTargetModuleID();
    }
    public javax.enterprise.deploy.spi.Target getTarget() {
        return delegate().getTarget();
    }
    public String getWebURL() {
        return delegate().getWebURL();
    }
    public String toString() {
        if (delegate == null)
            return id; //issue 37930
        else
            return delegate.toString();
    }
    public int hashCode() {
        return getId().hashCode();
    }
    public boolean equals(Object obj) {
        if (obj instanceof TargetModuleID) {
            TargetModuleID that = (TargetModuleID) obj;
            return this.getModuleID().equals(that.getModuleID()) && this.getTargetName().equals(that.getTarget().getName());
        }
        return false;
    }
    //NOTE: this also return list of TM's with delegate only
    public static java.util.List initDelegate(java.util.List targetModules, java.util.Map delegateTMIDsMap) {
        ArrayList result = new ArrayList();
        for (java.util.Iterator i=targetModules.iterator(); i.hasNext();) {
            TargetModule tm = (TargetModule) i.next();
            TargetModuleID tmid = (TargetModuleID) delegateTMIDsMap.get(tm.getId());
            if (tmid != null) {
                tm.initDelegate(tmid);
                result.add(tm);
            }
        }
        return result;
    }
    
//----------------------- persistence operations -------------------------------
    public static java.util.List findByContextRoot(ServerString server, String contextRoot) {
        String managerDirName = getManagerDirName(server);
        String[] targetNames = server.getTargets(true);
        ArrayList targetModules = new ArrayList();
        for (int i=0; i<targetNames.length; i++) {
            String targDirName = getReadableName(targetNames[i]);
            java.util.List tml = TargetModuleConverter.getTargetModulesByContextRoot(managerDirName, targDirName, contextRoot);
            targetModules.addAll(tml);
        }
        return targetModules;
    }
    public static void removeByContextRoot(ServerString server, String contextRoot) {
        java.util.List tms = TargetModule.findByContextRoot(server, contextRoot);
        for (java.util.Iterator i=tms.iterator(); i.hasNext(); ) {
            TargetModule tm = (TargetModule) i.next();
            tm.remove();
        }
    }
    private static String getManagerDirName(ServerString server) {
        return getReadableName(server.getUrl());
    }
    private String getManagerDirName() {
        return getReadableName(getInstanceUrl());
    }
    public static TargetModule[] load(ServerString server, String fileName) {
        String managerDirName = getManagerDirName(server);
        String[] targetNames = server.getTargets(true);
        java.util.List targetModules = new ArrayList();
        for (int i=0; i<targetNames.length; i++) {
            String targDirName = getReadableName(targetNames[i]);
            TargetModule tm = TargetModuleConverter.readTargetModule(managerDirName, targDirName, fileName);
            if (tm != null) {
                targetModules.add(tm);
            }
        }
        return (TargetModule[]) targetModules.toArray(new TargetModule[targetModules.size()]);
    }
    public void save(String fileName) {
        TargetModuleConverter.writeTargetModule(this, getManagerDirName(), getReadableName(targetName), fileName);
    }
    public void remove() {
        String managerDirName = getManagerDirName();
        String targDirName = getReadableName(targetName);
        //NOTE: no effect if filename was derived from module folder instead of content folder
        String fileName = shortNameFromPath(getContentDirectory());
        TargetModuleConverter.remove(managerDirName, targDirName, fileName);
    }
    public static String getReadableName(String s) {
        int code = s.hashCode();
        int end = 16;
        if (end > s.length())
            end = s.length();
        StringBuffer sb = TargetModule.subStringBuffer(s, 0, end);
        sb.append(String.valueOf(code));
        return sb.toString();
    }
    public static StringBuffer subStringBuffer(String s, int start, int end) {
        StringBuffer sb = new StringBuffer(64);
        if (end < 0)
            sb.append(s.substring(start));
        else
            sb.append(s.substring(start, end));
        for (int i=0; i<sb.length(); i++) {
            if (! Character.isLetterOrDigit(sb.charAt(i))) 
                sb.setCharAt(i, '_');
        }
        return sb;
    }

    public static String shortNameFromPath(String pathName) {
        int code = pathName.hashCode();
        int start = pathName.length()-16;
        if (start < 0)
            start = 0;
        StringBuffer sb = TargetModule.subStringBuffer(pathName, start, -1);
        sb.append(String.valueOf(code));
        return sb.toString();
    }
}
