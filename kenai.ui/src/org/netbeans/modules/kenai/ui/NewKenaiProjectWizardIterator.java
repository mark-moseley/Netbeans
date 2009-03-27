/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.kenai.ui;

import java.awt.Component;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.modules.kenai.api.Kenai;
import org.netbeans.modules.kenai.api.KenaiErrorMessage;
import org.netbeans.modules.kenai.api.KenaiException;
import org.netbeans.modules.kenai.api.KenaiProject;
import org.netbeans.modules.kenai.ui.spi.Dashboard;
import org.openide.WizardDescriptor;
import org.openide.WizardDescriptor.Panel;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author Milan Kubec
 */
public class NewKenaiProjectWizardIterator implements WizardDescriptor.ProgressInstantiatingIterator {

    private WizardDescriptor wizard;
    private WizardDescriptor.Panel[] panels;
    private transient int index;
    private Node activeNode;
    private boolean isShareExistingFolder;


    public static final String PROP_PRJ_NAME = "projectName";
    public static final String PROP_PRJ_TITLE = "projectTitle";
    public static final String PROP_PRJ_DESC = "projectDescription";
    public static final String PROP_PRJ_LICENSE = "projectLicense";
    public static final String PROP_SCM_TYPE = "projectSCMType";
    public static final String PROP_SCM_NAME = "projectSCMName";
    public static final String PROP_SCM_URL = "projectSCMUrl";
    public static final String PROP_SCM_LOCAL = "projectSCMLocal";
    public static final String PROP_ISSUES = "projectIssues";
    public static final String PROP_ISSUES_URL = "projectIssuesUrl";

    // special values when no features are created
    public static final String NO_REPO = "none";
    public static final String NO_ISSUES = "none";

    private Logger logger = Logger.getLogger("org.netbeans.modules.kenai");

    NewKenaiProjectWizardIterator(Node activatedNode) {
        this.activeNode = activatedNode;
        isShareExistingFolder = true;
    }

    NewKenaiProjectWizardIterator() {
        isShareExistingFolder = false;
    }

