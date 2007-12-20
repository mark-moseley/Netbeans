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

import org.netbeans.api.visual.layout.Layout;
import org.netbeans.modules.xml.wsdl.ui.view.grapheditor.border.GradientFillBorder;
import org.netbeans.modules.xml.wsdl.ui.view.grapheditor.layout.LeftRightLayout;

/**
 * Defines constants to be used throughout the widget classes.
 *
 * @author Nathan Fiedler
 * @author Shivanand Kini
 */
public interface WidgetConstants {
    /** The minimum width for message widget */
    public int MESSAGE_MINIMUM_WIDTH = 550;
    /** The minimum widget for the top-level widgets (e.g. collaborations). */
    public int PARTNERLLINKTYPE_MINIMUM_WIDTH = 625;
    
    public int ROLE_WIDGET_MINIMUM_WIDTH = 225;
    
    public int PORTTYPE_MINIMUM_WIDTH = 190;
    
    /** Minimum width for partnerlinktypes and messages header*/
    public int HEADER_MINIMUM_WIDTH = 700;
    
    public Color HIT_POINT_BORDER = new Color(0xE68B2C);

    /** Color for fault arrows*/
    public Color FAULT_ARROW_COLOR = Color.RED;
    
    /** Color for input/output arrows*/
    public Color INPUT_OUTPUT_ARROW_COLOR = new Color(0x3244A0);

    /** Color for selections in partner view*/
    public Color SELECTION_COLOR = new Color(0xff6600);

    /** Starting color for gradient in headers*/
    public Color GRADIENT_TOP_COLOR = Color.WHITE;
    /** Ending color for gradient in headers*/
    public Color GRADIENT_BOTTOM_COLOR = new Color(0xADCFEF);
    
    /** Color for showing disabled widgets*/
    public Color DISABLED_GRAY = new Color(0xDDDDDD);
    
    /** Border for message and partnerlinktype widgets*/
    public Border OUTER_BORDER = BorderFactory.createMatteBorder(2, 2, 2, 2, Color.BLACK);
    
    /** Header gradient border*/
    public org.netbeans.api.visual.border.Border GRADIENT_BLUE_WHITE_BORDER = new GradientFillBorder(0, 0, 4, 8,
            null, Color.WHITE, new Color(0xADCFEF));
    
    public org.netbeans.api.visual.border.Border GRADIENT_GRAY_WHITE_BORDER = new GradientFillBorder(0, 0, 4, 8,
            null, Color.WHITE, DISABLED_GRAY);
    public org.netbeans.api.visual.border.Border GRADIENT_GREEN_WHITE_BORDER = new GradientFillBorder(0, 0, 4, 8,
            null, Color.WHITE, Color.GREEN);
    
    /** Gap between header and content */
    public int GAP_BETWEEN_HEADER_AND_CONTENT = 15;
    
    /** Gap between child widgets in content*/
    public int GAP_BETWEEN_CHILD_WIDGETS = 16;
    
    /** Empty border for header widgets */
    public org.netbeans.api.visual.border.Border HEADER_BORDER = org.netbeans.api.visual.border.BorderFactory.createEmptyBorder(5, 0);
    
    public int TEXT_LABEL_HEIGHT = 25;
    
    public Border EMPTY_2PX_BORDER = BorderFactory.createEmptyBorder(2, 4, 2, 4);
    public Layout HEADER_LAYOUT = new LeftRightLayout (32);
    public int RECTANGLE_WIDGET_THICKNESS = 2;
    public int OPERATION_WIDGET_BORDER_THICKNESS = 3;

    
    
}
