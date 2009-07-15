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
package org.netbeans.modules.php.editor.model;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.modules.php.editor.model.nodes.NamespaceDeclarationInfo;
import org.netbeans.modules.php.editor.parser.astnodes.Expression;
import org.netbeans.modules.php.editor.parser.astnodes.Identifier;
import org.netbeans.modules.php.editor.parser.astnodes.NamespaceName;
import org.openide.util.Parameters;

/**
 * immutable
 * @author Radek Matous
 */
public class QualifiedName {

    private final QualifiedNameKind kind;
    private final LinkedList<String> segments;

    public static QualifiedName createUnqualifiedNameInClassContext(Expression expression, ClassScope clsScope) {
        if (expression instanceof Identifier) {
            return createUnqualifiedNameInClassContext((Identifier) expression, clsScope);
        } else if (expression instanceof NamespaceName) {
            NamespaceName namespaceName = (NamespaceName) expression;
            if (namespaceName.getSegments().size() == 1 && !namespaceName.isGlobal()) {
                return createUnqualifiedNameInClassContext(namespaceName.getSegments().get(0).getName(), clsScope);
            }
        }
        return create(expression) ;
    }
    @CheckForNull
    public static QualifiedName create(Expression expression) {
        if (expression instanceof NamespaceName) {
            return create((NamespaceName)expression);
        } else if (expression instanceof Identifier) {
            return createUnqualifiedName((Identifier)expression);
        }
        return null;
    }
    public static QualifiedName create(NamespaceScope namespaceScope) {
        final String[] segments = namespaceScope.getName().split("\\\\");//NOI18N
        return new QualifiedName(false,Arrays.asList(segments));
    }
    public static QualifiedName create(NamespaceName namespaceName) {
        return new QualifiedName(namespaceName);
    }
    public static QualifiedName createUnqualifiedNameInClassContext(Identifier identifier, ClassScope clsScope) {
        return createUnqualifiedNameInClassContext(identifier.getName(), clsScope);
    }
    public static QualifiedName createUnqualifiedName(Identifier identifier) {
        return new QualifiedName(identifier);
    }
    public static QualifiedName createUnqualifiedNameInClassContext(String name, ClassScope clsScope) {
        //TODO: everywhere should be used NameKindMatcher or something like this
        if (clsScope != null) {
            if ("self".equals(name)) {//NOI18N
                name = clsScope.getName();
            } else if ("parent".equals(name)) {//NOI18N
                String superClsName = ModelUtils.getFirst(clsScope.getSuperClassNames());
                if (superClsName != null) {
                  name = superClsName;
                }
            }
        }
        return createUnqualifiedName(name);
    }

    public static QualifiedName createForDefaultNamespaceName() {
        return QualifiedName.createUnqualifiedName(NamespaceDeclarationInfo.DEFAULT_NAMESPACE_NAME);
    }
    public static QualifiedName createUnqualifiedName(String name) {
        QualifiedNameKind kind = QualifiedNameKind.resolveKind(name);
        assert kind.equals(QualifiedNameKind.UNQUALIFIED);
        return new QualifiedName(false, Collections.singletonList(name));
    }
    public static QualifiedName create(String name) {
        QualifiedNameKind kind = QualifiedNameKind.resolveKind(name);
        if (kind.isUnqualified()) {
            return createUnqualifiedName(name);
        }
        final String[] segments = name.split("\\\\");//NOI18N
        return new QualifiedName(kind.isFullyQualified(),Arrays.asList(segments));
    }
    private QualifiedName(NamespaceName namespaceName) {
        this.kind = QualifiedNameKind.resolveKind(namespaceName);
        segments = new LinkedList<String>();
        for (Identifier identifier : namespaceName.getSegments()) {
            segments.add(identifier.getName());
        }
    }
    private QualifiedName(Identifier identifier) {
        this.kind = QualifiedNameKind.resolveKind(identifier);
        segments = new LinkedList<String>(Collections.singleton(identifier.getName()));
        assert kind.isUnqualified();
    }
    private QualifiedName(boolean isFullyQualified, List<String> segments) {
        this.segments = new LinkedList<String>(segments.size() == 0 ?
            Collections.singleton(NamespaceDeclarationInfo.DEFAULT_NAMESPACE_NAME) : segments);
        this.kind = isFullyQualified ? QualifiedNameKind.FULLYQUALIFIED : QualifiedNameKind.resolveKind(this.segments);
    }
    public LinkedList<String> getSegments() {
        return this.segments;
    }
    /**
     * @return the kind
     */
    public QualifiedNameKind getKind() {
        return kind;
    }
    /**
     * @return the internalName
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        QualifiedNameKind k = getKind();
        for (String oneSegment : segments) {
            if (sb.length() > 0 || (k != null && k.isFullyQualified())) {
                sb.append("\\");//NOI18N
            }
            sb.append(oneSegment);
        }
        return sb.toString();
    }
    public QualifiedName append(String name) {
        return append(createUnqualifiedName(name));
    }
    public QualifiedName append(QualifiedName qualifiedName) {
        return append(qualifiedName, false);
    }
    private  QualifiedName append(QualifiedName qualifiedName, boolean isFullyQualified) {
        LinkedList<String> list = isDefaultNamespace() ? new LinkedList<String>() : new LinkedList<String>(getSegments());
        list.addAll(qualifiedName.getSegments());
        return new QualifiedName(isFullyQualified, list);
    }
    public QualifiedName toFullyQualified() {
        return (getKind().isFullyQualified()) ? this : new QualifiedName(true, getSegments());
    }
    @CheckForNull
    public QualifiedName toFullyQualified(QualifiedName namespaceName) {
        Parameters.notNull("namespaceName", namespaceName);//NOI18N
        return namespaceName.append(this, true);
    }
    @CheckForNull
    public QualifiedName toFullyQualified(NamespaceScope namespaceScope) {
        Parameters.notNull("namespaceScope", namespaceScope);//NOI18N
        return (getKind().isFullyQualified()) ? this : namespaceScope.getQualifiedName().append(this).toFullyQualified();
    }
    public QualifiedName toName() {
        return createUnqualifiedName(getSegments().getLast());
    }
    public QualifiedName toNamespaceName() {
        LinkedList<String> list = new LinkedList<String>(getSegments());
        list.removeLast();
        return new QualifiedName(false, list);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final QualifiedName other = (QualifiedName) obj;
        if (this.kind != other.kind) {
            return false;
        }
        if (this.segments.size() != other.segments.size()) {
            return false;
        }
        return this.segments.equals(other.segments);
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 73 * hash + this.kind.hashCode();
        hash = 73 * hash + (this.segments != null ? this.segments.hashCode() : 0);
        return hash;
    }

    public boolean isDefaultNamespace() {
        return getSegments().size() == 1 && getSegments().get(0).equals(NamespaceDeclarationInfo.DEFAULT_NAMESPACE_NAME);
    }

}
