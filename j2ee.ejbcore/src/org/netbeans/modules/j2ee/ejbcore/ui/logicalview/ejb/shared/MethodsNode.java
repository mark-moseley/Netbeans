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

package org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.shared;

import java.io.IOException;
import javax.swing.Action;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.api.ejbjar.EjbJar;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJarMetadata;
import org.netbeans.modules.j2ee.dd.api.ejb.EntityAndSession;
import org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.action.AddActionGroup;
import org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.action.GoToSourceAction;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.openide.ErrorManager;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 * Represents Local/Remote Methods node under Session and Entity nodes 
 * in EJB logical view
 *
 * @author Martin Adamek
 */
public class MethodsNode extends AbstractNode implements OpenCookie {
    
    private final String ejbClass;
    private final MetadataModel<EjbJarMetadata> model;
    private final EjbViewController controller;
    private boolean local;

    public MethodsNode(String ejbClass, EjbJar ejbModule, Project project, Children children, boolean local) {
        this(new InstanceContent(), ejbClass, ejbModule, project, children, local);
    }
    
    private MethodsNode(InstanceContent content, String ejbClass, EjbJar ejbModule, Project project, Children children, boolean local) {
        super(children, new AbstractLookup(content));
        this.ejbClass = ejbClass;
        this.model = ejbModule.getMetadataModel();
        this.controller = new EjbViewController(ejbClass, ejbModule, project);
        this.local = local;
        content.add(this);
        if (controller.getBeanDo() != null) {
            content.add(controller.getBeanDo());
        }
    }
    
    public Action[] getActions(boolean context) {
        FileObject fileObject = null;
        try {
            fileObject = model.runReadAction(new MetadataModelAction<EjbJarMetadata, FileObject>() {
                public FileObject run(EjbJarMetadata metadata) throws Exception {
                    EntityAndSession entityAndSession = (EntityAndSession) metadata.findByEjbClass(ejbClass);
                    String className = local ? entityAndSession.getLocal() : entityAndSession.getRemote();
                    return metadata.findResource(className);
                }
            });
        } catch (IOException ioe) {
            ErrorManager.getDefault().notify(ioe);
        }
        return new Action[] {
            new GoToSourceAction(fileObject, NbBundle.getMessage(MethodsNode.class, "LBL_GoToSourceGroup")),
            SystemAction.get(AddActionGroup.class),
        };
    }

    public Action getPreferredAction() {
        FileObject fileObject = null;
        try {
            fileObject = model.runReadAction(new MetadataModelAction<EjbJarMetadata, FileObject>() {
                public FileObject run(EjbJarMetadata metadata) throws Exception {
                    EntityAndSession entityAndSession = (EntityAndSession) metadata.findByEjbClass(ejbClass);
                    String className = local ? entityAndSession.getLocal() : entityAndSession.getRemote();
                    return metadata.findResource(className);
                }
            });
        } catch (IOException ioe) {
            ErrorManager.getDefault().notify(ioe);
        }
        return new GoToSourceAction(fileObject, NbBundle.getMessage(MethodsNode.class, "LBL_GoToSourceGroup"));
    }

    public void open() {
        DataObject dataObject = controller.getBeanDo();
        if (dataObject != null) {
            OpenCookie cookie = dataObject.getCookie(OpenCookie.class);
            if(cookie != null){
                cookie.open();
            }
        }
    }
    
    public boolean isLocal() {
	return local;
    }
}
