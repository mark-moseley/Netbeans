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
package org.netbeans.modules.cnd.refactoring.codegen.ui;

import java.awt.Image;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import javax.swing.Action;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmVisibility;
import org.netbeans.modules.cnd.modelutil.CsmImageLoader;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.lookup.Lookups;

/** Node representing an Element
 *
 * @author Petr Hrebejk, Jan Lahoda, Dusan Balek
 * @author Vladimir Voskresensky
 */
public class ElementNode extends AbstractNode {

    private Description description;
    private boolean singleSelection;

    /** Creates a new instance of TreeNode */
    public ElementNode(Description description) {
        this(description, false);
    }

    private ElementNode(Description description, boolean sortChildren) {
        super(description.subs == null ? Children.LEAF : new ElementChilren(description.subs, sortChildren), Lookups.singleton(description));
        this.description = description;
        description.node = this;
        setDisplayName(description.name);
    }

    public void setSingleSelection(boolean singleSelection) {
        this.singleSelection = singleSelection;
    }

    @Override
    public Image getIcon(int type) {
        if (description.elementHandle == null) {
            return super.getIcon(type);
        }
        return ImageUtilities.icon2Image(CsmImageLoader.getIcon(description.elementHandle));
    }

    @Override
    public Image getOpenedIcon(int type) {
        return getIcon(type);
    }

    @Override
    public java.lang.String getDisplayName() {
        return description.name;
    }

    @Override
    public String getHtmlDisplayName() {
        return description.htmlHeader;
    }
    private static final Action[] EMPTY_ACTIONS = new Action[0];

    @Override
    public Action[] getActions(boolean context) {
        return EMPTY_ACTIONS;
    }

    public void assureSingleSelection() {
        Node pn = getParentNode();
        if (pn == null && singleSelection) {
            description.deepSetSelected(false);
        } else if (pn != null) {
            Description d = pn.getLookup().lookup(Description.class);
            if (d != null) {
                d.node.assureSingleSelection();
            }
        }
    }

    private static final class ElementChilren extends Children.Keys<Description> {

        public ElementChilren(List<Description> descriptions, boolean sortChildren) {
            if (sortChildren) {
                Collections.sort(descriptions, Description.ALPHA_COMPARATOR);
            }

            setKeys(descriptions);
        }

        protected Node[] createNodes(Description key) {
            return new Node[]{new ElementNode(key, true)};
        }
    }

    /** Stores all interesting data about given element.
     */
    public static class Description {

        public static final Comparator<Description> ALPHA_COMPARATOR = new DescriptionComparator();
        private ElementNode node;
        private String name;
        private CsmDeclaration elementHandle;
        private Set<CsmVisibility> modifiers;
        private List<Description> subs;
        private String htmlHeader;
        private boolean isSelected;
        private boolean isSelectable;

        public static Description create(List<Description> subs) {
            return new Description("<root>", null, null, subs, null, false, false); // NOI18N
        }

        public static Description create(CsmDeclaration element, List<Description> subs, boolean isSelectable, boolean isSelected) {
            return null;
//            String htmlHeader = null;
//            switch (element.getKind()) {
//                case ANNOTATION_TYPE:
//                case CLASS:
//                case ENUM:
//                case INTERFACE:
//                    htmlHeader = createHtmlHeader((TypeElement) element);
//                    break;
//                case ENUM_CONSTANT:
//                case FIELD:
//                    htmlHeader = createHtmlHeader((VariableElement) element);
//                    break;
//                case CONSTRUCTOR:
//                case METHOD:
//                    htmlHeader = createHtmlHeader((ExecutableElement) element);
//                    break;
//            }
//            return new Description(element.getSimpleName().toString(),
//                    ElementHandle.create(element),
//                    element.getModifiers(),
//                    subs,
//                    htmlHeader,
//                    isSelectable,
//                    isSelected);
        }

        private Description(String name, CsmDeclaration elementHandle,
                Set<CsmVisibility> modifiers, List<Description> subs, String htmlHeader,
                boolean isSelectable, boolean isSelected) {
            this.name = name;
            this.elementHandle = elementHandle;
            this.modifiers = modifiers;
            this.subs = subs;
            this.htmlHeader = htmlHeader;
            this.isSelectable = isSelectable;
            this.isSelected = isSelected;
        }

