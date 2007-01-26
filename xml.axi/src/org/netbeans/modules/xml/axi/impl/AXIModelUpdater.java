/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.xml.axi.impl;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.xml.axi.AXIComponent;
import org.netbeans.modules.xml.axi.AXIComponent.ComponentType;
import org.netbeans.modules.xml.axi.AXIContainer;
import org.netbeans.modules.xml.axi.AXIDocument;
import org.netbeans.modules.xml.axi.AXIType;
import org.netbeans.modules.xml.axi.AnyAttribute;
import org.netbeans.modules.xml.axi.AnyElement;
import org.netbeans.modules.xml.axi.Attribute;
import org.netbeans.modules.xml.axi.Compositor;
import org.netbeans.modules.xml.axi.Element;
import org.netbeans.modules.xml.axi.visitor.DeepAXITreeVisitor;
import org.netbeans.modules.xml.schema.model.AttributeReference;
import org.netbeans.modules.xml.schema.model.ElementReference;
import org.netbeans.modules.xml.schema.model.GlobalAttribute;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.LocalAttribute;
import org.netbeans.modules.xml.schema.model.LocalElement;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.axi.ContentModel;

/**
 * AXIModelUpdater updates the AXIModel to keep it in sync
 * with the SchemaModel. The sync alogirithm works as follows:
 * 
 * For every component X it creates another component X' by using
 * the peer in X.
 * 1. For each child in X, checks its existence in X'.
 *    If not found, deleted from X.
 * 2. For each child in X', checks its existence in X.
 *    If not found, added to X.
 * 3. Repeat the steps for for all remaining child in X after step 1.
 * 
 * @author Samaresh (Samaresh.Panda@Sun.Com)
 */
public class AXIModelUpdater extends DeepAXITreeVisitor {

    /**
     * Creates a new instance of AXIModelUpdater
     */
    public AXIModelUpdater(AXIModelImpl model) {
        this.model = model;
    }
        
    /**
     * Keeps the AXIModel in sync with schema model.
     * Returns true if success, false if failed.
     */
    public boolean doSync() {        
        try {
            syncCompleted = false;
            //first sync the document
            model.getRoot().accept(this);
            
            //sync all global elements and attributes
            for(AXIComponent child : model.getRoot().getChildren()) {
                if(child instanceof ContentModel)
                    continue;
                
                child.accept(this);
            }
            
            //finally sync all the content models
            for(ContentModel contentModel : model.getRoot().getContentModels()) {
                contentModel.accept(this);
            }        
            syncCompleted = true;
        } catch(Exception ex) {
            //bad things happened
        }        
        return syncCompleted;
    }
        
    /**
     * Syncs one component at a time. For each component, it creates
     * the same component as if it was created from scratch and then merges
     * the difference to the original one and then visits the modified list
     * of children.
     */
    protected void visitChildren(AXIComponent original) {
        if(!original.canVisitChildren())
            return;
        
        //skip proxies if the original is from the same model
        if(original.getComponentType() == ComponentType.PROXY &&
           original.getModel() == original.getOriginal().getModel()) {
            return;
        }
        
        AXIComponentCreator creator = new AXIComponentCreator(model);
        AXIComponent altered = getAltered(original);
        assert(altered != null);
        
        List<AXIComponent> modifiedChildren = synchronize(original, altered);
        if( (modifiedChildren == null) ||
            (original instanceof AXIDocument) ) {
            return;
        }
        
        //visit the children that were nethier removed nor added
        for(AXIComponent child : modifiedChildren) {
            child.accept(this);
        }
    }

    /**
     * We do not sync proxies, unless they represent items from other files.
     * Hence, for a proxy, get the original, else create a new one.
     */
    private AXIComponent getAltered(AXIComponent original) {
        if(original.getComponentType() == ComponentType.PROXY) {
            return original.getOriginal();
        }
        
        //create the same component from the original's peer
        AXIComponentCreator creator = new AXIComponentCreator(model);
        return creator.createNew(original.getPeer());
    }
    
