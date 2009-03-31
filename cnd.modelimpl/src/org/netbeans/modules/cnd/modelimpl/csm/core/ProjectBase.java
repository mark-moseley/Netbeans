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
package org.netbeans.modules.cnd.modelimpl.csm.core;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.netbeans.modules.cnd.api.model.*;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.api.model.util.UIDs;
import org.netbeans.modules.cnd.api.project.NativeFileItem;
import org.netbeans.modules.cnd.api.project.NativeFileItem.Language;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.netbeans.modules.cnd.api.project.NativeProjectItemsListener;
import org.netbeans.modules.cnd.apt.debug.DebugUtils;
import org.netbeans.modules.cnd.apt.support.StartEntry;
import org.netbeans.modules.cnd.apt.support.APTHandlersSupport;
import org.netbeans.modules.cnd.apt.support.APTSystemStorage;
import org.netbeans.modules.cnd.apt.structure.APTFile;
import org.netbeans.modules.cnd.apt.support.APTDriver;
import org.netbeans.modules.cnd.apt.support.APTIncludeHandler;
import org.netbeans.modules.cnd.apt.support.APTMacroMap;
import org.netbeans.modules.cnd.apt.support.APTPreprocHandler;
import org.netbeans.modules.cnd.apt.support.APTWalker;
import org.netbeans.modules.cnd.apt.utils.APTIncludeUtils;
import org.netbeans.modules.cnd.modelimpl.debug.Terminator;
import org.netbeans.modules.cnd.modelimpl.debug.Diagnostic;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;

import org.netbeans.modules.cnd.modelimpl.platform.*;
import org.netbeans.modules.cnd.modelimpl.csm.*;
import org.netbeans.modules.cnd.modelimpl.debug.DiagnosticExceptoins;
import org.netbeans.modules.cnd.modelimpl.parser.apt.APTParseFileWalker;
import org.netbeans.modules.cnd.modelimpl.parser.apt.APTRestorePreprocStateWalker;
import org.netbeans.modules.cnd.modelimpl.repository.KeyUtilities;
import org.netbeans.modules.cnd.modelimpl.repository.PersistentUtils;
import org.netbeans.modules.cnd.modelimpl.textcache.ProjectNameCache;
import org.netbeans.modules.cnd.modelimpl.textcache.QualifiedNameCache;
import org.netbeans.modules.cnd.modelimpl.repository.RepositoryUtils;
import org.netbeans.modules.cnd.modelimpl.textcache.DefaultCache;
import org.netbeans.modules.cnd.modelimpl.trace.TraceUtils;
import org.netbeans.modules.cnd.modelimpl.uid.LazyCsmCollection;
import org.netbeans.modules.cnd.modelimpl.uid.UIDCsmConverter;
import org.netbeans.modules.cnd.modelimpl.uid.UIDObjectFactory;
import org.netbeans.modules.cnd.modelimpl.uid.UIDUtilities;
import org.netbeans.modules.cnd.repository.spi.Key;
import org.netbeans.modules.cnd.repository.spi.Persistent;
import org.netbeans.modules.cnd.repository.support.SelfPersistent;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.cnd.utils.cache.CharSequenceKey;
import org.netbeans.modules.cnd.utils.cache.FilePathCache;
import org.netbeans.modules.cnd.utils.cache.TinyCharSequence;
import org.openide.filesystems.FileObject;
import org.openide.util.Cancellable;
import org.openide.util.RequestProcessor;

/**
 * Base class for CsmProject implementation
 * @author Dmitry Ivanov
 * @author Vladimir Kvashin
 */
public abstract class ProjectBase implements CsmProject, Persistent, SelfPersistent, CsmIdentifiable {

    private volatile boolean needParseOrphan;

    /** Creates a new instance of CsmProjectImpl */
    protected ProjectBase(ModelImpl model, Object platformProject, String name) {
        RepositoryUtils.openUnit(createProjectKey(platformProject));
        setStatus(Status.Initial);
        this.name = ProjectNameCache.getManager().getString(name);
        init(model, platformProject);
        NamespaceImpl ns = new NamespaceImpl(this, false);
        assert ns != null;
        this.globalNamespaceUID = UIDCsmConverter.namespaceToUID(ns);
        declarationsSorageKey = new DeclarationContainer(this).getKey();
        classifierStorageKey = new ClassifierContainer(this).getKey();
        fileContainerKey = new FileContainer(this).getKey();
        graphStorageKey = new GraphContainer(this).getKey();
        FAKE_GLOBAL_NAMESPACE = new NamespaceImpl(this, true);
    }

    private void init(ModelImpl model, Object platformProject) {
        this.model = model;
        this.platformProject = platformProject;
        // remember in repository
        RepositoryUtils.hang(this);
        // create global namespace

        if (TraceFlags.CLOSE_AFTER_PARSE) {
            Terminator.create(this);
        }
        needParseOrphan = ModelSupport.needParseOrphan(platformProject);
    }

    private boolean checkConsistency() {
        long time = TraceFlags.TIMING ? System.currentTimeMillis() : 0;
        if (getFileContainer() == FileContainer.empty()) {
            return false;
        }
        if (getDeclarationsSorage() == DeclarationContainer.empty()) {
            return false;
        }
        if (getGraph() == GraphContainer.empty()) {
            return false;
        }
        if (getGlobalNamespace() == FAKE_GLOBAL_NAMESPACE) {
            return false;
        }
        if (TraceFlags.TIMING) {
            System.err.printf("Consistency check took %d ms\n", System.currentTimeMillis() - time);
        }
        return true;
    }

    private void setStatus(Status newStatus) {
        //System.err.printf("CHANGING STATUS %s -> %s for %s (%s)\n", status, newStatus, name, getClass().getName());
        status = newStatus;
    }

    protected static void cleanRepository(Object platformProject, boolean articicial) {
        Key key = createProjectKey(platformProject);
        RepositoryUtils.closeUnit(key, null, true);
    }

    private static Key createProjectKey(Object platfProj) {
        return KeyUtilities.createProjectKey(getUniqueName(platfProj).toString());
    }

    protected static ProjectBase readInstance(ModelImpl model, Object platformProject, String name) {

        long time = 0;
        if (TraceFlags.TIMING) {
            System.err.printf("Project %s: instantiating...\n", name);
            time = System.currentTimeMillis();
        }

        assert TraceFlags.PERSISTENT_REPOSITORY;
        Key key = createProjectKey(platformProject);
        RepositoryUtils.openUnit(key);
        Persistent o = RepositoryUtils.get(key);
        if (o != null) {
            assert o instanceof ProjectBase;
            ProjectBase impl = (ProjectBase) o;
            CharSequence aName = ProjectNameCache.getManager().getString(name);
            if (!impl.name.equals(aName)) {
                impl.setName(aName);
            }
            impl.init(model, platformProject);
            if (TraceFlags.TIMING) {
                time = System.currentTimeMillis() - time;
                System.err.printf("Project %s: loaded. %d ms\n", name, time);
            }
            if (impl.checkConsistency()) {
                return impl;
            }
        }
        return null;
    }

    public final CsmNamespace getGlobalNamespace() {
        return _getGlobalNamespace();
    }

    public final CharSequence getName() {
        return name;
    }

    protected final void setName(CharSequence name) {
        this.name = name;
    }

    /**
     * Returns a string that uniquely identifies this project.
     * One should never rely on this name structure,
     * just use it as in unique identifier
     */
    public final CharSequence getUniqueName() {
        if (this.uniqueName == null) {
            this.uniqueName = getUniqueName(getPlatformProject());
        }
        return this.uniqueName;
    }

    public static CharSequence getUniqueName(Object platformProject) {
        String result;
        if (platformProject instanceof NativeProject) {
            result = ((NativeProject) platformProject).getProjectRoot() + 'N';
        } else if (platformProject instanceof CharSequence) {
            result = ((CharSequence)platformProject).toString() + 'L';
        } else if (platformProject == null) {
            throw new IllegalArgumentException("Incorrect platform project: null"); // NOI18N
        } else {
            throw new IllegalArgumentException("Incorrect platform project class: " + platformProject.getClass()); // NOI18N
        }
        return ProjectNameCache.getManager().getString(result);
    }

    /** Gets an object, which represents correspondent IDE project */
    public final Object getPlatformProject() {
        return platformProject;
    }

    /** Gets an object, which represents correspondent IDE project */
    protected final void setPlatformProject(Object platformProject) {
        this.platformProject = platformProject;
        // recreate unique name
        this.uniqueName = null;
        this.uniqueName = getUniqueName();
    }

    /** Finds namespace by its qualified name */
    public final CsmNamespace findNamespace(CharSequence qualifiedName, boolean findInLibraries) {
        CsmNamespace result = findNamespace(qualifiedName);
        if (result == null && findInLibraries) {
            for (Iterator<CsmProject> it = getLibraries().iterator(); it.hasNext();) {
                CsmProject lib = it.next();
                result = lib.findNamespace(qualifiedName);
                if (result != null) {
                    break;
                }
            }
        }
        return result;
    }

    /** Finds namespace by its qualified name */
    public final CsmNamespace findNamespace(CharSequence qualifiedName) {
        CsmNamespace nsp = _getNamespace(qualifiedName);
        return nsp;
    }

    private static String getNestedNamespaceQualifiedName(CharSequence name, NamespaceImpl parent, boolean createForEmptyNames) {
        StringBuilder sb = new StringBuilder(name);
        if (parent != null) {
            if (name.length() == 0 && createForEmptyNames) {
                sb.append(parent.getNameForUnnamedElement());
            }
            if (!parent.isGlobal()) {
                sb.insert(0, "::"); // NOI18N
                sb.insert(0, parent.getQualifiedName());
            }
        }
        return sb.toString();
    }

    public final NamespaceImpl findNamespaceCreateIfNeeded(NamespaceImpl parent, CharSequence name) {
        synchronized (namespaceLock) {
            String qualifiedName = ProjectBase.getNestedNamespaceQualifiedName(name, parent, true);
            NamespaceImpl nsp = _getNamespace(qualifiedName);
            if (nsp == null) {
                nsp = new NamespaceImpl(this, parent, name.toString(), qualifiedName);
            }
            return nsp;
        }
    }

    public final void registerNamespace(NamespaceImpl namespace) {
        _registerNamespace(namespace);
    }

    public final void unregisterNamesace(NamespaceImpl namespace) {
        _unregisterNamespace(namespace);
    }

    public final CsmClassifier findClassifier(CharSequence qualifiedName, boolean findInLibraries) {
        CsmClassifier result = findClassifier(qualifiedName);
        if (result == null && findInLibraries) {
            for (Iterator<CsmProject> it = getLibraries().iterator(); it.hasNext();) {
                CsmProject lib = it.next();
                result = lib.findClassifier(qualifiedName);
                if (result != null) {
                    break;
                }
            }
        }
        return result;
    }

    public final CsmClassifier findClassifier(CharSequence qualifiedName) {
        CsmClassifier result = getClassifierSorage().getClassifier(qualifiedName);
        return result;
    }

    public final Collection<CsmClassifier> findClassifiers(CharSequence qualifiedName) {
        CsmClassifier result = getClassifierSorage().getClassifier(qualifiedName);
        Collection<CsmClassifier> out = UIDCsmConverter.UIDsToDeclarations(new ArrayList<CsmUID<CsmClassifier>>());
        //Collection<CsmClassifier> out = new LazyCsmCollection<CsmClassifier, CsmClassifier>(new ArrayList<CsmUID<CsmClassifier>>(), TraceFlags.SAFE_UID_ACCESS);
        if (result != null) {
            if (CsmKindUtilities.isBuiltIn(result)) {
                return Collections.<CsmClassifier>singletonList(result);
            }
            CharSequence[] allClassifiersUniqueNames = Utils.getAllClassifiersUniqueNames(result.getUniqueName());
            for (CharSequence curUniqueName : allClassifiersUniqueNames) {
                Collection<? extends CsmDeclaration> decls = this.findDeclarations(curUniqueName);
                @SuppressWarnings("unchecked")
                Collection<CsmClassifier> classifiers = (Collection<CsmClassifier>) decls;
                out.addAll(classifiers);
            }
        }
        return out;
    }

