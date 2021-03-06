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

package org.netbeans.modules.subversion.ui.wizards.checkoutstep;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.File;
import java.net.MalformedURLException;
import java.util.logging.Level;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileFilter;
import org.netbeans.modules.subversion.RepositoryFile;
import org.netbeans.modules.subversion.Subversion;
import org.netbeans.modules.subversion.SvnModuleConfig;
import org.netbeans.modules.subversion.ui.browser.Browser;
import org.netbeans.modules.subversion.ui.wizards.AbstractStep;
import org.netbeans.modules.subversion.ui.wizards.CheckoutWizard;
import org.netbeans.modules.subversion.ui.browser.RepositoryPaths;
import org.netbeans.modules.subversion.ui.search.SvnSearch;
import org.netbeans.modules.versioning.util.AccessibleJFileChooser;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.tigris.subversion.svnclientadapter.SVNRevision;

/**
 * @author Tomas Stupka
 */
public class CheckoutStep extends AbstractStep implements ActionListener, DocumentListener, FocusListener {

    public static final String CHECKOUT_DIRECTORY = "checkoutStep.checkoutDirectory";
    
    private CheckoutPanel workdirPanel;
    private RepositoryPaths repositoryPaths;

    public HelpCtx getHelp() {    
        return new HelpCtx(CheckoutStep.class);
    }    

    protected JComponent createComponent() {
        if (workdirPanel == null) {
            workdirPanel = new CheckoutPanel();
            workdirPanel.browseWorkdirButton.addActionListener(this);
            workdirPanel.browseRepositoryButton.addActionListener(this);
            workdirPanel.scanForProjectsCheckBox.addActionListener(this);
                    
            workdirPanel.workdirTextField.setText(defaultWorkingDirectory().getPath());            
            workdirPanel.workdirTextField.getDocument().addDocumentListener(this);                
            workdirPanel.workdirTextField.addFocusListener(this);
            workdirPanel.repositoryPathTextField.getDocument().addDocumentListener(this);        
            workdirPanel.repositoryPathTextField.addFocusListener(this);
            workdirPanel.revisionTextField.getDocument().addDocumentListener(this);
            workdirPanel.revisionTextField.addFocusListener(this);                        
        }          
        validateUserInput(true);                                
        return workdirPanel;              
    }

    public void setup(RepositoryFile repositoryFile) {
        if(repositoryPaths == null) {                    
            repositoryPaths = 
                new RepositoryPaths(
                        repositoryFile, 
                        workdirPanel.repositoryPathTextField, 
                        workdirPanel.browseRepositoryButton, 
                        workdirPanel.revisionTextField, 
                        workdirPanel.searchRevisionButton
                );        
            String browserPurposeMessage = org.openide.util.NbBundle.getMessage(CheckoutStep.class, "LBL_BrowserMessage");
            int browserMode = Browser.BROWSER_SHOW_FILES | Browser.BROWSER_FOLDERS_SELECTION_ONLY;
            repositoryPaths.setupBehavior(browserPurposeMessage, browserMode, Browser.BROWSER_HELP_ID_CHECKOUT, SvnSearch.SEACRH_HELP_ID_CHECKOUT);
        } else {
            repositoryPaths.setRepositoryFile(repositoryFile);
        }                
        workdirPanel.repositoryPathTextField.setText(repositoryFile.getPath());
        if(!repositoryFile.getRevision().equals(SVNRevision.HEAD)) {
            workdirPanel.revisionTextField.setText(repositoryFile.getRevision().toString());
        } else {
            workdirPanel.revisionTextField.setText("");
        }
        workdirPanel.scanForProjectsCheckBox.setSelected(SvnModuleConfig.getDefault().getShowCheckoutCompleted());
    }    
     
    protected void validateBeforeNext() {
        if (validateUserInput(true)) {
            String text = workdirPanel.workdirTextField.getText();
            File file = new File(text);
            if (file.exists() == false) {
                boolean done = file.mkdirs();
                if (done == false) {
                    invalid(org.openide.util.NbBundle.getMessage(CheckoutWizard.class, "BK2013") + file.getPath());// NOI18N
                }
            }
        }
    }

    private boolean validateUserInput(boolean full) {                
        if(repositoryPaths != null) {
            try {           
                repositoryPaths.getRepositoryFiles();
            } catch (NumberFormatException ex) {
                invalid(org.openide.util.NbBundle.getMessage(CheckoutStep.class, "BK2018"));// NOI18N
                return false;
            } catch (MalformedURLException ex) {
                invalid(org.openide.util.NbBundle.getMessage(CheckoutStep.class, "BK2015"));// NOI18N
                return false;
            }
        }
        
        String text = workdirPanel.workdirTextField.getText();
        if (text == null || text.length() == 0) {
            invalid(org.openide.util.NbBundle.getMessage(CheckoutStep.class, "BK2014"));// NOI18N
            return false;
        }                
        
        String errorMessage = null;
        if (full) {
            File file = new File(text);
            if (file.exists() == false) {
                // it's automaticaly create later on, check for permisions here
                File parent = file.getParentFile();
                while (parent != null) {
                    if (parent.exists()) {
                        if (parent.canWrite() == false) {
                            errorMessage = org.openide.util.NbBundle.getMessage(CheckoutStep.class, "BK2016") + parent.getPath();// NOI18N
                        }
                        break;
                    }
                    parent = parent.getParentFile();
                }
            } else {
                if (file.isFile()) {
                    errorMessage = org.openide.util.NbBundle.getMessage(CheckoutStep.class, "BK2017");// NOI18N
                }
            }
        }

        if (errorMessage == null) {
            valid();
        } else {
            invalid(errorMessage);
        }

        return errorMessage == null;
    }
    
