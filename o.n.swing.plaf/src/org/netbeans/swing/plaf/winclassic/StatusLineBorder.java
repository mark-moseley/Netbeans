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
 /*
 * StatusLineBorder.java
 *
 * Created on March 14, 2004, 4:36 AM
 */

package org.netbeans.swing.plaf.winclassic;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import java.awt.*;

/**
 *
 * @author  Dafe Simonek
 */
class StatusLineBorder extends AbstractBorder {
     
    /** Constants for sides of status line border */
    public static final int LEFT = 1;
    public static final int TOP = 2;
    public static final int RIGHT = 4;

    private Insets insets;

    private int type;

    /** Constructs new status line border of specified type. Type is bit
     * mask specifying which sides of border should be visible */
    public StatusLineBorder(int type) {
        this.type = type;
    }

    public void paintBorder(Component c, Graphics g, int x, int y,
    int w, int h) {
        g.translate(x, y);
        Color shadowC = UIManager.getColor("controlShadow"); //NOI18N
        Color highlightC = UIManager.getColor("controlLtHighlight"); //NOI18N
        Color middleC = UIManager.getColor("control"); //NOI18N
        // top
        if ((type & TOP) != 0) {
            g.setColor(shadowC);
            g.drawLine(0, 0, w - 1, 0);
            g.drawLine(0, 3, w - 1, 3);
            g.setColor(highlightC);
            g.drawLine(0, 1, w - 1, 1);
            g.setColor(middleC);
            g.drawLine(0, 2, w - 1, 2);
        }
        // left side
        if ((type & LEFT) != 0) {
            g.setColor(middleC);
            g.drawLine(0, 2, 0, h - 1);
            g.setColor(shadowC);
            g.drawLine(1, 3, 1, h - 1);
        }
        // right side
        if ((type & RIGHT) != 0) {
            g.setColor(shadowC);
            g.drawLine(w - 2, 3, w - 2, h - 1);
            g.setColor(highlightC);
            g.drawLine(w - 1, 4, w - 1, h - 1);
            g.setColor(middleC);
            g.drawLine(w - 1, 3, w - 1, 3);
        }

        g.translate(-x, -y);
    }

    public Insets getBorderInsets(Component c) {
        if (insets == null) {
            insets = new Insets((type & TOP) != 0 ? 4 : 0,
            (type & LEFT) != 0 ? 2 : 0, 0,
            (type & RIGHT) != 0 ? 2 : 0);
        }
        return insets;
    }

}
