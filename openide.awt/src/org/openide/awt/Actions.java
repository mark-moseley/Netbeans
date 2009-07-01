/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
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
package org.openide.awt;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageProducer;
import java.awt.image.RGBImageFilter;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.Reference;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.WeakHashMap;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.Keymap;
import org.netbeans.api.actions.Closable;
import org.netbeans.api.actions.Editable;
import org.netbeans.api.actions.Openable;
import org.netbeans.api.actions.Printable;
import org.netbeans.api.actions.Viewable;
import org.openide.util.ContextAwareAction;
import org.openide.util.ImageUtilities;
import org.openide.util.actions.BooleanStateAction;
import org.openide.util.actions.SystemAction;


/** Supporting class for manipulation with menu and toolbar presenters.
*
* @author   Jaroslav Tulach
*/
public class Actions extends Object {
    private static Map<Action, Reference<JMenuItem>> menuActionCache;
    private static Object menuActionLock = new Object();

    /** Shared instance of filter for disabled icons */
    private static RGBImageFilter DISABLED_BUTTON_FILTER;

    /**
     * Make sure an icon is not null, so that e.g. menu items for javax.swing.Action's
     * with no specified icon are correctly aligned. SystemAction already does this so
     * that is not affected.
     */
    private static Icon nonNullIcon(Icon i) {
        return null;

        /*if (i != null) {
            return i;
        } else {
            if (BLANK_ICON == null) {
                BLANK_ICON = new ImageIcon(Utilities.loadImage("org/openide/resources/actions/empty.gif", true)); // NOI18N
            }
            return BLANK_ICON;
        }*/
    }

    /** Method that finds the keydescription assigned to this action.
    * @param action action to find key for
    * @return the text representing the key or null if  there is no text assigned
    */
    public static String findKey(SystemAction action) {
        return findKey((Action) action);
    }

    /** Same method as above, but works just with plain actions.
     */
    private static String findKey(Action action) {
        if (action == null) {
            return null;
        }

        KeyStroke accelerator = (KeyStroke) action.getValue(Action.ACCELERATOR_KEY);

        if (accelerator == null) {
            return null;
        }

        int modifiers = accelerator.getModifiers();
        String acceleratorText = ""; // NOI18N

        if (modifiers > 0) {
            acceleratorText = KeyEvent.getKeyModifiersText(modifiers);
            acceleratorText += "+"; // NOI18N
        } else if (accelerator.getKeyCode() == KeyEvent.VK_UNDEFINED) {
            return ""; // NOI18N
        }

        acceleratorText += KeyEvent.getKeyText(accelerator.getKeyCode());

        return acceleratorText;
    }

    /** Attaches menu item to an action.
    * @param item menu item
    * @param action action
    * @param popup create popup or menu item
     * @deprecated Use {@link #connect(JMenuItem, Action, boolean)} instead.
    */
    public static void connect(JMenuItem item, SystemAction action, boolean popup) {
        connect(item, (Action) action, popup);
    }

    /** Attaches menu item to an action.
     * You can supply an alternative implementation
     * for this method by implementing method
     * {@link ButtonActionConnector#connect(JMenuItem, Action, boolean)} and
     * registering an instance of {@link ButtonActionConnector} in the
     * default lookup. 
     * <p>
     * Since version 7.1 the action can also provide properties
     * "menuText" and "popupText" if one wants to use other text on the JMenuItem
     * than the name
     * of the action taken from Action.NAME. The popupText is checked only if the
     * popup parameter is true and takes the biggest precedence. The menuText is
     * tested everytime and takes precedence over standard <code>Action.NAME</code>
     * 
     * @param item menu item
     * @param action action
     * @param popup create popup or menu item
     * @since 3.29
     */
    public static void connect(JMenuItem item, Action action, boolean popup) {
        for (ButtonActionConnector bac : Lookup.getDefault().lookupAll(ButtonActionConnector.class)) {
            if (bac.connect(item, action, popup)) {
                return;
            }
        }
        Bridge b = new MenuBridge(item, action, popup);

        if (item instanceof Actions.MenuItem) {
            ((Actions.MenuItem)item).setBridge(b);
        }
        b.updateState(null);
        if (!popup) {
            // #39508 fix.
            setMenuActionConnection(item, action);
        }
    }

