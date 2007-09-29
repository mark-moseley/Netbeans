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
 * AdvancedLocalAttributeCustomizer.java
 *
 * Created on January 17, 2006, 10:26 PM
 */

package org.netbeans.modules.xml.schema.ui.nodes.categorized.customizer;

import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.openide.util.HelpCtx;
//local imports
import org.netbeans.modules.xml.schema.model.LocalAttribute;
import org.netbeans.modules.xml.schema.model.GlobalSimpleType;
import org.netbeans.modules.xml.schema.model.LocalSimpleType;
import org.netbeans.modules.xml.schema.model.SchemaComponentFactory;
import org.netbeans.modules.xml.schema.model.SchemaComponentReference;
import org.netbeans.modules.xml.schema.ui.nodes.categorized.customizer.AttributeCustomizer.AttributeTypeStyle;

/**
 * Global Element customizer
 *
 * @author  Ajit Bhate
 */
public class AdvancedLocalAttributeCustomizer extends AttributeCustomizer<LocalAttribute>
{
	
	static final long serialVersionUID = 1L;
	
	/**
	 * Creates new form AdvancedLocalAttributeCustomizer
	 */
	public AdvancedLocalAttributeCustomizer(
			SchemaComponentReference<LocalAttribute> reference)
	{
		this(reference, null, null);
	}
	
	public AdvancedLocalAttributeCustomizer(
			SchemaComponentReference<LocalAttribute> reference,
			SchemaComponent parent, GlobalSimpleType currentGlobalSimpleType)
	{
		super(reference, parent, currentGlobalSimpleType);
	}
		
	/**
	 * initializes non ui elements
	 */
	protected void initializeModel()
	{
		LocalAttribute attribute = getReference().get();
		if(!hasParent())
		{
			_setType(_getType());
		}
		else if (attribute.getType() != null)
		{
			_setType(attribute.getType().get());
		}
		else
		{
			_setType(attribute.getInlineType());
		}
	}
	
	/**
	 * Changes the type of attribute
	 *
	 */
	protected void setModelType()
	{
		LocalAttribute attribute = getReference().get();
		
		AttributeTypeStyle newStyle = getUIStyle();
		if(newStyle == AttributeTypeStyle.EXISTING)
		{
			GlobalSimpleType newType = getUIType();
			_setType(newType);
			if(attribute.getInlineType()!=null)
			{
				attribute.setInlineType(null);
			}
			SchemaComponentFactory factory = attribute.getModel().getFactory();
			attribute.setType(factory.createGlobalReference(
					newType, GlobalSimpleType.class, attribute));
		}
		else
		{
			if(attribute.getType()!=null)
			{
				attribute.setType(null);
			}
			LocalSimpleType lt = createLocalType();
			attribute.setInlineType(lt);
			_setType(lt);
		}
	}
	
	public HelpCtx getHelpCtx()
	{
		return new HelpCtx(AdvancedLocalAttributeCustomizer.class);
	}
}
