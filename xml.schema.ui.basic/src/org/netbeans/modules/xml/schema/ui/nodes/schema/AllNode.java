/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.xml.schema.ui.nodes.schema;

import org.netbeans.modules.xml.schema.model.All;
import org.netbeans.modules.xml.schema.model.Occur;
import org.netbeans.modules.xml.schema.model.SchemaComponentReference;
import org.netbeans.modules.xml.schema.ui.basic.editors.ZeroOrOneEditor;
import org.netbeans.modules.xml.schema.ui.nodes.*;
import org.netbeans.modules.xml.schema.ui.nodes.schema.properties.BaseSchemaProperty;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.Sheet;
import org.openide.util.NbBundle;
import org.openide.util.datatransfer.NewType;

/**
 *
 * @author  Todd Fast, todd.fast@sun.com
 * @author  Jeri Lockhart
 */
public class AllNode extends SchemaComponentNode<All>
{
    /**
     *
     *
     */
    public AllNode(SchemaUIContext context,
	SchemaComponentReference<All> reference,
	Children children) {
	super(context,reference,children);
	
	setIconBaseWithExtension(
	    "org/netbeans/modules/xml/schema/ui/nodes/resources/"+
	    "all.png");
	
	setDefaultExpanded(true);
    }
    
    
    /**
     *
     *
     */
    @Override
	public String getTypeDisplayName() {
	return NbBundle.getMessage(AllNode.class,
	    "LBL_AllNode_TypeDisplayName"); // NOI18N
    }
    
    
    @Override
	protected Sheet createSheet() {
	Sheet sheet = super.createSheet();
	Sheet.Set props = sheet.get(Sheet.PROPERTIES);
	if (props == null) {
	    props = Sheet.createPropertiesSet();
	    sheet.put(props);
	}
	
	try {
	    if (getReference().get().allowsFullMultiplicity()) {
		
		Node.Property minOccursProp = new BaseSchemaProperty(
		    getReference().get(),
		    Occur.ZeroOne.class, // Occur.ZeroOne.class as value type
		    All.MIN_OCCURS_PROPERTY, //property name
		    NbBundle.getMessage(AllNode.class,"PROP_MinOccurs_DisplayName"), // display name
		    NbBundle.getMessage(AllNode.class,"HINT_zero_or_one"),	// descr
		    ZeroOrOneEditor.class // ZeroOrOneEditor
		    );
		props.put(new SchemaModelFlushWrapper(getReference().get(), minOccursProp));
	    }
	} catch(NoSuchMethodException nsme) {
	    assert false : "properties should be defined";
	}
	return sheet;
    }
}
