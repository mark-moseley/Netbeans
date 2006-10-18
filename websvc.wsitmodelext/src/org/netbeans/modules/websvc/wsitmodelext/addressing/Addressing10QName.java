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
 * Software is Sun Microsystems, Inc. Portions Copyright 2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.modules.websvc.wsitmodelext.addressing;

import javax.xml.namespace.QName;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Martin Grebac
 */
public enum Addressing10QName {
    ENDPOINTREFERENCE(createAddressingQName("EndpointReference")),              //NOI18N
    USINGADDRESSING(createAddressingQName("UsingAddressing")),                  //NOI18N
    ANONYMOUS(createAddressingQName("Anonymous")),                              //NOI18N
    ADDRESS(createAddressingQName("Address")),                                  //NOI18N
    ADDRESSINGMETADATA(createAddressingQName("Metadata")),                      //NOI18N
    REFERENCEPROPERTIES(createAddressingQName("ReferenceProperties"));          //NOI18N

    public static final String ADDRESSING10_NS_URI = "http://www.w3.org/2005/08/addressing";  //NOI18N
    public static final String ADDRESSING10_NS_PREFIX = "wsaw";                 //NOI18N
    
    public static QName createAddressingQName(String localName){
        return new QName(ADDRESSING10_NS_URI, localName, ADDRESSING10_NS_PREFIX);
    }
    
    Addressing10QName(QName name) {
        qName = name;
    }
    
    public QName getQName(){
        return qName;
    }
    private static Set<QName> qnames = null;
    public static Set<QName> getQNames() {
        if (qnames == null) {
            qnames = new HashSet<QName>();
            for (Addressing10QName wq : values()) {
                qnames.add(wq.getQName());
            }
        }
        return qnames;
    }
    private final QName qName;

}
