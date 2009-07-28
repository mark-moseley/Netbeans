/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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

package org.netbeans.modules.autoupdate.services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.Module;
import org.netbeans.ModuleManager;
import org.netbeans.api.autoupdate.UpdateElement;
import org.netbeans.api.autoupdate.UpdateUnit;
import org.openide.modules.Dependency;
import org.openide.modules.ModuleInfo;

/**
 *
 * @author Radek Matous, Jiri Rechtacek
 */
abstract class OperationValidator {
    private final static OperationValidator FOR_INSTALL = new InstallValidator();
    private final static OperationValidator FOR_UNINSTALL = new UninstallValidator();
    private final static OperationValidator FOR_UPDATE = new UpdateValidator();
    private final static OperationValidator FOR_ENABLE = new EnableValidator();
    private final static OperationValidator FOR_DISABLE = new DisableValidator();
    private final static OperationValidator FOR_CUSTOM_INSTALL = new CustomInstallValidator();
    private final static OperationValidator FOR_CUSTOM_UNINSTALL = new CustomUninstallValidator();
    private static final Logger LOGGER = Logger.getLogger (OperationValidator.class.getName ());
    
    /** Creates a new instance of OperationValidator */
    private OperationValidator() {}
    
    public static boolean isValidOperation(OperationContainerImpl.OperationType type, UpdateUnit updateUnit, UpdateElement updateElement) {
        if (updateUnit.isPending ()) {
            return false;
        }
        boolean isValid = false;
        switch(type){
        case INSTALL:
            isValid = FOR_INSTALL.isValidOperationImpl(updateUnit, updateElement);
            break;
        case DIRECT_UNINSTALL:
        case UNINSTALL:
            isValid = FOR_UNINSTALL.isValidOperationImpl(updateUnit, updateElement);
            break;
        case UPDATE:
            isValid = FOR_UPDATE.isValidOperationImpl(updateUnit, updateElement);
            break;
        case ENABLE:
            isValid = FOR_ENABLE.isValidOperationImpl(updateUnit, updateElement);
            break;
        case DIRECT_DISABLE:
        case DISABLE:
            isValid = FOR_DISABLE.isValidOperationImpl(updateUnit, updateElement);
            break;
        case CUSTOM_INSTALL:
            isValid = FOR_CUSTOM_INSTALL.isValidOperationImpl(updateUnit, updateElement);
            break;
        case CUSTOM_UNINSTALL:
            isValid = FOR_CUSTOM_UNINSTALL.isValidOperationImpl(updateUnit, updateElement);
            break;
        default:
            assert false;
        }
        return isValid;
    }
    
    public static List<UpdateElement> getRequiredElements (OperationContainerImpl.OperationType type,
            UpdateElement updateElement,
            List<ModuleInfo> moduleInfos,
            Collection<String> brokenDependencies) {
        List<UpdateElement> retval = Collections.emptyList ();
        switch(type){
        case INSTALL:
            retval = FOR_INSTALL.getRequiredElementsImpl(updateElement, moduleInfos, brokenDependencies);
            break;
        case DIRECT_UNINSTALL:
        case UNINSTALL:
            retval = FOR_UNINSTALL.getRequiredElementsImpl(updateElement, moduleInfos, brokenDependencies);
            break;
        case UPDATE:
            retval = FOR_UPDATE.getRequiredElementsImpl(updateElement, moduleInfos, brokenDependencies);
            break;
        case ENABLE:
            retval = FOR_ENABLE.getRequiredElementsImpl(updateElement, moduleInfos, brokenDependencies);
            break;
        case DIRECT_DISABLE:
        case DISABLE:
            retval = FOR_DISABLE.getRequiredElementsImpl(updateElement, moduleInfos, brokenDependencies);
            break;
        case CUSTOM_INSTALL:
            retval = FOR_CUSTOM_INSTALL.getRequiredElementsImpl(updateElement, moduleInfos, brokenDependencies);
            break;
        case CUSTOM_UNINSTALL:
            retval = FOR_CUSTOM_UNINSTALL.getRequiredElementsImpl(updateElement, moduleInfos, brokenDependencies);
            break;
        default:
            assert false;
        }
        if (LOGGER.isLoggable (Level.FINE)) {
            LOGGER.log (Level.FINE, "== do getRequiredElements for " + type + " of " + updateElement + " ==");
            for (UpdateElement el : retval) {
                LOGGER.log (Level.FINE, "--> " + el);
            }
            LOGGER.log (Level.FINE, "== done. ==");
        }
        return retval;
    }
    
