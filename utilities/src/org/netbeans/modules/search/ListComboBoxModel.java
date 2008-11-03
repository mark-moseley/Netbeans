/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2007-2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.search;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.swing.ComboBoxModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import static javax.swing.event.ListDataEvent.CONTENTS_CHANGED;

/**
 * Unmodifiable {@code ComboBoxModel} built on a {@link java.util.List}
 * of elements.
 * This implementation is very simple and assumes that the passed list
 * of elements is not empty and is not modified during this model's lifetime.
 *
 * @author  Marian Petras
 */
final class ListComboBoxModel implements ComboBoxModel {

    private final List<? extends Object> elements;
    private final int maxIndex;
    private final boolean reverseOrder;
    private Object selectedItem;
    private Collection<ListDataListener> listeners;
    private ListDataEvent event
                          = new ListDataEvent(this, CONTENTS_CHANGED, -1, -1);

    public ListComboBoxModel(List<? extends Object> elements) {
        this(elements, false);
    }

    public ListComboBoxModel(List<? extends Object> elements,
                             final boolean reverseOrder) {
        if (elements == null) {
            throw new IllegalArgumentException(
                    "the list of elements must not be null");           //NOI18N
        }
        if (elements.isEmpty()) {
            throw new IllegalArgumentException(
                    "empty list of elements is not allowed");           //NOI18N
        }
        this.elements = elements;
        this.maxIndex = elements.size() - 1;
        this.reverseOrder = reverseOrder;
    }

    public void setSelectedItem(Object item) {
        if ((selectedItem != null) && !selectedItem.equals(item)
                || (selectedItem == null) && (item != null)) {
            this.selectedItem = item;
            fireSelectionChange();
        }
    }

    public Object getSelectedItem() {
        return selectedItem;
    }

    public int getSize() {
        return maxIndex + 1;
    }

    public Object getElementAt(int index) {
        return elements.get(reverseOrder ? maxIndex - index
                                         : index);
    }

    public void addListDataListener(ListDataListener l) {
        if (listeners == null) {
            listeners = new ArrayList<ListDataListener>(3);
            event = new ListDataEvent(this, CONTENTS_CHANGED, -1, -1);
        }
        listeners.add(l);
    }

    public void removeListDataListener(ListDataListener l) {
        if ((listeners != null) && listeners.remove(l) && listeners.isEmpty()) {
            listeners = null;
            event = null;
        }
    }

    private void fireSelectionChange() {
        if (listeners == null) {
            return;
        }

        for (ListDataListener l : listeners) {
            l.contentsChanged(event);
        }
    }

}
