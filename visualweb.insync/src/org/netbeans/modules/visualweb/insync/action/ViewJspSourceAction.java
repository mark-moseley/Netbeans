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


package org.netbeans.modules.visualweb.insync.action;

import com.sun.rave.designtime.DesignBean;
import com.sun.rave.designtime.DesignContext;
import org.netbeans.modules.visualweb.insync.live.LiveUnit;
import org.netbeans.modules.visualweb.insync.models.FacesModel;
import org.netbeans.modules.visualweb.spi.designtime.idebridge.action.AbstractDesignBeanAction;
import org.openide.ErrorManager;
import org.openide.cookies.EditCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.NbBundle;

/**
 * Action showing the jsp source.
 *
 * @author Peter Zavadsky
 */
public class ViewJspSourceAction  extends AbstractDesignBeanAction {

    /** Creates a new instance of ViewJspSourceAction */
    public ViewJspSourceAction() {
    }

    protected String getDisplayName(DesignBean[] designBeans) {
        return NbBundle.getMessage(ViewJspSourceAction.class, "LBL_ViewJspSourceAction");
    }

    protected String getIconBase(DesignBean[] designBeans) {
        return null;
    }

    protected boolean isEnabled(DesignBean[] designBeans) {
        return getJspFileEditCookie(designBeans) != null;
    }

    protected void performAction(DesignBean[] designBeans) {
        EditCookie editCookie = getJspFileEditCookie(designBeans);
        if (editCookie == null) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
                    new IllegalStateException("Open cookie on jsp file is null for designBeans=" + designBeans)); // NOI18N
        } else {
            editCookie.edit();
        }
    }


    private static EditCookie getJspFileEditCookie(DesignBean[] designBeans) {
        if (designBeans.length == 0) {
            return null;
        }
        DesignContext context = designBeans[0].getDesignContext();
        // XXX Casting is error-prone.
        FacesModel facesModel = ((LiveUnit)context).getModel();
        FileObject jspFile = facesModel.getMarkupFile();

        if (jspFile == null) {
            return null;
        }

        try {
            DataObject jspDataObject = DataObject.find(jspFile);
            return (EditCookie)jspDataObject.getCookie(EditCookie.class);
        } catch (DataObjectNotFoundException e) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
        }

        return null;
    }
}
