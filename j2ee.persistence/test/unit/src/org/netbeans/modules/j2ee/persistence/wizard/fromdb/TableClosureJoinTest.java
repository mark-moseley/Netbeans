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

package org.netbeans.modules.j2ee.persistence.wizard.fromdb;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import junit.framework.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.netbeans.api.progress.ProgressHandle;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Andrei Badea
 */
public class TableClosureJoinTest extends TestCase {

    private TableProviderImpl provider;
    private TableClosure closure;

    public TableClosureJoinTest(String testName) {
        super(testName);
    }

    public void setUp() {
        Map<String, Set<String>> tablesAndRefs = new HashMap<String, Set<String>>();
        Set<String> empty = Collections.emptySet();

        tablesAndRefs.put("BAR", empty);
        tablesAndRefs.put("FOO", empty);
        tablesAndRefs.put("ROOM", empty);
        tablesAndRefs.put("STUDENT", empty);
        tablesAndRefs.put("TEACHER", empty);
        tablesAndRefs.put("STUDENT_TEACHER", new HashSet(Arrays.asList(new String[] { "TEACHER", "STUDENT" })));
        tablesAndRefs.put("ROOM_STUDENT", new HashSet(Arrays.asList(new String[] { "STUDENT", "ROOM" })));
        tablesAndRefs.put("FOO_ROOM_STUDENT", new HashSet(Arrays.asList(new String[] { "FOO", "ROOM_STUDENT" })));

        provider = new TableProviderImpl(tablesAndRefs);
        closure = new TableClosure(provider);
    }

    public void tearDown() {
        closure = null;
    }

    public void testBasic() {
        closure.addTables(Collections.singleton(provider.getTableByName("STUDENT")));

        assertTables(new String[] { "STUDENT" }, closure.getWantedTables());
        assertTables(new String[] { }, closure.getReferencedTables());
        assertTables(new String[] { "STUDENT" }, closure.getSelectedTables());
        assertTables(new String[] { "BAR", "FOO", "FOO_ROOM_STUDENT", "ROOM", "ROOM_STUDENT", "STUDENT_TEACHER", "TEACHER" }, closure.getAvailableTables());

        closure.addTables(Collections.singleton(provider.getTableByName("TEACHER")));

        assertTables(new String[] { "STUDENT", "TEACHER" }, closure.getWantedTables());
        assertTables(new String[] { "STUDENT_TEACHER" }, closure.getReferencedTables());
        assertTables(new String[] { "STUDENT", "STUDENT_TEACHER", "TEACHER" }, closure.getSelectedTables());
        assertTables(new String[] { "BAR", "FOO", "FOO_ROOM_STUDENT", "ROOM", "ROOM_STUDENT" }, closure.getAvailableTables());

        closure.addTables(Collections.singleton(provider.getTableByName("ROOM")));

        assertTables(new String[] { "ROOM", "STUDENT", "TEACHER" }, closure.getWantedTables());
        assertTables(new String[] { "ROOM_STUDENT", "STUDENT_TEACHER" }, closure.getReferencedTables());
        assertTables(new String[] { "ROOM", "ROOM_STUDENT", "STUDENT", "STUDENT_TEACHER", "TEACHER" }, closure.getSelectedTables());
        assertTables(new String[] { "BAR", "FOO", "FOO_ROOM_STUDENT" }, closure.getAvailableTables());

        closure.removeTables(Collections.singleton(provider.getTableByName("STUDENT")));

        assertTables(new String[] { "ROOM", "TEACHER" }, closure.getWantedTables());
        assertTables(new String[] { }, closure.getReferencedTables());
        assertTables(new String[] { "ROOM", "TEACHER" }, closure.getSelectedTables());
        assertTables(new String[] { "BAR", "FOO", "FOO_ROOM_STUDENT", "ROOM_STUDENT", "STUDENT", "STUDENT_TEACHER" }, closure.getAvailableTables());

        closure.addAllTables();

        // assert adding all tables selects the tables based on the "normal" direction of the
        // relationships, not on the "inverse" direction, as it happens for join tables.
        // that is, the STUDENT and TEACHER tables are marked as referenced,
        // not STUDENT_TEACHER -- since the direction of the relationships is
        // STUDENT_TEACHER -> (TEACHER, STUDENT)
        assertTables(new String[] { "BAR", "FOO", "FOO_ROOM_STUDENT", "ROOM", "ROOM_STUDENT", "STUDENT", "STUDENT_TEACHER", "TEACHER" }, closure.getWantedTables());
        assertTables(new String[] { "FOO", "ROOM", "ROOM_STUDENT", "STUDENT", "TEACHER" }, closure.getReferencedTables());
        assertTables(new String[] { "BAR", "FOO", "FOO_ROOM_STUDENT", "ROOM", "ROOM_STUDENT", "STUDENT", "STUDENT_TEACHER", "TEACHER" }, closure.getSelectedTables());
        assertTables(new String[] { }, closure.getAvailableTables());

        closure.removeAllTables();

        assertTables(new String[] { }, closure.getWantedTables());
        assertTables(new String[] { }, closure.getReferencedTables());
        assertTables(new String[] { }, closure.getSelectedTables());
        assertTables(new String[] { "BAR", "FOO", "FOO_ROOM_STUDENT", "ROOM", "ROOM_STUDENT", "STUDENT", "STUDENT_TEACHER", "TEACHER" }, closure.getAvailableTables());
    }


