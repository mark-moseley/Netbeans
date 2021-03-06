
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


/*
 * RemoveResourceAction.java
 *
 * Created on April 6, 2007, 10:25 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.websvc.rest.wadl.design.view.actions;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.Set;
import javax.swing.AbstractAction;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.websvc.rest.wadl.model.*;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

/**
 *
 * @author Ayub Khan
 */
public class RemoveResourceAction extends AbstractAction{
    public static final String REMOVE_RESOURCES = "REMOVE_RESOURCES";
    
    private Set<Resource> resources;
    private WadlModel model;
    
    /** Creates a new instance of RemoveResourceAction */
    public RemoveResourceAction(Set<Resource> resources, WadlModel model) {
        super(getName());
        putValue(SHORT_DESCRIPTION, NbBundle.getMessage(RemoveResourceAction.class, "Hint_RemoveResource"));
        putValue(MNEMONIC_KEY, Integer.valueOf(NbBundle.getMessage(AddResourceAction.class, "LBL_RemoveResource_mnem_pos")));
        this.resources = resources;
        this.model = model;
//        setEnabled(false);
    }
    
    public void setWorkingSet(Set<Resource> resources) {
        this.resources = resources;
        setEnabled(resources!=null&&!resources.isEmpty());
    }

    public void actionPerformed(ActionEvent arg0) {
        if(resources == null || resources.isEmpty()) return;
        boolean singleSelection = resources.size()==1;
        String methodName = singleSelection?resources.iterator().next().getPath():""+resources.size();
        NotifyDescriptor desc = new NotifyDescriptor.Confirmation
                (NbBundle.getMessage(RemoveResourceAction.class, 
                (singleSelection?"MSG_RESOURCE_DELETE":"MSG_RESOURCES_DELETE"), methodName));
        Object retVal = DialogDisplayer.getDefault().notify(desc);
        if (retVal == NotifyDescriptor.YES_OPTION) {
            final ProgressHandle handle = ProgressHandleFactory.createHandle(NbBundle.
                    getMessage(RemoveResourceAction.class, 
                    (singleSelection?"MSG_RemoveResource":"MSG_RemoveResources"), methodName)); //NOI18N
//            Task task = new Task(new Runnable() {
//                public void run() {
                    handle.start();
                    try{
                        removeResource(resources);
                    }catch(IOException e){
                        handle.finish();
                        ErrorManager.getDefault().notify(e);
                    } finally{
                        handle.finish();
                    }
//                }});
//                RequestProcessor.getDefault().post(task);
        }
    }

    private void removeResource(Set<Resource> resources) throws IOException {
        try {
            model.startTransaction();
            for (Resource resource : resources) {
                if (resource.getParent() instanceof Resource) {
                    ((Resource) resource.getParent()).removeResource(resource);
                } else if (resource.getParent() instanceof Resources) {
                    ((Resources) resource.getParent()).removeResource(resource);
                }
            }
        } finally {
            model.endTransaction();
        }
        
        PropertyChangeListener[] listeners = getPropertyChangeListeners();
        for(PropertyChangeListener l:listeners) {
            l.propertyChange(new PropertyChangeEvent(model, REMOVE_RESOURCES, resources, null));
        }
    }

    private WadlModel getModel() {
        return this.model;
    }
    
    private static String getName() {
        return NbBundle.getMessage(RemoveResourceAction.class, "LBL_RemoveResource");
    }
    
}
