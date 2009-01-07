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

package org.netbeans;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Member;
import java.lang.reflect.Modifier;
import org.netbeans.junit.NbTestCase;
import org.openide.modules.PatchedPublic;

public class PatchByteCodeTest extends NbTestCase {

    public PatchByteCodeTest(String n) {
        super(n);
    }

    public static class C {
        static final long x = 123L; // test CONSTANT_Long, tricky!
        private C(boolean _) {}
        @PatchedPublic
        private C(int _) {}
        private void m1() {}
        @PatchedPublic
        private void m2() {}
    }

    public void testPatchingPublic() throws Exception {
        Class c = new L().loadClass(C.class.getName());
        assertNotSame(c, C.class);
        Member m;
        m = c.getDeclaredConstructor(Boolean.TYPE);
        assertEquals(0, m.getModifiers() & Modifier.PUBLIC);
        assertEquals(Modifier.PRIVATE, m.getModifiers() & Modifier.PRIVATE);
        m = c.getDeclaredConstructor(Integer.TYPE);
        assertEquals(Modifier.PUBLIC, m.getModifiers() & Modifier.PUBLIC);
        assertEquals(0, m.getModifiers() & Modifier.PRIVATE);
        m = c.getDeclaredMethod("m1");
        assertEquals(0, m.getModifiers() & Modifier.PUBLIC);
        assertEquals(Modifier.PRIVATE, m.getModifiers() & Modifier.PRIVATE);
        m = c.getDeclaredMethod("m2");
        assertEquals(Modifier.PUBLIC, m.getModifiers() & Modifier.PUBLIC);
        assertEquals(0, m.getModifiers() & Modifier.PRIVATE);
    }

    private static class L extends ClassLoader {

        L() {
            super(L.class.getClassLoader());
        }

        @Override
        protected synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
            if (name.startsWith(PatchByteCodeTest.class.getName() + "$")) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                InputStream in = getResourceAsStream(name.replace('.', '/') + ".class");
                int r;
                try {
                    while ((r = in.read()) != -1) {
                        baos.write(r);
                    }
                } catch (IOException x) {
                    throw new ClassNotFoundException(name, x);
                }
                byte[] data = PatchByteCode.patch(baos.toByteArray());
                Class c = defineClass(name, data, 0, data.length);
                if (resolve) {
                    resolveClass(c);
                }
                return c;
            } else {
                return super.loadClass(name, resolve);
            }
        }
    }

}
