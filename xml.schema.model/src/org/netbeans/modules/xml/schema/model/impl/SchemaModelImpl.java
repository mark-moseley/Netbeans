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

package org.netbeans.modules.xml.schema.model.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import org.netbeans.modules.xml.schema.model.Import;
import org.netbeans.modules.xml.schema.model.Include;
import org.netbeans.modules.xml.schema.model.Redefine;
import org.netbeans.modules.xml.xam.locator.CatalogModelException;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.SchemaComponentFactory;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.schema.model.SchemaModelFactory;
import org.netbeans.modules.xml.schema.model.SchemaModelReference;
import org.netbeans.modules.xml.schema.model.impl.xdm.SyncUpdateVisitor;
import org.netbeans.modules.xml.schema.model.visitor.FindGlobalReferenceVisitor;
import org.netbeans.modules.xml.xam.ComponentUpdater;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.Model.State;
import org.netbeans.modules.xml.xam.NamedReferenceable;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentModel;
import org.netbeans.modules.xml.xam.dom.ChangeInfo;
import org.netbeans.modules.xml.xam.dom.DocumentModelAccess;
import org.netbeans.modules.xml.xam.dom.SyncUnit;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;

/**
 *
 * @author Vidhya Narayanan
 */
public class SchemaModelImpl extends AbstractDocumentModel<SchemaComponent> implements SchemaModel {
    
    private SchemaImpl mSchema;
    private SchemaComponentFactory csef;
    private RefCacheSupport mRefCacheSupport;
    
    public SchemaModelImpl(ModelSource modelSource) {
        super(modelSource);
        csef = new SchemaComponentFactoryImpl(this);
        //getAccess().setAutoSync(true);
        mRefCacheSupport = new RefCacheSupport(this);
    }

    /**
     * It is mainly intended to be used by JUnit tests.
     * @return
     */
    public RefCacheSupport getRefCacheSupport() {
        return mRefCacheSupport;
    }
    
    /**
     *
     *
     * @return the schema represented by this model. The returned schema
     * instance will be valid and well formed, thus attempting to update
     * from a document which is not well formed will not result in any changes
     * to the schema model.
     */

    public SchemaImpl getSchema() {
        return (SchemaImpl)getRootComponent();
    }
    
    /**
     *
     *
     * @return common schema element factory valid for this instance
     */
    public SchemaComponentFactory getFactory() {
        return csef;
    }
    
    public SchemaComponent createRootComponent(org.w3c.dom.Element root) {
        SchemaImpl newSchema = (SchemaImpl)csef.create(root, null);
        if (newSchema != null) {
            mSchema = newSchema;
        } else {
            return null;
        }
        return getSchema();
    }

    public SchemaComponent getRootComponent() {
        return mSchema;
    }

    public <T extends NamedReferenceable>
            T resolve(String namespace, String localName, Class<T> type) 
    {
        if (XMLConstants.W3C_XML_SCHEMA_NS_URI.equals(namespace) &&
            XMLConstants.W3C_XML_SCHEMA_NS_URI.equals(getSchema().getTargetNamespace())) {
            return resolve(namespace, localName, type, null, new HashSet<SchemaModel>());
        }
        
        if (XMLConstants.W3C_XML_SCHEMA_NS_URI.equals(namespace)) {
            SchemaModel sm = SchemaModelFactory.getDefault().getPrimitiveTypesModel();
            return sm.findByNameAndType(localName, type);
        }
        
        return resolve(namespace, localName, type, null, new HashSet<SchemaModel>());
    }
    
