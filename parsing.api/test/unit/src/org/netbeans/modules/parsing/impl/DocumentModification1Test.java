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

import javax.swing.text.Document;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.TokenHierarchy;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import javax.swing.event.ChangeListener;

import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.mimelookup.test.MockMimeLookup;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.parsing.api.Embedding;
import org.netbeans.modules.parsing.api.MyScheduler;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.Task;
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
public class DocumentModification1Test extends NbTestCase {
    
    public DocumentModification1Test (String testName) {
        super (testName);
    }

    /**
     * @throws java.lang.Exception
     */
    public void testDocumentModification () throws Exception {

        // 1) register tasks and parsers
        MockServices.setServices (MockMimeLookup.class, MyScheduler.class);
        final CountDownLatch        latch1 = new CountDownLatch (1);
        final CountDownLatch        latch2 = new CountDownLatch (2);
        final CountDownLatch        latch3 = new CountDownLatch (3);
        final int[]                 fooParser = {1};
        final int[]                 fooParserResult = {1};
        final int[]                 fooEmbeddingProvider = {1};
        final int[]                 fooTask = {1};
        final int[]                 booParser = {1};
        final int[]                 booParserResult = {1};
        final int[]                 booTask = {1};
        final TestComparator        test = new TestComparator (
            "1 - reschedule all schedulers\n" +
            "foo get embeddings 1 (Snapshot 1), \n" +
            "Snapshot 1: Toto je testovaci file, na kterem se budou delat hnusne pokusy!!!, \n" +
            "Snapshot 2: stovaci fi, \n" +
            "foo parse 1 (Snapshot 1, FooParserResultTask 1, SourceModificationEvent -1:-1), \n" +
            "foo get result 1 (FooParserResultTask 1), \n" +
            "foo task 1 (FooResult 1 (Snapshot 1), SchedulerEvent 1), \n" +
            "foo invalidate 1, \n" +
            "boo parse 1 (Snapshot 2, BooParserResultTask 1, SourceModificationEvent -1:-1), \n" +
            "boo get result 1 (BooParserResultTask 1), \n" +
            "boo task 1 (BooResult 1 (Snapshot 2), SchedulerEvent 1), \n" +
            "boo invalidate 1, \n" +
            "2 - insert 14 chars on offset 22\n" +
            "foo get embeddings 1 (Snapshot 3), \n" +
            "Snapshot 3: Toto je testovaci file (druha verze), na kterem se budou delat hnusne pokusy!!!, \n" +
            "Snapshot 4: stovaci fi, \n" +
            "foo parse 1 (Snapshot 3, FooParserResultTask 1, SourceModificationEvent 18:37), \n" +
            "foo get result 1 (FooParserResultTask 1), \n" +
            "foo task 1 (FooResult 2 (Snapshot 3), SchedulerEvent 1), \n" +
            "foo invalidate 2, \n" +
            "boo parse 1 (Snapshot 4, BooParserResultTask 1, SourceModificationEvent -1:-1), \n" + //!! source unchanged
            "boo get result 1 (BooParserResultTask 1), \n" +
            "boo task 1 (BooResult 2 (Snapshot 4), SchedulerEvent 1), \n" +
            "boo invalidate 2, \n" +
            "3 - remove 5 chars on offset 44\n" +
            "foo get embeddings 1 (Snapshot 5), \n" +
            "Snapshot 5: Toto je testovaci file (druha verze), na ktee budou delat hnusne pokusy!!!, \n" +
            "Snapshot 6: stovaci fi, \n" +
            "foo parse 1 (Snapshot 5, FooParserResultTask 1, SourceModificationEvent 41:45), \n" +
            "foo get result 1 (FooParserResultTask 1), \n" +
            "foo task 1 (FooResult 3 (Snapshot 5), SchedulerEvent 2), \n" +
            "foo invalidate 3, \n" +
            "boo parse 1 (Snapshot 6, BooParserResultTask 1, SourceModificationEvent -1:-1), \n" + //!! source unchanged
            "boo get result 1 (BooParserResultTask 1), \n" +
            "boo task 1 (BooResult 3 (Snapshot 6), SchedulerEvent 2), \n" +
            "boo invalidate 3, \n" +
            "4 - end\n"
        );

        MockMimeLookup.setInstances (
            MimePath.get ("text/foo"),
            new ParserFactory () {
                public Parser createParser (Collection<Snapshot> snapshots2) {
                    return new Parser () {

                        private Snapshot        last;
                        private int             i = fooParser [0]++;

                        public void parse (Snapshot snapshot, Task task, SourceModificationEvent event) throws ParseException {
                            test.check ("foo parse " + i + " (Snapshot " + test.get (snapshot) + ", " + task + ", " + event + "), \n");
                            last = snapshot;
                        }

                        public Result getResult (Task task) throws ParseException {
                            test.check ("foo get result " + i + " (" + task + "), \n");
                            return new Result (last) {

                                public void invalidate () {
                                    test.check ("foo invalidate " + i + ", \n");
                                }

                                private int i = fooParserResult [0]++;

                                @Override
                                public String toString () {return "FooResult " + i + " (Snapshot " + test.get (getSnapshot ()) + ")";}
                            };
                        }

                        public void cancel () {
                        }

                        public void addChangeListener (ChangeListener changeListener) {
                        }

                        public void removeChangeListener (ChangeListener changeListener) {
                        }
                    };
                }
            },
            new TaskFactory () {
                public Collection<SchedulerTask> create (Snapshot snapshot) {
                    return Arrays.asList (new SchedulerTask[] {
                        new EmbeddingProvider () {

                            private int i = fooEmbeddingProvider [0]++;

                            public List<Embedding> getEmbeddings (Snapshot snapshot) {
                                test.check ("foo get embeddings " + i + " (Snapshot " + test.get (snapshot) + "), \n");
                                test.check ("Snapshot " + test.get (snapshot) + ": " + snapshot.getText () + ", \n");
                                Embedding embedding = snapshot.create (10, 10, "text/boo");
                                test.get (embedding.getSnapshot ());
                                test.check ("Snapshot " + test.get (embedding.getSnapshot ()) + ": " + embedding.getSnapshot ().getText () + ", \n");
                                return Arrays.asList (new Embedding[] {
                                    embedding
                                });
                            }

                            public int getPriority () {
                                return 10;
                            }

                            public void cancel () {
                            }
                        },
                        new ParserResultTask () {


                            public void run (Result result, SchedulerEvent event) {
                                test.check ("foo task " + i + " (" + result + ", SchedulerEvent " + test.get (event) + "), \n");
                            }

                            public int getPriority () {
                                return 100;
                            }

                            public Class<? extends Scheduler> getSchedulerClass () {
                                return Scheduler.EDITOR_SENSITIVE_TASK_SCHEDULER;
                            }

                            public void cancel () {
                            }

                            private int i = fooTask [0]++;

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
                                public void invalidate () {
                                    test.check ("boo invalidate " + i + ", \n");
                                    latch1.countDown ();
                                    latch2.countDown ();
                                    latch3.countDown ();
                                }

                                private int i = booParserResult [0]++;

                                @Override
                                public String toString () {return "BooResult " + i + " (Snapshot " + test.get (getSnapshot ()) + ")";}
                            };
                        }

                        public void cancel () {
                        }

                        public void addChangeListener (ChangeListener changeListener) {
                        }

                        public void removeChangeListener (ChangeListener changeListener) {
                        }
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

                            public int getPriority () {
                                return 150;
                            }

                            public Class<? extends Scheduler> getSchedulerClass () {
                                return Scheduler.EDITOR_SENSITIVE_TASK_SCHEDULER;
                            }

                            public void cancel () {
                            }

                            @Override
                            public String toString () {
                                return "BooParserResultTask " + i;
                            }
                        }
                    });
                }
            }
        );

        // 2) create source file
        clearWorkDir ();
        FileObject workDir = FileUtil.toFileObject (getWorkDir ());
        FileObject testFile = FileUtil.createData (workDir, "bla.foo");
        FileUtil.setMIMEType ("foo", "text/foo");
        OutputStream outputStream = testFile.getOutputStream ();
        OutputStreamWriter writer = new OutputStreamWriter (outputStream);
        writer.append ("Toto je testovaci file, na kterem se budou delat hnusne pokusy!!!");
        writer.close ();
        Source source = Source.create (testFile);
        Document document = source.getDocument (true);
        document.putProperty ("mimeType", "text/foo");
        document.putProperty (Language.class, new ALanguageHierarchy ().language ());
        TokenHierarchy th = TokenHierarchy.get (document);
        TokenSequence ts = th.tokenSequence();
        ts.tokenCount ();
        test.check ("1 - reschedule all schedulers\n");

        // 3) shcedulle CurrentDocumentScheduler
        for (Scheduler scheduler : Schedulers.getSchedulers ())
            if (scheduler instanceof CurrentDocumentScheduler)
                ((CurrentDocumentScheduler) scheduler).schedule (source);
        latch1.await ();
        test.check ("2 - insert 14 chars on offset 22\n");

        document.insertString (22, " (druha verze)", null);
        latch2.await ();
        test.check ("3 - remove 5 chars on offset 44\n");

        document.remove (44, 5);
        latch3.await ();
        test.check ("4 - end\n");

        assertEquals ("", test.getResult ());
    }
}







