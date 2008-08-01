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

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import javax.swing.text.Document;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.api.db.sql.support.SQLIdentifiers;
import org.netbeans.api.db.sql.support.SQLIdentifiers.Quoter;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.db.metadata.model.api.Action;
import org.netbeans.modules.db.metadata.model.api.Catalog;
import org.netbeans.modules.db.metadata.model.api.Metadata;
import org.netbeans.modules.db.metadata.model.api.MetadataModelException;
import org.netbeans.modules.db.metadata.model.api.MetadataModels;
import org.netbeans.modules.db.metadata.model.api.Schema;
import org.netbeans.modules.db.metadata.model.api.Table;
import org.netbeans.modules.db.sql.analyzer.FromClause;
import org.netbeans.modules.db.sql.analyzer.QualIdent;
import org.netbeans.modules.db.sql.analyzer.SQLStatement;
import org.netbeans.modules.db.sql.analyzer.SQLStatementAnalyzer;
import org.netbeans.modules.db.sql.editor.completion.SQLCompletionEnv.Context;
import org.netbeans.modules.db.sql.lexer.SQLTokenId;
import org.netbeans.spi.editor.completion.CompletionResultSet;
import org.netbeans.spi.editor.completion.support.AsyncCompletionQuery;
import org.openide.util.Exceptions;

/**
 *
 * @author Andrei Badea
 */
public class SQLCompletionQuery extends AsyncCompletionQuery {

    // XXX refactor to get rid of the one-line methods.
    // XXX quoted identifiers.

    private final DatabaseConnection dbconn;

    private Metadata metadata;
    private String quoteString;
    private SQLCompletionEnv env;
    private FromClause fromClause;
    private int anchorOffset = -1; // Relative to statement offset.
    private int substitutionOffset = 0; // Relative to statement offset.
    private SQLCompletionItems items;

    public SQLCompletionQuery(DatabaseConnection dbconn) {
        this.dbconn = dbconn;
    }

