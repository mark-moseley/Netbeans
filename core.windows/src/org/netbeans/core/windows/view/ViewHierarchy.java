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


import org.netbeans.core.windows.Constants;
import org.netbeans.core.windows.view.dnd.WindowDnDManager;
import org.netbeans.core.windows.view.ui.DesktopImpl;
import org.netbeans.core.windows.view.ui.EditorAreaFrame;
import org.netbeans.core.windows.view.ui.MainWindow;
import org.netbeans.core.windows.view.ui.slides.SlideOperation;
import org.openide.ErrorManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

/**
 * Class which manages GUI components.
 *
 * @author  Peter Zavadsky
 */
final class ViewHierarchy {

    /** Observes user changes to view hierarchy. */
    private final Controller controller;
    
    private final WindowDnDManager windowDnDManager;

    /** desktop component maintainer */
    private DesktopImpl desktop = new DesktopImpl();
    /** Map of separate mode views (view <-> accessor). */
    private final Map separateModeViews = new HashMap(10);
    /** Map of sliding mode views (view <-> accessor) */
    private final Map slidingModeViews = new HashMap(10);
    
    /** Component in which is editor area, when the editor state is separated. */
    private EditorAreaFrame editorAreaFrame;

    /** Active mode view. */
    private ModeView activeModeView;
    /** Maximized mode view. */
    private ModeView maximizedModeView;
    private ViewElement currentSplitRoot;
    
    /** */
    private final Map accessor2view = new HashMap(10);
    /** */
    private final Map view2accessor = new HashMap(10);
    
    private final MainWindow mainWindow = new MainWindow();

    private final MainWindowListener mainWindowListener;
    
    
    
    /** Creates a new instance of ViewHierarchy. */
    public ViewHierarchy(Controller controller, WindowDnDManager windowDnDManager) {
        this.controller = controller;
        this.windowDnDManager = windowDnDManager;
        
        this.mainWindowListener = new MainWindowListener(controller, this);
    }
    

    public boolean isDragInProgress() {
        return windowDnDManager.isDragging();
    }
    
    public MainWindow getMainWindow() {
        return mainWindow;
    }
    
    public void installMainWindowListeners() {
        mainWindow.addComponentListener(mainWindowListener);
        mainWindow.addWindowStateListener(mainWindowListener);
    }
    
    public void uninstallMainWindowListeners() {
        mainWindow.removeComponentListener(mainWindowListener);
        mainWindow.removeWindowStateListener(mainWindowListener);
    }
    
    /** Updates the view hierarchy according to new structure. */
    public void updateViewHierarchy(ModeStructureAccessor modeStructureAccessor, 
        boolean addingAllowed) {
        updateAccessors(modeStructureAccessor);
        currentSplitRoot = updateViewForAccessor(modeStructureAccessor.getSplitRootAccessor(), addingAllowed);
//        System.out.println("updateViewHierarchy... elem=" + elem);
//        if (maximizedModeView == null) {
////            System.out.println("updateViewHierarchy...splitoroot=" + elem);
//            setSplitRootIntoDesktop(elem);
//        } else {
////            System.out.println("updateViewHierarchy...mazimized=" + maximizedModeView);
//            setMaximizedViewIntoDesktop(maximizedModeView);
//        }
        if (desktop.getSplitRoot() == null) {
            setSplitRootIntoDesktop(currentSplitRoot);
        }
        updateSeparateViews(modeStructureAccessor.getSeparateModeAccessors());
        updateSlidingViews(modeStructureAccessor.getSlidingModeAccessors());
    }
    
    /** Puts new instances of accessors in and reuses the old relevant views. */
    public void updateAccessors(ModeStructureAccessor modeStructureAccessor) {
        Map a2v = new HashMap(accessor2view);
        
        accessor2view.clear();
        view2accessor.clear();

        Set accessors  = getAllAccessorsForTree(modeStructureAccessor.getSplitRootAccessor());
        accessors.addAll(Arrays.asList(modeStructureAccessor.getSeparateModeAccessors()));
        accessors.addAll(Arrays.asList(modeStructureAccessor.getSlidingModeAccessors()));
        
        for(Iterator it = accessors.iterator(); it.hasNext(); ) {
            ElementAccessor accessor = (ElementAccessor)it.next();
            ElementAccessor similar = findSimilarAccessor(accessor, a2v);
            if(similar != null) {
                Object view = a2v.get(similar);
                accessor2view.put(accessor, view);
                view2accessor.put(view, accessor);
            }
        }
    }
    
