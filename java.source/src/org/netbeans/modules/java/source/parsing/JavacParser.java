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

package org.netbeans.modules.java.source.parsing;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;
import com.sun.tools.javac.api.ClassNamesForFileOraculum;
import com.sun.tools.javac.api.JavacTaskImpl;
import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.parser.DocCommentScanner;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCBlock;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import com.sun.tools.javac.util.Abort;
import com.sun.tools.javac.util.CancelAbort;
import com.sun.tools.javac.util.CancelService;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.CouplingAbort;
import com.sun.tools.javac.util.Log;
import com.sun.tools.javadoc.JavadocClassReader;
import com.sun.tools.javadoc.JavadocMemberEnter;
import com.sun.tools.javadoc.Messager;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeEvent;
import  javax.swing.event.ChangeListener;
import javax.swing.text.Document;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticListener;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.ToolProvider;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.queries.SourceLevelQuery;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.ClasspathInfo.PathKind;
import org.netbeans.api.java.source.JavaParserResultTask;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.lexer.TokenChange;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenHierarchyEvent;
import org.netbeans.api.lexer.TokenHierarchyEventType;
import org.netbeans.api.lexer.TokenHierarchyListener;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.lib.editor.util.swing.PositionRegion;
import org.netbeans.modules.java.preprocessorbridge.spi.JavaFileFilterImplementation;
import org.netbeans.modules.java.source.JavaFileFilterQuery;
import org.netbeans.modules.java.source.JavaSourceAccessor;
import org.netbeans.modules.java.source.JavadocEnv;
import org.netbeans.modules.java.source.PostFlowAnalysis;
import org.netbeans.modules.java.source.TreeLoader;
import org.netbeans.modules.java.source.tasklist.CompilerSettings;
import org.netbeans.modules.java.source.usages.ClasspathInfoAccessor;
import org.netbeans.modules.java.source.usages.Index;
import org.netbeans.modules.java.source.usages.Pair;
import org.netbeans.modules.java.source.usages.RepositoryUpdater;
import org.netbeans.modules.java.source.util.LowMemoryEvent;
import org.netbeans.modules.java.source.util.LowMemoryListener;
import org.netbeans.modules.java.source.util.LowMemoryNotifier;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.Task;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.impl.Utilities;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.parsing.spi.ParserResultTask;
import org.netbeans.modules.parsing.spi.SourceModificationEvent;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.modules.SpecificationVersion;
import org.openide.util.ChangeSupport;
import org.openide.util.Exceptions;
import org.openide.util.WeakListeners;

/**
 * Provede Parsing API parser build on the top of Javac (JSR 199)
 * @author Tomas Zezula
 */
//@NotThreadSafe
public class JavacParser extends Parser {
    //Timer logger
    private static final Logger TIME_LOGGER = Logger.getLogger("TIMER");        //NOI18N    
    //Debug logger    
    private static final Logger LOGGER = Logger.getLogger(JavacParser.class.getName());
    //Java Mime Type
    public static final String MIME_TYPE = "text/x-java";
    //JavaFileObjectProvider used by the JavacParser - may be overriden by unit test
    static JavaFileObjectProvider jfoProvider = new DefaultJavaFileObjectProvider (); 
    //No output writer like /dev/null
    private static final PrintWriter DEV_NULL = new PrintWriter(new NullWriter(), false);
    
    //Max number of dump files
    private static final int MAX_DUMPS = 255;
    
    /**
     * Helper map mapping the {@link Phase} to message for performance logger
     */
    private static Map<Phase, String> phase2Message = new HashMap<Phase,String> ();
    
    static {
        phase2Message.put (Phase.PARSED,"Parsed");                              //NOI18N
        phase2Message.put (Phase.ELEMENTS_RESOLVED,"Signatures Attributed");    //NOI18N
        phase2Message.put (Phase.RESOLVED, "Attributed");                       //NOI18N
    }
    
    //Listener support
    private final ChangeSupport listeners = new ChangeSupport(this);
    //Cancelling of parser & index
    private final AtomicBoolean canceled = new AtomicBoolean();
    //When true the parser is a private copy not used by the parsing API, see JavaSourceAccessor.createCompilationController
    private final boolean privateParser;
    //File processed by this javac
    private FileObject file;
    //Root owning the file
    private FileObject root;
    //ClassPaths used by the parser
    private ClasspathInfo cpInfo;
    //Count of files the parser was created for
    private final int sourceCount;
    //Incremental parsing support
    private final boolean supportsReparse;
    //Incremental parsing support
    private final List<Pair<DocPositionRegion,MethodTree>> positions = Collections.synchronizedList(new LinkedList<Pair<DocPositionRegion,MethodTree>>());
    //Incremental parsing support
    //@GuardedBy(this)
    private Pair<DocPositionRegion,MethodTree> changedMethod;
    //Incremental parsing support
    private final DocListener listener;
    //J2ME preprocessor support
    private final FilterListener filterListener;
    //ClasspathInfo Listener
    private final ChangeListener cpInfoListener;
    //Cached javac impl
    private CompilationInfoImpl ciImpl;
    //State of the parser
    private boolean initialized;
    //Type of ClasspathInfo (explicit, implicit)
    private boolean explicitCpInfo;
    //Parser is invalidated, new parser impl need to be created
    private boolean invalid;
    //Last used snapshot
    private Snapshot cachedSnapShot;
    //Lamport clock of parse calls
    private long parseId;
    
