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

package org.netbeans.modules.junit;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.NestingKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.modules.junit.plugin.JUnitPlugin.CreateTestParam;
import static javax.lang.model.element.Modifier.ABSTRACT;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PROTECTED;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;
import static org.openide.NotifyDescriptor.WARNING_MESSAGE;
import static org.netbeans.modules.junit.TestCreator.ACCESS_MODIFIERS;

/**
 *
 * @author  Marian Petras
 */
public final class TestGeneratorSetup implements TestabilityJudge {
    /* the class is final only for performance reasons */
    
    /* attributes - private */
    static private final String JUNIT_SUPER_CLASS_NAME                = "TestCase";
    static private final String JUNIT_FRAMEWORK_PACKAGE_NAME    = "junit.framework";
    
    static private final String METHOD_NAME_SETUP = "setUp";            //NOI18N
    static private final String METHOD_NAME_TEARDOWN = "tearDown";      //NOI18N
    static private final String CLASS_COMMENT_LINE1 = "TestCreator.javaClass.addTestsHereComment.l1";
    static private final String CLASS_COMMENT_LINE2 = "TestCreator.javaClass.addTestsHereComment.l2";
    
    /** should test classes be skipped during generation of tests? */
    private boolean skipTestClasses = true;
    /** should package-private classes be skipped during generation of tests? */
    private boolean skipPkgPrivateClasses = false;
    /** should abstract classes be skipped during generation of tests? */
    private boolean skipAbstractClasses = false;
    /** should exception classes be skipped during generation of tests? */
    private boolean skipExceptionClasses = false;
    /**
     * should test suite classes be generated when creating tests for folders
     * and/or packages?
     */
    private boolean generateSuiteClasses = true;
    /**
     * bitmap defining whether public/protected methods should be tested
     *
     * @see  #testPackagePrivateMethods
     */
    private Set<Modifier> methodAccessModifiers
            = AbstractTestGenerator.createModifierSet(Modifier.PUBLIC,
                                                      Modifier.PROTECTED);
    /**
     * should package-private methods be tested? 
     *
     * @see  #methodAccessModifiers
     */
    private boolean testPkgPrivateMethods = true;
    /**
     * should default method bodies be generated for newly created test methods?
     *
     * @see  #generateMethodJavadoc
     * @see  #generateMethodBodyComments
     */
    private boolean generateDefMethodBody = true;
    /**
     * should Javadoc comment be generated for newly created test methods?
     *
     * @see  #generateDefMethodBody
     * @see  #generateMethodBodyComments
     */
    private boolean generateMethodJavadoc = true;
    /**
     * should method body comment be generated for newly created test methods?
     *
     * @see  #generateDefMethodBody
     * @see  #generateMethodJavadoc
     */
    private boolean generateSourceCodeHints = true;
    /**
     * should {@code setUp()} (or {@code @Before}) method be generated 
     * in test classes?
     *
     * @see  #generateTestTearDown
     * @see  #generateClassSetUp
     * @see  #generateMainMethod
     */
    private boolean generateTestSetUp = true;
    /**
     * should {@code tearDown()} (or {@code @After}) method be generated
     * in test classes?
     *
     * @see  #generateTestSetUp
     * @see  #generateClassTearDown
     * @see  #generateMainMethod
     */
    private boolean generateTestTearDown = true;
    /**
     * should {@code @BeforeClass} method be generated in test classes?
     *
     * @see  #generateClassTearDown
     * @see  #generateTestSetUp
     * @see  #generateMainMethod
     */
    private boolean generateClassSetUp = true;
    /**
     * should {@code @AfterClass} method be generated in test classes?
     *
     * @see  #generateClassSetUp
     * @see  #generateTestTearDown
     * @see  #generateMainMethod
     */
    private boolean generateClassTearDown = true;
    /**
     * should static method <code>main(String args[])</code>
     * be generated in test classes?
     *
     * @see  #generateSetUp
     * @see  #generateTearDown
     */
    private boolean generateMainMethod = true;
    
