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
import java.util.prefs.Preferences;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.cnd.api.compilers.CompilerSetManager;
import org.netbeans.modules.cnd.api.remote.ServerList;
import org.netbeans.modules.cnd.api.remote.ServerRecord;
import org.openide.util.ChangeSupport;
import org.openide.util.NbPreferences;
import org.openide.util.RequestProcessor;

/**
 *
 * @author gordonp
 */
public class RemoteServerList extends ArrayList<RemoteServerRecord> implements ServerList {
    
    public static final String PROP_SET_AS_ACTIVE = "setAsActive"; // NOI18N
    public static final String PROP_DELETE_SERVER = "deleteServer"; // NOI18N
    
    private static final String CND_REMOTE = "cnd.remote"; // NOI18N
    private static final String REMOTE_SERVERS = CND_REMOTE + ".servers"; // NOI18N
    private static final String ACTIVE_SERVER = CND_REMOTE + ".active"; // NOI18N
    
    private static RemoteServerList instance = null;
    
    private PropertyChangeSupport pcs;
    private ChangeSupport cs;
    
    public synchronized static RemoteServerList getInstance() {
        if (instance == null) {
            instance = new RemoteServerList();
        }
        return instance;
    }
    
    private RemoteServerList() {
        String slist = getPreferences().get(REMOTE_SERVERS, null);
        String active = getPreferences().get(ACTIVE_SERVER, null);
        
        pcs = new PropertyChangeSupport(this);
        cs = new ChangeSupport(this);
        
        // Creates the "localhost" record and any remote records cached in remote.preferences
        add(CompilerSetManager.LOCALHOST, true); 
        if (slist != null) {
            for (String hkey : slist.split(",")) { // NOI18N
                add(hkey, active != null && active.equals(hkey));
            }
        }
        refresh();
    }

    public ServerRecord get(String key) {
	for (RemoteServerRecord record : this) {
            if (key.equals(record.getName())) {
                return record;
            }
	}
	return null;
    }

    public int getDefaultServerIndex() {
        int i = 0;
        
        for (RemoteServerRecord record : this) {
            if (record.isActive()) {
                return i;
            } else {
                i++;
            }
        }
        return 0;
    }
    
    public String[] getServerNames() {
        Object[] oa;
        String[] sa;
        try {
            oa = toArray();
            sa = new String[oa.length];
            for (int i = 0; i < oa.length; i++) {
                if (oa[i] instanceof RemoteServerRecord) {
                    sa[i] = ((RemoteServerRecord) oa[i]).getName();
                }
            }
        } catch (Exception ex) {
            return new String[] { CompilerSetManager.LOCALHOST };
        }
        return sa;
    }
    
    public void add(final String name, boolean active) {
        RemoteServerRecord record = new RemoteServerRecord(name, active);
        add(record);
        addPropertyChangeListener(record);
        refresh();
        
        // TODO: this should follow toolchain loading
        // SystemIncludesUtils.load(record);
        
        // Register the new server
        if (!name.equals(CompilerSetManager.LOCALHOST)) {
            String slist = getPreferences().get(REMOTE_SERVERS, null);
            if (slist == null) {
                getPreferences().put(REMOTE_SERVERS, name);
            } else {
                boolean do_add = true;
                for (String server : slist.split(",")) { // NOI18N
                    if (server.equals(name)) {
                        do_add = false;
                        break;
                    }
                }
                if (do_add) {
                    getPreferences().put(REMOTE_SERVERS, slist + ',' + name);
                }
            }
            RequestProcessor.getDefault().post(new Runnable() {
                public void run() {
                    if (RemoteServerSetup.needsSetupOrUpdate(name)) {
                        RemoteServerSetup.setup(name);
                    }
                    CompilerSetManager.getDefault(name);
                }
            });
        }
        if (active) {
            record.setActive(true);
            getPreferences().put(ACTIVE_SERVER, name);
        }
    }

    public void deleteServer(RemoteServerRecord record) {
        if (remove(record)) {
            pcs.firePropertyChange(PROP_DELETE_SERVER, null, record);
            removePropertyChangeListener(record);
            if (record.isActive()) {
                getLocalhostRecord().setActive(true);
            }
            refresh();
        }
    }
    
    protected void refresh() {
        cs.fireChange();
    }
    
    public boolean contains(String key) {
        for (RemoteServerRecord record : this) {
            if (key.equals(record.getName())) {
                return true;
            }
        }
        return false;
    }

    public ServerRecord getActive() {
        for (RemoteServerRecord record : this) {
            if (record.isActive()) {
                return record;
            }
        }
        return null;
    }

    public RemoteServerRecord getLocalhostRecord() {
        return get(0);
    }
    
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
