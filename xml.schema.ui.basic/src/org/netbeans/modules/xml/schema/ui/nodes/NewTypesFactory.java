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

/*
 * NewTypesFactory.java
 *
 * Created on May 2, 2006, 5:08 PM
 *
 */

package org.netbeans.modules.xml.schema.ui.nodes;

import java.util.ArrayList;
import org.netbeans.modules.xml.schema.model.Annotation;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.SchemaComponentReference;
import org.netbeans.modules.xml.schema.model.visitor.DeepSchemaVisitor;
import org.netbeans.modules.xml.schema.ui.nodes.categorized.newtype.AdvancedSchemaComponentNewType;
import org.openide.util.datatransfer.NewType;

/**
 *
 * @author Ajit Bhate
 */
public class NewTypesFactory extends DeepSchemaVisitor
{
	private ArrayList<Class<? extends SchemaComponent>> childTypes;
	/**
	 * Creates a new instance of NewTypesFactory
	 */
	public NewTypesFactory()
	{
		childTypes = new ArrayList<Class<? extends SchemaComponent>>();
	}
	
	public NewType[] getNewTypes(
			SchemaComponentReference<? extends SchemaComponent> reference,
			Class<? extends SchemaComponent> filterClass)
	{
		childTypes.clear();
		reference.get().accept(this);
		ArrayList<NewType> result = new ArrayList<NewType>();
		for(Class<? extends SchemaComponent>childType:childTypes)
		{
			if(filterClass==null|| filterClass.isAssignableFrom(childType))
			{
				AdvancedSchemaComponentNewType newType =
						new AdvancedSchemaComponentNewType(reference,childType);
				if (newType.canCreate())
				{
					result.add(newType);
				}
			}
		}
		childTypes.clear();
		return result.toArray(new NewType[result.size()]);
	}
	
	protected void visitChildren(SchemaComponent sc)
	{
		addChildType(Annotation.class);
	}

	protected void addChildType(Class<? extends SchemaComponent> childType)
	{
		childTypes.add(childType);
	}
	
}
