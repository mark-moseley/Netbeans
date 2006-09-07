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

package org.netbeans.modules.subversion.ui.commit;

import org.netbeans.modules.subversion.client.ExceptionHandler;
import org.netbeans.modules.subversion.client.SvnClient;
import org.netbeans.modules.subversion.ui.actions.ContextAction;
import org.netbeans.modules.subversion.util.Context;
import org.netbeans.modules.subversion.*;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.nodes.Node;
import org.tigris.subversion.svnclientadapter.SVNBaseDir;
import org.tigris.subversion.svnclientadapter.SVNClientException;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.*;
import java.util.List;
import java.text.MessageFormat;

import org.netbeans.modules.subversion.client.SvnProgressSupport;
import org.netbeans.modules.subversion.util.SvnUtils;
import org.netbeans.modules.versioning.util.VersioningListener;
import org.netbeans.modules.versioning.util.VersioningEvent;
import org.openide.util.HelpCtx;
import org.openide.util.RequestProcessor;
import org.openide.util.NbBundle;
import org.tigris.subversion.svnclientadapter.ISVNProperty;
import org.tigris.subversion.svnclientadapter.SVNUrl;

/**
 * Commit action
 *
 * @author Petr Kuzel
 */
public class CommitAction extends ContextAction {

    protected String getBaseName(Node[] nodes) {
        return "CTL_MenuItem_Commit";    // NOI18N
    }

    protected boolean enable(Node[] nodes) {
        // XXX could be a performace issue, maybe a msg box in commit would be enough
        FileStatusCache cache = Subversion.getInstance().getStatusCache();
        File[] files = cache.listFiles(getContext(nodes), FileInformation.STATUS_LOCAL_CHANGE);
        return files.length > 0;
    }

    /** Run commit action. Shows UI */
    public static void commit(String contentTitle, final Context ctx) {
        FileStatusCache cache = Subversion.getInstance().getStatusCache();
        File[] roots = ctx.getFiles();
        if (roots.length == 0) {
            return;
        }
        
        File[][] split = SvnUtils.splitFlatOthers(roots);
        List<File> fileList = new ArrayList<File>();
        for (int c = 0; c < split.length; c++) {
            roots = split[c];
            boolean recursive = c == 1;
            if (recursive) {
                File[] files = cache.listFiles(ctx, FileInformation.STATUS_LOCAL_CHANGE);
                for (int i= 0; i < files.length; i++) {
                    for(int r = 0; r < roots.length; r++) {
                        if( SvnUtils.isParentOrEqual(roots[r], files[i]) ) {
                            if(!fileList.contains(files[i])) {
                                fileList.add(files[i]);
                            }
                        }                    
                    }                    
                }
            } else {
                File[] files = SvnUtils.flatten(roots, FileInformation.STATUS_LOCAL_CHANGE);
                for (int i= 0; i<files.length; i++) {
                    if(!fileList.contains(files[i])) {
                        fileList.add(files[i]);
                    }
                }                
            }
        }       

        if(fileList.size()==0) {
            return; 
        }
        
        // show commit dialog
        final CommitPanel panel = new CommitPanel();
        final CommitTable data = new CommitTable(panel.filesLabel, CommitTable.COMMIT_COLUMNS);
        panel.setCommitTable(data);
        SvnFileNode[] nodes;
        ArrayList<SvnFileNode> nodesList = new ArrayList<SvnFileNode>(fileList.size());

        for (Iterator<File> it = fileList.iterator(); it.hasNext();) {
            File file = it.next();
            SvnFileNode node = new SvnFileNode(file);
            nodesList.add(node);
        }        
        nodes = nodesList.toArray(new SvnFileNode[fileList.size()]);
        data.setNodes(nodes);

        JComponent component = data.getComponent();
        panel.filesPanel.setLayout(new BorderLayout());
        panel.filesPanel.add(component, BorderLayout.CENTER);

        DialogDescriptor dd = new DialogDescriptor(panel, org.openide.util.NbBundle.getMessage(CommitAction.class, "CTL_CommitDialog_Title", contentTitle)); // NOI18N
        dd.setModal(true);
        final JButton commitButton = new JButton(org.openide.util.NbBundle.getMessage(CommitAction.class, "CTL_Commit_Action_Commit")); // NOI18N
        dd.setOptions(new Object[] {commitButton, org.openide.util.NbBundle.getMessage(CommitAction.class, "CTL_Commit_Action_Cancel")}); // NOI18N
        dd.setHelpCtx(new HelpCtx(CommitAction.class));
        panel.addVersioningListener(new VersioningListener() {
            public void versioningEvent(VersioningEvent event) {
                refreshCommitDialog(panel, data, commitButton);
            }
        });

        panel.putClientProperty("contentTitle", contentTitle);  // NOI18N
        panel.putClientProperty("DialogDescriptor", dd); // NOI18N
        Dialog dialog = DialogDisplayer.getDefault().createDialog(dd);
        dialog.pack();
        dialog.setVisible(true);

        if (dd.getValue() == commitButton) {

            final Map<SvnFileNode, CommitOptions> commitFiles = data.getCommitFiles();
            final String message = panel.messageTextArea.getText();

            SVNUrl repository = getSvnUrl(ctx);
            RequestProcessor rp = Subversion.getInstance().getRequestProcessor(repository);
            SvnProgressSupport support = new SvnProgressSupport() {
                public void perform() {                    
                    performCommit(message, commitFiles, ctx, this);
                }
            };
            support.start(rp, repository, org.openide.util.NbBundle.getMessage(CommitAction.class, "LBL_Commit_Progress")); // NOI18N
        }

        // if OK setup sequence of add, remove and commit calls
        
    }
    
