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

package org.netbeans.modules.glassfish.javaee.test;

import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.glassfish.common.GlassfishInstanceProvider;
//import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;
import org.netbeans.modules.glassfish.spi.GlassfishModule;
import org.netbeans.modules.j2ee.deployment.impl.ServerInstance;
import org.netbeans.modules.j2ee.deployment.impl.ServerRegistry;
import org.netbeans.modules.j2ee.deployment.impl.ui.ProgressUI;

/**
 *
 * @author davisn9
 */
public class StartStopServer extends NbTestCase {
    
    private final int SLEEP = 10000;
    
    public StartStopServer(String testName) {
        super(testName);
    }


    public void startPreludeServer() {
        try {
            GlassfishInstanceProvider gip = GlassfishInstanceProvider.getPrelude();
            ServerInstance inst = ServerRegistry.getInstance().getServerInstance(gip.formatUri(Util._PRELUDE_LOCATION, Util._HOST, 4848));

            if(inst.isRunning())
                return;

            ProgressUI pui = new ProgressUI("Start Prelude V3", true);
            inst.start(pui);

            Util.sleep(SLEEP);

            if(!inst.isRunning())
                throw new Exception("Prelude V3 server start failed");

            if (inst.isDebuggable(null))
                fail("Server started in debug... it should not have done that");

            Util.sleep(SLEEP);
        } catch(Exception e) {
            fail(e.getMessage());
        }
    }


    public void stopPreludeServer() {
        try {
            GlassfishInstanceProvider gip = GlassfishInstanceProvider.getPrelude();
            ServerInstance inst = ServerRegistry.getInstance().getServerInstance(gip.formatUri(Util._PRELUDE_LOCATION, Util._HOST, 4848));

            if(!inst.isRunning())
                return;

            ProgressUI pui = new ProgressUI("Stop Prelude V3", true);
            inst.stop(pui);

            Util.sleep(SLEEP);

            if(inst.isRunning())
                throw new Exception("Prelude V3 server stop failed");

            Util.sleep(SLEEP);
        } catch(Exception e) {
            fail(e.getMessage());
        }
    }

    public void restartPreludeServer() {
        try {
            GlassfishInstanceProvider gip = GlassfishInstanceProvider.getPrelude();
            ServerInstance inst = ServerRegistry.getInstance().getServerInstance(gip.formatUri(Util._PRELUDE_LOCATION, "localhost", 4848));

            if(!inst.isRunning())
                return;

            ProgressUI pui = new ProgressUI("Restart Prelude", true);
            inst.restart(pui);

            Util.sleep(SLEEP);

            if(!inst.isRunning())
                throw new Exception("Prelude server restart failed");

            Util.sleep(SLEEP);
        } catch(Exception e) {
            fail(e.getMessage());
        }
    }

     public void startDebugPreludeServer() {
        try {
            GlassfishInstanceProvider gip = GlassfishInstanceProvider.getPrelude();
            ServerInstance inst = ServerRegistry.getInstance().getServerInstance(gip.formatUri(Util._PRELUDE_LOCATION, "localhost", 4848));

            if(inst.isRunning())
                return;

            ProgressUI pui = new ProgressUI("Start Debug Prelude", true);
            inst.startDebug(pui);

            Util.sleep(SLEEP);

            if(!inst.isRunning())
                throw new Exception("Prelude server start debug failed");

            if (!inst.isDebuggable(null))
                fail("server isn't debuggable...");

            Util.sleep(SLEEP);
        } catch(Exception e) {
            fail(e.getMessage());
        }
    }



    public void startV3Server() {
        try {
            GlassfishInstanceProvider gip = GlassfishInstanceProvider.getEe6();
            ServerInstance inst = ServerRegistry.getInstance().getServerInstance(gip.formatUri(Util._V3_LOCATION, Util._HOST, 4848));

            if(inst.isRunning())
                return;

            org.netbeans.api.server.ServerInstance si = gip.getInstances().get(0);
             ((GlassfishModule) si.getBasicNode().getLookup().lookup(GlassfishModule.class)).setEnvironmentProperty(GlassfishModule.JAVA_PLATFORM_ATTR,
                     System.getProperty("v3.server.javaExe"), true);

            ProgressUI pui = new ProgressUI("Start GlassFish V3", true);
            inst.start(pui);

            Util.sleep(SLEEP);

            if(!inst.isRunning())
                throw new Exception("GlassFish V3 server start failed");

            if (inst.isDebuggable(null))
                fail("Server started in debug... it should not have done that");

            Util.sleep(SLEEP);
        } catch(Exception e) {
            fail(e.getMessage());
        }
    }

    public void stopV3Server() {
        try {
            GlassfishInstanceProvider gip = GlassfishInstanceProvider.getEe6();
            ServerInstance inst = ServerRegistry.getInstance().getServerInstance(gip.formatUri(Util._V3_LOCATION, Util._HOST, 4848));

            if(!inst.isRunning())
                return;

            ProgressUI pui = new ProgressUI("Stop GlassFish V3", true);
            inst.stop(pui);

            Util.sleep(SLEEP);

            if(inst.isRunning())
                throw new Exception("GlassFish V3 server stop failed");

            Util.sleep(SLEEP);
        } catch(Exception e) {
            fail(e.getMessage());
        }
    }



    


    public void restartV3Server() {
        try {
            GlassfishInstanceProvider gip = GlassfishInstanceProvider.getEe6();
            ServerInstance inst = ServerRegistry.getInstance().getServerInstance(gip.formatUri(Util._V3_LOCATION, "localhost", 4848));

            if(!inst.isRunning())
                return;

            ProgressUI pui = new ProgressUI("Restart GlassFish V3", true);
            inst.restart(pui);

            Util.sleep(SLEEP);

            if(!inst.isRunning())
                throw new Exception("GlassFish server restart failed");

            Util.sleep(SLEEP);
        } catch(Exception e) {
            fail(e.getMessage());
        }
    }

    public void startDebugV3Server() {
        try {
            ServerInstance inst = ServerRegistry.getInstance().getServerInstance(Util._V3_LOCATION);

            if(inst.isRunning())
                return;

            ProgressUI pui = new ProgressUI("Start Debug GlassFIsh V3", true);
            inst.startDebug(pui);

            Util.sleep(SLEEP);

            if(!inst.isRunning())
                throw new Exception("GlassFish V3 server start debug failed");

            if (!inst.isDebuggable(null))
                fail("server isn't debuggable...");

            Util.sleep(SLEEP);
        } catch(Exception e) {
            fail(e.getMessage());
        }
    }

    
}
