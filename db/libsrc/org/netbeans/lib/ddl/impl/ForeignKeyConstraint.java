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

package org.netbeans.lib.ddl.impl;

import java.util.*;
import java.sql.*;
import org.netbeans.lib.ddl.*;

/**
* Implementation of ForeignKey constraint.
*/
public class ForeignKeyConstraint extends AbstractTableColumn implements ForeignKeyConstraintDescriptor {
    /** Refernced table */
    String tname;

    /** Referenced column */
    String cname;

    static final long serialVersionUID =9183651896170854492L;
    /** Returns name of Referenced table */
    public String getReferencedTableName()
    {
        return tname;
    }

    /** Sets name of Referenced table */
    public void setReferencedTableName(String name)
    {
        tname = name;
    }

    /** Returns name of Referenced column */
    public String getReferencedColumnName()
    {
        return cname;
    }

    /** Sets name of Referenced column */
    public void setReferencedColumnName(String name)
    {
        cname = name;
    }

    /**
    * Returns properties and it's values supported by this object.
    * object.name	Name of the object; use setObjectName() 
    * object.owner	Name of the object; use setObjectOwner() 
    * fkobject.name	Specification of foreign table 
    * fkcolumn.name	Specification of foreign column 
    */
    public Map getColumnProperties(AbstractCommand cmd) throws DDLException {
        Map args = super.getColumnProperties(cmd);
        args.put("fkobject.name", cmd.quote(tname)); // NOI18N
        args.put("fkcolumn.name", cmd.quote(cname)); // NOI18N
        
        return args;
    }
}
