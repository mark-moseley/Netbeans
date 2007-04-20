/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
import org.netbeans.api.java.source.TypesEvent;
import org.openide.util.Exceptions;
import org.openide.util.NotImplementedException;
import org.openide.util.WeakSet;

/**
 *
 * @author Andrei Badea
 */
public final class AnnotationModelHelper {

    // XXX threading model has unclear semantics. This class should be fully
    // thread-safe, and all methods bound to the java context should be move
    // to another class (e.g., JavaContext) whose lifecycle should be bound
    // to one invocation of the userActionTask() method

    // XXX userActionTask() should be runInJavaContext()

    private final ClasspathInfo cpi;
    private final Set<JavaContextListener> javaContextListeners = new WeakSet<JavaContextListener>();
    private final Set<PersistentObjectManager<? extends PersistentObject>> managers = new WeakSet<PersistentObjectManager<? extends PersistentObject>>();

    private ClassIndex classIndex;
    private ClassIndexListenerImpl listener;

    private JavaSource javaSource;
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

    public AnnotationScanner getAnnotationScanner() {
        assertUserActionTask();
        if (annotationScanner == null) {
            annotationScanner = new AnnotationScanner(this);
        }
        return annotationScanner;
    }

    public JavaSource getJavaSource() {
        assertUserActionTask();
        return javaSource;
    }

    public CompilationController getCompilationController() {
        assertUserActionTask();
        assert controller != null;
        return controller;
    }

    public <V> V userActionTask(final Callable<V> callable) throws IOException {
        final JavaSource newJavaSource = JavaSource.create(cpi);
        final List<V> result = new ArrayList<V>();
        try {
            newJavaSource.runUserActionTask(new CancellableTask<CompilationController>() {
                public void run(CompilationController controller) throws Exception {
                    controller.toPhase(Phase.ELEMENTS_RESOLVED);
                    AnnotationModelHelper.this.javaSource = newJavaSource;
                    AnnotationModelHelper.this.controller = controller;
                    try {
                        result.add(callable.call());
                    } catch (Throwable t) {
                        if (t instanceof IOException) {
                            throw (IOException)t;
                        } else {
                            IOException wrapper = new IOException(t.getMessage());
                            wrapper.initCause(t);
                            throw wrapper;
                        }
                    } finally {
                        AnnotationModelHelper.this.controller = null;
                        annotationScanner = null;
                    }
                }
                public void cancel() {
                    // we can't cancel
                }
            }, true);
        } finally {
            for (JavaContextListener hook : javaContextListeners) {
                hook.javaContextLeft();
            }
        }
        return result.get(0);
    }

    public void userActionTask(final Runnable run) throws IOException {
        userActionTask(new Callable<Void>() {
            public Void call() {
                run.run();
                return null;
            }
        });
    }

    private void assertUserActionTask() {
        if (javaSource == null) {
            throw new IllegalStateException("Not in an user action task"); // NOI18N
        }
    }

    public <T extends PersistentObject> PersistentObjectManager<T> createPersistentObjectManager(ObjectProvider<T> provider) {
        PersistentObjectManager<T> manager = new PersistentObjectManager<T>(this, provider);
        registerPersistentObjectManager(manager);
        return manager;
    }

    private void registerPersistentObjectManager(PersistentObjectManager<? extends PersistentObject> manager) {
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
        javaContextListeners.add(listener);
    }

    /**
     * @param typeName must be the name of a type element
     * (resolvable by {@link javax.lang.model.util.Elements#getTypeElement}).
     */
    public TypeMirror resolveType(String typeName) {
        assertUserActionTask();
        TypeElement type = getCompilationController().getElements().getTypeElement(typeName);
        if (type != null) {
            return type.asType();
        }
        return null;
    }

    public boolean isSameRawType(TypeMirror type1, String type2ElementName) {
        assertUserActionTask();
        TypeElement type2Element = getCompilationController().getElements().getTypeElement(type2ElementName);
        if (type2Element != null) {
            Types types = getCompilationController().getTypes();
            TypeMirror type2 = types.erasure(type2Element.asType());
            return types.isSameType(types.erasure(type1), type2);
        }
        return false;
    }

    public List<? extends TypeElement> getSuperclasses(TypeElement type) {
        assertUserActionTask();
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
        assertUserActionTask();
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
        assertUserActionTask();
        for (AnnotationMirror annotation : annotations) {
            String typeName = getAnnotationTypeName(annotation.getAnnotationType());
            if (annotationTypeName.equals(typeName)) {
                return true;
            }
        }
        return false;
    }

    public boolean hasAnyAnnotation(List<? extends AnnotationMirror> annotations, Set<String> annotationTypeNames) {
        assertUserActionTask();
        for (AnnotationMirror annotation : annotations) {
            String annotationTypeName = getAnnotationTypeName(annotation.getAnnotationType());
            if (annotationTypeName != null && annotationTypeNames.contains(annotationTypeName)) {
                return true;
            }
        }
        return false;
    }

    public Map<String, ? extends AnnotationMirror> getAnnotationsByType(List<? extends AnnotationMirror> annotations) {
        assertUserActionTask();
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
        assertUserActionTask();
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
                userActionTask(new Runnable() {
                    public void run() {
                        for (PersistentObjectManager<? extends PersistentObject> manager : managers) {
                            manager.typesAdded(event.getTypes());
                        }
                    }
                });
            } catch (IOException e) {
                Exceptions.printStackTrace(e);
            }
        }

        public void typesRemoved(final TypesEvent event) {
            try {
                userActionTask(new Runnable() {
                    public void run() {
                        for (PersistentObjectManager<? extends PersistentObject> manager : managers) {
                            manager.typesRemoved(event.getTypes());
                        }
                    }
                });
            } catch (IOException e) {
                Exceptions.printStackTrace(e);
            }
        }

        public void typesChanged(final TypesEvent event) {
            try {
                userActionTask(new Runnable() {
                    public void run() {
                        for (PersistentObjectManager<? extends PersistentObject> manager : managers) {
                            manager.typesChanged(event.getTypes());
                        }
                    }
                });
            } catch (IOException e) {
                Exceptions.printStackTrace(e);
            }
        }

        public void rootsAdded(RootsEvent event) {
            try {
                userActionTask(new Runnable() {
                    public void run() {
                        for (PersistentObjectManager<? extends PersistentObject> manager : managers) {
                            manager.rootsChanged();
                        }
                    }
                });
            } catch (IOException e) {
                Exceptions.printStackTrace(e);
            }
        }

        public void rootsRemoved(RootsEvent event) {
            try {
                userActionTask(new Runnable() {
                    public void run() {
                        for (PersistentObjectManager<? extends PersistentObject> manager : managers) {
                            manager.rootsChanged();
                        }
                    }
                });
            } catch (IOException e) {
                Exceptions.printStackTrace(e);
            }
        }
    }
}
