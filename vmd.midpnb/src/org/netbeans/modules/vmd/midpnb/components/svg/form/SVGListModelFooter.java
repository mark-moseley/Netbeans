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
package org.netbeans.modules.vmd.midpnb.components.svg.form;

import java.util.List;

import org.netbeans.modules.vmd.api.codegen.CodeReferencePresenter;
import org.netbeans.modules.vmd.api.codegen.MultiGuardedSection;
import org.netbeans.modules.vmd.api.model.PropertyValue;
import org.netbeans.modules.vmd.api.model.PropertyValue.Kind;
import org.netbeans.modules.vmd.midp.codegen.CodeClassInitHeaderFooterPresenter;


/**
 * @author ads
 *
 */
public class SVGListModelFooter extends CodeClassInitHeaderFooterPresenter {

    /* (non-Javadoc)
     * @see org.netbeans.modules.vmd.midp.codegen.CodeClassInitHeaderFooterPresenter#generateClassInitializationFooter(org.netbeans.modules.vmd.api.codegen.MultiGuardedSection)
     */
    @Override
    public void generateClassInitializationFooter( MultiGuardedSection section ) {
        PropertyValue value = null;
        if ( getComponent().getType().equals( SVGListCD.TYPEID) ){
            value = getComponent().readProperty( SVGListCD.PROP_MODEL);
        }
        else if (getComponent().getType().equals( SVGComboBoxCD.TYPEID) ){
            value = getComponent().readProperty( SVGComboBoxCD.PROP_MODEL);
        }
        if ( value == null ){
            return;
        }
        if ( value.getKind().equals( Kind.USERCODE )){
            section.getWriter().write( CodeReferencePresenter.
                    generateDirectAccessCode(getComponent()) +".setModel("+
                    value.getUserCode()+");\n");     // NOI18N
        }
        else {
            section.getWriter().write("java.util.Vector vector = new java.util.Vector();\n");  //NOI18N
            List<PropertyValue> list = value.getArray();
            
            if ( list == null ){
                return;
            }
            
            for (PropertyValue propertyValue : list) {
                String item = propertyValue.getPrimitiveValue().toString();
                section.getWriter().write( "vector.addElement(\""+ item +"\");\n");
            }
            
            section.getWriter().write( CodeReferencePresenter.
                    generateDirectAccessCode(getComponent()) +".setModel(");
            if ( getComponent().getType().equals( SVGListCD.TYPEID) ){
                section.getWriter().write( "new org.netbeans.microedition.svg." +
                        "SVGList.DefaultListModel(vector));\n");  // NOI18N
            }
            else if ( getComponent().getType().equals( SVGComboBoxCD.TYPEID) ){
                section.getWriter().write( "new org.netbeans.microedition.svg." +
                        "SVGComboBox.DefaultModel(vector));\n");  // NOI18N
            }
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.vmd.midp.codegen.CodeClassInitHeaderFooterPresenter#generateClassInitializationHeader(org.netbeans.modules.vmd.api.codegen.MultiGuardedSection)
     */
    @Override
    public void generateClassInitializationHeader( MultiGuardedSection section ) {
    }

}
