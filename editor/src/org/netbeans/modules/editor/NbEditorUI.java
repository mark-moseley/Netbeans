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

package org.netbeans.modules.editor;

import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.Action;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.editor.BaseKit;
import org.netbeans.editor.EditorUI;
import org.netbeans.editor.Utilities;
import org.netbeans.editor.ext.ExtEditorUI;
import org.netbeans.editor.ext.ExtKit;
import org.openide.TopManager;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.actions.ActionPerformer;
import org.openide.util.actions.SystemAction;
import org.openide.util.actions.CallbackSystemAction;
import org.openide.windows.TopComponent;
import org.openide.text.CloneableEditor;
import javax.swing.event.ChangeEvent;
import org.netbeans.editor.GuardedDocument;
import javax.swing.SwingUtilities;
import org.netbeans.editor.BaseDocument;
import javax.swing.text.Caret;
import javax.swing.JEditorPane;
import javax.swing.JComponent;

/**
* Editor UI
*
* @author Miloslav Metelka
* @version 1.00
*/

public class NbEditorUI extends ExtEditorUI {

    private FocusListener focusL;

    private SystemActionUpdater findActionUpdater;
    private SystemActionUpdater replaceActionUpdater;
    private SystemActionUpdater gotoActionUpdater;
    private SystemActionUpdater removeSelectionActionUpdater;

    protected SystemActionUpdater createSystemActionUpdater(
        String editorActionName, boolean updatePerformer, boolean syncEnabling) {
        return new SystemActionUpdater(editorActionName, updatePerformer, syncEnabling);
    }

    public NbEditorUI() {
        // Start syncing the selected system actions
        findActionUpdater = createSystemActionUpdater(ExtKit.findAction, true, false);
        replaceActionUpdater = createSystemActionUpdater(ExtKit.replaceAction, true, false);
        gotoActionUpdater = createSystemActionUpdater(ExtKit.gotoAction, true, false);
        removeSelectionActionUpdater = createSystemActionUpdater(ExtKit.removeSelectionAction,
                                       true, true);

        focusL = new FocusAdapter() {
                     public void focusGained(FocusEvent evt) {
                         // Refresh file object when component made active
                         Document doc = getDocument();
                         if (doc != null) {
                             DataObject dob = NbEditorUtilities.getDataObject(doc);
                             if (dob != null) {
                                 FileObject fo = dob.getPrimaryFile();
                                 if (fo != null) {
                                     fo.refresh();
                                 }
                             }
                         }
                     }
                 };

    }
    
    
    public void stateChanged(final ChangeEvent evt) {
        super.stateChanged(evt);
        SwingUtilities.invokeLater(
        new Runnable() {            
            private void setEnabledGuardedAction(Action a){
                JTextComponent component = getComponent();
                if (component == null)  return;
                BaseDocument bdoc = getDocument();
                if (bdoc instanceof GuardedDocument){
                    GuardedDocument gdoc = (GuardedDocument)bdoc;
                    boolean inGuardedBlock = (gdoc.isPosGuarded(component.getCaretPosition()) ||
                        gdoc.isPosGuarded(component.getSelectionStart()) ||
                        gdoc.isPosGuarded(component.getSelectionEnd()));
                    a.setEnabled(!inGuardedBlock);
                }
            }
            
            public void run() {
                boolean selectionVisible = ((Caret)evt.getSource()).isSelectionVisible();
                JTextComponent component = getComponent();
                if (component == null) return;
                BaseKit kit = Utilities.getKit(component);
                if (kit == null) return;

                Action a = kit.getActionByName(BaseKit.pasteAction);
                if (a != null) {
                    setEnabledGuardedAction(a);
                }
                
                a = kit.getActionByName(BaseKit.removeSelectionAction);
                if (a != null) {
                    setEnabledGuardedAction(a);
                }

                a = kit.getActionByName(BaseKit.cutAction);
                if (a != null) {
                    if (selectionVisible){
                        setEnabledGuardedAction(a);
                    }
                }
                
            }
            
        }
        );
    }

    protected void installUI(JTextComponent c) {
        super.installUI(c);

        c.addFocusListener(focusL);
    }


    protected void uninstallUI(JTextComponent c) {
        super.uninstallUI(c);

        c.removeFocusListener(focusL);
    }
    
    protected JComponent createExtComponent() {
        JComponent ec = super.createExtComponent();
        NbToolbarSupport.checkToolbar(ec, (JEditorPane)getComponent());
        return ec;
        
/*        String toolbarFolderPath = "Editors/" + mimeType + "/Toolbars";

        org.openide.filesystems.FileObject f = TopManager.getDefault().
        getRepository().getDefaultFileSystem().findResource(toolbarFolderPath);

        if (f != null) {
            try {
                DataObject dob = DataObject.find(f);
                DataFolder df = (DataFolder)dob.getCookie(DataFolder.class);
                if (df != null) {
                    
                    ToolbarPool tp = new ToolbarPool(df);
                    extComponent.add(tp, BorderLayout.NORTH);
                    tp.waitFinished();
                    tp.setConfiguration(ToolbarPool.DEFAULT_CONFIGURATION);
                    tp.setBorder(null);
                    
                    Toolbar[] all = tp.getToolbars();
                    for (int i = 0; i < all.length; i++) {
                        Toolbar t = all[i];
                        
                        // PENDING Hack to get rid of toolbar bump
                        t.addContainerListener(new ContainerAdapter() {
                            public void componentAdded(ContainerEvent evt) {
                                Container toolbarContainer = (Container)evt.getSource();
                                if (toolbarContainer.getComponentCount() == 2
                                    && toolbarContainer.getComponent(0).getClass().getName().equals(
                                        "org.openide.awt.Toolbar$ToolbarBump")
                                ) {
                                    toolbarContainer.getComponent(0).setVisible(false); // bump
                                    toolbarContainer.getComponent(1).setVisible(false); // separator
                                }
                            }
                        });
                        
                        // Remove toolbar insets
                        t.setBorder(null);
                    }
                }
 
            } catch (org.openide.loaders.DataObjectNotFoundException e) {
                e.printStackTrace();
            }
        }
*/
 
    }

    public final class SystemActionUpdater
        implements PropertyChangeListener, ActionPerformer {

        private String editorActionName;

        private boolean updatePerformer;

        private boolean syncEnabling;

        private Action editorAction;

        private Action systemAction;

        private PropertyChangeListener enabledPropertySyncL;


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

        public synchronized void propertyChange(PropertyChangeEvent evt) {
            String propName = evt.getPropertyName();

            if (TopComponent.Registry.PROP_ACTIVATED.equals (propName)) {
                TopComponent activated = (TopComponent)evt.getNewValue();

                if(activated instanceof CloneableEditor)
                    editorActivated();
                else
                    editorDeactivated();
            } else if (EditorUI.COMPONENT_PROPERTY.equals(propName)) {
                JTextComponent component = (JTextComponent)evt.getNewValue();
                TopComponent.Registry regs = TopComponent.getRegistry();

                if (component != null) { // just installed
                    component.addPropertyChangeListener(this);
                    regs.addPropertyChangeListener(this);

                } else { // just deinstalled
                    component = (JTextComponent)evt.getOldValue();

                    component.removePropertyChangeListener(this);
                    regs.removePropertyChangeListener(this);
                }

                reset();

            } else if ("editorKit".equals(propName)) { // NOI18N

                reset();
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
                            if (NbEditorUtilities.getTopManager() != null) {
                                systemAction = SystemAction.get(saClass);
                            }
                        }
                    }
                }
            }
            return systemAction;
        }

        protected void finalize() throws Throwable {
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

}
