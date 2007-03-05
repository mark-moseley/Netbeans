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
package com.sun.rave.web.ui.component.util.factories;

import com.sun.rave.web.ui.component.util.descriptors.LayoutComponent;
import com.sun.rave.web.ui.util.MessageUtil;

import javax.faces.component.UIComponent;
import javax.faces.component.UIOutput;
import javax.faces.component.html.HtmlOutputText;
import javax.faces.context.FacesContext;


/**
 *  <P>	This factory is responsible for creating a UIComponent that contains a
 *	localized message.</P>
 *
 *  @author Ken Paulsen	(ken.paulsen@sun.com)
 */
public class LocalizedStringFactory extends ComponentFactoryBase {

    /**
     *	This is the factory method responsible for creating the UIComponent.
     *
     *	@param	context	    The FacesContext
     *	@param	descriptor  The LayoutComponent descriptor that is associated
     *			    with the requested UIComponent.
     *	@param	parent	    The parent UIComponent
     *
     *	@return	The newly created HtmlOutputText
     */
    public UIComponent create(FacesContext context, LayoutComponent descriptor, UIComponent parent) {
	// First make sure we have all our input.
	String baseName = (String)getEvaluatedOption(
	    context, descriptor, parent, BASE_NAME, true);
	String key = (String)getEvaluatedOption(
	    context, descriptor, parent, KEY, true);

	// parameters are optional
	Object parameters = getEvaluatedOption(
	    context, descriptor, parent, PARAMETERS, false);
	Object args[] = null;
	if (parameters != null) {
	    // Convert from list to Object[]
	    if (parameters.getClass().isArray()) {
		args = (Object[]) parameters;
	    } else {
		// Not a list, just treat as a single value
		args = new Object[] {parameters.toString()};
	    }
	}

	// Get the Localized Message
	String value = MessageUtil.getMessage(context, baseName, key, args);

	// Create the UIComponent
	UIOutput output = new HtmlOutputText();

	// This needs to be done here (before setOptions) so that $...{...}
	// expressions can be resolved... may want to defer these?
	if (parent != null) {
//	    parent.getChildren().add(output);
	    addChild(context, descriptor, parent, output);
	}

	// Set the value
	output.setValue(value);

	// Set all the attributes / properties
	setOptions(context, descriptor, output);

	// Return the new UIComponent
	return output;
    }


    /**
     *	The key for the option representing the base name of the
     *	ResourceBundle.
     */
    public static final String	BASE_NAME	=   "baseName";

    /**
     *	The key for the option representing the ResourceBundle key.
     */
    public static final String	KEY		=   "key";

    /**
     *	The key for the option representing the ResourceBundle entries
     *	parameters.
     */
    public static final String	PARAMETERS	=   "parameters";
}
