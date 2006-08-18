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
 * @author David Kaspar
 */
public interface ConnectProvider {

    public enum ConnectorState {

        ACCEPT, REJECT, REJECT_AND_STOP

    }

    boolean isSourceWidget (Widget sourceWidget);

    ConnectorState isTargetWidget (Widget sourceWidget, Widget targetWidget);

    boolean hasCustomTargetWidgetResolver (Scene scene);

    Widget resolveTargetWidget (Scene scene, Point sceneLocation);

    void createConnection (Widget sourceWidget, Widget targetWidget);

}
