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
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementUtilities;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.ui.ElementOpen;
import org.netbeans.modules.spring.beans.editor.BeanClassFinder;
import org.netbeans.modules.spring.beans.editor.ContextUtilities;
import org.netbeans.modules.spring.java.JavaUtils;
import org.netbeans.modules.spring.java.Property;
import org.netbeans.modules.spring.java.PropertyFinder;
import org.openide.util.Exceptions;

/**
 * Hyperlink Processor for p-namespace stuff. Delegates to beanref processor
 * and property processor for computation
 *
 * @author Rohan Ranade (Rohan.Ranade@Sun.COM)
 */
public class PHyperlinkProcessor extends HyperlinkProcessor {

    private BeansRefHyperlinkProcessor beansRefHyperlinkProcessor
            = new BeansRefHyperlinkProcessor(true);

    public PHyperlinkProcessor() {
    }

    public void process(HyperlinkEnv env) {
        String attribName = env.getAttribName();
        if(env.getType().isValueHyperlink()) {
            if(attribName.endsWith("-ref")) { // NOI18N
                beansRefHyperlinkProcessor.process(env);
            }
        } else if(env.getType().isAttributeHyperlink()) {
            String temp = ContextUtilities.getLocalNameFromTag(attribName);
            if(temp.endsWith("-ref")) { // NOI18N
                temp = temp.substring(0, temp.indexOf("-ref")); // NOI18N
            }

            final String className = new BeanClassFinder(env.getBeanAttributes(), 
                    env.getFileObject()).findImplementationClass();
            if(className == null) {
                return;
            }
            
            JavaSource js = JavaUtils.getJavaSource(env.getFileObject());
            if(js == null) {
                return;
            }
            final String propName = temp;
            try {
                js.runUserActionTask(new Task<CompilationController>() {

                    public void run(CompilationController cc) throws Exception {
                        ElementUtilities eu = cc.getElementUtilities();
                        TypeElement type = JavaUtils.findClassElementByBinaryName(className, cc);
                        Property[] props = new PropertyFinder(type.asType(), propName, eu).findProperties();
                        if(props.length > 0 && props[0].getSetter() != null) {
                            ElementOpen.open(cc.getClasspathInfo(), props[0].getSetter());
                        }
                    }
                }, true);
            } catch (IOException ioe) {
                Exceptions.printStackTrace(ioe);
            }
            
        }
    }

    @Override
    public int[] getSpan(HyperlinkEnv env) {
        if(env.getType().isValueHyperlink()) {
            return super.getSpan(env);
        }
        
        if(env.getType().isAttributeHyperlink()) {
            return new int[] { env.getTokenStartOffset(), env.getTokenEndOffset() };
        }
        
        return null;
    }
    
    
}
