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

package org.netbeans.jemmy.drivers.lists;

import java.awt.Point;

import java.awt.event.KeyEvent;

import org.netbeans.jemmy.Timeout;

import org.netbeans.jemmy.drivers.DriverManager;
import org.netbeans.jemmy.drivers.KeyDriver;
import org.netbeans.jemmy.drivers.LightSupportiveDriver;
import org.netbeans.jemmy.drivers.ListDriver;

import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.ChoiceOperator;

/**
 * List driver for java.awt.Choice component type.
 *
 * @author Alexandre Iline(alexandre.iline@sun.com)
 */
public class ChoiceDriver extends LightSupportiveDriver implements ListDriver {
    private final static int RIGHT_INDENT = 10;

    /**
     * Constructs a ChoiceDriver.
     */
    public ChoiceDriver() {
	super(new String[] {"org.netbeans.jemmy.operators.ChoiceOperator"});
    }

    public void selectItem(ComponentOperator oper, int index) {
        ChoiceOperator coper = (ChoiceOperator)oper;
        Point pointToClick = getClickPoint(oper);
	DriverManager.getMouseDriver(oper).
	    clickMouse(oper, pointToClick.x, pointToClick.y,
		       1, oper.getDefaultMouseButton(), 0,
		       oper.getTimeouts().create("ComponentOperator.MouseClickTimeout"));
	KeyDriver kdriver = DriverManager.getKeyDriver(oper);
	Timeout pushTimeout = oper.getTimeouts().create("ComponentOperator.PushKeyTimeout");
        if(System.getProperty("java.version").startsWith("1.4")) {
            while(coper.getSelectedIndex() != index) {
                kdriver.pushKey(oper, (index > coper.getSelectedIndex()) ? KeyEvent.VK_DOWN : KeyEvent.VK_UP, 0, pushTimeout);
            }
        } else {
            int current = ((ChoiceOperator)oper).getSelectedIndex();
            int diff = 0;
            int key = 0;
            if(index > current) {
                diff = index - current;
                key = KeyEvent.VK_DOWN;
            } else {
                diff = current - index;
                key = KeyEvent.VK_UP;
            }
            for(int i = 0; i < diff; i++) {
                kdriver.pushKey(oper, key, 0, pushTimeout);
            }
        }
        kdriver.pushKey(oper, KeyEvent.VK_ENTER, 0, pushTimeout);
    }

    private Point getClickPoint(ComponentOperator oper) {
        return(new Point(oper.getWidth() - RIGHT_INDENT, oper.getHeight()/2));
    }
}
