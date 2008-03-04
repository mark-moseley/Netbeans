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
package org.netbeans.api.java.source;

import com.sun.javadoc.Doc;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.Scope;
import com.sun.source.util.JavacTask;
import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;
import com.sun.tools.javac.api.JavacScope;
import com.sun.tools.javac.api.JavacTaskImpl;
import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.Source;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symbol.ClassSymbol;
import com.sun.tools.javac.code.Symbol.MethodSymbol;
import com.sun.tools.javac.code.Symbol.PackageSymbol;
import com.sun.tools.javac.code.Symbol.TypeSymbol;
import com.sun.tools.javac.code.Symbol.VarSymbol;
import com.sun.tools.javac.code.Symtab;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.code.Type.ClassType;
import com.sun.tools.javac.code.Type.MethodType;
import com.sun.tools.javac.comp.AttrContext;
import com.sun.tools.javac.comp.Enter;
import com.sun.tools.javac.comp.Env;
import com.sun.tools.javac.comp.Resolve;
import com.sun.tools.javac.model.JavacElements;
import com.sun.tools.javac.model.JavacTypes;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.Name;
import com.sun.tools.javadoc.DocEnv;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import org.netbeans.modules.java.source.builder.ElementsService;
import org.netbeans.modules.java.source.JavadocEnv;

/**
 *
 * @author Jan Lahoda, Dusan Balek, Tomas Zezula
 */
public final class ElementUtilities {
    
    private final Context ctx;
    private final ElementsService delegate;
    private final CompilationInfo info;
    
    /** Creates a new instance of ElementUtilities */
    ElementUtilities(final CompilationInfo info) {
        assert info != null;
        JavacTask task = info.impl.getJavacTask();
        this.info = info;
        this.ctx = ((JavacTaskImpl)task).getContext();
        this.delegate = ElementsService.instance(ctx);
    }
    
    /**
     * Returns the type element within which this member or constructor
     * is declared. Does not accept pakages
     * If this is the declaration of a top-level type (a non-nested class
     * or interface), returns null.
     *
     * @return the type declaration within which this member or constructor
     * is declared, or null if there is none
     * @throws IllegalArgumentException if the provided element is a package element
     */
    public TypeElement enclosingTypeElement( Element element ) throws IllegalArgumentException {
	
	if( element.getKind() == ElementKind.PACKAGE ) {
	    throw new IllegalArgumentException();
	}
	
        if (element.getEnclosingElement().getKind() == ElementKind.PACKAGE) {
            //element is a top level class, returning null according to the contract:
            return null;
        }
        
	while( !(element.getEnclosingElement().getKind().isClass() || 
	       element.getEnclosingElement().getKind().isInterface()) ) {
	    element = element.getEnclosingElement();
	}
	
	return (TypeElement)element.getEnclosingElement(); // Wrong
    }
    
    /**
     * 
     * The outermost TypeElement which indirectly encloses this element.
     */
    public TypeElement outermostTypeElement(Element element) {
        return delegate.outermostTypeElement(element);
    }
    
    /**
     * Returns the implementation of a method in class origin; null if none exists.
     */
    public Element getImplementationOf(ExecutableElement method, TypeElement origin) {
        return delegate.getImplementationOf(method, origin);
    }
    
    /**Returns true if the given element is syntetic.
     * 
     *  @param element to check
     *  @return true if and only if the given element is syntetic, false otherwise
     */
    public boolean isSynthetic(Element element) {
        return (((Symbol) element).flags() & Flags.SYNTHETIC) != 0 || (((Symbol) element).flags() & Flags.GENERATEDCONSTR) != 0;
    }
    
    /**
     * Returns true if this element represents a method which overrides a
     * method in one of its superclasses.
     */
    public boolean overridesMethod(ExecutableElement element) {
        return delegate.overridesMethod(element);
    }
    
