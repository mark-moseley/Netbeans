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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.ruby;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import org.netbeans.api.gsf.CompilationInfo;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.netbeans.api.gsf.CompilationInfo;
import org.netbeans.api.gsf.Index;
import org.netbeans.api.gsf.NameKind;
import org.netbeans.modules.ruby.elements.IndexedClass;
import org.netbeans.modules.ruby.elements.IndexedElement;
import org.netbeans.modules.ruby.elements.IndexedField;
import org.netbeans.modules.ruby.elements.IndexedMethod;

/**
 * @author Tor Norbye
 */
public class RubyIndexerTest extends RubyTestBase {
    
    public RubyIndexerTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    private class TestIndex extends Index {
        private final String localUrl;
        private List<String> documents = new ArrayList<String>();
        private List<String> emptyDocuments = new ArrayList<String>();
        
        public TestIndex(String localUrl) {
            // Leave the end
            int index = localUrl.lastIndexOf('/');
            if (index != -1) {
                localUrl = localUrl.substring(0, index);
            }
            this.localUrl = localUrl;
        }
        
        @Override
        public String toString() {
            Collections.sort(emptyDocuments);
            Collections.sort(documents);
            StringBuilder sb = new StringBuilder();
            int documentNumber = 0;
            for (String s : emptyDocuments) {
                sb.append("\n\nDocument ");
                sb.append(Integer.toString(documentNumber++));
                sb.append("\n");
                sb.append(s);
            }

            for (String s : documents) {
                sb.append("\n\nDocument ");
                sb.append(Integer.toString(documentNumber++));
                sb.append("\n");
                sb.append(s);
            }


            return sb.toString().replace(localUrl, "<TESTURL>");
        }
        
        private String sortCommaList(String s) {
            String[] items = s.split(",");
            Arrays.sort(items);
            StringBuilder sb = new StringBuilder();
            for (String item : items) {
                if (sb.length() > 0) {
                    sb.append(",");
                }
                sb.append(item);
            }
            
            return sb.toString();
        }
        
        private String prettyPrintValue(String key, String value) {
            if (value == null) {
                return value;
            }
            if (RubyIndexer.FIELD_METHOD_NAME.equals(key)) {
                // Decode the attributes
                int attributeIndex = value.indexOf(';');
                if (attributeIndex != -1) {
                    int flags = IndexedElement.stringToFlag(value, attributeIndex+1);
                    if (flags != 0) {
                        String desc = IndexedMethod.decodeFlags(flags);
                        value = value.substring(0, attributeIndex) + desc + value.substring(attributeIndex+3);
                    }
                }
            } else if (RubyIndexer.FIELD_CLASS_ATTRS.equals(key)) {
                // Decode the attributes
                int flags = IndexedElement.stringToFlag(value, 0);
                if (flags != 0) {
                    String desc = IndexedClass.decodeFlags(flags);
                    value = desc + value.substring(2);
                } else {
                    value = "|CLASS|";
                }
            } else if (RubyIndexer.FIELD_FIELD_NAME.equals(key)) {
                // Decode the attributes
                int attributeIndex = value.indexOf(';');
                if (attributeIndex != -1) {
                    int flags = IndexedElement.stringToFlag(value, attributeIndex+1);
                    if (flags != 0) {
                        String desc = IndexedField.decodeFlags(flags);
                        value = value.substring(0, attributeIndex) + desc + value.substring(attributeIndex+3);
                    }
                }
            }
            
            return value;
        }

        public void gsfStore(Set<Map<String, String>> fieldToData, Set<Map<String, String>> noIndexData, Map<String, String> toDelete) throws IOException {
            StringBuilder sb = new StringBuilder();
            sb.append("Delete:");
            List<String> keys = new ArrayList<String>(toDelete.keySet());
            Collections.sort(keys);
            for (String key : keys) {
                sb.append("  ");
                sb.append(key);
                sb.append(" : ");
                sb.append(toDelete.get(key));
                sb.append("\n");
            }
            sb.append("\n");

            sb.append("Indexed:");
            sb.append("\n");
            List<String> strings = new ArrayList<String>();
            for (Map<String,String> map : fieldToData) {
                for (String key : map.keySet()) {
                    strings.add(key + " : " + prettyPrintValue(key, map.get(key)));
                }
            }
            Collections.sort(strings);
            for (String string : strings) {
                sb.append("  ");
                sb.append(string);
                sb.append("\n");
            }

            sb.append("\n");
            sb.append("Not Indexed:");
            sb.append("\n");
            strings = new ArrayList<String>();
            for (Map<String,String> map : noIndexData) {
                for (String key : map.keySet()) {
                    String value = prettyPrintValue(key, map.get(key));
                    if (value.indexOf(',') != -1) {
                        value = sortCommaList(value);
                    }
                    strings.add(key + " : " + value);
                }
            }
            Collections.sort(strings);
            for (String string : strings) {
                sb.append("  ");
                sb.append(string);
                sb.append("\n");
            }
            
            String s = sb.toString();
            if (fieldToData.size() == 0 && noIndexData.size() == 0) {
                emptyDocuments.add(s);
            } else {
                documents.add(s);
            }
        }

        public void gsfSearch(String primaryField, String name, NameKind kind, Set<SearchScope> scope, Set<SearchResult> result) throws IOException {
            throw new UnsupportedOperationException("Not supported in this test.");
        }
        
    }
    
