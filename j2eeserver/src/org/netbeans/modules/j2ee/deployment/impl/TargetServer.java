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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.j2ee.deployment.plugins.spi.config.ModuleConfigurationFactory;
import org.openide.filesystems.FileUtil;

import javax.enterprise.deploy.shared.ModuleType;

import org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.deployment.execution.DeploymentTarget;
import org.openide.util.NbBundle;
import org.openide.filesystems.FileObject;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.exceptions.TargetException;
import javax.enterprise.deploy.spi.status.ProgressObject;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeApplication;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.api.ModuleChangeReporter;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeApplicationProvider;
import org.netbeans.modules.j2ee.deployment.execution.ModuleConfigurationProvider;
import org.netbeans.modules.j2ee.deployment.impl.ui.ProgressUI;
import org.netbeans.modules.j2ee.deployment.plugins.api.AppChangeDescriptor;
import org.netbeans.modules.j2ee.deployment.plugins.api.DeploymentChangeDescriptor;
import org.netbeans.modules.j2ee.deployment.plugins.spi.IncrementalDeployment;
import org.netbeans.modules.j2ee.deployment.plugins.spi.config.ModuleConfiguration;
import org.netbeans.modules.j2ee.deployment.plugins.spi.TargetModuleIDResolver;
import org.openide.util.Exceptions;

/**
 * Encapsulates a set of ServerTarget(s), provides a wrapper for deployment
 * help.  This is a throw away object, that get created and used within
 * scope of one deployment execution.
 *
 * Typical user are ServerExecutor and Debugger code, with the following general sequence:
 *
 *      TargetServer ts = new TargetServer(deploymentTarget);
 *      ts.startTargets(deployProgressUI);
 *      TargetModule[] tms = ts.deploy(deployProgressUI);
 *      deploymentTarget.setTargetModules(tms);
 */
public class TargetServer {

    private static final Logger LOGGER = Logger.getLogger(TargetServer.class.getName());

    private Target[] targets;
    private final ServerInstance instance;
    private final DeploymentTarget dtarget;
    private IncrementalDeployment incremental; //null value signifies don't do incremental
    private Map availablesMap = null;
    private Set deployedRootTMIDs = new HashSet(); // type TargetModule
    private Set undeployTMIDs = new HashSet(); // TMID
    private Set distributeTargets = new HashSet(); //Target
    private TargetModule[] redeployTargetModules = null;
    private File application = null;
    private File currentContentDir = null;
    private String contextRoot = null;

    public TargetServer(DeploymentTarget target) {
        this.dtarget = target;
        this.instance = dtarget.getServer().getServerInstance();
    }

    private void init(ProgressUI ui, boolean start, boolean processLast) throws ServerException {
        if (targets == null) {
            if (start) {
                instance.start(ui);
                targets = dtarget.getServer().toTargets();
            } else {
                Set<Target> tempTargets = new HashSet<Target>(Arrays.asList(dtarget.getServer().toTargets()));
                for (Iterator<Target> it = tempTargets.iterator(); it.hasNext();) {
                    Target target = it.next();
                    if (!instance.getStartServer().isRunning(target)) {
                        it.remove();
                    }
                }
                targets = tempTargets.toArray(new Target[tempTargets.size()]);
            }

        }
        incremental = instance.getIncrementalDeployment();
        if (incremental != null && !checkServiceImplementations())
            incremental = null;

        try {
            FileObject contentFO = dtarget.getModule().getContentDirectory();
            if (contentFO != null) {
                currentContentDir = FileUtil.toFile(contentFO);
            }

        } catch (IOException ioe) {
            Logger.getLogger("global").log(Level.INFO, null, ioe);
        }

        J2eeModuleProvider.ConfigSupport configSupport = dtarget.getConfigSupport();
        if (J2eeModule.WAR.equals(dtarget.getModule().getModuleType())) {
            try {
                contextRoot = configSupport.getWebContextRoot();
            } catch (ConfigurationException e) {
                contextRoot = null;
            }
        }

        if (processLast) {
            processLastTargetModules();
        }
    }