    JavacParser (final Collection<Snapshot> snapshots, boolean privateParser) {
        this.privateParser = privateParser;
        this.sourceCount = snapshots.size();
        this.supportsReparse = this.sourceCount == 1 && MIME_TYPE.equals(snapshots.iterator().next().getSource().getMimeType());
        EditorCookie.Observable ec = null;
        JavaFileFilterImplementation filter = null;
        if (this.supportsReparse) {
            final Source source = snapshots.iterator().next().getSource();
            filter = JavaFileFilterQuery.getFilter(source.getFileObject());            
            try {
                final DataObject dobj = DataObject.find(source.getFileObject());
                ec = dobj.getCookie(EditorCookie.Observable.class);
                if (ec == null) {
                    LOGGER.log(Level.FINE,
                        String.format("File: %s has no EditorCookie.Observable", //NOI18N
                        FileUtil.getFileDisplayName (file)));
                }
            } catch (DataObjectNotFoundException e) {
                LOGGER.log(Level.FINE,"Invalid DataObject",e);
            }
        }
        this.filterListener = filter != null ? new FilterListener (filter) : null;
        this.listener = ec != null ? new DocListener(ec) : null;
        this.cpInfoListener = new ClasspathInfoListener (listeners);
    }
    
    private void init (final Snapshot snapshot, final Task task, final boolean singleSource) {
        if (!initialized) {
            final Source source = snapshot.getSource();
            final FileObject file = source.getFileObject();
            assert file != null;
            this.file = file;
            ClasspathInfo _tmpInfo;
            if (task instanceof ClasspathInfoProvider &&
                    (_tmpInfo = ((ClasspathInfoProvider)task).getClasspathInfo()) != null) {
                cpInfo = _tmpInfo;
                explicitCpInfo = true;
            }
            else {
                cpInfo = ClasspathInfo.create(file);
            }
            final ClassPath cp = cpInfo.getClassPath(PathKind.SOURCE);
            assert cp != null;
            this.root = cp.findOwnerRoot(file);
            if (singleSource) {
                cpInfo.addChangeListener(WeakListeners.change(cpInfoListener, cpInfo));
                initialized = true;
            }
        }
        else if (!explicitCpInfo) {
            //Recheck ClasspathInfo if still valid
            assert this.file != null;
            assert cpInfo != null;
            final ClassPath scp = ClassPath.getClassPath(this.file, ClassPath.SOURCE);
            if (scp != cpInfo.getClassPath(PathKind.SOURCE)) {
                //Revalidate
                final Project owner = FileOwnerQuery.getOwner(this.file);
                LOGGER.warning("ClassPath identity changed for " + this.file + ", class path owner: " +       //NOI18N
                        (owner == null ? "null" : (FileUtil.getFileDisplayName(owner.getProjectDirectory())+" ("+owner.getClass()+")")));       //NOI18N
                cpInfo = ClasspathInfo.create(this.file);
                JavaSourceAccessor.getINSTANCE().invalidateCachedClasspathInfo(this.file);                
            }
        }
    }
    
    public void invalidate () {
        this.invalid = true;
    }
        
    @Override
    public void parse(final Snapshot snapshot, final Task task, SourceModificationEvent event) throws ParseException {
        assert task != null;
        assert privateParser || Utilities.holdsParserLock();
        parseId++;
        canceled.set(false);
        try {            
            LOGGER.fine("parse: task: " + task.toString() +"\n" + (snapshot == null ? "null" : snapshot.getText()));      //NOI18N
            if (this.sourceCount == 0) {
                ClasspathInfo _tmpInfo = null;
                if (task instanceof ClasspathInfoProvider &&
                    (_tmpInfo = ((ClasspathInfoProvider)task).getClasspathInfo()) != null) {
                    cpInfo = _tmpInfo;
                    explicitCpInfo = true;
                    ciImpl = new CompilationInfoImpl(cpInfo);
                }
                else {
                    throw new IllegalArgumentException("Task has to provide classpath.");
                }
            }
            else if (this.sourceCount == 1) {
                init (snapshot, task, true);
                boolean needsFullReparse = true;
                if (supportsReparse) {
                    Pair<DocPositionRegion,MethodTree> _changedMethod;                    
                    synchronized (this) {
                        _changedMethod = this.changedMethod;
                        this.changedMethod = null;
                    }
                    if (_changedMethod != null && ciImpl != null) {
                        LOGGER.fine("\t:trying partial reparse:\n" + _changedMethod.first.getText());                           //NOI18N
                        needsFullReparse = !reparseMethod(ciImpl, snapshot, _changedMethod.second, _changedMethod.first.getText());
                    }
                }
                if (needsFullReparse) {
                    ciImpl = createCurrentInfo (this, file, root,snapshot, null);
                    LOGGER.fine("\t:created new javac");                                    //NOI18N
                }
            } 
            else {
                init (snapshot, task, false);
                ciImpl = createCurrentInfo(this, file, root, snapshot,
                        ciImpl == null ? null : ciImpl.getJavacTask());
            }            
            cachedSnapShot = snapshot;
        } catch (IOException ioe) {
            throw new ParseException ("JavacParser failure", ioe);            //NOI18N
        }
    }
    
