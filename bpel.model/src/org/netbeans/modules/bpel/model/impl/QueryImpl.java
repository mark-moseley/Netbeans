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
package org.netbeans.modules.bpel.model.impl;

import java.util.concurrent.atomic.AtomicReference;

import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.Query;
import org.netbeans.modules.bpel.model.api.events.VetoException;
import org.netbeans.modules.bpel.model.api.support.BpelModelVisitor;
import org.netbeans.modules.bpel.model.xam.BpelAttributes;
import org.netbeans.modules.bpel.model.xam.BpelElements;
import org.netbeans.modules.xml.xam.dom.Attribute;
import org.w3c.dom.Element;


/**
 * @author ads
 *
 */
public class QueryImpl extends BpelContainerImpl implements Query {


    QueryImpl( BpelModelImpl model, Element e ) {
        super(model, e);
    }

    QueryImpl( BpelBuilderImpl builder ) {
        super(builder, BpelElements.QUERY.getName() );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.api.BpelEntity#getElementType()
     */
    public Class<? extends BpelEntity> getElementType() {
        return Query.class;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.ContentElement#getContent()
     */
    public String getContent() {
        return getCorrectedText();
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.ContentElement#setContent(java.lang.String)
     */
    public void setContent( String content ) throws VetoException {
        setText( content );
    }


    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.api.QueryLanguageSpec#getQueryLanguage()
     */
    public String getQueryLanguage() {
        return getAttribute(BpelAttributes.QUERY_LANGUAGE);
    }


    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.api.QueryLanguageSpec#setQueryLanguage(java.lang.String)
     */
    public void setQueryLanguage( String value ) throws VetoException {
        setBpelAttribute(BpelAttributes.QUERY_LANGUAGE, value);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.api.QueryLanguageSpec#removeQueryLanguage()
     */
    public void removeQueryLanguage() {
        removeAttribute(BpelAttributes.QUERY_LANGUAGE);
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.impl.BpelContainerImpl#create(org.w3c.dom.Element)
     */
    @Override
    protected BpelEntity create( Element element )
    {
        return null;
    } 
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.api.BpelEntity#accept(org.netbeans.modules.bpel.model.api.support.BpelModelVisitor)
     */
    public void accept( BpelModelVisitor visitor ) {
        visitor.visit( this );
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.impl.BpelEntityImpl#getDomainAttributes()
     */
    protected Attribute[] getDomainAttributes() {
        if ( myAttributes.get() == null ){
            Attribute[] ret = new Attribute[] { BpelAttributes.QUERY_LANGUAGE ,
                    BpelAttributes.CONTENT };
            myAttributes.compareAndSet( null, ret);
        }
        return myAttributes.get();
    }
    
    private static AtomicReference<Attribute[]> myAttributes = 
        new AtomicReference<Attribute[]>();

}
