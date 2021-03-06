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

package org.netbeans.modules.xml.wsdl.model.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.namespace.QName;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.wsdl.model.spi.GenericExtensibilityElement;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.Documentation;
import org.netbeans.modules.xml.wsdl.model.ExtensibilityElement;
import org.netbeans.modules.xml.wsdl.model.Import;
import org.netbeans.modules.xml.wsdl.model.ReferenceableWSDLComponent;
import org.netbeans.modules.xml.wsdl.model.Types;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLComponentFactory;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.extensions.xsd.WSDLSchema;
import org.netbeans.modules.xml.wsdl.model.visitor.FindReferencedVisitor;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentModel;
import org.netbeans.modules.xml.xam.dom.DocumentComponent;
import org.netbeans.modules.xml.xam.ComponentUpdater;
import org.netbeans.modules.xml.xam.dom.ChangeInfo;
import org.netbeans.modules.xml.xam.dom.DocumentModelAccess;
import org.netbeans.modules.xml.xam.dom.SyncUnit;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 *
 * @author rico
 * @author Nam Nguyen
 */
public class WSDLModelImpl extends WSDLModel {
    private Definitions definitions;
    private WSDLComponentFactory wcf;
    
    public WSDLModelImpl(ModelSource source) {
        super(source);
        wcf = new WSDLComponentFactoryImpl(this);
    }
    	
    public WSDLComponent createRootComponent(Element root) {
        DefinitionsImpl newDefinitions = null;
        QName q = root == null ? null : AbstractDocumentComponent.getQName(root);
        if (root != null && WSDLQNames.DEFINITIONS.getQName().equals(q)) {
            newDefinitions = new DefinitionsImpl(this, root);
            setDefinitions(newDefinitions);
        } else {
            return null;
        }
        
        return getDefinitions();
    }
    
    public WSDLComponent getRootComponent() {
        return definitions;
    }
    
    public WSDLComponent createComponent(WSDLComponent parent, Element element) {
        return getFactory().create(element, parent);
    }
    
    protected ComponentUpdater<WSDLComponent> getComponentUpdater() {
        return new ChildComponentUpdateVisitor<WSDLComponent>();
    }
    
    public WSDLComponentFactory getFactory() {
        return wcf;
    }
    
    public void setDefinitions(Definitions def){
        assert (def instanceof DefinitionsImpl) ;
        definitions = DefinitionsImpl.class.cast(def);
    }
    
    public Definitions getDefinitions(){
        return definitions;
    }

    ElementFactoryRegistry getElementRegistry() {
        return ElementFactoryRegistry.getDefault();
    }
    
    public List<WSDLModel> getImportedWSDLModels() {
        List<WSDLModel> ret = new ArrayList<WSDLModel>();
        Collection<Import> imports = getDefinitions().getImports();
        for (Import i:imports) {
            try {
                WSDLModel m = i.getImportedWSDLModel();
                if (m != null) {
                    ret.add(m);
                }
            } catch(Exception e) {
                Logger.getLogger(this.getClass().getName()).log(Level.FINE, "getImportedWSDLModels", e);
            }
        }
        return ret;
    }

    public List<SchemaModel> getImportedSchemaModels() {
        List<SchemaModel> ret = new ArrayList<SchemaModel>();
        Collection<Import> imports = getDefinitions().getImports();
        for (Import i:imports) {
            try {
                SchemaModel m = ((ImportImpl)i).resolveToSchemaModel();
                if (m != null) {
                    ret.add(m);
                }
            } catch(Exception e) {
                Logger.getLogger(this.getClass().getName()).log(Level.FINE, "getImportedSchemaModels", e); //NOI18N
            }
        }
        return ret;
    }

    public List<SchemaModel> getEmbeddedSchemaModels() {
        List<SchemaModel> ret = new ArrayList<SchemaModel>();
        Types types = getDefinitions().getTypes();
        List<WSDLSchema> embeddedSchemas = Collections.emptyList();
        if (types != null) {
            embeddedSchemas = types.getExtensibilityElements(WSDLSchema.class);
        }
        for (WSDLSchema wschema : embeddedSchemas) {
            ret.add(wschema.getSchemaModel());
        }
        return ret;
    }
    
    public List<WSDLModel> findWSDLModel(String namespace) {
        if (namespace == null) {
            return Collections.emptyList();
        }
        
        List<WSDLModel> models = getImportedWSDLModels();
        models.add(0, this);

        List<WSDLModel> ret = new ArrayList<WSDLModel>();
        for (WSDLModel m : models) {
            String targetNamespace = m.getDefinitions().getTargetNamespace();
            if (namespace.equals(targetNamespace)) {
                ret.add(m);
            }
        }
        return ret;
    }

