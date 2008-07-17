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
package org.netbeans.modules.subversion.ui.browser;

import java.awt.Dialog;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import javax.swing.Action;
import javax.swing.JPanel;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import org.netbeans.modules.subversion.RepositoryFile;
import org.netbeans.modules.subversion.Subversion;
import org.netbeans.modules.subversion.client.SvnClient;
import org.netbeans.modules.subversion.client.SvnClientExceptionHandler;
import org.netbeans.modules.subversion.client.SvnProgressSupport;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.Visualizer;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.tigris.subversion.svnclientadapter.ISVNDirEntry;
import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.SVNNodeKind;

/**
 * Handles the UI for repository browsing.
 *
 * @author Tomas Stupka
 */
public class Browser implements VetoableChangeListener, BrowserClient, TreeExpansionListener {        
    
    public final static int BROWSER_SHOW_FILES                  = 1;
    public final static int BROWSER_SINGLE_SELECTION_ONLY       = 2;
    public final static int BROWSER_FILES_SELECTION_ONLY        = 4;
    public final static int BROWSER_FOLDERS_SELECTION_ONLY      = 8;
    public final static int BROWSER_SELECT_ANYTHING = BROWSER_FOLDERS_SELECTION_ONLY | BROWSER_FILES_SELECTION_ONLY;

    public final static String BROWSER_HELP_ID_SEARCH_HISTORY   = "org.netbeans.modules.subversion.ui.browser.searchhistory";
    public final static String BROWSER_HELP_ID_CHECKOUT         = "org.netbeans.modules.subversion.ui.browser.checkout";
    public final static String BROWSER_HELP_ID_URL_PATTERN      = "org.netbeans.modules.subversion.ui.browser.urlpattern";
    public final static String BROWSER_HELP_ID_MERGE            = "org.netbeans.modules.subversion.ui.browser.merge";
    public final static String BROWSER_HELP_ID_SWITCH_TO        = "org.netbeans.modules.subversion.ui.browser.switchto";
    public final static String BROWSER_HELP_ID_COPY             = "org.netbeans.modules.subversion.ui.browser.copy";
    public final static String BROWSER_HELP_ID_IMPORT           = "org.netbeans.modules.subversion.ui.browser.import";
    
    private final int mode;
    
    private final String helpID;
    
    private static final RepositoryFile[] EMPTY_ROOT = new RepositoryFile[0];
    private static final Action[] EMPTY_ACTIONS = new Action[0];
    
    private final BrowserPanel panel;    
            
    private RepositoryFile repositoryRoot;            
    private Action[] nodeActions;

    private boolean keepWarning = false;
    private boolean initialSelection = true;
    
