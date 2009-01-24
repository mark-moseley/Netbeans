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

package org.netbeans.modules.groovy.editor.completion;

import groovy.lang.MetaMethod;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.swing.ImageIcon;
import org.codehaus.groovy.ast.ASTNode;
import org.netbeans.modules.gsf.api.ElementHandle;
import org.netbeans.modules.gsf.api.ElementKind;
import org.netbeans.modules.gsf.api.HtmlFormatter;
import org.netbeans.modules.gsf.api.Modifier;
import org.netbeans.modules.groovy.editor.api.elements.KeywordElement;
import java.util.logging.Logger;
import java.util.logging.Level;
import javax.lang.model.type.TypeMirror;
import org.codehaus.groovy.ast.Variable;
import org.netbeans.api.java.source.ui.ElementIcons;
import org.netbeans.modules.groovy.editor.api.NbUtilities;
import org.netbeans.modules.groovy.editor.api.completion.CompletionHandler;
import org.netbeans.modules.groovy.editor.api.elements.AstMethodElement;
import org.netbeans.modules.groovy.editor.api.elements.ElementHandleSupport;
import org.netbeans.modules.groovy.editor.api.elements.GroovyElement;
import org.netbeans.modules.groovy.editor.java.Utilities;
import org.netbeans.modules.groovy.support.api.GroovySources;
import org.netbeans.modules.gsf.api.CompilationInfo;
import org.netbeans.modules.gsf.spi.DefaultCompletionProposal;
import org.openide.util.ImageUtilities;


/**
 *
 * @author schmidtm
 */
// FIXME static accessors
public abstract class CompletionItem extends DefaultCompletionProposal {

    private static final Logger LOG = Logger.getLogger(CompletionItem.class.getName());

    protected final GroovyElement element;

    private static volatile ImageIcon groovyIcon;

    private static volatile ImageIcon javaIcon;
    
    private static volatile ImageIcon newConstructorIcon;

    private CompletionItem(GroovyElement element, int anchorOffset) {
        this.element = element;
        this.anchorOffset = anchorOffset;

        LOG.setLevel(Level.OFF);
    }

    @Override
    public String getName() {
        return element.getName();
    }

    public ElementHandle getElement() {
        LOG.log(Level.FINEST, "getElement() element : {0}", element);

        return null;
    }

    @Override
    public ElementKind getKind() {
        return element.getKind();
    }

    @Override
    public Set<Modifier> getModifiers() {
        return element.getModifiers();
    }

    @Override
    public String toString() {
        String cls = getClass().getName();
        cls = cls.substring(cls.lastIndexOf('.') + 1);

        return cls + "(" + getKind() + "): " + getName();
    }

    public static CompletionItem forJavaMethod(String className, String simpleName, String parameterString,
            TypeMirror returnType, Set<javax.lang.model.element.Modifier> modifiers, int anchorOffset,
            boolean emphasise, boolean nameOnly) {
        return new JavaMethodItem(className, simpleName, parameterString, returnType, modifiers, anchorOffset, emphasise, nameOnly);
    }

    public static CompletionItem forJavaMethod(String className, String simpleName, String parameterString,
            String returnType, Set<javax.lang.model.element.Modifier> modifiers, int anchorOffset,
            boolean emphasise, boolean nameOnly) {
        return new JavaMethodItem(className, simpleName, parameterString, returnType, modifiers, anchorOffset, emphasise, nameOnly);
    }

    public static CompletionItem forDynamicMethod(int anchorOffset, String name, String[] parameters, String returnType, boolean nameOnly) {
        return new DynamicMethodItem(anchorOffset, name, parameters, returnType, nameOnly);
    }

//    public static CompletionItem forMetaMethod(Class clz, MetaMethod method, int anchorOffset, boolean isGDK) {
//        return new MetaMethodItem(clz, method, anchorOffset, isGDK);
//    }

    private static class JavaMethodItem extends CompletionItem {

        private final String className;

        private final String simpleName;

        private final String parameterString;

        private final String returnType;

        private final Set<javax.lang.model.element.Modifier> modifiers;

