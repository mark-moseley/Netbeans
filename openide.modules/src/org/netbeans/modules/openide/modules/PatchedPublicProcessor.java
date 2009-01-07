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

package org.netbeans.modules.openide.modules;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic.Kind;
import javax.tools.StandardLocation;
import org.openide.modules.PatchedPublic;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service=Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_6)
@SupportedAnnotationTypes("org.openide.modules.PatchedPublic")
public class PatchedPublicProcessor extends AbstractProcessor {

    private List<Element> originatingElements;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        originatingElements = new ArrayList<Element>();
    }

    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (roundEnv.processingOver()) {
            if (!originatingElements.isEmpty()) {
                try {
                    processingEnv.getFiler().createResource(
                            StandardLocation.CLASS_OUTPUT,
                            "", "META-INF/.bytecodePatched",
                            originatingElements.toArray(new Element[originatingElements.size()])).
                            openOutputStream().close();
                } catch (IOException x) {
                    processingEnv.getMessager().printMessage(Kind.ERROR, x.getMessage());
                }
            }
            return false;
        }
        for (Element e : roundEnv.getElementsAnnotatedWith(PatchedPublic.class)) {
            if (e.getAnnotationMirrors().size() > 1) {
                processingEnv.getMessager().printMessage(Kind.ERROR, "Cannot currently mix @PatchedPublic with other annotations", e);
                continue;
            }
            if (e.getModifiers().contains(Modifier.PUBLIC)) {
                processingEnv.getMessager().printMessage(Kind.ERROR, "@PatchedPublic cannot be applied to what is already public", e);
                continue;
            }
            originatingElements.add(e);
        }
        return true;
    }

}