    public static Set<String> getBrokenDependencies (OperationContainerImpl.OperationType type,
            UpdateElement updateElement,
            List<ModuleInfo> moduleInfos) {
            Set<String> broken = new HashSet<String> ();
            switch (type) {
            case ENABLE :
                broken = Utilities.getBrokenDependenciesInInstalledModules (updateElement);
                break;
            case INSTALL :
            case UPDATE :
                getRequiredElements (type, updateElement, moduleInfos, broken);
                break;
            case UNINSTALL :
            case DIRECT_UNINSTALL :
            case CUSTOM_UNINSTALL :
            case DISABLE :
            case DIRECT_DISABLE :
            case CUSTOM_INSTALL:
                broken = Utilities.getBrokenDependencies (updateElement, moduleInfos);
                break;
            default:
                assert false : "Unknown type of operation " + type;
            }
            return broken;
    }
    
    abstract boolean isValidOperationImpl(UpdateUnit updateUnit, UpdateElement uElement);
    abstract List<UpdateElement> getRequiredElementsImpl (UpdateElement uElement,
            List<ModuleInfo> moduleInfos,
            Collection<String> brokenDependencies);
    
    private static class InstallValidator extends OperationValidator {
        boolean isValidOperationImpl(UpdateUnit unit, UpdateElement uElement) {
            return unit.getInstalled() == null && containsElement (uElement, unit);
        }
        
        List<UpdateElement> getRequiredElementsImpl (UpdateElement uElement, List<ModuleInfo> moduleInfos, Collection<String> brokenDependencies) {
            Set<Dependency> brokenDeps = new HashSet<Dependency> ();
            List<UpdateElement> res = new LinkedList<UpdateElement> (Utilities.findRequiredUpdateElements (uElement, moduleInfos, brokenDeps));
            if (brokenDependencies != null) {
                for (Dependency dep : brokenDeps) {
                    brokenDependencies.add (dep.toString ());
                }
            }
            return res;
        }
    }
    
    private static Map<Module, Set<Module>> module2depending = new HashMap<Module, Set<Module>> ();
    private static Map<Module, Set<Module>> module2required = new HashMap<Module, Set<Module>> ();
    
    public static void clearMaps () {
        module2depending = new HashMap<Module, Set<Module>> ();
        module2required = new HashMap<Module, Set<Module>> ();
    }
    
    private static class UninstallValidator extends OperationValidator {
        
        boolean isValidOperationImpl(UpdateUnit unit, UpdateElement uElement) {
            return unit.getInstalled() != null && isValidOperationImpl (Trampoline.API.impl (uElement));
        }
        
        private boolean isValidOperationImpl (UpdateElementImpl impl) {
            boolean res = false;
            switch (impl.getType ()) {
            case KIT_MODULE :
            case MODULE :
                Module m =  Utilities.toModule (((ModuleUpdateElementImpl) impl).getModuleInfo ());
                res = ModuleDeleterImpl.getInstance ().canDelete (m);
                break;
            case STANDALONE_MODULE :
            case FEATURE :
                for (ModuleInfo info : ((FeatureUpdateElementImpl) impl).getModuleInfos ()) {
                    Module module = Utilities.toModule (info);
                    res |= ModuleDeleterImpl.getInstance ().canDelete (module);
                }
                break;
            case CUSTOM_HANDLED_COMPONENT :
                LOGGER.log (Level.INFO, "CUSTOM_HANDLED_COMPONENT doesn't support custom uninstaller yet."); // XXX
                res = false;
                break;
            default:
                assert false : "Not supported for impl " + impl;
            }
            return res;
        }
        
