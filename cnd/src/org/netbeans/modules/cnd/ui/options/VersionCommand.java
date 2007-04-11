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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.ui.options;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 *
 * @author gordonp
 */
public class VersionCommand implements Runnable {
    
    private ProcessBuilder pb;
    private String name;
    private String path;
    private String version = null;
    
    /**
     * Creates a new instance of VersionCommand
     */
    public VersionCommand(String flavor, String name, String path) {
        String option = null;
        
        this.name = name;
        this.path = path;
        
        if (flavor.equals("GNU")) { // NOI18N
            option = "--version"; // NOI18N
        } else if (flavor.equals("Sun")) { // NOI18N
            option = "-V"; // NOI18N
        } else if (Utilities.getOperatingSystem() == Utilities.OS_SOLARIS) { // NOI18N
            if (path.endsWith("/sfw/bin/gmake")) { // NOI18N
                option = "--version"; // NOI18N
            } else if (path.equals("/usr/ccs/bin/make") || path.equals("/usr/xpg4/bin/make")) { // NOI18N
                path = "/sbin/uname"; // NOI18N
                option = "-sr"; // NOI18N
            } else if (name.equals("dmake")) { // NOI18N
                File inv;
                String base = path.substring(0, path.length() - 10);
                
                inv = new File(base + "/inventory"); // NOI18N
                if (inv.exists() && inv.isDirectory()) {
                    String[] files = inv.list();
                    for (int i = 0; i < files.length; i++) {
                        if (files[i].equals("v14n1")) { // NOI18N
                            version = NbBundle.getMessage(VersionCommand.class, "DMAKE10"); // NOI18N
                        } else if (files[i].equals("v15n1")) { // NOI18N
                            version = NbBundle.getMessage(VersionCommand.class, "DMAKE11"); // NOI18N
                        } else if (files[i].equals("v16n1")) { // NOI18N
                            version = NbBundle.getMessage(VersionCommand.class, "DMAKE12"); // NOI18N
                        }
                    }
                }
            }
        }
        if (option == null) {
            option = "--version"; // NOI18N - Guessing its GNU ...
        }
        
        if (version == null) {
            pb = new ProcessBuilder(path, option);
            pb.redirectErrorStream(true);
        }
    }
    
    public void run() {
        
        if (pb != null) {
            try {
                Process process = pb.start();
                BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
                version = br.readLine(); // just read 1st line...
            } catch (IOException ioe) {
            }
        }
        if (version != null) {
            String message = NbBundle.getMessage(VersionCommand.class, "LBL_VersionInfo", name, path, version);
            NotifyDescriptor nd = new NotifyDescriptor.Message(message);
            
            nd.setTitle(NbBundle.getMessage(VersionCommand.class, "LBL_VersionInfo_Title"));
            DialogDisplayer.getDefault().notify(nd);
        }
    }
}
