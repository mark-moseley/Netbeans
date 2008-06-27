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

package org.netbeans.modules.db.sql.editor.completion;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.text.MessageFormat;
import javax.swing.ImageIcon;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.completion.Completion;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.db.sql.analyzer.QualIdent;
import org.netbeans.spi.editor.completion.CompletionItem;
import org.netbeans.spi.editor.completion.CompletionTask;
import org.netbeans.spi.editor.completion.support.CompletionUtilities;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 *
 * @author Andrei Badea
 */
public abstract class SQLCompletionItem implements CompletionItem {

    private static final String SCHEMA_COLOR = "<font color=#009900>"; // NOI18N
    private static final String TABLE_COLOR = "<font color=#cc7800>"; // NOI18N
    private static final String COLUMN_COLOR = "<font color=#0707ab>"; // NOI18N
    private static final String COLOR_END = "</font>"; // NOI18N

    private static final String BOLD = "<b>"; // NOI18N
    private static final String BOLD_END = "</b>"; // NOI18N

    private static final ImageIcon SCHEMA_ICON = new ImageIcon(ImageUtilities.loadImage("org/netbeans/modules/db/sql/editor/completion/resources/schema.gif")); // NOI18N
    private static final ImageIcon TABLE_ICON = new ImageIcon(ImageUtilities.loadImage("org/netbeans/modules/db/sql/editor/completion/resources/table.gif")); // NOI18N
    private static final ImageIcon COLUMN_ICON = new ImageIcon(ImageUtilities.loadImage("org/netbeans/modules/db/sql/editor/completion/resources/column.gif")); // NOI18N

    private final String substitutionText;
    private final int substitutionOffset;

    public static SQLCompletionItem schema(String schemaName, String quoteString, int substitutionOffset) {
        return new Schema(schemaName, quoteString, substitutionOffset);
    }

    public static SQLCompletionItem table(String tableName, String quoteString, int substitutionOffset) {
        return new Table(tableName, quoteString, substitutionOffset);
    }

    public static SQLCompletionItem alias(String alias, String quoteString, int substitutionOffset) {
        return new Alias(alias, quoteString, substitutionOffset);
    }

    public static SQLCompletionItem column(QualIdent tableName, String columnName, String quoteString, int substitutionOffset) {
        return new QualTableColumn(tableName, columnName, quoteString, substitutionOffset);
    }

    public static SQLCompletionItem column(String simpleTableName, String columnName, String quoteString, int substitutionOffset) {
        return new SimpleTableColumn(simpleTableName, columnName, quoteString, substitutionOffset);
    }

    private static String computeSubstitutionText(String substitutionText, String quoteString) {
        if (quoteString != null) {
            StringBuilder sb = new StringBuilder(substitutionText.length() + 2);
            sb.append(quoteString);
            sb.append(substitutionText);
            sb.append(quoteString);
            return sb.toString();
        }
        return substitutionText;
    }

    protected SQLCompletionItem(String substitutionText, String quoteString, int substitutionOffset) {
        this.substitutionText = computeSubstitutionText(substitutionText, quoteString);
        this.substitutionOffset = substitutionOffset;
    }

    public void defaultAction(JTextComponent component) {
        Completion.get().hideDocumentation();
        Completion.get().hideCompletion();
        int caretOffset = component.getSelectionEnd();
        substituteText(component, substitutionOffset, caretOffset - substitutionOffset, null);
    }

    public void processKeyEvent(KeyEvent evt) {
    }

    public int getPreferredWidth(Graphics g, Font defaultFont) {
        return CompletionUtilities.getPreferredWidth(getLeftHtmlText(), getRightHtmlText(), g, defaultFont);
    }

    public void render(Graphics g, Font defaultFont, Color defaultColor, Color backgroundColor, int width, int height, boolean selected) {
        CompletionUtilities.renderHtml(getImageIcon(), getLeftHtmlText(), getRightHtmlText(), g, defaultFont, defaultColor, width, height, selected);
    }

    public CompletionTask createDocumentationTask() {
        return null;
    }

    public CompletionTask createToolTipTask() {
        return null;
    }

    public boolean instantSubstitution(JTextComponent component) {
        defaultAction(component);
        return true;
    }

    public int getSortPriority() {
        return 0;
    }

    public CharSequence getSortText() {
        return null;
    }

    public CharSequence getInsertPrefix() {
        return substitutionText;
    }

    protected abstract ImageIcon getImageIcon();

    protected abstract String getLeftHtmlText();

    protected abstract String getRightHtmlText();