        List<UpdateElement> getRequiredElementsImpl  (UpdateElement uElement, List<ModuleInfo> moduleInfos, Collection<String> brokenDependencies) {
            ModuleManager mm = null;
            final Set<Module> modules = new LinkedHashSet<Module>();
            for (ModuleInfo moduleInfo : moduleInfos) {
                Module m = Utilities.toModule (moduleInfo);
                if (m == null) {
                    continue;
                }
                if (! Utilities.isEssentialModule (m)) {
                    modules.add (m);
                }
                if (mm == null) {
                    mm = m.getManager();
                }
            }
            Set<UpdateElement> retval = new HashSet<UpdateElement>();
            if (mm != null) {
                Set<Module> toUninstall = findRequiredModulesForDeactivate (modules, mm);
                toUninstall.removeAll (modules);
                for (Module module : toUninstall) {
                    if (Utilities.isEssentialModule (module)) {
                        LOGGER.log (Level.WARNING, "Essential module cannot be planned for uninstall but " + module);
                        continue;
                    } else if (! ModuleDeleterImpl.getInstance ().canDelete (module)) {
                        LOGGER.log (Level.WARNING, "The module " + module + " cannot be planned for uninstall because is read-only.");
                        continue;
                    }
                    // !!! e.g. applemodule can be found in the list for uninstall but has UpdateUnit nowhere else MacXOS
                    UpdateUnit unit = Utilities.toUpdateUnit (module);
                    if (unit != null) {
                        retval.add (unit.getInstalled ());
                    }
                }
            }
            return new ArrayList<UpdateElement> (retval);
        }
        
    }
    
    private static class UpdateValidator extends OperationValidator {
        boolean isValidOperationImpl(UpdateUnit unit, UpdateElement uElement) {
            return unit.getInstalled() != null && containsElement (uElement, unit);
        }
        
        List<UpdateElement> getRequiredElementsImpl (UpdateElement uElement, List<ModuleInfo> moduleInfos, Collection<String> brokenDependencies) {
             return FOR_INSTALL.getRequiredElementsImpl(uElement, moduleInfos, brokenDependencies);
        }
    }
    
    private static boolean containsElement (UpdateElement el, UpdateUnit unit) {
        return unit.getAvailableUpdates ().contains (el);
    }
    
    private static class EnableValidator extends OperationValidator {
        boolean isValidOperationImpl(UpdateUnit unit, UpdateElement uElement) {
            return unit.getInstalled () != null && isValidOperationImpl (Trampoline.API.impl (uElement));
        }
        
        private boolean isValidOperationImpl (UpdateElementImpl impl) {
            boolean res = false;
            switch (impl.getType ()) {
            case KIT_MODULE :
            case MODULE :
                Module module = Utilities.toModule (((ModuleUpdateElementImpl) impl).getModuleInfo ());
                res = Utilities.canEnable (module);
                break;
            case STANDALONE_MODULE :
            case FEATURE :
                for (ModuleInfo info : ((FeatureUpdateElementImpl) impl).getModuleInfos ()) {
                    Module m =  Utilities.toModule (info);
                    res |= Utilities.canEnable (m);
                }
                break;
            case CUSTOM_HANDLED_COMPONENT :
                res = false;
                break;
            default:
                assert false : "Not supported for impl " + impl;
            }
            return res;
        }
        