    private boolean canFileDeploy(Target[] targetz, J2eeModule deployable) throws IOException {
        if (targetz == null || targetz.length != 1) {
            Logger.getLogger("global").log(Level.INFO, NbBundle.getMessage(TargetServer.class, "MSG_MoreThanOneIncrementalTargets"));
            return false;
        }

        if (deployable == null || null == deployable.getContentDirectory() || !instance.getIncrementalDeployment().canFileDeploy(targetz[0], deployable))
            return false;

        return true;
    }

    private boolean canFileDeploy(TargetModule[] targetModules, J2eeModule deployable) throws IOException {
        if (targetModules == null || targetModules.length != 1) {
            Logger.getLogger("global").log(Level.INFO, NbBundle.getMessage(TargetServer.class, "MSG_MoreThanOneIncrementalTargets"));
            return false;
        }

        if (deployable == null || null == deployable.getContentDirectory() || !instance.getIncrementalDeployment().canFileDeploy(targetModules[0].getTarget(), deployable))
            return false;

        return true;
    }

    private AppChangeDescriptor distributeChanges(TargetModule targetModule, ProgressUI ui) throws IOException {
        ServerFileDistributor sfd = new ServerFileDistributor(instance, dtarget);
        try {
            ui.setProgressObject(sfd);
            ModuleChangeReporter mcr = dtarget.getModuleChangeReporter();
            AppChangeDescriptor acd = sfd.distribute(targetModule, mcr);
            return acd;
        } finally {
            ui.setProgressObject(null);
        }
    }

    private DeploymentChangeDescriptor distributeChangesOnSave(TargetModule targetModule, Iterable<File> artifacts) throws IOException {
        ServerFileDistributor sfd = new ServerFileDistributor(instance, dtarget);
        ModuleChangeReporter mcr = dtarget.getModuleChangeReporter();
        DeploymentChangeDescriptor acd = sfd.distributeOnSave(targetModule, mcr, artifacts);
        return acd;
    }

    private File initialDistribute(Target target, ProgressUI ui) {
        InitialServerFileDistributor sfd = new InitialServerFileDistributor(dtarget, target);
        try {
            ui.setProgressObject(sfd);
            return sfd.distribute();
        } finally {
            ui.setProgressObject(null);
        }
    }

    private boolean checkServiceImplementations() {
        String missing = null;
        if (instance.getServer().getModuleConfigurationFactory() == null) {
            missing = ModuleConfigurationFactory.class.getName();
        }

        if (missing != null) {
            String msg = NbBundle.getMessage(ServerFileDistributor.class, "MSG_MissingServiceImplementations", missing);
            Logger.getLogger("global").log(Level.INFO, msg);
            return false;
        }

        return true;
    }

    // return list of TargetModule to redeploy
    private TargetModule[] checkUndeployForChangedReferences(Set toRedeploy) {
        // PENDING: what are changed references for ejbmod, j2eeapp???
        if (dtarget.getModule().getModuleType() == J2eeModule.WAR) {
            for (Iterator j=toRedeploy.iterator(); j.hasNext();) {
                TargetModule deployed = (TargetModule) j.next();
                File lastContentDir = (deployed.getContentDirectory() == null) ? null : new File(deployed.getContentDirectory());

                // content dir or context root changes since last deploy
                if ((currentContentDir != null && ! currentContentDir.equals(lastContentDir)) ||
                      (contextRoot != null && ! contextRoot.equals(deployed.getContextRoot()))) {

                    distributeTargets.add(deployed.findTarget());
                    undeployTMIDs.add(deployed.delegate());
                    deployed.remove();
                    j.remove();
                }
            }
        }

        return (TargetModule[]) toRedeploy.toArray(new TargetModule[toRedeploy.size()]);
    }

