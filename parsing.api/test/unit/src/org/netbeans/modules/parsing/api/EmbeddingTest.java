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

package org.netbeans.modules.parsing.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.mimelookup.test.MockMimeLookup;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.parsing.spi.EmbeddingProvider;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.SchedulerTask;
import org.netbeans.modules.parsing.spi.TaskFactory;

/**
 *
 * @author vita
 */
public class EmbeddingTest extends NbTestCase {

    public EmbeddingTest(String name) {
        super(name);
    }

    public void testInjectedEmbeddings() throws ParseException {
        final String mimeType = "text/x-" + getName();
        final String embeddedMimeType = "text/x-embedded-" + getName();
        MockMimeLookup.setInstances(MimePath.parse(mimeType), new TaskFactory() {
            public @Override Collection<? extends SchedulerTask> create(Snapshot snapshot) {
                return Collections.singleton(new EmbeddingProvider() {
                    public @Override List<Embedding> getEmbeddings(Snapshot snapshot) {
                        List<Embedding> embeddings = new ArrayList<Embedding>();
                        embeddings.add(snapshot.create("Embedded Section 1\n", embeddedMimeType));
                        embeddings.add(snapshot.create("Embedded Section 2\n", embeddedMimeType));
                        embeddings.add(snapshot.create("Embedded Section 3\n", embeddedMimeType));
                        return Collections.singletonList(Embedding.create(embeddings));
                    }

                    public @Override int getPriority() {
                        return Integer.MAX_VALUE;
                    }

                    public @Override void cancel() {
                    }
                });
            }
        });

        EditorKit kit = new DefaultEditorKit();
        Document doc = kit.createDefaultDocument();
        doc.putProperty("mimeType", mimeType);

        Source source = Source.create(doc);
        ParserManager.parse(Collections.singleton(source), new UserTask() {
            public @Override void run(ResultIterator resultIterator) throws Exception {
                assertEquals(mimeType, resultIterator.getSnapshot().getMimeType());

                int cnt = 0;
                List<Embedding> embeddings = new ArrayList<Embedding>();
                for(Embedding e : resultIterator.getEmbeddings()) {
                    cnt++;
                    if (e.getMimeType().equals(embeddedMimeType)) {
                        embeddings.add(e);
                    }
                }

                assertEquals("Wrong number of embeddings", 1, cnt);
                assertEquals("Wrong number of our mebeddings", 1, embeddings.size());

                Embedding e = embeddings.get(0);
                CharSequence sourceSnapshot = resultIterator.getSnapshot().getText();
                for(int i = 0; i < sourceSnapshot.length(); i++) {
                    assertFalse("Injected embeddings should not contain offset: " + i, e.containsOriginalOffset(i));
                }
            }
        });
    }
}
