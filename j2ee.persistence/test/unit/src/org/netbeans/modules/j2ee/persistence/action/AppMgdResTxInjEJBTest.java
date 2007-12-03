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

package org.netbeans.modules.j2ee.persistence.action;

import org.netbeans.modules.j2ee.persistence.spi.entitymanagergenerator.ApplicationManagedResourceTransactionInjectableInEJB;
import org.netbeans.modules.j2ee.persistence.spi.entitymanagergenerator.EntityManagerGenerationStrategy;
import java.io.File;
import junit.framework.*;
import org.netbeans.modules.j2ee.metadata.model.support.TestUtilities;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * Tests for <code>ApplicationManagedResourceTransactionInjectableIeInEJB</code>, 
 * obscure name due to #122544.
 * 
 * @author Erno Mononen
 */
public class AppMgdResTxInjEJBTest extends EntityManagerGenerationTestSupport {
    
    public AppMgdResTxInjEJBTest(String testName) {
        super(testName);
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        File javaxAnnotation = new File(getWorkDir(), "javax" + File.separator + "annotation");
        javaxAnnotation.mkdirs();
        TestUtilities.copyStringToFile(
                new File(javaxAnnotation, "PostConstruct.java"), 
                "package javax.annotation; public @interface PostConstruct{}");
        TestUtilities.copyStringToFile(
                new File(javaxAnnotation, "PreDestroy.java"), 
                "package javax.annotation; public @interface PreDestroy{}");
    }

    public void testGenerate() throws Exception{
        
        File testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
                "package org.netbeans.test;\n\n" +
                "import java.util.*;\n\n" +
                "public class Test {\n" +
                "}"
                );
        GenerationOptions options = new GenerationOptions();
        options.setMethodName("create");
        options.setOperation(GenerationOptions.Operation.PERSIST);
        options.setParameterName("object");
        options.setParameterType("Object");
        options.setQueryAttribute("");
        options.setReturnType("Object");
        
        FileObject result = generate(FileUtil.toFileObject(testFile), options);
        assertFile(result);
    }
    
    public void testGenerateWithExistingEM() throws Exception{
        
        File testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
                "package org.netbeans.test;\n\n" +
                "import java.util.*;\n" +
                "import javax.persistence.EntityManager;\n\n" +
                "public class Test {\n\n" +
                "    private EntityManager myEm;\n" + 
                "}"
                );
        GenerationOptions options = new GenerationOptions();
        options.setMethodName("create");
        options.setOperation(GenerationOptions.Operation.PERSIST);
        options.setParameterName("object");
        options.setParameterType("Object");
        options.setQueryAttribute("");
        options.setReturnType("Object");
        
        FileObject result = generate(FileUtil.toFileObject(testFile), options);
        
        assertFile(getGoldenFile("testGenWithExistingEM.pass"), FileUtil.toFile(result));
    }

    protected Class<? extends EntityManagerGenerationStrategy> getStrategyClass() {
        return ApplicationManagedResourceTransactionInjectableInEJB.class;
    }
}

