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
package org.netbeans.modules.hibernate.completion;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.hibernate.cfg.Environment;
import org.netbeans.editor.TokenItem;
import org.netbeans.modules.hibernate.HibernateCfgProperties;
import org.netbeans.modules.xml.text.syntax.SyntaxElement;
import org.netbeans.modules.xml.text.syntax.dom.StartTag;
import org.netbeans.modules.xml.text.syntax.dom.Tag;
import org.netbeans.spi.editor.completion.CompletionResultSet;
import org.openide.util.NbBundle;
import org.w3c.dom.Text;

/**
 * This class figures out the completion items for various attributes
 * 
 * @author Dongmei Cao
 */
public final class HibernateCfgCompletionManager {

    private static final String PROPERTY_TAG = "property";
    private static final String MAPPING_TAG = "mapping";
    private static final String RESOURCE_ATTRIB = "resource";
    private static final String NAME_ATTRIB = "name";
    
    private static Map<String, Completor> completors = new HashMap<String, Completor>();

    private HibernateCfgCompletionManager() {
        setupCompletors();
    }

    private void setupCompletors() {

        // Completion items for configuration properties
        String[] propertyNames = new String[]{
            Environment.AUTOCOMMIT, NbBundle.getMessage(HibernateCfgCompletionManager.class, "AUTOCOMMIT_DESC"), // NOI18N
            Environment.AUTO_CLOSE_SESSION, NbBundle.getMessage(HibernateCfgCompletionManager.class, "AUTO_CLOSE_SESSION_DESC"), // NOI18N
            Environment.BYTECODE_PROVIDER, NbBundle.getMessage(HibernateCfgCompletionManager.class, "BYTECODE_PROVIDER_DESC"), // NOI18N
            Environment.BATCH_STRATEGY, NbBundle.getMessage(HibernateCfgCompletionManager.class, "BATCH_STRATEGY_DESC"), // NOI18N
            Environment.BATCH_VERSIONED_DATA, NbBundle.getMessage(HibernateCfgCompletionManager.class, "BATCH_VERSIONED_DATA_DESC"), // NOI18N
            Environment.C3P0_ACQUIRE_INCREMENT, NbBundle.getMessage(HibernateCfgCompletionManager.class, "C3P0_ACQUIRE_INCREMENT_DESC"), // NOI18N
            Environment.C3P0_IDLE_TEST_PERIOD, NbBundle.getMessage(HibernateCfgCompletionManager.class, "C3P0_IDLE_TEST_PERIOD_DESC"), // NOI18N
            Environment.C3P0_MAX_SIZE, NbBundle.getMessage(HibernateCfgCompletionManager.class, "C3P0_MAX_SIZE_DESC"), // NOI18N
            Environment.C3P0_MAX_STATEMENTS, NbBundle.getMessage(HibernateCfgCompletionManager.class, "C3P0_MAX_STATEMENTS_DESC"), // NOI18N
            Environment.C3P0_MIN_SIZE, NbBundle.getMessage(HibernateCfgCompletionManager.class, "C3P0_MIN_SIZE_DESC"), // NOI18N
            Environment.C3P0_TIMEOUT, NbBundle.getMessage(HibernateCfgCompletionManager.class, "C3P0_TIMEOUT_DESC"), // NOI18N
            Environment.CACHE_PROVIDER, NbBundle.getMessage(HibernateCfgCompletionManager.class, "CACHE_PROVIDER_DESC"), // NOI18N
            Environment.CACHE_REGION_PREFIX, NbBundle.getMessage(HibernateCfgCompletionManager.class, "CACHE_REGION_PREFIX_DESC"), // NOI18N
            Environment.CACHE_PROVIDER_CONFIG, NbBundle.getMessage(HibernateCfgCompletionManager.class, "CACHE_PROVIDER_CONFIG_DESC"), // NOI18N
            Environment.CACHE_NAMESPACE, NbBundle.getMessage(HibernateCfgCompletionManager.class, "CACHE_NAMESPACE_DESC"), // NOI18N
            Environment.CONNECTION_PROVIDER, NbBundle.getMessage(HibernateCfgCompletionManager.class, "CONNECTION_PROVIDER_DESC"), // NOI18N
            Environment.CONNECTION_PREFIX, NbBundle.getMessage(HibernateCfgCompletionManager.class, "CONNECTION_PREFIX_DESC"), // NOI18N
            Environment.CURRENT_SESSION_CONTEXT_CLASS, NbBundle.getMessage(HibernateCfgCompletionManager.class, "CURRENT_SESSION_CONTEXT_CLASS_DESC"), // NOI18N
            Environment.DATASOURCE, NbBundle.getMessage(HibernateCfgCompletionManager.class, "DATASOURCE_DESC"), // NOI18N
            Environment.DEFAULT_BATCH_FETCH_SIZE, NbBundle.getMessage(HibernateCfgCompletionManager.class, "DEFAULT_BATCH_FETCH_SIZE_DESC"), // NOI18N
            Environment.DEFAULT_CATALOG, NbBundle.getMessage(HibernateCfgCompletionManager.class, "DEFAULT_CATALOG_DESC"), // NOI18N
            Environment.DEFAULT_ENTITY_MODE, NbBundle.getMessage(HibernateCfgCompletionManager.class, "DEFAULT_ENTITY_MODE_DESC"), // NOI18N
            Environment.DEFAULT_SCHEMA, NbBundle.getMessage(HibernateCfgCompletionManager.class, "DEFAULT_SCHEMA_DESC"), // NOI18N
            Environment.DIALECT, NbBundle.getMessage(HibernateCfgCompletionManager.class, "DIALECT_DESC"), // NOI18N
            Environment.DRIVER, NbBundle.getMessage(HibernateCfgCompletionManager.class, "DRIVER_DESC"), // NOI18N
            Environment.FLUSH_BEFORE_COMPLETION, NbBundle.getMessage(HibernateCfgCompletionManager.class, "FLUSH_BEFORE_COMPLETION_DESC"), // NOI18N
            Environment.FORMAT_SQL, NbBundle.getMessage(HibernateCfgCompletionManager.class, "FORMAT_SQL_DESC"), // NOI18N
            Environment.GENERATE_STATISTICS, NbBundle.getMessage(HibernateCfgCompletionManager.class, "GENERATE_STATISTICS_DESC"), // NOI18N
            Environment.HBM2DDL_AUTO, NbBundle.getMessage(HibernateCfgCompletionManager.class, "HBM2DDL_AUTO_DESC"), // NOI18N
            Environment.ISOLATION, NbBundle.getMessage(HibernateCfgCompletionManager.class, "ISOLATION_DESC"), // NOI18N
            Environment.JACC_CONTEXTID, NbBundle.getMessage(HibernateCfgCompletionManager.class, "JACC_CONTEXTID_DESC"), // NOI18N
            Environment.JNDI_CLASS, NbBundle.getMessage(HibernateCfgCompletionManager.class, "JNDI_CLASS_DESC"), // NOI18N
            Environment.JNDI_URL, NbBundle.getMessage(HibernateCfgCompletionManager.class, "JNDI_URL_DESC"), // NOI18N
            Environment.JPAQL_STRICT_COMPLIANCE, NbBundle.getMessage(HibernateCfgCompletionManager.class, "JPAQL_STRICT_COMPLIANCE_DESC"), // NOI18N
            Environment.MAX_FETCH_DEPTH, NbBundle.getMessage(HibernateCfgCompletionManager.class, "MAX_FETCH_DEPTH_DESC"), // NOI18N
            Environment.ORDER_UPDATES, NbBundle.getMessage(HibernateCfgCompletionManager.class, "ORDER_UPDATES_DESC"), // NOI18N
            Environment.OUTPUT_STYLESHEET, NbBundle.getMessage(HibernateCfgCompletionManager.class, "OUTPUT_STYLESHEET_DESC"), // NOI18N
            Environment.PASS, NbBundle.getMessage(HibernateCfgCompletionManager.class, "PASS_DESC"), // NOI18N
            Environment.POOL_SIZE, NbBundle.getMessage(HibernateCfgCompletionManager.class, "POOL_SIZE_DESC"), // NOI18N
            Environment.PROXOOL_EXISTING_POOL, NbBundle.getMessage(HibernateCfgCompletionManager.class, "PROXOOL_EXISTING_POOL_DESC"), // NOI18N
            Environment.PROXOOL_POOL_ALIAS, NbBundle.getMessage(HibernateCfgCompletionManager.class, "PROXOOL_POOL_ALIAS_DESC"), // NOI18N
            Environment.PROXOOL_PREFIX, NbBundle.getMessage(HibernateCfgCompletionManager.class, "PROXOOL_PREFIX_DESC"), // NOI18N
            Environment.PROXOOL_PROPERTIES, NbBundle.getMessage(HibernateCfgCompletionManager.class, "PROXOOL_PROPERTIES_DESC"), // NOI18N
            Environment.PROXOOL_XML, NbBundle.getMessage(HibernateCfgCompletionManager.class, "PROXOOL_XML_DESC"), // NOI18N
            Environment.QUERY_CACHE_FACTORY, NbBundle.getMessage(HibernateCfgCompletionManager.class, "QUERY_CACHE_FACTORY_DESC"), // NOI18N
            Environment.QUERY_TRANSLATOR, NbBundle.getMessage(HibernateCfgCompletionManager.class, "QUERY_TRANSLATOR_DESC"), // NOI18N
            Environment.QUERY_SUBSTITUTIONS, NbBundle.getMessage(HibernateCfgCompletionManager.class, "QUERY_SUBSTITUTIONS_DESC"), // NOI18N
            Environment.QUERY_STARTUP_CHECKING, NbBundle.getMessage(HibernateCfgCompletionManager.class, "QUERY_STARTUP_CHECKING_DESC"), // NOI18N
            Environment.RELEASE_CONNECTIONS, NbBundle.getMessage(HibernateCfgCompletionManager.class, "RELEASE_CONNECTIONS_DESC"), // NOI18N
            Environment.SESSION_FACTORY_NAME, NbBundle.getMessage(HibernateCfgCompletionManager.class, "SESSION_FACTORY_NAME_DESC"), // NOI18N
            Environment.SHOW_SQL, NbBundle.getMessage(HibernateCfgCompletionManager.class, "SHOW_SQL_DESC"), // NOI18N
            Environment.SQL_EXCEPTION_CONVERTER, NbBundle.getMessage(HibernateCfgCompletionManager.class, "SQL_EXCEPTION_CONVERTER_DESC"), // NOI18N
            Environment.STATEMENT_BATCH_SIZE, NbBundle.getMessage(HibernateCfgCompletionManager.class, "STATEMENT_BATCH_SIZE_DESC"), // NOI18N
            Environment.STATEMENT_FETCH_SIZE, NbBundle.getMessage(HibernateCfgCompletionManager.class, "STATEMENT_FETCH_SIZE_DESC"), // NOI18N
            Environment.TRANSACTION_STRATEGY, NbBundle.getMessage(HibernateCfgCompletionManager.class, "TRANSACTION_STRATEGY_DESC"), // NOI18N
            Environment.TRANSACTION_MANAGER_STRATEGY, NbBundle.getMessage(HibernateCfgCompletionManager.class, "TRANSACTION_MANAGER_STRATEGY_DESC"), // NOI18N
            Environment.URL, NbBundle.getMessage(HibernateCfgCompletionManager.class, "URL_DESC"), // NOI18N
            Environment.USER, NbBundle.getMessage(HibernateCfgCompletionManager.class, "USER_DESC"), // NOI18N
            Environment.USE_GET_GENERATED_KEYS, NbBundle.getMessage(HibernateCfgCompletionManager.class, "USE_GET_GENERATED_KEYS_DESC"), // NOI18N
            Environment.USE_SCROLLABLE_RESULTSET, NbBundle.getMessage(HibernateCfgCompletionManager.class, "USE_SCROLLABLE_RESULTSET_DESC"), // NOI18N
            Environment.USE_STREAMS_FOR_BINARY, NbBundle.getMessage(HibernateCfgCompletionManager.class, "USE_STREAMS_FOR_BINARY_DESC"), // NOI18N
            Environment.USE_IDENTIFIER_ROLLBACK, NbBundle.getMessage(HibernateCfgCompletionManager.class, "USE_IDENTIFIER_ROLLBACK_DESC"), // NOI18N
            Environment.USE_SQL_COMMENTS, NbBundle.getMessage(HibernateCfgCompletionManager.class, "USE_SQL_COMMENTS_DESC"), // NOI18N
            Environment.USE_MINIMAL_PUTS, NbBundle.getMessage(HibernateCfgCompletionManager.class, "USE_MINIMAL_PUTS_DESC"), // NOI18N
            Environment.USE_QUERY_CACHE, NbBundle.getMessage(HibernateCfgCompletionManager.class, "USE_QUERY_CACHE_DESC"), // NOI18N
            Environment.USE_SECOND_LEVEL_CACHE, NbBundle.getMessage(HibernateCfgCompletionManager.class, "USE_SECOND_LEVEL_CACHE_DESC"), // NOI18N
            Environment.USE_STRUCTURED_CACHE, NbBundle.getMessage(HibernateCfgCompletionManager.class, "USE_STRUCTURED_CACHE_DESC"), // NOI18N
            Environment.USER_TRANSACTION, NbBundle.getMessage(HibernateCfgCompletionManager.class, "USER_TRANSACTION_DESC"), // NOI18N
            Environment.USE_REFLECTION_OPTIMIZER, NbBundle.getMessage(HibernateCfgCompletionManager.class, "USE_REFLECTION_OPTIMIZER_DESC"), // NOI18N
            Environment.WRAP_RESULT_SETS, NbBundle.getMessage(HibernateCfgCompletionManager.class, "WRAP_RESULT_SETS_DESC") // NOI18N
        };

        // Items for property names 
        AttributeValueCompletor propertyNamesCompletor = new AttributeValueCompletor(propertyNames);
        registerCompletor(PROPERTY_TAG, NAME_ATTRIB, propertyNamesCompletor);

        // Items for mapping xml files
        HbMappingFileCompletor mappingFilesCompletor = new HbMappingFileCompletor();
        registerCompletor(MAPPING_TAG, RESOURCE_ATTRIB, mappingFilesCompletor);
    }
    private static HibernateCfgCompletionManager INSTANCE = new HibernateCfgCompletionManager();

