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

package org.netbeans.modules.openide.awt;

import java.awt.event.ActionListener;
import java.util.Set;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import org.openide.awt.ActionRegistration;
import org.openide.filesystems.annotations.LayerBuilder.File;
import org.openide.filesystems.annotations.LayerGeneratingProcessor;
import org.openide.filesystems.annotations.LayerGenerationException;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
@ServiceProvider(service=Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_5)
@SupportedAnnotationTypes("org.openide.awt.ActionRegistration") // NOI18N
public final class ActionProcessor extends LayerGeneratingProcessor {
    @Override
    protected boolean handleProcess(
        Set<? extends TypeElement> annotations, RoundEnvironment roundEnv
    ) throws LayerGenerationException {
        for (Element e : roundEnv.getElementsAnnotatedWith(ActionRegistration.class)) {
            ActionRegistration ar = e.getAnnotation(ActionRegistration.class);
            String id = ar.id();
            if (id.length() == 0) {
                id = processingEnv.getElementUtils().getBinaryName((TypeElement)e).toString()
                        .replace('.', '-');
            }
            File f = layer(e).file("Actions/" + ar.category() + "/" + id + ".instance");
            f.bundlevalue("displayName", ar.displayName());
            if (ar.key().length() == 0) {
                f.methodvalue("instanceCreate", "org.openide.awt.Actions", "alwaysEnabled");
            } else {
                f.methodvalue("instanceCreate", "org.openide.awt.Actions", "callback");
                f.methodvalue("fallback", "org.openide.awt.Actions", "alwaysEnabled");
                f.stringvalue("key", ar.key());
            }
            f.instanceAttribute("delegate", ActionListener.class);
            f.boolvalue("noIconInMenu", !ar.iconInMenu());
            f.write();
        }
        return true;
    }

}
