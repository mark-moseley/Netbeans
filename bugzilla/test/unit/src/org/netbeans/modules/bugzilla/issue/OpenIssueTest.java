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

package org.netbeans.modules.bugzilla.issue;

import java.util.logging.Handler;
import org.netbeans.modules.bugtracking.spi.Issue;
import org.netbeans.modules.bugzilla.*;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import org.eclipse.mylyn.internal.bugzilla.core.BugzillaCorePlugin;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.bugzilla.repository.BugzillaRepository;

/**
 *
 * @author tomas
 */
public class OpenIssueTest extends NbTestCase implements TestConstants {

    private static String REPO_NAME = "Beautiful";

    public OpenIssueTest(String arg0) {
        super(arg0);
    }

    @Override
    protected Level logLevel() {
        return Level.ALL;
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        BugzillaCorePlugin bcp = new BugzillaCorePlugin();
        try {
            bcp.start(null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void testOpenNewIssue() throws Throwable {
        BugzillaRepository repository = getRepository();
        Issue issue = repository.createIssue();

        LogHandler handler = new LogHandler("open finish", LogHandler.Compare.ENDS_WITH);
        issue.open();
        handler.waitUntilDone();

    }

    private BugzillaRepository getRepository() {
        return TestUtil.getRepository(REPO_NAME, REPO_URL, REPO_USER, REPO_PASSWD);
    }

    private static class LogHandler extends Handler {
        private static final long TIMEOUT = 10 * 1000;
        private final String msg;
        private boolean done = false;
        private final Compare compare;
        private enum Compare {
            STARTS_WITH,
            ENDS_WITH
        }
        public LogHandler(String msg, Compare compare) {
            this.msg = msg;
            this.compare = compare;
            Bugzilla.LOG.addHandler(this);
        }

        @Override
        public void publish(LogRecord record) {
            if(!done) {
                switch (compare) {
                    case STARTS_WITH :
                        done = record.getMessage().startsWith(msg);
                        break;
                    case ENDS_WITH :
                        done = record.getMessage().endsWith(msg);
                        break;
                    default:
                        throw new IllegalStateException("wrong value " + compare);
                }
            }
        }
        @Override
        public void flush() { }
        @Override
        public void close() throws SecurityException { }

        void waitUntilDone() throws InterruptedException {
            long t = System.currentTimeMillis();
            while(!done) {
                Thread.sleep(200);
                if(System.currentTimeMillis() - t > TIMEOUT) {
                    throw new IllegalStateException("timeout");
                }
            }
        }
    }
}
