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


import org.netbeans.core.windows.*;
import org.netbeans.core.windows.model.ModelElement;
import org.netbeans.core.windows.view.dnd.WindowDnDManager;
import org.openide.awt.ToolbarPool;
import org.openide.util.WeakSet;
import org.openide.windows.TopComponent;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;

import org.openide.awt.ToolbarPool; // Why is this in open API?
import org.openide.util.WeakSet;
import org.openide.windows.TopComponent;

import org.netbeans.core.windows.Constants;
import org.netbeans.core.windows.Debug;
import org.netbeans.core.windows.ModeImpl;
import org.netbeans.core.windows.ModeStructureSnapshot;
import org.netbeans.core.windows.model.ModelElement;
import org.netbeans.core.windows.view.dnd.WindowDnDManager;
import org.netbeans.core.windows.view.ui.MainWindow;
import org.netbeans.core.windows.WindowManagerImpl;
import org.netbeans.core.windows.WindowSystemSnapshot;
import org.netbeans.core.windows.view.ui.slides.SlideOperation;
import org.openide.ErrorManager;


/**
 * Class which handles view requests, i.e. updates GUI accordingly (ViewHierarchy)
 * and also handles changes to GUI made by user, informs controller handler.
 *
 * @author  Peter Zavadsky
 */
class DefaultView implements View, Controller, WindowDnDManager.ViewAccessor {
    
    
    private final ViewHierarchy hierarchy = new ViewHierarchy(this, new WindowDnDManager(this));
    
    private final ControllerHandler controllerHandler;
    
    private final Set showingTopComponents = new WeakSet(10);

    /** Debugging flag. */
    private static final boolean DEBUG = Debug.isLoggable(DefaultView.class);
    
    private boolean reentryFlag = false;

    
    public DefaultView(ControllerHandler controllerHandler) {
        this.controllerHandler = controllerHandler;
    }
    

    // XXX
    public boolean isDragInProgress() {
        return hierarchy.isDragInProgress();
    }
    
    // XXX
    public Frame getMainWindow() {
        return hierarchy.getMainWindow();
    }
    
    public String guessSlideSide (TopComponent comp) {
        String toReturn = Constants.LEFT;
        Rectangle compb = comp.getBounds();
        Rectangle editorb = hierarchy.getPureEditorAreaBounds();
        Point leftTop = new Point(0, 0);
        SwingUtilities.convertPointToScreen(leftTop, comp);
        if (editorb.x > leftTop.x) {
            toReturn = Constants.LEFT;
        }
        if ((editorb.x + editorb.width) < leftTop.x) {
            toReturn = Constants.RIGHT;
        }
        if ((editorb.y + editorb.height) < leftTop.y) {
                toReturn = Constants.BOTTOM;
        }
        return toReturn;
    }
               