    /**
     * User changed a commit action.
     * 
     * @param panel
     * @param commit
     */ 
    private static void refreshCommitDialog(CommitPanel panel, CommitTable table, JButton commit) {
        ResourceBundle loc = NbBundle.getBundle(CommitAction.class);
        Map<SvnFileNode, CommitOptions> files = table.getCommitFiles();
        Set stickyTags = new HashSet();
        boolean conflicts = false;
        
        for (SvnFileNode fileNode : files.keySet()) {
            CommitOptions options = files.get(fileNode);
            if (options == CommitOptions.EXCLUDE) continue;
            stickyTags.add(SvnUtils.getBranch(fileNode.getFile()));
            int status = fileNode.getInformation().getStatus();
            if ((status & FileInformation.STATUS_REMOTE_CHANGE) != 0 || status == FileInformation.STATUS_VERSIONED_CONFLICT) {
                commit.setEnabled(false);
                String msg = (status == FileInformation.STATUS_VERSIONED_CONFLICT) ? 
                        loc.getString("MSG_CommitForm_ErrorConflicts") :
                        loc.getString("MSG_CommitForm_ErrorRemoteChanges");
                panel.setErrorLabel("<html><font color=\"#002080\">" + msg + "</font></html>");  // NOI18N
                conflicts = true;
            }
            stickyTags.add(SvnUtils.getBranch(fileNode.getFile()));
        }
        
        if (stickyTags.size() > 1) {
            table.setColumns(new String [] { CommitTableModel.COLUMN_NAME_NAME, CommitTableModel.COLUMN_NAME_BRANCH, CommitTableModel.COLUMN_NAME_STATUS, 
                                                CommitTableModel.COLUMN_NAME_ACTION, CommitTableModel.COLUMN_NAME_PATH });
        } else {
            table.setColumns(new String [] { CommitTableModel.COLUMN_NAME_NAME, CommitTableModel.COLUMN_NAME_STATUS, 
                                                CommitTableModel.COLUMN_NAME_ACTION, CommitTableModel.COLUMN_NAME_PATH });
        }
        
        String contentTitle = (String) panel.getClientProperty("contentTitle"); // NOI18N
        DialogDescriptor dd = (DialogDescriptor) panel.getClientProperty("DialogDescriptor"); // NOI18N
        String errorLabel;
        if (stickyTags.size() <= 1) {
            String stickyTag = stickyTags.size() == 0 ? null : (String) stickyTags.iterator().next(); 
            if (stickyTag == null) {
                dd.setTitle(MessageFormat.format(loc.getString("CTL_CommitDialog_Title"), new Object [] { contentTitle }));
                errorLabel = ""; // NOI18N
            } else {
                dd.setTitle(MessageFormat.format(loc.getString("CTL_CommitDialog_Title_Branch"), new Object [] { contentTitle, stickyTag }));
                String msg = MessageFormat.format(loc.getString("MSG_CommitForm_InfoBranch"), new Object [] { stickyTag });
                errorLabel = "<html><font color=\"#002080\">" + msg + "</font></html>"; // NOI18N
            }
        } else {
            dd.setTitle(MessageFormat.format(loc.getString("CTL_CommitDialog_Title_Branches"), new Object [] { contentTitle }));
            String msg = loc.getString("MSG_CommitForm_ErrorMultipleBranches");
            errorLabel = "<html><font color=\"#CC0000\">" + msg + "</font></html>"; // NOI18N
        }
        if (!conflicts) {
            panel.setErrorLabel(errorLabel);
            commit.setEnabled(true);
        }
    }
    