    /* public methods */
    
    /**
     * Creates a new <code>TestCreator</code>.
     *
     * @param  loadDefaults  <code>true</code> if defaults should be loaded
     *                       from <code>JUnitSettings</code>;
     *                       <code>false</code> otherwise
     */
    public TestGeneratorSetup(boolean loadDefaults) {
        if (loadDefaults) {
            loadDefaults();
        }
    }
    
    /**
     *
     */
    public TestGeneratorSetup(Map<CreateTestParam, Object> params) {
        final JUnitSettings settings = JUnitSettings.getDefault();
        
        skipTestClasses = !JUnitSettings.GENERATE_TESTS_FROM_TEST_CLASSES;
        
        skipPkgPrivateClasses = !Boolean.TRUE.equals(params.get(
                                        CreateTestParam.INC_PKG_PRIVATE_CLASS));
        skipAbstractClasses = !Boolean.TRUE.equals(params.get(
                                        CreateTestParam.INC_ABSTRACT_CLASS));
        skipExceptionClasses = !Boolean.TRUE.equals(params.get(
                                        CreateTestParam.INC_EXCEPTION_CLASS));
        generateSuiteClasses = Boolean.TRUE.equals(params.get(
                                        CreateTestParam.INC_GENERATE_SUITE));
        
        methodAccessModifiers.clear();
        if (Boolean.TRUE.equals(params.get(CreateTestParam.INC_PUBLIC))) {
            methodAccessModifiers.add(Modifier.PUBLIC);
        }
        if (Boolean.TRUE.equals(params.get(CreateTestParam.INC_PROTECTED))) {
            methodAccessModifiers.add(Modifier.PROTECTED);
        }
        testPkgPrivateMethods = Boolean.TRUE.equals(params.get(
                                        CreateTestParam.INC_PKG_PRIVATE));
        generateDefMethodBody = Boolean.TRUE.equals(params.get(
                                        CreateTestParam.INC_METHOD_BODIES));
        generateMethodJavadoc = Boolean.TRUE.equals(params.get(
                                        CreateTestParam.INC_JAVADOC));
        generateSourceCodeHints = Boolean.TRUE.equals(params.get(
                                        CreateTestParam.INC_CODE_HINT));
        generateTestSetUp = Boolean.TRUE.equals(params.get(
                                        CreateTestParam.INC_SETUP));
        generateTestTearDown = Boolean.TRUE.equals(params.get(
                                        CreateTestParam.INC_TEAR_DOWN));
        
        generateMainMethod = settings.isGenerateMainMethod();
    }

    
    /**
     * Loads default settings from <code>JUnitSettings</code>.
     */
    private void loadDefaults() {
        final JUnitSettings settings = JUnitSettings.getDefault();
        
        skipTestClasses = JUnitSettings.GENERATE_TESTS_FROM_TEST_CLASSES;
        skipPkgPrivateClasses = !settings.isIncludePackagePrivateClasses();
        skipAbstractClasses = !settings.isGenerateAbstractImpl();
        skipExceptionClasses = !settings.isGenerateExceptionClasses();
        generateSuiteClasses = settings.isGenerateSuiteClasses();
        
        methodAccessModifiers.clear();
        if (settings.isMembersPublic()) {
            methodAccessModifiers.add(Modifier.PUBLIC);
        }
        if (settings.isMembersProtected()) {
            methodAccessModifiers.add(Modifier.PROTECTED);
        }
        testPkgPrivateMethods = settings.isMembersPackage();
        
        generateDefMethodBody = settings.isBodyContent();
        generateMethodJavadoc = settings.isJavaDoc();
        generateSourceCodeHints = settings.isBodyComments();
        generateTestSetUp = settings.isGenerateSetUp();
        generateTestTearDown = settings.isGenerateTearDown();
        generateMainMethod = settings.isGenerateMainMethod();
    }
    
