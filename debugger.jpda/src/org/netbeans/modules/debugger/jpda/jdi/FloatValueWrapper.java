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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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

package org.netbeans.modules.debugger.jpda.jdi;

// DO NOT MODIFY THIS CODE, GENERATED AUTOMATICALLY
// Generated by org.netbeans.modules.debugger.jpda.jdi.Generate class.

/**
 * Wrapper for FloatValue JDI class.
 * Use methods of this class instead of direct calls on JDI objects.
 * These methods assure that exceptions thrown from JDI calls are handled appropriately.
 *
 * @author Martin Entlicher
 */
public final class FloatValueWrapper {

    private FloatValueWrapper() {}

    public static boolean equals0(com.sun.jdi.FloatValue a, java.lang.Object b) {
        try {
            return a.equals(b);
        } catch (com.sun.jdi.InternalException ex) {
            org.netbeans.modules.debugger.jpda.JDIExceptionReporter.report(ex);
            return false;
        } catch (com.sun.jdi.VMDisconnectedException ex) {
            return false;
        }
    }

    public static boolean equals(com.sun.jdi.FloatValue a, java.lang.Object b) throws org.netbeans.modules.debugger.jpda.jdi.InternalExceptionWrapper, org.netbeans.modules.debugger.jpda.jdi.VMDisconnectedExceptionWrapper {
        try {
            return a.equals(b);
        } catch (com.sun.jdi.InternalException ex) {
            org.netbeans.modules.debugger.jpda.JDIExceptionReporter.report(ex);
            throw new org.netbeans.modules.debugger.jpda.jdi.InternalExceptionWrapper(ex);
        } catch (com.sun.jdi.VMDisconnectedException ex) {
            throw new org.netbeans.modules.debugger.jpda.jdi.VMDisconnectedExceptionWrapper(ex);
        }
    }

    public static int hashCode0(com.sun.jdi.FloatValue a) {
        try {
            return a.hashCode();
        } catch (com.sun.jdi.InternalException ex) {
            org.netbeans.modules.debugger.jpda.JDIExceptionReporter.report(ex);
            return 0;
        } catch (com.sun.jdi.VMDisconnectedException ex) {
            return 0;
        }
    }

    public static int hashCode(com.sun.jdi.FloatValue a) throws org.netbeans.modules.debugger.jpda.jdi.InternalExceptionWrapper, org.netbeans.modules.debugger.jpda.jdi.VMDisconnectedExceptionWrapper {
        try {
            return a.hashCode();
        } catch (com.sun.jdi.InternalException ex) {
            org.netbeans.modules.debugger.jpda.JDIExceptionReporter.report(ex);
            throw new org.netbeans.modules.debugger.jpda.jdi.InternalExceptionWrapper(ex);
        } catch (com.sun.jdi.VMDisconnectedException ex) {
            throw new org.netbeans.modules.debugger.jpda.jdi.VMDisconnectedExceptionWrapper(ex);
        }
    }

    public static float value(com.sun.jdi.FloatValue a) throws org.netbeans.modules.debugger.jpda.jdi.InternalExceptionWrapper, org.netbeans.modules.debugger.jpda.jdi.VMDisconnectedExceptionWrapper {
        try {
            return a.value();
        } catch (com.sun.jdi.InternalException ex) {
            org.netbeans.modules.debugger.jpda.JDIExceptionReporter.report(ex);
            throw new org.netbeans.modules.debugger.jpda.jdi.InternalExceptionWrapper(ex);
        } catch (com.sun.jdi.VMDisconnectedException ex) {
            throw new org.netbeans.modules.debugger.jpda.jdi.VMDisconnectedExceptionWrapper(ex);
        }
    }

}
