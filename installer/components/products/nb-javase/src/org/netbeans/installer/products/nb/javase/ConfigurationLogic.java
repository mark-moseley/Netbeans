/*
 * The contents of this file are subject to the terms of the Common Development and
 * Distribution License (the License). You may not use this file except in compliance
 * with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html or
 * http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file and
 * include the License file at http://www.netbeans.org/cddl.txt. If applicable, add
 * the following below the CDDL Header, with the fields enclosed by brackets []
 * replaced by your own identifying information:
 * 
 *     "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original Software
 * is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun Microsystems, Inc. All
 * Rights Reserved.
 */
package org.netbeans.installer.products.nb.javase;

import org.netbeans.installer.product.components.NbClusterConfigurationLogic;
import org.netbeans.installer.utils.exceptions.InitializationException;

/**
 *
 * @author Kirill Sorokin
 */
public class ConfigurationLogic extends NbClusterConfigurationLogic {
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    private static final String JAVA_CLUSTER = 
            "{java-cluster}"; // NOI18N
    private static final String APISUPPORT_CLUSTER = 
            "{apisupport-cluster}"; // NOI18N
    private static final String HARNESS_CLUSTER = 
            "{harness-cluster}"; // NOI18N
    private static final String ID = 
            "JAVASE"; // NOI18N
    
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    public ConfigurationLogic() throws InitializationException {
        super(new String[]{
            JAVA_CLUSTER, 
            APISUPPORT_CLUSTER, 
            HARNESS_CLUSTER}, ID);
    }
}
