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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.search;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;
import org.openide.windows.TopComponent;
import org.openidex.search.FileObjectFilter;
import org.openidex.search.SearchInfo;
import org.openidex.search.SearchInfoFactory;
import static org.openide.windows.TopComponent.Registry.PROP_CURRENT_NODES;

/**
 * Defines search scope across selected nodes.
 *
 * @author  Marian Petras
 */
final class SearchScopeNodeSelection extends AbstractSearchScope
                                     implements PropertyChangeListener {
    
    private PropertyChangeListener currentNodesWeakListener;

    public SearchScopeNodeSelection() {
        super();
    }

    public String getDisplayName() {
        return NbBundle.getMessage(getClass(),
                                   "SearchScopeNameSelectedNodes");     //NOI18N
    }

    @Override
    protected boolean checkIsApplicable() {
        return checkIsApplicable(TopComponent.getRegistry().getCurrentNodes());
    }

    /**
     * Decides whether searching should be enabled with respect to a set
     * of selected nodes.
     * Searching is enabled if searching instructions
     * (<code>SearchInfo</code> object) are available for all selected nodes
     * and at least one registered search type is able to search all the
     * selected nodes.
     *
     * @param  nodes  selected nodes
     * @return  <code>true</code> if searching the selected nodes should be
     *          enabled; <code>false</code> otherwise
     * @see  SearchInfo
     * @see  SearchType
     */
    private static boolean checkIsApplicable(Node[] nodes) {
        if (nodes == null || nodes.length == 0) {
            return false;
        }

        for (Node node : nodes) {
            if (!canSearch(node)) {
                return false;
            }
        }
        return true;
    }

    /**
     */
    private static boolean canSearch(Node node) {
        Lookup nodeLookup = node.getLookup();
        
        /* 1st try - is the SearchInfo object in the node's lookup? */
        SearchInfo searchInfo = nodeLookup.lookup(SearchInfo.class);
        if (searchInfo != null) {
            return searchInfo.canSearch();
        }
    
        /* 2nd try - does the node represent a DataObject.Container? */
        return nodeLookup.lookup(DataObject.Container.class) != null;
    }
    
    protected void startListening() {

        /* thread: <any> */
        
        TopComponent.Registry tcRegistry = TopComponent.getRegistry();
        currentNodesWeakListener = WeakListeners.propertyChange(this, tcRegistry);
        tcRegistry.addPropertyChangeListener(currentNodesWeakListener);
    }

    protected void stopListening() {

        /* thread: <any> */
        
        TopComponent.getRegistry().removePropertyChangeListener(currentNodesWeakListener);
        currentNodesWeakListener = null;
    }

    public synchronized void propertyChange(PropertyChangeEvent e) {
        if (PROP_CURRENT_NODES.equals(e.getPropertyName())) {
            updateIsApplicable();
        }
    }

    public SearchInfo getSearchInfo() {
        return getSearchInfo(TopComponent.getRegistry().getActivatedNodes());
    }

    private SearchInfo getSearchInfo(Node[] nodes) {
        if ((nodes == null) || (nodes.length == 0)) {
            return createEmptySearchInfo();
        }
        
        nodes = normalizeNodes(nodes);
        if (nodes.length == 1) {
            SearchInfo searchInfo = getSearchInfo(nodes[0]);
            return (searchInfo != null) ? searchInfo : createEmptySearchInfo();
        }
        
        List<SearchInfo> searchInfos = new ArrayList<SearchInfo>(nodes.length);
        for (Node node : nodes) {
            SearchInfo searchInfo = getSearchInfo(node);
            if (searchInfo != null) {
                searchInfos.add(searchInfo);
            }
        }
        
        if (searchInfos.isEmpty()) {
            return createEmptySearchInfo();
        }
        int searchInfoCount = searchInfos.size();
        if (searchInfoCount == 1) {
            return searchInfos.get(0);
        } else {
            return SearchInfoFactory.createCompoundSearchInfo(
                        searchInfos.toArray(new SearchInfo[searchInfoCount]));
        }
    }

    /**
     */
    private static SearchInfo getSearchInfo(Node node) {
        /* 1st try - is the SearchInfo object in the node's lookup? */
        SearchInfo info = node.getLookup().lookup(SearchInfo.class);
        if (info != null) {
            return info;
        }

        /* 2nd try - does the node represent a DataObject.Container? */
        DataFolder dataFolder = node.getLookup().lookup(DataFolder.class);
        if (dataFolder == null) {
            return null;
        } else {
            return SearchInfoFactory.createSearchInfo(
                                dataFolder.getPrimaryFile(),
                                true,                       //recursive
                                new FileObjectFilter[] {
                                        SearchInfoFactory.VISIBILITY_FILTER });
        }
    }
    
    /**
     * Computes a subset of nodes (search origins) covering all specified nodes.
     * <p>
     * Search is performed on trees whose roots are the specified nodes.
     * If node A is a member of the tree determined by node B, then the A's tree
     * is a subtree of the B's tree. It means that it is redundant to extra
     * search the A's tree. This method computes a minimum set of nodes whose
     * trees cover all nodes' subtrees but does not contain any node not covered
     * by the original set of nodes.
     *
     * @param  nodes  roots of search trees
     * @return  subset of the original set of nodes
     *          (may be the same object as the parameter)
     */
    private static Node[] normalizeNodes(Node[] nodes) {
        
        /* No need to normalize: */
        if (nodes.length < 2) {
            return nodes;
        }
        
        /*
         * In the algorithm, we use two sets of nodes: "good nodes" and "bad
         * nodes". "Good nodes" are nodes not known to be covered by any 
         * search root. "Bad nodes" are nodes known to be covered by at least
         * one of the search roots.
         *
         * Since all the search roots are covered by themselves, they are all
         * put to "bad nodes" initially. To recognize whether a search root
         * is covered only by itself or whether it is covered by any other
         * search root, the former group of nodes has mapped value FALSE
         * and the later group of nodes has mapped value TRUE.
         *
         * Initially, all search roots have mapped value FALSE (not known to be
         * covered by any other search root) and as the procedure runs, some of
         * them may be remapped to value TRUE (known to be covered by at least
         * one other search root).
         *
         * The algorithm checks all search roots one by one. The ckeck starts
         * at a search root to be tested and continues up to its parents until
         * one of the following:
         *  a) the root of the whole tree of nodes is reached
         *     - i.e. the node being checked is not covered by any other
         *       search root
         *     - mark all the nodes in the path from the node being checked
         *       to the root as "good nodes", except the search root being
         *       checked
         *     - put the search root being checked into the resulting set
         *       of nodes
         *  b) a "good node" is reached
         *     - i.e. neither the good node nor any of the nodes on the path
         *       are covered by any other search root
         *     - mark all the nodes in the path from the node being checked
         *       to the root as "good nodes", except the search root being
         *       checked
         *     - put the search root being checked into the resulting set
         *       of nodes
         *  c) a "bad node" is reached (it may be either another search root
         *     or another "bad node")
         *     - i.e. we know that the reached node is covered by another search
         *       root or the reached node is another search root - in both cases
         *       the search root being checked is covered by another search root
         *     - mark all the nodes in the path from the node being checked
         *       to the root as "bad nodes"; the search root being checked
         *       will be remapped to value TRUE
         */
        
        Map<Node, Boolean> badNodes = new HashMap<Node, Boolean>(2 * nodes.length, 0.75f);
        Map<Node, Boolean> goodNodes = new HashMap<Node, Boolean>(2 * nodes.length, 0.75f);
        List<Node> path = new ArrayList<Node>(10);
        List<Node> result = new ArrayList<Node>(nodes.length);
        
        /* Put all search roots into "bad nodes": */
        for (int i = 0; i < nodes.length; i++) {
            badNodes.put(nodes[i], Boolean.FALSE);
        }
        
        main_cycle:
        for (int i = 0; i < nodes.length; i++) {
            path.clear();
            boolean isBad = false;
            for (Node n = nodes[i].getParentNode(); n != null;
                                                    n = n.getParentNode()) {
                if (badNodes.containsKey(n)) {
                    isBad = true;
                    break;
                }
                if (goodNodes.containsKey(n)) {
                    break;
                }
                path.add(n);
            }
            if (isBad) {
                badNodes.put(nodes[i], Boolean.TRUE);
		for (Node n : path) {
                    badNodes.put(n, Boolean.TRUE);
		}
            } else {
                for (Node n : path) {
                    goodNodes.put(n, Boolean.TRUE);
                }
                result.add(nodes[i]);
            }
        }
        return (Node[]) result.toArray(new Node[result.size()]);
    }

    @Override
    protected SearchScope getContextSensitiveInstance(Lookup context) {
        return new LookupSensitive(this, context);
    }

    /**
     * Lookup-sensitive variant of {@code SearchScopeNodeSelection}.
     */
    private static final class LookupSensitive extends AbstractSearchScope
                                               implements LookupListener {

        private static final Node[] emptyNodesArray = new Node[0];
        private final SearchScopeNodeSelection delegate;
        private final Lookup.Result<Node> lookupResult;
        private LookupListener lookupListener;

        LookupSensitive(SearchScopeNodeSelection delegate, Lookup lookup) {
            this.delegate = delegate;

            lookupResult = lookup.lookupResult(Node.class);
        }

        private Node[] nodes() {
            Collection<? extends Node> nodesColl = lookupResult.allInstances();
            return nodesColl.isEmpty() ? emptyNodesArray
                                       : nodesColl.toArray(emptyNodesArray);
        }

        protected void startListening() {

            /* thread: <any> */

            lookupListener = WeakListeners.create(LookupListener.class,
                                                  this,
                                                  lookupResult);
            lookupResult.addLookupListener(lookupListener);
        }

        protected void stopListening() {

            /* thread: <any> */

            if (lookupListener != null) {
                lookupResult.removeLookupListener(lookupListener);
            }
        }

        public void resultChanged(LookupEvent ev) {
            updateIsApplicable();
        }

        protected boolean checkIsApplicable() {
            return SearchScopeNodeSelection.checkIsApplicable(nodes());
        }

        protected SearchInfo getSearchInfo() {
            return delegate.getSearchInfo(nodes());
        }

        protected String getDisplayName() {
            return delegate.getDisplayName();
        }

    }

}
