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

package org.netbeans.modules.bugzilla.repository;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.MalformedURLException;
import java.net.URL;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.eclipse.mylyn.commons.net.AuthenticationCredentials;
import org.eclipse.mylyn.commons.net.AuthenticationType;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.bugtracking.spi.BugtrackingController;
import org.netbeans.modules.bugzilla.Bugzilla;
import org.netbeans.modules.bugzilla.BugzillaConfig;
import org.netbeans.modules.bugzilla.commands.ValidateCommand;
import org.openide.util.Cancellable;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;

/**
 *
 * @author Tomas Stupka
 */
public class RepositoryController extends BugtrackingController implements DocumentListener, ActionListener {
    private BugzillaRepository repository;
    private RepositoryPanel panel;
    private String errorMessage;
    private boolean validateError;
    private boolean populating;

    RepositoryController(BugzillaRepository repository) {
        this.repository = repository;
        panel = new RepositoryPanel(this);
        panel.nameField.getDocument().addDocumentListener(this);
        panel.userField.getDocument().addDocumentListener(this);
        panel.urlField.getDocument().addDocumentListener(this);
        panel.psswdField.getDocument().addDocumentListener(this);

        panel.validateButton.addActionListener(this);
        panel.addAncestorListener(new AncestorListener() {
            public void ancestorAdded(AncestorEvent event) {
                populate();
            }
            public void ancestorRemoved(AncestorEvent event) { }
            public void ancestorMoved(AncestorEvent event)   { }
        });

    }

    public JComponent getComponent() {
        return panel;
    }

    public HelpCtx getHelpContext() {
        return new HelpCtx(org.netbeans.modules.bugzilla.repository.BugzillaRepository.class);
    }

    public boolean isValid() {
        return validate();
    }

    private String getUrl() {
        String url = panel.urlField.getText().trim();
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url; // NOI18N
    }

    private String getName() {
        return panel.nameField.getText();
    }

    private String getUser() {
        return panel.userField.getText();
    }

    private String getPassword() {
        return new String(panel.psswdField.getPassword());
    }

    private String getHttpUser() {
        return panel.httpCheckBox.isSelected() ? panel.httpUserField.getText() : null;
    }

    private String getHttpPassword() {
        return panel.httpCheckBox.isSelected() ? new String(panel.httpPsswdField.getPassword()) : null;
    }

    private boolean validate() {
        if(validateError) {
            return false;
        }
        errorMessage = null;

        panel.validateButton.setEnabled(false);

        String name = panel.nameField.getText().trim();
        if(name.equals("")) { // NOI18N
            errorMessage = "Missing name"; // XXX bundle me
            return false;
        }

        String[] repositories = null;
        if(repository.getTaskRepository() == null) {
            repositories = BugzillaConfig.getInstance().getRepositories();
            for (String repositoryName : repositories) {
                if(name.equals(repositoryName)) {
                    errorMessage = "Issue tracker with the same name alreay exists"; // XXX bundle me
                    return false;
                }
            }
        }

        String url = getUrl();
        if(url.equals("")) { // NOI18N
            errorMessage = "Missing URL"; // XXX bundle me
            return false;
        }
        try {
            new URL(url);
        } catch (MalformedURLException ex) {
            errorMessage = "Wrong URL format"; // XXX bundle me
            return false;
        }

        if(repository.getTaskRepository() == null) {
            for (String repositoryName : repositories) {
                BugzillaRepository repo = BugzillaConfig.getInstance().getRepository(repositoryName);
                if(url.trim().equals(repo.getUrl())) {
                    errorMessage = "Issue tracker with the same url already exists"; // XXX bundle me
                    return false;
                }
            }
        }

        panel.validateButton.setEnabled(true);
        return true;
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx(getClass());
    }

    @Override
    public String getErrorMessage() {
        return errorMessage;
    }

    @Override
    public void applyChanges() {
        String newName = panel.nameField.getText().trim();
        if(!newName.equals(repository.getDisplayName())) {
            BugzillaConfig.getInstance().removeRepository(repository.getDisplayName());
        }
        repository.setName(newName);
        repository.setTaskRepository(
            getName(),
            getUrl(),
            getUser(),
            getPassword(),
            getHttpUser(),
            getHttpPassword());
        Bugzilla.getInstance().addRepository(repository);
        repository.getNode().setName(newName);
    }

