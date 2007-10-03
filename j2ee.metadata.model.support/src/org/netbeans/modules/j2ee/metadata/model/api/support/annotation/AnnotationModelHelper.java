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

package org.netbeans.modules.j2ee.metadata.model.api.support.annotation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.ClassIndex;
import org.netbeans.api.java.source.ClassIndexListener;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.RootsEvent;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.TypesEvent;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelException;
import org.openide.util.Exceptions;
import org.openide.util.WeakSet;

/**
 *
 * @author Andrei Badea
 */
public final class AnnotationModelHelper {

    // XXX exception wrapping in runJavaSourceTask()
    // XXX ExecutionException for the future returned by runJavaSourceTaskWhenScanFinished()

    private final ClasspathInfo cpi;
    // @GuardedBy("this")
    private final Set<JavaContextListener> javaContextListeners = new WeakSet<JavaContextListener>();
    // @GuardedBy("this")
    private final Set<PersistentObjectManager<? extends PersistentObject>> managers = new WeakSet<PersistentObjectManager<? extends PersistentObject>>();

    // @GuardedBy("this")
    private ClassIndex classIndex;
    // @GuardedBy("this")
    private ClassIndexListenerImpl listener;

    // not private because used in unit tests
    // @GuardedBy("this")
    JavaSource javaSource;
    // @GuardedBy("this")
    private Thread userActionTaskThread;

    private AnnotationScanner annotationScanner;
    private CompilationController controller;

    public static AnnotationModelHelper create(ClasspathInfo cpi) {
        return new AnnotationModelHelper(cpi);
    }

    private AnnotationModelHelper(ClasspathInfo cpi) {
        this.cpi = cpi;
    }

    public ClasspathInfo getClasspathInfo() {
        return cpi;
    }

    public <T extends PersistentObject> PersistentObjectManager<T> createPersistentObjectManager(ObjectProvider<T> provider) {
        synchronized (this) {
            PersistentObjectManager<T> manager = PersistentObjectManager.create(this, provider);
            registerPersistentObjectManager(manager);
            return manager;
        }
    }

    private void registerPersistentObjectManager(PersistentObjectManager<? extends PersistentObject> manager) {
        assert Thread.holdsLock(this);
        if (classIndex == null) {
            classIndex = cpi.getClassIndex();
            // this doesn't get removed anywhere, which should not matter, since
            // the classpath info, its class index and all managers have the same lifecycle
            listener = new ClassIndexListenerImpl();
            classIndex.addClassIndexListener(listener);
        }
        managers.add(manager);
    }

    public void addJavaContextListener(JavaContextListener listener) {
        synchronized (this) {
            javaContextListeners.add(listener);
        }
    }

    /**
     * Runs the given callable as a JavaSource user action task.
     * The context of the JavaSource task can be accessed by {@link #getCompilationController}.
     */
    public <V> V runJavaSourceTask(Callable<V> callable) throws IOException {
        return runJavaSourceTask(callable, true);
    }

    /**
     * Runs the given runnable as a JavaSource user action task.
     *
     * @see #runJavaSourceTask(Callable)
     */
    public void runJavaSourceTask(final Runnable run) throws IOException {
        runJavaSourceTask(new Callable<Void>() {
            public Void call() {
                run.run();
                return null;
            }
        });
    }

    /**
     * Runs the given callable as a JavaSource user action task. Not private because
     * used in unit tests.
     *
     * @param notify whether to notify <code>JavaContextListener</code>s.
     */
    <V> V runJavaSourceTask(final Callable<V> callable, final boolean notify) throws IOException {
        JavaSource existingJavaSource;
        synchronized (this) {
            existingJavaSource = javaSource;
        }
        JavaSource newJavaSource = existingJavaSource != null ? existingJavaSource : JavaSource.create(cpi);
        final List<V> result = new ArrayList<V>();
        try {
            newJavaSource.runUserActionTask(new CancellableTask<CompilationController>() {
                public void run(CompilationController controller) throws Exception {
                    result.add(runCallable(callable, controller, notify));
                }
                public void cancel() {
                    // we can't cancel
                }
            }, true);
        } catch (IOException e) {
            Throwable cause = e.getCause();
            if (cause instanceof MetadataModelException) {
                throw (MetadataModelException)cause;
            }
            throw e;
        }
        return result.get(0);
    }