    public Set<CreatedProjectInfo> instantiate(ProgressHandle handle) throws IOException {

        handle.start();

        String newPrjName = (String) wizard.getProperty(PROP_PRJ_NAME);
        String newPrjTitle = (String) wizard.getProperty(PROP_PRJ_TITLE);
        String newPrjDesc = (String) wizard.getProperty(PROP_PRJ_DESC);
        String newPrjLicense = (String) wizard.getProperty(PROP_PRJ_LICENSE);

        String newPrjScmType = (String) wizard.getProperty(PROP_SCM_TYPE);
        String newPrjScmName = (String) wizard.getProperty(PROP_SCM_NAME);
        String newPrjScmUrl = (String) wizard.getProperty(PROP_SCM_URL);
        String newPrjScmLocal = (String) wizard.getProperty(PROP_SCM_LOCAL);

        String newPrjIssues = (String) wizard.getProperty(PROP_ISSUES);
        String newPrjIssuesUrl = (String) wizard.getProperty(PROP_ISSUES_URL);

        // Create project
//        try {
//            handle.progress(NbBundle.getMessage(NewKenaiProjectWizardIterator.class,
//                "NewKenaiProject.progress.creatingProject"));
//
//            logger.log(Level.FINE, "Creating Kenai Project - Name: " + newPrjName +
//                    ", Title: " + newPrjTitle + ", Description: " + newPrjDesc + ", License: " + newPrjLicense);
//
//            Kenai.getDefault().createProject(newPrjName, newPrjTitle,
//                    newPrjDesc, new String[] { newPrjLicense }, /*no tags*/ null);
//
//        } catch (KenaiException kex) {
//            throw new IOException(getErrorMessage(kex, NbBundle.getMessage(NewKenaiProjectWizardIterator.class,
//                    "NewKenaiProject.progress.projectCreationFailed")));
//        }

        // Create feature - SCM repository
        if (!NO_REPO.equals(newPrjScmType)) {
            try {
                handle.progress(NbBundle.getMessage(NewKenaiProjectWizardIterator.class,
                        "NewKenaiProject.progress.creatingRepo"));
                String displayName = getScmDisplayName(newPrjScmType);
                String description = getScmDescription(newPrjScmType);
                String extScmUrl = (Utilities.EXT_REPO.equals(newPrjScmType) ? newPrjScmUrl : null);

                logger.log(Level.FINE, "Creating SCM Repository - Name: " + newPrjScmName +
                        ", Type: " + newPrjScmType + ", Ext. URL: " + newPrjScmUrl + ", Local Folder: " + newPrjScmLocal);

                Kenai.getDefault().getProject(newPrjName).createProjectFeature(newPrjScmName,
                        displayName, description, newPrjScmType, /*ext issues URL*/ null, extScmUrl, /*browse repo URL*/ null);

            } catch (KenaiException kex) {
                throw new IOException(getErrorMessage(kex, NbBundle.getMessage(NewKenaiProjectWizardIterator.class,
                        "NewKenaiProject.progress.repoCreationFailed")));
            }
        } else {
            logger.log(Level.FINE, "SCM Repository creation skipped.");
        }

        // Create feature - Issue tracking
        if (!NO_ISSUES.equals(newPrjIssues)) {
            try {
                handle.progress(NbBundle.getMessage(NewKenaiProjectWizardIterator.class,
                        "NewKenaiProject.progress.creatingIssues"));
                String displayName = getIssuesDisplayName(newPrjIssues);
                String description = getIssuesDescription(newPrjIssues);
                String extIssuesUrl = (Utilities.EXT_ISSUES.equals(newPrjIssues) ? newPrjIssuesUrl : null);

                logger.log(Level.FINE, "Creating Issue Tracking - Name: " + newPrjIssues + ", Ext. URL: " + newPrjIssuesUrl);

                // XXX issue tracking name not clear !!!
                Kenai.getDefault().getProject(newPrjName).createProjectFeature(newPrjName + newPrjIssues,
                    displayName, description, newPrjIssues, extIssuesUrl, /*ext repo URL*/ null, /*browse repo URL*/ null);
            } catch (KenaiException kex) {
                throw new IOException(getErrorMessage(kex, NbBundle.getMessage(NewKenaiProjectWizardIterator.class,
                        "NewKenaiProject.progress.issuesCreationFailed")));
            }
        } else {
            logger.log(Level.FINE, "Issue Tracking creation skipped.");
        }

        // After the repository is created it must be checked out
        // local folder to checkout needs to be created if not exists ?

        // Show Project creation summary
        

        // Open the project in Dashboard
        Set<CreatedProjectInfo> set = new HashSet<CreatedProjectInfo>();
        try {
            KenaiProject project = Kenai.getDefault().getProject(newPrjName);
            Dashboard.getDefault().addProject(new ProjectHandleImpl(project));
            set.add(new CreatedProjectInfo(project, newPrjScmLocal));
        } catch (KenaiException ex) {
            Exceptions.printStackTrace(ex);
        }

        handle.finish();

        return set;
        
    }

    public Set<?> instantiate() throws IOException {
        assert false;
        return null;
    }

    public void initialize(WizardDescriptor wizard) {
        this.wizard = wizard;
        this.panels = getPanels();
    }

    public void uninitialize(WizardDescriptor wizard) {
        // XXX set properties to null ???
    }

    public Panel current() {
        return panels[index];
    }

    public String name() {
        return "New Kenai Project Wizard"; // XXX from Bundle
    }

    public boolean hasNext() {
        return index < panels.length - 1;
    }

    public boolean hasPrevious() {
        return index > 0;
    }

