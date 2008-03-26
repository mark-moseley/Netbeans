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

import com.sun.source.util.TreePath;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.lexer.JavadocTokenId;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;

/**
 *
 * @author Jan Pokorsky
 */
public class JavadocImportsTest extends JavadocTestSupport {

    public JavadocImportsTest(String name) {
        super(name);
    }

    public void testComputeUnresolvedImports() throws Exception {
        String code = 
                "package p;\n" +
                "import java.util.Collections;\n" +
                "class C {\n" +
                "   /**\n" +
                "    * link1 {@link Runnable}\n" +
                "    * link2 {@linkplain MethodUnresolved}\n" +
                "    * link3 {@link Collections#binarySearch(java.util.List, java.lang.Object) search}\n" +
                "    * {@link java. uncomplete reference}" +
                "    * unclosed link {@value Math#PI\n" +
                "    * @see SeeUnresolved\n" +
                "    * @throws ThrowsUnresolved\n" +
                "    */\n" +
                "   void m() throws java.io.IOException {\n" +
                "   }\n" +
                "   /**\n" +
                "    * {@link FieldUnresolved}\n" +
                "    */\n" +
                "   int field;\n" +
                "   /** {@link my.pkg.InnerInterfaceUnresolved */\n" +
                "   interface InnerInterface {}\n" +
                "   /** {@link InnerAnnotationTypeUnresolved} */\n" +
                "   @interface InnerAnnotationType {}\n" +
                "}\n" +
                "/** {@link EnumReferenceUnresolved} */\n" +
                "enum TopLevelEnum {\n" +
                "   /** {@link ConstantReferenceUnresolved} */" +
                "   E1\n" +
                "}\n";
        prepareTest(code);

        List<String> exp = Arrays.asList("MethodUnresolved", "SeeUnresolved",
                "ThrowsUnresolved", "FieldUnresolved", "my.pkg.InnerInterfaceUnresolved",
                "InnerAnnotationTypeUnresolved", "EnumReferenceUnresolved",
                "ConstantReferenceUnresolved", "java."
                );
        Collections.sort(exp);
        
        Set<String> result = JavadocImports.computeUnresolvedImports(info);
        assertNotNull(result);
        
        List<String> sortedResult = new ArrayList<String>(result);
        Collections.sort(sortedResult);
        assertEquals(exp, sortedResult);
    }
    
