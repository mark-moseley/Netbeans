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
 * FixedProperty.java
 *
 * Created on January 5, 2006, 3:21 PM
 *
 */

package org.netbeans.modules.xml.schema.abe.nodes.properties;

import java.lang.reflect.InvocationTargetException;
import org.netbeans.modules.xml.axi.Element;
import org.netbeans.modules.xml.axi.Attribute;
import org.netbeans.modules.xml.axi.AXIComponent;
import org.netbeans.modules.xml.schema.ui.nodes.schema.GlobalElementNode;
import org.openide.ErrorManager;
import org.openide.util.NbBundle;


/**
 *
 * @author Ajit Bhate
 */
public class FixedProperty extends BaseABENodeProperty {
    
    /** Creates a new instance of FixedProperty */
    public FixedProperty(AXIComponent component, String propName,
            String propDesc) throws NoSuchMethodException {
        super(component,String.class,Element.PROP_FIXED,
                propName,propDesc,StringEditor.class);
    }
    
    public void setValue(Object o) throws 
            IllegalAccessException, InvocationTargetException{
        if(getComponent() instanceof Attribute) {
            if (((Attribute)getComponent()).getDefault()==null) {
                super.setValue(o);
            } else {
                throwError();
            }
        } else if(getComponent() instanceof Element) {
            if (((Element)getComponent()).getDefault()==null) {
                super.setValue(o);
            } else {
                throwError();
            }
        }
    }
    
    private void throwError(){
        String msg = NbBundle.getMessage(GlobalElementNode.class, "MSG_Fixed_DefaultAlreadySet"); //NOI18N
        IllegalArgumentException iae = new IllegalArgumentException(msg);
        ErrorManager.getDefault().annotate(iae, ErrorManager.USER,
                msg, msg, null, new java.util.Date());
        throw iae;
        
    }

}
