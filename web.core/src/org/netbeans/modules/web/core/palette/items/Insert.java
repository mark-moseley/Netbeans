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

package org.netbeans.modules.web.core.palette.items;

import java.util.ResourceBundle;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import org.netbeans.modules.web.core.palette.JSPPaletteUtilities;
import org.openide.text.ActiveEditorDrop;
import org.openide.util.NbBundle;


/**
 *
 * @author Libor Kotouc
 */
public class Insert implements ActiveEditorDrop {

    public static String STMT_DEFAULT = "INSERT INTO table_name (column1, column2,...)\nVALUES (value1, value2,....)"; // NOI18N
    SQLStmt stmt = null;
    private String variable = "";
    private int scopeIndex = SQLStmt.SCOPE_DEFAULT;
    private String dataSource = "";
    private String update = STMT_DEFAULT;
    private String displayName;
    private String stmtLabel = "";
    private String stmtACSN = "";
    private String stmtACSD = "";

    public Insert() {

        try {
            displayName = NbBundle.getBundle("org.netbeans.modules.web.core.palette.items.resources.Bundle").getString("NAME_jsp-Insert"); // NOI18N
        } catch (Exception e) {
        }

        ResourceBundle bundle = NbBundle.getBundle("org.netbeans.modules.web.core.palette.items.Bundle"); // NOI18N
        try {
            stmtLabel = bundle.getString("LBL_Insert_Stmt"); // NOI18N
        } catch (Exception e) {
        }
        try {
            stmtACSN = bundle.getString("ACSN_Insert_Stmt"); // NOI18N
        } catch (Exception e) {
        }
        try {
            stmtACSD = bundle.getString("ACSD_Insert_Stmt"); // NOI18N
        } catch (Exception e) {
        }

        stmt = new SQLStmt(variable, scopeIndex, dataSource, update, "InsertStmtCustomizer"); // NOI18N
    }

    public boolean handleTransfer(JTextComponent targetComponent) {

        boolean accept = stmt.customize(targetComponent, displayName, stmtLabel, stmtACSN, stmtACSD);
        if (accept) {
            String body = createBody();
            try {
                JSPPaletteUtilities.insert(body, targetComponent);
            } catch (BadLocationException ble) {
                accept = false;
            }
        }

        return accept;
    }

    private String createBody() {

        variable = stmt.getVariable();
        dataSource = stmt.getDataSource();
        if (variable.equals("")) {// NOI18N
            variable = JSPPaletteUtilities.CARET;
        } else if (dataSource.equals("")) {// NOI18N
            dataSource = JSPPaletteUtilities.CARET;
        }
        if (variable.equals("")) {
            variable = JSPPaletteUtilities.CARET;
        } else if (dataSource.equals("")) {// NOI18N
            dataSource = JSPPaletteUtilities.CARET;
        }
        String strVariable = " var=\"\""; // NOI18N
        if (variable.length() > 0) {
            strVariable = " var=\"" + variable + "\""; // NOI18N
        }
        scopeIndex = stmt.getScopeIndex();
        String strScope = "";
        if (scopeIndex != SQLStmt.SCOPE_DEFAULT) {
            strScope = " scope=\"" + SQLStmt.scopes[scopeIndex] + "\""; // NOI18N
        }
        String strDS = " dataSource=\"\""; // NOI18N
        if (strDS.length() > 0) {
            strDS = " dataSource=\"" + dataSource + "\""; // NOI18N
        }
        update = stmt.getStmt();
        String strUpdate = update;
        if (update.length() > 0) {
            strUpdate += "\n"; // NOI18N
        }
        String queryBody = "<sql:update" + strVariable + strScope + strDS + ">\n" + strUpdate + "</sql:update>"; // NOI18N
        return queryBody;
    }
}