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


import org.netbeans.core.windows.ModeImpl;
import org.netbeans.core.windows.ModeStructureSnapshot;
import org.netbeans.core.windows.ModeStructureSnapshot.ElementSnapshot;
import org.netbeans.core.windows.model.ModelElement;
import org.openide.windows.TopComponent;

import java.awt.*;
import java.util.Iterator;
import java.util.Set;


/**
 * Used to pass information of modes model state to view in nice fashion.
 * Need to figure out to which package this class belongs, temporary here.
 *
 * @author  Peter Zavadsky
 */
final class ModeStructureAccessorImpl implements ModeStructureAccessor {
    
    private final ElementAccessor splitRootAccessor;
    
    private final Set separateModeAccessors;
    
    private final Set slidingModeAccessors;
    
    /** Creates a new instance of ModesModelAccessorImpl. */
    public ModeStructureAccessorImpl(ElementAccessor splitRootAccessor, Set separateModeAccessors, Set slidingModeAccessors) {
        this.splitRootAccessor = splitRootAccessor;
        this.separateModeAccessors = separateModeAccessors;
        this.slidingModeAccessors = slidingModeAccessors;
    }

    public ElementAccessor getSplitRootAccessor() {
        return splitRootAccessor;
    }
    
    public ModeAccessor[] getSeparateModeAccessors() {
        return (ModeAccessor[])separateModeAccessors.toArray(new ModeAccessor[0]);
    }
    
    public SlidingAccessor[] getSlidingModeAccessors() {
        return (SlidingAccessor[])slidingModeAccessors.toArray(new SlidingAccessor[0]);
    }

    /** @param name name of mode */
    public ModeAccessor findModeAccessor(String name) {
        ModeAccessor ma = findModeAccessorOfName(splitRootAccessor, name);
        if(ma != null) {
            return ma;
        }
        
        for(Iterator it = separateModeAccessors.iterator(); it.hasNext(); ) {
            ma = (ModeAccessor)it.next();
            if(name.equals(ma.getName())) {
                return ma;
            }
        }
        
        for(Iterator it = slidingModeAccessors.iterator(); it.hasNext(); ) {
            ma = (ModeAccessor)it.next();
            if(name.equals(ma.getName())) {
                return ma;
            }
        }
        
        return null;
    }
    
    private static ModeAccessor findModeAccessorOfName(ElementAccessor accessor, String name) {
        if(accessor instanceof ModeAccessor) {
            ModeAccessor ma = (ModeAccessor)accessor;
            if(name.equals(ma.getName())) {
                return ma;
            }
        } else if(accessor instanceof SplitAccessor) {
            SplitAccessor split = (SplitAccessor)accessor; 
            ModeAccessor ma = findModeAccessorOfName(split.getFirst(), name);
            if(ma != null) {
                return ma;
            }
            ma = findModeAccessorOfName(split.getSecond(), name);
            if(ma != null) {
                return ma;
            }
        } else if(accessor instanceof EditorAccessor) {
            EditorAccessor editorAccessor = (EditorAccessor)accessor;
            ModeAccessor ma = findModeAccessorOfName(editorAccessor.getEditorAreaAccessor(), name);
            if(ma != null) {
                return ma;
            }
        }
        
        return null;
    }
    

    /** Superclass for accessor of model element.
     * There are three types, split, mode, and editor (represents editor area) type. */
    static abstract class ElementAccessorImpl implements ElementAccessor {
        // PENDING revise
        /** Corresponding object in model (SplitNode or ModeNode for separate mode). */
        private final ModelElement originator;
        /** Corresponding snapshot. */
        private final ModeStructureSnapshot.ElementSnapshot snapshot;
        
        
        public ElementAccessorImpl(ModelElement originator, ModeStructureSnapshot.ElementSnapshot snapshot) {
            this.originator = originator;
            this.snapshot = snapshot;
        }

