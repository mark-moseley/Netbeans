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

package org.netbeans.modules.server.output;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.server.output.LineProcessor;
import org.netbeans.api.server.output.LineReader;
import org.netbeans.api.server.output.ReaderManager;
import org.openide.util.Parameters;

/**
 *
 * This class is <i>ThreadSafe</i>.
 * @author Petr Hejl
 */
public class ReaderThread extends Thread {

    private static final Logger LOGGER = Logger.getLogger(ReaderThread.class.getName());

    private static final int DELAY = 500;

    private static final AtomicLong THREAD_COUNTER = new AtomicLong(0);

    private final ReaderManager readerManager;

    private final LineReader lineReader;

    private final LineProcessor lineProcessor;

    public ReaderThread(ReaderManager readerManager, LineReader lineReader) {
        this(readerManager, lineReader, null);
    }

    public ReaderThread(ReaderManager readerManager, LineReader lineReader,
            LineProcessor lineProcessor) {

        super("line_reader_thread_" + THREAD_COUNTER.incrementAndGet()); // NOI18N

        Parameters.notNull("readerManager", readerManager);
        Parameters.notNull("lineReader", lineReader);

        this.setDaemon(true);
        this.readerManager = readerManager;
        this.lineReader = lineReader;
        this.lineProcessor = lineProcessor;
    }

    @Override
    public void run() {
        boolean interrupted = false;
        try {
            while (true) {
                if (Thread.interrupted()) {
                    interrupted = true;
                    break;
                }

                lineReader.readLines(lineProcessor, false);

                try {
                    // give the producer some time to write the output
                    Thread.sleep(DELAY);
                } catch (InterruptedException e) {
                    interrupted = true;
                    break;
                }
            }

            lineReader.readLines(lineProcessor, true);
        } catch (Exception ex) {
            if (!interrupted && !Thread.currentThread().isInterrupted()) {
                LOGGER.log(Level.INFO, null, ex);
            }
        } finally {
            // perform cleanup
            try {
                lineReader.close();
            } catch (IOException ex) {
                LOGGER.log(Level.INFO, null, ex);
            } finally {
                Accessor.DEFAULT.notifyFinished(readerManager, this);
                if (interrupted) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    /**
     * The accessor pattern class.
     */
    public abstract static class Accessor {

        /** The default accessor. */
        public static Accessor DEFAULT;

        static {
            // invokes static initializer of ReaderManager.class
            // that will assign value to the DEFAULT field above
            Class c = ReaderManager.class;
            try {
                Class.forName(c.getName(), true, c.getClassLoader());
            } catch (ClassNotFoundException ex) {
                assert false : ex;
            }
        }

        /**
         * Accessor to notify the manager about finished thread.
         *
         * @param manager the manager to notify
         * @param thread the thread that was finished
         */
        public abstract void notifyFinished(ReaderManager manager, ReaderThread thread);

    }
}