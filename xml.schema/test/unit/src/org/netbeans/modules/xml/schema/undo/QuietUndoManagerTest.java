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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.xml.schema.undo;

import java.io.IOException;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import junit.framework.TestCase;
import org.netbeans.api.project.Project;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.xml.retriever.catalog.Utilities;
import org.netbeans.modules.xml.schema.SchemaDataObject;
import org.netbeans.modules.xml.schema.SchemaEditorSupport;
import org.netbeans.modules.xml.schema.Util;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.schema.model.SchemaModelFactory;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.ui.undo.QuietUndoManager;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;

/**
 * Tests the QuietUndoManager with the full data object/loader framework,
 * the purpose of which is to test using the CloneableEditorSupport to
 * catch errors that occur only when the editor support is in place.
 *
 * @author Nathan Fiedler
 */
public class QuietUndoManagerTest extends TestCase {
    public static final String XSD_PO = "PurchaseOrder.xsd";
    private Project project;
    private SchemaModel model;
    private FileObject purchaseOrderFO;
    private AbstractDocument document;
    private QuietUndoManager manager;

    public QuietUndoManagerTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        project = Util.createJavaTestProject();
        purchaseOrderFO = Util.populateProject(
                project, "resources/" + XSD_PO, "src/com/acme/schemas");
        ModelSource ms = Utilities.getModelSource(purchaseOrderFO, true);
        model = SchemaModelFactory.getDefault().getModel(ms);
    }

    @Override
    protected void tearDown() throws Exception {
        deleteModelFile(purchaseOrderFO);
    }
    
    private static void deleteModelFile(FileObject fo) throws Exception {
        if (fo == null) {
            return;
        }
        SchemaDataObject sdo = (SchemaDataObject) DataObject.find(fo);
        if (sdo != null) {
            SaveCookie save = sdo.getCookie(SaveCookie.class);
            if (save != null) {
                save.save();
            }
        }
        fo.delete();
    }

    /**
     * Ends the compound mode of the undo manager and discards all edits.
     */
    private void endTestCase() {
        manager.endCompound();
        document.removeUndoableEditListener(manager);
        if (model != null) {
            model.removeUndoableEditListener(manager);
        }
        manager.discardAllEdits();
    }

    /**
     * Simulate switching to the model view.
     */
    private void viewModel() {
        manager.endCompound();
        document.removeUndoableEditListener(manager);
        if (model != null) {
            try {
                // Sync any changes made to the document.
                model.sync();
            } catch (IOException ioe) {
                fail(ioe.toString());
            }
            // Ensure manager is not registered twice.
            model.removeUndoableEditListener(manager);
            model.addUndoableEditListener(manager);
        }
    }

    /**
     * Simulate switching to the source view.
     */
    private void viewSource() {
        if (model != null) {
            model.removeUndoableEditListener(manager);
        }
        // Ensure manager is not registered twice.
        document.removeUndoableEditListener(manager);
        document.addUndoableEditListener(manager);
        manager.beginCompound();
    }

    /**
     * Try to reproduce issue 83963.
     */
    public void testIssue83963() {
        try {
            SchemaDataObject sdo = (SchemaDataObject) DataObject.find(
                    purchaseOrderFO);
            SchemaEditorSupport ses = sdo.getSchemaEditorSupport();
            model = ses.getModel();
            manager = ses.getUndoManager();
        } catch (IOException ioe) {
            fail(ioe.toString());
        }

        assertNotNull("failed to acquire undo manager", manager);
        assertNotNull("failed to load SchemaModel", model);
        document = model.getModelSource().getLookup().
                lookup(AbstractDocument.class);
        assertNotNull("ModelSource did not contain AbstractDocument", document);
        // We are most interested in the issues that BaseDocument creates.
        assertTrue(document instanceof BaseDocument);

        // For some reason, SchemaEditorSupport.prepareDocument() is not run,
        // so we must do here what it would have done.
        document.removeUndoableEditListener(manager);
        manager.setDocument(document);

        String original = null;
        try {
            original = document.getText(0, document.getLength());
        } catch (BadLocationException ble) {
            fail(ble.toString());
        }
        viewModel();
        assertFalse(manager.canUndo());
        assertFalse(manager.canRedo());
        model.startTransaction();
        GlobalElement elem = model.getFactory().createGlobalElement();
        elem.setName("element83963");
        model.getSchema().addElement(elem);
        model.endTransaction();
        Schema schema = model.getSchema();
        boolean found = false;
        for (GlobalElement ge : schema.getElements()) {
            if (ge.getName().equals("element83963")) {
                found = true;
                break;
            }
        }
        assertTrue("failed to add global element", found);
        assertTrue(manager.canUndo());
        assertFalse(manager.canRedo());
        viewSource();
        assertTrue(manager.canUndo());
        assertFalse(manager.canRedo());
        try {
            int length = document.getLength();
            String text = document.getText(0, length);
            assertTrue(text.contains("element83963"));
            int mark = text.lastIndexOf("element83963");
            assertTrue(mark > 0 && mark < length);
            mark += "element83963\"/>".length();
            // Must create a document edit that overlaps the model edit,
            // and it must introduce a new DOM element (e.g. comment).
            // For example, the following two lines will not cause the error.
            //document.insertString(mark, "\n  \n", null);
            //assertEquals(length + 4, document.getLength());
            document.insertString(mark, "\n<!-- -->\n", null);
            assertEquals(length + 10, document.getLength());
        } catch (BadLocationException ble) {
            fail(ble.toString());
        }
        assertTrue(manager.canUndo());
        assertFalse(manager.canRedo());
        manager.undo();
        assertTrue(manager.canUndo());
        assertTrue(manager.canRedo());
        manager.undo();
        assertFalse(manager.canUndo());
        assertTrue(manager.canRedo());
        String actual = null;
        try {
            actual = document.getText(0, document.getLength());
        } catch (BadLocationException ble) {
            fail(ble.toString());
        }
        assertTrue(actual.equals(original));
        manager.redo();
        assertTrue(manager.canUndo());
        assertTrue(manager.canRedo());
// XXX: uncomment the redo() to get the error seen in issue 83963
//        manager.redo();
        assertTrue(manager.canUndo());
// XXX: Once the above redo() is working again, uncomment this code.
//        assertFalse(manager.canRedo());
        // Note that the 'xsd:' namespace is not used in this schema.
        String expected =
                "  <!-- etc. -->\n" +
                "    <element name=\"element83963\"/>\n" +
// XXX: Once the above redo() is working again, uncomment this code.
//                "<!-- -->\n" +
//                "\n" +
                "</schema>";
        try {
            actual = document.getText(0, document.getLength());
            assertTrue(actual.endsWith(expected));
        } catch (BadLocationException ble) {
            fail(ble.toString());
        }
        endTestCase();
    }
}