        private final boolean emphasise;

        private final boolean nameOnly;

        public JavaMethodItem(String className, String simpleName, String parameterString, TypeMirror returnType,
                Set<javax.lang.model.element.Modifier> modifiers, int anchorOffset, boolean emphasise, boolean nameOnly) {
            this(className, simpleName, parameterString,
                    Utilities.getTypeName(returnType, false).toString(), modifiers, anchorOffset, emphasise, nameOnly);
        }

        public JavaMethodItem(String className, String simpleName, String parameterString, String returnType,
                Set<javax.lang.model.element.Modifier> modifiers, int anchorOffset, boolean emphasise, boolean nameOnly) {
            super(null, anchorOffset);
            this.className = className;
            this.simpleName = simpleName;
            this.parameterString = parameterString;
            this.returnType = NbUtilities.stripPackage(returnType);
            this.modifiers = modifiers;
            this.emphasise = emphasise;
            this.nameOnly = nameOnly;
        }

        @Override
        public String getName() {
            return simpleName + "()";
        }

        @Override
        public ElementKind getKind() {
            return ElementKind.METHOD;
        }

        @Override
        public String getLhsHtml(HtmlFormatter formatter) {
            if (emphasise) {
                formatter.emphasis(true);
            }
            formatter.appendText(simpleName + "(" + parameterString + ")");
            if (emphasise) {
                formatter.emphasis(false);
            }
            return formatter.getText();
        }

        @Override
        public String getRhsHtml(HtmlFormatter formatter) {
            // FIXME
            String retType = "";
            if (returnType != null) {
                retType = returnType;
            }

            formatter.appendText(retType);

            return formatter.getText();
        }


        @Override
        public ImageIcon getIcon() {
            return (ImageIcon) ElementIcons.getElementIcon(
                    javax.lang.model.element.ElementKind.METHOD, modifiers);
        }

        @Override
        public Set<Modifier> getModifiers() {
            return Utilities.modelModifiersToGsf(modifiers);
        }

        @Override
        public ElementHandle getElement() {
            return ElementHandleSupport.createHandle(className, simpleName, ElementKind.METHOD,
                    Utilities.modelModifiersToGsf(modifiers));
        }

        @Override
        public String getCustomInsertTemplate() {
            if (nameOnly) {
                return simpleName;
            }
            return super.getCustomInsertTemplate();
        }

    }

    public static class DynamicFieldItem extends CompletionItem {

        private final String name;

        private final String type;

        public DynamicFieldItem(int anchorOffset, String name, String type) {
            super(null, anchorOffset);
            this.name = name;
            this.type = type;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public ElementKind getKind() {
            return ElementKind.FIELD;
        }

        @Override
        public String getRhsHtml(HtmlFormatter formatter) {
            // no FQN return types but only the classname, please:

            String retType = type;
            retType = NbUtilities.stripPackage(retType);

            formatter.appendText(retType);

            return formatter.getText();
        }

        @Override
        public ImageIcon getIcon() {
            if (groovyIcon == null) {
                groovyIcon = new ImageIcon(ImageUtilities.loadImage(GroovySources.GROOVY_FILE_ICON_16x16));
            }

            return groovyIcon;
        }

        @Override
        public Set<Modifier> getModifiers() {
            return Collections.singleton(Modifier.PROTECTED);
        }

        @Override
        public ElementHandle getElement() {
            return ElementHandleSupport.createHandle(null, name, ElementKind.FIELD,
                    Collections.singleton(Modifier.PROTECTED));
        }
    }

    private static class DynamicMethodItem extends CompletionItem {

        private final String name;

        private final String[] parameters;

        private final String returnType;

        private final boolean nameOnly;

        public DynamicMethodItem(int anchorOffset, String name, String[] parameters, String returnType, boolean nameOnly) {
            super(null, anchorOffset);
            this.name = name;
            this.parameters = parameters;
            this.returnType = returnType;
            this.nameOnly = nameOnly;
        }

        @Override
        public String getName() {
            return name + "()";
        }

