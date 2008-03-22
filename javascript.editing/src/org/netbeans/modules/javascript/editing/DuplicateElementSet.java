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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.javascript.editing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * A "set" which deliberately violates the Set contract - it's used to include duplicates that
 * match on object equality
 * 
 * @author Tor Norbye
 */
public class DuplicateElementSet implements Set<IndexedElement> {
    private List<IndexedElement> elements = new ArrayList<IndexedElement>();

    public DuplicateElementSet() {
    }

    public int size() {
        return elements.size();
    }

    public boolean isEmpty() {
        return elements.isEmpty();
    }

    public boolean contains(Object o) {
        return elements.contains(o);
    }

    public Iterator<IndexedElement> iterator() {
        return elements.iterator();
    }

    public Object[] toArray() {
        return elements.toArray();
    }

    public <T> T[] toArray(T[] a) {
        return elements.toArray(a);
    }

    public boolean add(IndexedElement o) {
        return elements.add(o);
    }

    public boolean remove(Object o) {
        return elements.remove(o);
    }

    public boolean containsAll(Collection<?> c) {
        return elements.containsAll(c);
    }

    public boolean addAll(Collection<? extends IndexedElement> c) {
        return elements.addAll(c);
    }

    public boolean retainAll(Collection<?> c) {
        return elements.retainAll(c);
    }

    public boolean removeAll(Collection<?> c) {
        return elements.removeAll(c);
    }

    public void clear() {
        elements.clear();
    }
}
