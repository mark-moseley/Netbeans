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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.autoupdate.services;

import org.netbeans.modules.autoupdate.updateprovider.NativeComponentItem;
import org.netbeans.modules.autoupdate.updateprovider.FeatureItem;
import org.netbeans.modules.autoupdate.updateprovider.UpdateItemImpl;
import org.netbeans.modules.autoupdate.updateprovider.LocalizationItem;
import org.netbeans.modules.autoupdate.updateprovider.InstalledModuleItem;
import org.netbeans.modules.autoupdate.updateprovider.ModuleItem;
import org.netbeans.modules.autoupdate.updateprovider.InstalledModuleProvider;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.autoupdate.UpdateElement;
import org.netbeans.api.autoupdate.UpdateManager;
import org.netbeans.api.autoupdate.UpdateUnit;
import org.netbeans.api.autoupdate.UpdateUnitProvider;
import org.netbeans.modules.autoupdate.updateprovider.BackupUpdateProvider;
import org.netbeans.modules.autoupdate.updateprovider.InstalledUpdateProvider;
import org.netbeans.spi.autoupdate.UpdateItem;
import org.netbeans.spi.autoupdate.UpdateProvider;
import org.openide.modules.Dependency;
import org.openide.modules.ModuleInfo;
import org.openide.util.NbBundle;

/**
 *
 * @author Jiri Rechtacek
 */
public class UpdateUnitFactory {
    
    /** Creates a new instance of UpdateItemFactory */
    private UpdateUnitFactory () {}
    
    private static final UpdateUnitFactory INSTANCE = new UpdateUnitFactory ();
    private final Logger log = Logger.getLogger (this.getClass ().getName ());
    private static final DateFormat FMT = new SimpleDateFormat ("mm:ss:SS");
    private static long runTime = -1;
    
    public static final String UNSORTED_CATEGORY = NbBundle.getMessage (UpdateUnitFactory.class, "UpdateUnitFactory_Unsorted_Category");
    public static final String LIBRARIES_CATEGORY = NbBundle.getMessage (UpdateUnitFactory.class, "UpdateUnitFactory_Libraries_Category");
    public static final String BRIDGES_CATEGORY = NbBundle.getMessage (UpdateUnitFactory.class, "UpdateUnitFactory_Bridges_Category");
    
    public static UpdateUnitFactory getDefault () {
        return INSTANCE;
    }
    
    public Map<String, UpdateUnit> getUpdateUnits () {
        resetRunTime ("Measuring of UpdateUnitFactory.getUpdateUnits()");
        List<UpdateUnitProvider> updates = UpdateUnitProviderImpl.getUpdateUnitProviders (true);
        
//        // loop installed modules
//        SortedSet<String> unmarked = new TreeSet<String> ();
//        SortedSet<String> markedTrue = new TreeSet<String> ();
//        SortedSet<String> markedFalse = new TreeSet<String> ();
//        for (ModuleInfo moduleInfo : InstalledModuleProvider.getInstalledModules ().values ()) {
//            Object f = moduleInfo.getAttribute (ATTR_VISIBLE);
//            if (f == null) {
//                unmarked.add (moduleInfo.getCodeName ());
//            } else if (Boolean.parseBoolean (f.toString ())) {
//                markedTrue.add (moduleInfo.getCodeName ());
//            } else {
//                markedFalse.add (moduleInfo.getCodeName ());
//            }
//        }
//        System.out.println("###### SIZE OF UNMARKED MODULES ? " + unmarked.size ());
//        System.out.println("###### SIZE OF VISIBLE MODULES ? " + markedTrue.size ());
//        System.out.println("###### SIZE OF HIDDEN MODULES ? " + markedFalse.size ());
        
        try {
            InstalledModuleProvider.getDefault().getUpdateItems();
        } catch (Exception x) {
            x.printStackTrace();
        }
        reportRunTime ("Get all installed modules.");
        
        // append installed units
        Map<String, UpdateUnit> mappedImpl = appendUpdateItems (
                new HashMap<String, UpdateUnit> (),
                InstalledModuleProvider.getDefault ());
        reportRunTime ("Append installed units.");

        for (UpdateUnitProvider up : updates) {
            UpdateUnitProviderImpl impl = Trampoline.API.impl (up);
            
            // append units from provider
            mappedImpl = appendUpdateItems (mappedImpl, impl.getUpdateProvider ());
            reportRunTime ("AppendUpdateItems for " + impl.getUpdateProvider ().getDisplayName ());
        }

        return mappedImpl;
    }
    
