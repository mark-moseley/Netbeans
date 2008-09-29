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
package org.netbeans.modules.editor.settings.storage.spi.support;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.KeyStroke;
import org.netbeans.modules.editor.settings.storage.*;
import org.openide.filesystems.FileObject;
import org.openide.util.Utilities;

public final class StorageSupport {

    private static final Logger LOG = Logger.getLogger(StorageSupport.class.getName());
    private static HashMap<String, Integer> names;

    private StorageSupport() {

    }

    public static String getLocalizingBundleMessage(FileObject fo, String key, String defaultValue) {
        return Utils.getLocalizedName(fo, key, defaultValue, false);
    }

    /**
     * Converts a list of <code>KeyStroke</code>s to its textual representation. There
     * are two available formats for the textual representation:
     * 
     * <li><b>Human readable</b> - this format encodes a <code>KeyStroke</code> to
     *   a string that looks like for example 'Ctrl+A' or 'Alt+Shift+M'.
     * <li><b>Emacs style</b> - this format encodes a <code>KeyStroke</code> to
     *   a string that's known from Emacs and that looks like for example 'C-A' or 'AS-M'.
     *   It uses methods from <code>org.openide.util.Utilities</code>, which take
     *   care of Mac OS specifics and use 'D' and 'O' wildcards for encoding 'Ctrl'
     *   and 'Alt' keys.
     * 
     * @param keys The <code>KeyStrokes</code> to convert.
     * @param emacsStyle If <code>true</code> the returned string will be in so called
     *   Emacs style, ortherwise it will be in human readable format.
     * 
     * @return The textual representation of <code>KeyStroke</code>s passed in.
     * @since 1.16
     */
    public static String keyStrokesToString(Collection<? extends KeyStroke> keys, boolean emacsStyle) {
        StringBuilder sb = new StringBuilder();

        for (Iterator<? extends KeyStroke> it = keys.iterator(); it.hasNext(); ) {
            KeyStroke keyStroke = it.next();
            if (emacsStyle) {
                sb.append(Utilities.keyToString(keyStroke));
                if (it.hasNext()) {
                    sb.append('$'); //NOI18N
                }
            } else {
                sb.append(keyStrokeToHumanReadableString(keyStroke));
                if (it.hasNext()) {
                    sb.append(' '); //NOI18N
                }
            }
        }

        return sb.toString();
    }

    /**
     * Converts a textual representation of key strokes to an array of <code>KeyStroke</code>
     * objects. Please see {@link #keyStrokesToString(Collection<KeyStroke>, boolean)}
     * ror details about the available formats.
     * 
     * @param key The textual representation of keystorkes to convert. Its format
     *   depends on the value of <code>emacsStyle</code> parameter.
     * @param emacsStyle If <code>true</code> the <code>key</code> string is expected to be
     *   in so called emacs format, ortherwise it will be in human readable format.
     * 
     * @return The <code>KeyStroke</code>s that were represented by the <code>key</code>
     *   text or <code>null</code> if the textual representation was malformed.
     * @since 1.16
     */
    public static KeyStroke[] stringToKeyStrokes(String key, boolean emacsStyle) {
        assert key != null : "The parameter key must not be null"; //NOI18N
        
        List<KeyStroke> result = new ArrayList<KeyStroke>();
        String delimiter = emacsStyle ? "$" : " "; //NOI18N
        
        for(StringTokenizer st = new StringTokenizer(key, delimiter); st.hasMoreTokens();) { //NOI18N
            String ks = st.nextToken().trim();
            KeyStroke keyStroke;
            
            if (emacsStyle) {
                keyStroke = Utilities.stringToKey(ks);
            } else {
                keyStroke = humanReadableStringToKeyStroke(ks);
            }
            
            if (keyStroke != null) {
                result.add(keyStroke);
            } else {
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine("Invalid keystroke string: '" + ks + "'"); //NOI18N
                }
                
                return null;
            }
        }

        return result.toArray(new KeyStroke[result.size()]);
    }

    private static final String EMACS_CTRL = "Ctrl+"; //NOI18N
    private static final String EMACS_ALT = "Alt+"; //NOI18N
    private static final String EMACS_SHIFT = "Shift+"; //NOI18N
    private static final String EMACS_META = "Meta+"; //NOI18N
    
    private static KeyStroke humanReadableStringToKeyStroke(String keyStroke) {
        int modifiers = 0;
        if (keyStroke.startsWith(EMACS_CTRL)) {
            modifiers |= InputEvent.CTRL_DOWN_MASK;
            keyStroke = keyStroke.substring(EMACS_CTRL.length());
        }
        if (keyStroke.startsWith(EMACS_ALT)) {
            modifiers |= InputEvent.ALT_DOWN_MASK;
            keyStroke = keyStroke.substring(EMACS_ALT.length());
        }
        if (keyStroke.startsWith(EMACS_SHIFT)) {
            modifiers |= InputEvent.SHIFT_DOWN_MASK;
            keyStroke = keyStroke.substring(EMACS_SHIFT.length());
        }
        if (keyStroke.startsWith(EMACS_META)) {
            modifiers |= InputEvent.META_DOWN_MASK;
            keyStroke = keyStroke.substring(EMACS_META.length());
        }
        KeyStroke ks = Utilities.stringToKey(keyStroke);
        if (ks != null) {
            return KeyStroke.getKeyStroke(ks.getKeyCode(), modifiers);
        } else {// probably a VK_* key
            return KeyStroke.getKeyStroke(getName2Keycode().get(keyStroke), modifiers);
        }
    }

    private static HashMap<String, Integer> getName2Keycode() {
        if (names != null) {
            return names;
        } else {
            Field[] fields = KeyEvent.class.getDeclaredFields();
            names = new HashMap<String, Integer>(fields.length * 4 / 3 + 5, 0.75f);

            for (Field f : fields) {
                if (Modifier.isStatic(f.getModifiers())) {
                    try {
                        int numb = f.getInt(null);
                        names.put(KeyEvent.getKeyText(numb), numb);
                    } catch (IllegalArgumentException ex) {
                    } catch (IllegalAccessException ex) {
                    }
                }
            }
            return names;
        }
    }

    private static String keyStrokeToHumanReadableString(KeyStroke keyStroke) {
        int modifiers = keyStroke.getModifiers();
        StringBuilder sb = new StringBuilder();
        if ((modifiers & InputEvent.CTRL_DOWN_MASK) > 0) {
            sb.append(EMACS_CTRL);
        }
        if ((modifiers & InputEvent.ALT_DOWN_MASK) > 0) {
            sb.append(EMACS_ALT);
        }
        if ((modifiers & InputEvent.SHIFT_DOWN_MASK) > 0) {
            sb.append(EMACS_SHIFT);
        }
        if ((modifiers & InputEvent.META_DOWN_MASK) > 0) {
            sb.append(EMACS_META);
        }
        if (keyStroke.getKeyCode() != KeyEvent.VK_SHIFT &&
                keyStroke.getKeyCode() != KeyEvent.VK_CONTROL &&
                keyStroke.getKeyCode() != KeyEvent.VK_META &&
                keyStroke.getKeyCode() != KeyEvent.VK_ALT &&
                keyStroke.getKeyCode() != KeyEvent.VK_ALT_GRAPH
        ) {
            sb.append(Utilities.keyToString(KeyStroke.getKeyStroke(keyStroke.getKeyCode(), 0)));
        }
        return sb.toString();
    }
    
}
