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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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


package org.netbeans.modules.j2ee.deployment.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.enterprise.deploy.shared.factories.DeploymentFactoryManager;
import javax.enterprise.deploy.spi.factories.DeploymentFactory;
import javax.enterprise.deploy.spi.exceptions.DeploymentManagerCreationException;
import org.netbeans.modules.j2ee.deployment.common.api.ValidationException;
import org.netbeans.modules.j2ee.deployment.impl.ui.RegistryNodeProvider;
import org.netbeans.modules.j2ee.deployment.plugins.spi.config.ModuleConfigurationFactory;
import org.openide.util.Lookup;
import org.openide.cookies.InstanceCookie;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import javax.enterprise.deploy.shared.ModuleType;
import javax.enterprise.deploy.spi.DeploymentManager;
import javax.xml.parsers.ParserConfigurationException;
import org.netbeans.modules.j2ee.deployment.impl.gen.nbd.ConfigBean;
import org.netbeans.modules.j2ee.deployment.impl.gen.nbd.NetbeansDeployment;
import org.netbeans.modules.j2ee.deployment.impl.gen.nbd.WebContextRoot;
import org.netbeans.modules.j2ee.deployment.plugins.spi.J2eePlatformFactory;
import org.netbeans.modules.j2ee.deployment.plugins.spi.OptionalDeploymentManagerFactory;
import org.netbeans.modules.j2ee.deployment.plugins.spi.RegistryNodeFactory;
import org.netbeans.modules.j2ee.deployment.plugins.spi.VerifierSupport;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.lookup.Lookups;
import org.xml.sax.SAXException;


public class Server implements Node.Cookie {

    private static final Logger LOGGER = Logger.getLogger(Server.class.getName());

    private static final String ATTR_NEEDS_FIND_SERVER_UI = "needsFindServerUI";

    private final NetbeansDeployment dep;
    private final Class factoryCls;
    private final String name;
    private final Lookup lkp;
    private final boolean needsFindServerUI;

    /** GuardedBy("this") */
    private DeploymentFactory factory = null;
    /** GuardedBy("this") */
    private DeploymentManager manager = null;
    /** GuardedBy("this") */
    private RegistryNodeProvider nodeProvider = null;


    public Server(FileObject fo) throws IOException, ParserConfigurationException,
            SAXException, ClassNotFoundException {

        initDeploymentConfigurationFileList(fo);

        name = fo.getName();
        FileObject descriptor = fo.getFileObject("Descriptor");
        if (descriptor == null) {
            String msg = NbBundle.getMessage(Server.class, "MSG_InvalidServerPlugin", name);
            throw new IllegalStateException(msg);
        }
        needsFindServerUI = getBooleanValue(descriptor.getAttribute(ATTR_NEEDS_FIND_SERVER_UI), false);

        dep = NetbeansDeployment.createGraph(descriptor.getInputStream());

        lkp = Lookups.forPath(fo.getPath());
        factory = lkp.lookup(DeploymentFactory.class);
        if (factory != null) {
            factoryCls = factory.getClass();
        } else {
            FileObject factoryinstance = fo.getFileObject("Factory.instance");
            if (factoryinstance == null) {
                String msg = NbBundle.getMessage(Server.class, "MSG_NoFactoryInstanceClass", name);
                LOGGER.log(Level.SEVERE, msg);
                factoryCls = null;
                return;
            }
            DataObject dobj = DataObject.find(factoryinstance);
            InstanceCookie cookie = (InstanceCookie) dobj.getCookie(InstanceCookie.class);
            if (cookie == null) {
                String msg = NbBundle.getMessage(Server.class, "MSG_FactoryFailed", name);
                LOGGER.log(Level.SEVERE, msg);
                factoryCls = null;
                return;
            }
            factoryCls = cookie.instanceClass();

            // speculative code depending on the DF implementation and if it registers
            // itself with DFM or not

            try {
                factory = (DeploymentFactory) cookie.instanceCreate();
            } catch (Exception e) {
                LOGGER.log(Level.INFO, null, e);
            }
        }
    }

    private synchronized DeploymentFactory getFactory() {
        if (factory == null) {

            DeploymentFactory[] factories = DeploymentFactoryManager.getInstance().getDeploymentFactories();
            for (int i = 0; i < factories.length; i++) {
                if (factoryCls.isInstance(factories[i])) {
                    factory = factories[i];
                    break;
                }
            }
        }
        if (factory == null) {
            throw new IllegalStateException("Can't acquire DeploymentFacory"); //NOI18N
        }
        return factory;
    }

    public synchronized DeploymentManager getDisconnectedDeploymentManager()
            throws DeploymentManagerCreationException  {

        if(manager == null) {
            manager = getDisconnectedDeploymentManager(dep.getDisconnectedString());
        }
        return manager;
    }

    public DeploymentManager getDisconnectedDeploymentManager(String uri) throws DeploymentManagerCreationException {
        return getFactory().getDisconnectedDeploymentManager(uri);
    }

    public boolean handlesUri(String uri) {
        try {
            return getFactory().handlesURI(uri);
        } catch (Exception e) {
            LOGGER.log(Level.INFO, null, e);
            return false;
        }
    }

    public DeploymentManager getDeploymentManager(String uri, String username, String password)
            throws DeploymentManagerCreationException {
        return getFactory().getDeploymentManager(uri, username, password);
    }

    public String getDisplayName() {
        return getFactory().getDisplayName();
    }

    public String getShortName() {
        return name;
    }

    public String getIconBase() {
        return dep.getIcon();
    }

