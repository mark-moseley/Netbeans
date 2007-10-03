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

import java.beans.*;

import org.openide.nodes.*;

import org.netbeans.modules.dbschema.*;
import org.netbeans.modules.dbschema.util.SQLTypeUtil;

/** Node representing a column.
 * @see ColumnElement
 */
public class ColumnElementNode extends DBMemberElementNode {
	/** Create a new column node.
	 * @param element column element to represent
	 * @param writeable <code>true</code> to be writable
	 */
	public ColumnElementNode (ColumnElement element, boolean writeable) {
		super(element, Children.LEAF, writeable);
	}

	/* Resolve the current icon base.
	 * @return icon base string.
	 */
	protected String resolveIconBase () {
		return COLUMN;
	}

	/* Creates property set for this node */
	protected Sheet createSheet () {
		Sheet sheet = Sheet.createDefault();
		Sheet.Set ps = sheet.get(Sheet.PROPERTIES);

		ps.put(createNameProperty(writeable));
		ps.put(createTypeProperty(writeable));
		ps.put(createNullableProperty(writeable));
		ps.put(createLengthProperty(writeable));
		ps.put(createPrecisionProperty(writeable));
		ps.put(createScaleProperty(writeable));

		return sheet;
	}

	/** Create a property for the column type.
	 * @param canW <code>false</code> to force property to be read-only
	 * @return the property
	 */
	protected Node.Property createTypeProperty (boolean canW) {
		return new ElementProp(PROP_TYPE, /*Integer.TYPE*/String.class, canW) {
			/** Gets the value */
			public Object getValue () {
                return SQLTypeUtil.getSqlTypeString(((ColumnElement) element).getType());
			}
        };
	}
    
	/** Create a property for the column nullable.
	 * @param canW <code>false</code> to force property to be read-only
	 * @return the property
	 */
	protected Node.Property createNullableProperty (boolean canW) {
		return new ElementProp(PROP_NULLABLE, Boolean.TYPE, canW) {
			/** Gets the value */
			public Object getValue () {
				return Boolean.valueOf(((ColumnElement)element).isNullable());
			}
        };
	}
    
	/** Create a property for the column length.
	 * @param canW <code>false</code> to force property to be read-only
	 * @return the property
	 */
	protected Node.Property createLengthProperty (boolean canW) {
		return new ElementProp(PROP_LENGTH, Integer.TYPE, canW) {
			/** Gets the value */
			public Object getValue () {
				return ((ColumnElement)element).getLength();
			}
        };
	}
    
	/** Create a property for the column precision.
	 * @param canW <code>false</code> to force property to be read-only
	 * @return the property
	 */
	protected Node.Property createPrecisionProperty (boolean canW) {
		return new ElementProp(PROP_PRECISION, Integer.TYPE, canW) {
			/** Gets the value */
			public Object getValue () {
				return ((ColumnElement)element).getPrecision();
			}
        };
	}
    
	/** Create a property for the column scale.
	 * @param canW <code>false</code> to force property to be read-only
	 * @return the property
	 */
	protected Node.Property createScaleProperty (boolean canW) {
		return new ElementProp(PROP_SCALE, Integer.TYPE, canW) {
			/** Gets the value */
			public Object getValue () {
				return ((ColumnElement)element).getScale();
			}
        };
    }
}