    private Set getAllAccessorsForTree(ElementAccessor accessor) {
        Set s = new HashSet();
        if(accessor instanceof ModeAccessor) {
            s.add(accessor);
        } else if(accessor instanceof SplitAccessor) {
            SplitAccessor sa = (SplitAccessor)accessor;
            s.add(sa);
            s.addAll(getAllAccessorsForTree(sa.getFirst()));
            s.addAll(getAllAccessorsForTree(sa.getSecond()));
        } else if(accessor instanceof EditorAccessor) {
            EditorAccessor ea = (EditorAccessor)accessor;
            s.add(ea);
            s.addAll(getAllAccessorsForTree(ea.getEditorAreaAccessor()));
        }
        
        return s;
    }
    
    private ElementAccessor findSimilarAccessor(ElementAccessor accessor, Map a2v) {
        for(Iterator it = a2v.keySet().iterator(); it.hasNext(); ) {
            ElementAccessor next = (ElementAccessor)it.next();
            if(accessor.originatorEquals(next)) {
                return next;
            }
        }
        
        return null;
    }

    
    private ViewElement updateViewForAccessor(ElementAccessor patternAccessor, boolean addingAllowed) {
        if(patternAccessor == null) {
            return null;
        }
        
        ViewElement view = (ViewElement)accessor2view.get(patternAccessor);
        
        if(view != null) {
            if(patternAccessor instanceof SplitAccessor) {
                SplitAccessor sa = (SplitAccessor)patternAccessor;
                SplitView sv = (SplitView)view;
                sv.setOrientation(sa.getOrientation());
                sv.setLocation(sa.getSplitPosition());
                sv.setFirst(updateViewForAccessor(sa.getFirst(), addingAllowed));
                sv.setSecond(updateViewForAccessor(sa.getSecond(), addingAllowed));
                return sv;
            } else if(patternAccessor instanceof EditorAccessor) {
                EditorAccessor ea = (EditorAccessor)patternAccessor;
                EditorView ev = (EditorView)view;
                ev.setEditorArea(updateViewForAccessor(ea.getEditorAreaAccessor(), addingAllowed), addingAllowed);
                return ev;
            } else if(patternAccessor instanceof SlidingAccessor) {
                SlidingAccessor sa = (SlidingAccessor)patternAccessor;
                SlidingView sv = (SlidingView)view;
                sv.setTopComponents(sa.getOpenedTopComponents(), sa.getSelectedTopComponent());
                return sv;
            } else if(patternAccessor instanceof ModeAccessor) {
                // It is a ModeView.
                ModeAccessor ma = (ModeAccessor)patternAccessor;
                ModeView mv = (ModeView)view;
                mv.setTopComponents(ma.getOpenedTopComponents(), ma.getSelectedTopComponent());
                if(ma.getState() == Constants.MODE_STATE_SEPARATED) {
                    mv.setFrameState(ma.getFrameState());
                }
                return mv;
            }
        } else {
            if(patternAccessor instanceof SplitAccessor) {
                SplitAccessor sa = (SplitAccessor)patternAccessor;
                ViewElement first = updateViewForAccessor(sa.getFirst(), addingAllowed);
                ViewElement second = updateViewForAccessor(sa.getSecond(), addingAllowed);
                SplitView sv = new SplitView(controller, sa.getResizeWeight(),
                    sa.getOrientation(), sa.getSplitPosition(), first, second);
                accessor2view.put(patternAccessor, sv);
                view2accessor.put(sv, patternAccessor);
                return sv;
            } else if(patternAccessor instanceof SlidingAccessor) {
                SlidingAccessor sa = (SlidingAccessor)patternAccessor;
                SlidingView sv = new SlidingView(controller, windowDnDManager, 
                            sa.getOpenedTopComponents(), sa.getSelectedTopComponent(),
                            sa.getSide());
                accessor2view.put(patternAccessor, sv);
                view2accessor.put(sv, patternAccessor);
                return sv;
            } else if(patternAccessor instanceof ModeAccessor) {
                ModeAccessor ma = (ModeAccessor)patternAccessor;
                ModeView mv;
                if(ma.getState() == Constants.MODE_STATE_JOINED) {
                    mv = new ModeView(controller, windowDnDManager, ma.getResizeWeight(), ma.getKind(), 
                            ma.getOpenedTopComponents(), ma.getSelectedTopComponent());
                } else {
                    mv = new ModeView(controller, windowDnDManager, ma.getBounds(), ma.getFrameState(),
                            ma.getOpenedTopComponents(), ma.getSelectedTopComponent());
                }
                accessor2view.put(patternAccessor, mv);
                view2accessor.put(mv, patternAccessor);
                return mv;
            } else if(patternAccessor instanceof EditorAccessor) {
                // Editor accesssor indicates there is a editor area (possible split subtree of editor modes).
                EditorAccessor editorAccessor = (EditorAccessor)patternAccessor;
                EditorView ev = new EditorView(controller, windowDnDManager, 
                                editorAccessor.getResizeWeight(), updateViewForAccessor(editorAccessor.getEditorAreaAccessor(), addingAllowed));
                accessor2view.put(patternAccessor, ev);
                view2accessor.put(ev, patternAccessor);
                return ev;
            }
        }
        
        throw new IllegalStateException("Unknown accessor type, accessor=" + patternAccessor); // NOI18N
    }
    
    
    private void updateSeparateViews(ModeAccessor[] separateModeAccessors) {
        Map newViews = new HashMap();
        for(int i = 0; i < separateModeAccessors.length; i++) {
            ModeAccessor ma = separateModeAccessors[i];
            ModeView mv = (ModeView)updateViewForAccessor(ma, true);
            newViews.put(mv, ma);
        }
        
        Set oldViews = new HashSet(separateModeViews.keySet());
        oldViews.removeAll(newViews.keySet());
        
        separateModeViews.clear();
        separateModeViews.putAll(newViews);
        
        // PENDING Close all old views.
        for(Iterator it = oldViews.iterator(); it.hasNext(); ) {
            ModeView mv = (ModeView)it.next();
            Component comp = mv.getComponent();
            if(comp.isVisible()) {
                comp.setVisible(false);
            }
//            // PENDING
//            ((java.awt.Window)mv.getComponent()).dispose();
        }
        
        // Open all new views.
        for(Iterator it = newViews.keySet().iterator(); it.hasNext(); ) {
            ModeView mv = (ModeView)it.next();
            Component comp = mv.getComponent();
            // #37463, it is needed to provide a check, otherwise the window would 
            // get fronted each time.
            if(!comp.isVisible()) {
                mv.getComponent().setVisible(true);
            }
        }
    }
    