    // return list of target modules to redeploy
    private TargetModule[] checkUndeployForSharedReferences(Target[] toDistribute) {
        Set distSet = new HashSet(Arrays.asList(toDistribute));
        return checkUndeployForSharedReferences(Collections.EMPTY_SET, distSet);
    }
    private TargetModule[] checkUndeployForSharedReferences(Set toRedeploy, Set toDistribute) {
        return checkUndeployForSharedReferences(toRedeploy, toDistribute, null);
    }
    private TargetModule[] checkUndeployForSharedReferences(Set toRedeploy, Set toDistribute, Map queryInfo) {
        // we don't want to undeploy anything when both distribute list and redeploy list are empty
        if (contextRoot == null || (toRedeploy.isEmpty() && toDistribute.isEmpty())) {
            return (TargetModule[]) toRedeploy.toArray(new TargetModule[toRedeploy.size()]);
        }

        Set allTargets = new HashSet(Arrays.asList(TargetModule.toTarget((TargetModule[]) toRedeploy.toArray(new TargetModule[toRedeploy.size()]))));
        allTargets.addAll(toDistribute);
        Target[] targs = (Target[]) allTargets.toArray(new Target[allTargets.size()]);

        boolean shared = false;
        List addToDistributeWhenSharedDetected = new ArrayList();
        List removeFromRedeployWhenSharedDetected = new ArrayList();
        List addToUndeployWhenSharedDetected = new ArrayList();
        List sharerTMIDs;

        TargetModuleIDResolver tmidResolver = instance.getTargetModuleIDResolver();
        if (tmidResolver != null) {
            if (queryInfo == null) {
                queryInfo = new HashMap();
                queryInfo.put(TargetModuleIDResolver.KEY_CONTEXT_ROOT, contextRoot);
            }

            TargetModuleID[] haveSameReferences = TargetModule.EMPTY_TMID_ARRAY;
            if (targs.length > 0) {
                haveSameReferences = tmidResolver.lookupTargetModuleID(queryInfo, targs);
            }
            for (int i=0; i<haveSameReferences.length; i++) {
                haveSameReferences[i] = new TargetModule(keyOf(haveSameReferences[i]), haveSameReferences[i]);
            }
            sharerTMIDs = Arrays.asList(haveSameReferences);

            for (Iterator i=sharerTMIDs.iterator(); i.hasNext();) {
                TargetModule sharer = (TargetModule) i.next();
                if ((toRedeploy.size() > 0 && ! toRedeploy.contains(sharer)) ||
                    toDistribute.contains(sharer.getTarget())) {
                    shared = true;
                    addToUndeployWhenSharedDetected.add(sharer.delegate());
                } else {
                    removeFromRedeployWhenSharedDetected.add(sharer);
                    addToDistributeWhenSharedDetected.add(sharer.getTarget());
                }
            }
        }

        // this is in addition to the above check: TMID provided from tomcat
        // plugin does not have module deployment name element
        if (!shared) {
            sharerTMIDs = TargetModule.findByContextRoot(dtarget.getServer(), contextRoot);
            sharerTMIDs = TargetModule.initDelegate(sharerTMIDs, getAvailableTMIDsMap());

            for (Iterator i=sharerTMIDs.iterator(); i.hasNext();) {
                TargetModule sharer = (TargetModule) i.next();
                boolean redeployHasSharer = false;
                for (Iterator j=toRedeploy.iterator(); j.hasNext();) {
                    TargetModule redeploying = (TargetModule) j.next();
                    if (redeploying.equals(sharer) && redeploying.getContentDirectory().equals(sharer.getContentDirectory())) {
                        redeployHasSharer = true;
                        break;
                    }
                }
                if (! redeployHasSharer ||
                    toDistribute.contains(sharer.getTarget())) {
                        shared = true;
                        addToUndeployWhenSharedDetected.add(sharer.delegate());
                } else {
                    removeFromRedeployWhenSharedDetected.add(sharer);
                    addToDistributeWhenSharedDetected.add(sharer.getTarget());
                }
            }
        }

        if (shared) {
            undeployTMIDs.addAll(addToUndeployWhenSharedDetected);
            //erase memory of them if any
            TargetModule.removeByContextRoot(dtarget.getServer(), contextRoot);
            // transfer from redeploy to distribute
            toRedeploy.removeAll(removeFromRedeployWhenSharedDetected);
            distributeTargets.addAll(addToDistributeWhenSharedDetected);
        }

        return (TargetModule[]) toRedeploy.toArray(new TargetModule[toRedeploy.size()]);
    }