    <T extends NamedReferenceable> T resolve(
            String namespace, String localName, Class<T> type,
            SchemaModelReference refToMe, Collection<SchemaModel> checked)
    {
        if (getState() != State.VALID) {
            return null;
        }
        
        T found = null;
        String targetNamespace = getSchema().getTargetNamespace();
        if ( targetNamespace != null && targetNamespace.equals(namespace) ||
            targetNamespace == null && namespace == null ||
            targetNamespace == null && refToMe instanceof Include) {
            found = findByNameAndType(localName, type);
            
            //Non-conservative approach to same namespace resolution.
            //See http://www.netbeans.org/issues/show_bug.cgi?id=122836
            if (found == null && refToMe == null) {
                Collection<SchemaModelImpl> models = getMegaIncludedModels(namespace);
                for (SchemaModelImpl sm : models) {
                    if (sm == null || checked.contains(sm)) {
                        continue;
                    }
                    //
                    found = sm.findByNameAndType(localName, type);
                    if (found != null) {
                        break;
                    }
                }
            }            
        }
        
        if (found == null && (! (refToMe instanceof Import) 
                    || ((Import)refToMe).getNamespace().equals(namespace))) {
            checked.add(this);
            
            Collection<SchemaModelReference> modelRefs = getSchemaModelReferences();
            for (SchemaModelReference ref : modelRefs) {
                // import should not have null namespace
                if (ref instanceof Import) {
                    if (namespace == null || ! namespace.equals(((Import)ref).getNamespace())) {
                        continue;
                    }
                }

                SchemaModelImpl resolvedRef = this.resolve(ref);
                if (resolvedRef != null && ! checked.contains(resolvedRef)) {
                    found = resolvedRef.resolve(namespace, localName, type, ref, checked);
                }
                if (found != null) {
                    break;
                }
            }
        }
        
        return found;
    }
    
    protected SchemaModelImpl resolve(SchemaModelReference ref) {
        if (mRefCacheSupport == null) {
            try {
                return (SchemaModelImpl) ref.resolveReferencedModel();
            } catch (CatalogModelException ex) {
                // Do nothing here. Exception means that the reference can't be resolved
                return null;
            }
        } else {
            return mRefCacheSupport.optimizedResolve(ref);
        }
    }

    public Collection<SchemaModelReference> getSchemaModelReferences() {
        Collection<SchemaModelReference> refs = new ArrayList<SchemaModelReference>();
        refs.addAll(getSchema().getRedefines());
        refs.addAll(getSchema().getIncludes());
        refs.addAll(getSchema().getImports());
        return refs;
    }
    
    /**
     * It looks for a set of schema models, which can be visible to each others.
     * The matter is, if model A includes B, then model B model's definitions
     * is visible to A. But the oposite assertion is also correct because B
     * logically becomes a part of A after inclusion.
     *
     * The problem is described in the issue http://www.netbeans.org/issues/show_bug.cgi?id=122836
     *
     * The task of the method is to find a set of schema models, which
     * can be visible to the current model. The parameter is used as a hint
     * to exclude the models from other namespace.
     *
     * @param soughtNs
     * @return
     */
    private Set<SchemaModelImpl> getMegaIncludedModels(String soughtNs) {
        //
        Schema mySchema = this.getSchema();
        if (mySchema == null) {
            return Collections.EMPTY_SET; 
        }
        //
        // If the current model has empty target namespace, then it can be included anywhere
        // If the current model has not empty target namespace, then it can be included only
        // to models with the same target namespace.
        String myTargetNs = this.getSchema().getTargetNamespace();
        if (myTargetNs != null && !soughtNs.equals(myTargetNs)) {
            return Collections.EMPTY_SET;
        }
        //
        // The map collects all inclusion between schema models
        // Key is including schema, value is the list of included schema
        MultivalueMap.Graph<SchemaModelImpl> inclusionGraph =
                new MultivalueMap.Graph<SchemaModelImpl>();
        //
        // Key is included schema, value is the list of including schema
        MultivalueMap.Graph<SchemaModelImpl> backInclusionMap =
                new MultivalueMap.Graph<SchemaModelImpl>();
        //
        // Populates inclusion map from all schema models, which have already loaded.
        List<SchemaModel> modelsList = SchemaModelFactory.getDefault().getModels();
        for(SchemaModel sModel: modelsList) {
            if(sModel == null || sModel.getSchema() == null || sModel == this) {
                continue;
            }
            //
            Schema otherSchema = sModel.getSchema();
            if (otherSchema == null) {
                continue;
            }
            //
            if (soughtNs != null) {
                String otherTargetNs = otherSchema.getTargetNamespace();
                if (otherTargetNs != null && !soughtNs.equals(otherTargetNs)) {
                    // Skip all other models with different targetNamespace.
                    continue;
                }
            }
            //
            assert sModel instanceof SchemaModelImpl;
            SchemaModelImpl otherSModel = SchemaModelImpl.class.cast(sModel);
            //
            Collection<Include> includes = otherSchema.getIncludes();
            for(Include ref: includes) {
                SchemaModelImpl includedSm = otherSModel.resolve(ref);
                if (includedSm != null) {
                    inclusionGraph.put(otherSModel, includedSm);
                    backInclusionMap.put(includedSm, otherSModel);
                }
            }
        }
        //
        // Now there is forward and back inclusion graphs.
        if (inclusionGraph.isEmpty()) {
            return Collections.EMPTY_SET;
        }
        //
        // Look for the roots of inclusion.
        // Root s the top schema model, which includes current schema recursively,
        // but isn't included anywhere itself.
        Set<SchemaModelImpl> inclusionRoots = 
                MultivalueMap.Utils.getTreeLeafs(backInclusionMap, this);
        //
        HashSet<SchemaModelImpl> result = new HashSet<SchemaModelImpl>();
        for (SchemaModelImpl root : inclusionRoots) {
            // The namespace of the inclusion root has to be exectly the same
            // as required.
            if (Util.equal(root.getSchema().getTargetNamespace(), soughtNs)) {
                MultivalueMap.Utils.populateAllSubItems(inclusionGraph, root, result);
            }
        }
        //
        result.remove(this);
        //
        return result;
    }
            
