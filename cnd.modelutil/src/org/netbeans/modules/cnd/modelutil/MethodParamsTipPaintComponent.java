/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.modelutil;

import java.awt.*;
import java.util.Iterator;
import java.util.List;
import javax.swing.*;

/**
 *
 * @author  Dusan Balek
 */
public class MethodParamsTipPaintComponent extends JToolTip {
    
    private int drawX;
    private int drawY;
    private int drawHeight;
    private int drawWidth;
    private Font drawFont;
    private int fontHeight;
    private int ascent;
    private FontMetrics fontMetrics;

    private List/*<List<String>>*/ params;
    private int idx;

    public MethodParamsTipPaintComponent(List params, int idx){
        super();
        this.params = params;
        this.idx = idx;
    }
    
    public void paintComponent(Graphics g) {
        // clear background
        g.setColor(getBackground());
        Rectangle r = g.getClipBounds();
        g.fillRect(r.x, r.y, r.width, r.height);
        g.setColor(getForeground());
        draw(g);
    }

    protected void draw(Graphics g) {
        Insets in = getInsets();
        if (in != null) {
            drawX = in.left;
            drawY = in.top;
        } else {
            drawX = 0;
            drawY = 0;
        }
        drawHeight = fontHeight;
        if (in != null) {
            drawHeight += in.bottom;
        }
        drawHeight += drawY;
        drawY += ascent;

        int startX = drawX;
        drawWidth = drawX;
        int i = 0;
        for (Iterator it = params.iterator(); it.hasNext(); i = 0) {
            for (Iterator itt = ((List)it.next()).iterator(); itt.hasNext(); i++) {
                String s = (String) itt.next();
                drawString(g, s, i == idx ? getDrawFont().deriveFont(Font.BOLD) : null);
            }
            if (drawWidth < drawX)
                drawWidth = drawX;
            drawY += drawHeight;
            drawX = startX;
        }
    }

    protected void drawString(Graphics g, String s, Font font) {
        if (g != null) {
            g.setFont(font);
            g.drawString(s, drawX, drawY);
            g.setFont(drawFont);
        }
        drawX += getWidth(s, font);
    }

    protected int getWidth(String s, Font font) {
        if (font == null) return fontMetrics.stringWidth(s);
        return getFontMetrics(font).stringWidth(s);
    }

    protected int getHeight(String s, Font font) {
        if (font == null) return fontMetrics.stringWidth(s);
        return getFontMetrics(font).stringWidth(s);
    }

    public void setFont(Font font) {
        super.setFont(font);
        fontMetrics = this.getFontMetrics(font);
        fontHeight = fontMetrics.getHeight();
        ascent = fontMetrics.getAscent();
        drawFont = font;
    }

    protected Font getDrawFont(){
        return drawFont;
    }

    public Dimension getPreferredSize() {
        draw(null);
        Insets i = getInsets();
        if (i != null) {
            drawX += i.right;
        }
        return new Dimension(drawWidth, drawHeight * params.size());
    }

}
