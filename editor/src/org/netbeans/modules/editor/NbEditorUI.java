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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.editor;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Iterator;
import java.util.Map;
import java.util.prefs.Preferences;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.editor.BaseKit;
import org.netbeans.editor.EditorUI;
import org.netbeans.editor.Utilities;
import org.netbeans.editor.ext.ExtKit;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.ActionPerformer;
import org.openide.util.actions.SystemAction;
import org.openide.util.actions.CallbackSystemAction;
import org.openide.windows.TopComponent;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.settings.SimpleValueNames;
import org.netbeans.editor.StatusBar;
import org.netbeans.modules.editor.impl.CustomizableSideBar;
import org.netbeans.modules.editor.impl.CustomizableSideBar.SideBarPosition;
import org.netbeans.modules.editor.impl.SearchBar;
import org.netbeans.modules.editor.impl.StatusLineFactories;
import org.netbeans.modules.editor.lib.EditorPreferencesDefaults;
import org.openide.text.CloneableEditorSupport;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.windows.WindowManager;

/**
* Editor UI
*
* @author Miloslav Metelka
* @version 1.00
*/

public class NbEditorUI extends EditorUI {

    private FocusListener focusL;

    private boolean attached = false;
    private ChangeListener listener;
    
    /**
     *
     * @deprecated - use {@link #attachSystemActionPerformer(String)} instead
     */
    protected SystemActionUpdater createSystemActionUpdater(
        String editorActionName, boolean updatePerformer, boolean syncEnabling) {
        return new SystemActionUpdater(editorActionName, updatePerformer, syncEnabling);
    }

    public NbEditorUI() {
        focusL = new FocusAdapter() {
            public @Override void focusGained(FocusEvent evt) {
                // Refresh file object when component made active
                Document doc = getDocument();
                if (doc != null) {
                    DataObject dob = NbEditorUtilities.getDataObject(doc);
                    if (dob != null) {
                        final FileObject fo = dob.getPrimaryFile();
                        if (fo != null) {
                            // Fixed #48151 - posting the refresh outside of AWT thread
                            RequestProcessor.getDefault().post(new Runnable() {
                                public void run() {
                                    fo.refresh();
                                }
                            });
                        }
                    }
                }

                // Check if editor is docked and if so then use global status bar.
                JTextComponent component = getComponent();
                // Check if component is inside main window
                boolean underMainWindow = (SwingUtilities.isDescendingFrom(component,
                WindowManager.getDefault().getMainWindow()));
                getStatusBar().setVisible(!underMainWindow); // Note: no longer checking the preferences settting
            }

            @Override
            public void focusLost(FocusEvent e) {
                // Clear global panel
                StatusLineFactories.clearStatusLine();
            }


        };
    }
    
    
    private static Lookup getContextLookup(java.awt.Component component){
        Lookup lookup = null;
        for (java.awt.Component c = component; c != null; c = c.getParent()) {
            if (c instanceof Lookup.Provider) {
                lookup = ((Lookup.Provider)c).getLookup ();
                if (lookup != null) {
                    break;
                }
            }
        }
        return lookup;
    }
    
    protected void attachSystemActionPerformer(String editorActionName){
        new NbEditorUI.SystemActionPerformer(editorActionName);
    }

    protected @Override void installUI(JTextComponent c) {
        super.installUI(c);

        if (!attached){
            attachSystemActionPerformer(SearchBar.IncrementalSearchForwardAction.ACTION_NAME);
            attachSystemActionPerformer(ExtKit.replaceAction);
            attachSystemActionPerformer(ExtKit.gotoAction);
            attachSystemActionPerformer(ExtKit.showPopupMenuAction);

            // replacing DefaultEditorKit.deleteNextCharAction by BaseKit.removeSelectionAction
            // #41223
            // attachSystemActionPerformer(BaseKit.removeSelectionAction);
            
            attached = true;
        }
        
        c.addFocusListener(focusL);
    }


    protected @Override void uninstallUI(JTextComponent c) {
        super.uninstallUI(c);

        c.removeFocusListener(focusL);
    }
    
