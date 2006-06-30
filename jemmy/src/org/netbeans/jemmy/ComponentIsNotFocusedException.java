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
 * The Original Software is the Jemmy library.
 * The Initial Developer of the Original Software is Alexandre Iline.
 * All Rights Reserved.
 *
 * Contributor(s): Alexandre Iline.
 *
 * $Id$ $Revision$ $Date$
 *
 */

package org.netbeans.jemmy;

import java.awt.Component;

/**
 *
 * Exception can be thrown as a result of attempt to produce a key operation
 * for a component which does not have focus.
 *
 * @author Alexandre Iline (alexandre.iline@sun.com)
 */

public class ComponentIsNotFocusedException extends JemmyInputException {

    /**
     * Constructs a ComponentIsNotFocusedException object.
     * @param	comp a Component.
     */
    public ComponentIsNotFocusedException(Component comp) {
	super("Component do not have focus", comp);
    }

}