    protected void performContextAction(Node[] nodes) {
        final Context ctx = getContext(nodes);
        commit(getContextDisplayName(nodes), ctx);
    }

    public static void performCommit(String message, Map<SvnFileNode, CommitOptions> commitFiles, Context ctx, SvnProgressSupport support) {
        try {
                                               
            SvnClient client;
            try {
                client = Subversion.getInstance().getClient(ctx, support);
            } catch (SVNClientException ex) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex); // should not hapen
                return;
            }                   
            support.setDisplayName(org.openide.util.NbBundle.getMessage(CommitAction.class, "LBL_Commit_Progress")); // NOI18N

            List<SvnFileNode> addCandidates = new ArrayList<SvnFileNode>();
            List<SvnFileNode> removeCandidates = new ArrayList<SvnFileNode>();
            Set<File> commitCandidates = new LinkedHashSet<File>();
            Set<File> binnaryCandidates = new HashSet<File>();
            
            Iterator<SvnFileNode> it = commitFiles.keySet().iterator();
            // XXX refactor the olowing loop. there seem to be redundant blocks
            while (it.hasNext()) {
                if(support.isCanceled()) {
                    return;
                }
                SvnFileNode node = it.next();
                CommitOptions option = commitFiles.get(node);
                if (CommitOptions.ADD_BINARY == option) {
                    List<File> l = listUnmanagedParents(node);
                    Iterator<File> dit = l.iterator();
                    while (dit.hasNext()) {
                        if(support.isCanceled()) {
                            return;
                        }
                        File file = dit.next();
                        addCandidates.add(new SvnFileNode(file));
                        commitCandidates.add(file);
                    }

                    if(support.isCanceled()) {
                        return;
                    }
                    binnaryCandidates.add(node.getFile());                                     

                    addCandidates.add(node);
                    commitCandidates.add(node.getFile());
                } else if (CommitOptions.ADD_TEXT == option || CommitOptions.ADD_DIRECTORY == option) {
                    // assute no MIME property or startin gwith text
                    List<File> l = listUnmanagedParents(node);
                    Iterator<File> dit = l.iterator();
                    while (dit.hasNext()) {
                        if(support.isCanceled()) {
                            return;
                        }
                        File file = dit.next();
                        addCandidates.add(new SvnFileNode(file));
                        commitCandidates.add(file);
                    }
                    if(support.isCanceled()) {
                        return;
                    }
                    addCandidates.add(node);
                    commitCandidates.add(node.getFile());
                } else if (CommitOptions.COMMIT_REMOVE == option) {
                    removeCandidates.add(node);
                    commitCandidates.add(node.getFile());
                } else if (CommitOptions.COMMIT == option) {
                    commitCandidates.add(node.getFile());
                }
            }

            // perform adds

            List<File> addFiles = new ArrayList<File>();
            List<File> addDirs = new ArrayList<File>();
            // XXX waht if user denied directory add but wants to add a file in it?
            it = addCandidates.iterator();
            while (it.hasNext()) {
                if(support.isCanceled()) {
                    return;
                }
                SvnFileNode svnFileNode = it.next();
                File file = svnFileNode.getFile();
                if (file.isDirectory()) {
                    addDirs.add(file);
                } else if (file.isFile()) {
                    addFiles.add(file);
                }
            }
            if(support.isCanceled()) {
                return;
            }

