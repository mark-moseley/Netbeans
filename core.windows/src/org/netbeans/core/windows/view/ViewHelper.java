/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.core.windows.view;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.netbeans.core.windows.Debug;
import org.netbeans.core.windows.ModeStructureSnapshot;
import org.netbeans.core.windows.ModeStructureSnapshot.ElementSnapshot;
import org.netbeans.core.windows.WindowSystemSnapshot;


/**
 * This class converts snapshot to accessor structure, which is a 'model'
 * of view (GUI) structure window system has to display to user.
 * It reflects the specific view implementation (the difference from snapshot)
 * e.g. the nesting splitted panes, which imitates (yet nonexisiting) multi-split
 * component and also contains only visible elements in that structure.
 * It also provides computing of split weights.
 *
 * @author  Peter Zavadsky
 */
final class ViewHelper {
    
    /** Debugging flag. */
    private static boolean DEBUG = Debug.isLoggable(ViewHelper.class);
    
    
    /** Creates a new instance of ViewHelper */
    private ViewHelper() {
    }
    
    
    public static WindowSystemAccessor createWindowSystemAccessor(
        WindowSystemSnapshot wss
    ) {
        // PENDING When hiding is null.
        if(wss == null) {
            return null;
        }
        
        WindowSystemAccessorImpl wsa = new WindowSystemAccessorImpl();

        ModeStructureAccessorImpl msa = createModeStructureAccessor(wss.getModeStructureSnapshot());
        wsa.setModeStructureAccessor(msa);

        ModeStructureSnapshot.ModeSnapshot activeSnapshot = wss.getActiveModeSnapshot();
        wsa.setActiveModeAccessor(activeSnapshot == null ? null : msa.findModeAccessor(activeSnapshot.getName()));
        
        ModeStructureSnapshot.ModeSnapshot maximizedSnapshot = wss.getMaximizedModeSnapshot();
        wsa.setMaximizedModeAccessor(maximizedSnapshot == null ? null : msa.findModeAccessor(maximizedSnapshot.getName()));

        wsa.setMainWindowBoundsJoined(wss.getMainWindowBoundsJoined());
        wsa.setMainWindowBoundsSeparated(wss.getMainWindowBoundsSeparated());
        wsa.setEditorAreaBounds(wss.getEditorAreaBounds());
        wsa.setEditorAreaState(wss.getEditorAreaState());
        wsa.setMainWindowFrameStateJoined(wss.getMainWindowFrameStateJoined());
        wsa.setMainWindowFrameStateSeparated(wss.getMainWindowFrameStateSeparated());
        wsa.setToolbarConfigurationName(wss.getToolbarConfigurationName());
        wsa.setProjectName(wss.getProjectName());
        return wsa;
    }
    
    private static ModeStructureAccessorImpl createModeStructureAccessor(ModeStructureSnapshot mss) {
        ElementAccessor splitRoot = createVisibleAccessor(mss.getSplitRootSnapshot());
        Set separateModes = createSeparateModeAccessors(mss.getSeparateModeSnapshots());
        
        ModeStructureAccessorImpl msa =  new ModeStructureAccessorImpl(splitRoot, separateModes);
        return msa;
    }
    
    private static Set createSeparateModeAccessors(ModeStructureSnapshot.ModeSnapshot[] separateModeSnapshots) {
        Set s = new HashSet();
        for(int i = 0; i < separateModeSnapshots.length; i++) {
            ModeStructureSnapshot.ModeSnapshot snapshot = separateModeSnapshots[i];
            if(snapshot.isVisibleSeparate()) {
                s.add(new ModeStructureAccessorImpl.ModeAccessorImpl(
                    snapshot.getOriginator(),
                    snapshot,
                    snapshot.getMode(),
                    snapshot.getName(),
                    snapshot.getState(),
                    snapshot.getKind(),
                    snapshot.getBounds(),
                    snapshot.getFrameState(),
                    snapshot.getSelectedTopComponent(),
                    snapshot.getOpenedTopComponents(),
                    snapshot.getResizeWeight()));
            }
        }
        
        return s;
    }