    private void onBrowseWorkdir() {
        File defaultDir = defaultWorkingDirectory();
        JFileChooser fileChooser = new AccessibleJFileChooser(NbBundle.getMessage(CheckoutStep.class, "ACSD_BrowseFolder"), defaultDir);// NOI18N
        fileChooser.setDialogTitle(NbBundle.getMessage(CheckoutStep.class, "BK0010"));// NOI18N
        fileChooser.setMultiSelectionEnabled(false);
        FileFilter[] old = fileChooser.getChoosableFileFilters();
        for (int i = 0; i < old.length; i++) {
            FileFilter fileFilter = old[i];
            fileChooser.removeChoosableFileFilter(fileFilter);

        }
        fileChooser.addChoosableFileFilter(new FileFilter() {
            public boolean accept(File f) {
                return f.isDirectory();
            }
            public String getDescription() {
                return NbBundle.getMessage(CheckoutStep.class, "BK0008");// NOI18N
            }
        });
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.showDialog(workdirPanel, NbBundle.getMessage(CheckoutStep.class, "BK0009"));// NOI18N
        File f = fileChooser.getSelectedFile();
        if (f != null) {
            workdirPanel.workdirTextField.setText(f.getAbsolutePath());
        }                
    }    
    
    /**
     * Returns file to be initaly used.
     * <ul>
     * <li>first is takes text in workTextField
     * <li>then recent project folder
     * <li>then recent checkout folder
     * <li>finally <tt>user.home</tt>
     * <ul>
     */
    private File defaultWorkingDirectory() {
        File defaultDir = null;
        String current = workdirPanel.workdirTextField.getText();
        if (current != null && !(current.trim().equals(""))) {  // NOI18N
            File currentFile = new File(current);
            while (currentFile != null && currentFile.exists() == false) {
                currentFile = currentFile.getParentFile();
            }
            if (currentFile != null) {
                if (currentFile.isFile()) {
                    defaultDir = currentFile.getParentFile();
                } else {
                    defaultDir = currentFile;
                }
            }
        }

        if (defaultDir == null) {
            String coDir = SvnModuleConfig.getDefault().getPreferences().get(CHECKOUT_DIRECTORY, null);
            if(coDir != null) {
                defaultDir = new File(coDir);               
            }            
        }

        if (defaultDir == null) {
            File projectFolder = ProjectChooser.getProjectsFolder();
            if (projectFolder.exists() && projectFolder.isDirectory()) {
                defaultDir = projectFolder;
            }
        }

        if (defaultDir == null) {
            defaultDir = new File(System.getProperty("user.home"));  // NOI18N
        }

        return defaultDir;
    }

    public void insertUpdate(DocumentEvent e) {        
        validateUserInput(false);
        refreshSkipLabel();
    }

    public void removeUpdate(DocumentEvent e) {
        validateUserInput(false);
        refreshSkipLabel();
    }

    public void changedUpdate(DocumentEvent e) {        
    }

    public void focusGained(FocusEvent e) {
    }

    public void focusLost(FocusEvent e) {
        validateUserInput(true);
        refreshSkipLabel();
    }
        
    public void actionPerformed(ActionEvent e) {
        if(e.getSource()==workdirPanel.browseWorkdirButton) {            
            onBrowseWorkdir();
        } else if (e.getSource() == workdirPanel.scanForProjectsCheckBox) {
            SvnModuleConfig.getDefault().setShowCheckoutCompleted(workdirPanel.scanForProjectsCheckBox.isSelected());
        }
    }
    
    public File getWorkdir() {
        return new File(workdirPanel.workdirTextField.getText());
    }        

    public RepositoryFile[] getRepositoryFiles() {
        try {            
            return repositoryPaths.getRepositoryFiles(".");
        } catch (MalformedURLException ex) {
            Subversion.LOG.log(Level.INFO, null, ex); // should not happen
        }
        return null;
    }

    public boolean isAtWorkingDirLevel() {
        return workdirPanel.atWorkingDirLevelCheckBox.isSelected();
    }

    private void refreshSkipLabel() {
        if(workdirPanel.repositoryPathTextField.getText().trim().equals("")) { 
            resetWorkingDirLevelCheckBox();
            return;
        }        
        
        RepositoryFile[] repositoryFiles = null;
        try {
            repositoryFiles = repositoryPaths.getRepositoryFiles();
        } catch (NumberFormatException ex) {
            // ignore
        } catch (MalformedURLException ex) {
            // ignore
        }
        
        if(repositoryFiles == null || 
           repositoryFiles.length >  1) 
        { 
            resetWorkingDirLevelCheckBox();
            return;
        }        
        
        String repositoryFolder = repositoryFiles[0].getFileUrl().getLastPathSegment().trim();                           
        if(repositoryFolder.equals("")  ||      // the skip option doesn't make sense if there is no one, 
           repositoryFolder.equals("."))        // or more as one folder to be checked out  
        {
            resetWorkingDirLevelCheckBox();
            return;
        } else {                        
            workdirPanel.atWorkingDirLevelCheckBox.setText (
                    NbBundle.getMessage(CheckoutStep.class, 
                                        "CTL_Checkout_CheckoutContentFolder", 
                                         new Object[] {repositoryFolder})
            );
            workdirPanel.atWorkingDirLevelCheckBox.setEnabled(true);                
        }
    }
    
    private void resetWorkingDirLevelCheckBox() {
        workdirPanel.atWorkingDirLevelCheckBox.setText(NbBundle.getMessage(CheckoutStep.class, "CTL_Checkout_CheckoutContentEmpty"));
        workdirPanel.atWorkingDirLevelCheckBox.setEnabled(false);
    }
}