        public boolean isSelectable() {
            return isSelectable;
        }

        public boolean hasSelectableSubs() {
            if (null == subs) {
                return false;
            }
            for (Description d : getSubs()) {
                if (d.isSelectable()) {
                    return true;
                }
            }
            return false;
        }

        public boolean isSelected() {
            return isSelected;
        }

        public List<Description> getSubs() {
            return subs;
        }

        public void setSelected(boolean selected) {

            if (selected == true && node != null) {
                node.assureSingleSelection();
            }

            this.isSelected = selected;
            if (node != null) {       // notity the node                
                node.fireDisplayNameChange(null, null);
            }
        }

        public void deepSetSelected(boolean value) {

            if (isSelectable() && value != isSelected()) {
                setSelected(value);
            }

            if (subs != null) {
                for (Description s : subs) {
                    s.deepSetSelected(value);
                }
            }
        }

        public CsmDeclaration getElementHandle() {
            return elementHandle;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Description)) {
                return false;
            }
            Description d = (Description) o;
            if (!this.name.equals(d.name)) {
                return false;
            }
            if (this.elementHandle != d.elementHandle) {
                if (this.elementHandle == null || d.elementHandle == null) {
                    return false;
                }
                if (this.elementHandle.getKind() != d.elementHandle.getKind()) {
                    return false;
                }
                if (!this.elementHandle.equals(d.elementHandle)) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 29 * hash + (this.name != null ? this.name.hashCode() : 0);
            hash = 29 * hash + (this.elementHandle != null ? this.elementHandle.getKind().hashCode() : 0);
            return hash;
        }

        public String getName() {
            return name;
        }