    public <T extends NamedReferenceable> T findByNameAndType(String localName, Class<T> type) {
        return new FindGlobalReferenceVisitor<T>().find(type, localName, getSchema());
    }
    
    public Set<Schema> findSchemas(String namespace) {
        Set<Schema> result = new HashSet<Schema>();
        
        // build-in XSD schema is always visible
        if (XMLConstants.W3C_XML_SCHEMA_NS_URI.equals(namespace)){
            SchemaModel primitiveModel = SchemaModelFactory.getDefault().getPrimitiveTypesModel();
            result.add(primitiveModel.getSchema());
            return result;
        } 
        
        return _findSchemas(namespace, result, null);
    }
    
    protected enum ReferenceType { IMPORT, INCLUDE, REDEFINE }
    
    Set<Schema> _findSchemas(String namespace, Set<Schema> result, ReferenceType refType) {
        SchemaImpl schema = getSchema();
        // schema could be null, if last sync throwed exception
        if (schema == null) {
            return result;
        }
        
        String targetNamespace = schema.getTargetNamespace();
        if (targetNamespace != null && targetNamespace.equals(namespace) ||
            targetNamespace == null && namespace == null) {
            result.add(schema);
        }
        
        if (refType != ReferenceType.IMPORT) {
            checkIncludeSchemas(namespace, result);
            checkRedefineSchemas(namespace, result);
            checkImportedSchemas(namespace, result);
        }
        
        return result;    
    }
    
    private void checkIncludeSchemas(String namespace, Set<Schema> result) {
        Collection<Include> includes = getSchema().getIncludes();
        for (Include include : includes) {
            try {
                SchemaModel model = include.resolveReferencedModel();
                if (model.getState() == Model.State.NOT_WELL_FORMED) {
                    continue;            
                }
                
                if (! result.contains(model.getSchema())) {
                    result.addAll(((SchemaModelImpl)model)._findSchemas(namespace, result, ReferenceType.INCLUDE));
                }
            } catch (CatalogModelException ex) {
                // ignore this exception to proceed with search
            }
        }
    }
    
    private void checkRedefineSchemas(String namespace, Set<Schema> result) {
        Collection<Redefine> redefines = getSchema().getRedefines();
        for (Redefine redefine : redefines) {
	       try {
		   SchemaModel model = redefine.resolveReferencedModel();
           if (model.getState() == Model.State.NOT_WELL_FORMED)
                continue;
            
		   if (! result.contains(model.getSchema())) {
		       result.addAll(((SchemaModelImpl)model)._findSchemas(namespace, result, ReferenceType.REDEFINE));
		   }
	       } catch (CatalogModelException ex) {
		   // ignore this exception to proceed with search
	       }
	   }
    }
    
