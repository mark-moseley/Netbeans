/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.java.j2seproject;

import java.io.File;
import java.io.IOException;
import java.util.regex.Pattern;
import org.apache.tools.ant.module.spi.AntEvent;
import org.apache.tools.ant.module.spi.AntLogger;
import org.apache.tools.ant.module.spi.AntSession;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * Logger which should suppress or prettify typical Ant output from a
 * j2seproject's build-impl.xml.
 * @author Jesse Glick
 */
public final class J2SEAntLogger extends AntLogger {
    
    /** Default constructor for lookup. */
    public J2SEAntLogger() {}
    
    public boolean interestedInSession(AntSession session) {
        // Even if the initiating project is not a J2SEProject, suppress these messages.
        // However disable our tricks when running at VERBOSE or higher.
        return session.getVerbosity() <= AntEvent.LOG_INFO;
    }
    
    private static boolean isJ2SEProject(File dir) {
        FileObject projdir = FileUtil.toFileObject(FileUtil.normalizeFile(dir));
        try {
            Project proj = ProjectManager.getDefault().findProject(projdir);
            if (proj != null) {
                // Check if it is a J2SEProject.
                return proj.getLookup().lookup(J2SEProject.AntProjectHelperProvider.class) != null;
            }
        } catch (IOException e) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
        }
        return false;
    }
    
    public boolean interestedInScript(File script, AntSession session) {
        if (script.getName().equals("build-impl.xml")) { // NOI18N
            File parent = script.getParentFile();
            if (parent != null && parent.getName().equals("nbproject")) { // NOI18N
                File parent2 = parent.getParentFile();
                if (parent2 != null) {
                    return isJ2SEProject(parent2);
                }
            }
        }
        // Was not a J2SEProject's nbproject/build-impl.xml; ignore it.
        return false;
    }
    
    public String[] interestedInTargets(AntSession session) {
        return AntLogger.ALL_TARGETS;
    }
    
    public String[] interestedInTasks(AntSession session) {
        // XXX will eventually need them all anyway; as is, could list just javac
        return AntLogger.ALL_TASKS;
    }
    
    public void taskFinished(AntEvent event) {
        if ("javac".equals(event.getTaskName())) { // NOI18N
            Throwable t = event.getException();
            AntSession session = event.getSession();
            if (t != null && !session.isExceptionConsumed(t)) {
                // Some error was thrown from build-impl.xml#compile. Ignore it; generally
                // it will have been a compilation error which we do not wish to show.
                session.consumeException(t);
            }
        }
    }
    
}
