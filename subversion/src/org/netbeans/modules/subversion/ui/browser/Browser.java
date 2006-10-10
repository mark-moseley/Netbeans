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
package org.netbeans.modules.subversion.ui.browser;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.Action;
import javax.swing.JPanel;
import org.netbeans.modules.subversion.RepositoryFile;
import org.netbeans.modules.subversion.Subversion;
import org.netbeans.modules.subversion.client.ExceptionHandler;
import org.netbeans.modules.subversion.client.SvnClient;
import org.netbeans.modules.subversion.client.SvnProgressSupport;
import org.openide.ErrorManager;
import org.openide.explorer.ExplorerManager;
import org.openide.nodes.Node;
import org.tigris.subversion.svnclientadapter.ISVNDirEntry;
import org.tigris.subversion.svnclientadapter.ISVNLogMessage;
import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.SVNNodeKind;
import org.tigris.subversion.svnclientadapter.SVNRevision;
import org.tigris.subversion.svnclientadapter.SVNUrl;

/**
 * Handles the UI for repository browsing.
 *
 * @author Tomas Stupka
 */
public class Browser implements VetoableChangeListener, BrowserClient {
        
    private final BrowserPanel panel;    
    
    private static final RepositoryFile[] EMPTY_ROOT = new RepositoryFile[0];
    private static final Action[] EMPTY_ACTIONS = new Action[0];
    
    private final boolean showFiles;    
    
    private RepositoryFile repositoryRoot;        
    
    private Action[] nodeActions;

    private SvnProgressSupport support;

    private boolean fileSelectionOnly; 

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
    public Browser(String title, boolean showFiles, boolean singleSelectionOnly, boolean fileSelectionOnly,
                   RepositoryFile repositoryRoot, RepositoryFile[] select, BrowserAction[] nodeActions) {
        this.showFiles = showFiles;
        this.fileSelectionOnly = fileSelectionOnly;

        panel = new BrowserPanel(title,           
                                 org.openide.util.NbBundle.getMessage(RepositoryPathNode.class, "ACSN_RepositoryTree"),         // NOI18N
                                 org.openide.util.NbBundle.getMessage(RepositoryPathNode.class, "ACSD_RepositoryTree"),         // NOI18N
                                 singleSelectionOnly);
        panel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RepositoryPathNode.class, "CTL_Browser_Prompt"));
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
        
        RepositoryPathNode rootNode = RepositoryPathNode.createRepositoryPathNode(this, repositoryRoot);                        
        Node[] selectedNodes = getSelectedNodes(rootNode, repositoryRoot, select);   
        getExplorerManager().setRootContext(rootNode);
        
        if(selectedNodes!=null) {
            try {
                getExplorerManager().setSelectedNodes(selectedNodes);    
            } catch (PropertyVetoException ex) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            }    
        }
        
    }       

    /**
     * Configures the browser instance with the given parameters
     *
     * @param repositoryRoot the RepositoryFile representing the repository root
     * @param select an array of RepositoryFile-s representing the items which has to be selected
     * @param nodeActions an array of actions from which the context menu on the tree items will be created
     */