    public void testComputeReferencedElements() throws Exception {
        String code = 
                "package p;\n" +
                "import java.util.Collections;\n" +
                "import java.util.List;\n" +
                "class C {\n" +
                "   /**\n" +
                "    * link1 {@link Runnable}\n" +
                "    * link3 {@linkplain Collections#binarySearch(java.util.List, java.lang.Object) search}\n" +
                "    * {@link java. uncomplete reference}" +
                "    * unclosed link {@value Math#PI\n" +
                "    * @see List\n" +
                "    * @throws java.io.IOException\n" +
                "    */\n" +
                "   void m() throws java.io.IOException {\n" +
                "   }\n" +
                "   /**\n" +
                "    * {@link Collections}\n" +
                "    */\n" +
                "   int field;\n" +
                "   /** {@link java.io.IOException */\n" +
                "   interface InnerInterface {}\n" +
                "   /** {@link Collections} */\n" +
                "   @interface InnerAnnotationType {}\n" +
                "}\n" +
                "/** {@link Collections} */\n" +
                "enum TopLevelEnum {\n" +
                "   /** {@link java.util.Collections} */" +
                "   E1\n" +
                "}\n";
        prepareTest(code);

        // C.m()
        Element member = findElement(code, "m() throws");
        assertNotNull(member);
        List <TypeElement> exp = Arrays.asList(
                info.getElements().getTypeElement("java.lang.Runnable"),
                info.getElements().getTypeElement("java.lang.Math"),
                info.getElements().getTypeElement("java.util.Collections"),
                info.getElements().getTypeElement("java.util.List"),
                info.getElements().getTypeElement("java.io.IOException")
                );
        Collections.<TypeElement>sort(exp, new ElementComparator());
        Set<TypeElement> result = JavadocImports.computeReferencedElements(info, member);
        assertNotNull(result);
        List<TypeElement> sortedResult = new ArrayList<TypeElement>(result);
        Collections.sort(sortedResult, new ElementComparator());
        assertEquals(exp, sortedResult);

        // C.field
        member = findElement(code, "field;");
        assertNotNull(member);
        exp = Arrays.asList(
                info.getElements().getTypeElement("java.util.Collections")
                );
        Collections.<TypeElement>sort(exp, new ElementComparator());
        result = JavadocImports.computeReferencedElements(info, member);
        assertNotNull(result);
        sortedResult = new ArrayList<TypeElement>(result);
        Collections.sort(sortedResult, new ElementComparator());
        assertEquals(exp, sortedResult);

        // C.InnerInterface
        member = findElement(code, "InnerInterface {");
        assertNotNull(member);
        exp = Arrays.asList(
                info.getElements().getTypeElement("java.io.IOException")
                );
        Collections.<TypeElement>sort(exp, new ElementComparator());
        result = JavadocImports.computeReferencedElements(info, member);
        assertNotNull(result);
        sortedResult = new ArrayList<TypeElement>(result);
        Collections.sort(sortedResult, new ElementComparator());
        assertEquals(exp, sortedResult);

        // C.InnerAnnotationType
        member = findElement(code, "InnerAnnotationType {");
        assertNotNull(member);
        exp = Arrays.asList(
                info.getElements().getTypeElement("java.util.Collections")
                );
        Collections.<TypeElement>sort(exp, new ElementComparator());
        result = JavadocImports.computeReferencedElements(info, member);
        assertNotNull(result);
        sortedResult = new ArrayList<TypeElement>(result);
        Collections.sort(sortedResult, new ElementComparator());
        assertEquals(exp, sortedResult);

        // TopLevelEnum
        member = findElement(code, "TopLevelEnum {");
        assertNotNull(member);
        exp = Arrays.asList(
                info.getElements().getTypeElement("java.util.Collections")
                );
        Collections.<TypeElement>sort(exp, new ElementComparator());
        result = JavadocImports.computeReferencedElements(info, member);
        assertNotNull(result);
        sortedResult = new ArrayList<TypeElement>(result);
        Collections.sort(sortedResult, new ElementComparator());
        assertEquals(exp, sortedResult);

        // TopLevelEnum.E1
        member = findElement(code, "E1\n");
        assertNotNull(member);
        exp = Arrays.asList(
                info.getElements().getTypeElement("java.util.Collections")
                );
        Collections.<TypeElement>sort(exp, new ElementComparator());
        result = JavadocImports.computeReferencedElements(info, member);
        assertNotNull(result);
        sortedResult = new ArrayList<TypeElement>(result);
        Collections.sort(sortedResult, new ElementComparator());
        assertEquals(exp, sortedResult);
    }
    
