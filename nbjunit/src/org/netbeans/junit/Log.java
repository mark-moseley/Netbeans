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

package org.netbeans.junit;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.WeakHashMap;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import junit.framework.Assert;
import junit.framework.AssertionFailedError;

/** Collects log messages.
 *
 * @author Jaroslav Tulach
 */
public final class Log extends Handler {
    /** the test that is currently running */
    private static NbTestCase current;
    /** last 40Kb of collected error messages */
    private static final StringBuffer messages = new StringBuffer ();
    /** initial length of messages */
    private static int initialMessages;
    /** stream to log to */
    private Reference<PrintStream> log;
    /** logger we are assigned to */
    private Logger logger;

        
    /** Creates a new instance of Log */
    public Log() {
    }

    /** Creates handler with assigned logger
     */
    private Log(Logger l, PrintStream ps) {
        log = new WeakReference<PrintStream>(ps);
        logger = l;
    }

    /** Enables logging for given logger name and given severity.
     * Everything logged to the object is going to go to the returned
     * CharSequence object which can be used to check the content or
     * converted <code>toString</code>.
     * <p>
     * The logging stops when the returned object is garbage collected.
     *
     * @param loggerName the name to capture logging for
     * @param level the level of details one wants to get
     * @return character sequence which can be check or converted to string
     * @since 1.27
     */
    public static CharSequence enable(String loggerName, Level level) {
        class MyPs extends PrintStream implements CharSequence {
            private ByteArrayOutputStream os;

            public MyPs() {
                this(new ByteArrayOutputStream());
            }

            private MyPs(ByteArrayOutputStream arr) {
                super(arr);
                os = arr;
            }

            public int length() {
                return toString().length();
            }

            public char charAt(int index) {
                return toString().charAt(index);
            }

            public CharSequence subSequence(int start, int end) {
                return toString().subSequence(start, end);
            }

            public String toString() {
                return os.toString();
            }
        }

        Logger l = Logger.getLogger(loggerName);
        if (l.getLevel() == null || l.getLevel().intValue() > level.intValue()) {
            l.setLevel(level);
        }
        MyPs ps = new MyPs();
        Log log = new Log(l, ps);
        log.setLevel(level);
        l.addHandler(log);
        return ps;
    }

    /** 
     * Can emulate the execution flow of multiple threads in a deterministic
     * way so it is easy to emulate race conditions or deadlocks just with
     * the use of additional log messages inserted into the code.
     * <p>
     * The best example showing usage of this method is real life test.
     * Read <a href="http://www.netbeans.org/source/browse/xtest/nbjunit/test/unit/src/org/netbeans/junit/FlowControlTest.java">FlowControlTest.java</a> to know everything
     * about the expected usage of this method.
     * <p>
     * The method does listen on output send to a logger <code>listenTo</code>
     * by various threads and either suspends them or wake them up trying
     * as best as it can to mimic the log output described in <code>order</code>.
     * Of course, this may not always be possible, so there is the <code>timeout</code>
     * value which specifies the maximum time a thread can be suspended while
     * waiting for a single message. The information about the internal behaviour
     * of the controlFlow method can be send to <code>reportTo</code> logger,
     * if provided, so in case of failure one can analyse what went wrong.
     * <p>
     * The format of the order is a set of lines like:
     * <pre>
     * THREAD:name_of_the_thread MSG:message_to_expect
     * </pre>
     * which define the order saying that at this time a thread with a given name
     * is expected to send given message. Both the name of the thread and
     * the message are regular expressions so one can shorten them by using <q>.*</q>
     * or any other trick. Btw. the format of the <code>order</code> is similar
     * to the one logged by the {@link Log#enable} or {@link NbTestCase#logLevel} methods,
     * so when one gets a test failure with enabled logging,
     * it is enough to just delete the unnecessary messages, replace too specific
     * texts like <code>@574904</code> with <code>.*</code> and the order is
     * ready for use.
     *
     * @param listenTo the logger to listen to and guide the execution according to messages sent to it
     * @param reportTo the logger to report internal state to or <code>null</code> if the logging is not needed
     * @param order the string describing the expected execution order of threads
     * @param timeout the maximal wait time of each thread on given message, zero if the waiting shall be infinite
     *
     * @author Jaroslav Tulach, invented during year 2005
     * @since 1.28
     */
    public static void controlFlow(Logger listenTo, Logger reportTo, String order, int timeout) {
        ControlFlow.registerSwitches(listenTo, reportTo, order, timeout);
    }
    
    /** Starts to listen on given log and collect parameters of messages that
     * were send to it. This is supposed to be called at the begining of a test,
     * to get messages from the programs that use 
     * <a href="http://wiki.netbeans.org/wiki/view/FitnessViaTimersCounters">timers/counters</a>
     * infrastructure. At the end one should call {@link #assertInstances}.
     * 
     * 
     * @param log logger to listen on, if null, it uses the standard timers/counters one
     * @param msg name of messages to collect, if null, all messages will be recorded
     * @param level level of messages to record
     * @since 1.44
     */
    public static void enableInstances(Logger log, String msg, Level level) {
        if (log == null) {
            log = Logger.getLogger("TIMER"); // NOI18N
        }
        
        log.addHandler(new InstancesHandler(msg, level));
        
        if (log.getLevel() == null || log.getLevel().intValue() > level.intValue()) {
            log.setLevel(level);
        }
    }

