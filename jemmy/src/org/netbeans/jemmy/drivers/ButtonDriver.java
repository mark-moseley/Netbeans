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

package org.netbeans.jemmy.drivers;

import org.netbeans.jemmy.operators.ComponentOperator;

/**
 * Defines how to work with buttons.
 */
public interface ButtonDriver {

    /**
     * Presses a button.
     * @param oper Button operator.
     */
    public void press(ComponentOperator oper);

    /**
     * Releases a button.
     * @param oper Button operator.
     */
    public void release(ComponentOperator oper);

    /**
     * Pushes a button.
     * @param oper Button operator.
     */
    public void push(ComponentOperator oper);
}
