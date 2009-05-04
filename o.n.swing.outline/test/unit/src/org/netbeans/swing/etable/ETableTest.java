/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
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
package org.netbeans.swing.etable;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.Properties;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import org.netbeans.junit.NbTestCase;

/**
 * Tests for class ETable.
 * @author David Strupl
 */
public class ETableTest extends NbTestCase {
    
    public ETableTest(String testName) {
        super(testName);
    }

    @Override
    protected boolean runInEQ () {
        return true;
    }

    /**
     * Test of isCellEditable method, of class org.netbeans.swing.etable.ETable.
     */
    public void testIsCellEditable() {
        System.out.println("testIsCellEditable");
        ETable t = createTestingTable(true);
        assertTrue("Should be editable according to the model", t.isCellEditable(0, 0));
        t.setFullyNonEditable(true);
        assertFalse("Should be non-editable when in fully non-ed mode" , t.isCellEditable(0, 0));
        t.setFullyEditable(true);
        assertTrue("Should be editable after fully editable", t.isCellEditable(0, 0));
    }

    /**
     * Test of convertRowIndexToModel method, of class org.netbeans.swing.etable.ETable.
     */
    public void testConvertRowIndexToModel() {
        System.out.println("testConvertRowIndexToModel");
        ETable t = createTestingTable(true);
        t.setQuickFilter(0, "b");
        assertEquals("Filter should hide rows", 3, t.convertRowIndexToModel(1));
        t.unsetQuickFilter();
        assertEquals("Unsetting filter should return original value", 1, t.convertRowIndexToModel(1));
        ETableColumnModel etcm = (ETableColumnModel)t.getColumnModel();
        ETableColumn etc = (ETableColumn)etcm.getColumn(3);
        etcm.toggleSortedColumn(etc, true);
        t.sortingPermutation = null; // because that is what we do after calling toggleSortedColumn
        assertEquals("Sort reorder (3) not ok", 3, t.convertRowIndexToModel(0));
        assertEquals("Sort reorder (4) not ok", 4, t.convertRowIndexToModel(5));
    }

    
    /**
     * Test of getFullyEditable method, of class org.netbeans.swing.etable.ETable.
     */
    public void testGetFullyEditable() {
        System.out.println("testGetFullyEditable");
        ETable t = createTestingTable(true);
        assertFalse("False after creation ", t.isFullyEditable());
        t.setFullyEditable(true);
        assertTrue("Should be editable after setting" , t.isFullyEditable());
        t.setFullyNonEditable(true);
        assertFalse("Should be false if fully non-editable", t.isFullyEditable());
    }

    /**
     * Test of getFullyNonEditable method, of class org.netbeans.swing.etable.ETable.
     */
    public void testGetFullyNonEditable() {
        System.out.println("testGetFullyNonEditable");
        
        ETable t = createTestingTable(true);
        assertFalse("False after creation ", t.isFullyNonEditable());
        t.setFullyNonEditable(true);
        assertTrue("Should be non-editable after setting" , t.isFullyNonEditable());
        t.setFullyEditable(true);
        assertFalse("Should be false if fully editable", t.isFullyNonEditable());
    }

    /**
     * Tests passing a QuickFilter object as a parameter to setQuickFilter method.
     */
    public void testSetQuickFilter() {
        System.out.println("testSetQuickFilter");
        ETable t = createTestingTable(true);
        QuickFilter quick = new QuickFilter() {
            public boolean accept(Object object) {
                return "x".equals(object);
            }
        };
        t.setQuickFilter(1, quick);
        assertEquals(1, t.getRowCount());
    }
    
    /**
     * Test of createDefaultColumnsFromModel method, of class org.netbeans.swing.etable.ETable.
     */
    public void testCreateDefaultColumnsFromModel() {
        System.out.println("testCreateDefaultColumnsFromModel");
        ETable t = createTestingTable(true);
        assertTrue("Should create ETableColumnModel", t.createDefaultColumnModel() instanceof ETableColumnModel);
    }

    /**
     * Test of createColumn method, of class org.netbeans.swing.etable.ETable.
     */
    public void testCreateColumn() {
        System.out.println("testCreateColumn");
        final boolean [] called = new boolean[1];
        ETable t = new ETable(1, 1) {
            public TableColumn createColumn(int index){ 
                TableColumn tc = super.createColumn(index);
                called[0] = tc instanceof ETableColumn;
                return tc;
            }
        };
        assertTrue("createColumn should have been called and returned correct type", called[0]);
    }

