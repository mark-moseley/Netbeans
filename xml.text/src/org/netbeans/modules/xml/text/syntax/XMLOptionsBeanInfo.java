/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.xml.text.syntax;

import java.beans.*;
import java.awt.Image;
import org.netbeans.modules.editor.options.OptionSupport;

/** BeanInfo for plain options
 *
 * @author Libor Kramolis
 */
public class XMLOptionsBeanInfo extends org.netbeans.modules.editor.options.BaseOptionsBeanInfo {

    public XMLOptionsBeanInfo () {
        super ("/org/netbeans/modules/editor/resources/htmlOptions"); // NOI18N
    }

    protected Class getBeanClass() {
        return XMLOptions.class;
    }
	
    protected String[] getPropNames() {
		String parentNames[] = super.getPropNames();
        return OptionSupport.mergeStringArrays(parentNames, XMLOptions.XML_PROP_NAMES);
    }
	
}
