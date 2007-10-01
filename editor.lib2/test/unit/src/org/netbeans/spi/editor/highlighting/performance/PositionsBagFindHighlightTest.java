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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.spi.editor.highlighting.performance;

import javax.swing.text.PlainDocument;
import javax.swing.text.SimpleAttributeSet;
import junit.framework.TestSuite;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.spi.editor.highlighting.support.PositionsBag;
import org.netbeans.spi.editor.highlighting.HighlightsSequence;

/**
 *
 * @author vita
 */
public class PositionsBagFindHighlightTest extends NbTestCase {

    public static TestSuite suite() {
        return NbTestSuite.speedSuite(PositionsBagFindHighlightTest.class, 2, 3);
    }

    private int cnt = 0;
    private PositionsBag bag = null;
    private int startOffset;
    private int endOffset;
    
    /** Creates a new instance of PerfTest */
    public PositionsBagFindHighlightTest(String name) {
        super(name);
    }
    
    protected @Override void setUp() {
        cnt = this.getTestNumber();
        bag = new PositionsBag(new PlainDocument(), false);
        
        for(int i = 0; i < cnt; i++) {
            bag.addHighlight(new SimplePosition(i * 10), new SimplePosition(i * 10 + 5), SimpleAttributeSet.EMPTY);
        }

        startOffset = 10 * cnt / 5 - 1;
        endOffset = 10 * (cnt/ 5 + 1) - 1;
        
//        System.out.println("cnt = " + cnt + " : startOffset = " + startOffset + " : endOffset = " + endOffset);
    }
    
    public void testFindHighlight10() {
        HighlightsSequence seq = bag.getHighlights(startOffset, endOffset);
    }
    
    public void testFindHighlight10000() {
        HighlightsSequence seq = bag.getHighlights(startOffset, endOffset);
    }
}
