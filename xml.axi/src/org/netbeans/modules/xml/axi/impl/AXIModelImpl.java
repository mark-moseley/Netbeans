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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.WeakHashMap;
import org.netbeans.modules.xml.axi.AXIComponent;
import org.netbeans.modules.xml.axi.AXIDocument;
import org.netbeans.modules.xml.axi.AXIModel;
import org.netbeans.modules.xml.axi.AXIModelFactory;
import org.netbeans.modules.xml.axi.SchemaGenerator;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.schema.model.SchemaModelReference;
import org.netbeans.modules.xml.xam.ComponentListener;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.locator.CatalogModelException;
import org.netbeans.modules.xml.xam.spi.Validation;
import org.netbeans.modules.xml.xam.spi.Validator.ResultItem;
import org.netbeans.modules.xml.xam.spi.Validator.ResultType;
import org.openide.util.WeakListeners;


/**
 * Represents an AXI model for a schema.
 * It keeps a map of AXI elements against schema global elements.
 *
 * @author Samaresh (Samaresh.Panda@Sun.Com)
 */
public class AXIModelImpl extends AXIModel {
            
    /**
     * Creates a new instance AXIModelImpl.
     */
    public AXIModelImpl(ModelSource modelSource) {
        super(modelSource);
        
        //create and add listener to listen to this model changes
        this.propertyListener = new AXIModelListener();
        addPropertyChangeListener(propertyListener);
        
        //create and add listener to listen to schema model changes
        this.schemaModelListener = new SchemaModelListener(this);
        this.getSchemaModel().addPropertyChangeListener(schemaModelListener);
    }
    
    /**
     * Returns true if the AXIDocument has been initialized, false otherwise.
     */
    boolean isAXIDocumentInitialized() {
        return isAXIDocumentInitialized;
    }
    
    /**
     * Initializes AXIDocument.
     */
    void initializeAXIDocument() {
        AXIDocument doc = getRoot();
        Schema schema = (Schema)doc.getPeer();
        if(schema == null) {
            doc.setPeer(getSchemaModel().getSchema());
        }
        Util.updateAXIDocument(doc);
        isAXIDocumentInitialized = true;
        updateReferencedModelListener();
				
        //initialize schema design pattern
        SchemaGenerator.Pattern dp = null;
        if(dp != null)
            setSchemaDesignPattern(dp);
        else
            setSchemaDesignPattern(SchemaGenerator.DEFAULT_DESIGN_PATTERN);
    }

    /**
     * Retunrs true if the specified schema component and this AXI model
     * belong to the same schema model. False otherwise.
     */
    public boolean fromSameSchemaModel(SchemaComponent schemaComponent) {
        return (getSchemaModel() == schemaComponent.getModel());
    }
        
    /**
     * Returns the global AXI component from other AXI model.
     */
    public AXIComponent lookupFromOtherModel(SchemaComponent schemaComponent) {
        assert(schemaComponent.isInDocumentModel());
        AXIModelFactory factory = AXIModelFactory.getDefault();
        AXIModelImpl model = (AXIModelImpl)factory.getModel(schemaComponent.getModel());
        return model!=null?model.lookup(schemaComponent):null;
    }    
    
    /**
     * Returns the global AXI component against the specified global schema component.
     */
    public AXIComponent lookup(SchemaComponent schemaComponent) {        
        for(AXIComponent globalChild : getRoot().getChildren()) {
            if(globalChild.getPeer() == schemaComponent) {
                return globalChild;
            }
        }
        
        return null;
    }
        
    /**
     * Check if sync is really required or not. True for the very first time.
     * Returns false, if user started a design pattern transformation or if the
     * model was mutated inside a transaction. Else, true if the listeners have
     * accumulated events.
     */
    protected boolean needsSync() {
        if(!isAXIDocumentInitialized())
            return true;
        
        if(designPatternMode || isIntransaction()) {
            return false;
        }
        
        if(axiModelListener != null)
           return axiModelListener.needsSync() || schemaModelListener.needsSync();
        
        return schemaModelListener.needsSync();
    }
        
    void disableAutoSync() {
        designPatternMode = true;
        super.getAccess().setAutoSync(false);
    }
    
