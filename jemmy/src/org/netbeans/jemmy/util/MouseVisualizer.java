/*
 * Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License Version
 * 1.0 (the "License"). You may not use this file except in compliance with
 * the License. A copy of the License is available at http://www.sun.com/
 * 
 * The Original Code is the Jemmy library.
 * The Initial Developer of the Original Code is Alexandre Iline.
 * All Rights Reserved.
 * 
 * Contributor(s): Alexandre Iline.
 * 
 * $Id$ $Revision$ $Date$
 * 
 */

package org.netbeans.jemmy.util;

import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.TestOut;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jemmy.Timeouts;

import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JInternalFrameOperator;
import org.netbeans.jemmy.operators.JScrollPaneOperator;
import org.netbeans.jemmy.operators.JTabbedPaneOperator;
import org.netbeans.jemmy.operators.Operator.ComponentVisualizer;
import org.netbeans.jemmy.operators.WindowOperator;

import java.awt.Component;
import java.awt.Container;
import java.awt.Point;

import javax.swing.JInternalFrame;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

/**
 *
 * Activates windows by robot mouse click on border.
 * 
 * @see org.netbeans.jemmy.operators.Operator#setVisualizer(Operator.ComponentVisualizer)
 * @see org.netbeans.jemmy.operators.Operator.ComponentVisualizer
 *
 * @author Alexandre Iline (alexandre.iline@sun.com)
 * 
 */
public class MouseVisualizer extends DefaultVisualizer {


    public static int TOP = 0;
    public static int BOTTOM = 1;
    public static int LEFT = 2;
    public static int RIGHT = 3;

    private int place = 0;
    private double pointLocation = 0;
    private int depth = 0;
    private boolean checkMouse = false;

    /**
     * Creates a visualizer which clicks on (0, 0) window coords.
     */
    public MouseVisualizer() {
    }
    /**
     * Creates a visualizer which clicks on window boder.
     * In case if <code>place == BOTTOM</code>, for example 
     * clicks on (width * pointLocation, depth) coordinates.
     * @param place One of the predefined value: TOP, BOTTOM, LEFT, RIGHT
     * @param pointLocation Proportial coordinates to click.
     * @param depth Distance from the border.
     * @param checkMouse Check if there is any java component under mouse
     */
    public MouseVisualizer(int place, double pointLocation, int depth, boolean checkMouse) {
	this.place = place;
	this.pointLocation = pointLocation;
	this.depth = depth;
	this.checkMouse = checkMouse;
    }

    protected void activate(WindowOperator winOper) 
	throws TimeoutExpiredException {
	if(winOper.getFocusOwner() == null) {
	    super.activate(winOper);
	    winOper.copyEnvironment(winOper);
	    winOper.getEventDispatcher().checkComponentUnderMouse(checkMouse);
	    winOper.setDispatchingModel(winOper.getDispatchingModel() |
					JemmyProperties.ROBOT_MODEL_MASK);
	    Point p = getClickPoint(winOper);
	    winOper.clickMouse((int)p.getX(), (int)p.getY(), 1);
	}
    }

    private Point getClickPoint(WindowOperator win) {
	int x, y;
	if(place == LEFT ||
	   place == RIGHT) {
	    y = ((int)(win.getHeight() * pointLocation - 1));
	    if(place == RIGHT) {
		x = win.getWidth() - 1 - depth;
	    } else {
		x = depth;
	    }
	} else {
	    x = ((int)(win.getWidth() * pointLocation - 1));
	    if(place == BOTTOM) {
		y = win.getHeight() - 1 - depth;
	    } else {
		y = depth;
	    }
	}
	if(x < 0) {
	    x = 0;
	}
	if(x >= win.getWidth()) {
	    x = win.getWidth() - 1;
	}
	if(y < 0) {
	    y = 0;
	}
	if(y >= win.getHeight()) {
	    y = win.getHeight() - 1;
	}
	return(new Point(x, y));
    }
}
