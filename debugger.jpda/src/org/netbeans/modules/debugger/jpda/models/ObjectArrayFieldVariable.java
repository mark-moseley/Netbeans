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

package org.netbeans.modules.debugger.jpda.models;

import com.sun.jdi.ArrayReference;
import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.InvalidTypeException;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.Value;

import org.netbeans.api.debugger.jpda.InvalidExpressionException;
import org.netbeans.api.debugger.jpda.JPDAClassType;
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;


/**
 * @author   Jan Jancura
 */
class ObjectArrayFieldVariable extends AbstractObjectVariable implements
org.netbeans.api.debugger.jpda.Field {

    private final ArrayReference array;
    private int index;
    private int maxIndexLog;
    private String declaredType;

    ObjectArrayFieldVariable (
        JPDADebuggerImpl debugger,
        ObjectReference value,
        String declaredType,
        ArrayReference array,
        int index,
        int maxIndex,
        String parentID
    ) {
        super (
            debugger, 
            value, 
            parentID + '.' + index +
                (value instanceof ObjectReference ? "^" : "")
        );
        this.index = index;
        this.maxIndexLog = ArrayFieldVariable.log10(maxIndex);
        this.declaredType = declaredType;
        this.array = array;
    }

    public String getName () {
        return ArrayFieldVariable.getName(maxIndexLog, index);
    }
    
    public String getClassName () {
        return getType ();
    }
    
    public JPDAClassType getDeclaringClass() {
        return new JPDAClassTypeImpl(getDebugger(), (ReferenceType) array.type());
    }

    public boolean isStatic () {
        return false;
    }
    
    public String getDeclaredType () {
        return declaredType;
    }
    
    protected void setValue (Value value) throws InvalidExpressionException {
        try {
            array.setValue(index, value);
        } catch (InvalidTypeException ex) {
            throw new InvalidExpressionException (ex);
        } catch (ClassNotLoadedException ex) {
            throw new InvalidExpressionException (ex);
        }
    }

    public ObjectArrayFieldVariable clone() {
        ObjectArrayFieldVariable clon = new ObjectArrayFieldVariable(
                getDebugger(),
                (ObjectReference) getJDIValue(),
                getDeclaredType(),
                array,
                index,
                0,
                getID());
        clon.maxIndexLog = this.maxIndexLog;
        return clon;
    }

    // other methods ...........................................................

    public String toString () {
        return "ObjectArrayFieldVariable " + getName ();
    }
}
