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
 *
 * BaseTabLayoutModel.java
 *
 * Created on May 16, 2003, 4:22 PM
 */

package org.netbeans.swing.tabcontrol.plaf;

import org.netbeans.swing.tabcontrol.TabDataModel;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Default implementation of TabLayoutModel.  Simply provides a series of
 * rectangles for each tab starting at 0 and ending at the last element, with
 * the width set to the calculated width for the string plus a padding value
 * assigned in <code>setPadding</code>.
 * <p>
 * To implement TabLayoutModel, it is often useful to create an implementation which
 * wraps an instance of <code>DefaultTabLayoutModel</code>, and uses it to calculate
 * tab sizes.
 *
 * <strong>Do not use this class directly, use DefaultTabLayoutModel - this class
 * exists to enable unit tests to provide a subclass</strong>
 *
 * @author Tim Boudreau
 */
class BaseTabLayoutModel implements TabLayoutModel {
    protected TabDataModel model;
    protected int textHeight = -1;
    protected int padX = 5;
    protected int padY = 5;
    protected JComponent renderTarget;

    protected BaseTabLayoutModel(TabDataModel model, JComponent renderTarget) {
        this.model = model;
        this.renderTarget = renderTarget;
    }

    private Font getFont() {
        return renderTarget.getFont();
    }

    protected int iconWidth(int index) {
        Icon ic = model.getTab(index).getIcon();
        int result;
        if (ic != null) {
            result = ic.getIconWidth();
        } else {
            result = 0;
        }
        return result;
    }
    
    protected int iconHeight (int index) {
        Icon ic = model.getTab(index).getIcon ();
        int result;
        if (ic != null) {
            result = ic.getIconHeight();
        } else {
            result = 0;
        }
        return result;
    }
    
    protected int textWidth(int index) {
        try {
            String text = model.getTab(index).getText();
            return textWidth(text);
        } catch (NullPointerException npe) {
            IllegalArgumentException iae = new IllegalArgumentException(
                    "Error fetching width for tab " + //NOI18N
                    index
                    + " - model size is "
                    + model.size()
                    + " TabData is " + //NOI18N
                    model.getTab(index)
                    + " model contents: "
                    + model); //NOI18N
            throw iae;
        }
    }

    private static Map widthMap = new HashMap(31);

    private int textWidth(String text) {
        //Note:  If we choose to support multiple fonts in different
        //tab controls in the system, make the cache non-static and
        //dump it if the font changes.
        Integer result = (Integer) widthMap.get(text);
        if (result == null) {
            double wid = Html.renderString(text, TabListPopup.getOffscreenGraphics(), 0, 0,
                                           Integer.MAX_VALUE,
                                           Integer.MAX_VALUE, getFont(),
                                           Color.BLACK, Html.STYLE_TRUNCATE,
                                           false);
            result = new Integer(Math.round(Math.round(wid)));
            widthMap.put(text, result);
        }
        return result.intValue();
    }

    protected int textHeight(int index) {
        if (textHeight == -1) {
            //No need to calculate for every string
            String testStr = "Zgj"; //NOI18N
            Font f = getFont();
            textHeight = new Double(f.getStringBounds(testStr, 
            TabListPopup.getOffscreenGraphics().getFontRenderContext()).getWidth()).intValue() + 2;
        }
        return textHeight;
    }

    public int getX(int index) {
        int result = renderTarget.getInsets().left;
        for (int i = 0; i < index; i++) {
            result += getW(i);
        }
        return result;
    }

    public int getY(int index) {
        return renderTarget.getInsets().top;
    }

    public int getH(int index) {
        return Math.max (textHeight(index) + padY, model.getTab(index).getIcon().getIconHeight() + padY);
    }

    public int getW(int index) {
        return textWidth(index) + iconWidth(index) + padX;
    }

    public int indexOfPoint(int x, int y) {
        int max = model.size();
        int pos = renderTarget.getInsets().left;
        for (int i = 0; i < max; i++) {
            pos += getW(i);
            if (pos > x) {
                return i;
            }
        }
        return -1;
    }

    public int dropIndexOfPoint(int x, int y) {
        Insets insets = renderTarget.getInsets();
        int contentWidth = renderTarget.getWidth()
                - (insets.left + insets.right);
        int contentHeight = renderTarget.getHeight()
                - (insets.bottom + insets.top);
        if (y < insets.top || y > contentHeight || x < insets.left
                || x > contentWidth) {
            return -1;
        }
        int max = model.size();
        int pos = insets.left;
        for (int i = 0; i < max; i++) {
            int delta = getW(i);
            pos += delta;
            if (x <= (pos - delta / 2)) {
                return i;
            } else if (x < pos) {
                return i + 1;
            }
        }
        return max;
    }

    public void setPadding(Dimension d) {
        padX = d.width;
        padY = d.height;
    }
}
