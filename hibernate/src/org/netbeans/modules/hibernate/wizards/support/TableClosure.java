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

package org.netbeans.modules.hibernate.wizards.support;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.swing.event.ChangeListener;
import org.openide.util.ChangeSupport;

/**
 *
 * @author Andrei Badea, gowri
 */
public class TableClosure {

    // XXX Javadoc

    private final Set<Table> tables;

    private final Set<Table> availableTables = new HashSet<Table>();
    private final Set<Table> wantedTables = new HashSet<Table>();
    private final Set<Table> selectedTables = new HashSet<Table>();
    private final Set<Table> referencedTables = new HashSet<Table>();

    // just for performance reasons
    private final Set<Table> unmodifAvailableTables = Collections.unmodifiableSet(availableTables);
    private final Set<Table> unmodifWantedTables = Collections.unmodifiableSet(wantedTables);
    private final Set<Table> unmodifSelectedTables = Collections.unmodifiableSet(selectedTables);
    private final Set<Table> unmodifReferencedTables = Collections.unmodifiableSet(referencedTables);

    private boolean closureEnabled = true;

    private final ChangeSupport changeSupport = new ChangeSupport(this);

    public TableClosure(TableProvider tableProvider) {
        tables = tableProvider.getTables();
        availableTables.addAll(tables);
    }

    public void addChangeListener(ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }

    public void removeChangeListener(ChangeListener listener) {
        changeSupport.removeChangeListener(listener);
    }

    public Set<Table> getAvailableTables() {
        return unmodifAvailableTables;
    }

    public Set<Table> getSelectedTables() {
        return unmodifSelectedTables;
    }

    public Set<Table> getReferencedTables() {
        return unmodifReferencedTables;
    }

    /**
     * Static because of the tests.
     */
    Set<Table> getWantedTables() {
        return unmodifWantedTables;
    }

    public void addTables(Set<Table> tables) {
        if (!canAddAllTables(tables)) {
            return;
        }
        if (closureEnabled) {
            if (wantedTables.addAll(tables)) {
                Set<Table> refTables = removeDisabledTables(getReferencedTablesTransitively(tables));
                Set<Table> addedTables = new HashSet<Table>(tables);
                addedTables.addAll(refTables);

                selectedTables.addAll(addedTables);
                referencedTables.addAll(refTables);
                availableTables.removeAll(addedTables);

                Set<Table> joinTables = removeDisabledTables(getJoinTablesTransitively(addedTables));
                // if some of the join tables are already selected, we don't want
                // to add them once more and especially we want to avoid adding
                // already selected tables as referenced tables -- that would be incorrect
                joinTables.removeAll(selectedTables);
                selectedTables.addAll(joinTables);

                referencedTables.addAll(joinTables);
                availableTables.removeAll(joinTables);

                changeSupport.fireChange();
            }
        } else {
            wantedTables.addAll(tables);
            selectedTables.addAll(tables);
            availableTables.removeAll(tables);

            changeSupport.fireChange();
        }
    }

    public void removeTables(Set<Table> tables) {
        if (!canRemoveAllTables(tables)) {
            return;
        }

        if (closureEnabled) {
            if (wantedTables.removeAll(tables)) {
                redoClosure();

                changeSupport.fireChange();
            }
        } else {
            wantedTables.removeAll(tables);
            selectedTables.removeAll(tables);
            availableTables.addAll(tables);

            changeSupport.fireChange();
        }
    }

    public void addAllTables() {
        wantedTables.clear();
        for (Table table : tables) {
            if (!table.isDisabled()) {
                wantedTables.add(table);
            }
        }

        if (closureEnabled) {
            redoClosure();

            changeSupport.fireChange();
        } else {
            selectedTables.addAll(wantedTables);
            availableTables.addAll(tables);
            availableTables.removeAll(wantedTables);

            changeSupport.fireChange();
        }
    }

    public void removeAllTables() {
        wantedTables.clear();
        selectedTables.clear();
        referencedTables.clear();
        availableTables.addAll(tables);

        changeSupport.fireChange();
    }

    public boolean getClosureEnabled() {
        return closureEnabled;
    }

    public void setClosureEnabled(boolean closureEnabled) {
        if (this.closureEnabled == closureEnabled) {
            return;
        }

        this.closureEnabled = closureEnabled;

        if (closureEnabled) {
            redoClosure();
        } else {
            selectedTables.clear();
            selectedTables.addAll(wantedTables);

            referencedTables.clear();

            availableTables.addAll(tables);
            availableTables.removeAll(wantedTables);
        }

        changeSupport.fireChange();
    }

    public boolean canAddAllTables(Set<Table> tables) {
        if (tables.size() <= 0) {
            return false;
        }
        // can't add disabled tables
        for (Table table : tables) {
            if (table.isDisabled()) {
                return false;
            }
        }
        return true;
    }

