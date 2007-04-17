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

package org.netbeans.modules.vmd.api.screen.display;

import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.Presenter;

import javax.swing.*;
import java.awt.*;
import java.util.Collection;
import java.util.Collections;

/**
 * A base presenter for the device display component. This presenter occupies the whole device display visible in the screen editor and
 * is also responsible for its painting.
 *
 * @author breh
 */
public abstract class ScreenDisplayPresenter extends Presenter {

    public final DesignComponent getRelatedComponent () {
        return getComponent ();
    }

    public abstract boolean isTopLevelDisplay ();

    /**
     * Gets children DesignComponent. Component elements are visible elements representing design components in this display.
     * @return the children component
     */
    public abstract Collection<DesignComponent> getChildren ();

    /**
     * Gets actual view of the component. The JComponent is supposed to 
     * cooperate nicely with various LayoutManagers (in MIDP there
     * are specific layout managers)
     * @return the view
     */
    public abstract JComponent getView();

    /**
     * Called immediately before the view component is to be added to the view tree. This method call is done in AWT thread.
     * @param deviceInfo the device info
     */
    public abstract void reload (ScreenDeviceInfo deviceInfo);

//    /**
//     * Gets selection shape at given point (within this display coordinates)
//     * @param point
//     * @return
//     */
//    public abstract Shape getHoverShape(Point point);

    /**
     * Gets selection shape at given point (within this display coordinates)
     * @return the shape
     */
    public abstract Shape getSelectionShape();
//    public abstract Shape getSelectionShape(Point point);

    /**
     * Returns a collection of all screen property descriptors
     * @return the collection
     */
    public abstract Collection<ScreenPropertyDescriptor> getPropertyDescriptors ();// {
      //  return Collections.emptySet ();
   // }

}
