/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;
import org.jdesktop.layout.GroupLayout;
import org.openide.awt.Mnemonics;
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
import static org.jdesktop.layout.GroupLayout.BASELINE;
import static org.jdesktop.layout.GroupLayout.DEFAULT_SIZE;
import static org.jdesktop.layout.GroupLayout.LEADING;
import static org.jdesktop.layout.GroupLayout.PREFERRED_SIZE;
import static org.jdesktop.layout.LayoutStyle.RELATED;

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

    // <editor-fold desc="UI Layout Code" defaultstate="collapsed">
    private void initComponents() {

        JLabel jLabel1      = new JLabel();
        tfCommitMessage     = new JTextField();
        JLabel jLabel2      = new JLabel();
        tfUsername          = new JTextField();
        JLabel jLabel3      = new JLabel();
        JLabel jLabel4      = new JLabel();
        JLabel jLabel5      = new JLabel();
        JLabel jLabel6      = new JLabel();
        JButton bBrowseFrom = new JButton();
        JButton bBrowseTo   = new JButton();

        jLabel1.setLabelFor(tfCommitMessage);
        jLabel2.setLabelFor(tfUsername);
        jLabel3.setLabelFor(tfFrom);
        jLabel4.setLabelFor(tfTo);

        tfCommitMessage.setColumns(20);
        tfUsername.setColumns(20);
        tfFrom.setColumns(20);
        tfTo.setColumns(20);

        ResourceBundle bundle = ResourceBundle.getBundle("org/netbeans/modules/subversion/ui/history/Bundle"); // NOI18N

        Mnemonics.setLocalizedText(jLabel1, bundle.getString("CTL_UseCommitMessage")); // NOI18N
        jLabel1.setToolTipText(bundle.getString("TT_CommitMessage")); // NOI18N

        Mnemonics.setLocalizedText(jLabel2, bundle.getString("CTL_UseUsername")); // NOI18N
        jLabel2.setToolTipText(bundle.getString("TT_Username")); // NOI18N

        Mnemonics.setLocalizedText(jLabel3, bundle.getString("CTL_UseFrom")); // NOI18N
        jLabel3.setToolTipText(bundle.getString("TT_From")); // NOI18N

        Mnemonics.setLocalizedText(jLabel4, bundle.getString("CTL_UseTo")); // NOI18N
        jLabel4.setToolTipText(bundle.getString("TT_To")); // NOI18N

        Mnemonics.setLocalizedText(jLabel5, bundle.getString("CTL_FromToHint")); // NOI18N

        Mnemonics.setLocalizedText(jLabel6, bundle.getString("CTL_FromToHint")); // NOI18N

        Mnemonics.setLocalizedText(bBrowseFrom, bundle.getString("CTL_BrowseFrom")); // NOI18N
        bBrowseFrom.setToolTipText(bundle.getString("TT_BrowseFrom")); // NOI18N

        Mnemonics.setLocalizedText(bBrowseTo, bundle.getString("CTL_BrowseTo")); // NOI18N
        bBrowseTo.setToolTipText(bundle.getString("TT_BrowseTo")); // NOI18N

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
                layout.createSequentialGroup()
                        .add(12)
                        .add(layout.createParallelGroup(LEADING)
                                .add(jLabel1)
                                .add(jLabel2)
                                .add(jLabel3)
                                .add(jLabel4))
                        .addPreferredGap(RELATED)
                        .add(layout.createParallelGroup(LEADING)
                                .add(tfCommitMessage)
                                .add(tfUsername)
                                .add(layout.createSequentialGroup()
                                        .add(tfFrom)
                                        .addPreferredGap(RELATED)
                                        .add(jLabel5))
                                .add(layout.createSequentialGroup()
                                        .add(tfTo)
                                        .addPreferredGap(RELATED)
                                        .add(jLabel6)))
                        .addPreferredGap(RELATED)
                        .add(layout.createParallelGroup(LEADING)
                                .add(bBrowseFrom)
                                .add(bBrowseTo))
                        .add(11)
        );
        layout.setVerticalGroup(
                layout.createSequentialGroup()
                        .add(8)
                        .add(layout.createParallelGroup(BASELINE)
                                .add(jLabel1)
                                .add(tfCommitMessage))
                        .addPreferredGap(RELATED)
                        .add(layout.createParallelGroup(BASELINE)
                                .add(jLabel2)
                                .add(tfUsername))
                        .addPreferredGap(RELATED)
                        .add(layout.createParallelGroup(BASELINE)
                                .add(jLabel3)
                                .add(tfFrom)
                                .add(jLabel5)
                                .add(bBrowseFrom))
                        .addPreferredGap(RELATED)
                        .add(layout.createParallelGroup(BASELINE)
                                .add(jLabel4)
                                .add(tfTo)
                                .add(jLabel6)
                                .add(bBrowseTo))
                        //no gap at the bottom
        );

        bBrowseFrom.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                        onFromBrowse(evt);
                }
        });
        bBrowseTo.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                        onToBrowse(evt);
                }
        });
    }// </editor-fold>

    private void onToBrowse(ActionEvent evt) {
        onBrowse(tfTo);
    }

    private void onFromBrowse(ActionEvent evt) {
        onBrowse(tfFrom);
    }

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
    
        // Variables declaration
        private JTextField tfCommitMessage;
        private JTextField tfUsername;
        final JTextField tfFrom = new JTextField();
        final JTextField tfTo = new JTextField();
        // End of variables declaration
    
}
