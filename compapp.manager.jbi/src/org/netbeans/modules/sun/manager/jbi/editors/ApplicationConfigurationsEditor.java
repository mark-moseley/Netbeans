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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.sun.manager.jbi.editors;


import javax.management.openmbean.CompositeData;
import javax.management.openmbean.TabularType;
import org.netbeans.modules.sun.manager.jbi.management.model.OldJBIComponentConfigurationDescriptor;


/**
 * A property editor for Applicaiton Configuration TabularData
 * (with partial inline rendering and no inline editing).
 *
 * @author jqian
 */
public class ApplicationConfigurationsEditor extends SimpleTabularDataEditor {
    
    private String[] keys;
    
    /**
     * Constructs a property editor for Application Configurations.
     * 
     * @param tableLabelText        label for the table
     * @param tablelabelDescription description for the table
     * @param tabularType           type of the tabular data
     * @param keys                  key columns in the tabular type
     */
    public ApplicationConfigurationsEditor(String tableLabelText,
            String tableLabelDescription, TabularType tabularType,
            String[] keys, OldJBIComponentConfigurationDescriptor descriptor,
            boolean isWritable){
        super(tableLabelText, tableLabelDescription, tabularType, descriptor,
                isWritable);
        this.keys = keys;
    }
    
    @Override
    protected String getStringForRowData(CompositeData rowData) {
        String ret = "["; // NOI18N
        for (String key : keys) {
            ret += "@" + rowData.get(key);  // NOI18N
        }
        ret += "]"; // NOI18N
        return ret;
    }
    
    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        assert false; // see attachEnv
    }
}

