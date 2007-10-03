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

package org.netbeans.modules.subversion.ui.history;

import org.tigris.subversion.svnclientadapter.*;
import org.netbeans.modules.subversion.ui.browser.Browser;
import org.netbeans.modules.subversion.RepositoryFile;
import org.netbeans.modules.subversion.util.SvnUtils;
import org.openide.util.NbBundle;
import javax.swing.*;
import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import org.netbeans.modules.subversion.client.SvnClientExceptionHandler;

/**
 * Packages search criteria in Search History panel.
 *
 * @author Maros Sandor
 */
class SearchCriteriaPanel extends javax.swing.JPanel {
    
    private final File[] roots;
    private final SVNUrl url;
    
    /** Creates new form SearchCriteriaPanel */
    public SearchCriteriaPanel(File [] roots) {
        this.roots = roots;
        this.url = null;
        initComponents();
    }

    public SearchCriteriaPanel(SVNUrl url) {
        this.url = url;
        this.roots = null;
        initComponents();
    }
    
    public SVNRevision getFrom() {
        String s = tfFrom.getText().trim();
        if(s.length() == 0) {
            return new SVNRevision.Number(1);
        }
        return toRevision(s);
    }

    public SVNRevision getTo() {
        String s = tfTo.getText().trim();
        if(s.length() == 0) {
            return SVNRevision.HEAD;
        }
        return toRevision(s);
    }
    
    private Date parseDate(String s) {
        if (s == null) return null;
        for (int i = 0; i < SearchExecutor.dateFormats.length; i++) {
            DateFormat dateformat = SearchExecutor.dateFormats[i];
            try {
                return dateformat.parse(s);
            } catch (ParseException e) {
                // try the next one
            }
        }
        return null;
    }

    private SVNRevision toRevision(String s) {
        Date date = parseDate(s);
        if (date != null) {
            return new SVNRevision.DateSpec(date);
        } else {
            if ("BASE".equals(s)) { // NOI18N
                return SVNRevision.BASE;
            } else if ("HEAD".equals(s)) { // NOI18N
                return SVNRevision.HEAD;
            } else {
                try {
                    return new SVNRevision.Number(Long.parseLong(s));
                } catch (NumberFormatException ex) {
                    // do nothing
                }
            }
        }
        return null;    
    }  
    
    public String getCommitMessage() {
        String s = tfCommitMessage.getText().trim();
        return s.length() > 0 ? s : null;
    }

    public String getUsername() {
        String s = tfUsername.getText().trim();
        return s.length() > 0 ? s : null;
    }

    public void setFrom(String from) {
        if (from == null) from = "";  // NOI18N
        tfFrom.setText(from);
    }

    public void setTo(String to) {
        if (to == null) to = "";  // NOI18N
        tfTo.setText(to);
    }
    
    public void setCommitMessage(String message) {
        if (message == null) message = ""; // NOI18N
        tfCommitMessage.setText(message);
    }

    public void setUsername(String username) {
        if (username == null) username = ""; // NOI18N
        tfUsername.setText(username);
    }
    
