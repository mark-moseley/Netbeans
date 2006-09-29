/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.api.visual.action;

import org.netbeans.api.visual.widget.Widget;
import org.netbeans.api.visual.widget.Scene;

import java.awt.*;

/**
 * This interface controls a connect action.
 *
 * @author David Kaspar
 */
public interface ConnectProvider {

    /**
     * Called for checking whether a specified source widget is a possible source of a connection.
     * @param sourceWidget the source widget
     * @return if true, then it is possible to create a connection for the source widget; if false, then is not allowed
     */
    boolean isSourceWidget (Widget sourceWidget);

    /**
     * Called for checking whether a connection could be created between a specified source and target widget.
     * Called only when a hasCustomTargetWidgetResolver returns false.
     * @param sourceWidget the source widget
     * @param targetWidget the target widget
     * @return the connector state
     */
    ConnectorState isTargetWidget (Widget sourceWidget, Widget targetWidget);

    /**
     * Called to check whether the provider has a custom target widget resolver.
     * @param scene the scene where the resolver will be called
     * @return if true, then the resolveTargetWidget method is called for resolving the target widget;
     *         if false, then the isTargetWidget method is called for resolving the target widget
     */
    boolean hasCustomTargetWidgetResolver (Scene scene);

    /**
     * Called to find the target widget of a possible connection.
     * Called only when a hasCustomTargetWidgetResolver returns true.
     * @param scene the scene
     * @param sceneLocation the scene location
     * @return the target widget; null if no target widget found
     */
    Widget resolveTargetWidget (Scene scene, Point sceneLocation);

    /**
     * Called for creating a new connection between a specified source and target widget.
     * This method is called only when the possible connection is available and an user approves its creation.
     * @param sourceWidget the source widget
     * @param targetWidget the target widget
     */
    void createConnection (Widget sourceWidget, Widget targetWidget);

}
