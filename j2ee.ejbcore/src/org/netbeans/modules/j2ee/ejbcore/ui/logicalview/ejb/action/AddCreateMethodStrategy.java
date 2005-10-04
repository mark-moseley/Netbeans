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
import org.netbeans.jmi.javamodel.JavaClass;
import org.netbeans.jmi.javamodel.Method;
import org.netbeans.jmi.javamodel.Type;
import org.netbeans.modules.j2ee.common.JMIUtils;
import org.netbeans.modules.j2ee.common.ui.nodes.MethodCollectorFactory;
import org.netbeans.modules.j2ee.common.ui.nodes.MethodCustomizer;
import org.netbeans.modules.j2ee.ejbcore.api.methodcontroller.EjbMethodController;
import org.netbeans.modules.j2ee.ejbcore.api.methodcontroller.MethodType;
import org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.shared.MethodsNode;
import org.openide.util.NbBundle;


/**
 * @author Pavel Buzek
 */
public class AddCreateMethodStrategy extends AbstractAddMethodStrategy {
    
    public AddCreateMethodStrategy(String name) {
        super (name);
    }
    
    public AddCreateMethodStrategy() {
        super(NbBundle.getMessage(AddCreateMethodAction.class, "LBL_AddCreateMethodAction"));
    }
    
    protected MethodType getPrototypeMethod(JavaClass jc) {
        Method me = JMIUtils.createMethod(jc);
        me.setName("create"); //NOI18N
        JMIUtils.addException(me, "javax.ejb.CreateException"); //NOI18N
        return new MethodType.CreateMethodType(me);
    }
    
    protected MethodCustomizer createDialog(MethodType pType, EjbMethodController c) {
        Method[] methodElements = org.netbeans.modules.j2ee.ejbcore.ui.logicalview.Utils.getMethods(c, true, false);
	MethodsNode methodsNode = getMethodsNode();
	boolean local = methodsNode == null ? c.hasLocal() : (methodsNode.isLocal() && c.hasLocal());
	boolean remote = methodsNode == null ? c.hasRemote() : (!methodsNode.isLocal() && c.hasRemote());
        return MethodCollectorFactory.createCollector(pType.getMethodElement(), c.hasRemote(), c.hasLocal(), methodElements, remote, local);
    }

    protected Type remoteReturnType(EjbMethodController c, Type t, boolean isOneReturn) {
        return JMIUtils.resolveType(c.getRemote());
    }

    protected Type localReturnType(EjbMethodController c, Type t, boolean isOneReturn) {
        return JMIUtils.resolveType(c.getLocal());
    }
    
    public int prototypeMethod() {
        return MethodType.METHOD_TYPE_CREATE;
    }
    
}
