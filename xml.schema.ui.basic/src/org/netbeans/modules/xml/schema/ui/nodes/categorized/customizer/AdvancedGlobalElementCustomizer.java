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
 * AdvancedGlobalElementCustomizer.java
 *
 * Created on January 17, 2006, 10:26 PM
 */

package org.netbeans.modules.xml.schema.ui.nodes.categorized.customizer;

import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.openide.util.HelpCtx;
//local imports
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.schema.model.LocalType;
import org.netbeans.modules.xml.schema.model.SchemaComponentFactory;
import org.netbeans.modules.xml.schema.model.SchemaComponentReference;
import org.netbeans.modules.xml.schema.ui.nodes.categorized.customizer.ElementCustomizer.ElementTypeStyle;

/**
 * Global Element customizer
 *
 * @author  Ajit Bhate
 */
public class AdvancedGlobalElementCustomizer extends ElementCustomizer<GlobalElement>
{
	
	static final long serialVersionUID = 1L;
	
	/**
	 * Creates new form AdvancedGlobalElementCustomizer
	 */
	/**
	 * Creates new form AdvancedLocalElementCustomizer
	 */
	public AdvancedGlobalElementCustomizer(
			SchemaComponentReference<GlobalElement> reference)
	{
		this(reference, null);
	}
	
	public AdvancedGlobalElementCustomizer(
			SchemaComponentReference<GlobalElement> reference,
			SchemaComponent parent)
	{
		super(reference, parent);
	}
	
	/**
	 * initializes non ui elements
	 */
	protected void initializeModel()
	{
		GlobalElement element = getReference().get();
		if (element.getType() != null)
		{
			_setType(element.getType().get());
		}
		else
		{
			_setType(element.getInlineType());
		}
	}
	
	/**
	 * Changes the type of element
	 *
	 */
	protected void setModelType()
	{
		GlobalElement element = getReference().get();
		SchemaComponentFactory factory = element.getModel().getFactory();
		
		ElementTypeStyle newStyle = getUIStyle();
		GlobalType newType = getUIType();
		_setType(newType);
		if(newStyle == ElementTypeStyle.EXISTING)
		{
			if(element.getInlineType()!=null)
			{
				element.setInlineType(null);
			}
			element.setType(factory.createGlobalReference(
					newType, GlobalType.class, element));
		}
		else
		{
			if(element.getType()!=null)
			{
				element.setType(null);
			}
			LocalType lt = createLocalType(factory, newStyle, newType);
			element.setInlineType(lt);
		}
	}

	public HelpCtx getHelpCtx()
	{
		return new HelpCtx(AdvancedGlobalElementCustomizer.class);
	}
}