    /**
     * Returns a binary name of a type.
     * @param element for which the binary name should be returned
     * @return the binary name, see Java Language Specification 13.1
     * @throws IllegalArgumentException when the element is not a javac element
     */
    public static String getBinaryName (TypeElement element) throws IllegalArgumentException {
        if (element instanceof Symbol.TypeSymbol) {
            return ((Symbol.TypeSymbol)element).flatName().toString();
        }
        else {
            throw new IllegalArgumentException ();
        } 
    }
    
    /**Get javadoc for given element.
     */
    public Doc javaDocFor(Element element) {
        if (element != null) {
            DocEnv env = DocEnv.instance(ctx);
            switch (element.getKind()) {
                case ANNOTATION_TYPE:
                case CLASS:
                case ENUM:
                case INTERFACE:
                    return env.getClassDoc((ClassSymbol)element);
                case ENUM_CONSTANT:
                case FIELD:
                    return env.getFieldDoc((VarSymbol)element);
                case METHOD:
                    if (element.getEnclosingElement().getKind() == ElementKind.ANNOTATION_TYPE)
                        return env.getAnnotationTypeElementDoc((MethodSymbol)element);
                    return env.getMethodDoc((MethodSymbol)element);
                case CONSTRUCTOR:
                    return env.getConstructorDoc((MethodSymbol)element);
                case PACKAGE:
                    return env.getPackageDoc((PackageSymbol)element);
            }
        }
        return null;
    }
    
    /**Find a {@link Element} corresponding to a given {@link Doc}.
     */
    public Element elementFor(Doc doc) {
        return (doc instanceof JavadocEnv.ElementHolder) ? ((JavadocEnv.ElementHolder)doc).getElement() : null;
    }
    
    /**
     * Returns all members of a type, whether inherited or
     * declared directly.  For a class the result also includes its
     * constructors, but not local or anonymous classes.
     * 
     * @param type  the type being examined
     * @param acceptor to filter the members
     * @return all members in the type
     * @see Elements#getAllMembers
     */
    public Iterable<? extends Element> getMembers(TypeMirror type, ElementAcceptor acceptor) {
        ArrayList<Element> members = new ArrayList<Element>();
        if (type != null) {
            Elements elements = JavacElements.instance(ctx);
            switch (type.getKind()) {
                case DECLARED:
                    HashMap<CharSequence, ArrayList<Element>> hiders = new HashMap<CharSequence, ArrayList<Element>>();
                    Types types = JavacTypes.instance(ctx);
                    for (Element member : elements.getAllMembers((TypeElement)((DeclaredType)type).asElement())) {
                        if (acceptor == null || acceptor.accept(member, type)) {
                            CharSequence name = member.getSimpleName();
                            ArrayList<Element> h = hiders.get(name);
                            if (!isHidden(member, h, types)) {
                                members.add(member);
                                if (h == null) {
                                    h = new ArrayList<Element>();
                                    hiders.put(name, h);
                                }
                                h.add(member);
                            }
                        }
                    }
                case BOOLEAN:
                case BYTE:
                case CHAR:
                case DOUBLE:
                case FLOAT:
                case INT:
                case LONG:
                case SHORT:
                case VOID:
                    Type t = Symtab.instance(ctx).classType;
                    com.sun.tools.javac.util.List<Type> typeargs = Source.instance(ctx).allowGenerics() ?
                        com.sun.tools.javac.util.List.of((Type)type) :
                        com.sun.tools.javac.util.List.<Type>nil();
                    t = new ClassType(t.getEnclosingType(), typeargs, t.tsym);
                    Element classPseudoMember = new VarSymbol(Flags.STATIC | Flags.PUBLIC | Flags.FINAL, Name.Table.instance(ctx)._class, t, ((Type)type).tsym);
                    if (acceptor == null || acceptor.accept(classPseudoMember, type))
                        members.add(classPseudoMember);
                    break;
                case ARRAY:
                    for (Element member : elements.getAllMembers((TypeElement)((Type)type).tsym)) {
                        if (acceptor == null || acceptor.accept(member, type))
                            members.add(member);
                    }
                    break;
            }
        }
        return members;
    }
    
