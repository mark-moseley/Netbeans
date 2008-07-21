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


package org.netbeans.modules.search;


import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.openide.ErrorManager;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openidex.search.SearchType;


/**
 * Holds search result data.
 *
 * @author  Petr Kuzel
 * @author  Marian Petras
 */
public final class ResultModel {

    /** maximum number of found objects */
    private static final int COUNT_LIMIT = 500;
    /** maximum total number of detail entries for found objects */
    private static final int DETAILS_COUNT_LIMIT = 5000;
    
    enum Limit {

        FILES_COUNT_LIMIT("TEXT_MSG_LIMIT_REACHED_FILES_COUNT",         //NOI18N
                          COUNT_LIMIT),
        MATCHES_COUNT_LIMIT("TEXT_MSG_LIMIT_REACHED_MATCHES_COUNT",     //NOI18N
                            DETAILS_COUNT_LIMIT);

        private final String bundleKey;
        private final Integer msgParam;

        private Limit(String bundleKey, Integer limit) {
            this.bundleKey = bundleKey;
            this.msgParam = limit;
        }

        String getDisplayName() {
            return NbBundle.getMessage(Limit.class, bundleKey, msgParam);
        }
    }

    /** */
    private final long creationTime;
    
    /** */
    private int size = 0;
    /** */
    private int totalDetailsCount = 0;
    /**
     */
    private ResultTreeModel treeModel;
    /** */
    private ResultView resultView;
    
    /**
     * limit (number of found files or matches) reached during search
     */
    private Limit limitReached = null;

    /** Search group this result shows search results for. */
    private SpecialSearchGroup searchGroup;
    
    /**
     * are all search types defined in the {@code SearchGroup} those
     * defined in the Utilities module?
     */
    final boolean isBasicCriteriaOnly;
    /** */
    final BasicSearchCriteria basicCriteria;
    /** */
    private final boolean isFullText;
    /** */
    final String replaceString;
    /** */
    final boolean searchAndReplace;
    /** list of matching objects (usually {@code DataObject}s) */
    final List<MatchingObject> matchingObjects
            = new ArrayList<MatchingObject>();
    /** set of matching objects - only used as a prevention from duplicates */
    private final Set<MatchingObject> matchingObjectsSet
            = new HashSet<MatchingObject>(40);

    /** Contains optional finnish message often reason why finished. */
    private String finishMessage;

    /** Creates new <code>ResultModel</code>. */
    ResultModel(SpecialSearchGroup searchGroup,
                       String replaceString) {
        this.searchGroup = searchGroup;
        this.replaceString = replaceString;
        this.searchAndReplace = (replaceString != null);
        
	basicCriteria = searchGroup.basicCriteria;
	isFullText = (basicCriteria != null) && basicCriteria.isFullText();
        isBasicCriteriaOnly = (searchGroup.getSearchTypes().length == 0);
        creationTime = System.currentTimeMillis();
    }
    
    /**
     */
    long getCreationTime() {
        return creationTime;
    }
    
    /**
     * Sets an observer which will be notified whenever an object is found.
     *
     * @param  observer  observer or <code>null</code>
     */
    void setObserver(ResultTreeModel observer) {
        this.treeModel = observer;
    }
    
    /**
     * Sets an observer which will be notified whenever an object is found.
     *
     * @param  observer  observer or <code>null</code>
     */
    void setObserver(ResultView observer) {
        this.resultView = observer;
    }

    /**
     * Clean the allocated resources. Do not rely on GC there as we are often
     * referenced from various objects. So keep leak as small as possible.
     * */
    void close() {
        if ((matchingObjects != null) && !matchingObjects.isEmpty()) {
            for (MatchingObject matchingObj : matchingObjects) {
                matchingObj.cleanup();
            }
        }
        
        // eliminate search group content
        // no other way then leaving it on GC, it should work because
        // search group is always recreated by a it's factory and
        // nobody keeps reference to it. 7th May 2004

        searchGroup = null;
    }

    /**
     * Notifies ths result model of a newly found matching object.
     *
     * @param  object  matching object
     * @return  {@code true} if this result model can accept more objects,
     *          {@code false} if number of found objects reached the limit
     * @param  charset  charset used for full-text search of the object,
     *                  or {@code null} if the object was not full-text searched
     */
    synchronized boolean objectFound(Object object, Charset charset) {
        MatchingObject matchingObject = new MatchingObject(this, object, charset);
        if (matchingObjectsSet.add(matchingObject) == false) {
            return true;
        }

        matchingObjects.add(matchingObject);
        
        assert limitReached == null;
        assert treeModel != null;
        assert resultView != null;
        
        totalDetailsCount += getDetailsCount(matchingObject);
        
        treeModel.objectFound(matchingObject, size++);
        resultView.objectFound(matchingObject, totalDetailsCount);
        
        if (size >= COUNT_LIMIT) {
            limitReached = Limit.FILES_COUNT_LIMIT;
        } else if (totalDetailsCount >= DETAILS_COUNT_LIMIT) {
            limitReached = Limit.MATCHES_COUNT_LIMIT;
        }
        return limitReached == null;
    }

