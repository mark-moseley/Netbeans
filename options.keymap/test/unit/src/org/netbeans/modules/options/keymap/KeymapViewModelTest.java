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
 * @author Jan Jancura
 */
public class KeymapViewModelTest extends NbTestCase {
    
    public KeymapViewModelTest (String testName) {
        super (testName);
    }
    
    public void testCancelCurrentProfile () {
        KeymapViewModel model = new KeymapViewModel ();
        String currentProfile = model.getCurrentProfile ();
        model.setCurrentProfile ("mine");
        assertEquals ("mine", model.getCurrentProfile ());
        model.cancel ();
        assertEquals (currentProfile, model.getCurrentProfile ());
        assertEquals (currentProfile, new KeymapViewModel ().getCurrentProfile ());
    }
    
    public void testOkCurrentProfile () {
        KeymapViewModel model = new KeymapViewModel ();
        String currentProfile = model.getCurrentProfile ();
        model.setCurrentProfile ("mine");
        assertEquals ("mine", model.getCurrentProfile ());
        assertEquals (currentProfile, new KeymapViewModel ().getCurrentProfile ());
        model.apply ();
        assertEquals ("mine", model.getCurrentProfile ());
        // TODO: this no longer works:
        // assertEquals ("mine", new KeymapViewModel ().getCurrentProfile ());
    }
    
    public void testChangeShortcuts () {
        KeymapViewModel model = new KeymapViewModel ();
        forAllActions (model, new R () {
            public void run (KeymapViewModel model, ShortcutAction action) {
                model.setShortcuts(action, Collections.<String>emptySet());
            }
        });
        forAllActions (model, new R () {
            public void run (KeymapViewModel model, ShortcutAction action) {
                assertEquals (0, model.getShortcuts (action).length);
            }
        });
        final Set<String> set = Collections.singleton ("Alt+K");
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

    /* XXX failing: #137748
    public void testChangeShortcutsOk () {
        KeymapViewModel model = new KeymapViewModel ();
        Map<Set<String>,ShortcutAction> shortcuts = setRandomShortcuts (model);
        System.out.println ("apply changes");
        model.apply ();
        System.gc ();
        model.apply ();
        System.gc ();
        checkShortcuts (model, shortcuts, true);
    }
     */
    
    public void testChangeShortcutsCancel () {
        KeymapViewModel model = new KeymapViewModel ();
        Map<Set<String>,ShortcutAction> shortcuts = getShortcuts (model);
        Map<Set<String>,ShortcutAction> shortcuts2 = setRandomShortcuts (model);
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
    private Map<Set<String>,ShortcutAction> setRandomShortcuts(final KeymapViewModel model) {
        final int[] ii = {1};
        final Map<Set<String>,ShortcutAction> result = new HashMap<Set<String>,ShortcutAction>();
        System.out.println("set random shortcuts");
        forAllActions (model, new R () {
            public void run (KeymapViewModel model, ShortcutAction action) {
                String shortcut = Integer.toString (ii [0], 36).toUpperCase ();
                StringBuffer sb = new StringBuffer ();
                int i, k = shortcut.length ();
                for (i = 0; i < k; i++) 
                    sb.append (shortcut.charAt (i)).append (' ');
                shortcut = sb.toString ().trim ();
                Set<String> s = Collections.singleton (shortcut);
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
    private Map<Set<String>,ShortcutAction> getShortcuts(final KeymapViewModel model) {
        final Map<Set<String>,ShortcutAction> result = new HashMap<Set<String>,ShortcutAction>();
        System.out.println("get shortcuts");
        forAllActions (model, new R () {
            public void run (KeymapViewModel model, ShortcutAction action) {
                String[] sh = model.getShortcuts (action);
                if (sh.length == 0) return;
                Set<String> shortcuts = new HashSet<String>(Arrays.asList(sh));
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
    
    private void checkShortcuts(final KeymapViewModel model, final Map<Set<String>,ShortcutAction> shortcuts, final boolean print) {
        System.out.println("check shortcuts");
        final Map<Set<String>,ShortcutAction> localCopy = new HashMap<Set<String>,ShortcutAction>(shortcuts);
        forAllActions (model, new R () {
            public void run (KeymapViewModel model, ShortcutAction action) {
                String[] sh = model.getShortcuts (action);
                if (sh.length == 0) return;
                Set<String> s = new HashSet<String>(Arrays.asList(sh));
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


