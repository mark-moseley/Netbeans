/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.modules.form.fakepeer;

import java.awt.*;
import java.awt.peer.ContainerPeer;

/**
 *
 * @author Tran Duc Trung
 */

abstract class FakeContainerPeer extends FakeComponentPeer 
{
    private Insets _insets;

    FakeContainerPeer(Container target) {
        super(target);
    }

    public Insets getInsets() {
        return insets();
    }

    public void beginValidate() {
    }

    public void endValidate() {
    }

    // JDK 1.4
    public void beginLayout() {
    }

    // JDK 1.4
    public void endLayout() {
    }

    // JDK 1.4
    public boolean isPaintPending() {
        return false;
    }

    // JDK 1.5
    public void cancelPendingPaint(int x, int y, int w, int h) {
    }

    // JDK 1.5
    public void restack() {
    }

    // JDK 1.5
    public boolean isRestackSupported() {
        return false;
    }

    // deprecated
    public Insets insets() {
        if (_insets == null)
            _insets = new Insets(0, 0, 0, 0);
        return _insets;
    }
}
