/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.form;

import java.util.*;
import javax.swing.*;
import javax.swing.plaf.metal.*;
import org.openide.util.*;

/**
 *
 * @author Tran Duc Trung
 */

class FormLAF {

    private static LookAndFeel defaultLookAndFeel;
    private static DefaultMetalTheme defaultMetalTheme;
    private static Map ideDefaults;
    private static int useIdeLaf = -1;
    private static boolean lafBlockEntered;

    private FormLAF() {}

    static Object executeWithLookAndFeel(final Mutex.ExceptionAction act)
        throws Exception
    {
        try {
            if (checkUseIdeLaf())
                return Mutex.EVENT.readAccess(act);
            else
                return Mutex.EVENT.readAccess(new Mutex.ExceptionAction() {
                    public Object run() throws Exception {
                        boolean restoreAfter = true;
                        try {
                            if (lafBlockEntered)
                                restoreAfter = false;
                            else {
                                lafBlockEntered = true;
                                useDefaultLookAndFeel();
                                restoreAfter = true;
                            }
                            return act.run();
                        }
                        finally {
                            if (restoreAfter) {
                                useIDELookAndFeel();
                                lafBlockEntered = false;
                            }
                        }
                    }
                });
        }
        catch (MutexException ex) {
            throw ex.getException();
        }
    }

    static void executeWithLookAndFeel(final Runnable run) {
        Mutex.EVENT.readAccess(new Mutex.Action() {
            public Object run() {
                if (checkUseIdeLaf())
                    run.run();
                else {
                    boolean restoreAfter = true;
                    try {
                        if (lafBlockEntered)
                            restoreAfter = false;
                        else {
                            lafBlockEntered = true;
                            useDefaultLookAndFeel();
                            restoreAfter = true;
                        }
                        run.run();
                    }
                    finally {
                        if (restoreAfter) {
                            useIDELookAndFeel();
                            lafBlockEntered = false;
                        }
                    }
                }
                return null;
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
    
    private static void useDefaultLookAndFeel() {
        if (defaultLookAndFeel == null) {
            try {
                String lafName = UIManager.getLookAndFeel().getClass().getName();
                defaultLookAndFeel = (LookAndFeel)
                                     Class.forName(lafName).newInstance();
                defaultLookAndFeel.initialize();
            }
            catch (Exception ex) {
                ex.printStackTrace();
                return;
            }
            catch (LinkageError ex) {
                ex.printStackTrace();
                return;
            }
        }

        if (ideDefaults == null)
            saveIDELookAndFeelDefaults();

        if (defaultLookAndFeel instanceof MetalLookAndFeel) {
            if (defaultMetalTheme == null)
                defaultMetalTheme = new DefaultMetalTheme();
            MetalLookAndFeel.setCurrentTheme(defaultMetalTheme);
        }

        copyMap(UIManager.getDefaults(), defaultLookAndFeel.getDefaults());
    }

    private static void useIDELookAndFeel() {
        if (ideDefaults != null)
            copyMap(UIManager.getDefaults(), ideDefaults);
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

    private static void copyMap(Map dest, Map src) {
        // call src.get() on each key to force LazyValues to be init'ed
        // see javax.swing.UIDefaults to see why
        Object[] keys = src.keySet().toArray();
        for (int i=0; i < keys.length; i++)
            src.get(keys[i]);
        
        dest.putAll(src);
    }
}
