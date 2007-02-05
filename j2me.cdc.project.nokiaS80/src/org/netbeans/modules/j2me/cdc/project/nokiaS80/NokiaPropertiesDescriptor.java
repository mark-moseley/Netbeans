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

package org.netbeans.modules.j2me.cdc.project.nokiaS80;

import java.util.Collections;
import java.util.Set;
import org.netbeans.api.mobility.project.PropertyDescriptor;
import org.netbeans.spi.mobility.project.ProjectPropertiesDescriptor;
import org.netbeans.spi.mobility.project.support.DefaultPropertyParsers;

/**
 *
 * @author Adam Sotona
 */
public class NokiaPropertiesDescriptor implements ProjectPropertiesDescriptor {
    
    public static final String NOKIA_APP_ICON = "nokiaS80.application.icon"; //NOI18N
    
    /** Creates a new instance of NokiaPropertiesDescriptor */
    public NokiaPropertiesDescriptor() {
    }
    
    public Set getPropertyDescriptors() {
        return Collections.singleton(new PropertyDescriptor(NOKIA_APP_ICON, true, DefaultPropertyParsers.STRING_PARSER,  "")); //NOI18N
    }
}