    void enableAutoSync() {
        designPatternMode = false;
        super.getAccess().setAutoSync(true);
    }
    
    /**
     * Sync started.
     */
    protected void syncStarted() {
        try {
            getSchemaModel().sync();
        } catch(IOException ex) {
            setState(Model.State.NOT_SYNCED);
        }
    }
    
    /**
     * Finished sync. Clear the event lists.
     */
    protected void syncCompleted() {
        schemaModelListener.syncCompleted();
        if(axiModelListener != null)
            axiModelListener.syncCompleted();
        propertyListener.clearEvents();
    }
    
    /**
     * This is where the actual sync starts.
     * Returns true if success, false if failed.
     */
    public synchronized boolean doSync() {        
        //update the referenced AXIModels
        updateReferencedModelListener();
        
        //finally sync itself.
        AXIModelUpdater updater = new AXIModelUpdater(this);
        return updater.doSync();        
    }
    
    private void updateReferencedModelListener() {
        for(AXIModel m: getReferencedModels()) {
            listenToReferencedModel(m);
        }
    }

    public SchemaGenerator.Pattern getSchemaDesignPattern() {
        return schemaDesignPattern;
    }
	
    public void setSchemaDesignPattern(SchemaGenerator.Pattern schemaDesignPattern) {
        this.schemaDesignPattern = schemaDesignPattern;
    }	
	
    /**
     * Returns the PCL who listens to this model changes.
     */
    public AXIModelListener getPropertyChangeListener() {
        return propertyListener;
    }
    
    /**
     * Runs the validator to see if the schema is valid or not.
     * Returns true for a valid schema, false otherwise.
     */
    public boolean validate() {
        Model.State state = getSchemaModel().getState();
        if(state != SchemaModel.State.VALID) {
            return false;
        }
        
        Validation validation = new Validation();
        validation.validate(getSchemaModel(), Validation.ValidationType.COMPLETE);
        List<ResultItem> results = validation.getValidationResult();
        if(results == null || results.size() == 0)
            return true;
        
        for(ResultItem i : results) {
            if(i.getType() == ResultType.ERROR)
                return false;
        }
        
        return true;
    }    
        
    public void listenToReferencedModel(AXIModel model) {
        if(listenerMap.get(model) == null) {
            ComponentListener listener = (ComponentListener)WeakListeners.
                    create(ComponentListener.class, axiModelListener, model);
            model.addComponentListener(listener);
            listenerMap.put(model, listener);
        }
    }
    
    public String toString() {
        if(getRoot() == null)
            return null;
        
        return getRoot().getTargetNamespace();
    }

    /**
     * Returns other AXIModels this model refers to.
     */
    public List<AXIModel> getReferencedModels() {
        List<AXIModel> models = null;
        Collection<SchemaModelReference> refs = getSchemaModel().
                getSchema().getSchemaReferences();
        if(refs == null || refs.size() == 0) {
            models = Collections.emptyList();
            return models;
        }
        models = new ArrayList<AXIModel>();
        Iterator<SchemaModelReference> iter = refs.iterator();
        while(iter.hasNext()) {
            try {
                SchemaModelReference ref = iter.next();
                AXIModel m = AXIModelFactory.getDefault().
                        getModel(ref.resolveReferencedModel());
                models.add(m);
            } catch (CatalogModelException ex) {
                //will not be added to the list
            }
        }
        
        return Collections.unmodifiableList(models);
    }
        
    /**
     * PCL to be used by code generator.
     */
    private AXIModelListener propertyListener;
	
    /*
     * Schema model listener.
     */
    private SchemaModelListener schemaModelListener; 

    /*
     * AXI Model listener to listen to other AXI models.
     */
    private OtherAXIModelListener axiModelListener = new OtherAXIModelListener(this);
    private WeakHashMap<AXIModel, ComponentListener> listenerMap = 
            new WeakHashMap<AXIModel, ComponentListener>();
        
    /*
     * True, when design patten transformation is being carried out.
     */
    private boolean designPatternMode = false;
        
    /*
     * Flag to indicate if the AXIDocument was initialized or no.
     */
    private boolean isAXIDocumentInitialized = false;

    private SchemaGenerator.Pattern schemaDesignPattern;
}