    private Map<String, TargetModuleID> getAvailableTMIDsMap() {
        if (availablesMap != null)
            return availablesMap;

        // existing TMID's
        DeploymentManager dm = instance.getDeploymentManager();
        availablesMap = new HashMap<String, TargetModuleID>();
        try {
            ModuleType type = (ModuleType) dtarget.getModule().getModuleType();
            TargetModuleID[] ids = dm.getAvailableModules(type, targets);
            if (ids == null) {
                return availablesMap;
            }
            for (int i=0; i<ids.length; i++) {
                availablesMap.put(keyOf(ids[i]), ids[i]);
            }
        } catch (TargetException te) {
            IllegalArgumentException illegalArgumentException = new IllegalArgumentException();
            illegalArgumentException.initCause(te);
            throw illegalArgumentException;
        }
        return availablesMap;
    }

    private IncrementalDeployment isModuleImplComplete(J2eeModule deployable) throws IOException {
        // defend against incomplete J2eeModule objects.
        IncrementalDeployment retVal = incremental;
        if (null != retVal && null == deployable.getContentDirectory()) {
            retVal = null;
        }
        if (null != retVal && deployable instanceof J2eeApplication) {
            // make sure all the sub modules will support directory deployment, too
            J2eeModule[] childModules = ((J2eeApplication) deployable).getModules();
            for (int i = 0; i < childModules.length; i++) {
                if (null == childModules[i].getContentDirectory()) {
                    retVal = null;
                }
            }
        }
        return retVal;
    }

    /**
     * Process last deployment TargetModuleID's for undeploy, redistribute, redeploy and oldest timestamp
     */
    private void processLastTargetModules() {
        TargetModule[] targetModules = dtarget.getTargetModules();

        // new module
        if (targetModules == null || targetModules.length == 0) {
            distributeTargets.addAll(Arrays.asList(targets));
            checkUndeployForSharedReferences(targets);
            return;
        }

        Set targetNames = new HashSet();
        for (int i=0; i<targets.length; i++) targetNames.add(targets[i].getName());

        Set toRedeploy = new HashSet(); //type TargetModule
        for (int i=0; i<targetModules.length; i++) {
            // not my module
            if (! targetModules[i].getInstanceUrl().equals(instance.getUrl()) ||
            ! targetNames.contains(targetModules[i].getTargetName()))
                continue;

            TargetModuleID tmID = (TargetModuleID) getAvailableTMIDsMap().get(targetModules[i].getId());

            // no longer a deployed module on server
            if (tmID == null) {
                Target target = targetModules[i].findTarget();
                if (target != null)
                    distributeTargets.add(target);
            } else {
                targetModules[i].initDelegate(tmID);
                toRedeploy.add(targetModules[i]);
            }
        }

        DeploymentManager dm = instance.getDeploymentManager();

        // check if redeploy not suppported and not incremental then transfer to distribute list
        if (incremental == null && getApplication() == null) {
            toRedeploy = Collections.EMPTY_SET;
        } else if (incremental == null) {
            long lastModified = getApplication().lastModified();
            for (Iterator j=toRedeploy.iterator(); j.hasNext();) {
                TargetModule deployed = (TargetModule) j.next();
                if (lastModified >= deployed.getTimestamp()) {
                    //transfer to distribute
                    if (! dm.isRedeploySupported()) {
                        distributeTargets.add(deployed.findTarget());
                        undeployTMIDs.add(deployed.delegate());
                        j.remove();
                    }
                } else {
                    // no need to redeploy
                    j.remove();
                }
            }
        }

        redeployTargetModules = checkUndeployForChangedReferences(toRedeploy);
        Set targetSet = new HashSet(distributeTargets);
        redeployTargetModules = checkUndeployForSharedReferences(toRedeploy, targetSet);
    }

    private File getApplication() {
        if (application != null) return application;
        try {
            FileObject archiveFO = dtarget.getModule().getArchive();
            if (archiveFO == null) return null;
            application = FileUtil.toFile(archiveFO);
            return application;
        } catch (IOException ioe) {
            Logger.getLogger("global").log(Level.SEVERE, ioe.getMessage());
            return null;
        }
    }

