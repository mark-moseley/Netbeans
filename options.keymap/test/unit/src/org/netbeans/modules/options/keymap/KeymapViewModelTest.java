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

package org.netbeans.modules.options.keymap;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javax.swing.Action;
import javax.swing.text.TextAction;
import org.netbeans.core.options.keymap.api.ShortcutAction;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author Jan Jancura
 */
public class KeymapViewModelTest extends NbTestCase {
    
    /**
     * 
     * @param testName 
     */
    public KeymapViewModelTest (String testName) {
        super (testName);
    }
    
    /**
     * 
     */
    public void testCancelCurrentProfile () {
        KeymapViewModel model = new KeymapViewModel ();
        String currentProfile = model.getCurrentProfile ();
        model.setCurrentProfile ("XXX");
        assertEquals ("XXX", model.getCurrentProfile ());
        model.cancel ();
        assertEquals (currentProfile, model.getCurrentProfile ());
        assertEquals (currentProfile, new KeymapViewModel ().getCurrentProfile ());
    }
    
    /**
     * 
     */
    public void testOkCurrentProfile () {
        KeymapViewModel model = new KeymapViewModel ();
        String currentProfile = model.getCurrentProfile ();
        model.setCurrentProfile ("XXX");
        assertEquals ("XXX", model.getCurrentProfile ());
        assertEquals (currentProfile, new KeymapViewModel ().getCurrentProfile ());
        model.apply ();
        assertEquals ("XXX", model.getCurrentProfile ());
        // TODO: this no longer works:
        // assertEquals ("XXX", new KeymapViewModel ().getCurrentProfile ());
    }
    
    /**
     * 
     */
    public void testChangeShortcuts () {
        KeymapViewModel model = new KeymapViewModel ();
        forAllActions (model, new R () {
            public void run (KeymapViewModel model, ShortcutAction action) {
                model.setShortcuts (action, Collections.EMPTY_SET);
            }
        });
        forAllActions (model, new R () {
            public void run (KeymapViewModel model, ShortcutAction action) {
                assertEquals (0, model.getShortcuts (action).length);
            }
        });
        final Set set = Collections.singleton ("Alt+K");
        forAllActions (model, new R () {
            public void run (KeymapViewModel model, ShortcutAction action) {
                model.setShortcuts (action, set);
            }
        });
        forAllActions (model, new R () {
            public void run (KeymapViewModel model, ShortcutAction action) {
                String[] s = model.getShortcuts (action);
                assertEquals (1, s.length);
                assertEquals ("Alt+K", s [0]);
            }
        });
    }
    
    /**
     * 
     */
    public void testChangeShortcutsOk () {
        KeymapViewModel model = new KeymapViewModel ();
        Map shortcuts = setRandomShortcuts (model);
        System.out.println ("apply changes");
        model.apply ();
        System.gc ();
        model.apply ();
        System.gc ();
        checkShortcuts (model, shortcuts, true);
        checkShortcuts (new KeymapViewModel (), shortcuts, true);
    }
    
    /**
     * 
     */
    public void testChangeShortcutsCancel () {
        KeymapViewModel model = new KeymapViewModel ();
        Map shortcuts = getShortcuts (model);
        Map shortcuts2 = setRandomShortcuts (model);
        checkShortcuts (model, shortcuts2, false);
        System.out.println ("cancel changes");
        model.cancel ();
        checkShortcuts (model, shortcuts, false);
        checkShortcuts (new KeymapViewModel (), shortcuts, false);
    }
    
    /**
     * Sets random shortcuts and returns them in 
     * Map (Set (String (shortcut)) > String (action name)).
     */
    private Map setRandomShortcuts (final KeymapViewModel model) {
        final int[] ii = {1};
        final Map result = new HashMap ();
        System.out.println("set random shortcuts");
        forAllActions (model, new R () {
            public void run (KeymapViewModel model, ShortcutAction action) {
                String shortcut = Integer.toString (ii [0], 36).toUpperCase ();
                StringBuffer sb = new StringBuffer ();
                int i, k = shortcut.length ();
                for (i = 0; i < k; i++) 
                    sb.append (shortcut.charAt (i)).append (' ');
                shortcut = sb.toString ().trim ();
                Set s = Collections.singleton (shortcut);
                model.setShortcuts (action, s);
                result.put (s, action);
                //System.out.println (s + " : " + action);
                ii [0] ++;
            }
        });
        return result;
    }
    
    /**
     * Returns Map (Set (String (shortcut)) > String (action name)) containing 
     * all current shortcuts.
     */
    private Map getShortcuts (final KeymapViewModel model) {
        final Map result = new HashMap ();
        System.out.println("get shortcuts");
        forAllActions (model, new R () {
            public void run (KeymapViewModel model, ShortcutAction action) {
                String[] sh = model.getShortcuts (action);
                if (sh.length == 0) return;
                Set shortcuts = new HashSet (Arrays.asList (sh));
                //System.out.println("sh: " + shortcuts + " : " + action);
                assertFalse ("Same shortcuts assigned to two actions ", result.containsKey (shortcuts));
                result.put (shortcuts, action);
            }
        });
        return result;
    }
    
    private static String getName (Object action) {
        if (action instanceof TextAction)
            return (String) ((TextAction) action).getValue (Action.SHORT_DESCRIPTION);
        if (action instanceof Action)
            return (String) ((Action) action).getValue (Action.NAME);
        return action.toString ();
    }
    
    private void checkShortcuts (final KeymapViewModel model, final Map shortcuts, final boolean print) {
        System.out.println("check shortcuts");
        final Map localCopy = new HashMap (shortcuts);
        forAllActions (model, new R () {
            public void run (KeymapViewModel model, ShortcutAction action) {
                String[] sh = model.getShortcuts (action);
                if (sh.length == 0) return;
                Set s = new HashSet (Arrays.asList (sh));
                if (print)
                    System.out.println (s + " : " + action + " : " + localCopy.get (s));
                assertEquals ("Shortcut changed: " + s + " : " + action, localCopy.get (s), action);
                localCopy.remove (s);
            }
        });
        assertTrue ("Some shortcuts found: " + localCopy, localCopy.isEmpty ());
    }
    
    private void forAllActions (KeymapViewModel model, R r) {
        forAllActions (model, r, "");
    }
    
    private void forAllActions (KeymapViewModel model, R r, String folder) {
        Iterator it = model.getItems (folder).iterator ();
        while (it.hasNext ()) {
            Object o = it.next ();
            if (o instanceof String) 
                forAllActions (model, r, (String) o);
            else
                r.run (model, (ShortcutAction) o);
        }
    }
    
    interface R {
        void run (KeymapViewModel model, ShortcutAction action);
    }
}