    private void updateSlidingViews(SlidingAccessor[] slidingModeAccessors) {
        Map newViews = new HashMap();
        for(int i = 0; i < slidingModeAccessors.length; i++) {
            SlidingAccessor sa = slidingModeAccessors[i];
            SlidingView sv = (SlidingView)updateViewForAccessor(sa, true);
            newViews.put(sv, sa);
        }
        
        Set oldViews = new HashSet(slidingModeViews.keySet());
        oldViews.removeAll(newViews.keySet());
    
        Set addedViews = new HashSet(newViews.keySet());
        addedViews.removeAll(slidingModeViews.keySet());

        slidingModeViews.clear();
        slidingModeViews.putAll(newViews);
        
        // remove old views.
        SlidingView curSv;
        for(Iterator it = oldViews.iterator(); it.hasNext(); ) {
            curSv = (SlidingView)it.next();
            desktop.removeSlidingView(curSv);
        }
        // add all new views.
        for(Iterator it = addedViews.iterator(); it.hasNext(); ) {
            curSv = (SlidingView)it.next();
            desktop.addSlidingView(curSv);
        }
    }
    
    
    
    public ModeView getModeViewForAccessor(ModeAccessor modeAccessor) {
        return (ModeView)accessor2view.get(modeAccessor);
    }
    
    public ElementAccessor getAccessorForView(ViewElement view) {
        return (ElementAccessor)view2accessor.get(view);
    }

    public void activateMode(ModeAccessor activeModeAccessor) {
        ModeView activeModeView = getModeViewForAccessor(activeModeAccessor);
        activateModeView(activeModeView);
    }

