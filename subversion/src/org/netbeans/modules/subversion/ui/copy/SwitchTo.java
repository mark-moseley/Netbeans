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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.subversion.ui.copy;

import java.io.File;
import java.net.MalformedURLException;
import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.text.JTextComponent;
import org.netbeans.modules.subversion.RepositoryFile;
import org.netbeans.modules.subversion.ui.browser.Browser;
import org.netbeans.modules.subversion.ui.browser.RepositoryPaths;
import org.netbeans.modules.subversion.util.SvnUtils;
import org.openide.ErrorManager;
import org.openide.util.NbBundle;
import org.tigris.subversion.svnclientadapter.SVNRevision;
import org.tigris.subversion.svnclientadapter.SVNUrl;

/**
 *
 * @author Tomas Stupka
 */
public class SwitchTo extends CopyDialog {

    private RepositoryPaths repositoryPaths;
    private final File root;
    private final RepositoryFile repositoryRoot;
        
    public SwitchTo(RepositoryFile repositoryRoot, File root, boolean localChanges) {
        super(new SwitchToPanel(), NbBundle.getMessage(SwitchTo.class, "CTL_SwitchTo_Title", root.getName()), NbBundle.getMessage(SwitchTo.class, "CTL_SwitchTo_Action")); // NOI18N
        
        this.root = root;        
        this.repositoryRoot = repositoryRoot;       
        
        SwitchToPanel panel = getSwitchToPanel();
        panel.warningLabel.setVisible(localChanges);

        repositoryPaths = 
            new SwitchToRepositoryPaths(
                repositoryRoot, 
                (JTextField) panel.urlComboBox.getEditor().getEditorComponent(),
                panel.browseRepositoryButton,
                panel.revisionTextField,
                panel.searchRevisionButton
            );
        repositoryPaths.addPropertyChangeListener(this);
        getSwitchToPanel().getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SwitchTo.class, "CTL_SwitchTo_RepositoryFile"));
        
        String browserPurposeMessage;
        int browserMode;
        if(root.isFile()) {
            getSwitchToPanel().urlLabel.setText(NbBundle.getMessage(SwitchTo.class, "CTL_SwitchTo_RepositoryFile")); // NOI18N
            browserPurposeMessage = org.openide.util.NbBundle.getMessage(CreateCopy.class, "LBL_BrowserMessageSwitchFile");
            browserMode = Browser.BROWSER_SINGLE_SELECTION_ONLY | Browser.BROWSER_SHOW_FILES | Browser.BROWSER_FILES_SELECTION_ONLY;                        
        } else {
            browserPurposeMessage = org.openide.util.NbBundle.getMessage(CreateCopy.class, "LBL_BrowserMessageSwitchFolder");
            browserMode = Browser.BROWSER_SINGLE_SELECTION_ONLY;                                    
        }
        repositoryPaths.setupBrowserBehavior(browserPurposeMessage, browserMode, null);    
        setupUrlComboBox(panel.urlComboBox, SwitchTo.class.getName());                
    }            
    
    RepositoryFile getRepositoryFile() {        
        try {
            RepositoryFile[] repositoryFiles = repositoryPaths.getRepositoryFiles();
            if(repositoryFiles.length > 0) {
                return repositoryFiles[0];
            } else {
                SVNRevision revision = repositoryPaths.getRevision();
                if(revision == null) {
                    return null;
                }
                SVNUrl url = SvnUtils.getRepositoryUrl(root);                
                RepositoryFile rf = new RepositoryFile(repositoryRoot.getRepositoryUrl(), url, revision);
                return rf;
            }
        } catch (MalformedURLException ex) {
            // should be already checked and 
            // not happen at this place anymore
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);            
        }
        return null;
    }    
    
    private SwitchToPanel getSwitchToPanel() {
        return (SwitchToPanel) getPanel();
    }    
    
    private class SwitchToRepositoryPaths extends RepositoryPaths {
        
        public SwitchToRepositoryPaths (RepositoryFile repositoryFile, 
                           JTextComponent repositoryPathTextField,  
                           JButton browseButton, 
                           JTextField revisionTextField, 
                           JButton searchRevisionButton) 
        {
            super(repositoryFile, repositoryPathTextField, browseButton, revisionTextField, searchRevisionButton);
        }                    
        
        protected boolean acceptEmptyUrl() {
            return true;
        }
    }
}
