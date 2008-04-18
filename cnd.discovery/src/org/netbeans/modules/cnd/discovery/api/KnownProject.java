/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.discovery.api;

import java.util.Map;
import org.openide.util.Lookup;

/**
 *
 * @author Alexander Simon
 */
public abstract class KnownProject {
    /** well known project name */
    public static final String PROJECT = "Project"; // NOI18N
    /** path to well known project sources */
    public static final String ROOT = "Root"; // NOI18N
    /** path to created netbeans projects */
    public static final String NB_ROOT = "NBProject"; // NOI18N

    private static KnownProject DEFAULT = new Default();

    public abstract boolean canCreate(Map<String,String> parameters);
    public abstract boolean create(Map<String,String> parameters);

    protected KnownProject() {
    }
    
    /**
     * Static method to obtain the CsmSelect implementation.
     * @return the selector
     */
    public static synchronized KnownProject getDefault() {
        return DEFAULT;
    }
    
    /**
     * Implementation of the default creator
     */  
    private static final class Default extends KnownProject {
        private final Lookup.Result<KnownProject> res;
        Default() {
            res = Lookup.getDefault().lookupResult(KnownProject.class);
        }

        @Override
        public boolean canCreate(Map<String, String> parameters) {
            KnownProject creator = find(parameters);
            return creator != null;
        }
        @Override
        public boolean create(Map<String, String> parameters) {
            KnownProject creator = find(parameters);
            if (creator != null) {
                return creator.create(parameters);
                
            }
            return false;
        }

        private KnownProject find(Map<String, String> parameters) {
            for (KnownProject creator : res.allInstances()) {
                if (creator.canCreate(parameters)){
                    return creator;
                }
            }
            return null;
        }
    }
}
