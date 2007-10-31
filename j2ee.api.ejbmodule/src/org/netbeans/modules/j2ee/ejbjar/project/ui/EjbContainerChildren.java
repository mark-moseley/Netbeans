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

package org.netbeans.modules.j2ee.ejbjar.project.ui;

import java.io.IOException;
import org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException;
import org.netbeans.modules.j2ee.dd.api.ejb.Ejb;
import org.netbeans.modules.j2ee.dd.api.ejb.EnterpriseBeans;
import org.netbeans.modules.j2ee.dd.api.ejb.Entity;
import org.netbeans.modules.j2ee.dd.api.ejb.MessageDriven;
import org.netbeans.modules.j2ee.dd.api.ejb.Session;
import org.netbeans.modules.j2ee.spi.ejbjar.EjbNodesFactory;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.swing.SwingUtilities;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.api.ejbjar.EjbJar;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJarMetadata;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.openide.util.Exceptions;

/**
 * Ejbs contained within a module
 * 
 * @author Chris Webster
 * @author Martin Adamek
 */
public class EjbContainerChildren extends Children.Keys<EjbContainerChildren.Key> implements PropertyChangeListener {

    private final EjbJar ejbModule;
    private final EjbNodesFactory nodeFactory;
    private final Project project;

    public EjbContainerChildren(org.netbeans.modules.j2ee.api.ejbjar.EjbJar ejbModule, EjbNodesFactory nodeFactory, Project project) {
        this.ejbModule = ejbModule;
        this.nodeFactory = nodeFactory;
        this.project = project;
        try {
            ejbModule.getMetadataModel().runReadAction(new MetadataModelAction<EjbJarMetadata, Void>() {
                public Void run(EjbJarMetadata metadata) {
                    org.netbeans.modules.j2ee.dd.api.ejb.EjbJar ejbJar = metadata.getRoot();
                    if (ejbJar != null) {
                        EnterpriseBeans enterpriseBeans = ejbJar.getEnterpriseBeans();
                        if (enterpriseBeans != null) {
                            enterpriseBeans.addPropertyChangeListener(EjbContainerChildren.this);
                        }
                    }
                    return null;
                }
            });
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
        }
    }

    protected void addNotify() {
        super.addNotify();
        try {
            updateKeys();
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
        }
    }

    private void updateKeys() throws IOException {
        
        List<Key> result = ejbModule.getMetadataModel().runReadAction(new MetadataModelAction<EjbJarMetadata, List<Key>>() {
            public List<Key> run(EjbJarMetadata metadata) throws Exception {
                EnterpriseBeans beans = metadata.getRoot().getEnterpriseBeans();
                if (beans != null) {
                    Key[] sessionBeans = Key.createArray(beans.getSession());
                    Key[] entityBeans = Key.createArray(beans.getEntity());
                    Key[] messageBeans = Key.createArray(beans.getMessageDriven());
                    Comparator<Key> ejbComparator = new Comparator<Key>() {
                        public int compare(Key key1, Key key2) {
                            return getEjbDisplayName(key1).compareTo(getEjbDisplayName(key2));
                        }

                        private String getEjbDisplayName(Key ejb) {
                            String name = ejb.defaultDisplayName;
                            if (name == null) {
                                name = ejb.ejbName;
                            }
                            if (name == null) {
                                name = "";
                            }
                            return name;
                        }
                    };
                    Arrays.sort(sessionBeans, ejbComparator);
                    Arrays.sort(entityBeans, ejbComparator);
                    Arrays.sort(messageBeans, ejbComparator);
                    List<Key> keys = new ArrayList<Key>(sessionBeans.length + entityBeans.length  + messageBeans.length);
                    keys.addAll(Arrays.asList(sessionBeans));
                    keys.addAll(Arrays.asList(messageBeans));
                    keys.addAll(Arrays.asList(entityBeans));
                    return keys;
                }
                return Collections.<Key>emptyList();
            }
        });
        
        setKeys(result);
    }

    protected void removeNotify() {
        //TODO: RETOUCHE stop listening on model
//        model.removePropertyChangeListener(this);
        setKeys(Collections.<Key>emptyList());
        super.removeNotify();
    }

    protected Node[] createNodes(Key key) {
        Node node = null;
        if (key.ejbType == Key.EjbType.SESSION) {
            // do not create node for web service
            if (!key.isWebService && nodeFactory != null) {
                node = nodeFactory.createSessionNode(key.ejbClass, ejbModule, project);
            }
        }
        if (key.ejbType == Key.EjbType.ENTITY && nodeFactory != null) {
            node = nodeFactory.createEntityNode(key.ejbClass, ejbModule, project);
        }
        if (key.ejbType == Key.EjbType.MESSAGE_DRIVEN && nodeFactory != null) {
            node = nodeFactory.createMessageNode(key.ejbClass, ejbModule, project);
        }
        return node == null ? new Node[0] : new Node[] { node };
    }

    public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
        
        SwingUtilities.invokeLater(new Runnable() {
            
            public void run() {
                try {
                    updateKeys();
                } catch (IOException ioe) {
                    Exceptions.printStackTrace(ioe);
                }
            }
        });
    }

    final static class Key {
        
        private enum EjbType { SESSION, ENTITY, MESSAGE_DRIVEN }
        
        private final EjbType ejbType;
        private final String ejbClass;
        private final String defaultDisplayName;
        private final String ejbName;
        private final boolean isWebService;
        
        private Key(EjbType ejbType, String ejbClass, String defaultDisplayName, String ejbName, boolean  isWebService) {
            this.ejbType = ejbType;
            this.ejbClass = ejbClass;
            this.defaultDisplayName = defaultDisplayName;
            this.ejbName = ejbName;
            this.isWebService = isWebService;
        }
        
        public static Key[] createArray(Ejb[] ejbs) {
            Key[] keys = new Key[ejbs.length];
            for (int i = 0; i < ejbs.length; i++) {
                Ejb ejb = ejbs[i];
                EjbType ejbType = null;
                boolean isWebService = false;
                if (ejb instanceof Session) {
                    ejbType = EjbType.SESSION;
                    try {
                        isWebService = ((Session) ejb).getServiceEndpoint() != null;
                    } catch (VersionNotSupportedException ex) {
                        // not supported for J2EE 1.3
                    }
                } else if (ejb instanceof Entity) {
                    ejbType = EjbType.ENTITY;
                } else if (ejb instanceof MessageDriven) {
                    ejbType = EjbType.MESSAGE_DRIVEN;
                }
                keys[i] = new Key(
                        ejbType,
                        ejb.getEjbClass(),
                        ejb.getDefaultDisplayName(),
                        ejb.getEjbName(),
                        isWebService
                        );
            }
            return keys;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Key other = (Key) obj;
            if (this.ejbClass != other.ejbClass && (this.ejbClass == null || !this.ejbClass.equals(other.ejbClass))) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 79 * hash + (this.ejbClass != null ? this.ejbClass.hashCode() : 0);
            return hash;
        }

        
    }
    
}
