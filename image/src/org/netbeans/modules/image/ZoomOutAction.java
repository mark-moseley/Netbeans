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


package org.netbeans.modules.image;


import org.openide.util.actions.CallableSystemAction;
import org.openide.util.HelpCtx;
import org.openide.windows.TopComponent;
import org.openide.util.NbBundle;


/**
 * Action which zooms out of an image.
 *
 * @author  Lukas Tadial
 */
public class ZoomOutAction extends CallableSystemAction {

    /** Generated serial version UID. */
    static final long serialVersionUID = 1859897546585041051L;

    
    /** Peforms action. */
    public void performAction() {
        TopComponent curComponent = TopComponent.getRegistry().getActivated();
        if(curComponent instanceof ImageViewer)
            ((ImageViewer) curComponent).zoomOut();
        
    }

    /** Gets name of action. Implements superclass abstract method. */
    public String getName() {
        return NbBundle.getBundle(ZoomOutAction.class).getString("LBL_ZoomOut");
    }
    
    /** Gets help context for action. Implements superclass abstract method. */
    public org.openide.util.HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    /** Overrides superclass method. */
    public boolean isEnabled() {
        return true;
    }
    
    /** Gets icon resource. Overrides superclass method. */
    protected String iconResource() {
        return "org/netbeans/modules/image/zoomOut.gif"; // NOI18N
    }
}
