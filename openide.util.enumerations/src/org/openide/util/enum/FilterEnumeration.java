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
package org.openide.util.enum;

import java.util.Enumeration;
import java.util.NoSuchElementException;


/**
 * Abstract class that takes an enumeration and filters its elements.
 * To get this class fully work one must override <CODE>accept</CODE> method.
 * Objects in the enumeration must not be <CODE>null</CODE>.
 * @deprecated JDK 1.5 treats enum as a keyword so this class was
 *             replaced by {@link org.openide.util.Enumerations#filter}.
 * @author Jaroslav Tulach
 */
public class FilterEnumeration extends Object implements Enumeration {
    /** marker object stating there is no nexte element prepared */
    private static final Object EMPTY = new Object();

    /** enumeration to filter */
    private Enumeration en;

    /** element to be returned next time or {@link #EMPTY} if there is
    * no such element prepared */
    private Object next = EMPTY;

    /**
    * @param en enumeration to filter
    */
    public FilterEnumeration(Enumeration en) {
        this.en = en;
    }

    /** Filters objects. Overwrite this to decide which objects should be
    * included in enumeration and which not.
    * <P>
    * Default implementation accepts all non-null objects
    *
    * @param o the object to decide on
    * @return true if it should be in enumeration and false if it should not
    */
    protected boolean accept(Object o) {
        return o != null;
    }

    /** @return true if there is more elements in the enumeration
    */
    public boolean hasMoreElements() {
        if (next != EMPTY) {
            // there is a object already prepared
            return true;
        }

        while (en.hasMoreElements()) {
            // read next
            next = en.nextElement();

            if (accept(next)) {
                // if the object is accepted
                return true;
            }

            ;
        }

        next = EMPTY;

        return false;
    }

    /** @return next object in the enumeration
    * @exception NoSuchElementException can be thrown if there is no next object
    *   in the enumeration
    */
    public Object nextElement() {
        if ((next == EMPTY) && !hasMoreElements()) {
            throw new NoSuchElementException();
        }

        Object res = next;
        next = EMPTY;

        return res;
    }
}