    /**Return members declared in the given scope.
     */
    public Iterable<? extends Element> getLocalMembersAndVars(Scope scope, ElementAcceptor acceptor) {
        ArrayList<Element> members = new ArrayList<Element>();
        HashMap<CharSequence, ArrayList<Element>> hiders = new HashMap<CharSequence, ArrayList<Element>>();
        Elements elements = JavacElements.instance(ctx);
        Types types = JavacTypes.instance(ctx);
        TypeElement cls;
        while(scope != null) {
            if ((cls = scope.getEnclosingClass()) != null) {
                for (Element local : scope.getLocalElements())
                    if (acceptor == null || acceptor.accept(local, null)) {
                        CharSequence name = local.getSimpleName();
                        ArrayList<Element> h = hiders.get(name);
                        if (!isHidden(local, h, types)) {
                            members.add(local);
                            if (h == null) {
                                h = new ArrayList<Element>();
                                hiders.put(name, h);
                            }
                            h.add(local);
                        }
                    }
                TypeMirror type = cls.asType();
                for (Element member : elements.getAllMembers(cls)) {
                    if (acceptor == null || acceptor.accept(member, type)) {
                        CharSequence name = member.getSimpleName();
                        ArrayList<Element> h = hiders.get(name);
                        if (!isHidden(member, h, types)) {
                            members.add(member);
                            if (h == null) {
                                h = new ArrayList<Element>();
                                hiders.put(name, h);
                            }
                            h.add(member);
                        }
                    }
                }
            } else {
                for (Element local : scope.getLocalElements()) {
                    if (!local.getKind().isClass() && !local.getKind().isInterface() &&
                        (acceptor == null || acceptor.accept(local, local.getEnclosingElement().asType()))) {
                        CharSequence name = local.getSimpleName();
                        ArrayList<Element> h = hiders.get(name);
                        if (!isHidden(local, h, types)) {
                            members.add(local);
                            if (h == null) {
                                h = new ArrayList<Element>();
                                hiders.put(name, h);
                            }
                            h.add(local);
                        }
                    }
                }
            }
            scope = scope.getEnclosingScope();
        }
        return members;
    }

    /**Return variables declared in the given scope.
     */
    public Iterable<? extends Element> getLocalVars(Scope scope, ElementAcceptor acceptor) {
        ArrayList<Element> members = new ArrayList<Element>();
        HashMap<CharSequence, ArrayList<Element>> hiders = new HashMap<CharSequence, ArrayList<Element>>();
        Types types = JavacTypes.instance(ctx);
        while(scope != null && scope.getEnclosingClass() != null) {
            for (Element local : scope.getLocalElements())
                if (acceptor == null || acceptor.accept(local, null)) {
                    CharSequence name = local.getSimpleName();
                    ArrayList<Element> h = hiders.get(name);
                    if (!isHidden(local, h, types)) {
                        members.add(local);
                        if (h == null) {
                            h = new ArrayList<Element>();
                            hiders.put(name, h);
                        }
                        h.add(local);
                    }
                }
            scope = scope.getEnclosingScope();
        }
        return members;
    }
    
