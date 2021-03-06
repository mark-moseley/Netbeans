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
package org.netbeans.modules.visualweb.dataconnectivity;

import java.awt.Image;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.openide.ErrorManager;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;


/** A BeanInfo for dataconnectivity settings.
*
* @author Joel Brown
*/
public class DataconnectivitySettingsBeanInfo extends SimpleBeanInfo {
    /** Provides an explicit property info. */
    PropertyDescriptor[] desc = null ;
    public PropertyDescriptor[] getPropertyDescriptors() {
        if ( desc == null ) {
            try {
                desc =
                    new PropertyDescriptor[] {
                        new PropertyDescriptor(DataconnectivitySettings.PROP_MAKE_IN_SESSION, DataconnectivitySettings.class,
                            "getMakeInSession", "setMakeInSession"), // NOI18N
                        new PropertyDescriptor(DataconnectivitySettings.PROP_CHECK_ROWSET, DataconnectivitySettings.class,
                            "getCheckRowSetProp", "setCheckRowSetProp"), // NOI18N
                        new PropertyDescriptor(DataconnectivitySettings.PROP_PROMPT_FOR_NAME, DataconnectivitySettings.class,
                            "getPromptForName", "setPromptForName"), // NOI18N
                        new PropertyDescriptor(DataconnectivitySettings.PROP_DATAPROVIDER, DataconnectivitySettings.class,
                            "getDataProviderSuffixProp", "setDataProviderSuffixProp"), // NOI18N
                        new PropertyDescriptor(DataconnectivitySettings.PROP_ROWSET, DataconnectivitySettings.class,
                            "getRowSetSuffixProp", "setRowSetSuffixProp"), // NOI18N
                    };

                desc[0].setDisplayName(NbBundle.getMessage(DataconnectivitySettings.class, "PROP_MAKE_IN_SESSION"));
                desc[0].setShortDescription(NbBundle.getMessage(DataconnectivitySettings.class, "PROP_MAKE_IN_SESSION"));

                desc[1].setDisplayName(NbBundle.getMessage(DataconnectivitySettings.class, "PROP_CHECK_ROWSET"));
                desc[1].setShortDescription(NbBundle.getMessage(DataconnectivitySettings.class, "PROP_CHECK_ROWSET"));

                desc[2].setDisplayName(NbBundle.getMessage(DataconnectivitySettings.class, "PROP_PROMPT_FOR_NAME"));
                desc[2].setShortDescription(NbBundle.getMessage(DataconnectivitySettings.class, "PROP_PROMPT_FOR_NAME"));

                desc[3].setDisplayName(NbBundle.getMessage(DataconnectivitySettings.class, "PROP_DATAPROVIDER"));
                desc[3].setShortDescription(NbBundle.getMessage(DataconnectivitySettings.class, "PROP_DATAPROVIDER"));

                desc[4].setDisplayName(NbBundle.getMessage(DataconnectivitySettings.class, "PROP_ROWSET"));
                desc[4].setShortDescription(NbBundle.getMessage(DataconnectivitySettings.class, "PROP_ROWSET"));

            } catch (IntrospectionException ex) {
                ErrorManager.getDefault().notify(ex);

                desc = null ;
            }
        }
        return desc ;
    }

    /** Returns the designer icon */
    public Image getIcon(int type) {
        return Utilities.loadImage("org/netbeans/modules/visualweb/dataconnectivity/resources/datasource.png"); // NOI18N
    }
}
