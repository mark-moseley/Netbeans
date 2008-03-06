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

/**
 *
 */
package org.netbeans.modules.bpel.model.impl.references;

import java.util.Collection;
import java.util.LinkedList;

import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.model.api.Import;
import org.netbeans.modules.bpel.model.api.references.SchemaReference;
import org.netbeans.modules.xml.xpath.ext.schema.ExNamespaceContext;
import org.netbeans.modules.bpel.model.impl.Utils;
import org.netbeans.modules.bpel.model.xam.spi.ExternalModelRetriever;
import org.netbeans.modules.xml.schema.model.GlobalComplexType;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.GlobalSimpleType;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.schema.model.ReferenceableSchemaComponent;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.schema.model.SchemaModelFactory;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent;
import org.netbeans.modules.xml.xam.dom.Attribute;
import org.openide.util.Lookup;
import org.openide.util.Lookup.Result;


/**
 * @author ads
 *
 */
public final class SchemaReferenceBuilder {

    private SchemaReferenceBuilder() {
        Result result = Lookup.getDefault().lookup(
                new Lookup.Template(ExternalModelRetriever.class));
        myRetrievers = result.allInstances();
        
        myCollection = new LinkedList<SchemaReferenceFactory>();
        myCollection.add( new SchemaElementFactory() );
        myCollection.add( new SchemaTypeFactory() );
    }
    
    public static SchemaReferenceBuilder getInstance() {
        return INSTANCE;
    }
    
    public <T extends ReferenceableSchemaComponent> SchemaReference<T> 
            build( Class<T> clazz ,AbstractDocumentComponent entity , 
                    Attribute attr )
    {
        SchemaReference<T>  ref = build( clazz , entity , 
                entity.getAttribute( attr ) );
        if ( ref instanceof MappedReference ){
            ((MappedReference)ref).setAttribute( attr );
        }
        return ref;
    }
    
    public <T extends ReferenceableSchemaComponent> SchemaReference<T> build( 
            Class<T> clazz ,AbstractDocumentComponent entity , String refString )
    {
        if ( refString == null ){
            return null;
        }
        for (SchemaReferenceFactory resolver : myCollection) {
            if ( resolver.isApplicable( clazz )){
                return resolver.createUnresolvedReference(clazz, entity,
                        refString);
            }
        }
        return null;
    }
    
    public <T extends ReferenceableSchemaComponent> SchemaReference<T> build( 
            T target , Class<T> clazz , AbstractDocumentComponent entity  )
    {
        for (SchemaReferenceFactory resolver : myCollection) {
            if ( resolver.isApplicable( clazz )){
                return resolver.create( target , clazz , entity );
            }
        }
        return null;
    }
    
    public BpelAttributesType.AttrType getAttributeType( Attribute attr ) {
        /*Class clazz = null;
        if ( List.class.isAssignableFrom( attr.getType() )){
            clazz = attr.getMemberType();
        }
        else {
            clazz = attr.getType();
        }
        for (SchemaReferenceFactory resolver : myCollection) {
            if ( resolver.isApplicable( clazz )){
                return resolver.getAttributeType();
            }
        }*/
        return BpelAttributesType.AttrType.QNAME;
    }
    
    public void setAttribute( SchemaReference ref , Attribute attr ) {
        if ( ref instanceof MappedReference ) {
            ((MappedReference)ref).setAttribute( attr );
        }
    }
    
    public static Collection<SchemaModel> getSchemaModels( 
            AbstractDocumentComponent entity , String prefix )
    {
        assert entity instanceof BpelEntity;
        ExNamespaceContext context = ((BpelEntity)entity).getNamespaceContext();
        String nsUri = context.getNamespaceURI( prefix );
        Collection<SchemaModel> collection = null;
        
        /*
         * Fix due Nikita request. For xsd primitive types we need to
         * use preexisted primitive model. 
         */ 
        if ( Import.SCHEMA_IMPORT_TYPE.equals(nsUri)) {
            collection = new LinkedList<SchemaModel>();
            collection.add( SchemaModelFactory.getDefault().
                    getPrimitiveTypesModel() );
        }
        Collection<SchemaModel> moreModels = getSchemaModels( 
                ((BpelEntity)entity).getBpelModel() , nsUri );
        if ( collection == null ) {
            collection = moreModels;
        }
        else {
            collection.addAll( moreModels );
        }
        return collection; 
    }
    
    public static Collection<SchemaModel> getSchemaModels( BpelModel model , 
            String namespace )
    {
        return getInstance().getModels(model, namespace);
    }

    private Collection<SchemaModel> getModels( BpelModel model, String namespace )
    {
        Collection<SchemaModel> ret = new LinkedList<SchemaModel>();
        if ( myRetrievers.size() == 1) {
            return ((ExternalModelRetriever)myRetrievers.iterator().next()).
                getSchemaModels(model, namespace);
        }
        for ( Object obj : myRetrievers ) {
            ExternalModelRetriever retriever = (ExternalModelRetriever)obj;
            Collection<SchemaModel> collection = 
                retriever.getSchemaModels(model, namespace);
            ret.addAll( collection );
        }
        return ret;
    }
    
