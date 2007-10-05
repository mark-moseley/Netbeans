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

package org.netbeans.modules.xml.wsdl.ui.netbeans.module;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;

import org.netbeans.modules.xml.catalogsupport.DefaultProjectCatalogSupport;
import org.netbeans.modules.xml.schema.model.Element;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.BindingOperation;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.ExtensibilityElement;
import org.netbeans.modules.xml.wsdl.model.Import;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.wsdl.model.PortType;
import org.netbeans.modules.xml.wsdl.model.Types;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.extensions.xsd.WSDLSchema;
import org.netbeans.modules.xml.wsdl.ui.actions.NameGenerator;
import org.netbeans.modules.xml.wsdl.ui.actions.schema.ExtensibilityElementCreatorVisitor;
import org.netbeans.modules.xml.wsdl.ui.schema.visitor.OptionalAttributeFinderVisitor;
import org.netbeans.modules.xml.wsdl.ui.wsdl.util.RelativePath;
import org.netbeans.modules.xml.xam.AbstractComponent;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent;
import org.netbeans.modules.xml.xam.locator.CatalogModelException;
import org.openide.ErrorManager;
import org.openide.explorer.view.TreeView;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

public class Utility {
    
    public static String getNamespacePrefix(String namespace, WSDLModel model) {
        if (model != null && namespace != null) {
            return ((AbstractDocumentComponent) model.getDefinitions()).lookupPrefix(namespace);
        }
        return null;
    }
    
    public static String getNamespacePrefix(String namespace, WSDLComponent element) {
        if (element != null && namespace != null) {
            return ((AbstractDocumentComponent) element).lookupPrefix(namespace);
        }
        return null;
    }
    
    public static String getNamespaceURI(String prefix, WSDLComponent element) {
        if (element != null && prefix != null) {
            return ((AbstractDocumentComponent) element).lookupNamespaceURI(prefix, true);
        }
        return null;
    }
    public static String getNamespaceURI(String prefix, WSDLModel model) {
        if (model != null && prefix != null) {
            return ((AbstractDocumentComponent) model.getDefinitions()).lookupNamespaceURI(prefix, true);
        }
        return null;
    }
    
    public static Import getImport(String namespace, WSDLModel model) {
        Collection imports = model.getDefinitions().getImports();
        if (imports != null) {
            Iterator iter = imports.iterator();
            for (;iter.hasNext();) {
                Import existingImport = (Import) iter.next();
                if (existingImport.getNamespace().equals(namespace)) {
                    return existingImport;
                }
            }
        }
        return null;
    }
    
    public static Collection<WSDLModel> getImportedDocuments(WSDLModel model) {
        Collection<Import> imports = model.getDefinitions().getImports();
        Collection<WSDLModel> returnImports = new ArrayList<WSDLModel>();
        if (imports != null) {
            Iterator iter = imports.iterator();
            for (;iter.hasNext();) {
                Import existingImport = (Import) iter.next();
                List<WSDLModel> impModels = model.findWSDLModel(existingImport.getNamespace());
                returnImports.addAll(impModels);
            }
        }
        return returnImports;
    }
    
    public static Map getNamespaces(Definitions def) {
        return ((AbstractDocumentComponent) def).getPrefixes();
    }
    
    public static GlobalElement findGlobalElement(Schema schema, String elementName) {
        Collection gElem = schema.findAllGlobalElements();
        if (gElem !=null){
            Iterator iter = gElem.iterator();
            while (iter.hasNext()) {
                GlobalElement elem = (GlobalElement) iter.next();
                if (elem.getName().equals(elementName)) {
                    return elem;
                }
            }
        }
        return null;
    }
    
    public static GlobalType findGlobalType(Schema schema, String typeName) {
        Collection gTypes = schema.findAllGlobalTypes();
        if (gTypes !=null){
            Iterator iter = gTypes.iterator();
            while (iter.hasNext()) {
                GlobalType type = (GlobalType) iter.next();
                if (type.getName().equals(typeName)) {
                    return type;
                }
            }
        }
        return null;
    }
    
    public static String fromQNameToString(QName qname) {
        if (qname.getPrefix() != null && qname.getPrefix().trim().length() > 0) {
            return qname.getPrefix() + ":" + qname.getLocalPart();
        }
        return qname.getLocalPart();
    }
    
