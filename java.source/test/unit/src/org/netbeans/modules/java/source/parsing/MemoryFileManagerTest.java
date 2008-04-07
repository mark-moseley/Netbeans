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

package org.netbeans.modules.java.source.parsing;

import java.util.EnumSet;
import java.util.List;
import javax.tools.FileObject;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author Tomas Zezula
 */
public class MemoryFileManagerTest extends NbTestCase {
    
    public MemoryFileManagerTest (String name) {
        super (name);
    }
    
    public void testFileManager () throws Exception {
        final MemoryFileManager mgr = new MemoryFileManager();
        List<JavaFileObject> jfos = mgr.list(StandardLocation.SOURCE_PATH, "org.me", EnumSet.of(JavaFileObject.Kind.SOURCE), false);
        assertTrue(jfos.isEmpty());
        long mtime = System.currentTimeMillis();
        String content = "package org.me;\n class Foo{}\n";
        mgr.register("org.me.Foo", mtime, content);
        jfos = mgr.list(StandardLocation.SOURCE_PATH, "org.me", EnumSet.of(JavaFileObject.Kind.SOURCE), false);
        assertEquals(1, jfos.size());        
        assertEquals("org.me.Foo", mgr.inferBinaryName(StandardLocation.SOURCE_PATH, jfos.get(0)));
        assertEquals(mtime, jfos.get(0).getLastModified());
        assertTrue(content.contentEquals(jfos.get(0).getCharContent(true)));
        mgr.register("org.me.Test", mtime, content);
        jfos = mgr.list(StandardLocation.SOURCE_PATH, "org.me", EnumSet.of(JavaFileObject.Kind.SOURCE), false);
        assertEquals(2, jfos.size());
        JavaFileObject jfo = mgr.getJavaFileForInput(StandardLocation.SOURCE_PATH, "org.me.Foo", JavaFileObject.Kind.SOURCE);
        assertNotNull(jfo);
        assertEquals("org.me.Foo", mgr.inferBinaryName(StandardLocation.SOURCE_PATH, jfo));
        FileObject fo = mgr.getFileForInput(StandardLocation.SOURCE_PATH, "org.me", "Foo.java");
        assertNotNull(jfo);
        mgr.unregister("org.me.Foo");
        jfos = mgr.list(StandardLocation.SOURCE_PATH, "org.me", EnumSet.of(JavaFileObject.Kind.SOURCE), false);
        assertEquals(1, jfos.size());        
        assertEquals("org.me.Test", mgr.inferBinaryName(StandardLocation.SOURCE_PATH, jfos.get(0)));
    }
}
