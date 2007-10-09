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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.java.navigation;

import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.ui.ElementIcons;
import org.netbeans.api.java.source.ui.ElementOpen;
import org.openide.ErrorManager;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.swing.Icon;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import org.netbeans.api.java.source.ui.ElementJavadoc;

/**
 * The tree model for members pop up window.
 * 
 * @author Sandip Chitale (Sandip.Chitale@Sun.Com)
 */
public final class JavaMembersModel extends DefaultTreeModel {
    static Element[] EMPTY_ELEMENTS_ARRAY = new Element[0];
    static ElementHandle[] EMPTY_ELEMENTHANDLES_ARRAY = new ElementHandle[0];

    private String pattern = ""; // NOI18N
    private String patternLowerCase = ""; // NOI18N
    private FileObject fileObject;
    private ElementHandle[] elementHandles;

    /**
     * 
     * @param fileObject 
     * @param elements 
     * @param compilationInfo 
     */
    public JavaMembersModel(FileObject fileObject, Element[] elements, CompilationInfo compilationInfo) {
        super(null);
        this.fileObject = fileObject;

        if ((elements == null) || (elements.length == 0)) {
            elementHandles = EMPTY_ELEMENTHANDLES_ARRAY;
        } else {
            List<ElementHandle> elementHandlesList = new ArrayList<ElementHandle>(elements.length);

            for (Element element : elements) {
                elementHandlesList.add(ElementHandle.create(element));
            }

            elementHandles = elementHandlesList.toArray(EMPTY_ELEMENTHANDLES_ARRAY);
        }

        update(elements, compilationInfo);
    }

    /**
     * Getter for property pattern.
     * @return Value of property pattern.
     */
    public String getPattern() {
        return this.pattern;
    }

    /**
     * Setter for property pattern.
     * @param pattern New value of property pattern.
     */
    public void setPattern(String pattern) {
        this.pattern = pattern;

        if (pattern == null) {
            patternLowerCase = null;
        } else {
            patternLowerCase = pattern.toLowerCase();
        }
    }

    /**
     * 
     */
    public void update() {
        update(elementHandles);
    }

    private void update(final ElementHandle[] elementHandles) {
        if ((elementHandles == null) && (elementHandles.length == 0)) {
            return;
        }

        JavaSource javaSource = JavaSource.forFileObject(fileObject);

        if (javaSource != null) {
            try {
                javaSource.runUserActionTask(new Task<CompilationController>() {

                        public void run(
                            CompilationController compilationController)
                            throws Exception {
                            compilationController.toPhase(Phase.ELEMENTS_RESOLVED);

                            List<Element> elementsList = new ArrayList<Element>(elementHandles.length);

                            for (ElementHandle elementHandle : elementHandles) {
                                elementsList.add(elementHandle.resolve(
                                        compilationController));
                            }

                            Element[] elements = elementsList.toArray(EMPTY_ELEMENTS_ARRAY);
                            update(elements, compilationController);
                        }
                    }, true);

                return;
            } catch (IOException ioe) {
                ErrorManager.getDefault().notify(ioe);
            }
        }
    }

    private void update(final Element[] elements,
        CompilationInfo compilationInfo) {
        if ((elements == null) && (elements.length == 0)) {
            return;
        }

        DefaultMutableTreeNode root = new DefaultMutableTreeNode();

        for (Element element : elements) {
            if (element.getKind() == ElementKind.PACKAGE) {
                root.add(new PackageTreeNode(fileObject,
                        ((PackageElement) element), compilationInfo));
            } else if ((element.getKind() == ElementKind.CLASS) ||
                    (element.getKind() == ElementKind.INTERFACE) ||
                    (element.getKind() == ElementKind.ENUM) ||
                    (element.getKind() == ElementKind.ANNOTATION_TYPE)) {
                root.add(new TypeTreeNode(fileObject, ((TypeElement) element),
                        compilationInfo));
            } else if (element.getKind() == ElementKind.CONSTRUCTOR) {
                root.add(new ConstructorTreeNode(fileObject,
                        ((ExecutableElement) element), compilationInfo));
            } else if (element.getKind() == ElementKind.METHOD) {
                root.add(new MethodTreeNode(fileObject,
                        ((ExecutableElement) element), compilationInfo));
            } else if (element.getKind() == ElementKind.FIELD) {
                root.add(new FieldTreeNode(fileObject,
                        ((VariableElement) element), compilationInfo));
            } else if (element.getKind() == ElementKind.ENUM_CONSTANT) {
                root.add(new EnumConstantTreeNode(fileObject,
                        ((VariableElement) element), compilationInfo));
            }
        }

        setRoot(root);
    }

