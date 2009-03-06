/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.db.sql.editor.completion;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.Document;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.api.db.sql.support.SQLIdentifiers;
import org.netbeans.api.db.sql.support.SQLIdentifiers.Quoter;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.db.metadata.model.api.Action;
import org.netbeans.modules.db.metadata.model.api.Catalog;
import org.netbeans.modules.db.metadata.model.api.Metadata;
import org.netbeans.modules.db.metadata.model.api.MetadataModelException;
import org.netbeans.modules.db.api.metadata.DBConnMetadataModelManager;
import org.netbeans.modules.db.metadata.model.api.Schema;
import org.netbeans.modules.db.metadata.model.api.Table;
import org.netbeans.modules.db.sql.analyzer.FromClause;
import org.netbeans.modules.db.sql.analyzer.InsertStatement;
import org.netbeans.modules.db.sql.analyzer.InsertStatement.InsertContext;
import org.netbeans.modules.db.sql.analyzer.InsertStatementAnalyzer;
import org.netbeans.modules.db.sql.analyzer.QualIdent;
import org.netbeans.modules.db.sql.analyzer.SQLStatement;
import org.netbeans.modules.db.sql.analyzer.SelectStatement;
import org.netbeans.modules.db.sql.analyzer.SelectStatement.SelectContext;
import org.netbeans.modules.db.sql.analyzer.SelectStatementAnalyzer;
import org.netbeans.modules.db.sql.analyzer.SQLStatementKind;
import org.netbeans.modules.db.sql.editor.api.completion.SQLCompletionResultSet;
import org.netbeans.modules.db.sql.lexer.SQLTokenId;
import org.netbeans.spi.editor.completion.CompletionResultSet;
import org.netbeans.spi.editor.completion.support.AsyncCompletionQuery;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

/**
 *
 * @author Andrei Badea
 */
public class SQLCompletionQuery extends AsyncCompletionQuery {

    private static final Logger LOGGER = Logger.getLogger(SQLCompletionQuery.class.getName());

    // XXX refactor to get rid of the one-line methods.
    // XXX quoted identifiers.

    private final DatabaseConnection dbconn;

    private Metadata metadata;
    private SQLCompletionEnv env;
    private Quoter quoter;
    private SQLStatement statement;
    private FromClause fromClause;
    private int anchorOffset = -1; // Relative to statement offset.
    private int substitutionOffset = 0; // Relative to statement offset.
    private SQLCompletionItems items;

    public SQLCompletionQuery(DatabaseConnection dbconn) {
        this.dbconn = dbconn;
    }

    @Override
    protected void query(CompletionResultSet resultSet, final Document doc, final int caretOffset) {
        doQuery(SQLCompletionEnv.forDocument(doc, caretOffset));
        if (items != null) {
            items.fill(resultSet);
        }
        if (anchorOffset != -1) {
            resultSet.setAnchorOffset(env.getStatementOffset() + anchorOffset);
        }
        resultSet.finish();
    }

    public void query(SQLCompletionResultSet resultSet, SQLCompletionEnv newEnv) {
        doQuery(newEnv);
        if (items != null) {
            items.fill(resultSet);
        }
        if (anchorOffset != -1) {
            resultSet.setAnchorOffset(newEnv.getStatementOffset() + anchorOffset);
        }
    }