    private void checkImportedSchemas(String namespace, Set<Schema> result) {
        Collection<Import> imports = getSchema().getImports();
        for (Import imp : imports) {
		try {
		    SchemaModel model = imp.resolveReferencedModel();
            if (model.getState() == Model.State.NOT_WELL_FORMED)
                continue;
            
		   if (! result.contains(model.getSchema())) {
		       result.addAll(((SchemaModelImpl)model)._findSchemas(namespace, result, ReferenceType.IMPORT));
		   }
		} catch (CatalogModelException ex) {
		    // ignore this exception to proceed with search
		}
	    }
    }
    
    /**
	 * This api returns the effective namespace for a given component. 
	 * If given component has a targetNamespace different than the 
	 * this schema, that namespace is returned. The special case is that if
	 * the targetNamespace of the component is null, there is no target
	 * namespace defined, then the import statements for this file are 
	 * examined to determine if this component is directly or indirectly 
	 * imported. If the component is imported, then null if returned 
	 * otherwise the component is assumed to be included or redefined and
	 * the namespace of this schema is returned. 
     */
    public String getEffectiveNamespace(SchemaComponent component) {
	SchemaModel componentModel = component.getModel();
	Schema schema = getSchema();
        Schema componentSchema = componentModel.getSchema();
        String tns = schema.getTargetNamespace();
        String componentTNS = componentSchema.getTargetNamespace();
	if (this == componentModel) {
	    return tns;
        } else if (componentTNS == null && tns != null) {
            // only include/redefine model can assum host model targetNamespace
            // so check if is from imported to just return null
	    Collection<Import> imports = schema.getImports();
	    for (Import imp: imports) {
		SchemaModel m = null;
		try {
		    m = imp.resolveReferencedModel();
		} catch (CatalogModelException ex) {
		    // the import cannot be resolved 
		}
		if(componentModel.equals(m)) {
		    return null;
		}
                if (m == null || m.getState() == Model.State.NOT_WELL_FORMED) {
                    continue;
                }
                String importedTNS = m.getSchema().getTargetNamespace();
                if (importedTNS == null) continue;
                Set<Schema> visibleSchemas = findSchemas(importedTNS);
                for (Schema visible : visibleSchemas) {
                    if (componentModel.equals(visible.getModel()))  {
                        return null;
                    }
                }
	    }
            return tns;
    	} else {
            return componentTNS;
        }
    }

    public SchemaComponent createComponent(SchemaComponent parent, org.w3c.dom.Element element) {
       return csef.create(element, parent);
    }

    protected ComponentUpdater<SchemaComponent> getComponentUpdater() {
        return new SyncUpdateVisitor();
    }

    @Override
    public Set<QName> getQNames() {
        return SchemaElements.allQNames();
    }
    
    @Override
    public SyncUnit prepareSyncUnit(ChangeInfo changes, SyncUnit unit) {
        unit = super.prepareSyncUnit(changes, unit);
        if (unit != null) {
            return new SyncUnitReviewVisitor().review(unit);
        }
        return null;
    }
    
    @Override
    public DocumentModelAccess getAccess() {
        if (access == null) {
            super.getAccess().setAutoSync(true);  // default autosync true
        }
        return super.getAccess();
    }
    
    @Override
    public Map<QName,List<QName>> getQNameValuedAttributes() {
        return SchemaAttributes.getQNameValuedAttributes();
    }

    public boolean isEmbedded() {
        return false;
    }

    @Override
    public String toString() {
        ModelSource source = getModelSource();
        if (source != null) {
            Lookup lookup = source.getLookup();
            if (lookup != null) {
                FileObject fileObject = lookup.lookup(FileObject.class);
                if (fileObject != null) {
                    return fileObject.getNameExt();
                }
            }
        }
        return super.toString();
    }

}
