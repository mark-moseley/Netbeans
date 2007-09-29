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
 * AdvancedEnumerationProperty.java
 *
 * Created on April 19, 2006, 11:53 AM
 *
 */

package org.netbeans.modules.xml.schema.ui.nodes.schema.properties;

import java.awt.Dialog;
import java.beans.FeatureDescriptor;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorSupport;
import java.util.Collection;
import org.netbeans.modules.xml.schema.model.Enumeration;
import org.netbeans.modules.xml.schema.model.SchemaComponentReference;
import org.netbeans.modules.xml.schema.model.SimpleRestriction;
import org.netbeans.modules.xml.schema.ui.basic.UIUtilities;
import org.netbeans.modules.xml.schema.ui.nodes.categorized.customizer.EnumerationCustomizer;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.explorer.propertysheet.PropertyEnv;

/**
 *
 * @author Ajit Bhate
 */
public class AdvancedEnumerationProperty extends BaseSchemaProperty
{

	private boolean editable;
	/** Creates a new instance of AdvancedEnumerationProperty */
	public AdvancedEnumerationProperty(SimpleRestriction component,
			String property,
			String propDispName,
			String propDesc,
			boolean editable) 
			throws NoSuchMethodException
	{
		super(component,
				Collection.class,
				SimpleRestriction.class.getMethod(BaseSchemaProperty.
				firstLetterToUpperCase(property, "get"), new Class[0]),
				null,
				property,propDispName,propDesc,null);
		this.editable = editable;
	}
	
	public PropertyEditor getPropertyEditor()
	{
		return new EnumEditor((SimpleRestriction) getComponent(), 
				getName(), canWrite());
	}

	@Override
	public boolean canWrite()
	{
		if(editable) return super.canWrite();
		return false;
	}

	@Override
	public boolean supportsDefaultValue()
	{
		return false;
	}

	public static class EnumEditor 
			extends PropertyEditorSupport implements ExPropertyEditor
	{
		private SimpleRestriction sr;
		private String name;
		private boolean editable;
		public EnumEditor(SimpleRestriction sr, String name, boolean editable)
		{
			this.sr = sr;
			this.name = name;
			this.editable = editable;
		}
		public boolean supportsCustomEditor()
		{
			return true;
		}
		
		public java.awt.Component getCustomEditor()
		{
			DialogDescriptor descriptor = UIUtilities.getCustomizerDialog(new 
					EnumerationCustomizer<SimpleRestriction>(
					SchemaComponentReference.create(sr)), name,editable);
	        Dialog dlg = DialogDisplayer.getDefault().createDialog(descriptor);
			return dlg;
		}
		
		public void attachEnv(PropertyEnv env)
		{
			FeatureDescriptor desc = env.getFeatureDescriptor();
			desc.setValue("canEditAsText", Boolean.FALSE); // NOI18N
		}

		public String getAsText()
		{
			StringBuffer retValue = new StringBuffer();
			Object obj = super.getValue();
			if(obj instanceof Collection)
			{
				Collection enums = (Collection)obj;
				boolean first = true;
				for(Object e:enums)
				{
					if(e instanceof Enumeration)
					{
						if(first)
							first = false;
						else
							retValue.append(", ");
						retValue.append(((Enumeration)e).getValue());
					}
				}
			}
			return retValue.toString();
		}
		
	}
}
