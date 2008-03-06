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

package org.netbeans.modules.groovy.editor.elements;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.MethodNode;
import org.netbeans.modules.gsf.api.Element;
import org.netbeans.modules.gsf.api.ElementKind;
import org.netbeans.modules.gsf.api.Modifier;
import org.netbeans.modules.gsf.api.ElementHandle;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Martin Adamek
 */
public abstract class AstElement implements Element, ElementHandle {

    protected final ASTNode node;
    protected List<AstElement> children;
    protected String name;
    protected Set<Modifier> modifiers;
    private String in;
    
    public AstElement(ASTNode node) {
        this.node = node;
    }
    
    public List<AstElement> getChildren() {
        if (children == null) {
            return Collections.<AstElement>emptyList();
        }

        return children;
    }

    public void addChild(AstElement child) {
        if (children == null) {
            children = new ArrayList<AstElement>();
        }

        children.add(child);
    }

    public abstract String getName();

    public ASTNode getNode() {
        return node;
    }
    
    public String getIn() {
        return in;
    }

    public ElementKind getKind() {
        return ElementKind.OTHER;
    }

    public Set<Modifier> getModifiers() {
        return Collections.<Modifier>emptySet();
    }

    public void setIn(String in) {
        this.in = in;
    }
    
    public boolean signatureEquals (final ElementHandle handle) {
        if (handle instanceof AstElement) {
                return this.equals(handle);
            }
        return false;
    }
    
    // FIXME: This is an empty implementations to make a 
    // AstElement a ElementHandle. Seems not to affect others. Sure?
    
    public FileObject getFileObject() {
        return null;
    }
    
    public String getMimeType() {
        return "text/x-groovy"; // NOI18N
    }
    

    public static AstElement create(ASTNode node) {
        if (node instanceof MethodNode) {
            return new AstMethodElement(node);
        }
        return null;
    }

}
