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

package org.netbeans.modules.cnd.completion;

import org.netbeans.modules.cnd.completion.cplusplus.ext.CompletionBaseTestCase;

/**
 *
 * @author Vladimir Voskresensky
 */
public class StaticMembersTestCase extends CompletionBaseTestCase {
    
    /**
     * Creates a new instance of StaticMembersTestCase
     */
    public StaticMembersTestCase(String testName) {
        super(testName);
    }

    /////////////////////////////////////////////////////////////////
    // int main() {
    
    public void testMainClassA() throws Exception {
        super.performTest("main.cc", 10, 5, "ClassA::");
    }
    
    /////////////////////////////////////////////////////////////////
    // void ClassA::aPubFun() {

    public void testClassAbPubFunVarA() throws Exception {
        performTest("file.cc", 10, 5, "a.");
    }
    
    public void testClassAbPubFunVarB() throws Exception {
        performTest("file.cc", 10, 5, "b.");
    }
    
    public void testClassAbPubFunVarC() throws Exception {
        performTest("file.cc", 10, 5, "c.");
    }
        
    public void testClassAbPubFunVarD() throws Exception {
        performTest("file.cc", 10, 5, "d.");
    }  
    
    public void testClassAbPubFunVarE() throws Exception {
        performTest("file.cc", 10, 5, "e.");
    }    
}
