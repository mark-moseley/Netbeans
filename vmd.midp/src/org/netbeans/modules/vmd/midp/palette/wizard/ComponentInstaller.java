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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 */

package org.netbeans.modules.vmd.midp.palette.wizard;

import org.netbeans.api.java.source.*;
import org.netbeans.api.project.Project;
import org.netbeans.modules.vmd.api.io.ProjectUtils;
import org.netbeans.modules.vmd.api.model.*;
import org.netbeans.modules.vmd.midp.components.MidpDocumentSupport;
import org.netbeans.modules.vmd.midp.components.MidpProjectSupport;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.midp.components.general.ClassCD;
import org.netbeans.modules.vmd.midp.palette.MidpPaletteProvider;
import org.netbeans.modules.vmd.midp.serialization.MidpPropertyPresenterSerializer;
import org.netbeans.modules.vmd.midp.serialization.MidpSetterPresenterSerializer;
import org.netbeans.modules.vmd.midp.serialization.MidpTypesConvertor;
import org.openide.ErrorManager;

import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.io.IOException;
import java.util.*;

/**
 * @author David Kaspar
 */
public final class ComponentInstaller {

    public static void install (Map<String, Item> componentsToInstall) {
        for (Item item : componentsToInstall.values ()) {
            ComponentSerializationSupport.serialize (MidpDocumentSupport.PROJECT_TYPE_MIDP, item.getTypeDescriptor (), item.getPaletteDescriptor (), item.getProperties (), item.getPresenters ());
        }
        ComponentSerializationSupport.refreshDescriptorRegistry (MidpDocumentSupport.PROJECT_TYPE_MIDP);
        // TODO
    }

    public static Map<String,Item> search (Project project) {
        ClasspathInfo info = MidpProjectSupport.getClasspathInfo (project);
        if (info == null)
            return Collections.emptyMap ();
        final Set<ElementHandle<TypeElement>> allHandles = info.getClassIndex ().getDeclaredTypes ("", ClassIndex.NameKind.PREFIX, EnumSet.of (ClassIndex.SearchScope.SOURCE, ClassIndex.SearchScope.DEPENDENCIES)); // NOI18N
        final Map<String, ComponentDescriptor> registry = resolveRegistryMap (project);
        final HashMap<String, Item> result = new HashMap<String, Item> ();

        try {
            JavaSource.create (info).runUserActionTask (new CancellableTask<CompilationController>() {
                public void cancel () {
                }
                public void run (CompilationController parameter) throws Exception {
                    HashSet<TypeElement> elements = new HashSet<TypeElement> ();
                    for (ElementHandle<TypeElement> handle : allHandles) {
                        TypeElement element = handle.resolve (parameter);
                        if (element != null  &&  element.getKind () != ElementKind.CLASS)
                            continue;
                        elements.add (element);
                    }

                    for (;;) {
                        Iterator<TypeElement> iterator = elements.iterator ();
                        if (! iterator.hasNext ())
                            break;
                        TypeElement element = iterator.next ();
                        search (element, elements, registry, result);
                    }
                }
            }, true);
        } catch (IOException e) {
            ErrorManager.getDefault ().notify (e);
        }
        return result;
    }

    private static Map<String,ComponentDescriptor> resolveRegistryMap (Project project) {
        final DescriptorRegistry registry = DescriptorRegistry.getDescriptorRegistry (MidpDocumentSupport.PROJECT_TYPE_MIDP, ProjectUtils.getProjectID (project));
        final HashMap<String, ComponentDescriptor> registryMap = new HashMap<String, ComponentDescriptor> ();

        registry.readAccess (new Runnable() {
            public void run () {
                for (ComponentDescriptor descriptor : registry.getComponentDescriptors ()) {
                    TypeID thisType = descriptor.getTypeDescriptor ().getThisType ();

                    String string = thisType.getString ();
                    if (! checkForJavaIdentifierCompliant (string))
                        continue;

                    if (! registry.isInHierarchy (ClassCD.TYPEID, thisType)  ||  ClassCD.TYPEID.equals (thisType))
                        continue;

                    registryMap.put (string, descriptor);
                }
            }
        });

        return registryMap;
    }

    private static boolean search (TypeElement element, Set<TypeElement> elements, Map<String, ComponentDescriptor> registry, Map<String, Item> result) {
        if (element == null)
            return false;

        elements.remove (element);

        if (element.getKind () != ElementKind.CLASS)
            return false;

        String fqn = element.getQualifiedName ().toString ();

        ComponentDescriptor descriptor = registry.get (fqn);
        if (descriptor != null)
            return true;
        Item item = result.get (fqn);
        if (item != null)
            return true;

        TypeElement superElement = getSuperElement (element);
        if (superElement == null)
            return false;
        if (! search (superElement, elements, registry, result))
            return false;

        String superFQN = superElement.getQualifiedName ().toString ();
        if (! registry.containsKey(superFQN)  &&  ! result.containsKey (superFQN))
            return false;

        boolean isAbstract = element.getModifiers ().contains (Modifier.ABSTRACT);
        boolean isFinal = element.getModifiers ().contains (Modifier.FINAL);
        item = new Item (superFQN, fqn, isAbstract, isFinal);

        boolean hasConstructor = inspectElement (item, element);
        if (! isAbstract  ||  ! hasConstructor)
            return false;

        result.put (fqn, item);
        return true;
    }

