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

package org.netbeans.modules.db.mysql.impl;

import org.netbeans.modules.db.mysql.util.Utils;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;
import org.openide.util.NbPreferences;

/**
 * Storage and retrieval of options for MySQL support.  These options are
 * stored persistently using NbPreferences API.
 * 
 * @author David Van Couvering
 */
public class MySQLOptions {
    private String adminPassword;

    private static final Logger LOGGER = Logger.getLogger(MySQLOptions.class.getName());

    private static final MySQLOptions DEFAULT = new MySQLOptions();

    static final String PROP_MYSQL_LOCATION = "location"; // NOI18N
    static final String PROP_HOST = "host"; // NO18N
    static final String PROP_PORT = "port"; // NO18N
    static final String PROP_ADMINUSER = "adminuser"; // NO18N
    static final String PROP_ADMINPWD = "adminpwd"; // NO18N
    static final String PROP_SAVEPWD = "savepwd"; // NO18N
    static final String PROP_DBDIR = "dbdir"; // NO18N
    static final String PROP_CONN_REGISTERED = "conn-registered"; // NOI18N
    static final String PROP_PROVIDER_REGISTERED = "provider-registered"; // NOI18N
    static final String PROP_PROVIDER_REMOVED = "provider-removed"; // NO18N
    static final String PROP_ADMIN_PATH = "admin-path"; // NOI18N
    static final String PROP_START_PATH = "start-path"; // NOI18N
    static final String PROP_STOP_PATH = "stop-path"; // NOI18N
    static final String PROP_ADMIN_ARGS = "admin-args"; // NOI18N
    static final String PROP_START_ARGS = "start-args"; // NOI18N
    static final String PROP_STOP_ARGS = "stop-args"; // NOI18N
    static final String PROP_COMMANDS_CONFIRMED = "commands-confirmed"; // NOI18N
    
    // These options are not currently visible in the properties dialog, but
    // can be set by users through direct editing of the preferences file
    
    // How long to wait on the network before giving up on an attempt to
    // connect, in milliseconds
    static final String PROP_CONNECT_TIMEOUT = "connect-timeout"; // NOII18N
    static final String PROP_REFRESH_THREAD_SLEEP_INTERVAL = 
            "refresh-thread-sleep-interval"; // NOI18N
    
    // Currently not modifiable...
    private static final String DRIVER_CLASS = "com.mysql.jdbc.Driver";
    private static final String DEFAULT_PORT = "3306";
    private static final String DEFAULT_HOST = "localhost";
    private static final String DEFAULT_ADMIN_USER = "root";
    private static final String DEFAULT_ADMIN_PASSWORD = "";
    // In milliseconds
    private static final String DEFAULT_CONNECT_TIMEOUT = "15000";
    // In milliseconds
    private static final long DEFAULT_REFRESH_THREAD_SLEEP_INTERVAL = 3000;
    
    private CopyOnWriteArrayList<PropertyChangeListener> listeners = 
            new CopyOnWriteArrayList<PropertyChangeListener>();
    
    public static MySQLOptions getDefault() {
        return DEFAULT;
    }
    
    private MySQLOptions() {
        if ( Utils.isEmpty(getConnectTimeout()) ) {
            setConnectTimeout(DEFAULT_CONNECT_TIMEOUT);
        }
    }
    
    protected final void putProperty(String key, String value) {
        String oldval;
        synchronized(this) {
            oldval = getProperty(key);
            if (value != null) {
                NbPreferences.forModule(MySQLOptions.class).put(key, value);
            } else {
                NbPreferences.forModule(MySQLOptions.class).remove(key);
            }
        }
        notifyPropertyChange(key, oldval, value);
    }

    protected final void putProperty(String key, boolean value) {
        boolean oldval;
        synchronized(this) {
            oldval = getBooleanProperty(key);
            NbPreferences.forModule(MySQLOptions.class).putBoolean(key, value);
        }
        
        notifyPropertyChange(key, oldval, value);
    }
    
    protected final void putProperty(String key, long value, long def) {
        long oldval;
        synchronized(this) {
            oldval = getLongProperty(key, def);
            NbPreferences.forModule(MySQLOptions.class).putLong(key, value);
        }
        
        notifyPropertyChange(key, oldval, value);
    }
    
    protected final void clearProperty(String key) {
        String oldval;
        synchronized(this) {
            oldval = getProperty(key);
            NbPreferences.forModule(MySQLOptions.class).remove(key);
        }
        notifyPropertyChange(key, oldval, null);
    }
    
    protected final String getProperty(String key) {
        return NbPreferences.forModule(MySQLOptions.class).get(key, "");
    }
    
    protected final boolean getBooleanProperty(String key) {
        return NbPreferences.forModule(MySQLOptions.class).getBoolean(key, false); 
    }
    