        @Override
        public ElementKind getKind() {
            return ElementKind.METHOD;
        }

        @Override
        public String getLhsHtml(HtmlFormatter formatter) {

            ElementKind kind = getKind();

            formatter.name(kind, true);

            formatter.appendText(name);

            StringBuilder buf = new StringBuilder();
            // construct signature by removing package names.
            for (String param : parameters) {
                if (buf.length() > 0) {
                    buf.append(", ");
                }
                buf.append(NbUtilities.stripPackage(Utilities.translateClassLoaderTypeName(param)));
            }

            String simpleSig = buf.toString();
            formatter.appendText("(" + simpleSig + ")");


            formatter.name(kind, false);

            return formatter.getText();
        }

        @Override
        public String getRhsHtml(HtmlFormatter formatter) {
            // no FQN return types but only the classname, please:

            String retType = returnType;
            retType = NbUtilities.stripPackage(retType);

            formatter.appendText(retType);

            return formatter.getText();
        }

        @Override
        public ImageIcon getIcon() {
            if (groovyIcon == null) {
                groovyIcon = new ImageIcon(ImageUtilities.loadImage(GroovySources.GROOVY_FILE_ICON_16x16));
            }

            return groovyIcon;
        }

        @Override
        public Set<Modifier> getModifiers() {
            return Collections.singleton(Modifier.PROTECTED);
        }

        @Override
        public ElementHandle getElement() {
            return ElementHandleSupport.createHandle(null, name, ElementKind.METHOD,
                    Collections.singleton(Modifier.PROTECTED));
        }

        @Override
        public String getCustomInsertTemplate() {
            if (nameOnly) {
                return name;
            }
            return super.getCustomInsertTemplate();
        }

    }

    public static class MetaMethodItem extends CompletionItem {

        private final MetaMethod method;

        private final boolean isGDK;

        private final AstMethodElement methodElement;

        private final boolean nameOnly;

        public MetaMethodItem(Class clz, MetaMethod method, int anchorOffset, boolean isGDK, boolean nameOnly) {
            super(null, anchorOffset);
            this.method = method;
            this.isGDK = isGDK;
            this.nameOnly = nameOnly;

            // This is an artificial, new ElementHandle which has no real
            // equivalent in the AST. It's used to match the one passed to super.document()
            methodElement = new AstMethodElement(new ASTNode(), clz, method, isGDK);
        }

        public MetaMethod getMethod() {
            return method;
        }

        @Override
        public String getName() {
            return method.getName() + "()";
        }

        @Override
        public ElementKind getKind() {
            return ElementKind.METHOD;
        }

        @Override
        public String getLhsHtml(HtmlFormatter formatter) {

            ElementKind kind = getKind();

            formatter.name(kind, true);

            if (isGDK) {
                formatter.appendText(method.getName());

                // construct signature by removing package names.

                String signature = method.getSignature();
                int start = signature.indexOf("(");
                int end = signature.indexOf(")");

                String sig = signature.substring(start + 1, end);

                StringBuffer buf = new StringBuffer();

                for (String param : sig.split(",")) {
                    if (buf.length() > 0) {
                        buf.append(", ");
                    }
                    buf.append(NbUtilities.stripPackage(Utilities.translateClassLoaderTypeName(param)));
                }

                String simpleSig = buf.toString();
                formatter.appendText("(" + simpleSig + ")");
            } else {
                formatter.appendText(CompletionHandler.getMethodSignature(method, false, isGDK));
            }


            formatter.name(kind, false);

            return formatter.getText();
        }

        @Override
        public String getRhsHtml(HtmlFormatter formatter) {
            // no FQN return types but only the classname, please:

            String retType = method.getReturnType().getSimpleName();
            retType = NbUtilities.stripPackage(retType);

            formatter.appendText(retType);

            return formatter.getText();
        }

        @Override
        public ImageIcon getIcon() {
            if (!isGDK) {
                return (ImageIcon) ElementIcons.getElementIcon(javax.lang.model.element.ElementKind.METHOD,
                        Utilities.reflectionModifiersToModel(method.getModifiers()));
            }

            if (groovyIcon == null) {
                groovyIcon = new ImageIcon(ImageUtilities.loadImage(GroovySources.GROOVY_FILE_ICON_16x16));
            }

            return groovyIcon;
        }

