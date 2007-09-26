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

package org.netbeans.modules.ant.debugger;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.text.Collator;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import org.apache.tools.ant.module.AntModule;
import org.apache.tools.ant.module.api.AntProjectCookie;
import org.apache.tools.ant.module.api.AntTargetExecutor;
import org.apache.tools.ant.module.api.support.TargetLister;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.awt.Actions;
import org.openide.execution.ExecutorTask;
import org.openide.util.ContextAwareAction;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.SystemAction;
import org.openide.util.actions.Presenter;

/**
 * Submenu which permits the user to run various targets from the project.
 * Distinction made between the main target, other documented targets, and other
 * undocumented targets.
 */
public final class RunTargetsAction extends SystemAction implements ContextAwareAction {

    public String getName () {
        return NbBundle.getMessage (RunTargetsAction.class, "LBL_run_targets_action");
    }

    public HelpCtx getHelpCtx () {
        return HelpCtx.DEFAULT_HELP;
    }

    public void actionPerformed(ActionEvent e) {
        assert false : "Action should never be called without a context";
    }

    public Action createContextAwareInstance(Lookup actionContext) {
        return new ContextAction(actionContext);
    }
    
    /**
     * The particular instance of this action for a given project.
     */
    private static final class ContextAction extends AbstractAction implements Presenter.Popup {
        
        private final AntProjectCookie project;
        
        public ContextAction(Lookup lkp) {
            super(SystemAction.get(RunTargetsAction.class).getName());
            Collection/*<AntProjectCookie>*/ apcs = lkp.lookup(new Lookup.Template(AntProjectCookie.class)).allInstances();
            AntProjectCookie _project = null;
            if (apcs.size() == 1) {
                _project = (AntProjectCookie) apcs.iterator().next();
                if (_project.getParseException() != null) {
                    _project = null;
                }
            }
            project = _project;
            super.setEnabled(project != null);
        }

        public void actionPerformed(ActionEvent e) {
            assert false : "Action should not be called directly";
        }

        public JMenuItem getPopupPresenter() {
            if (project != null) {
                return createMenu(project);
            } else {
                return new Actions.MenuItem(this, false);
            }
        }

        public void setEnabled(boolean b) {
            assert false : "No modifications to enablement status permitted";
        }
        
    }

    /**
     * Create the submenu.
     */
    private static JMenu createMenu(AntProjectCookie project) {
        return new LazyMenu(project);
    }
    
    private static final class LazyMenu extends JMenu {
        
        private final AntProjectCookie project;
        private boolean initialized = false;
        
        public LazyMenu(AntProjectCookie project) {
            super(SystemAction.get(RunTargetsAction.class).getName());
            this.project = project;
        }
        