    public static HibernateCfgCompletionManager getDefault() {
        return INSTANCE;
    }

    public void completeAttributeValues(CompletionResultSet resultSet, CompletionContext context) {
        String tagName = context.getTag().getNodeName();
        TokenItem attrib = ContextUtilities.getAttributeToken(context.getCurrentToken());
        String attribName = attrib != null ? attrib.getImage() : null;

        Completor completor = locateCompletor(tagName, attribName);
        if (completor != null) {
            resultSet.addAllItems(completor.doCompletion(context));
            if (completor.getAnchorOffset() != -1) {
                resultSet.setAnchorOffset(completor.getAnchorOffset());
            }
        }
    }

    public void completeValues(CompletionResultSet resultSet, CompletionContext context) {
        DocumentContext docContext = context.getDocumentContext();
        SyntaxElement curElem = docContext.getCurrentElement();
        SyntaxElement prevElem = docContext.getCurrentElement().getPrevious();
        Tag propTag = null;

        // If current element is a start tag and its tag is <property>
        // or the current element is text and its prev is a start <property> tag,
        // then do the code completion
        if ((curElem instanceof StartTag) && ((StartTag) curElem).getTagName().equalsIgnoreCase(PROPERTY_TAG)) {
            propTag = (StartTag) curElem;
        } else if ((curElem instanceof Text) && (prevElem instanceof StartTag) &&
                ((StartTag) prevElem).getTagName().equalsIgnoreCase(PROPERTY_TAG)) {
            propTag = (StartTag) prevElem;
        } else {
            return;
        }
        
        String propName = HibernateCompletionEditorUtil.getHbPropertyName(propTag);
        int caretOffset = context.getCaretOffset();
        String typedChars = context.getTypedPrefix();
        
        Object possibleValue = HibernateCfgProperties.getPossiblePropertyValue(propName);
        
        if (possibleValue instanceof String[]) {
            
            // Add the values in the String[] as completion items
            String[] values = (String[])possibleValue;
            
            for (int i = 0; i < values.length; i++) {
                if (values[i].startsWith(typedChars.trim())
                        || values[i].startsWith( "org.hibernate.dialect." + typedChars.trim()) ) { // NOI18N
                    HibernateCompletionItem item = 
                            HibernateCompletionItem.createHbPropertyValueItem(caretOffset, values[i]);
                    resultSet.addItem(item);
                }
            }

            resultSet.setAnchorOffset(context.getCurrentToken().getPrevious().getOffset() + 1);
        } 
    }

