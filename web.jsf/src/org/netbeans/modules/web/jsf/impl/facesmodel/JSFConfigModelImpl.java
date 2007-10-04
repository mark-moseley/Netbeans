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

package org.netbeans.modules.web.jsf.impl.facesmodel;

import java.util.Set;
import java.util.logging.Logger;
import javax.xml.namespace.QName;
import org.netbeans.modules.web.jsf.api.facesmodel.FacesConfig;
import org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigComponent;
import org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigComponentFactory;
import org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigModel;
import org.netbeans.modules.web.jsf.api.facesmodel.JSFVersion;
import org.netbeans.modules.xml.xam.ComponentUpdater;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentModel;
import org.w3c.dom.Element;

/**
 *
 * @author Petr Pisl
 */
public class JSFConfigModelImpl extends AbstractDocumentModel<JSFConfigComponent> implements JSFConfigModel {
    
    private static final Logger LOGGER = Logger.getLogger(JSFConfigModelImpl.class.getName());
    
    private FacesConfig facesConfig;
    private final JSFConfigComponentFactory componentFactory;
    
    /** Creates a new instance of JSFConfigModelImpl */
    public JSFConfigModelImpl(ModelSource source) {
        super(source);
        componentFactory = new JSFConfigComponentFactoryImpl(this);
    }

    public JSFConfigComponent createRootComponent(Element root) {
        FacesConfig newFacesConfig = (FacesConfig) getFactory().create(root, null);
        if (newFacesConfig != null) {
            facesConfig = newFacesConfig;
        }
        return newFacesConfig;
    }

    protected ComponentUpdater<JSFConfigComponent> getComponentUpdater() {
        return new SyncUpdateVisitor();
    }

    public FacesConfig getRootComponent() {
        LOGGER.fine("getRootComponent()");
        return facesConfig;
    }

    public JSFConfigComponent createComponent(JSFConfigComponent parent, Element element) {
        return getFactory().create(element, parent);
    }

    public JSFConfigComponentFactory getFactory() {
        return componentFactory;
    }
    
    public JSFVersion getVersion() {
        String namespaceURI = getRootComponent().getPeer().getNamespaceURI();
        JSFVersion version = JSFVersion.JSF_1_1;
        if (namespaceURI != null){
            version = JSFVersion.JSF_1_2;
        }
        return version;
    }
    
    public Set<QName> getQNames() {
        return JSFConfigQNames.getMappedQNames(getVersion());
    }

}
