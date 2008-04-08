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

package org.netbeans.modules.php.editor.nav;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import junit.framework.TestCase;
import org.netbeans.modules.gsf.api.CancellableTask;
import org.netbeans.modules.gsf.api.ColoringAttributes;
import org.netbeans.modules.gsf.api.CompilationInfo;
import org.netbeans.modules.gsf.api.OffsetRange;

/**
 *
 * @author Jan Lahoda
 */
public class OccurrencesFinderImplTest extends TestBase {
    
    public OccurrencesFinderImplTest(String testName) {
        super(testName);
    }            

    public void testOccurrences1() throws Exception {
        performTestOccurrences("<?php\n^$name^ = \"test\";\n echo \"^$na|me^\";\n?>");
    }
    
    public void testOccurrences2() throws Exception {
        performTestOccurrences("<?php\necho \"^$name^\";\n echo \"^$na|me^\";\n?>");
    }
    
    public void testOccurrences3() throws Exception {
        performTestOccurrences("<?php\n" +
                               "$name = \"test\";\n" +
                               "function foo() {\n" +
                               "    echo \"^$na|me^\";\n" +
                               "}\n" + 
                               "?>");
    }
    
    public void testOccurrences4() throws Exception {
        performTestOccurrences("<?php\n" +
                               "^$name^ = \"test\";\n" +
                               "function foo() {\n" +
                               "    global ^$name^;\n" +
                               "    echo \"^$na|me^\";\n" +
                               "}\n" + 
                               "?>");
    }
    
    public void testOccurrences5() throws Exception {
        performTestOccurrences("<?php\n" +
                               "^$name^ = \"test\";\n" +
                               "function foo() {\n" +
                               "    echo ^$GLOBALS['na|me']^;\n" +
                               "}\n" + 
                               "?>");
    }
    
    public void testOccurrencesDefines() throws Exception {
        performTestOccurrences("<?php\n" +
                               "echo \"fff\".test.\"dddd\";\n" +
                               "define('^test^', 'testttttt');\n" +
                               "echo \"fff\".^te|st^.\"dddd\";\n" +
                               "echo \"fff\".^test^.\"dddd\";\n" +
                               "?>");
    }
    
    private void performTestOccurrences(String code) throws Exception {
        int caretOffset = code.replaceAll("\\^", "").indexOf('|');
        String[] split = code.replaceAll("\\|", "").split("\\^");
        
        assertTrue(split.length > 1);
        
        int[] goldenRanges = new int[split.length - 1];
        int offset = split[0].length();
        
        for (int cntr = 1; cntr < split.length; cntr++) {
            goldenRanges[cntr - 1] = offset;
            offset += split[cntr].length();
        }
        
        assertTrue(caretOffset != (-1));
        
        performTestOccurrences(code.replaceAll("\\^", "").replaceAll("\\|", ""), caretOffset, goldenRanges);
    }

    private void performTestOccurrences(String code, final int caretOffset, final int... goldenRanges) throws Exception {
        performTest(new String[] {code}, new CancellableTask<CompilationInfo>() {
            public void cancel() {}
            public void run(CompilationInfo parameter) throws Exception {
                Collection<OffsetRange> ranges = OccurrencesFinderImpl.compute(parameter, caretOffset);
                List<Integer> golden = new LinkedList<Integer>();
                List<Integer> out = new LinkedList<Integer>();
                
                for (OffsetRange r : ranges) {
                    out.add(r.getStart());
                    out.add(r.getEnd());
                }
                
                for (int i : goldenRanges) {
                    golden.add(i);
                }
                
                assertEquals(golden, out);
            }
        });
    }
}
