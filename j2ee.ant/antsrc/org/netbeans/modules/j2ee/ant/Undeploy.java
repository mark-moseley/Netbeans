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

package org.netbeans.modules.j2ee.ant;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import org.openide.util.Lookup;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * Ant task that starts the server if needed and deploys module to the server
 * @author Martin Grebac
 */
public class Undeploy extends Task implements Deployment.Logger {

    private boolean failOnError;

    private boolean startServer;

    public void execute() throws BuildException {

        ClassLoader originalLoader = null;

        try {
            // see issue #62448
            ClassLoader current = (ClassLoader)Lookup.getDefault().lookup(ClassLoader.class);
            if (current == null) {
                current = ClassLoader.getSystemClassLoader();
            }
            if (current != null) {
                originalLoader = Thread.currentThread().getContextClassLoader();
                Thread.currentThread().setContextClassLoader(current);
            }

            J2eeModuleProvider jmp = null;
            try {
                FileObject fob = FileUtil.toFileObject(getProject().getBaseDir());
                fob.refresh(); // without this the "build" directory is not found in filesystems
                jmp = (J2eeModuleProvider) FileOwnerQuery.getOwner(fob).getLookup().lookup(J2eeModuleProvider.class);
            } catch (Exception e) {
                throw new BuildException(e);
            }

            try {
                Deployment.getDefault ().undeploy(jmp, startServer, this);
            } catch (Exception ex) {
                if (failOnError) {
                    throw new BuildException(ex);
                }
            }
        } finally {
            if (originalLoader != null) {
                Thread.currentThread().setContextClassLoader(originalLoader);
            }
        }
    }

    public boolean isFailOnError() {
        return failOnError;
    }

    public void setFailOnError(boolean failOnError) {
        this.failOnError = failOnError;
    }

    public boolean isStartServer() {
        return startServer;
    }

    public void setStartServer(boolean startServer) {
        this.startServer = startServer;
    }

}
