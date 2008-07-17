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

package org.netbeans.modules.subversion.ui.wizards.importstep;

import java.awt.event.FocusEvent;
import java.io.File;
import java.net.MalformedURLException;
import java.util.logging.Level;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.subversion.FileStatusCache;
import org.netbeans.modules.subversion.RepositoryFile;
import org.netbeans.modules.subversion.Subversion;
import org.netbeans.modules.subversion.client.SvnClient;
import org.netbeans.modules.subversion.client.SvnClientExceptionHandler;
import org.netbeans.modules.subversion.client.WizardStepProgressSupport;
import org.netbeans.modules.subversion.ui.browser.Browser;
import org.netbeans.modules.subversion.ui.wizards.AbstractStep;
import org.netbeans.modules.subversion.ui.browser.BrowserAction;
import org.netbeans.modules.subversion.ui.browser.RepositoryPaths;
import org.netbeans.modules.subversion.ui.checkout.CheckoutAction;
import org.netbeans.modules.subversion.util.FileUtils;
import org.netbeans.modules.subversion.util.SvnUtils;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.RequestProcessor;
import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.SVNUrl;

/**
 * @author Tomas Stupka
 */
public class ImportStep extends AbstractStep implements DocumentListener, WizardDescriptor.AsynchronousValidatingPanel, WizardDescriptor.FinishablePanel {
    
    private ImportPanel importPanel;

    private RepositoryPaths repositoryPaths;
    private BrowserAction[] actions;
    private File importDirectory;       
    private WizardStepProgressSupport support;
    
    public ImportStep(BrowserAction[] actions, File importDirectory) {
        this.actions = actions;
        this.importDirectory = importDirectory;
    }
    
    public HelpCtx getHelp() {    
        return new HelpCtx(ImportStep.class);
    }    

    protected JComponent createComponent() {
        if (importPanel == null) {
            importPanel = new ImportPanel();            
            importPanel.messageTextArea.getDocument().addDocumentListener(this);            
            importPanel.repositoryPathTextField.getDocument().addDocumentListener(this);                       
        }            
        return importPanel;              
    }

    protected void validateBeforeNext() {
        try {
            support =  new ImportProgressSupport(importPanel.progressPanel, importPanel.progressLabel);  
            SVNUrl url = getUrl();
            support.setRepositoryRoot(url);            
            RequestProcessor rp = Subversion.getInstance().getRequestProcessor(url);
            RequestProcessor.Task task = support.start(rp, url, org.openide.util.NbBundle.getMessage(ImportStep.class, "CTL_Import_Progress"));
            task.waitFinished();
        } finally {
            support = null;
        }
    }   
        
    public void prepareValidation() {        
    }

    private SVNUrl getUrl() {        
        RepositoryFile repositoryFile = getRepositoryFile();
        return repositoryFile.getRepositoryUrl();
    }
    
    public boolean validateUserInput() {
        invalid(null);
        
        String text = importPanel.repositoryPathTextField.getText().trim();
        if (text.length() == 0) {
            invalid(new AbstractStep.WizardMessage(org.openide.util.NbBundle.getMessage(ImportStep.class, "BK2014"), true)); // NOI18N
            return false;
        }        
        
        text = importPanel.messageTextArea.getText().trim();
        boolean valid = text.length() > 0;
        if(valid) {
            valid();
        } else {
            invalid(new AbstractStep.WizardMessage(org.openide.util.NbBundle.getMessage(ImportStep.class, "CTL_Import_MessageRequired"), true)); // NOI18N
        }

        return valid;
    }
    
    public void insertUpdate(DocumentEvent e) {
        validateUserInput();
    }

    public void removeUpdate(DocumentEvent e) {
        validateUserInput();
    }

    public void changedUpdate(DocumentEvent e) {
    }

    public void focusGained(FocusEvent e) {
        
    }

    public void focusLost(FocusEvent e) {
        validateUserInput();
    }

    public String getImportMessage() {
        return importPanel.messageTextArea.getText();
    }

    public void setup(RepositoryFile repositoryFile) {
        if(importPanel.repositoryPathTextField.getText().trim().equals("")) { // NOI18N
            // no value set yet ...
            if(repositoryPaths == null) {
                repositoryPaths =
                    new RepositoryPaths (
                        repositoryFile,
                        importPanel.repositoryPathTextField,
                        importPanel.browseRepositoryButton,
                        null,
                        null
                    );
                String browserPurposeMessage = org.openide.util.NbBundle.getMessage(ImportStep.class, "LBL_BrowserMessage");
                int browserMode = Browser.BROWSER_SINGLE_SELECTION_ONLY;
                repositoryPaths.setupBehavior(browserPurposeMessage, browserMode, actions, Browser.BROWSER_HELP_ID_IMPORT, null);
            } else {
                repositoryPaths.setRepositoryFile(repositoryFile);
            }
        }
        importPanel.repositoryPathTextField.setText(repositoryFile.getPath());
        validateUserInput();
    }