//    public void setup(RepositoryFile repositoryRoot, RepositoryFile[] select, BrowserAction[] nodeActions) 
//    {        
//        if(nodeActions!=null) {
//            this.nodeActions = nodeActions;
//            panel.setActions(nodeActions);    
//            for (int i = 0; i < nodeActions.length; i++) {
//                nodeActions[i].setBrowser(this);
//            }
//        } else {
//            this.nodeActions = EMPTY_ACTIONS;
//        }        
//        this.repositoryRoot = repositoryRoot;
//        
//        RepositoryPathNode rootNode = RepositoryPathNode.createRepositoryPathNode(this, repositoryRoot);                        
//        Node[] selectedNodes = getSelectedNodes(rootNode, repositoryRoot, select);   
//        getExplorerManager().setRootContext(rootNode);
//        
//        if(selectedNodes!=null) {
//            try {
//                getExplorerManager().setSelectedNodes(selectedNodes);    
//            } catch (PropertyVetoException ex) {
//                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
//            }    
//        }
//    }

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
                RepositoryPathNode segmentNode = RepositoryPathNode.createRepositoryPathNode(this, segmentFile);
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
    public void cancel() {
        Node rootNode = getExplorerManager().getRootContext();
        if(rootNode != null) {
            getExplorerManager().setRootContext(Node.EMPTY);
            try {                                
                rootNode.destroy();
                if(support != null) {
                    support.cancel();
                }
            } catch (IOException ex) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex); // should not happen
            }            
        }
    }
    
    public List listRepositoryPath(final RepositoryPathNode.RepositoryPathEntry entry, SvnProgressSupport support) throws SVNClientException {
        List<RepositoryPathNode.RepositoryPathEntry> ret;
        try {

            this.support = support;

            if(entry.getSvnNodeKind().equals(SVNNodeKind.FILE)) {
                return Collections.EMPTY_LIST; // nothing to do...
            }

            SvnClient client = Subversion.getInstance().getClient(this.repositoryRoot.getRepositoryUrl(), support);
            if(support.isCanceled()) {
                return null;
            }

            ret = new ArrayList<RepositoryPathNode.RepositoryPathEntry>();

            ISVNDirEntry[] dirEntries = client.getList(
                                            entry.getRepositoryFile().getFileUrl(),
                                            entry.getRepositoryFile().getRevision(),
                                            false
                                        );             

            if(dirEntries == null || dirEntries.length == 0) {
                return Collections.EMPTY_LIST; // nothing to do...
            }
                        
            for (int i = 0; i < dirEntries.length; i++) {
                if(support.isCanceled()) {
                    return null;
                }

                ISVNDirEntry dirEntry = dirEntries[i];                
                if( dirEntry.getNodeKind()==SVNNodeKind.DIR || 
                    (dirEntry.getNodeKind()==SVNNodeKind.FILE && showFiles) ) 
                {
                    RepositoryFile repositoryFile = entry.getRepositoryFile();
                    RepositoryPathNode.RepositoryPathEntry e = 
                            new RepositoryPathNode.RepositoryPathEntry(
                            repositoryFile.appendPath(dirEntry.getPath()), 
                            dirEntry.getNodeKind(),
                            dirEntry.getLastChangedRevision(),
                            dirEntry.getLastChangedDate(),
                            dirEntry.getLastCommitAuthor());                    
                    ret.add(e);   
                }                
            }        
            
        } catch (SVNClientException ex) {
            if(ExceptionHandler.isWrongURLInRevision(ex.getMessage())) {
                // is not a folder in the repository
                return null;
            } else {
                support.annotate(ex);                
                throw ex;
            }                
        }
        finally {
            this.support = null;
        }

        return ret;
    }
    
    public JPanel getBrowserPanel() {
        return panel;
    }
    
    public Node[] getSelectedNodes() {
        return getExplorerManager().getSelectedNodes();
    }

    public RepositoryFile[] getSelectedFiles() {
        Node[] nodes = (Node[]) getExplorerManager().getSelectedNodes();
        
        if(nodes.length == 0) {
            return EMPTY_ROOT;
        }
        
        RepositoryFile[] ret = new RepositoryFile[nodes.length];
        for (int i = 0; i < nodes.length; i++) {
            ret[i] = ((RepositoryPathNode) nodes[i]).getEntry().getRepositoryFile();
        }
        return ret;
    }
    
    public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {
        if (ExplorerManager.PROP_SELECTED_NODES.equals(evt.getPropertyName())) {

            Node[] newSelection = (Node[]) evt.getNewValue();
            if(newSelection == null || newSelection.length == 0) {
                return;
            }

            // RULE: don't select the repository node
//            if(containsRootNode(newSelection)) {                
//                throw new PropertyVetoException("", evt); // NOI18N
//            }
            
            Node[] oldSelection = (Node[]) evt.getOldValue();                                    

            if(fileSelectionOnly) {
                for (int i = 0; i < newSelection.length; i++) {
                    if(newSelection[i] instanceof RepositoryPathNode) {
                        RepositoryPathNode node = (RepositoryPathNode) newSelection[i];
                        if(node.getEntry().getSvnNodeKind() == SVNNodeKind.DIR) {
                            throw new PropertyVetoException("", evt); // NOI18N
                        }
                    }
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
                throw new PropertyVetoException("", evt); // NOI18N
            }
    
        }
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
}