    private void doQuery(final SQLCompletionEnv newEnv) {
        try {
            DBConnMetadataModelManager.get(dbconn).runReadAction(new Action<Metadata>() {
                public void run(Metadata metadata) {
                    Connection conn = dbconn.getJDBCConnection();
                    if (conn == null) {
                        return;
                    }
                    Quoter quoter = null;
                    try {
                        DatabaseMetaData dmd = conn.getMetaData();
                        quoter = SQLIdentifiers.createQuoter(dmd);
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                    doQuery(newEnv, metadata, quoter);
                }
            });
        } catch (MetadataModelException e) {
            reportError(e);
        }
    }

    // Called by unit tests.
    SQLCompletionItems doQuery(SQLCompletionEnv env, Metadata metadata, Quoter quoter) {
        this.env = env;
        this.metadata = metadata;
        this.quoter = quoter;
        anchorOffset = -1;
        substitutionOffset = 0;
        items = new SQLCompletionItems(quoter, env.getSubstitutionHandler());
        SQLStatementKind kind = SQLStatementAnalyzer.analyzeKind (env.getTokenSequence());
        switch (kind) {
            case SELECT:
                statement = SelectStatementAnalyzer.analyze(env.getTokenSequence(), quoter);
                if (statement != null) {
                    assert statement.getKind() == SQLStatementKind.SELECT : statement.getKind ();
                    completeSelect ();
                }
                break;
            case INSERT:
                statement = InsertStatementAnalyzer.analyze(env.getTokenSequence(), quoter);
                if (statement != null) {
                    assert statement.getKind() == SQLStatementKind.INSERT : statement.getKind ();
                    completeInsert ();
                }
                break;
        }
        return items;
    }

    private void completeSelect() {
        SelectStatement selectStatement = (SelectStatement) statement;
        SelectContext context = selectStatement.getContextAtOffset(env.getCaretOffset());
        if (context == null) {
            return;
        }
        fromClause = selectStatement.getTablesInEffect(env.getCaretOffset());

        Identifier ident = findIdentifier();
        if (ident == null) {
            return;
        }
        anchorOffset = ident.anchorOffset;
        substitutionOffset = ident.substitutionOffset;
        switch (context) {
            case SELECT:
                insideSelect(ident);
                break;
            case FROM:
                insideFrom(ident);
                break;
            case JOIN_CONDITION:
                insideClauseAfterFrom(ident);
                break;
            default:
                if (fromClause != null) {
                    insideClauseAfterFrom(ident);
                }
        }
    }

    private void completeInsert () {
        InsertStatement insertStatement = (InsertStatement) statement;
        InsertContext context = insertStatement.getContextAtOffset(env.getCaretOffset());
        if (context == null) {
            return;
        }

        Identifier ident = findIdentifier();
        if (ident == null) {
            return;
        }
        anchorOffset = ident.anchorOffset;
        substitutionOffset = ident.substitutionOffset;
        switch (context) {
            case INSERT:
                break;
            case INTO:
                insideFrom (ident);
                break;
            case COLUMNS:
                insideColumns (ident, insertStatement.getTable ());
                break;
            case VALUES:
                break;
        }
    }

    private void insideSelect(Identifier ident) {
        if (ident.fullyTypedIdent.isEmpty()) {
            completeSelectSimpleIdent(ident.lastPrefix, ident.quoted);
        } else {
            completeSelectQualIdent(ident.fullyTypedIdent, ident.lastPrefix, ident.quoted);
        }
    }

    private void insideColumns (Identifier ident, QualIdent table) {
        if (ident.fullyTypedIdent.isEmpty()) {
            if (table == null) {
                completeColumnWithTableIfSimpleIdent (ident.lastPrefix, ident.quoted);
            } else {
                items.addColumns (resolveTable (table), ident.lastPrefix, ident.quoted, substitutionOffset);
            }
        } else {
            if (table == null) {
                completeColumnWithTableIfQualIdent (ident.fullyTypedIdent, ident.lastPrefix, ident.quoted);
            } else {
                items.addColumns (resolveTable (table), ident.lastPrefix, ident.quoted, substitutionOffset);
            }
        }
    }

    private void insideFrom(Identifier ident) {
        if (ident.fullyTypedIdent.isEmpty()) {
            completeFromSimpleIdent(ident.lastPrefix, ident.quoted);
        } else if (ident.fullyTypedIdent.isSimple()) {
            completeFromQualIdent(ident.fullyTypedIdent, ident.lastPrefix, ident.quoted);
        }
    }

    private void insideClauseAfterFrom(Identifier ident) {
        if (ident.fullyTypedIdent.isEmpty()) {
            completeSimpleIdentBasedOnFromClause(ident.lastPrefix, ident.quoted);
        } else {
            completeQualIdentBasedOnFromClause(ident.fullyTypedIdent, ident.lastPrefix, ident.quoted);
        }
    }

    private void completeSelectSimpleIdent(String typedPrefix, boolean quoted) {
        if (fromClause != null) {
            completeSimpleIdentBasedOnFromClause(typedPrefix, quoted);
        } else {
            Schema defaultSchema = metadata.getDefaultSchema();
            if (defaultSchema != null) {
                // All columns in default schema, but only if a prefix has been typed, otherwise there
                // would be too many columns.
                if (typedPrefix != null) {
                    for (Table table : defaultSchema.getTables()) {
                        items.addColumns(table, typedPrefix, quoted, substitutionOffset);
                    }
                }
                // All tables in default schema.
                items.addTables(defaultSchema, null, typedPrefix, quoted, substitutionOffset);
            }
            // All schemas.
            Catalog defaultCatalog = metadata.getDefaultCatalog();
            items.addSchemas(defaultCatalog, null, typedPrefix, quoted, substitutionOffset);
            // All catalogs.
            items.addCatalogs(metadata, null, typedPrefix, quoted, substitutionOffset);
        }
    }

    private void completeColumnWithTableIfSimpleIdent(String typedPrefix, boolean quoted) {
        Schema defaultSchema = metadata.getDefaultSchema();
        if (defaultSchema != null) {
            // All columns in default schema, but only if a prefix has been typed, otherwise there
            // would be too many columns.
            if (typedPrefix != null) {
                for (Table table : defaultSchema.getTables()) {
                    items.addColumnsWithTableName (table, null, typedPrefix, quoted, substitutionOffset - 1);
                }
            } else {
                // All tables in default schema.
                items.addTablesAtInsertInto (defaultSchema, null, null, typedPrefix, quoted, substitutionOffset - 1);
            }
        }
        // All schemas.
        Catalog defaultCatalog = metadata.getDefaultCatalog();
        items.addSchemas(defaultCatalog, null, typedPrefix, quoted, substitutionOffset);
        // All catalogs.
        items.addCatalogs(metadata, null, typedPrefix, quoted, substitutionOffset);
    }

    private void completeColumnWithTableIfQualIdent(QualIdent fullyTypedIdent, String lastPrefix, boolean quoted) {
            // Assume fullyTypedIdent is a table.
            Table table = resolveTable(fullyTypedIdent);
            if (table != null) {
                items.addColumnsWithTableName (table, fullyTypedIdent, lastPrefix, quoted,
                        substitutionOffset - 1);
            }
            // Assume fullyTypedIdent is a schema.
            Schema schema = resolveSchema(fullyTypedIdent);
            if (schema != null) {
                items.addTablesAtInsertInto (schema, fullyTypedIdent, null, lastPrefix, quoted,
                        substitutionOffset - 1);
            }
            // Assume fullyTypedIdent is a catalog.
            Catalog catalog = resolveCatalog(fullyTypedIdent);
            if (catalog != null) {
                completeCatalog(catalog, lastPrefix, quoted);
            }
    }

    private void completeSelectQualIdent(QualIdent fullyTypedIdent, String lastPrefix, boolean quoted) {
        if (fromClause != null) {
            completeQualIdentBasedOnFromClause(fullyTypedIdent, lastPrefix, quoted);
        } else {
            // Assume fullyTypedIdent is a table.
            Table table = resolveTable(fullyTypedIdent);
            if (table != null) {
                items.addColumns(table, lastPrefix, quoted, substitutionOffset);
            }
            // Assume fullyTypedIdent is a schema.
            Schema schema = resolveSchema(fullyTypedIdent);
            if (schema != null) {
                items.addTables(schema, null, lastPrefix, quoted, substitutionOffset);
            }
            // Assume fullyTypedIdent is a catalog.
            Catalog catalog = resolveCatalog(fullyTypedIdent);
            if (catalog != null) {
                completeCatalog(catalog, lastPrefix, quoted);
            }
        }
    }

    private void completeFromSimpleIdent(String typedPrefix, boolean quoted) {
        Schema defaultSchema = metadata.getDefaultSchema();
        if (defaultSchema != null) {
            // All tables in default schema.
            items.addTables(defaultSchema, null, typedPrefix, quoted, substitutionOffset);
        }
        // All schemas.
        Catalog defaultCatalog = metadata.getDefaultCatalog();
        items.addSchemas(defaultCatalog, null, typedPrefix, quoted, substitutionOffset);
        // All catalogs.
        items.addCatalogs(metadata, null, typedPrefix, quoted, substitutionOffset);
    }

    private void completeFromQualIdent(QualIdent fullyTypedIdent, String lastPrefix, boolean quoted) {
        Schema schema = resolveSchema(fullyTypedIdent);
        if (schema != null) {
            // Tables in the typed schema.
            items.addTables(schema, null, lastPrefix, quoted, substitutionOffset);
        }
        Catalog catalog = resolveCatalog(fullyTypedIdent);
        if (catalog != null) {
            // Items in the typed catalog.
            completeCatalog(catalog, lastPrefix, quoted);
        }
    }

    private void completeSimpleIdentBasedOnFromClause(String typedPrefix, boolean quoted) {
        assert fromClause != null;
        Set<QualIdent> tableNames = fromClause.getUnaliasedTableNames();
        Set<Table> tables = resolveTables(tableNames);
        Set<QualIdent> allTableNames = new TreeSet<QualIdent>(tableNames);
        Set<Table> allTables = new LinkedHashSet<Table>(tables);
        Map<String, QualIdent> aliases = fromClause.getAliasedTableNames();
        for (Entry<String, QualIdent> entry : aliases.entrySet()) {
            QualIdent tableName = entry.getValue();
            allTableNames.add(tableName);
            Table table = resolveTable(tableName);
            if (table != null) {
                allTables.add(table);
            }
        }
        // Aliases.
        Map<String, QualIdent> sortedAliases = new TreeMap<String, QualIdent>(aliases);
        items.addAliases(sortedAliases, typedPrefix, quoted, substitutionOffset);
        // Columns from aliased and non-aliased tables in the FROM clause.
        for (Table table : allTables) {
            items.addColumns(table, typedPrefix, quoted, substitutionOffset);
        }
        // Tables from default schema, restricted to non-aliased table names in the FROM clause.
        Schema defaultSchema = metadata.getDefaultSchema();
        if (defaultSchema != null) {
            Set<String> simpleTableNames = new HashSet<String>();
            for (Table table : tables) {
                if (table.getParent().isDefault()) {
                    simpleTableNames.add(table.getName());
                }
            }
            items.addTables(defaultSchema, simpleTableNames, typedPrefix, quoted, substitutionOffset);
        }
        // Schemas from default catalog other than the default schema, based on non-aliased table names in the FROM clause.
        // Catalogs based on non-aliased tables names in the FROM clause.
        Set<String> schemaNames = new HashSet<String>();
        Set<String> catalogNames = new HashSet<String>();
        for (Table table : tables) {
            Schema schema = table.getParent();
            Catalog catalog = schema.getParent();
            if (!schema.isDefault() && !schema.isSynthetic() && catalog.isDefault()) {
                schemaNames.add(schema.getName());
            }
            if (!catalog.isDefault()) {
                catalogNames.add(catalog.getName());
            }

        }
        Catalog defaultCatalog = metadata.getDefaultCatalog();
        items.addSchemas(defaultCatalog, schemaNames, typedPrefix, quoted, substitutionOffset);
        items.addCatalogs(metadata, catalogNames, typedPrefix, quoted, substitutionOffset);
    }

    private void completeQualIdentBasedOnFromClause(QualIdent fullyTypedIdent, String lastPrefix, boolean quoted) {
        assert fromClause != null;
        Set<Table> tables = resolveTables(fromClause.getUnaliasedTableNames());
        // Assume fullyTypedIdent is the name of a table in the default schema.
        Table foundTable = resolveTable(fullyTypedIdent);
        if (foundTable == null || !tables.contains(foundTable)) {
            // Table not found, or it is not in the FROM clause.
            foundTable = null;
            // Then assume fullyTypedIdent is an alias.
            if (fullyTypedIdent.isSimple()) {
                QualIdent aliasedTableName = fromClause.getTableNameByAlias(fullyTypedIdent.getSimpleName());
                if (aliasedTableName != null) {
                    foundTable = resolveTable(aliasedTableName);
                }
            }
        }
        if (foundTable != null) {
            items.addColumns(foundTable, lastPrefix, quoted, substitutionOffset);
        }
        // Now assume fullyTypedIdent is the name of a schema in the default catalog.
        Schema schema = resolveSchema(fullyTypedIdent);
        if (schema != null) {
            Set<String> tableNames = new HashSet<String>();
            for (Table table : tables) {
                if (table.getParent().equals(schema)) {
                    tableNames.add(table.getName());
                }
            }
            items.addTables(schema, tableNames, lastPrefix, quoted, substitutionOffset);
        }
        // Now assume fullyTypedIdent is the name of a catalog.
        Catalog catalog = resolveCatalog(fullyTypedIdent);
        if (catalog != null) {
            Set<String> syntheticSchemaTableNames = new HashSet<String>();
            Set<String> schemaNames = new HashSet<String>();
            for (Table table : tables) {
                schema = table.getParent();
                if (schema.getParent().equals(catalog)) {
                    if (!schema.isSynthetic()) {
                        schemaNames.add(schema.getName());
                    } else {
                        syntheticSchemaTableNames.add(table.getName());
                    }
                }
            }
            items.addSchemas(catalog, schemaNames, lastPrefix, quoted, substitutionOffset);
            items.addTables(catalog.getSyntheticSchema(), syntheticSchemaTableNames, lastPrefix, quoted, substitutionOffset);
        }
    }

    private void completeCatalog(Catalog catalog, String prefix, boolean quoted) {
        items.addSchemas(catalog, null, prefix, quoted, substitutionOffset);
        Schema syntheticSchema = catalog.getSyntheticSchema();
        if (syntheticSchema != null) {
            items.addTables(syntheticSchema, null, prefix, quoted, substitutionOffset);
        }
    }

    private Catalog resolveCatalog(QualIdent catalogName) {
        if (catalogName.isSimple()) {
            return metadata.getCatalog(catalogName.getSimpleName());
        }
        return null;
    }

    private Schema resolveSchema(QualIdent schemaName) {
        Schema schema = null;
        switch (schemaName.size()) {
            case 1:
                Catalog catalog = metadata.getDefaultCatalog();
                schema = catalog.getSchema(schemaName.getSimpleName());
                break;
            case 2:
                catalog = metadata.getCatalog(schemaName.getFirstQualifier());
                if (catalog != null) {
                    schema = catalog.getSchema(schemaName.getSimpleName());
                }
                break;
        }
        return schema;
    }

    private Table resolveTable(QualIdent tableName) {
        Table table = null;
        switch (tableName.size()) {
            case 1:
                Schema schema = metadata.getDefaultSchema();
                if (schema != null) {
                    return schema.getTable(tableName.getSimpleName());
                }
                break;
            case 2:
                Catalog catalog = metadata.getDefaultCatalog();
                schema = catalog.getSchema(tableName.getFirstQualifier());
                if (schema != null) {
                    table = schema.getTable(tableName.getSimpleName());
                }
                if (table == null) {
                    catalog = metadata.getCatalog(tableName.getFirstQualifier());
                    if (catalog != null) {
                        schema = catalog.getSyntheticSchema();
                        if (schema != null) {
                            table = schema.getTable(tableName.getSimpleName());
                        }
                    }
                }
                break;
            case 3:
                catalog = metadata.getCatalog(tableName.getFirstQualifier());
                if (catalog != null) {
                    schema = catalog.getSchema(tableName.getSecondQualifier());
                    if (schema != null) {
                        table = schema.getTable(tableName.getSimpleName());
                    }
                }
                break;
        }
        return table;
    }

    private Set<Table> resolveTables(Set<QualIdent> tableNames) {
        Set<Table> result = new LinkedHashSet<Table>(tableNames.size());
        for (QualIdent tableName : tableNames) {
            Table table = resolveTable(tableName);
            if (table != null) {
                result.add(table);
            }
        }
        return result;
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
        } else {
            if (!seq.movePrevious()) {
                return null;
            }
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
        boolean quoted = false;
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
                String quoteString = quoter.getQuoteString();
                if (lastPrefix.startsWith(quoteString)) {
                    if (lastPrefix.endsWith(quoteString) && lastPrefix.length() > quoteString.length()) {
                        // User typed '"foo"."bar"|', can't complete that.
                        return null;
                    }
                    int lastPrefixLength = lastPrefix.length();
                    lastPrefix = quoter.unquote(lastPrefix);
                    lastPrefixOffset = lastPrefixOffset + (lastPrefixLength - lastPrefix.length());
                    quoted = true;
                } else if (lastPrefix.endsWith(quoteString)) {
                    // User typed '"foo".bar"|', can't complete.
                    return null;
                }
            }
            for (int i = 0; i < parts.size(); i++) {
                String unquoted = quoter.unquote(parts.get(i));
                if (unquoted.length() == 0) {
                    // User typed something like '"foo".""."bar|'.
                    return null;
                }
                parts.set(i, unquoted);
            }
        }
        return new Identifier(new QualIdent(parts), lastPrefix, quoted, lastPrefixOffset, substOffset);
    }

    private static void reportError(MetadataModelException e) {
        LOGGER.log(Level.INFO, null, e);
        String error = e.getMessage();
        String message;
        if (error != null) {
            message = NbBundle.getMessage(SQLCompletionQuery.class, "MSG_Error", error);
        } else {
            message = NbBundle.getMessage(SQLCompletionQuery.class, "MSG_ErrorNoMessage");
        }
        DialogDisplayer.getDefault().notifyLater(new NotifyDescriptor.Message(message, NotifyDescriptor.ERROR_MESSAGE));
    }

    private static final class Identifier {

        final QualIdent fullyTypedIdent;
        final String lastPrefix;
        final boolean quoted;
        final int anchorOffset;
        final int substitutionOffset;

        private Identifier(QualIdent fullyTypedIdent, String lastPrefix, boolean quoted, int anchorOffset, int substitutionOffset) {
            this.fullyTypedIdent = fullyTypedIdent;
            this.lastPrefix = lastPrefix;
            this.quoted = quoted;
            this.anchorOffset = anchorOffset;
            this.substitutionOffset = substitutionOffset;
        }
    }
}
