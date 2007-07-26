/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt. 
  * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 */
package org.netbeans.modules.mobility.svgcore.composer;

import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;
import javax.microedition.m2g.SVGImage;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import org.netbeans.modules.mobility.svgcore.SVGDataObject;
import org.netbeans.modules.mobility.svgcore.composer.actions.CursorPositionActionFactory;
import org.netbeans.modules.mobility.svgcore.composer.actions.DeleteActionFactory;
import org.netbeans.modules.mobility.svgcore.composer.actions.HighlightActionFactory;
import org.netbeans.modules.mobility.svgcore.composer.actions.MoveBackwardActionFactory;
import org.netbeans.modules.mobility.svgcore.composer.actions.MoveForwardActionFactory;
import org.netbeans.modules.mobility.svgcore.composer.actions.MoveToBottomActionFactory;
import org.netbeans.modules.mobility.svgcore.composer.actions.MoveToTopActionFactory;
import org.netbeans.modules.mobility.svgcore.composer.actions.RotateActionFactory;
import org.netbeans.modules.mobility.svgcore.composer.actions.ScaleActionFactory;
import org.netbeans.modules.mobility.svgcore.composer.actions.SelectAction;
import org.netbeans.modules.mobility.svgcore.composer.actions.SelectActionFactory;
import org.netbeans.modules.mobility.svgcore.composer.actions.TranslateActionFactory;
import org.netbeans.modules.mobility.svgcore.view.svg.AbstractSVGAction;
import org.netbeans.modules.mobility.svgcore.view.svg.SVGStatusBar;
import org.openide.util.Lookup;
import org.openide.util.Utilities;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.w3c.dom.Node;
import org.w3c.dom.svg.SVGLocatableElement;

/**
 *
 * @author Pavel Benes
 */
public class SceneManager {   
    private transient SVGDataObject               m_dObj = null;
    private transient InstanceContent             m_lookupContent;
    private transient Lookup                      m_lookup;   
    private transient PerseusController           m_perseusController;
    private transient ScreenManager               m_screenMgr;
    private transient InputControlManager         m_inputControlMgr;
    private transient List<ComposerActionFactory> m_registeredActions;
    private transient Stack<ComposerAction>       m_activeActions;
    //TODO revisit
    public transient SelectActionFactory          m_selectActionFactory;
    private transient List<SelectionListener>     m_selectionListeners;
    private transient SVGImage                    m_svgImage;
    private transient SVGLocatableElement         m_popupElement = null;
    private transient String                      m_selectedId = null;
    
    /** persistent properties */
    private boolean  m_isReadOnly = true;
            float    m_zoomRatio  = (float)1.0;
    
    public static interface SelectionListener {
        public void selectionChanged( SVGObject [] newSelection, SVGObject [] oldSelection, boolean isReadOnly);
    }
    
    public SceneManager() {}

    public void initialize(SVGDataObject dObj) {
        assert m_dObj == null : "Scene manager cannot be initialized twice";
        m_dObj              = dObj;
        m_lookupContent     = new InstanceContent();
        m_lookup            = new AbstractLookup(m_lookupContent);        

        m_registeredActions     = new ArrayList<ComposerActionFactory>();
        m_activeActions         = new Stack<ComposerAction>();
        m_selectionListeners    = new ArrayList<SelectionListener>();
        
        //TODO maybe use some Netbeans mechanism for action registration
        m_selectActionFactory = new SelectActionFactory(this); 
        addSelectionListener(m_selectActionFactory);
        
        m_registeredActions.add( new HighlightActionFactory(this));
        m_registeredActions.add( m_selectActionFactory);
        m_registeredActions.add( new TranslateActionFactory(this));
        m_registeredActions.add( new ScaleActionFactory(this));
        m_registeredActions.add( new RotateActionFactory(this));
        m_registeredActions.add( new DeleteActionFactory(this));
        m_registeredActions.add( new MoveToTopActionFactory(this));
        m_registeredActions.add( new MoveToBottomActionFactory(this));
        m_registeredActions.add( new MoveForwardActionFactory(this));
        m_registeredActions.add( new MoveBackwardActionFactory(this));
        m_registeredActions.add( new CursorPositionActionFactory(this));

        m_screenMgr = new ScreenManager(this);
        updateStatusBar();
    }

    public void setImage(SVGImage svgImage) {
        if (m_svgImage != null) {
            resetImage();
        }
        
        m_lookupContent.add(svgImage);
        m_svgImage = svgImage;
        
        m_perseusController = new PerseusController(this);
        m_inputControlMgr   = new InputControlManager(this);
        
        m_perseusController.initialize();
        m_screenMgr.initialize();
        m_inputControlMgr.initialize();
                
        m_screenMgr.refresh();
    }
    
