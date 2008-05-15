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

package org.netbeans.modules.css.gsf;

import java.util.Collections;
import java.util.Set;
import org.netbeans.modules.css.editor.Css;
import org.netbeans.modules.css.parser.SimpleNode;
import org.netbeans.modules.gsf.api.CompilationInfo;
import org.netbeans.modules.gsf.api.ElementHandle;
import org.netbeans.modules.gsf.api.ElementKind;
import org.netbeans.modules.gsf.api.Modifier;
import org.openide.filesystems.FileObject;

/**
 *
 * @author marek
 */
public class CssElementHandle implements ElementHandle {

    private String selectorListText;
    private int elementAstStartOffset, elementAstEndOffset;
    private CompilationInfo ci;
    
    CssElementHandle(SimpleNode ruleNode, SimpleNode selectorListNode, CompilationInfo ci) {
        this.ci = ci;
        this.selectorListText = selectorListNode.image();
        this.elementAstStartOffset = ruleNode.startOffset();
        this.elementAstEndOffset = ruleNode.endOffset();
    }
    
    public FileObject getFileObject() {
        return ci.getFileObject();
    }

    public String getMimeType() {
        return Css.CSS_MIME_TYPE;
    }

    public String getName() {
        return selectorListText;
    }

    //XXX what's that????
    public String getIn() {
        return null;
    }

    public ElementKind getKind() {
        return ElementKind.FIELD;
    }

    public Set<Modifier> getModifiers() {
        return Collections.emptySet();
    }

    public boolean signatureEquals(ElementHandle handle) {
        //TODO implement
        return false;
    }

    public int elementAstStartOffset() {
        return elementAstStartOffset;
    }
    
    public int elementAstEndOffset() {
        return elementAstEndOffset;
    }
    
    public CompilationInfo compilationInfo() {
        return ci;
    }
    
}
