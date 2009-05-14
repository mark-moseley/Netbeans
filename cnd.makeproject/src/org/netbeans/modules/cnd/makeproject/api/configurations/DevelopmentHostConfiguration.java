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
import java.util.List;
import org.netbeans.modules.cnd.api.compilers.CompilerSetManager;
import org.netbeans.modules.cnd.api.compilers.PlatformTypes;
import org.netbeans.modules.cnd.api.remote.ServerRecord;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.cnd.api.remote.ServerList;
import org.netbeans.modules.cnd.makeproject.api.platforms.Platform;
import org.netbeans.modules.cnd.makeproject.api.platforms.Platforms;
import org.netbeans.modules.cnd.spi.remote.RemoteSyncFactory;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
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

    private int buildPlatform; // Actual build platform

    private boolean modified;
    private boolean dirty = false;
    private PropertyChangeSupport pcs;

    public DevelopmentHostConfiguration(ExecutionEnvironment execEnv) {
        servers = ServerList.getEnvironments();
        value = 0;
        for (int i = 0; i < servers.size(); i++) {
            if (execEnv.equals(servers.get(i))) {
                value = i;
                break;
            }
        }
        def = value;
        pcs = new PropertyChangeSupport(this);

        buildPlatform = CompilerSetManager.getDefault(execEnv).getPlatform();
        if (buildPlatform == -1) {
            // TODO: CompilerSet is not reliable about platform; it must be.
            buildPlatform = PlatformTypes.PLATFORM_NONE;
        }
    }

    /** TODO: deprecate and remove, see #158983 */
    public String getHostKey() {
        return ExecutionEnvironmentFactory.toUniqueID(servers.get(value));
    }

    public ExecutionEnvironment getExecutionEnvironment() {
        return servers.get(value);
    }

    public String getDisplayName(boolean displayIfNotFound) {
        String out = ServerList.get(getExecutionEnvironment()).getDisplayName();
        if (displayIfNotFound && !isConfigured()) {
            out = NbBundle.getMessage(DevelopmentHostConfiguration.class,  "NOT_CONFIGURED", out); // NOI18N
        }
        else {
            int platformID = CompilerSetManager.getDefault(getExecutionEnvironment()).getPlatform();
            Platform platform = Platforms.getPlatform(platformID);
            if (platform != null) {
                out += " [" + platform.getDisplayName() + "]"; // NOI18N
            }
        }
        return out;
    }

    public String getHostDisplayName(boolean displayIfNotFound) {
        String out = ServerList.get(getExecutionEnvironment()).getServerDisplayName();
        if (displayIfNotFound && !isConfigured()) {
            out = NbBundle.getMessage(DevelopmentHostConfiguration.class,  "NOT_CONFIGURED", out); // NOI18N
        }
        return out;
    }

    public boolean isConfigured() {
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
        if (setValueImpl(v, firePC)) {
            return;
        }
        // The project's configuration wants a dev host not currently defined.
        // We don't want to ask user at this moment, so we create offline host and preserve compilerset name
        // User will be asked about connection after choosing action like build for this particular project
        // or after click on brand-new "..." button!
        addDevelopmentHost(v);
        servers = ServerList.getEnvironments();
        setValueImpl(v, firePC);
    }

    public boolean setHost(ExecutionEnvironment execEnv) {
        CndUtils.assertTrue(execEnv != null);
        for (int i = 0; i < servers.size(); i++) {
            if (servers.get(i).equals(execEnv)) {
                value = i;
                setBuildPlatform(CompilerSetManager.getDefault(execEnv).getPlatform());
                if (getBuildPlatform() == -1) {
                    // TODO: CompilerSet is not reliable about platform; it must be.
                    setBuildPlatform(PlatformTypes.PLATFORM_NONE);
                }
                return true;
            }
        }
        return false;
    }

    private boolean setValueImpl(final String v, boolean firePC) {
        for (int i = 0; i < servers.size(); i++) {
            final ExecutionEnvironment currEnv = servers.get(i);
            final ServerRecord currRecord = ServerList.get(currEnv);
            //TODO: could we use something straightforward here?
            if (currRecord.getDisplayName().equals(v)) {
                value = i;
                setBuildPlatform(CompilerSetManager.getDefault(currEnv).getPlatform());
                if (getBuildPlatform() == -1) {
                    // TODO: CompilerSet is not reliable about platform; it must be.
                    setBuildPlatform(PlatformTypes.PLATFORM_NONE);
                }
                if (firePC) {
                    pcs.firePropertyChange(PROP_DEV_HOST, 
                            ExecutionEnvironmentFactory.toUniqueID(currRecord.getExecutionEnvironment()),
                            this);
                }
                return true;
            }
        }
        return false;
    }

    private boolean addDevelopmentHost(String host) {
        final ServerRecord record = ServerList.addServer(ExecutionEnvironmentFactory.fromUniqueID(host), null, RemoteSyncFactory.getDefault(), false, false);
        return record != null;
    }

    public void reset() {
        servers = ServerList.getEnvironments();
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
        ExecutionEnvironment oldEnv = getExecutionEnvironment();
        ExecutionEnvironment newEnv = conf.getExecutionEnvironment();

        if (servers.size() != conf.servers.size()) {
            servers = ServerList.getEnvironments();
            dirty2 = true;
        }
        if (!newEnv.equals(oldEnv)) {
            dirty2 = true;
        }
        setDirty(dirty2);
        setHost(newEnv);
    }

    @Override
    public DevelopmentHostConfiguration clone() {
        DevelopmentHostConfiguration clone = new DevelopmentHostConfiguration(getExecutionEnvironment());
        // FIXUP: left setValue call to leave old logic
        clone.setHost(getExecutionEnvironment());
        return clone;
    }

    public void addPropertyChangeListener(PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }

    public void removePropertyChangeListener(PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }

    public boolean isLocalhost() {
        return getExecutionEnvironment().isLocal();
    }

    /**
     * @return the buildPlatform
     */
    public int getBuildPlatform() {
        return buildPlatform;
    }

    /**
     * @param buildPlatform the buildPlatform to set
     */
    public void setBuildPlatform(int buildPlatform) {
        this.buildPlatform = buildPlatform;
    }

    public String getBuildPlatformDisplayName() {
        if (isConfigured()) {
            return Platforms.getPlatform(getBuildPlatform()).getDisplayName();
        }
        else {
            return "";
        }
    }

}
