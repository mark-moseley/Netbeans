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

package org.netbeans.modules.xml.xam;

import java.beans.PropertyChangeListener;
import javax.swing.event.UndoableEditListener;

/**
 * Interface describing an abstract model. The model is based on a
 * document representation that represents the persistent form.
 * @author Chris Webster
 * @author Nam Nguyen
 * @author Rico Cruz
 */
public interface Model<C extends Component<C>> extends Referenceable {
    public static final String STATE_PROPERTY = "state";
    
    /**
     * Add coarse-grained change listener for events on model components.
     */
    public void removeComponentListener(ComponentListener cl);

    /**
     * Remove component event listener.
     */
    public void addComponentListener(ComponentListener cl);

    /**
     * Add fine-grained property change listener for events on model components.
     */
    public void addPropertyChangeListener(PropertyChangeListener pcl);

    /**
     * Remove property change listener.
     */
    public void removePropertyChangeListener(PropertyChangeListener pcl);

    /**
     * Removes undoable edit listener.
     */
    void removeUndoableEditListener(UndoableEditListener uel);

    /**
     * Adds undoable edit listener.
     */
    void addUndoableEditListener(UndoableEditListener uel);

    /**
     * Removes undoable refactoring edit listener.  This will also restored
     * the existing undoable edit listeners to the set before the start of
     * refactoring.  Note, if these listeners are UndoManager instances
     * their queues are cleared of existing edits.
     */
    void removeUndoableRefactorListener(UndoableEditListener uel);

    /**
     * Adds undoable refactoring edit listener.  This is typically called by a
     * refactoring manager before start refactoring changes.  This
     * will also save existing undoable edit listeners.  Note, if these listeners
     * are UndoManager instances, their queues will be cleared of existing edits.
     */
    void addUndoableRefactorListener(UndoableEditListener uel);

    /**
     * make the current memory model consistent with the underlying
     * representation, typically a swing document. 
     */
    void sync() throws java.io.IOException;
    
    /**
     * return true if sync is being performed. 
     */
    boolean inSync();
    
    /**
     * State of the model.
     * VALID - Source is well-formed and model is in-sync.
     * NOT_WELL_FORMED - Source is not well-formed, model is not synced.
     * NOT_SYNCED - Source is well-formed, but there was error from last sync.
     */
    enum State {
        VALID, 
        NOT_WELL_FORMED,
        NOT_SYNCED
    }
    /**
     * @return the last known state of the document. This method is affected
     * by invocations of #sync().
     */
    State getState();
    
    /**
     * @return true if model is in middle of transformation tranasction.
     */
    boolean isIntransaction();
    
    /** 
     * This method will block until a transaction can be started. A transaction
     * in this context will fire events (such as property change) when 
     * #endTransaction() has been invoked. A transaction must be 
     * be acquired during a mutation, reading can be performed without
     * a transaction. Only a single transaction at a time is supported. Mutations
     * which occur based on events will not be reflected until the transaction
     * has completed.
     * @return true if transaction is acquired successfully, else false, for example
     * if model has transitioned into invalid state.
     */
    boolean startTransaction();
    
    /**
     * This method stops the transaction and causes all events to be fired. 
     * After all events have been fired, the document representation will be 
     * modified to reflect the current value of the model (flush). 
     */
    void endTransaction();
    
    /**
     * Add child component at specified index.
     * @param target the parent component.
     * @param child the child component to be added.
     * @param index position among same type of child components, or -1 if not relevant.
     */
    void addChildComponent(Component target, Component child, int index);
    
    /**
     * Remove specified component from model.
     */
    void removeChildComponent(Component child);

    /**
     * @return the source of this model or null if this model does associate
     * with any model source.
     */
    ModelSource getModelSource();

}
