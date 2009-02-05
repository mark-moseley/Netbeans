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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.openide.filesystems.test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import static junit.framework.Assert.*;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * Common utility methods for massaging and inspecting files from tests.
 */
public class TestFileUtils {

    private TestFileUtils() {}

    /**
     * Create a new data file with specified initial contents.
     * @param root a root folder which should already exist
     * @param path a /-separated path to the new file within that root
     * @param body the complete contents of the new file (in UTF-8 encoding)
     */
    public static FileObject writeFile(FileObject root, String path, String body) throws IOException {
        FileObject fo = FileUtil.createData(root, path);
        OutputStream os = fo.getOutputStream();
        PrintWriter pw = new PrintWriter(new OutputStreamWriter(os, "UTF-8"));
        pw.print(body);
        pw.flush();
        os.close();
        return fo;
    }

    /**
     * Create a new ZIP file.
     * @param root a root folder which should already exist
     * @param path a /-separated path to the new ZIP file within that root
     * @param entries a list of entries in the form of "filename:UTF8-contents"; parent dirs created automatically
     * @return the newly created ZIP file (use {@link FileUtil#getArchiveRoot} if you want the root entry)
     * @throws IOException for the usual reasons
     */
    public static FileObject writeZipFile(FileObject root, String path, String... entries) throws IOException {
        FileObject fo = FileUtil.createData(root, path);
        org.openide.util.test.TestFileUtils.writeZipFile(fo.getOutputStream(), entries);
        return fo;
    }

    /**
     * Make sure the timestamp on a file changes.
     * @param f a file to touch (make newer)
     * @param ref if not null, make f newer than this file; else make f newer than it was before
     */
    public static void touch(FileObject f, FileObject ref) throws IOException, InterruptedException {
        File ff = FileUtil.toFile(f);
        long older = ff.lastModified();
        if (ref != null) {
            older = Math.max(older, FileUtil.toFile(ref).lastModified());
        }
        for (long pause = 1; pause < 9999; pause *= 2) {
            Thread.sleep(pause);
            ff.setLastModified(System.currentTimeMillis());
            if (ff.lastModified() > older) {
                f.refresh();
                return;
            }
        }
        fail("Did not manage to touch " + ff);
    }

    /**
     * Read the contents of a file as a single string.
     * @param a data file
     * @return its contents (in UTF-8 encoding)
     * @deprecated Use file.asText()
     */
    @Deprecated
    public static String readFile(FileObject file) throws IOException {
        return file.asText("UTF-8");
    }

    /**
     * Assert that the contents of a file (in UTF-8) include the specified text as a substring.
     * @param file an existing data file
     * @param contents a substring expected to be found in it
     */
    public static void assertContains(FileObject file, String contents) throws IOException {
        String text = file.asText("UTF-8");
        assertTrue("Found '" + contents + "' in '" + text + "'", text.contains(contents));
    }

}
