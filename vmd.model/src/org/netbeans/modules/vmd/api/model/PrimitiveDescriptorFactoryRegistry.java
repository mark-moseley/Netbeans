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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.vmd.api.model;

import org.openide.util.Lookup;

/**
 * @author David Kaspar
 */
final class PrimitiveDescriptorFactoryRegistry {

    private static final Lookup.Result<PrimitiveDescriptorFactory> factoriesLookupResult = Lookup.getDefault ().lookupResult (PrimitiveDescriptorFactory.class);

    static PrimitiveDescriptor getDescriptor (String projectType, TypeID type) {
        assert Debug.isFriend (PropertyValue.class);
        assert type.getKind () == TypeID.Kind.PRIMITIVE;
        assert type.getDimension () == 0;

        for (PrimitiveDescriptorFactory factory : factoriesLookupResult.allInstances ()) {
            if (projectType ==  null  ||  ! projectType.equals (factory.getProjectType ()))
                continue;
            PrimitiveDescriptor descriptor = factory.getDescriptorForTypeIDString (type.getString ());
            if (descriptor != null)
                return descriptor;
        }

        Debug.warning ("No PrimitiveDescriptorFactory found", type); // NOI18N
        return null;
    }

}