    private void activateModeView(ModeView modeView) {
        setActiveModeView(modeView);
        if(modeView != null) {
            modeView.focusSelectedTopComponent();
        }
    }
    
    /** Set active mode view. */
    private void setActiveModeView(ModeView modeView) {
        //#39729 fix - when main window has focus, do not discard (in SDI the actual component can be hidden
        if(modeView == activeModeView && !mainWindow.isActive()) {
            return;
        }
        
        if(activeModeView != null) {
            activeModeView.setActive(false);
        }
        
        activeModeView = modeView;
        
        if(activeModeView != null) {
            activeModeView.setActive(true);
        }
    }

    /** Gets active mode view. */
    public ModeView getActiveModeView() {
        return activeModeView;
    }
    
    public void setMaximizedModeView(ModeView modeView) {
        if(modeView == maximizedModeView) {
            return;
        }

        maximizedModeView = modeView;
    }
    
    public ModeView getMaximizedModeView() {
        return maximizedModeView;
    }
    
    public void removeModeView(ModeView modeView) {
        if(!view2accessor.containsKey(modeView)) {
            return;
        }
        
        Object accessor = view2accessor.remove(modeView);
        accessor2view.remove(accessor);

        if(separateModeViews.keySet().contains(modeView)) {
            separateModeViews.keySet().remove(modeView);
            modeView.getComponent().setVisible(false);
            return;
        }
        
        setSplitRootIntoDesktop((SplitView)removeModeViewFromElement(desktop.getSplitRoot(), modeView));
    }
    
    /** Gets set of all mode view components. */
    public Set getModeComponents() {
        Set set = new HashSet();
        for(Iterator it = view2accessor.keySet().iterator(); it.hasNext(); ) {
            Object next = it.next();
            if(next instanceof ModeView) {
                ModeView modeView = (ModeView)next;
                set.add(modeView.getComponent());
            }
        }
        
        return set;
    }
    
    /** Gets set of separate mode view frames and editor frame (if separated). */
    public Set getSeparateModeFrames() {
        Set s = new HashSet();
        for(Iterator it = separateModeViews.keySet().iterator(); it.hasNext(); ) {
            ModeView modeView = (ModeView)it.next();
            s.add(modeView.getComponent());
        }
        
        if(editorAreaFrame != null) {
            s.add(editorAreaFrame);
        }
        
        return s;
    }
    
    
    private ViewElement removeModeViewFromElement(ViewElement view, ModeView modeView) {
        if(view == modeView) {
            return null;
        } else if(view instanceof SplitView) {
            SplitView sv = (SplitView)view;
            if(sv.getFirst() == modeView) {
                return sv.getSecond();
            }
            
            if(sv.getSecond() == modeView) {
                return sv.getFirst();
            }
            
            sv.setFirst(removeModeViewFromElement(sv.getFirst(), modeView));
            sv.setSecond(removeModeViewFromElement(sv.getSecond(), modeView));
            return sv;
        } else if(view instanceof EditorView) {
            EditorView ev = (EditorView)view;
            ev.setEditorArea(removeModeViewFromElement(ev.getEditorArea(), modeView), true);
            return ev;
        }
        
        return view;
    }
    
    private Component getDesktopComponent() {
        return currentSplitRoot == null ? null : desktop.getDesktopComponent();
    }    

    public ViewElement getSplitRootElement() {
        return currentSplitRoot;
//        return desktop.getSplitRoot();
    }
    
    public void releaseAll() {
        setSplitRootIntoDesktop(null);
        separateModeViews.clear();
        activeModeView = null;
        accessor2view.clear();
    }
    
    public void setSplitModesVisible(boolean visible) {
        setVisibleModeElement(desktop.getSplitRoot(), visible);
    }
    
    private static void setVisibleModeElement(ViewElement view, boolean visible) {
        if(view instanceof ModeView) {
            view.getComponent().setVisible(visible);
        } else if(view instanceof SplitView) {
            SplitView sv = (SplitView)view;
            setVisibleModeElement(sv.getFirst(), visible);
            setVisibleModeElement(sv.getSecond(), visible);
        } else if(view instanceof EditorView) {
            setVisibleModeElement(((EditorView)view).getEditorArea(), visible);
        }
    }
    
