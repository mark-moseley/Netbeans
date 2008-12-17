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

package org.netbeans.modules.websvc.design.view.actions;

import java.awt.event.ActionEvent;
import java.io.IOException;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import org.netbeans.modules.websvc.api.jaxws.project.config.Service;
import org.netbeans.modules.websvc.api.support.AddOperationCookie;
import org.netbeans.modules.websvc.core.AddWsOperationHelper;
import org.netbeans.modules.websvc.core._RetoucheUtil;
import org.openide.ErrorManager;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author Ajit Bhate
 */
public class AddOperationAction extends AbstractAction implements AddOperationCookie {
    
    private FileObject implementationClass;
    private Service service;
    /**
     * Creates a new instance of AddOperationAction
     * @param implementationClass fileobject of service implementation class
     */
    public AddOperationAction(Service service, FileObject implementationClass) {
        super(getName());
        putValue(SMALL_ICON, new ImageIcon(ImageUtilities.loadImage
            ("org/netbeans/modules/websvc/design/view/resources/operation.png")));
        putValue(SHORT_DESCRIPTION, NbBundle.getMessage(AddOperationAction.class, "Hint_AddOperation"));
        putValue(MNEMONIC_KEY, Integer.valueOf(NbBundle.getMessage(AddOperationAction.class, "LBL_AddOperation_mnem_pos")));
        this.service=service;
        this.implementationClass = implementationClass;
    }
    
    private static String getName() {
        return NbBundle.getMessage(AddOperationAction.class, "LBL_AddOperation");
    }
    
    public void actionPerformed(ActionEvent arg0) {
        try{
            // no need to create new task or progress handle, as strategy does it.
            addJavaMethod();
        }catch(IOException e){
            ErrorManager.getDefault().notify(e);
        }
    }
    
    private void saveImplementationClass(FileObject implementationClass) throws IOException{
        DataObject dobj = DataObject.find(implementationClass);
        if(dobj.isModified()) {
            SaveCookie cookie = dobj.getCookie(SaveCookie.class);
            if(cookie!=null) cookie.save();
        }
    }
    
    private void addJavaMethod() throws IOException{
        AddWsOperationHelper strategy = new AddWsOperationHelper(getName());
        String className = _RetoucheUtil.getMainClassName(implementationClass);
        if (className != null) {
            strategy.addMethod(implementationClass, className);
            saveImplementationClass(implementationClass);
        }
    }

    public void addOperation() {
        actionPerformed(null);
    }

    @Override
    public boolean isEnabledInEditor(Lookup nodeLookup) {
        return service != null && service.getWsdlUrl() == null;
    }
}