    public void startTargets(boolean debugMode, ProgressUI ui) throws ServerException {
        if (instance.getStartServer().isAlsoTargetServer(null)) {
            if (debugMode) {
                instance.startDebug(ui);
            } else {
                instance.start(ui);
            }
            this.targets = dtarget.getServer().toTargets();
            return;
        }
        instance.start(ui);
        this.targets = dtarget.getServer().toTargets();
        if (debugMode) {
            for (int i=0; i<targets.length; i++) {
                instance.startDebugTarget(targets[i], ui);
            }
        } else {
            for (int i=0; i<targets.length; i++) {
                instance.startTarget(targets[i], ui);
            }
        }
    }

    private static String keyOf(TargetModuleID tmid) {
        /*StringBuffer sb =  new StringBuffer(256);
        sb.append(tmid.getModuleID());
        sb.append("@"); //NOI18N
        sb.append(tmid.getTarget().getName());
        return sb.toString();*/
        return tmid.toString();
    }

    //collect root modules into TargetModule with timestamp
    private TargetModuleID[] saveRootTargetModules(TargetModuleID [] modules) {
        long timestamp = System.currentTimeMillis();

        Set originals = new HashSet();
        for (int i=0; i<modules.length; i++) {
            if (modules[i].getParentTargetModuleID() == null) {
                String id = keyOf(modules[i]);
                String targetName = modules[i].getTarget().getName();
                String fromDir = "";
                if (null != currentContentDir)
                    fromDir = currentContentDir.getAbsolutePath();
                TargetModule tm = new TargetModule(id, instance.getUrl(), timestamp, fromDir, contextRoot, modules[i]);
                deployedRootTMIDs.add(tm);
                originals.add(modules[i]);
            }
        }
        return (TargetModuleID[]) originals.toArray(new TargetModuleID[originals.size()]);
    }

    public TargetModule[] deploy(ProgressUI ui, boolean forceRedeploy) throws IOException, ServerException {
        ProgressObject po = null;
        boolean hasActivities = false;

        init(ui, true, true);

        boolean missingModule = false;
        if (ModuleType.EAR.equals(dtarget.getModule().getModuleType())
                && dtarget.getModule() instanceof J2eeApplication
                && redeployTargetModules != null
                && redeployTargetModules.length == 1) {

            // TODO more precise check
            // this is namely because of glassfish deploying EAR without EJB module
            // see gf issue #5240
            missingModule = redeployTargetModules[0].getChildTargetModuleID().length < ((J2eeApplication) dtarget.getModule()).getModules().length;
            if (missingModule) {
                LOGGER.log(Level.INFO, "Enterprise application needs to be redeployed due to missing module");
            }
        }

        if (forceRedeploy || missingModule) {
            if (redeployTargetModules != null) {
                for (int i = 0; i < redeployTargetModules.length; i++) {
                    distributeTargets.add(redeployTargetModules [i].findTarget ());
                    undeployTMIDs.add(redeployTargetModules [i].delegate());
                    redeployTargetModules [i].remove();
                }
                redeployTargetModules = null;
            }
        }

        File plan = null;
        J2eeModule deployable = null;
        ModuleConfigurationProvider mcp = dtarget.getModuleConfigurationProvider();
        if (mcp != null)
            deployable = mcp.getJ2eeModule(null);
        boolean hasDirectory = (dtarget.getModule().getContentDirectory() != null);

        // undeploy if necessary
        if (undeployTMIDs.size() > 0) {
            TargetModuleID[] tmIDs = (TargetModuleID[]) undeployTMIDs.toArray(new TargetModuleID[undeployTMIDs.size()]);
            ui.progress(NbBundle.getMessage(TargetServer.class, "MSG_Undeploying"));
            ProgressObject undeployPO = instance.getDeploymentManager().undeploy(tmIDs);
            try {
                ProgressObjectUtil.trackProgressObject(ui, undeployPO, instance.getDeploymentTimeout()); // lets use the same timeout as for deployment
            } catch (TimedOutException e) {
                // undeployment failed, try to continue anyway
            }
        }

        // handle initial file deployment or distribute
        if (distributeTargets.size() > 0) {
            hasActivities = true;
            Target[] targetz = (Target[]) distributeTargets.toArray(new Target[distributeTargets.size()]);
            IncrementalDeployment lincremental = isModuleImplComplete(deployable);
            if (lincremental != null && hasDirectory && canFileDeploy(targetz, deployable)) {
                ModuleConfiguration cfg = dtarget.getModuleConfigurationProvider().getModuleConfiguration();
                File dir = initialDistribute(targetz[0], ui);
                po = lincremental.initialDeploy(targetz[0], deployable, cfg, dir);
                trackDeployProgressObject(ui, po, false);
            } else {  // standard DM.distribute
                if (getApplication() == null) {
                    throw new RuntimeException(NbBundle.getMessage(TargetServer.class, "MSG_NoArchive"));
                }

                ui.progress(NbBundle.getMessage(TargetServer.class, "MSG_Distributing", application, Arrays.asList(targetz)));
                plan = dtarget.getConfigurationFile();
                po = instance.getDeploymentManager().distribute(targetz, getApplication(), plan);
                trackDeployProgressObject(ui, po, false);
            }
        }

        // handle increment or standard redeploy
        if (redeployTargetModules != null && redeployTargetModules.length > 0) {
            hasActivities = true;
            // defend against incomplete J2eeModule objects.
            IncrementalDeployment lincremental = isModuleImplComplete(deployable);
            if (lincremental != null && hasDirectory && canFileDeploy(redeployTargetModules, deployable)) {
                AppChangeDescriptor acd = distributeChanges(redeployTargetModules[0], ui);
                if (anyChanged(acd)) {
                    ui.progress(NbBundle.getMessage(TargetServer.class, "MSG_IncrementalDeploying", redeployTargetModules[0]));
                    po = lincremental.incrementalDeploy(redeployTargetModules[0].delegate(), acd);
                    trackDeployProgressObject(ui, po, true);

                } else { // return original target modules
                    return dtarget.getTargetModules();
                }
            } else { // standard redeploy
                if (getApplication() == null)
                    throw new IllegalArgumentException(NbBundle.getMessage(TargetServer.class, "MSG_NoArchive"));

                ui.progress(NbBundle.getMessage(TargetServer.class, "MSG_Redeploying", application));
                TargetModuleID[] tmids = TargetModule.toTargetModuleID(redeployTargetModules);
                if (plan == null) plan = dtarget.getConfigurationFile();
                po = instance.getDeploymentManager().redeploy(tmids, getApplication(), plan);
                trackDeployProgressObject(ui, po, false);
            }
        }

        if (hasActivities) {
            return (TargetModule[]) deployedRootTMIDs.toArray(new TargetModule[deployedRootTMIDs.size()]);
        } else {
            return dtarget.getTargetModules();
        }
    }