    public List<Schema> findSchemas(String namespace) {
        List<Schema> ret = new ArrayList<Schema>();
        for (SchemaModel sm : getEmbeddedSchemaModels()) {
            try {
                ret.addAll(sm.findSchemas(namespace));
            } catch(Exception ex) {
                Logger.getLogger(this.getClass().getName()).log(Level.FINE, "findSchemas", ex);
            }
        }
        SchemaModel sm = findSchemaModelFromImports(namespace);
        if (sm != null) {
            ret.add(sm.getSchema());
        }
        return ret;
    }
    
    private SchemaModel findSchemaModelFromImports(String namespace) {
        if (namespace == null) {
            return null;
        }
        
        List<SchemaModel> models = getImportedSchemaModels();
        for (SchemaModel m : models) {
            String targetNamespace = m.getSchema().getTargetNamespace();
            if (namespace.equals(targetNamespace)) {
                return m;
            }
        }
        return null;
    }

    public <T extends ReferenceableWSDLComponent> T findComponentByName(String name, Class<T> type) {
        return type.cast(new FindReferencedVisitor(getDefinitions()).find(name, type));
    }
    
    public <T extends ReferenceableWSDLComponent> T findComponentByName(QName name, Class<T> type) {
        String namespace = name.getNamespaceURI();
        if (namespace == null) {
            return findComponentByName(name.getLocalPart(), type);
        } else {
            for (WSDLModel targetModel : findWSDLModel(namespace)) {
                T found = targetModel.findComponentByName(name.getLocalPart(), type);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }
    
    public Set<QName> getQNames() {
        return getElementRegistry().getKnownQNames();
    }

    public Set<String> getElementNames() {
        return getElementRegistry().getKnownElementNames();
    }
    
    public ChangeInfo prepareChangeInfo(List<Node> pathToRoot) {
        ChangeInfo change = super.prepareChangeInfo(pathToRoot);
        DocumentComponent parentComponent = findComponent(change.getRootToParentPath());
        if (parentComponent == null) {
            return change;
        }
        if (! (parentComponent.getModel() instanceof WSDLModel)) 
        {
            getElementRegistry().addEmbeddedModelQNames((AbstractDocumentModel)parentComponent.getModel());
            change = super.prepareChangeInfo(pathToRoot);
        } else if (isDomainElement(parentComponent.getPeer()) && 
                ! change.isDomainElement() && change.getChangedElement() != null) 
        {
            if (change.getOtherNonDomainElementNodes() == null ||
                change.getOtherNonDomainElementNodes().isEmpty()) 
            {
                // case add or remove generic extensibility element
                change.setDomainElement(true);
                change.setParentComponent(null);
            } else if (! (parentComponent instanceof Documentation)) {
                List<Element> rootToChanged = new ArrayList<Element>(change.getRootToParentPath());
                rootToChanged.add(change.getChangedElement());
                DocumentComponent changedComponent = findComponent(rootToChanged);
                if (changedComponent != null && 
                    changedComponent.getClass().isAssignableFrom(GenericExtensibilityElement.class)) {
                    // case generic extensibility element changed
                    change.markNonDomainChildAsChanged();
                    change.setParentComponent(null);
                }
            }
        } else {
            change.setParentComponent(parentComponent);
        }
        return change;
    }
    
    public SyncUnit prepareSyncUnit(ChangeInfo changes, SyncUnit unit) {
        unit = super.prepareSyncUnit(changes, unit);
        if (unit != null) {
            return new SyncReviewVisitor().review(unit);
        }
        return null;
    }
    
    public AbstractDocumentComponent findComponent(
            AbstractDocumentComponent current,
            List<org.w3c.dom.Element> pathFromRoot, 
            int iCurrent) {
        
        if (current instanceof ExtensibilityElement.EmbeddedModel) {
            ExtensibilityElement.EmbeddedModel emb = (ExtensibilityElement.EmbeddedModel) current;
            AbstractDocumentModel axm = (AbstractDocumentModel) emb.getEmbeddedModel();
            AbstractDocumentComponent embedded = (AbstractDocumentComponent) axm.getRootComponent();
            return axm.findComponent(embedded, pathFromRoot, iCurrent);
        } else {
            return super.findComponent(current, pathFromRoot, iCurrent);
        }
    }

    @Override
    public Map<QName, List<QName>> getQNameValuedAttributes() {
        return WSDLAttribute.getQNameValuedAttributes();
    }

}