    void populate() {
        if(repository.getTaskRepository() != null) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    populating = true;
                    AuthenticationCredentials c = repository.getTaskRepository().getCredentials(AuthenticationType.REPOSITORY);
                    panel.userField.setText(c.getUserName());
                    panel.psswdField.setText(c.getPassword());
                    c = repository.getTaskRepository().getCredentials(AuthenticationType.HTTP);
                    if(c != null) {
                        String httpUser = c.getUserName();
                        String httpPsswd = c.getPassword();
                        if(httpUser != null && !httpUser.equals("") &&          // NOI18N
                           httpPsswd != null && !httpPsswd.equals(""))          // NOI18N
                        {
                            panel.httpCheckBox.setSelected(true);
                            panel.httpUserField.setText(httpUser);
                            panel.httpPsswdField.setText(httpPsswd);
                        }
                    }
                    panel.urlField.setText(repository.getTaskRepository().getUrl());
                    panel.nameField.setText(repository.getDisplayName());
                    populating = false;
                }
            });
        }
    }

    public void insertUpdate(DocumentEvent e) {
        if(populating) return;
        validateErrorOff(e);
        fireDataChanged();
    }

    public void removeUpdate(DocumentEvent e) {
        if(populating) return;
        validateErrorOff(e);
        fireDataChanged();
    }

    public void changedUpdate(DocumentEvent e) {
        if(populating) return;
        validateErrorOff(e);
        fireDataChanged();
    }

    public void actionPerformed(ActionEvent e) {
        if(e.getSource() == panel.validateButton) {
            onValidate();
        }
    }

    private void onValidate() {
        RequestProcessor rp = Bugzilla.getInstance().getRequestProcessor();

        final Task[] task = new Task[1];
        Cancellable c = new Cancellable() {
            public boolean cancel() {
                panel.progressPanel.setVisible(false);
                panel.validateLabel.setVisible(false);
                if(task[0] != null) {
                    task[0].cancel();
                }
                return true;
            }
        };
        final ProgressHandle handle = ProgressHandleFactory.createHandle(NbBundle.getMessage(RepositoryPanel.class, "LBL_Validating"), c); // NOI18N
        JComponent comp = ProgressHandleFactory.createProgressComponent(handle);
        panel.progressPanel.removeAll();
        panel.progressPanel.add(comp, BorderLayout.CENTER);

        task[0] = rp.create(new Runnable() {
            public void run() {
                handle.start();
                panel.progressPanel.setVisible(true);
                panel.validateLabel.setVisible(true);
                panel.enableFields(false);
                panel.validateLabel.setText(NbBundle.getMessage(RepositoryPanel.class, "LBL_Validating")); // NOI18N
                try {
                    repository.resetRepository(); // reset mylyns caching
                    TaskRepository taskRepo = BugzillaRepository.createTaskRepository(
                            getName(),
                            getUrl(),
                            getUser(),
                            getPassword(),
                            getHttpUser(),
                            getHttpPassword());

                    ValidateCommand cmd = new ValidateCommand(taskRepo);
                    repository.getExecutor().execute(cmd, false);
                    if(cmd.hasFailed()) {
                        if(cmd.getErrorMessage() == null) {
                            Bugzilla.LOG.warning("validate command has failed, yet the returned error message is null."); // NOI18N
                            errorMessage = "Validation failed."; // XXX bundle me
                        } else {
                            errorMessage = cmd.getErrorMessage();
                        }
                        validateError = true;
                        fireDataChanged();
                        
                    }
                } finally {
                    panel.enableFields(true);
                    panel.progressPanel.setVisible(false);
                    panel.validateLabel.setVisible(false);
                    handle.finish();
                }
            }
        });
        task[0].schedule(0);
    }

    private void validateErrorOff(DocumentEvent e) {
        if (e.getDocument() == panel.userField.getDocument() || e.getDocument() == panel.urlField.getDocument() || e.getDocument() == panel.psswdField.getDocument()) {
            validateError = false;
        }
    }
}