    /**
     * Test of createDefaultColumnModel method, of class org.netbeans.swing.etable.ETable.
     */
    public void testCreateDefaultColumnModel() {
        System.out.println("testCreateDefaultColumnModel");
        final boolean [] called = new boolean[1];
        ETable t = new ETable(1, 1) {
            protected TableColumnModel createDefaultColumnModel() {  
                TableColumnModel tcm = super.createDefaultColumnModel();
                called[0] = tcm instanceof ETableColumnModel;
                return tcm;
            }
        };
        assertTrue("createColumn should have been called and created correct type", called[0]);
    }

    /**
     * Test of getValueAt method, of class org.netbeans.swing.etable.ETable.
     */
    public void testGetValueAt() {
        System.out.println("testGetValueAt");
        System.out.println("testSetValueAt");
        ETable t = createTestingTable(true);
        t.setQuickFilter(0, "b");
        assertEquals("Filter should hide rows", t.getValueAt(1,1), t.getModel().getValueAt(3, 1));
        t.unsetQuickFilter();
        ETableColumnModel etcm = (ETableColumnModel)t.getColumnModel();
        ETableColumn etc = (ETableColumn)etcm.getColumn(3);
        etcm.toggleSortedColumn(etc, true);
        t.sortingPermutation = null; // because that is what we do after calling toggleSortedColumn
        assertEquals("Sort reorder (3) not ok", t.getValueAt(0,1), t.getModel().getValueAt(3, 1));
        assertEquals("Sort reorder (4) not ok", t.getValueAt(5,1), t.getModel().getValueAt(4, 1));
    }

    /**
     * Test of setValueAt method, of class org.netbeans.swing.etable.ETable.
     */
    public void testSetValueAt() {
        System.out.println("testSetValueAt");
        ETable t = createTestingTable(true);
        t.setQuickFilter(0, "b");
        t.setValueAt("ahoj", 1, 1);
        assertEquals("Filter should hide rows", "ahoj", t.getModel().getValueAt(3, 1));
        t.unsetQuickFilter();
        ETableColumnModel etcm = (ETableColumnModel)t.getColumnModel();
        ETableColumn etc = (ETableColumn)etcm.getColumn(3);
        etcm.toggleSortedColumn(etc, true);
        t.sortingPermutation = null; // because that is what we do after calling toggleSortedColumn
        t.setValueAt("ahoj1", 0, 1);
        t.setValueAt("ahoj2", 5, 1);
        assertEquals("Sort reorder (3) not ok", "ahoj1", t.getModel().getValueAt(3, 1));
        assertEquals("Sort reorder (4) not ok", "ahoj2", t.getModel().getValueAt(4, 1));
    }

    /**
     * Test of getRowCount method, of class org.netbeans.swing.etable.ETable.
     */
    public void testGetRowCount() {
        System.out.println("testGetRowCount");
        
        ETable t = createTestingTable(true);
        t.setQuickFilter(0, "b");
        assertEquals("Filter should hide rows", 2, t.getRowCount());
    }

    /**
     * Test of setModel method, of class org.netbeans.swing.etable.ETable.
     */
    public void testSetModel() {
        System.out.println("testSetModel");
        ETable t = createTestingTable(true);
        t.setQuickFilter(0, "b");
        t.setModel(new DefaultTableModel(100, 100));
        assertEquals("row count should be according to the new model", 100, t.getRowCount());
    }

    /**
     * Test of initializeLocalVars method, of class org.netbeans.swing.etable.ETable.
     */
    public void testInitializeLocalVars() {
        System.out.println("testInitializeLocalVars");
        ETable t = createTestingTable(true);
        for (int i = 0; i < t.getColumnCount(); i++) {
            int pw = t.getColumnModel().getColumn(i).getPreferredWidth();
            if ((pw == 0) || (pw == 75)) { // the default values
                fail("PreferredWidth is " + pw);
            }
        }
    }

    /**
     * Test of processKeyBinding method, of class org.netbeans.swing.etable.ETable.
     */
    public void testProcessKeyBinding() {
        System.out.println("testProcessKeyBinding");
        final boolean []called = new boolean[1];
        ETable t = new ETable() {
            void updatePreferredWidths() {
                super.updatePreferredWidths();
                called[0] = true;
            }
        };
        KeyEvent ke = new KeyEvent(t, 0, System.currentTimeMillis(), InputEvent.CTRL_MASK, 0, '+');
        t.processKeyBinding(null, ke, 0, true);
        assertTrue("update pref size not called", called[0]);
    }

