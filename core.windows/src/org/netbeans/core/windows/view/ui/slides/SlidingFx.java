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

package org.netbeans.core.windows.view.ui.slides;

import java.awt.Rectangle;
import javax.swing.JLayeredPane;
import javax.swing.event.ChangeListener;

/*
 * Interface for slide in and slide out operations. Acts as command interface
 * for desktop part of winsys to be able to request slide operation.
 *
 * @author Dafe Simonek
 */
public interface SlidingFx {
    
    public void prepareEffect (SlideOperation operation);
    
    public void showEffect(JLayeredPane pane, Integer layer, SlideOperation operation);
    
    public boolean shouldOperationWait();
    
    public void setFinishListener(ChangeListener finishL);
    
}