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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.groovy.grails;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import org.netbeans.api.project.Project;
import org.openide.execution.NbProcessDescriptor;
import org.openide.util.Exceptions;
import org.openide.windows.OutputWriter;
import org.netbeans.modules.groovy.grails.settings.Settings;

/**
 *
 * @author schmidtm
 */
public class GrailsServerRunnable implements Runnable {
    OutputWriter writer = null;
    String grailsExecutable;
    Settings settings;
    Project project;
    String cmd;
    
    public GrailsServerRunnable(OutputWriter writer, Project project, String cmd){
        this.writer = writer;
        this.settings = Settings.getInstance();
        this.grailsExecutable = settings.getGrailsBase() + "/bin/grails";
        this.project = project; 
        this.cmd = cmd;
        }
    
    
    public void run() {
        if (new File(grailsExecutable).exists()) {
            try {
                NbProcessDescriptor grailsProcessDesc = new NbProcessDescriptor(grailsExecutable, cmd);
                
                String cwdName = "/" + project.getProjectDirectory().getPath();
                writer.print("Working Dir: " + cwdName + "\n");
                
                File cwd = new File(cwdName);
                Process process = grailsProcessDesc.exec(null, null, cwd);
                // Process process = grailsProcessDesc.exec();
                readOutput(process.getInputStream());

                // process.waitFor();
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
        } else {
        
            System.out.println("Executable doesn't exist...");
            }
    }

    /**
    * Parse the output.
    */
    void readOutput(InputStream outStream) {
        try {
            BufferedReader error = new BufferedReader(new InputStreamReader(outStream));
            String errString = null;
            
            while ((errString = error.readLine()) != null) {
                writer.print(errString + "\n");
            }
                } catch (Exception e) {
                    System.out.println("Could not read Process output " +e);
                    }
    }
    
    
    
    
    
    
    
}