    public final CsmDeclaration findDeclaration(CharSequence uniqueName) {
        return getDeclarationsSorage().getDeclaration(uniqueName);
    }

    public final Collection<CsmOffsetableDeclaration> findDeclarations(CharSequence uniqueName) {
        return getDeclarationsSorage().findDeclarations(uniqueName);
    }

    public final Collection<CsmOffsetableDeclaration> findDeclarationsByPrefix(String prefix) {
        // To improve performance use char(255) instead real Character.MAX_VALUE
        char maxChar = 255; //Character.MAX_VALUE;
        return getDeclarationsSorage().getDeclarationsRange(prefix, prefix + maxChar); // NOI18N
    }

    public final Collection<CsmFriend> findFriendDeclarations(CsmOffsetableDeclaration decl) {
        return getDeclarationsSorage().findFriends(decl);
    }

    public static boolean isCppFile(CsmFile file) {
        return (file instanceof FileImpl) && ((FileImpl) file).isCppFile();
    }

//    public void registerClassifier(ClassEnumBase ce) {
//        classifiers.put(ce.getNestedNamespaceQualifiedName(), ce);
//        registerDeclaration(ce);
//    }
    public static boolean canRegisterDeclaration(CsmDeclaration decl) {
        // WAS: don't put unnamed declarations
        assert decl != null;
        assert decl.getName() != null;
        if (decl.getName().length() == 0) {
            return false;
        }
        CsmScope scope = decl.getScope();
        if (scope instanceof CsmCompoundClassifier) {
            return canRegisterDeclaration((CsmCompoundClassifier) scope);
        }
        return true;
    }

    public final boolean registerDeclaration(CsmOffsetableDeclaration decl) {

        if (!ProjectBase.canRegisterDeclaration(decl)) {
            if (TraceFlags.TRACE_REGISTRATION) {
                traceRegistration("not registered decl " + decl + " UID " + UIDs.get(decl)); //NOI18N
            } 
            return false;
        }

        if (CsmKindUtilities.isClass(decl) || CsmKindUtilities.isEnum(decl)) {

            ClassEnumBase cls = (ClassEnumBase) decl;
            CharSequence qname = cls.getQualifiedName();

            synchronized (classifierReplaceLock) {
                CsmClassifier old = getClassifierSorage().getClassifier(qname);
                if (old != null) {
                    // don't register if the new one is weaker
                    if (cls.shouldBeReplaced(old)) {
                        if (TraceFlags.TRACE_REGISTRATION) {
                            traceRegistration("not registered decl " + decl + " UID " + UIDs.get(decl)); //NOI18N
                        }
                        return false;
                    }
                    // remove the old one if the new one is stronger
                    if ((old instanceof ClassEnumBase) && ((ClassEnumBase) old).shouldBeReplaced(cls)) {
                        if (TraceFlags.TRACE_REGISTRATION) {
                            System.err.println("disposing old decl " + old + " UID " + UIDs.get(decl)); //NOI18N
                        } 
                        ((ClassEnumBase) old).dispose();
                    }
                }
                getDeclarationsSorage().putDeclaration(decl);
                getClassifierSorage().putClassifier((CsmClassifier) decl);
            }

        } else if (CsmKindUtilities.isTypedef(decl)) { // isClassifier(decl) or isTypedef(decl) ??
            getDeclarationsSorage().putDeclaration(decl);
            getClassifierSorage().putClassifier((CsmClassifier) decl);
        } else {
            // only classes, enums and typedefs are registered as classifiers;
            // even if you implement CsmClassifier, this doesn't mean you atomatically get there ;)
            getDeclarationsSorage().putDeclaration(decl);
        }

        if (TraceFlags.TRACE_REGISTRATION) {
            System.err.println("registered " + decl + " UID " + UIDs.get(decl)); //NOI18N
        }
        return true;
    }

    public final void unregisterDeclaration(CsmOffsetableDeclaration decl) {
        if (TraceFlags.TRACE_REGISTRATION) {
            traceRegistration("unregistered " + decl + " UID " + UIDs.get(decl)); //NOI18N
        }
        if (decl instanceof CsmClassifier) {
            getClassifierSorage().removeClassifier(decl);
        }
        getDeclarationsSorage().removeDeclaration(decl);
    }

    private static void traceRegistration(String text) {
        assert TraceFlags.TRACE_REGISTRATION : "TraceFlags.TRACE_REGISTRATION should be checked *before* call !"; //NOI18N
        System.err.printf("registration: %s\n", text);
    }

    public final void waitParse() {
        boolean insideParser = ParserThreadManager.instance().isParserThread();
        if (insideParser) {
            new Throwable("project.waitParse should NEVER be called in parser thread !!!").printStackTrace(System.err); // NOI18N
        }
        if (insideParser) {
            return;
        }
        ensureFilesCreated();
        ensureChangedFilesEnqueued();
        waitParseImpl();
    }

    private void waitParseImpl() {
        synchronized (waitParseLock) {
            while (ParserQueue.instance().hasFiles(this, null)) {
                try {
                    //FIXUP - timeout is a workaround for #146436 hang on running unit tests
                    waitParseLock.wait(10000);
                } catch (InterruptedException ex) {
                    // do nothing
                }
            }
        }
    }

    protected void ensureChangedFilesEnqueued() {
    }

    /**
     * @param skipFile if null => check all files, otherwise skip checking
     * this file
     *
     */
    protected boolean hasChangedFiles(CsmFile skipFile) {
        return false;
    }

    public final boolean acceptNativeItem(NativeFileItem item) {
        if (item.getFile() == null) {
            return false;
        }
        NativeFileItem.Language language = item.getLanguage();
        return (language == NativeFileItem.Language.C ||
                language == NativeFileItem.Language.CPP ||
                language == NativeFileItem.Language.C_HEADER) &&
                !item.isExcluded();
    }

    protected final synchronized void registerProjectListeners() {
        if (platformProject instanceof NativeProject) {
            if (projectListener == null) {
                projectListener = new NativeProjectListenerImpl(getModel(), (NativeProject) platformProject);
            }
            ((NativeProject) platformProject).addProjectItemsListener(projectListener);
        }
    }

    protected final synchronized void unregisterProjectListeners() {
        if (projectListener != null) {
            if (platformProject instanceof NativeProject) {
                ((NativeProject) platformProject).removeProjectItemsListener(projectListener);
            }
        }
    }

    protected void ensureFilesCreated() {
        boolean notify = false;
        synchronized (this) {
            if (status == Status.Initial || status == Status.Restored) {
                try {
                    setStatus((status == Status.Initial) ? Status.AddingFiles : Status.Validating);
                    long time = 0;
                    if (TraceFlags.SUSPEND_PARSE_TIME != 0) {
                        System.err.println("suspend queue");
                        ParserQueue.instance().suspend();
                        if (TraceFlags.TIMING) {
                            time = System.currentTimeMillis();
                        }
                    }
                    ParserQueue.instance().onStartAddingProjectFiles(this);
                    registerProjectListeners();
                    NativeProject nativeProject = ModelSupport.getNativeProject(platformProject);
                    if (nativeProject != null) {
                        try {
                            ParserQueue.instance().suspend();
                            createProjectFilesIfNeed(nativeProject);
                        } finally {
                            ParserQueue.instance().resume();
                        }
                    }
                    if (TraceFlags.SUSPEND_PARSE_TIME != 0) {
                        if (TraceFlags.TIMING) {
                            time = System.currentTimeMillis() - time;
                            System.err.println("getting files from project system + put in queue took " + time + "ms");
                        }
                        try {
                            System.err.println("sleep for " + TraceFlags.SUSPEND_PARSE_TIME + "sec before resuming queue");
                            Thread.sleep(TraceFlags.SUSPEND_PARSE_TIME * 1000);
                            System.err.println("woke up after sleep");
                        } catch (InterruptedException ex) {
                            // do nothing
                        }
                        ParserQueue.instance().resume();
                    }
                    notify = true;
                } finally {
                    setStatus(Status.Ready);
                }
            }
        }
        if (notify) {
            ParserQueue.instance().onEndAddingProjectFiles(this);
        }
    }

    private void createProjectFilesIfNeed(NativeProject nativeProject) {

        if (TraceFlags.TIMING) {
            System.err.printf("\n\nGetting files from project system for %s...\n", getName());
        }
        if (TraceFlags.SUSPEND_PARSE_TIME != 0) {
            try {
                System.err.println("sleep for " + TraceFlags.SUSPEND_PARSE_TIME + "sec before getting files from project");
                Thread.sleep(TraceFlags.SUSPEND_PARSE_TIME * 1000);
                System.err.println("woke up after sleep");
            } catch (InterruptedException ex) {
                // do nothing
            }
        }
        long time = System.currentTimeMillis();
        final Set<NativeFileItem> removedFiles = Collections.synchronizedSet(new HashSet<NativeFileItem>());
        NativeProjectItemsListener projectItemListener = new NativeProjectItemsListener() {

            public void fileAdded(NativeFileItem fileItem) {
            }

            public void filesAdded(List<NativeFileItem> fileItems) {
            }

            public void fileRemoved(NativeFileItem fileItem) {
                removedFiles.add(fileItem);
            }

            public void filesRemoved(List<NativeFileItem> fileItems) {
                removedFiles.addAll(fileItems);
            }

            public void fileRenamed(String oldPath, NativeFileItem newFileIetm) {
            }

            public void filePropertiesChanged(NativeFileItem fileItem) {
            }

            public void filesPropertiesChanged(List<NativeFileItem> fileItems) {
            }

            public void filesPropertiesChanged() {
            }

            public void projectDeleted(NativeProject nativeProject) {
            }
        };
        nativeProject.addProjectItemsListener(projectItemListener);
        List<NativeFileItem> sources = new ArrayList<NativeFileItem>();
        List<NativeFileItem> headers = new ArrayList<NativeFileItem>();
        List<NativeFileItem> excluded = new ArrayList<NativeFileItem>();
        for (NativeFileItem item : nativeProject.getAllFiles()) {
            if (!item.isExcluded()) {
                switch (item.getLanguage()) {
                    case C:
                    case CPP:
                        sources.add(item);
                        break;
                    case C_HEADER:
                        headers.add(item);
                        break;
                    default:
                        break;
                }
            } else {
                switch (item.getLanguage()) {
                    case C:
                    case CPP:
                    case C_HEADER:
                        excluded.add(item);
                        break;
                    default:
                        break;
                }
            }
        }

        if (TraceFlags.TIMING) {
            time = System.currentTimeMillis() - time;
            System.err.printf("Getting files from project system took  %d ms for %s\n", time, getName());
            System.err.printf("FILES COUNT for %s:\nSource files:\t%d\nHeader files:\t%d\nTotal files:\t%d\n",
                    getName(), sources.size(), headers.size(), sources.size() + headers.size());
            time = System.currentTimeMillis();
        }
        if (TraceFlags.SUSPEND_PARSE_TIME != 0) {
            try {
                System.err.println("sleep for " + TraceFlags.SUSPEND_PARSE_TIME + "sec after getting files from project");
                Thread.sleep(TraceFlags.SUSPEND_PARSE_TIME * 1000);
                System.err.println("woke up after sleep");
            } catch (InterruptedException ex) {
                // do nothing
            }
        }
        if (TraceFlags.DUMP_PROJECT_ON_OPEN) {
            ModelSupport.dumpNativeProject(nativeProject);
        }

        try {
            disposeLock.readLock().lock();

            if (TraceFlags.TIMING) {
                time = System.currentTimeMillis() - time;
                System.err.printf("Waited on disposeLock: %d ms for %s\n", time, getName());
                time = System.currentTimeMillis();
            }

            if (disposing) {
                if (TraceFlags.TRACE_MODEL_STATE) {
                    System.err.printf("filling parser queue interrupted for %s\n", getName());
                }
                return;
            }

            ProjectSettingsValidator validator = null;
            if (status == Status.Validating) {
                validator = new ProjectSettingsValidator(this);
                validator.restoreSettings();
            }
            projectRoots.fixFolder(nativeProject.getProjectRoot());
            for (String root : nativeProject.getSourceRoots()) {
                projectRoots.fixFolder(root);
            }
            projectRoots.addSources(sources);
            projectRoots.addSources(headers);
            projectRoots.addSources(excluded);
            createProjectFilesIfNeed(sources, true, removedFiles, validator);
            createProjectFilesIfNeed(headers, false, removedFiles, validator);

        } finally {
            disposeLock.readLock().unlock();
            if (TraceFlags.TIMING) {
                time = System.currentTimeMillis() - time;
                System.err.printf("FILLING PARSER QUEUE took %d ms for %s\n", time, getName());
            }
        }
        nativeProject.removeProjectItemsListener(projectItemListener);
    // in fact if visitor used for parsing => visitor will parse all included files
    // recursively starting from current source file
    // so, when we visit headers, they should not be reparsed if already were parsed
    }