    public Map<String, UpdateUnit> getUpdateUnits (UpdateProvider provider) {
        resetRunTime ("Measuring UpdateUnitFactory.getUpdateUnits (" + provider.getDisplayName () + ")");
        
        // prepare items accessible in provider
        Collection<UpdateItem> itemsFromProvider = null;
        
        try {
            itemsFromProvider = provider.getUpdateItems ().values();
            reportRunTime ("Get itemsFromProvider for " + provider.getDisplayName ());
        } catch (IOException ioe) {
            log.log (Level.INFO, "Cannot read UpdateItems from UpdateProvider " + provider, ioe);
            itemsFromProvider = Collections.emptySet ();
        }
        
        // check installed units
        Map<String, UpdateUnit> temp  = appendUpdateItems (
                new HashMap<String, UpdateUnit> (),
                InstalledModuleProvider.getDefault ());
        reportRunTime ("Get appendUpdateItems installed modules.");
        
        // append units from provider
        temp = appendUpdateItems (temp, provider);
        reportRunTime ("Get appendUpdateItems for " + provider.getDisplayName ());
        
        assert itemsFromProvider != null : provider + " UpdateProvider cannot returns null items.";
        Map<String, UpdateUnit> retval = new HashMap<String, UpdateUnit>();
        for (UpdateItem updateItem : itemsFromProvider) {
            UpdateItemImpl itemImpl = Trampoline.SPI.impl(updateItem);
            UpdateUnit unit = temp.get (itemImpl.getCodeName ());
            if (unit != null) {
                retval.put (itemImpl.getCodeName (), unit);
            }            
        }
        reportRunTime ("Get filltering by " + provider.getDisplayName ());
        
        return retval;
    }
    
    Map<String, UpdateUnit> appendUpdateItems (Map<String, UpdateUnit> originalUnits, UpdateProvider provider) {
        assert originalUnits != null : "Map of original UnitImpl cannot be null";

        Map<String, UpdateItem> items;
        try {
            items = provider.getUpdateItems ();
        } catch (IOException ioe) {
            log.log (Level.INFO, "Cannot read UpdateItem from UpdateProvider " + provider, ioe);
            return originalUnits;
        }
        
        assert items != null : "UpdateProvider[" + provider.getName () + "] should return non-null items.";
        
        // append updates
        for (String simpleItemId : items.keySet ()) {

            // create UpdateItemImpl
            UpdateItemImpl itemImpl = Trampoline.SPI.impl (items.get (simpleItemId));

            UpdateElement updateEl = null;
            boolean isKitModule = false;
            if (itemImpl instanceof ModuleItem) {
                ModuleInfo mi = ((ModuleItem) itemImpl).getModuleInfo ();
                assert mi != null : "ModuleInfo must be found for " + itemImpl;
                isKitModule = Utilities.isKitModule (mi);
            }
            if (itemImpl instanceof InstalledModuleItem) {
                if (isKitModule) {
                    KitModuleUpdateElementImpl impl = new KitModuleUpdateElementImpl ((InstalledModuleItem) itemImpl, null);
                    updateEl = Trampoline.API.createUpdateElement (impl);
                } else {
                    ModuleUpdateElementImpl impl = new ModuleUpdateElementImpl ((InstalledModuleItem) itemImpl, null);
                    updateEl = Trampoline.API.createUpdateElement (impl);
                }
            } else if (itemImpl instanceof ModuleItem) {
                if (isKitModule) {
                    KitModuleUpdateElementImpl impl = new KitModuleUpdateElementImpl ((ModuleItem) itemImpl, provider.getDisplayName ());
                    updateEl = Trampoline.API.createUpdateElement (impl);
                } else {
                    ModuleUpdateElementImpl impl = new ModuleUpdateElementImpl ((ModuleItem) itemImpl, provider.getDisplayName ());
                    updateEl = Trampoline.API.createUpdateElement (impl);
                }
            } else if (itemImpl instanceof LocalizationItem) {
                updateEl = Trampoline.API.createUpdateElement (new LocalizationUpdateElementImpl ((LocalizationItem) itemImpl, provider.getDisplayName ()));
            } else if (itemImpl instanceof NativeComponentItem) {
                updateEl = Trampoline.API.createUpdateElement (new NativeComponentUpdateElementImpl ((NativeComponentItem) itemImpl, provider.getDisplayName ()));
            } else if (itemImpl instanceof FeatureItem) {
                FeatureUpdateElementImpl impl = new FeatureUpdateElementImpl.Agent (
                        (FeatureItem) itemImpl,
                        provider.getDisplayName (),
                        UpdateManager.TYPE.FEATURE);
                updateEl = Trampoline.API.createUpdateElement (impl);
            } else {
                assert false : "Unknown type of UpdateElement " + updateEl;
            }

            // add element to map
            if (updateEl != null) {
                addElement (originalUnits, updateEl, provider);
            }
        }
        
        return originalUnits;
    }
    
