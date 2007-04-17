/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.spi.diff;

import org.netbeans.api.diff.DiffController;

import javax.swing.*;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyChangeListener;

/**
 * Provider class for DiffController, encapsulates a single Diff panel that displays differences between two files (sources).
 * 
 * @author Maros Sandor
 * @since 1.18
 */
public abstract class DiffControllerImpl {
    
    /**
     * A PropertyChangeSupport instance.
     */
    protected final PropertyChangeSupport support = new PropertyChangeSupport(this);

    private int differenceIndex = -1;

    /**
     * Default constructor, does nothing.
     */
    protected DiffControllerImpl() {
    }

    /**
     * Ensure the requested location in the Diff view is visible on screen. Diff view can be requested to jump to
     * a given line in either source or to a given Difference.
     * Diff controller may ignore the request if it does not support this functionality.
     * This method must be called from AWT thread.
     * 
     * @param pane defines which pane the location parameter refers to
     * @param type defines the location parameter, see below
     * @param location depending on the type parameter this defines either a line number or a Difference index, both 0-based
     * @throws IllegalArgumentException if location parameter is out of range for the given pane and location type
     */
    public void setLocation(DiffController.DiffPane pane, DiffController.LocationType type, int location) {
    }

    /**
     * Retrieves visual presenter of the Diff.
     * 
     * @return JComponent component to be embedded into client UI
     */
    public abstract JComponent getJComponent();

    /**
     * Gets total number of Differences between sources currently displayed in the Diff view.
     * 
     * @return total number of Differences in sources, an integer >= 0
     */
    public abstract int getDifferenceCount();

    /**
     * Gets the current (highlighted) difference in the Diff view.
     * 
     * @return current difference index or -1 of there is no Current difference
     */
    public final int getDifferenceIndex() {
        return differenceIndex;
    }

    /**
     * Implementors use this method to set the current (highlighted) difference. 
     * 
     * @param idx a new current difference indox
     */
    protected final void setDifferenceIndex(int idx) {
        assert idx >= -1;
        if (idx == differenceIndex) return;
        differenceIndex = idx;
        support.firePropertyChange(DiffController.PROP_DIFFERENCES, null, null);
    }
    
    /**
     * Adds a property change listener.
     * 
     * @param listener property change listener
     */
    public final void addPropertyChangeListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }
    
    /**
     * Removes a property change listener.
     * 
     * @param listener property change listener
     */
    public final void removePropertyChangeListener(PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
    }
}
