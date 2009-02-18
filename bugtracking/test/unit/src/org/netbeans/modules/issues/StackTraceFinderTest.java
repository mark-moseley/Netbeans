/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.issues;

import java.util.List;
import junit.framework.TestCase;
import org.netbeans.modules.tasks.api.StackTraceFinder;
import org.netbeans.modules.tasks.api.StackTraceFinder.StackTracePosition;

/**
 *
 * @author tomas
 */
public class StackTraceFinderTest extends TestCase {

    public void testStackTrace() {
        String prefix = "simply something \n";
        String st ="org.netbeans.ProxyClassLoader.printDefaultPackageWarning(ProxyClassLoader.java:539)";
        List<StackTracePosition> res = StackTraceFinder.find(prefix + st);
        assertEquals(1, res.size());
        assertEquals(prefix.length(), res.get(0).getStartOffset());
        assertEquals(prefix.length() + st.length(), res.get(0).getEndOffset());

        prefix = " got this bloody stacktrace\n   at ";
        st ="org.netbeans.ProxyClassLoader.printDefaultPackageWarning(ProxyClassLoader.java:539)\n" +
            "   at org.netbeans.ProxyClassLoader.getResource(ProxyClassLoader.java:312)\n" +
            "   at java.lang.ClassLoader.getResourceAsStream(ClassLoader.java:1214)";
        res = StackTraceFinder.find(prefix + st);
        assertEquals(1, res.size());
        assertEquals(prefix.length(), res.get(0).getStartOffset());
        assertEquals(prefix.length() + st.length(), res.get(0).getEndOffset());

        String prefix1 = " got those 2 stacktraces\nthis one: \n   at ";
        String st1 = "org.netbeans.ProxyClassLoader.printDefaultPackageWarning(ProxyClassLoader.java:539)\n" +
            "   at org.netbeans.ProxyClassLoader.getResource(ProxyClassLoader.java:312)\n" +
            "   at java.lang.ClassLoader.getResourceAsStream(ClassLoader.java:1214)\n";
        String prefix2 = "\n\nand this another one: \n   at ";
        String st2 = "org.netbeans.ProxyClassLoader.printDefaultPackageWarning(ProxyClassLoader.java:539)\n" +
            "   at org.netbeans.ProxyClassLoader.getResource(ProxyClassLoader.java:312)\n" +
            "   at java.lang.ClassLoader.getResourceAsStream(ClassLoader.java:1214)";

        st = (prefix1 + st1 + prefix2 + st2);
        res = StackTraceFinder.find(st);
        assertEquals(2, res.size());
        assertEquals(prefix1.length(), res.get(0).getStartOffset());
        assertEquals(prefix1.length() + st1.length() - 1, res.get(0).getEndOffset()); // XXX -1 house numero?
        assertEquals(prefix1.length() + st1.length() + prefix2.length(), res.get(1).getStartOffset());
        assertEquals(prefix1.length() + st1.length() + prefix2.length() + st2.length(), res.get(1).getEndOffset());
    }

}