    public void nextPanel() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        index++;
    }

    public void previousPanel() {
        if (!hasPrevious()) {
            throw new NoSuchElementException();
        }
        index--;
    }

    public void addChangeListener(ChangeListener l) { }

    public void removeChangeListener(ChangeListener l) { }

    // ----------

    public static class CreatedProjectInfo {

        public KenaiProject project;
        public String localRepoPath;

        public CreatedProjectInfo(KenaiProject prj, String pth) {
            project = prj;
            localRepoPath = pth;
        }

    }

    // ----------

    private String getErrorMessage(KenaiException kex, String prepend) {
        String errMsg = null;
        if (kex instanceof KenaiErrorMessage) {
            KenaiErrorMessage kem = (KenaiErrorMessage) kex;
            Map<String,String> errMap = kem.getErrors();
            StringBuffer sb = new StringBuffer();
            if (prepend != null) {
                sb.append(prepend + " "); // NOI18N
            }
            for (Iterator<String> it = errMap.keySet().iterator(); it.hasNext(); ) {
                String fld = it.next();
                sb.append(errMap.get(fld));
            }
            errMsg = sb.toString();
        } else {
            errMsg = kex.getLocalizedMessage();
        }
        return errMsg;
    }

    // XXX from bundle
    private String getScmDisplayName(String scmName) {
        String displayName = "Source Code Repository";
        if (Utilities.SVN_REPO.equals(scmName)) {
            displayName = "Subversion Repository";
        } else if (Utilities.HG_REPO.equals(scmName)) {
            displayName = "Mercurial Repository";
        } else if (Utilities.EXT_REPO.equals(scmName)) {
            displayName = "External Repository";
        }
        return displayName;
    }

    // XXX from bundle
    private String getScmDescription(String scmName) {
        String desc = "Source Code Repository";
        if (Utilities.SVN_REPO.equals(scmName)) {
            desc = "Subversion Source Code Repository";
        } else if (Utilities.HG_REPO.equals(scmName)) {
            desc = "Mercurial Source Code Repository";
        } else if (Utilities.EXT_REPO.equals(scmName)) {
            desc = "External Source Code Repository";
        }
        return desc;
    }

    // XXX from bundle
    private String getIssuesDisplayName(String issues) {
        String displayName = "Issue Tracking";
        if (Utilities.BGZ_ISSUES.equals(issues)) {
            displayName = "Bugzilla";
        } else if (Utilities.JIRA_ISSUES.equals(issues)) {
            displayName = "JIRA";
        } else if (Utilities.EXT_ISSUES.equals(issues)) {
            displayName = "External";
        }
        return displayName;
    }

    // XXX from bundle
    private String getIssuesDescription(String issues) {
        String desc = "Issue Tracking";
        if (Utilities.BGZ_ISSUES.equals(issues)) {
            desc = "Bugzilla Issue Tracking";
        } else if (Utilities.JIRA_ISSUES.equals(issues)) {
            desc = "JIRA Issue Tracking";
        } else if (Utilities.EXT_ISSUES.equals(issues)) {
            desc = "External Issue Tracking";
        }
        return desc;
    }

    // ----------

    private WizardDescriptor.Panel[] getPanels() {
        if (panels == null) {
            panels = createPanels();
            String[] steps = new String[panels.length];
            for (int i = 0; i < panels.length; i++) {
                Component c = panels[i].getComponent();
                steps[i] = c.getName();
                if (c instanceof JComponent) {
                    JComponent jc = (JComponent) c;
                    // Sets step number of a component
                    jc.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, new Integer(i));
                    // Sets steps names for a panel
                    jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, steps);
                    // Turn on subtitle creation on each step
                    jc.putClientProperty(WizardDescriptor.PROP_AUTO_WIZARD_STYLE, Boolean.TRUE);
                    // Show steps on the left side with the image on the background
                    jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DISPLAYED, Boolean.TRUE);
                    // Turn on numbering of all steps
                    jc.putClientProperty(WizardDescriptor.PROP_CONTENT_NUMBERED, Boolean.TRUE);
                }
            }
        }
        return panels;
    }

    private WizardDescriptor.Panel[] createPanels() {
        if (isShareExistingFolder) {
            return new WizardDescriptor.Panel[]{
                        new NameAndLicenseWizardPanel(activeNode)
                    };

        } else {
            return new WizardDescriptor.Panel[]{
                        new NameAndLicenseWizardPanel(),
                        new SourceAndIssuesWizardPanel()
                    };
        }
    }

}