    public void setSeparateModesVisible(boolean visible) {
        if(editorAreaFrame != null) {
            editorAreaFrame.setVisible(visible);
        }
        
        for(Iterator it = separateModeViews.keySet().iterator(); it.hasNext(); ) {
            ModeView mv = (ModeView)it.next();
            mv.getComponent().setVisible(visible);
        }
    }

    public void updateEditorAreaFrameState(int frameState) {
        if(editorAreaFrame != null) {
            editorAreaFrame.setExtendedState(frameState);
        }
    }
    
    public void updateFrameStates() {
        for(Iterator it = separateModeViews.keySet().iterator(); it.hasNext(); ) {
            ModeView mv = (ModeView)it.next();
            mv.updateFrameState();
        }
    }
    
    public void updateSplits() {
        if(maximizedModeView != null) { // PENDING
            return;
        }
        
        // #38014 The destkop can be null if special switch used.
        Component desktopComp = getDesktopComponent();
        if(desktopComp != null) {
            updateSplitElement(desktop.getSplitRoot(), desktopComp.getSize());
        }
    }

    private static void updateSplitElement(ViewElement view, Dimension realSize) {
        if(view instanceof SplitView) {
            SplitView sv = (SplitView)view;
            
            sv.updateSplit(realSize);
            
            Dimension firstRealSize;
            Dimension secondRealSize;
            double location = sv.getLocation();
            int dividerSize = sv.getDividerSize();
            if(sv.getOrientation() == javax.swing.JSplitPane.VERTICAL_SPLIT) {
                firstRealSize = new Dimension(realSize.width, (int)(realSize.height * location) - dividerSize);
                secondRealSize = new Dimension(realSize.width, (int)(realSize.height * (1D - location)) - dividerSize);
            } else {
                firstRealSize = new Dimension((int)(realSize.width * location) - dividerSize, realSize.height);
                secondRealSize = new Dimension((int)(realSize.width * (1D - location)) - dividerSize, realSize.height);
            }

            updateSplitElement(sv.getFirst(), firstRealSize);
            updateSplitElement(sv.getSecond(), secondRealSize);
        } else if(view instanceof EditorView) {
            EditorView ev = (EditorView)view;
            updateSplitElement(ev.getEditorArea(), realSize);
        }
    }

    
    public void updateMainWindowBounds(WindowSystemAccessor wsa) {
        if(wsa.getEditorAreaState() == Constants.EDITOR_AREA_JOINED) {
            mainWindow.setBounds(wsa.getMainWindowBoundsJoined());
        } else {
            mainWindow.setBounds(wsa.getMainWindowBoundsSeparated());
        }
        // #38146 So the updateSplit works with correct size.
        mainWindow.validate();
        // PENDING consider better handling this event so there is not doubled
        // validation (one in MainWindow - needs to be provided here) and this as second one.
    }
    
    public void setProjectName(String projectName) {
        mainWindow.setProjectName(projectName);
    }
    
    private void setMaximizedViewIntoDesktop(ViewElement elem) {
        elem.updateAWTHierarchy();
        desktop.setMaximizedView(elem);
    }
    
    
    private void setSplitRootIntoDesktop(ViewElement root) {
        if (root != null) {
            root.updateAWTHierarchy();
        }
        desktop.setSplitRoot(root);
//        EditorView editView = findEditorAreaElement();
//        if (editView != null) {
//            // hack to trigger readding the editor view compnent into editor view..
//            editView.assureComponentInEditorArea();
//        }
    }