    private List<SvnProgressSupport> supportList = new ArrayList<SvnProgressSupport>();
    private volatile boolean cancelled = false;
    /**
     * Creates a new instance
     *
     * @param title the browsers window title
     * @param showFiles 
     * @param singleSelectionOnly
     * @param fileSelectionOnly
     * @param repositoryRoot the RepositoryFile representing the repository root
     * @param select an array of RepositoryFile-s representing the items which has to be selected
     * @param nodeActions an array of actions from which the context menu on the tree items will be created
     * 
     */    
    public Browser(String title, int mode, RepositoryFile repositoryRoot, RepositoryFile[] select, BrowserAction[] nodeActions, String helpID) {
        this.mode = mode;       
        this.helpID = helpID;
        
        panel = new BrowserPanel(title,           
                                 org.openide.util.NbBundle.getMessage(Browser.class, "ACSN_RepositoryTree"),                                            // NOI18N
                                 org.openide.util.NbBundle.getMessage(Browser.class, "ACSD_RepositoryTree"),                                            // NOI18N
                                 (mode & BROWSER_SINGLE_SELECTION_ONLY) == BROWSER_SINGLE_SELECTION_ONLY);
        
        panel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RepositoryPathNode.class, "CTL_Browser_Prompt"));    // NOI18N
        panel.addTreeExpansionListener(this);
        getExplorerManager().addVetoableChangeListener(this);                        
                
        if(nodeActions!=null) {
            this.nodeActions = nodeActions;
            panel.setActions(nodeActions);    
            for (int i = 0; i < nodeActions.length; i++) {
                nodeActions[i].setBrowser(this);
            }
        } else {
            this.nodeActions = EMPTY_ACTIONS;
        }        
        this.repositoryRoot = repositoryRoot;
        
        RepositoryPathNode rootNode = RepositoryPathNode.createRepositoryRootNode(this, repositoryRoot);                        
        rootNode.expand();
        
        Node[] selectedNodes = getSelectedNodes(rootNode, repositoryRoot, select);   
        getExplorerManager().setRootContext(rootNode);
        
        if(selectedNodes == null) {
            selectedNodes = new Node[] {};
        }
        
        try {        
            getExplorerManager().setSelectedNodes(selectedNodes);    
            for (int i = 0; i < selectedNodes.length; i++) {
                getExplorerManager().setExploredContext(selectedNodes[i]);                           
            }   
            if(selectedNodes.length > 0) {
                ((RepositoryPathNode) selectedNodes[selectedNodes.length - 1]).expand();
            } else {
                rootNode.expand();        
            }
        } catch (PropertyVetoException ex) {
            Subversion.LOG.log(Level.INFO, null, ex);
        }                  
                
    }       

    public RepositoryFile[] getRepositoryFiles() {
        if(!show()) {
            cancel();  
            return EMPTY_ROOT;
        }
        
        // get the nodes first
        Node[] nodes = getExplorerManager().getSelectedNodes();
        
        // clean up - even if the dialog was closed, we always cancel all running tasks
        cancel();  
        
        if(nodes.length == 0) {
            return EMPTY_ROOT;
        }
        
        RepositoryFile[] ret = new RepositoryFile[nodes.length];
        for (int i = 0; i < nodes.length; i++) {
            ret[i] = ((RepositoryPathNode) nodes[i]).getEntry().getRepositoryFile();
        }                        
        return ret;
    }    
    
    private boolean show() {
        final DialogDescriptor dialogDescriptor = 
                new DialogDescriptor(getBrowserPanel(), NbBundle.getMessage(Browser.class, "CTL_Browser_BrowseFolders_Title")); 
        dialogDescriptor.setModal(true);
        dialogDescriptor.setHelpCtx(new HelpCtx(helpID));
        dialogDescriptor.setValid(false);
        
        addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if( ExplorerManager.PROP_SELECTED_NODES.equals(evt.getPropertyName()) ) {
                    dialogDescriptor.setValid(getSelectedNodes().length > 0);
                }
            }
        });        
        
        Dialog dialog = DialogDisplayer.getDefault().createDialog(dialogDescriptor);     
        dialog.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(Browser.class, "CTL_Browser_BrowseFolders_Title"));
        dialog.setVisible(true);                

        return DialogDescriptor.OK_OPTION.equals(dialogDescriptor.getValue());
    }
    
    private Node[] getSelectedNodes(RepositoryPathNode rootNode, RepositoryFile repositoryRoot, RepositoryFile[] select) {        
        if(select==null || select.length <= 0) {
            return null;
        }
        Node segmentParentNode = null;        
        List<Node> nodesToSelect = new ArrayList<Node>(select.length);

        for (int i = 0; i < select.length; i++) {                
            String[] segments = select[i].getPathSegments();                
            segmentParentNode = rootNode;
            RepositoryFile segmentFile = repositoryRoot;
            for (int j = 0; j < segments.length; j++) {
                segmentFile = segmentFile.appendPath(segments[j]);                                                                
                RepositoryPathNode segmentNode = j == segments.length - 1 ?  
                    RepositoryPathNode.createRepositoryPathNode(this, segmentFile) : 
                    RepositoryPathNode.createPreselectedPathNode(this, segmentFile);    
                segmentParentNode.getChildren().add(new Node[] {segmentNode});                
                segmentParentNode = segmentNode;
            }   
            nodesToSelect.add(segmentParentNode);                    
        }                
        return nodesToSelect.toArray(new Node[nodesToSelect.size()]);                        
    }

    /**
     * Cancels all running tasks
     */
    private void cancel() {                   
        SvnProgressSupport[] progressSupports = null;    
        synchronized(supportList) {            
            cancelled = true; 
            progressSupports = supportList.toArray(new SvnProgressSupport[supportList.size()]);
            supportList.clear();
        }

        Node rootNode = getExplorerManager().getRootContext();
        if(rootNode != null) {
            getExplorerManager().setRootContext(Node.EMPTY);
            try {                                
                rootNode.destroy();
                
                if(progressSupports != null && progressSupports.length > 0) {
                    for(SvnProgressSupport sps : progressSupports) {
                        sps.cancel();   
                    }                    
                }
            } catch (IOException ex) {
                Subversion.LOG.log(Level.INFO, null, ex); // should not happen
            }            
        }
    }
            
    public List<RepositoryPathNode.RepositoryPathEntry> listRepositoryPath(final RepositoryPathNode.RepositoryPathEntry entry, SvnProgressSupport support) throws SVNClientException {

        List<RepositoryPathNode.RepositoryPathEntry> ret = new ArrayList<RepositoryPathNode.RepositoryPathEntry>();
        
        synchronized (supportList) {
            if(cancelled) {
                support.cancel();
                return ret;
            }            
            supportList.add(support);                                
        }        
        
        try {            
            
            if(entry.getSvnNodeKind().equals(SVNNodeKind.FILE)) {
                return ret; // nothing to do...
            }

            SvnClient client = Subversion.getInstance().getClient(this.repositoryRoot.getRepositoryUrl(), support);
            if(support.isCanceled()) {
                return null;
            }
            
            ISVNDirEntry[] dirEntries = client.getList(
                                            entry.getRepositoryFile().getFileUrl(),
                                            entry.getRepositoryFile().getRevision(),
                                            false
                                        );             

            if(dirEntries == null || dirEntries.length == 0) {
                return ret; // nothing to do...
            }
                        
            for (int i = 0; i < dirEntries.length; i++) {
                if(support.isCanceled()) {
                    return null;
                }

                ISVNDirEntry dirEntry = dirEntries[i];                
                if( dirEntry.getNodeKind()==SVNNodeKind.DIR ||                  // directory or
                    (dirEntry.getNodeKind()==SVNNodeKind.FILE &&                // (file and show_files_allowed) 
                     ((mode & BROWSER_SHOW_FILES) == BROWSER_SHOW_FILES)) ) 
                {
                    RepositoryFile repositoryFile = new RepositoryFile(
                                                            entry.getRepositoryFile().getRepositoryUrl(), 
                                                            entry.getRepositoryFile().getFileUrl().appendPath(dirEntry.getPath()), 
                                                            dirEntry.getLastChangedRevision());
                    RepositoryPathNode.RepositoryPathEntry e = 
                            new RepositoryPathNode.RepositoryPathEntry(
                            repositoryFile, 
                            dirEntry.getNodeKind(),
                            dirEntry.getLastChangedRevision(),
                            dirEntry.getLastChangedDate(),
                            dirEntry.getLastCommitAuthor());                    
                    ret.add(e);   
                }                
            }        
            
        } catch (SVNClientException ex) {
            if(SvnClientExceptionHandler.isWrongURLInRevision(ex.getMessage())) {
                // is not a folder in the repository
                return null;
            } else {
                support.annotate(ex);
                throw ex;
            }                
        }
        finally {
            synchronized (supportList) {
                supportList.remove(support);
            }            
        }

        return ret;
    }
    
    private JPanel getBrowserPanel() {
        return panel;
    }
    
    public Node[] getSelectedNodes() {
        return getExplorerManager().getSelectedNodes();
    }
    
    public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {
        if (ExplorerManager.PROP_SELECTED_NODES.equals(evt.getPropertyName())) {
           
            boolean initialSelectionDone = !initialSelection;
            initialSelection = false;
            
            if(!keepWarning) {
                panel.warning(null);                
            }
            keepWarning = false;    
            
            Node[] newSelection = (Node[]) evt.getNewValue();
            Node[] oldSelection = (Node[]) evt.getOldValue();                                    
            if(newSelection == null || newSelection.length == 0) {                
                return;
            }
           
            // applies if file selection only
            if((mode & BROWSER_FILES_SELECTION_ONLY) == BROWSER_FILES_SELECTION_ONLY) {          
                if(checkForNodeType(newSelection, SVNNodeKind.DIR))  {
                    panel.warning(org.openide.util.NbBundle.getMessage(Browser.class, "LBL_Warning_FileSelectionOnly"));        // NOI18N
                    if(initialSelectionDone) keepWarning = true;
                    throw new PropertyVetoException("", evt);                                                                   // NOI18N                
                }       
            }
            
            // applies if folder selection only                
            if((mode & BROWSER_FOLDERS_SELECTION_ONLY) == BROWSER_FOLDERS_SELECTION_ONLY) {     
                if(checkForNodeType(newSelection, SVNNodeKind.FILE)) {                         
                    panel.warning(org.openide.util.NbBundle.getMessage(Browser.class, "LBL_Warning_FolderSelectionOnly"));      // NOI18N
                    if(initialSelectionDone) keepWarning = true;
                    throw new PropertyVetoException("", evt);                                                                   // NOI18N
                }                    
            }            
            
            // RULE: don't select nodes on a different level as the already selected 
            if(oldSelection.length == 0 && newSelection.length == 1) {
                // it is first node selected ->
                // -> there is nothig to check                       
                return;
            }   
                                    
            if(oldSelection.length != 0 && areDisjunct(oldSelection, newSelection)) {
                // as if the first node would be selected ->
                // -> there is nothig to check
                return;
            }

            Node selectedNode = null;
            if(oldSelection.length > 0) {
                // we anticipate that nothing went wrong and
                // all nodes in the old selection are at the same level
                selectedNode = oldSelection[0];
            } else {
                selectedNode = newSelection[0];
            }
            if(!selectionIsAtLevel(newSelection, getNodeLevel(selectedNode))) {
                panel.warning(org.openide.util.NbBundle.getMessage(Browser.class, "LBL_Warning_NoMultiSelection"));     // NOI18N
                if(initialSelectionDone) keepWarning = true;
                throw new PropertyVetoException("", evt);                                                               // NOI18N
            }                 
        }
    }    
    
    private boolean checkForNodeType(Node[] newSelection, SVNNodeKind nodeKind) {
        for (int i = 0; i < newSelection.length; i++) {
            if(newSelection[i] instanceof RepositoryPathNode) {
                RepositoryPathNode node = (RepositoryPathNode) newSelection[i];
                if(node.getEntry().getSvnNodeKind() == nodeKind) {                            
                    return true;
                }
            }
        }
        return false;
    }
   
    
    private boolean selectionIsAtLevel(Node[] newSelection, int level) {
        for (int i = 0; i < newSelection.length; i++) {
             if (getNodeLevel(newSelection[i]) != level)  {
                return false;
             }
        }        
        return true;
    }
    
    private boolean areDisjunct(Node[] oldSelection, Node[] newSelection) { 
        for (int i = 0; i < oldSelection.length; i++) {
            if(isInArray(oldSelection[i], newSelection)) {                
                return false;
            }
        }
        return true;
    }
    
    private int getNodeLevel(Node node) {
        int level = 0;
        while(node!=null) {
            node = node.getParentNode();
            level++;
        }
        return level;
    }
    
    private boolean isInArray(Node node, Node[] nodeArray) {
        for (int i = 0; i < nodeArray.length; i++) {
            if(node==nodeArray[i]) {
                return true;
            }
        }
        return false;
    }    
    
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        getExplorerManager().addPropertyChangeListener(listener);
    }
    
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        getExplorerManager().removePropertyChangeListener(listener);
    }
    
    ExplorerManager getExplorerManager() {        
        return panel.getExplorerManager();
    }

    public Action[] getActions() {
        return nodeActions;        
    }

    void setSelectedNodes(Node[] selection) throws PropertyVetoException {
        getExplorerManager().setSelectedNodes(selection);
    }

    public void treeExpanded(TreeExpansionEvent event) {
        Object obj = event.getPath().getLastPathComponent();                                
        if(obj == null) return;
        Node n = Visualizer.findNode(obj);
        if(n instanceof RepositoryPathNode) {
            RepositoryPathNode node = (RepositoryPathNode) n;
            node.expand();
        }
    }

    public void treeCollapsed(TreeExpansionEvent event) {
        // do nothing
    }

}
