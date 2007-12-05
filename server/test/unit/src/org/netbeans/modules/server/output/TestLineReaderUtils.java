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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.server.output;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Random;
import org.netbeans.api.server.output.FileInputProvider;
import org.netbeans.api.server.output.FileInputProvider.FileInput;

/**
 *
 * @author Petr Hejl
 */
public final class TestLineReaderUtils {

    private TestLineReaderUtils() {
        super();
    }

    public static InputStream prepareInputStream(String[] lines, String separator,
            Charset charset, boolean terminate) {

        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < lines.length; i++) {
            buffer.append(lines[i]);
            if (terminate || i < (lines.length - 1)) {
                buffer.append(separator);
            }
        }

        ByteBuffer byteBuffer = charset.encode(buffer.toString());
        int length = byteBuffer.limit();
        byte[] byteArray = new byte[length];
        byteBuffer.position(0);
        byteBuffer.get(byteArray);

        return new ByteArrayInputStream(byteArray);
    }

    public static File prepareFile(String name, String[] lines, String separator,
            Charset charset, File workDir, boolean terminate) throws IOException {

        File file = new File(workDir, name);

        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), charset));
        try {
            for (int i = 0; i < lines.length; i++) {
            writer.write(lines[i]);
            if (terminate || i < (lines.length - 1)) {
                writer.write(separator);
            }
            }
        } finally {
            writer.close();
        }
        return file;
    }

    public static class InfiniteAsciiInputStream extends InputStream {

        private final Random random = new Random();

        @Override
        public int read() throws IOException {
            return random.nextInt(256);
        }

        @Override
        public int available() throws IOException {
            return 1;
        }

    }

    public static class TestFileInputProvider implements FileInputProvider {

        private FileInput input;

        public void setFileInput(FileInput input) {
            this.input = input;
        }

        public FileInput getFileInput() {
            return input;
        }

    }

}
