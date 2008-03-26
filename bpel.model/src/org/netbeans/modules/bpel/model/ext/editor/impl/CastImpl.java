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
package org.netbeans.modules.bpel.model.ext.editor.impl;

import java.util.concurrent.atomic.AtomicReference;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.ExtensionEntity;
import org.netbeans.modules.bpel.model.api.events.VetoException;
import org.netbeans.modules.bpel.model.api.references.SchemaReference;
import org.netbeans.modules.bpel.model.api.support.EntityUpdater;
import org.netbeans.modules.bpel.model.ext.editor.api.Cast;
import org.netbeans.modules.bpel.model.ext.editor.api.Casts;
import org.netbeans.modules.bpel.model.ext.editor.api.Source;
import org.netbeans.modules.bpel.model.ext.editor.xam.EditorAttributes;
import org.netbeans.modules.bpel.model.ext.editor.xam.EditorElements;
import org.netbeans.modules.bpel.model.impl.BpelBuilderImpl;
import org.netbeans.modules.bpel.model.impl.BpelModelImpl;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.xam.ComponentUpdater.Operation;
import org.netbeans.modules.xml.xam.Reference;
import org.netbeans.modules.xml.xam.dom.Attribute;
import org.w3c.dom.Element;

/**
 *
 * @author Vitaly Bychkov
 * @version 1.0
 */
public class CastImpl extends EditorEntityImpl implements Cast {

    private static AtomicReference<Attribute[]> myAttributes =
        new AtomicReference<Attribute[]>();

    CastImpl(EditorEntityFactory factory, BpelModelImpl model, Element e ) {
        super(factory, model, e);
    }

    CastImpl(EditorEntityFactory factory, BpelBuilderImpl builder ) {
        super(factory, builder, EditorElements.CAST);
    }

    @Override
    protected BpelEntity create( Element element ) {
        return null;
    }

    protected Attribute[] getDomainAttributes() {
        if ( myAttributes.get() == null ){
            Attribute[] ret = new Attribute[] {
                EditorAttributes.SOURCE,
                EditorAttributes.PATH,
                EditorAttributes.TYPE
            };
            myAttributes.compareAndSet( null ,  ret);
        }
        return myAttributes.get();
    }

    public Class<? extends BpelEntity> getElementType() {
        return Cast.class;
    }

    public EntityUpdater getEntityUpdater() {
        return CastEntityUpdater.getInstance();
    }

    public Source getSource() {
        readLock();
        try {
            String str = getAttribute(EditorAttributes.SOURCE);
            return Source.forString(str);
        }
        finally {
            readUnlock();
        }
    }

    public void setSource(Source value) {
        setBpelAttribute(EditorAttributes.SOURCE, value);
    }

    public void removeSource() {
        removeAttribute(EditorAttributes.SOURCE);
    }

    public String getPath() {
        readLock();
        try {
            return getAttribute(EditorAttributes.SOURCE);
        }
        finally {
            readUnlock();
        }
    }

    public void setPath(String value) throws VetoException {
        setBpelAttribute(EditorAttributes.PATH, value);
    }

    public void removePath() {
        removeAttribute(EditorAttributes.PATH);
    }

    public SchemaReference<GlobalType> getType() {
        readLock();
        try {
            return getSchemaReference(EditorAttributes.TYPE, GlobalType.class);
        }
        finally {
            readUnlock();
        }
    }

    public void setType(SchemaReference<GlobalType> value) {
        setSchemaReference(EditorAttributes.TYPE, value);
    }

    public void removeType() {
        removeAttribute(EditorAttributes.TYPE);
    }

    private static class CastEntityUpdater implements EntityUpdater {
        private static EntityUpdater INSTANCE =
                new CastEntityUpdater();

        public static EntityUpdater getInstance() {
            return INSTANCE;
        }

        private CastEntityUpdater() {

        }

        public void update(BpelEntity target, ExtensionEntity child, Operation operation) {
            if (target instanceof Casts) {
                Casts casts = (Casts)target;
                Cast cast = (Cast)child;
                switch (operation) {
                case ADD:
                    casts.addCast(cast);
                    break;
                case REMOVE:
                    casts.remove(cast);
                    break;
                }
            }
        }

        public void update(BpelEntity target, ExtensionEntity child, int index, Operation operation) {
            if (target instanceof Casts) {
                Casts casts = (Casts)target;
                Cast cast = (Cast)child;
                switch (operation) {
                case ADD:
                    casts.insertCast(cast, index);
                    break;
                case REMOVE:
                    casts.remove(cast);
                    break;
                }
            }
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.references.ReferenceCollection#getReferences()
     */
    public Reference[] getReferences() {
        return new Reference[] { getType()};
    }
}

