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

/*
 * KeyRefTest.java
 *
 * Created on November 6, 2005, 9:02 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.schema.model;

import java.util.Collection;
import junit.framework.TestCase;

/**
 *
 * @author rico
 */
public class KeyRefTest extends TestCase{
    public static final String TEST_XSD = "resources/KeyRef.xsd";
    
    /** Creates a new instance of KeyRefTest */
    public KeyRefTest(String testcase) {
        super(testcase);
    }
    
    Schema schema = null;
    protected void setUp() throws Exception {
        SchemaModel model = Util.loadSchemaModel(TEST_XSD);
        schema = model.getSchema();
    }
    
    protected void tearDown() throws Exception {
        TestCatalogModel.getDefault().clearDocumentPool();
    }
    
    public void testKeyRef(){
        Collection<GlobalElement> elements = schema.getElements();
        GlobalElement elem = elements.iterator().next();
        LocalType localType = elem.getInlineType();
        assertTrue("localType instanceof LocalComplexType",
                     localType instanceof LocalComplexType);
        LocalComplexType lct = (LocalComplexType)localType;
        ComplexTypeDefinition ctd = lct.getDefinition();
        assertTrue("ComplextTypeDefinition instanceof Sequence",
                    ctd instanceof Sequence);
        Sequence seq = (Sequence)ctd;
        java.util.List <SequenceDefinition> seqDefs = seq.getContent();
        SequenceDefinition seqDef = seqDefs.iterator().next();
        assertTrue("SequenceDefinition instanceof LocalElement",
                seqDef instanceof LocalElement);
        LocalElement le = (LocalElement)seqDef;  
        Collection<Constraint> constraints = le.getConstraints();
        Constraint constraint = constraints.iterator().next();
        assertTrue("Constraint instanceof KeyRef", constraint instanceof KeyRef);
        KeyRef keyRef = (KeyRef)constraint;
        Constraint key = keyRef.getReferer();
        System.out.println("key: " + key.getName());
        assertEquals("Referred key", "pNumKey", key.getName());
    }
}
