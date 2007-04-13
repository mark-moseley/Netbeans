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

package org.netbeans.modules.j2ee.metadata.model.support;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Andrei Badea
 */
public class TestUtilities {

    private TestUtilities() {
    }

    public static final FileObject copyStringToFileObject(FileObject parent, String path, String contents) throws IOException {
        FileObject fo = FileUtil.createData(parent, path);
        copyStringToFileObject(fo, contents);
        return fo;
    }

    public static final void copyStringToFileObject(FileObject fo, String contents) throws IOException {
        OutputStream os = fo.getOutputStream();
        try {
            InputStream is = new ByteArrayInputStream(contents.getBytes("UTF-8"));
            FileUtil.copy(is, os);
        } finally {
            os.close();
        }
    }

    public static final String copyStreamToString(InputStream input) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        FileUtil.copy(input, output);
        return Charset.forName("UTF-8").newDecoder().decode(ByteBuffer.wrap(output.toByteArray())).toString();
    }

    public static final String copyFileObjectToString (FileObject fo) throws IOException {
        InputStream stream = fo.getInputStream();
        try {
            return copyStreamToString(stream);
        } finally {
            stream.close();
        }
    }
}
