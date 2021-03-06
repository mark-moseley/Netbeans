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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.dbschema.nodes;

import java.text.MessageFormat;

import org.openide.util.NbBundle;
import org.openide.nodes.*;

import org.netbeans.modules.dbschema.*;

/** Node representing a database table.
 * @see TableElement
 */
public class TableElementNode extends DBElementNode {
    /** Return value of getIconAffectingProperties method. */
    private static final String[] ICON_AFFECTING_PROPERTIES = new String[] {
        PROP_TABLE_OR_VIEW
    };

    /** Create a new class node.
    * @param element class element to represent
    * @param children node children
    * @param writeable <code>true</code> to be writable
    */
    public TableElementNode(TableElement element, Children children, boolean writeable) {
        super(element, children, writeable);
        setDisplayName(MessageFormat.format((element.isTable() ? NbBundle.getBundle("org.netbeans.modules.dbschema.nodes.Bundle_noi18n").getString("SHORT_tableElement") : NbBundle.getBundle("org.netbeans.modules.dbschema.nodes.Bundle_noi18n").getString("SHORT_viewElement")), new Object[]{super.getDisplayName()})); //NOI18N
    }

    /* Resolve the current icon base.
    * @return icon base string.
    */
    protected String resolveIconBase() {
        return ((TableElement)element).isView() ? VIEW : TABLE;
    }

    /* This method is used for resolving the names of the properties,
    * which could affect the icon (such as "modifiers").
    * @return the appropriate array.
    */
    protected String[] getIconAffectingProperties() {
        return ICON_AFFECTING_PROPERTIES;
    }
  
    /* Creates property set for this node */
    protected Sheet createSheet () {
        Sheet sheet = Sheet.createDefault();
        Sheet.Set ps = sheet.get(Sheet.PROPERTIES);
        ps.put(createNameProperty(writeable));
        ps.put(createTableOrViewProperty(writeable));

        return sheet;
    }

	/** Create a node property representing the element's name.
	 * @param canW if <code>false</code>, property will be read-only
	 * @return the property.
	 */
	protected Node.Property createNameProperty (boolean canW) {
		return new ElementProp(Node.PROP_NAME, String.class,canW) {
			/** Gets the value */
			public Object getValue () {
                String name = ((TableElement) element).getName().getFullName();
                int pos;
                    
                pos = name.lastIndexOf("."); //NOI18N
                if (pos != -1)
                    name = name.substring(pos + 1);
                
                return name;
			}
		};
	}

	/** Create a property for the table or view flag.
	 * @param canW <code>false</code> to force property to be read-only
	 * @return the property
	 */
	protected Node.Property createTableOrViewProperty (boolean canW) {
		return new ElementProp(PROP_TABLE_OR_VIEW, String.class, canW) {
			/** Gets the value */
			public Object getValue () {
                if (((TableElement)element).isTableOrView())
                    return NbBundle.getMessage(TableElementNode.class, "Table"); //NOI18N
                else
                    return NbBundle.getMessage(TableElementNode.class, "View"); //NOI18N
			}
		};
	}
}
