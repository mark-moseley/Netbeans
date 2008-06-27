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

package org.netbeans.modules.quicksearch;

import java.util.List;
import org.netbeans.junit.NbTest;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.spi.quicksearch.SearchProvider;
import org.netbeans.spi.quicksearch.SearchRequest;
import org.netbeans.spi.quicksearch.SearchResponse;

/**
 *
 * @author Dafe Simonek
 */
public class CommandEvaluatorTest extends NbTestCase {
    
    private static final int MAX_TIME = 1000;
    
    private Exception exc = null;
    
    public CommandEvaluatorTest() {
        super("");
    }
    
    public CommandEvaluatorTest(String testName) {
        super(testName);
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static NbTest suite() {
        NbTestSuite suite = new NbTestSuite(CommandEvaluatorTest.class);
        return suite;
    }
    
    public void testResponsiveness () throws Exception {
        UnitTestUtils.prepareTest(new String [] { "/org/netbeans/modules/quicksearch/resources/testSlowProvider.xml" });

        System.out.println("Testing resposiveness against slow providers...");
        
        long startTime = System.currentTimeMillis();
        
        CommandEvaluator.evaluate("sample text", ResultsModel.getInstance());
        
        long endTime = System.currentTimeMillis();
        
        assertFalse("Evaluator is slower then expected, max allowed time is " +
                MAX_TIME + " millis, but was " + (endTime - startTime) + " millis.",
                (endTime - startTime) > 1000);
    }

    public void testObsoleteSupport () throws Exception {
        System.out.println("Testing obsolete support...");

        UnitTestUtils.prepareTest(new String [] { "/org/netbeans/modules/quicksearch/resources/testGetProviders.xml" });
        ResultsModel rm = ResultsModel.getInstance();

        CommandEvaluator.evaluate("test obsolete 1", rm);
        List<? extends CategoryResult> categories = rm.getContent();

        CommandEvaluator.evaluate("test obsolete 2", rm);

        for (CategoryResult cr : categories) {
            assertTrue("Category " + cr.getCategory().getDisplayName() +
                    " should be obsolete", cr.isObsolete());
        }
    }
    
    public static class SlowProvider implements SearchProvider {

        public void evaluate(SearchRequest request, SearchResponse response) {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException ex) {
                System.err.println("SlowProvider interrupted...");
            }
        }
        
    }

}
