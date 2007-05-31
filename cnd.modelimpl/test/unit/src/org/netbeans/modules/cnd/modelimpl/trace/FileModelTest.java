/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.modelimpl.trace;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;

/**
 * pre-integration tests for parser
 * @author Vladimir Voskresensky
 */
public class FileModelTest extends TraceModelTestBase {

    public FileModelTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
	System.setProperty("parser.report.errors", "true");
        super.setUp();
    }

    protected void postSetUp() {
        // init flags needed for file model tests
        getTraceModel().setDumpModel(true);
        getTraceModel().setDumpPPState(true);
    }
    
    public void testDummy() throws Exception {
        performTest("dummy.cc"); // NOI18N
    }
    
    public void testDefineMacro() throws Exception {
        performTest("define_macro.cc"); // NOI18N
    }
    
    public void testIncludeCorrectness() throws Exception {
        performTest("test_include_correcteness.cc"); // NOI18N
    }   
    
    public void testTemplateExplicitInstantiation() throws Exception {
        performTest("template_explicit_instantiation.cc"); // NOI18N
    }
    
    public void testIntStaticField() throws Exception {
	performTest("int_static_field.cc"); // NOI18N
    }

    public void testResolverInfiniteLoop1() throws Exception {
	performTest("infinite1.cc"); // NOI18N
    }

    public void testResolverInfiniteLoop2() throws Exception {
	performTest("infinite2.cc"); // NOI18N
    }
    
    public void testResolverInfiniteLoop3() throws Exception {
	performTest("infinite3.cc"); // NOI18N
    }
    
    /////////////////////////////////////////////////////////////////////
    // FAILS
    
    public static class Failed extends TraceModelTestBase {
	
        public Failed(String testName) {
            super(testName);
        }

	@Override
	protected void setUp() throws Exception {
	    System.setProperty("parser.report.errors", "true");
	    super.setUp();
	}
	
	protected Class getTestCaseDataClass() {
	    return FileModelTest.class;
	}
	

	public void testTemplateInnerClassDtorDefinition() throws Exception {
	    performTest("template_inner_class_dtor_definition.cc"); // NOI18N
	}

	public void testFunctionPointerAsParameterType () throws Exception {
	    performTest("function_pointer_as_param_type.cc"); // NOI18N
	}
	
	public void testFunctionPointerAsReturnType () throws Exception {
	    performTest("function_pointer_as_return_type.cc"); // NOI18N
	}
	
	public void testFunctionPointerAsVariableType () throws Exception {
	    performTest("function_pointer_as_var_type.cc"); // NOI18N
	}
	
	protected void postSetUp() {
	    // init flags needed for file model tests
	    getTraceModel().setDumpModel(true);
	    getTraceModel().setDumpPPState(true);
	}	
   }
    
}
