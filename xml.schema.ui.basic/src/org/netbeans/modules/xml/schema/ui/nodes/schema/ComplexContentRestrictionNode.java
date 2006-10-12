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

package org.netbeans.modules.xml.schema.ui.nodes.schema;

import org.netbeans.modules.xml.schema.model.ComplexContentRestriction;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.schema.model.SchemaComponentReference;
import org.netbeans.modules.xml.schema.ui.nodes.*;
import org.netbeans.modules.xml.schema.ui.nodes.schema.properties.GlobalReferenceProperty;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.Sheet;
import org.openide.util.NbBundle;
import org.openide.util.datatransfer.NewType;
/**
 *
 * @author  Todd Fast, todd.fast@sun.com
 */
public class ComplexContentRestrictionNode extends SchemaComponentNode<ComplexContentRestriction>
{
    /**
     *
     *
     */
    public ComplexContentRestrictionNode(SchemaUIContext context, 
		SchemaComponentReference<ComplexContentRestriction> reference,
		Children children)
    {
        super(context,reference,children);
    }


	/**
	 *
	 *
	 */
	@Override
	public String getTypeDisplayName()
	{
		return NbBundle.getMessage(ComplexContentRestrictionNode.class,
			"LBL_ComplexContentRestrictionNode_TypeDisplayName"); // NOI18N
	}

    @Override
    protected Sheet createSheet() {
        Sheet sheet = null;
        try {
            sheet = super.createSheet();
            Sheet.Set props = sheet.get(Sheet.PROPERTIES);
            if (props == null) {
                    props = Sheet.createPropertiesSet();
                    sheet.put(props);
            }
	
            Node.Property baseTypeProp = new GlobalReferenceProperty<GlobalType>(
                    getReference().get(),
                    ComplexContentRestriction.BASE_PROPERTY,
                    NbBundle.getMessage(ComplexContentRestrictionNode.class,
                    "PROP_BaseType_DisplayName"), // display name
                    NbBundle.getMessage(ComplexContentRestrictionNode.class,
                    "HINT_BaseType__ComplexContent_ShortDesc"),	// descr
                    getTypeDisplayName(), // type display name
                    NbBundle.getMessage(ComplexContentRestrictionNode.class,
                    "LBL_GlobalTypeNode_TypeDisplayName"), // reference type display name
                    GlobalType.class
                    );
            props.put(new SchemaModelFlushWrapper(getReference().get(), baseTypeProp));
        } catch (NoSuchMethodException ex) {
            assert false : "properties should be defined";
        }
        return sheet;
    }

}