    public static String getNameAndDropPrefixIfInCurrentModel(String ns, String localPart, WSDLModel model) {
        if (ns == null || model == null)
            return localPart;
        
        String tns = model.getDefinitions().getTargetNamespace();
        if (tns != null && !tns.equals(ns)) {
            String prefix = getNamespacePrefix(ns, model);
            if (prefix != null) 
                return prefix + ":" + localPart;
        }

        return localPart;
    }    
    
    
    public static List<QName> getExtensionAttributes(WSDLComponent comp) {
        ArrayList<QName> result = new ArrayList<QName>();
        Map<QName, String> attrMap = comp.getAttributeMap();
        Set<QName> set = attrMap.keySet();
        if (set != null) {
            Iterator<QName> iter = set.iterator();
            while (iter.hasNext()) {
                QName name =  iter.next();
                String ns = name.getNamespaceURI();
                if (ns != null && ns.trim().length() > 0 && !ns.equals(((AbstractDocumentComponent) comp).getQName().getNamespaceURI())) {
                    result.add(name);
                }
            }
        }
        return result;
    }
    public static List<QName> getOptionalAttributes(WSDLComponent comp, Element elem) {
        ArrayList<QName> result = new ArrayList<QName>();
        Map<QName, String> attrMap = comp.getAttributeMap();
        Set<QName> set = attrMap.keySet();
        if (set != null) {
            Iterator<QName> iter = set.iterator();
            while (iter.hasNext()) {
                QName name =  iter.next();
                String ns = name.getNamespaceURI();
                if (ns != null && ns.trim().length() > 0 && !ns.equals(comp.getModel().getDefinitions().getTargetNamespace())) {
                    //extension attibute
                    //do nothing
                } else {
                    //not a extension attribute
                    OptionalAttributeFinderVisitor visitor = new OptionalAttributeFinderVisitor(name.getLocalPart());
                    elem.accept(visitor);
                    if (visitor.isOptional()) {
                        result.add(name);
                    }
                }
            }
        }
        return result;
    }
    
    /**
     * Expands nodes on the treeview till given levels
     * @param tv the treeview object
     * @param level the level till which the nodes should be expanded. 0 means none.
     * @param rootNode the rootNode
     */
    public static void expandNodes(TreeView tv, int level, Node rootNode) {
        if (level == 0) return;
        
        Children children = rootNode.getChildren();
        if (children != null) {
            Node[]  nodes = children.getNodes();
            if (nodes != null) {
                for (int i= 0; i < nodes.length; i++) {
                    tv.expandNode(nodes[i]); //Expand node
                    expandNodes(tv, level - 1, nodes[i]); //expand children
                }
            }
        }
    }
    
    public static void addNamespacePrefix(WSDLComponent comp, WSDLModel model, String prefix) {
        if (comp != null && model != null) {
            addNamespacePrefix(comp.getModel(), model, prefix);
        }
    }
    
    private static void addNamespacePrefix(WSDLModel imported, WSDLModel model, String prefix) {
        assert model != null;
        if(imported != null) {
            Definitions definitions = model.getDefinitions();
            String targetNamespace = imported.getDefinitions().getTargetNamespace();
            String computedPrefix = null;

            if(targetNamespace != null) {
                if (Utility.getNamespacePrefix(targetNamespace, model) != null) {
                    //already exists, doesnt need to be added
                    return;
                }
                //Use the prefix (in parameter) or generate new one.
                if(prefix != null) {
                    computedPrefix = prefix;
                } else {
                    computedPrefix = NameGenerator.getInstance().generateNamespacePrefix(null, model.getDefinitions());
                }
                boolean isAlreadyInTransaction = Utility.startTransaction(model);
                ((AbstractDocumentComponent)definitions).addPrefix(computedPrefix, targetNamespace);

                Utility.endTransaction(model, isAlreadyInTransaction);

            }

        }
        
    }

    public static void addNamespacePrefix(Element schemaElement,
            WSDLComponent element,
            String prefix) {
        if(schemaElement != null && element.getModel() != null) {
            addNamespacePrefix(schemaElement.getModel().getSchema(), element.getModel(), prefix);
        }
        
    }
    