    @Override
    public JavacParserResult getResult (final Task task) throws ParseException {
        assert ciImpl != null;
        assert privateParser || Utilities.holdsParserLock();
        LOGGER.fine ("getResult: task:" + task.toString());                     //NOI18N
        //Assumes that caller is synchronized by the Parsing API lock
        if (invalid) {
            LOGGER.fine ("\t:invalid, reparse");                                //NOI18N
            invalid = false;
            if (cachedSnapShot != null) {
                parse (cachedSnapShot, task, null);
            }            
        }
        final boolean isJavaParserResultTask = task instanceof JavaParserResultTask;
        final boolean isParserResultTask = task instanceof ParserResultTask;
        final boolean isUserTask = task instanceof UserTask;
        final boolean isClasspathInfoProvider = task instanceof ClasspathInfoProvider;
        if (isClasspathInfoProvider) {
            //Verify validity of explicit classpath
            //Not sure about the parsing.api contract for multiple files
            //maybe not needed and assertion is enough
            final ClasspathInfo providedInfo = ((ClasspathInfoProvider)task).getClasspathInfo();
            if (providedInfo != null && !providedInfo.equals(cpInfo)) {
                if (sourceCount != 0) {
                    LOGGER.fine ("Task "+task+" has changed ClasspathInfo form: " + cpInfo +" to:" + providedInfo); //NOI18N
                }
                assert cachedSnapShot != null;
                parse (cachedSnapShot, task, null);
            }
        }
        JavacParserResult result = null;
        if (isParserResultTask) {
            Phase requiredPhase;
            if (isJavaParserResultTask) {
                requiredPhase = ((JavaParserResultTask)task).getPhase();
            }
            else {
                requiredPhase = JavaSource.Phase.RESOLVED;
                LOGGER.warning("ParserResultTask: " + task + " doesn't provide phase, assuming RESOLVED");                   //NOI18N
            }
            Phase reachedPhase;
            try {
                    reachedPhase = moveToPhase(requiredPhase, ciImpl, true, false, false);
            } catch (IOException ioe) {
                throw new ParseException ("JavacParser failure", ioe);      //NOI18N
            }
            if (reachedPhase.compareTo(requiredPhase)>=0) {
                Index.cancel.set(canceled);
                result = new JavacParserResult(JavaSourceAccessor.getINSTANCE().createCompilationInfo(ciImpl));
            }
        }
        else if (isUserTask) {
            result = new JavacParserResult(JavaSourceAccessor.getINSTANCE().createCompilationController(ciImpl));
        }
        else {
            LOGGER.warning("Ignoring unknown task: " + task);                   //NOI18N
        }
        //Todo: shared = false should replace this
        //for now it creates a new parser and passes it outside the infrastructure
        //used by debugger private api
        if (task instanceof NewComilerTask) {
            NewComilerTask nct = (NewComilerTask)task;
            if (nct.getCompilationController() == null || nct.getTimeStamp() != parseId) {
                try {
                    nct.setCompilationController(
                        JavaSourceAccessor.getINSTANCE().createCompilationController(new CompilationInfoImpl(this, file, root, null, cachedSnapShot, false)),
                        parseId);
                } catch (IOException ioe) {
                    throw new ParseException ("Javac Failure", ioe);
                }
            }
        }
        return result;
    }
    
    @Override
    public void cancel () {
        canceled.set(true);
    }
        
    public void resultFinished (boolean isCancelable) {
        if (isCancelable) {
            Index.cancel.remove();
        }
    }


    @Override
    public void addChangeListener(ChangeListener changeListener) {
        assert changeListener != null;
        this.listeners.addChangeListener(changeListener);
    }
 
    @Override
    public void removeChangeListener(ChangeListener changeListener) {
        assert changeListener != null;
        this.listeners.removeChangeListener(changeListener);
    }
    
    
    /**
     * Returns {@link ClasspathInfo} used by this javac
     * @return the ClasspathInfo
     */
    public ClasspathInfo getClasspathInfo () {
        return this.cpInfo;
    }
                
