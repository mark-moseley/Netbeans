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
package org.netbeans.core.startup;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.StreamHandler;
import java.util.logging.XMLFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;


/**
 * Checks the top logging delegates to handlers in lookup.
 */
public class TopLoggingLookupTest extends NbTestCase {
    private MyHandler handler;
    
    public TopLoggingLookupTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        clearWorkDir();
        System.setProperty("netbeans.user", getWorkDirPath());

        MockServices.setServices();

        // initialize logging
        TopLogging.initialize();
    }


    protected void tearDown() throws Exception {
    }

    public void testLogOneLine() throws Exception {
        MockServices.setServices(MyHandler.class);
        handler = Lookup.getDefault().lookup(MyHandler.class);
        assertNotNull("Handler found", handler);

        
        Logger.getLogger(TopLoggingTest.class.getName()).log(Level.INFO, "First visible message");

        assertEquals("[First visible message]", handler.logs.toString());
    }

    public void testDeadlock78865() throws Exception {
        MockServices.setServices(AnotherThreadLoggingHandler.class);
        handler = Lookup.getDefault().lookup(MyHandler.class);
        assertNotNull("Handler found", handler);

        Logger.getLogger(TopLoggingTest.class.getName()).log(Level.INFO, "First visible message");

        assertEquals("[First visible message]", handler.logs.toString());
    }

    public static class MyHandler extends Handler {
        public List<String> logs = new ArrayList<String>();

        public void publish(LogRecord record) {
            logs.add(record.getMessage());
        }

        public void flush() {
            logs.add("flush");
        }

        public void close() throws SecurityException {
            logs.add("close");
        }

    }
    public static final class AnotherThreadLoggingHandler extends MyHandler
    implements Runnable {
        public AnotherThreadLoggingHandler() {
            Logger.global.info("in constructor before");
            RequestProcessor.getDefault().post(this).waitFinished();
            Logger.global.info("in constructor after");
        }
        public void run() {
            Logger.global.warning("running in parael");
        }

    }
}
