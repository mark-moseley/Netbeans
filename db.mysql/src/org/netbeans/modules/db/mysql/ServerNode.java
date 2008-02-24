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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.db.mysql;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.swing.Action;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.db.explorer.DatabaseException;
import org.openide.actions.DeleteAction;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;
import org.openide.util.actions.SystemAction;

/**
 * Represents a MySQL Server instance.  
 * 
 * @author David Van Couvering
 */
class ServerNode extends AbstractNode implements ChangeListener {  
    private final ServerInstance server;
    
    // I'd like a less generic icon, but this is what we have for now...
    private static final String ICON = "org/netbeans/modules/db/mysql/resources/catalog.gif";
            
    public static ServerNode create(ServerInstance server) {
        ChildFactory factory = new ChildFactory(server);
        return new ServerNode(factory, server);
    }
    
    private ServerNode(ChildFactory factory, ServerInstance server) {
        super(Children.create(factory, true));
        this.server = server;
        
        setName(""); // NOI18N
        setDisplayName(server.getDisplayName());
        setShortDescription(server.getShortDescription());
        setIconBaseWithExtension(ICON);
        
        registerListeners();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Node.Cookie getCookie(Class cls) {
        if ( cls == ServerInstance.class ) {
            return server;
        } else {
            return super.getCookie(cls);
        }
        
    }
    
    private void registerListeners() {
        ServerInstance.getDefault().addChangeListener(
                WeakListeners.create(ChangeListener.class, this,
                    ServerInstance.getDefault()));
        
        stateChanged(new ChangeEvent(ServerInstance.getDefault()));
    }
    

    public void stateChanged(ChangeEvent evt) {
        // The display name changes depending on the 
        // state of the server instance
        String oldName = getDisplayName();
        setDisplayName(server.getDisplayName());
        this.fireNameChange(oldName, getDisplayName());
    }
                
    @Override
    public Action[] getActions(boolean context) {
        if ( context ) {
            return super.getActions(context);
        } else {
            return new SystemAction[] {
                SystemAction.get(CreateDatabaseAction.class),
                SystemAction.get(StartAction.class),
                SystemAction.get(StopAction.class),
                SystemAction.get(ConnectServerAction.class),
                SystemAction.get(RefreshAction.class),
                SystemAction.get(DeleteAction.class),
                SystemAction.get(AdministerAction.class),
                SystemAction.get(PropertiesAction.class)
            };
        }
    }
    
    @Override
    public boolean canDestroy() {
        return true;
    }
    
    @Override
    public void destroy() {
       ServerNodeProvider.getDefault().setRegistered(false);
    }
            
    private static class ChildFactory 
            extends org.openide.nodes.ChildFactory<DatabaseModel> 
            implements ChangeListener {
        
        private static final Comparator<DatabaseModel> COMPARATOR = 
                new InstanceComparator();

        private final ServerInstance server;


        public ChildFactory(ServerInstance server) {            
            super();
            
            this.server = server;
            
            server.addChangeListener(
                WeakListeners.create(ChangeListener.class, this, server));
            stateChanged(new ChangeEvent(server));
        }

        @Override
        protected Node createNodeForKey(DatabaseModel db) {
            return new DatabaseNode(db);
        }

        @Override
        protected boolean createKeys(List<DatabaseModel> toPopulate) {
            List<DatabaseModel> fresh = new ArrayList<DatabaseModel>();

            try {
                fresh.addAll(server.getDatabases());
            } catch (DatabaseException ex) {
                Utils.displayError(NbBundle.getMessage(ServerNode.class, 
                        "MSG_UnableToGetDatabaseList"), ex);
                return true;
            }

            Collections.sort(fresh, COMPARATOR);

            toPopulate.addAll(fresh);
            
            return true;
        }

        public void stateChanged(ChangeEvent e) {
            refresh(false);
        }
    }

    private static class InstanceComparator 
            implements Comparator<DatabaseModel>, Serializable {

        public int compare(DatabaseModel o1, DatabaseModel o2) {
            return o1.getDisplayName().compareTo(o2.getDisplayName());
        }

    }

}