            Iterator<File> itFiles = addDirs.iterator();
            List<File> dirsToAdd = new ArrayList<File>();
            while (itFiles.hasNext()) {
                File dir = itFiles.next();
                if (!dirsToAdd.contains(dir)) {
                    dirsToAdd.add(dir);
                }
            }
            if(dirsToAdd.size() > 0) {
                client.addFile(dirsToAdd.toArray(new File[dirsToAdd.size()]), false);
            }
            if(support.isCanceled()) {
                return;
            }

            if(addFiles.size() > 0) {
                client.addFile(addFiles.toArray(new File[addFiles.size()]), false);       
            }
            
            // TODO perform removes. especialy package removes where
            // metadata must be replied from SvnMetadata (hold by FileSyatemHandler)

            // set binary mimetype and group commitCandidates by managed trees
            FileStatusCache cache = Subversion.getInstance().getStatusCache();
            List<List<File>> managedTrees = new ArrayList<List<File>>();
            for (Iterator<File> itCommitCandidates = commitCandidates.iterator(); itCommitCandidates.hasNext();) {
                File commitCandidateFile = itCommitCandidates.next();
                
                // set MIME property application/octet-stream
                if(binnaryCandidates.contains(commitCandidateFile)) {
                    ISVNProperty prop = client.propertyGet(commitCandidateFile, ISVNProperty.MIME_TYPE);
                    if(prop != null) {
                        String s = prop.getValue();
                        if (s == null || s.startsWith("text/")) { // NOI18N
                            client.propertySet(commitCandidateFile, ISVNProperty.MIME_TYPE, "application/octet-stream", false); // NOI18N
                        }    
                    } else {
                         client.propertySet(commitCandidateFile, ISVNProperty.MIME_TYPE, "application/octet-stream", false); // NOI18N
                    }   
                }
                
                List<File> managedTreesList = null;
                for (Iterator<List<File>> itManagedTrees = managedTrees.iterator(); itManagedTrees.hasNext();) {
                    List<File> list = itManagedTrees.next();
                    File managedTreeFile = list.get(0);

                    File base = SVNBaseDir.getRootDir(new File[] {commitCandidateFile, managedTreeFile});
                    if(base != null) {
                        FileInformation status = cache.getStatus(base);
                        if ((status.getStatus() & FileInformation.STATUS_MANAGED) != 0) {
                            // found a list with files from the same working copy
                            managedTreesList = list;
                            break;
                        }
                    }
                }
                if(managedTreesList == null) {
                    // no list for files from the same wc as commitCandidateFile created yet
                    managedTreesList = new ArrayList<File>();
                    managedTrees.add(managedTreesList);
                }                
                managedTreesList.add(commitCandidateFile);                
            }

            // finally commit            
            for (Iterator<List<File>> itCandidates = managedTrees.iterator(); itCandidates.hasNext();) {
                // one commit for each wc
                List<File> list = itCandidates.next();
                File[] files = list.toArray(new File[0]);
                
                client.commit(files, message, false);
                // XXX it's probably already catched by cache's onNotify()
                for (int i = 0; i < files.length; i++) {
                    cache.refresh(files[i], FileStatusCache.REPOSITORY_STATUS_UNKNOWN);
                }
                if(support.isCanceled()) {
                    return;
                }
            }                        

        } catch (SVNClientException ex) {
            support.annotate(ex);
        } 
    }

    private static List<File> listUnmanagedParents(SvnFileNode node) {
        List<File> unmanaged = new ArrayList<File>();
        File file = node.getFile();
        File parent = file.getParentFile();
        while (true) {
            if (new File(parent, ".svn/entries").canRead() || new File(parent, "_svn/entries").canRead()) { // NOI18N
                break;
            }
            unmanaged.add(0, parent);
            parent = parent.getParentFile();
            if (parent == null) {
                break;
            }
        }

        List<File> ret = new ArrayList<File>();
        Iterator<File> it = unmanaged.iterator();
        while (it.hasNext()) {
            File un = it.next();
            ret.add(un);
        }

        return ret;
    }
}
