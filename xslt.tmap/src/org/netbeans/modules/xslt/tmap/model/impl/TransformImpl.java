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
package org.netbeans.modules.xslt.tmap.model.impl;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xml.xam.Reference;
import org.netbeans.modules.xslt.tmap.model.api.BooleanType;
import org.netbeans.modules.xslt.tmap.model.api.Invoke;
import org.netbeans.modules.xslt.tmap.model.api.Operation;
import org.netbeans.modules.xslt.tmap.model.api.Param;
import org.netbeans.modules.xslt.tmap.model.api.TMapAttributes;
import org.netbeans.modules.xslt.tmap.model.api.TMapComponent;
import org.netbeans.modules.xslt.tmap.model.api.TMapReference;
import org.netbeans.modules.xslt.tmap.model.api.TMapVisitor;
import org.netbeans.modules.xslt.tmap.model.api.Transform;
import org.netbeans.modules.xslt.tmap.model.api.TransformerDescriptor;
import org.netbeans.modules.xslt.tmap.model.api.Variable;
import org.netbeans.modules.xslt.tmap.model.api.VariableReference;
import org.netbeans.modules.xslt.tmap.model.api.WSDLReference;
import org.w3c.dom.Element;

/**
 *
 * @author Vitaly Bychkov
 * @version 1.0
 */
public class TransformImpl extends TMapComponentContainerImpl 
    implements Transform 
{

    public TransformImpl(TMapModelImpl model) {
        this(model, createNewElement(TMapComponents.TRANSFORM, model));
    }

    public TransformImpl(TMapModelImpl model, Element element) {
        super(model, element);
    }

    public void accept(TMapVisitor visitor) {
        visitor.visit(this);
    }

    public Class<? extends TMapComponent> getComponentType() {
        return Transform.class;
    }

    public String getFile() {
        return getAttribute(TMapAttributes.FILE);
    }

    public void setFile(String locationURI) {
        setAttribute(Transform.FILE, TMapAttributes.FILE, locationURI);
    }

    public VariableReference getSource() {
        return getTMapVarReference(TMapAttributes.SOURCE);
    }

    public void setSource(String source) {
        setAttribute(Transform.SOURCE, TMapAttributes.SOURCE, source);
    }

    public void setSource(VariableReference source) {
        setAttribute(Transform.SOURCE, TMapAttributes.SOURCE, source == null ? "" : source.getRefString());
    }

    public VariableReference getResult() {
        return getTMapVarReference(TMapAttributes.RESULT);
    }

    public void setResult(String result) {
        setAttribute(Transform.RESULT, TMapAttributes.RESULT, result);
    }

    public List<Param> getParams() {
        return getChildren(Param.class);
    }

    public void addParam(Param param) {
        addAfter(TYPE.getTagName(), param, TYPE.getChildTypes());
    }

    public void removeParam(Param param) {
        removeChild(TYPE.getTagName(), param);
    }

    public Reference[] getReferences() {
        List<Reference> refs = new ArrayList<Reference>();
        VariableReference sourceRef = getSource();
        if (sourceRef != null) {
            refs.add(sourceRef);
            refs.add(sourceRef.getPart());
        }
        
        VariableReference resultRef = getResult();
        if (resultRef != null) {
            refs.add(resultRef);
            refs.add(resultRef.getPart());
        }
        
        return refs.toArray(new Reference[refs.size()]);
    }

}
