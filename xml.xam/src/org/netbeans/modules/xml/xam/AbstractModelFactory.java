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

package org.netbeans.modules.xml.xam;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.xml.xam.spi.ModelAccessProvider;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Chris Webster
 * @author Nam Nguyen
 */
public abstract class AbstractModelFactory<M extends Model> {
    public AbstractModelFactory() {
        factories.add(new WeakReference<AbstractModelFactory>(this));
    }
    
    public static final int DELAY_SYNCER = 2000;  // milisecs.
    public static final int DELAY_DIRTY = 1000;  // milisecs.

    private WeakHashMap<Object, WeakReference<M>> cachedModels = 
	new WeakHashMap<Object,WeakReference<M>>();
    
    protected abstract M createModel(ModelSource source);
    

    /**
     * Create new model from given model source or null if there are errors.
     * The returned model might not be valid, i.e., source is well-formed. 
     * Note that the returned model is not cached so client code should handle 
     * sharing if needed.
     */
    public M createFreshModel(ModelSource modelSource) {
        M model = createModel(modelSource);
        try {
            if (model != null) {
                model.sync();
            }
        } catch (IOException ioe) {
            Logger.getLogger(this.getClass().getName()).log(Level.FINE, "Sync has errors", ioe);
        }
        return model;
    }
    
    /**
     * This method extracts the key from the model source. A subclass can 
     * change the ModelSource lookup requirements and thus this method may
     * be overridden to allow a different key to be used. 
     */
    protected Object getKey(ModelSource source) {
        ModelAccessProvider p = getEffectiveAccessProvider(source);
        if (p != null) {
            return p.getModelSourceKey(source);
        }
	return (File) source.getLookup().lookup(File.class);
    }
    
    private ModelAccessProvider getEffectiveAccessProvider(ModelSource ms) {
	ModelAccessProvider p = (ModelAccessProvider)
	    ms.getLookup().lookup(ModelAccessProvider.class);
	return p == null ? getAccessProvider() : p;
    }
    
    public static ModelAccessProvider getAccessProvider() {
        return (ModelAccessProvider) Lookup.getDefault().lookup(ModelAccessProvider.class);
    }
    
    protected synchronized M getModel(ModelSource source) {
        if (source == null) {
            return null;
        }
	Object key = getKey(source);
	assert key != null;
        WeakReference<M> modelRef = cachedModels.get(key);
        M model = (modelRef == null ? null : modelRef.get());
        if (model == null) {
            model = createModel(source);
            if (model != null) {
                try {
                    model.sync();
                } catch (IOException ioe) {
                    Logger.getLogger(this.getClass().getName()).log(Level.FINE, "Sync has errors", ioe);
                }
                cachedModels.put(key, new WeakReference<M>(model));
            }
        }
        return model;
    }

    private static List<WeakReference<AbstractModelFactory>> factories = 
            new ArrayList<WeakReference<AbstractModelFactory>>();
    
    private static RequestProcessor.Task SYNCER = null; 
    static {
        if (getAccessProvider() != null) {
            SYNCER = RequestProcessor.getDefault().post(
                new Runnable() {
                    public void run() {
                        try {
                            for (AbstractModel model : getAllModels()) {
                                if (model.isAutoSyncActive() && 
                                    model.getAccess().dirtyIntervalMillis() > DELAY_DIRTY) {
                                    model.runAutoSync();
                                }
                            }
                        } catch (Exception e) {
                            Logger.getLogger(getClass().getName()).log(Level.FINE, "auto-sync", e);
                        }
                        SYNCER.schedule(DELAY_SYNCER);
                    }
                }, DELAY_SYNCER);
        }
    }
    
    private synchronized static List<AbstractModel> getAllModels() {
        List<AbstractModel> models = new ArrayList<AbstractModel>();
        List<WeakReference<AbstractModelFactory>> mfactories = 
            new ArrayList<WeakReference<AbstractModelFactory>>(factories);
        
        for (WeakReference<AbstractModelFactory> r : mfactories) {
            AbstractModelFactory factory = r.get();
            if (factory != null) {
                for (Object m : factory.getModels()) {
                    if (m instanceof AbstractModel) {
                        AbstractModel am = (AbstractModel) m;
                        models.add(am);
                    }
                }
            }
        }
        return models;
    }
    
    public List<M> getModels() {
        List<WeakReference<M>> refs;
        synchronized(this) {
            refs = new ArrayList<WeakReference<M>>(cachedModels.values());
        }

        List<M> ret = new ArrayList<M>();
        for (WeakReference<M> ref : refs) {
            if (ref != null) {
                M model = ref.get();
                if (model != null) {
                    ret.add(model);
                }
            }
        }
        return ret;
    }
}

