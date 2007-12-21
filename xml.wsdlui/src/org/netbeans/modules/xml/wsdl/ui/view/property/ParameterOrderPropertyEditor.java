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

package org.netbeans.modules.xml.wsdl.ui.view.property;

import java.awt.Component;
import java.beans.FeatureDescriptor;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyEditorSupport;
import java.util.List;

import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.wsdl.ui.view.treeeditor.OperationNode;
import org.netbeans.modules.xml.xam.ui.XAMUtils;
import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.explorer.propertysheet.PropertyEnv;

/**
 *
 * @author radval
 *
 */
public class ParameterOrderPropertyEditor extends PropertyEditorSupport implements ExPropertyEditor {

    /** Environment passed to the ExPropertyEditor*/
    private PropertyEnv env;
    
    private OperationNode mOperationNode;
    
    private Operation mOperation;
    
    public ParameterOrderPropertyEditor(OperationNode operationNode) {
        this.mOperationNode = operationNode;
        this.mOperation = (Operation) mOperationNode.getWSDLComponent();
    }
    
    /**
     * This method is called by the IDE to pass
     * the environment to the property editor.
     * @param env Environment passed by the ide.
     */
    public void attachEnv(PropertyEnv ev) {
        this.env = ev;
        FeatureDescriptor desc = env.getFeatureDescriptor();
        // make this is not editable  
        desc.setValue("canEditAsText", Boolean.FALSE); // NOI18N
    }
    
    @Override
    public String getAsText() {
        List<String> order = mOperation.getParameterOrder();
        if (order == null) return "";
        
        StringBuilder builder = new StringBuilder();
        for (String str : order) {
            builder.append(str).append(" ");
        }
        return builder.toString().trim();
    }
    
    /** @return tags */
    @Override
    public String[] getTags() {
        return null;
    }
    
    /** @return true */
    @Override
    public boolean supportsCustomEditor () {
        return XAMUtils.isWritable(mOperation.getModel());
    }
    
    /** @return editor component */
    @Override
    public Component getCustomEditor () {
        final ParameterOrderPropertyPanel editor = new ParameterOrderPropertyPanel(mOperation, env);
        env.addPropertyChangeListener(new PropertyChangeListener() {
            
            public void propertyChange(PropertyChangeEvent evt) {
                if ((PropertyEnv.PROP_STATE.equals(evt.getPropertyName()) && evt.getNewValue() == PropertyEnv.STATE_VALID)) {
                    setValue(editor.getParameterOrder());
                }
            }
        
        });
        return editor;
    }
}

