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

package org.netbeans.modules.j2ee.persistence.provider;

import java.util.Collections;
import java.util.Map;
import org.openide.util.NbBundle;

/**
 * This class represents Toplink provider.
 *
 * @author Erno Mononen
 */
class ToplinkProvider extends Provider{
    
    /**
     * There are two valid provider classes for TopLink, i.e. 
     * <code>oracle.toplink.essentials.PersistenceProvider</code> and 
     * <code>oracle.toplink.essentials.ejb.cmp3.EntityManagerFactoryProvider</code>. 
     * The former is preferred, whereas the latter is needed for compatibility reasons since
     * it was used in 5.5.
     */ 
    private static final String PREFERRED_PROVIDER_CLASS = "oracle.toplink.essentials.PersistenceProvider"; //NOI18N
    private static final String ALTERNATIVE_PROVIDER_CLASS = "oracle.toplink.essentials.ejb.cmp3.EntityManagerFactoryProvider";//NOI18N

    /**
     * Creates a new instance using the preferred provider class.
     * 
     * @see #PREFERRED_PROVIDER_CLASS
     */ 
    static ToplinkProvider create(){
        return new ToplinkProvider(PREFERRED_PROVIDER_CLASS);
    }
    
    /**
     * Creates a new instance using the provider class used in NetBeans 5.5. Note
     * that this is just for compatiblity, otherwise it is recommended to use 
     * {@link #create()} instead.
     * 
     * @see #ALTERNATIVE_PROVIDER_CLASS
     */ 
    static ToplinkProvider create55Compatible(){
        return new ToplinkProvider(ALTERNATIVE_PROVIDER_CLASS);
    }
    
    private ToplinkProvider(String providerClass){
        super(providerClass); //NOI18N
    }
    
    public String getDisplayName() {
        return NbBundle.getMessage(ToplinkProvider.class, "LBL_TopLink"); //NOI18N
    }
    
    public String getJdbcUrl() {
        return "toplink.jdbc.url";
    }

    public String getJdbcDriver() {
        return "toplink.jdbc.driver";
    }

    public String getJdbcUsername() {
        return "toplink.jdbc.user";
    }

    public String getJdbcPassword() {
        return "toplink.jdbc.password";
    }

    public String getTableGenerationPropertyName() {
        return "toplink.ddl-generation";
    }

    public String getTableGenerationDropCreateValue() {
        return "drop-and-create-tables";
    }

    public String getTableGenerationCreateValue() {
        return "create-tables";
    }

    public Map getUnresolvedVendorSpecificProperties() {
        return Collections.EMPTY_MAP;
    }

    public Map getDefaultVendorSpecificProperties() {
        return Collections.EMPTY_MAP;
    }
    
    
}
