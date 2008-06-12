/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.iep.editor.ps;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import org.netbeans.modules.iep.editor.model.NameGenerator;
import org.netbeans.modules.iep.editor.wizard.database.ColumnInfo;
import org.netbeans.modules.iep.model.IEPModel;
import org.netbeans.modules.iep.model.OperatorComponent;
import org.netbeans.modules.iep.model.SchemaAttribute;
import org.netbeans.modules.iep.model.SchemaComponent;

/**
 *
 * @author radval
 */
public class GUIUtil {

    
    public static List<String> convertCommaSeperatedValuesToList(String commaSeperatedValues) {
        List<String> list = new ArrayList<String>();
        StringTokenizer st = new StringTokenizer(commaSeperatedValues, ",");
        while(st.hasMoreElements()) {
            String val = (String) st.nextElement();
            list.add(val);
        }
        return list;
    }
    
    public static String generateUniqueAsColumnName(ColumnInfo column, 
                                              Set<String> nameSet) {
        String baseName = column.getColumnName();

        String newName = baseName;

        int counter = 0;
        while(nameSet.contains(newName)) {
            newName = baseName + "_" + counter;
            counter++;
        }
        return newName;

    }

    public static String convertListToCommaSeperatedValues(List<String> list) {
        StringBuffer strBuf = new StringBuffer();
        Iterator<String> it = list.iterator();
        while(it.hasNext()) {
            String value = it.next();
            strBuf.append(value);
            if(it.hasNext()) {
                strBuf.append(",");
            }
        }

        return strBuf.toString();
    }

    public static SchemaComponent createRecordIdentifierSchema(String recordIdColumnsText,
                                                         OperatorComponent mComponent,
                                                         String outputSchemaName,
                                                         SelectPanel selectPanel) {
            SchemaComponent recordSchema = null;
            
            
//            String recordIdColumns = mRecordIdentifyingColumnsTextField.getText();
            String recordIdColumns = recordIdColumnsText;
            List<String> columnList = GUIUtil.convertCommaSeperatedValuesToList(recordIdColumns);
            if(columnList.size() > 0) {
                IEPModel model = mComponent.getModel();
                List<String> skipSchemaNames = new ArrayList<String>();
                skipSchemaNames.add(outputSchemaName);
                String schemaName = NameGenerator.generateSchemaName(model.getPlanComponent().getSchemaComponentContainer(), skipSchemaNames);
                recordSchema = model.getFactory().createSchema(model);
                recordSchema.setName(schemaName);

                Iterator<String> it = columnList.iterator();
                while(it.hasNext()) {
                    String column = it.next();
                    SchemaAttribute sa = selectPanel.findSchemaAttribute(column);
                    if(sa != null) {
                        SchemaAttribute rsa = model.getFactory().createSchemaAttribute(model);
                        rsa.setAttributeName(sa.getAttributeName());
                        rsa.setAttributeType(sa.getAttributeType());
                        rsa.setAttributeSize(sa.getAttributeSize());
                        rsa.setAttributeScale(sa.getAttributeScale());
                        rsa.setAttributeComment(sa.getAttributeComment());
                        recordSchema.addSchemaAttribute(rsa);
                    }
                }
            }
            
            return recordSchema;
        }

}