    /**
     * Step 1: From the original tree, delete the children that no longer
     *         exist in new tree. Remaining ones exist and we must sync them.
     * Step 2: From the new tree, add the children that are new w.r.t. the
     *         original.
     */
    private List<AXIComponent> synchronize(AXIComponent original, AXIComponent altered) {
        //first remove the removed children
        List<AXIComponent> modifiedChildren = removeRemovedChildren(original, altered);
        
        //add new children
        addNewChildren(original, altered);
        
        return modifiedChildren;
    }
    
    /**
     * Removes the list of children, that no longer exists in the altered tree.
     */
    private List<AXIComponent> removeRemovedChildren(AXIComponent original, AXIComponent altered) {
        List<AXIComponent> removedChildren = new ArrayList<AXIComponent>();
        List<AXIComponent> dirtyChildren = new ArrayList<AXIComponent>();
        for(AXIComponent oChild : original.getChildren()) {
            int index = childExists(oChild, altered, true);
            if( (index == -1) ||
                oChild.getPeer().getParent() == null ||
                oChild.getPeer().getModel() == null) {
                removedChildren.add(oChild);
                continue;
            }
            dirtyChildren.add(oChild);
        }
        for(AXIComponent child : removedChildren) {
            original.removeChild(child);
        }
        
        return dirtyChildren;
    }
    
    /**
     * Finds all newly added children and surgically inserts
     * them into the original component at appropriate position.
     */
    private void addNewChildren(AXIComponent original, AXIComponent altered) {
        int size = altered.getChildren().size();
        for(int index=0; index<size; index++) {
            AXIComponent aChild = altered.getChildren().get(index);
            int indexInOriginalTree = childExists(aChild, original, false);
            //this is a new child, add it to the original tree            
            if(indexInOriginalTree == -1) {
                if(original.getComponentType() == ComponentType.PROXY) {
                    AXIComponent proxy = model.getComponentFactory().createProxy(aChild);
                    original.addChildAtIndex(proxy, index); //items from other model/file.
                } else {
                    original.addChildAtIndex(aChild, index); //same model/file.
                }
                continue;
            }
            //if found, remove these transient objects as listeners.
            if(aChild.getComponentType() == ComponentType.PROXY) {
                aChild.getSharedComponent().removeListener(aChild);
            }
        }
    }
    
    /**
     * Checks if the specified component exists as a child of the given parent.
     * Returns a non negative index if found, -1 otherwise.
     */
    private int childExists(AXIComponent child, AXIComponent parent, boolean checkOriginal) {
        int size = parent.getChildren().size();
        for(int index=0; index<size; index++) {
            AXIComponent c = parent.getChildren().get(index);
            if(c.getPeer() == child.getPeer()) {
                if(checkOriginal) {
                    if(!validateOriginal(child, c))
                        return -1;
                }
                return index;
            }
        }
        
        return -1;
    }
    
    /**
     * Validates the original. First, it checks for a valid peer.
     * Then some more sanity checks.
     */
    private boolean validateOriginal(AXIComponent original, AXIComponent altered) {
        //first pass: validate the peer
        PeerValidator validator = new PeerValidator();
        if(!validator.validate(original))
            return false;
        
        //altered child is a proxy where as original is not.
        //possible that codegen creates a GCT(SEQ(LE))) and sets
        //the peer of SEQ and LE to arbitrary AXI components.
        //These components should be removed and then be added as proxies.
        if(altered.getComponentType() == ComponentType.PROXY &&
           original.getComponentType() != ComponentType.PROXY) {
            return false;
        }
        
        return true;
    }
        
    /**
     * Visit the AXIDocument.
     */
    public void visit(AXIDocument document) {
        Util.updateAXIDocument(document);        
        visitChildren(document);
    }
    
