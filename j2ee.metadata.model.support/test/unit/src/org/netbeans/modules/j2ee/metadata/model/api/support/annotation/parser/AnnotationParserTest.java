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

package org.netbeans.modules.j2ee.metadata.model.api.support.annotation.parser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationModelHelper;
import org.netbeans.modules.j2ee.metadata.model.support.TestUtilities;
import org.netbeans.modules.j2ee.metadata.model.support.JavaSourceTestCase;
import org.netbeans.modules.java.source.usages.RepositoryUpdater;

/**
 *
 * @author Andrei Badea
 */
public class AnnotationParserTest extends JavaSourceTestCase {

    public AnnotationParserTest(String testName) {
        super(testName);
    }

    public void testPrimitive() throws Exception {
        TestUtilities.copyStringToFileObject(srcFO, "Annotated.java",
                "@interface Annotation {" +
                "   int intValue();" +
                "   int intValue2();" +
                "   int intValue3();" +
                "   double doubleValue();" +
                "   String stringValue();" +
                "   String stringValue2();" +
                "   String stringValue3();" +
                "}" +
                "@Annotation(intValue = 2, doubleValue = \"error\", stringValue = \"foo\", " +
                "       stringValue2 = @SuppressWarnings(\"error\")" +
                "public class Annotated {" +
                "}");
        RepositoryUpdater.getDefault().scheduleCompilationAndWait(srcFO, srcFO).await();
        ClasspathInfo cpi = ClasspathInfo.create(srcFO);
        final AnnotationModelHelper helper = AnnotationModelHelper.create(cpi);
        helper.runJavaSourceTask(new Runnable() {
            public void run() {
                TypeElement annotated = helper.getCompilationController().getElements().getTypeElement("Annotated");
                AnnotationMirror annotation = annotated.getAnnotationMirrors().iterator().next();
                AnnotationParser parser = AnnotationParser.create(helper);
                parser.expectPrimitive("intValue", Integer.class, parser.defaultValue(0));
                parser.expectPrimitive("intValue2", Integer.class, parser.defaultValue(Integer.MAX_VALUE));
                parser.expectPrimitive("intValue3", Integer.class, parser.defaultValue(Integer.MIN_VALUE));
                parser.expectPrimitive("doubleValue", Double.class, parser.defaultValue(0.0));
                parser.expectString("stringValue", parser.defaultValue("stringValue"));
                parser.expectString("stringValue2", parser.defaultValue("stringValue2"));
                parser.expectString("stringValue3", parser.defaultValue("stringValue3"));
                ParseResult parseResult = parser.parse(annotation);
                assertEquals(2, (int)parseResult.get("intValue", Integer.class));
                assertEquals(Integer.MAX_VALUE, (int)parseResult.get("intValue2", Integer.class));
                assertEquals(Integer.MIN_VALUE, (int)parseResult.get("intValue3", Integer.class));
                assertEquals(0.0, (double)parseResult.get("doubleValue", Double.class));
                assertEquals("foo", parseResult.get("stringValue", String.class));
                // XXX does not work
                // assertEquals("stringValue2", parseResult.get("stringValue2", String.class));
                assertEquals("stringValue3", parseResult.get("stringValue3", String.class));
            }
        });
    }

    public void testClass() throws Exception {
        TestUtilities.copyStringToFileObject(srcFO, "Annotated.java",
                "import java.lang.annotation.*;" +
                "@interface Annotation {" +
                "   Class<?> classValue();" +
                "   Class<?> classValue2();" +
                "   Class<?> classValue3();" +
                "}" +
                "@Annotation(classValue = Object.class, classValue2 = \"error\")" +
                "public class Annotated {" +
                "}");
        RepositoryUpdater.getDefault().scheduleCompilationAndWait(srcFO, srcFO).await();
        ClasspathInfo cpi = ClasspathInfo.create(srcFO);
        final AnnotationModelHelper helper = AnnotationModelHelper.create(cpi);
        helper.runJavaSourceTask(new Runnable() {
            public void run() {
                TypeElement annotated = helper.getCompilationController().getElements().getTypeElement("Annotated");
                AnnotationMirror annotation = annotated.getAnnotationMirrors().iterator().next();
                AnnotationParser parser = AnnotationParser.create(helper);
                parser.expectClass("classValue", parser.defaultValue("java.lang.Double"));
                parser.expectClass("classValue2", parser.defaultValue("java.lang.String"));
                parser.expectClass("classValue3", parser.defaultValue("java.lang.Integer"));
                ParseResult parseResult = parser.parse(annotation);
                assertEquals("java.lang.Object", parseResult.get("classValue", String.class));
                assertEquals("java.lang.String", parseResult.get("classValue2", String.class));
                assertEquals("java.lang.Integer", parseResult.get("classValue3", String.class));
            }
        });
    }