    /**
     * Sets whether tests for test classes should be generated
     * The default is <code>true</code>.
     *
     * @param  test  <code>false</code> if test classes should be skipped
     *               during test creation;
     *               <code>true</code> otherwise
     */
    public void setSkipTestClasses(boolean skip) {
        this.skipTestClasses = skip;
    }
    
    /**
     */
    public boolean isSkipTestClasses() {
        return skipTestClasses;
    }
    
    /**
     * Sets whether tests for package-private classes should be generated
     * The default is <code>false</code>.
     *
     * @param  test  <code>false</code> if package-private classes should
     *               be skipped during test creation;
     *               <code>true</code> otherwise
     */
    public void setSkipPackagePrivateClasses(boolean skip) {
        this.skipPkgPrivateClasses = skip;
    }

    /**
     */
    public boolean isSkipPackagePrivateClasses() {
        return skipPkgPrivateClasses;
    }
    
    /**
     * Sets whether tests for abstract classes should be generated
     * The default is <code>false</code>.
     *
     * @param  test  <code>false</code> if abstract classes should be skipped
     *               during test creation;
     *               <code>true</code> otherwise
     */
    public void setSkipAbstractClasses(boolean skip) {
        this.skipAbstractClasses = skip;
    }

    /**
     */
    public boolean isSkipAbstractClasses() {
        return skipAbstractClasses;
    }
    
    /**
     * Sets whether tests for exception classes should be generated
     * The default is <code>false</code>.
     *
     * @param  test  <code>false</code> if exception classes should be skipped
     *               during test creation;
     *               <code>true</code> otherwise
     */
    public void setSkipExceptionClasses(boolean skip) {
        this.skipExceptionClasses = skip;
    }

    /**
     */
    public boolean isSkipExceptionClasses() {
        return skipExceptionClasses;
    }
    
    /**
     * Sets whether test suite classes should be generated when creating tests
     * for folders and/or packages.
     *
     * @param  generate  <code>true</code> if test suite classes should
     *                   be generated; <code>false</code> otherwise
     */
    public void setGenerateSuiteClasses(boolean generate) {
        this.generateSuiteClasses = generate;
    }

    /**
     */
    public boolean isGenerateSuiteClasses() {
        return generateSuiteClasses;
    }
    
    /**
     * Sets whether public methods should be tested or not.
     * The default is <code>true</code>.
     *
     * @param  test  <code>true</code> if public methods should be tested;
     *               <code>false</code> if public methods should be skipped
     */
    public void setTestPublicMethods(boolean test) {
        if (test) {
            methodAccessModifiers.add(Modifier.PUBLIC);
        } else {
            methodAccessModifiers.remove(Modifier.PUBLIC);
        }
    }

    /**
     */
    public boolean isTestPublicMethods() {
        return methodAccessModifiers.contains(Modifier.PUBLIC);
    }
    
    /**
     * Sets whether protected methods should be tested or not.
     * The default is <code>true</code>.
     *
     * @param  test  <code>true</code> if protected methods should be tested;
     *               <code>false</code> if protected methods should be skipped
     */
    public void setTestProtectedMethods(boolean test) {
        if (test) {
            methodAccessModifiers.add(Modifier.PROTECTED);
        } else {
            methodAccessModifiers.remove(Modifier.PROTECTED);
        }
    }
    
    /**
     */
    public boolean isTestProtectedMethods() {
        return methodAccessModifiers.contains(Modifier.PROTECTED);
    }
    
    /**
     * Tells which methods should be tested - public, protected, private 
     * or a combination of these.
     * 
     * @return  {@code EnumSet} of access modifiers that determine which methods
     * should be tested (for which test skeletons should be created).
     */
    public EnumSet<Modifier> getMethodAccessModifiers() {
        return EnumSet.copyOf(methodAccessModifiers);
    }
    
