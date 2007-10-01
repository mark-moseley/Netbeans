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

package org.netbeans.modules.editor.fold;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.event.DocumentEvent;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.editor.fold.Fold;
import org.netbeans.api.editor.fold.FoldHierarchyEvent;
import org.netbeans.api.editor.fold.FoldHierarchyListener;
import org.netbeans.api.editor.fold.FoldStateChange;
import org.netbeans.api.editor.fold.FoldType;
import org.netbeans.api.editor.fold.FoldUtilities;
import org.netbeans.modules.editor.fold.FoldUtilitiesImpl;
import org.netbeans.spi.editor.fold.FoldHierarchyTransaction;
import org.netbeans.spi.editor.fold.FoldManager;
import org.openide.ErrorManager;

/**
 * Class encapsulating a modification
 * of the code folding hierarchy.
 * <br>
 * It's provided by {@link RootFold#createHierarchyTransaction()}.
 * <br>
 * It can accumulate arbitrary number of changes of various folds.
 * <br>
 * Only one transaction can be active at the time.
 * <br>
 * Once all the modifications are done the transaction must be
 * committed by {@link #commit()} which creates
 * a {@link org.netbeans.api.editor.fold.FoldHierarchyEvent}
 * and fires it to the listeners automatically.
 * <br>
 * Once the transaction is committed no additional
 * changes can be made to it.
 * <br>
 * There is currently no way to rollback the transaction.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public final class FoldHierarchyTransactionImpl {
    
    private static final boolean debug
        = Boolean.getBoolean("netbeans.debug.editor.fold");
    
    private static final Fold[] EMPTY_FOLDS = new Fold[0];

    private static final FoldStateChange[] EMPTY_FOLD_STATE_CHANGES
        = new FoldStateChange[0];
    
    private static final int[] EMPTY_INT_ARRAY = new int[0];
    
    
    private FoldHierarchyTransaction transaction;

    private boolean committed;
    
    private FoldHierarchyExecution execution;
    
    /**
     * Fold inside which the last operation (insert or remove)
     * was done.
     */
    private Fold lastOperationFold;

    /**
     * Index at which the last operation (insert or remove) was done.
     */
    private int lastOperationIndex;
    
    /**
     * Fold that is block in case the inspectOverlap() returns null.
     * <br>
     * This is instance var so that inspectOverlap() can set it.
     */
    private Fold addFoldBlock;
    
    /**
     * List of lists of folds that were unblocked by removing
     * of a blocked fold indexed by the fold priority.
     * <br>
     * Prior to the commit of the transaction the unblocked
     * folds are attempted to be reinserted into the hierarchy
     * starting with folds with the highest priority
     * going to folds with the lowest priority.
     */
    private List unblockedFoldLists = new ArrayList(4);
    
    /**
     * Maximum priority of the unblocked folds added
     * since the start of this transaction.
     */
    private int unblockedFoldMaxPriority = -1;
    
    /**
     * Set of folds that were added to the hierarchy
     * during this transaction.
     */
    private Set addedToHierarchySet;
    
    /**
     * Set of folds that were removed from the hierarchy
     * during this transaction.
     */
    private Set removedFromHierarchySet;

    private Map fold2StateChange;
    
    private int affectedStartOffset;
    
    private int affectedEndOffset;
    
    
    public FoldHierarchyTransactionImpl(FoldHierarchyExecution execution) {
        this.execution = execution;
        this.affectedStartOffset = Integer.MAX_VALUE;
        this.affectedEndOffset = -1;
        
        this.transaction = SpiPackageAccessor.get().createFoldHierarchyTransaction(this);
    }
    
    public FoldHierarchyTransaction getTransaction() {
        return transaction;
    }

    /**
     * Commit this active transaction.
     * <br>
     * The <code>FoldHierarchyEvent</code> will be fired automatically
     * (if there were any changes done during this transaction).
     * <br>
     * The transaction can only be commited once.
     */
    public void commit() {
        checkNotCommitted();

        /**
         * Mark the transaction as committed now
         * to prevent problems in case one of the listeners fails later.
         */
        committed = true;
        execution.clearActiveTransaction();

        if (!isEmpty()) {
            
            int size;
            Fold[] removedFolds;
            if (removedFromHierarchySet != null && ((size = removedFromHierarchySet.size()) != 0)) {
                removedFolds = new Fold[size];
                removedFromHierarchySet.toArray(removedFolds);

            } else {
                removedFolds = EMPTY_FOLDS;
            }
            
            Fold[] addedFolds;
            if (addedToHierarchySet != null && ((size = addedToHierarchySet.size()) != 0)) {
                addedFolds = new Fold[size];
                addedToHierarchySet.toArray(addedFolds);

            } else {
                addedFolds = EMPTY_FOLDS;
            }

            FoldStateChange[] stateChanges;
            if (fold2StateChange != null) {
                stateChanges = new FoldStateChange[fold2StateChange.size()];
                fold2StateChange.values().toArray(stateChanges);
            } else { // no state changes => use empty array
                stateChanges = EMPTY_FOLD_STATE_CHANGES;
            }
            
            for (int i = stateChanges.length - 1; i >= 0; i--) {
                FoldStateChange change = stateChanges[i];
                Fold fold = change.getFold();
                updateAffectedOffsets(fold);
                int origOffset = change.getOriginalStartOffset();
                if (origOffset != -1) {
                    updateAffectedStartOffset(origOffset);
                }
                origOffset = change.getOriginalEndOffset();
                if (origOffset != -1) {
                    updateAffectedEndOffset(origOffset);
                }
            }

            execution.createAndFireFoldHierarchyEvent(
                removedFolds, addedFolds, stateChanges,
                affectedStartOffset, affectedEndOffset
            );
        }
    }
    
    /**
     * This method implements the <code>DocumentListener</code>.
     * <br>
     * It is not intended to be called by clients.
     */
    public void insertUpdate(DocumentEvent evt) {
        // Check whether there was an insert done right
        // at the original ending offset of the fold
        // so the fold end offset should be moved back.
        if (debug) {
            /*DEBUG*/System.err.println("insertUpdate: offset=" + evt.getOffset() // NOI18N
                + ", length=" + evt.getLength()); // NOI18N
        }

        try {
            insertCheckEndOffset(execution.getRootFold(), evt);

        } catch (BadLocationException e) {
            ErrorManager.getDefault().notify(e);
        }
    }
    
    private void insertCheckEndOffset(Fold fold, DocumentEvent evt)
    throws BadLocationException {
        int insertEndOffset = evt.getOffset() + evt.getLength();
        // Find first fold that starts at (or best represents) the insertEndOffset
        int childIndex = FoldUtilitiesImpl.findFoldStartIndex(fold, insertEndOffset, false);
        if (childIndex >= 0) { // could be at end of the child fold with the index
            Fold childFold = fold.getFold(childIndex);
            // Check whether not in fact searching for previous fold
            if (childIndex > 0 && childFold.getStartOffset() == insertEndOffset) {
                childIndex--;
                childFold = fold.getFold(childIndex);
            }
            
            int childFoldEndOffset = childFold.getEndOffset();
            // Check whether the child fold "contains" the insert
            // i.e. the children of the child must be checked as well
            if (childFoldEndOffset >= insertEndOffset) { // check children
                // Must dig into children first to maintain consistency
                // in case when the last child fold would end right at end offset
                // of this child.
                insertCheckEndOffset(childFold, evt);

                // Inform the fold about insertion
                ApiPackageAccessor.get().foldInsertUpdate(childFold, evt);

                if (childFoldEndOffset == insertEndOffset) {
                    // Now correct the end offset to the one before insertion
                    setEndOffset(childFold, evt.getDocument(), evt.getOffset());
                    
                } else { // not right at the end of the fold -> check damaged
                    ApiPackageAccessor api = ApiPackageAccessor.get();
                    if (api.foldIsStartDamaged(childFold) || api.foldIsEndDamaged(childFold)) {
                        execution.remove(childFold, this);
                        removeDamagedNotify(childFold);
                        
                        if (debug) {
                            /*DEBUG*/System.err.println("insertUpdate: removed damaged " // NOI18N
                                + childFold);
                        }
                    }
                }
            }
        }
    }
    
    private FoldOperationImpl getOperation(Fold fold) {
        return ApiPackageAccessor.get().foldGetOperation(fold);
    }
    
    private FoldManager getManager(Fold fold) {
        return getOperation(fold).getManager();
    }
    
    private void setEndOffset(Fold fold, Document doc, int endOffset)
    throws BadLocationException {
        int origEndOffset = fold.getEndOffset();
        ApiPackageAccessor api = ApiPackageAccessor.get();
        api.foldSetEndOffset(fold, doc, endOffset);
        api.foldStateChangeEndOffsetChanged(getFoldStateChange(fold), origEndOffset);
    }

    public void setCollapsed(Fold fold, boolean collapsed) {
        boolean oldCollapsed = fold.isCollapsed();
        if (oldCollapsed != collapsed) {
            ApiPackageAccessor api = ApiPackageAccessor.get();
            api.foldSetCollapsed(fold, collapsed);
            api.foldStateChangeCollapsedChanged(getFoldStateChange(fold));
        }
    }
    
    private void removeDamagedNotify(Fold fold) {
        getManager(fold).removeDamagedNotify(fold);
    }
    
    private void removeEmptyNotify(Fold fold) {
        getManager(fold).removeEmptyNotify(fold);
    }
    
    /**
     * This method implements the <code>DocumentListener</code>.
     * <br>
     * It is not intended to be called by clients.
     */
    public void removeUpdate(DocumentEvent evt) {
        // Check whether the remove damaged any folds
        // or made them empty.
        if (debug) {
            /*DEBUG*/System.err.println("removeUpdate: offset=" + evt.getOffset());
        }

        removeCheckDamaged(execution.getRootFold(), evt);
    }
    
    private void removeCheckDamaged(Fold fold, DocumentEvent evt) {
        ApiPackageAccessor api = ApiPackageAccessor.get();
        int childIndex = FoldUtilitiesImpl.findFoldStartIndex(fold, evt.getOffset(), true);
        if (childIndex >= 0) {
            boolean removed;
            do {
                Fold childFold = fold.getFold(childIndex);
                removed = false;
                if (FoldUtilities.isEmpty(childFold)) {
                    removeCheckDamaged(childFold, evt); // nest prior removing
                    execution.remove(childFold, this);
                    getManager(childFold).removeEmptyNotify(childFold);
                    removed = true;

                    if (debug) {
                        /*DEBUG*/System.err.println("insertUpdate: removed empty " // NOI18N
                        + childFold);
                    }

                } else if (api.foldIsStartDamaged(childFold) || api.foldIsEndDamaged(childFold)) {
                    removeCheckDamaged(childFold, evt); // nest prior removing
                    execution.remove(childFold, this);
                    getManager(childFold).removeDamagedNotify(childFold);
                    removed = true;

                    if (debug) {
                        /*DEBUG*/System.err.println("insertUpdate: removed damaged " // NOI18N
                        + childFold);
                    }

                } else if (childFold.getFoldCount() > 0) { // check children
                    // Some children could be damaged even if this one was not
                    removeCheckDamaged(childFold, evt);
                }
                
                // Check whether the expand is necessary
                if (!removed) { // only if not removed yet
                    if (childFold.isCollapsed() && api.foldIsExpandNecessary(childFold)) {
                        setCollapsed(childFold , false);
                    }
                    
                    api.foldRemoveUpdate(childFold, evt);
                }

                // intentionally do not increase childIndex
            } while (removed && childIndex < fold.getFoldCount());
        }
    }
    
    private boolean isEmpty() {
        return (fold2StateChange == null || fold2StateChange.size() == 0)
            && (addedToHierarchySet == null || addedToHierarchySet.size() == 0)
            && (removedFromHierarchySet == null || removedFromHierarchySet.size() == 0);
    }

    public FoldStateChange getFoldStateChange(Fold fold) {
        if (fold2StateChange == null) {
            fold2StateChange = new HashMap();
        }
        
        FoldStateChange change = (FoldStateChange)fold2StateChange.get(fold);
        if (change == null) {
            change = ApiPackageAccessor.get().createFoldStateChange(fold);
            fold2StateChange.put(fold, change);
        }

        return change;
    }

    /**
     * Remove the fold either from the hierarchy or from the blocked list.
     */
    void removeFold(Fold fold) {
        if (debug) {
            /*DEBUG*/System.err.println("removeFold: " + fold);
        }

        Fold parent = fold.getParent();
        if (parent != null) { // present in hierarchy
            int index = parent.getFoldIndex(fold);
            removeFoldFromHierarchy(parent, index, null); // no block passed here

            lastOperationFold = parent;
            lastOperationIndex = index;

        } else { // not present in hierarchy - must be blocked (or error)
            if (!execution.isBlocked(fold)) { // not blocked i.e. already removed
                throw new IllegalStateException("Fold already removed: " + fold); // NOI18N
            }
            execution.unmarkBlocked(fold);
            // If the fold was blocking other folds then unblock them here
            unblockBlocked(fold);
        }
        
        processUnblocked(); // attempt to reinsert unblocked folds
    }
    
    /**
     * Remove all present folds in the hierarchy
     * once the managers are going to be switched.
     */
    void removeAllFolds(Fold[] allBlocked) {
        // First remove all blocked folds 
        for (int i = allBlocked.length - 1; i >= 0; i--) {
            removeFold(allBlocked[i]);
        }
        
        removeAllChildrenAndSelf(execution.getRootFold());
    }
    
    private void removeAllChildrenAndSelf(Fold fold) {
        int foldCount = fold.getFoldCount();
        if (foldCount > 0) {
            for (int i = foldCount - 1; i >= 0; i--) {
                removeAllChildrenAndSelf(fold.getFold(i));
            }
        }
        if (!FoldUtilities.isRootFold(fold)) {
            removeFold(fold);
        }
    }
    
    public void changedUpdate(DocumentEvent evt) {
        // No explicit checking actions upon document change notification
    }

    /**
     * Called by FoldHierarchySpi to attempt to insert
     * the fold into hierarchy. It's also possible that
     * the fold cannot be inserted and will be added to the list
     * of blocked folds.
     *
     * @param fold fold to add
     * @return true if the fold was successfully added to hierarchy
     *  or false if it could not be added and became blocked.
     */
    boolean addFold(Fold fold) {
        if (debug) {
            /*DEBUG*/System.err.println("addFold: " + fold); // NOI18N
        }

        return addFold(fold, null);
    }
    
    /**
     * Recursive method to add fold under the given parent.
     *
     * @param fold non-null fold to be inserted into hierarchy
     * @param parentFold parent fold under which to insert. If it's null
     *  then attempt to use hints from lastOperationFold and lastOperationIndex.
     *  The explicit passing of root fold can be used to force to ignore the hints.
     * @return true if the fold was successfully added or false if it became blocked.
     */
    private boolean addFold(Fold fold, Fold parentFold) {
        int foldStartOffset = fold.getStartOffset();
        int foldEndOffset = fold.getEndOffset();
        int foldPriority = getOperation(fold).getPriority();
        
        int index;
        boolean useLast; // use hints from lastOperationFold and lastOperationIndex
        if (parentFold == null) { // attempt to guess
            parentFold = lastOperationFold;
            if (parentFold == null // no valid guess
                || foldStartOffset < parentFold.getStartOffset()
                || foldEndOffset > parentFold.getEndOffset()
            ) { // Use root fold
                parentFold = execution.getRootFold();
                index = FoldUtilitiesImpl.findFoldInsertIndex(parentFold, foldStartOffset);
                useLast = false;
            } else {
                index = lastOperationIndex;
                useLast = true;
            }

        } else { // already valid parentFold (do not use last* vars)
            index = FoldUtilitiesImpl.findFoldInsertIndex(parentFold, foldStartOffset);
            useLast = false;
        }            
        
        // Check whether the index is withing bounds
        int foldCount = parentFold.getFoldCount();
        if (useLast && index > foldCount) {
            index = FoldUtilitiesImpl.findFoldInsertIndex(parentFold, foldStartOffset);
            useLast = false;
        }

        // Fill in the prevFold variable
        // and verify that the guessed index is correct - startOffset
        // of the prev fold must be lower than foldStartOffset
        // and start offset of the next fold must be greater than foldStartOffset

        Fold prevFold; // fold that precedes fold being added
        if (index > 0) {
            prevFold = parentFold.getFold(index - 1);
            if (useLast && foldStartOffset < prevFold.getStartOffset()) { // bad guess
                index = FoldUtilitiesImpl.findFoldInsertIndex(parentFold, foldStartOffset);
                useLast = false;
                prevFold = (index > 0) ? parentFold.getFold(index - 1) : null;
            }

        } else { // index == 0
            prevFold = null;
        }

        // Fold that will follow the fold being inserted
        // By default guess it's the fold at "index" but it may be a fold
        // at higher index as well.
        Fold nextFold;
        if (index < foldCount) { // next fold exists
            nextFold = parentFold.getFold(index);
            if (useLast && foldStartOffset >= nextFold.getStartOffset()) { // bad guess
                index = FoldUtilitiesImpl.findFoldInsertIndex(parentFold, foldStartOffset);
                useLast = false;
                prevFold = (index > 0) ? parentFold.getFold(index - 1) : null;
                nextFold = (index < foldCount) ? parentFold.getFold(index) : null;
            }

        } else { // index >= foldCount
            nextFold = null;
        }

        // Check whether the fold to be added overlaps
        // with previous fold (it's start offset is before end offset
        // of the previous fold.
        // Check whether end offset of the fold
        // does not overlap with folds that would follow it
        boolean blocked;
        // Index hints:
        //   null - no overlapping (clear insert of start offset)
        //   length == 0 - overlapping but no children
        //   length > 0 - overlapping and children - see inspectOverlap()
        int[] prevOverlapIndexes;
        if (prevFold != null && foldStartOffset < prevFold.getEndOffset()) { // overlap
            if (foldEndOffset <= prevFold.getEndOffset()) { // fold fully nested
                // Nest into prevFold
                return addFold(fold, prevFold);
                
            } else { // fold overlaps with prevFold
                if (foldPriority > getOperation(prevFold).getPriority()) { // can replace
                    if (prevFold.getFoldCount() > 0) { // must check children too
                        prevOverlapIndexes = inspectOverlap(prevFold,
                            foldStartOffset, foldPriority, 1);

                        if (prevOverlapIndexes == null) { // blocked
                            // "addFoldBlock" var was assigned by inspectOverlap()
                            blocked = true;
                        } else { // not blocked
                            blocked = false;
                        }

                    } else { // prevFold has no children
                        blocked = false;
                        prevOverlapIndexes = EMPTY_INT_ARRAY;
                    }
                } else { // cannot remove -> overlaps
                    blocked = true;
                    addFoldBlock = prevFold;
                    prevOverlapIndexes = null; 
                }
            }

        } else { // no overlapping with prevFold -> insert after
            blocked = false;
            prevOverlapIndexes = null;
        }


        if (!blocked) {
            // Which fold will be the next important for the insert (possibly overlapped)
            int nextIndex = index;
            // Non-null in case of active overlapping for foldEndOffset
            int[] nextOverlapIndexes = null;
            if (nextFold != null) { // next fold exists
                if (foldEndOffset > nextFold.getStartOffset()) {
                    // End inside or after the current fold
                    if (foldEndOffset >= nextFold.getEndOffset()) {
                        // Fold ends after end offset of the current nextFold
                        // Find the fold in (or after) which the inserted fold really ends.
                        // Do binary search to have deterministic non-linear perf
                        // Third param is false i.e. get possibly last fold
                        //  in multiple empty folds (same like in findFoldInsertIndex())
                        nextIndex = FoldUtilitiesImpl.findFoldStartIndex(parentFold,
                        foldEndOffset, false);

                        // nextIndex should not be -1 - otherwise should not reach this code
                        nextFold = parentFold.getFold(nextIndex);
                    }

                    if (foldEndOffset < nextFold.getEndOffset()) { // ends inside
                        if (foldPriority > getOperation(nextFold).getPriority()) { // remove next fold
                            if (nextFold.getFoldCount() > 0) { // next has children
                                nextOverlapIndexes = inspectOverlap(nextFold, foldEndOffset, foldPriority, 1);
                                if (nextOverlapIndexes == null) { // blocked
                                    // "addFoldBlock" var was assigned by inspectOverlap()
                                    blocked = true;
                                } // can remove nested folds

                            } else { // nextFold has no children => can be removed
                                nextOverlapIndexes = EMPTY_INT_ARRAY;
                            }

                        } else { // blocked by next fold
                            blocked = true;
                            addFoldBlock = nextFold;
                        }

                    } else { // fold ends after bounds of nextFold but prior start of next fold
                        nextIndex++; // insert clearly after the nextFold
                    }

                } // fold ends before start offset of nextFold => insert normally later
            } // next fold does not exist - no folds at index or after it

            
            if (!blocked) {
                // Here it should be possible to insert the fold
                // prevOverlapIndexes and nextOverlapIndexes need to be resolved first
                // (and the possible index shift consequences)
                // Finally the lastOperationFold and lastOperationIndex
                // should be set for future use.
                
                if (prevOverlapIndexes != null) {
                    int replaceIndexShift;
                    if (prevOverlapIndexes.length == 0) { // no children
                        replaceIndexShift = 0;
                    } else { // children
                        replaceIndexShift = removeOverlap(prevFold,
                            prevOverlapIndexes, fold);
                        // Must shift nextIndex by number of replaced children
                        nextIndex += prevFold.getFoldCount();
                    }

                    removeFoldFromHierarchy(parentFold, index - 1, fold);
                    index += replaceIndexShift - 1; // -1 for removed prevFold
                    nextIndex--; // -1 for removed prevFold
                }
                
                if (nextOverlapIndexes != null) {
                    int replaceIndexShift;
                    if (nextOverlapIndexes.length == 0) { // no children
                        replaceIndexShift = 0;
                    } else { // children
                        replaceIndexShift = removeOverlap(nextFold,
                            nextOverlapIndexes, fold);
                    }
                    
                    removeFoldFromHierarchy(parentFold, nextIndex, fold);
                    nextIndex += replaceIndexShift;
                }
                
                ApiPackageAccessor.get().foldExtractToChildren(parentFold, index, nextIndex - index, fold);
                
                // Update affected offsets
                updateAffectedOffsets(fold);
                markFoldAddedToHierarchy(fold);
                processUnblocked();
            }
        }
        
        if (blocked) {
             // Fold is blocked - "addFoldBlock" var holds the blocker
            execution.markBlocked(fold, addFoldBlock);
            addFoldBlock = null; // enable GC
        }
        
        // Remember hints for next call
        lastOperationFold = parentFold;
        lastOperationIndex = index + 1;
        
        return !blocked;
    }
    
    /**
     * Nested check of possibility of inserting a fold.
     *
     * @param fold that has at least one child fold. Folds with empty
     *  children cannot be used here.
     * @param offset of inserting
     * @param priority of the fold
     * @param level nesting level of check - starting at 1
     * @return array of ints containing deepest-level + 1 entries
     *   where each item presents the index of the overlapped item
     *   that needs to be removed. The first array item
     *   is either 0 - clean insert after the index inside the deepest level
     *   or 1 - overlapping but removable (has no children).
     *   <br>
     *   <code>null</code> is returned if folds overlap but priority
     *   of present fold is higher so the attempted fold will become blocked.
     *   <code>checkOverlapBlock</code> will be filled with the deep fold
     *   that actually blocks.
     *
     *   <p>
     *   Example:<pre>
     *   [0] = 0   - clean insert after fold at index 5
     *   [1] = 2   - overlapping with fold at index 2 at level 1
     *   [2] = 5   - deepest level ?overlapping? => no, index 0 says clean insert
     *   </pre>
     *
     *   <p>
     *   Example 2:<pre>
     *   [0] = 1   - overlap with fold at index 4 (fold will be removed)
     *   [1] = 1   - overlapping with fold at index 1 at level 1
     *   [2] = 4   - deepest level ?overlapping? => yes, index 0 says overlapping
     *   </pre>

     */
    private int[] inspectOverlap(Fold fold, int offset, int priority, int level) {
        int index = FoldUtilitiesImpl.findFoldStartIndex(fold, offset, false);
        int[] result;
        Fold indexFold;
        if (index >= 0 && FoldUtilities.containsOffset(
            (indexFold = fold.getFold(index)), offset)
        ) {
            if (priority > getOperation(indexFold).getPriority()) { // can be replaced
                if (indexFold.getFoldCount() > 0) { // has non-empty children
                    result = inspectOverlap(indexFold, offset, priority, level + 1);
                    if (result != null) { // no blocking in children
                        result[level] = index;
                    } // result == null => blocking in children
                    
                } else { // has no or empty children
                    result = new int[level + 1];
                    result[0] = 1; // overlapping at the last level
                    result[level] = index; // will later insert at index 0
                }
            } else { // higher priority of existing fold -> return null
                addFoldBlock = indexFold; // remember the blocking fold
                result = null;
            }

        } else { // before first child fold or no overlapping
            result = new int[level + 1];
            result[0] = 0; // clearly nested
            result[level] = index; // will later insert at index 0
        }
        return result;
    }
    
    /**
     * Remove overlapping folds based on information from previous call
     * to <code>inspectOverlap()</code>.
     *
     * @param fold fold which blocking children will be removed. The fold itself
     *  will remain (must be removed by caller).
     * @param indexes indexes array obtained by previous call to inspectOverlap().
     * @param block blocking fold that will be used when marking the removed
     *  children as blocked.
     *
     * @return fold insert index that corresponds to the originally used offset.
     */
    private int removeOverlap(Fold fold, int[] indexes, Fold block) {
        int indexShift = 0; // how many new children was inserted prior to offset
        int indexesLengthM1 = indexes.length - 1;
        for (int i = 1; i < indexesLengthM1; i++) {
            int index = indexes[i] + indexShift;
            removeFoldFromHierarchy(fold, index, block);
            indexShift += index;
        }
        
        // Need to process last (most inner) fold
        int index = indexes[indexesLengthM1] + indexShift;
        if (indexes[0] == 0) { // clearly nested after the fold
            index++; // move after the fold
        } else { // indexes[0] == 1 => remove the overlap fold
            removeFoldFromHierarchy(fold, index, block);
        }
        return index;
    }
    
    /**
     * Physically remove the fold from the hierarchy and update the appropriate
     * state variables.
     */
    private void removeFoldFromHierarchy(Fold parentFold, int index, Fold block) {
        Fold removedFold = ApiPackageAccessor.get().foldReplaceByChildren(parentFold, index);
        updateAffectedOffsets(removedFold);
        markFoldRemovedFromHierarchy(removedFold);
        unblockBlocked(removedFold);
        if (block != null) {
            execution.markBlocked(removedFold, block);
        }
    }
    
    /**
     * Remove the block that was removed from hierarchy
     * because of adding of another fold. Remember
     * all folds that were blocked by the remove block
     * because they will be attempted to be reinserted
     * prior committing of this transaction.
     */
    private void unblockBlocked(Fold block) {
        Set blockedSet = execution.unmarkBlock(block);
        if (blockedSet != null) {
            for (Iterator it = blockedSet.iterator(); it.hasNext();) {
                Fold blocked = (Fold)it.next();
                int priority = getOperation(blocked).getPriority();
                while (unblockedFoldLists.size() <= priority) {
                    unblockedFoldLists.add(new ArrayList(4));
                }
                ((List)unblockedFoldLists.get(priority)).add(blocked);
                if (priority > unblockedFoldMaxPriority) {
                    unblockedFoldMaxPriority = priority;
                }
            }
        }
    }

    /**
     * Attempt to reinsert the folds unblocked by particular add/remove operation.
     */
    private void processUnblocked() {
        if (unblockedFoldMaxPriority >= 0) { // some folds became unblocked
            for (int priority = unblockedFoldMaxPriority; priority >= 0; priority--) {
                List foldList = (List)unblockedFoldLists.get(priority);
                Fold rootFold = execution.getRootFold();
                for (int i = foldList.size() - 1; i >= 0; i--) {
                    // Remove last fold from the list
                    Fold unblocked = (Fold)foldList.remove(i);
                    
                    if (!execution.isAddedOrBlocked(unblocked)) { // not yet processed
                        unblockedFoldMaxPriority = -1;

                        // Attempt to reinsert the fold - random order - use root fold
                        addFold(unblocked, rootFold);

                        if (unblockedFoldMaxPriority >= priority) {
                            throw new IllegalStateException("Folds removed with priority=" // NOI18N
                                + unblockedFoldMaxPriority);
                        }
                        if (foldList.size() != i) {
                            throw new IllegalStateException("Same priority folds removed"); // NOI18N
                        }
                    }
                }
            }
        }
        unblockedFoldMaxPriority = -1;
    }

    private void markFoldAddedToHierarchy(Fold fold) {
        // Check and remove from removedFromHierarchySet if marked removed
        if (removedFromHierarchySet == null || !removedFromHierarchySet.remove(fold)) {
            if (addedToHierarchySet == null) {
                addedToHierarchySet = new HashSet();
            }
            addedToHierarchySet.add(fold);
        }
    }
    
    private void markFoldRemovedFromHierarchy(Fold fold) {
        // Check and remove from addedToHierarchySet if marked added
        if (addedToHierarchySet == null || !addedToHierarchySet.remove(fold)) {
            if (removedFromHierarchySet == null) {
                removedFromHierarchySet = new HashSet();
            }
            removedFromHierarchySet.add(fold);
        }
    }
    
    
    private void updateAffectedOffsets(Fold fold) {
        updateAffectedStartOffset(fold.getStartOffset());
        updateAffectedEndOffset(fold.getEndOffset());
    }

    /**
     * Extend affectedStartOffset in downward direction.
     */
    private void updateAffectedStartOffset(int offset) {
        if (offset < affectedStartOffset) {
            affectedStartOffset = offset;
        }
    }
            
    /**
     * Extend affectedEndOffset in upward direction.
     */
    private void updateAffectedEndOffset(int offset) {
        if (offset > affectedEndOffset) {
            affectedEndOffset = offset;
        }
    }
            
    private void checkNotCommitted() {
        if (committed) {
            throw new IllegalStateException("FoldHierarchyChange already committed."); // NOI18N
        }
    }
    
}
