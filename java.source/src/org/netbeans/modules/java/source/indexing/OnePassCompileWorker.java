/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.java.source.indexing;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.tools.javac.api.JavacTaskImpl;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.util.Abort;
import com.sun.tools.javac.util.MissingPlatformError;
import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileManager;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.modules.java.source.parsing.JavacParser;
import org.netbeans.modules.java.source.parsing.OutputFileManager;
import org.netbeans.modules.java.source.tasklist.RebuildOraculum;
import org.netbeans.modules.java.source.tasklist.TaskCache;
import org.netbeans.modules.java.source.usages.ClasspathInfoAccessor;
import org.netbeans.modules.java.source.usages.ExecutableFilesIndex;
import org.netbeans.modules.java.source.usages.Pair;
import org.netbeans.modules.java.source.util.LowMemoryNotifier;
import org.netbeans.modules.parsing.spi.indexing.Context;
import org.netbeans.modules.parsing.spi.indexing.Indexable;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

/**
 *
 * @author Jan Lahoda, Dusan Balek
 */
final class OnePassCompileWorker extends CompileWorker {

    ParsingOutput compile(ParsingOutput previous, Context context, JavaParsingContext javaContext, Iterable<? extends Indexable> files) {
        final JavaFileManager fileManager = ClasspathInfoAccessor.getINSTANCE().getFileManager(javaContext.cpInfo);
        final Map<URI, List<String>> file2FQNs = new HashMap<URI, List<String>>();
        final Set<ElementHandle<TypeElement>> addedTypes = new HashSet<ElementHandle<TypeElement>>();
        final Set<Indexable> finished = new HashSet<Indexable>();
        final Map<URL, Set<URL>> root2Rebuild = new HashMap<URL, Set<URL>>();

        final LowMemoryListenerImpl mem = new LowMemoryListenerImpl();
        LowMemoryNotifier.getDefault().addLowMemoryListener(mem);

        try {
            final DiagnosticListenerImpl dc = new DiagnosticListenerImpl();
            final LinkedList<Pair<CompilationUnitTree, CompileTuple>> units = new LinkedList<Pair<CompilationUnitTree, CompileTuple>>();
            JavacTaskImpl jt = null;
            boolean stopAfterParse = false;

            for (Indexable i : files) {
                try {
                    if (mem.isLowMemory()) {
                        stopAfterParse = true;
                        jt = null;
                        units.clear();
                        System.gc();
                    }
                    if (jt == null) {
                        jt = JavacParser.createJavacTask(javaContext.cpInfo, dc, javaContext.sourceLevel, null);
                    }
                    CompileTuple tuple = createTuple(context, javaContext, i);
                    for (CompilationUnitTree cut : jt.parse(tuple.jfo)) { //TODO: should be exactly one
                        if (!stopAfterParse)
                            units.add(Pair.<CompilationUnitTree, CompileTuple>of(cut, tuple));
                        computeFQNs(file2FQNs, cut, tuple.jfo);
                    }
                } catch (Throwable t) {
                    if (JavaCustomIndexer.LOG.isLoggable(Level.FINEST)) {
                        final ClassPath bootPath   = javaContext.cpInfo.getClassPath(ClasspathInfo.PathKind.BOOT);
                        final ClassPath classPath  = javaContext.cpInfo.getClassPath(ClasspathInfo.PathKind.COMPILE);
                        final ClassPath sourcePath = javaContext.cpInfo.getClassPath(ClasspathInfo.PathKind.SOURCE);
                        final String message = String.format("OnePassCompileWorker caused an exception Root: %s File: %s Bootpath: %s Classpath: %s Sourcepath: %s", //NOI18N
                                    FileUtil.getFileDisplayName(context.getRoot()),
                                    i.getURL().toString(),
                                    bootPath == null   ? null : bootPath.toString(),
                                    classPath == null  ? null : classPath.toString(),
                                    sourcePath == null ? null : sourcePath.toString()
                                    );
                        JavaCustomIndexer.LOG.log(Level.FINEST, message, t);  //NOI18N
                    }
                    if (t instanceof ThreadDeath) {
                        throw (ThreadDeath) t;
                    }
                    else {
                        stopAfterParse = true;
                        jt = null;
                        units.clear();
                        System.gc();
                        final ClassPath bootPath   = javaContext.cpInfo.getClassPath(ClasspathInfo.PathKind.BOOT);
                        final ClassPath classPath  = javaContext.cpInfo.getClassPath(ClasspathInfo.PathKind.COMPILE);
                        final ClassPath sourcePath = javaContext.cpInfo.getClassPath(ClasspathInfo.PathKind.SOURCE);
                        t = Exceptions.attachMessage(t, String.format("Root: %s File: %s Bootpath: %s Classpath: %s Sourcepath: %s", //NOI18N
                                FileUtil.getFileDisplayName(context.getRoot()),
                                i.getURL().toString(),
                                bootPath == null   ? null : bootPath.toString(),
                                classPath == null  ? null : classPath.toString(),
                                sourcePath == null ? null : sourcePath.toString()
                                ));
                        Exceptions.printStackTrace(t);
                    }
                }
            }

            if (stopAfterParse) {
                return new ParsingOutput(false, file2FQNs, addedTypes, Collections.<File>emptyList(), finished, root2Rebuild);
            }

            CompileTuple active = null;
            try {
                for (Pair<CompilationUnitTree, CompileTuple> unit : units) {
                    active = unit.second;
                    if (mem.isLowMemory()) {
                        System.gc();
                        return new ParsingOutput(false, file2FQNs, addedTypes, Collections.<File>emptyList()/*XXX*/, finished, root2Rebuild);
                    }
                    Iterable<? extends TypeElement> types = jt.enterTrees(Collections.singletonList(unit.first));
                    if (mem.isLowMemory()) {
                        System.gc();
                        return new ParsingOutput(false, file2FQNs, addedTypes, Collections.<File>emptyList()/*XXX*/, finished, root2Rebuild);
                    }
                    jt.analyze(types);
                    if (mem.isLowMemory()) {
                        System.gc();
                        return new ParsingOutput(false, file2FQNs, addedTypes, Collections.<File>emptyList()/*XXX*/, finished, root2Rebuild);
                    }
                    boolean[] main = new boolean[1];
                    javaContext.sa.analyse(Collections.singleton(unit.first), jt, fileManager, false, true, active.jfo, addedTypes, main);
                    ExecutableFilesIndex.DEFAULT.setMainClass(context.getRoot().getURL(), active.jfo.toUri().toURL(), main[0]);
                    if (mem.isLowMemory()) {
                        System.gc();
                        return new ParsingOutput(false, file2FQNs, addedTypes, Collections.<File>emptyList()/*XXX*/, finished, root2Rebuild);
                    }
                    if (!context.isSupplementaryFilesIndexing()) {
                        for (Map.Entry<URL, Collection<URL>> toRebuild : RebuildOraculum.findFilesToRebuild(context.getRootURI(), active.jfo.toUri().toURL(), javaContext.cpInfo, jt.getElements(), types).entrySet()) {
                            Set<URL> urls = root2Rebuild.get(toRebuild.getKey());
                            if (urls == null) {
                                root2Rebuild.put(toRebuild.getKey(), urls = new HashSet<URL>());
                            }
                            urls.addAll(toRebuild.getValue());
                    }
                    }
                    jt.generate(types);
                    TaskCache.getDefault().dumpErrors(context.getRootURI(), active.indexable.getURL(), dc.getDiagnostics(active.jfo));
                    finished.add(active.indexable);
                }
                return new ParsingOutput(true, file2FQNs, addedTypes, Collections.<File>emptyList(), finished, root2Rebuild);
            } catch (Throwable t) {
                if (JavaCustomIndexer.LOG.isLoggable(Level.FINEST)) {
                    final ClassPath bootPath   = javaContext.cpInfo.getClassPath(ClasspathInfo.PathKind.BOOT);
                    final ClassPath classPath  = javaContext.cpInfo.getClassPath(ClasspathInfo.PathKind.COMPILE);
                    final ClassPath sourcePath = javaContext.cpInfo.getClassPath(ClasspathInfo.PathKind.SOURCE);
                    final String message = String.format("OnePassCompileWorker caused an exception Root: %s File: %s Bootpath: %s Classpath: %s Sourcepath: %s", //NOI18N
                                FileUtil.getFileDisplayName(context.getRoot()),
                                active.jfo.toUri().toString(),
                                bootPath == null   ? null : bootPath.toString(),
                                classPath == null  ? null : classPath.toString(),
                                sourcePath == null ? null : sourcePath.toString()
                                );
                    JavaCustomIndexer.LOG.log(Level.FINEST, message, t);  //NOI18N
                }
                if (t instanceof ThreadDeath) {
                    throw (ThreadDeath) t;
                }
                else if (t instanceof OutputFileManager.InvalidSourcePath) {
                    //Handled above
                    throw (OutputFileManager.InvalidSourcePath) t;
                }
                else if (t instanceof MissingPlatformError) {
                    //Handled above
                    throw (MissingPlatformError) t;
                }
                else {
                    if (!(t instanceof Abort || t instanceof Symbol.CompletionFailure)) {
                        final ClassPath bootPath   = javaContext.cpInfo.getClassPath(ClasspathInfo.PathKind.BOOT);
                        final ClassPath classPath  = javaContext.cpInfo.getClassPath(ClasspathInfo.PathKind.COMPILE);
                        final ClassPath sourcePath = javaContext.cpInfo.getClassPath(ClasspathInfo.PathKind.SOURCE);
                        t = Exceptions.attachMessage(t, String.format("Root: %s File: %s Bootpath: %s Classpath: %s Sourcepath: %s", //NOI18N
                                FileUtil.getFileDisplayName(context.getRoot()),
                                active.jfo.toUri().toString(),
                                bootPath == null   ? null : bootPath.toString(),
                                classPath == null  ? null : classPath.toString(),
                                sourcePath == null ? null : sourcePath.toString()
                                ));
                        Exceptions.printStackTrace(t);
                    }
                }
            }
            return new ParsingOutput(false, file2FQNs, addedTypes, Collections.<File>emptyList(), finished, root2Rebuild);
        } finally {
            LowMemoryNotifier.getDefault().removeLowMemoryListener(mem);
        }
    }
}