    public void testComputeTokensOfReferencedElements() throws Exception {
        String code = 
                "package p;\n" +
                "import java.util.Collections;\n" +
                "class C {\n" +
                "   /**\n" +
                "    * link1 {@link Runnable}\n" +
                "    * link2 {@link Collections#binarySearch(java.util.List, java.lang.Object) search}\n" +
                "    * {@link java. uncomplete reference}" +
                "    * unclosed link {@value Math#PI\n" +
                "    * @see java.util.Collections\n" +
                "    * @throws ThrowsUnresolved\n" +
                "    */\n" +
                "   void m() throws java.io.IOException {\n" +
                "       Collections.<String>binarySearch(Collections.<String>emptyList(), \"\");\n" +
                "       double pi = Math.PI;\n" +
                "   }\n" +
                "}\n";
        prepareTest(code);

        Element where = findElement(code, "m() throws");
        assertNotNull(where);
        TokenSequence<JavadocTokenId> jdts = JavadocCompletionUtils.findJavadocTokenSequence(info, where);
        assertNotNull(jdts);
        List<Token> exp;
        
        // toFind java.lang.Runnable
        Element toFind = info.getElements().getTypeElement("java.lang.Runnable");
        assertNotNull(toFind);
        List<Token> tokens = JavadocImports.computeTokensOfReferencedElements(info, where, toFind);
        assertNotNull(toFind.toString(), tokens);
        jdts.move(code.indexOf("Runnable", code.indexOf("link1")));
        assertTrue(jdts.moveNext());
        exp = Arrays.<Token>asList(jdts.token());
        assertEquals(toFind.toString(), exp, tokens);
        
        // toFind java.util.Collections
        toFind = info.getElements().getTypeElement("java.util.Collections");
        assertNotNull(toFind);
        tokens = JavadocImports.computeTokensOfReferencedElements(info, where, toFind);
        assertNotNull(toFind.toString(), tokens);
        exp = new ArrayList<Token>();
        jdts.move(code.indexOf("Collections", code.indexOf("link2")));
        assertTrue(jdts.moveNext());
        exp.add(jdts.token());
        jdts.move(code.indexOf("Collections", code.indexOf("* @see")));
        assertTrue(jdts.moveNext());
        exp.add(jdts.token());
        assertEquals(toFind.toString(), exp, tokens);
        
        // toFind Math#PI
        toFind = findElement(code, "PI;\n");
        assertNotNull(toFind);
        tokens = JavadocImports.computeTokensOfReferencedElements(info, where, toFind);
        assertNotNull(toFind.toString(), tokens);
        jdts.move(code.indexOf("PI", code.indexOf("unclosed link")));
        assertTrue(jdts.moveNext());
        exp = Arrays.<Token>asList(jdts.token());
        assertEquals(toFind.toString(), exp, tokens);
        
        // toFind Collections#binarySearch
        toFind = findElement(code, "binarySearch(Collections.<String>emptyList()");
        assertNotNull(toFind);
        tokens = JavadocImports.computeTokensOfReferencedElements(info, where, toFind);
        assertNotNull(toFind.toString(), tokens);
        jdts.move(code.indexOf("binarySearch", code.indexOf("link2")));
        assertTrue(jdts.moveNext());
        exp = Arrays.<Token>asList(jdts.token());
        assertEquals(toFind.toString(), exp, tokens);
    }
    
    public void testFindReferencedElement() throws Exception {
        String code = 
                "package p;\n" +
                "import java.util.Collections;\n" +
                "class C {\n" +
                "   /**\n" +
                "    * link1 {@link Runnable}\n" +
                "    * link2 {@link Collections#binarySearch(java.util.List, java.lang.Object) search}\n" +
                "    * {@link java. uncomplete reference}" +
                "    * local_link {@link #m()}\n" +
                "    * unclosed link {@value Math#PI\n" +
                "    * @see java.util.Collections\n" +
                "    * @throws ThrowsUnresolved\n" +
                "    */\n" +
                "   void m() throws java.io.IOException {\n" +
                "       Collections.<String>binarySearch(Collections.<String>emptyList(), \"\");\n" +
                "       double pi = Math.PI;\n" +
                "   }\n" +
                "}\n";
        prepareTest(code);

        Element where = findElement(code, "m() throws");
        assertNotNull(where);
        TokenSequence<JavadocTokenId> jdts = JavadocCompletionUtils.findJavadocTokenSequence(info, where);
        assertNotNull(jdts);

        // java.lang.Runnable
        Element exp = info.getElements().getTypeElement("java.lang.Runnable");
        assertNotNull(exp);
        Element el = JavadocImports.findReferencedElement(info, code.indexOf("Runnable", code.indexOf("link1")));
        assertEquals(exp, el);

        // java.util.Collections in {@link Collections
        exp = info.getElements().getTypeElement("java.util.Collections");
        assertNotNull(exp);
        el = JavadocImports.findReferencedElement(info, code.indexOf("Collections", code.indexOf("link2")));
        assertEquals(exp, el);

        // java.util.Collections in @see Collections
        exp = info.getElements().getTypeElement("java.util.Collections");
        assertNotNull(exp);
        el = JavadocImports.findReferencedElement(info, code.indexOf("Collections", code.indexOf("@see")));
        assertEquals(exp, el);

        // java.util in @see Collections
        exp = info.getElements().getTypeElement("java.util.Collections");
        exp = exp.getEnclosingElement(); // package
        assertNotNull(exp);
        el = JavadocImports.findReferencedElement(info, code.indexOf("util", code.indexOf("@see")));
        assertEquals(exp, el);

        // java.util.Collections#binarySearch
        exp = findElement(code, "binarySearch(Collections.<String>emptyList()");
        assertNotNull(exp);
        el = JavadocImports.findReferencedElement(info, code.indexOf("binarySearch", code.indexOf("link2")));
        assertEquals(exp, el);

        // java.util.Collections#PI
        exp = findElement(code, "PI;\n");
        assertNotNull(exp);
        el = JavadocImports.findReferencedElement(info, code.indexOf("PI", code.indexOf("unclosed link")));
        assertEquals(exp, el);

        // #m()
        exp = where;
        el = JavadocImports.findReferencedElement(info, code.indexOf("m()", code.indexOf("local_link")));
        assertEquals(exp, el);

        // not java reference
        el = JavadocImports.findReferencedElement(info, code.indexOf("unclosed link"));
        assertNull(el);
    }
    
