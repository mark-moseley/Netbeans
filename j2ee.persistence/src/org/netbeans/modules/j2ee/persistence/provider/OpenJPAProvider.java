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
package org.netbeans.modules.j2ee.persistence.provider;

import java.util.Collections;
import java.util.Map;
import org.openide.util.NbBundle;

/**
 *
 * @author Erno Mononen
 */
class OpenJPAProvider extends Provider{

    public OpenJPAProvider() {
        super("org.apache.openjpa.persistence.PersistenceProviderImpl"); //NO18N
    }

    public String getDisplayName() {
        return NbBundle.getMessage(KodoProvider.class, "LBL_OpenJPA"); //NOI18N
    }
    
    public String getJdbcUrl() {
        return "openjpa.ConnectionURL";//NOI18N
    }
    
    public String getJdbcDriver() {
        return "openjpa.ConnectionDriverName";//NOI18N
    }
    
    public String getJdbcUsername() {
        return "openjpa.ConnectionUserName";//NOI18N
    }
    
    public String getJdbcPassword() {
        return "openjpa.ConnectionPassword";//NOI18N
    }

    public String getTableGenerationPropertyName() {
        return "openjpa.jdbc.SynchronizeMappings";//NOI18N
    }

    public String getTableGenerationDropCreateValue() {
        return "buildSchema(SchemaAction='add,deleteTableContents',ForeignKeys=true)";//NOI18N
    }

    public String getTableGenerationCreateValue() {
        return "buildSchema(ForeignKeys=true)";//NOI18N
    }

    public Map getUnresolvedVendorSpecificProperties() {
        return Collections.EMPTY_MAP;
    }

    public Map getDefaultVendorSpecificProperties() {
        return Collections.EMPTY_MAP;
    }

}
