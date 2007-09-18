/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.j2ee.jboss4.test;

import java.io.File;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import javax.enterprise.deploy.shared.ModuleType;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.QueryExp;
import javax.naming.Context;
import javax.naming.InitialContext;
import org.netbeans.api.project.Project;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.junit.ide.ProjectSupport;
import org.netbeans.modules.j2ee.api.ejbjar.EjbProjectConstants;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.deployment.impl.ServerInstance;
import org.netbeans.modules.j2ee.deployment.impl.ServerRegistry;
import org.netbeans.modules.j2ee.deployment.impl.ui.ProgressUI;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.j2ee.jboss4.JBDeploymentFactory;
import org.netbeans.modules.j2ee.jboss4.ide.ui.JBInstantiatingIterator;
import org.netbeans.modules.j2ee.jboss4.ide.ui.JBPluginProperties;
import org.netbeans.modules.j2ee.jboss4.ide.ui.JBPluginUtils;
import org.netbeans.spi.project.ActionProvider;
import org.openide.WizardDescriptor;
import org.openide.WizardDescriptor.Panel;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;

/**
 *
 * @author Michal Mocnak
 */
public class JBoss4TestSuite extends NbTestCase {

    private static final String DISPLAY_NAME = "JBoss Application Server";
    private static final String EJB_PROJECT_NAME = "JBoss4EjbTest";
    private static final String WEB_PROJECT_NAME = "JBoss4WebTest";
    private static final String EJB_PROJECT_PATH = System.getProperty("xtest.tmpdir") + File.separator + EJB_PROJECT_NAME;
    private static final String WEB_PROJECT_PATH = System.getProperty("xtest.tmpdir") + File.separator + WEB_PROJECT_NAME;

    private static String URL = null;

    public JBoss4TestSuite(String testName) {
        super(testName);
    }

