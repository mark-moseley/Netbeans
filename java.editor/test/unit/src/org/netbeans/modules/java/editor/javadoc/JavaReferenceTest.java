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

package org.netbeans.modules.java.editor.javadoc;

import com.sun.javadoc.Doc;
import com.sun.javadoc.Tag;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.lexer.JavadocTokenId;
import org.netbeans.api.lexer.TokenSequence;

/**
 *
 * @author Jan Pokorsky
 */
public class JavaReferenceTest extends JavadocTestSupport {

    public JavaReferenceTest(String name) {
        super(name);
    }

    public void testResolve() throws Exception {
        String code = 
                "package p;\n" +
                "import java.util.Collections;\n" +
                "class C {\n" +
                "   /**\n" +
                "    * link1 {@link Runnable}\n" +
                "    * link2 {@linkplain IOException}\n" +
                "    * link3 {@link Collections#binarySearch(java.util.List, java.lang.Object) search}\n" +
                "    * unclosed link {@value Math#PI\n" +
                "    * @see java.io.IOException\n" +
                "    * @throws java.io.IOException\n" +
                "    */\n" +
                "   void m() throws java.io.IOException {\n" +
                "       Collections.<String>binarySearch(Collections.<String>emptyList(), \"\");\n" +
                "       double pi = Math.PI;\n" +
                "   }\n" +
                "}\n";
        prepareTest(code);
        
        String what = "link1 {@link ";
        int offset = code.indexOf(what) + what.length();
        TokenSequence<JavadocTokenId> jdts = JavadocCompletionUtils.findJavadocTokenSequence(doc, offset);
        String dump = insertPointer(code, offset);
        assertNotNull(dump, jdts);
        Doc javadoc = JavadocCompletionUtils.findJavadoc(info, doc, offset);
        assertNotNull(dump, javadoc);
        TypeElement scope = (TypeElement) info.getElementUtilities().elementFor(javadoc).getEnclosingElement();
        DocPositions positions = DocPositions.get(info, javadoc, jdts);
        assertNotNull(dump, positions);
        
        // link1
        Tag tag = positions.getTag(offset);
        assertNotNull(dump + '\n' + positions, tag);
        int[] tagSpan = positions.getTagSpan(tag);
        assertNotNull(dump, tagSpan);

        JavaReference ref = JavaReference.resolve(jdts, offset, tagSpan[1]);
        assertNotNull(dump, ref);
        Element exp = info.getElements().getTypeElement("java.lang.Runnable");
        Element result = ref.getReferencedElement(info, scope);
        assertEquals(ref + "\n" + dump, exp, result);
        
        // link2
        what = "link2 {@linkplain ";
        offset = code.indexOf(what) + what.length();
        tag = positions.getTag(offset);
        dump = insertPointer(code, offset);
        assertNotNull(dump + '\n' + positions, tag);
        tagSpan = positions.getTagSpan(tag);
        assertNotNull(dump, tagSpan);

        ref = JavaReference.resolve(jdts, offset, tagSpan[1]);
        assertNotNull(dump, ref);
        exp = null;
        result = ref.getReferencedElement(info, scope);
        assertEquals(ref + "\n" + dump, exp, result);
        
        // link3
        what = "link3 {@link ";
        offset = code.indexOf(what) + what.length();
        tag = positions.getTag(offset);
        dump = insertPointer(code, offset);
        assertNotNull(dump + '\n' + positions, tag);
        tagSpan = positions.getTagSpan(tag);
        assertNotNull(dump, tagSpan);

        ref = JavaReference.resolve(jdts, offset, tagSpan[1]);
        assertNotNull(dump, ref);
        exp = findCollectionsBinaryMethod(code, "Collections.<String>binary");
        result = ref.getReferencedElement(info, scope);
        assertEquals(ref + "\n" + dump, exp, result);
        
        // unclosed link
        what = "unclosed link {@value ";
        offset = code.indexOf(what) + what.length();
        tag = positions.getTag(offset);
        dump = insertPointer(code, offset);
        assertNotNull(dump + '\n' + positions, tag);
        tagSpan = positions.getTagSpan(tag);
        assertNotNull(dump, tagSpan);

        ref = JavaReference.resolve(jdts, offset, tagSpan[1]);
        assertNotNull(dump, ref);
        exp = findCollectionsBinaryMethod(code, "double pi = Math.P");
        result = ref.getReferencedElement(info, scope);
        assertEquals(ref + "\n" + dump, exp, result);
        
        // see
        what = "@see ";
        offset = code.indexOf(what) + what.length();
        tag = positions.getTag(offset);
        dump = insertPointer(code, offset);
        assertNotNull(dump + '\n' + positions, tag);
        tagSpan = positions.getTagSpan(tag);
        assertNotNull(dump, tagSpan);

        ref = JavaReference.resolve(jdts, offset, tagSpan[1]);
        assertNotNull(dump, ref);
        exp = info.getElements().getTypeElement("java.io.IOException");
        result = ref.getReferencedElement(info, scope);
        assertEquals(ref + "\n" + dump, exp, result);
        
        // throws
        what = "@throws ";
        offset = code.indexOf(what) + what.length();
        tag = positions.getTag(offset);
        dump = insertPointer(code, offset);
        assertNotNull(dump + '\n' + positions, tag);
        tagSpan = positions.getTagSpan(tag);
        assertNotNull(dump, tagSpan);

        ref = JavaReference.resolve(jdts, offset, tagSpan[1]);
        assertNotNull(dump, ref);
        exp = info.getElements().getTypeElement("java.io.IOException");
        result = ref.getReferencedElement(info, scope);
        assertEquals(ref + "\n" + dump, exp, result);
    }
    
    private Element findCollectionsBinaryMethod(String code, String where) {
        int pos = code.indexOf(where) + where.length();
        return info.getTrees().getElement(info.getTreeUtilities().pathFor(pos));
    }
    
}