    /**
     * #39508 fix. the MenuItems have the accelerator set by the Bridge, however that is not removed when the Menu is invisible.
     * it's because of the AddNotify/removeNotify method calls and the Bridge.VisL listener.
     * that conflicts with the changes done in the global keymap (NbKeymap) that are not propagated to the menu items when such change occurs.
     * Fixed by having one global observer on the NbKeyMap (or any other Keymap impl which is Observable) and always synchronizing the menus with the current
     * global Keymap
     */
    private static void setMenuActionConnection(JMenuItem menu, Action action) {
        synchronized (menuActionLock) {
            if (menuActionCache == null) {
                menuActionCache = new WeakHashMap<Action, Reference<JMenuItem>>();

                Keymap map = Lookup.getDefault().lookup(Keymap.class);

                if (map instanceof Observable) {
                    //HACK MAJOR - assuming we have the NbKeymap which is observable
                    ((Observable) map).addObserver(
                        new Observer() {
                            public void update(Observable o, Object arg) {
                                synchronized (menuActionLock) {
                                    Iterator<Map.Entry<Action, Reference<JMenuItem>>> it = menuActionCache.entrySet().iterator();

                                    while (it.hasNext()) {
                                        Map.Entry<Action, Reference<JMenuItem>> entry = it.next();
                                        Action act = entry.getKey();
                                        Reference<JMenuItem> ref = entry.getValue();
                                        JMenuItem mn = ref.get();

                                        if ((act != null) && (mn != null)) {
                                            KeyStroke actKey = (KeyStroke) act.getValue(Action.ACCELERATOR_KEY);
                                            KeyStroke mnKey = mn.getAccelerator();

                                            if (
                                                ((mnKey == null) && (actKey != null)) ||
                                                    ((mnKey != null) && (actKey == null)) ||
                                                    ((mnKey != null) && (actKey != null) && !actKey.equals(mnKey))
                                            ) {
                                                mn.setAccelerator(actKey);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    );
                } else {
                    Logger.getLogger(Actions.class.getName()).warning(
                        "Keymap is not observable, behaviour described in bug #39508 can reappear."
                    );
                }
            }

            menuActionCache.put(action, new WeakReference<JMenuItem>(menu));
        }
    }

    /** Attaches checkbox menu item to boolean state action.
    * @param item menu item
    * @param action action
    * @param popup create popup or menu item
    */
    public static void connect(JCheckBoxMenuItem item, BooleanStateAction action, boolean popup) {
        Bridge b = new CheckMenuBridge(item, action, popup);
        b.updateState(null);
    }

    /** Connects buttons to action.
    * @param button the button
    * @param action the action
     * @deprecated Use {@link #connect(AbstractButton, Action)} instead.
    */
    public static void connect(AbstractButton button, SystemAction action) {
        connect(button, (Action) action);
    }

    /** Connects buttons to action. If the action supplies value for "iconBase"
     * key from getValue(String) with a path to icons the methods setIcon,
     * setPressedIcon, setDisabledIcon and setRolloverIcon will be called on the
     * button with loaded icons using the iconBase. E.g. if the value for "iconBase"
     * will be "com/mycompany/myIcon.gif" following images will be tried "com/mycompany/myIcon.gif"
     * for setIcon, "com/mycompany/myIcon_pressed.gif" for setPressedIcon,
     * "com/mycompany/myIcon_disabled.gif" for setDisabledIcon and
     * "com/mycompany/myIcon_rollover.gif" for setRolloverIcon. SystemAction has
     * special support for iconBase - please check {@link SystemAction#iconResource}
     * for more details.
     * You can supply an alternative implementation
     * for this method by implementing method
     * {@link ButtonActionConnector#connect(AbstractButton, Action)} and
     * registering an instance of {@link ButtonActionConnector} in the
     * default lookup.
     * @param button the button
     * @param action the action
     * @since 3.29
     */
    public static void connect(AbstractButton button, Action action) {
        for (ButtonActionConnector bac : Lookup.getDefault().lookupAll(ButtonActionConnector.class)) {
            if (bac.connect(button, action)) {
                return;
            }
        }
        Bridge b = new ButtonBridge(button, action);
        b.updateState(null);
    }

    /** Connects buttons to action.
    * @param button the button
    * @param action the action
    */
    public static void connect(AbstractButton button, BooleanStateAction action) {
        Bridge b = new BooleanButtonBridge(button, action);
        b.updateState(null);
    }

    /** Sets the text for the menu item or other subclass of AbstractButton.
    * Cut from the name '&' char.
    * @param item AbstractButton
    * @param text new label
    * @param useMnemonic if true and '&' char found in new text, next char is used
    *           as Mnemonic.
    * @deprecated Use either {@link AbstractButton#setText} or {@link Mnemonics#setLocalizedText(AbstractButton, String)} as appropriate.
    */
    public static void setMenuText(AbstractButton item, String text, boolean useMnemonic) {
        if (useMnemonic) {
            Mnemonics.setLocalizedText(item, text);
        } else {
            item.setText(cutAmpersand(text));
        }
    }

    /**
     * Removes an ampersand from a text string; commonly used to strip out unneeded mnemonics.
     * Replaces the first occurence of <samp>&amp;?</samp> by <samp>?</samp> or <samp>(&amp;??</samp> by the empty string
     * where <samp>?</samp> is a wildcard for any character.
     * <samp>&amp;?</samp> is a shortcut in English locale.
     * <samp>(&amp;?)</samp> is a shortcut in Japanese locale.
     * Used to remove shortcuts from workspace names (or similar) when shortcuts are not supported.
     * <p>The current implementation behaves in the same way regardless of locale.
     * In case of a conflict it would be necessary to change the
     * behavior based on the current locale.
     * @param text a localized label that may have mnemonic information in it
     * @return string without first <samp>&amp;</samp> if there was any
     */
    public static String cutAmpersand(String text) {
        // XXX should this also be deprecated by something in Mnemonics?
        int i;
        String result = text;

        /* First check of occurence of '(&'. If not found check
          * for '&' itself.
          * If '(&' is found then remove '(&??'.
          */
        i = text.indexOf("(&"); // NOI18N

        if ((i >= 0) && ((i + 3) < text.length()) && /* #31093 */
                (text.charAt(i + 3) == ')')) { // NOI18N
            result = text.substring(0, i) + text.substring(i + 4);
        } else {
            //Sequence '(&?)' not found look for '&' itself
            i = text.indexOf('&');

            if (i < 0) {
                //No ampersand
                result = text;
            } else if (i == (text.length() - 1)) {
                //Ampersand is last character, wrong shortcut but we remove it anyway
                result = text.substring(0, i);
            } else {
                //Remove ampersand from middle of string
                //Is ampersand followed by space? If yes do not remove it.
                if (" ".equals(text.substring(i + 1, i + 2))) {
                    result = text;
                } else {
                    result = text.substring(0, i) + text.substring(i + 1);
                }
            }
        }

        return result;
    }
    
    //
    // Factories 
    //

    
    /** Creates new action which is always enabled. 
     * This method can also be used from 
     * <a href="@org-openide-modules@/org/openide/modules/doc-files/api.html#how-layer">XML Layer</a> 
     * directly by following XML definition:
     * <pre>
     * &lt;file name="your-pkg-action-id.instance"&gt;
     *   &lt;attr name="instanceCreate" methodvalue="org.openide.awt.Actions.alwaysEnabled"/&gt;
     *   &lt;attr name="delegate" methodvalue="your.pkg.YourAction.factoryMethod"/&gt;
     *   &lt;attr name="displayName" bundlevalue="your.pkg.Bundle#key"/&gt;
     *   &lt;attr name="iconBase" stringvalue="your/pkg/YourImage.png"/&gt;
     *   &lt;!-- if desired: &lt;attr name="noIconInMenu" boolvalue="false"/&gt; --&gt;
     * &lt;/file&gt;
     * </pre>
     * In case the "delegate" is not just {@link ActionListener}, but also
     * {@link Action}, the returned action acts as a lazy proxy - it defers initialization
     * of the action itself, but as soon as it is created, it delegates all queries
     * to it. This way one can create an action that looks statically enabled, and as soon
     * as user really uses it, it becomes active - it can change its name, it can
     * change its enabled state, etc.
     *
     * 
     * @param delegate the task to perform when action is invoked
     * @param displayName the name of the action
     * @param iconBase the location to the actions icon
     * @param noIconInMenu true if this icon shall not have an item in menu
     * @since 7.3
     */
    public static Action alwaysEnabled(
        ActionListener delegate, String displayName, String iconBase, boolean noIconInMenu
    ) {
        HashMap<String,Object> map = new HashMap<String,Object>();
        map.put("delegate", delegate); // NOI18N
        map.put("displayName", displayName); // NOI18N
        map.put("iconBase", iconBase); // NOI18N
        map.put("noIconInMenu", noIconInMenu); // NOI18N
        return alwaysEnabled(map);
    }
    // for use from layers
    static Action alwaysEnabled(Map map) {
        return new AlwaysEnabledAction(map);
    }

    /** Creates new "callback" action. Such action has an assigned key
     * which is used to find proper delegate in {@link ActionMap} of currently
     * active component.
     * <p>
     * This action can be lazily declared in a
     * <a href="@org-openide-modules@/org/openide/modules/doc-files/api.html#how-layer">
     * layer file</a> using following XML snippet:
     * <pre>
     * &lt;file name="action-pkg-ClassName.instance"&gt;
     *   &lt;attr name="instanceCreate" methodvalue="org.openide.awt.Actions.callback"/&gt;
     *   &lt;attr name="key" stringvalue="KeyInActionMap"/&gt;
     *   &lt;attr name="surviveFocusChange" boolvalue="false"/&gt; &lt;!-- defaults to false --&gt;
     *   &lt;attr name="fallback" newvalue="action.pkg.DefaultAction"/&gt; &lt;!-- may be missing --&gt;
     *   &lt;attr name="displayName" bundlevalue="your.pkg.Bundle#key"/&gt;
     *   &lt;attr name="iconBase" stringvalue="your/pkg/YourImage.png"/&gt;
     *   &lt;!-- if desired: &lt;attr name="noIconInMenu" boolvalue="false"/&gt; --&gt;
     * &lt;/file&gt;
     * </pre>
     * 
     *
     * @param key the key to search for in an {@link ActionMap}
     * @param surviveFocusChange true to remember action provided by previously
     *   active component even some other component is currently active
     * @param fallback action to delegate to when no key found. Use <code>null</code>
     *   to make the action disabled if delegate assigned to key is missing
     * @param displayName localized name of the action (including ampersand)
     * @param iconBase the location to the action icon
     * @param noIconInMenu true if this icon shall not have an item in menu
     * @return creates new action associated with given key
     * @since 7.10
     */
    public static ContextAwareAction callback(
        String key, Action fallback, boolean surviveFocusChange,
        String displayName, String iconBase, boolean noIconInMenu
    ) {
        Map<String,Object> map = new HashMap<String, Object>();
        map.put("key", key); // NOI18N
        map.put("surviveFocusChange", surviveFocusChange); // NOI18N
        map.put("fallback", fallback); // NOI18N
        map.put("displayName", displayName); // NOI18N
        map.put("iconBase", iconBase); // NOI18N
        map.put("noIconInMenu", noIconInMenu); // NOI18N
        return callback(map);
    }
    static ContextAwareAction callback(Map fo) {
        return GeneralAction.callback(fo);
    }

    /** Creates new "context" action, an action that observes the current
     * selection for a given type and enables if instances of given type are
     * present. Common interfaces to watch for include {@link Openable},
     * {@link Editable}, {@link Closable}, {@link Viewable}, and any interfaces
     * defined and exposed by various other APIs.
     * <p>
     * Actions of this kind can be declared in
     * <a href="@org-openide-modules@/org/openide/modules/doc-files/api.html#how-layer">
     * layer file</a> using following XML snippet:
     * <pre>
     * &lt;file name="action-pkg-ClassName.instance"&gt;
     *   &lt;attr name="instanceCreate" methodvalue="org.openide.awt.Actions.context"/&gt;
     *   &lt;attr name="type" stringvalue="org.netbeans.api.actions.Openable"/&gt;
     *   &lt;attr name="selectionType" stringvalue="ANY"/&gt; &lt-- or EXACTLY_ONE --&gt;
     *   &lt;attr name="delegate" newvalue="action.pkg.YourAction"/&gt;
     * 
     *   &lt;!--
     *      Similar registration like in case of "callback" action.
     *      May be missing completely:
     *   --&gt;
     *   &lt;attr name="key" stringvalue="KeyInActionMap"/&gt;
     *   &lt;attr name="surviveFocusChange" boolvalue="false"/&gt; 
     *   &lt;attr name="fallback" newvalue="action.pkg.DefaultAction"/&gt;
     *   &lt;attr name="displayName" bundlevalue="your.pkg.Bundle#key"/&gt;
     *   &lt;attr name="iconBase" stringvalue="your/pkg/YourImage.png"/&gt;
     *   &lt;!-- if desired: &lt;attr name="noIconInMenu" boolvalue="false"/&gt; --&gt;
     * &lt;/file&gt;
     * </pre>
     * In the previous case there has to be a class with public default constructor
     * named <code>action.pkg.YourAction</code>. It has to implement
     * {@link ContextAwareAction} interface. Its {@link ContextAwareAction#createContextAwareInstance(org.openide.util.Lookup)}
     * method is called when the action is invoked. The passed in {@link Lookup}
     * contains instances of the <code>type</code> interface, <code>actionPerformed</code>
     * is then called on the returned clone.
     * <p>
     * Alternatively one can use support for simple dependency injection by
     * using following attributes:
     * <pre>
     * &lt;file name="action-pkg-ClassName.instance"&gt;
     *   &lt;attr name="instanceCreate" methodvalue="org.openide.awt.Actions.context"/&gt;
     *   &lt;attr name="type" stringvalue="org.netbeans.api.actions.Openable"/&gt;
     *   &lt;attr name="delegate" methodvalue="org.openide.awt.Action.inject"/&gt;
     *   &lt;attr name="selectionType" stringvalue="EXACTLY_ONE"/&gt;
     *   &lt;attr name="injectable" stringvalue="pkg.YourClass"/&gt;
     *   &lt;attr name="displayName" bundlevalue="your.pkg.Bundle#key"/&gt;
     *   &lt;attr name="iconBase" stringvalue="your/pkg/YourImage.png"/&gt;
     *   &lt;!-- if desired: &lt;attr name="noIconInMenu" boolvalue="false"/&gt; --&gt;
     * &lt;/file&gt;
     * </pre>
     * where <code>pkg.YourClass</code> is defined with public constructor taking
     * <code>type</code>:
     * <pre>
     * public final class YourClass implements ActionListener {
     *    Openable context;
     *
     *    public YourClass(Openable context) {
     *      this.context = context;
     *    }
     *
     *    public void actionPerformed(ActionEvent ev) {
     *       // do something with context
     *    }
     * }
     * </pre>
     * The instance of this class is created when the action is invoked and
     * its constructor is fed with the instance of <code>type</code> inside
     * the active context. <code>actionPerformed</code> method is called then.
     * <p>
     * To create action that handled multiselection
     * one can use following XML snippet:
     * <pre>
     * &lt;file name="action-pkg-ClassName.instance"&gt;
     *   &lt;attr name="type" stringvalue="org.netbeans.api.actions.Openable"/&gt;
     *   &lt;attr name="delegate" methodvalue="org.openide.awt.Action.inject"/&gt;
     *   &lt;attr name="selectionType" stringvalue="ANY"/&gt;
     *   &lt;attr name="injectable" stringvalue="pkg.YourClass"/&gt;
     *   &lt;attr name="displayName" bundlevalue="your.pkg.Bundle#key"/&gt;
     *   &lt;attr name="iconBase" stringvalue="your/pkg/YourImage.png"/&gt;
     *   &lt;!-- if desired: &lt;attr name="noIconInMenu" boolvalue="false"/&gt; --&gt;
     * &lt;/file&gt;
     * </pre>
     * Now the constructor of <code>YourClass</code> needs to have following
     * form:
     * <pre>
     * public final class YourClass implements ActionListener {
     *    List&lt;Openable&gt; context;
     *
     *    public YourClass(List&lt;Openable&gt; context) {
     *      this.context = context;
     *    }
     *  }
     * </pre>
     *
     * @param type the object to seek for in the active context
     * @param single shall there be just one or multiple instances of the object
     * @param surviveFocusChange shall the action remain enabled and act on
     *    previous selection even if no selection is currently in context?
     * @param delegate action to call when this action is invoked
     * @param key alternatively an action can be looked up in action map
     *    (see {@link Actions#callback(java.lang.String, javax.swing.Action, boolean, java.lang.String, java.lang.String, boolean)})
     * @param fallback action to fallback to (can be <code>null</code>)
     * @param displayName localized name of the action (including ampersand)
     * @param iconBase the location to the action icon
     * @param noIconInMenu true if this icon shall not have an item in menu
     * @return new instance of context aware action watching for type
     * @since 7.10
     */
    public static ContextAwareAction context(
        Class<?> type,
        boolean single,
        boolean surviveFocusChange,
        ContextAwareAction delegate,
        String key,
        Action fallback,
        String displayName, String iconBase, boolean noIconInMenu
    ) {
        Map<String,Object> map = new HashMap<String, Object>();
        map.put("key", key); // NOI18N
        map.put("surviveFocusChange", surviveFocusChange); // NOI18N
        map.put("fallback", fallback); // NOI18N
        map.put("delegate", delegate); // NOI18N
        map.put("type", type); // NOI18N
        map.put("selectionType", single ? ContextSelection.EXACTLY_ONE : ContextSelection.ANY);
        map.put("displayName", displayName); // NOI18N
        map.put("iconBase", iconBase); // NOI18N
        map.put("noIconInMenu", noIconInMenu); // NOI18N
        return context(map);
    }
    static ContextAwareAction context(Map fo) {
        return GeneralAction.context(fo);
    }
    static ContextAction.Performer<?> inject(final Map fo) {
        Object t = fo.get("selectionType"); // NOI18N
        if (ContextSelection.EXACTLY_ONE.toString().equals(t)) {
            return new InjectorExactlyOne(fo);
        }
        if (ContextSelection.ANY.toString().equals(t)) {
            return new InjectorAny(fo);
        }
        throw new IllegalStateException("no selectionType parameter in " + fo); // NOI18N
    }
    static ContextAction.Performer<?> performer(final Map fo) {
        String type = (String)fo.get("type");
        if (type.equals(Openable.class.getName())) {
            return new ActionDefaultPerfomer(0);
        }
        if (type.equals(Viewable.class.getName())) {
            return new ActionDefaultPerfomer(1);
        }
        if (type.equals(Editable.class.getName())) {
            return new ActionDefaultPerfomer(2);
        }
        if (type.equals(Closable.class.getName())) {
            return new ActionDefaultPerfomer(3);
        }
        if (type.equals(Printable.class.getName())) {
            return new ActionDefaultPerfomer(4);
        }
        throw new IllegalStateException(type);
    }
    
    /** Extracts help from action.
     */
    private static HelpCtx findHelp(Action a) {
        if (a instanceof HelpCtx.Provider) {
            return ((HelpCtx.Provider) a).getHelpCtx();
        } else {
            return HelpCtx.DEFAULT_HELP;
        }
    }

    // #40824 - when the text changes, it's too late to update in JmenuPlus.popup.show() (which triggers the updateState() in the MenuBridge).
    // check JmenuPlus.setPopupMenuVisible()
    static void prepareMenuBridgeItemsInContainer(Container c) {
        Component[] comps = c.getComponents();

        for (int i = 0; i < comps.length; i++) {
            if (comps[i] instanceof JComponent) {
                JComponent cop = (JComponent) comps[i];
                MenuBridge bridge = (MenuBridge) cop.getClientProperty("menubridgeresizehack");

                if (bridge != null) {
                    bridge.updateState(null);
                }
            }
        }
    }

    //
    // Methods for configuration of MenuItems
    //

    /** Method to prepare the margins and text positions.
    */
    static void prepareMargins(JMenuItem item, Action action) {
        item.setHorizontalTextPosition(JMenuItem.RIGHT);
        item.setHorizontalAlignment(JMenuItem.LEFT);
    }

    /** Updates value of the key
    * @param item item to update
    * @param action the action to update
    */
    static void updateKey(JMenuItem item, Action action) {
        if (!(item instanceof JMenu)) {
            item.setAccelerator((KeyStroke) action.getValue(Action.ACCELERATOR_KEY));
        }
    }

    private static Icon createDisabledIcon(Image img) {
        return new LazyDisabledIcon( img );
    }

    private static RGBImageFilter disabledButtonFilter() {
        if (DISABLED_BUTTON_FILTER == null) {
            DISABLED_BUTTON_FILTER = new DisabledButtonFilter();
        }

        return DISABLED_BUTTON_FILTER;
    }
    
    private static class LazyDisabledIcon implements Icon {
        private Image img;
        private Icon disabledIcon;
        
        public LazyDisabledIcon( Image img ) {
            assert null != img;
            this.img = img;
        }
        
        public void paintIcon(Component c, Graphics g, int x, int y) {
            getDisabledIcon().paintIcon(c, g, x, y);
        }

        public int getIconWidth() {
            return getDisabledIcon().getIconWidth();
        }

        public int getIconHeight() {
            return getDisabledIcon().getIconHeight();
        }
        
        private Icon getDisabledIcon() {
            if( null == disabledIcon ) {
                ImageProducer prod = new FilteredImageSource(img.getSource(), disabledButtonFilter());

                disabledIcon = new ImageIcon(Toolkit.getDefaultToolkit().createImage(prod), "");
            }
            return disabledIcon;
        }
    }
            

    /** Interface for the creating Actions.SubMenu. It provides the methods for
    * all items in submenu: name shortcut and perform method. Also has methods
    * for notification of changes of the model.
    */
    public static interface SubMenuModel {
        /** @return count of the submenu items. */
        public int getCount();

        /** Gets label for specific index
        * @param index of the submenu item
        * @return label for this menu item (or <code>null</code> for a separator)
        */
        public String getLabel(int index);

        /** Gets shortcut for specific index
        * @index of the submenu item
        * @return menushortcut for this menu item
        */

        //    public MenuShortcut getMenuShortcut(int index);

        /** Get context help for the specified item.
        * This can be used to associate help with individual items.
        * You may return <code>null</code> to just use the context help for
        * the associated system action (if any).
        * Note that only help IDs will work, not URLs.
        * @return the context help, or <code>null</code>
        */
        public HelpCtx getHelpCtx(int index);

        /** Perform the action on the specific index
        * @param index of the submenu item which should be performed
        */
        public void performActionAt(int index);

        /** Adds change listener for changes of the model.
        */
        public void addChangeListener(ChangeListener l);

        /** Removes change listener for changes of the model.
        */
        public void removeChangeListener(ChangeListener l);
    }

    /** Listener on showing/hiding state of the component.
    * Is attached to menu or toolbar item in prepareXXX methods and
    * method addNotify is called when the item is showing and
    * the method removeNotify is called when the item is hidding.
    * <P>
    * There is a special support listening on changes in the action and
    * if such change occures, updateState method is called to
    * reflect it.
    */
    private static abstract class Bridge extends Object implements PropertyChangeListener {
        /** component to work with */
        protected JComponent comp;

        /** action to associate */
        protected Action action;
        
        protected PropertyChangeListener listener;
        /** @param comp component
        * @param action the action
        */
        public Bridge(JComponent comp, Action action) {
            if(comp == null || action == null) {
                throw new IllegalArgumentException(
                    "None of the arguments can be null: comp=" + comp + //NOI18N
                    ", action=" + action); // NOI18N
            }
            this.comp = comp;
            this.action = action;

            // visibility listener
            listener = new VisL();
            Bridge.this.comp.addPropertyChangeListener(listener);

            if (Bridge.this.comp.isShowing()) {
                addNotify();
            }

            // associate context help, if applicable
            // [PENDING] probably belongs in ButtonBridge.updateState to make it dynamic
            HelpCtx help = findHelp(action);

            if ((help != null) && !help.equals(HelpCtx.DEFAULT_HELP) && (help.getHelpID() != null)) {
                HelpCtx.setHelpIDString(comp, help.getHelpID());
            }
        }

        /** Attaches listener to given action */
        public void addNotify() {
            action.addPropertyChangeListener(this);
            updateState(null);
        }

        /** Remove the listener */
        public void removeNotify() {
            action.removePropertyChangeListener(this);
        }

        /** @param changedProperty the name of property that has changed
        * or null if it is not known
        */
        public abstract void updateState(String changedProperty);

        /** Listener to changes of some properties.
        * Multicast - reacts to keymap changes and ancestor changes
        * together.
        */
        public void propertyChange(final PropertyChangeEvent ev) {
            //assert EventQueue.isDispatchThread();
            if (!EventQueue.isDispatchThread()) {
                new IllegalStateException("This must happen in the event thread!").printStackTrace();
            }

            updateState(ev.getPropertyName());
        }

        // Must be separate from general PCL, because otherwise
        // SystemAction.PROP_ENABLED -> updateState("enabled") ->
        // button.setEnabled(...) -> JButton.PROP_ENABLED ->
        // updateState("enabled") -> button.setEnabled(same)
        private class VisL implements PropertyChangeListener {
            VisL() {
            }

            public void propertyChange(final PropertyChangeEvent ev) {
                if ("ancestor".equals(ev.getPropertyName())) {
                    // ancestor change - decide if parent is null or not
                    if (ev.getNewValue() != null) {
                        addNotify();
                    } else {
                        removeNotify();
                    }
                }
            }
        }
    }

    /** Bridge between an action and button.
    */
    private static class ButtonBridge extends Bridge 
    implements ActionListener {
        /** UI logger to notify about invocation of an action */
        private static Logger UILOG = Logger.getLogger("org.netbeans.ui.actions"); // NOI18N
        
        /** the button */
        protected AbstractButton button;

        public ButtonBridge(AbstractButton button, Action action) {
            super(button, action);
            button.addActionListener(action);
            this.button = button;
            button.addActionListener(this);
        }
        
        public void actionPerformed(ActionEvent ev) {
            LogRecord rec = new LogRecord(Level.FINER, "UI_ACTION_BUTTON_PRESS"); // NOI18N
            rec.setParameters(new Object[] { button, button.getClass().getName(), action, action.getClass().getName(), action.getValue(Action.NAME) });
            rec.setResourceBundle(NbBundle.getBundle(Actions.class));
            rec.setResourceBundleName(Actions.class.getPackage().getName() + ".Bundle"); // NOI18N
            rec.setLoggerName(UILOG.getName());
            UILOG.log(rec);
        }

        protected void updateButtonIcon() {
            Object i = null;
            Object base = action.getValue("iconBase"); // NOI18N
            boolean useSmallIcon = true;
            Object prop = button.getClientProperty("PreferredIconSize"); //NOI18N

            if (prop instanceof Integer) {
                if (((Integer) prop).intValue() == 24) {
                    useSmallIcon = false;
                }
            }

            if (action instanceof SystemAction) {
                if (base instanceof String) {
                    String b = (String) base;
                    Image img = null;

                    if (!useSmallIcon) {
                        img = ImageUtilities.loadImage(insertBeforeSuffix(b, "24"), true); // NOI18N

                        if (img == null) {
                            img = ImageUtilities.loadImage(b, true);
                        }
                    } else {
                        img = ImageUtilities.loadImage(b, true);
                    }

                    if (img != null) {
                        i = new ImageIcon(img);
                        button.setIcon((Icon) i);
                        button.setDisabledIcon(createDisabledIcon(img));
                    } else {
                        SystemAction sa = (SystemAction) action;
                        i = sa.getIcon(useTextIcons());
                        button.setIcon((Icon) i);

                        if (i instanceof ImageIcon) {
                            button.setDisabledIcon(createDisabledIcon(((ImageIcon) i).getImage()));
                        }
                    }
                } else {
                    SystemAction sa = (SystemAction) action;
                    i = sa.getIcon(useTextIcons());
                    button.setIcon((Icon) i);

                    if (i instanceof ImageIcon) {
                        button.setDisabledIcon(createDisabledIcon(((ImageIcon) i).getImage()));
                    }
                }
            } else {
                //Try to get icon from iconBase for non SystemAction action
                if (base instanceof String) {
                    String b = (String) base;
                    Image img = null;

                    if (!useSmallIcon) {
                        img = ImageUtilities.loadImage(insertBeforeSuffix(b, "24"), true); // NOI18N

                        if (img == null) {
                            img = ImageUtilities.loadImage(b, true);
                        }
                    } else {
                        img = ImageUtilities.loadImage(b, true);
                    }

                    if (img != null) {
                        i = new ImageIcon(img);
                        button.setIcon((Icon) i);
                        button.setDisabledIcon(createDisabledIcon(img));
                    } else {
                        i = action.getValue(Action.SMALL_ICON);

                        if (i instanceof Icon) {
                            button.setIcon((Icon) i);

                            if (i instanceof ImageIcon) {
                                button.setDisabledIcon(createDisabledIcon(((ImageIcon) i).getImage()));
                            }
                        } else {
                            button.setIcon(nonNullIcon(null));
                        }
                    }
                } else {
                    i = action.getValue(Action.SMALL_ICON);

                    if (i instanceof Icon) {
                        button.setIcon((Icon) i);

                        if (i instanceof ImageIcon) {
                            button.setDisabledIcon(createDisabledIcon(((ImageIcon) i).getImage()));
                        }
                    } else {
                        button.setIcon(nonNullIcon(null));
                    }
                }
            }

            if (base instanceof String) {
                String b = (String) base;

                if (!useSmallIcon) {
                    b = insertBeforeSuffix(b, "24"); //NOI18N
                }

                Image img = null;

                if (i == null) {
                    // even for regular icon
                    img = ImageUtilities.loadImage(b, true);

                    if (img != null) {
                        button.setIcon(new ImageIcon(img));
                    }

                    i = img;
                }

                Image pImg = ImageUtilities.loadImage(insertBeforeSuffix(b, "_pressed"), true); // NOI18N

                if (pImg != null) {
                    button.setPressedIcon(new ImageIcon(pImg));
                }

                Image rImg = ImageUtilities.loadImage(insertBeforeSuffix(b, "_rollover"), true); // NOI18N

                if (rImg != null) {
                    button.setRolloverIcon(new ImageIcon(rImg));
                }

                Image dImg = ImageUtilities.loadImage(insertBeforeSuffix(b, "_disabled"), true); // NOI18N

                if (dImg != null) {
                    button.setDisabledIcon(ImageUtilities.image2Icon(dImg));
                } else if (img != null) {
                    button.setDisabledIcon(createDisabledIcon(img));
                }
            }
        }

        static String insertBeforeSuffix(String path, String toInsert) {
            String withoutSuffix = path;
            String suffix = ""; // NOI18N

            if (path.lastIndexOf('.') >= 0) {
                withoutSuffix = path.substring(0, path.lastIndexOf('.'));
                suffix = path.substring(path.lastIndexOf('.'), path.length());
            }

            return withoutSuffix + toInsert + suffix;
        }

        /** @param changedProperty the name of property that has changed
        * or null if it is not known
        */
        public void updateState(String changedProperty) {
            // note: "enabled" (== SA.PROP_ENABLED) hardcoded in AbstractAction
            if ((changedProperty == null) || changedProperty.equals(SystemAction.PROP_ENABLED)) {
                button.setEnabled(action.isEnabled());
            }

            if (
                (changedProperty == null) || changedProperty.equals(SystemAction.PROP_ICON) ||
                    changedProperty.equals(Action.SMALL_ICON) || changedProperty.equals("iconBase")
            ) { // NOI18N
                updateButtonIcon();
            }

            if (
                (changedProperty == null) || changedProperty.equals(Action.ACCELERATOR_KEY) ||
                    (changedProperty.equals(Action.NAME) && (action.getValue(Action.SHORT_DESCRIPTION) == null)) ||
                    changedProperty.equals(Action.SHORT_DESCRIPTION)
            ) {
                String tip = findKey(action);
                String toolTip = (String) action.getValue(Action.SHORT_DESCRIPTION);

                if (toolTip == null) {
                    toolTip = (String) action.getValue(Action.NAME);
                    toolTip = (toolTip == null) ? "" : cutAmpersand(toolTip);
                }

                if ((tip == null) || tip.equals("")) { // NOI18N
                    button.setToolTipText(toolTip);
                } else {
                    button.setToolTipText(
                        org.openide.util.NbBundle.getMessage(Actions.class, "FMT_ButtonHint", toolTip, tip)
                    );
                }
            }

            if (
                button instanceof javax.accessibility.Accessible &&
                    ((changedProperty == null) || changedProperty.equals(Action.NAME))
            ) {
                button.getAccessibleContext().setAccessibleName((String) action.getValue(Action.NAME));
            }
        }

        /** Should textual icons be used when lacking a real icon?
        * In the default implementation, <code>true</code>.
        * @return <code>true</code> if so
        */
        protected boolean useTextIcons() {
            return true;
        }
    }

    /** Bridge for button and boolean action.
    */
    private static class BooleanButtonBridge extends ButtonBridge {
        public BooleanButtonBridge(AbstractButton button, BooleanStateAction action) {
            super(button, action);
        }

        /** @param changedProperty the name of property that has changed
        * or null if it is not known
        */
        @Override
        public void updateState(String changedProperty) {
            super.updateState(changedProperty);

            if ((changedProperty == null) || changedProperty.equals(BooleanStateAction.PROP_BOOLEAN_STATE)) {
                button.setSelected(((BooleanStateAction) action).getBooleanState());
            }
        }
    }

    /** Menu item bridge.
    */
    private static class MenuBridge extends ButtonBridge {
        /** behave like menu or popup */
        private boolean popup;

        /** Constructor.
        * @param popup pop-up menu
        */
        public MenuBridge(JMenuItem item, Action action, boolean popup) {
            super(item, action);
            this.popup = popup;
            
            if (item instanceof Actions.MenuItem) {
                // addnotify/remove notify doens't make sense for menus and
                // popups.
                MenuBridge.this.comp.removePropertyChangeListener(listener);
            }
            
            if (popup) {
                prepareMargins(item, action);
            } else {
                // #40824 hack
                item.putClientProperty("menubridgeresizehack", this);

                // #40824 hack end.
            }
        }

        /** @param changedProperty the name of property that has changed
        * or null if it is not known
        */
        @Override
        public void updateState(String changedProperty) {
            if (this.button == null) {
                this.button = (AbstractButton) this.comp;
            }
            if ((changedProperty == null) || changedProperty.equals(SystemAction.PROP_ENABLED)) {
                button.setEnabled(action.isEnabled());
            }

            if ((changedProperty == null) || !changedProperty.equals(Action.ACCELERATOR_KEY)) {
                updateKey((JMenuItem) comp, action);
            }

            if (!popup) {
                if (
                    (changedProperty == null) || changedProperty.equals(SystemAction.PROP_ICON) ||
                        changedProperty.equals(Action.SMALL_ICON) || changedProperty.equals("iconBase")
                ) { // NOI18N
                    updateButtonIcon();
                }
            }

            if ((changedProperty == null) || changedProperty.equals(Action.NAME)) {
                Object s = null;
                if (popup) {
                    s = action.getValue("popupText"); // NOI18N
                }
                if (s == null) {
                    s = action.getValue("menuText"); // NOI18N
                }
                if (s == null) {
                    s = action.getValue(Action.NAME);
                }

                if (s instanceof String) {
                    setMenuText(((JMenuItem) comp), (String) s, true);

                    //System.out.println("Menu item: " + s);
                    //System.out.println("Action class: " + action.getClass());
                }
            }
        }

        @Override
        protected void updateButtonIcon() {
            Object i = null;
            Object obj = action.getValue("noIconInMenu"); //NOI18N

            if (Boolean.TRUE.equals(obj)) {
                //button.setIcon(nonNullIcon(null));
                return;
            }

            if (action instanceof SystemAction) {
                SystemAction sa = (SystemAction) action;
                i = sa.getIcon(useTextIcons());
                button.setIcon((Icon) i);

                if (i instanceof ImageIcon) {
                    button.setDisabledIcon(createDisabledIcon(((ImageIcon) i).getImage()));
                }
            } else {
                i = action.getValue(Action.SMALL_ICON);

                if (i instanceof Icon) {
                    button.setIcon((Icon) i);

                    if (i instanceof ImageIcon) {
                        button.setDisabledIcon(createDisabledIcon(((ImageIcon) i).getImage()));
                    }
                } else {
                    //button.setIcon(nonNullIcon(null));
                }
            }

            Object base = action.getValue("iconBase"); // NOI18N

            if (base instanceof String) {
                String b = (String) base;
                Image img = null;

                if (i == null) {
                    // even for regular icon
                    img = ImageUtilities.loadImage(b, true);

                    if (img != null) {
                        button.setIcon(new ImageIcon(img));
                        button.setDisabledIcon(createDisabledIcon(img));
                    }
                }

                Image pImg = ImageUtilities.loadImage(insertBeforeSuffix(b, "_pressed"), true); // NOI18N

                if (pImg != null) {
                    button.setPressedIcon(new ImageIcon(pImg));
                }

                Image rImg = ImageUtilities.loadImage(insertBeforeSuffix(b, "_rollover"), true); // NOI18N

                if (rImg != null) {
                    button.setRolloverIcon(new ImageIcon(rImg));
                }

                Image dImg = ImageUtilities.loadImage(insertBeforeSuffix(b, "_disabled"), true); // NOI18N

                if (dImg != null) {
                    button.setDisabledIcon(new ImageIcon(dImg));
                } else if (img != null) {
                    button.setDisabledIcon(createDisabledIcon(img));
                }
            }
        }

        @Override
        protected boolean useTextIcons() {
            return false;
        }
    }

    /** Check menu item bridge.
    */
    private static final class CheckMenuBridge extends BooleanButtonBridge {
        /** is popup or menu */
        private boolean popup;
        private boolean hasOwnIcon = false;

        /** Popup menu */
        public CheckMenuBridge(JCheckBoxMenuItem item, BooleanStateAction action, boolean popup) {
            super(item, action);
            this.popup = popup;

            if (popup) {
                prepareMargins(item, action);
            }

            Object base = action.getValue("iconBase"); //NOI18N
            Object i = null;

            if (action instanceof SystemAction) {
                i = action.getValue(SystemAction.PROP_ICON);
            } else {
                i = action.getValue(Action.SMALL_ICON);
            }

            hasOwnIcon = (base != null) || (i != null);
        }

        /** @param changedProperty the name of property that has changed
        * or null if it is not known
        */
        @Override
        public void updateState(String changedProperty) {
            super.updateState(changedProperty);

            if ((changedProperty == null) || !changedProperty.equals(Action.ACCELERATOR_KEY)) {
                updateKey((JMenuItem) comp, action);
            }

            if ((changedProperty == null) || changedProperty.equals(Action.NAME)) {
                Object s = action.getValue(Action.NAME);

                if (s instanceof String) {
                    setMenuText(((JMenuItem) comp), (String) s, true);
                }
            }
        }

        @Override
        protected void updateButtonIcon() {
            if (hasOwnIcon) {
                super.updateButtonIcon();

                return;
            }

            if (!popup) {
                boolean jdk16orNewer = "1.6".compareTo(System.getProperty("java.version")) <= 0;
                button.setIcon(ImageUtilities.loadImageIcon(jdk16orNewer ? "org/openide/resources/actions/empty.gif" : "org/openide/resources/actions/gap.gif", true)); // NOI18N
            }
        }

        @Override
        protected boolean useTextIcons() {
            return false;
        }
    }


    /** The class that listens to the menu item selections and forwards it to the
     * action class via the performAction() method.
     */
    private static class ISubActionListener implements java.awt.event.ActionListener {
        int index;
        SubMenuModel support;
        
        public ISubActionListener(int index, SubMenuModel support) {
            this.index = index;
            this.support = support;
        }
        
        /** called when a user clicks on this menu item */
        public void actionPerformed(ActionEvent e) {
            support.performActionAt(index);
        }
    }

    /** Sub menu bridge 2.
    */
    private static final class SubMenuBridge extends MenuBridge implements ChangeListener, DynamicMenuContent {
        /** model to obtain subitems from */
        private SubMenuModel model;
        private List<JMenuItem> currentOnes;
        private JMenuItem single;
        private JMenu multi;
        /** Constructor.
        */
        public SubMenuBridge(JMenuItem one, JMenu more, Action action, SubMenuModel model, boolean popup) {
            super(one, action, popup);
            single = one;
            multi = more;
            setMenuText(multi, (String)action.getValue(Action.NAME), popup);
            prepareMargins(one, action);
            prepareMargins(more, action);
            currentOnes = new ArrayList<JMenuItem>();
            this.model = model;
        }

        /** Called when model changes. Regenerates the model.
        */
        public void stateChanged(ChangeEvent ev) {
            //assert EventQueue.isDispatchThread();
            if (!EventQueue.isDispatchThread()) {
                new IllegalStateException("This must happen in the event thread!").printStackTrace();
            }
            // change in keys or in submenu model
//            checkVisibility();
        }
        
        @Override
        public void updateState(String changedProperty) {
            super.updateState(changedProperty);
//            checkVisibility();
        }        

        
        
        public JComponent[] getMenuPresenters() {
            return synchMenuPresenters(null);
        }
        
        public JComponent[] synchMenuPresenters(JComponent[] items) {
            currentOnes.clear();
            int cnt = model.getCount();
            
            if (cnt == 0) {
                updateState(null);
                currentOnes.add(single);
                // menu disabled
                single.setEnabled(false);
            } else if (cnt == 1) {
                updateState(null);
                currentOnes.add(single);
                single.setEnabled(action.isEnabled());
                // generate without submenu
                HelpCtx help = model.getHelpCtx(0);
                associateHelp(single, (help == null) ? findHelp(action) : help);
            } else {
                currentOnes.add(multi);
                multi.removeAll();
                //TODO
                Mnemonics.setLocalizedText(multi, (String)action.getValue(Action.NAME));
            
                boolean addSeparator = false;
                int count = model.getCount();
            
                for (int i = 0; i < count; i++) {
                    String label = model.getLabel(i);
                
                    //          MenuShortcut shortcut = support.getMenuShortcut(i);
                    if (label == null) {
                        addSeparator = multi.getItemCount() > 0;
                    } else {
                        if (addSeparator) {
                            multi.addSeparator();
                            addSeparator = false;
                        }
                    
                        //       if (shortcut == null)
                        // (Dafe) changed to support mnemonics in item labels
                        JMenuItem item = new JMenuItem();
                        Mnemonics.setLocalizedText(item, label);
                    
                        // attach the shortcut to the first item
                        if (i == 0) {
                            updateKey(item, action);
                        }
                    
                        item.addActionListener(new ISubActionListener(i, model));
                    
                        HelpCtx help = model.getHelpCtx(i);
                        associateHelp(item, (help == null) ? findHelp(action) : help);
                        multi.add(item);
                    }
                
                    associateHelp(multi, findHelp(action));
                }
                multi.setEnabled(true);
            }
            return currentOnes.toArray(new JMenuItem[currentOnes.size()]);
            
        }

        private void associateHelp(JComponent comp, HelpCtx help) {
            if ((help != null) && !help.equals(HelpCtx.DEFAULT_HELP) && (help.getHelpID() != null)) {
                HelpCtx.setHelpIDString(comp, help.getHelpID());
            } else {
                HelpCtx.setHelpIDString(comp, null);
            }
        }
    }
    //
    //
    // The presenter classes
    //
    //

    /**
     * Extension of Swing menu item with connection to
     * system actions.
     */
    public static class MenuItem extends javax.swing.JMenuItem implements DynamicMenuContent {
        static final long serialVersionUID = -21757335363267194L;
        private Actions.Bridge bridge;
        /** Constructs a new menu item with the specified label
        * and no keyboard shortcut and connects it to the given SystemAction.
        * @param aAction the action to which this menu item should be connected
        * @param useMnemonic if true, the menu try to find mnemonic in action label
        */
        public MenuItem(SystemAction aAction, boolean useMnemonic) {
            Actions.connect(this, aAction, !useMnemonic);
        }

        /** Constructs a new menu item with the specified label
        * and no keyboard shortcut and connects it to the given SystemAction.
        * @param aAction the action to which this menu item should be connected
        * @param useMnemonic if true, the menu try to find mnemonic in action label
        */
        public MenuItem(Action aAction, boolean useMnemonic) {
            Actions.connect(this, aAction, !useMnemonic);
        }
        
        void setBridge(Actions.Bridge br) {
            bridge = br;
        }

        public JComponent[] synchMenuPresenters(JComponent[] items) {
            if (bridge != null) {
                bridge.updateState(null);
            }
            return getMenuPresenters();
        }

        public JComponent[] getMenuPresenters() {
            return new JComponent[] {this};
        }
        
    }

    /** CheckboxMenuItem extends the java.awt.CheckboxMenuItem and adds
    * a connection to boolean state actions. The ActCheckboxMenuItem
    * processes the ItemEvents itself and calls the action.seBooleanState() method.
    * It also tracks the enabled and boolean state of the action and reflects it
    * as its visual enabled/check state.
    *
    * @author   Ian Formanek, Jan Jancura
    */
    public static class CheckboxMenuItem extends javax.swing.JCheckBoxMenuItem {
        static final long serialVersionUID = 6190621106981774043L;

        /** Constructs a new ActCheckboxMenuItem with the specified label
        *  and connects it to the given BooleanStateAction.
        * @param aAction the action to which this menu item should be connected
        * @param useMnemonic if true, the menu try to find mnemonic in action label
        */
        public CheckboxMenuItem(BooleanStateAction aAction, boolean useMnemonic) {
            Actions.connect(this, aAction, !useMnemonic);
        }
    }

    /** Component shown in toolbar, representing an action.
    * @deprecated extends deprecated ToolbarButton
    */
    public static class ToolbarButton extends org.openide.awt.ToolbarButton {
        static final long serialVersionUID = 6564434578524381134L;

        public ToolbarButton(SystemAction aAction) {
            super(null);
            Actions.connect(this, aAction);
        }

        public ToolbarButton(Action aAction) {
            super(null);
            Actions.connect(this, aAction);
        }

        /**
         * Gets the maximum size of this component.
         * @return A dimension object indicating this component's maximum size.
         * @see #getMinimumSize
         * @see #getPreferredSize
         * @see java.awt.LayoutManager
         */
        @Override
        public Dimension getMaximumSize() {
            return this.getPreferredSize();
        }

        @Override
        public Dimension getMinimumSize() {
            return this.getPreferredSize();
        }
    }

    /** The Component for BooleeanState action that is to be shown
    * in a toolbar.
    *
    * @deprecated extends deprecated ToolbarToggleButton
    */
    public static class ToolbarToggleButton extends org.openide.awt.ToolbarToggleButton {
        static final long serialVersionUID = -4783163952526348942L;

        /** Constructs a new ActToolbarToggleButton for specified action */
        public ToolbarToggleButton(BooleanStateAction aAction) {
            super(null, false);
            Actions.connect(this, aAction);
        }

        /**
         * Gets the maximum size of this component.
         * @return A dimension object indicating this component's maximum size.
         * @see #getMinimumSize
         * @see #getPreferredSize
         * @see java.awt.LayoutManager
         */
        @Override
        public Dimension getMaximumSize() {
            return this.getPreferredSize();
        }

        @Override
        public Dimension getMinimumSize() {
            return this.getPreferredSize();
        }
    }
    

    /** SubMenu provides easy way of displaying submenu items based on
    * SubMenuModel.
    */
    public static class SubMenu extends JMenuPlus implements DynamicMenuContent {
        static final long serialVersionUID = -4446966671302959091L;

        private SubMenuBridge bridge;

        /** Constructs a new ActMenuItem with the specified label
        * and no keyboard shortcut and connects it to the given SystemAction.
        * No icon is used by default.
        * @param aAction the action to which this menu item should be connected
        * @param model the support for the menu items
        */
        public SubMenu(SystemAction aAction, SubMenuModel model) {
            this(aAction, model, true);
        }

        /** Constructs a new ActMenuItem with the specified label
        * and no keyboard shortcut and connects it to the given SystemAction.
        * No icon is used by default.
        * @param aAction the action to which this menu item should be connected
        * @param model the support for the menu items
        * @param popup whether this is a popup menu
        */
        public SubMenu(SystemAction aAction, SubMenuModel model, boolean popup) {
            this((Action) aAction, model, popup);
        }

        /** Constructs a new ActMenuItem with the specified label
        * and no keyboard shortcut and connects it to the given SystemAction.
        * No icon is used by default.
        * @param aAction the action to which this menu item should be connected
        * @param model the support for the menu items
        * @param popup whether this is a popup menu
        */
        public SubMenu(Action aAction, SubMenuModel model, boolean popup) {
            bridge = new SubMenuBridge(new JMenuItem(), this, aAction, model, popup);

            // set at least the name to have reasonable bounds
            bridge.updateState(Action.NAME);
        }
        
        public JComponent[] getMenuPresenters() {
            return bridge.getMenuPresenters();
        }
        
        public JComponent[] synchMenuPresenters(JComponent[] items) {
            return bridge.synchMenuPresenters(items);
        }
        
    }

    /**
     * SPI for supplying alternative implementation of connection between actions and presenters.
     * The implementations
     * of this interface are being looked up in the default lookup.
     * If there is no implemenation in the lookup the default implementation
     * is used.
     * @see Lookup#getDefault()
     * @since org.openide.awt 6.9
     */
    public interface ButtonActionConnector {
        /**
         * Connects the action to the supplied button.
         * @return true if the connection was successful and no
         *    further actions are needed. If false is returned the
         *    default connect implementation is called
         */
        boolean connect(AbstractButton button, Action action);
        /**
         * Connects the action to the supplied JMenuItem.
         * @return true if the connection was successful and no
         *    further actions are needed. If false is returned the
         *    default connect implementation is called
         */
        boolean connect(JMenuItem item, Action action, boolean popup);
    }

    private static class DisabledButtonFilter extends RGBImageFilter {
        DisabledButtonFilter() {
            canFilterIndexColorModel = true;
        }

        public int filterRGB(int x, int y, int rgb) {
            // Reduce the color bandwidth in quarter (>> 2) and Shift 0x88.
            return (rgb & 0xff000000) + 0x888888 + ((((rgb >> 16) & 0xff) >> 2) << 16) +
            ((((rgb >> 8) & 0xff) >> 2) << 8) + (((rgb) & 0xff) >> 2);
        }

        // override the superclass behaviour to not pollute
        // the heap with useless properties strings. Saves tens of KBs
        @Override
        public void setProperties(Hashtable props) {
            props = (Hashtable) props.clone();
            consumer.setProperties(props);
        }
    }
}
