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

package org.netbeans.modules.j2ee.deployment.config;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.ConfigurationFilesListener;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.deployment.impl.Server;
import org.netbeans.modules.j2ee.deployment.impl.ServerRegistry;
import org.openide.filesystems.FileObject;

/**
 * @author nn136682
 */
public class ConfigFilesListener extends AbstractFilesListener {
    List consumers; //ConfigurationFileListener's
    
    public ConfigFilesListener(J2eeModuleProvider provider, List consumers) {
        super(provider);
        this.consumers = consumers;
    }
    
    protected File[] getTargetFiles() {
        //locate the root to listen to
        Collection servers = ServerRegistry.getInstance().getServers();
        ArrayList result = new ArrayList();
        for (Iterator i=servers.iterator(); i.hasNext();) {
            Server s = (Server) i.next();
            String[] paths = s.getDeploymentPlanFiles(provider.getJ2eeModule().getType());
            if (paths == null)
                continue;
            
            for (int j = 0; j < paths.length; j++) {
                File f = provider.getJ2eeModule().getDeploymentConfigurationFile(paths[j]);
                if (f != null)
                    result.add(f);
            }
        }
        return (File[]) result.toArray(new File[result.size()]);
    }

    protected void targetDeleted(FileObject deleted) {
        fireConfigurationFilesChanged(false, deleted);
    }
    
    protected void targetCreated(FileObject added) {
        fireConfigurationFilesChanged(true, added);
    }
    
    protected void targetChanged(FileObject deleted) {
    }
    
    private void fireConfigurationFilesChanged(boolean added, FileObject fo) {
        for (Iterator i=consumers.iterator(); i.hasNext();) {
            ConfigurationFilesListener cfl = (ConfigurationFilesListener) i.next();
            if (added) {
                cfl.fileCreated(fo);
            } else {
                cfl.fileDeleted(fo);
            }
        }
    }

    protected boolean isTarget(FileObject fo) {
        return isTarget(fo.getNameExt());
    }
    protected boolean isTarget(String fileName) {
        return ServerRegistry.getInstance().isConfigFileName(fileName, provider.getJ2eeModule().getType());
    }
}
