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

/*
 * Created on May 16, 2005
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.netbeans.modules.xml.wsdl.ui.api.property;

import java.awt.Component;
import java.beans.FeatureDescriptor;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyEditorSupport;

import org.netbeans.modules.xml.xam.ui.XAMUtils;
import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.explorer.propertysheet.PropertyEnv;



/**
 * @author radval
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class ElementOrTypeOrMessagePartPropertyEditor extends PropertyEditorSupport 
implements ExPropertyEditor, PropertyChangeListener {

    /** property name used by propertyChangeEvent */
    static final String PROP_NAME = "ElementOrType";//NOI18N

    /** Environment passed to the ExPropertyEditor*/
    private PropertyEnv mEnv;
    
    private ElementOrTypeOrMessagePartProvider mElementOrTypeOrMessagePartProvider;
    
    
    public ElementOrTypeOrMessagePartPropertyEditor(ElementOrTypeOrMessagePartProvider elementOrTypeProvider) {
        this.mElementOrTypeOrMessagePartProvider = elementOrTypeProvider;
    }
    
    /**
     * This method is called by the IDE to pass
     * the environment to the property editor.
     * @param env Environment passed by the ide.
     */
    public void attachEnv(PropertyEnv env) {
        this.mEnv = env;
        FeatureDescriptor desc = env.getFeatureDescriptor();
        // make this is not editable  
        desc.setValue("canEditAsText", Boolean.FALSE); // NOI18N
    }
    
    
    
    
    /** @return tags */
    @Override
    public String[] getTags() {
        return null;
    }
    
    /** @return true */
    @Override
    public boolean supportsCustomEditor () {
        return XAMUtils.isWritable(mElementOrTypeOrMessagePartProvider.getModel());
    }
    
    /** @return editor component */
    @Override
    public Component getCustomEditor () {
        ElementOrTypeOrMessagePartPropertyPanel editor = new ElementOrTypeOrMessagePartPropertyPanel(mElementOrTypeOrMessagePartProvider, this.mEnv);
        editor.addPropertyChangeListener(PROP_NAME, this);
        return editor;
    }
    /** handles property change
     *  
     * @param evt propertyChangeEvent
     */
    public void propertyChange(PropertyChangeEvent evt) {
        setValue(evt.getNewValue());
    }
}


