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
package org.netbeans.modules.j2ee.jpa.verification.fixes;

import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.modules.j2ee.jpa.model.AccessType;
import org.netbeans.modules.j2ee.jpa.model.JPAAnnotations;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Tomasz.Slota@Sun.COM
 */
public class CreateOneToManyRelationshipHint extends AbstractCreateRelationshipHint {
    
    public CreateOneToManyRelationshipHint(FileObject fileObject,
            ElementHandle<TypeElement> classHandle,
            AccessType accessType,
            String localAttrName,
            String targetEntityClassName) {
        super(fileObject, classHandle, accessType, localAttrName, targetEntityClassName,
                JPAAnnotations.ONE_TO_MANY, JPAAnnotations.MANY_TO_ONE);
    }
    
    @Override protected CreateRelationshipPanel.AvailableSelection getAvailableRelationTypeSelection() {
        return CreateRelationshipPanel.AvailableSelection.INVERSE_ONLY;
    }
}
