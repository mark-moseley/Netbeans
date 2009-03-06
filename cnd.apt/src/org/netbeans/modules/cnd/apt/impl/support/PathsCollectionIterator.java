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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.apt.impl.support;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * iterator which encapsulates two lists ans start index of combined collection
 * @author Vladimir Voskresensky
 */
class PathsCollectionIterator implements Iterator<CharSequence> {
    private final List<CharSequence> col1;
    private final List<CharSequence> col2;
    private int startIndex;
    
    public PathsCollectionIterator(List<CharSequence> col1, List<CharSequence> col2, int startIndex) {
        this.col1 = col1;
        this.col2 = col2;
        this.startIndex = startIndex;
    }

    public boolean hasNext() {
        return startIndex < col1.size() + col2.size();
    }

    public CharSequence next() {
        if (hasNext()) {
            int index = startIndex++;
            if (index < col1.size()) {
                return col1.get(index);
            } else {
                return col2.get(index - col1.size());
            }
        } else {
            throw new NoSuchElementException();
        }
    }

    public void remove() {
        throw new UnsupportedOperationException("Not supported."); // NOI18N
    }
}