    public static void addNamespacePrefix(Schema schema,
            WSDLModel model,
            String prefix) {
        assert model != null;
        if(schema != null) {
            Definitions definitions = model.getDefinitions();
            String targetNamespace = schema.getTargetNamespace();
            String computedPrefix = null;

            if(targetNamespace != null) {
                if (Utility.getNamespacePrefix(targetNamespace, model) != null) {
                    //already exists, doesnt need to be added
                    return;
                }
                //Use the prefix (in parameter) or generate new one.
                if(prefix != null) {
                    computedPrefix = prefix;
                } else {
                    computedPrefix = NameGenerator.getInstance().generateNamespacePrefix(null, model.getDefinitions());
                }
                boolean isAlreadyInTransaction = Utility.startTransaction(model);
                ((AbstractDocumentComponent)definitions).addPrefix(computedPrefix, targetNamespace);

                Utility.endTransaction(model, isAlreadyInTransaction);

            }

        }
    }
    
    public static void addExtensibilityElement(WSDLComponent element, Element schemaElement, String prefix) {
        // Issue 93424, create a single transaction to encapsulate all changes.
        WSDLModel model = element.getModel();
        boolean in = startTransaction(model);
        Utility.addNamespacePrefix(schemaElement, element, prefix);
        ExtensibilityElementCreatorVisitor eeCreator = new ExtensibilityElementCreatorVisitor(element);
        schemaElement.accept(eeCreator);
        endTransaction(model, in);
    }
    
    public static boolean startTransaction(WSDLModel model) {
        if (model.isIntransaction()) return true;
        model.startTransaction();
        return false;
    }
    
    public static void endTransaction(WSDLModel model, boolean isInTransaction) {
        if (isInTransaction) return;
        model.endTransaction();
    }
    
    public static Collection<Operation> getImplementableOperations(PortType portType, Binding binding) {
        if (portType == null || portType.getOperations() == null || portType.getOperations().size() == 0
                || binding == null) {
            return null;
        }
        List<Operation> listData = new ArrayList<Operation>(portType.getOperations().size());
        Set<String> bindingOperationsSet = new HashSet<String>();
        Collection<BindingOperation> bindingOperations = binding.getBindingOperations();
        if (bindingOperations != null) {
            Iterator<BindingOperation> iter = bindingOperations.iterator();
            while (iter.hasNext()) {
                bindingOperationsSet.add(iter.next().getOperation().get().getName());
            }
            
        }
        Iterator it = portType.getOperations().iterator();
        
        while(it.hasNext()) {
            Operation operation = (Operation) it.next();
            if(operation.getName() != null) {
                if (!bindingOperationsSet.contains(operation.getName())) {
                    listData.add(operation);
                }
            }
        }
        
        return listData;
    }
    
    @SuppressWarnings("unchecked")
    public static Map<String,String> getPrefixes(WSDLComponent wsdlComponent) {
        AbstractDocumentComponent comp = ((AbstractDocumentComponent) wsdlComponent);
        Map<String,String> prefixes = comp.getPrefixes();
        while(comp.getParent() != null) {
            comp = (AbstractDocumentComponent) comp.getParent();
            prefixes.putAll(comp.getPrefixes());
        }
        
        return prefixes;
    }
    
    public static void splitExtensibilityElements(List<ExtensibilityElement> list, 
            Set<String> specialTargetNamespaces, 
            List<ExtensibilityElement> specialExtensibilityElements,
            List<ExtensibilityElement> nonSpecialExtensibilityElements) {
        if (specialExtensibilityElements == null) {
            specialExtensibilityElements = new ArrayList<ExtensibilityElement>();
        }
        if (nonSpecialExtensibilityElements == null) {
            nonSpecialExtensibilityElements = new ArrayList<ExtensibilityElement>();
        }
        
        if (list != null) {
            for (ExtensibilityElement element : list) {
                if (specialTargetNamespaces.contains(element.getQName().getNamespaceURI())) {
                    specialExtensibilityElements.add(element);
                } else {
                    nonSpecialExtensibilityElements.add(element);
                }
            }
        }
    }
    
    public static List<ExtensibilityElement> getSpecialExtensibilityElements(List<ExtensibilityElement> list, 
            String specialNamespace) {
        List<ExtensibilityElement> specialList = new ArrayList<ExtensibilityElement>();
        if (list != null) {
            for (ExtensibilityElement element : list) {
                if (specialNamespace.equals(element.getQName().getNamespaceURI())) {
                    specialList.add(element);
                }
            }
        }
        return specialList;
    }
    