        /** Gets originator object. Used only in model. */
        public final ModelElement getOriginator() {
            return originator;
        }
        
        public final ModeStructureSnapshot.ElementSnapshot getSnapshot() {
            return snapshot;
        }
        
        public boolean originatorEquals(ElementAccessor o) {
            if(o instanceof ElementAccessorImpl) {
                return getClass().equals(o.getClass()) // To prevent mismatch between split and mode accessor.
                                                       // Split has now originator corresponding to first child.
                    && ((ElementAccessorImpl)o).originator == originator;
            }
            return false;
        }
        
        public String toString() {
            return super.toString() + "[originatorHash=" + (originator != null ? Integer.toHexString(originator.hashCode()) : "null") + "]"; // NOI18N
        }
    }
    
    /** */ 
    static final class SplitAccessorImpl extends ElementAccessorImpl implements SplitAccessor {
        private final int orientation;
        private final double splitPosition; // relative
        private final ElementAccessor first;
        private final ElementAccessor second;
        private final double resizeWeight;
        
        public SplitAccessorImpl(ModelElement originator, ElementSnapshot snapshot,
        int orientation, double splitPosition,
        ElementAccessor first, ElementAccessor second, double resizeWeight) {
            super(originator, snapshot); // It correspond to the first child model element.
            
            this.orientation = orientation;
            this.splitPosition = splitPosition;
            this.first = first;
            this.second = second;
            this.resizeWeight = resizeWeight;
        }

        public int getOrientation() {
            return orientation;
        }
        
        public double getSplitPosition() {
            return splitPosition;
        }
        
        public ElementAccessor getFirst() {
            return first;
        }
        
        public ElementAccessor getSecond() {
            return second;
        }
        
        public double getResizeWeight() {
            return resizeWeight;
        }
        
        public String toString() {
            return super.toString() + "[orientation=" + orientation // NOI18N
                + ", splitPosition=" + splitPosition + "]"; // NOI18N
        }
       
    }

    /** */
    static class ModeAccessorImpl extends ElementAccessorImpl implements ModeAccessor { 
        private final ModeImpl mode;
        
        private final String name;
        private final int state;
        private final int kind;
        private final Rectangle bounds;
        private final int frameState;
        private final TopComponent selectedTopComponent;
        private final TopComponent[] openedTopComponents;
        private final double resizeWeight;
        
        public ModeAccessorImpl(ModelElement originator, ElementSnapshot snapshot,
            ModeImpl mode,
            String name,
            int state,
            int kind,
            Rectangle bounds,
            int frameState,
            TopComponent selectedTopComponent,
            TopComponent[] openedTopComponents,
            double resizeWeight
        ) {
            super(originator, snapshot);

            this.mode = mode;
            
            this.name = name;
            this.state = state;
            this.kind = kind;
            this.bounds = bounds;
            this.frameState = frameState;
            this.selectedTopComponent = selectedTopComponent;
            this.openedTopComponents = openedTopComponents;
            this.resizeWeight = resizeWeight;
        }
        
        
        public boolean originatorEquals(ElementAccessor o) {
            if(!super.originatorEquals(o)) {
                return false;
            }
            
            // XXX Even if originators are same, they differ if their states are different.
            // Difference -> split vs. separate representations.
            ModeAccessor me = (ModeAccessor)o;
            return getState() == me.getState();
        }
        
        public ModeImpl getMode() {
            return mode;
        }
        
        public String getName() {
            return name;
        }

        public int getState() {
            return state;
        }

        public int getKind() {
            return kind;
        }

        public Rectangle getBounds() {
            return bounds;
        }

        public int getFrameState() {
            return frameState;
        }

        public TopComponent getSelectedTopComponent() {
            return selectedTopComponent;
        }

        public TopComponent[] getOpenedTopComponents() {
            return openedTopComponents;
        }
        
        public double getResizeWeight() {
            return resizeWeight;
        }
        