    /**
     * Sets whether package-private methods should be tested or not.
     * The default is <code>true</code>.
     *
     * @param  test  <code>true</code> if package-private methods should be
     *               tested;
     *               <code>false</code> if package-private methods should be
     *              skipped
     */
    public void setTestPackagePrivateMethods(boolean test) {
        this.testPkgPrivateMethods = test;
    }

    /**
     */
    public boolean isTestPackagePrivateMethods() {
        return testPkgPrivateMethods;
    }
    
    /**
     * Sets whether default method bodies should be generated for newly created
     * test methods.
     * The default is <code>true</code>.
     *
     * @param  generate  <code>true</code> if default method bodies should
     *                   be generated; <code>false</code> otherwise
     */
    public void setGenerateDefMethodBody(boolean generate) {
        this.generateDefMethodBody = generate;
    }

    /**
     */
    public boolean isGenerateDefMethodBody() {
        return generateDefMethodBody;
    }
    
    /**
     * Sets whether Javadoc comment should be generated for newly created
     * test methods.
     * The default is <code>true</code>.
     *
     * @param  generate  <code>true</code> if Javadoc comment should
     *                   be generated; <code>false</code> otherwise
     */
    public void setGenerateMethodJavadoc(boolean generate) {
        this.generateMethodJavadoc = generate;
    }

    /**
     */
    public boolean isGenerateMethodJavadoc() {
        return generateMethodJavadoc;
    }
    
    /**
     * Sets whether method body comment should be generated for newly created
     * test methods.
     * The default is <code>true</code>.
     *
     * @param  generate  <code>true</code> if method body comment should
     *                   be generated; <code>false</code> otherwise
     */
    public void setGenerateMethodBodyComment(boolean generate) {
        this.generateSourceCodeHints = generate;
    }

    /**
     */
    public boolean isGenerateMethodBodyComment() {
        return generateSourceCodeHints;
    }
    
    /**
     * Sets whether <code>setUp()</code> method should be generated
     * in test classes being created/updated.
     * The default is <code>true</code>.
     *
     * @param  generate  <code>true</code> if <code>setUp()</code> method
     *                   should be generated; <code>false</code> otherwise
     * @see  #setGenerateTearDown
     * @see  #setGenerateMainMethod
     */
    public void setGenerateSetUp(boolean generate) {
        this.generateTestSetUp = generate;
    }

    /**
     */
    public boolean isGenerateSetUp() {
        return generateTestSetUp;
    }
    
    /**
     * Sets whether <code>tearDown()</code> method should be generated
     * in test classes being created/updated.
     * The default is <code>true</code>.
     *
     * @param  generate  <code>true</code> if <code>tearDown()</code> method
     *                   should be generated; <code>false</code> otherwise
     * @see  #setGenerateSetUp
     * @see  #setGenerateMainMethod
     */
    public void setGenerateTearDown(boolean generate) {
        this.generateTestTearDown = generate;
    }

    /**
     */
    public boolean isGenerateTearDown() {
        return generateTestTearDown;
    }
    
    /**
     * Sets whether <code>setUp()</code> method should be generated
     * in test classes being created/updated.
     * The default is <code>true</code>.
     *
     * @param  generate  <code>true</code> if <code>setUp()</code> method
     *                   should be generated; <code>false</code> otherwise
     * @see  #setGenerateTearDown
     * @see  #setGenerateMainMethod
     */
    public void setGenerateBefore(boolean generate) {
        setGenerateSetUp(generate);
    }

    /**
     */
    public boolean isGenerateBefore() {
        return isGenerateSetUp();
    }
    