    /** */
    private static ElementAccessor createVisibleAccessor(ModeStructureSnapshot.ElementSnapshot snapshot) {
        if(snapshot == null) {
            return null;
        }

        if(snapshot instanceof ModeStructureSnapshot.EditorSnapshot) { // Is always visible.
            ModeStructureSnapshot.EditorSnapshot editorSnapshot = (ModeStructureSnapshot.EditorSnapshot)snapshot;
            return new ModeStructureAccessorImpl.EditorAccessorImpl(
                editorSnapshot.getOriginator(),
                editorSnapshot,
                createVisibleAccessor(editorSnapshot.getEditorAreaSnapshot()),
                editorSnapshot.getResizeWeight());
        }
        
        if(snapshot.isVisibleInSplit()) {
            if(snapshot instanceof ModeStructureSnapshot.SplitSnapshot) {
                ModeStructureSnapshot.SplitSnapshot splitSnapshot = (ModeStructureSnapshot.SplitSnapshot)snapshot;
                return createSplitAccessor(splitSnapshot);
            } else if(snapshot instanceof ModeStructureSnapshot.ModeSnapshot) {
                ModeStructureSnapshot.ModeSnapshot modeSnapshot = (ModeStructureSnapshot.ModeSnapshot)snapshot;
                return new ModeStructureAccessorImpl.ModeAccessorImpl(
                    modeSnapshot.getOriginator(),
                    modeSnapshot,
                    modeSnapshot.getMode(),
                    modeSnapshot.getName(),
                    modeSnapshot.getState(),
                    modeSnapshot.getKind(),
                    modeSnapshot.getBounds(),
                    modeSnapshot.getFrameState(),
                    modeSnapshot.getSelectedTopComponent(),
                    modeSnapshot.getOpenedTopComponents(),
                    modeSnapshot.getResizeWeight());
            }
        } else {
            if(snapshot instanceof ModeStructureSnapshot.SplitSnapshot) {
                ModeStructureSnapshot.SplitSnapshot splitSnapshot = (ModeStructureSnapshot.SplitSnapshot)snapshot;
                for(Iterator it = splitSnapshot.getChildSnapshots().iterator(); it.hasNext(); ) {
                    ModeStructureSnapshot.ElementSnapshot child = (ModeStructureSnapshot.ElementSnapshot)it.next();
                    if(child.hasVisibleDescendant()) {
                        return createVisibleAccessor(child);
                    }
                }
            }
        }
        
        return null;
    }
    
