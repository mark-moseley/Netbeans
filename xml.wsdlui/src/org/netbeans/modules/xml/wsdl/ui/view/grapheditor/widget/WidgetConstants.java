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

package org.netbeans.modules.xml.wsdl.ui.view.grapheditor.widget;

import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.border.Border;

import org.netbeans.modules.xml.wsdl.ui.view.grapheditor.border.GradientFillBorder;

/**
 * Defines constants to be used throughout the widget classes.
 *
 * @author Nathan Fiedler
 */
public interface WidgetConstants {
    /** The minimum widget for the top-level widgets (e.g. collaborations). */
    public int MINIMUM_WIDTH = 600;
    
    public Color HIT_POINT_BORDER = new Color(0xE68B2C);

    public Color FAULT_ARROW_COLOR = Color.RED;
    
    public Color INPUT_OUTPUT_ARROW_COLOR = new Color(0x3244A0);

    public Color SELECTION_COLOR = new Color(0xff6600);

    public Color GRADIENT_TOP_COLOR = new Color(0xADCFEF);
    
    public Color GRADIENT_BOTTOM_COLOR = Color.WHITE;
    
    public Color DISABLED_GRAY = new Color(0xDDDDDD);
    
    public Border OUTER_BORDER = BorderFactory.createMatteBorder(2, 2, 2, 2, Color.BLACK);
    
    public org.netbeans.api.visual.border.Border GRADIENT_BLUE_WHITE_BORDER = new GradientFillBorder(0, 0, 4, 8,
            null, WidgetConstants.GRADIENT_TOP_COLOR, WidgetConstants.GRADIENT_BOTTOM_COLOR);

    
    
}
