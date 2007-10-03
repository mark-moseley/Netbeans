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
package org.netbeans.modules.db.sql.visualeditor.querymodel;

import java.util.Collection;

import org.netbeans.api.db.sql.support.SQLIdentifiers;

/**
 * Represents a SQL literal valus
 */
public class Literal implements Value {

    // Fields

    private Object _value;


    // Constructors

    public Literal() {
    }

    public Literal(Object value) {
        _value = value;
    }


    // Methods

    public String genText(SQLIdentifiers.Quoter quoter) {
        return _value.toString();
    }

    public String toString() {
        return _value.toString();
    }


    // Accessors/Mutators

    public Object getValue(SQLIdentifiers.Quoter quoter) {
        return _value;
    }

    //REVIEW: this should probably go away, and change Literal to not be a QueryItem?
    public void getReferencedColumns(Collection columns) {}
    public Expression findExpression(String table1, String column1, String table2, String column2) {
        return null;
    }

    public boolean isParameterized() {
        // Original version
        // return _value.equals("?");

        // Expand prev version, to try to catch literals like "id = ?" or "id IN (?,?,?).
        // return (_value.toString().indexOf("?") != -1 );

        // Prev version was also catching "id = '?'", which is a string, not a parameter.  Exclude it.
	// return (_value.toString().matches(".*[^']?[^']"));

        // Prev regexp had problems because ? was not escaped
        // Return true if (a) string contains ? (b) string does not contain '?'
        // This still gets other cases wrong, like ? in the middle of a string.
        return (((_value.toString().indexOf("?")) != -1) &&
                ((_value.toString().indexOf("'?'")) == -1));
    }

    public void renameTableSpec(String oldTableSpec, String corrName) {}

}