    /**
     * Tests that adding a table which causes the addition of a join table which causes
     * the addition of another join table works. Here adding STUDENT causes
     * ROOM_STUDENT to be added, which causes FOO_ROOM_STUDENT to be added.
     */
    public void testRecursiveJoin() {
        closure.addTables(Collections.singleton(provider.getTableByName("ROOM")));
        closure.addTables(Collections.singleton(provider.getTableByName("FOO")));
        closure.addTables(Collections.singleton(provider.getTableByName("STUDENT")));

        assertTables(new String[] { "FOO", "ROOM", "STUDENT" }, closure.getWantedTables());
        assertTables(new String[] { "FOO_ROOM_STUDENT", "ROOM_STUDENT" }, closure.getReferencedTables());
        assertTables(new String[] { "FOO", "FOO_ROOM_STUDENT", "ROOM", "ROOM_STUDENT", "STUDENT" }, closure.getSelectedTables());
        assertTables(new String[] { "BAR", "STUDENT_TEACHER", "TEACHER" }, closure.getAvailableTables());
    }

    /**
     * Tests that adding and removing a table from a set containing a
     * related join table does not cause the reordering or the references (that is,
     * it is still the join table that is marked as referenced, not the tables it joins).
     */
    public void testRemoveDoesNotCauseReferenceReordering() {
        closure.addTables(Collections.singleton(provider.getTableByName("ROOM")));
        closure.addTables(Collections.singleton(provider.getTableByName("FOO")));
        closure.addTables(Collections.singleton(provider.getTableByName("STUDENT")));

        // the result is ok, tested in testRecursiveJoin()

        closure.addTables(Collections.singleton(provider.getTableByName("BAR")));

        assertTables(new String[] { "BAR", "FOO", "ROOM", "STUDENT" }, closure.getWantedTables());
        assertTables(new String[] { "FOO_ROOM_STUDENT", "ROOM_STUDENT" }, closure.getReferencedTables());
        assertTables(new String[] { "BAR", "FOO", "FOO_ROOM_STUDENT", "ROOM", "ROOM_STUDENT", "STUDENT" }, closure.getSelectedTables());
        assertTables(new String[] { "STUDENT_TEACHER", "TEACHER" }, closure.getAvailableTables());

        closure.removeTables(Collections.singleton(provider.getTableByName("BAR")));

        assertTables(new String[] { "FOO", "ROOM", "STUDENT" }, closure.getWantedTables());
        assertTables(new String[] { "FOO_ROOM_STUDENT", "ROOM_STUDENT" }, closure.getReferencedTables());
        assertTables(new String[] { "FOO", "FOO_ROOM_STUDENT", "ROOM", "ROOM_STUDENT", "STUDENT" }, closure.getSelectedTables());
        assertTables(new String[] { "BAR", "STUDENT_TEACHER", "TEACHER" }, closure.getAvailableTables());

        closure.addTables(Collections.singleton(provider.getTableByName("TEACHER")));

        assertTables(new String[] { "FOO", "ROOM", "STUDENT", "TEACHER" }, closure.getWantedTables());
        assertTables(new String[] { "FOO_ROOM_STUDENT", "ROOM_STUDENT", "STUDENT_TEACHER" }, closure.getReferencedTables());
        assertTables(new String[] { "FOO", "FOO_ROOM_STUDENT", "ROOM", "ROOM_STUDENT", "STUDENT", "STUDENT_TEACHER", "TEACHER" }, closure.getSelectedTables());
        assertTables(new String[] { "BAR" }, closure.getAvailableTables());

        closure.removeTables(Collections.singleton(provider.getTableByName("TEACHER")));

        assertTables(new String[] { "FOO", "ROOM", "STUDENT" }, closure.getWantedTables());
        assertTables(new String[] { "FOO_ROOM_STUDENT", "ROOM_STUDENT" }, closure.getReferencedTables());
        assertTables(new String[] { "FOO", "FOO_ROOM_STUDENT", "ROOM", "ROOM_STUDENT", "STUDENT" }, closure.getSelectedTables());
        assertTables(new String[] { "BAR", "STUDENT_TEACHER", "TEACHER" }, closure.getAvailableTables());
    }

    /**
     * Tests that an already selected table is never added as a join table. For example,
     * adding ROOM, STUDENT, STUDENT_TEACHER and TEACHER will add ROOM_STUDENT as a join
     * table, but should not mark STUDENT_TEACHER as a join (referenced) table.
     */
    public void testNeverAddingAlreadySelectedTablesAsJoinTables() {
        HashSet<Table> tables = new HashSet();
        tables.add(provider.getTableByName("ROOM"));
        tables.add(provider.getTableByName("STUDENT"));
        tables.add(provider.getTableByName("STUDENT_TEACHER"));
        tables.add(provider.getTableByName("TEACHER"));

        closure.addTables(tables);

        assertTables(new String[] { "ROOM", "STUDENT", "STUDENT_TEACHER", "TEACHER" }, closure.getWantedTables());
        assertTables(new String[] { "STUDENT", "TEACHER", "ROOM_STUDENT" }, closure.getReferencedTables());
        assertTables(new String[] { "ROOM", "ROOM_STUDENT", "STUDENT", "STUDENT_TEACHER", "TEACHER" }, closure.getSelectedTables());
        assertTables(new String[] { "BAR", "FOO", "FOO_ROOM_STUDENT" }, closure.getAvailableTables());
    }

    private void assertTables(String[] expected, Set<Table> actual) {
        assertEquals(expected.length, actual.size());
        for (String tableName : expected) {
            assertNotNull(actual.contains(tableName));
        }
    }
}
