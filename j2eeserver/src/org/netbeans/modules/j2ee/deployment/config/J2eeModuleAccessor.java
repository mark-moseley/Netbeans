/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.j2ee.deployment.config;

import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleImplementation;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.openide.util.Exceptions;

/**
 * Utility class for accessing some of the non-public methods of the J2eeModule.
 * 
 * @author sherold
 */
public abstract class J2eeModuleAccessor {
    
    public static J2eeModuleAccessor DEFAULT;
    
    // force loading of J2eeModule class. That will set DEFAULT variable.
    static {
        try {
            Object o = Class.forName("org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule", true, J2eeModuleAccessor.class.getClassLoader()); // NOI18N
        } catch (ClassNotFoundException cnf) {
            Exceptions.printStackTrace(cnf);
        }
    }
    
    /**
     * Factory method that creates a J2eeModule for the J2eeModuleImplementation.
     * 
     * @param impl SPI J2eeModuleImplementation object
     * 
     * @return J2eeModule for the J2eeModuleImplementation.
     */
    public abstract J2eeModule createJ2eeModule(J2eeModuleImplementation impl);
    
    /**
     * Returns the J2eeModuleProvider that belongs to the given j2eeModule.
     * 
     * @param j2eeModule J2eeModule
     * 
     * @return J2eeModuleProvider that belongs to the given j2eeModule.
     */
    public abstract J2eeModuleProvider getJ2eeModuleProvider(J2eeModule j2eeModule);
    
    /**
     * Associates the J2eeModuleProvider with the spcecified J2eeModule.
     * 
     * @param j2eeModule J2eeModule
     * @param J2eeModuleProvider J2eeModuleProvider that belongs to the given J2eeModule.
     */
    public abstract void setJ2eeModuleProvider(J2eeModule j2eeModule, J2eeModuleProvider j2eeModuleProvider);
}
