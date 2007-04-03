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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.sun.manager.jbi.actions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;

import org.netbeans.modules.j2ee.sun.bridge.apis.RefreshCookie;
import org.netbeans.modules.sun.manager.jbi.nodes.Uninstallable;
import org.openide.awt.Actions;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.NodeAction;
import org.openide.util.actions.Presenter;
import org.openide.util.actions.SystemAction;

/**
 *
 * @author jqian
 */
public abstract class UninstallAction extends NodeAction {
    
    protected void performAction(final Node[] activatedNodes) {
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                try {
                    // a list of nodes that need refreshing
                    final List<Node> parentNodes = new ArrayList<Node>();    
                    for (int i = 0; i < activatedNodes.length; i++) {
                        Node node = activatedNodes[i];
                        Lookup lookup = node.getLookup();
                        Object obj = lookup.lookup(Uninstallable.class);
                        
                        if (obj instanceof Uninstallable) {
                            // There will be at least one parent node that
                            // needs refreshing
                            Node parentNode = node.getParentNode();
                            if (!parentNodes.contains(parentNode)) {
                                parentNodes.add(parentNode);
                            }
                            Uninstallable uninstallable = (Uninstallable)obj;
                            uninstallable.uninstall(isForceAction());
                        }
                    }
                    
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            for (Iterator<Node> it = parentNodes.iterator(); it.hasNext();) {
                                Node parentNode = it.next();
                                final RefreshCookie refreshAction =
                                        (RefreshCookie) parentNode.getCookie(RefreshCookie.class);
                                if (refreshAction != null){
                                    refreshAction.refresh();
                                }
                            }
                        }
                    });                  
                    
                } catch(RuntimeException rex) {
                    //gobble up exception
                }
            }
        });
    }
    
    protected boolean enable(Node[] nodes) {
        boolean ret = false;
        
        if (nodes != null && nodes.length > 0) {
            
            ret = true;
            
            for (int i = 0; i < nodes.length; i++) {
                Node node = nodes[i];
                Lookup lookup = node.getLookup();
                Object obj = lookup.lookup(Uninstallable.class);
                
                try {
                    if(obj instanceof Uninstallable) {
                        Uninstallable uninstallable = (Uninstallable)obj;
                        if (!uninstallable.canUninstall()) {
                            ret = false;
                            break;
                        }
                    }
                } catch(RuntimeException rex) {
                    //gobble up exception
                }
            }
        }
        
        return ret;
    }
    
    protected boolean asynchronous() {
        return false;
    }
    
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }    
    
    protected abstract boolean isForceAction();
    
    
    /**
     * Normal uninstall action.
     */
    public static class Normal extends UninstallAction {
                
        public String getName() {
            return NbBundle.getMessage(ShutdownAction.class, "LBL_UninstallAction");  // NOI18N
        }
        
        protected boolean isForceAction() {
            return false;
        }
    }
    
    /**
     * Force uninstall action.
     */
    public static class Force extends UninstallAction  implements Presenter.Popup {
                
        public String getName() {
            return NbBundle.getMessage(ShutdownAction.class, "LBL_ForceUninstallAction");  // NOI18N
        }
        
        public JMenuItem getPopupPresenter() {
            JMenu result = new JMenu(
                    NbBundle.getMessage(ShutdownAction.class, "LBL_Advanced"));  // NOI18N
            
            //result.add(new JMenuItem(SystemAction.get(ShutdownAction.Force.class)));            
            Action forceShutdownAction = SystemAction.get(ShutdownAction.Force.class);
            JMenuItem forceShutdownMenuItem = new JMenuItem();
            Actions.connect(forceShutdownMenuItem, forceShutdownAction, false);
            result.add(forceShutdownMenuItem);
            
            //result.add(new JMenuItem(this));
            Action forceUninstallAction = SystemAction.get(UninstallAction.Force.class);
            JMenuItem forceUninstallMenuItem = new JMenuItem();
            Actions.connect(forceUninstallMenuItem, forceUninstallAction, false);
            result.add(forceUninstallMenuItem);
            
            return result;
        }        
        
        protected boolean isForceAction() {
            return true;
        }
    }
}