        public static Description deepCopy(Description d) {

            List<Description> subsCopy;

            if (d.subs == null) {
                subsCopy = null;
            } else {
                subsCopy = new ArrayList<Description>(d.subs.size());
                for (Description s : d.subs) {
                    subsCopy.add(deepCopy(s));
                }
            }

            return new Description(d.name, d.elementHandle, d.modifiers, subsCopy,
                    d.htmlHeader, d.isSelectable, d.isSelected);

        }

//        private static String createHtmlHeader(ExecutableElement e) {
//            StringBuilder sb = new StringBuilder();
//            if (e.getKind() == ElementKind.CONSTRUCTOR) {
//                sb.append(e.getEnclosingElement().getSimpleName());
//            } else {
//                sb.append(e.getSimpleName());
//            }
//            sb.append("("); // NOI18N
//            for (Iterator<? extends VariableElement> it = e.getParameters().iterator(); it.hasNext();) {
//                VariableElement param = it.next();
//                if (!it.hasNext() && e.isVarArgs() && param.asType().getKind() == TypeKind.ARRAY) {
//                    sb.append(print(((ArrayType) param.asType()).getComponentType()));
//                    sb.append("...");
//                } else {
//                    sb.append(print(param.asType()));
//                }
//                sb.append(" "); // NOI18N
//                sb.append(param.getSimpleName());
//                if (it.hasNext()) {
//                    sb.append(", "); // NOI18N
//                }
//            }
//            sb.append(")"); // NOI18N
//            if (e.getKind() != ElementKind.CONSTRUCTOR) {
//                TypeMirror rt = e.getReturnType();
//                if (rt.getKind() != TypeKind.VOID) {
//                    sb.append(" : "); // NOI18N
//                    sb.append(print(e.getReturnType()));
//                }
//            }
//            return sb.toString();
//        }
//
//        private static String createHtmlHeader(VariableElement e) {
//            StringBuilder sb = new StringBuilder();
//            sb.append(e.getSimpleName());
//            if (e.getKind() != ElementKind.ENUM_CONSTANT) {
//                sb.append(" : "); // NOI18N
//                sb.append(print(e.asType()));
//            }
//            return sb.toString();
//        }
//
//        private static String createHtmlHeader(TypeElement e) {
//            StringBuilder sb = new StringBuilder();
//            sb.append(e.getSimpleName());
//            List<? extends TypeParameterElement> typeParams = e.getTypeParameters();
//            if (typeParams != null && !typeParams.isEmpty()) {
//                sb.append("<"); // NOI18N
//                for (Iterator<? extends TypeParameterElement> it = typeParams.iterator(); it.hasNext();) {
//                    TypeParameterElement tp = it.next();
//                    sb.append(tp.getSimpleName());
//                    try {
//                        List<? extends TypeMirror> bounds = tp.getBounds();
//                        if (!bounds.isEmpty()) {
//                            sb.append(printBounds(bounds));
//                        }
//                    } catch (NullPointerException npe) {
//                    }
//                    if (it.hasNext()) {
//                        sb.append(", "); // NOI18N
//                    }
//                }
//                sb.append(">"); // NOI18N
//            }
//            return sb.toString();
//        }

//        private static String printBounds(List<? extends TypeMirror> bounds) {
//            if (bounds.size() == 1 && "java.lang.Object".equals(bounds.get(0).toString())) // NOI18N
//            {
//                return "";
//            }
//            StringBuilder sb = new StringBuilder();
//            sb.append(" extends "); // NOI18N
//            for (Iterator<? extends TypeMirror> it = bounds.iterator(); it.hasNext();) {
//                TypeMirror bound = it.next();
//                sb.append(print(bound));
//                if (it.hasNext()) {
//                    sb.append(" & "); // NOI18N
//                }
//            }
//            return sb.toString();
//        }
//
//        private static String print(TypeMirror tm) {
//            StringBuilder sb;
//            switch (tm.getKind()) {
//                case DECLARED:
//                    DeclaredType dt = (DeclaredType) tm;
//                    sb = new StringBuilder(dt.asElement().getSimpleName().toString());
//                    List<? extends TypeMirror> typeArgs = dt.getTypeArguments();
//                    if (!typeArgs.isEmpty()) {
//                        sb.append("<"); // NOI18N
//                        for (Iterator<? extends TypeMirror> it = typeArgs.iterator(); it.hasNext();) {
//                            TypeMirror ta = it.next();
//                            sb.append(print(ta));
//                            if (it.hasNext()) {
//                                sb.append(", "); // NOI18N
//                            }
//                        }
//                        sb.append(">"); // NOI18N
//                    }
//                    return sb.toString();
//                case TYPEVAR:
//                    TypeVariable tv = (TypeVariable) tm;
//                    sb = new StringBuilder(tv.asElement().getSimpleName().toString());
//                    return sb.toString();
//                case ARRAY:
//                    ArrayType at = (ArrayType) tm;
//                    sb = new StringBuilder(print(at.getComponentType()));
//                    sb.append("[]"); // NOI18N
//                    return sb.toString();
//                case WILDCARD:
//                    WildcardType wt = (WildcardType) tm;
//                    sb = new StringBuilder("?"); // NOI18N
//                    if (wt.getExtendsBound() != null) {
//                        sb.append(" extends "); // NOI18N
//                        sb.append(print(wt.getExtendsBound()));
//                    }
//                    if (wt.getSuperBound() != null) {
//                        sb.append(" super "); // NOI18N
//                        sb.append(print(wt.getSuperBound()));
//                    }
//                    return sb.toString();
//                default:
//                    return tm.toString();
//            }
//        }
        private static class DescriptionComparator implements Comparator<Description> {

            public int compare(Description d1, Description d2) {

                if (k2i(d1.elementHandle.getKind()) != k2i(d2.elementHandle.getKind())) {
                    return k2i(d1.elementHandle.getKind()) - k2i(d2.elementHandle.getKind());
                }

                return d1.name.compareTo(d2.name);
            }

            int k2i(CsmDeclaration.Kind kind) {
                switch (kind) {
                    case FUNCTION:
                        return 1;
                    case FUNCTION_DEFINITION:
                        return 2;
                    case VARIABLE:
                    case VARIABLE_DEFINITION:
                        return 3;
                    case CLASS:
                        return 4;
                    case STRUCT:
                        return 5;
                    case UNION:
                        return 6;
                    case ENUM:
                        return 7;
                    default:
                        return 100;
                }
            }
        }
    }
}
