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

package org.netbeans.modules.extexecution.api.input;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author Petr Hejl
 */
public class InputReadersReaderTest extends NbTestCase {

    private static final byte[] TEST_BYTES = new byte[] {
        0x41, 0x42, 0x43, 0x44, 0x45, 0x46, 0x47, 0x48, 0x49, 0x50
    };

    private static final long STARVATION_TIMEOUT = 2000;

    private static final int MAX_RETRIES = TEST_BYTES.length * 2;

    private static final Charset TEST_CHARSET = Charset.forName("UTF-8");

    public InputReadersReaderTest(String name) {
        super(name);
    }

    public void testReadOutput() throws IOException {
        Reader reader = new InputStreamReader(TestInputUtils.prepareInputStream(
                TEST_BYTES), TEST_CHARSET);
        InputReader outputReader = InputReaders.forReader(reader, TEST_CHARSET);
        TestInputProcessor processor = new TestInputProcessor(false);

        int read = 0;
        int retries = 0;
        while (read < TEST_BYTES.length && retries < MAX_RETRIES) {
            read += outputReader.readOutput(processor);
            retries++;
        }

        assertEquals(read, TEST_BYTES.length);
        assertEquals(0, processor.getResetCount());

        byte[] processed = processor.getBytesProcessed();
        for (int i = 0; i < TEST_BYTES.length; i++) {
            assertEquals(TEST_BYTES[i], processed[i]);
        }
    }


//    public void testGreedy() throws IOException {
//        Reader reader = new InputStreamReader(TestInputUtils.prepareInputStream(
//                TEST_BYTES), TEST_CHARSET);
//        InputReader outputReader = InputReaders.forReader(reader, TEST_CHARSET, false);
//        TestInputProcessor processor = new TestInputProcessor(false);
//
//        int read = outputReader.readOutput(processor);
//
//        assertEquals(read, TEST_BYTES.length);
//        assertEquals(0, processor.getResetCount());
//
//        byte[] processed = processor.getBytesProcessed();
//        for (int i = 0; i < TEST_BYTES.length; i++) {
//            assertEquals(TEST_BYTES[i], processed[i]);
//        }
//    }
}