    /**
     * Runs the given callable as a JavaSource user action task either immediately,
     * or, if the Java infrastructure is just performing a classpath scan,
     * when the scan has finished. This method is the equivalent of
     * {@link JavaSource#runWhenScanFinished}.
     */
    public <V> Future<V> runJavaSourceTaskWhenScanFinished(final Callable<V> callable) throws IOException {
        JavaSource existingJavaSource;
        synchronized (this) {
            existingJavaSource = javaSource;
        }
        JavaSource newJavaSource = existingJavaSource != null ? existingJavaSource : JavaSource.create(cpi);
        final DelegatingFuture<V> result = new DelegatingFuture<V>();
        try {
            result.setDelegate(newJavaSource.runWhenScanFinished(new CancellableTask<CompilationController>() {
                public void run(CompilationController controller) throws Exception {
                    result.setResult(runCallable(callable, controller, true));
                }
                public void cancel() {
                    // we can't cancel
                }
            }, true));
        } catch (IOException e) {
            Throwable cause = e.getCause();
            if (cause instanceof MetadataModelException) {
                throw (MetadataModelException)cause;
            }
            throw e;
        }
        assert result.delegate != null;
        return result;
    }

    /**
     * Runs the given callable in a javac context. Reentrant only in a single thread
     * (should be guaranteed by JavaSource.javacLock).
     */
    private <V> V runCallable(Callable<V> callable, CompilationController controller, boolean notify) throws IOException {
        synchronized (AnnotationModelHelper.this) {
            if (userActionTaskThread != null && userActionTaskThread != Thread.currentThread()) {
                throw new IllegalStateException("JavaSource.runUserActionTask() should not be executed by multiple threads concurrently"); // NOI18N
            }
            userActionTaskThread = Thread.currentThread();
            AnnotationModelHelper.this.javaSource = controller.getJavaSource();
        }
        AnnotationModelHelper.this.controller = controller;
        controller.toPhase(Phase.ELEMENTS_RESOLVED);
        try {
            return callable.call();
        } catch (Exception e) {
            if (e instanceof RuntimeException) {
                throw (RuntimeException)e;
            } else {
                throw new MetadataModelException(e);
            }
        } finally {
            AnnotationModelHelper.this.controller = null;
            annotationScanner = null;
            synchronized (AnnotationModelHelper.this) {
                javaSource = null;
                userActionTaskThread = null;
            }
            if (notify) {
                // have to notify while still under the javac lock
                // to ensure the visibility of any changes made by the listeners
                for (JavaContextListener hook : javaContextListeners) {
                    hook.javaContextLeft();
                }
            }
        }
    }

    /**
     * Returns the {@link CompilationController} of a running JavaSource
     * user action task. This method can only be called when such an user action
     * task in running.
     *
     * @see #runJavaSourceTask(Callable)
     */
    public CompilationController getCompilationController() {
        assertUserActionTaskThread();
        assert controller != null;
        return controller;
    }

    public AnnotationScanner getAnnotationScanner() {
        assertUserActionTaskThread();
        if (annotationScanner == null) {
            annotationScanner = new AnnotationScanner(this);
        }
        return annotationScanner;
    }

    /**
     * Returns true if the Java infrastructure is just performing a classpath
     * scan.
     */
    public boolean isJavaScanInProgress() {
        return SourceUtils.isScanInProgress();
    }

    private void assertUserActionTaskThread() {
        synchronized (this) {
            if (userActionTaskThread != Thread.currentThread()) {
                throw new IllegalStateException("The current thread is not running userActionTask()"); // NOI18N
            }
        }
    }

    /**
     * @param typeName must be the name of a type element
     * (resolvable by {@link javax.lang.model.util.Elements#getTypeElement}).
     */
    public TypeMirror resolveType(String typeName) {
        assertUserActionTaskThread();
        TypeElement type = getCompilationController().getElements().getTypeElement(typeName);
        if (type != null) {
            return type.asType();
        }
        return null;
    }

    public boolean isSameRawType(TypeMirror type1, String type2ElementName) {
        assertUserActionTaskThread();
        TypeElement type2Element = getCompilationController().getElements().getTypeElement(type2ElementName);
        if (type2Element != null) {
            Types types = getCompilationController().getTypes();
            TypeMirror type2 = types.erasure(type2Element.asType());
            return types.isSameType(types.erasure(type1), type2);
        }
        return false;
    }

    public List<? extends TypeElement> getSuperclasses(TypeElement type) {
        assertUserActionTaskThread();
        List<TypeElement> result = new ArrayList<TypeElement>();
        TypeElement currentType = type;
        for (;;) {
            currentType = getSuperclass(currentType);
            if (currentType != null) {
                result.add(currentType);
            } else {
                break;
            }
        }
        return Collections.unmodifiableList(result);
    }

    public TypeElement getSuperclass(TypeElement type) {
        assertUserActionTaskThread();
        TypeMirror supertype = type.getSuperclass();
        if (TypeKind.DECLARED.equals(supertype.getKind())) {
            Element element = ((DeclaredType)supertype).asElement();
            if (ElementKind.CLASS.equals(element.getKind())) {
                TypeElement superclass = (TypeElement)element;
                if (!superclass.getQualifiedName().contentEquals("java.lang.Object")) { // NOI18N
                    return superclass;
                }
            }
        }
        return null;
    }