    public RepositoryFile getRepositoryFile() {
        try {
            return repositoryPaths.getRepositoryFiles()[0]; // more files doesn't make sence
        } catch (MalformedURLException ex) {
            Subversion.LOG.log(Level.INFO, null, ex);
        } 
        return null;
    }

    public SVNUrl getRepositoryFolderUrl() {
        return getRepositoryFile().getFileUrl();
    }

    public void stop() {
        if(support != null) {
            support.cancel();
        }
    }

    public boolean isFinishPanel() {
        return true;
    }
    
    private class ImportProgressSupport extends WizardStepProgressSupport {
        public ImportProgressSupport(JPanel panel, JLabel label) {
            super(panel);
        }
        public void perform() {
            AbstractStep.WizardMessage invalidMsg = null;
            try {
                if(!validateUserInput()) {
                    return;
                }        

                invalid(null);

                SvnClient client;
                try {
                    client = Subversion.getInstance().getClient(repositoryPaths.getRepositoryUrl(), this);
                } catch (SVNClientException ex) {
                    SvnClientExceptionHandler.notifyException(ex, true, true);
                    invalidMsg = new AbstractStep.WizardMessage(SvnClientExceptionHandler.parseExceptionMessage(ex), false);
                    return;
                }

                try {
                    RepositoryFile repositoryFile = getRepositoryFile();
                    SVNUrl repositoryUrl = repositoryFile.getRepositoryUrl();
                    try {
                        // if the user came back from the last step and changed the repository folder name,
                        // then this could be already a working copy ...    
                        FileUtils.deleteRecursively(new File(importDirectory.getAbsoluteFile() + "/" + SvnUtils.SVN_ADMIN_DIR)); // NOI18N
                        File importDummyFolder = new File(System.getProperty("java.io.tmpdir") + "/svn_dummy/" + importDirectory.getName()); // NOI18N
                        importDummyFolder.mkdirs();                     
                        importDummyFolder.deleteOnExit();
                        client.doImport(importDummyFolder, repositoryFile.getFileUrl(), getImportMessage(), false);
                    } catch (SVNClientException ex) {
                        if(SvnClientExceptionHandler.isFileAlreadyExists(ex.getMessage()) ) {
                            // ignore
                        } else {
                            throw ex;
                        }         
                    }
                    if(isCanceled()) {
                        return;
                    }

                    RepositoryFile[] repositoryFiles = new RepositoryFile[] { repositoryFile };
                    CheckoutAction.checkout(client, repositoryUrl, repositoryFiles, importDirectory, true, this);
                    Subversion.getInstance().versionedFilesChanged();
                    SvnUtils.refreshParents(importDirectory);
                    // XXX this is ugly and expensive! the client should notify (onNotify()) the cache. find out why it doesn't work...
                    Subversion.getInstance().getStatusCache().refreshRecursively(importDirectory);
                    if(isCanceled()) {                        
                        FileUtils.deleteRecursively(new File(importDirectory.getAbsoluteFile() + "/" + SvnUtils.SVN_ADMIN_DIR)); // NOI18N
                        return;
                    }
                } catch (SVNClientException ex) {
                    annotate(ex);
                    invalidMsg = new AbstractStep.WizardMessage(SvnClientExceptionHandler.parseExceptionMessage(ex), false);
                }

            } finally {
                Subversion.getInstance().versionedFilesChanged();
                if(isCanceled()) {
                    valid(new AbstractStep.WizardMessage(org.openide.util.NbBundle.getMessage(ImportStep.class, "MSG_Import_ActionCanceled"), false)); // NOI18N
                } else if(invalidMsg != null) {
                    valid(invalidMsg);
                } else {
                    valid();
                }
            }
        }            

        public void setEditable(boolean editable) {
            importPanel.browseRepositoryButton.setEnabled(editable);
            importPanel.messageTextArea.setEditable(editable);
            importPanel.repositoryPathTextField.setEditable(editable);
        }

        private void deleteDirectory(File file) {
             File[] files = file.listFiles();
             if(files !=null || files.length > 0) {
                 for (int i = 0; i < files.length; i++) {
                     if(files[i].isDirectory()) {
                         deleteDirectory(files[i]);
                     } else {
                        files[i].delete();
                     }
                 }
             }
             file.delete();
        }
    };

}