    /**
     */
    void objectBecameInvalid(MatchingObject matchingObj) {
        
        /* may be called from non-EDT thread */
        
        int index = matchingObjects.indexOf(matchingObj);
        assert index != -1;
        
        treeModel.objectBecameInvalid(matchingObj);
    }
    
    /**
     */
    synchronized int getTotalDetailsCount() {
        return totalDetailsCount;
    }
    
    /**
     */
    synchronized MatchingObject[] getMatchingObjects() {
        return matchingObjects.toArray(
                                    new MatchingObject[matchingObjects.size()]);
    }
    
    /**
     */
    synchronized Object[] getFoundObjects() {
        Object[] foundObjects = new Object[matchingObjects.size()];
        int index = 0;
        for (MatchingObject matchingObj : matchingObjects) {
            foundObjects[index++] = matchingObj.object;
        }
        return foundObjects;
    }
    
    public void run() {
        
    }
    
    /**
     */
    boolean hasDetails() {
        return totalDetailsCount != 0;      //PENDING - synchronization?
    }
    
    /**
     * Performs a quick check whether
     * {@linkplain MatchingObject matching objects} contained in this model
     * can have details.
     * 
     * @return  {@code Boolean.TRUE} if all matching objects have details,
     *          {@code Boolean.FALSE} if no matching object has details,
     *          {@code null} if matching objects may have details
     *                         (if more time consuming check would be necessary)
     */
    Boolean canHaveDetails() {
        Boolean ret;
        if (isFullText) {
            ret = Boolean.TRUE;
        } else if (isBasicCriteriaOnly) {
            ret = Boolean.FALSE;
        } else {
            ret = null;
        }
        return ret;
    }
    
    /*
     * A cache exists for information about a single MatchingObject
     * to prevent from repetitive calls of time-consuming queries on
     * number of details and list of details. These calls are initiated
     * by the node renderer (class NodeRenderer).
     */
    
    private MatchingObject infoCacheMatchingObject;
    private Boolean infoCacheHasDetails;
    private int infoCacheDetailsCount;
    private Node[] infoCacheDetailNodes;
    private final Node[] EMPTY_NODES_ARRAY = new Node[0];
    
    /**
     */
    private void prepareCacheFor(MatchingObject matchingObject) {
        if (matchingObject != infoCacheMatchingObject) {
            infoCacheHasDetails = null;
            infoCacheDetailsCount = -1;
            infoCacheDetailNodes = null;
            infoCacheMatchingObject = matchingObject;
        }
    }
    
    /**
     */
    boolean hasDetails(MatchingObject matchingObject) {
        prepareCacheFor(matchingObject);
        if (infoCacheHasDetails != null) {
            return infoCacheHasDetails.booleanValue();
        }
        
        boolean hasDetails = hasDetailsReal(matchingObject);
        infoCacheHasDetails = Boolean.valueOf(hasDetails);
        
        assert (infoCacheHasDetails == Boolean.TRUE)
               || (infoCacheHasDetails == Boolean.FALSE);
        return hasDetails;
    }
    
    /**
     */
    private boolean hasDetailsReal(MatchingObject matchingObject) {
        boolean ret;
        if (isFullText) {
            ret = true;
        } else if (isBasicCriteriaOnly) {
            ret = false;
        } else {
            ret = false;
            final Object foundObject = matchingObject.object;
            for (SearchType searchType : searchGroup.getSearchTypes()) {
                Node[] detailNodes = searchType.getDetails(foundObject);
                if ((detailNodes != null) && (detailNodes.length != 0)) {
                    ret = true;
                    break;
                }
            }
        }
        return ret;
    }
    
    /**
     */
    int getDetailsCount(MatchingObject matchingObject) {
        prepareCacheFor(matchingObject);
        if (infoCacheDetailsCount == -1) {
            infoCacheDetailsCount = getDetailsCountReal(matchingObject);
            if (infoCacheDetailsCount == 0) {
                infoCacheDetailNodes = EMPTY_NODES_ARRAY;
            }
        }
        
        assert infoCacheDetailsCount >= 0;
        return infoCacheDetailsCount;
    }
    
