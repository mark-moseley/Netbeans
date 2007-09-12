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
package org.netbeans.modules.soa.ui.form;

import org.netbeans.modules.soa.ui.form.valid.ValidStateManager;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;

/**
 * This interface describe subsidiary methods for a Custom Editor which
 * is intended to show property of a Node.
 *
 * @author nk160297
 */
public interface CustomNodeEditor<T>
        extends EditorLifeCycle, Lookup.Provider, ValidStateManager.Provider, 
        HelpCtx.Provider {

    /**
     * This string is used as a key to set property to different UI controls
     * which should be bound to a Node.Property.
     */
    String PROPERTY_BINDER = "PropertyBinder"; // NOI18N
    
    /**
     * Returns the Node which properties the editor shows. 
     */
    Node getEditedNode();

    /**
     * Returns the original object which is edited
     */
    T getEditedObject();
    
    /**
     * This method does part of standard steps to process Ok button. 
     * It is intended to be used internally by the NodeEditorDescriptor only.
     * It's recommend to avoid using the method. 
     * Returns the success flag.
     */
    boolean doValidateAndSave();
    
    /**
     * Indicates the current editting mode of the editor. 
     */ 
    EditingMode getEditingMode();
    
    /**
     * This method change the current editing mode. 
     * It should be used carefully!
     * Usually the editing mode can be change at initialization stage.
     */ 
    void setEditingMode(EditingMode newValue);
    
    enum EditingMode {
        NOT_SPECIFIED, 
        CREATE_NEW_INSTANCE, // The editor shows an object which is just created.
        EDIT_INSTANCE // The editor shows an old object.
    };
    
    interface Owner {
        void setEditor(CustomNodeEditor editor);
        CustomNodeEditor getEditor();
    }
}
