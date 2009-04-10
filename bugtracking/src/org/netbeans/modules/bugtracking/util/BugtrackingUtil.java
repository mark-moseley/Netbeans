/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008-2009 Sun Microsystems, Inc. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008-2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.bugtracking.util;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Level;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.SwingConstants;
import org.jdesktop.layout.LayoutStyle;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.bugtracking.BugtrackingManager;
import org.netbeans.modules.bugtracking.kenai.KenaiRepositories;
import org.netbeans.modules.bugtracking.patch.ContextualPatch;
import org.netbeans.modules.bugtracking.patch.PatchException;
import org.netbeans.modules.bugtracking.spi.BugtrackingConnector;
import org.netbeans.modules.bugtracking.ui.issue.IssueTopComponent;
import org.netbeans.modules.bugtracking.spi.Issue;
import org.netbeans.modules.bugtracking.spi.Repository;
import org.netbeans.modules.bugtracking.ui.issue.PatchContextChooser;
import org.netbeans.modules.bugtracking.ui.search.QuickSearchComboBar;
import org.netbeans.modules.bugtracking.ui.selectors.RepositorySelector;
import org.netbeans.modules.kenai.api.KenaiProject;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;

/**
 *
 * @author Tomas Stupka, Jan Stola
 * @author Marian Petras
 */
public class BugtrackingUtil {

    public static boolean show(JPanel panel, String title, String okName) {
        JButton ok = new JButton(okName);
        JButton cancel = new JButton(NbBundle.getMessage(BugtrackingUtil.class, "LBL_Cancel")); // NOI18N
        final DialogDescriptor dd =
            new DialogDescriptor(
                    panel,
                    title,
                    true,
                    new Object[]{ok, cancel},
                    ok,
                    DialogDescriptor.DEFAULT_ALIGN,
                    new HelpCtx(panel.getClass()),
                    null);
        return DialogDisplayer.getDefault().notify(dd) == ok;
    }

    public static Issue[] getOpenIssues() {
        Set<TopComponent> tcs = TopComponent.getRegistry().getOpened();
        List<Issue> issues = new ArrayList<Issue>();
        for (TopComponent tc : tcs) {
            if(tc instanceof IssueTopComponent) {
                issues.add(((IssueTopComponent)tc).getIssue());
            }
        }
        return issues.toArray(new Issue[issues.size()]);
    }

    public static Issue[] getByIdOrSummary(Issue[] issues, String criteria) {
        if(criteria == null) {
            return issues;
        }
        criteria = criteria.trim();
        if(criteria.equals("")) {                                               // NOI18N
            return issues;
        }
        List<Issue> ret = new ArrayList<Issue>();
        for (Issue issue : issues) {            
            if(criteria.equals(issue.getID()) ||
               issue.getSummary().indexOf(criteria) > -1)
            {
                ret.add(issue);
            }  
        }
        return ret.toArray(new Issue[ret.size()]);
    }

    public static Repository createRepository() {
        RepositorySelector rs = new RepositorySelector();
        Repository repo = rs.create();
        return repo;
    }

    public static boolean editRepository(Repository repository, String errorMessage) {
        RepositorySelector rs = new RepositorySelector();
        return rs.edit(repository, errorMessage);
    }

    public static boolean editRepository(Repository repository) {
        return editRepository(repository, null);
    }

    public static Repository[] getKnownRepositories() {
        return BugtrackingManager.getInstance().getKnownRepositories();
    }

    public static BugtrackingConnector[] getBugtrackingConnectors() {
        return BugtrackingManager.getInstance().getConnectors();
    }

    public static Repository getKenaiBugtrackingRepository(KenaiProject project) {
        return KenaiRepositories.getInstance().getRepository(project);
    }

    public static String scramble(String str) {
        return Scrambler.getInstance().scramble(str);
    }

    public static String descramble(String str) {
        return Scrambler.getInstance().descramble(str);
    }

