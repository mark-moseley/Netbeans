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

package org.netbeans.modules.compapp.javaee.sunresources.tool.annotation;

import org.netbeans.modules.classfile.Annotation;
import org.netbeans.modules.classfile.AnnotationComponent;
import org.netbeans.modules.classfile.ArrayElementValue;
import org.netbeans.modules.classfile.CPEntry;
import org.netbeans.modules.classfile.ElementValue;
import org.netbeans.modules.classfile.NestedElementValue;
import org.netbeans.modules.classfile.PrimitiveElementValue;

/**
 *
 * @author echou
 */
public class MessageDrivenAnnoWrapper extends AnnotationWrapperBase {
    
    private ActivationConfigPropertyAnnoWrapper[] activationConfig;
    private String description;
    private String mappedName;
    private String messageListenerInterface;
    private String name;
    
    /** Creates a new instance of MessageDrivenAnnoWrapper */
    public MessageDrivenAnnoWrapper(Annotation anno) {
        initActivationConfig(anno);
        this.description = getStringValue(anno, "description"); // NOI18N
        this.mappedName = getStringValue(anno, "mappedName"); // NOI18N
        this.messageListenerInterface = getClassValue(anno, "messageListenerInterface"); // NOI18N
        this.name = getStringValue(anno, "name"); // NOI18N
    }
    
    private void initActivationConfig(Annotation anno) {
        AnnotationComponent ac = anno.getComponent("activationConfig"); // NOI18N
        if (ac == null) {
            this.activationConfig = new ActivationConfigPropertyAnnoWrapper[] {};
        }
        ArrayElementValue aev = (ArrayElementValue) ac.getValue();
        ElementValue[] evs = aev.getValues();
        this.activationConfig = new ActivationConfigPropertyAnnoWrapper[evs.length];
        for (int i = 0; i < evs.length; i++) {
            NestedElementValue nev = (NestedElementValue) evs[i];
            this.activationConfig[i] = 
                    new ActivationConfigPropertyAnnoWrapper(nev.getNestedValue());
        }
    }
    
    public  ActivationConfigPropertyAnnoWrapper[] activationConfig() {
        return this.activationConfig;
    }
    public String description() {
        return this.description;
    }
    public String mappedName() {
        return this.mappedName;
    }
    public String messageListenerInterface() {
        return this.messageListenerInterface;
    }
    public String name() {
        return this.name;
    }
}
