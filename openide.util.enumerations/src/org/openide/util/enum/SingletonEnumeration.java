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
package org.openide.util.enum;

import java.util.Enumeration;
import java.util.NoSuchElementException;


/**
 * The class that encapsulates one object into one element enumeration.
 * @deprecated JDK 1.5 treats enum as a keyword so this class was
 *             replaced by {@link org.openide.util.Enumerations#singleton}.
 * @author Jaroslav Tulach
 */
public class SingletonEnumeration implements Enumeration {
    /** object to return */
    private Object object;

    /** @param object object to be put into the enumeration
    */
    public SingletonEnumeration(Object object) {
        this.object = object;
    }

    /** Tests if this enumeration contains next element.
    * @return  <code>true</code> if this enumeration contains it
    *          <code>false</code> otherwise.
    */
    public boolean hasMoreElements() {
        return object != null;
    }

    /** Returns the next element of this enumeration.
    * @return     the next element of this enumeration.
    * @exception  NoSuchElementException  if no more elements exist.
    */
    public synchronized Object nextElement() {
        if (object == null) {
            throw new NoSuchElementException();
        } else {
            Object o = object;
            object = null;

            return o;
        }
    }
}
