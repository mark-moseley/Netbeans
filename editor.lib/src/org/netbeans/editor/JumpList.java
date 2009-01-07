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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URL;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.JTextComponent;
import javax.swing.text.BadLocationException;
import org.netbeans.modules.editor.lib.NavigationHistory;
import org.openide.modules.PatchedPublic;
import org.openide.util.WeakListeners;

/**
 * The list of marked positions in text components.
 *
 * @author Miloslav Metelka
 * @version 1.01
 */
public final class JumpList {

    private static final Logger LOG = Logger.getLogger(JumpList.class.getName());
    
    private static final WeakPropertyChangeSupport support = new WeakPropertyChangeSupport();
    
    private static PropertyChangeListener listener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent evt) {
            support.firePropertyChange(JumpList.class, null, null, null);
        }
    };
    
    static {
        NavigationHistory.getNavigations().addPropertyChangeListener(
            WeakListeners.propertyChange(listener, NavigationHistory.getNavigations()));
    }
    
    static void addPropertyChangeListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }
    
    /**
     * Adds the caret position of the active <code>JTextComponent</code> to the
     * list. If the active component can't be determined this method does nothing.
     * 
     * <p class="nonormative">The active <code>JTextComponent</code> is obtained
     * from the system by tracking components that gain focus as a users uses them.
     * In Netbeans IDE the active component is usually the component selected in
     * the IDE's editor area.
     * 
     * @see #addEntry(JTextComponent, int)
     */
    public static void checkAddEntry() {
        JTextComponent c = Utilities.getLastActiveComponent();
        if (c != null) {
            addEntry(c, c.getCaret().getDot());
        }
    }

    /**
     * Adds the caret position of the provided <code>JTextComponent</code> to the
     * list.
     * 
     * @param c The offset of this component's caret will be added to the list.
     * 
     * @see #addEntry(JTextComponent, int)
     */
    public static void checkAddEntry(JTextComponent c) {
        addEntry(c, c.getCaret().getDot());
    }

    /**
     * The same as {@link #addEntry(JTextComponent, int)}.
     * 
     * @deprecated Use {@link #addEntry(JTextComponent, int)} instead.
     */
    public static void checkAddEntry(JTextComponent c, int pos) {
        addEntry(c, pos);
    }

    /**
     * Adds a new entry to the list. If the component and the position passed
     * in are the same as those from the last entry in the list, nothing
     * will be added.
     * 
     * @param c The component to add to the list.
     * @param pos The offset of the component's caret to add to the list.
     */
    public static void addEntry(JTextComponent c, int pos) {
        try {
            NavigationHistory.getNavigations().markWaypoint(c, pos, false, false);
        } catch (BadLocationException e) {
            LOG.log(Level.WARNING, "Can't add position to the navigation history.", e); //NOI18N
        }
    }

    /**
     * Navigates to the component and position from the previous entry in the list.
     * It will skip all entries with the same component and position as the
     * current caret position in the component passed in.
     * 
     * <p class="nonnormative">This method will try to move focus to the component
     * stored in the list and set its caret position.
     * 
     * @param c The component to check for the current position.
     */
    public static void jumpPrev(JTextComponent c) {
        NavigationHistory.Waypoint wpt = NavigationHistory.getNavigations().navigateBack();
        show(wpt);
    }

    /**
     * Navigates to the component and position from the previous entry in the list.
     * It will skip all entries with the same component as the one passed in, but
     * it does not perform any checks on the positions.
     * 
     * <p class="nonnormative">This method will try to move focus to the component
     * stored in the list and set its caret position.
     * 
     * @param c The component to check for the current position.
     */
    public static void jumpPrevComponent(JTextComponent c) {
        List<NavigationHistory.Waypoint> list = NavigationHistory.getNavigations().getPreviousWaypoints();
        for(NavigationHistory.Waypoint wpt : list) {
            JTextComponent wptComp = wpt.getComponent();
            if (wptComp != null && wptComp != c) {
                show(wpt);
                return;
            }
        }
    }

    /**
     * Checks if there is the previous entry in the list.
     * 
     * @return <code>true</code> if there is a previous entry in the list and it
     *   is possible to use {@link #jumpPrev(JTextComponent)} or {@link #jumpPrevComponent(JTextComponent)}
     *   for navigation. Otherwise <code>false</code>.
     */
    public static boolean hasPrev() {
        return NavigationHistory.getNavigations().hasPreviousWaypoints();
    }

    /**
     * Navigates to the component and position from the next entry in the list.
     * It will skip all entries with the same component and position as the
     * current caret position in the component passed in.
     * 
     * <p class="nonnormative">This method will try to move focus to the component
     * stored in the list and set its caret position.
     * 
     * @param c The component to check for the current position.
     */
    public static void jumpNext(JTextComponent c) {
        NavigationHistory.Waypoint wpt = NavigationHistory.getNavigations().navigateForward();
        show(wpt);
    }

    /**
     * Navigates to the component and position from the next entry in the list.
     * It will skip all entries with the same component as the one passed in, but
     * it does not perform any checks on the positions.
     * 
     * <p class="nonnormative">This method will try to move focus to the component
     * stored in the list and set its caret position.
     * 
     * @param c The component to check for the current position.
     */
    public static void jumpNextComponent(JTextComponent c) {
        List<NavigationHistory.Waypoint> list = NavigationHistory.getNavigations().getNextWaypoints();
        for(NavigationHistory.Waypoint wpt : list) {
            JTextComponent wptComp = wpt.getComponent();
            if (wptComp != null && wptComp != c) {
                show(wpt);
                return;
            }
        }
    }
    
    /**
     * Checks if there is the next entry in the list.
     * 
     * @return <code>true</code> if there is a previous entry in the list and it
     *   is possible to use {@link #jumpPrev(JTextComponent)} or {@link #jumpPrevComponent(JTextComponent)}
     *   for navigation. Otherwise <code>false</code>.
     */
    public static boolean hasNext() {
        return NavigationHistory.getNavigations().hasNextWaypoints();
    }

    /**
     * @return Unspecified string.
     * @deprecate Should have never been public.
     */
    public static String dump() {
        StringBuilder sb = new StringBuilder();
        
        sb.append("Previous waypoints: {\n"); //NOI18N
        List<NavigationHistory.Waypoint> prev = NavigationHistory.getNavigations().getPreviousWaypoints();
        for(NavigationHistory.Waypoint wpt : prev) {
            URL url = wpt.getUrl();
            sb.append("    ").append(url.toString()).append("\n"); //NOI18N
        }
        sb.append("}\n"); //NOI18N
        
        sb.append("Next waypoints: {\n"); //NOI18N
        List<NavigationHistory.Waypoint> next = NavigationHistory.getNavigations().getNextWaypoints();
        for(NavigationHistory.Waypoint wpt : next) {
            URL url = wpt.getUrl();
            sb.append("    ").append(url.toString()).append("\n"); //NOI18N
        }
        sb.append("}\n"); //NOI18N
        
        return sb.toString();
    }

    /** Just to prevent instantialization. */
    @PatchedPublic
    private JumpList() {
    }
    
    private static void show(NavigationHistory.Waypoint wpt) {
        JTextComponent c = wpt == null ? null : wpt.getComponent();
        if (c != null) {
            if (Utilities.getLastActiveComponent() != c) {

                Utilities.requestFocus(c); // possibly request for the component
                Registry.activate(c);
            }

            int offset = wpt.getOffset();
            if (offset >= 0 && offset <= c.getDocument().getLength()) {
                c.getCaret().setDot(offset); // set the dot
            }
        }
    }
    
    /**
     * An entry in the list with <code>JTextComponent</code> and a position in
     * its <code>Document</code>.
     */
    public static final class Entry {

        private Entry(JTextComponent component, int offset, Entry last) throws BadLocationException {
        }

        /**
         * Gets the offset of the position maintaind by this entry.
         * 
         * @return An offset within this entry's component's document or -1 if
         *   this entry is not valid anymore.
         */
        public int getPosition() {
            return -1;
        }

        /**
         * Gets the component maintained by this entry.
         * 
         * @return The component or <code>null</code> if this entry is not valid
         *   anymore.
         */
        public JTextComponent getComponent() {
            return null;
        }

        /** 
         * Navigates to the component and position maintained by this entry.
         * 
         * <p class="nonnormative">This method will try to move focus to the component
         * stored in this entry and set its caret to the position maintained by this entry.
         * 
         * @return <code>true</code> if the navigation was successful, <code>false</code>
         *   otherwise.
         */
        public boolean setDot() {
            return false;
        }
    } // End of Entry class

}
