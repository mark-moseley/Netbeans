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

package org.netbeans.modules.db.explorer.infos;

import java.sql.ResultSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;
import org.netbeans.api.db.explorer.DatabaseException;
import org.netbeans.lib.ddl.impl.DriverSpecification;
import org.netbeans.modules.db.explorer.nodes.DatabaseNode;

public class ForeignKeyListNodeInfo extends DatabaseNodeInfo {
    static final long serialVersionUID =5809643799834921044L;

    @Override
    public void initChildren(Vector children) throws DatabaseException {
        try {
            if (!ensureConnected()) {
                return;
            }
            String table = (String)get(DatabaseNode.TABLE);

            DriverSpecification drvSpec = getDriverSpecification();
            drvSpec.getImportedKeys(table);
            ResultSet rs = drvSpec.getResultSet();
            if (rs != null) {
                Set fkmap = new HashSet();
                HashMap rset = new HashMap();
                ForeignKeyNodeInfo info;
                while (rs.next()) {
                    rset = drvSpec.getRow();
                    if (rset.get(new Integer(12)) != null) {
                        info = (ForeignKeyNodeInfo)DatabaseNodeInfo.createNodeInfo(this, DatabaseNode.IMPORTED_KEY, rset);
                        if (info != null) {
                            String fkName = info.getName();
                            if (fkName == null || fkName.trim().equals("")) {// NOI18N 
                                String refName = (String) rset.get(new Integer(3));
                                info.setName("GENERATED_FK_" + refName); // NOI18N
                            }
                            if (!fkmap.contains(info.getName())) {
                                fkmap.add(info.getName());
                                info.put(DatabaseNode.IMPORTED_KEY, info.getName());
                                children.add(info);
                            }
                        } else
                            throw new Exception(bundle().getString("EXC_UnableToCreateForeignNodeInfo")); //NOI18N
                    }
                    rset.clear();
                }
                rs.close();
            }
        } catch (Exception e) {
            throw new DatabaseException(e.getMessage());
        }
    }
    
    @Override
    public String getShortDescription() {
        return bundle().getString("ND_ForeignKeyList"); // NOI18N
    }
    
    @Override
    public String getDisplayName() {
        return bundle().getString("NDN_ForeignKeys"); //NOI18N
    }

}