    /**Return {@link TypeElement}s:
     * <ul>
     *    <li>which are imported</li>
     *    <li>which are in the same package as the current file</li>
     *    <li>which are in the java.lang package</li>
     * </ul>
     */
    public Iterable<? extends TypeElement> getGlobalTypes(ElementAcceptor acceptor) {
        HashSet<TypeElement> members = new HashSet<TypeElement>();
        HashMap<CharSequence, ArrayList<Element>> hiders = new HashMap<CharSequence, ArrayList<Element>>();
        Trees trees = JavacTrees.instance(ctx);
        Types types = JavacTypes.instance(ctx);
        for (CompilationUnitTree unit : Collections.singletonList(info.getCompilationUnit())) {
            TreePath path = new TreePath(unit);
            Scope scope = trees.getScope(path);
            while (scope != null && scope instanceof JavacScope && !((JavacScope)scope).isStarImportScope()) {
                for (Element local : scope.getLocalElements())
                    if (local.getKind().isClass() || local.getKind().isInterface()) {
                        CharSequence name = local.getSimpleName();
                        ArrayList<Element> h = hiders.get(name);
                        if (!isHidden(local, h, types)) {
                            if (acceptor == null || acceptor.accept(local, null))
                                members.add((TypeElement)local);
                            if (h == null) {
                                h = new ArrayList<Element>();
                                hiders.put(name, h);
                            }
                            h.add(local);
                        }
                    }
                scope = scope.getEnclosingScope();
            }
            Element element = trees.getElement(path);
            if (element != null && element.getKind() == ElementKind.PACKAGE) {
                for (Element member : element.getEnclosedElements()) {
                    CharSequence name = member.getSimpleName();
                    ArrayList<Element> h = hiders.get(name);
                    if (!isHidden(member, h, types)) {
                        if (acceptor == null || acceptor.accept(member, null))
                            members.add((TypeElement) member);
                        if (h == null) {
                            h = new ArrayList<Element>();
                            hiders.put(name, h);
                        }
                        h.add(member);
                    }
                }
            }
            while (scope != null) {
                for (Element local : scope.getLocalElements())
                    if (local.getKind().isClass() || local.getKind().isInterface()) {
                        CharSequence name = local.getSimpleName();
                        ArrayList<Element> h = hiders.get(name);
                        if (!isHidden(local, h, types)) {
                            if (acceptor == null || acceptor.accept(local, null))
                                members.add((TypeElement)local);
                            if (h == null) {
                                h = new ArrayList<Element>();
                                hiders.put(name, h);
                            }
                            h.add(local);
                        }
                    }
                scope = scope.getEnclosingScope();
            }
        }
        return members;
    }

    /**Filter {@link Element}s
     */
    public static interface ElementAcceptor {
        /**Is the given element accepted.
         * 
         * @param e element to test
         * @param type the type for which to check if the member is accepted
         * @return true if and only if given element should be accepted
         */
        boolean accept(Element e, TypeMirror type);
    }

    private boolean isHidden(Element member, Iterable<Element> hiders, Types types) {
        if (hiders != null) {
            for (Element hider : hiders) {
                if (hider == member || (hider.getClass() == member.getClass() && //TODO: getClass() should not be used here
                    hider.getSimpleName() == member.getSimpleName() &&
                    ((hider.getKind() != ElementKind.METHOD && hider.getKind() != ElementKind.CONSTRUCTOR)
                    || types.isSubsignature((ExecutableType)hider.asType(), (ExecutableType)member.asType()))))
		    return true;
            }
        }
        return false;
    }
    
    /**
     * Returns true if the element is declared (directly or indirectly) local
     * to a method or variable initializer.  Also true for fields of inner 
     * classes which are in turn local to a method or variable initializer.
     */
    public boolean isLocal(Element element) {
        return delegate.isLocal(element);
    }
    
    /**
     * Returns true if a method specified by name and type is defined in a
     * class type.
     */
    public boolean alreadyDefinedIn(CharSequence name, ExecutableType method, TypeElement enclClass) {
        return delegate.alreadyDefinedIn(name, method, enclClass);
    }
    
    /**
     * Returns true if a type element has the specified element as a member.
     */
    public boolean isMemberOf(Element e, TypeElement type) {
        return delegate.isMemberOf(e, type);
    }                
    
    /**
     * Returns the parent method which the specified method overrides, or null
     * if the method does not override a parent class method.
     */
    public ExecutableElement getOverriddenMethod(ExecutableElement method) {
        return delegate.getOverriddenMethod(method);
    }        
    /**
     * Returns true if this element represents a method which 
     * implements a method in an interface the parent class implements.
     */
    public boolean implementsMethod(ExecutableElement element) {
        return delegate.implementsMethod(element);
    }
    