    private void createProjectFilesIfNeed(List<NativeFileItem> items, boolean sources,
            Set<NativeFileItem> removedFiles, ProjectSettingsValidator validator) {

        List<FileImpl> reparseOnEdit = new ArrayList<FileImpl>();
        List<NativeFileItem> reparseOnPropertyChanged = new ArrayList<NativeFileItem>();
        for (NativeFileItem nativeFileItem : items) {
            if (disposing) {
                if (TraceFlags.TRACE_MODEL_STATE) {
                    System.err.printf("filling parser queue interrupted for %s\n", getName());
                }
                return;
            }
            if (removedFiles.contains(nativeFileItem)) {
                continue;
            }
            assert (nativeFileItem.getFile() != null) : "native file item must have valid File object";
            if (TraceFlags.DEBUG) {
                ModelSupport.trace(nativeFileItem);
            }
            try {
                createIfNeed(nativeFileItem, sources, validator, reparseOnEdit, reparseOnPropertyChanged);
            } catch (Exception ex) {
                DiagnosticExceptoins.register(ex);
            }
        }
        if (!reparseOnEdit.isEmpty()) {
            DeepReparsingUtils.reparseOnEdit(reparseOnEdit, this, true);
        }
        if (!reparseOnPropertyChanged.isEmpty()) {
            DeepReparsingUtils.reparseOnPropertyChanged(reparseOnPropertyChanged, this);
        }
    }

    /**
     * Creates FileImpl instance for the given file item if it hasn/t yet been created.
     * Is called when initializing the project or new file is added to project.
     * Isn't intended to be used in #included file processing.
     */
    final protected void createIfNeed(NativeFileItem nativeFile, boolean isSourceFile) {
        createIfNeed(nativeFile, isSourceFile, null, null, null);
    }

    private void createIfNeed(NativeFileItem nativeFile, boolean isSourceFile,
            ProjectSettingsValidator validator, List<FileImpl> reparseOnEdit, List<NativeFileItem> reparseOnPropertyChanged) {

        assert (nativeFile != null && nativeFile.getFile() != null);
        if (!acceptNativeItem(nativeFile)) {
            return;
        }
        File file = nativeFile.getFile();
        int fileType = isSourceFile ? getFileType(nativeFile) : FileImpl.HEADER_FILE;

        FileAndHandler fileAndHandler = createOrFindFileImpl(ModelSupport.getFileBuffer(file), nativeFile, fileType);

        if (fileAndHandler.preprocHandler == null) {
            fileAndHandler.preprocHandler = createPreprocHandler(nativeFile);
        }
        if (validator != null) {
            // fill up needed collections based on validation
            if (fileAndHandler.fileImpl.validate()) {
                if (validator.arePropertiesChanged(nativeFile)) {
                    if (TraceFlags.TRACE_VALIDATION) {
                        System.err.printf("Validation: %s properties are changed \n", nativeFile.getFile().getAbsolutePath());
                    }
                    reparseOnPropertyChanged.add(nativeFile);
                }
            } else {
                if (TraceFlags.TRACE_VALIDATION) {
                    System.err.printf("Validation: file %s is changed\n", nativeFile.getFile().getAbsolutePath());
                }
                reparseOnEdit.add(fileAndHandler.fileImpl);
            }
        } else {
            // put directly into parser queue if needed
            if (isSourceFile || needParseOrphan) {
                ParserQueue.instance().add(fileAndHandler.fileImpl, fileAndHandler.preprocHandler.getState(), ParserQueue.Position.TAIL);
            }
        }
    }

    /**
     * Is called after project is added to model
     * and all listeners are notified
     */
    public final void onAddedToModel() {
        final boolean isRestored = status == Status.Restored;
        //System.err.printf("onAddedToModel isRestored=%b status=%s for %s (%s) \n", isRestored, status, name, getClass().getName());
        if (status == Status.Initial || status == Status.Restored) {
            Runnable r = new Runnable() {

                public void run() {
                    onAddedToModelImpl(isRestored);
                    synchronized (initializationTaskLock) {
                        initializationTask = null;
                    }
                }
                ;
            };
            String text = (status == Status.Initial) ? "Filling parser queue for " : "Validating files for ";	// NOI18N
            synchronized (initializationTaskLock) {
                initializationTask = ModelImpl.instance().enqueueModelTask(r, text + getName());
            }
        }
    }

    protected final Status getStatus() {
        return status;
    }

    private void onAddedToModelImpl(boolean isRestored) {

        if (disposing) {
            return;
        }

        try {
            disposeLock.readLock().lock();
            if (disposing) {
                return;
            }

            ensureFilesCreated();
            if (disposing) {
                return;
            }

            ensureChangedFilesEnqueued();
            if (disposing) {
                return;
            }
            Notificator.instance().flush();
        } finally {
            disposeLock.readLock().unlock();
        }

        if (isRestored) {
            ProgressSupport.instance().fireProjectLoaded(ProjectBase.this);
        }

        try {
            disposeLock.readLock().lock();
            if (isRestored && !disposing) {
                // FIXUP for #109105 fix the reason instead!
                try {
                    // TODO: refactor this - remove waiting here!
                    // It was introduced in version 1.2.2.27.2.94.4.41
                    // when validation was introduced
                    waitParseImpl();
                    checkForRemoved();
                } catch (Exception e) {
                    DiagnosticExceptoins.register(e);
                }
            }
            if (disposing) {
                return;
            }
            Notificator.instance().flush();
        } finally {
            disposeLock.readLock().unlock();
        }
    }

    /**
     * For the project that is restored from persistence,
     * is called when 1-st time parsed.
     * Cheks whether there are files in code model, that are removed from the project system
     */
    private void checkForRemoved() {

        NativeProject nativeProject = (platformProject instanceof NativeProject) ? (NativeProject) platformProject : null;

        // we might just ask NativeProject to find file,
        // but it's too ineffective; so we have to create a set of project files paths
        Set<String> projectFiles = null;
        if (nativeProject != null) {
            projectFiles = new HashSet<String>();
            for (NativeFileItem item : nativeProject.getAllFiles()) {
                if (!item.isExcluded()) {
                    switch (item.getLanguage()) {
                        case C:
                        case CPP:
                        case C_HEADER:
                            projectFiles.add(item.getFile().getAbsolutePath());
                            //this would be a workaround for #116706 Code assistance do not recognize changes in file
                            //projectFiles.add(item.getFile().getCanonicalPath());
                            break;
                        default:
                            break;
                    }
                }
            }
        }

        Set<FileImpl> candidates = new HashSet<FileImpl>();
        Set<FileImpl> removedPhysically = new HashSet<FileImpl>();
        for (FileImpl file : getAllFileImpls()) {
            if (!file.getFile().exists()) {
                removedPhysically.add(file);
            } else if (projectFiles != null) { // they might be null for library
                if (!projectFiles.contains(file.getAbsolutePath().toString())) {
                    candidates.add(file);
                }
            }
        }
        if (!removedPhysically.isEmpty()) {
            if (TraceFlags.TRACE_VALIDATION) {
                for (FileImpl file : removedPhysically) {
                    System.err.printf("Validation: removing (physically deleted) %s\n", file.getAbsolutePath()); //NOI18N
                }
            }
            onFileImplRemoved(new ArrayList<FileImpl>(removedPhysically));
        }
        for (FileImpl file : candidates) {
            boolean remove = true;
            Set<CsmFile> parents = getGraphStorage().getParentFiles(file);
            for (CsmFile parent : parents) {
                if (!candidates.contains(parent)) {
                    remove = false;
                    break;
                }
            }
            if (remove) {
                if (TraceFlags.TRACE_VALIDATION) {
                    System.err.printf("Validation: removing (removed from project) %s\n", file.getAbsolutePath());
                } //NOI18N
                onFileRemoved(file);
            }
        }
    }

    protected final APTPreprocHandler createEmptyPreprocHandler(File file) {
        StartEntry startEntry = new StartEntry(FileContainer.getFileKey(file, true).toString(),
                RepositoryUtils.UIDtoKey(getUID()));
        return APTHandlersSupport.createEmptyPreprocHandler(startEntry);
    }

    protected final APTPreprocHandler createPreprocHandler(NativeFileItem nativeFile) {
        assert (nativeFile != null);
        APTMacroMap macroMap = getMacroMap(nativeFile);
        APTIncludeHandler inclHandler = getIncludeHandler(nativeFile);
        APTPreprocHandler preprocHandler = APTHandlersSupport.createPreprocHandler(macroMap, inclHandler, isSourceFile(nativeFile));
        return preprocHandler;
    }

    private APTIncludeHandler getIncludeHandler(NativeFileItem nativeFile) {
        if (!isSourceFile(nativeFile)) {
            nativeFile = DefaultFileItem.toDefault(nativeFile);
        }
        List<String> origUserIncludePaths = nativeFile.getUserIncludePaths();
        List<String> origSysIncludePaths = nativeFile.getSystemIncludePaths();
        List<CharSequence> userIncludePaths = FilePathCache.asList(origUserIncludePaths);
        List<CharSequence> sysIncludePaths = sysAPTData.getIncludes(origSysIncludePaths.toString(), origSysIncludePaths);
        StartEntry startEntry = new StartEntry(FileContainer.getFileKey(nativeFile.getFile(), true).toString(),
                RepositoryUtils.UIDtoKey(getUID()));
        return APTHandlersSupport.createIncludeHandler(startEntry, sysIncludePaths, userIncludePaths);
    }

    private APTMacroMap getMacroMap(NativeFileItem nativeFile) {
        if (!isSourceFile(nativeFile)) {
            nativeFile = DefaultFileItem.toDefault(nativeFile);
        }
        List<String> userMacros = nativeFile.getUserMacroDefinitions();
        List<String> sysMacros = nativeFile.getSystemMacroDefinitions();
        APTMacroMap map = APTHandlersSupport.createMacroMap(getSysMacroMap(sysMacros), userMacros);
        return map;
    }

    protected final boolean isSourceFile(NativeFileItem nativeFile) {
        int type = getFileType(nativeFile);
        return type == FileImpl.SOURCE_CPP_FILE || type == FileImpl.SOURCE_C_FILE || type == FileImpl.SOURCE_FILE;
    //return nativeFile.getSystemIncludePaths().size()>0;
    }

    protected static int getFileType(NativeFileItem nativeFile) {
        switch (nativeFile.getLanguage()) {
            case C:
                return FileImpl.SOURCE_C_FILE;
            case CPP:
                return FileImpl.SOURCE_CPP_FILE;
            case C_HEADER:
                return FileImpl.HEADER_FILE;
            default:
                return FileImpl.UNDEFINED_FILE;
        }
    }

    private APTMacroMap getSysMacroMap(List<String> sysMacros) {
        //TODO: it's faster to use sysAPTData.getMacroMap(configID, sysMacros);
        // but we need this ID to get somehow... how?
        APTMacroMap map = sysAPTData.getMacroMap(sysMacros.toString(), sysMacros);
        return map;
    }

    //@Deprecated
    public final APTPreprocHandler getPreprocHandler(File file) {
        return createPreprocHandler(file, getFileContainer().getPreprocState(file));
    }

