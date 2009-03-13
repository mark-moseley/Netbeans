
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.cnd.makeproject.api.configurations;

import java.util.ArrayList;
import java.util.List;

public class VectorConfiguration<E> {

    private VectorConfiguration<E> master;
    private List<E> value;
    private boolean dirty = false;

    public VectorConfiguration(VectorConfiguration<E> master) {
        this.master = master;
        value = new ArrayList<E>(0);
        reset();
    }

    public VectorConfiguration<E> getMaster() {
        return master;
    }

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    public boolean getDirty() {
        return dirty;
    }

    public void add(E o) {
        getValue().add(o);
    }

    public void setValue(List<E> l) {
        this.value = l;
    }

    public List<E> getValue() {
        return value;
    /*
    if (master != null && !getModified())
    return master.getValue();
    else
    return value;
     */
    }

    public boolean getModified() {
        return value.size() != 0;
    }

    public void reset() {
        //value.removeAll(); // FIXUP
        value = new ArrayList<E>();
    }

    // Clone and Assign
    public void assign(VectorConfiguration<E> conf) {
        setDirty(!this.equals(conf));
        reset();
        getValue().addAll(conf.getValue());
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof VectorConfiguration)) {
            return false;
        }
        VectorConfiguration conf = (VectorConfiguration)obj;
        boolean eq = true;
        if (getValue().size() != conf.getValue().size()) {
            eq = false;
        } else {
            for (int i = 0; i < getValue().size(); i++) {
                if (!getValue().get(i).equals(conf.getValue().get(i))) {
                    eq = false;
                    break;
                }
            }
        }
        return eq;
    }

    @Override
    public VectorConfiguration<E> clone() {
        VectorConfiguration<E> clone = new VectorConfiguration<E>(master);
        clone.setValue(new ArrayList<E>(getValue()));
        return clone;
    }

    /**
     * Converts each element of the vector to <code>String</code>
     * and concatenates the results into a single <code>String</code>.
     * Elements are separated with spaces.
     *
     * @param visitor  will be used to convert each element to <code>String</code>
     * @return concatenated <code>String</code>
     */
    public String toString(ToString<E> visitor) {
        StringBuilder buf = new StringBuilder();
        List<E> list = getValue();
        for (E item : list) {
            String s = visitor.toString(item);
            if (s != null && 0 < s.length()) {
                buf.append(s).append(' '); // NOI18N
            }
        }
        return buf.toString();
    }

    /**
     * Used to convert vector elements to <code>String</code>.
     * See {@link VectorConfiguration#toString(ToString)}.
     *
     * @param <E> vector element type
     */
    public static interface ToString<E> {
        String toString(E item);
    }

}