    private void addElement (Map<String, UpdateUnit> impls, UpdateElement element, UpdateProvider provider) {
        // find if corresponding element exists
        UpdateUnit unit = impls.get (element.getCodeName ());
        
        // XXX: it's should be moved in UI what should filter all elements w/ broken dependencies
        // #101515: Plugin Manager must filter updates by platform dependency
        boolean passed = false;
        UpdateElementImpl elImpl = Trampoline.API.impl (element);
        if (elImpl instanceof ModuleUpdateElementImpl && elImpl.getModuleInfos () != null && elImpl.getModuleInfos ().size() == 1) {
            for (Dependency d : elImpl.getModuleInfos ().get (0).getDependencies ()) {
                if (Dependency.TYPE_REQUIRES == d.getType ()) {
                    //log.log (Level.FINEST, "Dependency: NAME: " + d.getName () + ", TYPE: " + d.getType () + ": " + d.toString ());
                    if (d.getName ().startsWith ("org.openide.modules.os")) { // NOI18N
                        for (ModuleInfo info : InstalledModuleProvider.getInstalledModules ().values ()) {
                            if (Arrays.asList (info.getProvides ()).contains (d.getName ())) {
                                log.log (Level.FINEST, element + " which requires OS " + d + " succeed.");
                                passed = true;
                                break;
                            }
                        }
                        if (! passed) {
                            log.log (Level.FINE, element + " which requires OS " + d + " fails.");
                            return ;
                        }
                    }
                }
            }
        }
        
        UpdateUnitImpl unitImpl = null;
        
        if (unit == null) {
            switch (elImpl.getType ()) {
            case MODULE :
                unitImpl = new ModuleUpdateUnitImpl (element.getCodeName ());
                break;
            case KIT_MODULE :
                unitImpl = new KitModuleUpdateUnitImpl (element.getCodeName ());
                break;
            case STANDALONE_MODULE :
            case FEATURE :
                unitImpl = new FeatureUpdateUnitImpl (element.getCodeName (), elImpl.getType ());
                break;
            case CUSTOM_HANDLED_COMPONENT :
                unitImpl = new NativeComponentUpdateUnitImpl (element.getCodeName ());
                break;
            case LOCALIZATION :
                unitImpl = new LocalizationUpdateUnitImpl (element.getCodeName ());
                break;
            default:
                assert false : "Unsupported for type " + elImpl.getType ();
            }
            unit = Trampoline.API.createUpdateUnit (unitImpl);
            impls.put (unit.getCodeName (), unit);
        } else {
            unitImpl = Trampoline.API.impl (unit);
        }
        
        if (provider instanceof InstalledUpdateProvider) {
            unitImpl.setInstalled (element);
        } else if (provider instanceof BackupUpdateProvider) {
            unitImpl.setBackup (element);
        } else {
            // suppose common UpdateProvider
            unitImpl.addUpdate (element);
        }
        
        // set UpdateUnit into element
        elImpl.setUpdateUnit (unit);
        
    }
    
    // XXX
    private static void resetRunTime (String msg) {
        if (msg != null) System.out.println ("|=== " + msg + " ===|");
        runTime = System.currentTimeMillis ();
    }
    
    // XXX
    private static void reportRunTime (String msg) {
        System.out.println(msg + " === " + FMT.format (new Date (System.currentTimeMillis () - runTime)));
        resetRunTime (null);
    }
    
}
