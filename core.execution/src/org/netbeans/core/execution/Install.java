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

package org.netbeans.core.execution;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.beans.Introspector;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyEditorManager;
import java.util.*;
import javax.swing.*;

import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.actions.ExecuteAction;
import org.openide.execution.ExecutorTask;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerPanel;
import org.openide.explorer.view.ListView;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.modules.ModuleInstall;
import org.openide.nodes.*;
import org.openide.util.*;
import org.openide.util.actions.SystemAction;

import org.netbeans.TopSecurityManager;

import org.openide.ErrorManager;

/**
 * Registers security manager for execution.
 * Also shows Pending Tasks dialog at shutdown time.
 * Also adds/removes specific beaninfo and property editor search paths.
 * @author Jesse Glick
 */
public class Install extends ModuleInstall {
    
    private static final String BEANINFO_PATH
        = "org.netbeans.core.execution.beaninfo"; // NOI18N
    private static final String EDITOR_PATH
        = "org.netbeans.core.execution.beaninfo.editors"; // NOI18N
    
    public void restored() {
        TopSecurityManager.register(SecMan.DEFAULT);
        // load neccessary SystemOptions because of ExecuteAction setup
        SharedClassObject.findObject (ExecutionSettings.class, true);
        // run classes int Startup folder
        FileObject startup = Repository.getDefault().getDefaultFileSystem().findResource("Startup"); // NOI18N
        if (startup != null && startup.getChildren().length > 0) {
            startFolder(DataFolder.findFolder(startup));
        }

        // Add beaninfo search path.
        String[] sp = Introspector.getBeanInfoSearchPath();
        java.util.List paths = Arrays.asList(sp);
        if(!paths.contains(BEANINFO_PATH)) {
            paths = new ArrayList(paths);
            paths.add(BEANINFO_PATH);
            Introspector.setBeanInfoSearchPath(
                (String[])paths.toArray(new String[0]));
        }
        
        // Add property editor search path.
        sp = PropertyEditorManager.getEditorSearchPath();
        paths = Arrays.asList(sp);
        if(!paths.contains(EDITOR_PATH)) {
            paths = new ArrayList(paths);
            paths.add(EDITOR_PATH);
            PropertyEditorManager.setEditorSearchPath(
                (String[])paths.toArray(new String[0]));
        }
        
        // XXX #37543
        ExecutionViewAction.installExecutionListener();
    }
    
    public void uninstalled() {
        // XXX #37543
        ExecutionViewAction.uninstallExecutionListener();

        showPendingTasks();

        TopSecurityManager.unregister(SecMan.DEFAULT);

        // Remove beaninfo search path.
        String[] sp = Introspector.getBeanInfoSearchPath();
        java.util.List paths = Arrays.asList(sp);
        if(paths.contains(BEANINFO_PATH)) {
            paths = new ArrayList(paths);
            paths.remove(BEANINFO_PATH);
            Introspector.setBeanInfoSearchPath(
                (String[])paths.toArray(new String[0]));
        }
        
        // Remove property editor seach path.
        sp = PropertyEditorManager.getEditorSearchPath();
        paths = Arrays.asList(sp); 
        if(paths.contains(EDITOR_PATH)) {
            paths = new ArrayList(paths);
            paths.remove(EDITOR_PATH);
            PropertyEditorManager.setEditorSearchPath(
                (String[])paths.toArray(new String[0]));
        }
    }
    
    public boolean closing() {
        return showPendingTasks();
    }
    
    // From NonGui
    
    /** Starts a folder by executing all of its executable children
    * @param f the folder
    */
    private static void startFolder(DataFolder f) {
        DataObject[] obj = f.getChildren();
        if (obj.length == 0) return;
        ErrorManager err = ErrorManager.getDefault();
        if (err.isLoggable(ErrorManager.WARNING)) {
            StringBuffer b = new StringBuffer("Warning - using the Startup folder is deprecated. Found objects:"); // NOI18N
            for (int i = 0; i < obj.length; i++) {
                b.append(' '); // NOI18N
                b.append(obj[i].getPrimaryFile().getPath());
            }
            err.log(ErrorManager.WARNING, b.toString());
        }
        ExecuteAction.execute(obj, true);
    }

    // Remainder moved from ExitDialog:
    
