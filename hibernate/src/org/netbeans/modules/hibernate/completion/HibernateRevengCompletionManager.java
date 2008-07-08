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
import java.util.List;
import java.util.Map;
import org.netbeans.editor.TokenItem;
import org.netbeans.modules.hibernate.reveng.HibernateRevengXmlConstants;
import org.openide.util.NbBundle;

/**
 * This class figures out the completion items for various attributes
 * 
 * @author gowri
 */
public final class HibernateRevengCompletionManager {

    private static Map<String, Completor> completors = new HashMap<String, Completor>();

    private HibernateRevengCompletionManager() {
        setupCompletors();
    }

    private void setupCompletors() {

        // Completion items for id generator
        String[] generatorClasses = new String[]{
            "increment", NbBundle.getMessage(HibernateRevengCompletionManager.class, "INCREMENT_GENERATOR_DESC"), // NOI18N
            "identity", NbBundle.getMessage(HibernateRevengCompletionManager.class, "IDENTITY_GENERATOR_DESC"), // NOI18N
            "sequence", NbBundle.getMessage(HibernateRevengCompletionManager.class, "SEQUENCE_GENERATOR_DESC"), // NOI18N
            "hilo", NbBundle.getMessage(HibernateRevengCompletionManager.class, "HILO_GENERATOR_DESC"), // NOI18N
            "seqhilo", NbBundle.getMessage(HibernateRevengCompletionManager.class, "SEQHILO_GENERATOR_DESC"), // NOI18N
            "uuid", NbBundle.getMessage(HibernateRevengCompletionManager.class, "UUID_GENERATOR_DESC"), // NOI18N
            "guid", NbBundle.getMessage(HibernateRevengCompletionManager.class, "GUID_GENERATOR_DESC"), // NOI18N
            "native", NbBundle.getMessage(HibernateRevengCompletionManager.class, "NATIVE_GENERATOR_DESC"), // NOI18N
            "assigned", NbBundle.getMessage(HibernateRevengCompletionManager.class, "ASSIGNED_GENERATOR_DESC"), // NOI18N
            "select", NbBundle.getMessage(HibernateRevengCompletionManager.class, "SELECT_GENERATOR_DESC"), // NOI18N
            "foreign", NbBundle.getMessage(HibernateRevengCompletionManager.class, "FOREIGN_GENERATOR_DESC"), // NOI18N
            "sequence-identity", NbBundle.getMessage(HibernateRevengCompletionManager.class, "SEQUENCE_IDENTITY_GENERATOR_DESC") // NOI18N
         // NOI18N
         // NOI18N
         // NOI18N
        };

        // Completion items for Hibernate type
        String[] hibernateTypes = new String[]{
            "big_decimal", NbBundle.getMessage(HibernateRevengCompletionManager.class, "BIG_DECIMAL_DESC"), // NOI18N
            "big_integer", NbBundle.getMessage(HibernateRevengCompletionManager.class, "BIG_INTEGER_DESC"), // NOI18N
            "binary", NbBundle.getMessage(HibernateRevengCompletionManager.class, "BINARY_DESC"), // NOI18N
            "blob", NbBundle.getMessage(HibernateRevengCompletionManager.class, "BLOB_DESC"), // NOI18N
            "boolean", NbBundle.getMessage(HibernateRevengCompletionManager.class, "BOOLEAN_DESC"), // NOI18N
            "byte", NbBundle.getMessage(HibernateRevengCompletionManager.class, "BYTE_DESC"), // NOI18N
            "calendar", NbBundle.getMessage(HibernateRevengCompletionManager.class, "CALENDAR_DESC"), // NOI18N
            "calendar_date", NbBundle.getMessage(HibernateRevengCompletionManager.class, "CALENDAR_DATE_DESC"), // NOI18N
            "character", NbBundle.getMessage(HibernateRevengCompletionManager.class, "CHARACTER_DESC"), // NOI18N
            "class", NbBundle.getMessage(HibernateRevengCompletionManager.class, "CLASS_DESC"), // NOI18N
            "clob", NbBundle.getMessage(HibernateRevengCompletionManager.class, "CLOB_DESC"), // NOI18N
            "currency", NbBundle.getMessage(HibernateRevengCompletionManager.class, "CURRENCY_DESC"), // NOI18N
            "date", NbBundle.getMessage(HibernateRevengCompletionManager.class, "DATE_DESC"), // NOI18N
            "double", NbBundle.getMessage(HibernateRevengCompletionManager.class, "DOUBLE_DESC"), // NOI18N
            "float", NbBundle.getMessage(HibernateRevengCompletionManager.class, "FLOAT_DESC"), // NOI18N
            "imm_binary", NbBundle.getMessage(HibernateRevengCompletionManager.class, "IMM_BINARY_DESC"), // NOI18N
            "imm_calendar", NbBundle.getMessage(HibernateRevengCompletionManager.class, "IMM_CALENDAR_DESC"), // NOI18N
            "imm_calendar_date", NbBundle.getMessage(HibernateRevengCompletionManager.class, "IMM_CALENDAR_DATE_DESC"), // NOI18N
            "imm_date", NbBundle.getMessage(HibernateRevengCompletionManager.class, "IMM_DATE_DESC"), // NOI18N
            "imm_serializable", NbBundle.getMessage(HibernateRevengCompletionManager.class, "IMM_SERIALIZABLE_DESC"), // NOI18N
            "imm_time", NbBundle.getMessage(HibernateRevengCompletionManager.class, "IMM_TIME_DESC"), // NOI18N
            "imm_timestamp", NbBundle.getMessage(HibernateRevengCompletionManager.class, "IMM_TIMESTAMP_DESC"), // NOI18N
            "integer", NbBundle.getMessage(HibernateRevengCompletionManager.class, "INTEGER_DESC"), // NOI18N
            "locale", NbBundle.getMessage(HibernateRevengCompletionManager.class, "LOCALE_DESC"), // NOI18N
            "long", NbBundle.getMessage(HibernateRevengCompletionManager.class, "LONG_DESC"), // NOI18N
            "serializable", NbBundle.getMessage(HibernateRevengCompletionManager.class, "SERIALIZABLE_DESC"), // NOI18N
            "short", NbBundle.getMessage(HibernateRevengCompletionManager.class, "SHORT_DESC"), // NOI18N
            "string", NbBundle.getMessage(HibernateRevengCompletionManager.class, "STRING_DESC"), // NOI18N
            "text", NbBundle.getMessage(HibernateRevengCompletionManager.class, "TEXT_DESC"), // NOI18N
            "time", NbBundle.getMessage(HibernateRevengCompletionManager.class, "TIME_DESC"), // NOI18N
            "timestamp", NbBundle.getMessage(HibernateRevengCompletionManager.class, "TIMESTAMP_DESC"), // NOI18N
            "timezone", NbBundle.getMessage(HibernateRevengCompletionManager.class, "TIMEZONE_DESC"), // NOI18N,
            "true_false", NbBundle.getMessage(HibernateRevengCompletionManager.class, "TRUE_FALSE_DESC"), // NOI18N
            "yes_no", NbBundle.getMessage(HibernateRevengCompletionManager.class, "YES_NO_DESC") // NOI18N
         // NOI18N
         // NOI18N
         // NOI18N
        };

        // Items for package attribute in the root element
        Completor.JavaClassCompletor javaPackageCompletor = new Completor.JavaClassCompletor(true);
        registerCompletor(HibernateRevengXmlConstants.TABLE_FILTER_TAG, HibernateRevengXmlConstants.PACKAGE_ATTRIB, javaPackageCompletor);

        // Items for Id generator classes
        Completor.AttributeValueCompletor generatorCompletor = new Completor.AttributeValueCompletor(generatorClasses);
        registerCompletor(HibernateRevengXmlConstants.GENERATOR_TAG, HibernateRevengXmlConstants.CLASS_ATTRIB, generatorCompletor);

        // Items for Hibernate type 
        Completor.AttributeValueCompletor typeCompletor = new Completor.AttributeValueCompletor(hibernateTypes);
        registerCompletor(HibernateRevengXmlConstants.SQL_TYPE_TAG, HibernateRevengXmlConstants.HIBERNATE_TYPE_ATTRIB, typeCompletor);
        
        // Items for classes to be mapped
        Completor.JavaClassCompletor javaClassCompletor = new Completor.JavaClassCompletor(false);
        registerCompletor(HibernateRevengXmlConstants.TABLE_TAG, HibernateRevengXmlConstants.CLASS_ATTRIB, javaClassCompletor);        
        
        // Items for database tables to be mapped to
        Completor.DatabaseTableCompletor databaseTableCompletor = new Completor.DatabaseTableCompletor();
        registerCompletor(HibernateRevengXmlConstants.TABLE_TAG, HibernateRevengXmlConstants.NAME_ATTRIB, databaseTableCompletor);
        registerCompletor(HibernateRevengXmlConstants.TABLE_FILTER_TAG, HibernateRevengXmlConstants.MATCH_NAME_ATTRIB, databaseTableCompletor);        
        
    }
    private static HibernateRevengCompletionManager INSTANCE = new HibernateRevengCompletionManager();

    public static HibernateRevengCompletionManager getDefault() {
        return INSTANCE;
    }

    public int completeAttributeValues(CompletionContext context, List<HibernateCompletionItem> valueItems) {
        int anchorOffset = -1;    
        if(context.getTag() == null)
            return anchorOffset;
        
        String tagName = context.getTag().getNodeName();
        TokenItem attrib = ContextUtilities.getAttributeToken(context.getCurrentToken());
        String attribName = attrib != null ? attrib.getImage() : null;

        Completor completor = locateCompletor(tagName, attribName);
        if (completor != null) {
            valueItems.addAll(completor.doCompletion(context));
            if (completor.getAnchorOffset() != -1) {
                anchorOffset = completor.getAnchorOffset();
            }
        }
        
        return anchorOffset;
    }

    public int completeAttributes(CompletionContext context, List<HibernateCompletionItem> items) {
        return -1;
    }

    public int completeElements(CompletionContext context, List<HibernateCompletionItem> items) {
        return -1;
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