    /**
     * Sets whether <code>tearDown()</code> method should be generated
     * in test classes being created/updated.
     * The default is <code>true</code>.
     *
     * @param  generate  <code>true</code> if <code>tearDown()</code> method
     *                   should be generated; <code>false</code> otherwise
     * @see  #setGenerateSetUp
     * @see  #setGenerateMainMethod
     */
    public void setGenerateAfter(boolean generate) {
        setGenerateTearDown(generate);
    }

    /**
     */
    public boolean isGenerateAfter() {
        return isGenerateTearDown();
    }
    
    /**
     * Sets whether <code>setUp()</code> method should be generated
     * in test classes being created/updated.
     * The default is <code>true</code>.
     *
     * @param  generate  <code>true</code> if <code>setUp()</code> method
     *                   should be generated; <code>false</code> otherwise
     * @see  #setGenerateTearDown
     * @see  #setGenerateMainMethod
     */
    public void setGenerateBeforeClass(boolean generate) {
        this.generateClassSetUp = generate;
    }

    /**
     */
    public boolean isGenerateBeforeClass() {
        return generateClassSetUp;
    }
    
    /**
     * Sets whether <code>tearDown()</code> method should be generated
     * in test classes being created/updated.
     * The default is <code>true</code>.
     *
     * @param  generate  <code>true</code> if <code>tearDown()</code> method
     *                   should be generated; <code>false</code> otherwise
     * @see  #setGenerateSetUp
     * @see  #setGenerateMainMethod
     */
    public void setGenerateAfterClass(boolean generate) {
        this.generateClassTearDown = generate;
    }

    /**
     */
    public boolean isGenerateAfterClass() {
        return generateClassTearDown;
    }
    
    /**
     * Sets whether static method <code>main(String args[])</code> should
     * be generated in test classes.
     * The default is <code>true</code>.
     *
     * @param  generate  <code>true</code> if the method should be generated;
     *                   <code>false</code> otherwise
     * @see  #setGenerateSetUp
     * @see  #setGenerateTearDown
     */
    public void setGenerateMainMethod(boolean generate) {
        this.generateMainMethod = generate;
    }

    /**
     */
    public boolean isGenerateMainMethod() {
        return generateMainMethod;
    }

    /**
     * Checks whether the given class or at least one of its nested classes
     * is testable.
     *
     * @param  compInfo  used for {@link CompilationInfo#getElements()}
     *                   and {@link CompilationInfo#getTypes()}
     * @param  classElem  class to be checked
     * @return  TestabilityResult that isOk, if the class is testable or carries
     *          the information why the class is not testable
     */
    public TestabilityResult isClassTestable(CompilationInfo compInfo,
                                             TypeElement classElem) {
        assert classElem != null;
        
        TestabilityResult result = isClassTestableSingle(compInfo, classElem);

        if (result.isTestable()) {
            return TestabilityResult.OK;
        }

        List<? extends Element> enclosedElems = classElem.getEnclosedElements();
        if (enclosedElems.isEmpty()) {
            /* Not testable, no contained types - no more chance: */
            return result;
        }
        
        List<TypeElement> enclosedTypes = ElementFilter.typesIn(enclosedElems);
        if (enclosedTypes.isEmpty()) {
            /* Not testable, no contained types - no more chance: */
            return result;
        }
        
        /* Not testable but maybe one of its nested classes is testable: */
        List<TypeElement> stack
               = new ArrayList<TypeElement>(Math.max(10, enclosedTypes.size()));
        stack.addAll(enclosedTypes);
        int stackSize = stack.size();

        Set<TypeElement> nonTestable = new HashSet<TypeElement>(64);
        nonTestable.add(classElem);

        do {
            TypeElement classToCheck = stack.remove(--stackSize);
            
            if (!TopClassFinder.isTestable(classElem)) {   //it is an annotation
                continue;
            }

            if (!nonTestable.add(classToCheck)) {
                continue; //we already know this single class is nontestable
            }

            TestabilityResult resultSingle
                                = isClassTestableSingle(compInfo, classToCheck);
            if (resultSingle.isTestable()) {
                return TestabilityResult.OK;
            } else {
                result = TestabilityResult.combine(result, resultSingle);
            }

            enclosedTypes = ElementFilter.typesIn(classToCheck.getEnclosedElements());
            if (!enclosedTypes.isEmpty()) {
                stack.addAll(enclosedTypes);
                stackSize = stack.size();
            }
        } while (stackSize != 0);

        /* So not a single contained class is testable - no more chance: */
        return result;
    }
    
