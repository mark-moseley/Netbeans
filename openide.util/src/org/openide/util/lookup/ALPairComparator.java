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
package org.openide.util.lookup;

import java.util.Comparator;
import org.openide.util.lookup.AbstractLookup.Pair;


/** Implementation of comparator for AbstractLookup.Pair
 *
 * @author  Jaroslav Tulach
 */
final class ALPairComparator implements Comparator<Pair<?>> {
    public static final Comparator<Pair<?>> DEFAULT = new ALPairComparator();

    /** Creates a new instance of ALPairComparator */
    private ALPairComparator() {
    }

    /** Compares two items.
    */
    public int compare(Pair<?> i1, Pair<?> i2) {
        int result = i1.getIndex() - i2.getIndex();

        if (result == 0) {
            if (i1 != i2) {
                java.io.ByteArrayOutputStream bs = new java.io.ByteArrayOutputStream();
                java.io.PrintStream ps = new java.io.PrintStream(bs);

                ps.println(
                    "Duplicate pair in tree" + // NOI18N
                    "Pair1: " + i1 + " pair2: " + i2 + " index1: " + i1.getIndex() + " index2: " +
                    i2.getIndex() // NOI18N
                     +" item1: " + i1.getInstance() + " item2: " + i2.getInstance() // NOI18N
                     +" id1: " + Integer.toHexString(System.identityHashCode(i1)) // NOI18N
                     +" id2: " + Integer.toHexString(System.identityHashCode(i2)) // NOI18N
                );

                //                print (ps, false);
                ps.close();

                throw new IllegalStateException(bs.toString());
            }

            return 0;
        }

        return result;
    }
}
