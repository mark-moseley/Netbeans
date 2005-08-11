/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.modules.derby;

import javax.swing.Action;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CallableSystemAction;

import org.netbeans.modules.db.explorer.driver.JDBCDriverManager;
import org.netbeans.modules.db.explorer.driver.JDBCDriver;


import java.lang.reflect.Method;
import javax.sql.DataSource;

/**
 *
 * @author   Petr Jiricka
 */
public class CreateDatabaseAction extends CallableSystemAction {

    /** Generated sreial version UID. */
    //static final long serialVersionUID =3322896507302889271L;

    public CreateDatabaseAction() {
        putValue("noIconInMenu", Boolean.TRUE);
    }    
    
    public void performAction() {
        NotifyDescriptor.InputLine il = new NotifyDescriptor.InputLine(
            NbBundle.getBundle(CreateDatabaseAction.class).getString("CTL_SelectName"),
            NbBundle.getBundle(CreateDatabaseAction.class).getString("CTL_CreateDBAction"));
        if (DialogDisplayer.getDefault().notify(il) == NotifyDescriptor.OK_OPTION) {
            try {
                makeDataSource(il.getInputText());
            }
            catch (Exception e) {
                ErrorManager.getDefault().notify(ErrorManager.WARNING, e);
                
            }
        }        
        
    }
    
/*    DataSource makeDataSource(String dbname) throws Exception {
        JDBCDriver drivers[] = JDBCDriverManager.getDefault().getDrivers();
        //ClassLoader cl = new ClassLo
        return null;
    }*/
    
    DataSource makeDataSource(String dbname) throws Exception {
        Class edsClass = Class.forName("org.apache.derby.jdbc.EmbeddedDataSource");
        DataSource ds =  (DataSource)edsClass.newInstance();
        Method setName = edsClass.getMethod("setDatabaseName", new Class[] {String.class});
        setName.invoke(ds, new Object[] {dbname});
        return ds;
    }

    protected boolean asynchronous() {
        return false;
    }


    /** Gets localized name of action. Overrides superclass method. */
    public String getName() {
        return NbBundle.getBundle(CreateDatabaseAction.class).getString("CTL_CreateDBAction");
    }

    /** Gets the action's help context. Implemenst superclass abstract method. */
    public HelpCtx getHelpCtx() {
        return new HelpCtx(CreateDatabaseAction.class);
    }

}
