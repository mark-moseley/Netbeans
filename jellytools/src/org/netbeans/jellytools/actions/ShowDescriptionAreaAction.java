/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.jellytools.actions;

import org.netbeans.jellytools.Bundle;

/** Used to call "Show Description Area" popup menu item on a property sheet.
 * @see Action
 * @author Jiri.Skrivanek@sun.com
 */
public class ShowDescriptionAreaAction extends Action {

    private static final String popupPath = Bundle.getString("org.openide.explorer.propertysheet.Bundle", "CTL_ShowDescription");

    /** creates new ShowDescriptionAreaAction instance */    
    public ShowDescriptionAreaAction() {
        super(null, popupPath);
    }
}