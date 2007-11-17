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

package org.netbeans.editor;

import java.util.ArrayList;
import java.util.Arrays;
import javax.swing.text.JTextComponent;
import javax.swing.KeyStroke;
import javax.swing.Action;

/**
* Extension of JTextComponent.KeyBinding to hold several successive keystrokes.
* The binding containing null key(s) is assumed to assign the default action.
*
* @author Miloslav Metelka
* @version 1.00
*/

public class MultiKeyBinding extends JTextComponent.KeyBinding
    implements java.io.Externalizable {

    /** Successive keystroke. They must be pressed in the order they
    * are stored in the array in order to invoke the associated action.
    */
    public KeyStroke[] keys;

    static final long serialVersionUID =-8602816556604003688L;

    /** Constructor for serialization */
    public MultiKeyBinding() {
        super(null, null);
    }

    /** Constructor for assigning keystroke sequence to action
    * @param keys successive keystroke that must be pressed in order
    *    to invoke action
    * @param actionName action that will be invoked. Action is resolved
    *    from name by calling kit.getActions() after the kit is constructed
    */
    public MultiKeyBinding(KeyStroke[] keys, String actionName) {
        super(null, actionName);
        this.keys = keys;
    }

    /** Compatibility constructor */
    public MultiKeyBinding(KeyStroke key, String actionName) {
        super(key, actionName);
    }

    /** Constructor for existing KeyBinding */
    public MultiKeyBinding(JTextComponent.KeyBinding kb) {
        this(kb.key, kb.actionName);
    }

    public boolean equals(Object o) {
        if (o instanceof MultiKeyBinding) {
            MultiKeyBinding kb = (MultiKeyBinding)o;

            // Compare action names
            if (actionName == null) {
                if (kb.actionName != null) {
                    return false;
                }
            } else {
                if (!actionName.equals(kb.actionName)) {
                    return false;
                }
            }

            // Action names match, now compare action keys
            if (keys == null) {
                if (kb.keys == null) {
                    return (key == null && kb.key == null)
                           || (key != null && key.equals(kb.key));
                } else {
                    return (kb.keys.length == 1
                            && ((key == null && kb.keys[0] == null)
                                || (key != null && key.equals(kb.keys[0]))));
                }
            } else { // keys != null
                if (kb.keys != null) {
                    return Arrays.equals(keys, kb.keys);
                } else { // kb.keys == null
                    return (keys.length == 1
                            && ((kb.key == null && keys[0] == null)
                                || (kb.key != null && kb.key.equals(keys[0]))));
                }
            }
        }
        return false;
    }

    /** Add or replace key bindings array by changes given in
    * the second bindings array
    * @param target target list of bindings
    * @param changes list of changes to apply:
    *  binding containing the non-null keystroke(s) and non-null action
    *    will add the binding or replace the old binding with the same
    *    keystroke(s) in the target array,
    *  binding of the non-null keystroke(s) and null action removes
    *    the binding for that keystroke from the target array (if it existed)
    *  binding containing null keystroke and non-null action adds
    *    or replaces default action
    */
    public static void updateKeyBindings(JTextComponent.KeyBinding[] target,
                                         JTextComponent.KeyBinding[] changes) {
        ArrayList tgt = new ArrayList(Arrays.asList(target));
        MultiKeyBinding tmp = new MultiKeyBinding(new KeyStroke[1], null);
        MultiKeyBinding cur;
        for (int i = 0; i < changes.length; i++) {
            if (changes[i] instanceof MultiKeyBinding) {
                cur = (MultiKeyBinding)changes[i];
                if (cur.keys == null) { // single key multi binding
                    tmp.keys[0] = cur.key;
                    tmp.actionName = cur.actionName;
                    cur = tmp;
                }
            } else { // simulate multi binding
                tmp.keys[0] = changes[i].key;
                tmp.actionName = changes[i].actionName;
                cur = tmp;
            }
            // cycle through all bindings
            boolean matched = false;
            for (int j = 0; j < tgt.size(); j++) {
                JTextComponent.KeyBinding kb = (JTextComponent.KeyBinding)tgt.get(j);
                if (kb instanceof MultiKeyBinding) {
                    MultiKeyBinding mkb = (MultiKeyBinding)kb;
                    if (mkb.keys == null) { // single key multi binding
                        if (cur.keys.length == 1 && cur.keys[0].equals(mkb.key)) { // found
                            if (mkb.actionName == null) { // remove
                                tgt.remove(i);
                            } else { // replace
                                tgt.set(i, mkb);
                            }
                            matched = true;
                            break;
                        }
                    } else { // multi binding
                        if (cur.keys.length == mkb.keys.length) {
                            matched = true;
                            for (int k = 0; k < cur.keys.length; k++) {
                                if (!cur.keys[k].equals(mkb.keys[k])) {
                                    matched = false;
                                    break;
                                }
                            }
                            if (matched) {
                                if (mkb.actionName == null) { // remove
                                    tgt.remove(i);
                                } else { // replace
                                    tgt.set(i, mkb);
                                }
                                break;
                            }
                        }
                    }
                } else { // single key binding
                    if (cur.keys.length == 1 && cur.keys[0].equals(kb.key)) { // found
                        if (kb.actionName == null) { // remove
                            tgt.remove(i);
                        } else { // replace
                            tgt.set(i, kb);
                        }
                        matched = true;
                        break;
                    }
                }
            }
            if (!matched) {
                tgt.add(changes[tgt.size()]);
            }
        }
    }

    public void readExternal(java.io.ObjectInput in)
    throws java.io.IOException, ClassNotFoundException {
        Object obj = in.readObject ();

        if( obj instanceof Integer ) { // new settings format
            int len = ((Integer)obj).intValue();
            if( len >= 0 ) {
                keys = new KeyStroke[ len ];
                for( int i=0; i<len; i++ ) {
                    keys[i] = KeyStroke.getKeyStroke( in.readInt(), in.readInt(), in.readBoolean() );
                }
            } else {
                keys = null;
            }

            if( in.readBoolean() ) {
                key = KeyStroke.getKeyStroke( in.readInt(), in.readInt(), in.readBoolean() );
            } else {
                key = null;
            }

            actionName = (String)in.readObject();

        } else { // compatibility mode, settings in old format
            keys = (KeyStroke[])obj;
            key = (KeyStroke)in.readObject();
            actionName = (String)in.readObject();
        }
    }

    public void writeExternal(java.io.ObjectOutput out)
    throws java.io.IOException {

        if( keys != null ) {
            out.writeObject( new Integer( keys.length ) );
            for( int i=0; i<keys.length; i++ ) {
                out.writeInt( keys[i].getKeyCode() );
                out.writeInt( keys[i].getModifiers() );
                out.writeBoolean( keys[i].isOnKeyRelease() );
            }
        } else {
            out.writeObject( new Integer( -1 ) );
        }

        if( key != null ) {
            out.writeBoolean( true );
            out.writeInt( key.getKeyCode() );
            out.writeInt( key.getModifiers() );
            out.writeBoolean( key.isOnKeyRelease() );
        } else {
            out.writeBoolean( false );
        }
        out.writeObject( actionName );
    }

    public String toString() {
        if (keys == null) {
            return "key=" + key + ", actionName=" + actionName; // NOI18N
        } else {
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < keys.length; i++) {
                sb.append("key"); // NOI18N
                sb.append(i);
                sb.append('=');
                sb.append(keys[i]);
                sb.append(", "); // NOI18N
            }
            sb.append("actionName="); // NOI18N
            sb.append(actionName);
            return sb.toString();
        }
    }

}