        public String toString() {
            return super.toString() + "[name=" + name + " ]"; // NOI18N
        }

    }

    /** Data accessor for sliding view */
    static final class SlidingAccessorImpl extends ModeAccessorImpl implements SlidingAccessor { 

        private final String side;
        
        public SlidingAccessorImpl(ModelElement originator, ElementSnapshot snapshot,
            ModeImpl mode,
            String name,
            int state,
            int kind,
            Rectangle bounds,
            int frameState,
            TopComponent selectedTopComponent,
            TopComponent[] openedTopComponents,
            double resizeWeight,
            String side
        ) {
            super(originator, snapshot, mode, name, state, kind, bounds, frameState,
                  selectedTopComponent, openedTopComponents, resizeWeight);

            this.side = side;
        }
    
        public String getSide() {
            return side;
        }
        
        public boolean originatorEquals(ElementAccessor o) {
            if(!super.originatorEquals(o)) {
                return false;
            }
            
            // XXX Even if originators are same, they differ if their side are different.
            SlidingAccessor me = (SlidingAccessor)o;
            return getSide() == me.getSide();
        }
        
    } // end of SlidingAccessorImpl
        
    
    /** */
    static final class EditorAccessorImpl extends ElementAccessorImpl implements EditorAccessor {
        private final ElementAccessor editorAreaAccessor;
        private final double resizeWeight;
        
        public EditorAccessorImpl(ModelElement originator, ElementSnapshot snapshot,
        ElementAccessor editorAreaAccessor, double resizeWeight) {
            super(originator, snapshot);
            
            this.editorAreaAccessor = editorAreaAccessor;
            this.resizeWeight = resizeWeight;
        }
        
        public double getResizeWeight() {
            return resizeWeight;
        }
        
        public ElementAccessor getEditorAreaAccessor() {
            return editorAreaAccessor;
        }
        
        public String toString() {
            return super.toString() + "\n" + editorAreaAccessor; // NOI18N
        }
    }

    
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("\nModesAccessorImpl hashCode=" + hashCode()); // NOI18N
        sb.append("\nSplit modes:\n"); // NOI18N
        sb.append(dumpAccessor(splitRootAccessor, 0)); 
        sb.append("\nSeparate Modes:"); // NOI18N
        sb.append(dumpSet(separateModeAccessors));
        return sb.toString();
    }
    
    private static String dumpAccessor(ElementAccessor accessor, int indent) {
        StringBuffer sb = new StringBuffer();
        String indentString = createIndentString(indent);
        
        if(accessor instanceof SplitAccessor) {
            SplitAccessor splitAccessor = (SplitAccessor)accessor;
            sb.append(indentString + "split="+splitAccessor); // NOI18N
            indent++;
            sb.append("\n" + dumpAccessor(splitAccessor.getFirst(), indent)); // NOI18N
            sb.append("\n" + dumpAccessor(splitAccessor.getSecond(), indent)); // NOI18N
        } else if(accessor instanceof ModeAccessor) {
            sb.append(indentString + "mode=" + accessor); // NOI18N
        } else if(accessor instanceof EditorAccessor) {
            sb.append(indentString + "editor=" + accessor); // NOI18N
            sb.append(dumpAccessor(((EditorAccessor)accessor).getEditorAreaAccessor(), ++indent));
        }
        
        return sb.toString();
    }
    
    private static String createIndentString(int indent) {
        StringBuffer sb = new StringBuffer(indent);
        for(int i = 0; i < indent; i++) {
            sb.append(' ');
        }
        
        return sb.toString();
    }
    
    private static String dumpSet(Set separateModes) {
        StringBuffer sb = new StringBuffer();
        
        for(java.util.Iterator it = separateModes.iterator(); it.hasNext(); ) {
            sb.append("\nmode=" + it.next());
        }
        
        return sb.toString();
    }
    
    
}

