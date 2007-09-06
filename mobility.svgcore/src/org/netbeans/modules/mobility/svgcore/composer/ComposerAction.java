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

import java.awt.AWTEvent;
import java.awt.Graphics;

/**
 *
 * @author Pavel Benes
 */
public interface ComposerAction {
    public boolean consumeEvent(AWTEvent event, boolean isOutsideEvent);
    
    public ActionMouseCursor getMouseCursor(boolean isOutsideEvent);

    public void actionCompleted();

    public void paint(Graphics g, int x, int y, boolean isReadOnly);

    public boolean isCompleted();
}