    /* package */ final APTPreprocHandler createPreprocHandler(File file, APTPreprocHandler.State state) {
        APTPreprocHandler preprocHandler = createEmptyPreprocHandler(file);
        if (state != null) {
            if (state.isCleaned()) {
                return restorePreprocHandler(file, preprocHandler, state);
            } else {
                if (TRACE_PP_STATE_OUT) {
                    System.err.println("copying state for " + file);
                }
                preprocHandler.setState(state);
                return preprocHandler;
            }
        }
        if (TRACE_PP_STATE_OUT) {
            System.err.printf("null state for %s, returning default one", file);
        }
        return preprocHandler;
    }

    public final Collection<APTPreprocHandler> getPreprocHandlers(File file) {
        Collection<APTPreprocHandler.State> states = getFileContainer().getPreprocStates(file);
        Collection<APTPreprocHandler> result = new ArrayList<APTPreprocHandler>(states.size());
        for (APTPreprocHandler.State state : states) {
            APTPreprocHandler preprocHandler = createEmptyPreprocHandler(file);
            if (state != null) {
                if (state.isCleaned()) {
                    preprocHandler = restorePreprocHandler(file, preprocHandler, state);
                } else {
                    if (TRACE_PP_STATE_OUT) {
                        System.err.println("copying state for " + file);
                    }
                    preprocHandler.setState(state);
                }
            }
            if (TRACE_PP_STATE_OUT) {
                System.err.printf("null state for %s, returning default one", file);
            }
            result.add(preprocHandler);
        }
        return result;
    }

    //@Deprecated
    public final APTPreprocHandler.State getPreprocState(FileImpl fileImpl) {
        APTPreprocHandler.State state = null;
        FileContainer fc = getFileContainer();
        File file = fileImpl.getBuffer().getFile();
        state = fc.getPreprocState(file);
        return state;
    }

    public final Collection<APTPreprocHandler.State> getPreprocStates(FileImpl fileImpl) {
        FileContainer fc = getFileContainer();
        return fc.getPreprocStates(fileImpl.getFile());
    }

    /**
     * This method for testing purpose only. Used from TraceModel
     */
    public final CsmFile testAPTParseFile(NativeFileItem item) {
        APTPreprocHandler preprocHandler = this.createPreprocHandler(item);
        return findFile(item.getFile(), getFileType(item), preprocHandler, true, preprocHandler.getState(), item);
    }

    /**
     * This method must be called only under stateLock,
     * to get state lock use
     * Object stateLock = getFileContainer().getLock(file);
     */
    private final void putPreprocState(File file, APTPreprocHandler.State state) {
        if (state != null && !state.isCleaned()) {
            state = APTHandlersSupport.createCleanPreprocState(state);
        }
        getFileContainer().putPreprocState(file, state);
    }

    protected final APTPreprocHandler.State setChangedFileState(NativeFileItem nativeFile) {
        APTPreprocHandler.State state;
        state = createPreprocHandler(nativeFile).getState();
        File file = nativeFile.getFile();
        FileContainer fileContainer = getFileContainer();
        FileContainer.Entry entry = fileContainer.getEntry(file);
        synchronized (entry.getLock()) {
            entry.invalidateStates();
            entry.setState(state, null);
        }
        fileContainer.put();
        return state;
    }

    protected final void invalidatePreprocState(File file) {
        Object stateLock = getFileContainer().getLock(file);
        synchronized (stateLock) {
            getFileContainer().invalidatePreprocState(file);
        }
    }

    /**
     * The method is for tracing/testing/debugging purposes only
     */
    public final void debugInvalidateFiles() {
        getFileContainer().debugClearState();
        for (Iterator<CsmProject> it = getLibraries().iterator(); it.hasNext();) {
            ProjectBase lib = (ProjectBase) it.next();
            lib.debugInvalidateFiles();
        }
    }

    /**
     * called to inform that file was #included from another file with specific preprocHandler
     *
     * @param file included file path
     * @param preprocHandler preprocHandler with which the file is including
     * @param mode of walker forced onFileIncluded for #include directive
     * @return true if it's first time of file including
     *          false if file was included before
     */
    public final FileImpl onFileIncluded(ProjectBase base, CharSequence file, APTPreprocHandler preprocHandler, int mode) throws IOException {
        boolean updateFileContainer = false;
        try {
            disposeLock.readLock().lock();
            if (disposing) {
                return null;
            }
            FileImpl csmFile = findFile(new File(file.toString()), FileImpl.HEADER_FILE, preprocHandler, false, null, null);

            APTFile aptLight = getAPTLight(csmFile);

            if (aptLight == null) {
                // in the case file was just removed
                Utils.LOG.info("Can not find or build APT for file " + file); //NOI18N
                return csmFile;
            }

            APTPreprocHandler.State newState = preprocHandler.getState();

//            if (TraceFlags.TRACE_PC_STATE) {
//                System.err.printf("onFileIncluded  %s %s %s\n", //NOI18N
//                        csmFile.getAbsolutePath(),
//                        TraceUtils.getPreprocStateString(preprocHandler.getState()),
//                        TraceUtils.getMacroString(preprocHandler, TraceFlags.logMacros));
//            }

            FileContainer.Entry entry = getFileContainer().getEntry(csmFile.getBuffer().getFile());

            if (entry == null) {
                // since file container can return empty container the entry can be null.
                StringBuilder buf = new StringBuilder("File container does not have file "); //NOI18N
                buf.append("["+file+"]"); //NOI18N
                if (getFileContainer() == FileContainer.empty()) {
                    buf.append(" because file container is EMPTY."); //NOI18N
                } else {
                    buf.append("."); //NOI18N
                }
                if (isDisposing()) {
                    buf.append("\n\tIt is very strange but project is disposing."); //NOI18N
                }
                if (!isValid()) {
                    buf.append("\n\tIt is very strange but project is invalid."); //NOI18N
                }
                Status st = getStatus();
                if (st != null) {
                    buf.append("\n\tProject "+toString()+" has status "+st+"."); //NOI18N
                }
                Utils.LOG.info(buf.toString());
                return csmFile;
            }

            int entryModCount = 0;

            //
            // Make check based on preprocessor states *before* gathering preprocessor info.
            // If the file should be (re)parsed with new state,
            // store new information in the entry
            //

            Collection<FileContainer.StatePair> statesToKeep = new ArrayList<FileContainer.StatePair>();
            ComparisonResult comparisonResult;
            AtomicBoolean newStateFound = new AtomicBoolean();

            // We need to make this pre check
            // at least for the case of recursion
            synchronized (entry.getLock()) {
                comparisonResult = fillStatesToKeep(newState, entry.getStates(), statesToKeep, newStateFound);
                if (comparisonResult == ComparisonResult.BETTER) {
                    entry.setPendingReparse(true); // #148608 Instable test regressions on CLucene
                    // some of the old states are worse than the new one; we'll deinitely parse
                    if (TraceFlags.SMART_HEADERS_PARSE) {
                        entry.setStates(statesToKeep, new FileContainer.StatePair(newState, null));
                    } else {
                        entry.setState(newState, null);
                    }
                    updateFileContainer = true;
                }
                entryModCount = entry.getModCount();
            }

            // gather macro map from all includes
            FilePreprocessorConditionState pcState = new FilePreprocessorConditionState(csmFile/*, preprocHandler*/);
            APTParseFileWalker walker = new APTParseFileWalker(base, aptLight, csmFile, preprocHandler, pcState);
            walker.visit();

            if (comparisonResult == ComparisonResult.WORSE) {
                return csmFile;
            } else if (comparisonResult == ComparisonResult.SAME && newStateFound.get() /*&& csmFile.isParsed()*/) {
                // it's better than rely on pcStates check -
                // somebody could place state, but not yet calculate pcState
                return csmFile;
            }

            // 1) check that the entry has not been changed since previous check;
            //    if it has, perform the check again
            // 2) check preocessor conditions state (if needed)

            synchronized (entry.getLock()) {
                // if the entry has been changed since previous check, check again
                if (entry.getModCount() != entryModCount) {
                    comparisonResult = fillStatesToKeep(newState, entry.getStates(), statesToKeep, newStateFound);
                    if (comparisonResult == ComparisonResult.WORSE) {
                        return csmFile;
                    }
                    if (!newStateFound.get()) {
                        // our state was removed => it was invalidated => no need to parse
                        return csmFile;
                    }
                }
                // from that point we are NOT interested in what is in the entry:
                // it's locked; "good" states are are in statesToKeep, "bad" states don't matter

                assert comparisonResult != ComparisonResult.WORSE;

                // if another thread decided that it should be REparsed, let's fo it
                // (#148608 Instable test regressions on CLucene)
                boolean clean = entry.isPendingReparse();
                entry.setPendingReparse(false);

                Collection<APTPreprocHandler.State> statesToParse = new ArrayList<APTPreprocHandler.State>();
                statesToParse.add(newState);

                if (comparisonResult == ComparisonResult.BETTER) {
                    clean = true;
                } else {  // comparisonResult == SAME
                    if (TraceFlags.SMART_HEADERS_PARSE) {
                        comparisonResult = fillStatesToKeep(pcState, new ArrayList<FileContainer.StatePair>(statesToKeep), statesToKeep);
                        switch (comparisonResult) {
                            case BETTER:
                                clean = true;
                                break;
                            case SAME:
                                //clean is set by isPendingReparse() call
                                break;
                            case WORSE:
                                return csmFile;
                            default:
                                assert false : "unexpected comparison result: " + comparisonResult; //NOI18N
                                return csmFile;
                        }
                    } else {
                        if (isBetterThanAll(pcState, statesToKeep)) {
                            statesToKeep.clear();
                            clean = true;
                        } else {
                            return csmFile;
                        }
                    }
                }
                // TODO: think over, what if we aready changed entry,
                // but now deny parsing, because base, but not this project, is disposing?!
                if (!isDisposing() && !base.isDisposing()) {
                    if (clean) {
                        if (TraceFlags.SMART_HEADERS_PARSE) {
                            for (FileContainer.StatePair pair : statesToKeep) {
                                statesToParse.add(pair.state);
                            }
                        }
                    }
                    entry.setStates(statesToKeep, new FileContainer.StatePair(newState, pcState));
                    ParserQueue.instance().add(csmFile, statesToParse, ParserQueue.Position.HEAD, clean,
                            clean ? ParserQueue.FileAction.MARK_REPARSE : ParserQueue.FileAction.MARK_MORE_PARSE);
                    if (TraceFlags.TRACE_PC_STATE || TraceFlags.TRACE_PC_STATE_COMPARISION) {
                        traceIncludeScheduling(csmFile, newState, pcState, clean,
                                statesToParse, statesToKeep);
                    }
                    updateFileContainer = true;
                }
            }
            return csmFile;
        } finally {
            disposeLock.readLock().unlock();
            if (updateFileContainer) {
                getFileContainer().put();
            }
        }
    }

    private static void traceIncludeScheduling(
            FileImpl file, APTPreprocHandler.State newState, FilePreprocessorConditionState pcState,
            boolean clean, Collection<APTPreprocHandler.State> statesToParse, Collection<FileContainer.StatePair> statesToKeep) {

        StringBuilder sb = new StringBuilder();
        for (FileContainer.StatePair pair : statesToKeep) {
            if (sb.length() > 0) {
                sb.append(", "); //NOI18N
            }
            sb.append(pair.pcState);
        }


        APTPreprocHandler preprocHandler = file.getProjectImpl(true).createEmptyPreprocHandler(file.getBuffer().getFile());
        preprocHandler.setState(newState);

        System.err.printf("scheduling %s (1) %s %s %s %s keeping [%s]\n", //NOI18N
                (clean ? "reparse" : "  parse"), file.getAbsolutePath(), //NOI18N
                TraceUtils.getPreprocStateString(preprocHandler.getState()),
                TraceUtils.getMacroString(preprocHandler, TraceFlags.logMacros),
                pcState, sb);

        for (APTPreprocHandler.State state : statesToParse) {
            if (!newState.equals(state)) {
                FilePreprocessorConditionState currPcState = null;
                for (FileContainer.StatePair pair : statesToKeep) {
                    if (newState.equals(pair.state)) {
                        currPcState = pair.pcState;
                        break;
                    }
                }
                System.err.printf("scheduling %s (2) %s valid %b context %b %s\n", //NOI18N
                        "  parse", file.getAbsolutePath(), //NOI18N
                        state.isValid(), state.isCompileContext(), currPcState);
            }
        }
    }

