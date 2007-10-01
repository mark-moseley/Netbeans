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
package org.netbeans.modules.java.hints;

import org.netbeans.modules.java.hints.AddOverrideAnnotation;
import com.sun.source.util.TreePath;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.SourceUtilsTestUtil;
import org.netbeans.modules.java.hints.infrastructure.TreeRuleTestBase;
import org.netbeans.spi.editor.hints.ErrorDescription;

/**
 *
 * @author Jan Lahoda
 */
public class AddOverrideAnnotationTest extends TreeRuleTestBase {
    
    public AddOverrideAnnotationTest(String testName) {
        super(testName);
    }
    
    public void testAddOverride1() throws Exception {
        performAnalysisTest("test/Test.java", "package test; public class Test extends java.util.ArrayList {public int size() {return 0;}}", 121-48, "0:72-0:76:verifier:Add @Override Annotation");
    }

    public void testAddOverride2() throws Exception {
        performAnalysisTest("test/Test.java", "package test; public class Test implements Runnable {public void run() {}}", 115-48);
    }
    
    public void testAddOverride3() throws Exception {
        sourceLevel = "1.6";
        performAnalysisTest("test/Test.java", "package test; public class Test implements Runnable {public void run() {}}", 115-48, "0:65-0:68:verifier:Add @Override Annotation");
    }
    
    public void testAddOverride4() throws Exception {
        performAnalysisTest("test/Test.java", "package test; public class UUUU {public void () {} private static class W extends UUUU {public void () {}}}", 150-48);
    }
    
    protected List<ErrorDescription> computeErrors(CompilationInfo info, TreePath path) {
        if (!new AddOverrideAnnotation().getTreeKinds().contains(path.getLeaf().getKind()))
            return null;
        
        SourceUtilsTestUtil.setSourceLevel(info.getFileObject(), sourceLevel);
        return new AddOverrideAnnotation().run(info, path);
    }
    
    private String sourceLevel = "1.5";
    
}