    public void resetImage() {
        if (m_svgImage != null) {
            m_lookupContent.remove(m_svgImage);
            m_svgImage          = null;
        }
        m_popupElement      = null;
        m_perseusController = null;
        m_screenMgr.reset();
        m_inputControlMgr   = null;
        // remove all running actions
        m_activeActions.clear();
        
    }
        
    public void saveSelection() {
        SVGObject [] selected = getSelected();
        if ( selected != null && selected.length > 0 && selected[0] != null) {
            m_selectedId = selected[0].getElementId();
        } else {
            m_selectedId = null;
        }
    }
    
    public void restoreSelection() {
        if ( m_selectedId != null) {
            setSelection(m_selectedId);
            m_selectedId = null;
        }
    }
    
    public void serialize(ObjectOutputStream out) throws IOException {
        out.writeBoolean(m_isReadOnly);
        out.writeFloat(m_zoomRatio);
    }

    public void deserialize(ObjectInputStream in) throws IOException {
        m_isReadOnly = in.readBoolean();
        m_zoomRatio = in.readFloat();
    }
    
    public void registerPopupActions( Action [] actions, Lookup lookup) {
        List<Action> factoryMenuActions = new ArrayList(Arrays.asList(actions));
            
        for (ComposerActionFactory factory : m_registeredActions) {
            Action a;
            if ( (a=factory.getMenuAction()) != null) {
                factoryMenuActions.add(a);
            }
        }
        
        actions = factoryMenuActions.toArray( new Action[factoryMenuActions.size()]);
        JPopupMenu popup = Utilities.actionsToPopup( actions, lookup);
        m_screenMgr.registerPopupMenu(popup);
    }
    
    public Action [] getMenuActions() {
        List<Action> factoryMenuActions = new ArrayList();
        
        for (ComposerActionFactory factory : m_registeredActions) {
            Action a;
            if ( (a=factory.getMenuAction()) != null) {
                factoryMenuActions.add(a);
            }
        } 
        return factoryMenuActions.toArray( new Action[factoryMenuActions.size()]);
    }

    public SVGDataObject getDataObject() {
        return m_dObj;
    }
    
    public Lookup getLoookup() {
        return m_lookup;
    }
    
    public PerseusController getPerseusController() {
        return m_perseusController;
    }

    public ScreenManager getScreenManager() {
        return m_screenMgr;
    }
    
    public SVGImage getSVGImage() {
        return m_svgImage;
    }

    public JComponent getComposerGUI() {
        return m_screenMgr.getComponent();
    }

    public void addSelectionListener( SelectionListener listener) {
        m_selectionListeners.add(listener);
    }

    public void removeSelectionListener( SelectionListener listener) {
        m_selectionListeners.remove(listener);
    }
    
    public boolean isReadOnly() {
        return m_isReadOnly;
    }
    
    public void setReadOnly(boolean isReadOnly) {
        if ( m_isReadOnly != isReadOnly) {
            m_isReadOnly = isReadOnly;
            updateStatusBar();
            if ( !m_isReadOnly) {
                m_perseusController.stopAnimator();
            }
            SVGObject [] selected = getSelected();
            notifySelectionChanged(selected, selected);
            m_screenMgr.repaint();
        }
    }
    
    public void setSelection(String id) {
        SVGObject selectedObj = m_perseusController.getObjectById(id);
        
        if (selectedObj != null) {
            SVGObject [] oldSelection = getSelected();

            SelectAction action = m_selectActionFactory.getActiveAction();
            if (action != null) {
                action.actionCompleted();
            }
            
            m_activeActions.push( m_selectActionFactory.startAction(selectedObj));
            ActionMouseCursor cursor = m_selectActionFactory.getMouseCursor(null, false);
            m_screenMgr.setCursor(cursor != null ? cursor.getCursor() : null);

            //TODO implement better selection change handling
            SVGObject [] newSelection = getSelected();
            if (!areSame(newSelection, oldSelection)) {
                selectionChanged(newSelection, oldSelection);
            }        
        }
    }
    
