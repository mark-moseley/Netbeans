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

package org.netbeans.modules.php.editor.codegen;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.text.JTextComponent;
import org.netbeans.lib.editor.codetemplates.api.CodeTemplate;
import org.netbeans.lib.editor.codetemplates.api.CodeTemplateManager;
import org.netbeans.modules.gsf.api.CancellableTask;
import org.netbeans.modules.gsf.api.CompilationInfo;
import org.netbeans.modules.gsf.api.SourceModel;
import org.netbeans.modules.gsf.api.SourceModelFactory;
import org.netbeans.modules.php.editor.codegen.ASTNodeUtilities.VariableAcceptor;
import org.netbeans.modules.php.editor.codegen.ui.TableGeneratorPanel;
import org.netbeans.modules.php.editor.codegen.ui.TableGeneratorPanel.TableAndColumns;
import org.netbeans.modules.php.editor.nav.NavUtils;
import org.netbeans.spi.editor.codegen.CodeGenerator;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author Andrei Badea
 */
public class TableGenerator implements CodeGenerator {

    private static final String HEADER_BEGIN =
            "echo '<table>';\n" + // NOI18N
            "echo '<tr>';\n"; // NOI18N

    private static final String HEADER_LINE =
            "echo '<th>${COLUMN}</th>';\n"; // NOI18N

    private static final String HEADER_END =
            "echo '</tr>';\n"; // NOI18N

    private static final String CONN_BEGIN =
            "$$${RESULT newVarName default=\"result\"} = mysqli_query($$${CONN}, '${SQL}');\n" + // NOI18N
            "while (($$${ROW newVarName default=\"row\"} = mysqli_fetch_array($$${RESULT}, MYSQLI_ASSOC)) != NULL) {\n" + // NOI18N
            "   echo '<tr>';\n"; // NOI18N

    private static final String CONN_LINE =
            "   echo '<td>' . $$${ROW}['${COLUMN}'] . '</td>';\n"; // NOI18N

    private static final String CONN_END =
            "   echo '</tr>';\n" + // NOI18N
            "}\n" + // NOI18N
            "mysqli_free_result($$${RESULT});\n" + // NOI18N
            "echo '</table>';"; // NOI18N

    private final JTextComponent component;

    public TableGenerator(JTextComponent component) {
        this.component = component;
    }

    public String getDisplayName() {
        return NbBundle.getMessage(TableGenerator.class, "LBL_DatabaseTable");
    }

    public void invoke() {
        String connVariable = findConnVariableInScope();
        if (connVariable == null) {
            connVariable = "conn"; // NOI18N
        }
        TableAndColumns tableAndColumns = TableGeneratorPanel.selectTableAndColumns(connVariable); // NOI18N
        if (tableAndColumns == null) {
            return;
        }

        String text = generateTemplateText(tableAndColumns);
        CodeTemplateManager manager = CodeTemplateManager.get(component.getDocument());
        CodeTemplate template = manager.createTemporary(text);
        template.insert(component);
    }

    private String findConnVariableInScope() {
        FileObject file = NavUtils.getFile(component.getDocument());
        if (file == null) {
            return null;
        }
        SourceModel model = SourceModelFactory.getInstance().getModel(file);
        final List<String> connVariables = new ArrayList<String>();
        try {
            model.runUserActionTask(new CancellableTask<CompilationInfo>() {
                public void cancel() {
                }
                public void run(CompilationInfo info) throws IOException {
                    ASTNodeUtilities.getVariablesInScope(info, component.getCaretPosition(), new VariableAcceptor() {
                        public boolean acceptVariable(String variableName) {
                            if (variableName.contains("conn")) { // NOI18N
                                connVariables.add(variableName);
                            }
                            return false;
                        }
                    });
                }
            }, true);
        } catch (IOException e) {
            Exceptions.printStackTrace(e);
            return null;
        }
        if (connVariables.contains("conn")) { // NOI18N
            return "conn"; // NOI18N
        }
        if (connVariables.contains("connection")) { // NOI18N
            return "connection"; // NOI18N
        }
        for (String connVariable : connVariables) {
            return connVariable;
        }
        return null;
    }

    private String generateTemplateText(TableAndColumns tableAndColumns) {
        StringBuilder builder = new StringBuilder();
        builder.append(HEADER_BEGIN);
        List<String> columns = tableAndColumns.getSelectedColumns().isEmpty() ? tableAndColumns.getAllColumns() : tableAndColumns.getSelectedColumns();
        for (String column : columns) {
            builder.append(HEADER_LINE.replace("${COLUMN}", column)); // NOI18N
        }
        builder.append(HEADER_END);
        builder.append(CONN_BEGIN.replace("${SQL}", generateSelect(tableAndColumns)).replace("${CONN}", tableAndColumns.getConnVariable())); // NOI18N
        for (String column : columns) {
            builder.append(CONN_LINE.replace("${COLUMN}", column)); // NOI18N
        }
        builder.append(CONN_END);
        return builder.toString();
    }

    private String generateSelect(TableAndColumns tableAndColumns) {
        StringBuilder builder = new StringBuilder("SELECT "); // NOI18N
        List<String> columns = tableAndColumns.getSelectedColumns();
        if (columns.isEmpty()) {
            builder.append(" * "); // NOI18N
        } else {
            int index = 0;
            for (String column : columns) {
                builder.append(column);
                if (index < columns.size() - 1) {
                    builder.append(','); // NOI18N
                }
                index++;
                builder.append(' '); // NOI18N
            }
        }
        builder.append("FROM "); // NOI18N
        builder.append(tableAndColumns.getTable());
        return builder.toString();
    }

    public static final class Factory implements CodeGenerator.Factory {

        public List<? extends CodeGenerator> create(Lookup context) {
            JTextComponent component = context.lookup(JTextComponent.class);
            return Collections.singletonList(new TableGenerator(component));
        }
    }
}