    /**
     * Checks whether the given class is testable.
     *
     * @param  jc  class to be checked
     * @return  TestabilityResult that isOk, if the class is testable or carries
     *          the information why the class is not testable
     */
    private TestabilityResult isClassTestableSingle(CompilationInfo compInfo,
                                                    TypeElement classElem) {
        assert classElem != null;
        
        TestabilityResult result = TestabilityResult.OK;

        /*
         * If the class is a test class and test classes should be skipped,
         * do not check nested classes (skip all):
         */
        /* Check if the class itself (w/o nested classes) is testable: */
        Set<Modifier> modifiers = classElem.getModifiers();

        if (modifiers.contains(PRIVATE))
            result = TestabilityResult.combine(result, TestabilityResult.PRIVATE_CLASS);
        if (isSkipTestClasses() && TestUtil.isClassImplementingTestInterface(compInfo, classElem)) 
            result = TestabilityResult.combine(result, TestabilityResult.TEST_CLASS);
        if (isSkipPackagePrivateClasses() && (modifiers.isEmpty() || !EnumSet.copyOf(modifiers).removeAll(ACCESS_MODIFIERS)))
            result = TestabilityResult.combine(result, TestabilityResult.PACKAGE_PRIVATE_CLASS);
        if (isSkipAbstractClasses() && modifiers.contains(ABSTRACT))
            result = TestabilityResult.combine(result, TestabilityResult.ABSTRACT_CLASS);
        if (!modifiers.contains(STATIC) && (classElem.getNestingKind() != NestingKind.TOP_LEVEL))
            result = TestabilityResult.combine(result, TestabilityResult.NONSTATIC_INNER_CLASS);
        if (!hasTestableMethods(classElem))
            result = TestabilityResult.combine(result, TestabilityResult.NO_TESTEABLE_METHODS);
        if (isSkipExceptionClasses() && TestUtil.isClassException(compInfo, classElem)) 
            result = TestabilityResult.combine(result, TestabilityResult.EXCEPTION_CLASS);

        return result;
    }
    
    /**
     */
    private boolean hasTestableMethods(TypeElement classElem) {
        List<? extends Element> enclosedElems = classElem.getEnclosedElements();
        if (enclosedElems.isEmpty()) {
            return false;
        }
        
        List<ExecutableElement> methods = ElementFilter.methodsIn(enclosedElems);
        if (methods.isEmpty()) {
            return false;
        }
        
        for (ExecutableElement method : methods) {
            if (isMethodTestable(method)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Checks whether a test for the given method should be created.
     * Access modifiers of the given method are compared to this creator's
     * settings.
     *
     * @param  m  method to be checked
     * @return  <code>true</code> if this creator is configured to create tests
     *          for methods having the given method's access modifiers;
     *          <code>false</code> otherwise
     */
    public boolean isMethodTestable(ExecutableElement method) {
        Set<Modifier> modifiers = method.getModifiers();
        
        if (modifiers.isEmpty()) {
            /*
             * EnumSet.copyOf(modifiers) may throw an exception if 'modifiers'
             * is empty.
             */
            return isTestPackagePrivateMethods();
        } else {
            return (isTestPackagePrivateMethods()
                        && !EnumSet.copyOf(modifiers).removeAll(ACCESS_MODIFIERS))
                   || EnumSet.copyOf(modifiers).removeAll(getMethodAccessModifiers());
        }
    }
    
}
