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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.visual.border;

import org.netbeans.modules.visual.util.RenderUtil;
import org.netbeans.api.visual.border.Border;

import java.awt.*;

/**
 * @author David Kaspar
 */
public final class LineBorder implements Border {

    private Insets insets;
    private Color color;

    public LineBorder (int top, int left, int bottom, int right, Color color) {
        insets = new Insets (top, left, bottom, right);
        this.color = color;
    }

    public Insets getInsets () {
        return insets;
    }

    public void paint (Graphics2D gr, Rectangle bounds) {
        gr.setColor (color);
        RenderUtil.drawRect (gr, bounds);
    }

    public boolean isOpaque () {
        return true;
    }

}