    public boolean canAddSomeTables(Set<Table> tables) {
        if (tables.size() <= 0) {
            return false;
        }
        for (Table table : tables) {
            if (!table.isDisabled()) {
                return true;
            }
        }
        return false;
    }

    public boolean canRemoveAllTables(Set<Table> tables) {
        if (tables.size() <= 0) {
            return false;
        }

        // no need to check for disabled tables, as there can not be
        // disabled tables in selectedTables

        if (!closureEnabled) {
            return true;
        }
        // can't remove referenced tables
        // PENDING but we could support removing a table which is
        // only referenced by another removed table
        for (Table table : tables) {
            if (referencedTables.contains(table)) {
                return false;
            }
        }
        return true;
    }

    private void redoClosure() {
        referencedTables.clear();
        referencedTables.addAll(removeDisabledTables(getReferencedTablesTransitively(wantedTables)));

        selectedTables.clear();
        selectedTables.addAll(wantedTables);
        selectedTables.addAll(referencedTables);

        Set<Table> joinTables = removeDisabledTables(getJoinTablesTransitively(selectedTables));
        // avoid adding already selected tables as join tables
        joinTables.removeAll(selectedTables);

        selectedTables.addAll(joinTables);
        referencedTables.addAll(joinTables);

        availableTables.clear();
        availableTables.addAll(tables);
        availableTables.removeAll(selectedTables);
    }

    /**
     * Returns the tables transitively referenced by the contents of the tables parameter
     * (not including tables passed in this parameter). If a table references itself,
     * it is not added to the result.
     */
    private Set<Table> getReferencedTablesTransitively(Set<Table> tables) {
        Queue<Table> tableQueue = new Queue<Table>(tables);
        Set<Table> referencedTables = new HashSet<Table>();

        while (!tableQueue.isEmpty()) {
            Table table = tableQueue.poll();

            for (Table referencedTable : table.getReferencedTables()) {
                if (!referencedTable.equals(table)) {
                    referencedTables.add(referencedTable);
                }
                tableQueue.offer(referencedTable);
            }
        }

        return referencedTables;
    }

    private Set<Table> getJoinTablesTransitively(Set<Table> tables) {
        Queue<Table> tableQueue = new Queue<Table>(tables);
        Set<Table> joinTables = new HashSet<Table>();

        while (!tableQueue.isEmpty()) {
            Table table = tableQueue.poll();

            for (Table joinTable : table.getJoinTables()) {
                if (areReferencedTablesSelected(joinTable, joinTables)) {
                    joinTables.add(joinTable);
                    tableQueue.offer(joinTable);
                }
            }
        }

        return joinTables;
    }

    /**
     * Returns true if all tables referenced by tableName are in {@link selectedTables} or
     * the additionalTables parameter. The additionalTables parameter is necessary
     * because it contains the already added join tables, which are not yet in selectedTables,
     * but will eventually become part of it.
     */
    private boolean areReferencedTablesSelected(Table table, Set<Table> additionalTables) {
        for (Table referencedTable : table.getReferencedTables()) {
            if (!selectedTables.contains(referencedTable) && !additionalTables.contains(referencedTable)) {
                return false;
            }
        }
        return true;
    }

    private static Set<Table> removeDisabledTables(Set<Table> tables) {
        for (Iterator<Table> i = tables.iterator(); i.hasNext();) {
            if (i.next().isDisabled()) {
                i.remove();
            }
        }
        return tables;
    }

    /**
     * A simple queue. An object can only be added once, even
     * if it has already been removed from the queue. This class could implement
     * the {@link java.util.Queue} interface, but it doesn't because that
     * interface has too many unneeded methods. Not private because of the tests.
     */
    static final class Queue<T> {

        /**
         * The queue. Implemented as ArrayList since will be iterated using get().
         */
        private final List<T> queue;

        /**
         * The contents of the queue, needed in order to quickly (ideally
         * in a constant time) tell if a table has been already added.
         */
        private final Set<T> contents;

        /**
         * The position in the queue.
         */
        private int currentIndex;

        /**
         * Creates a queue with an initial contents.
         */
        public Queue(Set<T> initialContents) {
            assert !initialContents.contains(null);

            queue = new ArrayList(initialContents);
            contents = new HashSet(initialContents);
        }

        /**
         * Adds an elements to the queue if it hasn't been already added.
         */
        public void offer(T element) {
            assert element != null;

            if (!contents.contains(element)) {
                contents.add(element);
                queue.add(element);
            }
        }

        /**
         * Returns true if the queue is empty.
         */
        public boolean isEmpty() {
            return currentIndex >= queue.size();
        }

        /**
         * Returns and removes the elements at the top of the queue or null if
         * the queue is empty.
         */
        public T poll() {
            T result = null;
            if (!isEmpty()) {
                result = queue.get(currentIndex);
                currentIndex++;
            }
            return result;
        }
    }
}
