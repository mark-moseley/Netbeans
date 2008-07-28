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
package org.netbeans.modules.cnd.remote.mapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.cnd.api.compilers.CompilerSetManager;
import org.netbeans.modules.cnd.api.utils.PlatformInfo;

/**
 *
 * @author Sergey Grinev
 */
public class HostMappingsAnalyzer {

//    private final String remoteHkey;
//    private final String userName;
//    private final String hostName;
    private final PlatformInfo secondPI;
    private final PlatformInfo firstPI;

    public HostMappingsAnalyzer(String remoteHkey) {
        this(remoteHkey, CompilerSetManager.LOCALHOST);
    }

    public HostMappingsAnalyzer(String secondHkey, String firstHkey) {
//        this.remoteHkey = remoteHkey;
//        this.localHkey = localHkey;
//        hostName = RemoteUtils.getHostName(remoteHkey);
//        userName = RemoteUtils.getUserName(remoteHkey);
        secondPI = PlatformInfo.getDefault(secondHkey);
        firstPI = PlatformInfo.getDefault(firstHkey);

        //providers
        providers = new ArrayList<HostMappingProvider>();
        // should it be Lookup?
        providers.add(new HostMappingProviderWindows());
        providers.add(new HostMappingProviderSamba());
        providers.add(new HostMappingProviderUnix());
    }

    public Map<String, String> getMappings() {
        Map<String, String> mappingsFirst2Second = new HashMap<String, String>();
            // all maps are host network name -> host local name
        Map<String, String> firstNetworkNames2Inner = populateMappingsList(firstPI, secondPI);
        Map<String, String> secondNetworkNames2Inner = populateMappingsList(secondPI, firstPI);
        
        if (firstNetworkNames2Inner.size() > 0 && secondNetworkNames2Inner.size() > 0) {
            for (String firstNetworkName : firstNetworkNames2Inner.keySet()) {
                for (String secondNetworkName : secondNetworkNames2Inner.keySet()) {
                    //TODO: investigate more complex cases
                    if (firstNetworkName.equals(secondNetworkName)) {
                        mappingsFirst2Second.put(firstNetworkNames2Inner.get(firstNetworkName), secondNetworkNames2Inner.get(secondNetworkName));
                    }
                }
            }
        }
        return mappingsFirst2Second;
    }

    private Map<String, String> populateMappingsList(PlatformInfo pi1, PlatformInfo pi2) {
        Map<String, String> map = new HashMap<String, String>();
        for (HostMappingProvider prov : providers) {
            if (prov.isApplicable(pi1, pi2)) {
                map.putAll( prov.findMappings(pi1.getHkey()) );
            }
        }
        return map;
    }

    private final List<HostMappingProvider> providers;
}
