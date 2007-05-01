/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

/*
 * MyOptionsPanel.java
 *
 * Created on October 28, 2005, 2:43 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.uml.propertysupport.options;

import org.netbeans.spi.options.OptionsCategory;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.NbBundle;

/**
 *
 * @author krichard
 */
public class UMLOptionsCategory extends OptionsCategory {

    /**
     * Creates a new instance of UMLOptionsPanel
     */
    public UMLOptionsCategory() {
    }
    
    public OptionsPanelController create() {
	return new UMLPanelController() ;
    }
    
    public String getCategoryName() {
//          Returns name of category used in list on the left side of Options Dialog.
	return NbBundle.getMessage(UMLOptionsCategory.class, "UML") ;
    }
    
    public String getIconBase() {
//          Returns base name of 32x32 icon used in list on the left side of Options Dialog.
	return "org/netbeans/modules/uml/propertysupport/nonsource/uml_icon32" ; //No I18N

    }
    
    public String getTitle() {
	return NbBundle.getMessage(UMLOptionsCategory.class, "UML_Options") ;
    }
}