    /**Find all methods in given type and its supertypes, which are not implemented.
     * 
     * @param type to inspect
     * @return list of all unimplemented methods
     * 
     * @since 0.20
     */
    public List<? extends ExecutableElement> findUnimplementedMethods(TypeElement impl) {
        return findUnimplementedMethods(impl, impl);
    }

    // private implementation --------------------------------------------------


    private List<? extends ExecutableElement> findUnimplementedMethods(TypeElement impl, TypeElement element) {
        List<ExecutableElement> undef = new ArrayList<ExecutableElement>();
        if (element.getModifiers().contains(Modifier.ABSTRACT)) {
            for (Element e : element.getEnclosedElements()) {
                if (e.getKind() == ElementKind.METHOD && e.getModifiers().contains(Modifier.ABSTRACT)) {
                    ExecutableElement ee = (ExecutableElement)e;
                    Element eeImpl = getImplementationOf(ee, impl);
                    if (eeImpl == null || (eeImpl == ee && impl != element))
                        undef.add(ee);
                }
            }
        }
        Types types = JavacTypes.instance(ctx);
        DeclaredType implType = (DeclaredType)impl.asType();
        for (TypeMirror t : types.directSupertypes(element.asType())) {
            for (ExecutableElement ee : findUnimplementedMethods(impl, (TypeElement) ((DeclaredType) t).asElement())) {
                //check if "the same" method has already been added:
                boolean exists = false;
                ExecutableType eeType = (ExecutableType)types.asMemberOf(implType, ee);
                for (ExecutableElement existing : undef) {
                    if (existing.getSimpleName().contentEquals(ee.getSimpleName())) {
                        ExecutableType existingType = (ExecutableType)types.asMemberOf(implType, existing);
                        if (types.isSubsignature(existingType, eeType)) {
                            TypeMirror existingReturnType = existingType.getReturnType();
                            TypeMirror eeReturnType = eeType.getReturnType();
                            if (!types.isSubtype(existingReturnType, eeReturnType)) {
                                if (types.isSubtype(eeReturnType, existingReturnType)) {
                                    undef.remove(existing);
                                    undef.add(ee);
                                } else if (existingReturnType.getKind() == TypeKind.DECLARED && eeReturnType.getKind() == TypeKind.DECLARED) {
                                    Env<AttrContext> env = Enter.instance(ctx).getClassEnv((TypeSymbol)impl);
                                    DeclaredType subType = findCommonSubtype((DeclaredType)existingReturnType, (DeclaredType)eeReturnType, env);
                                    if (subType != null) {
                                        undef.remove(existing);
                                        MethodSymbol ms = ((MethodSymbol)existing).clone((Symbol)impl);
                                        MethodType mt = (MethodType)ms.type.clone();
                                        mt.restype = (Type)subType;
                                        ms.type = mt;
                                        undef.add(ms);
                                    }
                                }
                            }
                            exists = true;
                            break;
                        }
                    }
                }
                if (!exists) {
                    undef.add(ee);
                }
            }
        }
        return undef;
    }
    
    private DeclaredType findCommonSubtype(DeclaredType type1, DeclaredType type2, Env<AttrContext> env) {
        List<DeclaredType> subtypes1 = getSubtypes(type1, env);
        List<DeclaredType> subtypes2 = getSubtypes(type2, env);
        Types types = info.getTypes();
        for (DeclaredType subtype1 : subtypes1) {
            for (DeclaredType subtype2 : subtypes2) {
                if (types.isSubtype(subtype1, subtype2))
                    return subtype1;
                if (types.isSubtype(subtype2, subtype1))
                    return subtype2;
            }
        }
        return null;
    }
    