    /**
     * Moves the Javac into the required {@link JavaSource#Phase}
     * Not synchronized, has to be called under Parsing API lock.
     * @param the required {@link JavaSource#Phase}
     * @parma currentInfo - the javac 
     * @param cancellable when true the method checks cancels
     * @param hasMoreFile true when the parser processes more files in a batch
     * @return the reached phase
     * @throws IOException when the javac throws an exception
     */
    Phase moveToPhase (final Phase phase, final CompilationInfoImpl currentInfo,
            final boolean cancellable, final boolean hasMoreFiles, final boolean clone) throws IOException {
        JavaSource.Phase parserError = currentInfo.parserCrashed;
        assert parserError != null;
        Phase currentPhase = currentInfo.getPhase();        
        LowMemoryNotifier lm = null;
        LMListener lmListener = null;
        if (hasMoreFiles) {
            lm = LowMemoryNotifier.getDefault();
            assert lm != null;
            lmListener = new LMListener ();
            lm.addLowMemoryListener (lmListener);
        }                                
        try {
            if (lmListener != null && lmListener.lowMemory.getAndSet(false)) {
                currentInfo.needsRestart = true;
                return currentPhase;
            }
            if (currentPhase.compareTo(Phase.PARSED)<0 && phase.compareTo(Phase.PARSED)>=0 && phase.compareTo(parserError)<=0) {
                if (cancellable && canceled.get()) {
                    //Keep the currentPhase unchanged, it may happen that an userActionTask
                    //runnig after the phace completion task may still use it.
                    return Phase.MODIFIED;
                }
                long start = System.currentTimeMillis();
                // XXX - this might be with wrong encoding
                Iterable<? extends CompilationUnitTree> trees = currentInfo.getJavacTask().parse(new JavaFileObject[] {currentInfo.jfo});
                assert trees != null : "Did not parse anything";        //NOI18N
                Iterator<? extends CompilationUnitTree> it = trees.iterator();
                assert it.hasNext();
                CompilationUnitTree unit = it.next();
                currentInfo.setCompilationUnit(unit);
                assert !it.hasNext();
                final Document doc = listener == null ? null : listener.document;
                if (doc != null && supportsReparse && !clone) {
                    FindMethodRegionsVisitor v = new FindMethodRegionsVisitor(doc,Trees.instance(currentInfo.getJavacTask()).getSourcePositions(),this.canceled);
                    v.visit(unit, null);
                    synchronized (positions) {
                        positions.clear();
                        positions.addAll(v.getResult());
                    }
                }
                currentPhase = Phase.PARSED;
                long end = System.currentTimeMillis();
                FileObject file = currentInfo.getFileObject();
                TIME_LOGGER.log(Level.FINE, "Compilation Unit",
                    new Object[] {file, unit});

                logTime (file,currentPhase,(end-start));
            }                
            if (lmListener != null && lmListener.lowMemory.getAndSet(false)) {
                currentInfo.needsRestart = true;
                return currentPhase;
            }
            if (currentPhase == Phase.PARSED && phase.compareTo(Phase.ELEMENTS_RESOLVED)>=0 && phase.compareTo(parserError)<=0) {
                if (cancellable && canceled.get()) {
                    return Phase.MODIFIED;
                }
                long start = System.currentTimeMillis();
                currentInfo.getJavacTask().enter();
                currentPhase = Phase.ELEMENTS_RESOLVED;
                long end = System.currentTimeMillis();
                logTime(currentInfo.getFileObject(),currentPhase,(end-start));
           }
            if (lmListener != null && lmListener.lowMemory.getAndSet(false)) {
                currentInfo.needsRestart = true;
                return currentPhase;
            }
            if (currentPhase == Phase.ELEMENTS_RESOLVED && phase.compareTo(Phase.RESOLVED)>=0 && phase.compareTo(parserError)<=0) {
                if (cancellable && canceled.get()) {
                    return Phase.MODIFIED;
                }
                long start = System.currentTimeMillis ();
                JavacTaskImpl jti = currentInfo.getJavacTask();
                PostFlowAnalysis.analyze(jti.analyze(), jti.getContext());
                currentPhase = Phase.RESOLVED;
                long end = System.currentTimeMillis ();
                logTime(currentInfo.getFileObject(),currentPhase,(end-start));
            }
            if (lmListener != null && lmListener.lowMemory.getAndSet(false)) {
                currentInfo.needsRestart = true;
                return currentPhase;
            }
            if (currentPhase == Phase.RESOLVED && phase.compareTo(Phase.UP_TO_DATE)>=0) {
                currentPhase = Phase.UP_TO_DATE;
            }
        } catch (CouplingAbort a) {
            RepositoryUpdater.couplingAbort(a, currentInfo.jfo);
            currentInfo.needsRestart = true;
            return currentPhase;            
        } catch (CancelAbort ca) {
            currentPhase = Phase.MODIFIED;
        } catch (Abort abort) {
            parserError = currentPhase;
        } catch (IOException ex) {
            currentInfo.parserCrashed = currentPhase;
            dumpSource(currentInfo, ex);
            throw ex;
        } catch (RuntimeException ex) {
            parserError = currentPhase;
            dumpSource(currentInfo, ex);
            throw ex;        
        } catch (Error ex) {
            parserError = currentPhase;
            dumpSource(currentInfo, ex);
            throw ex;
        }

        finally {
            if (hasMoreFiles) {
                assert lm != null;
                assert lmListener != null;
                lm.removeLowMemoryListener (lmListener);
            }
            currentInfo.setPhase(currentPhase);
            currentInfo.parserCrashed = parserError;
        }
        return currentPhase;
    }
    