    public void testEnumConstant() throws Exception {
        TestUtilities.copyStringToFileObject(srcFO, "Annotated.java",
                "import java.lang.annotation.*;" +
                "@interface Annotation {" +
                "   RetentionPolicy enumValue();" +
                "   RetentionPolicy enumValue2();" +
                "   RetentionPolicy enumValue3();" +
                "}" +
                "@Annotation(enumValue = RetentionPolicy.CLASS, enumValue2 = ElementType.TYPE)" +
                "public class Annotated {" +
                "}");
        RepositoryUpdater.getDefault().scheduleCompilationAndWait(srcFO, srcFO).await();
        ClasspathInfo cpi = ClasspathInfo.create(srcFO);
        final AnnotationModelHelper helper = AnnotationModelHelper.create(cpi);
        helper.runJavaSourceTask(new Runnable() {
            public void run() {
                Elements elements = helper.getCompilationController().getElements();
                TypeElement annotated = elements.getTypeElement("Annotated");
                AnnotationMirror annotation = annotated.getAnnotationMirrors().iterator().next();
                AnnotationParser parser = AnnotationParser.create(helper);
                TypeMirror rpType = elements.getTypeElement("java.lang.annotation.RetentionPolicy").asType();
                parser.expectEnumConstant("enumValue", rpType, parser.defaultValue("SOURCE"));
                parser.expectEnumConstant("enumValue2", rpType, parser.defaultValue("CLASS"));
                parser.expectEnumConstant("enumValue3", rpType, parser.defaultValue("RUNTIME"));
                ParseResult parseResult = parser.parse(annotation);
                assertEquals("CLASS", parseResult.get("enumValue", String.class));
                assertEquals("CLASS", parseResult.get("enumValue2", String.class));
                assertEquals("RUNTIME", parseResult.get("enumValue3", String.class));
            }
        });
    }

    public void testAnnotation() throws Exception {
        TestUtilities.copyStringToFileObject(srcFO, "Annotated.java",
                "@interface Annotation {" +
                "   SuppressWarnings annValue();" +
                "   SuppressWarnings annValue2();" +
                "   SuppressWarnings annValue3();" +
                "}" +
                "@Annotation(annValue = @SuppressWarnings(\"unchecked\"), annValue2 = \"error\")" +
                "public class Annotated {" +
                "}");
        RepositoryUpdater.getDefault().scheduleCompilationAndWait(srcFO, srcFO).await();
        ClasspathInfo cpi = ClasspathInfo.create(srcFO);
        final AnnotationModelHelper helper = AnnotationModelHelper.create(cpi);
        helper.runJavaSourceTask(new Runnable() {
            public void run() {
                Elements elements = helper.getCompilationController().getElements();
                TypeElement annotated = elements.getTypeElement("Annotated");
                AnnotationMirror annotation = annotated.getAnnotationMirrors().iterator().next();
                AnnotationParser parser = AnnotationParser.create(helper);
                Object defaultValue = "defaultValue", defaultValue2 = "defaultValue2", defaultValue3 = "defaultValue3";
                AnnotationValueHandler swHandler = new AnnotationValueHandler() {
                    public Object handleAnnotation(AnnotationMirror annotation) {
                        // better to use AnnotationParser again here
                        @SuppressWarnings("unchecked")
                        List<AnnotationValue> arrayMembers = (List<AnnotationValue>)annotation.getElementValues().values().iterator().next().getValue();
                        return (String)arrayMembers.iterator().next().getValue();
                    }
                };
                TypeMirror swType = elements.getTypeElement("java.lang.SuppressWarnings").asType();
                parser.expectAnnotation("annValue", swType, swHandler, parser.defaultValue(defaultValue));
                parser.expectAnnotation("annValue2", swType, swHandler, parser.defaultValue(defaultValue2));
                parser.expectAnnotation("annValue3", swType, swHandler, parser.defaultValue(defaultValue3));
                ParseResult parseResult = parser.parse(annotation);
                assertEquals("unchecked", parseResult.get("annValue", String.class));
                assertEquals("defaultValue2", parseResult.get("annValue2", String.class));
                assertEquals("defaultValue3", parseResult.get("annValue3", String.class));
            }
        });
    }

