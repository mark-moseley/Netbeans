/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.utilities;

import org.openide.modules.ModuleInstall;
import org.openide.util.RequestProcessor;

import org.netbeans.modules.openfile.Server;
import org.netbeans.modules.openfile.Settings;
import org.netbeans.modules.openfile.SettingsBeanInfo;
import org.netbeans.modules.openfile.OpenFile;
import org.netbeans.modules.pdf.LinkProcessor;
import org.openide.ErrorManager;


/** Module install class for Utilities module.
 *
 * @author Jesse Glick, Petr Kuzel, Martin Ryzl
 */
public class Installer extends ModuleInstall {
    /** System property name to indicate file to open on NetBeans start-up. */
    private static final String NETBEANS_OPENFILE = "netbeans.openfile"; // NOI18N


    /** Serial version UID. */
    private final static long serialVersionUID = 1;

    /** Installation instance for search 'sub-module'.  */
    private final org.netbeans.modules.search.Installer searchInstaller;

    
    /** Constructs modules installer. */
    public Installer() {
        searchInstaller = new org.netbeans.modules.search.Installer();
    }

    
    /** Restores module. Restores search 'sub-module', schedules to start
     * openfile server and inits pdf link processor. Overrides superclass method. */
    public void restored() {
        searchInstaller.restored();

        // Don't ask.
        RequestProcessor.getDefault().post(new Runnable() {
                public void run () {
                    Settings.getDefault().isRunning();
                }
            }, 60000);

        // test netbeans.openfile property
        String fileName = System.getProperty (NETBEANS_OPENFILE);

        ErrorManager em = ErrorManager.getDefault()
                          .getInstance("org.netbeans.modules.openfile");//NOI18N
        String msg = "../utilities/Installer.restored: " + NETBEANS_OPENFILE + "='" + fileName + "'"; // NOI18N
        em.log (msg);

        if ( fileName != null ) {
            OpenFile.open (fileName);
        }
    }

    
    /** Uninstalls module. Shuts down openfile server and uninstalls
     * search 'sub-module'. Overrides superclass method. */
    public void uninstalled() {
        // OpenFile:
        Server.shutdown();

        searchInstaller.uninstalled();
    }

    /** Closes module. Shuts down openfile server. Overrides superclass method. */
    public boolean closing() {
        // OpenFile:
        Server.shutdown();

        return super.closing();
    }

}
