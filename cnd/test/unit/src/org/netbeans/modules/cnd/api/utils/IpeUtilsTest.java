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

package org.netbeans.modules.cnd.api.utils;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * @author Alexey Vladykin
 */
public class IpeUtilsTest {

    @Test
    public void testGetPathNameArray() {
        assertArrayEquals(new String[] {"C:", "tmp", "test.cpp"}, // NOI18N
                     IpeUtils.getPathNameArray("C:\\tmp\\test.cpp")); // NOI18N
        assertArrayEquals(new String[] {"C:", "tmp", "test.cpp"}, // NOI18N
                     IpeUtils.getPathNameArray("C:/tmp/test.cpp")); // NOI18N
        assertArrayEquals(new String[] {"tmp", "test.cpp"}, // NOI18N
                     IpeUtils.getPathNameArray("\\tmp\\test.cpp")); // NOI18N
        assertArrayEquals(new String[] {"tmp", "test.cpp"}, // NOI18N
                     IpeUtils.getPathNameArray("/tmp/test.cpp")); // NOI18N
        assertArrayEquals(new String[] {}, // NOI18N
                     IpeUtils.getPathNameArray("tmp/test.cpp")); // NOI18N
    }

    @Test
    public void testIsPathAbsolute() {
        assertTrue(IpeUtils.isPathAbsolute("C:\\tmp\\test.cpp")); // NOI18N
        assertTrue(IpeUtils.isPathAbsolute("C:/tmp/test.cpp")); // NOI18N
        assertTrue(IpeUtils.isPathAbsolute("C:/./test.cpp")); // NOI18N
        assertTrue(IpeUtils.isPathAbsolute("\\temp\\..\\tmp\\test.cpp")); // NOI18N
        assertTrue(IpeUtils.isPathAbsolute("/tmp/test.cpp")); // NOI18N
        assertTrue(IpeUtils.isPathAbsolute("/../tmp/test.cpp")); // NOI18N
        assertTrue(IpeUtils.isPathAbsolute("/.")); // NOI18N
        assertFalse(IpeUtils.isPathAbsolute("../tmp/test.cpp")); // NOI18N
        assertFalse(IpeUtils.isPathAbsolute("./")); // NOI18N
        assertFalse(IpeUtils.isPathAbsolute("tmp\\test.cpp")); // NOI18N
    }

    @Test
    public void test158596() {
        assertEquals("c", // NOI18N
                     normalize(IpeUtils.toRelativePath("\\C:\\f", "C:\\f\\c"))); // NOI18N
    }

    @Test
    public void testToRelativePath() {
        assertEquals("D:\\tmp\\test.cpp", // NOI18N
                     IpeUtils.toRelativePath("C:\\tmp", "D:\\tmp\\test.cpp")); // NOI18N
        assertEquals("/tmp/test.cpp", // NOI18N
                     IpeUtils.toRelativePath("/temp/", "/tmp/test.cpp")); // NOI18N
        assertEquals("1\\test.cpp", // NOI18N
                     IpeUtils.toRelativePath("C:\\tmp", "C:\\tmp\\1\\test.cpp")); // NOI18N
        assertEquals("1/test.cpp", // NOI18N
                     IpeUtils.toRelativePath("/tmp", "/tmp/1/test.cpp")); // NOI18N
        assertEquals("test.cpp", // NOI18N
                     IpeUtils.toRelativePath("C:\\tmp", "C:\\tmp\\test.cpp")); // NOI18N
        assertEquals("test.cpp", // NOI18N
                     IpeUtils.toRelativePath("/tmp", "/tmp/test.cpp")); // NOI18N
        assertEquals("../../../3/2/1/test.cpp", // NOI18N
                     normalize(IpeUtils.toRelativePath("C:\\tmp\\dir\\1\\2\\3", "C:\\tmp\\dir\\3\\2\\1\\test.cpp"))); // NOI18N
        assertEquals("../../../3/2/1/test.cpp", // NOI18N
                     normalize(IpeUtils.toRelativePath("/tmp/dir/1/2/3", "/tmp/dir/3/2/1/test.cpp"))); // NOI18N
        assertEquals(".", // NOI18N
                     IpeUtils.toRelativePath("C:\\", "C:\\")); // NOI18N
        assertEquals(".", // NOI18N
                     IpeUtils.toRelativePath("/tmp", "/tmp")); // NOI18N
    }

    private String normalize(String path) {
        return path.replace('\\', '/');
    }

}