    public void completeAttributes(CompletionResultSet resultSet, CompletionContext context) {
    }

    public void completeElements(CompletionResultSet resultSet, CompletionContext context) {
    }

    private static abstract class Completor {

        private int anchorOffset = -1;

        public abstract List<HibernateCompletionItem> doCompletion(CompletionContext context);

        protected void setAnchorOffset(int anchorOffset) {
            this.anchorOffset = anchorOffset;
        }

        public int getAnchorOffset() {
            return anchorOffset;
        }
    }

    private void registerCompletor(String tagName, String attribName,
            Completor completor) {
        completors.put(createRegisteredName(tagName, attribName), completor);
    }

    private static String createRegisteredName(String nodeName, String attributeName) {
        StringBuilder builder = new StringBuilder();
        if (nodeName != null && nodeName.trim().length() > 0) {
            builder.append("/nodeName=");  // NOI18N
            builder.append(nodeName);
        } else {
            builder.append("/nodeName=");  // NOI18N
            builder.append("*");  // NOI18N
        }

        if (attributeName != null && attributeName.trim().length() > 0) {
            builder.append("/attribute="); // NOI18N
            builder.append(attributeName);
        }

        return builder.toString();
    }

    private Completor locateCompletor(String nodeName, String attributeName) {
        String key = createRegisteredName(nodeName, attributeName);
        if (completors.containsKey(key)) {
            return completors.get(key);
        }

        key = createRegisteredName("*", attributeName); // NOI18N
        if (completors.containsKey(key)) {
            return completors.get(key);
        }

        return null;
    }

