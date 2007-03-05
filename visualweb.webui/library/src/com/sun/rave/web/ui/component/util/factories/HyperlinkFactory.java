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

import com.sun.rave.web.ui.component.Hyperlink;
import com.sun.rave.web.ui.component.util.descriptors.LayoutComponent;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;


/**
 *  <P>	This factory is responsible for instantiating a Hyperlink
 *	UIComponent.</P>
 *
 *  @author Ken Paulsen	(ken.paulsen@sun.com)
 */
public class HyperlinkFactory extends ComponentFactoryBase {

    /**
     *	<p> This is the factory method responsible for creating the
     *	    <code>UIComponent</code>.</p>
     *
     *	@param	context	    The <code>FacesContext</code>
     *	@param	descriptor  The {@link LayoutComponent} descriptor associated
     *			    with the requested <code>UIComponent</code>.
     *	@param	parent	    The parent <code>UIComponent</code>
     *
     *	@return	The newly created {@link Hyperlink}
     */
    public UIComponent create(FacesContext context, LayoutComponent descriptor, UIComponent parent) {
	// Create the UIComponent
	Hyperlink link = new Hyperlink();

	// This needs to be done here (before setOptions) so that $...{...}
	// expressions can be resolved... may want to defer these?
	if (parent != null) {
//	    parent.getChildren().add(link);
	    addChild(context, descriptor, parent, link);
	}

	// Set all the attributes / properties
	setOptions(context, descriptor, link);

	// Return the component
	return link;
    }
}
