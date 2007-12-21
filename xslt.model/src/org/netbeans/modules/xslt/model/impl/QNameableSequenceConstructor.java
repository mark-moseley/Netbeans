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
package org.netbeans.modules.xslt.model.impl;

import javax.xml.namespace.QName;

import org.netbeans.modules.xslt.model.QualifiedNameable;
import org.w3c.dom.Element;


/**
 * @author ads
 *
 */
abstract class QNameableSequenceConstructor extends SequenceConstructorImpl
        implements QualifiedNameable
{

    QNameableSequenceConstructor( XslModelImpl model, Element e ) {
        super(model, e);
    }

    QNameableSequenceConstructor( XslModelImpl model , XslElements type ){
        super( model , type );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.model.QualifiedNameable#getName()
     */
    public QName getName() {
        return QNameBuilder.createQName( this , XslAttributes.NAME );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.model.QualifiedNameable#setName(javax.xml.namespace.QName)
     */
    public void setName( QName name ) {
        setAttribute( XslAttributes.NAME , name );
    }

}
