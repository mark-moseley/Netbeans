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
 * MergeTest.java
 * JUnit based test
 *
 * Created on October 28, 2005, 3:40 PM
 */

package org.netbeans.modules.xml.schema.model.impl.xdm;

import java.util.ArrayList;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.undo.CompoundEdit;
import javax.swing.undo.UndoManager;
import junit.framework.*;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.xml.schema.model.*;
import org.netbeans.modules.xml.xam.ComponentEvent;
import org.netbeans.modules.xml.xam.ComponentListener;
import org.netbeans.modules.xml.xam.dom.DocumentComponent;

/**
 *
 * @author Ayub Khan
 */
public class UndoRedoTest extends TestCase {
    
    public UndoRedoTest(String testName) {
        super(testName);
    }
    @Override
            protected void setUp() throws Exception {
        
    }
    
    @Override
            protected void tearDown() {
        TestCatalogModel.getDefault().clearDocumentPool();
    }
    
    class TestComponentListener implements ComponentListener {
        ArrayList<ComponentEvent> accu = new ArrayList<ComponentEvent>();
        public void valueChanged(ComponentEvent evt) {
            accu.add(evt);
        }
        public void childrenAdded(ComponentEvent evt) {
            accu.add(evt);
        }
        public void childrenDeleted(ComponentEvent evt) {
            accu.add(evt);
        }
        public void reset() { accu.clear(); }
        public int getEventCount() { return accu.size(); }
        public java.util.List<ComponentEvent> getEvents() { return accu; }
        
        private void assertEvent(ComponentEvent.EventType type, DocumentComponent source) {
            for (ComponentEvent e : accu) {
                if (e.getEventType().equals(type) &&
                        e.getSource() == source) {
                    return;
                }
            }
            assertTrue("Expect component change event " + type +" on source " + source +
                    ". Instead received: " + accu, false);
        }
    }
    
    public void testIssue83963() throws Exception {
        SchemaModel model = Util.loadSchemaModel("resources/undoredo.xsd");
        BaseDocument doc = (BaseDocument) model.getModelSource().
                getLookup().lookup(BaseDocument.class);
        Schema s = model.getSchema();
        TestComponentListener listener = new TestComponentListener();
        model.addComponentListener(listener);
        UndoManager ur = new UndoManager();
        model.addUndoableEditListener(ur);
        
        String original = doc.getText(0, doc.getLength());
        //System.out.println("doc before add ComplexType"+doc.getText(0, doc.getLength()));
        GlobalComplexType gct = model.getFactory().createGlobalComplexType();
        model.startTransaction();
        s.addComplexType(gct);
        model.endTransaction();
        model.removeUndoableEditListener(ur);
        doc.addUndoableEditListener(ur);
        
        //System.out.println("doc after add ComplexType"+doc.getText(0, doc.getLength()));
        
        String stStr = "   <xsd:simpleType name=\"lend\">\n     <xsd:list>\n       <xsd:simpleType>\n         <xsd:restriction base=\"xsd:string\"/>\n       </xsd:simpleType>\n     </xsd:list>\n   </xsd:simpleType>";
        
        String afterInsert = doc.getText(0, doc.getLength());
        //System.out.println("doc after insert simpleType"+doc.getText(290, 10));
        doc.insertString(290, "\n", null);
        model.sync();
        doc.insertString(291, stStr, null);
        model.sync();
        
        //System.out.println("doc after insert simpleType"+doc.getText(0, doc.getLength()));
        ur.undo();
        //System.out.println("doc after first undo"+doc.getText(0, doc.getLength()));
        ur.undo();
        assertEquals(afterInsert,doc.getText(0, doc.getLength()));
        //System.out.println("doc after second undo"+doc.getText(0, doc.getLength()));
        ur.undo();
        //System.out.println("doc after third undo"+doc.getText(0, doc.getLength()));
        assertEquals(original, doc.getText(0, doc.getLength()));
        
        ur.redo();
        assertEquals(afterInsert,doc.getText(0, doc.getLength()));
        //System.out.println("doc after first redo"+doc.getText(0, doc.getLength()));
        ur.redo();
        //System.out.println("doc after second redo"+doc.getText(0, doc.getLength()));
        ur.redo();
        //System.out.println("doc after third redo"+doc.getText(0, doc.getLength()));
    }
    
    public void testIssue83963_1() throws Exception {
        SchemaModel model = Util.loadSchemaModel("resources/undoredo.xsd");
        BaseDocument doc = (BaseDocument) model.getModelSource().
                getLookup().lookup(BaseDocument.class);
        Schema s = model.getSchema();
        TestComponentListener listener = new TestComponentListener();
        model.addComponentListener(listener);
        UndoManager ur = new UndoManager();
        model.addUndoableEditListener(ur);
        doc.removeUndoableEditListener(ur);
        
        //System.out.println("doc before add ComplexType"+doc.getText(0, doc.getLength()));
        GlobalComplexType gct = model.getFactory().createGlobalComplexType();
        doc.insertString(271, "<complexType/>",null);
        
        //System.out.println("doc after add ComplexType"+doc.getText(0, doc.getLength()));
        
        String stStr = "   <xsd:simpleType name=\"lend\">\n     <xsd:list>\n       <xsd:simpleType>\n         <xsd:restriction base=\"xsd:string\"/>\n       </xsd:simpleType>\n     </xsd:list>\n   </xsd:simpleType>";
        model.sync();

        doc.insertString(285, "\n", null);
        model.sync();
        doc.insertString(286, stStr, null);
        model.sync();

        //System.out.println("doc after insert simpleType"+doc.getText(0, doc.getLength()));
        ur.undo();
        //System.out.println("doc after first undo"+doc.getText(0, doc.getLength()));
        ur.undo();
        //System.out.println("doc after second undo"+doc.getText(0, doc.getLength()));
        ur.undo();
        //System.out.println("doc after third undo"+doc.getText(0, doc.getLength()));
        
        ur.redo();
        //System.out.println("doc after first redo"+doc.getText(0, doc.getLength()));
        ur.redo();
        //System.out.println("doc after second redo"+doc.getText(0, doc.getLength()));
        ur.redo();
        //System.out.println("doc after third redo"+doc.getText(0, doc.getLength()));
    }
    private SchemaModel model;
    
}