        private List<Module> getModulesToEnable(ModuleManager mm, final Set<Module> modules) {
            List<Module> toEnable = new ArrayList<Module>();
            boolean stateChanged = true;
            while (stateChanged) {
                stateChanged = false;
                try {
                    toEnable = mm.simulateEnable(modules);
                } catch (IllegalArgumentException e) {
                    //#160500
                    LOGGER.log(Level.INFO, "Cannot enable all modules " + modules, e);
                    Set<Module> tempModules = new LinkedHashSet<Module>(modules);
                    for (Module module : tempModules) {
                        if (!Utilities.canEnable(module)) {
                            modules.remove(module);
                            stateChanged = true;
                        }
                    }
                    assert stateChanged : "Can`t enable modules " + modules;
                }
            }
            return toEnable;
        }

        List<UpdateElement> getRequiredElementsImpl (UpdateElement uElement, List<ModuleInfo> moduleInfos, Collection<String> brokenDependencies) {
            ModuleManager mm = null;
            final Set<Module> modules = new LinkedHashSet<Module>();
            for (ModuleInfo moduleInfo : moduleInfos) {
                Module m = Utilities.toModule (moduleInfo);
                if (Utilities.canEnable (m)) {
                    modules.add(m);
                }
                if (mm == null) {
                    mm = m.getManager();
                }
            }
            List<UpdateElement> retval = new ArrayList<UpdateElement>();
            if (mm != null) {
                List<Module> toEnable = getModulesToEnable(mm, modules);
                for (Module module : toEnable) {
                    if (!modules.contains(module) && Utilities.canEnable (module)) {
                        retval.add(Utilities.toUpdateUnit(module).getInstalled());
                    }
                }
            }
            return retval;
        }
    }
    
    private static class DisableValidator extends OperationValidator {
        boolean isValidOperationImpl(UpdateUnit unit, UpdateElement uElement) {
            return unit.getInstalled () != null && isValidOperationImpl (Trampoline.API.impl (uElement));
        }
        
        private boolean isValidOperationImpl (UpdateElementImpl impl) {
            boolean res = false;
            switch (impl.getType ()) {
            case KIT_MODULE :
            case MODULE :
                Module module = Utilities.toModule (((ModuleUpdateElementImpl) impl).getModuleInfo ());
                res = Utilities.canDisable (module);
                break;
            case STANDALONE_MODULE :
            case FEATURE :
                for (ModuleInfo info : ((FeatureUpdateElementImpl) impl).getModuleInfos ()) {
                    Module m =  Utilities.toModule (info);
                    res |= Utilities.canDisable (m);
                }
                break;
            case CUSTOM_HANDLED_COMPONENT :
                res = false;
                break;
            default:
                assert false : "Not supported for impl " + impl;
            }
            return res;
        }
        
        List<UpdateElement> getRequiredElementsImpl (UpdateElement uElement, List<ModuleInfo> moduleInfos, Collection<String> brokenDependencies) {
            ModuleManager mm = null;
            final Set<Module> modules = new LinkedHashSet<Module>();
            for (ModuleInfo moduleInfo : moduleInfos) {
                Module m = Utilities.toModule (moduleInfo);
                if (Utilities.canDisable (m)) {
                    modules.add(m);
                }
                if (mm == null) {
                    mm = m.getManager();
                }
            }
            
            if (mm == null) {
                LOGGER.log (Level.WARNING, "No modules can be disabled when disabling UpdateElement " + uElement);
                return Collections.emptyList ();
            } 
            
            Set<Module> requestedToDisable = findRequiredModulesForDeactivate (modules, mm);

            List<Module> toDisable = mm.simulateDisable (modules);
            boolean wasAdded = requestedToDisable.addAll (toDisable);
            
            // XXX why sometimes happens that no all module for deactivated found?
            // assert ! wasAdded : "The requestedToDisable cannot be enlarged by " + toDisable;
            if (LOGGER.isLoggable (Level.FINE) && wasAdded) {
                toDisable.removeAll (filterCandidatesToDeactivate (modules, requestedToDisable, mm));
                LOGGER.log (Level.FINE, "requestedToDisable was enlarged by " + toDisable);
            }
            
            Set<UpdateElement> retval = new HashSet<UpdateElement> ();
            for (Module module : requestedToDisable) {
                if (! modules.contains (module) && Utilities.canDisable (module)) {
                    // !!! e.g. applemodule can be found in the list for uninstall but has UpdateUnit nowhere else MacXOS
                    UpdateUnit unit = Utilities.toUpdateUnit (module);
                    if (unit != null) {
                        retval.add (unit.getInstalled ());
                    }
                }
            }
            
            return new ArrayList<UpdateElement> (retval);
        }
    }
    