    /**
     * Visit an element.
     */
    public void visit(Element element) {
        if(element instanceof ElementImpl)
            visit((ElementImpl)element);
        if(element instanceof ElementRef)
            visit((ElementRef)element);
        if(element instanceof ElementProxy)
            ((ElementProxy)element).forceFireEvent();
    }
        
    /**
     * For an ElementImpl, add and remove child means that type
     * changed, in which case we should recreate children,
     * Same if the type changed.
     */
    public void visit(ElementImpl element) {
        SchemaComponent schemaComponent = element.getPeer();
        if(schemaComponent instanceof LocalElement)
            Util.updateLocalElement(element);
        if(schemaComponent instanceof GlobalElement)
            Util.updateGlobalElement(element);
                
        //if type changed, update children
        SchemaComponent oldType = element.getTypeSchemaComponent();
        SchemaComponent newType = Util.getSchemaType(model, element.getPeer());
        if(oldType != newType) {
            AXIType axiType = Util.getAXIType(element, newType);
            element.setType(axiType);
            return;
        }
        //type didnt change: sync children as well.
        visitChildren(element);
    }
                
    public void visit(ElementRef elementRef) {
        ElementReference ref = (ElementReference)elementRef.getPeer();
        GlobalElement newGE = ref.getRef().get();
        SchemaComponent originalGE = elementRef.getReferent().getPeer();
        if(originalGE == newGE) {
            Util.updateElementReference(elementRef);
            elementRef.forceFireEvent();
            visitChildren(elementRef);
            return;
        }
        //the element ref now points to a different global element
        AXIComponent newElement = Util.lookup(elementRef.getModel(), newGE);
        if(newElement != null && newElement instanceof Element) {
            elementRef.setRef((Element)newElement);
            elementRef.forceFireEvent();
        }
    }

    public void visit(Attribute attribute) {
        if(attribute instanceof AttributeImpl)
            visit((AttributeImpl)attribute);
        if(attribute instanceof AttributeRef)
            visit((AttributeRef)attribute);
        if(attribute instanceof AttributeProxy)
            ((AttributeProxy)attribute).forceFireEvent();
    }
    
    public void visit(AttributeImpl attribute) {
        SchemaComponent schemaComponent = attribute.getPeer();
        if(schemaComponent instanceof LocalAttribute)
            Util.updateLocalAttribute(attribute);
        if(schemaComponent instanceof GlobalAttribute)
            Util.updateGlobalAttribute(attribute);
    }
    
    public void visit(AttributeRef attributeRef) {
        AttributeReference ref = (AttributeReference)attributeRef.getPeer();
        SchemaComponent originalGA = attributeRef.getReferent().getPeer();
        GlobalAttribute newGA = ref.getRef().get();
        if(originalGA == newGA) {
            Util.updateAttributeReference(attributeRef);
            attributeRef.forceFireEvent();
            return;
        }
        
        //the attribute ref now points to a different global attribute
        AXIComponent newAttr = Util.lookup(attributeRef.getModel(), newGA);
        if(newAttr != null && newAttr instanceof Attribute) {
            attributeRef.setRef((Attribute)newAttr);
            attributeRef.forceFireEvent();
        }
    }
    
    public void visit(Compositor compositor) {
        Util.updateCompositor(compositor);
        visitChildren(compositor);
    }    
    
    public void visit(ContentModel contentModel) {
        Util.updateContentModel(contentModel);
        visitChildren(contentModel);
    }
    
    public void visit(AnyAttribute attribute) {
        Util.updateAnyAttribute(attribute);
    }
    
    public void visit(AnyElement element) {
        Util.updateAnyElement(element);
    }

    ////////////////////////////////////////////////////////////////////
    ////////////////////////// member variables ////////////////////////
    ////////////////////////////////////////////////////////////////////
    private AXIModelImpl model;
    private boolean syncCompleted;
}
