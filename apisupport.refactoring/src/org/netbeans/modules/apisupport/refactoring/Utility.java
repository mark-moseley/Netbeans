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

package org.netbeans.modules.apisupport.refactoring;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.refactoring.api.Problem;
import org.openide.ErrorManager;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;


public class Utility {
    
    private static final ErrorManager err = ErrorManager.getDefault().getInstance("org.netbeans.modules.j2ee.refactoring");   // NOI18N
    
    /** Creates a new instance of Utility */
    private Utility() {
    }

    
    public static Problem addProblemsToEnd(Problem where, Problem what) {
        Problem whereCopy = where;
        err.log("Where: " + where);
        err.log("What: " + what);
        if (what != null) {
            if (where == null) {
                whereCopy = what;
            } else {
                while (where.getNext() != null) {
                    where = where.getNext();
                }
                err.log("Last where: " + where);
                while (what != null) {
                    Problem elem = what;
                    err.log("Elem: " + elem);
                    where.setNext(elem);
                    where = where.getNext();
                    what = what.getNext();
                }
            }
        }
        err.log("wherecopy return: " + whereCopy);
        return whereCopy;
    } 
    
    /**
     * Creates full class name from package name and simple class name
     * @param pkg package name
     * @param simpleName simple class name
     * @return full class name
     */
    public static String getClassName(String pkg, final String simpleName) {
        return (pkg == null || pkg.length() == 0 ? "" : pkg + ".") + simpleName; // NOI18N
    }
    
    static void writeFileFromString(FileObject fileObject, String content) {
        if (content == null) {
            return;
        }
        FileLock lock = null;
        PrintWriter writer = null;
        try {
            lock = fileObject.lock();
            writer = new PrintWriter(new OutputStreamWriter(fileObject.getOutputStream(lock), "UTF-8")); // NOI18N
            writer.print(content);
            
        } catch (IOException exc) {
            //TODO
        } finally {
            if (writer != null) {
                writer.close();
            }
            if (lock != null) {
                lock.releaseLock();
            }
        }
        
    }
    
    static String readFileIntoString(FileObject fileObject) {
        BufferedReader reader = null;
        String content = null;
        try {
            StringWriter writer =new StringWriter();
            reader = new BufferedReader(new InputStreamReader(fileObject.getInputStream(), "UTF-8")); // NOI18N
            int chr = reader.read();
            while (chr != -1) {
                writer.write(chr);
                chr = reader.read();
            }
            content = writer.toString();
        } catch (IOException exc) {
            //TODO
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException x) {
                    // ignore
                }
            }
        }
        return content;
    }
    
    public static final FileObject findMetaInfServices(Project project) {
        Sources srcs = ProjectUtils.getSources(project);
        SourceGroup[] grps = srcs.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_RESOURCES);
        for (int i = 0; i < grps.length; i++) {
            FileObject fo = grps[i].getRootFolder().getFileObject("META-INF/services"); //NOI18N
            if (fo != null) {
                return fo;
            }
        }
        grps = srcs.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        for (int i = 0; i < grps.length; i++) {
            FileObject fo = grps[i].getRootFolder().getFileObject("META-INF/services"); //NOI18N
            if (fo != null) {
                return fo;
            }
        }
        return null;
    }    

}