    enum ComparisonResult {

        BETTER,
        SAME,
        WORSE
    }
    private static final int BETTER = 1;
    private static final int SAME = 0;
    private static final int WORSE = -1;

    /**
     * Checks old states and new one, decides
     * 1. which states to keep
     *    (returns collection of these states)
     * 2. is new state better, worse, or ~same than old ones
     *
     * NB: all OUT parameters are set in this function, so their initial values don't matter
     *
     * @param newState  IN:  new proprocessor state
     *
     * @param oldStates IN:  a collection of old states;
     *                       it might contain newState as well
     * 
     * @param statesToKeep  OUT: aray to fill with of old states
     *                      (except for new state! - it isnt copied here)
     *                      Unpredictable in the case function returns WORSE 
     *
     * @param  newStateFound  OUT: set to true if new state is found among old ones
     *
     * @return  BETTER - new state is better than old ones 
     *          SAME - new state is more or less  the same :) as old ones
     *          WORSE - new state is worse than old ones
     *
     * NB: if exit is true, the return value is unpredictable and shouldn't be used.
     */
    private ComparisonResult fillStatesToKeep(
            APTPreprocHandler.State newState,
            Collection<FileContainer.StatePair> oldStates,
            Collection<FileContainer.StatePair> statesToKeep,
            AtomicBoolean newStateFound) {

        if (newState == null || !newState.isValid()) {
            return ComparisonResult.WORSE;
        }

        statesToKeep.clear();
        newStateFound.set(false);
        ComparisonResult result = ComparisonResult.SAME;

        for (FileContainer.StatePair pair : oldStates) {
            // newState might already be contained in oldStates
            // it should NOT be added to result
            if (newState.equals(pair.state)) {
                assert !newStateFound.get();
                newStateFound.set(true);
            } else {
                boolean keep = false;
                if (pair.state != null && pair.state.isValid()) {
                    if (pair.state.isCompileContext()) {
                        keep = true;
                        if (!newState.isCompileContext()) {
                            return ComparisonResult.WORSE;
                        }
                    } else {
                        keep = !newState.isCompileContext();
                    }
                }
                if (keep) {
                    if (!pair.state.isCleaned()) {
                        pair = new FileContainer.StatePair(APTHandlersSupport.createCleanPreprocState(pair.state), pair.pcState);
                    }
                    statesToKeep.add(pair);
                } else {
                    result = ComparisonResult.BETTER;
                }
            }
        }
        return result;
    }

    private boolean isBetterThanAll(
            FilePreprocessorConditionState pcState,
            Collection<FileContainer.StatePair> oldStates) {

        if (TraceFlags.NO_HEADERS_REPARSE) {
            return false;
        }
        for (FileContainer.StatePair pair : oldStates) {
            if (!pcState.isBetter(pair.pcState)) {
                return false;
            }
        }
        return true;
    }

    /**
     * If it returns EXIT, statesToKeep content is unpredictable!
     * 
     * @param newState
     * @param pcState
     * @param oldStates
     * @param statesToKeep
     * @return
     */
    private ComparisonResult fillStatesToKeep(
            FilePreprocessorConditionState pcState,
            Collection<FileContainer.StatePair> oldStates,
            Collection<FileContainer.StatePair> statesToKeep) {

        boolean isSuperset = true; // true if this state is a superset of each old state

        Collection<FilePreprocessorConditionState> possibleSuperSet = new ArrayList<FilePreprocessorConditionState>();

        // we assume that
        // 1. all statesToKeep are valid
        // 2. either them all are compileContext
        //    or this one and them all are NOT compileContext
        // so we do *not* check isValid & isCompileContext

        statesToKeep.clear();

        for (FileContainer.StatePair old : oldStates) {
            if (pcState.isSubset(old.pcState)) {
                return ComparisonResult.WORSE;
            }
            if (old.pcState == null) {
                isSuperset = false;
                // not yet filled - somebody is filling it right now => we don't know what it will be => keep it
                if (!old.state.isCleaned()) {
                    old = new FileContainer.StatePair(APTHandlersSupport.createCleanPreprocState(old.state), old.pcState);
                }
                statesToKeep.add(old);
            } else {
                possibleSuperSet.add(old.pcState);
                if (!old.pcState.isSubset(pcState)) {
                    isSuperset = false;
                    if (!old.state.isCleaned()) {
                        old = new FileContainer.StatePair(APTHandlersSupport.createCleanPreprocState(old.state), old.pcState);
                    }
                    statesToKeep.add(old);
                }
            }
        }
        if (isSuperset) {
            statesToKeep.clear();
            return ComparisonResult.BETTER;
        } else {
            if (pcState.isSubset(possibleSuperSet)) {
                return ComparisonResult.WORSE;
            } else {
                return ComparisonResult.SAME;
            }
        }
    }

//    private static final boolean isValid(APTPreprocHandler.State state) {
//        return state != null && state.isValid();
//    }
    public ProjectBase findFileProject(CharSequence absPath) {
        // check own files
        // Wait while files are created. Otherwise project file will be recognized as library file.
        ensureFilesCreated();
        File file = new File(absPath.toString());
        if (getFile(file) != null) {
            return this;
        } else {
            // else check in libs
            for (CsmProject prj : getLibraries()) {
                // Wait while files are created. Otherwise project file will be recognized as library file.
                ((ProjectBase) prj).ensureFilesCreated();
                if (((ProjectBase) prj).getFile(file) != null) {
                    return (ProjectBase) prj;
                }
            }
        }
        return null;
    }

    public final boolean isMySource(String includePath) {
        return projectRoots.isMySource(includePath);
    }

    public abstract void onFileAdded(NativeFileItem nativeFile);

    public abstract void onFileAdded(List<NativeFileItem> items);
    //public abstract void onFileRemoved(NativeFileItem nativeFile);

    public abstract void onFileRemoved(FileImpl fileImpl);

    public abstract void onFileImplRemoved(List<FileImpl> files);

    public abstract void onFileRemoved(List<NativeFileItem> items);

    public abstract void onFilePropertyChanged(NativeFileItem nativeFile);

    public abstract void onFilePropertyChanged(List<NativeFileItem> items);

    protected abstract ParserQueue.Position getIncludedFileParserQueuePosition();

    public abstract NativeFileItem getNativeFileItem(CsmUID<CsmFile> file);

    protected abstract void putNativeFileItem(CsmUID<CsmFile> file, NativeFileItem nativeFileItem);

    protected abstract void removeNativeFileItem(CsmUID<CsmFile> file);

    protected abstract void clearNativeFileContainer();

    public final void onFileRemoved(File nativeFile) {
        onFileRemoved(getFile(nativeFile));
    }

    public final void onFileExternalCreate(FileObject file) {
        APTIncludeUtils.clearFileExistenceCache();
        DeepReparsingUtils.reparseOnAdded(file, this);
    }

    public final void onFileExternalChange(FileImpl file) {
        DeepReparsingUtils.reparseOnEdit(file, this);
    }

    public final CsmFile findFile(Object absolutePathOrNativeFileItem) {
        if (absolutePathOrNativeFileItem instanceof CharSequence) {
            return findFileByPath((CharSequence) absolutePathOrNativeFileItem);
        } else if (absolutePathOrNativeFileItem instanceof NativeFileItem) {
            return findFileByItem((NativeFileItem) absolutePathOrNativeFileItem);
        }
        return null;
    }

    private CsmFile findFileByPath(CharSequence absolutePath) {
        File file = new File(absolutePath.toString());
        APTPreprocHandler preprocHandler = null;
        if (getFileContainer().getPreprocState(file) == null) {
            NativeFileItem nativeFile = null;
            // Try to find native file
            if (getPlatformProject() instanceof NativeProject) {
                NativeProject prj = (NativeProject) getPlatformProject();
                if (prj != null) {
                    nativeFile = prj.findFileItem(file);
                    if (nativeFile == null) {
                        // if not belong to NB project => not our file
                        return null;
                    // nativeFile = new DefaultFileItem(prj, absolutePath);
                    }
                    if (!acceptNativeItem(nativeFile)) {
                        return null;
                    }
                    preprocHandler = createPreprocHandler(nativeFile);
                }
            }
            if (preprocHandler != null) {
                return findFile(file, FileImpl.UNDEFINED_FILE, preprocHandler, true, preprocHandler.getState(), nativeFile);
            }
        }
        // if getPreprocState(file) isn't null, the file alreasy exists, so we may not pass nativeFile
        return findFile(file, FileImpl.UNDEFINED_FILE, preprocHandler, true, null, null);
    }

    private CsmFile findFileByItem(NativeFileItem nativeFile) {
        File file = nativeFile.getFile().getAbsoluteFile();
        APTPreprocHandler preprocHandler = null;
        if (getFileContainer().getPreprocState(file) == null) {
            if (!acceptNativeItem(nativeFile)) {
                return null;
            }
            // Try to find native file
            if (getPlatformProject() instanceof NativeProject) {
                NativeProject prj = nativeFile.getNativeProject();
                if (prj != null && nativeFile.getFile() != null) {
                    preprocHandler = createPreprocHandler(nativeFile);
                }
            }
            if (preprocHandler != null) {
                return findFile(file, FileImpl.UNDEFINED_FILE, preprocHandler, true, preprocHandler.getState(), nativeFile);
            }
        }
        // if getPreprocState(file) isn't null, the file alreasy exists, so we may not pass nativeFile
        return findFile(file, FileImpl.UNDEFINED_FILE, preprocHandler, true, null, null);
    }

    protected final FileImpl findFile(File file, int fileType, APTPreprocHandler preprocHandler,
            boolean scheduleParseIfNeed, APTPreprocHandler.State initial, NativeFileItem nativeFileItem) {
        FileImpl impl = getFile(file);
        if (impl == null){
            impl = findFileImpl(file, fileType, preprocHandler, scheduleParseIfNeed, initial, nativeFileItem);
        }
        return impl;
    }

    private FileImpl findFileImpl(File file, int fileType, APTPreprocHandler preprocHandler,
            boolean scheduleParseIfNeed, APTPreprocHandler.State initial, NativeFileItem nativeFileItem) {
        FileImpl impl = null;
        synchronized (fileContainerLock) {
            impl = getFile(file);
            if (impl == null) {
                preprocHandler = (preprocHandler == null) ? getPreprocHandler(file) : preprocHandler;
                impl = new FileImpl(ModelSupport.getFileBuffer(file), this, fileType, nativeFileItem);
                if (nativeFileItem != null) {
                    putNativeFileItem(impl.getUID(), nativeFileItem);
                }
                putFile(file, impl, initial);
                // NB: parse only after putting into a map
                if (scheduleParseIfNeed) {
                    APTPreprocHandler.State ppState = preprocHandler == null ? null : preprocHandler.getState();
                    ParserQueue.instance().add(impl, ppState, ParserQueue.Position.TAIL);
                }
            }
        }
        if (fileType == FileImpl.SOURCE_FILE && !impl.isSourceFile()) {
            impl.setSourceFile();
        } else if (fileType == FileImpl.HEADER_FILE && !impl.isHeaderFile()) {
            impl.setHeaderFile();
        }
        if (initial != null) {
            synchronized (getFileContainer().getLock(file)) {
                Collection<APTPreprocHandler.State> states = getFileContainer().getPreprocStates(file);
                if (states == null || states.isEmpty() || (states.size() == 1 && states.iterator().next() == null)) {
                    putPreprocState(file, initial);
                }
            }
        }
        return impl;
    }

//    protected FileImpl createOrFindFileImpl(final NativeFileItem nativeFile) {
//	File file = nativeFile.getFile();
//	assert file != null;
//	return createOrFindFileImpl(ModelSupport.instance().getFileBuffer(file), nativeFile);
//    }
    protected final FileImpl createOrFindFileImpl(final FileBuffer buf, final NativeFileItem nativeFile) {
        return createOrFindFileImpl(buf, nativeFile, getFileType(nativeFile)).fileImpl;
    }