    // PENDING Revise, updating desktop and editor area, bounds... separate this method.
    public void updateDesktop(WindowSystemAccessor wsa) {
//        System.out.println("updatedesktop(param)");
        if(wsa.getEditorAreaState() == Constants.EDITOR_AREA_JOINED) {
            if(maximizedModeView != null) {
                setMainWindowDesktop(getDesktopComponent());
//                System.out.println("viewhierarchyupdddesktop: have maximized=" + maximizedModeView.getClass());
                setMaximizedViewIntoDesktop(maximizedModeView);
//                setMainWindowDesktop(maximizedModeView.getComponent());
                return;
            }
        }

        int editorAreaState = wsa.getEditorAreaState();
        if(editorAreaState == Constants.EDITOR_AREA_JOINED) {
            if(editorAreaFrame != null) {
                editorAreaFrame.setVisible(false);
                editorAreaFrame = null;
            }
            setMainWindowDesktop(getDesktopComponent());
//            System.out.println("viewhierarchyupdddesktop: no maximized");
            setSplitRootIntoDesktop(getSplitRootElement());
            
        } else {
//            System.out.println("viewhierarchyupdddesktop: EDITOR_AREA_SPLIT");
            boolean showEditorFrame = hasEditorAreaVisibleView();
            
            if(editorAreaFrame == null && showEditorFrame) {
                editorAreaFrame = createEditorAreaFrame();
                Rectangle editorAreaBounds = wsa.getEditorAreaBounds();
                if(editorAreaBounds != null) {
                    editorAreaFrame.setBounds(editorAreaBounds);
                }
            } else if(editorAreaFrame != null && !showEditorFrame) { // XXX
                editorAreaFrame.setVisible(false);
                editorAreaFrame = null;
            }
            
            setMainWindowDesktop(null);
            if(showEditorFrame) {
                setSplitRootIntoDesktop(getSplitRootElement());
                setEditorAreaDesktop(getDesktopComponent());
                // #39755 restore the framestate of the previously closed editorArea.
                updateEditorAreaFrameState(wsa.getEditorAreaFrameState());
            }
        }
    }
    
    public void updateDesktop() {
//        System.out.println("updatedesktop()");
        if(mainWindow.hasDesktop()) {
            setMainWindowDesktop(getDesktopComponent());
            if(maximizedModeView != null) {
//                System.out.println("viewhierarchy: have maximized=" + maximizedModeView.getClass());
                setMaximizedViewIntoDesktop(maximizedModeView);
//                setMainWindowDesktop();
            } else {
//                System.out.println("viewhierarchy: no maximized");
                setSplitRootIntoDesktop(getSplitRootElement());
//                setMainWindowDesktop(getDesktopComponent());
            }
        } else {
            boolean showEditorFrame = hasEditorAreaVisibleView();
            
            if(editorAreaFrame != null) {
                if(showEditorFrame) {
                    editorAreaFrame.setDesktop(getDesktopComponent());
                } else { // XXX
                    editorAreaFrame.setVisible(false);
                    editorAreaFrame = null;
                }
            }
        }
    }
    
    public void performSlideIn(SlideOperation operation) {
        desktop.performSlideIn(operation, getPureEditorAreaBounds());
    }
    
    public void performSlideOut(SlideOperation operation) {
        desktop.performSlideOut(operation, getPureEditorAreaBounds());
    }
    
    private void setMainWindowDesktop(Component component) {
        setDesktop(component, true);
    }
    
    private void setEditorAreaDesktop(Component component) {
        setDesktop(component, false);
    }
    
    private void setDesktop(Component component, boolean toMainWindow) {
        Component focusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
        List focusOwnerAWTHierarchyChain; // To find out whether there was a change in AWT hierarchy according to focusOwner.
        if(focusOwner != null) {
            focusOwnerAWTHierarchyChain = getComponentAWTHierarchyChain(focusOwner);
        } else {
            focusOwnerAWTHierarchyChain = Collections.EMPTY_LIST;
        }
        
        if(toMainWindow) {
            mainWindow.setDesktop(component);
        } else {
            editorAreaFrame.setDesktop(component);
        }

        // XXX #37239, #37632 Preserve focus in case the focusOwner component
        // was 'shifted' in AWT hierarchy. I.e. when removed/added it loses focus,
        // but we need to keep it, e.g. for case when its parent split is removing.
        if(focusOwner != null
        && !focusOwnerAWTHierarchyChain.equals(getComponentAWTHierarchyChain(focusOwner))) {
            focusOwner.requestFocus();
        }
    }
    
    
    private List getComponentAWTHierarchyChain(Component comp) {
        List l = new ArrayList();
        Component c = comp;
        while(c != null) {
            l.add(c);
            c = c.getParent();
        }
        
        Collections.reverse(l);
        return l;
    }
    