    public void addNotify() {
        super.addNotify();
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                tfCommitMessage.requestFocusInWindow();
            }
        });
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jLabel1 = new javax.swing.JLabel();
        tfCommitMessage = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        tfUsername = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        bBrowseFrom = new javax.swing.JButton();
        jLabel4 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        bBrowseTo = new javax.swing.JButton();

        setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 12, 0, 11));
        setLayout(new java.awt.GridBagLayout());

        jLabel1.setLabelFor(tfCommitMessage);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/subversion/ui/history/Bundle"); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, bundle.getString("CTL_UseCommitMessage")); // NOI18N
        jLabel1.setToolTipText(bundle.getString("TT_CommitMessage")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        add(jLabel1, gridBagConstraints);

        tfCommitMessage.setColumns(20);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 0);
        add(tfCommitMessage, gridBagConstraints);

        jLabel2.setLabelFor(tfUsername);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, bundle.getString("CTL_UseUsername")); // NOI18N
        jLabel2.setToolTipText(bundle.getString("TT_Username")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        add(jLabel2, gridBagConstraints);

        tfUsername.setColumns(20);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 4, 0, 0);
        add(tfUsername, gridBagConstraints);

        jLabel3.setLabelFor(tfFrom);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, bundle.getString("CTL_UseFrom")); // NOI18N
        jLabel3.setToolTipText(bundle.getString("TT_From")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        add(jLabel3, gridBagConstraints);

        tfFrom.setColumns(20);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 4);
        add(tfFrom, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel5, bundle.getString("CTL_FromToHint")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new java.awt.Insets(0, 2, 0, 4);
        add(jLabel5, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(bBrowseFrom, bundle.getString("CTL_BrowseFrom")); // NOI18N
        bBrowseFrom.setToolTipText(bundle.getString("TT_BrowseFrom")); // NOI18N
        bBrowseFrom.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                onFromBrowse(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 0);
        add(bBrowseFrom, gridBagConstraints);

        jLabel4.setLabelFor(tfTo);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel4, bundle.getString("CTL_UseTo")); // NOI18N
        jLabel4.setToolTipText(bundle.getString("TT_To")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 0, 0);
        add(jLabel4, gridBagConstraints);

        tfTo.setColumns(20);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 4);
        add(tfTo, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel6, bundle.getString("CTL_FromToHint")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.PAGE_START;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 2, 0, 4);
        add(jLabel6, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(bBrowseTo, bundle.getString("CTL_BrowseTo")); // NOI18N
        bBrowseTo.setToolTipText(bundle.getString("TT_BrowseTo")); // NOI18N
        bBrowseTo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                onToBrowse(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 0);
        add(bBrowseTo, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void onToBrowse(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_onToBrowse
        onBrowse(tfTo);
    }//GEN-LAST:event_onToBrowse

    private void onFromBrowse(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_onFromBrowse
        onBrowse(tfFrom);
    }//GEN-LAST:event_onFromBrowse

    private void onBrowse(final JTextField destination) {
        final SVNUrl repositoryUrl;
        try {            
            repositoryUrl = url != null ? url : SvnUtils.getRepositoryRootUrl(roots[0]); 
        } catch (SVNClientException ex) {
            SvnClientExceptionHandler.notifyException(ex, true, true);
            return;
        }                

        String title = destination == tfFrom ? NbBundle.getMessage(SearchCriteriaPanel.class, "CTL_BrowseTag_StartTag") : NbBundle.getMessage(SearchCriteriaPanel.class, "CTL_BrowseTag_EndTag"); // NOI18N
        final Browser browser;
        RepositoryFile repoFile = new RepositoryFile(repositoryUrl, SVNRevision.HEAD);
        int browserMode;
        if(roots[0].isFile()) {
            browserMode = Browser.BROWSER_SINGLE_SELECTION_ONLY | Browser.BROWSER_SHOW_FILES | Browser.BROWSER_FOLDERS_SELECTION_ONLY;                        
        } else {
            browserMode = Browser.BROWSER_SHOW_FILES;                        
        }        
        browser = new Browser(title, browserMode, repoFile, null, null, Browser.BROWSER_HELP_ID_SEARCH_HISTORY);        
        final RepositoryFile[] repositoryFiles = browser.getRepositoryFiles();
        if(repositoryFiles == null || repositoryFiles.length == 0) {
            return;
        }
        
//        final SVNUrl tagURL = repositoryFiles[0].getFileUrl();
//        destination.setText(NbBundle.getMessage(SearchCriteriaPanel.class, "MSG_Search_PleaseWait")); // NOI18N

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                destination.setText(repositoryFiles[0].getRevision().toString());
            }
        });
        
//        RequestProcessor rp = Subversion.getInstance().getRequestProcessor(repositoryUrl);
//        SvnProgressSupport support = new SvnProgressSupport() {
//            public void perform() {                    
//                processTagSelection(destination, repositoryUrl, tagURL, this);
//            }
//        };
//        support.start(rp, repositoryUrl, NbBundle.getMessage(SearchCriteriaPanel.class, "MSG_Search_ResolvingTagProgress")); // NOI18N
    }
//
//    private void processTagSelection(final JTextField destination, SVNUrl repositoryURL, final SVNUrl tagURL, SvnProgressSupport progress) {
//        SvnClient client;
//        try {
//            client = Subversion.getInstance().getClient(repositoryURL, progress);
//        } catch (SVNClientException ex) {
//            SvnClientExceptionHandler.notifyException(ex, true, true);
//            return;
//        }
//        ISVNLogMessage[] log = new org.tigris.subversion.svnclientadapter.ISVNLogMessage[0];
//        try {
//            log = client.getLogMessages(tagURL, null, new SVNRevision.Number(1), SVNRevision.HEAD, true, false, 1);
//        } catch (SVNClientException e) {
//            SvnClientExceptionHandler.notifyException(e, true, true);
//            return;
//        }
//        final SVNRevision.Number revision = log[0].getRevision();
//        SwingUtilities.invokeLater(new Runnable() {
//            public void run() {
//                destination.setText(Long.toString(revision.getNumber()));
//            }
//        });
//    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton bBrowseFrom;
    private javax.swing.JButton bBrowseTo;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JTextField tfCommitMessage;
    final javax.swing.JTextField tfFrom = new javax.swing.JTextField();
    final javax.swing.JTextField tfTo = new javax.swing.JTextField();
    private javax.swing.JTextField tfUsername;
    // End of variables declaration//GEN-END:variables
    
}