    /**
     * Inform the plugin about the deploy action, even if there was
     * really nothing needed to be deployed.
     *
     * @param modules list of modules which are being deployed
     */
    public void notifyIncrementalDeployment(TargetModuleID[] modules) {
        if (modules !=  null && incremental != null) {
            for (int i = 0; i < modules.length; i++) {
                incremental.notifyDeployment(modules[i]);
            }
        }
    }

    public static boolean anyChanged(AppChangeDescriptor acd) {
        return (acd.manifestChanged() || acd.descriptorChanged() || acd.classesChanged()
        || acd.ejbsChanged() || acd.serverDescriptorChanged());
    }

    public boolean supportsDeployOnSave(TargetModule[] modules) throws IOException {
        J2eeModule deployable = null;
        ModuleConfigurationProvider deployment = dtarget.getModuleConfigurationProvider();
        if (deployment != null) {
            deployable = deployment.getJ2eeModule(null);
        }

        boolean hasDirectory = (dtarget.getModule().getContentDirectory() != null);
        IncrementalDeployment lincremental = isModuleImplComplete(deployable);
        if (lincremental == null || !hasDirectory || !canFileDeploy(modules, deployable)
                || !lincremental.isDeployOnSaveSupported()) {
            return false;
        }
        return true;
    }

