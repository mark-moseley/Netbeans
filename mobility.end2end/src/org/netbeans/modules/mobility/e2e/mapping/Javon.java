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

package org.netbeans.modules.mobility.e2e.mapping;

import org.netbeans.modules.mobility.javon.JavonProfileProvider;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.mobility.javon.JavonTemplate;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author Michal Skvor
 */
public class Javon {
    
    private JavonMappingImpl mapping;
    
    /** 
     * Creates a new instance of Javon 
     * 
     * @param mapping 
     */
    public Javon( JavonMappingImpl mapping ) {
        this.mapping = mapping;
    }
    
    /**
     * Generate output
     */
    public void generate( ProgressHandle ph ) {
        // Get providers
        Lookup.Result<JavonProfileProvider> providersResult = 
                Lookup.getDefault().lookup( new Lookup.Template<JavonProfileProvider>(
                    JavonProfileProvider.class ));
        Map<String, JavonProfileProvider> providers = new HashMap<String, JavonProfileProvider>();
        for( JavonProfileProvider provider : providersResult.allInstances()) {
            providers.put( provider.getName(), provider );
        }
        if( providers.size() == 0 ) {
            // No providers
            return;
        }
        
        // TODO: Hack for default provider. Name should be set from the dialog
        JavonProfileProvider provider = providers.get( "default" ); // NOI18N
        
        List<JavonTemplate> templates = provider.getTemplates( mapping );
        
        // Run templates
        for( JavonTemplate template : templates ) {
            Set<String> targets = template.getTargets();
            for( String target : targets ) {
                template.generateTarget( ph, target );
            }
        }
        
        ph.finish();        
    }    
}
