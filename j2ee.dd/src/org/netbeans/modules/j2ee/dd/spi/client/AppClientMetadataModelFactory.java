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

package org.netbeans.modules.j2ee.dd.spi.client;

import org.netbeans.modules.j2ee.dd.api.client.AppClientMetadata;
import org.netbeans.modules.j2ee.dd.impl.client.annotation.AppClientMetadataModelImpl;
import org.netbeans.modules.j2ee.dd.spi.MetadataUnit;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.metadata.model.spi.MetadataModelFactory;

/**
 * Factory for application client metadata.
 * @author Tomas Mysik
 */
public class AppClientMetadataModelFactory {
    
    private AppClientMetadataModelFactory() {
    }
    
    /**
     * Create metadata model of application client.
     * @param metadataUnit XXX ???
     * @return metadata model of application client.
     */
    public static MetadataModel<AppClientMetadata> createMetadataModel(MetadataUnit metadataUnit) {
        return MetadataModelFactory.createMetadataModel(new AppClientMetadataModelImpl(metadataUnit));
    }
}
