/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.cnd.makeproject.api.configurations;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.netbeans.modules.cnd.api.compilers.CompilerSetManager;
import org.netbeans.modules.cnd.api.remote.ExecutionEnvironmentFactory;
import org.netbeans.modules.cnd.api.remote.ServerList;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * @author gordonp
 */
public class DevelopmentHostConfiguration {

    public static final String PROP_DEV_HOST = "devHost"; // NOI18N
    private final int def;

    // TODO: rewrite! list/value concept is error prone!

    private int value;
    private List<ExecutionEnvironment> servers;

    private boolean modified;
    private boolean dirty = false;
    private PropertyChangeSupport pcs;
    private static ServerList serverList = null;

    public DevelopmentHostConfiguration(ExecutionEnvironment execEnv) {
        servers = getServerEnvironments();
        value = 0;
        for (int i = 0; i < servers.size(); i++) {
            if (execEnv.equals(servers.get(i))) {
                value = i;
                break;
            }
        }
        def = value;
        pcs = new PropertyChangeSupport(this);
    }

    /** TODO: deprecate and remove, see #158983 */
    public String getName() {
        return ExecutionEnvironmentFactory.getHostKey(servers.get(value));
    }

    public ExecutionEnvironment getExecutionEnvironment() {
        return ExecutionEnvironmentFactory.getExecutionEnvironment(getName());
    }

    public String getDisplayName(boolean displayIfNotFound) {
        String out = getName();
        if (displayIfNotFound && !isOnline()) {
            out = NbBundle.getMessage(DevelopmentHostConfiguration.class,  "NOT_CONFIGURED", out); // NOI18N
        }
        return out;
    }

    public boolean isOnline() {
        // localhost is always STATE_COMPLETE so isLocalhost() is assumed
        // keeping track of online status takes more efforts and can miss sometimes
        return !CompilerSetManager.getDefault(getExecutionEnvironment()).isUninitialized();
    }

    public int getValue() {
        return value;
    }

    public void setValue(String v) {
        setValue(v, false);
    }

    public void setValue(final String v, boolean firePC) {
        for (int i = 0; i < servers.size(); i++) {
            String currName = ExecutionEnvironmentFactory.getHostKey(servers.get(i));
            if (v.equals(currName)) {
                String oname = currName;
                value = i;
                if (firePC) {
                    pcs.firePropertyChange(PROP_DEV_HOST, oname, this);
                }
                return;
            }
        }

        // The project's configuration wants a dev host not currently defined.
        // We don't want to ask user at this moment, so we create offline host and preserve compilerset name
        // User will be asked about connection after choosing action like build for this particular project
        // or after click on brand-new "..." button!
        addDevelopmentHost(v);
        servers = getServerEnvironments();
        setValue(v, firePC);
    }

    private boolean addDevelopmentHost(String host) {
        ServerList list = Lookup.getDefault().lookup(ServerList.class);
        if (list != null) {
            list.addServer(ExecutionEnvironmentFactory.getExecutionEnvironment(host), false, false);
        }
        return list != null;
    }

    public void reset() {
        servers = getServerEnvironments();
        value = def;
    }

    public boolean getModified() {
        return modified;
    }

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    public boolean getDirty() {
        return dirty;
    }

    void assign(DevelopmentHostConfiguration conf) {
        boolean dirty2 = false;
        String oldName = getName();
        String newName = conf.getName();

        if (servers.size() != conf.servers.size()) {
            servers = getServerEnvironments();
            dirty2 = true;
        }
        if (!newName.equals(oldName)) {
            dirty2 = true;
        }
        setDirty(dirty2);
        setValue(newName);
    }

    @Override
    public DevelopmentHostConfiguration clone() {
        DevelopmentHostConfiguration clone = new DevelopmentHostConfiguration(getExecutionEnvironment());
        // FIXUP: left setValue call to leave old logic
        clone.setValue(getName());
        return clone;
    }

    public void addPropertyChangeListener(PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }

    public void removePropertyChangeListener(PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }

    public String[] getServerNames() {
        List<String> l = new ArrayList<String>();
        int pos = 0;
        for (ExecutionEnvironment env : getServerEnvironments()) {
            l.add(ExecutionEnvironmentFactory.getHostKey(env));
        }
        return l.toArray(new String[l.size()]);
    }

    public List<ExecutionEnvironment> getServerEnvironments() {
        if (getServerList() != null) {
            return getServerList().getEnvironments();
        }
        return Arrays.asList(ExecutionEnvironmentFactory.getLocalExecutionEnvironment());
    }

    private static ServerList getServerList() {
        if (serverList == null) {
            serverList = Lookup.getDefault().lookup(ServerList.class);
        }
        return serverList;
    }

    public boolean isLocalhost() {
        return getExecutionEnvironment().isLocal();
    }
}
