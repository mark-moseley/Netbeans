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

package org.netbeans.modules.xml.xam;


import org.openide.util.Lookup;
/**
 * This is the class that encapsulates the physical file for each model.
 * @author girix
 */
public class ModelSource implements Lookup.Provider {
    
    private Lookup lookup;
    private boolean editable;
    
    /**
     * Create a model source object given the lookup context.  If editable is false
     * the model cannot be mutated.  Note that editable is static attribute of the
     * model source, and does not reflect the access attribute of the associated file.
     *
     * @param lookup Lookup object associated with this ModelSource. Lookup minimally 
     * contains a File path of the backing file of the model and a javax.swing.text.Document object.
     * @param editable whether the model is supposed to be mutated.
     */
    public ModelSource(Lookup lookup, boolean editable){
        this.editable = editable;
        this.lookup = lookup;
    }
    
    /**
     * Returns the lookup object associated with this ModelSource. Lookup minimally 
     * contains a File absolute path or FileObject of the backing file of the model 
     * and javax.swing.text.Document object.  If model is DOM, the lookup should 
     * also contains javax.xml.transform.Source object for use in cases of relative 
     * resolution of resource such as validation.
     */
    public Lookup getLookup(){
        return lookup;
    }
    
    /**
     * States if the backing file can be edited.
     * @return true if the model source file is writable.
     */
    public boolean isEditable(){
        return editable;
    }
}
