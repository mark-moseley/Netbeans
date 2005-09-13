/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 2004-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.jmx.actions;
import java.util.ArrayList;
import org.netbeans.jmi.javamodel.JavaClass;
import org.netbeans.jmi.javamodel.Resource;
import org.netbeans.modules.javacore.api.JavaModel;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.actions.CookieAction;
import org.openide.cookies.SourceCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.netbeans.modules.jmx.*;
import org.netbeans.modules.jmx.actions.dialog.AddAttributesInfoPanel;
import org.netbeans.modules.jmx.actions.dialog.AddAttributesPanel;
import org.netbeans.modules.jmx.mbeanwizard.generator.StdMBeanClassGen;
import org.openide.cookies.EditorCookie;
import org.openide.util.NbBundle;

/**
 * Action used to add attributes to an existing MBean.
 * @author tl156378
 */
public class AddAttrAction extends CookieAction {
    
    private DataObject dob;
    private Resource rc;
    
    /**
     * Creates a new instance of AddAttrAction 
     */
    public AddAttrAction() {
        putValue("noIconInMenu", Boolean.TRUE); // NOI18N
    }
    
    protected Class[] cookieClasses() {
        return new Class[] { SourceCookie.class };
    }
     
    protected int mode() {
        // allow one exactly class node
        return MODE_EXACTLY_ONE;    
    }
    
    public boolean asynchronous() {
        return true; // yes, this action should run asynchronously
        // would be better to rewrite it to synchronous (running in AWT thread),
        // just replanning test generation to RequestProcessor
    }  
    
    protected boolean enable (Node[] nodes) {
        if (!super.enable(nodes)) return false;
        if (nodes.length == 0) return false;
        
        dob = (DataObject)nodes[0].getCookie(DataObject.class);
        FileObject fo = null;
        if (dob != null) fo = dob.getPrimaryFile();
        
        JavaClass foClass = WizardHelpers.getJavaClassInProject(fo);
        if (foClass == null) return false;
        
        //We need to do all MDR access in a transaction
        //Introspection is resolving some interfaces.
        //It has to be done in a transaction contextualized by fo.
        JavaModel.getJavaRepository().beginTrans(false);
        try {
            JavaModel.setClassPath(fo);
            rc = JavaModel.getResource(fo);
            return Introspector.isStandard(foClass);
        } finally {
            JavaModel.getJavaRepository().endTrans();
        }
    }
    
    protected void performAction (Node[] nodes) {
        // show configuration dialog
        // when dialog is canceled, escape the action
        DataObject dataObj = (DataObject)nodes[0].getCookie(DataObject.class);
        FileObject fo = null;
        if (dataObj != null) fo = dataObj.getPrimaryFile();
        //We need to do all MDR access in a transaction
        JavaModel.getJavaRepository().beginTrans(false);
        try {
            JavaModel.setClassPath(fo);
            AddAttributesPanel cfg = new AddAttributesPanel(nodes[0]);
            if (!cfg.configure()) {
                return;
            }
            //detect if implementation of attributes exists
            JavaClass mbeanClass = cfg.getMBeanClass();
            MBeanAttribute[] attributes = cfg.getAttributes();
            boolean hasExistAttr = false;
            for (int i = 0; i < attributes.length; i++) {
                boolean methodExist =
                        Introspector.discoverAttrMethods(mbeanClass,rc,attributes[i]);
                if (methodExist)
                    Introspector.updateAttrExceptions(mbeanClass,rc,attributes[i]);
                if (methodExist)
                    hasExistAttr = true;
            }
            
            if (hasExistAttr) {
                AddAttributesInfoPanel infoPanel =
                        new AddAttributesInfoPanel(mbeanClass.getSimpleName(),
                        attributes);
                if (!infoPanel.configure()) {
                    return;
                }
            }
            
            StdMBeanClassGen generator = new StdMBeanClassGen();
            try {
                generator.updateAttributes(mbeanClass,rc,attributes);
                EditorCookie ec = (EditorCookie)dob.getCookie(EditorCookie.class);
                ec.open();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } finally {
            JavaModel.getJavaRepository().endTrans();
        }
    }
    
    public HelpCtx getHelpCtx () {
        return new HelpCtx(""); // NOI18N
    }
    
    public String getName () {
        return NbBundle.getMessage(AddAttrAction.class,"LBL_Action_AddMBeanAttribute"); // NOI18N
    }
}
