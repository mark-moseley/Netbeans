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
/*
 * WinClassicCompBorder.java
 *
 * Created on March 14, 2004, 8:34 PM
 */

package org.netbeans.swing.plaf.winclassic;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

/** Lower border for the tab control
 *
 * @author  Dafe Simonek
 */
public class WinClassicCompBorder implements Border {
        
    private static final Insets insets = new Insets(0, 2, 2, 2);

    public Insets getBorderInsets(Component c) {
        return insets;
    }

    public boolean isBorderOpaque() {
        return true;
    }

    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        g.translate(x, y);
        g.setColor(UIManager.getColor("InternalFrame.borderShadow")); //NOI18N
        g.drawLine(0, 0, 0, height - 1);
        g.setColor(UIManager.getColor("InternalFrame.borderDarkShadow")); //NOI18N
        g.drawLine(1, 0, 1, height - 2);
        g.setColor(UIManager.getColor("InternalFrame.borderHighlight")); //NOI18N
        g.drawLine(1, height - 1, width - 1, height - 1);
        g.drawLine(width - 1, height - 2, width - 1, 0);
        g.setColor(UIManager.getColor("InternalFrame.borderLight")); //NOI18N
        g.drawLine(2, height - 2, width - 2, height - 2);
        g.drawLine(width - 2, height - 3, width - 2, 0);
        g.translate(-x, -y);
    }
    
}
