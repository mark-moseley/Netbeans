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
package org.netbeans.modules.visual.layout;

import org.netbeans.api.visual.widget.Widget;
import org.netbeans.api.visual.layout.Layout;
import org.netbeans.api.visual.layout.SceneLayout;

/**
 * @author David Kaspar
 */
public final class DevolveWidgetLayout extends SceneLayout {

    private Widget widget;
    private Layout devolveLayout;
    private boolean animate;

    public DevolveWidgetLayout (Widget widget, Layout devolveLayout, boolean animate) {
        super (widget.getScene ());
        assert devolveLayout != null;
        this.widget = widget;
        this.devolveLayout = devolveLayout;
        this.animate = animate;
    }

    protected void performLayout () {
        devolveLayout.layout (widget);
        for (Widget child : widget.getChildren ()) {
            if (animate)
                widget.getScene ().getSceneAnimator ().animatePreferredLocation (child, child.getLocation ());
            else
                child.setPreferredLocation (child.getLocation ());
            child.revalidate ();
        }
    }

}
