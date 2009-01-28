/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.ui.options;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import org.netbeans.modules.cnd.api.compilers.CompilerSet;
import org.netbeans.modules.cnd.api.compilers.CompilerSetManager;
import org.netbeans.modules.cnd.api.remote.ServerList;
import org.netbeans.modules.cnd.api.remote.ServerRecord;
import org.netbeans.modules.cnd.api.remote.ServerUpdateCache;
import org.openide.util.Lookup;

/**
 *
 * @author Sergey Grinev
 */
public final class ToolsCacheManager {

    private final ServerList serverList;
    private ServerUpdateCache serverUpdateCache;
    private HashMap<String, CompilerSetManager> copiedManagers = new HashMap<String, CompilerSetManager>();

    public ToolsCacheManager() {
        serverList = Lookup.getDefault().lookup(ServerList.class);
    }

    public ServerUpdateCache getServerUpdateCache() {
        return serverUpdateCache;
    }

    public String[] getHostKeyList() {
        if (serverUpdateCache != null) {
            return serverUpdateCache.getHostKeyList();
        } else if (isRemoteAvailable()) {
            return serverList.getServerNames();
        } else {
            return null;
        }
    }

    public int getDefaultHostIndex() {
        if (serverUpdateCache != null) {
            return serverUpdateCache.getDefaultIndex();
        } else if (isRemoteAvailable()) {
            return serverList.getDefaultIndex();
        } else {
            return 0;
        }
    }

    public void setHostKeyList(String[] list) {
        if (serverUpdateCache == null) {
            serverUpdateCache = new ServerUpdateCache();
        }
        serverUpdateCache.setHostKeyList(list);
    }

    public void setDefaultIndex(int index) {
        serverUpdateCache.setDefaultIndex(index);
    }

    public boolean hasCache() {
        return serverUpdateCache != null;
    }

    private void saveCompileSetManagers(List<String> liveServers) {
        Collection<CompilerSetManager> allCSMs = new ArrayList<CompilerSetManager>();
        for (String copiedServer : copiedManagers.keySet()) {
            if (liveServers == null || liveServers.contains(copiedServer)) {
                allCSMs.add(copiedManagers.get(copiedServer));
            }
        }
        CompilerSetManager.setDefaults(allCSMs);
        copiedManagers.clear();
    }

    public void applyChanges() {
        applyChanges(0);
    }
    
    public void applyChanges(int selectedIndex) {
        List<String> liveServers = null;
        if (isRemoteAvailable()) {
            if (serverUpdateCache != null) {
                liveServers = new ArrayList<String>();
                serverList.clear();
                for (String key : serverUpdateCache.getHostKeyList()) {
                    serverList.addServer(key, false, false);
                    liveServers.add(key);
                }
                serverList.setDefaultIndex(serverUpdateCache.getDefaultIndex());
                serverUpdateCache = null;
            } else {
                serverList.setDefaultIndex(selectedIndex);
            }
        }

        saveCompileSetManagers(liveServers);
    }

    public void clear() {
        serverUpdateCache = null;
        copiedManagers.clear();
    }

    public boolean show() {
        assert isRemoteAvailable();
        // Show the Dev Host Manager dialog
        return serverList.show(this);
    }

    //TODO: we should be ensured already....check
    public void ensureHostSetup(String hkey) {
        if (hkey != null) {
            serverList.get(hkey); // this will ensure the remote host is setup
        }
    }

    public boolean isDevHostValid(String hkey) {
        if (isRemoteAvailable()) {
            ServerRecord record = serverList.get(hkey);
            return record != null && record.isOnline();
        } else {
            return false;
        }
    }

    public boolean isRemoteAvailable() {
        return serverList != null;
    }

    public String getDefaultHostKey() {
        return serverList.getDefaultRecord().getName();
    }

    public synchronized CompilerSetManager getCompilerSetManagerCopy(String hKey) {
        CompilerSetManager out = copiedManagers.get(hKey);
        if (out == null) {
            out = CompilerSetManager.getDefault(hKey).deepCopy();
            if (out.getCompilerSets().size() == 1 && out.getCompilerSets().get(0).getName().equals(CompilerSet.None)) {
                out.remove(out.getCompilerSets().get(0));
            }
            copiedManagers.put(hKey, out);
        }
        return out;
    }

    public void addCompilerSetManager(CompilerSetManager newCsm) {
        copiedManagers.put(newCsm.getHost(), newCsm);
    }
}
