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
 * Software is Sun Microsystems, Inc. Portions Copyright 2004-2005 Sun
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

package org.netbeans.modules.jmx.actions;
import java.io.IOException;
import org.netbeans.api.java.source.JavaSource;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.netbeans.modules.jmx.*;
import org.netbeans.modules.jmx.actions.dialog.AddRegistIntfPanel;
import org.netbeans.modules.jmx.mbeanwizard.generator.AddRegistIntfGenerator;
import org.openide.cookies.EditorCookie;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;

/**
 * Action used to add MBeanRegistration implementation to an existing MBean.
 * @author tl156378
 */
public class AddRegisterIntfAction extends NodeAction {
    
    private DataObject dob;
    
    /**
     * Creates a new instance of UpdateAttrAction
     */
    public AddRegisterIntfAction() {
        putValue("noIconInMenu", Boolean.TRUE); // NOI18N
        
    }
    
    public boolean asynchronous() {
        return false; // yes, this action should run asynchronously
        // would be better to rewrite it to synchronous (running in AWT thread),
        // just replanning test generation to RequestProcessor
    }
    
    protected boolean enable(Node[] nodes) {
        if (nodes.length == 0) return false;
        dob = (DataObject) nodes[0].getLookup().lookup(DataObject.class);
        if(dob == null) return false;
        
        FileObject fo = dob.getPrimaryFile();
        if(fo == null)
            return false;
        
        JavaSource foClass = JavaModelHelper.getSource(fo);
        if (foClass == null) return false;
        try {
            boolean isMBean = JavaModelHelper.testMBeanCompliance(foClass);
            boolean hasMBeanRegistIntf = JavaModelHelper.implementsMBeanRegistrationItf(foClass);
            
            return isMBean && !hasMBeanRegistIntf;
        }catch(IOException ex) {
            ex.printStackTrace();
        }
        return false;
    }
    
    protected void performAction(Node[] nodes) {
        try {
            FileObject fo = null;
            if (dob != null) fo = dob.getPrimaryFile();
            
            // show configuration dialog
            // when dialog is canceled, escape the action
            AddRegistIntfPanel cfg = new AddRegistIntfPanel(nodes[0]);
            if (!cfg.configure()) {
                return;
            }
            AddRegistIntfGenerator generator = new AddRegistIntfGenerator();
            
            generator.update(fo, cfg.getKeepRefSelected());
            EditorCookie ec = (EditorCookie)dob.getCookie(EditorCookie.class);
            ec.open();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public HelpCtx getHelpCtx() {
        return new HelpCtx(""); // NOI18N
    }
    
    public String getName() {
        return NbBundle.getMessage(AddRegisterIntfAction.class,"LBL_Action_AddMBeanRegistrationIntf"); // NOI18N
    }
}
