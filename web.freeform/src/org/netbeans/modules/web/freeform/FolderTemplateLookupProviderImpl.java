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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.freeform;

import org.netbeans.api.project.Project;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.LookupProvider;
import org.netbeans.spi.project.ui.PrivilegedTemplates;

import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 * Special lookup provider to provide 'Folder' template as the last item in New
 * context menu of Web Freeform project
 * 
 * @author Milan Kubec
 */
public class FolderTemplateLookupProviderImpl implements LookupProvider {

    public FolderTemplateLookupProviderImpl() {
    }
    
    public Lookup createAdditionalLookup(Lookup baseContext) {
        
        Lookup retVal = Lookup.EMPTY;
        
        assert baseContext.lookup(Project.class) != null;
        AuxiliaryConfiguration aux = baseContext.lookup(AuxiliaryConfiguration.class);
        assert aux != null;
        
        if (isWebFFProject(aux)) {
            retVal = Lookups.fixed(new Object[] {
                new PrivilegedTemplatesImpl(),
            });
        }
        
        return retVal;
        
    }
    
    private boolean isWebFFProject(AuxiliaryConfiguration aux) {
        return aux.getConfigurationFragment(WebProjectNature.EL_WEB, WebProjectNature.NS_WEB_1, true) != null // NOI18N
               || aux.getConfigurationFragment(WebProjectNature.EL_WEB, WebProjectNature.NS_WEB_2, true) != null; // NOI18N
    }
    
    private static final class PrivilegedTemplatesImpl implements PrivilegedTemplates {
        
        private static final String[] PRIVILEGED_NAMES = new String[] {
            "Templates/Other/Folder",
        };
        
        public String[] getPrivilegedTemplates() {
            return PRIVILEGED_NAMES;
        }
        
    }
    
}
