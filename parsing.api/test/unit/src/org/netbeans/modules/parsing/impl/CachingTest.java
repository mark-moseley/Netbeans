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
package org.netbeans.modules.parsing.impl;

import java.util.Collections;
import java.util.Iterator;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import javax.swing.event.ChangeListener;

import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.mimelookup.test.MockMimeLookup;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.parsing.api.Embedding;
import org.netbeans.modules.parsing.api.MyScheduler;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.Task;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.EmbeddingProvider;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.parsing.spi.Parser.Result;
import org.netbeans.modules.parsing.spi.ParserFactory;
import org.netbeans.modules.parsing.spi.ParserResultTask;
import org.netbeans.modules.parsing.spi.SchedulerEvent;
import org.netbeans.modules.parsing.spi.SchedulerTask;
import org.netbeans.modules.parsing.spi.SourceModificationEvent;
import org.netbeans.modules.parsing.spi.TaskFactory;
import org.netbeans.modules.parsing.spi.Scheduler;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;


/**
 *
 * @author hanz
 */
public class CachingTest extends NbTestCase {
    
    public CachingTest (String testName) {
        super (testName);
    }

    public void testCaching () throws Exception {

        // 1) register tasks and parsers
        MockServices.setServices (MockMimeLookup.class, MyScheduler.class);
        final CountDownLatch        latch1 = new CountDownLatch (1);
        final CountDownLatch        latch2 = new CountDownLatch (3);

        final int[]                 fooParser = {1};
        final int[]                 fooParserResult = {1};
        final int[]                 fooEmbeddingProvider = {1};
        final int[]                 fooTask = {1};
        final int[]                 booParser = {1};
        final int[]                 booParserResult = {1};
        final int[]                 booTask = {1};
        final TestComparator        test = new TestComparator (
            "1 - schedule SchedulerEvent 1\n" +
            "foo get embeddings 1 (Snapshot 1), \n" +
            "foo parse 1 (Snapshot 1, FooParserResultTask 1, SourceModificationEvent -1:-1), \n" +
            "foo get result 1 (FooParserResultTask 1), \n" +
            "foo task 1 (FooResult 1 (Snapshot 1), SchedulerEvent 1), \n" +
            "foo invalidate 1, \n" +
            "boo parse 1 (Snapshot 2, BooParserResultTask 1, SourceModificationEvent -1:-1), \n" +
            "boo get result 1 (BooParserResultTask 1), \n" +
            "boo task 1 (BooResult 1 (Snapshot 2), SchedulerEvent 1), \n" +
            "boo invalidate 1, \n" +
            "2 - run user task\n" +
            "foo get result 1 (MyUserTask), \n" +
            "user task (Snapshot 1, FooResult 2 (Snapshot 1)), \n" +
            "boo get result 1 (MyUserTask), \n" +
            "user task - embedding text/boo (Snapshot 2, BooResult 2 (Snapshot 2)), \n" +
            "false, \n" +
            "foo invalidate 2, \n" +
            "boo invalidate 2, \n" +
            "3 - schedule SchedulerEvent 2\n" +
            "foo get embeddings 1 (Snapshot 1), \n" + //XXX:DELETE!
            "foo get result 1 (FooParserResultTask 1), \n" +
            "foo task 1 (FooResult 3 (Snapshot 1), SchedulerEvent 2), \n" +
            "foo invalidate 3, \n" +
            "boo get result 1 (BooParserResultTask 1), \n" +
            "boo task 1 (BooResult 3 (Snapshot 2), SchedulerEvent 2), \n" +
            "boo invalidate 3, \n" +
            "4 - end\n"
        );

        MockMimeLookup.setInstances (
            MimePath.get ("text/foo"),
            new ParserFactory () {
                public Parser createParser (Collection<Snapshot> snapshots2) {
                    return new Parser () {

                        private Snapshot last;
                        private int i = fooParser [0]++;

                        public void parse (Snapshot snapshot, Task task, SourceModificationEvent event) throws ParseException {
                            test.check ("foo parse " + i + " (Snapshot " + test.get (snapshot) + ", " + task + ", " + event + "), \n");
                            last = snapshot;

                        }

                        public Result getResult (Task task) throws ParseException {
                            test.check ("foo get result " + i + " (" + task + "), \n");
                            return new Result (last) {

                                private int i = fooParserResult [0]++;

                                public void invalidate () {
                                    test.check ("foo invalidate " + i + ", \n");
                                }

                                @Override
                                public String toString () {return "FooResult " + i + " (Snapshot " + test.get (getSnapshot ()) + ")";}
                            };
                        }

                        public void cancel () {}

                        public void addChangeListener (ChangeListener changeListener) {}

                        public void removeChangeListener (ChangeListener changeListener) {}

                        @Override public String toString () {return "FooParser";}
                    };
                }
            },
            new TaskFactory () {
                public Collection<SchedulerTask> create (Snapshot snapshot) {
                    return Arrays.asList (new SchedulerTask[] {
                        new EmbeddingProvider() {

                            private int i = fooEmbeddingProvider [0]++;

                            public List<Embedding> getEmbeddings (Snapshot snapshot) {
                                test.check ("foo get embeddings " + i + " (Snapshot " + test.get (snapshot) + "), \n");
                                Embedding embedding = snapshot.create (10, 10, "text/boo");
                                test.get (embedding.getSnapshot ());
                                return Arrays.asList (new Embedding[] {
                                    embedding
                                });
                            }

                            public int getPriority () {return 10;}

                            public void cancel () {}

                            @Override public String toString () {return "FooEmbeddingProvider " + getPriority ();}
                        },
                        new ParserResultTask () {

                            private int i = fooTask [0]++;

                            public void run (Result result, SchedulerEvent event) {
                                test.check ("foo task " + i + " (" + result + ", SchedulerEvent " + test.get (event) + "), \n");
                            }

                            public int getPriority () {
                                return 100;
                            }

                            public Class<? extends Scheduler> getSchedulerClass () {
                                return MyScheduler.class;
                            }

                            public void cancel () {
                            }

                            @Override
                            public String toString () {
                                return "FooParserResultTask " + i;
                            }
                        }

                    });
                }
            }

        );
        MockMimeLookup.setInstances (
            MimePath.get ("text/boo"),
            new ParserFactory () {
                public Parser createParser (Collection<Snapshot> snapshots2) {
                    return new Parser () {

                        private Snapshot last;
                        private int i = booParser [0]++;

                        public void parse (Snapshot snapshot, Task task, SourceModificationEvent event) throws ParseException {
                            test.check ("boo parse " + i + " (Snapshot " + test.get (snapshot) + ", " + task + ", " + event + "), \n");
                            last = snapshot;
                        }

                        public Result getResult (Task task) throws ParseException {
                            test.check ("boo get result " + i + " (" + task + "), \n");
                            return new Result (last) {

                                private int i = booParserResult [0]++;

                                public void invalidate () {
                                    test.check ("boo invalidate " + i + ", \n");
                                    latch1.countDown ();
                                    latch2.countDown ();
                                }

                                @Override public String toString () {return "BooResult " + i + " (Snapshot " + test.get (getSnapshot ()) + ")";}
                            };
                        }

                        public void cancel () {}

                        public void addChangeListener (ChangeListener changeListener) {}

                        public void removeChangeListener (ChangeListener changeListener) {}

                        @Override public String toString () {return "BooParser";}
                    };
                }
            },
            new TaskFactory () {
                public Collection<SchedulerTask> create (Snapshot snapshot) {
                    return Arrays.asList (new SchedulerTask[] {
                        new ParserResultTask () {

                            private int i = booTask [0]++;

                            public void run (Result result, SchedulerEvent event) {
                                test.check ("boo task " + i + " (" + result + ", SchedulerEvent " + test.get (event) + "), \n");
                            }

                            public int getPriority () {return 150;}

                            public Class<? extends Scheduler> getSchedulerClass () {return MyScheduler.class;}

                            public void cancel () {}

                            @Override public String toString () {return "BooParserResultTask " + i;}
                        }
                    });
                }
            }
        );

        // 2) create source file
        clearWorkDir ();
        //Collection c = MimeLookup.getLookup("text/boo").lookupAll (ParserFactory.class);
        FileObject workDir = FileUtil.toFileObject (getWorkDir ());
        FileObject testFile = FileUtil.createData (workDir, "bla.foo");
        FileUtil.setMIMEType ("foo", "text/foo");
        OutputStream outputStream = testFile.getOutputStream ();
        OutputStreamWriter writer = new OutputStreamWriter (outputStream);
        writer.append ("Toto je testovaci file, na kterem se budou delat hnusne pokusy!!!");
        writer.close ();
        Source source = Source.create (testFile);
        SchedulerEvent event1 = new ASchedulerEvent ();
        test.check ("1 - schedule SchedulerEvent " + test.get (event1) + "\n");

        MyScheduler.schedule2 (source, event1);
        latch1.await ();
        test.check ("2 - run user task\n");

        ParserManager.parse (
            Collections.<Source>singleton (source),
            new UserTask () {
                @Override
                public void run (ResultIterator resultIterator) throws Exception {
                    test.check ("user task (Snapshot " + test.get (resultIterator.getSnapshot ()) + ", " + resultIterator.getParserResult () + "), \n");
                    Iterator<Embedding> it = resultIterator.getEmbeddings ().iterator ();
                    Embedding embedding = it.next ();
                    ResultIterator resultIterator1 = resultIterator.getResultIterator (embedding);
                    test.check ("user task - embedding " + embedding.getMimeType () + " (Snapshot " + test.get (resultIterator1.getSnapshot ()) + ", " + resultIterator1.getParserResult () + "), \n");
                    test.check ("" + it.hasNext () + ", \n");
                }
                @Override public String toString () {return "MyUserTask";}
            }
        );
        SchedulerEvent event2 = new ASchedulerEvent ();
        test.check ("3 - schedule SchedulerEvent " + test.get (event2) + "\n");

        MyScheduler.schedule2 (source, event2);
        latch2.await ();
        test.check ("4 - end\n");
        assertEquals ("", test.getResult ());
    }
}