    private static boolean inspectElement (Item item, TypeElement clazz) {
        String fqn = clazz.getQualifiedName ().toString ();
        boolean hasConstructor = false;
        int constructorIndex = 1;

        for (Element el : clazz.getEnclosedElements ()) {
            if (! el.getModifiers ().contains (Modifier.PUBLIC))
                continue;

            if (el.getKind () == ElementKind.CONSTRUCTOR) {
                ExecutableElement method = (ExecutableElement) el;
                ArrayList<String> properties = new ArrayList<String> ();
                int index = 1;
                for (VariableElement parameter : method.getParameters ()) {
                    PropertyDescriptor property = MidpTypesConvertor.createPropertyDescriptorForParameter (fqn + "#" + constructorIndex + "#" + index, true, parameter);
                    item.addProperty (property);
                    properties.add (property.getName ());
                    item.addPresenter (new MidpPropertyPresenterSerializer ("" + constructorIndex + ". constructor - " + index + ". parameter", property));
                    index ++;
                }
                item.addPresenter (new MidpSetterPresenterSerializer (null, properties));

            } else if (el.getKind () == ElementKind.METHOD) {
                ExecutableElement method = (ExecutableElement) el;
                String name = method.getSimpleName ().toString ();
                ArrayList<String> properties = new ArrayList<String> ();
                List<? extends VariableElement> parameters = method.getParameters ();
                if (parameters.size () != 1)
                    continue;
                VariableElement parameter = parameters.iterator ().next ();

                PropertyDescriptor property = MidpTypesConvertor.createPropertyDescriptorForParameter (fqn + "#" + name, false, parameter);
                item.addProperty (property);
                properties.add (property.getName ());
                item.addPresenter (new MidpPropertyPresenterSerializer (name + " method parameter", property));

                item.addPresenter (new MidpSetterPresenterSerializer (name, properties));
            }
        }

        return hasConstructor;
    }

    private static TypeElement getSuperElement (TypeElement element) {
        TypeMirror superType = element.getSuperclass ();
        if (superType.getKind () != TypeKind.DECLARED)
            return null;
        return (TypeElement) ((DeclaredType) superType).asElement ();
    }

    private static boolean checkForJavaIdentifierCompliant (String fqn) {
        if (fqn == null || fqn.length () < 1)
            return false;
        if (! Character.isJavaIdentifierStart (fqn.charAt (0)))
            return false;
        boolean dot = false;
        for (int index = 1; index < fqn.length (); index ++) {
            char c = fqn.charAt (index);
            if (Character.isJavaIdentifierPart (c)) {
                dot = false;
                continue;
            }
            if (c != '.')
                return false;
            if (dot)
                return false;
            dot = true;
        }
        return ! dot;
    }

    public static class Item {

        private TypeDescriptor typeDescriptor;
        private PaletteDescriptor paletteDescriptor;
        private String fqn;
        private ArrayList<PropertyDescriptor> properties = new ArrayList<PropertyDescriptor> ();
        private ArrayList<PresenterSerializer> presenters = new ArrayList<PresenterSerializer> ();

        public Item (String superFQN, String fqn, boolean isAbstract, boolean isFinal) {
            this.fqn = fqn;
            TypeID typeID = new TypeID (TypeID.Kind.COMPONENT, fqn);
            typeDescriptor = new TypeDescriptor (new TypeID (TypeID.Kind.COMPONENT, superFQN), typeID, isAbstract, isFinal);
            paletteDescriptor = new PaletteDescriptor (MidpPaletteProvider.CATEGORY_CUSTOM, MidpTypes.getSimpleClassName (typeID), fqn, null, null);
        }

        public String getFQN () {
            return fqn;
        }

        public TypeDescriptor getTypeDescriptor () {
            return typeDescriptor;
        }

        public PaletteDescriptor getPaletteDescriptor () {
            return paletteDescriptor;
        }

        public List<PropertyDescriptor> getProperties () {
            return properties;
        }

        public List<PresenterSerializer> getPresenters () {
            return presenters;
        }

        public void addPresenter (PresenterSerializer serializer) {
            presenters.add (serializer);
        }

        public void addProperty (PropertyDescriptor property) {
            properties.add (property);
        }

    }

}
