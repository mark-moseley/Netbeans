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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.php.editor.index;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.netbeans.modules.gsf.api.ElementKind;
import org.netbeans.modules.gsf.api.Modifier;

/**
 *
 * @author Tor Norbye
 */
public class IndexedFunction extends IndexedElement implements FunctionElement {
    private String arguments;
    private String[] args;
    private List<String> parameters;
    
    public IndexedFunction(String name, String in, PHPIndex index, String fileUrl, String arguments, int offset, int flags, ElementKind kind) {
        super(name, in, index, fileUrl, offset, flags, kind);
        this.arguments = arguments;
    }
    
    @Override
    public String toString() {
        return getSignature() + ":" + getFilenameUrl();
    }

    @Override
    public String getSignature() {
        if (textSignature == null) {
            StringBuilder sb = new StringBuilder();
            if (in != null) {
                sb.append(in);
                sb.append('.');
            }
            sb.append(name);
            sb.append("(");
            List<String> parameterList = getParameters();
            if (parameterList.size() > 0) {
                for (int i = 0, n = parameterList.size(); i < n; i++) {
                    if (i > 0) {
                        sb.append(",");
                    }
                    sb.append(parameterList.get(i));
                }
            }
            sb.append(")");
            textSignature = sb.toString();
        }

        return textSignature;
    }

    public String[] getArgs() {
        if (args == null) {
            if(arguments == null || arguments.length() == 0) {
                return new String[0];
            } else {
                args = arguments.split(","); // NOI18N
            }
        }

        return args;
    }

    public List<String> getParameters() {
        if (parameters == null) {
            String[] a = getArgs();

            if ((a != null) && (a.length > 0)) {
                parameters = new ArrayList<String>(a.length);

                for (String arg : a) {
                    parameters.add(arg);
                }
            } else {
                parameters = Collections.emptyList();
            }
        }

        return parameters;
    }

    public boolean isDeprecated() {
        return false;
    }

    @Override
    public String getDisplayName() {
        String modifierStr = getModifiersString();
        return modifierStr.length() == 0 ? getSignature() : (getModifiersString() + " " + getSignature());
    }

    @Override
    public Set<Modifier> getModifiers() {
        
        
        
        return super.getModifiers();
    }
    
    
}