    public void testIsInsideReference() throws Exception {
        String code = 
                "package p;\n" +
                "import java.util.Collections;\n" +
                "class C {\n" +
                "   /**\n" +
                "    * link1 {@link Runnable}\n" +
                "    * link2 {@link Collections#binarySearch(java.util.List, java.lang.Object) search}\n" +
                "    * {@link java. uncomplete reference}" +
                "    * unclosed link {@value Math#PI\n" +
                "    * @see java.util.Collections\n" +
                "    * @throws ThrowsUnresolved\n" +
                "    */\n" +
                "   void m() throws java.io.IOException {\n" +
                "       Collections.<String>binarySearch(Collections.<String>emptyList(), \"\");\n" +
                "       double pi = Math.PI;\n" +
                "   }\n" +
                "}\n";
        prepareTest(code);

        Element where = findElement(code, "m() throws");
        assertNotNull(where);
        TokenSequence<JavadocTokenId> jdts = JavadocCompletionUtils.findJavadocTokenSequence(info, where);
        assertNotNull(jdts);

        assertFalse(JavadocImports.isInsideReference(jdts, code.indexOf("link1")));
        assertFalse(JavadocImports.isInsideReference(jdts, code.indexOf("@link", code.indexOf("link1")) + 2));
        assertTrue(JavadocImports.isInsideReference(jdts, code.indexOf("Runnable", code.indexOf("link1"))));
        assertTrue(JavadocImports.isInsideReference(jdts, code.indexOf("Collections", code.indexOf("link2"))));
        assertTrue(JavadocImports.isInsideReference(jdts, code.indexOf("binarySearch", code.indexOf("link2"))));
        assertFalse(JavadocImports.isInsideReference(jdts, code.indexOf("#", code.indexOf("link2"))));
        assertFalse(JavadocImports.isInsideReference(jdts, code.indexOf("search}\n", code.indexOf("link2"))));
        assertTrue(JavadocImports.isInsideReference(jdts, code.indexOf("util", code.indexOf("@see"))));
    }
    
    private Element findElement(String code, String pattern) {
        int offset = code.indexOf(pattern) + 1;
        TreePath tpath = info.getTreeUtilities().pathFor(offset);
        return info.getTrees().getElement(tpath);
    }
    
    private static class ElementComparator implements Comparator<TypeElement> {

        public int compare(TypeElement o1, TypeElement o2) {
            // type elements are never null
            if (o1 == o2) {
                return 0;
            }
            
            return o1.getQualifiedName().toString().compareTo(o2.getQualifiedName().toString());
        }
        
    }
}
