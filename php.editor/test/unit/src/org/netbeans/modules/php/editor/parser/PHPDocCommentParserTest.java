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
package org.netbeans.modules.php.editor.parser;

import java.io.File;
import java.util.List;
import org.netbeans.modules.gsf.GsfTestBase;
import org.netbeans.modules.php.editor.parser.astnodes.PHPDocBlock;
import org.netbeans.modules.php.editor.parser.astnodes.PHPDocTag;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author PetrPisl
 */
public class PHPDocCommentParserTest extends GsfTestBase {

    public PHPDocCommentParserTest(String testName) {
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

    public void testEmpty() throws Exception {
        String comment = " ";

        PHPDocCommentParser parser = new PHPDocCommentParser();
        PHPDocBlock block = parser.parse(100, 150, comment);
        assertNotNull(block);
        assertEquals("", block.getDescription());
    }
    
    public void testEmpty2() throws Exception {
        String comment = " *     ";

        PHPDocCommentParser parser = new PHPDocCommentParser();
        PHPDocBlock block = parser.parse(100, 150, comment);
        assertNotNull(block);
        assertEquals("", block.getDescription());
    }
    
    public void testDescriptionSimple() throws Exception {
        String comment = " simple";

        PHPDocCommentParser parser = new PHPDocCommentParser();
        PHPDocBlock block = parser.parse(100, 150, comment);
        assertNotNull(block);
        assertEquals("simple", block.getDescription());
    }
    
    public void testDescriptionOnly() throws Exception {
        String comment = " * hello this is a * very simple comment \n * and seccond line";

        PHPDocCommentParser parser = new PHPDocCommentParser();
        PHPDocBlock block = parser.parse(100, 150, comment);
        assertNotNull(block);
        assertEquals("hello this is a * very simple comment\nand seccond line", block.getDescription());
    }
    
    public void testNoDescriptionOneTag() throws Exception {
        String comment = " * @author Petr";

        PHPDocCommentParser parser = new PHPDocCommentParser();
        PHPDocBlock block = parser.parse(0, comment.length(), comment);
        List<PHPDocTag> tags = block.getTags();
        assertNotNull(block);
        assertEquals("", block.getDescription());
        assertEquals("Nunber of tags", 1, tags.size());
        assertEquals(PHPDocTag.Type.AUTHOR, tags.get(0).getKind());
        assertEquals("Petr", tags.get(0).getValue());
        assertEquals(comment.indexOf("@author"), tags.get(0).getStartOffset());
        assertEquals(comment.indexOf("@author Petr") + "@author Petr".length(), tags.get(0).getEndOffset() - 3);
    }
    
    public void testNoDescriptionTwoTags() throws Exception {
        String comment = " * @author Petr  \n * @since 1.5";

        PHPDocCommentParser parser = new PHPDocCommentParser();
        PHPDocBlock block = parser.parse(0, comment.length(), comment);
        List<PHPDocTag> tags = block.getTags();
        assertNotNull(block);
        assertEquals("", block.getDescription());
        assertEquals("Nunber of tags", 2, tags.size());
        assertEquals(PHPDocTag.Type.AUTHOR, tags.get(0).getKind());
        assertEquals("Petr", tags.get(0).getValue());
        assertEquals(comment.indexOf("@author"), tags.get(0).getStartOffset());
        assertEquals(comment.indexOf("@author Petr  ") + "@author Petr  ".length(), tags.get(0).getEndOffset() - 3);
        assertEquals(PHPDocTag.Type.SINCE, tags.get(1).getKind());
        assertEquals("1.5", tags.get(1).getValue());
        assertEquals(comment.indexOf("@since 1.5") + 3 , tags.get(1).getStartOffset());
        assertEquals(comment.indexOf("@since 1.5") + "@since 1.5".length(), tags.get(1).getEndOffset() - 3);
    }
    
    public void testNoDescriptionThreeTags() throws Exception {
        String comment = " * @author Petr  \n *    @since 1.5  \n *      @License mine";

        PHPDocCommentParser parser = new PHPDocCommentParser();
        PHPDocBlock block = parser.parse(0, comment.length(), comment);
        List<PHPDocTag> tags = block.getTags();
        assertNotNull(block);
        assertEquals("", block.getDescription());
        assertEquals("Nunber of tags", 3, tags.size());
        assertEquals(PHPDocTag.Type.AUTHOR, tags.get(0).getKind());
        assertEquals("Petr", tags.get(0).getValue());
        assertEquals(comment.indexOf("@author"), tags.get(0).getStartOffset());
        assertEquals(comment.indexOf("@author Petr  ") + "@author Petr  ".length(), tags.get(0).getEndOffset() - 3);
        assertEquals(PHPDocTag.Type.SINCE, tags.get(1).getKind());
        assertEquals("1.5", tags.get(1).getValue());
        assertEquals(comment.indexOf("@since 1.5") + 3 , tags.get(1).getStartOffset());
        assertEquals(comment.indexOf("@since 1.5") + "@since 1.5  ".length(), tags.get(1).getEndOffset() - 3);
        assertEquals(PHPDocTag.Type.LICENSE, tags.get(2).getKind());
        assertEquals("mine", tags.get(2).getValue());
        assertEquals(comment.indexOf("@License mine") + 3 , tags.get(2).getStartOffset());
        assertEquals(comment.indexOf("@License mine") + "@License mine".length(), tags.get(2).getEndOffset() - 3);
    }
    
    public void testDescriptionTags() throws Exception {
        String comment = " * hello this is a * very simple comment \n * and seccond line \n  * \n * last line of description\n * @link   http://www.seznam.cz   \n * @author";

        PHPDocCommentParser parser = new PHPDocCommentParser();

        PHPDocBlock block = parser.parse(0, comment.length(), comment);
        assertNotNull(block);
        assertEquals("hello this is a * very simple comment\nand seccond line\n\nlast line of description", block.getDescription());
        List<PHPDocTag> tags = block.getTags();
        assertEquals("Nunber of tags", 2, tags.size());
        assertEquals(PHPDocTag.Type.LINK, tags.get(0).getKind());
        assertEquals("http://www.seznam.cz", tags.get(0).getValue());
        assertEquals(comment.indexOf("@link") + 3, tags.get(0).getStartOffset());
        assertEquals(comment.indexOf("@link   http://www.seznam.cz   ") + "@link   http://www.seznam.cz   ".length(), tags.get(0).getEndOffset() - 3);
        assertEquals(PHPDocTag.Type.AUTHOR, tags.get(1).getKind());
        assertEquals("", tags.get(1).getValue());
        assertEquals(comment.indexOf("@author") + 3 , tags.get(1).getStartOffset());
        assertEquals(comment.indexOf("@author") + "@author".length(), tags.get(1).getEndOffset() - 3);
    }
    
    public void testDescriptionWithHtml() throws Exception {
        String comment = "*   <dd> \"*word\"  => ENDS_WITH(word)\n *   <dd> \"/^word.* /\" => REGEX(^word.*)\n *   <dd> \"word*word\" => REGEX(word.*word)";
        PHPDocCommentParser parser = new PHPDocCommentParser();

        PHPDocBlock block = parser.parse(100, 150, comment);
        assertNotNull(block);
    }

    public void testProperty() throws Exception {
        String comment = " * PHP Template.\n" +
                " * @property string name\n" +
                " * @property-read int ahoj\n" +
                " * @property-write int death";
        PHPDocCommentParser parser = new PHPDocCommentParser();
        PHPDocBlock block = parser.parse(0, comment.length(), comment);
        assertNotNull(block);
        assertEquals("PHP Template.", block.getDescription());
        List<PHPDocTag> tags = block.getTags();
        assertEquals("Nunber of tags", 3, tags.size());
        assertEquals(PHPDocTag.Type.PROPERTY, tags.get(0).getKind());
        assertEquals("string name", tags.get(0).getValue());
        assertEquals(comment.indexOf("@property string name") + 3, tags.get(0).getStartOffset());
        assertEquals(comment.indexOf("@property string name") + "@property string name".length(), tags.get(0).getEndOffset() - 3);
        assertEquals(PHPDocTag.Type.PROPERTY_READ, tags.get(1).getKind());
        assertEquals("int ahoj", tags.get(1).getValue());
        assertEquals(comment.indexOf("@property-read int ahoj") + 3, tags.get(1).getStartOffset());
        assertEquals(comment.indexOf("@property-read int ahoj") + "@property-read int ahoj".length(), tags.get(1).getEndOffset() - 3);
        assertEquals(PHPDocTag.Type.PROPERTY_WRITE, tags.get(2).getKind());
        assertEquals("int death", tags.get(2).getValue());
        assertEquals(comment.indexOf("@property-write int death") + 3, tags.get(2).getStartOffset());
        assertEquals(comment.indexOf("@property-write int death") + "@property-write int death".length(), tags.get(2).getEndOffset() - 3);
    }

    public void testReturnType01() throws Exception {
        String comment = " * Function XYZ.\n" +
                " * @return string\n";
        perform(comment, "ReturnType01");
    }

    public void testReturnType02() throws Exception {
        String comment = " * Function XYZ.\n" +
                " * @return string|int test documentation\n";
        perform(comment, "ReturnType02");
    }

    public void testReturnType03() throws Exception {
        String comment = " * Function XYZ.\n" +
                " * @return TClass::CONSTANT test documentation\n";
        perform(comment, "ReturnType03");
    }

    public void testParamReturn01() throws Exception {
        String comment =
                "   * Retrieves the entry at a specific index.\n" +
                "   *\n" +
                "   * @param int $index An entry index\n" +
                "   *\n" +
                "   * @return sfActionStackEntry An action stack entry implementation.\n";
        perform(comment, "ParamReturn01");
    }

    public void testExample01() throws Exception {
        String comment =
         "* Dispatches to the action defined by the 'action' parameter of the sfRequest object.\n" +
         "   *\n" +
         "   * This method try to execute the executeXXX() method of the current object where XXX is the\n" +
         "   * defined action name.\n" +
         "   *\n" +
         "   * @param  sfRequest $request The current sfRequest object\n" +
         "   *\n" +
         "   * @return string    A string containing the view name associated with this action\n" +
         "   *\n" +
         "   * @throws sfInitializationException\n" +
         "   *\n" +
         "   * @see sfAction";
        perform(comment, "Example01");
    }

    public void testHTMLWrapper() throws Exception {
        String comment =
                "*\n" +
                "* @throws <b>sfInitializationException</b> If an error occurs while initializing this sfCache instance.";
        perform(comment, "HTMLWrapper");
    }
    
    public void perform(String comment, String filename) throws Exception {
        PHPDocCommentParser parser = new PHPDocCommentParser();
        PHPDocBlock block = parser.parse(0, comment.length(), comment);
        PrintASTVisitor visitor = new PrintASTVisitor();
        String result = visitor.printTree(block);

        // try to find golden file
        String fullClassName = this.getClass().getName();
        String goldenFileDir = fullClassName.replace('.', '/');
        String goldenFolder = getDataSourceDir().getAbsolutePath() + "/goldenfiles/" + goldenFileDir + "/";
        File goldenFile = new File(goldenFolder + filename + ".pass");
        if (!goldenFile.exists()) {
            // if doesn't exist, create it
            FileObject goldenFO = touch(goldenFolder, filename + ".pass");
            copyStringToFileObject(goldenFO, result);
        }
        else {
            // if exist, compare it.
            goldenFile = getGoldenFile(filename + ".pass");
            FileObject resultFO = touch(getWorkDir(), filename + ".result");
            copyStringToFileObject(resultFO, result);
            assertFile(FileUtil.toFile(resultFO), goldenFile, getWorkDir());
        }
    }
}
