/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.entity;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.*;
import javax.swing.SwingUtilities;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJar;
import org.netbeans.modules.j2ee.dd.api.ejb.Entity;
import org.netbeans.modules.j2ee.ejbcore.api.methodcontroller.EntityMethodController;
import org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.shared.EjbViewController;
import org.openide.filesystems.FileObject;

import org.openide.nodes.*;
import org.openide.util.NbBundle;
import org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.shared.MethodsNode;


/**
 * @author Chris Webster
 * @author Martin Adamek
 */
public class EntityChildren extends Children.Keys implements PropertyChangeListener {
    private static final String REMOTE_KEY = "remote"; //NOI18N
    private static final String LOCAL_KEY = "local"; //NOI18N
    private static final String CMP_FIELDS = "fields"; //NOI18N
    
    private final Entity model;
    private final ClassPath srcPath;
    private final EntityMethodController controller;
    private final EjbJar jar;
    private final FileObject ddFile;
    
    public EntityChildren(Entity model, ClassPath srcPath, EjbJar jar, FileObject ddFile) {
        this.srcPath = srcPath;
        this.model = model;
        this.jar = jar;
        this.ddFile = ddFile;
        controller = new EntityMethodController(model, srcPath, jar);
    }
    
    protected void addNotify() {
        super.addNotify();
        updateKeys();
        model.addPropertyChangeListener(this);
        srcPath.addPropertyChangeListener(this);
    }
    
    private void updateKeys() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                List keys = new ArrayList();
                if (model.getRemote() != null) {
                    keys.add(REMOTE_KEY);
                }
                if (model.getLocal()!=null) {
                    keys.add(LOCAL_KEY);
                }
                if (Entity.PERSISTENCE_TYPE_CONTAINER.equals(model.getPersistenceType())) {
                    keys.add(CMP_FIELDS);
                }
                setKeys(keys);
            }
        });
    }
    
    protected void removeNotify() {
        model.removePropertyChangeListener(this);
        srcPath.removePropertyChangeListener(this);
        setKeys(Collections.EMPTY_SET);
        super.removeNotify();
    }
     
    protected Node[] createNodes(Object key) {
        if (LOCAL_KEY.equals(key)) {
            Children c = new MethodChildren(controller, model, controller.getLocalInterfaces(), true, ddFile);
            MethodsNode n = new MethodsNode(model, jar, srcPath, c, true);
            n.setIconBaseWithExtension("org/netbeans/modules/j2ee/ejbcore/resources/LocalMethodContainerIcon.gif");
            n.setDisplayName(NbBundle.getMessage(EjbViewController.class, "LBL_LocalMethods"));
            return new Node[] { n };
        }
        if (REMOTE_KEY.equals(key)) {
            Children c = new MethodChildren(controller, model, controller.getRemoteInterfaces(), false, ddFile);
            MethodsNode n = new MethodsNode(model, jar, srcPath, c, false);
            n.setIconBaseWithExtension("org/netbeans/modules/j2ee/ejbcore/resources/RemoteMethodContainerIcon.gif");
            n.setDisplayName(NbBundle.getMessage(EjbViewController.class, "LBL_RemoteMethods"));
            return new Node[] { n };
        }
        if (CMP_FIELDS.equals(key)) {
            CMPFieldsNode n = new CMPFieldsNode(controller,model,jar, ddFile);
            n.setIconBaseWithExtension("org/netbeans/modules/j2ee/ejbcore/resources/CMFieldContainerIcon.gif");
            n.setDisplayName(NbBundle.getMessage(EntityChildren.class, "LBL_CMPFields"));
            return new Node[] { n };
        }
        return null;
    }
    
    public void propertyChange(PropertyChangeEvent ev) {
        //TODO add code for detecting class name changes 
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                updateKeys();
            }
        });
    }
    
}