    /**
     * A simple completor for general attribute value items
     * 
     * Takes an array of strings, the even elements being the display text of the items
     * and the odd ones being the corresponding documentation of the items
     * 
     */
    private static class AttributeValueCompletor extends Completor {

        private String[] itemTextAndDocs;

        public AttributeValueCompletor(String[] itemTextAndDocs) {
            this.itemTextAndDocs = itemTextAndDocs;
        }

        public List<HibernateCompletionItem> doCompletion(CompletionContext context) {
            List<HibernateCompletionItem> results = new ArrayList<HibernateCompletionItem>();
            int caretOffset = context.getCaretOffset();
            String typedChars = context.getTypedPrefix();
            
            for (int i = 0; i < itemTextAndDocs.length; i += 2) {
                if (itemTextAndDocs[i].startsWith(typedChars.trim()) 
                        || itemTextAndDocs[i].startsWith( "hibernate." + typedChars.trim()) ) { // NOI18N
                    HibernateCompletionItem item = HibernateCompletionItem.createAttribValueItem(caretOffset - typedChars.length(),
                            itemTextAndDocs[i], itemTextAndDocs[i + 1]);
                    results.add(item);
                }
            }

            setAnchorOffset(context.getCurrentToken().getOffset() + 1);
            return results;
        }
    }

    private static class HbMappingFileCompletor extends Completor {

        public HbMappingFileCompletor() {
        }

        public List<HibernateCompletionItem> doCompletion(CompletionContext context) {
            List<HibernateCompletionItem> results = new ArrayList<HibernateCompletionItem>();
            int caretOffset = context.getCaretOffset();
            String typedChars = context.getTypedPrefix();

            // TODO: hard-code some mapping files here for testing code-completion
            String[] mappingFiles = new String[]{
                "travel/Person.hbm.xml",
                "travel/Trip.hbm.xml"
            };

            for (int i = 0; i < mappingFiles.length; i++) {
                if (mappingFiles[i].startsWith(typedChars.trim())) {
                    HibernateCompletionItem item =
                            HibernateCompletionItem.createHbMappingFileItem(caretOffset - typedChars.length(),
                            mappingFiles[i]);
                    results.add(item);
                }
            }

            setAnchorOffset(context.getCurrentToken().getOffset() + 1);
            return results;
        }
    }
}