    boolean patternMatch(JavaElement javaToolsJavaElement) {
        return Utils.patternMatch(javaToolsJavaElement, pattern, patternLowerCase);
    }

    abstract class AbstractMembersTreeNode
        extends DefaultMutableTreeNode implements JavaElement {
        private FileObject fileObject;
        private ElementHandle<?extends Element> elementHandle;
        private ElementKind elementKind;
        private Set<Modifier> modifiers;
        private String name = "";
        private String label = "";
        private String tooltip = null;
        private Icon icon = null;
        private ElementJavadoc javaDoc;

        AbstractMembersTreeNode(FileObject fileObject,
            Element element, CompilationInfo compilationInfo) {
            this.fileObject = fileObject;
            this.elementHandle = ElementHandle.create(element);
            this.elementKind = element.getKind();
            this.modifiers = element.getModifiers();

            if (element.getKind() == ElementKind.CONSTRUCTOR) {                
                setName(element.getEnclosingElement().getSimpleName().toString());
            } else {
                setName(element.getSimpleName().toString());
            }
            setIcon(ElementIcons.getElementIcon(element.getKind(),
                    element.getModifiers()));
            setLabel(Utils.format(element));
            setToolTip(Utils.format(element, true));
            setJavaDoc( ElementJavadoc.create(compilationInfo, element) );
            loadChildren(element, compilationInfo);
        }

        public FileObject getFileObject() {
            return fileObject;
        }

        public String getName() {
            return name;
        }

        protected void setName(String name) {
            this.name = name;
        }

        public String getLabel() {
            return label;
        }

        protected void setLabel(String label) {
            this.label = label;
        }

        public String getTooltip() {
            return tooltip;
        }

        protected void setToolTip(String tooltip) {
            this.tooltip = tooltip;
        }

        public Icon getIcon() {
            return icon;
        }

        protected void setIcon(Icon icon) {
            this.icon = icon;
        }

        protected void setElementHandle(
            ElementHandle<?extends Element> elementHandle) {
            this.elementHandle = elementHandle;
        }

        public ElementJavadoc getJavaDoc() {
            return javaDoc;
        }

        public void setJavaDoc(ElementJavadoc javaDoc) {
            this.javaDoc = javaDoc;
        }

        public Set<Modifier> getModifiers() {
            return modifiers;
        }

        public ElementHandle getElementHandle() {
            return elementHandle;
        }

        public void gotoElement() {
            openElementHandle();
        }

        protected abstract void loadChildren(Element element,
            CompilationInfo compilationInfo);

        public String toString() {
            return getLabel();
        }

        protected void openElementHandle() {
            if (fileObject == null) {
                StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(JavaMembersModel.class, "MSG_CouldNotOpenElement", getLabel()));
                return;
            }
            
            if (elementHandle == null) {                
                return;
            }

            ElementOpen.open(fileObject, elementHandle);
        }
    }

    class PackageTreeNode extends AbstractMembersTreeNode {
        PackageTreeNode(FileObject fileObject, PackageElement packageElement,
            CompilationInfo compilationInfo) {
            super(fileObject, packageElement, compilationInfo);
        }

        public boolean isLeaf() {
            return true;
        }

        protected void loadChildren(Element element,
            CompilationInfo compilationInfo) {
        }
    }

    class TypeTreeNode extends AbstractMembersTreeNode {
        private boolean inSuperClassRole;

        TypeTreeNode(FileObject fileObject, TypeElement typeElement,
            CompilationInfo compilationInfo) {
            this(fileObject, typeElement, compilationInfo, false);
        }

        TypeTreeNode(FileObject fileObject, TypeElement typeElement,
            CompilationInfo compilationInfo, boolean inSuperClassRole) {
            super(fileObject, typeElement, compilationInfo);
            this.inSuperClassRole = inSuperClassRole;
        }

        public boolean isLeaf() {
            return false;
        }

        protected void loadChildren(Element element,
            CompilationInfo compilationInfo) {
            loadChildren(element, compilationInfo, 0);
        }

        protected int loadChildren(Element element,
            CompilationInfo compilationInfo, int index) {
            TypeElement typeElement = (TypeElement) element;

            List<?extends Element> enclosedElements = typeElement.getEnclosedElements();

            for (Element enclosedElement : enclosedElements) {
                AbstractMembersTreeNode node = null;

                if (enclosedElement.getKind() == ElementKind.CLASS ||
                        enclosedElement.getKind() == ElementKind.INTERFACE||
                        enclosedElement.getKind() == ElementKind.ENUM) {
                    if (!JavaMembersAndHierarchyOptions.isShowInner()) {
                        continue;
                    }

                    if (JavaMembersAndHierarchyOptions.isShowInherited()) {
                        continue;
                    }

                    node = new TypeTreeNode(getFileObject(),
                            (TypeElement) enclosedElement, compilationInfo);
                } else {
                    Set<Modifier> modifiers = enclosedElement.getModifiers();

                    if (enclosedElement.getKind() == ElementKind.CONSTRUCTOR) {
                        if (!JavaMembersAndHierarchyOptions.isShowConstructors()) {
                            continue;
                        }

                        ExecutableElement constructor = (ExecutableElement) enclosedElement;

                        if ((!modifiers.contains(Modifier.PUBLIC) &&
                                !modifiers.contains(Modifier.PROTECTED) &&
                                !modifiers.contains(Modifier.PRIVATE)) &&
                                !JavaMembersAndHierarchyOptions.isShowPackage()) {
                            continue;
                        } else if (modifiers.contains(Modifier.PROTECTED) &&
                                !JavaMembersAndHierarchyOptions.isShowProtected()) {
                            continue;
                        } else if (modifiers.contains(Modifier.PRIVATE) &&
                                !JavaMembersAndHierarchyOptions.isShowPrivate()) {
                            continue;
                        }

                        node = new ConstructorTreeNode(getFileObject(),
                                constructor, compilationInfo);
                    } else if (enclosedElement.getKind() == ElementKind.METHOD) {
                        if (!JavaMembersAndHierarchyOptions.isShowMethods()) {
                            continue;
                        }

                        ExecutableElement method = (ExecutableElement) enclosedElement;

                        if ((!modifiers.contains(Modifier.PUBLIC) &&
                                !modifiers.contains(Modifier.PROTECTED) &&
                                !modifiers.contains(Modifier.PRIVATE)) &&
                                !JavaMembersAndHierarchyOptions.isShowPackage()) {
                            continue;
                        } else if (modifiers.contains(Modifier.PROTECTED) &&
                                !JavaMembersAndHierarchyOptions.isShowProtected()) {
                            continue;
                        } else if (modifiers.contains(Modifier.PRIVATE) &&
                                !JavaMembersAndHierarchyOptions.isShowPrivate()) {
                            continue;
                        }

                        if (modifiers.contains(Modifier.STATIC) &&
                                !JavaMembersAndHierarchyOptions.isShowStatic()) {
                            continue;
                        }

                        node = new MethodTreeNode(getFileObject(), method,
                                compilationInfo);
                    } else if (enclosedElement.getKind() == ElementKind.FIELD) {
                        if (!JavaMembersAndHierarchyOptions.isShowFields()) {
                            continue;
                        }

                        VariableElement field = (VariableElement) enclosedElement;

                        if ((!modifiers.contains(Modifier.PUBLIC) &&
                                !modifiers.contains(Modifier.PROTECTED) &&
                                !modifiers.contains(Modifier.PRIVATE)) &&
                                !JavaMembersAndHierarchyOptions.isShowPackage()) {
                            continue;
                        } else if (modifiers.contains(Modifier.PROTECTED) &&
                                !JavaMembersAndHierarchyOptions.isShowProtected()) {
                            continue;
                        } else if (modifiers.contains(Modifier.PRIVATE) &&
                                !JavaMembersAndHierarchyOptions.isShowPrivate()) {
                            continue;
                        }

                        if (modifiers.contains(Modifier.STATIC) &&
                                !JavaMembersAndHierarchyOptions.isShowStatic()) {
                            continue;
                        }

                        node = new FieldTreeNode(getFileObject(), field,
                                compilationInfo);
                    } else if (enclosedElement.getKind() == ElementKind.ENUM_CONSTANT) {
                        if (!JavaMembersAndHierarchyOptions.isShowEnumConstants()) {
                            continue;
                        }

                        VariableElement enumConstant = (VariableElement) enclosedElement;

                        if ((!modifiers.contains(Modifier.PUBLIC) &&
                                !modifiers.contains(Modifier.PROTECTED) &&
                                !modifiers.contains(Modifier.PRIVATE)) &&
                                !JavaMembersAndHierarchyOptions.isShowPackage()) {
                            continue;
                        } else if (modifiers.contains(Modifier.PROTECTED) &&
                                !JavaMembersAndHierarchyOptions.isShowProtected()) {
                            continue;
                        } else if (modifiers.contains(Modifier.PRIVATE) &&
                                !JavaMembersAndHierarchyOptions.isShowPrivate()) {
                            continue;
                        }

                        if (modifiers.contains(Modifier.STATIC) &&
                                !JavaMembersAndHierarchyOptions.isShowStatic()) {
                            continue;
                        }

                        node = new EnumConstantTreeNode(getFileObject(),
                                enumConstant, compilationInfo);
                    }

                    if ((node == null) || !patternMatch(node)) {
                        continue;
                    }
                }

                if (node == null) {
                    continue;
                }

                insert(node, index++);
            }

            if (JavaMembersAndHierarchyOptions.isShowInherited()) {
                TypeMirror superClassTypeMirror = typeElement.getSuperclass();

                if (superClassTypeMirror.getKind() == TypeKind.NONE) {
                    //
                } else {
                    TypeElement superClass = (TypeElement) ((DeclaredType) typeElement.getSuperclass()).asElement();

                    if ((superClass != null) &&
                            !superClass.getQualifiedName().toString()
                                           .equals(Object.class.getName())) {
                        FileObject fileObject = SourceUtils.getFile(ElementHandle.create(superClass),
                                compilationInfo.getClasspathInfo());
                        insert(new TypeTreeNode(fileObject, superClass,
                                compilationInfo, true), index++);
                    }
                }

                List<?extends TypeMirror> interfaces = typeElement.getInterfaces();

                for (TypeMirror interfaceTypeMirror : interfaces) {
                    TypeElement anInterface = (TypeElement) ((DeclaredType) interfaceTypeMirror).asElement();
                    FileObject fileObject = SourceUtils.getFile(ElementHandle.create(anInterface),
                            compilationInfo.getClasspathInfo());
                    insert(new TypeTreeNode(fileObject, anInterface,
                            compilationInfo, true), index++);
                }
            }

            if (JavaMembersAndHierarchyOptions.isShowInner()) {
                if (!inSuperClassRole &&
                        JavaMembersAndHierarchyOptions.isShowInherited()) {
                    for (Element enclosedElement : enclosedElements) {
                        if (enclosedElement.getKind() == ElementKind.CLASS ||
                                enclosedElement.getKind() == ElementKind.INTERFACE ||
                                enclosedElement.getKind() == ElementKind.ENUM
                                ) {
                            AbstractMembersTreeNode node = new TypeTreeNode(getFileObject(),
                                    (TypeElement) enclosedElement,
                                    compilationInfo);
                            insert(node, index++);
                        }
                    }
                }
            }

            return index;
        }
    }

    class ConstructorTreeNode extends AbstractMembersTreeNode {
        ConstructorTreeNode(FileObject fileObject,
            ExecutableElement contructorElement, CompilationInfo compilationInfo) {
            super(fileObject, contructorElement, compilationInfo);
        }

        public boolean isLeaf() {
            return true;
        }

        protected void loadChildren(Element element,
            CompilationInfo compilationInfo) {
        }
    }

    class MethodTreeNode extends AbstractMembersTreeNode {
        MethodTreeNode(FileObject fileObject, ExecutableElement methodElement,
            CompilationInfo compilationInfo) {
            super(fileObject, methodElement, compilationInfo);
        }

        public boolean isLeaf() {
            return true;
        }

        protected void loadChildren(Element element,
            CompilationInfo compilationInfo) {
        }
    }

    class FieldTreeNode extends AbstractMembersTreeNode {
        FieldTreeNode(FileObject fileObject, VariableElement variableElement,
            CompilationInfo compilationInfo) {
            super(fileObject, variableElement, compilationInfo);
        }

        public boolean isLeaf() {
            return true;
        }

        protected void loadChildren(Element element,
            CompilationInfo compilationInfo) {
        }
    }

    class EnumConstantTreeNode extends AbstractMembersTreeNode {
        EnumConstantTreeNode(FileObject fileObject,
            VariableElement variableElement, CompilationInfo compilationInfo) {
            super(fileObject, variableElement, compilationInfo);
        }

        public boolean isLeaf() {
            return true;
        }

        protected void loadChildren(Element element,
            CompilationInfo compilationInfo) {
        }
    }
}
