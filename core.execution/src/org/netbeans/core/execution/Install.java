/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.core.execution;

import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.image.BufferedImage;
import java.beans.Introspector;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyEditorManager;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.text.DefaultEditorKit;
import org.netbeans.TopSecurityManager;
import org.netbeans.progress.module.Controller;
import org.netbeans.progress.module.InternalHandle;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.Mnemonics;
import org.openide.execution.ExecutorTask;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerUtils;
import org.openide.explorer.view.ListView;
import org.openide.modules.ModuleInstall;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.NodeEvent;
import org.openide.nodes.NodeListener;
import org.openide.nodes.NodeMemberEvent;
import org.openide.nodes.NodeReorderEvent;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;

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

        // Add beaninfo search path.
        String[] sp = Introspector.getBeanInfoSearchPath();
        java.util.List<String> paths = Arrays.asList(sp);
        if(!paths.contains(BEANINFO_PATH)) {
            paths = new ArrayList<String>(paths);
            paths.add(BEANINFO_PATH);
            Introspector.setBeanInfoSearchPath(
                paths.toArray(new String[0]));
        }
        
        // Add property editor search path.
        sp = PropertyEditorManager.getEditorSearchPath();
        paths = Arrays.asList(sp);
        if(!paths.contains(EDITOR_PATH)) {
            paths = new ArrayList<String>(paths);
            paths.add(EDITOR_PATH);
            PropertyEditorManager.setEditorSearchPath(
                paths.toArray(new String[0]));
        }
    }
    
    public void uninstalled() {
        showPendingTasks();

        TopSecurityManager.unregister(SecMan.DEFAULT);

        // Remove beaninfo search path.
        String[] sp = Introspector.getBeanInfoSearchPath();
        java.util.List<String> paths = Arrays.asList(sp);
        if(paths.contains(BEANINFO_PATH)) {
            paths = new ArrayList<String>(paths);
            paths.remove(BEANINFO_PATH);
            Introspector.setBeanInfoSearchPath(
                paths.toArray(new String[0]));
        }
        
        // Remove property editor seach path.
        sp = PropertyEditorManager.getEditorSearchPath();
        paths = Arrays.asList(sp); 
        if(paths.contains(EDITOR_PATH)) {
            paths = new ArrayList<String>(paths);
            paths.remove(EDITOR_PATH);
            PropertyEditorManager.setEditorSearchPath(
                paths.toArray(new String[0]));
        }
    }
    
    public boolean closing() {
        return showPendingTasks();
    }
    
    /** A class that server as a pending dialog manager.
     * It closes the dialog if there are no more pending tasks
     * and also servers as the action listener for killing the tasks.
     */
    private static class PendingDialogCloser extends WindowAdapter implements Runnable,
		PropertyChangeListener, ActionListener, NodeListener {
	private Dialog[] dialogHolder;
	private Object exitOption;
	PendingDialogCloser(Dialog[] holder, Object exit) {
	    dialogHolder = holder;
	    exitOption = exit;
	}
	
	public void run() {
            dialogHolder[0].setVisible(false);
	}

	// Beware: this may be called also from rootNode's prop changes
        // Once all pending tasks are gone, close the dialog.
        public void propertyChange(PropertyChangeEvent evt) {
            if(ExplorerManager.PROP_EXPLORED_CONTEXT.equals(evt.getPropertyName())) {
                checkClose();
            }
	}

	// kill pending tasks and close the dialog
        public void actionPerformed(ActionEvent evt) {
            if(evt.getSource() == exitOption) {
                killPendingTasks();
                Mutex.EVENT.readAccess(this); // close in AWT
            }
        }
	
        public void childrenRemoved(NodeMemberEvent evt) {
            checkClose();
	}
	
        // Dialog was opened but pending tasks could disappear inbetween.
        public void windowOpened(java.awt.event.WindowEvent evt) {
            checkClose();
	}
        
        /** Checks if there are pending tasks and closes (in AWT)
         * the dialog if not. */
        private void checkClose() {
            if(dialogHolder[0] != null && getPendingTasks().isEmpty()) {
                Mutex.EVENT.readAccess(this);
            }
        }

	// noop - rest of node listener
	public void childrenAdded (NodeMemberEvent ev) {}
	public void childrenReordered(NodeReorderEvent ev) {}
	public void nodeDestroyed (NodeEvent ev) {}

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
  
        EM panel = new EM();
        
        Dialog[] dialog = new Dialog[1];
        Node root = new AbstractNode(new PendingChildren());


        JButton exitOption = new JButton();
        Mnemonics.setLocalizedText(exitOption, NbBundle.getMessage(Install.class, "LAB_EndTasks"));
        // No default button.
        // exitOption.setDefaultCapable(false);
        exitOption.getAccessibleContext().setAccessibleDescription(
            NbBundle.getMessage(Install.class, "ACSD_EndTasks"));

	PendingDialogCloser closer = new PendingDialogCloser(dialog, exitOption);

        panel.getExplorerManager().setRootContext(root);
        // closer will autoclose the dialog if all pending tasks finish
        panel.getExplorerManager().addPropertyChangeListener(closer);
        
        DialogDescriptor dd = new DialogDescriptor(
            panel,
            NbBundle.getMessage(Install.class, "CTL_PendingTitle"),
            true, // modal
            new Object[] {
                exitOption,
                DialogDescriptor.CANCEL_OPTION
            },
            exitOption,
            DialogDescriptor.DEFAULT_ALIGN,
            null,
	    closer
        );
        // #33135 - no Help button for this dialog
        dd.setHelpCtx(null);

        if(!getPendingTasks().isEmpty()) {
            root.addNodeListener(closer);

            dialog[0] = DialogDisplayer.getDefault().createDialog(dd);
            
            dialog[0].addWindowListener(closer);
            
            dialog[0].setVisible(true);
            dialog[0].dispose();

            if(dd.getValue() == DialogDescriptor.CANCEL_OPTION
            || dd.getValue() == DialogDescriptor.CLOSED_OPTION) {
                return false;
            }
            
        }
        
        return true;
    }
 
    private static class EM extends JPanel implements ExplorerManager.Provider {
        private ExplorerManager manager = new ExplorerManager();
        private org.openide.util.Lookup lookup;

        public EM() {
            manager = new ExplorerManager();
            ActionMap map = getActionMap();
            map.put(DefaultEditorKit.copyAction, ExplorerUtils.actionCopy(manager));
            map.put(DefaultEditorKit.cutAction, ExplorerUtils.actionCut(manager));
            map.put(DefaultEditorKit.pasteAction, ExplorerUtils.actionPaste(manager));
            map.put("delete", ExplorerUtils.actionDelete(manager, true)); // or false

            lookup = ExplorerUtils.createLookup (manager, map);

            initComponent();
        }
       
        private void initComponent() {
            setLayout(new GridBagLayout());

            GridBagConstraints cons = new GridBagConstraints();
            cons.gridx = 0;
            cons.gridy = 0;
            cons.weightx = 1.0D;
            cons.fill = GridBagConstraints.HORIZONTAL;
            cons.insets = new Insets(11, 11, 0, 12);

            JLabel label = new JLabel();
	    Mnemonics.setLocalizedText(label, NbBundle.getMessage(Install.class, "LAB_PendingTasks"));

            add(label, cons);

            cons.gridy = 1;
            cons.weighty = 1.0D;
            cons.fill = GridBagConstraints.BOTH;
            cons.insets = new Insets(2, 11, 0, 12);

            ListView view = new ListView();
            //#66881
            view.setBorder(UIManager.getBorder("Nb.ScrollPane.border"));
            label.setLabelFor(view);

            add(view, cons);

            view.getAccessibleContext().setAccessibleDescription(
            NbBundle.getMessage(Install.class, "ACSD_PendingTasks"));
            getAccessibleContext().setAccessibleDescription(
            NbBundle.getMessage(Install.class, "ACSD_PendingTitle"));

            // set size requested by HIE guys
            Dimension origSize = getPreferredSize();
            setPreferredSize(new Dimension(origSize.width * 5 / 4, origSize.height));
        }

        public ExplorerManager getExplorerManager() {
            return manager;
        }
        public Lookup getLookup() {
            return lookup;
        }
        public void addNotify() {
            super.addNotify();
            ExplorerUtils.activateActions(manager, true);
        }
        public void removeNotify() {
            ExplorerUtils.activateActions(manager, false);
            super.removeNotify();
        }
    }
    
    /** Gets pending (running) tasks. Used as keys 
     * for pending dialog root node children. Currently it gets pending
     * actions only. */
    static Collection getPendingTasks() {
        
        ArrayList<Object> pendingTasks = new ArrayList<Object>( 10 ); // Action | ExecutorTask | InternalHandle
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
        
	pendingTasks.addAll(Arrays.asList(Controller.getDefault().getModel().getHandles()));
	
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
       ArrayList<ExecutorTask> tasks = new ArrayList<ExecutorTask>(ee.getRunningTasks());
       
       for ( Iterator<ExecutorTask> it = tasks.iterator(); it.hasNext(); ) {
           ExecutorTask et = it.next();
           if ( !et.isFinished() ) {
               et.stop();
           }
       }
       
   }

    /** Children showing pending tasks. */
    /* non private because of tests - was private before */ 
    static class PendingChildren extends Children.Keys implements ExecutionListener, ListDataListener {

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
	    Controller.getDefault().getModel().addListDataListener(this);
        }

        /** Implements superclass abstract method. Creates nodes from key.
         * @return <code>PendingActionNode</code> if key is of 
         * <code>Action</code> type otherwise <code>null</code> */
        protected Node[] createNodes(Object key) {
            Node n = null;
            if(key instanceof Action) {
                n = new PendingActionNode((Action)key);
            }
            else if ( key instanceof ExecutorTask) {
                AbstractNode an = new AbstractNode( Children.LEAF );
                an.setName(key.toString());
                an.setDisplayName(NbBundle.getMessage(Install.class, "CTL_PendingExternalProcess2", 
                    // getExecutionEngine() had better be non-null, since getPendingTasks gave an ExecutorTask:
                    ExecutionEngine.getExecutionEngine().getRunningTaskName((ExecutorTask) key)));
                n = an;
            }
	    else if (key instanceof InternalHandle) {
                AbstractNode an = new AbstractNode( Children.LEAF );
                an.setName(((InternalHandle)key).getDisplayName());
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
	    Controller.getDefault().getModel().removeListDataListener(this);
        }
        
        // ExecutionListener implementation ------------------------------------
        
        public void startedExecution( ExecutionEvent ev ) {
            setKeys(getPendingTasks());
        }
        
        public void finishedExecution( ExecutionEvent ev ) {
            setKeys(getPendingTasks());
        }

	public void intervalAdded(ListDataEvent e) {
            setKeys(getPendingTasks());
	}
	
	public void intervalRemoved(ListDataEvent e) {
            setKeys(getPendingTasks());
	}
	
	
	public void contentsChanged(ListDataEvent e) {
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
                + NbBundle.getMessage(Install.class, "CTL_ActionInProgress"));
            
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
        public Action[] getActions(boolean context) {
            return new Action[0];
        }
        
    } // End of class PendingActionNode.
    
}