        @Override
        public Set<Modifier> getModifiers() {
            return Collections.emptySet();
        }

        @Override
        public ElementHandle getElement() {

            // to display the documentation box for each element, the completion-
            // element needs to implement this method. Otherwise document(...)
            // won't even be called at all.

            return methodElement;
        }

        @Override
        public String getCustomInsertTemplate() {
            if (nameOnly) {
                return method.getName();
            }
            return super.getCustomInsertTemplate();
        }

    }

    public static class KeywordItem extends CompletionItem {

        private static final String JAVA_KEYWORD   = "org/netbeans/modules/groovy/editor/resources/duke.png"; //NOI18N
        private final String keyword;
        private final String description;
        private final boolean isGroovy;
        private final CompilationInfo info;

        public KeywordItem(String keyword, String description, int anchorOffset, CompilationInfo info, boolean isGroovy) {
            super(null, anchorOffset);
            this.keyword = keyword;
            this.description = description;
            this.info = info;
            this.isGroovy = isGroovy;
        }

        @Override
        public String getName() {
            return keyword;
        }

        @Override
        public ElementKind getKind() {
            return ElementKind.KEYWORD;
        }

        @Override
        public String getRhsHtml(HtmlFormatter formatter) {
            if (description != null) {
                //formatter.appendText(description);
                formatter.appendHtml(description);

                return formatter.getText();
            } else {
                return null;
            }
        }

        @Override
        public ImageIcon getIcon() {

            if (isGroovy) {
                if (groovyIcon == null) {
                    groovyIcon = new ImageIcon(ImageUtilities.loadImage(GroovySources.GROOVY_FILE_ICON_16x16));
                }
                return groovyIcon;
            } else {
                if (javaIcon == null) {
                    javaIcon = new ImageIcon(ImageUtilities.loadImage(JAVA_KEYWORD));
                }
                return javaIcon;
            }
        }

        @Override
        public Set<Modifier> getModifiers() {
            return Collections.emptySet();
        }

        @Override
        public ElementHandle getElement() {
            // For completion documentation
            return ElementHandleSupport.createHandle(info, new KeywordElement(keyword));
        }
    }

    public static class PackageItem extends CompletionItem {

        private final String keyword;

        private final CompilationInfo info;

        public PackageItem(String keyword, int anchorOffset, CompilationInfo info) {
            super(null, anchorOffset);
            this.keyword = keyword;
            this.info = info;
        }

        @Override
        public String getName() {
            return keyword;
        }

        @Override
        public ElementKind getKind() {
            return ElementKind.PACKAGE;
        }

        @Override
        public String getRhsHtml(HtmlFormatter formatter) {
            return null;
        }

        @Override
        public ImageIcon getIcon() {
            return (ImageIcon) ElementIcons.getElementIcon(javax.lang.model.element.ElementKind.PACKAGE, null);
        }

        @Override
        public Set<Modifier> getModifiers() {
            return Collections.emptySet();
        }

        @Override
        public ElementHandle getElement() {
            // For completion documentation
            return ElementHandleSupport.createHandle(info, new KeywordElement(keyword));
        }
    }

    public static class TypeItem extends CompletionItem {

        private final String name;
        private final javax.lang.model.element.ElementKind ek;

        public TypeItem(String name, int anchorOffset, javax.lang.model.element.ElementKind ek) {
            super(null, anchorOffset);
            this.name = name;
            this.ek = ek;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public ElementKind getKind() {
            return ElementKind.CLASS;
        }

        @Override
        public String getRhsHtml(HtmlFormatter formatter) {
            return null;
        }

        @Override
        public ImageIcon getIcon() {
            return (ImageIcon) ElementIcons.getElementIcon(ek, null);
        }

        @Override
        public Set<Modifier> getModifiers() {
            return Collections.emptySet();
        }

        @Override
        public ElementHandle getElement() {
            // For completion documentation
            // return ElementHandleSupport.createHandle(request.info, new ClassElement(name));
            return null;
        }
    }