    public boolean canDeployEars() {
        return dep.getContainerLimitation() == null || dep.getContainerLimitation().isEarDeploy();
    }

    public boolean canDeployWars() {
        return dep.getContainerLimitation() == null || dep.getContainerLimitation().isWarDeploy();
    }

    public boolean canDeployEjbJars() {
        return dep.getContainerLimitation() == null || dep.getContainerLimitation().isEjbjarDeploy();
    }

    // PENDING should be cached?
    public String getHelpId(String beanClass) {
        ConfigBean[] beans = dep.getConfigBean();
        for(int i = 0; i < beans.length; i++) {
            if(beans[i].getClassName().equals(beanClass)) {
                return beans[i].getHelpid();
            }
        }
        return null;
    }

    public synchronized RegistryNodeProvider getNodeProvider() {
        if (nodeProvider != null) {
            return nodeProvider;
        }

        RegistryNodeFactory nodeFact = (RegistryNodeFactory) lkp.lookup(RegistryNodeFactory.class);
        if (nodeFact == null) {
            String msg = NbBundle.getMessage(Server.class, "MSG_NoInstance", name, RegistryNodeFactory.class);
            LOGGER.log(Level.INFO, msg);
        }
        nodeProvider = new RegistryNodeProvider(nodeFact); //null is acceptable
        return nodeProvider;
    }

    public RegistryNodeFactory getRegistryNodeFactory() {
        return (RegistryNodeFactory) lkp.lookup(RegistryNodeFactory.class);
    }

    /** returns OptionalDeploymentManagerFactory or null it is not provided by the plugin */
    public OptionalDeploymentManagerFactory getOptionalFactory () {
        OptionalDeploymentManagerFactory o = (OptionalDeploymentManagerFactory)
                lkp.lookup(OptionalDeploymentManagerFactory.class);
        return o;
    }

    /** returns J2eePlatformFactory or null if it is not provided by the plugin */
    public J2eePlatformFactory getJ2eePlatformFactory () {
        J2eePlatformFactory o = (J2eePlatformFactory) lkp.lookup(J2eePlatformFactory.class);
        return o;
    }

    public ModuleConfigurationFactory getModuleConfigurationFactory() {
        return lkp.lookup(ModuleConfigurationFactory.class);
    }

    public VerifierSupport getVerifierSupport() {
        VerifierSupport vs = (VerifierSupport) lkp.lookup(VerifierSupport.class);
        return vs;
    }

    public boolean canVerify(Object moduleType) {
        VerifierSupport vs = getVerifierSupport();
        return  vs != null && vs.supportsModuleType(moduleType);
    }

    public void verify(FileObject target, OutputStream logger) throws ValidationException {
        getVerifierSupport().verify(target, logger);
    }

    public ServerInstance[] getInstances() {
        Collection ret = new ArrayList();
        for (Iterator i=ServerRegistry.getInstance().getInstances().iterator(); i.hasNext();) {
            ServerInstance inst = (ServerInstance) i.next();
            if (name.equals(inst.getServer().getShortName())) {
                ret.add(inst);
            }
        }
        return (ServerInstance[]) ret.toArray(new ServerInstance[ret.size()]);
    }

    public WebContextRoot getWebContextRoot() {
        return dep.getWebContextRoot();
    }

    public DeploymentFactory getDeploymentFactory() {
        return getFactory();
    }

    private static boolean getBooleanValue(Object v, boolean dvalue) {
        if (v instanceof Boolean)
            return ((Boolean)v).booleanValue();
        if (v instanceof String)
            return Boolean.valueOf((String) v).booleanValue();
        return dvalue;
    }

    public boolean needsFindServerUI() {
        return needsFindServerUI;
    }

    @Override
    public String toString() {
        return getShortName ();
    }

    public boolean supportsModuleType(ModuleType type) {
        if (J2eeModule.WAR.equals(type)) {
            return this.canDeployWars();
        } else if (J2eeModule.EJB.equals(type)) {
            return this.canDeployEjbJars();
        } else if (J2eeModule.EAR.equals(type)) {
            return this.canDeployEars();
        } else {
            // PENDING, precise answer for other module types, for now assume true
            return true;
        }
    }

    private static final String LAYER_DEPLOYMENT_FILE_NAMES = "DeploymentFileNames"; //NOI18N
    private Map deployConfigDescriptorMap;

    private final void initDeploymentConfigurationFileList(FileObject fo) {
        deployConfigDescriptorMap = new HashMap();
        FileObject deplFNames = fo.getFileObject(LAYER_DEPLOYMENT_FILE_NAMES);
        if (deplFNames != null) {
            FileObject mTypes [] = deplFNames.getChildren();
            for (int j = 0; j < mTypes.length; j++) {
                String mTypeName = mTypes[j].getName().toUpperCase();
                FileObject allNames[] = mTypes[j].getChildren();
                if (allNames == null || allNames.length == 0) {
                    continue;
                }
                ArrayList filepaths = new ArrayList();
                for (int i = 0; i < allNames.length; i++) {
                    if (allNames[i] == null) {
                        continue;
                    }
                    String fname = allNames[i].getNameExt();
                    filepaths.add(fname.replace('\\', '/')); //just in case..
                }
                deployConfigDescriptorMap.put(mTypeName, filepaths.toArray(new String[filepaths.size()]));
            }
        }
    }

    public String[] getDeploymentPlanFiles(Object type) {
        return (String[]) deployConfigDescriptorMap.get(type.toString().toUpperCase());
    }
}