    private boolean hasEditorAreaVisibleView() {
        //#41875 fix, checking for null EditorView, can be null when using the netbeans.winsys.hideEmptyDocArea command line property
        EditorView view = findEditorAreaElement();
        return (view != null ? (view.getEditorArea() != null) : false);
    }
    
    
    private EditorAreaFrame createEditorAreaFrame() {
        final EditorAreaFrame frame = new EditorAreaFrame();
        frame.addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent evt) {
                if(frame.getExtendedState() == Frame.MAXIMIZED_BOTH) {
                    // Ignore changes when the frame is in maximized state.
                    return;
                }
                controller.userResizedEditorArea(frame.getBounds());
            }
            
            public void componentMoved(ComponentEvent evt) {
                if(frame.getExtendedState() == Frame.MAXIMIZED_BOTH) {
                    // Ignore changes when the frame is in maximized state.
                    return;
                }
                controller.userResizedEditorArea(frame.getBounds());
            }
        });
        
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent evt) {
                closeEditorModes();
            }
            
            public void windowActivated(WindowEvent evt) {
                controller.userActivatedEditorWindow();
            }
        });
        
        frame.addWindowStateListener(new WindowStateListener() {
            public void windowStateChanged(WindowEvent evt) {
     // All the timestamping is a a workaround beause of buggy GNOME and of its kind who iconify the windows on leaving the desktop.
                long currentStamp = System.currentTimeMillis();
                if (currentStamp > (frame.getUserStamp() + 500) && currentStamp > (frame.getMainWindowStamp() + 1000)) {
                    controller.userChangedFrameStateEditorArea(evt.getNewState());
                    long stamp = System.currentTimeMillis();
                    frame.setUserStamp(stamp);
                } else {
                    frame.setUserStamp(0);
                    frame.setMainWindowStamp(0);
                    frame.setExtendedState(evt.getOldState());
                    //frame.setExtendedState(evt.getOldState());
                }
                
            }
        });
        
        return frame;
    }
    
    private void closeEditorModes() {
        closeModeForView(findEditorAreaElement().getEditorArea());
    }
    
    private void closeModeForView(ViewElement view) {
        if(view instanceof ModeView) {
            controller.userClosingMode((ModeView)view);
        } else if(view instanceof SplitView) {
            SplitView sv = (SplitView)view;
            closeModeForView(sv.getFirst());
            closeModeForView(sv.getSecond());
        }
    }
    
    
    public void updateEditorAreaBounds(Rectangle bounds) {
        if(editorAreaFrame != null) {
            editorAreaFrame.setBounds(bounds);
        }
    }

    // XXX
    public Rectangle getPureEditorAreaBounds() {
        EditorView editorView = findEditorAreaElement();
        if(editorView == null) {
            return new Rectangle();
        } else {
            return editorView.getPureBounds();
        }
    }
    
    private EditorView findEditorAreaElement() {
        return findEditorViewForElement(getSplitRootElement());
    }
    
    private EditorView findEditorViewForElement(ViewElement view) {
        if(view instanceof EditorView) {
            return (EditorView)view;
        } else if(view instanceof SplitView) {
            SplitView sv = (SplitView)view;
            EditorView ev = findEditorViewForElement(sv.getFirst());
            if(ev != null) {
                return ev;
            }
            ev = findEditorViewForElement(sv.getSecond());
            if(ev != null) {
                return ev;
            }
        }
        
        return null;
    }
    
    public void updateUI() {
        SwingUtilities.updateComponentTreeUI(mainWindow);
        if(editorAreaFrame != null) {
            SwingUtilities.updateComponentTreeUI(editorAreaFrame);
        }
        for(Iterator it = separateModeViews.keySet().iterator(); it.hasNext(); ) {
            ModeView mv = (ModeView)it.next();
            SwingUtilities.updateComponentTreeUI(mv.getComponent());
        }
    }
    
    public Set getShowingTopComponents() {
        Set s = new HashSet();
        for(Iterator it = accessor2view.keySet().iterator(); it.hasNext(); ) {
            Object accessor = it.next();
            if(accessor instanceof ModeAccessor) {
                s.add(((ModeAccessor)accessor).getSelectedTopComponent());
            }
        }
        for(Iterator it = separateModeViews.values().iterator(); it.hasNext(); ) {
            Object accessor = it.next();
            if(accessor instanceof ModeAccessor) {
                s.add(((ModeAccessor)accessor).getSelectedTopComponent());
            }
        }
        
        return s;
    }
    
    public String toString() {
        return dumpElement(desktop.getSplitRoot(), 0) + "\nseparateViews=" + separateModeViews.keySet(); // NOI18N
    }
    
    private String dumpElement(ViewElement view, int indent) {
        String indentString = createIndentString(indent);
        StringBuffer sb = new StringBuffer();
        
        if(view instanceof ModeView) {
            sb.append(indentString + view + "->" + view.getComponent().getClass() + "@"  + view.getComponent().hashCode());
        } else if(view instanceof EditorView) {
            sb.append(indentString + view);
            sb.append("\n" + dumpElement(((EditorView)view).getEditorArea(), ++indent));
        } else if(view instanceof SplitView) {
            sb.append(indentString + view + "->" + view.getComponent().getClass() + "@"  + view.getComponent().hashCode());
            indent++;
            sb.append("\n" + dumpElement(((SplitView)view).getFirst(), indent));
            sb.append("\n" + dumpElement(((SplitView)view).getSecond(), indent));
        }
         
        return sb.toString();
    }
    
    private static String createIndentString(int indent) {
        StringBuffer sb = new StringBuffer(indent);
        for(int i = 0; i < indent; i++) {
            sb.append("  ");
        }
        
        return sb.toString();
    }

    private String dumpAccessors() {
        StringBuffer sb = new StringBuffer();
        for(Iterator it = accessor2view.keySet().iterator(); it.hasNext(); ) {
            Object accessor = it.next();
            sb.append("accessor="+accessor + "\tview="+accessor2view.get(accessor) + "\n"); // NOI18N
        }
        
        return sb.toString();
    }

    private void changeStateOfSeparateViews(boolean iconify) {
     // All the timestamping is a a workaround beause of buggy GNOME and of its kind who iconify the windows on leaving the desktop.
        long mainStamp = System.currentTimeMillis();
        if(editorAreaFrame != null) {
            if (iconify) {
                if (mainStamp < (editorAreaFrame.getUserStamp() + 500)) {
                    int newState = editorAreaFrame.getExtendedState() &  ~Frame.ICONIFIED;
                    controller.userChangedFrameStateEditorArea(newState);
                    editorAreaFrame.setExtendedState(newState);
                }
            }
            editorAreaFrame.setMainWindowStamp(mainStamp);
            editorAreaFrame.setVisible(!iconify);
        }
        for(Iterator it = separateModeViews.keySet().iterator(); it.hasNext(); ) {
            ModeView mv = (ModeView)it.next();
            Component comp = mv.getComponent();
            if(comp instanceof Frame) {
                Frame fr = (Frame)comp;
                if (iconify) {
                    if (mainStamp < (mv.getUserStamp() + 500)) {
                        int newState = fr.getExtendedState() &  ~Frame.ICONIFIED;
                        controller.userChangedFrameStateMode(mv,  newState);
                        mv.setFrameState(newState);
                    }
                }
                mv.setMainWindowStamp(mainStamp);
                fr.setVisible(!iconify);
            }
        }
    }

    
    /** Main window listener. */
    private static class MainWindowListener extends ComponentAdapter
    implements WindowStateListener {
        
        private final Controller controller;
        private final ViewHierarchy hierarchy;
        
        public MainWindowListener(Controller controller, ViewHierarchy hierarchy) {
            this.controller = controller;
            this.hierarchy  = hierarchy;
        }
        
        public void componentResized(ComponentEvent evt) {
            controller.userResizedMainWindow(evt.getComponent().getBounds());
        }
        
        public void componentMoved(ComponentEvent evt) {
            controller.userMovedMainWindow(evt.getComponent().getBounds());
        }
        
        public void windowStateChanged(WindowEvent evt) {
            int oldState = evt.getOldState();
            int newState = evt.getNewState();
            controller.userChangedFrameStateMainWindow(newState);
            
            if (((oldState & Frame.ICONIFIED) == 0) &&
                ((newState & Frame.ICONIFIED) == Frame.ICONIFIED)) {
                hierarchy.changeStateOfSeparateViews(true);
            } else if (((oldState & Frame.ICONIFIED) == Frame.ICONIFIED) && 
                       ((newState & Frame.ICONIFIED) == 0 )) {
                hierarchy.changeStateOfSeparateViews(false);
            }
        }
    } // End of main window listener.
    
}