    @Override
    protected JComponent createExtComponent() {

        JTextComponent component = getComponent();

        // Add the scroll-pane with the component to the center
        JScrollPane scroller = new JScrollPane(component);
        scroller.getViewport().setMinimumSize(new Dimension(4,4));

        // remove default scroll-pane border, winsys will handle borders itself 
        Border empty = BorderFactory.createEmptyBorder();
        // Important:  Do not delete or use null instead, will cause
        //problems on GTK L&F.  Must set both scroller border & viewport
        //border! - Tim
        scroller.setBorder(empty);
        scroller.setViewportBorder(empty);

        // extComponent will be a panel
        JComponent ec = new JPanel(new BorderLayout());
        ec.putClientProperty(JTextComponent.class, component);
        ec.add(scroller);

        // Initialize sidebars
        Map<SideBarPosition, JComponent> sideBars = CustomizableSideBar.getSideBars(component);
        processSideBars(sideBars, ec);
        
        if (listener == null){
            listener = new SideBarsListener(component);
            CustomizableSideBar.addChangeListener(NbEditorUtilities.getMimeType(component), listener);
        }
        
        // Initialize the corner component
        initGlyphCorner(scroller);

        return ec;
    }
    

    public @Override boolean isLineNumberEnabled() {
        Preferences prefs = MimeLookup.getLookup(MimePath.EMPTY).lookup(Preferences.class);
        return prefs.getBoolean(SimpleValueNames.LINE_NUMBER_VISIBLE, EditorPreferencesDefaults.defaultLineNumberVisible);
    }

    public @Override void setLineNumberEnabled(boolean lineNumberEnabled) {
        Preferences prefs = MimeLookup.getLookup(MimePath.EMPTY).lookup(Preferences.class);
        boolean visible = prefs.getBoolean(SimpleValueNames.LINE_NUMBER_VISIBLE, EditorPreferencesDefaults.defaultLineNumberVisible);
        prefs.putBoolean(SimpleValueNames.LINE_NUMBER_VISIBLE, !visible);
    }
    
    private static void processSideBars(Map sideBars, JComponent ec) {
        JScrollPane scroller = (JScrollPane) ec.getComponent(0);

        // Remove all existing sidebars
        ec.removeAll();

        // Add the scroller and the new sidebars
        ec.add(scroller);
        scroller.setRowHeader(null);
        scroller.setColumnHeaderView(null);
//        final MouseDispatcher mouse = new MouseDispatcher((JTextComponent) ec.getClientProperty(JTextComponent.class));
        for (Iterator entries = sideBars.entrySet().iterator(); entries.hasNext(); ) {
            Map.Entry entry = (Map.Entry) entries.next();
            SideBarPosition position = (SideBarPosition) entry.getKey();
            JComponent sideBar = (JComponent) entry.getValue();
            
//            if (position.getPosition() == SideBarPosition.WEST) {
//                JPanel p = new JPanel(new BorderLayout()) {
//
//                    @Override
//                    public void addNotify() {
//                        super.addNotify();
//                        infiltrateContainer(this, mouse, true);
//                    }
//
//                    @Override
//                    public void removeNotify() {
//                        infiltrateContainer(this, mouse, false);
//                        super.removeNotify();
//                    }
//
//                };
//                p.add(sideBar, BorderLayout.CENTER);
//                sideBar = p;
//            }
            
            if (position.isScrollable()) {
                if (position.getPosition() == SideBarPosition.WEST) {
                    scroller.setRowHeaderView(sideBar);
                } else {
                    if (position.getPosition() == SideBarPosition.NORTH) {
                        scroller.setColumnHeaderView(sideBar);
                    } else {
                        throw new IllegalArgumentException("Unsupported side bar position, scrollable = true, position=" + position.getBorderLayoutPosition()); // NOI18N
                    }
                }
            } else {
                ec.add(sideBar, position.getBorderLayoutPosition());
            }
        }
    }
    
//    private static void infiltrateContainer(Container c, MouseDispatcher mouse, boolean add) {
//        for (Component comp : c.getComponents()) {
//            if (add) {
//                comp.addMouseListener(mouse);
//                comp.addMouseMotionListener(mouse);
//            } else {
//                comp.removeMouseListener(mouse);
//                comp.removeMouseMotionListener(mouse);
//            }
//            if (comp instanceof Container) {
//                Container cont = (Container) comp;
//                if (add) {
//                    cont.addContainerListener(mouse);
//                } else {
//                    cont.removeContainerListener(mouse);
//                }
//                infiltrateContainer(cont, mouse,add);
//            }
//        }
//
//    }
//
//    private static final class MouseDispatcher implements MouseListener, MouseMotionListener, ContainerListener {
//
//        private final Component target;
//
//        public MouseDispatcher(Component comp) {
//            this.target = comp;
//        }
//
//        private void redispatch(MouseEvent oe) {
//            if (oe.isConsumed()) {
//                return;
//            }
//            MouseEvent ne = SwingUtilities.convertMouseEvent(
//                    oe.getComponent(), oe, target);
//            target.dispatchEvent(ne);
//        }
//
//        public void mouseDragged(MouseEvent e) {
//            redispatch(e);
//        }
//
//        public void mouseMoved(MouseEvent e) {
//            redispatch(e);
//        }
//
//        public void mouseClicked(MouseEvent e) {
//            redispatch(e);
//        }
//
//        public void mousePressed(MouseEvent e) {
//            redispatch(e);
//        }
//
//        public void mouseReleased(MouseEvent e) {
//            redispatch(e);
//        }
//
//        public void mouseEntered(MouseEvent e) {
//            redispatch(e);
//        }
//
//        public void mouseExited(MouseEvent e) {
//            redispatch(e);
//        }
//
//        public void componentAdded(ContainerEvent e) {
//            Component comp = e.getChild();
//            if (comp instanceof Container) {
//                infiltrateContainer((Container) comp, this, true);
//            } else {
//                comp.addMouseListener(this);
//                comp.addMouseMotionListener(this);
//            }
//        }
//
//        public void componentRemoved(ContainerEvent e) {
//            Component comp = e.getChild();
//            if (comp instanceof Container) {
//                infiltrateContainer((Container) comp, this, false);
//            } else {
//                comp.removeMouseListener(this);
//                comp.removeMouseMotionListener(this);
//            }
//        }
//
//    }
    
