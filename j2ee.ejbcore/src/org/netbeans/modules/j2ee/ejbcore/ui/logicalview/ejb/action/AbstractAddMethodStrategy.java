/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.action;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.jmi.javamodel.JavaClass;
import org.netbeans.jmi.javamodel.Method;
import org.netbeans.jmi.javamodel.Type;
import org.netbeans.modules.j2ee.api.ejbjar.EjbJar;
import org.netbeans.modules.j2ee.common.JMIUtils;
import org.netbeans.modules.j2ee.common.ui.nodes.MethodCustomizer;
import org.netbeans.modules.j2ee.ejbcore.api.methodcontroller.EjbMethodController;
import org.netbeans.modules.j2ee.ejbcore.api.methodcontroller.MethodType;
import org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.shared.MethodsNode;
import org.netbeans.modules.javacore.api.JavaModel;
import org.netbeans.modules.javacore.internalapi.JavaMetamodel;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.Utilities;

/**
 * Strategy for visual support for adding various methods into an EJB.
 * @author Pavel Buzek
 */
public abstract class AbstractAddMethodStrategy {
    
    private String name;
    
    public AbstractAddMethodStrategy (String name) {
        this.name = name;
    }
    
    protected abstract MethodType getPrototypeMethod(JavaClass jc);
    
    /** Describes method type handled by this action. */
    public abstract int prototypeMethod();
    
    public String getTitle() {
        return name;
    }
    
    protected abstract MethodCustomizer createDialog(MethodType prototypeMethod, EjbMethodController c);
    protected Type localReturnType(EjbMethodController c, Type t, boolean oneReturn) {return t;}
    protected Type remoteReturnType(EjbMethodController c, Type t, boolean oneReturn) {return t;}
    
    public void addMethod (JavaClass jc) {
        JavaMetamodel.getDefaultRepository().beginTrans(false);
        MethodType pType = null;
        Method prototypeMethod = null;
        EjbMethodController c = null;
        try {
            if (jc != null) {
                c = EjbMethodController.createFromClass(jc);
                pType = getPrototypeMethod(jc);
                prototypeMethod = pType.getMethodElement();
            }
        }
        finally {
            JavaMetamodel.getDefaultRepository().endTrans();
        }
        MethodCustomizer mc = createDialog(pType, c);
        mc.setEjbQL(c.createDefaultQL(pType));
        final NotifyDescriptor nd = new NotifyDescriptor(mc, getTitle(),
            NotifyDescriptor.OK_CANCEL_OPTION, 
            NotifyDescriptor.PLAIN_MESSAGE,
            null, null
        );
        mc.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getPropertyName().equals(MethodCustomizer.OK_ENABLED)) {
                    Object newvalue = evt.getNewValue();
                    if ((newvalue != null) && (newvalue instanceof Boolean)) {
                        nd.setValid(((Boolean)newvalue).booleanValue());
                    }
                }
            }
        });
        Object rv = DialogDisplayer.getDefault().notify(nd);
        mc.isOK(); // apply possible changes in dialog fields
        if (rv == NotifyDescriptor.OK_OPTION) {
            try {
                okButtonPressed(pType, mc, prototypeMethod, c, jc);
            } catch (IOException ioe) {
                NotifyDescriptor ndd = 
                    new NotifyDescriptor.Message(ioe.getMessage(), NotifyDescriptor.ERROR_MESSAGE);
                DialogDisplayer.getDefault().notify(ndd);
            }
        }
    }

    protected void okButtonPressed(MethodType pType, MethodCustomizer mc,
                                   Method prototypeMethod, 
                                   EjbMethodController c, JavaClass jc)
    throws IOException {
	ProgressHandle handle = ProgressHandleFactory.createHandle("Adding method");
	handle.start(100);
        boolean isComponent = pType instanceof MethodType.BusinessMethodType;
        boolean isOneReturn = mc.finderReturnIsSingle();
	handle.progress(10);
        if (mc.publishToLocal()) {
            Type localReturn =
                    localReturnType(c, prototypeMethod.getType(), isOneReturn);
            prototypeMethod.setType(localReturn);
            c.createAndAdd(JMIUtils.duplicate(prototypeMethod),true, isComponent);
        }
	handle.progress(60);
        if (mc.publishToRemote()) {
            Type remoteReturn =
                    remoteReturnType(c, prototypeMethod.getType(), isOneReturn);
            prototypeMethod.setType(remoteReturn);
            c.createAndAdd(JMIUtils.duplicate(prototypeMethod),false, isComponent);
        }
	handle.progress(80);
        String ejbql = mc.getEjbQL();
        if (ejbql != null && ejbql.length() > 0) {
            c.addEjbQl(JMIUtils.duplicate(prototypeMethod), ejbql, getDDFile(jc));
        }
	handle.progress(99);
	handle.finish();
    }
    
    protected FileObject getDDFile(JavaClass jc) {
        FileObject fo = JavaModel.getFileObject(jc.getResource());
        return EjbJar.getEjbJar(fo).getDeploymentDescriptor();
    }
    
    protected static MethodsNode getMethodsNode() {
        Node[] nodes = (Node[])Utilities.actionsGlobalContext().lookup(new Lookup.Template(Node.class)).allInstances().toArray(new Node[0]);
	if (nodes.length != 1) {
	    return null;
	}
	Object o = nodes[0].getLookup().lookup(MethodsNode.class);
	if (o instanceof MethodsNode) {
	    return (MethodsNode) o;
	}
	return null;
    }
    
}
