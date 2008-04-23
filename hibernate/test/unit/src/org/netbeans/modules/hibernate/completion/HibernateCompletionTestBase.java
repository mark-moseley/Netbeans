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

package org.netbeans.modules.hibernate.completion;

import java.util.ArrayList;
import java.util.List;
import javax.swing.text.Document;
import javax.tools.FileObject;
import org.hibernate.cfg.Environment;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.xml.lexer.XMLTokenId;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.editor.completion.CompletionProvider;

/**
 *
 * @author Dongmei Cao
 */
public class HibernateCompletionTestBase  extends NbTestCase{
      
    protected String instanceResourcePath;
    protected FileObject instanceFileObject;
    protected Document instanceDocument;
    
    public HibernateCompletionTestBase(String name) {
        super(name);
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    @Override
    public void setUp() {
       
    }

    @After
    @Override
    public void tearDown() {
    }
    
    protected void setupCompletion(String path, StringBuffer buffer) throws Exception {
        this.instanceResourcePath = path;
        this.instanceDocument = Util.getResourceAsDocument(path);
        if(buffer != null) {
            instanceDocument.remove(0, instanceDocument.getLength());
            instanceDocument.insertString(0, buffer.toString(), null);
        }
        instanceDocument.putProperty(Language.class, XMLTokenId.language());        
    }
 
    protected void assertResult(List<HibernateCompletionItem> result,
            String[] expectedResult) {
        
        assertNotNull(result);
        assertNotNull(expectedResult);
            
        assert(result.size() == expectedResult.length);
        
        List<String> resultItemNames = new ArrayList<String>();
        for(HibernateCompletionItem item : result) {
            //System.out.println( "-----" + item.getDisplayText());
            resultItemNames.add(item.getDisplayText());
        }
        
        for(int i = 0; i < expectedResult.length; i ++) {
            boolean found = true;
            if(!resultItemNames.contains(expectedResult[i])) {
                found = false;
            }
            assertTrue("Not found " + expectedResult[i], found);
        }
    }
}