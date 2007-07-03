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
package org.netbeans.modules.xslt.tmap.model.api;

import java.util.List;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xslt.tmap.model.impl.TMapComponents;

/**
 *
 * @author Vitaly Bychkov
 * @version 1.0
 */
public interface Transform extends TMapComponent, ReferenceCollection {
    TMapComponents TYPE = TMapComponents.TRANSFORM;
   
    String FILE = "file"; // NOI18N
    String SOURCE = "source"; // NOI18N
    String RESULT = "result"; // NOI18N
    
    String getFile();
    
    void setFile(String file);
    
    VariableReference getSource();
    
    void setSource(String source);
    
    VariableReference getResult();

    void setResult(String result);
    
    List<Param> getParams();
    
    void addParam(Param param);
    
    void removeParam(Param param);
}