    private static ElementAccessor createSplitAccessor(ModeStructureSnapshot.SplitSnapshot splitSnapshot) {
        List visibleChildren = splitSnapshot.getVisibleChildSnapshots();
        // Prepare how to distribute the rest of weights.
        double visibleResizeWeights = 0D;
        for(Iterator it = visibleChildren.iterator(); it.hasNext(); ) {
            ModeStructureSnapshot.ElementSnapshot next = (ModeStructureSnapshot.ElementSnapshot)it.next();
            visibleResizeWeights += next.getResizeWeight();
        }
        
        List invisibleChildren = splitSnapshot.getChildSnapshots();
        invisibleChildren.removeAll(visibleChildren);
        double invisibleWeights = 0D;
        for(Iterator it = invisibleChildren.iterator(); it.hasNext(); ) {
            ModeStructureSnapshot.ElementSnapshot next = (ModeStructureSnapshot.ElementSnapshot)it.next();
            invisibleWeights += splitSnapshot.getChildSnapshotSplitWeight(next);
        }
        
        // Get the refined weights to work with.
        Map visibleChild2refinedWeight = new HashMap();
        for(Iterator it = visibleChildren.iterator(); it.hasNext(); ) {
            ModeStructureSnapshot.ElementSnapshot next = (ModeStructureSnapshot.ElementSnapshot)it.next();
            double refinedWeight;
            if(visibleResizeWeights > 0D) {
                refinedWeight = splitSnapshot.getChildSnapshotSplitWeight(next)
                    + ((next.getResizeWeight() / visibleResizeWeights) * invisibleWeights);
            } else {
                refinedWeight = splitSnapshot.getChildSnapshotSplitWeight(next);
            }
            
            visibleChild2refinedWeight.put(next, new Double(refinedWeight));
        }
        
        // Begin from the end.
        // I.e. the splits are always nested the way,
        // the one at the LEFT (or TOP) side is the top level one.
        int orientation = splitSnapshot.getOrientation();
        // Group the split children into SplitSnapshots (-> corresponding to JSplitPanes)
        SplitAccessor se = null;
        List reversedVisibleChildren = new ArrayList(visibleChildren);
        Collections.reverse(reversedVisibleChildren);
        for(Iterator it = reversedVisibleChildren.iterator(); it.hasNext(); ) {
            ElementAccessor secondAccessor;
            if(se == null) {
                ModeStructureSnapshot.ElementSnapshot second = (ModeStructureSnapshot.ElementSnapshot)it.next();
                secondAccessor = createVisibleAccessor(second);
            } else {
                // There is nested split add that one to second place.
                secondAccessor = se;
            }

            if(!it.hasNext()) {
                // No other element present.
                return secondAccessor;
            }

            // Get first element.
            ModeStructureSnapshot.ElementSnapshot first = (ModeStructureSnapshot.ElementSnapshot)it.next();
            ElementAccessor firstAccessor = createVisibleAccessor(first);

            double firstSplitWeight = ((Double)visibleChild2refinedWeight.get(first)).doubleValue();
            
            // Find nextVisible weights.
            double nextVisibleWeights = 0D;
            List anotherReversedChildren = new ArrayList(visibleChildren);
            Collections.reverse(anotherReversedChildren);
            for(Iterator it2 = anotherReversedChildren.iterator(); it2.hasNext(); ) {
                ModeStructureSnapshot.ElementSnapshot next = (ModeStructureSnapshot.ElementSnapshot)it2.next();
                if(next == first) {
                    break;
                }
                nextVisibleWeights += ((Double)visibleChild2refinedWeight.get(next)).doubleValue();
            }
            
            if(DEBUG) {
                debugLog(""); // NOI18N
                debugLog("Computing split"); // NOI18N
                debugLog("firstSplitWeight=" + firstSplitWeight); // NOI18N
                debugLog("nextVisibleWeigths=" + nextVisibleWeights); // NOI18N
            }
            
            // Compute split position.
            double splitPosition = firstSplitWeight/(firstSplitWeight + nextVisibleWeights);
            if(DEBUG) {
                debugLog("splitPosition=" + splitPosition); // NOI18N
            }

            se = new ModeStructureAccessorImpl.SplitAccessorImpl(
                first.getOriginator(), splitSnapshot, orientation, splitPosition, firstAccessor, secondAccessor, splitSnapshot.getResizeWeight());
        }

        return se;
    }

    
    public static boolean computeSplitWeights(double location, SplitAccessor splitAccessor,
    ElementAccessor firstAccessor, ElementAccessor secondAccessor, ControllerHandler controllerHandler) {
        ModeStructureSnapshot.SplitSnapshot splitSnapshot = (ModeStructureSnapshot.SplitSnapshot)splitAccessor.getSnapshot();
        if(splitSnapshot == null) {
            return false;
        }
        
        ElementSnapshot first = firstAccessor.getSnapshot();
        ElementSnapshot second = secondAccessor.getSnapshot();

        List visibleChildren = splitSnapshot.getVisibleChildSnapshots();
        
        // XXX #36696 If it is 'nested-split' find the real element.
        if(first == splitSnapshot) {
            first = ((SplitAccessor)firstAccessor).getFirst().getSnapshot();
        }
        
        // Find the corresponding nodes in the split.
        while(first != null && !visibleChildren.contains(first)) {
            first = first.getParent();
        }
        if(first == null) {
            // Is not in this split.
            return false;
        }

        // XXX #36696 If it is 'nested-split' find the real element.
        if(second == splitSnapshot) {
            second = ((SplitAccessor)secondAccessor).getFirst().getSnapshot();
        }

        while(second != null && !visibleChildren.contains(second)) {
            second = second.getParent();
        }
        if(second == null) {
            // Is not in this split.
            return false;
        }
        
        // Validation finished, do the update.

        // Prepare how to distribute the rest of weights.
        double visibleResizeWeights = 0D;
        for(Iterator it = visibleChildren.iterator(); it.hasNext(); ) {
            ElementSnapshot snapshot = (ElementSnapshot)it.next();
            visibleResizeWeights += snapshot.getResizeWeight();
        }
        
        List invisibleChildren = splitSnapshot.getChildSnapshots();
        invisibleChildren.removeAll(visibleChildren);
        double invisibleWeights = 0D;
        for(Iterator it = invisibleChildren.iterator(); it.hasNext(); ) {
            ElementSnapshot snapshot = (ElementSnapshot)it.next();
            invisibleWeights += splitSnapshot.getChildSnapshotSplitWeight(snapshot);
        }
        
        // Get the refined weights to work with.
        Map visibleChild2refinedWeight = new HashMap();
        for(Iterator it = visibleChildren.iterator(); it.hasNext(); ) {
            ElementSnapshot snapshot = (ElementSnapshot)it.next();
            double refinedWeight;
            if(visibleResizeWeights > 0D) {
                refinedWeight = splitSnapshot.getChildSnapshotSplitWeight(snapshot) + ((snapshot.getResizeWeight() / visibleResizeWeights) * invisibleWeights);
            } else {
                refinedWeight = splitSnapshot.getChildSnapshotSplitWeight(snapshot);
            }
            visibleChild2refinedWeight.put(snapshot, new Double(refinedWeight));
        }
        
        double firstWeight = ((Double)visibleChild2refinedWeight.get(first)).doubleValue();
        double secondWeight = ((Double)visibleChild2refinedWeight.get(second)).doubleValue();

        // Find and nextAll weights.
        double nextVisibleWeights = 0D;
        List anotherReversedChildren = new ArrayList(visibleChildren);
        Collections.reverse(anotherReversedChildren);
        for(Iterator it = anotherReversedChildren.iterator(); it.hasNext(); ) {
            ElementSnapshot snapshot = (ElementSnapshot)it.next();
            if(snapshot == first) {
                break;
            }
            nextVisibleWeights += ((Double)visibleChild2refinedWeight.get(snapshot)).doubleValue();
        }
        
        if(DEBUG) {
            debugLog(""); // NOI18N
            debugLog("location=" + location); // NOI18N
            debugLog("first=" + first); // NOI18N
            debugLog("second=" + second); // NOI18N
            debugLog("1st original=" + firstWeight); // NOI18N
            debugLog("2nd original=" + secondWeight); // NOI18N
            debugLog("nextAllWeights=" + nextVisibleWeights); // NOI18N
        }

        // What value has to be added from second to first weight
        // (if it is vice versa the value has minus sign).
        double delta = location * (firstWeight + nextVisibleWeights) - firstWeight;

        if(DEBUG) {
            debugLog(""); // NOI18N
            debugLog("delta=" + delta); // NOI18N
        }
        
        double littleSum = firstWeight + secondWeight;
        
        firstWeight += delta;
        secondWeight -= delta;
        
        if(DEBUG) {
            debugLog("1st after=" + firstWeight); // NOI18N
            debugLog("2nd after=" + secondWeight); // NOI18N
        }
        
        // Substract the invisible weights.
        if(visibleResizeWeights > 0D) {
            firstWeight = firstWeight - ((first.getResizeWeight() / visibleResizeWeights) * invisibleWeights);
            secondWeight = secondWeight - ((second.getResizeWeight() / visibleResizeWeights) * invisibleWeights);
        }
        
        if(DEBUG) {
            debugLog("1st after validation=" + firstWeight + ",\t originator=" + first.getOriginator()); // NOI18N
            debugLog("2nd after validation=" + secondWeight + ",\t originator=" + second.getOriginator()); // NOI18N
        }
        
        controllerHandler.userChangedSplit(first.getOriginator(), firstWeight, second.getOriginator(), secondWeight);
        
        return true;
    }

    
    private static void debugLog(String message) {
        Debug.log(ViewHelper.class, message);
    }

}