    private static CompilationInfoImpl createCurrentInfo (final JavacParser parser,
            final FileObject file,
            final FileObject root,
            final Snapshot snapshot,
            final JavacTaskImpl javac) throws IOException {                
        CompilationInfoImpl info = new CompilationInfoImpl(parser, file, root, null,snapshot, false);
        if (file != null) {
            Logger.getLogger("TIMER").log(Level.FINE, "CompilationInfo",    //NOI18N
                    new Object[] {file, info});
        }
        return info;
    }
    
    static JavacTaskImpl createJavacTask(
            final FileObject file,
            final FileObject root,
            final ClasspathInfo cpInfo,
            final JavacParser parser,
            final DiagnosticListener<? super JavaFileObject> diagnosticListener,
            final ClassNamesForFileOraculum oraculum) {
        String sourceLevel = null;
        if (file != null) {
            if (LOGGER.isLoggable(Level.FINER)) {
                LOGGER.finer("Created new JavacTask for: " + FileUtil.getFileDisplayName(file));
            }            
            sourceLevel = SourceLevelQuery.getSourceLevel(file);
                                  
            if (root != null && sourceLevel != null) {
                try {
                    RepositoryUpdater.getDefault().verifySourceLevel(root.getURL(), sourceLevel);
                } catch (IOException ex) {
                    LOGGER.log(Level.FINE, null, ex);
                }
            }
        }
        if (sourceLevel == null) {
            sourceLevel = JavaPlatformManager.getDefault().getDefaultPlatform().getSpecification().getVersion().toString();
        }
        JavacTaskImpl javacTask = createJavacTask(cpInfo, diagnosticListener, sourceLevel, false, oraculum);
        Context context = javacTask.getContext();
        if (parser != null) {
            JavacCancelService.preRegister(context, parser);
        }
        JavacFlowListener.preRegister(context);
        TreeLoader.preRegister(context, cpInfo);
        Messager.preRegister(context, null, DEV_NULL, DEV_NULL, DEV_NULL);
        ErrorHandlingJavadocEnter.preRegister(context);
        JavadocMemberEnter.preRegister(context);       
        JavadocEnv.preRegister(context, cpInfo);
        DocCommentScanner.Factory.preRegister(context);
        com.sun.tools.javac.main.JavaCompiler.instance(context).keepComments = true;
        return javacTask;
    }
    
    public static JavacTaskImpl createJavacTask (final ClasspathInfo cpInfo, final DiagnosticListener<? super JavaFileObject> diagnosticListener, String sourceLevel,  ClassNamesForFileOraculum cnih) {
        if (sourceLevel == null) {
            sourceLevel = JavaPlatformManager.getDefault().getDefaultPlatform().getSpecification().getVersion().toString();
        }
        return createJavacTask(cpInfo, diagnosticListener, sourceLevel, true, cnih);
    }
    
    private static JavacTaskImpl createJavacTask(final ClasspathInfo cpInfo, final DiagnosticListener<? super JavaFileObject> diagnosticListener, final String sourceLevel, final boolean backgroundCompilation, ClassNamesForFileOraculum cnih) {
        final List<String> options = new ArrayList<String>();
        String lintOptions = CompilerSettings.getCommandLine();
        com.sun.tools.javac.code.Source validatedSourceLevel = validateSourceLevel(sourceLevel);
        if (lintOptions.length() > 0) {
            options.addAll(Arrays.asList(lintOptions.split(" ")));
        }
        if (!backgroundCompilation) {
            options.add("-Xjcov"); //NOI18N, Make the compiler store end positions
            options.add("-XDdisableStringFolding"); //NOI18N
        } else {
            options.add("-XDbackgroundCompilation");    //NOI18N
            options.add("-XDcompilePolicy=byfile");     //NOI18N
            options.add("-target");                     //NOI18N
            options.add(validatedSourceLevel.requiredTarget().name);
        }
        options.add("-XDide");   // NOI18N, javac runs inside the IDE
        options.add("-XDsave-parameter-names");   // NOI18N, javac runs inside the IDE
        options.add("-g:");      // NOI18N, Enable some debug info
        options.add("-g:source"); // NOI18N, Make the compiler to maintian source file info
        options.add("-g:lines"); // NOI18N, Make the compiler to maintain line table
        options.add("-g:vars");  // NOI18N, Make the compiler to maintain local variables table
        options.add("-source");  // NOI18N
        options.add(validatedSourceLevel.name);
        options.add("-proc:none"); // NOI18N, Disable annotation processors

        ClassLoader orig = Thread.currentThread().getContextClassLoader();
        try {            
            //The ToolProvider.defaultJavaCompiler will use the context classloader to load the javac implementation
            //it should be load by the current module's classloader (should delegate to other module's classloaders as necessary)
            Thread.currentThread().setContextClassLoader(ClasspathInfo.class.getClassLoader());
            JavaCompiler tool = ToolProvider.getSystemJavaCompiler();
            JavacTaskImpl task = (JavacTaskImpl)tool.getTask(null, 
                    ClasspathInfoAccessor.getINSTANCE().getFileManager(cpInfo),
                    diagnosticListener, options, null, Collections.<JavaFileObject>emptySet());
            Context context = task.getContext();
            JavadocClassReader.preRegister(context, !backgroundCompilation);
            if (cnih != null) {
                context.put(ClassNamesForFileOraculum.class, cnih);
            }
            return task;
        } finally {
            Thread.currentThread().setContextClassLoader(orig);
        }
    }
    