    private static class FileAndHandler {

        public FileAndHandler(FileImpl fileImpl, APTPreprocHandler preprocHandler) {
            this.fileImpl = fileImpl;
            this.preprocHandler = preprocHandler;
        }
        public FileImpl fileImpl;
        public APTPreprocHandler preprocHandler;
    }

    private FileAndHandler createOrFindFileImpl(final FileBuffer buf, final NativeFileItem nativeFile, int fileType) {
        APTPreprocHandler preprocHandler = null;
        File file = buf.getFile();
        FileImpl impl = getFile(file);
        if (impl == null) {
            synchronized (fileContainerLock) {
                impl = getFile(file);
                if (impl == null) {
                    preprocHandler = createPreprocHandler(nativeFile);
                    assert preprocHandler != null;
                    impl = new FileImpl(buf, this, fileType, nativeFile);
                    putFile(file, impl, preprocHandler.getState());
                } else {
                    putNativeFileItem(impl.getUID(), nativeFile);
                }
            }
        } else {
            putNativeFileItem(impl.getUID(), nativeFile);
        }
        return new FileAndHandler(impl, preprocHandler);
    }

    public final FileImpl getFile(File file) {
        return getFileContainer().getFile(file);
    }

    protected final void removeFile(CharSequence file) {
        getFileContainer().removeFile(file);
    }

    protected final void putFile(File file, FileImpl impl, APTPreprocHandler.State state) {
        if (state != null && !state.isCleaned()) {
            state = APTHandlersSupport.createCleanPreprocState(state);
        }
        getFileContainer().putFile(file, impl, state);
    }

    protected Collection<Key> getLibrariesKeys() {
        List<Key> res = new ArrayList<Key>();
        if (platformProject instanceof NativeProject) {
            for (NativeProject nativeLib : ((NativeProject) platformProject).getDependences()) {
                final Key key = createProjectKey(nativeLib);
                if (key != null) {
                    res.add(key);
                }
            }
        }
        // Last dependent project is common library.
        //final Key lib = KeyUtilities.createProjectKey("/usr/include"); // NOI18N
        //if (lib != null) {
        //    res.add(lib);
        //}
        if (!isArtificial()) {
            for (CsmUID<CsmProject> library : LibraryManager.getInstance().getLirariesKeys(getUID())) {
                res.add(RepositoryUtils.UIDtoKey(library));
            }
        }
        return res;
    }

    public Collection<CsmProject> getLibraries() {
        List<CsmProject> res = new ArrayList<CsmProject>();
        if (platformProject instanceof NativeProject) {
            for (NativeProject nativeLib : ((NativeProject) platformProject).getDependences()) {
                CsmProject prj = model.findProject(nativeLib);
                if (prj != null) {
                    res.add(prj);
                }
            }
        }
        // Last dependent project is common library.
        //ProjectBase lib = getModel().getLibrary("/usr/include"); // NOI18N
        //if (lib != null) {
        //    res.add(lib);
        //}
        if (!isArtificial()) {
            for (LibProjectImpl library : LibraryManager.getInstance().getLibraries((ProjectImpl) this)) {
                res.add(library);
            }
        }
        return res;
    }

    public final List<ProjectBase> getDependentProjects() {
        List<ProjectBase> res = new ArrayList<ProjectBase>();
        for (CsmProject prj : model.projects()) {
            if (prj instanceof ProjectBase) {
                if (prj.getLibraries().contains(this)) {
                    res.add((ProjectBase) prj);
                }
            }
        }
        return res;
    }

    /**
     * Creates a dummy ClassImpl for uresolved name, stores in map
     * @param nameTokens name
     * @param file file that contains unresolved name (used for the purpose of statictics)
     * @param name offset that contains unresolved name (used for the purpose of statictics)
     */
    public final CsmClass getDummyForUnresolved(CharSequence[] nameTokens, CsmFile file, int offset) {
        if (Diagnostic.needStatistics()) {
            Diagnostic.onUnresolvedError(nameTokens, file, offset);
        }
        return getUnresolved().getDummyForUnresolved(nameTokens);
    }

    /**
     * Creates a dummy ClassImpl for uresolved name, stores in map.
     * Should be used only when restoring from persistence:
     * in contrary to getDummyForUnresolved(String[] nameTokens, CsmFile file, int offset),
     * it does not gather statistics!
     * @param nameTokens name
     */
    public final CsmClass getDummyForUnresolved(CharSequence name) {
        return getUnresolved().getDummyForUnresolved(name);
    }

    public final CsmNamespace getUnresolvedNamespace() {
        return getUnresolved().getUnresolvedNamespace();
    }

    public final CsmFile getUnresolvedFile() {
        return getUnresolved().getUnresolvedFile();
    }

    private synchronized Unresolved getUnresolved() {
        // we don't sinc here since this isn't important enough:
        // at worst a map with one or two dummies will be thrown away
        if (unresolved == null) {
            unresolved = new Unresolved(this);
        }
        return unresolved;
    }

    public final boolean isValid() {
        return platformProject != null && !disposing;
    }

    public void setDisposed() {
        disposing = true;
        synchronized (initializationTaskLock) {
            if (initializationTask != null) {
                initializationTask.cancel();
                initializationTask = null;
            }
        }
        unregisterProjectListeners();
        ParserQueue.instance().removeAll(this);
    }

    public final boolean isDisposing() {
        return disposing;
    }

    public final void dispose(final boolean cleanPersistent) {

        long time = 0;
        if (TraceFlags.TIMING) {
            System.err.printf("\n\nProject %s: disposing...\n", name);
            time = System.currentTimeMillis();
        }

        // just in case it wasn't called before (it's inexpensive)
        setDisposed();

        try {

            disposeLock.writeLock().lock();

            ProjectSettingsValidator validator = new ProjectSettingsValidator(this);
            validator.storeSettings();
            getUnresolved().dispose();
            RepositoryUtils.closeUnit(getUID(), getRequiredUnits(), cleanPersistent);

            platformProject = null;
            unresolved = null;
            uid = null;
        } finally {
            disposeLock.writeLock().unlock();
        }

        if (TraceFlags.TIMING) {
            time = System.currentTimeMillis() - time;
            System.err.printf("Project %s: disposing took %d ms\n", name, time);
        }
    }

    protected final Set<String> getRequiredUnits() {
        Set<String> requiredUnits = new HashSet<String>();
        for (Key dependent : this.getLibrariesKeys()) {
            requiredUnits.add(dependent.getUnit().toString());
        }
        return requiredUnits;
    }

//    private void disposeFiles() {
//        Collection<FileImpl> list = getFileContainer().getFileImpls();
//        getFileContainer().clear();
//        for (FileImpl file : list){
//            file.onProjectClose();
//            APTDriver.getInstance().invalidateAPT(file.getBuffer());
//        }
//        //clearNativeFileContainer();
//    }
    private NamespaceImpl _getGlobalNamespace() {
        NamespaceImpl ns = (NamespaceImpl) UIDCsmConverter.UIDtoNamespace(globalNamespaceUID);
        if (ns == null) {
            DiagnosticExceptoins.register(new IllegalStateException("Failed to get global namespace by key " + globalNamespaceUID)); // NOI18N
        }
        return ns != null ? ns : FAKE_GLOBAL_NAMESPACE;
    }

    private NamespaceImpl _getNamespace(CharSequence key) {
        key = CharSequenceKey.create(key);
        CsmUID<CsmNamespace> nsUID = namespaces.get(key);
        NamespaceImpl ns = (NamespaceImpl) UIDCsmConverter.UIDtoNamespace(nsUID);
        return ns;
    }

    private void _registerNamespace(NamespaceImpl ns) {
        assert (ns != null);
        CharSequence key = ns.getQualifiedName();
        assert key instanceof TinyCharSequence;
        CsmUID<CsmNamespace> nsUID = RepositoryUtils.<CsmNamespace>put(ns);
        assert nsUID != null;
        namespaces.put(key, nsUID);
    }

    private void _unregisterNamespace(NamespaceImpl ns) {
        assert (ns != null);
        assert !ns.isGlobal();
        CharSequence key = ns.getQualifiedName();
        assert key instanceof TinyCharSequence;
        CsmUID<CsmNamespace> nsUID = namespaces.remove(key);
        assert nsUID != null;
        RepositoryUtils.remove(nsUID);
    }

    protected final ModelImpl getModel() {
        return model;
    }

    public void onFileEditStart(FileBuffer buf, NativeFileItem nativeFile) {
    }

    public void onFileEditEnd(FileBuffer buf, NativeFileItem nativeFile) {
    }
    private CsmUID<CsmProject> uid = null;

    public final CsmUID<CsmProject> getUID() { // final because called from constructor
        CsmUID<CsmProject> out = uid;
        if (out == null) {
            synchronized (this) {
                if (uid == null) {
                    uid = out = UIDUtilities.createProjectUID(this);
                    if (TraceFlags.TRACE_CPU_CPP) {System.err.println("getUID for project UID@"+System.identityHashCode(uid) + uid + "on prj@"+System.identityHashCode(this));}
                }
            }
        }
        return uid;
    }

    public boolean isStable(CsmFile skipFile) {
        if (status == Status.Ready && !disposing) {
            return !ParserQueue.instance().hasFiles(this, (FileImpl) skipFile);
        }
        return false;
    }

    public final void onParseFinish(boolean libsAlreadyParsed) {
        synchronized (waitParseLock) {
            waitParseLock.notifyAll();
        }
        // it's ok to move the entire sycle into synchronized block,
        // because from inter-session persistence point of view,
        // if we don't fix fakes, we'll later consider that files are ok,
        // which is incorrect if there are some fakes
        try {
            disposeLock.readLock().lock();

            if (!disposing) {
                fixFakeRegistration(libsAlreadyParsed);
            }
        } catch (Exception e) {
            DiagnosticExceptoins.register(e);
        } finally {
            disposeLock.readLock().unlock();
            ProjectComponent.setStable(declarationsSorageKey);
            ProjectComponent.setStable(fileContainerKey);
            ProjectComponent.setStable(graphStorageKey);
            ProjectComponent.setStable(classifierStorageKey);

            if (!libsAlreadyParsed) {
                ParseFinishNotificator.onParseFinish(this);
            }
        }
        if (TraceFlags.PARSE_STATISTICS) {
            ParseStatistics.getInstance().printResults(this);
            ParseStatistics.getInstance().clear(this);
        }
    }

    private int getNumberThreads(){
        int threadCount = Integer.getInteger("cnd.modelimpl.parser.threads", // NOI18N
                Runtime.getRuntime().availableProcessors()).intValue(); // NOI18N
        threadCount = Math.min(threadCount, 4);
        return Math.max(threadCount, 1);
    }

    public final void fixFakeRegistration(boolean libsAlreadyParsed){
        final Collection<CsmUID<CsmFile>> files = getAllFilesUID();
        CountDownLatch countDownLatch = new CountDownLatch(files.size());
        RequestProcessor rp = new RequestProcessor("Fix registration", getNumberThreads()); // NOI18N
        for (CsmUID<CsmFile> file : files) {
            MyRunnable r = new MyRunnable(countDownLatch, file, libsAlreadyParsed);
            rp.post(r);
        }
        try {
            countDownLatch.await();
        } catch (InterruptedException ex) {
        }
    }


    public final void onLibParseFinish() {
        onParseFinish(true);
    }

    /**
     * CsmProject implementation
     */
    public final Collection<CsmFile> getAllFiles() {
        return getFileContainer().getFiles();
    }

    /**
     * CsmProject implementation
     */
    public final Collection<CsmUID<CsmFile>> getAllFilesUID() {
        return getFileContainer().getFilesUID();
    }

    /**
     * We'd better name this getFiles();
     * but unfortunately there already is such method,
     * and it is used intensively
     */
    public final Collection<FileImpl> getAllFileImpls() {
        return getFileContainer().getFileImpls();
    }