    protected @Override JToolBar createToolBarComponent() {
        return new NbEditorToolBar(getComponent());
    }

    private class SystemActionPerformer implements PropertyChangeListener{

        private String editorActionName;

        private Action editorAction;

        private Action systemAction;
        
        
        SystemActionPerformer(String editorActionName) {
            this.editorActionName = editorActionName;

            synchronized (NbEditorUI.this.getComponentLock()) {
                // if component already installed in EditorUI simulate installation
                JTextComponent component = getComponent();
                if (component != null) {
                    propertyChange(new PropertyChangeEvent(NbEditorUI.this,
                                                           EditorUI.COMPONENT_PROPERTY, null, component));
                }

                NbEditorUI.this.addPropertyChangeListener(this);
            }
        }
        
        private void attachSystemActionPerformer(JTextComponent c){
            if (c == null) return;

            Action action = getEditorAction(c);
            if (action == null) return;

            Action globalSystemAction = getSystemAction(c);
            if (globalSystemAction == null) return;

            if (globalSystemAction instanceof CallbackSystemAction){
                Object key = ((CallbackSystemAction)globalSystemAction).getActionMapKey();
                c.getActionMap ().put (key, action);
            }                        
        }
        
        private void detachSystemActionPerformer(JTextComponent c){
            if (c == null) return;

            Action action = getEditorAction(c);
            if (action == null) return;

            Action globalSystemAction = getSystemAction(c);
            if (globalSystemAction == null) return;

            if (globalSystemAction instanceof CallbackSystemAction){
                Object key = ((CallbackSystemAction)globalSystemAction).getActionMapKey();
                ActionMap am = c.getActionMap();
                if (am != null) {
                    Object ea = am.get(key);
                    if (action.equals(ea)) {
                        am.remove(key);
                    }
                }
            }                        
                                
        }
        
        
        public synchronized void propertyChange(PropertyChangeEvent evt) {
            String propName = evt.getPropertyName();

            if (EditorUI.COMPONENT_PROPERTY.equals(propName)) {
                JTextComponent component = (JTextComponent)evt.getNewValue();

                if (component != null) { // just installed
                    component.addPropertyChangeListener(this);
                    attachSystemActionPerformer(component);
                } else { // just deinstalled
                    component = (JTextComponent)evt.getOldValue();
                    component.removePropertyChangeListener(this);
                    detachSystemActionPerformer(component);
                }
            }
        }   

