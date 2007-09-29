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
 * PrimitiveSimpleTypesNode.java
 *
 * Created on April 13, 2006, 9:49 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.schema.abe.nodes;

import java.awt.Image;
import org.netbeans.modules.xml.axi.datatype.Datatype;
import org.netbeans.modules.xml.axi.datatype.DatatypeFactory;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.netbeans.modules.xml.schema.model.GlobalSimpleType;
import org.netbeans.modules.xml.schema.model.SchemaModelFactory;

/**
 *
 * @author Ayub Khan
 */
public class PrimitiveSimpleTypesNode extends AbstractNode
{
	/** Creates a new instance of PrimitiveSimpleTypesNode */
	public PrimitiveSimpleTypesNode(ABEUIContext context)
	{
		super(new TypesChildren(context));
		setName(NbBundle.getMessage(PrimitiveSimpleTypesNode.class,
				"LBL_CategoryNode_PrimitiveSimpleTypes"));
	}

	public boolean canRename()
	{
		return false;
	}

	public boolean canDestroy()
	{
		return false;
	}

	public boolean canCut()
	{
		return false;
	}

	public boolean canCopy()
	{
		return false;
	}
        
           public Image getOpenedIcon(int i) {
               return org.netbeans.modules.xml.schema.ui.nodes.categorized.
                       CategorizedChildren.getBadgedFolderIcon(i, GlobalSimpleType.class);
           }
           
           public Image getIcon(int i) {
               return org.netbeans.modules.xml.schema.ui.nodes.categorized.
                       CategorizedChildren.getOpenedBadgedFolderIcon(i, GlobalSimpleType.class);
           }
	
	private static class TypesChildren extends Children.Keys
	{
		TypesChildren(ABEUIContext context) {
			super();
			this.context = context;
		}
		protected Node[] createNodes(Object key)
		{
			if(key instanceof GlobalSimpleType)
			{
				GlobalSimpleType gst = (GlobalSimpleType)key;
				Datatype type = DatatypeFactory.getDefault().
					createPrimitive(((GlobalSimpleType)gst).getName());
				Node node = context.getFactory().createNode(getNode(), type);
				return new Node[] {new TypeNode(node)};
			}
			assert false;
			return new Node[]{};
		}

		protected void addNotify()
		{
			setKeys(SchemaModelFactory.getDefault().getPrimitiveTypesModel().
					getSchema().getSimpleTypes());
		}
		
	    private ABEUIContext context;
	}
	
	public static class TypeNode extends FilterNode {
		
		private TypeNode(Node original)
		{
			super(original, Children.LEAF);
			final String details = NbBundle.getMessage(
					PrimitiveSimpleTypesNode.class,"MSG_"+getOriginal().getName());
			setShortDescription(details);
		}
		
		public String getHtmlDisplayName()
		{
			return null;
		}
		
		public Datatype getType() {
			return ((DatatypeNode)getOriginal()).getType();
		}
	}
	
}