    public final Collection<CsmFile> getSourceFiles() {
        List<CsmUID<CsmFile>> uids = new ArrayList<CsmUID<CsmFile>>();
        for (FileImpl file : getAllFileImpls()) {
            if (file.isSourceFile()) {
                uids.add(file.getUID());
            }
        }
        return new LazyCsmCollection<CsmFile, CsmFile>(uids, TraceFlags.SAFE_UID_ACCESS);
    }

    public final Collection<CsmFile> getHeaderFiles() {
        List<CsmUID<CsmFile>> uids = new ArrayList<CsmUID<CsmFile>>();
        for (FileImpl file : getAllFileImpls()) {
            if (!file.isSourceFile()) {
                uids.add(file.getUID());
            }
        }
        return new LazyCsmCollection<CsmFile, CsmFile>(uids, TraceFlags.SAFE_UID_ACCESS);
    }

    public final long getMemoryUsageEstimation() {
        //TODO: replace with some smart algorythm
        return getFileContainer().getSize();
    }

    @Override
    public final String toString() {
        return getName().toString() + ' ' + getClass().getName() + " @" + hashCode() + ":" + super.hashCode(); // NOI18N
    }

    /**
     * Just a struct for the getStartEntryInfo return valie:
     * if java allowed passing pointers by reference, we won't create this...
     */
    private static class /*struct*/ StartEntryInfo {

        public final APTPreprocHandler preprocHandler;
        public final ProjectBase startProject;
        public final FileImpl csmFile;

        public StartEntryInfo(APTPreprocHandler preprocHandler, ProjectBase startProject, FileImpl csmFile) {
            this.preprocHandler = preprocHandler;
            this.startProject = startProject;
            this.csmFile = csmFile;
        }
    }

    private StartEntryInfo getStartEntryInfo(APTPreprocHandler preprocHandler, APTPreprocHandler.State state) {
        StartEntry startEntry = APTHandlersSupport.extractStartEntry(state);
        ProjectBase startProject = getStartProject(startEntry);
        FileImpl csmFile = startProject == null ? null : startProject.getFile(new File(startEntry.getStartFile().toString()));
        if (csmFile != null) {
            NativeFileItem nativeFile = csmFile.getNativeFileItem();
            if (nativeFile != null && nativeFile.getFile() != null) {
                preprocHandler = startProject.createPreprocHandler(nativeFile);
            }
        }
        return new StartEntryInfo(preprocHandler, startProject, csmFile);
    }

    private APTPreprocHandler restorePreprocHandler(File interestedFile, APTPreprocHandler preprocHandler, APTPreprocHandler.State state) {
        assert state != null;
        assert state.isCleaned();
        // walk through include stack to restore preproc information
        List<APTIncludeHandler.IncludeInfo> reverseInclStack = APTHandlersSupport.extractIncludeStack(state);
        assert (reverseInclStack != null);
        if (reverseInclStack.isEmpty()) {
            if (TRACE_PP_STATE_OUT) {
                System.err.println("stack is empty; return default for " + interestedFile);
            }
            return getStartEntryInfo(preprocHandler, state).preprocHandler;
        } else {
            if (TRACE_PP_STATE_OUT) {
                System.err.println("restoring for " + interestedFile);
            }
            // we need to reverse includes stack
            assert (!reverseInclStack.isEmpty()) : "state of stack is " + reverseInclStack;
            Stack<APTIncludeHandler.IncludeInfo> inclStack = reverse(reverseInclStack);
            StartEntryInfo sei = getStartEntryInfo(preprocHandler, state);
            FileImpl csmFile = sei.csmFile;
            ProjectBase startProject = sei.startProject;
            preprocHandler = sei.preprocHandler;

            APTFile aptLight = null;
            try {
                aptLight = csmFile == null ? null : getAPTLight(csmFile);
            } catch (IOException ex) {
                System.err.println("can't restore preprocessor state for " + interestedFile + //NOI18N
                        "\nreason: " + ex.getMessage());//NOI18N
                DiagnosticExceptoins.register(ex);
            }
            boolean ppStateRestored = false;
            if (aptLight != null) {
                // for testing remember restored file
                long time = REMEMBER_RESTORED ? System.currentTimeMillis() : 0;
                int stackSize = inclStack.size();
                APTWalker walker = new APTRestorePreprocStateWalker(startProject, aptLight, csmFile, preprocHandler, inclStack, FileContainer.getFileKey(interestedFile, false).toString());
                walker.visit();
                if (preprocHandler.isValid()) {
                    if (REMEMBER_RESTORED) {
                        if (testRestoredFiles == null) {
                            testRestoredFiles = new ArrayList<String>();
                        }
                        FileImpl interestedFileImpl = getFile(interestedFile);
                        assert interestedFileImpl != null;
                        String msg = interestedFile.getAbsolutePath() + " [" + (interestedFileImpl.isHeaderFile() ? "H" : interestedFileImpl.isSourceFile() ? "S" : "U") + "]"; // NOI18N
                        time = System.currentTimeMillis() - time;
                        msg = msg + " within " + time + "ms" + " stack " + stackSize + " elems"; // NOI18N
                        System.err.println("#" + testRestoredFiles.size() + " restored: " + msg); // NOI18N
                        testRestoredFiles.add(msg);
                    }
                    if (TRACE_PP_STATE_OUT) {
                        System.err.println("after restoring " + preprocHandler); // NOI18N
                    }
                    ppStateRestored = true;
                }
            }
            if (!ppStateRestored) {
                // need to recover from the problem, when start file is invalid or absent
                // try to find project who can create default handler with correct
                // compiler settings
                // preferences is start project
                if (startProject == null) {
                    // otherwise use the project owner
                    startProject = this;
                }
                preprocHandler = startProject.createDefaultPreprocHandler(interestedFile);
            // remember
            // TODO: file container should accept all without checks
            // otherwise state will not be replaced
//                synchronized (getFileContainer().getLock(interestedFile)) {
//                    if (state.equals(getPreprocState(interestedFile))) {
//                        APTPreprocHandler.State recoveredState = preprocHandler.getState();
//                        assert !recoveredState.isCompileContext();
//                        putPreprocState(interestedFile, recoveredState);
//                    }
//                }
            }
            return preprocHandler;
        }
    }

    /*
    private APTPreprocHandler restorePreprocHandler(File interestedFile, APTPreprocHandler preprocHandler, APTPreprocHandler.State state) {
    assert state != null;
    assert state.isCleaned();
    // walk through include stack to restore preproc information
    List<APTIncludeHandler.IncludeInfo> reverseInclStack = APTHandlersSupport.extractIncludeStack(state);
    assert (reverseInclStack != null);
    if (reverseInclStack.isEmpty()) {
    if (TRACE_PP_STATE_OUT) System.err.println("stack is empty; return default for " + interestedFile);
    return getStartEntryInfo(preprocHandler, state).preprocHandler;
    } else {
    if (TRACE_PP_STATE_OUT) System.err.println("restoring for " + interestedFile);
    // we need to reverse includes stack
    assert (!reverseInclStack.isEmpty()) : "state of stack is " + reverseInclStack;
    StartEntryInfo sei = getStartEntryInfo(preprocHandler, state);
    FileImpl csmFile = sei.csmFile;
    ProjectBase startProject = sei.startProject;
    preprocHandler = sei.preprocHandler;

    ProjectBase prevProject = startProject;
    // for testing remember restored file
    long time = REMEMBER_RESTORED ? System.currentTimeMillis() : 0;
    boolean ppStateRestored = false;
    for (int i = 0; i < reverseInclStack.size() && !ppStateRestored; i++) {
    Stack<APTIncludeHandler.IncludeInfo> inclStack = reverse(reverseInclStack);
    if (i > 0) {
    System.err.println("need to try smaller stack");
    sei = getStartEntryInfo(preprocHandler, state);
    preprocHandler = sei.preprocHandler;
    for (int j = i; j > 0; j--) {
    inclStack.pop();
    }
    String includedPath = inclStack.peek().getIncludedPath();
    File startFile = new File(includedPath);
    csmFile = null;
    if (prevProject != null) {
    startProject = LibraryManager.getInstance().searchInProjectFiles(prevProject, startFile);
    csmFile = startProject == null ? null : startProject.getFile(startFile);
    }
    if (csmFile == null) {
    CsmFile foundCsmFile = CsmModelAccessor.getModel().findFile(includedPath);
    if (foundCsmFile instanceof FileImpl) {
    csmFile = (FileImpl)foundCsmFile;
    }
    }
    if (csmFile != null) {
    prevProject = csmFile.getProjectImpl();
    }
    }
    APTFile aptLight = null;
    try {
    aptLight = csmFile == null ? null : getAPTLight(csmFile);
    } catch (IOException ex) {
    System.err.println("can't restore preprocessor state for " + interestedFile + //NOI18N
    "\nreason: " + ex.getMessage());//NOI18N
    }
    if (aptLight != null) {
    int stackSize = inclStack.size();
    APTWalker walker = new APTRestorePreprocStateWalker(startProject, aptLight, csmFile, preprocHandler, inclStack, FileContainer.getFileKey(interestedFile, false));
    walker.visit();
    if (preprocHandler.isValid()) {
    if (REMEMBER_RESTORED) {
    if (testRestoredFiles == null) {
    testRestoredFiles = new ArrayList<String>();
    }
    FileImpl interestedFileImpl = getFile(interestedFile);
    assert interestedFileImpl != null;
    String msg = interestedFile.getAbsolutePath() + " [" + (interestedFileImpl.isHeaderFile() ? "H" : interestedFileImpl.isSourceFile() ? "S" : "U") + "]"; // NOI18N
    time = System.currentTimeMillis() - time;
    msg = msg + " within " + time + "ms" + " stack " + stackSize + " elems" + " orininal size was " + reverseInclStack.size(); // NOI18N
    System.err.println("#" + testRestoredFiles.size() + " restored: " + msg); // NOI18N
    testRestoredFiles.add(msg);
    }
    if (TRACE_PP_STATE_OUT) {
    System.err.println("after restoring " + preprocHandler); // NOI18N
    }
    ppStateRestored = true;
    }
    }
    }
    if (!ppStateRestored) {
    // need to recover from the problem, when start file is invalid or absent
    // try to find project who can create default handler with correct
    // compiler settings
    // preferences is start project
    if (startProject == null) {
    // otherwise use the project owner
    startProject = this;
    }
    preprocHandler = startProject.createDefaultPreprocHandler(interestedFile);
    // remember
    // TODO: file container should accept all without checks
    // otherwise state will not be replaced
    //                synchronized (getFileContainer().getLock(interestedFile)) {
    //                    if (state.equals(getPreprocState(interestedFile))) {
    //                        APTPreprocHandler.State recoveredState = preprocHandler.getState();
    //                        assert !recoveredState.isCompileContext();
    //                        putPreprocState(interestedFile, recoveredState);
    //                    }
    //                }
    }
    return preprocHandler;
    }
    }
     */
    private NativeProject findNativeProjectHolder(Set<ProjectBase> visited) {
        visited.add(this);
        NativeProject nativeProject = ModelSupport.getNativeProject(getPlatformProject());
        if (nativeProject == null) {
            // try to find dependent projects and ask them
            List<ProjectBase> deps = this.getDependentProjects();
            for (ProjectBase dependentPrj : deps) {
                if (!visited.contains(dependentPrj)) {
                    nativeProject = dependentPrj.findNativeProjectHolder(visited);
                    if (nativeProject != null) {
                        // found
                        break;
                    }
                }
            }
        }
        return nativeProject;
    }

    private APTPreprocHandler createDefaultPreprocHandler(File interestedFile) {
        NativeProject nativeProject = findNativeProjectHolder(new HashSet<ProjectBase>(10));
        APTPreprocHandler out = null;
        if (nativeProject != null) {
            // we have own native project to get settings from
            NativeFileItem item = new DefaultFileItem(nativeProject, interestedFile.getAbsolutePath());
            out = createPreprocHandler(item);
        } else {
            out = createEmptyPreprocHandler(interestedFile);
        }
        assert out != null : "failed creating default ppState for " + interestedFile;
        return out;
    }

    private static <T> Stack<T> reverse(List<T> original) {
        Stack<T> reverse = new Stack<T>();
        for (int i = original.size() - 1; i >= 0; i--) {
            T inclInfo = original.get(i);
            reverse.push(inclInfo);
        }
        return reverse;
    }

