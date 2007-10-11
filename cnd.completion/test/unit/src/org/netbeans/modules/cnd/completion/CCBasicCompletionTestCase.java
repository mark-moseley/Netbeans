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

package org.netbeans.modules.cnd.completion;

import org.netbeans.modules.cnd.completion.cplusplus.ext.CompletionBaseTestCase;

/**
 *
 * @author Vladimir Voskresensky
 */
public class CCBasicCompletionTestCase extends CompletionBaseTestCase {
    
    /**
     * Creates a new instance of CCBasicCompletionTestCase
     */
    public CCBasicCompletionTestCase(String testName) {
        super(testName, true);
    }
    
    public void testCompletionInEmptyFile() throws Exception {
        super.performTest("empty.cc", 1,1);
    }
    
    public void testRecoveryBeforeFoo() throws Exception {
        super.performTest("file.cc", 43, 5, "a.");
    }
    
    public void testExtraDeclarationOnTypeInsideFun() throws Exception {
        super.performTest("file.cc", 39, 5, "p");
    }
    
    public void testSwitchCaseVarsInCompound() throws Exception {
        super.performTest("file.cc", 24, 13);
    }

    public void testSwitchCaseVarsNotIncCompound() throws Exception {
        super.performTest("file.cc", 28, 13);
    }
    
    public void testSwitchCaseVarsAfterCompoundAndNotCompoundInDefault() throws Exception {
        super.performTest("file.cc", 32, 13);
    }
    
    public void testCompletionOnEmptyInGlobal() throws Exception {
        super.performTest("file.cc", 1, 1);
    }
    
    public void testCompletionOnEmptyInClassFunction() throws Exception {
        super.performTest("file.cc", 7, 1);
    }  
    
    public void testCompletionOnEmptyInGlobFunction() throws Exception {
        super.performTest("file.cc", 19, 1);
    }  
    
    public void testCompletionInsideInclude() throws Exception {
        // IZ#98530]  Completion list appears in #include directive
        super.performTest("file.cc", 2, 11); // completion inside #include "file.c"
    }

    public void testCompletionInsideString() throws Exception {
        // no completion inside string
        super.performTest("file.cc", 8, 18); // completion inside strings
    }

    public void testCompletionInsideChar() throws Exception {
        // no completion inside char literal
        super.performTest("file.cc", 14, 15); // completion inside chars
    }

    public void testGlobalCompletionInGlobal() throws Exception {
        super.performTest("file.cc", 5, 1, "::");
    } 
    
    public void testGlobalCompletionInClassFunction() throws Exception {
        super.performTest("file.cc", 7, 1, "::");
    }  

    public void testGlobalCompletionInGlobFunction() throws Exception {
        super.performTest("file.cc", 19, 1, "::");
    }  
    
    public void testCompletionInConstructor() throws Exception {
        super.performTest("file.h", 20, 9);
    }
    
    public void testProtectedMethodByClassPrefix() throws Exception {
        super.performTest("file.h", 23, 9, "B::");
    }
    ////////////////////////////////////////////////////////////////////////////
    // tests for incomplete or incorrect constructions
    
    public void testErrorCompletion1() throws Exception {
        super.performTest("file.cc", 5, 1, "->");
    }    

    public void testErrorCompletion2() throws Exception {
        super.performTest("file.cc", 5, 1, ".");
    }    

    public void testErrorCompletion3() throws Exception {
        super.performTest("file.cc", 5, 1, ".->");
    }    

    public void testErrorCompletion4() throws Exception {
        super.performTest("file.cc", 5, 1, "::.");
    }    

    public void testErrorCompletion5() throws Exception {
        super.performTest("file.cc", 5, 1, "*:");
    }    

    public void testErrorCompletion6() throws Exception {
        super.performTest("file.cc", 5, 1, ":");
    }    

    public void testErrorCompletion7() throws Exception {
        super.performTest("file.cc", 5, 1, "->");
    }    

    public void testErrorCompletion8() throws Exception {
        super.performTest("file.cc", 5, 1, "#inc");
    } 
    
    public void testErrorCompletionInFun1() throws Exception {
        super.performTest("file.cc", 7, 1, "->");
    }    

    public void testErrorCompletionInFun2() throws Exception {
        super.performTest("file.cc", 7, 1, ".");
    }    

    public void testErrorCompletionInFun3() throws Exception {
        super.performTest("file.cc", 7, 1, ".->");
    }    

    public void testErrorCompletionInFun4() throws Exception {
        super.performTest("file.cc", 7, 1, "::.");
    }    

    public void testErrorCompletionInFun5() throws Exception {
        super.performTest("file.cc", 7, 1, "*:");
    }    

    public void testErrorCompletionInFun6() throws Exception {
        super.performTest("file.cc", 7, 1, ":");
    }    

    public void testErrorCompletionInFun7() throws Exception {
        super.performTest("file.cc", 7, 1, "->");
    }     

    public void testCompletionInEmptyUsrInclude() throws Exception {
        super.performTest("file.cc", 1, 1, "#include \"\"", -1);
    }

    public void testCompletionInEmptySysInclude() throws Exception {
        super.performTest("file.cc", 1, 1, "#include <>", -1);
    }
}
