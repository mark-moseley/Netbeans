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

package org.netbeans.modules.debugger.jpda.expr;

import java.util.Collection;

/**
 * Support class to help assertions and error processing when evaluating an expression.
 *
 * @author Maros Sandor
 */
class Assert {

    static Object error(SimpleNode node, String param) throws EvaluationException {
        return error(node, param, null);
    }

    static Object error(SimpleNode node, String cause, Object p2) throws EvaluationException {
        return error(node, cause, new Object [] { p2 });
    }

    static Object error(SimpleNode node, String cause, Object p2, Object p3) throws EvaluationException {
        return error(node, cause, new Object [] { p2, p3});
    }

    static Object error(SimpleNode node, String cause, Object p1, Object p2, Object p3) throws EvaluationException {
        return error(node, cause, new Object [] { p1, p2, p3});
    }

    private static Object error (SimpleNode node, String cause, Object [] params) throws EvaluationException {
        throw new EvaluationException(node, cause, params);
    }

    static void assertAssignable(Object o, Class aClass, SimpleNode s, String p1, Object p2) {
        if (o != null && !aClass.isAssignableFrom(o.getClass())) {
            error(s, p1, p2);
        }
    }

    static void assertAssignable(Object o, Class aClass, SimpleNode s, String p1, Object p2, Object p3) {
        if (o != null && !aClass.isAssignableFrom(o.getClass())) {
            error(s, p1, p2, p3);
        }
    }

    static void assertNotAssignable(Object o, Class aClass, SimpleNode s, String p1, Object p2) {
        if (aClass.isAssignableFrom(o.getClass())) {
            error(s, p1, p2);
        }
    }

    static void assertNotAssignable(Object o, Class aClass, SimpleNode s, String p1, Object p2, Object p3) {
        if (aClass.isAssignableFrom(o.getClass())) {
            error(s, p1, p2, p3);
        }
    }

    static void assertNotAssignable(Object o, Class aClass, SimpleNode node, String s) {
        if (aClass.isAssignableFrom(o.getClass())) {
            error(node, s);
        }
    }

    static void assertLess(int a, int b, SimpleNode s, String p1, Object p2, Object p3) {
        if (a >= b) {
            error(s, p1, p2, p3);
        }
    }

    static void assertNotNull(Object obj, SimpleNode s, String identifier) {
        if (obj == null) {
            error(s, identifier);
        }
    }

    static void assertNonEmpty(Collection collection, SimpleNode s, String p1, Object p2) {
        if (collection == null || collection.size() == 0) {
            error(s, p1, p2);
        }
    }

    static void assertNotNull(Object obj, SimpleNode node, String p1, Object p2) {
        if (obj == null) {
            error(node, p1, p2);
        }
    }

}