    private static class CustomInstallValidator extends OperationValidator {
        boolean isValidOperationImpl (UpdateUnit unit, UpdateElement uElement) {
            boolean res = false;
            UpdateElementImpl impl = Trampoline.API.impl (uElement);
            assert impl != null;
            if (impl != null && impl instanceof NativeComponentUpdateElementImpl) {
                NativeComponentUpdateElementImpl ni = (NativeComponentUpdateElementImpl) impl;
                if (ni.getInstallInfo ().getCustomInstaller () != null) {
                    res = containsElement (uElement, unit);
                }
            }
            return res;
        }

        List<UpdateElement> getRequiredElementsImpl (UpdateElement uElement, List<ModuleInfo> moduleInfos, Collection<String> brokenDependencies) {
            LOGGER.log (Level.INFO, "CustomInstallValidator doesn't care about required elements."); // XXX
            return Collections.emptyList ();
        }
    }

    private static class CustomUninstallValidator extends OperationValidator {
        boolean isValidOperationImpl (UpdateUnit unit, UpdateElement uElement) {
            boolean res = false;
            UpdateElementImpl impl = Trampoline.API.impl (uElement);
            assert impl != null;
            if (impl != null && impl instanceof NativeComponentUpdateElementImpl) {
                NativeComponentUpdateElementImpl ni = (NativeComponentUpdateElementImpl) impl;
                res = ni.getNativeItem ().getUpdateItemDeploymentImpl ().getCustomUninstaller () != null;
            }
            return res;
        }

        List<UpdateElement> getRequiredElementsImpl (UpdateElement uElement, List<ModuleInfo> moduleInfos, Collection<String> brokenDependencies) {
            LOGGER.log (Level.INFO, "CustomUninstallValidator doesn't care about required elements."); // XXX
            return Collections.emptyList ();
            //return new LinkedList<UpdateElement> (Utilities.findRequiredUpdateElements (uElement, moduleInfos));
        }
    }

    private static Set<Module> findRequiredModulesForDeactivate (final Set<Module> requestedToDeactivate, ModuleManager mm) {
        // go up and find kits which depending on requestedToDeactivate modules
        Set<Module> extendReqToDeactivate = new HashSet<Module> (requestedToDeactivate);
        boolean inscreasing = true;
        while (inscreasing) {
            inscreasing = false;
            Set<Module> tmp = new HashSet<Module> (extendReqToDeactivate);
            for (Module dep : tmp) {
                Set<Module> deps = Utilities.findDependingModules (dep, mm, module2depending);
                inscreasing |= extendReqToDeactivate.addAll (deps);
            }
        }

        // go down and find all modules (except for other kits) which can be deactivated
        Set<Module> moreToDeactivate = new HashSet<Module> (extendReqToDeactivate);
        inscreasing = true;
        while (inscreasing) {
            inscreasing = false;
            Set<Module> tmp = new HashSet<Module> (moreToDeactivate);
            for (Module req : tmp) {
                if ((! Utilities.isKitModule (req) && ! Utilities.isEssentialModule (req)) || extendReqToDeactivate.contains (req)) {
                    Set<Module> reqs = Utilities.findRequiredModules (req, mm, module2required);
                    inscreasing |= moreToDeactivate.addAll (reqs);
                }
            }
        }

        return filterCandidatesToDeactivate (extendReqToDeactivate, moreToDeactivate, mm);
    }
    