    public boolean hasAnnotation(List<? extends AnnotationMirror> annotations, String annotationTypeName) {
        assertUserActionTaskThread();
        for (AnnotationMirror annotation : annotations) {
            String typeName = getAnnotationTypeName(annotation.getAnnotationType());
            if (annotationTypeName.equals(typeName)) {
                return true;
            }
        }
        return false;
    }

    public boolean hasAnyAnnotation(List<? extends AnnotationMirror> annotations, Set<String> annotationTypeNames) {
        assertUserActionTaskThread();
        for (AnnotationMirror annotation : annotations) {
            String annotationTypeName = getAnnotationTypeName(annotation.getAnnotationType());
            if (annotationTypeName != null && annotationTypeNames.contains(annotationTypeName)) {
                return true;
            }
        }
        return false;
    }

    public Map<String, ? extends AnnotationMirror> getAnnotationsByType(List<? extends AnnotationMirror> annotations) {
        assertUserActionTaskThread();
        Map<String, AnnotationMirror> result = new HashMap<String, AnnotationMirror>();
        for (AnnotationMirror annotation : annotations) {
            String typeName = getAnnotationTypeName(annotation.getAnnotationType());
            if (typeName != null) {
                result.put(typeName, annotation);
            }
        }
        return Collections.unmodifiableMap(result);
    }

    /**
     * @return the annotation type name or null if <code>typeMirror</code>
     *         was not an annotation type.
     */
    public String getAnnotationTypeName(DeclaredType typeMirror) {
        assertUserActionTaskThread();
        if (!TypeKind.DECLARED.equals(typeMirror.getKind())) {
            return null;
        }
        Element element = typeMirror.asElement();
        if (!ElementKind.ANNOTATION_TYPE.equals(element.getKind())) {
            return null;
        }
        return ((TypeElement)element).getQualifiedName().toString();
    }

    private final class ClassIndexListenerImpl implements ClassIndexListener {

        public void typesAdded(final TypesEvent event) {
            try {
                runInJavacContext(new Callable<Void>() {
                    public Void call() {
                        for (PersistentObjectManager<? extends PersistentObject> manager : managers) {
                            manager.typesAdded(event.getTypes());
                        }
                        return null;
                    }
                });
            } catch (IOException e) {
                Exceptions.printStackTrace(e);
            }
        }

        public void typesRemoved(final TypesEvent event) {
            try {
                runInJavacContext(new Callable<Void>() {
                    public Void call() {
                        for (PersistentObjectManager<? extends PersistentObject> manager : managers) {
                            manager.typesRemoved(event.getTypes());
                        }
                        return null;
                    }
                });
            } catch (IOException e) {
                Exceptions.printStackTrace(e);
            }
        }

        public void typesChanged(final TypesEvent event) {
            try {
                runInJavacContext(new Callable<Void>() {
                    public Void call() {
                        for (PersistentObjectManager<? extends PersistentObject> manager : managers) {
                            manager.typesChanged(event.getTypes());
                        }
                        return null;
                    }
                });
            } catch (IOException e) {
                Exceptions.printStackTrace(e);
            }
        }

        public void rootsAdded(RootsEvent event) {
            rootsChanged();
        }

        public void rootsRemoved(RootsEvent event) {
            rootsChanged();
        }

        private void rootsChanged() {
            try {
                runInJavacContext(new Callable<Void>() {
                    public Void call() {
                        for (PersistentObjectManager<? extends PersistentObject> manager : managers) {
                            manager.rootsChanged();
                        }
                        return null;
                    }
                });
            } catch (IOException e) {
                Exceptions.printStackTrace(e);
            }
        }

        private <V> void runInJavacContext(final Callable<V> call) throws IOException {
            synchronized (AnnotationModelHelper.this) {
                if (userActionTaskThread == Thread.currentThread()) {
                    throw new IllegalStateException("Retouche is sending ClassIndex events from within JavaSource.runUserActionTask()"); // NOI18N
                }
            }
            runJavaSourceTask(call, false);
        }
    }

    private static final class DelegatingFuture<V> implements Future<V> {

        private volatile Future<Void> delegate;
        private volatile V result;
        private volatile ExecutionException executionException;

        public void setDelegate(Future<Void> delegate) {
            assert this.delegate == null;
            this.delegate = delegate;
        }

        public void setResult(V result) {
            this.result = result;
        }

        public boolean cancel(boolean mayInterruptIfRunning) {
            return delegate.cancel(mayInterruptIfRunning);
        }

        public boolean isCancelled() {
            return delegate.isCancelled();
        }

        public boolean isDone() {
            return delegate.isDone();
        }

        public V get() throws InterruptedException, ExecutionException {
            delegate.get();
            return result;
        }

        public V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            delegate.get(timeout, unit);
            return result;
        }
    }
}
