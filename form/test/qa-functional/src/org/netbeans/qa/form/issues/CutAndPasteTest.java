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
package org.netbeans.qa.form.issues;

import org.netbeans.jellytools.modules.form.ComponentInspectorOperator;
import org.netbeans.jellytools.modules.form.FormDesignerOperator;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.jellytools.actions.*;
import org.netbeans.jellytools.*;
import org.netbeans.qa.form.ExtJellyTestCase;
import org.netbeans.jellytools.nodes.Node;
import java.util.*;

/**
 * Issue #103199 test - Undo after Cut&Paste removes componets from designer
 * @see <a href="http://www.netbeans.org/issues/show_bug.cgi?id=103199">issue</a>
 *
 * @author Jiri Vagner
 */
public class CutAndPasteTest extends ExtJellyTestCase {
    
    /** Constructor required by JUnit */
    public CutAndPasteTest(String testName) {
        super(testName);
    }
    
    /* Method allowing to execute test directly from IDE. */
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    /** Creates suite from particular test cases. */
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new CutAndPasteTest("testCutAndPaste")); // NOI18N
        return suite;
    }
    
    /** Cut and paste test. */
    public void testCutAndPaste() {
        String frameName = createJFrameFile();
        FormDesignerOperator designer = new FormDesignerOperator(frameName);
        ComponentInspectorOperator inspector = new ComponentInspectorOperator();
        
        Node actNode = new Node(inspector.treeComponents(), "JFrame"); // NOI18N
        
        runPopupOverNode("Add From Palette|Swing Containers|Panel", actNode); // NOI18N
        runPopupOverNode("Add From Palette|Swing Controls|Button", actNode); // NOI18N
        
        actNode = new Node(inspector.treeComponents(), "[JFrame]|jButton1 [JButton]"); // NOI18N
        runPopupOverNode("Cut", actNode); // NOI18N
        
        actNode = new Node(inspector.treeComponents(), "[JFrame]|jPanel1 [JPanel]"); // NOI18N
        runPopupOverNode("Paste", actNode); // NOI18N
        
        findInCode("javax.swing.JButton jButton1", designer); // NOI18N
        
        removeFile(frameName);
    }
}