    /**
     * This method finds the absolute index in the definitions where the component needs to be inserted, 
     * such that the component is at given index with respect to its kind.
     * 
     * For example, There are 5 messages, and one needs to insert another at index 4. Then this method will insert
     * it at some index on Definitions, which will make it look like the 4th Message.
     * 
     * it doesnt call startTransaction or endTransaction, so its the responsibility of the caller to do it.
     * 
     * @param index
     * @param model
     * @param compToInsert
     * @param propertyName
     */
    public static void insertIntoDefinitionsAtIndex(int index, WSDLModel model, WSDLComponent compToInsert, String propertyName) {
        assert model.isIntransaction() : "Need to call startTransaction on this model, before calling this method";
        //find index among all definitions elements. 
        //for inserting at index = 5, find index of the 4th PLT and insert after this index
        int defIndex = -1;
        int indexOfPreviousPLT = index - 1;
        List<WSDLComponent> comps = model.getDefinitions().getChildren();
        for (int i = 0; i < comps.size(); i++) {
            WSDLComponent comp = comps.get(i);
            if (compToInsert.getClass().isAssignableFrom(comp.getClass())) {
                if (indexOfPreviousPLT > defIndex) {
                    defIndex ++;
                } else {
                    ((AbstractComponent<WSDLComponent>) model.getDefinitions()).insertAtIndex(propertyName, compToInsert, i);
                    break;
                }
            }
        }
    }

    
    /* Similiar logic can be found in SchemaImportsGenerator.processImports(). So if there are changes here, also change in SchemaImportsGenerator*/
    public static void addSchemaImport(SchemaComponent comp1, WSDLModel wsdlModel) {
        Map<String, String> existingLocationToNamespaceMap = new HashMap<String, String>();
        
        FileObject wsdlFileObj = wsdlModel.getModelSource().getLookup().lookup(FileObject.class);
        URI wsdlFileURI = FileUtil.toFile(wsdlFileObj).toURI();
        
        Definitions def = wsdlModel.getDefinitions();
        Types types = def.getTypes();
        if (types == null) {
            types = wsdlModel.getFactory().createTypes();
            def.setTypes(types);
        }
        
        Schema defaultInlineSchema = null;
        String wsdlTNS = def.getTargetNamespace();
        if (wsdlTNS != null) {
            Collection<Schema> schmas = types.getSchemas();
            if (schmas != null) {
                for (Schema s : schmas) {
                    if (s.getTargetNamespace() != null && s.getTargetNamespace().equals(wsdlTNS)) {
                        defaultInlineSchema = s;
                        break;
                    }
                }
            }
        }
        
        WSDLSchema wsdlSchema = null;
        if (defaultInlineSchema == null) {
            wsdlSchema = wsdlModel.getFactory().createWSDLSchema();
            SchemaModel schemaModel = wsdlSchema.getSchemaModel();
            defaultInlineSchema = schemaModel.getSchema();
            defaultInlineSchema.setTargetNamespace(wsdlTNS);
        }
        
        //if any import with same namespace is present, dont import it.
        Collection<org.netbeans.modules.xml.schema.model.Import> imports = defaultInlineSchema.getImports();
        for (org.netbeans.modules.xml.schema.model.Import imp : imports) {
            existingLocationToNamespaceMap.put(imp.getSchemaLocation(), imp.getNamespace());
        }
        
        Collection<Schema> schemas = types.getSchemas();
        if (schemas != null) {
             for (Schema schema : schemas) {
                 Collection<org.netbeans.modules.xml.schema.model.Import> schemaImports = schema.getImports();
                 for (org.netbeans.modules.xml.schema.model.Import imp : schemaImports) {
                     existingLocationToNamespaceMap.put(imp.getSchemaLocation(), imp.getNamespace());
                 }
             }
        }
        
        SchemaModel model = comp1.getModel();

        if (model != null) {
            
            String schemaTNS = model.getSchema().getTargetNamespace();
            if (schemaTNS != null && 
                    !schemaTNS.equals(XMLConstants.W3C_XML_SCHEMA_NS_URI)) {

                FileObject fo = model.getModelSource().getLookup().lookup(FileObject.class);
                
                
                if (fo != null) {
                    String path = null;
                    //should be different files. in case of inline schemas.
                    if (!FileUtil.toFile(fo).toURI().equals(wsdlFileURI)) {
                        DefaultProjectCatalogSupport catalogSupport = DefaultProjectCatalogSupport.getInstance(wsdlFileObj);
                        if (catalogSupport.needsCatalogEntry(wsdlFileObj, fo)) {
                            // Remove the previous catalog entry, then create new one.
                            URI uri;
                            try {
                                uri = catalogSupport.getReferenceURI(wsdlFileObj, fo);
                                catalogSupport.removeCatalogEntry(uri);
                                catalogSupport.createCatalogEntry(wsdlFileObj, fo);
                                path = catalogSupport.getReferenceURI(wsdlFileObj, fo).toString();
                            } catch (URISyntaxException use) {
                                ErrorManager.getDefault().notify(use);
                            } catch (IOException ioe) {
                                ErrorManager.getDefault().notify(ioe);
                            } catch (CatalogModelException cme) {
                                ErrorManager.getDefault().notify(cme);
                            }
                        } else {
                            path = RelativePath.getRelativePath(FileUtil.toFile(wsdlFileObj).getParentFile(), FileUtil.toFile(fo));
                        }
                    }
                    if (path != null && (!existingLocationToNamespaceMap.containsKey(path) ||
                            existingLocationToNamespaceMap.get(path) == null ||
                            !existingLocationToNamespaceMap.get(path).equals(schemaTNS)))
                    { 
                        org.netbeans.modules.xml.schema.model.Import schemaImport =
                            defaultInlineSchema.getModel().getFactory().createImport();
                        schemaImport.setNamespace(schemaTNS);
                        schemaImport.setSchemaLocation(path);
                        defaultInlineSchema.addExternalReference(schemaImport);
                        if (wsdlSchema != null) {
                            types.addExtensibilityElement(wsdlSchema);
                        }
                    }
                }
            }
        }
        
    } 
    