    public static Issue selectIssue(String message, Repository repository, JPanel caller) {
        QuickSearchComboBar bar = new QuickSearchComboBar(caller);
        bar.setRepository(repository);
        bar.setAlignmentX(0f);
        bar.setMaximumSize(new Dimension(Short.MAX_VALUE, bar.getPreferredSize().height));
        JPanel panel = new JPanel();
        BoxLayout layout = new BoxLayout(panel, BoxLayout.PAGE_AXIS);
        panel.setLayout(layout);
        JLabel label = new JLabel(message);
        panel.add(label);
        LayoutStyle layoutStyle = LayoutStyle.getSharedInstance();
        int gap = layoutStyle.getPreferredGap(label, bar, LayoutStyle.RELATED, SwingConstants.SOUTH, panel);
        panel.add(Box.createVerticalStrut(gap));
        panel.add(bar);
        panel.add(Box.createVerticalStrut(gap));
        ResourceBundle bundle = NbBundle.getBundle(BugtrackingUtil.class);
        JLabel hintLabel = new JLabel(bundle.getString("MSG_SelectIssueHint")); // NOI18N
        hintLabel.setEnabled(false);
        panel.add(hintLabel);
        panel.add(Box.createVerticalStrut(80));
        panel.setBorder(BorderFactory.createEmptyBorder(
                layoutStyle.getContainerGap(panel, SwingConstants.NORTH, null),
                layoutStyle.getContainerGap(panel, SwingConstants.WEST, null),
                0,
                layoutStyle.getContainerGap(panel, SwingConstants.EAST, null)));
        Issue issue = null;

        JButton ok = new JButton(bundle.getString("LBL_Select")); // NOI18N
        JButton cancel = new JButton(bundle.getString("LBL_Cancel")); // NOI18N
        DialogDescriptor descriptor = new DialogDescriptor(
                panel,
                bundle.getString("LBL_Issues"), // NOI18N
                true,
                NotifyDescriptor.OK_CANCEL_OPTION,
                ok,
                null);
        descriptor.setOptions(new Object [] {ok, cancel});
        descriptor.setHelpCtx(new HelpCtx("org.netbeans.modules.bugtracking.issueChooser")); // NOI18N
        DialogDisplayer.getDefault().createDialog(descriptor).setVisible(true);
        if (descriptor.getValue() == ok) {
            issue = bar.getIssue();
        }
        return issue;
    }

    public static File selectPatchContext() {
        PatchContextChooser chooser = new PatchContextChooser();
        ResourceBundle bundle = NbBundle.getBundle(BugtrackingUtil.class);
        JButton ok = new JButton(bundle.getString("LBL_Apply")); // NOI18N
        JButton cancel = new JButton(bundle.getString("LBL_Cancel")); // NOI18N
        DialogDescriptor descriptor = new DialogDescriptor(
                chooser,
                bundle.getString("LBL_ApplyPatch"), // NOI18N
                true,
                NotifyDescriptor.OK_CANCEL_OPTION,
                ok,
                null);
        descriptor.setOptions(new Object [] {ok, cancel});
        descriptor.setHelpCtx(new HelpCtx("org.netbeans.modules.bugtracking.patchContextChooser")); // NOI18N
        File context = null;
        DialogDisplayer.getDefault().createDialog(descriptor).setVisible(true);
        if (descriptor.getValue() == ok) {
            context = chooser.getSelectedFile();
        }
        return context;
    }