    protected final long getLongProperty(String key, long def) {
        return NbPreferences.forModule(MySQLOptions.class).getLong(key, def);
    }
    
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        listeners.add(listener);
    }
    
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        listeners.remove(listener);
    }
    
    private void notifyPropertyChange(String key, Object oldval, Object newval) {
        PropertyChangeEvent event = new PropertyChangeEvent(
                this, key, oldval, newval);
                
        for ( PropertyChangeListener listener : listeners ) {
            listener.propertyChange(event);
        }
    }

    public String getHost() {
        return getProperty(PROP_HOST);
    }

    public void setHost(String host) {
        putProperty(PROP_HOST, host);
    }

    public String getPort() {
        return getProperty(PROP_PORT);
    }

    public void setPort(String port) {
        putProperty(PROP_PORT, port);
    }

    public String getAdminUser() {
        return getProperty(PROP_ADMINUSER);
    }

    public void setAdminUser(String adminUser) {
        putProperty(PROP_ADMINUSER, adminUser);
    }

    public synchronized String getAdminPassword() {
        if ( isSavePassword() ) {
            return getProperty(PROP_ADMINPWD);
        } else {
            return adminPassword;
        }
    }

    public synchronized void setAdminPassword(String adminPassword) {
        // 'null' is a valid password, but if we save as null
        // it will actually clear the property.  So convert it to
        // an empty string.
        if ( adminPassword == null ) {
            adminPassword = "";
        }
        
        // Cache password for this session whether we save it or not.
        this.adminPassword = adminPassword;
        
        if ( isSavePassword() ) {
            putProperty(PROP_ADMINPWD, adminPassword);
        } 
    }
    
    public void clearAdminPassword() {
        clearProperty(PROP_ADMINPWD);
    }

    public boolean isSavePassword() {
        return getBooleanProperty(PROP_SAVEPWD);
    }

    public void setSavePassword(boolean savePassword) {
        putProperty(PROP_SAVEPWD, savePassword);
        
        // Clear the password from the persistent file if saving
        // passwords is turned off; save the password to the persistent
        // file if saving passwords is turned on
        if ( ! savePassword ) {
            clearAdminPassword();
        } else {
            putProperty(PROP_ADMINPWD, adminPassword);
        }
    }
    
    public void setConnectionRegistered(boolean registered) {
        putProperty(PROP_CONN_REGISTERED, registered);
    }

    public boolean isConnectionRegistered() {
        return getBooleanProperty(PROP_CONN_REGISTERED);
    }
    
    public void setProviderRegistered(boolean registered) {
        // If the user is unregistering the provider and it was
        // previously registered, mark it as removed so we don't keep
        // trying to auto-register it - very annoying.
        if ( isProviderRegistered() && ! registered ) {
            setProviderRemoved(true);
        } else if ( registered ) {
            setProviderRemoved(false);
        }
        putProperty(PROP_PROVIDER_REGISTERED, registered);
    }
    
    public boolean isProviderRegistered() {
        return getBooleanProperty(PROP_PROVIDER_REGISTERED);
    }
    
    private void setProviderRemoved(boolean removed) {
        putProperty(PROP_PROVIDER_REMOVED, removed);
    }
    
    public boolean isProviderRemoved() {
        return getBooleanProperty(PROP_PROVIDER_REMOVED);
    }
    
    public String getStartPath() {
        return getProperty(PROP_START_PATH);
    }
    
    public void setStartPath(String path) {
        putProperty(PROP_START_PATH, path);
    }
    
    public String getStartArgs() {
        return getProperty(PROP_START_ARGS);
    }
    
    public void setStartArgs(String args) {
        putProperty(PROP_START_ARGS, args);
    }
    
    public String getStopPath() {
        return getProperty(PROP_STOP_PATH);
    }
    
    public void setStopPath(String path) {
        putProperty(PROP_STOP_PATH, path);
    }
    
    public String getStopArgs() {
        return getProperty(PROP_STOP_ARGS);
    }
    
    public void setStopArgs(String args) {
        putProperty(PROP_STOP_ARGS, args);
    }
    public String getAdminPath() {
        return getProperty(PROP_ADMIN_PATH);
    }
    
    public void setAdminPath(String path) {
        putProperty(PROP_ADMIN_PATH, path);
    }

    public String getAdminArgs() {
        return getProperty(PROP_ADMIN_ARGS);
    }
    
    public void setAdminArgs(String args) {
        putProperty(PROP_ADMIN_ARGS, args);
    }
    
    public boolean isAdminCommandsConfirmed() {
        return getBooleanProperty(PROP_COMMANDS_CONFIRMED);
    }
    
    public void setAdminCommandsConfirmed(boolean confirmed) {
        putProperty(PROP_COMMANDS_CONFIRMED, confirmed);
    }

    public String getConnectTimeout() {
        return getProperty(PROP_CONNECT_TIMEOUT);
    }
    
    public void setConnectTimeout(String timeout) {
        putProperty(PROP_CONNECT_TIMEOUT, timeout);
    }
    
    public long getRefreshThreadSleepInterval() {
        return getLongProperty(PROP_REFRESH_THREAD_SLEEP_INTERVAL,
                DEFAULT_REFRESH_THREAD_SLEEP_INTERVAL);
    }
    
    public void setRefreshThreadSleepInterval(long interval) {
        putProperty(PROP_REFRESH_THREAD_SLEEP_INTERVAL, interval,
                DEFAULT_REFRESH_THREAD_SLEEP_INTERVAL);
    }

    public static String getDriverClass() {
        return DRIVER_CLASS;
    }
    public static String getDefaultPort() {
        return DEFAULT_PORT;
    }

    public static String getDefaultAdminPassword() {
        return DEFAULT_ADMIN_PASSWORD;
    }

    public static String getDefaultAdminUser() {
        return DEFAULT_ADMIN_USER;
    }

    public static String getDefaultHost() {
        return DEFAULT_HOST;
    }


}
