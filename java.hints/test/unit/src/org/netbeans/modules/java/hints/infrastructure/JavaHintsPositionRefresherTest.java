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

package org.netbeans.modules.java.hints.infrastructure;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.text.Document;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.SourceUtilsTestUtil;
import org.netbeans.api.java.source.TestUtilities;
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.Token;
import static org.junit.Assert.*;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.editor.java.JavaKit;
import org.netbeans.modules.java.JavaDataLoader;
import org.netbeans.modules.parsing.api.indexing.IndexingManager;
import org.netbeans.spi.editor.hints.Context;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.PositionRefresher;
import org.netbeans.spi.editor.mimelookup.MimeDataProvider;
import org.netbeans.spi.lexer.LanguageEmbedding;
import org.netbeans.spi.lexer.LanguageProvider;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 * Test class for JavaHints implementation of Editor Hints {@link PositionRefresher}
 * @author Max Sauer
 */
public class JavaHintsPositionRefresherTest extends NbTestCase {

    private FileObject sourceRoot;
    private CompilationInfo info;
    private Document doc;

    public JavaHintsPositionRefresherTest(String name) {
        super(name);
    }


    @Override
    public void setUp() throws Exception {
        super.setUp();
        SourceUtilsTestUtil.prepareTest(new String[]{"org/netbeans/modules/java/editor/resources/layer.xml"},
                new Object[]{JavaDataLoader.class,
                new MimeDataProvider() {
                public Lookup getLookup(MimePath mimePath) {
                    return Lookups.fixed(new Object[] {
                        new JavaKit(),
                    });
                }
            },
            new LanguageProvider() {
                public Language<?> findLanguage(String mimePath) {
                    return JavaTokenId.language();
                }

                public LanguageEmbedding<?> findLanguageEmbedding(Token<?> token,
                        LanguagePath languagePath,
                        InputAttributes inputAttributes) {
                    return null;
                }
            }
        });
    }

    private void prepareTest(String fileName, String code) throws Exception {
        clearWorkDir();

        FileObject workFO = FileUtil.toFileObject(getWorkDir());

        assertNotNull(workFO);

        sourceRoot = workFO.createFolder("src");

        FileObject buildRoot  = workFO.createFolder("build");
        FileObject cache = workFO.createFolder("cache");

        FileObject data = FileUtil.createData(sourceRoot, fileName);
        File dataFile = FileUtil.toFile(data);

        assertNotNull(dataFile);

        TestUtilities.copyStringToFile(dataFile, code);

//        SourceUtilsTestUtil.prepareTest(new String[0], new Object[]{
//                    new JavaCustomIndexer.Factory()});
        SourceUtilsTestUtil.prepareTest(sourceRoot, buildRoot, cache);

        DataObject od = DataObject.find(data);
        EditorCookie ec = od.getCookie(EditorCookie.class);

        assertNotNull(ec);

        doc = ec.openDocument();
        doc.putProperty(Language.class, JavaTokenId.language());
        doc.putProperty("mimeType", "text/x-java");

        //XXX: takes a long time
        //re-index, in order to find classes-living-elsewhere
        IndexingManager.getDefault().refreshIndexAndWait(sourceRoot.getURL(), null);

        JavaSource js = JavaSource.forFileObject(data);

        assertNotNull(js);

        info = SourceUtilsTestUtil.getCompilationInfo(js, Phase.RESOLVED);

        assertNotNull(info);
    }

    private Context prepareContext(int position) {
        Context ctx = null;
        try {
            Constructor constructor = Context.class.getDeclaredConstructor(int.class, AtomicBoolean.class);
            constructor.setAccessible(true);
            ctx = (Context) constructor.newInstance(new Integer(position), new AtomicBoolean());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return ctx;
    }

    public void testErrorHint0() throws Exception {
        performTest("test/Test.java", "package test; public class Test {public void foo() {\n| new Foo();}}", "1:0-1:14:error:illegal start of expression1:6-1:9:error:cannot find symbol\n  symbol  : class Foo\n  location: class test.Test");
    }

    private void performTest(String fileName , String code, String expected) throws Exception {
        prepareTest(fileName, code);
        int[] caretPosition = new int[1];
        code = org.netbeans.modules.java.hints.TestUtilities.detectOffsets(code, caretPosition);
        Context ctx = prepareContext(caretPosition[0]);
        Map<String, List<ErrorDescription>> errorDescriptionsAt = new JavaHintsPositionRefresher().getErrorDescriptionsAt(ctx, doc);
        StringBuffer buf = new StringBuffer();
        for (Entry<String, List<ErrorDescription>> e : errorDescriptionsAt.entrySet()) {
            for (ErrorDescription ed : e.getValue()) {
                buf.append(ed.toString());
            }

        }
        assertEquals("Provided error messages differ. ", expected, buf.toString());
    }
   

}