    public static void applyPatch(File patch, File context) {
        try {
            ContextualPatch cp = ContextualPatch.create(patch, context);
            cp.patch(false);
        } catch (PatchException ex) {
            BugtrackingManager.LOG.log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            BugtrackingManager.LOG.log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Recursively deletes all files and directories under a given file/directory.
     *
     * @param file file/directory to delete
     */
    public static void deleteRecursively(File file) {
        if (file.isDirectory()) {
            File [] files = file.listFiles();
            for (int i = 0; i < files.length; i++) {
                deleteRecursively(files[i]);
            }
        }
        file.delete();
    }

    public static File getLargerContext() {
        FileObject openFile = getOpenFileObj();
        if (openFile != null) {
            File largerContext = getLargerContext(openFile);
            if (largerContext != null) {
                return largerContext;
            }
        }

        return getContextFromProjects();
    }

    public static File getContextFromProjects() {
        final OpenProjects projects = OpenProjects.getDefault();

        Project mainProject = projects.getMainProject();
        if (mainProject != null) {
            return getLargerContext(mainProject);       //null or non-null
        }

        Project[] openProjects = projects.getOpenProjects();
        if ((openProjects != null) && (openProjects.length == 1)) {
            return getLargerContext(openProjects[0]);
        }

        return null;
    }

    public static File getLargerContext(File file) {
        return getLargerContext(file, null);
    }

    public static File getLargerContext(FileObject fileObj) {
        return getLargerContext(null, fileObj);
    }

    public static File getLargerContext(File file, FileObject fileObj) {
        if ((file == null) && (fileObj == null)) {
            throw new IllegalArgumentException(
                    "both File and FileObject are null");               //NOI18N
        }

        assert (file == null)
               || (fileObj == null)
               || FileUtil.toFileObject(file).equals(fileObj);

        if (fileObj == null) {
            fileObj = FileUtil.toFileObject(file);
        } else if (file == null) {
            file = FileUtil.toFile(fileObj);
        }

        if (fileObj == null) {
            return null;
        }
        if (!fileObj.isValid()) {
            return null;
        }

        Project parentProject = FileOwnerQuery.getOwner(fileObj);
        if (parentProject != null) {
            FileObject parentProjectFolder = parentProject.getProjectDirectory();
            if (parentProjectFolder.equals(fileObj) && (file != null)) {
                return file;
            }
            File folder = FileUtil.toFile(parentProjectFolder);
            if (folder != null) {
                return folder;
            }
        }

        if (fileObj.isFolder()) {
            return file;                        //whether it is null or non-null
        } else {
            fileObj = fileObj.getParent();
            assert fileObj != null;      //every non-folder should have a parent
            return FileUtil.toFile(fileObj);    //whether it is null or non-null
        }
    }

    public static File getLargerContext(Project project) {
        FileObject projectFolder = project.getProjectDirectory();
        assert projectFolder != null;

        return FileUtil.toFile(projectFolder);
    }

    private static FileObject getOpenFileObj() {
        TopComponent activatedTopComponent = TopComponent.getRegistry()
                                             .getActivated();
        if (activatedTopComponent == null) {
            return null;
        }

        DataObject dataObj = activatedTopComponent.getLookup()
                             .lookup(DataObject.class);
        if ((dataObj == null) || !dataObj.isValid()) {
            return null;
        }

        return dataObj.getPrimaryFile();
    }

    public static void keepFocusedComponentVisible(JScrollPane scrollPane) {
        keepFocusedComponentVisible(scrollPane.getViewport().getView());
    }

    public static void keepFocusedComponentVisible(Component component) {
        FocusListener listener= getScrollingFocusListener();
        component.removeFocusListener(listener); // Making sure that it is not added twice
        component.addFocusListener(listener);
        if (component instanceof Container) {
            for (Component subComponent : ((Container)component).getComponents()) {
                keepFocusedComponentVisible(subComponent);
            }
        }
    }

    private static FocusListener scrollingFocusListener;
    private static FocusListener getScrollingFocusListener() {
        if (scrollingFocusListener == null) {
            scrollingFocusListener = new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    if (!e.isTemporary()) {
                        Component comp = e.getComponent();
                        Container cont = comp.getParent();
                        if (cont instanceof JViewport) {
                            // comp is JViewport's view;
                            // we want the viewport itself to be shown in this case
                            comp = cont;
                            cont = cont.getParent();
                        }
                        if (cont instanceof JComponent) {
                            ((JComponent)cont).scrollRectToVisible(comp.getBounds());
                        }
                    }
                }
            };
        }
        return scrollingFocusListener;
    }

}