    public static class ConstructorItem extends CompletionItem {

        private final String name;
        private static final String NEW_CSTR   = "org/netbeans/modules/groovy/editor/resources/new_constructor_16.png"; //NOI18N
        private boolean expand; // should this item expand to a constructor body?
        private final String paramListString;
        private final List<ParameterDescriptor> paramList;

        public ConstructorItem(String name, String paramListString, List<ParameterDescriptor> paramList, int anchorOffset, boolean expand) {
            super(null, anchorOffset);
            this.name = name;
            this.expand = expand;
            this.paramListString = paramListString;
            this.paramList = paramList;
        }

        @Override
        public String getLhsHtml(HtmlFormatter formatter) {
            if (expand) {
                return name + " - generate"; // NOI18N
            } else {
                return name + "(" + paramListString +  ")";
            }
        }

        @Override
        public String getName() {
            if (expand) {
                return name  + "()\n{\n}";
            } else {
                return name;
            }
        }

        @Override
        public ElementKind getKind() {
            return ElementKind.CONSTRUCTOR;
        }

        @Override
        public String getRhsHtml(HtmlFormatter formatter) {
            return null;
        }

        @Override
        public ImageIcon getIcon() {

            if (newConstructorIcon == null) {
                newConstructorIcon = new ImageIcon(ImageUtilities.loadImage(NEW_CSTR));
            }
            return newConstructorIcon;
        }

        @Override
        public Set<Modifier> getModifiers() {
            return Collections.emptySet();
        }

        @Override
        public ElementHandle getElement() {
            // For completion documentation
            // return ElementHandleSupport.createHandle(request.info, new ClassElement(name));
            return null;
        }

        // Constructors are smart by definition (have to be place above others)
        @Override
        public boolean isSmart() {
            return true;
        }

        // See IDE help-topic: "Creating and Customizing Ruby Code Templates" or
        // RubyCodeCompleter.MethodItem.getCustomInsertTemplate() for syntax.
        @Override
        public String getCustomInsertTemplate() {

            StringBuilder sb = new StringBuilder();

            sb.append(getInsertPrefix());
            sb.append("(");

            int id = 1;

            // sb.append("${cursor}"); // NOI18N

            for (ParameterDescriptor paramDesc : paramList) {

                LOG.log(Level.FINEST, "-------------------------------------------------------------------");
                LOG.log(Level.FINEST, "paramDesc.fullTypeName : {0}", paramDesc.getFullTypeName());
                LOG.log(Level.FINEST, "paramDesc.typeName     : {0}", paramDesc.getTypeName());
                LOG.log(Level.FINEST, "paramDesc.name         : {0}", paramDesc.getName());

                sb.append("${"); //NOI18N

                sb.append("groovy-cc-"); // NOI18N
                sb.append(Integer.toString(id));

                sb.append(" default=\""); // NOI18N
                sb.append(paramDesc.getName());
                sb.append("\""); // NOI18N

                sb.append("}"); //NOI18N

                // simply hardcoded values. For testing purposes.
                // sb.append(paramDesc.name);


                if (id < paramList.size()) {
                    sb.append(", "); //NOI18N
                }

                id++;
            }

            sb.append(")");

            LOG.log(Level.FINEST, "Template returned : {0}", sb.toString());
            return sb.toString();

        }
    }

    public static class JavaFieldItem extends CompletionItem {

        private final String className;

        private final String name;

        private final TypeMirror type;

        private final Set<javax.lang.model.element.Modifier> modifiers;

        private final boolean emphasise;