    /**
     * Returns number of detail nodes available to the given found object.
     *
     * @param  foundObject  object matching the search criteria
     * @return  number of detail items (represented by individual nodes)
     *          available for the given object (usually {@code DataObject})
     */
    private int getDetailsCountReal(MatchingObject matchingObject) {
        int count = isFullText ? basicCriteria.getDetailsCount(matchingObject.object)
                               : 0;
        if (isBasicCriteriaOnly) {
            return count;
        }
        
        final Object foundObject = matchingObject.object;
        for (SearchType searchType : searchGroup.getSearchTypes()) {
            Node[] detailNodes = searchType.getDetails(foundObject);
            count += (detailNodes != null) ? detailNodes.length : 0;
        }
        return count;
    }
    
    /**
     * 
     * @return  non-empty array of detail nodes
     *          or {@code null} if there are no detail nodes
     */
    Node[] getDetails(MatchingObject matchingObject) {
        prepareCacheFor(matchingObject);
        Node[] detailNodes;
        if (infoCacheDetailNodes == null) {
            detailNodes = getDetailsReal(matchingObject);
            infoCacheDetailNodes = (detailNodes != null)
                                   ? detailNodes
                                   : EMPTY_NODES_ARRAY;
            infoCacheDetailsCount = infoCacheDetailNodes.length;
        } else {
            detailNodes = (infoCacheDetailNodes != EMPTY_NODES_ARRAY)
                          ? infoCacheDetailNodes
                          : null;
        }
        
        assert (infoCacheDetailNodes != null)
               && ((infoCacheDetailNodes == EMPTY_NODES_ARRAY)
                   || (infoCacheDetailNodes.length > 0));
        assert (detailNodes == null) || (detailNodes.length > 0);
        return detailNodes;
    }
    
    /**
     * 
     * @return  non-empty array of detail nodes
     *          or {@code null} if there are no detail nodes
     */
    private Node[] getDetailsReal(MatchingObject matchingObject) {
        Node[] nodesTotal = null;
        if (basicCriteria != null) {
            nodesTotal = basicCriteria.isFullText()
                         ? basicCriteria.getDetails(matchingObject.object)
                         : null;
	}
        if (isBasicCriteriaOnly) {
            return nodesTotal;
        }
        
        final Object foundObject = matchingObject.object;
        for (SearchType searchType : searchGroup.getSearchTypes()) {
            Node[] detailNodes = searchType.getDetails(foundObject);
            if ((detailNodes == null) || (detailNodes.length == 0)) {
                continue;
            }
            if (nodesTotal == null) {
                nodesTotal = detailNodes;
            } else {
                Node[] oldNodesTotal = nodesTotal;
                nodesTotal = new Node[nodesTotal.length + detailNodes.length];
                System.arraycopy(oldNodesTotal, 0,
                                 nodesTotal, 0,
                                 oldNodesTotal.length);
                System.arraycopy(detailNodes, 0,
                                 nodesTotal, oldNodesTotal.length,
                                 detailNodes.length);
            }
        }
        return nodesTotal;
    }
    
    /**
     */
    synchronized int size() {
        return size;
    }
    
    /**
     */
    boolean isEmpty() {
        return size == 0;
    }

    /** Getter for search group property. */
    SpecialSearchGroup getSearchGroup() {
        return searchGroup;
    }
    
    /**
     * Returns search types that were used during the search.
     *
     * @return  array of <code>SearchType</code>s that each tested object was
     *          tested for compliance
     */
    SearchType[] getQueriedSearchTypes() {
        return searchGroup.getSearchTypes();
    }

    /**
     */
    boolean wasLimitReached() {
        return limitReached != null;
    }

    /**
     */
    String getLimitDisplayName() {
        return (limitReached != null) ? limitReached.getDisplayName() : null;
    }

    /** This exception stoped search */
    void searchException(RuntimeException ex) {
        ErrorManager.Annotation[] annotations = ErrorManager.getDefault().findAnnotations(ex);
        for (int i = 0; i < annotations.length; i++) {
            ErrorManager.Annotation annotation = annotations[i];
            if (annotation.getSeverity() == ErrorManager.USER) {
                finishMessage = annotation.getLocalizedMessage();
                if (finishMessage != null) return;
            }
        }
        finishMessage = ex.getLocalizedMessage();
    }
    
    /**
     */
    String getExceptionMsg() {
        return finishMessage;
    }

}
