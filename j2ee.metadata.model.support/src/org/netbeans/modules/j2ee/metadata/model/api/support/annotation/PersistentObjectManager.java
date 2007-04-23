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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.metadata.model.api.support.annotation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.ElementHandle;

/**
 *
 * @author Andrei Badea
 */
public class PersistentObjectManager<T extends PersistentObject> {

    private static final Logger LOGGER = Logger.getLogger(PersistentObjectManager.class.getName());

    private final AnnotationModelHelper helper;
    private final ObjectProvider<T> provider;
    private final Map<ElementHandle<TypeElement>, T> objects = new HashMap<ElementHandle<TypeElement>, T>();

    private boolean initialized = false;

    PersistentObjectManager(AnnotationModelHelper helper, ObjectProvider<T> provider) {
        this.helper = helper;
        this.provider = provider;
    }

    public AnnotationModelHelper getHelper() {
        return helper;
    }

    private void ensureInitialized() {
        if (!initialized) {
            LOGGER.log(Level.FINE, "intializing"); // NOI18N
            for (T object : provider.createInitialObjects()) {
                LOGGER.log(Level.FINE, "created object {0}", object); // NOI18N
                objects.put(object.getSourceElementHandle(), object);
            }
            initialized = true;
        }
    }

    public Collection<T> getObjects() {
        ensureInitialized();
        Collection<T> values = objects.values();
        LOGGER.log(Level.FINE, "getObjects returning {0} objects", values.size()); // NOI18N
        return values;
    }

    void typesAdded(Iterable<? extends ElementHandle<TypeElement>> typeHandles) {
        LOGGER.log(Level.FINE, "typesAdded called with {0}", typeHandles); // NOI18N
        List<TypeElement> types = new ArrayList<TypeElement>();
        for (ElementHandle<TypeElement> typeHandle : typeHandles) {
            TypeElement type = typeHandle.resolve(helper.getCompilationController());
            if (type == null) {
                continue;
            }
            T object = provider.createObject(type);
            if (object != null) {
                LOGGER.log(Level.FINE, "typesAdded: new object {0}", object); // NOI18N
                objects.put(object.getSourceElementHandle(), object);
            }
        }
    }

    void typesRemoved(Iterable<? extends ElementHandle<TypeElement>> typeHandles) {
        LOGGER.log(Level.FINE, "typesRemoved called with {0}", typeHandles); // NOI18N
        for (ElementHandle<TypeElement> typeHandle : typeHandles) {
            T object = objects.remove(typeHandle);
            if (object != null) {
                LOGGER.log(Level.FINE, "typesRemoved: removing object {0}", object); // NOI18N
            }
        }
    }

    void typesChanged(Iterable<? extends ElementHandle<TypeElement>> typeHandles) {
        LOGGER.log(Level.FINE, "typesChanged called with {0}", typeHandles); // NOI18N
        for (ElementHandle<TypeElement> typeHandle : typeHandles) {
            T object = objects.get(typeHandle);
            if (object != null) {
                boolean valid = object.sourceElementChanged();
                if (valid) {
                    LOGGER.log(Level.FINE, "typesChanged: changing object {0}", object); // NOI18N
                } else {
                    objects.remove(typeHandle);
                    LOGGER.log(Level.FINE, "typesChanged: removing object {0}", object); // NOI18N
                }
            } else {
                TypeElement type = typeHandle.resolve(helper.getCompilationController());
                if (type != null) {
                    T newObject = provider.createObject(type);
                    if (newObject != null) {
                        objects.put(newObject.getSourceElementHandle(), newObject);
                        LOGGER.log(Level.FINE, "typesChanged: new object {0}", newObject); // NOI18N
                    }
                }
            }
        }
    }

    void rootsChanged() {
        LOGGER.log(Level.FINE, "rootsChanged called"); // NOI18N
        initialized = false;
        objects.clear();
    }
}