    private List<DeclaredType> getSubtypes(DeclaredType baseType, Env<AttrContext> env) {
        LinkedList<DeclaredType> subtypes = new LinkedList<DeclaredType>();
        HashSet<TypeElement> elems = new HashSet<TypeElement>();
        LinkedList<DeclaredType> bases = new LinkedList<DeclaredType>();
        bases.add(baseType);
        ClassIndex index = info.getClasspathInfo().getClassIndex();
        Trees trees = info.getTrees();
        Types types = info.getTypes();
        Resolve resolve = Resolve.instance(ctx);
        while(!bases.isEmpty()) {
            DeclaredType head = bases.remove();
            TypeElement elem = (TypeElement)head.asElement();
            if (!elems.add(elem))
                continue;
            subtypes.add(head);
            List<? extends TypeMirror> tas = head.getTypeArguments();
            boolean isRaw = !tas.iterator().hasNext();
            subtypes:
            for (ElementHandle<TypeElement> eh : index.getElements(ElementHandle.create(elem), EnumSet.of(ClassIndex.SearchKind.IMPLEMENTORS), EnumSet.allOf(ClassIndex.SearchScope.class))) {
                TypeElement e = eh.resolve(info);
                if (e != null) {
                    if (resolve.isAccessible(env, (TypeSymbol)e)) {
                        if (isRaw) {
                            DeclaredType dt = types.getDeclaredType(e);
                            bases.add(dt);
                        } else {
                            HashMap<Element, TypeMirror> map = new HashMap<Element, TypeMirror>();
                            TypeMirror sup = e.getSuperclass();
                            if (sup.getKind() == TypeKind.DECLARED && ((DeclaredType)sup).asElement() == elem) {
                                DeclaredType dt = (DeclaredType)sup;
                                Iterator<? extends TypeMirror> ittas = tas.iterator();
                                Iterator<? extends TypeMirror> it = dt.getTypeArguments().iterator();
                                while(it.hasNext() && ittas.hasNext()) {
                                    TypeMirror basetm = ittas.next();
                                    TypeMirror stm = it.next();
                                    if (basetm != stm) {
                                        if (stm.getKind() == TypeKind.TYPEVAR) {
                                            map.put(((TypeVariable)stm).asElement(), basetm);
                                        } else {
                                            continue subtypes;
                                        }
                                    }
                                }
                                if (it.hasNext() != ittas.hasNext()) {
                                    continue subtypes;
                                }
                            } else {
                                for (TypeMirror tm : e.getInterfaces()) {
                                    if (((DeclaredType)tm).asElement() == elem) {
                                        DeclaredType dt = (DeclaredType)tm;
                                        Iterator<? extends TypeMirror> ittas = tas.iterator();
                                        Iterator<? extends TypeMirror> it = dt.getTypeArguments().iterator();
                                        while(it.hasNext() && ittas.hasNext()) {
                                            TypeMirror basetm = ittas.next();
                                            TypeMirror stm = it.next();
                                            if (basetm != stm) {
                                                if (stm.getKind() == TypeKind.TYPEVAR) {
                                                    map.put(((TypeVariable)stm).asElement(), basetm);
                                                } else {
                                                    continue subtypes;
                                                }
                                            }
                                        }
                                        if (it.hasNext() != ittas.hasNext()) {
                                            continue subtypes;
                                        }
                                        break;
                                    }
                                }
                            }
                            bases.add(getDeclaredType(e, map, types));
                        }
                    }
                }
            }
        }
        return subtypes;
    }

    private DeclaredType getDeclaredType(TypeElement e, HashMap<? extends Element, ? extends TypeMirror> map, Types types) {
        List<? extends TypeParameterElement> tpes = e.getTypeParameters();
        TypeMirror[] targs = new TypeMirror[tpes.size()];
        int i = 0;
        for (Iterator<? extends TypeParameterElement> it = tpes.iterator(); it.hasNext();) {
            TypeParameterElement tpe = it.next();
            TypeMirror t = map.get(tpe);
            targs[i++] = t != null ? t : tpe.asType();
        }
        Element encl = e.getEnclosingElement();
        if ((encl.getKind().isClass() || encl.getKind().isInterface()) && !((TypeElement)encl).getTypeParameters().isEmpty())
                return types.getDeclaredType(getDeclaredType((TypeElement)encl, map, types), e, targs);
        return types.getDeclaredType(e, targs);
    }
}
