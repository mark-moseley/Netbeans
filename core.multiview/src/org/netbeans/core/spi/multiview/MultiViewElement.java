/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.core.spi.multiview;

import java.util.TooManyListenersException;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JToolBar;
import org.openide.awt.UndoRedo;
import org.openide.util.Lookup;

/** View element for multi view, provides the UI components to the multiview component. 
 * Gets notified by the enclosing component about the changes in the lifecycle.
 *
 * @author  Dafe Simonek, Milos Kleint
 */
public interface MultiViewElement {

    /** Returns Swing visual representation of this multi view element. Should be relatively fast
     * and always return the same component.
     */
    JComponent getVisualRepresentation ();
    
    /**
     * Returns the visual component with the multi view element's toolbar.Should be relatively fast as it's called
     * everytime the current perspective is switched.
     */
    JComponent getToolbarRepresentation ();
    
    /**
     * Gets the actions which will appear in the popup menu of this component.
     * <p>Subclasses are encouraged to use add the default TopComponent actions to
     * the array of their own. These are accessible by calling MultiViewElementCallback.createDefaultActions()
     *<pre>
     * <code>
     *          public Action[] getActions() {
     *             Action[] retValue;
     *             // the multiviewObserver was passed to the element in setMultiViewCallback() method.
     *             if (multiViewObserver != null) {
     *                 retValue = multiViewObserver.createDefaultActions();
     *                 // add you own custom actions here..
     *             } else {
     *                 // fallback..
     *                 retValue = super.getActions();
     *             }
     *             return retValue;
     *         }
     *   </code>
     *</pre>
     * @return array of actions for this component
     */
    Action[] getActions();

    /**
     * Lookup for the MultiViewElement. Will become part of the TopComponent's lookup.
     * @return the lookup to use when the MultiViewElement is active.
     */
    Lookup getLookup();
    
    
    /** Called only when enclosing multi view top component was closed before and
     * now is opened again for the first time. The intent is to
     * provide subclasses information about multi view TopComponent's life cycle.
     * Subclasses will usually perform initializing tasks here.
     */
    void componentOpened();
    
    /** Called only when multi view top component was closed. The intent is 
     * to provide subclasses information about TopComponent's life cycle.
     * Subclasses will usually perform cleaning tasks here.
     */
    void componentClosed();

    /** Called when this MultiViewElement is about to be shown. 
     * That can happen when switching the current perspective/view or when the topcomonent itself is shown for the first time.
     */
    void componentShowing();
    
    /** Called when this MultiViewElement was hidden. This happens when other view replaces this one as the selected view or
     * when the whole topcomponent gets hidden (eg. when user selects anothe topcomponent in the current mode).
     */
    void componentHidden();
    
    /** Called when this multi view element is activated.
    * This happens when the parent window of this component gets focus
    * (and this component is the preferred one in it), <em>or</em> when
    * this component is selected in its window (and its window was already focused).
    */
    void componentActivated ();

    /** Called when this multi view element is deactivated.
    * This happens when the parent window of this component loses focus
    * (and this component is the preferred one in the parent),
    * <em>or</em> when this component loses preference in the parent window
    * (and the parent window is focussed).
    */
    void componentDeactivated ();
    
    /**
     * UndoRedo support, 
     * Get the undo/redo support for this element.
     *
     * @return undoable edit for this component, null if not implemented.
     */
    UndoRedo getUndoRedo();
    

    /**
     * The enclosing window enviroment attaches the callback right after creating 
     * the element from the description. Same applies for deserialization of MultiViewTopComponent.
     * Implementors shall not attempt to serialize the passed instance.
     */ 
    void setMultiViewCallback (MultiViewElementCallback callback);
    
    /**
     * Element decides if it can be safely closed. The element shall just return a value 
     * (created by MultiViewFactory.createCloseState() factory method),
     * not open any UI, that is a semantical difference from TopComponent.canClose().
     * The CloseOperationHandler is the centralized place to show dialogs to the user.
     * In cases when the element is consistent, just return CloseOperationState.STATE_OK.
     */
    CloseOperationState canCloseElement();
    
   
}