    /** Assert to verify that all collected instances via {@link #enableInstances} 
     * can disappear. Uses {@link NbTestCase#assertGC} on each of them. 
     * 
     * @param msg message to display in case of potential failure
     */
    public static void assertInstances(String msg) {
        InstancesHandler.assertGC(msg);
    }



    static void configure(Level lev, NbTestCase current) {
        String c = "handlers=" + Log.class.getName() + "\n" +
                   ".level=" + lev.intValue() + "\n";

        ByteArrayInputStream is = new ByteArrayInputStream(c.getBytes());
        try {
            LogManager.getLogManager().readConfiguration(is);
        } catch (IOException ex) {
            // exception
            ex.printStackTrace();
        }

        Log.current = current;
        Log.messages.setLength(0);
        Log.messages.append("Starting test ");
        Log.messages.append(current.getName());
        Log.messages.append('\n');
        Log.initialMessages = Log.messages.length();
    }

    private PrintStream getLog(LogRecord record) {
        if (log != null) {
            PrintStream ps = log.get();
            if (ps == null) {
                // gc => remove the handler
                logger.removeHandler(this);
            }

            return ps == null ? System.err : ps;
        }

        NbTestCase c = current;
        return c == null ? System.err : c.getLog();
    }

    public void publish(LogRecord record) {
        StringBuffer sb = new StringBuffer();
        sb.append('[');
        sb.append(record.getLoggerName());
        sb.append("] THREAD: ");
        sb.append(Thread.currentThread().getName());
        sb.append(" MSG: ");
        String msg = record.getMessage();
        ResourceBundle b = record.getResourceBundle();
        if (b != null) {
            try {
                msg = b.getString(msg);
            } catch (MissingResourceException ex) {
                // ignore
            }
        }
        
        if (msg != null && record.getParameters() != null) {
            msg = MessageFormat.format(msg, record.getParameters());
        }
        sb.append(msg);
        
        Throwable t = record.getThrown();
        if (t != null) {
            for (StackTraceElement s : t.getStackTrace()) {
                sb.append("\n  ").append(s.toString());
            }
        }
        
        getLog(record).println(sb.toString());


        messages.append(sb.toString());
        messages.append ('\n');

        if (messages.length() > 40000) {
            messages.delete(0, 20000);
        }


        
        if (record.getThrown() != null) {
            record.getThrown().printStackTrace(getLog(record));
        }
    }

    public void flush() {
    }

    public void close() {
    }

    static Throwable wrapWithMessages(Throwable ex) {
        if (messages.length() == initialMessages) {
            // no wrapping
            return ex;
        }


        if (ex instanceof AssertionFailedError) {
            AssertionFailedError ne = new AssertionFailedError (ex.getMessage () + " Log:\n" + messages);
            ne.setStackTrace (ex.getStackTrace ());
            return ne;
        }


        if (ex instanceof IOException) {//#66208
            IOException ne = new IOException (ex.getMessage () + " Log:\n" + messages);
            ne.setStackTrace (ex.getStackTrace ());
            return ne;
        }

        if (ex instanceof Exception) {
            return new InvocationTargetException(ex, ex.getMessage() + " Log:\n" + messages);
        }

        return ex;
    }
        
        
    private static class InstancesHandler extends Handler {
        private static final Map<Object,String> instances = Collections.synchronizedMap(new WeakHashMap<Object,String>());
        private static int cnt;
        
        
        private final String msg;
        
        public InstancesHandler(String msg, Level level) {
            setLevel(level);
            this.msg = msg;
        }

        @Override
        public void publish(LogRecord record) {
            Object[] param = record.getParameters();
            if (param == null) {
                return;
            }
            if (msg != null && !msg.equals(record.getMessage())) {
                return;
            }
            cnt++;
            for (Object o : param) {
                instances.put(o, record.getMessage());
            }
        }

        @Override
        public void flush() {
        }

        @Override
        public void close() throws SecurityException {
        }
        
        public static void assertGC(String msg) {
            if (cnt == 0) {
                Assert.fail("No objects to track reported: " + cnt);
            }
            // start from scratch
            cnt = 0;
            
            AssertionFailedError t = null;
            
            List<Reference> refs = new ArrayList<Reference>();
            List<String> txts = new ArrayList<String>();
            int count = 0;
            synchronized (instances) {
                for (Map.Entry<Object, String> entry : instances.entrySet()) {
                    refs.add(new WeakReference<Object>(entry.getKey()));
                    txts.add(entry.getValue());
                    count++;
                }
                instances.clear();
            }
            
            for (int i = 0; i < count; i++) {
                Reference<?> r = refs.get(i);
                try {
                    NbTestCase.assertGC(msg + " " + txts.get(i), r);
                } catch (AssertionFailedError ex) {
                    if (t == null) {
                        t = ex;
                    } else {
                        Throwable last = t;
                        while (last.getCause() != null) {
                            last = last.getCause();
                        }
                        last.initCause(ex);
                    }
                }
            }
            if (t != null) {
                throw t;
            }
        }
        
    } // end of InstancesHandler
}