    private void substituteText(JTextComponent component, int offset, int len, String toAdd) {
        CharSequence prefix = getInsertPrefix();
        if (prefix ==  null) {
            return;
        }
        BaseDocument baseDoc = (BaseDocument) component.getDocument();
        baseDoc.atomicLock();
        try {
            baseDoc.remove(offset, len);
            baseDoc.insertString(offset, prefix.toString(), null);
            if (toAdd != null) {
                baseDoc.insertString(offset, toAdd, null);
            }
        } catch (BadLocationException ex) {
            // No can do, document may have changed.
        } finally {
            baseDoc.atomicUnlock();
        }
    }

    private static final class Schema extends SQLCompletionItem {

        private final String schemaName;
        private String leftText;

        public Schema(String schemaName, String quoteString, int substitutionOffset) {
            super(schemaName, quoteString, substitutionOffset);
            this.schemaName = schemaName;
        }

        @Override
        protected ImageIcon getImageIcon() {
            return SCHEMA_ICON;
        }

        @Override
        protected String getLeftHtmlText() {
            if (leftText == null) {
                StringBuilder sb = new StringBuilder();
                sb.append(SCHEMA_COLOR);
                sb.append(schemaName);
                sb.append(COLOR_END);
                leftText = sb.toString();
            }
            return leftText;
        }

        @Override
        protected String getRightHtmlText() {
            return null;
        }

        @Override
        public String toString() {
            return MessageFormat.format("Schema {0}", schemaName); // NOI18N
        }
    }

    private static final class Table extends SQLCompletionItem {

        private final String tableName;
        private String leftText;

        public Table(String tableName, String quoteString, int substitutionOffset) {
            super(tableName, quoteString, substitutionOffset);
            this.tableName = tableName;
        }

        @Override
        protected ImageIcon getImageIcon() {
            return TABLE_ICON;
        }

        @Override
        protected String getLeftHtmlText() {
            if (leftText == null) {
                StringBuilder sb = new StringBuilder();
                sb.append(TABLE_COLOR);
                sb.append(tableName);
                sb.append(COLOR_END);
                leftText = sb.toString();
            }
            return leftText;
        }

        @Override
        protected String getRightHtmlText() {
            // XXX should have schema here.
            return null;
        }

        @Override
        public String toString() {
            return MessageFormat.format("Table {0}", tableName); // NOI18N
        }
    }

    private static final class Alias extends SQLCompletionItem {

        private final String alias;

        public Alias(String alias, String quoteString, int substitutionOffset) {
            super(alias, quoteString, substitutionOffset);
            this.alias = alias;
        }

        @Override
        protected ImageIcon getImageIcon() {
            return null;
        }

        @Override
        protected String getLeftHtmlText() {
            return alias;
        }

        @Override
        protected String getRightHtmlText() {
            return null;
        }

        @Override
        public String toString() {
            return MessageFormat.format("Alias {0}", alias); // NOI18N
        }
    }

    private static abstract class AbstractColumn extends SQLCompletionItem {

        private final String columnName;
        private String leftText;
        private String rightText;

        public AbstractColumn(String columnName, String quoteString, int substitutionOffset) {
            super(columnName, quoteString, substitutionOffset);
            this.columnName = columnName;
        }

        protected abstract String getTableName();

        protected String getColumnName() {
            return columnName;
        }

        @Override
        protected ImageIcon getImageIcon() {
            return COLUMN_ICON;
        }

        @Override
        protected String getLeftHtmlText() {
            if (leftText == null) {
                StringBuilder sb = new StringBuilder();
                sb.append(COLUMN_COLOR);
                sb.append(BOLD); // NOI18N
                sb.append(columnName);
                sb.append(BOLD_END); // NOI18N
                leftText = sb.toString();
            }
            return leftText;
        }

        @Override
        protected String getRightHtmlText() {
            if (rightText == null) {
                StringBuilder sb = new StringBuilder();
                sb.append(TABLE_COLOR);
                sb.append(getTableName());
                sb.append(COLOR_END);
                rightText = MessageFormat.format(NbBundle.getMessage(SQLCompletionItem.class, "MSG_Table"), sb.toString());
            }
            return rightText;
        }

        @Override
        public String toString() {
            return MessageFormat.format("Column {0} in table {1}", columnName, getTableName()); // NOI18N
        }
    }

    private static final class QualTableColumn extends AbstractColumn {

        private final QualIdent tableName;

        public QualTableColumn(QualIdent tableName, String columnName, String quoteString, int substitutionOffset) {
            super(columnName, quoteString, substitutionOffset);
            this.tableName = tableName;
        }

        @Override
        protected String getTableName() {
            return tableName.toString();
        }
    }

    private static final class SimpleTableColumn extends AbstractColumn {

        private final String simpleTableName;

        public SimpleTableColumn(String simpleTableName, String columnName, String quoteString, int substitutionOffset) {
            super(columnName, quoteString, substitutionOffset);
            this.simpleTableName = simpleTableName;
        }

        @Override
        protected String getTableName() {
            return simpleTableName;
        }
    }
}