        public JavaFieldItem(String className, String name, TypeMirror type,
                Set<javax.lang.model.element.Modifier> modifiers, int anchorOffset, boolean emphasise) {
            super(null, anchorOffset);
            this.className = className;
            this.name = name;
            this.type = type;
            this.modifiers = modifiers;
            this.emphasise = emphasise;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public ElementKind getKind() {
            return ElementKind.FIELD;
        }

        @Override
        public String getRhsHtml(HtmlFormatter formatter) {
            String retType = "";
            if (type != null) {
                retType = Utilities.getTypeName(type, false).toString();
            }

            formatter.appendText(retType);

            return formatter.getText();
        }

        @Override
        public ImageIcon getIcon() {
            return (ImageIcon) ElementIcons.getElementIcon(
                    javax.lang.model.element.ElementKind.FIELD, modifiers);
        }

        @Override
        public Set<Modifier> getModifiers() {
            return Utilities.modelModifiersToGsf(modifiers);
        }

        @Override
        public ElementHandle getElement() {
            // For completion documentation
            return ElementHandleSupport.createHandle(className, name, ElementKind.FIELD,
                    Utilities.modelModifiersToGsf(modifiers));
        }
    }

    public static class FieldItem extends CompletionItem {

        private final String name;

        private final CompilationInfo info;

        private final String typeName;

        private final int modifiers;

        public FieldItem(String name, int modifiers, int anchorOffset, CompilationInfo info, String typeName) {
            super(null, anchorOffset);
            this.name = name;
            this.info = info;
            this.typeName = typeName;
            this.modifiers = modifiers;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public ElementKind getKind() {
            return ElementKind.FIELD;
        }

        @Override
        public String getRhsHtml(HtmlFormatter formatter) {
            return typeName;
        }

        @Override
        public ImageIcon getIcon() {
            // todo: what happens, if i get a CCE here?
            return (ImageIcon) ElementIcons.getElementIcon(javax.lang.model.element.ElementKind.FIELD,
                    Utilities.reflectionModifiersToModel(modifiers));
        }

        @Override
        public Set<Modifier> getModifiers() {
            return Collections.emptySet();
        }

        @Override
        public ElementHandle getElement() {
            // For completion documentation
            return ElementHandleSupport.createHandle(info, new KeywordElement(name));
        }
    }

    public static class LocalVarItem extends CompletionItem {

        private final Variable var;

        public LocalVarItem(Variable var, int anchorOffset) {
            super(null, anchorOffset);
            this.var = var;
        }

        @Override
        public String getName() {
            return var.getName();
        }

        @Override
        public ElementKind getKind() {
            return ElementKind.VARIABLE;
        }

        @Override
        public String getRhsHtml(HtmlFormatter formatter) {
            return var.getType().getNameWithoutPackage();
        }

        @Override
        public ImageIcon getIcon() {
            // todo: what happens, if i get a CCE here?
            return (ImageIcon) ElementIcons.getElementIcon(javax.lang.model.element.ElementKind.LOCAL_VARIABLE, null);
        }

        @Override
        public Set<Modifier> getModifiers() {
            return Collections.emptySet();
        }

        @Override
        public ElementHandle getElement() {
            return null;
        }
    }

    public static class NewVarItem extends CompletionItem {

        private final String var;

        public NewVarItem(String var, int anchorOffset) {
            super(null, anchorOffset);
            this.var = var;
        }

        @Override
        public String getName() {
            return var;
        }

        @Override
        public ElementKind getKind() {
            return ElementKind.VARIABLE;
        }

        @Override
        public String getRhsHtml(HtmlFormatter formatter) {
            return null;
        }

        @Override
        public ImageIcon getIcon() {
            return (ImageIcon) ElementIcons.getElementIcon(javax.lang.model.element.ElementKind.LOCAL_VARIABLE, null);
        }

        @Override
        public Set<Modifier> getModifiers() {
            return Collections.emptySet();
        }

        @Override
        public ElementHandle getElement() {
            return null;
        }
    }

    // This is from JavaCompletionItem
    public static class ParameterDescriptor {

        private final String fullTypeName;

        private final String typeName;

        private final String name;

        public ParameterDescriptor(String fullTypeName, String typeName, String name) {
            this.fullTypeName = fullTypeName;
            this.typeName = typeName;
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public String getTypeName() {
            return typeName;
        }

        public String getFullTypeName() {
            return fullTypeName;
        }
    }
}

