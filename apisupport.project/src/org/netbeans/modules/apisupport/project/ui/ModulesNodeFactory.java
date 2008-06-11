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

package org.netbeans.modules.apisupport.project.ui;

import java.awt.EventQueue;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.Collections;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.modules.apisupport.project.Util;
import org.netbeans.modules.apisupport.project.suite.SuiteProject;
import org.netbeans.modules.apisupport.project.ui.customizer.SuiteUtils;
import org.netbeans.modules.apisupport.project.ui.wizard.NewNbModuleWizardIterator;
import org.netbeans.spi.project.SubprojectProvider;
import org.netbeans.spi.project.ui.support.NodeFactory;
import org.netbeans.spi.project.ui.support.NodeFactorySupport;
import org.netbeans.spi.project.ui.support.NodeList;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.awt.StatusDisplayer;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;
import org.openide.util.actions.CookieAction;
import org.openide.util.actions.NodeAction;
import org.openide.util.lookup.Lookups;
import org.openide.windows.WindowManager;

/**
 * @author mkleint
 */
public class ModulesNodeFactory implements NodeFactory {

    public ModulesNodeFactory() {}

    public NodeList createNodes(Project p) {
        SuiteProject prj = p.getLookup().lookup(SuiteProject.class);
        assert prj != null;
        return NodeFactorySupport.fixedNodeList(new ModulesNode(prj));
    }

    private static String getMessage(final String key) {
        return NbBundle.getMessage(SuiteLogicalView.class, key);
    }


    /** Represent <em>Modules</em> node in the Suite Logical View. */
    static final class ModulesNode extends AbstractNode {

        private SuiteProject suite;

        ModulesNode(final SuiteProject suite) {
            super(new ModuleChildren(suite));
            this.suite = suite;
            setName("modules"); // NOI18N
            setDisplayName(getMessage("CTL_Modules"));
        }

        public @Override Action[] getActions(boolean context) {
            return new Action[] {
                new AddNewSuiteComponentAction(suite),
                new AddNewLibraryWrapperAction(suite),
                new AddSuiteComponentAction(suite),
            };
        }

        private Image getIcon(boolean opened) {
            Image badge = Utilities.loadImage("org/netbeans/modules/apisupport/project/suite/resources/module-badge.png", true);
            return Utilities.mergeImages(UIUtil.getTreeFolderIcon(opened), badge, 9, 9);
        }

        public @Override Image getIcon(int type) {
            return getIcon(false);
        }

        public @Override Image getOpenedIcon(int type) {
            return getIcon(true);
        }

        static final class ModuleChildren extends Children.Keys<NbModuleProject> implements ChangeListener {

            private final SuiteProject suite;

            public ModuleChildren(SuiteProject suite) {
                suite.getLookup().lookup(SubprojectProvider.class).addChangeListener(this);
                this.suite = suite;
            }

            protected @Override void addNotify() {
                updateKeys();
            }