    private void checkIndexer(String relFilePath) throws Exception {
        CompilationInfo info = getInfo(relFilePath);
        RubyParseResult rpr = (RubyParseResult) info.getParserResult();

        File rubyFile = new File(getDataDir(), relFilePath);
        Index index = new TestIndex(rubyFile.toURI().toURL().toExternalForm());
        RubyIndexer indexer = new RubyIndexer();
        RubyIndex.setClusterUrl("file:/bogus"); // No translation
        indexer.updateIndex(index, rpr);
        
        String annotatedSource = index.toString();

        assertDescriptionMatches(relFilePath, annotatedSource, false, ".indexed");
    }
    
    public void testAnalysis2() throws Exception {
        checkIndexer("testfiles/ape.rb");
    }
    
    public void testAnalysis() throws Exception {
        checkIndexer("testfiles/postgresql_adapter.rb");
    }

    public void testAnalysis3() throws Exception {
        checkIndexer("testfiles/date.rb");
    }

    public void testAnalysis4() throws Exception {
        checkIndexer("testfiles/resolv.rb");
    }

    public void testUnused() throws Exception {
        checkIndexer("testfiles/unused.rb");
    }

    public void testRails1() throws Exception {
        checkIndexer("testfiles/action_controller.rb");
    }

    public void testRails2() throws Exception {
        checkIndexer("testfiles/action_view.rb");
    }
    
    public void testRails3() throws Exception {
        checkIndexer("testfiles/action_mailer.rb");
    }
    
    public void testRails4() throws Exception {
        checkIndexer("testfiles/action_web_service.rb");
    }
    
    public void testRails5() throws Exception {
        checkIndexer("testfiles/active_record.rb");
    }

    public void testRails6() throws Exception {
        checkIndexer("testfiles/lib/action_controller/assertions.rb");
    }

    public void testTopLevel() throws Exception {
        checkIndexer("testfiles/top_level.rb");
    }
    
    public void testTopLevel2() throws Exception {
        checkIndexer("testfiles/option_parser_spec.rb");
    }

    public void testTopLevel3() throws Exception {
        checkIndexer("testfiles/method_definer_test.rb");
    }

    public void testMigration1() throws Exception {
        checkIndexer("testfiles/migrate/001_create_products.rb");
    }
    public void testMigration2() throws Exception {
        checkIndexer("testfiles/migrate/002_add_price.rb");
    }
    public void testMigration3() throws Exception {
        checkIndexer("testfiles/migrate/003_add_test_data.rb");
    }
    public void testMigration4() throws Exception {
        checkIndexer("testfiles/migrate/004_add_sessions.rb");
    }
    public void testMigration5() throws Exception {
        checkIndexer("testfiles/migrate/005_create_orders.rb");
    }
    public void testMigration6() throws Exception {
        checkIndexer("testfiles/migrate/006_create_line_items.rb");
    }
    public void testMigration7() throws Exception {
        checkIndexer("testfiles/migrate/007_create_users.rb");
    }
    public void testMigration8() throws Exception {
        checkIndexer("testfiles/migrate/007_add_assets_and_resources.rb");
    }
    public void testMigration9() throws Exception {
        checkIndexer("testfiles/migrate/029_add_correct_comment_lifetime.rb");
    }
    public void testMigration10() throws Exception {
        checkIndexer("testfiles/migrate/044_store_single_filter.rb");
    }
    public void testMigration11() throws Exception {
        checkIndexer("testfiles/migrate/001_create_products_renamed.rb");
    }

    public void testRails20Migrations() throws Exception {
        checkIndexer("testfiles/migrate/100_rails20_migrations.rb");
    }
    
    public void testRails20Migrations2() throws Exception {
        checkIndexer("testfiles/migrate/101_rails20_migrations.rb");
    }
    
    public void testRails20Migrations3() throws Exception {
        checkIndexer("testfiles/migrate/102_rails20_migrations.rb");
    }
    
    public void testSchemaDepot() throws Exception {
        checkIndexer("testfiles/migrate/schemas/depot/db/schema.rb");
    }

    public void testSchemaMephisto() throws Exception {
        checkIndexer("testfiles/migrate/schemas/mephisto/db/schema.rb");
    }

    public void testClassvar() throws Exception {
        checkIndexer("testfiles/classvar.rb");
    }

    public void testRails20SchemaDefs() throws Exception {
        checkIndexer("testfiles/activerecord-2.0.1/schema_definitions.rb");
    }
    
    // TODO - test :nodoc: on methods and classes!!!
}
