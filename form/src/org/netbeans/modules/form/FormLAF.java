/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.form;

import java.util.*;
import javax.swing.*;
import javax.swing.plaf.metal.*;
import org.openide.util.Mutex;

/**
 *
 * @author Tran Duc Trung
 */

class FormLAF
{
    private static Map ideDefaults;
    private static Map lafInstances = new HashMap(5);
    private static int useIdeLaf = -1;
    private static DefaultMetalTheme defMetalTheme;
    private static MetalTheme ideMetalTheme;
    private static String lastLAFName;
    
    private FormLAF() {}

    static Object executeWithLookAndFeel(final String lafclassname,
                                         final Mutex.ExceptionAction act)
        throws Exception
    {
        return Mutex.EVENT.readAccess(
            new Mutex.ExceptionAction () {
                public Object run() throws Exception {
                    boolean restoreAfter = true;
                    UIDefaults defaults = UIManager.getDefaults();
                    synchronized (defaults) {
                        try {
                            if (lafclassname.equals(lastLAFName))
                                restoreAfter = false;
                            else {
                                lastLAFName = lafclassname;
                                useLookAndFeel(lafclassname);
                                restoreAfter = true;
                            }
                            return act.run();
                        }
                        finally {
                            if (restoreAfter) {
                                useIDELookAndFeel();
                                lastLAFName = null;
                            }
                        }
                    }
                }
            });
    }
    
    private static boolean checkUseIdeLaf() {
        if (useIdeLaf == -1) {
            if (System.getProperty("netbeans.form.use_idelaf") != null) // NOI18N
                useIdeLaf = 1;
            else
                useIdeLaf = 0;
        }
        return useIdeLaf > 0;
    }
    
    private static void saveIDELookAndFeelDefaults() {
        if (checkUseIdeLaf())
            return;
        
        if (ideDefaults != null)
            return;

        UIDefaults defaults = UIManager.getDefaults();
        UIDefaults lafDefaults = UIManager.getLookAndFeelDefaults();
        
        ideDefaults = new HashMap(defaults.size() + lafDefaults.size());
        copyMap(ideDefaults, lafDefaults);
        copyMap(ideDefaults, defaults);
    }

    private static void useLookAndFeel(String lafClassName) {
        if (checkUseIdeLaf())
            return;
        
        LookAndFeel laf = (LookAndFeel) lafInstances.get(lafClassName);
        if (laf == null) {
            try {
                laf = (LookAndFeel) Class.forName(lafClassName).newInstance();
                laf.initialize();
                lafInstances.put(lafClassName, laf);
            }
            catch (Throwable ignore) {
                return;
            }
        }
        useLookAndFeel(laf);
    }
    
    private static void useLookAndFeel(LookAndFeel laf) {
        if (checkUseIdeLaf())
            return;
        
        saveIDELookAndFeelDefaults();
        UIDefaults defaults = UIManager.getDefaults();
        synchronized (defaults) {
//              defaults.clear();
//              if (laf.getDefaults().get("LabelUI") != null) // NOI18N
//                  defaults.put("LabelUI", laf.getDefaults().get("LabelUI")); // NOI18N
            
            if (laf instanceof MetalLookAndFeel) {
                if (defMetalTheme == null)
                    defMetalTheme = new DefaultMetalTheme();
                MetalLookAndFeel.setCurrentTheme(defMetalTheme);
            }

            copyMap(defaults, laf.getDefaults());
        }
    }

    private static void useIDELookAndFeel() {
        if (checkUseIdeLaf())
            return;
        
        if (ideDefaults != null) {
            UIDefaults defaults = UIManager.getDefaults();
            synchronized (defaults) {
//                  defaults.clear();
//                  if (ideDefaults.get("LabelUI") != null) // NOI18N
//                      defaults.put("LabelUI", ideDefaults.get("LabelUI")); // NOI18N
                copyMap(defaults, ideDefaults);
            }
        }
    }

    private static void copyMap(Map dest, Map src) {
        // call src.get() on each key to force LazyValues to be init'ed
        // see javax.swing.UIDefaults to see why
        Object[] keys = src.keySet().toArray();
        for (int i=0; i < keys.length; i++)
            src.get(keys[i]);
        
        dest.putAll(src);
    }
}
