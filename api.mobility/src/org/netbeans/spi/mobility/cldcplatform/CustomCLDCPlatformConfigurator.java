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

/*
 * CustomCLDCPlatformConfigurator.java
 *
 */
package org.netbeans.spi.mobility.cldcplatform;

import java.io.File;

/**
 * CustomCLDCPlatformConfigurator is an SPI for service providing information about some non-standard CLDC platform (SDK, emulator).
 * This interface has to be implemented and registered in module META-INF/services/org.netbeans.spi.mobility.cldcplatform.CustomCLDCPlatformConfigurator
 * #position=xx attribute of the registration is important if two different CustomCLDCPlatformConfigurator implementation recognize the same platform.
 * @author Adam Sotona
 */
public interface CustomCLDCPlatformConfigurator {
    
    /**
     * This method must provide just quick answer if the given folder might be a home for a platform recognized by this configurator.
     * This method is not intended to perform any deep detection.
     * The best way is to just check for any unique files inside.
     * @param platformPath Given platform home directory to query.
     * @return True if this configurator recognizes the folder as possible known platform home directory.
     */
    public boolean isPossiblePlatform(File platformPath);
    
    /**
     * This method is called when the previous for deep detection of the platform.
     * The method should return full platform descriptor or null.
     * @param platformPath Given platform home for the detection.
     * @return CLDCPlatformDescriptor with full information about the platform or null.
     */
    public CLDCPlatformDescriptor getPlatform(File platformPath);
    
    /**
     * Optional method helping the automated platforms detection using Windows registry.
     * If the platform installer stores any keys pointing to the platform installation directory then this method is usefull.
     * Usual pattern is to store such information somewhere under HKEY_LOCAL_MACHINE/Software/&lt;provider name&gt; or HKEY_CURRENT_USER/Software/&lt;provider name&gt;
     * @return Part of the provider name that may help to locate registry key with reference to the platform installation (f.ex.: Nokia)
     */
    public String getRegistryProviderName();
    
}