            private void updateKeys() {
                // e.g.(?) Explorer view under Children.MUTEX subsequently calls e.g.
                // SuiteProject$Info.getSimpleName() which acquires ProjectManager.mutex(). And
                // since this method might be called under ProjectManager.mutex() write access
                // and updateKeys() --> setKeys() in turn calls Children.MUTEX write access,
                // deadlock is here, so preventing it... (also got this under read access)
                EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        // #70112: sort them.
                        SortedSet<NbModuleProject> subModules = new TreeSet<NbModuleProject>(Util.projectDisplayNameComparator());
                        subModules.addAll(SuiteUtils.getSubProjects(suite));
                        setKeys(subModules);
                    }
                });
            }

            protected @Override void removeNotify() {
                suite.getLookup().lookup(SubprojectProvider.class).removeChangeListener(this);
                setKeys(Collections.<NbModuleProject>emptySet());
            }

            protected Node[] createNodes(NbModuleProject p) {
                return new Node[] {new SuiteComponentNode(p)};
            }

            public void stateChanged(ChangeEvent ev) {
                updateKeys();
            }

        }

    }

    private static final class AddSuiteComponentAction extends AbstractAction {

        private final SuiteProject suite;

        public AddSuiteComponentAction(final SuiteProject suite) {
            super(getMessage("CTL_AddModule"));
            this.suite = suite;
        }

        public void actionPerformed(ActionEvent evt) {
            NbModuleProject project = UIUtil.chooseSuiteComponent(
                    WindowManager.getDefault().getMainWindow(),
                    suite);
            if (project != null) {
                if (!SuiteUtils.contains(suite, project)) {
                    try {
                        SuiteUtils.addModule(suite, project);
                        ProjectManager.getDefault().saveProject(suite);
                    } catch (IOException ex) {
                        ErrorManager.getDefault().notify(ex);
                    }
                } else {
                    DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                            NbBundle.getMessage(SuiteLogicalView.class, "MSG_SuiteAlreadyContainsCNB", project.getCodeNameBase())));
                }
            }
        }

    }

    private static final class AddNewSuiteComponentAction extends AbstractAction {

        private final SuiteProject suite;

        public AddNewSuiteComponentAction(final SuiteProject suite) {
            super(getMessage("CTL_AddNewModule"));
            this.suite = suite;
        }

        public void actionPerformed(ActionEvent evt) {
            NewNbModuleWizardIterator iterator = NewNbModuleWizardIterator.createSuiteComponentIterator(suite);
            UIUtil.runProjectWizard(iterator, "CTL_NewModuleProject"); // NOI18N
        }

    }

    static final class AddNewLibraryWrapperAction extends AbstractAction {

        private final Project suiteProvider;
        private final NbModuleProject target;

        public AddNewLibraryWrapperAction(final Project suiteProvider, final NbModuleProject target) {
            super(getMessage("CTL_AddNewLibrary"));
            this.suiteProvider = suiteProvider;
            this.target = target;
        }

        public AddNewLibraryWrapperAction(final Project suiteProvider) {
            this(suiteProvider, null);
        }

        public void actionPerformed(ActionEvent evt) {
            NbModuleProject project = UIUtil.runLibraryWrapperWizard(suiteProvider);
            if (project != null && target != null) {
                try {
                    Util.addDependency(target, project);
                    ProjectManager.getDefault().saveProject(target);
                } catch (IOException e) {
                    assert false : e;
                }
            }
        }

    }

    /** Represent one module (a suite component) node. */
    private static final class SuiteComponentNode extends AbstractNode {

        private final static Action REMOVE_ACTION = new RemoveSuiteComponentAction();
        private final static Action OPEN_ACTION = new OpenProjectAction();

        public SuiteComponentNode(final NbModuleProject suiteComponent) {
            super(Children.LEAF, Lookups.fixed(new Object[] {suiteComponent}));
            ProjectInformation info = ProjectUtils.getInformation(suiteComponent);
            setName(info.getName());
            setDisplayName(info.getDisplayName());
            setIconBaseWithExtension(NbModuleProject.NB_PROJECT_ICON_PATH);
            info.addPropertyChangeListener(new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent evt) {
                    if (ProjectInformation.PROP_DISPLAY_NAME.equals(evt.getPropertyName())) {
                        SuiteComponentNode.this.setDisplayName((String) evt.getNewValue());
                    } else if (ProjectInformation.PROP_NAME.equals(evt.getPropertyName())) {
                        SuiteComponentNode.this.setName((String) evt.getNewValue());
                    }
                }
            });
        }

        public @Override Action[] getActions(boolean context) {
            return new Action[] {
                OPEN_ACTION, REMOVE_ACTION
            };
        }

        public @Override Action getPreferredAction() {
            return OPEN_ACTION;
        }

    }

    private static final class RemoveSuiteComponentAction extends NodeAction {

        protected void performAction(Node[] activatedNodes) {
            for (int i = 0; i < activatedNodes.length; i++) {
                final NbModuleProject suiteComponent =
                        activatedNodes[i].getLookup().lookup(NbModuleProject.class);
                assert suiteComponent != null : "NbModuleProject in lookup"; // NOI18N
                boolean remove = true;
                try {
                    NbModuleProject[] modules = SuiteUtils.getDependentModules(suiteComponent);
                    if (modules.length > 0) {
                        StringBuffer sb = new StringBuffer("<ul>"); // NOI18N
                        for (int j = 0; j < modules.length; j++) {
                            sb.append("<li>" + ProjectUtils.getInformation(modules[j]).getDisplayName() + "</li>"); // NOI18N
                        }
                        sb.append("</ul>"); // NOI18N
                        String displayName = ProjectUtils.getInformation(suiteComponent).getDisplayName();
                        String confirmMessage = NbBundle.getMessage(SuiteLogicalView.class,
                                "MSG_RemovingModuleMessage", displayName, sb.toString()); // NOI18N
                        remove = UIUtil.showAcceptCancelDialog(
                                NbBundle.getMessage(SuiteLogicalView.class, "CTL_RemovingModuleTitle", displayName),
                                confirmMessage, getMessage("CTL_RemoveDependency"), null, NotifyDescriptor.QUESTION_MESSAGE);
                    }
                } catch (IOException ex) {
                    Logger.getLogger(ModulesNodeFactory.class.getName()).log(Level.INFO, null, ex);
                    // #137021: suite may have broken platform dependency, so just continue
                }
                if (remove) {
                    try {
                        SuiteUtils.removeModuleFromSuiteWithDependencies(suiteComponent);
                    } catch (Exception x) {
                        Exceptions.printStackTrace(x);
                    }
                }
            }
        }

        protected boolean enable(Node[] activatedNodes) {
            return true;
        }

        public String getName() {
            return getMessage("CTL_RemoveModule");
        }

        public HelpCtx getHelpCtx() {
            return HelpCtx.DEFAULT_HELP;
        }

        protected @Override boolean asynchronous() {
            return false;
        }

    }

    private static final class OpenProjectAction extends CookieAction {

        protected void performAction(Node[] activatedNodes) {
            final Project[] projects = new Project[activatedNodes.length];
            for (int i = 0; i < activatedNodes.length; i++) {
                Project project = activatedNodes[i].getLookup().lookup(Project.class);
                projects[i] = project;
            }
            RequestProcessor.getDefault().post(new Runnable() {
                public void run() {
                    StatusDisplayer.getDefault().setStatusText(getMessage("MSG_OpeningProjects"));
                    OpenProjects.getDefault().open(projects, false);
                }
            });
        }

        public String getName() {
            return getMessage("CTL_OpenProject");
        }

        public HelpCtx getHelpCtx() {
            return HelpCtx.DEFAULT_HELP;
        }

        protected @Override boolean asynchronous() {
            return false;
        }

        protected int mode() {
            return CookieAction.MODE_ALL;
        }

        protected Class[] cookieClasses() {
            return new Class[] { Project.class };
        }

    }

}