    /**
     * Test of readSettings and writeSettings methods, 
     * of class org.netbeans.swing.etable.ETable.
     */
    public void testWriteReadSettings() {
        System.out.println("testWriteReadSettings");
        ETable t = createTestingTable(false);
        ETableColumnModel etcm = (ETableColumnModel) t.getColumnModel();
        ETableColumn etc = (ETableColumn)etcm.getColumn(3);
        etcm.setColumnHidden(etcm.getColumn(0), true);
        etcm.toggleSortedColumn(etc, true);

        assertEquals("One column should be hidden", 3, t.getColumnCount());
        assertEquals("Sort reorder (3) not ok", 3, t.convertRowIndexToModel(0));
        assertEquals("Sort reorder (4) not ok", 4, t.convertRowIndexToModel(5));
        assertEquals("Sort reorder (3) not ok", t.getValueAt(0, 0), t.getModel().getValueAt(3, 1));
        assertEquals("Sort reorder (4) not ok", t.getValueAt(5, 0), t.getModel().getValueAt(4, 1));
        
        Properties p = new Properties();
        t.writeSettings(p, "blabla");
        
        ETable t2 = createTestingTable(false);
        t2.readSettings(p, "blabla");
        
        assertEquals("One column should be hidden", 3, t2.getColumnCount());
        assertEquals("Sort reorder (3) not ok", 3, t2.convertRowIndexToModel(0));
        assertEquals("Sort reorder (4) not ok", 4, t2.convertRowIndexToModel(5));
        assertEquals("Sort reorder (3) not ok", t2.getValueAt(0, 0), t2.getModel().getValueAt(3, 1));
        assertEquals("Sort reorder (4) not ok", t2.getValueAt(5, 0), t2.getModel().getValueAt(4, 1));
    }
    
    public void testTableColumnSelector() {
        ETable t = createTestingTable(false);
        t.setColumnSelector(new TableColumnSelector() {
            public String[] selectVisibleColumns(TableColumnSelector.TreeNode root, String[] selected) {
                return new String[] { "DD", "BB", "AA" };
            }
            public String[] selectVisibleColumns(String[] available, String[] selected) {
                return new String[] { "CC" };
            }
        });
        ColumnSelectionPanel.showColumnSelectionDialog(t);
        assertEquals(1, t.getColumnCount());
        ETableColumnModel etcm = (ETableColumnModel) t.getColumnModel();
        etcm.setColumnHierarchyRoot(new TableColumnSelector.TreeNode() {
            public String getText() {
                return "Root";
            }

            public boolean isLeaf() {
                return false;
            }

            public TableColumnSelector.TreeNode[] getChildren() {
                return new TableColumnSelector.TreeNode[0];
            }
        } );
        ColumnSelectionPanel.showColumnSelectionDialog(t);
        assertEquals(3, t.getColumnCount());
        // The columns should be also re-sorted:
        assertEquals("DD", t.getColumnModel().getColumn(0).getIdentifier());
        assertEquals("BB", t.getColumnModel().getColumn(1).getIdentifier());
        assertEquals("AA", t.getColumnModel().getColumn(2).getIdentifier());
    }
    
    public void testDefaultTableColumnSelector() {
        ETable t = createTestingTable(false);
        ETable.setDefaultColumnSelector(new TableColumnSelector() {
            public String[] selectVisibleColumns(TableColumnSelector.TreeNode root, String[] selected) {
                return new String[] { "AA", "BB" };
            }
            public String[] selectVisibleColumns(String[] available, String[] selected) {
                return new String[] { "CC" };
            }
        });
        ColumnSelectionPanel.showColumnSelectionDialog(t);
        assertEquals(1, t.getColumnCount());
        ETableColumnModel etcm = (ETableColumnModel) t.getColumnModel();
        etcm.setColumnHierarchyRoot(new TableColumnSelector.TreeNode() {
            public String getText() {
                return "Root";
            }

            public boolean isLeaf() {
                return false;
            }

            public TableColumnSelector.TreeNode[] getChildren() {
                return new TableColumnSelector.TreeNode[0];
            }
        } );
        ColumnSelectionPanel.showColumnSelectionDialog(t);
        assertEquals(2, t.getColumnCount());
    }
    
    /**
     * Create a test ETable instance with some dummy data. BUT please
     * be aware that the tests result depend on this data so if you do
     * any change here make sure you fix all the tests.
     */
    private ETable createTestingTable(final boolean cellsEditable) {
        ETable eTable1 = new ETable();
        eTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {"a", "x", "tttttttt", new Integer(5)},
                {"a", "y", "ggggggggg", new Integer(10)},
                {"b", "z", "nnnnnnnn", new Integer(7)},
                {"b", "w", "mmmmmm", new Integer(1)},
                {"c", "m", "kkkkkkkkkk", new Integer(10000)},
                {"c", "n", "kkkkk", new Integer(4)}
            },
            new String [] { "AA", "BB", "CC", "DD"}
        ) {
            Class[] types = new Class [] {
                java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Integer.class
            };
            boolean[] canEdit = new boolean [] {
                cellsEditable, cellsEditable, cellsEditable, cellsEditable
            };
            
            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
            
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        return eTable1;
    }
}
