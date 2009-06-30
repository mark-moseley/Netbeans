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

package org.netbeans.modules.apisupport.project.universe;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.junit.Log;
import org.netbeans.junit.RandomlyFails;
import org.netbeans.modules.apisupport.project.TestBase;
import org.openide.util.Mutex;
import org.openide.util.MutexException;

/**
 *
 * @author Richard Michalsky
 */
public class Issue167725DeadlockTest extends TestBase {

    public Issue167725DeadlockTest(String name) {
        super(name);
    }

    @Override
    protected Level logLevel() {
        return Level.FINE;
    }

    @Override
    protected int timeOut() {
        return 20000;
    }
    
    @RandomlyFails // not random, cannot be run in binary dist, requires sources; XXX test against fake platform
    public void testConcurrentScanDeadlock() throws Exception {
        final Logger LOG = Logger.getLogger("org.netbeans.modules.apisupport.project.universe.ModuleList");
        Logger observer = Logger.getLogger("observer");
        Log.enable("org.netbeans.modules.apisupport.project.universe.ModuleList", Level.ALL);
        
        String mt = "THREAD: Test Watch Dog: testConcurrentScanDeadlock MSG:";
        String wt = "THREAD: worker MSG:";
        String order = 
            mt + "beforeFindOrCreateML" +
            wt + "before PM.mutex" +
            wt + "beforeFindOrCreateML" +
            mt + "runProtected: sync 0";
        Log.controlFlow(LOG, observer, order, 0);
        Thread t = new Thread("worker") {

            @Override
            public void run() {
                try {
                    LOG.log(Level.FINE, "before PM.mutex");
                    ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Void>() {
                        public Void run() throws Exception {
                            LOG.log(Level.FINE, "beforeFindOrCreateML");
                            ModuleList.findOrCreateModuleListFromNetBeansOrgSources(nbRootFile());
                            LOG.log(Level.FINE, "afterFindOrCreateML");
                            return null;
                        }
                    });
                } catch (MutexException ex) {
                    throw new RuntimeException(ex);
                }
            }
        };
        t.start();
        LOG.log(Level.FINE, "beforeFindOrCreateML");
        ModuleList.findOrCreateModuleListFromNetBeansOrgSources(nbRootFile());
        LOG.log(Level.FINE, "afterFindOrCreateML");
        t.join();
    }

}