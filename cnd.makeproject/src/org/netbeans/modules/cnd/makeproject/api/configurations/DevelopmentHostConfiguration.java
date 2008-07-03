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
import org.netbeans.modules.cnd.api.remote.ServerList;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author gordonp
 */
public class DevelopmentHostConfiguration {
    
    public static final String PROP_DEV_HOST = "devHost"; // NOI18N
    private final static String LOCALHOST = "localhost"; // NOI18N
    
    private int def;
    private int value;
    private String[] names;
    private boolean modified;
    private boolean dirty = false;
    private PropertyChangeSupport pcs;
    
    private static ServerList serverList = null;
    
    public DevelopmentHostConfiguration() {
        names = getServerNames();
        value = 0;
        def = 0; // localost is always defined and should be considered the default
        pcs = new PropertyChangeSupport(this);
    }
    
    public String getName() {
        return names[value];
    }
    
    public String getDisplayName() {
        return names[value];
    }

    public int getValue() {
        return value;
    }
    
    public void setValue(String v) {
        setValue(v, false);
    }
    
    public void setValue(String v, boolean firePC) {
        for (int i = 0; i < names.length; i++) {
            if (v.equals(names[i])) {
                value = i;
                if (firePC) {
                    pcs.firePropertyChange(PROP_DEV_HOST, null, v);
                }
                return;
            }
        }
        
        // The project's configuration wants a dev host not currently defined. Ask the
        // user what they want to do...
        NotifyDescriptor nd = new NotifyDescriptor.Confirmation(
                NbBundle.getMessage(DevelopmentHostConfiguration.class, "AddMissingRemoteServerQuestion", v));
        if (DialogDisplayer.getDefault().notify(nd) == NotifyDescriptor.YES_OPTION) {
            if (addDevelopmentHost(v)) {
                names = getServerNames();
                setValue(v, true);
            }
        } else {
            setValue(LOCALHOST, true);
        }
    }
    
    private boolean addDevelopmentHost(String host) {
        ServerList list = (ServerList) Lookup.getDefault().lookup(ServerList.class);
        if (list != null) {
            list.add(host, false);
        }
        return list != null;
    }

    public void reset() {
        names = getServerNames();
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
        
        if (names.length != conf.names.length) {
            names = getServerNames();
            dirty2 = true;
        }
        if (!newName.equals(oldName)) {
            dirty2 = true;
        }
        setDirty(dirty2);
        setValue(newName);
    }
    
    @Override
    public Object clone() {
        DevelopmentHostConfiguration clone = new DevelopmentHostConfiguration();
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
        if (getServerList() != null) {
            String[] nu = serverList.getServerNames();
            return nu;
        }
        return new String[] { LOCALHOST };
    }
    
    private static ServerList getServerList() {
        if (serverList == null) {
            serverList = (ServerList) Lookup.getDefault().lookup(ServerList.class);
        }
        return serverList;
    }
    
    public boolean isLocalhost() {
        return LOCALHOST.equals(getName());
    }
}