    public void changeGUI(ViewEvent[] viewEvents, WindowSystemSnapshot snapshot) {
        if (reentryFlag) {
            // winsys is not reentrant. having the later snapshot proceesed before the
            // original one causes problems.
            boolean isDangerous = false;
            for(int i = 0; i < viewEvents.length; i++) {
                ViewEvent viewEvent = viewEvents[i];
                int type = viewEvent.getType();
                // these should not cause any problems since they don't change the structure of the snapshot.
                if (type != CHANGE_TOPCOMPONENT_DISPLAY_NAME_CHANGED &&
                    type != CHANGE_TOPCOMPONENT_DISPLAY_NAME_ANNOTATION_CHANGED &&
                    type != CHANGE_TOPCOMPONENT_TOOLTIP_CHANGED && 
                    type != CHANGE_TOPCOMPONENT_ICON_CHANGED &&
                    type != CHANGE_PROJECT_NAME) {
                        isDangerous = true;
                        break;
                }
            }
            if (isDangerous) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
                     new IllegalStateException("Assertion failed. Windows API is not meant to be reentrant. In such a case, non predictable side effects can emerge. " +
                                               "Please consider making your calls to Window System at a different point.")); // NOI18N
            }
        }
        reentryFlag = true;
        // Change to view understandable-convenient structure.
        WindowSystemAccessor wsa = ViewHelper.createWindowSystemAccessor(snapshot);
        
        if(DEBUG) {
            debugLog("CHANGEGUI()"); // NOI18N
            debugLog("Structure=" + wsa); // NOI18N
            debugLog(""); // NOI18N
        }

        // Update accessors.
        if(wsa != null) { // wsa == null during hiding.
            hierarchy.updateViewHierarchy(wsa.getModeStructureAccessor(),
                wsa.getMaximizedModeAccessor() == null || wsa.getEditorAreaState() == Constants.EDITOR_AREA_SEPARATED);
        }

        // Update showing TopComponents.
        Set oldShowing = new HashSet(showingTopComponents);
        Set newShowing = hierarchy.getShowingTopComponents();
        showingTopComponents.clear();
        showingTopComponents.addAll(newShowing);
        
        Set toShow = new HashSet(newShowing);
        toShow.removeAll(oldShowing);
        for(Iterator it = toShow.iterator(); it.hasNext(); ) {
            TopComponent tc = (TopComponent)it.next();
            WindowManagerImpl.getInstance().componentShowing(tc);
        }
        if(DEBUG) {
            debugLog("ChangeGUI: Checking view events...") ; // NOI18N
        }
        
        // PENDING Find main event first.
        for(int i = 0; i < viewEvents.length; i++) {
            ViewEvent viewEvent = viewEvents[i];
            int changeType = viewEvent.getType();
            if(DEBUG) {
                debugLog("ViewEvent=" + viewEvent) ; // NOI18N
            }
            
            if(changeType == CHANGE_VISIBILITY_CHANGED) {
                if(DEBUG) {
                    debugLog("Winsys visibility changed, visible=" + viewEvent.getNewValue()) ; // NOI18N
                }
                
                windowSystemVisibilityChanged(((Boolean)viewEvent.getNewValue()).booleanValue(), wsa);
                // PENDING this should be processed separatelly, there is nothing to coallesce.

                reentryFlag = false;
                return;
            }
        }
        
        // Process all event types.
        for(int i = 0; i < viewEvents.length; i++) {
            ViewEvent viewEvent = viewEvents[i];
            int changeType = viewEvent.getType();
            
            // The other types.
            if(changeType == CHANGE_MAIN_WINDOW_BOUNDS_JOINED_CHANGED) {
                if(DEBUG) {
                    debugLog("Main window bounds joined changed"); // NOI18N
                }

                if(wsa.getEditorAreaState() == Constants.EDITOR_AREA_JOINED) {
                    Rectangle bounds = (Rectangle)viewEvent.getNewValue();
                    if(bounds != null) {
                        hierarchy.getMainWindow().setBounds(bounds);
                    }
                }
            } else if(changeType == CHANGE_MAIN_WINDOW_BOUNDS_SEPARATED_CHANGED) {
                if(DEBUG) {
                    debugLog("Main window bounds separated changed"); // NOI18N
                }

                if(wsa.getEditorAreaState() == Constants.EDITOR_AREA_SEPARATED) {
                    Rectangle bounds = (Rectangle)viewEvent.getNewValue();
                    if(bounds != null) {
                        hierarchy.getMainWindow().setBounds(bounds);
                    }
                }
            } else if(changeType == CHANGE_MAIN_WINDOW_FRAME_STATE_JOINED_CHANGED) {
                if(DEBUG) {
                    debugLog("Main window frame state joined changed"); // NOI18N
                }

                if(wsa.getEditorAreaState() == Constants.EDITOR_AREA_JOINED) {
                    hierarchy.getMainWindow().setExtendedState(wsa.getMainWindowFrameStateJoined());
                }
            } else if(changeType == CHANGE_MAIN_WINDOW_FRAME_STATE_SEPARATED_CHANGED) {
                if(DEBUG) {
                    debugLog("Main window frame state separated changed"); // NOI18N
                }

                if(wsa.getEditorAreaState() == Constants.EDITOR_AREA_SEPARATED) {
                    hierarchy.getMainWindow().setExtendedState(wsa.getMainWindowFrameStateSeparated());
                }
            } else if(changeType == CHANGE_EDITOR_AREA_STATE_CHANGED) {
                if(DEBUG) {
                    debugLog("Editor area state changed"); // NOI18N
                }

                hierarchy.updateDesktop(wsa);
                hierarchy.updateMainWindowBounds(wsa);
                hierarchy.setSeparateModesVisible(true);
            } else if(changeType == CHANGE_EDITOR_AREA_FRAME_STATE_CHANGED) {
                if(DEBUG) {
                    debugLog("Editor area frame state changed"); // NOI18N
                }
                hierarchy.updateEditorAreaFrameState(wsa.getEditorAreaFrameState());
            } else if(changeType == CHANGE_EDITOR_AREA_BOUNDS_CHANGED) {
                if(DEBUG) {
                    debugLog("Editor area bounds changed"); // NOI18N
                }

                hierarchy.updateEditorAreaBounds((Rectangle)viewEvent.getNewValue());
            } else if(changeType == CHANGE_EDITOR_AREA_CONSTRAINTS_CHANGED) {
                if(DEBUG) {
                    debugLog("Editor area constraints changed"); // NOI18N
                }

                hierarchy.updateDesktop(wsa);
            } else if(changeType == CHANGE_ACTIVE_MODE_CHANGED) {
                if(DEBUG) {
                    debugLog("Active mode changed, mode=" + viewEvent.getNewValue()); // NOI18N
                }

                hierarchy.updateDesktop(wsa);
                hierarchy.activateMode(wsa.getActiveModeAccessor());
            } else if(changeType == CHANGE_TOOLBAR_CONFIGURATION_CHANGED) {
                if(DEBUG) {
                    debugLog("Toolbar config name changed"); // NOI18N
                }

                ToolbarPool.getDefault().setConfiguration(wsa.getToolbarConfigurationName());
            } else if(changeType == CHANGE_MAXIMIZED_MODE_CHANGED) {
                if(DEBUG) {
                    debugLog("Maximized mode changed"); // NOI18N
                }

                hierarchy.setMaximizedModeView(hierarchy.getModeViewForAccessor(wsa.getMaximizedModeAccessor()));
                hierarchy.updateDesktop(wsa);
                hierarchy.activateMode(wsa.getActiveModeAccessor());
            } else if(changeType == CHANGE_MODE_ADDED) {
                if(DEBUG) {
                    debugLog("Mode added"); // NOI18N
                }

                hierarchy.updateDesktop(wsa);
            } else if(changeType == CHANGE_MODE_REMOVED) {
                if(DEBUG) {
                    debugLog("Mode removed"); // NOI18N
                }

                hierarchy.updateDesktop(wsa);
                hierarchy.activateMode(wsa.getActiveModeAccessor());
            } else if(changeType == CHANGE_MODE_CONSTRAINTS_CHANGED) {
                if(DEBUG) {
                    debugLog("Mode constraints changed"); // NOI18N
                }

                hierarchy.updateDesktop(wsa);
            } else if(changeType == CHANGE_MODE_BOUNDS_CHANGED) {
                if(DEBUG) {
                    debugLog("Mode bounds changed"); // NOI18N
                }

                ModeView modeView = hierarchy.getModeViewForAccessor(wsa.findModeAccessor((String)viewEvent.getSource())); // XXX
                if(modeView != null) {
                    modeView.getComponent().setBounds((Rectangle)viewEvent.getNewValue());
                }
            } else if(changeType == CHANGE_MODE_FRAME_STATE_CHANGED) {
                if(DEBUG) {
                    debugLog("Mode state changed"); // NOI18N
                }

                ModeView modeView = hierarchy.getModeViewForAccessor(wsa.findModeAccessor((String)viewEvent.getSource())); // XXX
                if(modeView != null) {
                    modeView.setFrameState(((Integer)viewEvent.getNewValue()).intValue());
                    modeView.updateFrameState();
                }
            } else if(changeType == CHANGE_MODE_SELECTED_TOPCOMPONENT_CHANGED) {
                if(DEBUG) {
                    debugLog("Selected topcomponent changed, tc=" + viewEvent.getNewValue()); // NOI18N
                }

                // XXX PENDING see TopComponent.requestFocus (it's wrongly overriden).
                hierarchy.updateDesktop(wsa);
                // XXX if the selection is changed in the active mode reactivate it.
                ModeAccessor ma = wsa.getActiveModeAccessor();
                if(ma == wsa.getActiveModeAccessor()) {
                    hierarchy.activateMode(ma);
                }
            } else if(changeType == CHANGE_MODE_TOPCOMPONENT_ADDED) {
                if(DEBUG) {
                    debugLog("TopComponent added"); // NOI18N
                }

                hierarchy.updateDesktop(wsa);
                hierarchy.setSeparateModesVisible(true);
                ModeView modeView = hierarchy.getModeViewForAccessor(wsa.findModeAccessor((String)viewEvent.getSource())); // XXX
                if (modeView != null) {
                    // #39755 - seems to require to call the updateframestate() in order to have a closed mode to show in the last framestate.
                    // not 100% sure this is the correct location for the call, for editorarea the relevant change resides in ViewHierarchy.updateDesktop,
                    // prefer not to call hierarchy.updateframestates() because it's only needed for the currently opened mode..
                    modeView.updateFrameState();
                }
            } else if(changeType == CHANGE_MODE_TOPCOMPONENT_REMOVED) {
                if(DEBUG) {
                    debugLog("TopComponent removed"); // NOI18N
                }

                hierarchy.setMaximizedModeView(hierarchy.getModeViewForAccessor(wsa.getMaximizedModeAccessor()));
                hierarchy.updateDesktop(wsa);
                ModeView modeView = hierarchy.getModeViewForAccessor(wsa.findModeAccessor((String)viewEvent.getSource())); // XXX
                if(modeView != null) {
                    modeView.removeTopComponent((TopComponent)viewEvent.getNewValue());
                }
                hierarchy.activateMode(wsa.getActiveModeAccessor());
            } else if(changeType == CHANGE_TOPCOMPONENT_DISPLAY_NAME_CHANGED) {
                if(DEBUG) {
                    debugLog("TopComponent display name changed, tc=" + viewEvent.getNewValue()); // NOI18N
                }

                ModeView modeView = hierarchy.getModeViewForAccessor(wsa.findModeAccessor((String)viewEvent.getSource())); // XXX
                if(modeView != null) { // PENDING investigate
                    modeView.updateName((TopComponent)viewEvent.getNewValue());
                }
            } else if(changeType == CHANGE_TOPCOMPONENT_DISPLAY_NAME_ANNOTATION_CHANGED) {
                if(DEBUG) {
                    debugLog("TopComponent display name annotation changed, tc=" + viewEvent.getNewValue()); // NOI18N
                }

                ModeView modeView = hierarchy.getModeViewForAccessor(wsa.findModeAccessor((String)viewEvent.getSource())); // XXX
                if(modeView != null) { // PENDING investigate
                    modeView.updateName((TopComponent)viewEvent.getNewValue());
                }
            } else if(changeType == CHANGE_TOPCOMPONENT_TOOLTIP_CHANGED) {
                if(DEBUG) {
                    debugLog("TopComponent tooltip changed, tc=" + viewEvent.getNewValue()); // NOI18N
                }

                ModeView modeView = hierarchy.getModeViewForAccessor(wsa.findModeAccessor((String)viewEvent.getSource())); // XXX
                if(modeView != null) { // PENDING investigate
                    modeView.updateToolTip((TopComponent)viewEvent.getNewValue());
                }
            } else if(changeType == CHANGE_TOPCOMPONENT_ICON_CHANGED) {
                if(DEBUG) {
                    debugLog("TopComponent icon changed"); // NOI18N
                }

                ModeView modeView = hierarchy.getModeViewForAccessor(wsa.findModeAccessor((String)viewEvent.getSource())); // XXX
                if(modeView != null) { // PENDING investigate
                    modeView.updateIcon((TopComponent)viewEvent.getNewValue());
                }
            } else if(changeType == CHANGE_TOPCOMPONENT_ATTACHED) {
                if(DEBUG) {
                    debugLog("TopComponent attached"); // NOI18N
                }                

                hierarchy.updateDesktop(wsa);
                hierarchy.activateMode(wsa.getActiveModeAccessor());
            } else if(changeType == CHANGE_TOPCOMPONENT_ARRAY_ADDED) {
                if(DEBUG) {
                    debugLog("TopComponent array added:" // NOI18N
                        + Arrays.asList((TopComponent[])viewEvent.getNewValue()));
                }

                hierarchy.updateDesktop(wsa);
            } else if(changeType == CHANGE_TOPCOMPONENT_ARRAY_REMOVED) {
                if(DEBUG) {
                    debugLog("TopComponent array removed:" // NOI18N
                        + Arrays.asList((TopComponent[])viewEvent.getNewValue()));
                }

                hierarchy.updateDesktop(wsa);
                hierarchy.activateMode(wsa.getActiveModeAccessor());
            } else if(changeType == CHANGE_TOPCOMPONENT_ACTIVATED) {
                if(DEBUG) {
                    debugLog("TopComponent activated, tc=" + viewEvent.getNewValue()); // NOI18N
                }
                
                hierarchy.updateDesktop(wsa);
                hierarchy.activateMode(wsa.getActiveModeAccessor());
            } else if(changeType == CHANGE_MODE_CLOSED) {
                if(DEBUG) {
                    debugLog("Mode closed, mode=" + viewEvent.getSource()); // NOI18N
                }
                
                hierarchy.updateDesktop();
            } else if(changeType == CHANGE_DND_PERFORMED) {
                if(DEBUG) {
                    debugLog("DnD performed"); // NOI18N
                }

                hierarchy.setMaximizedModeView(hierarchy.getModeViewForAccessor(wsa.getMaximizedModeAccessor()));
                hierarchy.updateDesktop();
                hierarchy.activateMode(wsa.getActiveModeAccessor());
            } else if(changeType == CHANGE_UI_UPDATE) {
                if(DEBUG) {
                    debugLog("UI update"); // NOI18N
                }

                hierarchy.updateUI();
            } else if(changeType == CHANGE_PROJECT_NAME) {
                if(DEBUG) {
                    debugLog("Update project name"); // NOI18N
                }
                
                hierarchy.setProjectName(wsa.getProjectName());
            } else if(changeType == CHANGE_TOPCOMPONENT_AUTO_HIDE_ENABLED ||
                      changeType == CHANGE_TOPCOMPONENT_AUTO_HIDE_DISABLED) {
                if(DEBUG) {
                    debugLog("Top Component Auto Hide changed"); // NOI18N
                }
                hierarchy.setMaximizedModeView(hierarchy.getModeViewForAccessor(wsa.getMaximizedModeAccessor()));
                hierarchy.updateDesktop(wsa);
                hierarchy.activateMode(wsa.getActiveModeAccessor());
            }
        }
        
        Set toHide = new HashSet(oldShowing);
        toHide.removeAll(newShowing);
        for(Iterator it = toHide.iterator(); it.hasNext(); ) {
            TopComponent tc = (TopComponent)it.next();
            WindowManagerImpl.getInstance().componentHidden(tc);
        }
        
        reentryFlag = false;
    }
    
    /** Whether the window system should show or hide its GUI. */
    private void windowSystemVisibilityChanged(boolean visible, WindowSystemAccessor wsa) {
        if(visible) {
            showWindowSystem(wsa);
        } else {
            hideWindowSystem();
        }
    }
    

    //////////////////////////////////////////////////////////
    private void showWindowSystem(final WindowSystemAccessor wsa) {
        long start = System.currentTimeMillis();
        if(DEBUG) {
            debugLog("ShowWindowSystem--"); // NOI18N
        }
        
        hierarchy.getMainWindow().initializeComponents();
        // Init toolbar.
        ToolbarPool.getDefault().setConfiguration(wsa.getToolbarConfigurationName());
        
        if(DEBUG) {
            debugLog(wsa.getModeStructureAccessor().toString());
        }
        // Prepare main window (pack and set bounds).
        hierarchy.getMainWindow().prepareWindow();

        if(DEBUG) {
            debugLog("Init view 4="+(System.currentTimeMillis() - start) + " ms"); // NOI18N
        }


        if(DEBUG) {
            debugLog("Init view 2="+(System.currentTimeMillis() - start) + " ms"); // NOI18N
        }
        
        hierarchy.setSplitModesVisible(true);

        if(DEBUG) {
            debugLog("Init view 3="+(System.currentTimeMillis() - start) + " ms"); // NOI18N
        }
        
       
        // Shows main window
        hierarchy.getMainWindow().setVisible(true);
        
        hierarchy.setMaximizedModeView(hierarchy.getModeViewForAccessor(wsa.getMaximizedModeAccessor()));

        // Init desktop.
        hierarchy.updateDesktop(wsa);
        
        // XXX Seems it needs to be called after setVisible(true);
        // When tried to be set (maximized) immediately after prepareWindow, it didn't work,
        // even thou its peer was already present (addNotify from pack was called on frame).
        if(wsa.getEditorAreaState() == Constants.EDITOR_AREA_JOINED) {
            hierarchy.getMainWindow().setExtendedState(wsa.getMainWindowFrameStateJoined());
        } else {
            hierarchy.getMainWindow().setExtendedState(wsa.getMainWindowFrameStateSeparated());
        }
        
        
        // Show separate modes.
        hierarchy.setSeparateModesVisible(true);

        hierarchy.updateEditorAreaFrameState(wsa.getEditorAreaFrameState());
        
        // Updates frame states of separate modes.
        hierarchy.updateFrameStates();
        
        hierarchy.setProjectName(wsa.getProjectName());
        
        // XXX PENDING
        if(wsa.getEditorAreaState() == Constants.EDITOR_AREA_JOINED) {
            // Ignore when main window is maximized.
            if(hierarchy.getMainWindow().getExtendedState() != Frame.MAXIMIZED_BOTH) {
                if (DEBUG) {
                    debugLog("do updateMainWindowBoundsSeparatedHelp");
                }
                updateMainWindowBoundsSeparatedHelp();
                updateEditorAreaBoundsHelp();
                updateSeparateBoundsForView(hierarchy.getSplitRootElement());
            }
        }

        //#39238 in maximazed mode swing posts a lot of stuff to Awt thread using SwingUtilities.invokeLater
        // for that reason the installation of window listeners and the update of splits kicked in too early when
        // the window was not maximazed yet -> resulted in wrong calculation of splits and also bad reactions from the listeners
        // which considered the automated change to maximazed mode to be issued by the user.
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                if (DEBUG) {
                    debugLog("Installing main window listeners.");
                }
                //#40501 it seems that activating mode needs to be done after the splits are recalculated.
                //otherwise it possibly failes.
                hierarchy.activateMode(wsa.getActiveModeAccessor());
                hierarchy.installMainWindowListeners();
            }
        });
        
        if(DEBUG) {
            debugLog("Init view 5="+(System.currentTimeMillis() - start) + " ms"); // NOI18N
        }
    }
    
    private void hideWindowSystem() {
        hierarchy.uninstallMainWindowListeners();
        
        hierarchy.setSeparateModesVisible(false);
        hierarchy.getMainWindow().setVisible(false);
        // Release all.
        hierarchy.releaseAll();
    }
    
    ////////////////////////////////////////////////////
    // Controller >>
    public void userActivatedModeView(ModeView modeView) {
        if(DEBUG) {
            debugLog("User activated mode view, mode=" + modeView); // NOI18N
        }
        
        ModeAccessor modeAccessor = (ModeAccessor)hierarchy.getAccessorForView(modeView);
        ModeImpl mode = getModeForModeAccessor(modeAccessor);
        controllerHandler.userActivatedMode(mode);
    }
    
    public void userActivatedModeWindow(ModeView modeView) {
        if(DEBUG) {
            debugLog("User activated mode window, mode=" + modeView); // NOI18N
        }
        
        ModeAccessor modeAccessor = (ModeAccessor)hierarchy.getAccessorForView(modeView);
        ModeImpl mode = getModeForModeAccessor(modeAccessor);
        controllerHandler.userActivatedModeWindow(mode);
    }
    
    public void userActivatedEditorWindow() {
        if(DEBUG) {
            debugLog("User activated editor window"); // NOI18N
        }
        
        controllerHandler.userActivatedEditorWindow();
    }
    
    public void userSelectedTab(ModeView modeView, TopComponent selected) {
        if(DEBUG) {
            debugLog("User selected tab, tc=" + WindowManagerImpl.getInstance().getTopComponentDisplayName(selected)); // NOI18N
        }

        ModeAccessor modeAccessor = (ModeAccessor)hierarchy.getAccessorForView(modeView);
        ModeImpl mode = getModeForModeAccessor(modeAccessor);
        controllerHandler.userActivatedTopComponent(mode, selected);
    }
    
    public void userClosingMode(ModeView modeView) {
        if(DEBUG) {
            debugLog("User closing mode="+modeView); // NOI18N
        }

        ModeAccessor modeAccessor = (ModeAccessor)hierarchy.getAccessorForView(modeView);
        ModeImpl mode = getModeForModeAccessor(modeAccessor);
        controllerHandler.userClosedMode(mode);
    }
    
    private void removeModeViewFromHierarchy(ModeView modeView) {
        hierarchy.removeModeView(modeView);
        hierarchy.updateDesktop();
    }
    
    public void userResizedMainWindow(Rectangle bounds) {
        if(DEBUG) {
            debugLog("User resized main window"); // NOI18N
        }

        // Ignore when main window is maximized.
        if(hierarchy.getMainWindow().getExtendedState() != Frame.MAXIMIZED_BOTH) {
            controllerHandler.userResizedMainWindow(bounds);
        } 
        // Update also the splits.
        updateChangedSplits();

        // Ignore when main window is maximized.
        if(hierarchy.getMainWindow().getExtendedState() != Frame.MAXIMIZED_BOTH) {
            // XXX PENDING
            updateMainWindowBoundsSeparatedHelp();
            updateEditorAreaBoundsHelp();
            updateSeparateBoundsForView(hierarchy.getSplitRootElement());
        }
    }
    
    
    private void updateChangedSplits() {
        if(hierarchy.getMaximizedModeView() != null) { // PENDING
            return;
        }
        splitChangedForView(hierarchy.getSplitRootElement());
    }
    
    private void splitChangedForView(ViewElement view) {
        if(view instanceof SplitView) {
            SplitView sv = (SplitView)view;
            JSplitPane sp = (JSplitPane)sv.getComponent();
            int absoluteLocation = sp.getDividerLocation();
            double relativeLocation;
            if(sp.getOrientation() == JSplitPane.VERTICAL_SPLIT) {
                relativeLocation = (double)absoluteLocation/(sp.getHeight() - sp.getDividerSize() );
            } else {
                relativeLocation = (double)absoluteLocation/(sp.getWidth() - sp.getDividerSize() );
            }
            userMovedSplit(relativeLocation, sv, sv.getFirst(), sv.getSecond());
            
            splitChangedForView(sv.getFirst());
            splitChangedForView(sv.getSecond());
        }
    }
    
    public void userMovedMainWindow(Rectangle bounds) {
        if(DEBUG) {
            debugLog("User moved main window"); // NOI18N
        }

        // Ignore when main window is maximized.
        if (hierarchy.getMainWindow().getExtendedState() != Frame.MAXIMIZED_BOTH) {
            controllerHandler.userResizedMainWindow(bounds);
        }
    }
    
    public void userResizedEditorArea(Rectangle bounds) {
        if(DEBUG) {
            debugLog("User resized editor area"); // NOI18N
        }

        controllerHandler.userResizedEditorArea(bounds);
    }
    
    public void userResizedModeBounds(ModeView modeView, Rectangle bounds) {
        if(DEBUG) {
            debugLog("User resized mode"); // NOI18N
        }

        ModeAccessor modeAccessor = (ModeAccessor)hierarchy.getAccessorForView(modeView);
        // XXX PENDING #39083 Investigate how it could happen.
        if(modeAccessor != null) {
            ModeImpl mode = getModeForModeAccessor(modeAccessor);
            controllerHandler.userResizedModeBounds(mode, bounds);
        }
    }
    
    public void userMovedSplit(double splitLocation, SplitView splitView,
    ViewElement first, ViewElement second) {
        if(DEBUG) {
            debugLog("User moved split"); // NOI18N
//            Debug.dumpStack(DefaultView.class);
        }

        SplitAccessor splitAccessor = (SplitAccessor)hierarchy.getAccessorForView(splitView);
        ElementAccessor firstAccessor = hierarchy.getAccessorForView(first);
        ElementAccessor secondAccessor = hierarchy.getAccessorForView(second);
        
        ViewHelper.computeSplitWeights(splitLocation, splitAccessor, firstAccessor, secondAccessor, controllerHandler);
        
        // XXX PENDING
        updateSeparateBoundsForView(splitView);
    }
    
    public void userClosedTopComponent(ModeView modeView, TopComponent tc) {
        if(DEBUG) {
            debugLog("User closed topComponent=" + tc); // NOI18N
        }

        ModeAccessor modeAccessor = (ModeAccessor)hierarchy.getAccessorForView(modeView);
        ModeImpl mode = getModeForModeAccessor(modeAccessor);
        controllerHandler.userClosedTopComponent(mode, tc);
    }
    
    public void userChangedFrameStateMainWindow(int frameState) {
        if(DEBUG) {
            debugLog("User changed frame state main window"); // NOI18N
        }
        
        controllerHandler.userChangedFrameStateMainWindow(frameState);
    }
    
    public void userChangedFrameStateEditorArea(int frameState) {
        if(DEBUG) {
            debugLog("User changed frame state editor area"); // NOI18N
        }
        controllerHandler.userChangedFrameStateEditorArea(frameState);
    }
    
    public void userChangedFrameStateMode(ModeView modeView, int frameState) {
        if(DEBUG) {
            debugLog("User changed frame state mode"); // NOI18N
        }

        ModeAccessor modeAccessor = (ModeAccessor)hierarchy.getAccessorForView(modeView);
        ModeImpl mode = getModeForModeAccessor(modeAccessor);
        controllerHandler.userChangedFrameStateMode(mode, frameState);
    }
    
    // DnD
    public void userDroppedTopComponents(ModeView modeView, TopComponent[] tcs) {
        if(tcs.length == 0) {
            return;
        }
        
        if(DEBUG) {
            debugLog("User dropped TopComponent's"); // NOI18N
        }

        ModeAccessor modeAccessor = (ModeAccessor)hierarchy.getAccessorForView(modeView);
        ModeImpl mode = getModeForModeAccessor(modeAccessor);
        controllerHandler.userDroppedTopComponents(mode, tcs);
    }
    
    public void userDroppedTopComponents(ModeView modeView, TopComponent[] tcs, int index) {
        if(tcs.length == 0) {
            return;
        }
        
        if(DEBUG) {
            debugLog("User dropped TopComponent's to index=" + index); // NOI18N
        }
        
        ModeAccessor modeAccessor = (ModeAccessor)hierarchy.getAccessorForView(modeView);
        ModeImpl mode = getModeForModeAccessor(modeAccessor);
        
        // #37127 Refine the index if the TC is moving inside the mode.
        int position = Arrays.asList(modeAccessor.getOpenedTopComponents()).indexOf(tcs[0]);
        if(position > -1 && position <= index) {
            index--;
        }
                
        controllerHandler.userDroppedTopComponents(mode, tcs, index);
    }
    
    public void userDroppedTopComponents(ModeView modeView, TopComponent[] tcs, String side) {
        if(tcs.length == 0) {
            return;
        }
        
        if(DEBUG) {
            debugLog("User dropped TopComponent's to side=" + side); // NOI18N
        }
        
        ModeAccessor modeAccessor = (ModeAccessor)hierarchy.getAccessorForView(modeView);
        ModeImpl mode = getModeForModeAccessor(modeAccessor);
        controllerHandler.userDroppedTopComponents(mode, tcs, side);
    }

    public void userDroppedTopComponentsIntoEmptyEditor(TopComponent[] tcs) {
        if(tcs.length == 0) {
            return;
        }

        if(DEBUG) {
            debugLog("User dropped TopComponent's into empty editor"); // NOI18N
        }
        
        controllerHandler.userDroppedTopComponentsIntoEmptyEditor(tcs);
    }
    
    public void userDroppedTopComponentsAround(TopComponent[] tcs, String side) {
        if(tcs.length == 0) {
            return;
        }

        if(DEBUG) {
            debugLog("User dropped TopComponent's around, side=" + side); // NOI18N
        }
        
        controllerHandler.userDroppedTopComponentsAround(tcs, side);
    }
    
    public void userDroppedTopComponentsIntoSplit(SplitView splitView, TopComponent[] tcs) {
        if(tcs.length == 0) {
            return;
        }
        
        if(DEBUG) {
            debugLog("User dropped TopComponent's into split=" + splitView); // NOI18N
        }

        SplitAccessor splitAccessor = (SplitAccessor)hierarchy.getAccessorForView(splitView);
        ElementAccessor firstAccessor = hierarchy.getAccessorForView(splitView.getFirst());
        ElementAccessor secondAccessor = hierarchy.getAccessorForView(splitView.getSecond());
        
        ModelElement splitElement  = getModelElementForAccessor(splitAccessor);
        ModelElement firstElement  = getModelElementForAccessor(firstAccessor);
        ModelElement secondElement = getModelElementForAccessor(secondAccessor);
        
        controllerHandler.userDroppedTopComponentsIntoSplit(splitElement, firstElement, secondElement, tcs);
    }
    
    public void userDroppedTopComponentsAroundEditor(TopComponent[] tcs, String side) {
        if(tcs.length == 0) {
            return;
        }

        if(DEBUG) {
            debugLog("User dropped TopComponent's around editor, side=" + side); // NOI18N
        }
        
        controllerHandler.userDroppedTopComponentsAroundEditor(tcs, side);
    }
    
    public void userDroppedTopComponentsIntoFreeArea(TopComponent[] tcs, Rectangle bounds) {
        if(tcs.length == 0) {
            return;
        }
        
        if(DEBUG) {
            debugLog("User dropped TopComponent's into free area, bounds=" + bounds); // NOI18N
        }
        
        controllerHandler.userDroppedTopComponentsIntoFreeArea(tcs, bounds);
    }
    
    // Sliding

    public void userDisabledAutoHide(ModeView modeView, TopComponent tc) {
        ModeAccessor modeAccessor = (ModeAccessor)hierarchy.getAccessorForView(modeView);
        ModeImpl mode = getModeForModeAccessor(modeAccessor);
        controllerHandler.userDisabledAutoHide(tc, mode);
    }    

    public void userEnabledAutoHide(ModeView modeView, TopComponent tc) {
        ModeAccessor modeAccessor = (ModeAccessor)hierarchy.getAccessorForView(modeView);
        ModeImpl mode = getModeForModeAccessor(modeAccessor);
        String side = guessSlideSide(tc);
        controllerHandler.userEnabledAutoHide(tc, mode, side);
    }
    
    public void userTriggeredSlideIn(ModeView modeView, SlideOperation operation) {
        hierarchy.performSlideIn(operation);
    }    
    
    public void userTriggeredSlideOut(ModeView modeView, SlideOperation operation) {
        hierarchy.performSlideOut(operation);
        // restore focus if needed
        if (operation.requestsActivation()) {
            ModeView lastNonSlidingActive = hierarchy.getLastNonSlidingActiveModeView();
            ModeImpl mode = null;
            if (lastNonSlidingActive != null) {
                mode = getModeForModeAccessor((ModeAccessor)hierarchy.getAccessorForView(lastNonSlidingActive));
            }
            if (mode != null) {
                controllerHandler.userActivatedMode(mode);
            } else {
                // no appropriate mode exists - select editor as last resort
                controllerHandler.userActivatedEditorWindow();
            }
        }
    }    
    
    public void userTriggeredSlideIntoDesktop(ModeView modeView, SlideOperation operation) {
        hierarchy.performSlideIntoDesktop(operation);
    }    
    
    public void userTriggeredSlideIntoEdge(ModeView modeView, SlideOperation operation) {
        hierarchy.performSlideIntoEdge(operation);
    }
    
    private static ModeImpl getModeForModeAccessor(ModeAccessor accessor) {
        return accessor == null ? null : accessor.getMode();
    }
    
    private static ModelElement getModelElementForAccessor(ElementAccessor accessor) {
        return accessor == null ? null : accessor.getOriginator();
    }
    // Controller <<
    ////////////////////////////////////////////////////
    
    // XXX
    private void updateMainWindowBoundsSeparatedHelp() {
        controllerHandler.userResizedMainWindowBoundsSeparatedHelp(
                hierarchy.getMainWindow().getPureMainWindowBounds());
    }
    
    // XXX
    private void updateEditorAreaBoundsHelp() {
        Rectangle bounds = hierarchy.getPureEditorAreaBounds();
        controllerHandler.userResizedEditorAreaBoundsHelp(bounds);
    }
    
    // XXX PENDING This is just for the cases split modes doesn't have a separated
    // opposite ones, so they keep the bounds for them. Revise.
    private void updateSeparateBoundsForView(ViewElement view) {
        if(view instanceof ModeView) {
            ModeView mv = (ModeView)view;
            ModeAccessor ma = (ModeAccessor)hierarchy.getAccessorForView(mv);
            if(ma != null) {
                Component comp = mv.getComponent();
                Rectangle bounds = comp.getBounds();
                Point point = new Point(0, 0);
                SwingUtilities.convertPointToScreen(point, comp);
                bounds.setLocation(point);
                
                ModeImpl mode = getModeForModeAccessor(ma);
                // XXX ControllerHandler
                controllerHandler.userResizedModeBoundsSeparatedHelp(mode, bounds);
            }
        } else if(view instanceof SplitView) {
            SplitView sv = (SplitView)view;
            updateSeparateBoundsForView(sv.getFirst());
            updateSeparateBoundsForView(sv.getSecond());
        } else if(view instanceof EditorView) {
            updateEditorAreaBoundsHelp();
            // Editor area content isn't needed to remember.
        }
    }

    ///////////////
    // ViewAccessor
    public Set getModeComponents() {
        return hierarchy.getModeComponents();
    }
    
    public Set getSeparateModeFrames() {
        return hierarchy.getSeparateModeFrames();
    }
    
    public Controller getController() {
        return this;
    }
    // ViewAccessor
    ///////////////

    
    private static void debugLog(String message) {
        Debug.log(DefaultView.class, message);
    }
    
    
}

