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
package org.netbeans.modules.spring.beans.hyperlink;

import java.io.IOException;
import java.util.StringTokenizer;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementUtilities;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.ui.ElementOpen;
import org.netbeans.editor.TokenItem;
import org.netbeans.modules.spring.beans.editor.BeanClassFinder;
import org.netbeans.modules.spring.beans.editor.Property;
import org.netbeans.modules.spring.beans.editor.PropertyFinder;
import org.netbeans.modules.spring.beans.editor.SpringXMLConfigEditorUtils;
import org.openide.util.Exceptions;

/**
 *
 * @author Rohan Ranade (Rohan.Ranade@Sun.COM)
 */
public class PropertyHyperlinkProcessor extends HyperlinkProcessor {

    public PropertyHyperlinkProcessor() {
    }

    public void process(HyperlinkEnv env) {
        try {
            final String className = new BeanClassFinder(
                                SpringXMLConfigEditorUtils.getBean(env.getCurrentTag()), 
                                env.getFile()).findImplementationClass();
            if (className == null) {
                return;
            }

            final String propChain = getPropertyChainUptoPosition(env);
            if (propChain == null || propChain.equals("")) { // NOI18N
                return;
            }

            JavaSource js = SpringXMLConfigEditorUtils.getJavaSource(env.getDocument());
            if (js == null) {
                return;
            }

            final int dotIndex = propChain.lastIndexOf(".");
            js.runUserActionTask(new Task<CompilationController>() {

                public void run(CompilationController cc) throws Exception {
                    TypeElement te = SpringXMLConfigEditorUtils.findClassElementByBinaryName(className, cc);
                    if (te == null) {
                        return;
                    }
                    TypeMirror startType = te.asType();
                    ElementUtilities eu = cc.getElementUtilities();

                    // property chain
                    if (dotIndex != -1) {
                        String getterChain = propChain.substring(0, dotIndex);
                        StringTokenizer tokenizer = new StringTokenizer(getterChain, "."); // NOI18N
                        while (tokenizer.hasMoreTokens() && startType != null) {
                            String propertyName = tokenizer.nextToken();
                            Property[] props = new PropertyFinder(startType, propertyName, eu).findProperties();

                            // no matching element found
                            if (props.length == 0 || props[0].getGetter() == null) {
                                startType = null;
                                break;
                            }

                            TypeMirror retType = props[0].getGetter().getReturnType();
                            if (retType.getKind() == TypeKind.DECLARED) {
                                startType = retType;
                            } else {
                                startType = null;
                            }
                        }
                    }

                    if (startType == null) {
                        return;
                    }

                    String setterProp = propChain.substring(dotIndex + 1);
                    Property[] sProps = new PropertyFinder(startType, setterProp, eu).findProperties();
                    if (sProps.length > 0 && sProps[0].getSetter() != null) {
                        ElementOpen.open(cc.getClasspathInfo(), sProps[0].getSetter());
                    }
                }
            }, true);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    @Override
    public int[] getSpan(HyperlinkEnv env) {
        TokenItem tok = env.getToken();
        int addOffset = tok.getOffset() + 1;
        
        String propChain = getPropertyChainUptoPosition(env);
        if(propChain == null || propChain.equals("")) { // NOI18N
            return null;
        }
        
        int endPos = tok.getOffset() + propChain.length() + 1;
        int startPos = propChain.lastIndexOf("."); // NOI18N
        startPos = (startPos == -1) ? 0 : ++startPos;
        startPos += addOffset;
        
        return new int[] { startPos, endPos };
    }

    private String getPropertyChainUptoPosition(HyperlinkEnv env) {
        TokenItem tok = env.getToken();
        int relOffset = env.getOffset() - tok.getOffset() - 1;
        
        int endPos = env.getValueString().indexOf(".", relOffset); // NOI18N
        // no . after the current pos, return full string
        if(endPos == -1) {
            return env.getValueString();
        } else {
            return env.getValueString().substring(0, endPos);
        }
    }
}