        private synchronized Action getEditorAction(JTextComponent component) {
            if (editorAction == null) {
                BaseKit kit = Utilities.getKit(component);
                if (kit != null) {
                    editorAction = kit.getActionByName(editorActionName);
                }
            }
            return editorAction;
        }

        private Action getSystemAction(JTextComponent c) {
            if (systemAction == null) {
                Action ea = getEditorAction(c);
                if (ea != null) {
                    String saClassName = (String)ea.getValue(NbEditorKit.SYSTEM_ACTION_CLASS_NAME_PROPERTY);
                    if (saClassName != null) {
                        Class saClass;
                        try {
                            saClass = Class.forName(saClassName);
                        } catch (Throwable t) {
                            saClass = null;
                        }

                        if (saClass != null) {
                            systemAction = SystemAction.get(saClass);
                            if (systemAction instanceof ContextAwareAction){
                                Lookup lookup = getContextLookup(c);
                                if (lookup!=null){
                                    systemAction = ((ContextAwareAction)systemAction).createContextAwareInstance(lookup);
                                }
                            }
                            
                        }
                    }
                }
            }
            return systemAction;
        }
        
    }
    

    /**
     *
     * @deprecated use SystemActionPerformer instead
     */
    public final class SystemActionUpdater
        implements PropertyChangeListener, ActionPerformer {

        private String editorActionName;

        private boolean updatePerformer;

        private boolean syncEnabling;

        private Action editorAction;

        private Action systemAction;

        private PropertyChangeListener enabledPropertySyncL;
        
        private boolean listeningOnTCRegistry;


        SystemActionUpdater(String editorActionName, boolean updatePerformer,
                            boolean syncEnabling) {
            this.editorActionName = editorActionName;
            this.updatePerformer = updatePerformer;
            this.syncEnabling = syncEnabling;

            synchronized (NbEditorUI.this.getComponentLock()) {
                // if component already installed in EditorUI simulate installation
                JTextComponent component = getComponent();
                if (component != null) {
                    propertyChange(new PropertyChangeEvent(NbEditorUI.this,
                                                           EditorUI.COMPONENT_PROPERTY, null, component));
                }

                NbEditorUI.this.addPropertyChangeListener(this);
            }
        }

        public void editorActivated() {
            Action ea = getEditorAction();
            Action sa = getSystemAction();
            if (ea != null && sa != null) {
                if (updatePerformer) {
                    if (ea.isEnabled() && sa instanceof CallbackSystemAction) {
                        ((CallbackSystemAction)sa).setActionPerformer(this);
                    }
                }

                if (syncEnabling) {
                    if (enabledPropertySyncL == null) {
                        enabledPropertySyncL = new EnabledPropertySyncListener(sa);
                    }
                    ea.addPropertyChangeListener(enabledPropertySyncL);
                }
            }
        }

        public void editorDeactivated() {
            Action ea = getEditorAction();
            Action sa = getSystemAction();
            if (ea != null && sa != null) {
                /*        if (sa instanceof CallbackSystemAction) {
                          CallbackSystemAction csa = (CallbackSystemAction)sa;
                          if (csa.getActionPerformer() == this) {
                            csa.setActionPerformer(null);
                          }
                        }
                */

                if (syncEnabling && enabledPropertySyncL != null) {
                    ea.removePropertyChangeListener(enabledPropertySyncL);
                }
            }
        }

        private void reset() {
            if (enabledPropertySyncL != null) {
                editorAction.removePropertyChangeListener(enabledPropertySyncL);
            }

            /*      if (systemAction != null) {
                    if (systemAction instanceof CallbackSystemAction) {
                      CallbackSystemAction csa = (CallbackSystemAction)systemAction;
                      if (!csa.getSurviveFocusChange() || csa.getActionPerformer() == this) {
                        csa.setActionPerformer(null);
                      }
                    }
                  }
            */

            editorAction = null;
            systemAction = null;
            enabledPropertySyncL = null;
        }

        /** Perform the callback action */
        public void performAction(SystemAction action) {
            JTextComponent component = getComponent();
            Action ea = getEditorAction();
            if (component != null && ea != null) {
                ea.actionPerformed(new ActionEvent(component, 0, "")); // NOI18N
            }
        }
        
        private void startTCRegistryListening() {
            if (!listeningOnTCRegistry) {
                listeningOnTCRegistry = true;
                TopComponent.getRegistry().addPropertyChangeListener(this);
            }
        }
        
        private void stopTCRegistryListening() {
            if (listeningOnTCRegistry) {
                listeningOnTCRegistry = false;
                TopComponent.getRegistry().removePropertyChangeListener(this);
            }
        }

        public synchronized void propertyChange(PropertyChangeEvent evt) {
            String propName = evt.getPropertyName();

            if (TopComponent.Registry.PROP_ACTIVATED.equals (propName)) {
                TopComponent activated = (TopComponent)evt.getNewValue();

                if(activated instanceof CloneableEditorSupport.Pane)
                    editorActivated();
                else
                    editorDeactivated();
            } else if (EditorUI.COMPONENT_PROPERTY.equals(propName)) {
                JTextComponent component = (JTextComponent)evt.getNewValue();

                if (component != null) { // just installed
                    component.addPropertyChangeListener(this);
                    if (component.isDisplayable()) {
                        startTCRegistryListening();
                    }

                } else { // just deinstalled
                    component = (JTextComponent)evt.getOldValue();
                    component.removePropertyChangeListener(this);
                    stopTCRegistryListening();
                }

                reset();

            } else if ("editorKit".equals(propName)) { // NOI18N
                reset();

            } else if ("ancestor".equals(propName)) { // NOI18N
                if (((Component)evt.getSource()).isDisplayable()) { // now displayable
                    startTCRegistryListening();
                } else { // not displayable
                    stopTCRegistryListening();
                }
            }
        }

        private synchronized Action getEditorAction() {
            if (editorAction == null) {
                BaseKit kit = Utilities.getKit(getComponent());
                if (kit != null) {
                    editorAction = kit.getActionByName(editorActionName);
                }
            }
            return editorAction;
        }

        private Action getSystemAction() {
            if (systemAction == null) {
                Action ea = getEditorAction();
                if (ea != null) {
                    String saClassName = (String)ea.getValue(NbEditorKit.SYSTEM_ACTION_CLASS_NAME_PROPERTY);
                    if (saClassName != null) {
                        Class saClass;
                        try {
                            saClass = Class.forName(saClassName);
                        } catch (Throwable t) {
                            saClass = null;
                        }

                        if (saClass != null) {
                            systemAction = SystemAction.get(saClass);
                        }
                    }
                }
            }
            return systemAction;
        }

        protected @Override void finalize() throws Throwable {
            reset();
        }

    }

    /** Listener that listen on changes of the "enabled" property
    * and if changed it changes the same property of the action
    * given in constructor.
    */
    static class EnabledPropertySyncListener implements PropertyChangeListener {

        Action action;

        EnabledPropertySyncListener(Action actionToBeSynced) {
            this.action = actionToBeSynced;
        }

        public void propertyChange(PropertyChangeEvent evt) {
            if ("enabled".equals(evt.getPropertyName())) { // NOI18N
                action.setEnabled(((Boolean)evt.getNewValue()).booleanValue());
            }
        }

    }

    private static final class SideBarsListener implements ChangeListener {

        private final JTextComponent component;
        
        public SideBarsListener(JTextComponent component) {
            this.component = component;
        }
        
        public void stateChanged(ChangeEvent e) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    EditorUI eui = Utilities.getEditorUI(component);
                    if (eui != null) {
                        JComponent ec = eui.getExtComponent();
                        if (ec != null) {
                            Map newMap = CustomizableSideBar.getSideBars(component);
                            processSideBars(newMap, ec);
                            ec.revalidate();
                            ec.repaint();
                        }
                    }
                }
            });
        }
    } //End of SideBarPosition class
}