    interface SchemaResolver {
        <T extends ReferenceableSchemaComponent> T resolve(
                AbstractNamedComponentReference<T> reference );
    }

    private static final SchemaReferenceBuilder INSTANCE = 
        new SchemaReferenceBuilder();
    
    private static Collection myRetrievers;
    
    private Collection<SchemaReferenceFactory> myCollection;
}

interface SchemaReferenceFactory extends SchemaReferenceBuilder.SchemaResolver{
    
    <T extends ReferenceableSchemaComponent> boolean isApplicable( Class<T> clazz);
    
    <T extends ReferenceableSchemaComponent> SchemaReference<T> create( T target,
            Class<T> clazz , AbstractDocumentComponent entity );
    
    <T extends ReferenceableSchemaComponent> SchemaReference<T> 
        createUnresolvedReference( Class<T> clazz, AbstractDocumentComponent entity, 
                String refString );
    
    <T extends ReferenceableSchemaComponent> SchemaReference<T> create( T target,
            Class<T> clazz, AbstractDocumentComponent entity, String refString );
}

abstract class AbstractSchemaReferenceFactory implements SchemaReferenceFactory{

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.impl.references.SchemaReferenceFactory#create(T, java.lang.Class, AbstractDocumentComponent, java.lang.String)
     */
    public <T extends ReferenceableSchemaComponent> SchemaReference<T> create( 
            T target, Class<T> clazz, AbstractDocumentComponent entity, 
            String refString ) 
    {
        return create( target, clazz, entity );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.impl.references.SchemaReferenceFactory#create(T, java.lang.Class, AbstractDocumentComponent)
     */
    public <T extends ReferenceableSchemaComponent> SchemaReference<T> create( 
            T target, Class<T> clazz, AbstractDocumentComponent entity ) 
    {
        return new SchemaReferenceImpl<T>( target, clazz , entity , this );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.impl.references.SchemaReferenceFactory#createUnresolvedReference(java.lang.Class, org.netbeans.modules.xml.xam.AbstractDocumentComponent, java.lang.String)
     */
    public <T extends ReferenceableSchemaComponent> SchemaReference<T> 
        createUnresolvedReference( Class<T> clazz, AbstractDocumentComponent entity, 
                String refString ) 
    {
        return new SchemaReferenceImpl<T>( clazz , entity , refString , this );
    }
    
}

class SchemaElementFactory extends AbstractSchemaReferenceFactory {

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.impl.references.SchemaReferenceFactory#isApplicable(java.lang.Class)
     */
    public <T extends ReferenceableSchemaComponent> boolean isApplicable( 
            Class<T> clazz ) 
    {
        return GlobalElement.class.isAssignableFrom(clazz);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.impl.references.SchemaReferenceFactory#resolve(java.lang.Class, org.netbeans.modules.xml.xam.AbstractDocumentComponent, java.lang.String)
     */
    public <T extends ReferenceableSchemaComponent> T resolve( 
            AbstractNamedComponentReference<T> reference ) 
    {
        AbstractDocumentComponent entity = 
            (AbstractDocumentComponent)reference.getParent();
        String refString = reference.getRefString();
        Class<T> clazz = reference.getType();
        
        String[] splited = new String[2];
        Utils.splitQName( refString , splited );
        Collection<SchemaModel> collection = SchemaReferenceBuilder.
            getSchemaModels(entity, splited[0]);
        for (SchemaModel model : collection) {
            Collection<GlobalElement> elements = model.getSchema()
                    .getElements();
            for (GlobalElement element : elements) {
                if (splited[1].equals( element.getName())) {
                    return clazz.cast(element);
                }
            }
        }
        return null;
    }
    
}

class SchemaTypeFactory extends AbstractSchemaReferenceFactory {

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.impl.references.SchemaReferenceFactory#isApplicable(java.lang.Class)
     */
    public <T extends ReferenceableSchemaComponent> boolean isApplicable( 
            Class<T> clazz ) 
    {
        return GlobalType.class.isAssignableFrom(clazz);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.impl.references.SchemaReferenceFactory#resolve(java.lang.Class, org.netbeans.modules.xml.xam.AbstractDocumentComponent, java.lang.String)
     */
    public <T extends ReferenceableSchemaComponent> T resolve( 
            AbstractNamedComponentReference<T> reference ) 
    {
        AbstractDocumentComponent entity = 
            (AbstractDocumentComponent)reference.getParent();
        String refString = reference.getRefString();
        Class<T> clazz = reference.getType();
        
        String[] splited = new String[2];
        Utils.splitQName( refString , splited );
        Collection<SchemaModel> collection = SchemaReferenceBuilder.
            getSchemaModels(entity, splited[0]);
        for (SchemaModel model : collection) {
            Collection<GlobalSimpleType> simpleTypes = model.getSchema()
                    .getSimpleTypes();
            for (GlobalSimpleType simpleType : simpleTypes) {
                if (splited[1].equals(simpleType.getName())) {
                    return clazz.cast(simpleType);
                }
            }
            Collection<GlobalComplexType> complexTypes = model.getSchema()
                    .getComplexTypes();
            for (GlobalComplexType complexType : complexTypes) {
                if (splited[1].equals(complexType.getName())) {
                    return clazz.cast(complexType);
                }
            }
        }
        return null;
    }
}