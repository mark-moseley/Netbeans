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
package org.netbeans.modules.ruby.rubyproject.rake;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.api.project.Project;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.util.Exceptions;

final class RakeTaskReader {

    /** Error message emitted by 'rake' tool during fail. */
    private static final String RAKE_ABORTED = "rake aborted!"; // NOI18N

    private final Project project;

    RakeTaskReader(final Project project) {
        this.project = project;
    }

    List<RakeTask> getRakeTaskTree() {
        try {
            String rawOutput = rawRead();
            return rawOutput == null
                    ? Collections.<RakeTask>emptyList()
                    : parseTasks(new StringReader(rawOutput));
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
            return Collections.emptyList();
        }
    }

    String rawRead() {
        final FileObject projectDir = project.getProjectDirectory();
        try {
            final StringBuilder sb = new StringBuilder(5000);
            projectDir.getFileSystem().runAtomicAction(new FileSystem.AtomicAction() {
                public void run() throws IOException {
                    FileObject rakeTasksFile = projectDir.getFileObject(RakeSupport.RAKE_T_OUTPUT);

                    if (rakeTasksFile == null) {
                        return;
                    }

                    BufferedReader reader = null;
                    try {
                        InputStream is = rakeTasksFile.getInputStream();
                        reader = new BufferedReader(new InputStreamReader(is));

                        while (true) {
                            String line = reader.readLine();
                            if (line == null) {
                                break;
                            }
                            sb.append(line);
                            sb.append('\n');
                        }
                    } finally {
                        if (reader != null) {
                            reader.close();
                        }
                    }
                }
            });

            return sb.length() > 0 ? sb.toString() : null;
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
            return null;
        }
    }

    private static List<RakeTask> parseTasks(Reader is) throws IOException {
        BufferedReader reader = new BufferedReader(is);
        List<RakeTask> tasks = new ArrayList<RakeTask>(40);
        Map<String, RakeTask> map = new HashMap<String, RakeTask>(50);
        Set<String> processedTasks = new HashSet<String>();

        while (true) {
            String line = reader.readLine();

            if (line == null) {
                break;
            }

            if (line.startsWith(RAKE_ABORTED)) {
                continue;
            }

            if (!line.startsWith("rake ")) { // NOI18N
                continue;
            }

            int start = 5;
            int end = line.indexOf(' ', start);

            if (end == -1) {
                end = line.indexOf('#');
                if (end == -1) {
                    end = line.length();
                }
            }

            String task = line.substring(start, end);
            if (!processedTasks.add(task)) {
                continue;
            }
            String description = null;
            int descIndex = line.indexOf('#', end);

            if ((descIndex != -1) && (descIndex < (line.length() - 2))) {
                description = line.substring(descIndex + 2);
            }

            // Tokenize into categories (db:fixtures:load -> db | fixtures | load)
            RakeTask parent = null;
            String[] path = task.split(":"); // NOI18N
            StringBuilder sb = new StringBuilder();

            for (int i = 0; i < (path.length - 1); i++) {
                if (sb.length() > 0) {
                    sb.append(':');
                }

                sb.append(path[i]);

                String folderPath = sb.toString();
                RakeTask p = map.get(folderPath);

                if (p == null) {
                    RakeTask r = RakeTask.newNameSpace(path[i]);

                    if (parent == null) {
                        tasks.add(r);
                    } else {
                        parent.addChild(r);
                    }

                    map.put(folderPath, r);
                    parent = r;
                } else {
                    parent = p;
                }
            }

            RakeTask t = new RakeTask(task, path[path.length - 1], description);

            if (parent != null) {
                parent.addChild(t);
            } else {
                tasks.add(t);
            }
        }

        return tasks;
    }
    
}