        public JPopupMenu getPopupMenu() {
            if (!initialized) {
                initialized = true;
                Set/*<TargetLister.Target>*/ allTargets;
                try {
                    allTargets = TargetLister.getTargets(project);
                } catch (IOException e) {
                    DialogDisplayer.getDefault().notify(
                            new NotifyDescriptor.Message(e.getLocalizedMessage()));
                    allTargets = Collections.EMPTY_SET;
                }
                String defaultTarget = null;
                SortedSet/*<String>*/ describedTargets = new TreeSet(Collator.getInstance());
                SortedSet/*<String>*/ otherTargets = new TreeSet(Collator.getInstance());
                Iterator it = allTargets.iterator();
                while (it.hasNext()) {
                    TargetLister.Target t = (TargetLister.Target) it.next();
                    if (t.isOverridden()) {
                        // Cannot be called.
                        continue;
                    }
                    if (t.isInternal()) {
                        // Don't present in GUI.
                        continue;
                    }
                    String name = t.getName();
                    if (t.isDefault()) {
                        defaultTarget = name;
                    } else if (t.isDescribed()) {
                        describedTargets.add(name);
                    } else {
                        otherTargets.add(name);
                    }
                }
                boolean needsep = false;
                if (defaultTarget != null) {
                    needsep = true;
                    JMenuItem menuitem = new JMenuItem(defaultTarget);
                    menuitem.addActionListener(new TargetMenuItemHandler(project, defaultTarget));
                    add(menuitem);
                }
                if (needsep) {
                    needsep = false;
                    addSeparator();
                }
                if (!describedTargets.isEmpty()) {
                    needsep = true;
                    it = describedTargets.iterator();
                    while (it.hasNext()) {
                        String target = (String) it.next();
                        JMenuItem menuitem = new JMenuItem(target);
                        menuitem.addActionListener(new TargetMenuItemHandler(project, target));
                        add(menuitem);
                    }
                }
                if (needsep) {
                    needsep = false;
                    addSeparator();
                }
                if (!otherTargets.isEmpty()) {
                    needsep = true;
                    JMenu submenu = new JMenu(NbBundle.getMessage(RunTargetsAction.class, "LBL_run_other_targets"));
                    it = otherTargets.iterator();
                    while (it.hasNext()) {
                        String target = (String) it.next();
                        JMenuItem menuitem = new JMenuItem(target);
                        menuitem.addActionListener(new TargetMenuItemHandler(project, target));
                        submenu.add(menuitem);
                    }
                    add(submenu);
                }
                if (needsep) {
                    needsep = false;
                    addSeparator();
                }
                add(new AdvancedAction(project, allTargets));
            }
            return super.getPopupMenu();
        }
        
    }

    /**
     * Action handler for a menu item representing one target.
     */
    private static final class TargetMenuItemHandler implements ActionListener, Runnable {
        
        private final AntProjectCookie project;
        private final String target;
        
        public TargetMenuItemHandler(AntProjectCookie project, String target) {
            this.project = project;
            this.target = target;
        }
        
        public void actionPerformed(ActionEvent ev) {
            // #16720 part 2: don't do this in the event thread...
            RequestProcessor.getDefault().post(this);
        }
        
        public void run() {
            try {
                DebuggerAntLogger.getDefault ().debugFile (project.getFile ());
                AntTargetExecutor.Env env = new AntTargetExecutor.Env ();
                AntTargetExecutor executor = AntTargetExecutor.createTargetExecutor 
                    (env);
                ExecutorTask executorTask = executor.execute 
                    (project, new String[] {target});
                DebuggerAntLogger.getDefault().fileExecutor(project.getFile(), executorTask);
            } catch (IOException ioe) {
                AntModule.err.notify(ioe);
            }
        }
        
    }
    
    /**
     * Menu item to let the user select a random target(s), and set properties and verbosity.
     */
    private static final class AdvancedAction extends AbstractAction {
        
        private final AntProjectCookie project;
        private final Set/*<TargetLister.Target>*/ allTargets;
        
        public AdvancedAction(AntProjectCookie project, Set/*<TargetLister.Target>*/ allTargets) {
            super(NbBundle.getMessage(RunTargetsAction.class, "LBL_run_advanced"));
            this.project = project;
            this.allTargets = allTargets;
        }

        public void actionPerformed(ActionEvent e) {
            String title = NbBundle.getMessage(RunTargetsAction.class, "TITLE_run_advanced");
            AdvancedActionPanel panel = new AdvancedActionPanel(project, allTargets);
            DialogDescriptor dd = new DialogDescriptor(panel, title);
            dd.setOptionType(NotifyDescriptor.OK_CANCEL_OPTION);
            JButton run = new JButton(NbBundle.getMessage(RunTargetsAction.class, "LBL_run_advanced_run"));
            run.setDefaultCapable(true);
            JButton cancel = new JButton(NbBundle.getMessage(RunTargetsAction.class, "LBL_run_advanced_cancel"));
            dd.setOptions(new Object[] {run, cancel});
            dd.setModal(true);
            Object result = DialogDisplayer.getDefault().notify(dd);
            if (result.equals(run)) {
                try {
                    panel.run();
                } catch (IOException x) {
                    AntModule.err.notify(x);
                }
            }
        }
        
    }
    
}