    /** Shows dialog which waits for finishing of pending tasks,
     * (currently actions only) and offers to user to leave IDE 
     * immediatelly interrupting those tasks.
     * @return <code>true</code> if to continue with the action
     * <code>false</code> if the action to cancel
     */
    private static boolean showPendingTasks() {
        if(getPendingTasks().isEmpty()) {
            return true;
        }
  
        ExplorerPanel panel = createExplorerPanel();
        
        final Dialog[] dialog = new Dialog[1];
        final Node root = new AbstractNode(new PendingChildren());

        panel.getExplorerManager().setRootContext(root);
        panel.getExplorerManager().addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                // Listen on changes of pending tasks and if all has finished
                // close the dialog.
                if(ExplorerManager.PROP_EXPLORED_CONTEXT.equals(evt.getPropertyName())) {
                    if(dialog[0] != null && getPendingTasks().isEmpty()) {
                        dialog[0].setVisible(false);
                    }
                }
            }
        });

        final JButton exitOption = new JButton(
            NbBundle.getBundle(Install.class).getString("LAB_EndTasks"));
        exitOption.setMnemonic(NbBundle.getBundle(Install.class).
            getString("LAB_EndTasksMnem").charAt(0));
        // No default button.
        exitOption.setDefaultCapable(false);
        exitOption.getAccessibleContext().setAccessibleDescription(
            NbBundle.getBundle(Install.class).getString("ACSD_EndTasks"));
        
        DialogDescriptor dd = new DialogDescriptor(
            panel,
            NbBundle.getBundle(Install.class).getString("CTL_PendingTitle"),
            true, // modal
            new Object[] {
                exitOption,
                DialogDescriptor.CANCEL_OPTION
            },
            null,
            DialogDescriptor.DEFAULT_ALIGN,
            null,
            new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    if(evt.getSource() == exitOption) {
                        killPendingTasks();
                        dialog[0].setVisible(false);
                    }
                }
            }
        );
        // #33135 - no Help button for this dialog
        dd.setHelpCtx(null);

        if(!getPendingTasks().isEmpty()) {
            root.addNodeListener(new NodeAdapter() {
                public void childrenRemoved(NodeMemberEvent evt) {
                    if(dialog[0] != null && getPendingTasks().isEmpty()) {
                        dialog[0].setVisible(false);
                    }
                }
            });

            dialog[0] = DialogDisplayer.getDefault().createDialog(dd);
            
            dialog[0].addWindowListener(new java.awt.event.WindowAdapter() {
                public void windowOpened(java.awt.event.WindowEvent evt) {
                    // Dialog was opened but pending tasks could disappear
                    // inbetween.
                    if(getPendingTasks().isEmpty()) {
                        dialog[0].setVisible(false);
                    }
                }
            });
            
            dialog[0].show();
            dialog[0].dispose();

            if(dd.getValue() == DialogDescriptor.CANCEL_OPTION
            || dd.getValue() == DialogDescriptor.CLOSED_OPTION) {
                return false;
            }
            
        }
        
        return true;
    }
 
    /** Creates dialod for showing pending tasks. */
    private static ExplorerPanel createExplorerPanel() {
        ExplorerPanel panel = new ExplorerPanel();
        
        panel.setLayout(new GridBagLayout());
        
        GridBagConstraints cons = new GridBagConstraints();
        cons.gridx = 0;
        cons.gridy = 0;
        cons.weightx = 1.0D;
        cons.fill = GridBagConstraints.HORIZONTAL;
        cons.insets = new Insets(11, 11, 0, 12);

        JLabel label = new JLabel(NbBundle.getBundle(Install.class)
            .getString("LAB_PendingTasks"));
        label.setDisplayedMnemonic(NbBundle.getBundle(Install.class)
            .getString("LAB_PendingTasksMnem").charAt(0));
        
        panel.add(label, cons);
        
        cons.gridy = 1;
        cons.weighty = 1.0D;
        cons.fill = GridBagConstraints.BOTH;
        cons.insets = new Insets(7, 11, 0, 12);

        ListView view = new ListView();
        label.setLabelFor(view);
        
        panel.add(view, cons);
        
        view.getAccessibleContext().setAccessibleDescription(NbBundle.getBundle(Install.class)
            .getString("ACSD_PendingTasks"));
        panel.getAccessibleContext().setAccessibleDescription(NbBundle.getBundle(Install.class)
            .getString("ACSD_PendingTitle"));

        return panel;
    }
    
    /** Gets pending (running) tasks. Used as keys 
     * for pending dialog root node children. Currently it gets pending
     * actions only. */
    private static Collection getPendingTasks() {
        
        ArrayList pendingTasks = new ArrayList( 10 );
        // XXX no access to running actions at the moment
        //pendingTasks.addAll(CallableSystemAction.getRunningActions());
        pendingTasks.addAll(org.netbeans.core.ModuleActions.getDefaultInstance().getRunningActions());
        
        if ( !Boolean.getBoolean( "netbeans.full.hack" ) ) { // NOI18N
            // Avoid showing the tasks in the dialog when running internal tests
            ExecutionEngine ee = ExecutionEngine.getExecutionEngine();
            if (ee != null) {
                pendingTasks.addAll(ee.getRunningTasks());
            }
        }
        
        // [PENDING] When it'll be added another types of tasks (locks etc.)
        // add them here to the list. Then you need to create also a nodes
        // for them in PendingChildren.createNodes.
        
        return pendingTasks;
    }
    
    /** Ends penidng tasks. */
    private static void killPendingTasks() {
        // XXX
        //CallableSystemAction.killRunningActions();
        killRunningExecutors();
        
        // [PENDING] When it'll be added another types of tasks (locks etc.)
        // kill them here.
   }
    
   /** Tries to kill running executions */
   private static void killRunningExecutors() {
       ExecutionEngine ee = ExecutionEngine.getExecutionEngine();
       if (ee == null) {
           return;
       }
       ArrayList tasks = new ArrayList(ee.getRunningTasks());
       
       for ( Iterator it = tasks.iterator(); it.hasNext(); ) {
           ExecutorTask et = (ExecutorTask) it.next();
           if ( !et.isFinished() ) {
               et.stop();
           }
       }
       
   }

    /** Children showing pending tasks. */
    private static class PendingChildren extends Children.Keys implements ExecutionListener {

        /** Listens on changes of sources from getting the tasks from.
         * Currently on module actions only. */
        private PropertyChangeListener propertyListener;
        
        
        /** Constructs new children. */
        public PendingChildren() {
            /* XXX no equiv yet in CallableSystemAction
            propertyListener = new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent evt) {
                    if (ModuleActions.PROP_RUNNING_ACTIONS.equals(evt.getPropertyName())) {
                        setKeys(getPendingTasks());
                    }
                }
            };

            ModuleActions.getDefault().addPropertyChangeListener(
                org.openide.util.WeakListeners.propertyChange (propertyListener, ModuleActions.getDefault())
            );
             */
            
            ExecutionEngine ee = ExecutionEngine.getExecutionEngine();
            if (ee != null) {
                ee.addExecutionListener(this);
            }
        }

        /** Implements superclass abstract method. Creates nodes from key.
         * @return <code>PendingActionNode</code> if key is of 
         * <code>Action</code> type otherwise <code>null</code> */
        protected Node[] createNodes(Object key) {
            Node n = null;
            if(key instanceof Action) {
                n = new PendingActionNode((Action)key);
            }
            else if ( key instanceof ExecutorTask ) {
                AbstractNode an = new AbstractNode( Children.LEAF );
                an.setName(key.toString());
                an.setDisplayName(NbBundle.getMessage(Install.class, "CTL_PendingExternalProcess2", 
                    // getExecutionEngine() had better be non-null, since getPendingTasks gave an ExecutorTask:
                    ExecutionEngine.getExecutionEngine().getRunningTaskName((ExecutorTask) key)));
                an.setIconBase( "org/netbeans/core/resources/execution" ); //NOI18N
                n = an;
            }
            return n == null ? null : new Node[] { n };
        }

        /** Implements superclass abstract method. */
        protected void addNotify() {
            setKeys(getPendingTasks());
            super.addNotify();            
        }
        
        /** Implements superclass abstract method. */
        protected void removeNotify() {
            setKeys(Collections.EMPTY_SET);
            super.removeNotify();
            ExecutionEngine ee = ExecutionEngine.getExecutionEngine();
            if (ee != null) {
                ee.removeExecutionListener(this);
            }
        }
        
        // ExecutionListener implementation ------------------------------------
        
        public void startedExecution( ExecutionEvent ev ) {
            setKeys(getPendingTasks());
        }
        
        public void finishedExecution( ExecutionEvent ev ) {
            setKeys(getPendingTasks());
        }
        
    } //  End of class PendingChildren.

    
    /** Node representing pending action task. */
    private static class PendingActionNode extends AbstractNode {

        /** Icon retrieved from action if it is 
         * of <code>SystemAction</code> instance. */
        private Icon icon;
        
        /** Creates node for action. */
        public PendingActionNode(Action action) {
            super(Children.LEAF);
            
            String actionName = (String)action.getValue(Action.NAME);
            if (actionName == null) {
                actionName = ""; // NOI18N
            }
            actionName = org.openide.awt.Actions.cutAmpersand(actionName);
            setName(actionName);
            setDisplayName(actionName + " " // NOI18N
                + NbBundle.getBundle(Install.class)
                    .getString("CTL_ActionInProgress"));
            
            if(action instanceof SystemAction) {
                this.icon = ((SystemAction)action).getIcon();
            }
        }

        /** Overrides superclass method. */
        public Image getIcon(int type) {
            if(icon != null) {
                Image im = new BufferedImage(
                    icon.getIconWidth(),
                    icon.getIconHeight(),
                    BufferedImage.TYPE_INT_ARGB
                );

                icon.paintIcon(null, im.getGraphics(), 0, 0);
                
                return im;
            } else {
                return super.getIcon(type);
            }
        }

        /** Overrides superclass method.
         * @return empty array of actions */
        protected SystemAction[] createActions() {
            return new SystemAction[0];
        }
        
    } // End of class PendingActionNode.
    
}
