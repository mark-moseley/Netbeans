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

package org.netbeans.modules.cnd.remote.server;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.cnd.api.remote.ServerRecord;
import org.netbeans.modules.cnd.api.utils.IpeUtils;
import org.netbeans.modules.cnd.remote.support.RemoteCommandSupport;
import org.netbeans.modules.cnd.spi.remote.ServerListImplementation;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.openide.util.ChangeSupport;
import org.openide.util.NbPreferences;

/**
 * The cnd.remote implementation of ServerList.
 * 
 * @author gordonp
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.cnd.spi.remote.ServerListImplementation.class)
public class RemoteServerList implements ServerListImplementation {
    
    private static final String CND_REMOTE = "cnd.remote"; // NOI18N
    private static final String REMOTE_SERVERS = CND_REMOTE + ".servers"; // NOI18N
    private static final char SERVER_RECORD_SEPARATOR = '|'; //NOI18N
    private static final String SERVER_LIST_SEPARATOR = ","; //NOI18N
    private static final String DEFAULT_INDEX = CND_REMOTE + ".default"; // NOI18N
    
    private static final Logger log = Logger.getLogger("cnd.remote.logger"); // NOI18N
    
    private int defaultIndex;
    private final PropertyChangeSupport pcs;
    private final ChangeSupport cs;
    private final ArrayList<RemoteServerRecord> unlisted;
    private final ArrayList<RemoteServerRecord> items = new ArrayList<RemoteServerRecord>();
    
    public RemoteServerList() {
        String slist = getPreferences().get(REMOTE_SERVERS, null);
        defaultIndex = getPreferences().getInt(DEFAULT_INDEX, 0);
        pcs = new PropertyChangeSupport(this);
        cs = new ChangeSupport(this);
        unlisted = new ArrayList<RemoteServerRecord>();
        
        // Creates the "localhost" record and any remote records cached in remote.preferences
        addServer(ExecutionEnvironmentFactory.getLocal(), null, false, RemoteServerRecord.State.ONLINE);
        if (slist != null) {
            for (String hostKey : slist.split(SERVER_LIST_SEPARATOR)) { // NOI18N
                // there moght be to forms:
                // 1) user@host:port
                // 2) user@host:port|DisplayName
                String displayName = null;
                int sepPos = hostKey.indexOf(SERVER_RECORD_SEPARATOR);
                if (sepPos >= 0) {
                    assert sepPos > 0;
                    displayName = hostKey.substring(sepPos+1);
                    if (displayName.length() == 0) {
                        displayName = null;
                    }
                    hostKey = hostKey.substring(0, sepPos);
                }
                ExecutionEnvironment env = ExecutionEnvironmentFactory.fromUniqueID(hostKey);
                if (env.isRemote()) {
                    addServer(env, displayName, false, RemoteServerRecord.State.OFFLINE);
                }
            }
        }
        refresh();
    }

    /**
     * Get a ServerRecord pertaining to env. If needed, create the record.
     * 
     * @param env specvifies the host
     * @return A RemoteServerRecord for env
     */
    @Override
    public synchronized ServerRecord get(ExecutionEnvironment env) {

        // Search the active server list
	for (RemoteServerRecord record : items) {
            if (env.equals(record.getExecutionEnvironment())) {
                return record;
            }
	}
        
        // Search the unlisted servers list. These are records created by Tools->Options
        // which haven't been added yet (and won't until/unless OK is pressed in T->O).
	for (RemoteServerRecord record : unlisted) {
            if (env.equals(record.getExecutionEnvironment())) {
                return record;
            }
	}
        
        // Create a new unlisted record and return it
        RemoteServerRecord record = new RemoteServerRecord(env);
        unlisted.add(record);
        return record;
    }

    @Override
    public synchronized ServerRecord getDefaultRecord() {
        return items.get(defaultIndex);
    }

    @Override
    public synchronized int getDefaultIndex() {
        return defaultIndex;
    }

    @Override
    public synchronized void setDefaultIndex(int defaultIndex) {
        this.defaultIndex = defaultIndex;
        getPreferences().putInt(DEFAULT_INDEX, defaultIndex);
    }
    
    @Override
    public synchronized  List<ExecutionEnvironment> getEnvironments() {
        List<ExecutionEnvironment> result = new ArrayList<ExecutionEnvironment>(items.size());
        for (RemoteServerRecord item : items) {
            result.add(item.getExecutionEnvironment());
        }
        return result;
    }
    
    private void addServer(ExecutionEnvironment execEnv, String displayName, boolean asDefault, RemoteServerRecord.State state) {
        RemoteServerRecord addServer = (RemoteServerRecord) addServer(execEnv, displayName, asDefault, false);
        addServer.setState(state);
    }


    @Override
    public synchronized ServerRecord addServer(final ExecutionEnvironment execEnv, String displayName, boolean asDefault, boolean connect) {

        RemoteServerRecord record = null;
        
        // First off, check if we already have this record
        for (RemoteServerRecord r : items) {
            if (r.getExecutionEnvironment().equals(execEnv)) {
                if (asDefault) {
                    defaultIndex = items.indexOf(r);
                    getPreferences().putInt(DEFAULT_INDEX, defaultIndex);
                }
                return r;
            }
        }
        
        // Now see if its unlisted (created in Tools->Options but cancelled with no OK)
        for (RemoteServerRecord r : unlisted) {
            if (r.getExecutionEnvironment().equals(execEnv)) {
                record = r;
                break;
            }
        }
        
        if (record == null) {
            record = new RemoteServerRecord(execEnv, displayName, connect);
        } else {
            record.setDeleted(false);
            record.setDisplayName(displayName);
            unlisted.remove(record);
        }
        items.add(record);
        if (asDefault) {
            defaultIndex = items.size() - 1;
        }
        refresh();

        // Register the new server
        // TODO: Save the state as well as name. On restart, only try connecting to
        // ONLINE hosts.
        String slist = getPreferences().get(REMOTE_SERVERS, null);
        String hostKey = ExecutionEnvironmentFactory.toUniqueID(execEnv);
        String preferencesKey = hostKey + SERVER_RECORD_SEPARATOR + ((displayName == null) ? "" : displayName);
        if (slist == null) {
            getPreferences().put(REMOTE_SERVERS, preferencesKey);
        } else {
            StringBuilder sb = new StringBuilder(preferencesKey);
            for (String server : slist.split(SERVER_LIST_SEPARATOR)) { // NOI18N
                int sepPos = server.indexOf(SERVER_RECORD_SEPARATOR);
                String serverKey = (sepPos > 0) ? server.substring(0, sepPos) : server;
                if (!serverKey.equals(hostKey)) {
                    sb.append(SERVER_LIST_SEPARATOR);
                    sb.append(server);
                }
            }
            getPreferences().put(REMOTE_SERVERS, sb.toString());
        }
        getPreferences().putInt(DEFAULT_INDEX, defaultIndex);
        return record;
    }

    public synchronized void removeServer(ServerRecord record) {
        if (items.remove(record)) {
            removeFromPreferences(record);
            refresh();
        }
    }
    
    @Override
    public synchronized void clear() {
        for (RemoteServerRecord record : items) {
            record.setDeleted(true);
        }
        getPreferences().remove(REMOTE_SERVERS);
        unlisted.addAll(items);
        items.clear();
    }

    private void removeFromPreferences(ServerRecord recordToRemove) {
        StringBuilder sb = new StringBuilder();        
        for (RemoteServerRecord record : items) {
            if (!recordToRemove.equals(record)) {
                sb.append(record.getDisplayName());
                sb.append(',');
            }
        }
        getPreferences().put(REMOTE_SERVERS, sb.toString());
    }

    protected void refresh() {
        cs.fireChange();
    }
    
    public synchronized RemoteServerRecord getLocalhostRecord() {
        return items.get(0);
    }

    //TODO: why this is here?
    //TODO: deprecate and remove
    @Override
    public boolean isValidExecutable(ExecutionEnvironment env, String path) {
        if (path == null || path.length() == 0) {
            return false;
        }
        if (SwingUtilities.isEventDispatchThread()) {
            log.warning("RemoteServerList.isValidExecutable from EDT"); // NOI18N
        }
        String cmd = "test -x " + path; // NOI18N
        int exit_status = RemoteCommandSupport.run(env, cmd);
        if (exit_status != 0 && !IpeUtils.isPathAbsolute(path)) {
            // Validate 'path' against user's PATH.
            cmd = "test -x " + "`which " + path + "`"; // NOI18N
            exit_status = RemoteCommandSupport.run(env, cmd);
        }
        return exit_status == 0;
    }
    
    @Override
    public synchronized Collection<? extends ServerRecord> getRecords() {
        return new ArrayList<RemoteServerRecord>(items);
    }
    
    // TODO: Are these still needed?
    public void addChangeListener(ChangeListener listener) {
        cs.addChangeListener(listener);
    }
    
    public void removeChangeListener(ChangeListener listener) {
        cs.removeChangeListener(listener);
    }
    
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }
    
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }
    
    public void firePropertyChange(String property, Object n) {
        pcs.firePropertyChange(property, null, n);
    }
    
    private Preferences getPreferences() {
        return NbPreferences.forModule(RemoteServerList.class);
    }
    
}
