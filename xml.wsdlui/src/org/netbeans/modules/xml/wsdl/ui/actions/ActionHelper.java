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
 * Created on Jul 6, 2005
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.netbeans.modules.xml.wsdl.ui.actions;

import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.ui.cookies.ViewComponentCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;

/**
 * @author radval
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class ActionHelper {

    /**
     * Show and select the Node which represents the given component.
     *
     * @param  comp  model component to select.
     */
    public static void selectNode(WSDLComponent comp) {
        if (comp == null) return;
        DataObject dobj = getDataObject(comp);
        if (dobj != null) {
            ViewComponentCookie cookie = dobj.getCookie(
                    ViewComponentCookie.class);
            if (cookie != null) {
                // Do not switch views, use the currently showing view.
                cookie.view(ViewComponentCookie.View.CURRENT, comp,
                        (Object[]) null);
            }
        }
    }
    
    
    public static void selectNode(SchemaComponent comp, WSDLModel model) {
        if (comp == null || model == null) return;
        DataObject dobj = getDataObject(model);
        if (dobj != null) {
            ViewComponentCookie cookie = dobj.getCookie(
                    ViewComponentCookie.class);
            if (cookie != null) {
                // Do not switch views, use the currently showing view.
                cookie.view(ViewComponentCookie.View.CURRENT, comp,
                        (Object[]) null);
            }
        }
    }
    
    public static DataObject getDataObject(Component comp) {
        try {
            Model model = comp.getModel();
            if (model != null) {
                FileObject fobj = model.getModelSource().
                        getLookup().lookup(FileObject.class);
                if (fobj != null) {
                    return DataObject.find(fobj);
                }
            }
        } catch (DataObjectNotFoundException donfe) {
            // fall through to return null
        }
        return null;
    }

    public static DataObject getDataObject(Model model) {
        try {
            if (model != null) {
                FileObject fobj = model.getModelSource().
                getLookup().lookup(FileObject.class);
                if (fobj != null) {
                    return DataObject.find(fobj);
                }
            }
        } catch (DataObjectNotFoundException donfe) {
            // fall through to return null
        }
        return null;
    }
	
}
