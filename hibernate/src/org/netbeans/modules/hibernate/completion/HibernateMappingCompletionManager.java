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

import org.netbeans.modules.hibernate.editor.ContextUtilities;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.editor.TokenItem;
import org.netbeans.modules.hibernate.mapping.HibernateMappingXmlConstants;
import org.netbeans.spi.editor.completion.CompletionResultSet;
import org.openide.util.NbBundle;

/**
 * This class figures out the completion items for various attributes
 * 
 * @author Dongmei Cao
 */
public final class HibernateMappingCompletionManager {

    private static Map<String, Completor> completors = new HashMap<String, Completor>();

    private HibernateMappingCompletionManager() {
        setupCompletors();
    }

    private void setupCompletors() {

        // Completion items for id generator
        String[] generatorClasses = new String[]{
            "increment", NbBundle.getMessage(HibernateMappingCompletionManager.class, "INCREMENT_GENERATOR_DESC"), // NOI18N
            "identity", NbBundle.getMessage(HibernateMappingCompletionManager.class, "IDENTITY_GENERATOR_DESC"), // NOI18N
            "sequence", NbBundle.getMessage(HibernateMappingCompletionManager.class, "SEQUENCE_GENERATOR_DESC"), // NOI18N
            "hilo", NbBundle.getMessage(HibernateMappingCompletionManager.class, "HILO_GENERATOR_DESC"), // NOI18N
            "seqhilo", NbBundle.getMessage(HibernateMappingCompletionManager.class, "SEQHILO_GENERATOR_DESC"), // NOI18N
            "uuid", NbBundle.getMessage(HibernateMappingCompletionManager.class, "UUID_GENERATOR_DESC"), // NOI18N
            "guid", NbBundle.getMessage(HibernateMappingCompletionManager.class, "GUID_GENERATOR_DESC"), // NOI18N
            "native", NbBundle.getMessage(HibernateMappingCompletionManager.class, "NATIVE_GENERATOR_DESC"), // NOI18N
            "assigned", NbBundle.getMessage(HibernateMappingCompletionManager.class, "ASSIGNED_GENERATOR_DESC"), // NOI18N
            "select", NbBundle.getMessage(HibernateMappingCompletionManager.class, "SELECT_GENERATOR_DESC"), // NOI18N
            "foreign", NbBundle.getMessage(HibernateMappingCompletionManager.class, "FOREIGN_GENERATOR_DESC"), // NOI18N
            "sequence-identity", NbBundle.getMessage(HibernateMappingCompletionManager.class, "SEQUENCE_IDENTITY_GENERATOR_DESC") // NOI18N
         // NOI18N
        };

        // Completion items for Hibernate type
        String[] hibernateTypes = new String[]{
            "big_decimal", NbBundle.getMessage(HibernateMappingCompletionManager.class, "BIG_DECIMAL_DESC"), // NOI18N
            "big_integer", NbBundle.getMessage(HibernateMappingCompletionManager.class, "BIG_INTEGER_DESC"), // NOI18N
            "binary", NbBundle.getMessage(HibernateMappingCompletionManager.class, "BINARY_DESC"), // NOI18N
            "blob", NbBundle.getMessage(HibernateMappingCompletionManager.class, "BLOB_DESC"), // NOI18N
            "boolean", NbBundle.getMessage(HibernateMappingCompletionManager.class, "BOOLEAN_DESC"), // NOI18N
            "byte", NbBundle.getMessage(HibernateMappingCompletionManager.class, "BYTE_DESC"), // NOI18N
            "calendar", NbBundle.getMessage(HibernateMappingCompletionManager.class, "CALENDAR_DESC"), // NOI18N
            "calendar_date", NbBundle.getMessage(HibernateMappingCompletionManager.class, "CALENDAR_DATE_DESC"), // NOI18N
            "character", NbBundle.getMessage(HibernateMappingCompletionManager.class, "CHARACTER_DESC"), // NOI18N
            "class", NbBundle.getMessage(HibernateMappingCompletionManager.class, "CLASS_DESC"), // NOI18N
            "clob", NbBundle.getMessage(HibernateMappingCompletionManager.class, "CLOB_DESC"), // NOI18N
            "currency", NbBundle.getMessage(HibernateMappingCompletionManager.class, "CURRENCY_DESC"), // NOI18N
            "date", NbBundle.getMessage(HibernateMappingCompletionManager.class, "DATE_DESC"), // NOI18N
            "double", NbBundle.getMessage(HibernateMappingCompletionManager.class, "DOUBLE_DESC"), // NOI18N
            "float", NbBundle.getMessage(HibernateMappingCompletionManager.class, "FLOAT_DESC"), // NOI18N
            "imm_binary", NbBundle.getMessage(HibernateMappingCompletionManager.class, "IMM_BINARY_DESC"), // NOI18N
            "imm_calendar", NbBundle.getMessage(HibernateMappingCompletionManager.class, "IMM_CALENDAR_DESC"), // NOI18N
            "imm_calendar_date", NbBundle.getMessage(HibernateMappingCompletionManager.class, "IMM_CALENDAR_DATE_DESC"), // NOI18N
            "imm_date", NbBundle.getMessage(HibernateMappingCompletionManager.class, "IMM_DATE_DESC"), // NOI18N
            "imm_serializable", NbBundle.getMessage(HibernateMappingCompletionManager.class, "IMM_SERIALIZABLE_DESC"), // NOI18N
            "imm_time", NbBundle.getMessage(HibernateMappingCompletionManager.class, "IMM_TIME_DESC"), // NOI18N
            "imm_timestamp", NbBundle.getMessage(HibernateMappingCompletionManager.class, "IMM_TIMESTAMP_DESC"), // NOI18N
            "integer", NbBundle.getMessage(HibernateMappingCompletionManager.class, "INTEGER_DESC"), // NOI18N
            "locale", NbBundle.getMessage(HibernateMappingCompletionManager.class, "LOCALE_DESC"), // NOI18N
            "long", NbBundle.getMessage(HibernateMappingCompletionManager.class, "LONG_DESC"), // NOI18N
            "serializable", NbBundle.getMessage(HibernateMappingCompletionManager.class, "SERIALIZABLE_DESC"), // NOI18N
            "short", NbBundle.getMessage(HibernateMappingCompletionManager.class, "SHORT_DESC"), // NOI18N
            "string", NbBundle.getMessage(HibernateMappingCompletionManager.class, "STRING_DESC"), // NOI18N
            "text", NbBundle.getMessage(HibernateMappingCompletionManager.class, "TEXT_DESC"), // NOI18N
            "time", NbBundle.getMessage(HibernateMappingCompletionManager.class, "TIME_DESC"), // NOI18N
            "timestamp", NbBundle.getMessage(HibernateMappingCompletionManager.class, "TIMESTAMP_DESC"), // NOI18N
            "timezone", NbBundle.getMessage(HibernateMappingCompletionManager.class, "TIMEZONE_DESC"), // NOI18N,
            "true_false", NbBundle.getMessage(HibernateMappingCompletionManager.class, "TRUE_FALSE_DESC"), // NOI18N
            "yes_no", NbBundle.getMessage(HibernateMappingCompletionManager.class, "YES_NO_DESC") // NOI18N
         // NOI18N
        };

        String[] cascadeStyles = new String[]{
            "none", null, // NOI18N
            "all", null, // NOI18N
            "delete", null, // NOI18N
            "delete-orphan", null, // NOI18N
            "evict", null, // NOI18N
            "refresh", null, // NOI18N
            "lock", null, // NOI18N
            "merge", null, // NOI18N
            "persist", null, // NOI18N
            "replicate", null, // NOI18N
            "save-update", null // NOI18N
        };

        // Items for package attribute in the root element
        Completor.JavaClassCompletor javaPackageCompletor = new Completor.JavaClassCompletor(true);
        registerCompletor(HibernateMappingXmlConstants.MAPPING_TAG, HibernateMappingXmlConstants.PACKAGE_ATTRIB, javaPackageCompletor);

        // Items for Id generator classes
        Completor.AttributeValueCompletor generatorCompletor = new Completor.AttributeValueCompletor(generatorClasses);
        registerCompletor(HibernateMappingXmlConstants.GENERATOR_TAG, HibernateMappingXmlConstants.CLASS_ATTRIB, generatorCompletor);

        // Items for Hibernate type 
        Completor.AttributeValueCompletor typeCompletor = new Completor.AttributeValueCompletor(hibernateTypes);
        registerCompletor(HibernateMappingXmlConstants.PROPERTY_TAG, HibernateMappingXmlConstants.TYPE_ATTRIB, typeCompletor);
        registerCompletor(HibernateMappingXmlConstants.ID_TAG, HibernateMappingXmlConstants.TYPE_ATTRIB, typeCompletor);
        registerCompletor(HibernateMappingXmlConstants.DISCRIMINATOR_TAG, HibernateMappingXmlConstants.TYPE_ATTRIB, typeCompletor);
        registerCompletor(HibernateMappingXmlConstants.KEY_PROPERTY_TAG, HibernateMappingXmlConstants.TYPE_ATTRIB, typeCompletor);
        registerCompletor(HibernateMappingXmlConstants.VERSION_TAG, HibernateMappingXmlConstants.TYPE_ATTRIB, typeCompletor);
        registerCompletor(HibernateMappingXmlConstants.ELEMENT_TAG, HibernateMappingXmlConstants.TYPE_ATTRIB, typeCompletor);
        registerCompletor(HibernateMappingXmlConstants.MAP_KEY_TAG, HibernateMappingXmlConstants.TYPE_ATTRIB, typeCompletor);
        registerCompletor(HibernateMappingXmlConstants.INDEX_TAG, HibernateMappingXmlConstants.TYPE_ATTRIB, typeCompletor);
        registerCompletor(HibernateMappingXmlConstants.ANY_TAG, HibernateMappingXmlConstants.ID_TYPE_ATTRIB, typeCompletor);

        // Items for classes to be mapped
        Completor.JavaClassCompletor javaClassCompletor = new Completor.JavaClassCompletor(false);
        registerCompletor(HibernateMappingXmlConstants.CLASS_TAG, HibernateMappingXmlConstants.NAME_ATTRIB, javaClassCompletor);
        registerCompletor(HibernateMappingXmlConstants.ONE_TO_MANY_TAG, HibernateMappingXmlConstants.CLASS_ATTRIB, javaClassCompletor);
        registerCompletor(HibernateMappingXmlConstants.COMPOSITE_ID_TAG, HibernateMappingXmlConstants.CLASS_ATTRIB, javaClassCompletor);
        registerCompletor(HibernateMappingXmlConstants.KEY_MANY_TO_ONE_TAG, HibernateMappingXmlConstants.CLASS_ATTRIB, javaClassCompletor);
        registerCompletor(HibernateMappingXmlConstants.MANY_TO_ONE_TAG, HibernateMappingXmlConstants.CLASS_ATTRIB, javaClassCompletor);
        registerCompletor(HibernateMappingXmlConstants.ONE_TO_ONE_TAG, HibernateMappingXmlConstants.CLASS_ATTRIB, javaClassCompletor);
        registerCompletor(HibernateMappingXmlConstants.COMPONENT_TAG, HibernateMappingXmlConstants.CLASS_ATTRIB, javaClassCompletor);
        registerCompletor(HibernateMappingXmlConstants.SUBCLASS_TAG, HibernateMappingXmlConstants.NAME_ATTRIB, javaClassCompletor);
        registerCompletor(HibernateMappingXmlConstants.SUBCLASS_TAG, HibernateMappingXmlConstants.EXTENDS_ATTRIB, javaClassCompletor);
        registerCompletor(HibernateMappingXmlConstants.JOINED_SUBCLASS_TAG, HibernateMappingXmlConstants.NAME_ATTRIB, javaClassCompletor);
        registerCompletor(HibernateMappingXmlConstants.JOINED_SUBCLASS_TAG, HibernateMappingXmlConstants.EXTENDS_ATTRIB, javaClassCompletor);
        registerCompletor(HibernateMappingXmlConstants.JOINED_SUBCLASS_TAG, HibernateMappingXmlConstants.PERSISTER_ATTRIB, javaClassCompletor);
        registerCompletor(HibernateMappingXmlConstants.UNION_SUBCLASS_TAG, HibernateMappingXmlConstants.NAME_ATTRIB, javaClassCompletor);
        registerCompletor(HibernateMappingXmlConstants.UNION_SUBCLASS_TAG, HibernateMappingXmlConstants.EXTENDS_ATTRIB, javaClassCompletor);
        registerCompletor(HibernateMappingXmlConstants.UNION_SUBCLASS_TAG, HibernateMappingXmlConstants.PERSISTER_ATTRIB, javaClassCompletor);
        registerCompletor(HibernateMappingXmlConstants.IMPORT_TAG, HibernateMappingXmlConstants.CLASS_ATTRIB, javaClassCompletor);
        registerCompletor(HibernateMappingXmlConstants.MANY_TO_MANY_TAG, HibernateMappingXmlConstants.CLASS_ATTRIB, javaClassCompletor);

        // Items for properties to be mapped
        Completor.PropertyCompletor propertyCompletor = new Completor.PropertyCompletor();
        registerCompletor(HibernateMappingXmlConstants.PROPERTY_TAG, HibernateMappingXmlConstants.NAME_ATTRIB, propertyCompletor);
        registerCompletor(HibernateMappingXmlConstants.ID_TAG, HibernateMappingXmlConstants.NAME_ATTRIB, propertyCompletor);
        registerCompletor(HibernateMappingXmlConstants.SET_TAG, HibernateMappingXmlConstants.NAME_ATTRIB, propertyCompletor);
        registerCompletor(HibernateMappingXmlConstants.COMPOSITE_ID_TAG, HibernateMappingXmlConstants.NAME_ATTRIB, propertyCompletor);
        registerCompletor(HibernateMappingXmlConstants.KEY_PROPERTY_TAG, HibernateMappingXmlConstants.NAME_ATTRIB, propertyCompletor);
        registerCompletor(HibernateMappingXmlConstants.KEY_MANY_TO_ONE_TAG, HibernateMappingXmlConstants.NAME_ATTRIB, propertyCompletor);
        registerCompletor(HibernateMappingXmlConstants.VERSION_TAG, HibernateMappingXmlConstants.NAME_ATTRIB, propertyCompletor);
        registerCompletor(HibernateMappingXmlConstants.TIMESTAMP_TAG, HibernateMappingXmlConstants.NAME_ATTRIB, propertyCompletor);
        registerCompletor(HibernateMappingXmlConstants.MANY_TO_ONE_TAG, HibernateMappingXmlConstants.NAME_ATTRIB, propertyCompletor);
        registerCompletor(HibernateMappingXmlConstants.ONE_TO_ONE_TAG, HibernateMappingXmlConstants.NAME_ATTRIB, propertyCompletor);
        registerCompletor(HibernateMappingXmlConstants.COMPONENT_TAG, HibernateMappingXmlConstants.NAME_ATTRIB, propertyCompletor);
        registerCompletor(HibernateMappingXmlConstants.ANY_TAG, HibernateMappingXmlConstants.NAME_ATTRIB, propertyCompletor);
        registerCompletor(HibernateMappingXmlConstants.MAP_TAG, HibernateMappingXmlConstants.NAME_ATTRIB, propertyCompletor);
        registerCompletor(HibernateMappingXmlConstants.LIST_TAG, HibernateMappingXmlConstants.NAME_ATTRIB, propertyCompletor);

        // Items for database tables to be mapped to
        Completor.DatabaseTableCompletor databaseTableCompletor = new Completor.DatabaseTableCompletor();
        registerCompletor(HibernateMappingXmlConstants.CLASS_TAG, HibernateMappingXmlConstants.TABLE_ATTRIB, databaseTableCompletor);
        registerCompletor(HibernateMappingXmlConstants.SET_TAG, HibernateMappingXmlConstants.TABLE_ATTRIB, databaseTableCompletor);
        registerCompletor(HibernateMappingXmlConstants.JOINED_SUBCLASS_TAG, HibernateMappingXmlConstants.TABLE_ATTRIB, databaseTableCompletor);
        registerCompletor(HibernateMappingXmlConstants.UNION_SUBCLASS_TAG, HibernateMappingXmlConstants.TABLE_ATTRIB, databaseTableCompletor);
        registerCompletor(HibernateMappingXmlConstants.JOIN_TAG, HibernateMappingXmlConstants.TABLE_ATTRIB, databaseTableCompletor);
        registerCompletor(HibernateMappingXmlConstants.MAP_TAG, HibernateMappingXmlConstants.TABLE_ATTRIB, databaseTableCompletor);

        // Items for database columns to be mapped to
        Completor.DatabaseTableColumnCompletor databaseColumnCompletor = new Completor.DatabaseTableColumnCompletor();
        registerCompletor(HibernateMappingXmlConstants.PROPERTY_TAG, HibernateMappingXmlConstants.COLUMN_ATTRIB, databaseColumnCompletor);
        registerCompletor(HibernateMappingXmlConstants.ID_TAG, HibernateMappingXmlConstants.COLUMN_ATTRIB, databaseColumnCompletor);
        registerCompletor(HibernateMappingXmlConstants.KEY_TAG, HibernateMappingXmlConstants.COLUMN_ATTRIB, databaseColumnCompletor);
        registerCompletor(HibernateMappingXmlConstants.DISCRIMINATOR_TAG, HibernateMappingXmlConstants.COLUMN_ATTRIB, databaseColumnCompletor);
        registerCompletor(HibernateMappingXmlConstants.KEY_PROPERTY_TAG, HibernateMappingXmlConstants.COLUMN_ATTRIB, databaseColumnCompletor);
        registerCompletor(HibernateMappingXmlConstants.KEY_MANY_TO_ONE_TAG, HibernateMappingXmlConstants.COLUMN_ATTRIB, databaseColumnCompletor);
        registerCompletor(HibernateMappingXmlConstants.VERSION_TAG, HibernateMappingXmlConstants.COLUMN_ATTRIB, databaseColumnCompletor);
        registerCompletor(HibernateMappingXmlConstants.TIMESTAMP_TAG, HibernateMappingXmlConstants.COLUMN_ATTRIB, databaseColumnCompletor);
        registerCompletor(HibernateMappingXmlConstants.MANY_TO_ONE_TAG, HibernateMappingXmlConstants.COLUMN_ATTRIB, databaseColumnCompletor);
        registerCompletor(HibernateMappingXmlConstants.COLUMN_TAG, HibernateMappingXmlConstants.NAME_ATTRIB, databaseColumnCompletor);
        registerCompletor(HibernateMappingXmlConstants.LIST_INDEX_TAG, HibernateMappingXmlConstants.COLUMN_ATTRIB, databaseColumnCompletor);
        registerCompletor(HibernateMappingXmlConstants.INDEX_TAG, HibernateMappingXmlConstants.COLUMN_ATTRIB, databaseColumnCompletor);
        registerCompletor(HibernateMappingXmlConstants.MAP_KEY_TAG, HibernateMappingXmlConstants.COLUMN_ATTRIB, databaseColumnCompletor);
        registerCompletor(HibernateMappingXmlConstants.ELEMENT_TAG, HibernateMappingXmlConstants.COLUMN_ATTRIB, databaseColumnCompletor);
        registerCompletor(HibernateMappingXmlConstants.MANY_TO_MANY_TAG, HibernateMappingXmlConstants.COLUMN_ATTRIB, databaseColumnCompletor);

        // Items for cascade attribute
        Completor.CascadeStyleCompletor cascadeStyleCompletor = new Completor.CascadeStyleCompletor(cascadeStyles);
        registerCompletor(HibernateMappingXmlConstants.MANY_TO_ONE_TAG, HibernateMappingXmlConstants.CASCADE_ATTRIB, cascadeStyleCompletor);
        registerCompletor(HibernateMappingXmlConstants.ONE_TO_ONE_TAG, HibernateMappingXmlConstants.CASCADE_ATTRIB, cascadeStyleCompletor);
        registerCompletor(HibernateMappingXmlConstants.ANY_TAG, HibernateMappingXmlConstants.CASCADE_ATTRIB, cascadeStyleCompletor);
        registerCompletor(HibernateMappingXmlConstants.MAP_TAG, HibernateMappingXmlConstants.CASCADE_ATTRIB, cascadeStyleCompletor);
    }
    private static HibernateMappingCompletionManager INSTANCE = new HibernateMappingCompletionManager();

    public static HibernateMappingCompletionManager getDefault() {
        return INSTANCE;
    }

    public void completeAttributeValues(CompletionResultSet resultSet, CompletionContext context) {
        if(context.getTag() == null)
            return;
        
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

    public void completeAttributes(CompletionResultSet resultSet, CompletionContext context) {
    }

    public void completeElements(CompletionResultSet resultSet, CompletionContext context) {
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
}