    /**
     * suite method automatically generated by JUnit module
     */
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite("JBoss4TestSuite");
        suite.addTest(new JBoss4TestSuite("addJBossDefaultInstance"));
        suite.addTest(new JBoss4TestSuite("startServer"));
        suite.addTest(new JBoss4TestSuite("restartServer"));
        suite.addTest(new JBoss4TestSuite("stopServer"));
        suite.addTest(new JBoss4TestSuite("startDebugServer"));
        suite.addTest(new JBoss4TestSuite("restartServer"));
        suite.addTest(new JBoss4TestSuite("stopServer"));
        suite.addTest(new JBoss4TestSuite("deployWebModule"));
        suite.addTest(new JBoss4TestSuite("stopServer"));
        suite.addTest(new JBoss4TestSuite("deployEjbModule"));
        suite.addTest(new JBoss4TestSuite("stopServer"));
        suite.addTest(new JBoss4TestSuite("removeJBossInstance"));
        return suite;
    }

    public void addJBossInstance(String domain) {
        try {
            String installLocation = System.getProperty("jboss.server.path");
            String host = "localhost";
            String port = "8080";
            String server = domain;
            String serverPath = installLocation + File.separator + "server" + File.separator + domain;
            String displayName = DISPLAY_NAME;

            URL = "jboss-deployer:"+host+":"+port+"#"+domain+"&"+installLocation;

            JBInstantiatingIterator inst = new JBInstantiatingIterator();
            WizardDescriptor wizard = new WizardDescriptor(new Panel[] {inst.current()});
            wizard.putProperty(org.netbeans.modules.j2ee.deployment.impl.ui.wizard.AddServerInstanceWizard.PROP_DISPLAY_NAME, displayName);
            JBPluginProperties.getInstance().setInstallLocation(installLocation);

            inst.setInstallLocation(installLocation);
            inst.setHost(host);
            inst.setPort(port);
            inst.setServer(server);
            inst.setServerPath(serverPath);
            inst.setDeployDir(JBPluginUtils.getDeployDir(serverPath));
            inst.initialize(wizard);
            inst.instantiate();

            ServerRegistry.getInstance().checkInstanceExists(URL);

            sleep();
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    public void addJBossDefaultInstance() {
        addJBossInstance("default");
    }

    public void removeJBossInstance() {
        try {
            sleep();

            ServerInstance inst = ServerRegistry.getInstance().getServerInstance(URL);
            inst.remove();

            try {
                ServerRegistry.getInstance().checkInstanceExists(URL);
            } catch(Exception e) {
                return;
            }

            fail("JBoss instance still exists !");
        } catch(Exception e) {
            fail(e.getMessage());
        }
    }

    public void startServer() {
        try {
            ServerInstance inst = ServerRegistry.getInstance().getServerInstance(URL);

            if(inst.isRunning())
                return;

            ProgressUI pui = new ProgressUI("Start JBoss", true);
            inst.start(pui);

            sleep();

            if(!inst.isRunning())
                throw new Exception("JBoss server start failed");

            sleep();
        } catch(Exception e) {
            fail(e.getMessage());
        }
    }

    public void startDebugServer() {
        try {
            ServerInstance inst = ServerRegistry.getInstance().getServerInstance(URL);

            if(inst.isRunning())
                return;

            ProgressUI ui = new ProgressUI(DISPLAY_NAME, true);
            inst.startDebug(ui);

            sleep();

            if(!inst.isRunning())
                throw new Exception("JBoss4 server start debug failed");

            sleep();
        } catch(Exception e) {
            fail(e.getMessage());
        }
    }

    public void stopServer() {
        try {
            ServerInstance inst = ServerRegistry.getInstance().getServerInstance(URL);

            if(!inst.isRunning())
                return;

            ProgressUI pui = new ProgressUI("Stop JBoss", true);
            inst.stop(pui);

            sleep();

            if(inst.isRunning())
                throw new Exception("JBoss server stop failed");

            sleep();
        } catch(Exception e) {
            fail(e.getMessage());
        }
    }

    public void restartServer() {
        try {
            ServerInstance inst = ServerRegistry.getInstance().getServerInstance(URL);

            if(!inst.isRunning())
                return;

            ProgressUI ui = new ProgressUI(DISPLAY_NAME, true);
            inst.restart(ui);

            sleep();

            if(!inst.isRunning())
                throw new Exception("JBoss4 server stop failed");

            sleep();
        } catch(Exception e) {
            fail(e.getMessage());
        }
    }

    public void deployWebModule() {
        try {
            Project project = (Project)openProject(new File(WEB_PROJECT_PATH));
            ActionProvider ap = (ActionProvider)project.getLookup().lookup(ActionProvider.class);
            J2eeModuleProvider jmp = (J2eeModuleProvider)project.getLookup().lookup(J2eeModuleProvider.class);
            final ServerInstance si = ServerRegistry.getInstance().getServerInstance(jmp.getServerInstanceID());

            Runnable startCondition = new Runnable() {
                public void run() {
                    while(!si.isConnected()) {
                        try {
                            Thread.sleep(5000);
                        } catch(Exception e) {}
                    }
                }
            };

            Runnable deployCondition = new Runnable() {
                public void run() {
                    while(!isProjectDeployed(ModuleType.WAR, WEB_PROJECT_NAME, si.getInstanceProperties())) {
                        try {
                            Thread.sleep(5000);
                        } catch(Exception e) {}
                    }
                }
            };

            Task t = RequestProcessor.getDefault().create(startCondition);
            ap.invokeAction(EjbProjectConstants.COMMAND_REDEPLOY, project.getLookup());
            t.run();
            if(!t.waitFinished(300000))
                throw new Exception("Server start timeout");

            t = RequestProcessor.getDefault().create(deployCondition);
            t.run();
            if(!t.waitFinished(300000))
                throw new Exception("WEB Application deploy timeout");

            closeProject(WEB_PROJECT_NAME);

            sleep();
        } catch(Exception e) {
            fail(e.getMessage());
        }
    }

    public void deployEjbModule() {
        try {
            Project project = (Project)openProject(new File(EJB_PROJECT_PATH));
            ActionProvider ap = (ActionProvider)project.getLookup().lookup(ActionProvider.class);
            J2eeModuleProvider jmp = (J2eeModuleProvider)project.getLookup().lookup(J2eeModuleProvider.class);
            final ServerInstance si = ServerRegistry.getInstance().getServerInstance(jmp.getServerInstanceID());

            Runnable startCondition = new Runnable() {
                public void run() {
                    while(!si.isConnected()) {
                        try {
                            Thread.sleep(5000);
                        } catch(Exception e) {}
                    }
                }
            };

            Runnable deployCondition = new Runnable() {
                public void run() {
                    while(!isProjectDeployed(ModuleType.WAR, WEB_PROJECT_NAME, si.getInstanceProperties())) {
                        try {
                            Thread.sleep(5000);
                        } catch(Exception e) {}
                    }
                }
            };

            Task t = RequestProcessor.getDefault().create(startCondition);
            ap.invokeAction(EjbProjectConstants.COMMAND_REDEPLOY, project.getLookup());
            t.run();
            if(!t.waitFinished(300000))
                throw new Exception("Server start timeout");

            t = RequestProcessor.getDefault().create(deployCondition);
            t.run();
            if(!t.waitFinished(300000))
                throw new Exception("EJB Application deploy timeout");

            closeProject(EJB_PROJECT_NAME);

            sleep();
        } catch(Exception e) {
            fail(e.getMessage());
        }
    }

    public void sleep() {
        try {
            Thread.sleep(10000);
        } catch(Exception e) {
            fail(e.getMessage());
        }
    }

    private Object openProject(File projectDir) {
        return ProjectSupport.openProject(projectDir);
    }

    private void closeProject(String projectName) {
        ProjectSupport.closeProject(projectName);
    }

    private boolean isProjectDeployed(ModuleType mt, String name, InstanceProperties ip) {
        try {
            ClassLoader loader = JBDeploymentFactory.getJBClassLoader(
                    ip.getProperty(JBPluginProperties.PROPERTY_ROOT_DIR),
                    ip.getProperty(JBPluginProperties.PROPERTY_SERVER_DIR));

            ClassLoader original = Thread.currentThread().getContextClassLoader();
            try {
                Thread.currentThread().setContextClassLoader(loader);

                Hashtable env = new Hashtable();

                env.put(Context.INITIAL_CONTEXT_FACTORY, "org.jnp.interfaces.NamingContextFactory");
                env.put(Context.PROVIDER_URL, "jnp://localhost:"+JBPluginUtils.getJnpPort(ip.getProperty(JBPluginProperties.PROPERTY_SERVER_DIR)));
                env.put(Context.OBJECT_FACTORIES, "org.jboss.naming");
                env.put(Context.URL_PKG_PREFIXES, "org.jboss.naming:org.jnp.interfaces" );
                env.put("jnp.disableDiscovery", Boolean.TRUE);

                InitialContext ctx = new InitialContext(env);
                Object server = ctx.lookup("/jmx/invoker/RMIAdaptor");

                ObjectName searchPattern = null;

                if(mt.equals(ModuleType.WAR)) {
                    name += ".war";
                    searchPattern = new ObjectName("jboss.management.local:j2eeType=WebModule,*");
                } else if (mt.equals(ModuleType.EJB)) {
                    name += ".jar";
                    searchPattern = new ObjectName("jboss.management.local:j2eeType=EJBModule,*");
                }

                Set managedObj = (Set)server.getClass().getMethod("queryMBeans", new Class[] {ObjectName.class, QueryExp.class}).invoke(server, new Object[] {searchPattern, null});

                for (Iterator it = managedObj.iterator(); it.hasNext();) {
                    ObjectName elem = ((ObjectInstance) it.next()).getObjectName();
                    if(elem.getKeyProperty("name").equals(name))
                        return true;
                }
            } finally {
                Thread.currentThread().setContextClassLoader(original);
            }
        } catch(Exception e) {
            return false;
        }

        return false;
    }
}