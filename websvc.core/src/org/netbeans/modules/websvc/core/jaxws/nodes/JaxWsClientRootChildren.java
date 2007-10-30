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
package org.netbeans.modules.websvc.core.jaxws.nodes;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.websvc.api.jaxws.project.config.Client;
import org.netbeans.modules.websvc.api.jaxws.project.config.JaxWsModel;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import java.beans.PropertyChangeListener;
import org.openide.util.RequestProcessor;

public class JaxWsClientRootChildren extends Children.Keys<Client> {
    JaxWsModel jaxWsModel;
    Client[] clients;
    JaxWsListener listener;
    FileObject srcRoot;
    
    private RequestProcessor.Task updateNodeTask = RequestProcessor.getDefault().create(new Runnable() {
        public void run() {
            updateKeys();
        }
    });
    
    public JaxWsClientRootChildren(JaxWsModel jaxWsModel, FileObject srcRoot) {
        this.jaxWsModel = jaxWsModel;
        this.srcRoot=srcRoot;
    }
    
    @Override
    protected void addNotify() {
        listener = new JaxWsListener();
        jaxWsModel.addPropertyChangeListener(listener);
        updateKeys();
    }
    
    @Override
    protected void removeNotify() {
        setKeys(Collections.<Client>emptySet());
        jaxWsModel.removePropertyChangeListener(listener);
    }
       
    private void updateKeys() {
        List<Client> keys = new ArrayList<Client>();
        clients = jaxWsModel.getClients();
        if (clients != null) {
            for (int i = 0; i < clients.length; i++) {
                keys.add(clients[i]);
            }
        }
        setKeys(keys);
    }

    protected Node[] createNodes(Client key) {
        return new Node[] {new JaxWsClientNode(jaxWsModel, key, srcRoot)};
    }
    
    class JaxWsListener implements PropertyChangeListener {
        public void propertyChange(PropertyChangeEvent evt) {
            updateNodeTask.schedule(2000);
        }        
    }

}