    public void testStringArray() throws Exception {
        TestUtilities.copyStringToFileObject(srcFO, "Annotated.java",
                "@interface Annotation {" +
                "   String[] arrayValue();" +
                "   String[] arrayValue2();" +
                "   String]] arrayValue3();" +
                "}" +
                "@Annotation(arrayValue = { \"foo\", \"bar\" }, arrayValue2 = @Error)" +
                "public class Annotated {" +
                "}");
        RepositoryUpdater.getDefault().scheduleCompilationAndWait(srcFO, srcFO).await();
        ClasspathInfo cpi = ClasspathInfo.create(srcFO);
        final AnnotationModelHelper helper = AnnotationModelHelper.create(cpi);
        helper.runJavaSourceTask(new Runnable() {
            public void run() {
                Elements elements = helper.getCompilationController().getElements();
                TypeElement annotated = elements.getTypeElement("Annotated");
                AnnotationMirror annotation = annotated.getAnnotationMirrors().iterator().next();
                AnnotationParser parser = AnnotationParser.create(helper);
                List<String> defaultValue = new ArrayList<String>(), defaultValue2 = new ArrayList<String>(), defaultValue3 = new ArrayList<String>();
                ArrayValueHandler arrayHandler = new ArrayValueHandler() {
                    public Object handleArray(List<AnnotationValue> array) {
                        List<String> result = new ArrayList<String>();
                        for (AnnotationValue arrayMember : array) {
                            result.add((String)arrayMember.getValue());
                        }
                        return result;
                    }
                };
                parser.expectStringArray("arrayValue", arrayHandler, parser.defaultValue(defaultValue));
                parser.expectStringArray("arrayValue2", arrayHandler, parser.defaultValue(defaultValue2));
                parser.expectStringArray("arrayValue3", arrayHandler, parser.defaultValue(defaultValue3));
                ParseResult parseResult = parser.parse(annotation);
                @SuppressWarnings("unchecked")
                List list = (List<String>)parseResult.get("arrayValue", List.class);
                assertEquals(2, list.size());
                assertTrue(list.contains("foo"));
                assertTrue(list.contains("bar"));
                // XXX does not work
                // assertSame(defaultValue2, parseResult.get("arrayValue2", List.class));
                assertSame(defaultValue3, parseResult.get("arrayValue3", List.class));
            }
        });
    }

    public void testAnnotationArray() throws Exception {
        TestUtilities.copyStringToFileObject(srcFO, "Annotated.java",
                "import java.lang.annotation.*" +
                "@interface Annotation {" +
                "   SuppressWarnings[] annotationValue();" +
                "   SuppressWarnings[] annotationValue2();" +
                "   SuppressWarnings]] annotationValue3();" +
                "}" +
                "@Annotation(annotationValue = { @SuppressWarnings(\"foo\"), @SuppressWarnings({\"bar\", \"baz\"}) }, annotationValue2 = @Retention(RetentionPolicy.SOURCE)" +
                "public class Annotated {" +
                "}");
        RepositoryUpdater.getDefault().scheduleCompilationAndWait(srcFO, srcFO).await();
        ClasspathInfo cpi = ClasspathInfo.create(srcFO);
        final AnnotationModelHelper helper = AnnotationModelHelper.create(cpi);
        helper.runJavaSourceTask(new Runnable() {
            public void run() {
                Elements elements = helper.getCompilationController().getElements();
                TypeElement annotated = elements.getTypeElement("Annotated");
                AnnotationMirror annotation = annotated.getAnnotationMirrors().iterator().next();
                AnnotationParser parser = AnnotationParser.create(helper);
                final AnnotationParser swParser = AnnotationParser.create(helper);
                swParser.expectStringArray("value", new ArrayValueHandler() {
                    public Object handleArray(List<AnnotationValue> arrayMembers) {
                        List<String> result = new ArrayList<String>();
                        for (AnnotationValue arrayMember : arrayMembers) {
                            result.add((String)arrayMember.getValue());
                        }
                        return result;
                    }
                }, parser.defaultValue(Collections.emptyList()));
                List<String> defaultValue = new ArrayList<String>();
                ArrayValueHandler arrayHandler = new ArrayValueHandler() {
                    public Object handleArray(List<AnnotationValue> array) {
                        List<String> result = new ArrayList<String>();
                        for (AnnotationValue arrayMember : array) {
                            AnnotationMirror swAnotation = (AnnotationMirror)arrayMember.getValue();
                            @SuppressWarnings("unchecked")
                            List<String> subresult = (List<String>)swParser.parse(swAnotation).get("value", List.class);
                            result.addAll(subresult);
                        }
                        return result;
                    }
                };
                TypeMirror swType = elements.getTypeElement("java.lang.SuppressWarnings").asType();
                parser.expectAnnotationArray("annotationValue", swType, arrayHandler, null);
                parser.expectAnnotationArray("annotationValue2", swType, arrayHandler, null);
                parser.expectAnnotationArray("annotationValue3", swType, arrayHandler, parser.defaultValue(defaultValue));
                ParseResult parseResult = parser.parse(annotation);
                @SuppressWarnings("unchecked")
                List list = (List<String>)parseResult.get("annotationValue", List.class);
                assertEquals(3, list.size());
                assertTrue(list.contains("foo"));
                assertTrue(list.contains("bar"));
                assertTrue(list.contains("baz"));
                @SuppressWarnings("unchecked")
                List list2 = (List<String>)parseResult.get("annotationValue2", List.class);
                assertEquals(0, list2.size());
                assertSame(defaultValue, parseResult.get("annotationValue3", List.class));
            }
        });
    }
}