    public static void addWSDLImport(WSDLComponent comp, WSDLModel wsdlModel) {
        if (comp != null && wsdlModel != null && comp.getModel() != wsdlModel) {
            
            String importedWSDLTargetNamespace = comp.getModel().getDefinitions().getTargetNamespace();
            
            if (importedWSDLTargetNamespace != null) {
                Import wsdlImport = wsdlModel.getFactory().createImport();
                wsdlImport.setNamespace(importedWSDLTargetNamespace);
                
                FileObject wsdlFileObj = wsdlModel.getModelSource().getLookup().lookup(FileObject.class);
                URI wsdlFileURI = FileUtil.toFile(wsdlFileObj).toURI();
                
                FileObject fo = comp.getModel().getModelSource().getLookup().lookup(FileObject.class);
                String path = null;
                if (!FileUtil.toFile(fo).toURI().equals(wsdlFileURI)) {
                    DefaultProjectCatalogSupport catalogSupport = DefaultProjectCatalogSupport.getInstance(wsdlFileObj);
                    if (catalogSupport.needsCatalogEntry(wsdlFileObj, fo)) {
                        // Remove the previous catalog entry, then create new one.
                        URI uri;
                        try {
                            uri = catalogSupport.getReferenceURI(wsdlFileObj, fo);
                            catalogSupport.removeCatalogEntry(uri);
                            catalogSupport.createCatalogEntry(wsdlFileObj, fo);
                            path = catalogSupport.getReferenceURI(wsdlFileObj, fo).toString();
                        } catch (URISyntaxException use) {
                            ErrorManager.getDefault().notify(use);
                        } catch (IOException ioe) {
                            ErrorManager.getDefault().notify(ioe);
                        } catch (CatalogModelException cme) {
                            ErrorManager.getDefault().notify(cme);
                        }
                    } else {
                        path = RelativePath.getRelativePath(FileUtil.toFile(wsdlFileObj).getParentFile(), FileUtil.toFile(fo));
                    }
                }
                
                if (path != null) wsdlImport.setLocation(path);
                
                wsdlModel.getDefinitions().addImport(wsdlImport);
            }
        }
    }

    /**
     * Does basic escaping of html characters. and is not complete.
     * replaces & => &amp;
     *          < => &lt;
     *          > => &gt;
     *          and all spaces with &nbsp;
     *          
     * The best solution is probably to use commons lang, StringEscapeUtils.escapeHtml();
     * @param str
     * @return the escaped string.
     */
    public static String escapeHtml(String str) {
        if (str == null || str.length() == 0) return str;
        return str.replaceAll("&", "&amp;").replaceAll("<", "&lt;").replaceAll(">", "&gt;").replaceAll("\\s", "&nbsp;");
    }
        
}
