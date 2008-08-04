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

package org.netbeans.modules.cnd.makeproject.configurations.ui;

import java.beans.PropertyEditor;
import java.beans.PropertyEditorSupport;
import java.util.List;
import java.util.ResourceBundle;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.PackagingConfiguration;
import org.netbeans.modules.cnd.makeproject.packaging.FileElement;
import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.nodes.PropertySupport;
import org.openide.util.NbBundle;

public class PackagingNodeProp extends PropertySupport {
    private PackagingConfiguration packagingConfiguration;
    MakeConfiguration conf;
    
    public PackagingNodeProp(PackagingConfiguration packagingConfiguration, MakeConfiguration conf, String[] txts) {
        super(txts[0], List.class, txts[1], txts[2], true, true);
        this.packagingConfiguration = packagingConfiguration;
	this.conf = conf;
    }

//    public String getHtmlDisplayName() {
//        if (vectorConfiguration.getModified())
//            return "<b>" + getDisplayName(); // NOI18N
//        else
//            return null;
//    }
    
    public Object getValue() {
        return packagingConfiguration;
    }
    
    public void setValue(Object v) {
        packagingConfiguration = (PackagingConfiguration) v; // FIXUP
    }
    
//    public void restoreDefaultValue() {
//        vectorConfiguration.reset();
//    }
    
    @Override
    public boolean supportsDefaultValue() {
        return false;
    }
    
//    public boolean isDefaultValue() {
//        return vectorConfiguration.getValue().size() == 0;
//    }

    @Override
    public PropertyEditor getPropertyEditor() {
	return new Editor((PackagingConfiguration)packagingConfiguration);
    }

    @Override
    public Object getValue(String attributeName) {
        if (attributeName.equals("canEditAsText")) // NOI18N
            return Boolean.FALSE;
        return super.getValue(attributeName);
    }

    
    
    private class Editor extends PropertyEditorSupport implements ExPropertyEditor {
        private PackagingConfiguration packagingConfiguration;
        private PropertyEnv env;
        
        public Editor(PackagingConfiguration packagingConfiguration) {
            this.packagingConfiguration = packagingConfiguration;
        }
        
        @Override
        public void setAsText(String text) {
        }
        
        @Override
        public String getAsText() {
            int noFiles = packagingConfiguration.getFiles().getValue().size();
            String val = "" + noFiles + " " + getString("FilesText");
            if (noFiles > 0) {
                val += ": " + ((FileElement)packagingConfiguration.getFiles().getValue().get(0)).getFrom() + "..."; // NOI18N
            }
            return val;
        }
        
        @Override
        public java.awt.Component getCustomEditor() {
            return new PackagingPanel(packagingConfiguration, this, env, conf);
        }
        
        @Override
        public boolean supportsCustomEditor() {
            return true;
        }

        public void attachEnv(PropertyEnv env) {
            this.env = env;
        }
    }
    /** Look up i18n strings here */
    private static ResourceBundle bundle;
    private static String getString(String s) {
	if (bundle == null) {
	    bundle = NbBundle.getBundle(PackagingNodeProp.class);
	}
	return bundle.getString(s);
    }
}
