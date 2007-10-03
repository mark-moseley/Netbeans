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

import org.netbeans.api.db.sql.support.SQLIdentifiers;

/**
 * Represents an identifier (schema/table/column name)
 */
public class Identifier {
    
    // Fields
    private String      _name;
    private boolean     _delimited;
    
    // Constructors
    
    // Create an Identifier with delimiter status explicitly specified.
    // Only occurs when the parser has that information
    public Identifier(String name, boolean delimited) {
        _name = name;
        _delimited = delimited;
    }
    
    
    // Create an Identifier with delimiter status decided heuristically,
    // depending whether the name contains any special characters
    public Identifier(String name) {
        _name=name;
        _delimited = needsDelimited(name);
    }
    
    
    // Accessors
    
    public String genText(SQLIdentifiers.Quoter quoter) {
        return quoter.quoteIfNeeded(_name);
        
//        if (_delimited) {
//	    String delimiter = qbMetaData.getIdentifierQuoteString();
//	    return delimiter + _name + delimiter;
//	} else {
//            return _name;
//	}
    }
    
    
    public String getName() {
        return _name;
    }
    
    
    /**
     * Returns true if the argument contains any non-word characters, which
     * will require it to be delimited.
     */
    private boolean needsDelimited(String name) {
        //        String[] split=name.split("\\W");
        //        return (split.length>1);
        // For consistency with Netbeans, mark all Identifiers as delimited for now.  
        // See IZ# 87920.
        return true;
    }
    
}

