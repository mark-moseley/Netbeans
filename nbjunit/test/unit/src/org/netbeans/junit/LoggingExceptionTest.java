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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.junit;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/** Checks that we can do proper logging of exceptions.
 *
 * @author Jaroslav Tulach
 */
public class LoggingExceptionTest extends NbTestCase {
    private Throwable toThrow;
    
    public LoggingExceptionTest(String testName) {
        super(testName);
    }
    
    @Override
    protected Level logLevel() {
        return Level.FINE;
    }
    @Override
    protected void setUp() throws IOException {
        clearWorkDir();
    }

    @Override
    protected int timeOut() {
        return getName().contains("Time") ? 10000 : 0;
    }
    
    
    public void testLoggedExceptionIsPrinted() throws Exception {
        Exception ex = new IOException("Ahoj");
        LogRecord rec = new LogRecord(Level.WARNING, "Cannot process {0}");
        rec.setThrown(ex);
        rec.setParameters(new Object[] { "Jardo" });
        Logger.global.log(rec);
        
        File[] arr = getWorkDir().listFiles();
        assertEquals("One log file", 1, arr.length);
        String s = LoggingTest.readFile(arr[0]);
        
        if (s.indexOf("Ahoj") == -1) {
            fail("There needs to be 'Ahoj':\n" + s);
        }
        if (s.indexOf("Jardo") == -1) {
            fail("There needs to be 'Jardo':\n" + s);
        }
        if (s.indexOf("testLoggedExceptionIsPrinted") == -1) {
            fail("There needs to be name of the method:\n" + s);
        }
    }
    public void testLoggedExceptionIsPrintedWithTimeout() throws Exception {
        testLoggedExceptionIsPrinted();
    }
    public void testLoggedExceptionIsPrintedNoFormat() throws Exception {
        Exception ex = new IOException("Ahoj");
        Logger.global.log(Level.WARNING, "No format Jardo", ex);
        
        File[] arr = getWorkDir().listFiles();
        assertEquals("One log file", 1, arr.length);
        String s = LoggingTest.readFile(arr[0]);
        
        if (s.indexOf("Ahoj") == -1) {
            fail("There needs to be 'Ahoj':\n" + s);
        }
        if (s.indexOf("Jardo") == -1) {
            fail("There needs to be 'Jardo':\n" + s);
        }
        if (s.indexOf("testLoggedExceptionIsPrinted") == -1) {
            fail("There needs to be name of the method:\n" + s);
        }
    }
    public void testLoggedExceptionIsPrintedWithTimeoutNoFormat() throws Exception {
        testLoggedExceptionIsPrintedNoFormat();
    }
}