    public static NativeFileItem getCompiledFileItem(FileImpl fileImpl) {
        NativeFileItem out = null;
        ProjectBase filePrj = fileImpl.getProjectImpl(true);
        if (filePrj != null) {
            APTPreprocHandler.State state = filePrj.getPreprocState(fileImpl);
            FileImpl startFile = getStartFile(state);
            out = startFile != null ? startFile.getNativeFileItem() : null;
        }
        return out;
    }

    public static FileImpl getStartFile(final APTPreprocHandler.State state) {
        StartEntry startEntry = APTHandlersSupport.extractStartEntry(state);
        ProjectBase startProject = getStartProject(startEntry);
        FileImpl csmFile = startProject == null ? null : startProject.getFile(new File(startEntry.getStartFile().toString()));
        return csmFile;
    }

    public static ProjectBase getStartProject(final APTPreprocHandler.State state) {
        return getStartProject(APTHandlersSupport.extractStartEntry(state));
    }

    public static ProjectBase getStartProject(StartEntry startEntry) {
        if (startEntry == null) {
            return null;
        }
        Key key = startEntry.getStartFileProject();
        ProjectBase prj = (ProjectBase) RepositoryUtils.get(key);
        return prj;
    }

    public final APTFile getAPTLight(CsmFile csmFile) throws IOException {
        APTFile aptLight = null;
        aptLight = APTDriver.getInstance().findAPTLight(((FileImpl) csmFile).getBuffer());
        return aptLight;
    }

    public final GraphContainer getGraph() {
        return getGraphStorage();
    }

    protected final static class DefaultFileItem implements NativeFileItem {

        private NativeProject project;
        private String absolutePath;

        public DefaultFileItem(NativeProject project, String absolutePath) {
            this.project = project;
            this.absolutePath = absolutePath;
        }

        public DefaultFileItem(NativeFileItem nativeFile) {
            this.project = nativeFile.getNativeProject();
            this.absolutePath = nativeFile.getFile().getAbsolutePath();
        }

        public static NativeFileItem toDefault(NativeFileItem nativeFile) {
            // if not already fake
            if (!(nativeFile instanceof DefaultFileItem)) {
                nativeFile = new DefaultFileItem(nativeFile);
            }
            return nativeFile;
        }

        public List<String> getUserMacroDefinitions() {
            if (project != null) {
                return project.getUserMacroDefinitions();
            }
            return Collections.<String>emptyList();
        }

        public List<String> getUserIncludePaths() {
            if (project != null) {
                return project.getUserIncludePaths();
            }
            return Collections.<String>emptyList();
        }

        public List<String> getSystemMacroDefinitions() {
            if (project != null) {
                return project.getSystemMacroDefinitions();
            }
            return Collections.<String>emptyList();
        }

        public List<String> getSystemIncludePaths() {
            if (project != null) {
                return project.getSystemIncludePaths();
            }
            return Collections.<String>emptyList();
        }

        public NativeProject getNativeProject() {
            return project;
        }

        public File getFile() {
            return new File(absolutePath);
        }

        public Language getLanguage() {
            return NativeFileItem.Language.C_HEADER;
        }

        public LanguageFlavor getLanguageFlavor() {
            return NativeFileItem.LanguageFlavor.GENERIC;
        }

        public boolean isExcluded() {
            return false;
        }

        @Override
        public String toString() {
            return absolutePath;
        }
    }

    /**
     * Represent the project status.
     *
     * Concerns only initial stage of project lifecycle:
     * allows to distingwish just newly-created project,
     * the phase when files are being added to project (and to parser queue)
     * and the phase when all files are already added.
     *
     * It isn't worth tracking further stages (stable/unstable)
     * since it's error prone (it's better to ask, say, parser queue
     * whether it contains files that belong to this projec or not)
     */
    protected static enum Status {

        Initial,
        Restored,
        AddingFiles,
        Validating,
        Ready;
    }
    private volatile Status status;
    /** The task that is run in a request processor during project initialization */
    private Cancellable initializationTask;
    /** The lock under which the initializationTask is set */
    private final Object initializationTaskLock = new Object();
    private final Object waitParseLock = new Object();
// to profile monitor usages    
//    private static final class ClassifierReplaceLock {
//    }
    private final Object classifierReplaceLock = new Object(); // ClassifierReplaceLock();
    private ModelImpl model;
    private Unresolved unresolved;
    private CharSequence name;
    private final CsmUID<CsmNamespace> globalNamespaceUID;
    private final NamespaceImpl FAKE_GLOBAL_NAMESPACE;
    private Object platformProject;
    /**
     * Some notes concerning disposing and disposeLock fields.
     *
     * The purpose is not to perform some actions
     * (such as adding new files, continuing initialization, etc)
     * when the project is going to be disposed.
     *
     * The disposing field is changed only once,
     * from false to true (in setDispose() method)
     *
     * When it is changed to true, no lock is aquired, BUT:
     * it is guaranteed that events take place in the following order:
     * 1) disposing is set to true
     * 2) the disposeLock.writeLock() is locked after that
     * and remains locked during the entire project closure.
     *
     * Clients who need to check this, are obliged to
     * act in the following sequence:
     * 1) require disposeLock.readLock()
     * 2) check that the disposing field is still false
     * 3) keep disposeLock.readLock() locked
     * while performing critical actions
     * (the actions that should not be done
     * when the project is being disposed)
     *
     */
    private volatile boolean disposing;
    private ReadWriteLock disposeLock = new ReentrantReadWriteLock();
    private CharSequence uniqueName = null; // lazy initialized
    private Map<CharSequence, CsmUID<CsmNamespace>> namespaces = new ConcurrentHashMap<CharSequence, CsmUID<CsmNamespace>>();
    //private ClassifierContainer classifierContainer = new ClassifierContainer();
    private final Key classifierStorageKey;

    // collection of sharable system macros and system includes
    private APTSystemStorage sysAPTData = APTSystemStorage.getDefault();
    private final Object namespaceLock = new String("namespaceLock in Projectbase " + hashCode()); // NOI18N
    private final Key declarationsSorageKey;
    private final Key fileContainerKey;
    private final Object fileContainerLock = new Object();
    private final Key graphStorageKey;
    protected final SourceRootContainer projectRoots = new SourceRootContainer();
    private NativeProjectListenerImpl projectListener;

    //private NamespaceImpl fakeNamespace;

    // test variables.
    private static final boolean TRACE_PP_STATE_OUT = DebugUtils.getBoolean("cnd.dump.preproc.state", false); // NOI18N
    private static final boolean REMEMBER_RESTORED = TraceFlags.CLEAN_MACROS_AFTER_PARSE && (DebugUtils.getBoolean("cnd.remember.restored", false) || TRACE_PP_STATE_OUT);// NOI18N
    public static final int GATHERING_MACROS = 0;
    public static final int GATHERING_TOKENS = 1;

    ////////////////////////////////////////////////////////////////////////////
    /**
     * for tests only
     */
    public static List<String> testGetRestoredFiles() {
        return testRestoredFiles;
    }
    private static List<String> testRestoredFiles = null;
    ////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////////
    // impl of persistent
    public void write(DataOutput aStream) throws IOException {
        assert aStream != null;
        UIDObjectFactory aFactory = UIDObjectFactory.getDefaultFactory();
        assert aFactory != null;
        assert this.name != null;
        PersistentUtils.writeUTF(name, aStream);
        //PersistentUtils.writeUTF(RepositoryUtils.getUnitName(getUID()), aStream);
        aFactory.writeUID(this.globalNamespaceUID, aStream);
        aFactory.writeStringToUIDMap(this.namespaces, aStream, false);

        ProjectComponent.writeKey(fileContainerKey, aStream);
        ProjectComponent.writeKey(declarationsSorageKey, aStream);
        ProjectComponent.writeKey(graphStorageKey, aStream);
        ProjectComponent.writeKey(classifierStorageKey, aStream);

        PersistentUtils.writeUTF(this.uniqueName, aStream);
    }

    protected ProjectBase(DataInput aStream) throws IOException {

        setStatus(Status.Restored);

        assert aStream != null;
        UIDObjectFactory aFactory = UIDObjectFactory.getDefaultFactory();
        assert aFactory != null : "default UID factory can not be bull";

        this.name = PersistentUtils.readUTF(aStream, ProjectNameCache.getManager());
        assert this.name != null : "project name can not be null";

        //CharSequence unitName = PersistentUtils.readUTF(aStream, DefaultCache.getManager());

        this.globalNamespaceUID = aFactory.readUID(aStream);
        assert globalNamespaceUID != null : "globalNamespaceUID can not be null";

        aFactory.readStringToUIDMap(this.namespaces, aStream, QualifiedNameCache.getManager());

        fileContainerKey = ProjectComponent.readKey(aStream);
        assert fileContainerKey != null : "fileContainerKey can not be null";

        declarationsSorageKey = ProjectComponent.readKey(aStream);
        assert declarationsSorageKey != null : "declarationsSorageKey can not be null";

        graphStorageKey = ProjectComponent.readKey(aStream);
        assert graphStorageKey != null : "graphStorageKey can not be null";

        classifierStorageKey = ProjectComponent.readKey(aStream);
        assert classifierStorageKey != null : "classifierStorageKey can not be null";

        this.uniqueName = PersistentUtils.readUTF(aStream, DefaultCache.getManager());
        assert uniqueName != null : "uniqueName can not be null";
        this.uniqueName = ProjectNameCache.getManager().getString(this.uniqueName);

        this.model = (ModelImpl) CsmModelAccessor.getModel();

        this.FAKE_GLOBAL_NAMESPACE = new NamespaceImpl(this, true);
    }

    DeclarationContainer getDeclarationsSorage() {
        DeclarationContainer dc = (DeclarationContainer) RepositoryUtils.get(declarationsSorageKey);
        if (dc == null && isValid()) {
            DiagnosticExceptoins.register(new IllegalStateException("Failed to get DeclarationsSorage by key " + declarationsSorageKey)); // NOI18N
        }
        return dc != null ? dc : DeclarationContainer.empty();
    }

    FileContainer getFileContainer() {
        FileContainer fc = (FileContainer) RepositoryUtils.get(fileContainerKey);
        if (fc == null && isValid()) {
            DiagnosticExceptoins.register(new IllegalStateException("Failed to get FileContainer by key " + fileContainerKey)); // NOI18N
        }
        return fc != null ? fc : FileContainer.empty();
    }

    public final GraphContainer getGraphStorage() {
        GraphContainer gc = (GraphContainer) RepositoryUtils.get(graphStorageKey);
        if (gc == null && isValid()) {
            DiagnosticExceptoins.register(new IllegalStateException("Failed to get GraphContainer by key " + graphStorageKey)); // NOI18N
        }
        return gc != null ? gc : GraphContainer.empty();
    }

    final ClassifierContainer getClassifierSorage() {
        ClassifierContainer cc = (ClassifierContainer) RepositoryUtils.get(classifierStorageKey);
        if (cc == null && isValid()) {
            DiagnosticExceptoins.register(new IllegalStateException("Failed to get ClassifierSorage by key " + classifierStorageKey)); // NOI18N
        }
        return cc != null ? cc : ClassifierContainer.empty();
    }

    private class MyRunnable implements Runnable {
        private final CountDownLatch countDownLatch;
        private final CsmUID<CsmFile> file;
        private final boolean libsAlreadyParsed;

        private MyRunnable(CountDownLatch countDownLatch, CsmUID<CsmFile> file, boolean libsAlreadyParsed){
            this.countDownLatch = countDownLatch;
            this.file = file;
            this.libsAlreadyParsed = libsAlreadyParsed;
        }
        public void run() {
            try {
                if (file == null){
                    return;
                }
                FileImpl impl = (FileImpl) file.getObject();
                CndUtils.assertTrue(impl != null, "");
                if (impl == null) {
                    return;
                }
                Thread.currentThread().setName("Fix registration "+file); // NOI18N
                impl.onProjectParseFinished(libsAlreadyParsed);
            } finally {
                countDownLatch.countDown();
            }
        }
    }
}
