/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
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

package org.netbeans.test.languages;

import org.netbeans.test.lib.BasicTokensTest;

/**
 *
 * @author Jindrich Sedek
 */
public class TokensTest extends BasicTokensTest {

    public TokensTest(String name) {
        super(name);
        try {
            Thread.sleep(2000);// wait for NB inicialization
        } catch (InterruptedException ex) {
           ex.printStackTrace(); 
        }
    }

    public void testDIFF(){
        testRun("sample.diff");
    }
    
    public void testBAT(){ //DISABLED BECAUSE OF FAILTURES
        testRun("sample.bat");
    }

    public void testSH(){
        testRun("sample.sh");
    }
    
    public void testMF(){
        testRun("sample.mf");
    }
    
    protected boolean generateGoldenFiles() {
        return false;
    }
    
}
