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

import org.netbeans.modules.xslt.model.Text;
import org.netbeans.modules.xslt.model.XslComponent;
import org.netbeans.modules.xslt.model.XslVisitor;
import org.netbeans.modules.xslt.model.enums.TBoolean;
import org.w3c.dom.Element;


/**
 * @author ads
 *
 */
public class TextImpl extends SequenceElementImpl implements Text {

    TextImpl( XslModelImpl model, Element element ) {
        super( model , element );
    }
    
    TextImpl( XslModelImpl model ) {
        super( model , XslElements.TEXT );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.model.impl.XslComponentImpl#accept(org.netbeans.modules.xslt.model.XslVisitor)
     */
    @Override
    public void accept( XslVisitor visitor )
    {
        visitor.visit( this );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.model.impl.XslComponentImpl#getComponentType()
     */
    @Override
    public Class<? extends XslComponent> getComponentType()
    {
        return Text.class;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.model.DisableOutputExcapingSpec#getDisableOutputEscaping()
     */
    public TBoolean getDisableOutputEscaping() {
        return TBoolean.forString( getAttribute( 
                XslAttributes.DISABLE_OUTPUT_ESCAPING));
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.model.DisableOutputExcapingSpec#setDisableOutputEscaping(org.netbeans.modules.xslt.model.enums.TBoolean)
     */
    public void setDisableOutputEscaping( TBoolean value ) {
        setAttribute( XslAttributes.DISABLE_OUTPUT_ESCAPING, value);
    }

}