    private static com.sun.tools.javac.code.Source validateSourceLevel (String sourceLevel) {
        com.sun.tools.javac.code.Source[] sources = com.sun.tools.javac.code.Source.values();
        if (sourceLevel == null) {
            //Should never happen but for sure
            return sources[sources.length-1];
        }
        for (com.sun.tools.javac.code.Source source : sources) {
            if (source.name.equals(sourceLevel)) {
                return source;
            }
        }
        SpecificationVersion specVer = new SpecificationVersion (sourceLevel);
        SpecificationVersion JAVA_12 = new SpecificationVersion ("1.2");   //NOI18N
        if (JAVA_12.compareTo(specVer)>0) {
            //Some SourceLevelQueries return 1.1 source level which is invalid, use 1.2
            return sources[0];
        }
        else {
            return sources[sources.length-1];
        }
    }
    
    private static void logTime (FileObject source, Phase phase, long time) {
        assert source != null && phase != null;
        String message = phase2Message.get(phase);
        assert message != null;
        TIME_LOGGER.log(Level.FINE, message, new Object[] {source, time});                
    }
    
    
    /**
     * Dumps the source code to the file. Used for parser debugging. Only a limited number
     * of dump files is used. If the last file exists, this method doesn't dump anything.
     *
     * @param  info  CompilationInfo for which the error occurred.
     * @param  exc  exception to write to the end of dump file
     */
    private static void dumpSource(CompilationInfoImpl info, Throwable exc) {
        String userDir = System.getProperty("netbeans.user");
        if (userDir == null) {
            return;
        }
        String dumpDir =  userDir + "/var/log/"; //NOI18N
        String src = info.getText();
        FileObject file = info.getFileObject();
        String fileName = FileUtil.getFileDisplayName(file);
        String origName = file.getName();
        File f = new File(dumpDir + origName + ".dump"); // NOI18N
        boolean dumpSucceeded = false;
        int i = 1;
        while (i < MAX_DUMPS) {
            if (!f.exists())
                break;
            f = new File(dumpDir + origName + '_' + i + ".dump"); // NOI18N
            i++;
        }
        if (!f.exists()) {
            try {
                OutputStream os = new FileOutputStream(f);
                PrintWriter writer = new PrintWriter(new OutputStreamWriter(os, "UTF-8")); // NOI18N
                try {
                    writer.println(src);
                    writer.println("----- Classpath: ---------------------------------------------"); // NOI18N
                    
                    final ClassPath bootPath   = info.getClasspathInfo().getClassPath(ClasspathInfo.PathKind.BOOT);
                    final ClassPath classPath  = info.getClasspathInfo().getClassPath(ClasspathInfo.PathKind.COMPILE);
                    final ClassPath sourcePath = info.getClasspathInfo().getClassPath(ClasspathInfo.PathKind.SOURCE);

                    writer.println("bootPath: " + (bootPath != null ? bootPath.toString() : "null"));
                    writer.println("classPath: " + (classPath != null ? classPath.toString() : "null"));
                    writer.println("sourcePath: " + (sourcePath != null ? sourcePath.toString() : "null"));
                    
                    writer.println("----- Original exception ---------------------------------------------"); // NOI18N
                    exc.printStackTrace(writer);
                } finally {
                    writer.close();
                    dumpSucceeded = true;
                }
            } catch (IOException ioe) {
                LOGGER.log(Level.INFO, "Error when writing parser dump file!", ioe); // NOI18N
            }
        }
        if (dumpSucceeded) {
            Throwable t = Exceptions.attachMessage(exc, "An error occurred during parsing of \'" + fileName + "\'. Please report a bug against java/source and attach dump file '"  // NOI18N
                    + f.getAbsolutePath() + "'."); // NOI18N
            Exceptions.printStackTrace(t);
        } else {
            LOGGER.log(Level.WARNING,
                    "Dump could not be written. Either dump file could not " + // NOI18N
                    "be created or all dump files were already used. Please " + // NOI18N
                    "check that you have write permission to '" + dumpDir + "' and " + // NOI18N
                    "clean all *.dump files in that directory."); // NOI18N
        }
    }
    
    
    private static boolean reparseMethod (final CompilationInfoImpl ci,
            final Snapshot snapshot,
            final MethodTree orig,
            final String newBody) throws IOException {        
        assert ci != null;
        final FileObject fo = ci.getFileObject();
        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.finer("Reparse method in: " + fo);          //NOI18N
        }
        if (((JCMethodDecl)orig).localEnv == null) {
            //We are seeing interface method or abstract or native method with body.
            //Don't do any optimalization of this broken code - has no attr env.
            return false;
        }
        final Phase currentPhase = ci.getPhase();
        if (Phase.PARSED.compareTo(currentPhase) > 0) {
            return false;
        }
        try {
            final CompilationUnitTree cu = ci.getCompilationUnit();
            if (cu == null || newBody == null) {
                return false;
            }
            final JavacTaskImpl task = ci.getJavacTask();
            final JavacTrees jt = JavacTrees.instance(task);
            final int origStartPos = (int) jt.getSourcePositions().getStartPosition(cu, orig.getBody());
            final int origEndPos = (int) jt.getSourcePositions().getEndPosition(cu, orig.getBody());
            if (origStartPos > origEndPos) {
                LOGGER.warning("Javac returned startpos: "+origStartPos+" > endpos: "+origEndPos);  //NOI18N
                return false;
            }
            final FindAnonymousVisitor fav = new FindAnonymousVisitor();
            fav.scan(orig.getBody(), null);
            if (fav.hasLocalClass) {
                if (LOGGER.isLoggable(Level.FINER)) {
                    LOGGER.finer("Skeep reparse method (old local classes): " + fo);   //NOI18N
                }
                return false;
            }
            final int firstInner = fav.firstInner;
            final int noInner = fav.noInner;
            final Context ctx = task.getContext();
            final TreeLoader treeLoader = TreeLoader.instance(ctx);
            if (treeLoader != null) {
                treeLoader.startPartialReparse();
            }
            try {
                final Log l = Log.instance(ctx);
                l.startPartialReparse();
                final JavaFileObject prevLogged = l.useSource(cu.getSourceFile());
                JCBlock block;
                try {
                    DiagnosticListener dl = ctx.get(DiagnosticListener.class);
                    assert dl instanceof CompilationInfoImpl.DiagnosticListenerImpl;
                    ((CompilationInfoImpl.DiagnosticListenerImpl)dl).startPartialReparse(origStartPos, origEndPos);
                    long start = System.currentTimeMillis();
                    block = task.reparseMethodBody(cu, orig, newBody, firstInner);                
                    if (LOGGER.isLoggable(Level.FINER)) {
                        LOGGER.finer("Reparsed method in: " + fo);     //NOI18N
                    }
                    assert block != null;
                    fav.reset();
                    fav.scan(block, null);
                    final int newNoInner = fav.noInner;
                    if (fav.hasLocalClass || noInner != newNoInner) {
                        if (LOGGER.isLoggable(Level.FINER)) {
                            LOGGER.finer("Skeep reparse method (new local classes): " + fo);   //NOI18N
                        }
                        return false;
                    }
                    long end = System.currentTimeMillis();                
                    if (fo != null) {
                        logTime (fo,Phase.PARSED,(end-start));
                    }
                    final int newEndPos = (int) jt.getSourcePositions().getEndPosition(cu, block);
                    final int delta = newEndPos - origEndPos;
                    final Map<JCTree,Integer> endPos = ((JCCompilationUnit)cu).endPositions;
                    final TranslatePositionsVisitor tpv = new TranslatePositionsVisitor(orig, endPos, delta);
                    tpv.scan(cu, null);
                    ((JCMethodDecl)orig).body = block;
                    if (Phase.RESOLVED.compareTo(currentPhase)<=0) {
                        start = System.currentTimeMillis();
                        task.reattrMethodBody(orig, block);
                        if (LOGGER.isLoggable(Level.FINER)) {
                            LOGGER.finer("Resolved method in: " + fo);     //NOI18N
                        }
                        if (!((CompilationInfoImpl.DiagnosticListenerImpl)dl).hasPartialReparseErrors()) {
                            final JavacFlowListener fl = JavacFlowListener.instance(ctx);
                            if (fl != null && fl.hasFlowCompleted(fo)) {
                                if (LOGGER.isLoggable(Level.FINER)) {
                                    final List<? extends Diagnostic> diag = ci.getDiagnostics();
                                    if (!diag.isEmpty()) {
                                        LOGGER.finer("Reflow with errors: " + fo + " " + diag);     //NOI18N
                                    }                            
                                }
                                TreePath tp = TreePath.getPath(cu, orig);       //todo: store treepath in changed method => improve speed
                                Tree t = tp.getParentPath().getLeaf();
                                task.reflowMethodBody(cu, (ClassTree) t, orig);
                                if (LOGGER.isLoggable(Level.FINER)) {
                                    LOGGER.finer("Reflowed method in: " + fo); //NOI18N
                                }
                            }
                        }
                        end = System.currentTimeMillis();
                        if (fo != null) {
                            logTime (fo, Phase.ELEMENTS_RESOLVED,0L);
                            logTime (fo,Phase.RESOLVED,(end-start));
                        }
                    }
                    ((CompilationInfoImpl.DiagnosticListenerImpl)dl).endPartialReparse (delta);
                } finally {
                    l.endPartialReparse();
                    l.useSource(prevLogged);
                }
                ci.update(snapshot);
            } finally {
              if (treeLoader != null) {
                  treeLoader.endPartialReparse();
              }  
            }
        } catch (CouplingAbort ca) {
            //Needs full reparse
            return false;
        } catch (Throwable t) {
            if (t instanceof ThreadDeath) {
                throw (ThreadDeath) t;
            }
            boolean a = false;
            assert a = true; 
            if (a) {
                dumpSource(ci, t);
            }
            return false;
        }
        return true;
    }
    
    //Helper classes
            
    /**
     * Implementation of CancelService responsible for stopping
     * expensive parser operations when the result is not needed any more.
     */
    private static class JavacCancelService extends CancelService {
                       
        private final JavacParser parser;
        boolean active;        
        
        private JavacCancelService (final JavacParser parser) {
            this.parser = parser;
        }
        
        public static JavacCancelService instance (final Context context) {
            final CancelService cancelService = CancelService.instance(context);
            return (cancelService instanceof JavacCancelService) ? (JavacCancelService) cancelService : null;
        }
        
        static void preRegister(final Context context, final JavacParser parser) {
            assert context != null;
            assert parser != null;
            context.put(cancelServiceKey, new JavacCancelService(parser));
        }
        
        @Override
        public boolean isCanceled () {
            final boolean res =  active && parser.canceled.get();
            return res;
        }
               
    }
    
    /**
     * The MBean memory listener
     */
    private static class LMListener implements LowMemoryListener {
        private final AtomicBoolean lowMemory = new AtomicBoolean (false);
        
        public void lowMemory(LowMemoryEvent event) {
            lowMemory.set(true);
        }        
    }
    
    /**
     * Lexer listener used to detect partial reparse
     */
    private class DocListener implements PropertyChangeListener, TokenHierarchyListener {
        
        private EditorCookie.Observable ec;
        private TokenHierarchyListener lexListener;
        private volatile Document document;
        
        public DocListener (EditorCookie.Observable ec) {
            assert ec != null;
            this.ec = ec;
            this.ec.addPropertyChangeListener(WeakListeners.propertyChange(this, this.ec));
            Document doc = ec.getDocument();            
            if (doc != null) {
                TokenHierarchy th = TokenHierarchy.get(doc);
                th.addTokenHierarchyListener(lexListener = WeakListeners.create(TokenHierarchyListener.class, this,th));
                document = doc;
            }            
        }
                                   
        public void propertyChange(PropertyChangeEvent evt) {
            if (EditorCookie.Observable.PROP_DOCUMENT.equals(evt.getPropertyName())) {
                Object old = evt.getOldValue();                
                if (old instanceof Document && lexListener != null) {
                    TokenHierarchy th = TokenHierarchy.get((Document) old);
                    th.removeTokenHierarchyListener(lexListener);
                    lexListener = null;
                }                
                Document doc = ec.getDocument();                
                if (doc != null) {
                    TokenHierarchy th = TokenHierarchy.get(doc);
                    th.addTokenHierarchyListener(lexListener = WeakListeners.create(TokenHierarchyListener.class, this,th));
                    this.document = doc;    //set before rescheduling task to avoid race condition
                }
                else {
                    //reset document
                    this.document = doc;
                }
            }
        }
        
        public void tokenHierarchyChanged(TokenHierarchyEvent evt) {
            Pair<DocPositionRegion,MethodTree> changedMethod = null;
            if (evt.type() == TokenHierarchyEventType.MODIFICATION) {
                if (supportsReparse) {
                    int start = evt.affectedStartOffset();
                    int end = evt.affectedEndOffset();                                                                
                    synchronized (positions) {
                        for (Pair<DocPositionRegion,MethodTree> pe : positions) {
                            PositionRegion p = pe.first;
                            if (start > p.getStartOffset() && end < p.getEndOffset()) {
                                changedMethod = pe;
                                break;
                            }
                        }
                        if (changedMethod != null) {
                            TokenChange<JavaTokenId> change = evt.tokenChange(JavaTokenId.language());
                            if (change != null) {
                                TokenSequence<JavaTokenId> ts = change.removedTokenSequence();
                                if (ts != null) {
                                    while (ts.moveNext()) {
                                        switch (ts.token().id()) {
                                            case LBRACE:
                                            case RBRACE:
                                                changedMethod = null;
                                                break;
                                        }
                                    }
                                }
                                if (changedMethod != null) {
                                    TokenSequence<JavaTokenId> current = change.currentTokenSequence();                
                                    current.moveIndex(change.index());
                                    for (int i=0; i< change.addedTokenCount(); i++) {
                                        current.moveNext();
                                        switch (current.token().id()) {
                                            case LBRACE:
                                            case RBRACE:
                                                changedMethod = null;
                                                break;
                                            }
                                    }
                                }
                            }
                        }
                        positions.clear();
                        if (changedMethod!=null) {
                            positions.add (changedMethod);
                        }
                        synchronized (JavacParser.this) {
                            JavacParser.this.changedMethod = changedMethod;
                        }
                    }
                }
            }            
        }        
    }


    /**
     * For unit tests only
     * Used by JavaSourceTest.testIncrementalReparse
     * @param changedMethod
     */
    public synchronized void setChangedMethod (final Pair<DocPositionRegion,MethodTree> changedMethod) {
        assert changedMethod != null;
        this.changedMethod = changedMethod;
    }

    
    /**
     * Filter listener to listen on j2me preprocessor
     */
    private final class FilterListener implements ChangeListener {        
        
        public FilterListener (final JavaFileFilterImplementation filter) {
            filter.addChangeListener(WeakListeners.change(this, filter));
        }
        
        public void stateChanged(ChangeEvent event) {
            listeners.fireChange();
        }
    }        
}