    @Override
    protected void query(CompletionResultSet resultSet, final Document doc, final int caretOffset) {
        final SQLCompletionEnv newEnv = SQLCompletionEnv.create(doc, caretOffset);
        try {
            MetadataModels.get(dbconn).runReadAction(new Action<Metadata>() {
                public void run(Metadata metadata) {
                    Connection conn = dbconn.getJDBCConnection();
                    if (conn == null) {
                        return;
                    }
                    String identifierQuoteString = null;
                    Quoter quoter = null;
                    try {
                        DatabaseMetaData dmd = conn.getMetaData();
                        identifierQuoteString = dmd.getIdentifierQuoteString();
                        quoter = SQLIdentifiers.createQuoter(dmd);
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                    doQuery(newEnv, metadata, quoter, identifierQuoteString);
                }
            });
        } catch (MetadataModelException e) {
            Exceptions.printStackTrace(e);
        }
        if (items != null) {
            items.fill(resultSet);
        }
        if (anchorOffset != -1) {
            resultSet.setAnchorOffset(env.getStatementOffset() + anchorOffset);
        }
        resultSet.finish();
    }

    // Called by unit tests.
    SQLCompletionItems doQuery(SQLCompletionEnv env, Metadata metadata, Quoter quoter, String quoteString) {
        this.env = env;
        this.metadata = metadata;
        this.quoteString = quoteString;
        anchorOffset = -1;
        substitutionOffset = 0;
        items = new SQLCompletionItems(quoter, env.getStatementOffset());
        if (env != null && env.isSelect()) {
            completeSelect();
        }
        return items;
    }

    private void completeSelect() {
        Context context = env.getContext();
        if (context == null) {
            return;
        }
        SQLStatement statement = SQLStatementAnalyzer.analyze(env.getTokenSequence());
        fromClause = statement.getTablesInEffect(env.getCaretOffset());
        switch (context) {
            case SELECT:
                insideSelect();
                break;
            case FROM:
                insideFrom();
                break;
            case WHERE:
                if (fromClause != null) {
                    insideWhere();
                }
        }
    }

    private void insideSelect() {
        Identifier ident = findIdentifier();
        if (ident == null) {
            return;
        }
        anchorOffset = ident.anchorOffset;
        substitutionOffset = ident.substitutionOffset;
        if (ident.fullyTypedIdent.isEmpty()) {
            completeSelectSimpleIdent(ident.lastPrefix, ident.prefixQuoteString);
        } else if (ident.fullyTypedIdent.isSimple()) {
            completeSelectSingleQualIdent(ident.fullyTypedIdent, ident.lastPrefix, ident.prefixQuoteString);
        } else if (ident.fullyTypedIdent.isSingleQualified()) {
            completeSelectDoubleQualIdent(ident.fullyTypedIdent, ident.lastPrefix, ident.prefixQuoteString);
        }
    }

    private void insideFrom() {
        Identifier ident = findIdentifier();
        if (ident == null) {
            return;
        }
        anchorOffset = ident.anchorOffset;
        substitutionOffset = ident.substitutionOffset;
        if (ident.fullyTypedIdent.isEmpty()) {
            completeFromSimpleIdent(ident.lastPrefix, ident.prefixQuoteString);
        } else if (ident.fullyTypedIdent.isSimple()) {
            completeFromSingleQualIdent(ident.fullyTypedIdent, ident.lastPrefix, ident.prefixQuoteString);
        }
    }

    private void insideWhere() {
        Identifier ident = findIdentifier();
        if (ident == null) {
            return;
        }
        anchorOffset = ident.anchorOffset;
        substitutionOffset = ident.substitutionOffset;
        if (ident.fullyTypedIdent.isEmpty()) {
            completeWhereSimpleIdent(ident.lastPrefix, ident.prefixQuoteString);
        } else if (ident.fullyTypedIdent.isSimple()) {
            completeWhereSingleQualIdent(ident.fullyTypedIdent, ident.lastPrefix, ident.prefixQuoteString);
        } else if (ident.fullyTypedIdent.isSingleQualified()) {
            completeWhereDoubleQualIdent(ident.fullyTypedIdent, ident.lastPrefix, ident.prefixQuoteString);
        }
    }

    private void completeSelectSimpleIdent(String typedPrefix, String prefixQuoteString) {
        if (fromClause != null) {
            completeSimpleIdentBasedOnFromClause(typedPrefix, prefixQuoteString);
        } else {
            Catalog defaultCatalog = metadata.getDefaultCatalog();
            Schema defaultSchema = metadata.getDefaultCatalog().getDefaultSchema();
            if (defaultSchema != null) {
                // All columns in default schema, but only if a prefix has been typed, otherwise there
                // would be too many columns.
                if (typedPrefix != null) {
                    for (Table table : defaultSchema.getTables()) {
                        items.addColumns(defaultSchema, table, typedPrefix, prefixQuoteString, substitutionOffset);
                    }
                }
                // All tables in default schema.
                items.addTables(defaultSchema, null, typedPrefix, prefixQuoteString, substitutionOffset);
                // All schemas.
                items.addSchemas(defaultCatalog, null, typedPrefix, prefixQuoteString, substitutionOffset);
            }
        }
    }

    private void completeSelectSingleQualIdent(QualIdent fullyTypedIdent, String lastPrefix, String prefixQuoteString) {
        if (fromClause != null) {
            completeSingleQualIdentBasedOnFromClause(fullyTypedIdent, lastPrefix, prefixQuoteString);
        } else {
            Catalog defaultCatalog = metadata.getDefaultCatalog();
            Schema defaultSchema = defaultCatalog.getDefaultSchema();
            if (defaultSchema != null) {
                // All columns in the typed table.
                Table table = defaultSchema.getTable(fullyTypedIdent.getSimpleName());
                if (table != null) {
                    items.addColumns(defaultSchema, table, lastPrefix, prefixQuoteString, substitutionOffset);
                }
                // All tables in the typed schema.
                Schema schema = defaultCatalog.getSchema(fullyTypedIdent.getSimpleName());
                if (schema != null) {
                    items.addTables(schema, null, lastPrefix, prefixQuoteString, substitutionOffset);
                }
            }
        }
    }

    private void completeSelectDoubleQualIdent(QualIdent fullyTypedIdent, String lastPrefix, String prefixQuoteString) {
        if (fromClause != null) {
            completeDoubleQualIdentBasedOnFromClause(fullyTypedIdent, lastPrefix, prefixQuoteString);
        } else {
            items.addColumns(metadata.getDefaultCatalog(), fullyTypedIdent, lastPrefix, prefixQuoteString, substitutionOffset);
        }
    }

    private void completeFromSimpleIdent(String typedPrefix, String prefixQuoteString) {
        Catalog defaultCatalog = metadata.getDefaultCatalog();
        Schema schema = defaultCatalog.getDefaultSchema();
        if (schema != null) {
            items.addTables(schema, null, typedPrefix, prefixQuoteString, substitutionOffset);
        }
        // All schemas.
        items.addSchemas(defaultCatalog, null, typedPrefix, prefixQuoteString, substitutionOffset);
    }

    private void completeFromSingleQualIdent(QualIdent fullyTypedIdent, String lastPrefix, String prefixQuoteString) {
        Schema schema = metadata.getDefaultCatalog().getSchema(fullyTypedIdent.getSimpleName());
        if (schema != null) {
            items.addTables(schema, null, lastPrefix, prefixQuoteString, substitutionOffset);
        }
    }

    private void completeWhereSimpleIdent(String typedPrefix, String prefixQuoteString) {
        completeSimpleIdentBasedOnFromClause(typedPrefix, prefixQuoteString);
    }

    private void completeWhereSingleQualIdent(QualIdent fullyTypedIdent, String lastPrefix, String prefixQuoteString) {
        completeSingleQualIdentBasedOnFromClause(fullyTypedIdent, lastPrefix, prefixQuoteString);
    }

    private void completeWhereDoubleQualIdent(QualIdent fullyTypedIdent, String lastPrefix, String prefixQuoteString) {
        completeDoubleQualIdentBasedOnFromClause(fullyTypedIdent, lastPrefix, prefixQuoteString);
    }

    private void completeSimpleIdentBasedOnFromClause(String typedPrefix, String prefixQuoteString) {
        assert fromClause != null;
        // Columns from tables and aliases.
        Set<QualIdent> tableNames = fromClause.getUnaliasedTableNames();
        Set<QualIdent> allTableNames = new TreeSet<QualIdent>(tableNames);
        Map<String, QualIdent> aliases = fromClause.getAliasedTableNames();
        for (Entry<String, QualIdent> entry : aliases.entrySet()) {
            allTableNames.add(entry.getValue());
        }
        Catalog defaultCatalog = metadata.getDefaultCatalog();
        for (QualIdent tableName : allTableNames) {
            items.addColumns(defaultCatalog, tableName, typedPrefix, prefixQuoteString, substitutionOffset);
        }
        Schema defaultSchema = defaultCatalog.getDefaultSchema();
        // Tables from default schema, restricted to those already in the FROM clause.
        if (defaultSchema != null) {
            String defaultSchemaName = (defaultSchema != null) ? defaultSchema.getName() : null;
            Set<String> simpleTableNames = new HashSet<String>();
            for (QualIdent tableName : tableNames) {
                String simpleTableName = tableName.getSimpleName();
                if (tableName.isSimple()) {
                    simpleTableNames.add(simpleTableName);
                } else if (tableName.isSingleQualified() && defaultSchemaName != null) {
                    if (defaultSchemaName.equals(tableName.getFirstQualifier())) {
                        simpleTableNames.add(simpleTableName);
                    }
                }
            }
            items.addTables(defaultSchema, simpleTableNames, typedPrefix, prefixQuoteString, substitutionOffset);
        }
        // Aliases.
        List<String> sortedAliases = new ArrayList<String>(aliases.keySet());
        Collections.sort(sortedAliases);
        items.addAliases(sortedAliases, typedPrefix, prefixQuoteString, substitutionOffset);
        // Schemas based on qualified tables.
        Set<String> schemaNames = new HashSet<String>();
        for (QualIdent tableName : tableNames) {
            if (!tableName.isSimple()) {
                schemaNames.add(tableName.getFirstQualifier());
            }
        }
        items.addSchemas(defaultCatalog, schemaNames, typedPrefix, prefixQuoteString, substitutionOffset);
    }

    private void completeSingleQualIdentBasedOnFromClause(QualIdent fullyTypedIdent, String lastPrefix, String prefixQuoteString) {
        assert fromClause != null;
        Catalog defaultCatalog = metadata.getDefaultCatalog();
        // Assume table name. It must be in the FROM clause either as a simple name, or qualified by the default schema.
        QualIdent tableName = fullyTypedIdent;
        String defaultSchemaName = getDefaultSchemaName();
        boolean found = false;
        for (QualIdent unaliasedTableName : fromClause.getUnaliasedTableNames()) {
            if (unaliasedTableName.equals(tableName) || (defaultSchemaName != null && unaliasedTableName.equals(new QualIdent(defaultSchemaName, tableName)))) {
                found = true;
                break;
            }
        }
        if (!found) {
            String alias = fullyTypedIdent.getFirstQualifier();
            tableName = fromClause.getTableNameByAlias(alias);
        }
        if (tableName != null) {
            items.addColumns(defaultCatalog, tableName, lastPrefix, prefixQuoteString,substitutionOffset);
        }
        // Now assume schema name.
        Schema schema = defaultCatalog.getSchema(fullyTypedIdent.getSimpleName());
        if (schema != null) {
            Set<String> tableNames = null;
            tableNames = new HashSet<String>();
            for (QualIdent unaliasedTableName : fromClause.getUnaliasedTableNames()) {
                if (unaliasedTableName.isSingleQualified() && unaliasedTableName.getPrefix().equals(fullyTypedIdent)) {
                    tableNames.add(unaliasedTableName.getSimpleName());
                }
            }
            items.addTables(schema, tableNames, lastPrefix, prefixQuoteString, substitutionOffset);
        }
    }

    private void completeDoubleQualIdentBasedOnFromClause(QualIdent fullyTypedIdent, String lastPrefix, String prefixQuoteString) {
        assert fromClause != null;
        if (fromClause.getUnaliasedTableNames().contains(fullyTypedIdent)) {
            items.addColumns(metadata.getDefaultCatalog(), fullyTypedIdent, lastPrefix, prefixQuoteString, substitutionOffset);
        }
    }

    private String getDefaultSchemaName() {
        Schema defaultSchema = metadata.getDefaultCatalog().getDefaultSchema();
        return defaultSchema != null ? defaultSchema.getName() : null;
    }

    private Identifier findIdentifier() {
        TokenSequence<SQLTokenId> seq = env.getTokenSequence();
        int caretOffset = env.getCaretOffset();
        final List<String> parts = new ArrayList<String>();
        if (seq.move(caretOffset) > 0) {
            // Not on token boundary.
            if (!seq.moveNext() && !seq.movePrevious()) {
                return null;
            }
            switch (seq.token().id()) {
                case LINE_COMMENT:
                case BLOCK_COMMENT:
                case INT_LITERAL:
                case DOUBLE_LITERAL:
                case STRING:
                case INCOMPLETE_STRING:
                    return null;
            }
        } else {
            if (!seq.movePrevious()) {
                return null;
            }
        }
        boolean incomplete = false; // Whether incomplete, like '"foo.bar."|'.
        boolean wasDot = false; // Whether the previous token was a dot.
        int lastPrefixOffset = -1;
        main: do {
            switch (seq.token().id()) {
                case DOT:
                    if (parts.isEmpty()) {
                        lastPrefixOffset = caretOffset; // Not the dot offset,
                        // since the user may have typed whitespace after the dot.
                        incomplete = true;
                    }
                    wasDot = true;
                    break;
                case IDENTIFIER:
                case KEYWORD:
                    if (wasDot || parts.isEmpty()) {
                        if (parts.isEmpty() && lastPrefixOffset == -1) {
                            lastPrefixOffset = seq.offset();
                        }
                        wasDot = false;
                        String part;
                        int offset = caretOffset - seq.offset();
                        if (offset > 0 && offset < seq.token().length()) {
                            part = seq.token().text().subSequence(0, offset).toString();
                        } else {
                            part = seq.token().text().toString();
                        }
                        parts.add(part);
                    } else {
                        // Two following identifiers.
                        return null;
                    }
                    break;
                case WHITESPACE:
                case LINE_COMMENT:
                case BLOCK_COMMENT:
                    if (seq.movePrevious()) {
                        // Cannot complete 'SELECT foo |'.
                        if (seq.token().id() == SQLTokenId.IDENTIFIER) {
                            return null;
                        } else if (seq.token().id() == SQLTokenId.DOT) {
                            // Process the dot in the main loop.
                            seq.moveNext();
                            continue main;
                        }
                    }
                    break main;

                default:
                    break main;
            }
        } while (seq.movePrevious());
        Collections.reverse(parts);
        return createIdentifier(parts, incomplete, lastPrefixOffset >= 0 ? lastPrefixOffset : caretOffset);
    }

    /**
     * @param lastPrefixOffset the offset of the last prefix in the identifier, or
     *        if no such prefix, the caret offset.
     * @return
     */
    private Identifier createIdentifier(List<String> parts, boolean incomplete, int lastPrefixOffset) {
        String lastPrefix = null;
        String prefixQuoteString = null;
        int substOffset = lastPrefixOffset;
        if (parts.isEmpty()) {
            if (incomplete) {
                // Just a dot was typed.
                return null;
            }
            // Fine, nothing was typed.
        } else {
            if (!incomplete) {
                lastPrefix = parts.remove(parts.size() - 1);
                if (quoteString != null) {
                    if (lastPrefix.startsWith(quoteString)) {
                        if (lastPrefix.endsWith(quoteString) && lastPrefix.length() > quoteString.length()) {
                            // User typed '"foo"."bar"|', can't complete that.
                            return null;
                        }
                        int lastPrefixLength = lastPrefix.length();
                        lastPrefix = unquote(lastPrefix, quoteString);
                        if (lastPrefix != null) {
                            lastPrefixOffset = lastPrefixOffset + (lastPrefixLength - lastPrefix.length());
                        } else {
                            lastPrefixOffset = lastPrefixOffset + lastPrefixLength;
                        }
                        prefixQuoteString = quoteString;
                    } else if (lastPrefix.endsWith(quoteString)) {
                        // User typed '"foo".bar"|', can't complete.
                        return null;
                    }
                }
            }
            for (int i = 0; i < parts.size(); i++) {
                String unquoted = unquote(parts.get(i), quoteString);
                if (unquoted == null) {
                    // User typed something like '"foo".""."bar|'.
                    return null;
                }
                parts.set(i, unquoted);
            }
        }
        return new Identifier(new QualIdent(parts), lastPrefix, prefixQuoteString, lastPrefixOffset, substOffset);
    }

    static String unquote(String identifier, String quote) {
        if (quote == null) {
            return identifier;
        }
        int start = 0;
        while (identifier.regionMatches(start, quote, 0, quote.length())) {
            start += quote.length();
        }
        int end = identifier.length();
        if (end > start) {
            for (;;) {
                int offset = end - quote.length();
                if (identifier.regionMatches(offset, quote, 0, quote.length())) {
                    end = offset;
                } else {
                    break;
                }
            }
        }
        String result = null;
        if (start < end) {
            result = identifier.substring(start, end);
            if (result.length() == 0) {
                result = null;
            }
        }
        return result;
    }

    private static final class Identifier {

        final QualIdent fullyTypedIdent;
        final String lastPrefix;
        final String prefixQuoteString;
        final int anchorOffset;
        final int substitutionOffset;

        private Identifier(QualIdent fullyTypedIdent, String lastPrefix, String prefixQuoteString, int anchorOffset, int substitutionOffset) {
            this.fullyTypedIdent = fullyTypedIdent;
            this.lastPrefix = lastPrefix;
            this.prefixQuoteString = prefixQuoteString;
            this.anchorOffset = anchorOffset;
            this.substitutionOffset = substitutionOffset;
        }
    }
}
