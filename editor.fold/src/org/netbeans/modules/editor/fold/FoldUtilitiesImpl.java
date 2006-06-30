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

package org.netbeans.modules.editor.fold;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import javax.swing.text.AbstractDocument;
import org.netbeans.api.editor.fold.Fold;
import org.netbeans.api.editor.fold.FoldHierarchy;
import org.netbeans.api.editor.fold.FoldHierarchyEvent;
import org.netbeans.api.editor.fold.FoldStateChange;
import org.netbeans.api.editor.fold.FoldUtilities;

/**
 * Implementations of methods from {@link org.netbeans.api.editor.fold.FoldUtilities}.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public final class FoldUtilitiesImpl {
    
    private FoldUtilitiesImpl() {
        // No instances
    }
    
    public static void collapseOrExpand(FoldHierarchy hierarchy, Collection foldTypes,
    boolean collapse) {

        AbstractDocument adoc = (AbstractDocument)hierarchy.getComponent().getDocument();
        adoc.readLock();
        try {
            hierarchy.lock();
            try {
                List foldList = findRecursive(null,
                    hierarchy.getRootFold(), foldTypes);
                if (collapse) {
                    hierarchy.collapse(foldList);
                } else {
                    hierarchy.expand(foldList);
                }
            } finally {
                hierarchy.unlock();
            }
        } finally {
            adoc.readUnlock();
        }
    }

    public static int findFoldStartIndex(Fold fold, int offset, boolean first) {
        int foldCount = fold.getFoldCount();
        int low = 0;
        int high = foldCount - 1;
        
        while (low <= high) {
            int mid = (low + high) / 2;
            Fold midFold = fold.getFold(mid);
            int midFoldStartOffset = midFold.getStartOffset();
            
            if (midFoldStartOffset < offset) {
                low = mid + 1;
            } else if (midFoldStartOffset > offset) {
                high = mid - 1;
            } else {
                // fold starting exactly at the given offset found
                if (first) { // search for first fold
                    mid--;
                    while (mid >= 0 && fold.getFold(mid).getStartOffset() == offset) {
                        mid--;
                    }
                    mid++;
                    
                } else { // search for last fold
                    mid++;
                    // Search for fold with startOffset greater than offset
                    while (mid < foldCount && fold.getFold(mid).getStartOffset() == offset) {
                        mid++;
                    }
                    mid--;
                }
                return mid;
            }
        }
        return high;
    }

    /**
     * Find a hint index of where a child fold should be inserted in its parent.
     *
     * @param fold fold into which the child fold should be inserted.
     * @param childStartOffset starting offset of the child to be inserted.
     * @return hint index at which the child fold should be inserted.
     *  <br>
     *  The client must additionally check whether the end offset
     *  of the preceding child fold does not overlap with the given child fold
     *  and if so then either remove the clashing fold or stop inserting
     *  the child fold.
     *  <br>
     *  The client must also check whether ending offset of the given child fold
     *  does not overlap with the starting offset of the following child fold.
     */
    public static int findFoldInsertIndex(Fold fold, int childStartOffset) {
        return findFoldStartIndex(fold, childStartOffset, false) + 1;
    }

    public static int findFoldEndIndex(Fold fold, int offset) {
        int foldCount = fold.getFoldCount();
        int low = 0;
        int high = foldCount - 1;
        
        while (low <= high) {
            int mid = (low + high) / 2;
            Fold midFold = fold.getFold(mid);
            int midFoldEndOffset = midFold.getEndOffset();
            
            if (midFoldEndOffset < offset) {
                low = mid + 1;
            } else if (midFoldEndOffset > offset) {
                high = mid - 1;
            } else {
                // fold ending exactly at the given offset found => move to next one above
                mid++;
                while (mid < foldCount && fold.getFold(mid).getEndOffset() <= offset) {
                    mid++;
                }
                return mid;
            }
        }
        return low;
    }

    public static List childrenAsList(Fold fold, int index, int count) {
        List l = new ArrayList(count);
        while (--count >= 0) {
            l.add(fold.getFold(index));
            index++;
        }
        return l;
    }

    public static List find(Fold fold, Collection foldTypes) {
        List l = new ArrayList();
        int foldCount = fold.getFoldCount();
        for (int i = 0; i < foldCount; i++) {
            Fold child = fold.getFold(i);
            if (foldTypes == null || foldTypes.contains(child.getType())) {
                l.add(child);
            }
        }
        return l;
    }
    
    public static List findRecursive(List l, Fold fold, Collection foldTypes) {
        if (l == null) {
            l = new ArrayList();
        }

        int foldCount = fold.getFoldCount();
        for (int i = 0; i < foldCount; i++) {
            Fold child = fold.getFold(i);
            if (foldTypes == null || foldTypes.contains(child.getType())) {
                l.add(child);
            }
            findRecursive(l, child, foldTypes);
        }
        return l;

    }
    
    /** Returns the fold at the specified offset. Returns null in case of root fold */
    public static Fold findOffsetFold(FoldHierarchy hierarchy, int offset) {
        int distance = Integer.MAX_VALUE;
        Fold rootFold = hierarchy.getRootFold();
        Fold fold = rootFold;
        
        boolean inspectNested = true;
        while (inspectNested) {
            int childIndex = findFoldStartIndex(fold, offset, false);
            if (childIndex >= 0) {
                Fold wrapFold = fold.getFold(childIndex);
                int startOffset = wrapFold.getStartOffset();
                int endOffset = wrapFold.getEndOffset();
                // This is not like containsOffset() because of "<= endOffset"
                if (startOffset <= offset && offset <= endOffset) {
                    fold = wrapFold;
                }else{
                    inspectNested = false;
                }
            } else { // no children => break
                inspectNested = false;
            }
        }
        return (fold != rootFold) ? fold : null;
    }
    
    public static Fold findNearestFold(FoldHierarchy hierarchy, int offset, int endOffset) {
        Fold nearestFold = null;
        int distance = Integer.MAX_VALUE;
        Fold fold = hierarchy.getRootFold();
        
        boolean inspectNested = true;
        while (inspectNested) {
            int childCount = fold.getFoldCount();
            int childIndex = findFoldEndIndex(fold, offset);
            if (childIndex < childCount) {
                Fold wrapOrAfterFold = fold.getFold(childIndex);
                int startOffset = wrapOrAfterFold.getStartOffset();
                if (startOffset >= endOffset) { // starts at or after endOffset
                    break;
                }

                Fold afterFold; // fold after the offset
                if (startOffset < offset) { // starts below offset
                    childIndex++;
                    afterFold = (childIndex < childCount) ? fold.getFold(childIndex) : null;
                    // leave inspectNested to be true and prepare fold variable
                    fold = wrapOrAfterFold;
                    
                } else { // starts above offset
                    afterFold = wrapOrAfterFold;
                    inspectNested = false;
                }
                
                // Check whether the afterFold is the nearest
                if (afterFold != null) {
                    int afterFoldDistance = afterFold.getStartOffset() - offset;
                    if (afterFoldDistance < distance) {
                        distance = afterFoldDistance;
                        nearestFold = afterFold;
                    }
                }
                
            } else { // no children => break
                inspectNested = false;
            }
        }
        
        return nearestFold;
    }
    
    public static Fold findFirstCollapsedFold(FoldHierarchy hierarchy,
    int startOffset, int endOffset) {
        
        Fold fold = hierarchy.getRootFold();
        Fold lastFold = null;
        int lastIndex = 0;
        while (true) {
            // Find fold covering the startOffset
            int index = findFoldEndIndex(fold, startOffset);
            if (index >= fold.getFoldCount()) {
                if (lastFold != null) {
                    return findCollapsedRec(lastFold, lastIndex + 1, endOffset);
                } else { // root level - no satisfying folds
                    return null;
                }
                
            } else { // fold index within bounds
                Fold childFold = fold.getFold(index);
                if (childFold.isCollapsed()) { // return it if it's collapsed
                    return childFold;
                }

                if (childFold.getStartOffset() >= startOffset) { // do not nest
                    return findCollapsedRec(fold, index, endOffset);
                } else { // need to inspect children
                    lastFold = fold;
                    lastIndex = index;
                    fold = childFold;
                }
            }
        }
    }

    public static Iterator collapsedFoldIterator(FoldHierarchy hierarchy, int startOffset, int endOffset) {
        return new CollapsedFoldIterator(
            findFirstCollapsedFold(hierarchy, startOffset, endOffset),
            endOffset
        );
    }
    
    private static final class CollapsedFoldIterator implements Iterator {
        
        private Fold nextFold;
        
        private int endOffset;
        
        public CollapsedFoldIterator(Fold nextFold, int endOffset) {
            this.nextFold = nextFold;
            this.endOffset = endOffset;
        }
        
        public boolean hasNext() {
            return (nextFold != null);
        }        
        
        public Object next() {
            Fold result = nextFold;
            nextFold = findNextCollapsedFold(nextFold, endOffset);
            return result;
        }
        
        public void remove() {
            throw new UnsupportedOperationException();
        }
        
    }
        
    public static Fold findNextCollapsedFold(Fold fold, int endOffset) {
        if (FoldUtilities.isRootFold(fold)) { // start from the begining
            return findCollapsedRec(fold, 0, endOffset);

        } else { // continue from valid fold
            Fold parent = fold.getParent();
            return findCollapsedRec(parent, parent.getFoldIndex(fold) + 1, endOffset);
        }
    }
    
    private static Fold findCollapsedRec(Fold fold,
    int startIndex, int endOffset) {
        return findCollapsedRec(fold, startIndex, endOffset, true);
    }    
    
    private static Fold findCollapsedRec(Fold fold,
    int startIndex, int endOffset, boolean findInUpperLevel) {

        if (fold.getStartOffset() > endOffset) {
            return null;
        }

        int foldCount = fold.getFoldCount();
        while (startIndex < foldCount) {
            Fold child = fold.getFold(startIndex);
            if (child.isCollapsed()) {
                return child;
            } else {
                Fold maybeCollapsed = findCollapsedRec(child, 0, endOffset, false);
                if (maybeCollapsed != null) {
                    return maybeCollapsed;
                }
            }
            startIndex++;
        }

        // No child was found collapsed -> go one level up
        if (FoldUtilities.isRootFold(fold) || !findInUpperLevel) {
            return null;
        } else { // not root fold
            Fold parent = fold.getParent();
            return findCollapsedRec(parent, parent.getFoldIndex(fold) + 1, endOffset, true);
        }
    }
    
    public static String foldToString(Fold fold) {
        return "[" + fold.getType() + "] " // NOI18N
            + (fold.isCollapsed() ? "C" : "E")// NOI18N
            + (FoldUtilities.isRootFold(fold) ? "" : Integer.toString(
                ApiPackageAccessor.get().foldGetOperation(fold).getPriority()))
            + " <" + fold.getStartOffset() // NOI18N
            + "," + fold.getEndOffset() + ">" // NOI18N
            + (FoldUtilities.isRootFold(fold) ? "" : (", desc='" + fold.getDescription() + "'")); // NOI18N
    }
    
    public static void appendSpaces(StringBuffer sb, int spaces) {
        while (--spaces >= 0) {
            sb.append(' ');
        }
    }

    public static String foldToStringChildren(Fold fold, int indent) {
        indent += 4;
        StringBuffer sb = new StringBuffer();
        sb.append(fold);
        sb.append('\n');
        int foldCount = fold.getFoldCount();
        for (int i = 0; i < foldCount; i++) {
            appendSpaces(sb, indent);
            sb.append('[');
            sb.append(i);
            sb.append("]: "); // NOI18N
            sb.append(foldToStringChildren(fold.getFold(i), indent));
        }
        
        return sb.toString();
    }
    
    public static String foldHierarchyEventToString(FoldHierarchyEvent evt) {
        StringBuffer sb = new StringBuffer();
        int removedFoldCount = evt.getRemovedFoldCount();
        for (int i = 0; i < removedFoldCount; i++) {
            sb.append("R["); // NOI18N
            sb.append(i);
            sb.append("]: "); // NOI18N
            sb.append(evt.getRemovedFold(i));
            sb.append('\n');
        }
        
        int addedFoldCount = evt.getAddedFoldCount();
        for (int i = 0; i < addedFoldCount; i++) {
            sb.append("A["); // NOI18N
            sb.append(i);
            sb.append("]: "); // NOI18N
            sb.append(evt.getAddedFold(i));
            sb.append('\n');
        }
        
        int foldStateChangeCount = evt.getFoldStateChangeCount();
        for (int i = 0; i < foldStateChangeCount; i++) {
            FoldStateChange change = evt.getFoldStateChange(i);
            sb.append("SC["); // NOI18N
            sb.append(i);
            sb.append("]: "); // NOI18N
            sb.append(change);
            sb.append('\n');
        }
        if (foldStateChangeCount == 0) {
            sb.append("No FoldStateChange\n"); // NOI18N
        }
        
        sb.append("affected: <"); // NOI18N
        sb.append(evt.getAffectedStartOffset());
        sb.append(","); // NOI18N
        sb.append(evt.getAffectedEndOffset());
        sb.append(">\n"); // NOI18N
        
        return sb.toString();
    }
    
    public static String foldStateChangeToString(FoldStateChange change) {
        StringBuffer sb = new StringBuffer();
        if (change.isCollapsedChanged()) {
            sb.append("C"); // NOI18N
        }
        if (change.isDescriptionChanged()) {
            sb.append("D"); // NOI18N
        }
        if (change.isEndOffsetChanged()) {
            sb.append("E"); // NOI18N
        }
        sb.append(" fold="); // NOI18N
        sb.append(change.getFold());
        return sb.toString();
    }
    
}
