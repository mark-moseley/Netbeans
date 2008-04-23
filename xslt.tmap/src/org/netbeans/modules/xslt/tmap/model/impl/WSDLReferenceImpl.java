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
package org.netbeans.modules.xslt.tmap.model.impl;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import org.netbeans.modules.xml.wsdl.model.ReferenceableWSDLComponent;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.netbeans.modules.xslt.tmap.model.api.WSDLReference;
import org.netbeans.modules.xslt.tmap.model.impl.AttributesType.AttrType;

/**
 *
 * @author Vitaly Bychkov
 * @version 1.0
 */
public class WSDLReferenceImpl<T extends ReferenceableWSDLComponent>
    extends AbstractNamedComponentReference<T> 
    implements NamedComponentReference<T>, WSDLReference<T>
{


    WSDLReferenceImpl( T target, Class<T> type, AbstractDocumentComponent parent ,
            String refString , WSDLReferenceBuilder.WSDLResolver resolver )
    {
        super( type, parent, refString );
        setReferenced( target );
        myResolver = resolver;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xml.xam.Reference#get()
     */
    public T get() {
        if ( getReferenced() == null ){
            T ret = myResolver.resolve( this );
            setReferenced( ret );
            return ret;
        }
        return getReferenced();
    }

    @Override
    public AbstractDocumentComponent getParent() {
        return super.getParent();
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xml.xam.NamedComponentReference#getEffectiveNamespace()
     */
    public String getEffectiveNamespace() {
        if  ( !isResolved() ) {
            return XMLConstants.NULL_NS_URI;
        }
        return getReferenced().getModel().getDefinitions()
                .getTargetNamespace();
    }
    

    /* (non-Javadoc)
     * @see org.netbeans.modules.xml.xam.AbstractNamedComponentReference#getRefString()
     */
    @Override
    public String getRefString()
    {
        return refString;
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.xml.xam.dom.AbstractNamedComponentReference#getQName()
     */
    @Override
    public QName getQName()
    {
        return new QName( getEffectiveNamespace() , getRefString() );
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.impl.references.BpelAttributesType#getAttributeType()
     */
    public AttrType getAttributeType() {
        return AttrType.NCNAME;
    }

    private WSDLReferenceBuilder.WSDLResolver myResolver;
}