    public CompileOnSaveManager.DeploymentState notifyArtifactsUpdated(
            J2eeModuleProvider provider, Iterable<File> artifacts) {

        if (!dtarget.getServer().getServerInstance().isRunning()) {
            return CompileOnSaveManager.DeploymentState.NOT_DEPLOYED;
        }

        try {
            init(null, false, false);
        } catch (ServerException ex) {
            // this should never occur
            Exceptions.printStackTrace(ex);
        }

        TargetModule[] modules = getDeploymentDirectoryModules();

        try {
            if (!supportsDeployOnSave(modules)) {
                return CompileOnSaveManager.DeploymentState.NOT_DEPLOYED;
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }

        // FIXME target
        TargetModule targetModule = dtarget.getTargetModules()[0];
        if (!targetModule.hasDelegate()) {
            return CompileOnSaveManager.DeploymentState.NOT_DEPLOYED;
        }

        ProgressUI ui = new ProgressUI(NbBundle.getMessage(TargetServer.class, "MSG_DeployOnSave"), false);
        ui.start(Integer.valueOf(0));
        try {
            DeploymentChangeDescriptor changes = distributeChangesOnSave(targetModule, artifacts);
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.log(Level.FINE, changes.toString());
            }
            boolean completed = reloadArtifacts(ui, modules, changes);
            if (!completed) {
                LOGGER.log(Level.INFO, "On save deployment failed");
                return CompileOnSaveManager.DeploymentState.FAILED;
            }
            return CompileOnSaveManager.DeploymentState.UPDATED;
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
            return CompileOnSaveManager.DeploymentState.FAILED;
        } finally {
            ui.finish();
        }
    }

    private boolean reloadArtifacts(ProgressUI ui, TargetModule[] modules, DeploymentChangeDescriptor desc) {
        boolean completed = true;
        for (TargetModule module : modules) {
            ProgressObject obj = incremental.deployOnSave(module.delegate(), desc);
            try {
                // this also save last deploy timestamp
                completed = completed && trackDeployProgressObject(ui, obj, true);
            } catch (ServerException ex) {
                Exceptions.printStackTrace(ex);
                completed = false;
            }
        }
        notifyIncrementalDeployment(modules);

        return completed;
    }

    private TargetModule[] getDeploymentDirectoryModules() {
        TargetModule[] modules = dtarget.getTargetModules();

        ServerInstance serverInstance = dtarget.getServer().getServerInstance();
        Set<String> targetNames = new HashSet<String>();
        for (int i = 0; i < targets.length; i++) {
            targetNames.add(targets[i].getName());
        }

        Set<TargetModule> ret = new HashSet<TargetModule>();
        for (TargetModule module : modules) {
            // not my module
            if (!module.getInstanceUrl().equals(serverInstance.getUrl())
                    || ! targetNames.contains(module.getTargetName())) {
                continue;
            }

            TargetModuleID tmID = (TargetModuleID) getAvailableTMIDsMap().get(module.getId());

            // no longer a deployed module on server
            if (tmID != null) {
                module.initDelegate(tmID);
                ret.add(module);
            }
        }
        return ret.toArray(new TargetModule[ret.size()]);
    }

    /**
     * Waits till the deploy progress object is in final state or till the timeout
     * runs out. If the deploy completes successfully the module will be started
     * if needed.
     *
     * @param ui progress ui which will be notified about progress object changes .
     * @param po progress object which will be tracked.
     * @param incremental is it incremental deploy?
     * @return true if the progress object completed successfully, false otherwise
     */
    private boolean trackDeployProgressObject(ProgressUI ui, ProgressObject po, boolean incremental) throws ServerException {
        long deploymentTimeout = instance.getDeploymentTimeout();
        long startTime = System.currentTimeMillis();
        try {
            boolean completed = ProgressObjectUtil.trackProgressObject(ui, po, deploymentTimeout);
            if (completed) {
                TargetModuleID[] modules = po.getResultTargetModuleIDs();
                modules = saveRootTargetModules(modules);
                if (!incremental) {
                    // if incremental, plugin is responsible for starting module, depending on nature of changes
                    ProgressObject startPO = instance.getDeploymentManager().start(modules);
                    long deployTime = System.currentTimeMillis() - startTime;
                    return ProgressObjectUtil.trackProgressObject(ui, startPO, deploymentTimeout - deployTime);
                }
            }
            return completed;
        } catch (TimedOutException e) {
            throw new ServerException(NbBundle.getMessage(TargetServer.class, "MSG_DeploymentTimeoutExceeded"));
        }
    }
}