    private static Set<Module> filterCandidatesToDeactivate (final Collection<Module> requested, final Collection<Module> candidates, ModuleManager mm) {
        // go down and find all modules (except other kits) which can be deactivated
        Set<Module> result = new HashSet<Module> ();
        Set<Module> compactSet = new HashSet<Module> (candidates);
        
        // create collection of all installed eagers
        Set<Module> installedEagers = new HashSet<Module> ();
        for (UpdateElement eagerEl : UpdateManagerImpl.getInstance ().getInstalledEagers ()) {
            // take a module
            UpdateElementImpl impl = Trampoline.API.impl (eagerEl);
            if(impl instanceof ModuleUpdateElementImpl) {
                ModuleInfo mi = ((ModuleUpdateElementImpl) impl).getModuleInfo ();
                installedEagers.add (Utilities.toModule (mi));
            } else if(impl instanceof FeatureUpdateElementImpl) {
                List <ModuleInfo> infos = ((FeatureUpdateElementImpl) impl).getModuleInfos();
                for(ModuleInfo mi : infos) {
                    installedEagers.add (Utilities.toModule (mi));
                }
            } else {
                assert false : eagerEl + " is instanceof neither ModuleUpdateElementImpl nor FeatureUpdateElementImpl";
            }
            
        }
        // add installedEagers into affected modules to don't break uninstall of candidates
        compactSet.addAll (installedEagers);
        
        Set<Module> mustRemain = new HashSet<Module> ();
        Set<Module> affectedEagers = new HashSet<Module> ();
        for (Module depM : candidates) {
            if ((Utilities.isKitModule (depM) || Utilities.isEssentialModule (depM)) && ! requested.contains (depM)) {
                if (LOGGER.isLoggable (Level.FINE)) {
                    LOGGER.log(Level.FINE, "The module " + depM.getCodeNameBase() +
                        " is KIT_MODULE and won't be deactivated now not even " + Utilities.findRequiredModules(depM, mm, module2required));
                }
                mustRemain.add (depM);
            } else if (mustRemain.contains (depM)) {
                LOGGER.log (Level.FINE, "The module " + depM.getCodeNameBase () + " was investigated already and won't be deactivated now.");
            } else {
                Set<Module> depends = Utilities.findDependingModules (depM, mm, module2depending);
                if (! compactSet.containsAll (depends)) {
                    mustRemain.add (depM);
                    mustRemain.addAll (Utilities.findRequiredModules (depM, mm, module2required));
                    LOGGER.log (Level.FINE, "The module " + depM.getCodeNameBase () + " is shared and cannot be deactivated now.");
                    if (LOGGER.isLoggable (Level.FINER)) {
                        Set<Module> outsideModules = new HashSet<Module> (depends);
                        outsideModules.removeAll (compactSet);
                        LOGGER.log (Level.FINER, "On " + depM.getCodeNameBase () + " depending modules outside of set now deactivating modules: " + outsideModules);
                    }
                } else {
                    result.add (depM);
                    depends.retainAll (installedEagers);
                    if (! depends.isEmpty ()) {
                        affectedEagers.addAll (depends);
                    }
                }
            }
        }
        result.removeAll (installedEagers);

        // add only affected eagers again
        LOGGER.log (Level.FINE, "Affected eagers are " + affectedEagers);
        result.addAll (affectedEagers);

        result.removeAll (findDeepRequired (mustRemain, mm));

        return result;
    }
    
    private static Set<Module> findDeepRequired (Set<Module> orig, ModuleManager mm) {
        Set<Module> more = new HashSet<Module> (orig);
        boolean inscreasing = true;
        while (inscreasing) {
            Set<Module> tmp = new HashSet<Module> (more);
            inscreasing = false;
            for (Module req : tmp) {
                Set<Module> reqs = Utilities.findRequiredModules (req, mm, module2required);
                inscreasing |= more.addAll (reqs);
            }
        }
        return more;
    }
    
}