     void processEvent(InputEvent event) {
        boolean isOutsideEvent = event.getSource() != m_screenMgr.getAnimatorView();             
        SVGObject [] oldSelection = getSelected();

        //first let ongoing actions to process the event         
        boolean consumed = false;
        ActionMouseCursor cursor = null;
        
        for (int i = m_activeActions.size() - 1; i >= 0; i--) {
            ComposerAction action = m_activeActions.get(i);
            ActionMouseCursor c = action.getMouseCursor(isOutsideEvent);
            if (cursor == null && c != null) {
                cursor = c;
            }
            if ( action.consumeEvent(event, isOutsideEvent)) {
                consumed = true;
                break;
            }
            if (action.isCompleted()) {
                m_activeActions.remove(i);
            }
        }

        ComposerAction action = null;

        if ( !consumed) {
            //now check if the new action should be started
            for (int i = m_registeredActions.size() - 1; i >= 0; i--) {
                if ( (action=m_registeredActions.get(i).startAction(event, isOutsideEvent)) != null) {
                    m_activeActions.push(action);
                    break;
                }
            }
        } 

        if ( event instanceof MouseEvent && cursor == null) {
            MouseEvent me = (MouseEvent) event;
            for (int i = m_registeredActions.size() - 1; i >= 0; i--) {
                ActionMouseCursor c;
                if ( (c=m_registeredActions.get(i).getMouseCursor(me, isOutsideEvent)) != null) {
                    if (cursor == null || cursor.getPriority() < c.getPriority()) {
                        cursor = c;
                    }
                }
            }  
        }
        m_screenMgr.setCursor(cursor != null ? cursor.getCursor() : null);

        //TODO implement better selection change handling
        SVGObject [] newSelection = getSelected();
        if (!areSame(newSelection, oldSelection)) {
            selectionChanged(newSelection, oldSelection);
        }                    
    }

    public SVGObject [] getSelected() {
        SVGObject    selected = null;
        SelectAction action   = m_selectActionFactory.getActiveAction();
        
        if (action != null) {
            selected = action.getSelected();
        }
        if (selected != null) {
            return new SVGObject[] { selected };
        } else {
            return null;
        }
    }
    
    public Stack<ComposerAction> getActiveActions() {
        return m_activeActions;
    }

    public boolean containsAction( Class clazz) {
        for (int i = m_activeActions.size() - 1; i >= 0; i--) {
            if ( clazz.isInstance( m_activeActions.get(i))) {
                return true;
            }
        }
        return false;    
    }
    
    public void deleteObject(SVGObject svgObj) {
        SVGObject [] oldSelection = getSelected();
        svgObj.delete();
        SVGObject [] newSelection = getSelected();
        if (!areSame(newSelection, oldSelection)) {
            selectionChanged(newSelection, oldSelection);
        }                    
    }
        
    /**
     * Sets the position of the cursor. The position is in the component's coordinate
     * system.
     *
     * @param x the new position of the cursor along the x-axis.
     * @param y the new position of the cursor along the y-axis.
     */
    void popupAt(final int x, final int y) {
        SVGLocatableElement elem = m_perseusController._findElementAt(x, y);
        if (m_popupElement != null) {
            m_lookupContent.remove(m_popupElement);
        }
        
        m_popupElement = elem;
        if (m_popupElement != null){
            m_lookupContent.add(m_popupElement);
        }
        m_screenMgr.repaint();
    }
    
    protected void selectionChanged(SVGObject [] newSelection, SVGObject [] oldSelection) { 
        if (oldSelection != null) {
            for (int i = 0; i < oldSelection.length; i++) {
                m_lookupContent.remove(oldSelection[i]);
            }
        }

        if (newSelection != null && newSelection.length > 0) {
            for (int i = 0; i < newSelection.length; i++) {
                m_lookupContent.add(newSelection[i]);
            }
            //TODO use better mechanism for selection handling
            //m_dObj.getModel().setSelected( newSelection[0].getElementId());
        }  
        notifySelectionChanged(newSelection, oldSelection);
    }
    
    protected void notifySelectionChanged(SVGObject [] newSelection, SVGObject [] oldSelection) {
        for (SelectionListener listener : m_selectionListeners) {
            listener.selectionChanged(newSelection, oldSelection, m_isReadOnly);
        }
    }    
    
    //TODO move to SVGObject class
    protected static boolean areSame(SVGObject [] arr1,SVGObject [] arr2) {
        if (arr1 == arr2) {
            return true;
        } else if (arr1 == null || arr2 == null) {
            return false;
        } else if (arr1.length != arr2.length) {
            return false;
        } else {
            for (int i = 0; i < arr1.length; i++) {
                if ( arr1[i] != arr2[i]) {
                    return false;
                }
            }
            return true;
        }
    }

    private void updateStatusBar() {
        m_screenMgr.getStatusBar().setText( SVGStatusBar.CELL_MODE, m_isReadOnly ? SVGStatusBar.LOCKED : SVGStatusBar.UNLOCKED);
    }
    
/*    
    private void writeObject(ObjectOutputStream s) throws IOException {
        assert false : "WriteObject"; 
    }
    
    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
        assert false : "ReadObject";
    }
*/    
}   
