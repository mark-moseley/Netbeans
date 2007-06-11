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
package org.netbeans.modules.j2ee.ejbverification;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.modules.j2ee.ejbverification.EJBProblemFinder.ProblemFinderCompControl;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Severity;
import org.netbeans.spi.tasklist.FileTaskScanner;
import org.netbeans.spi.tasklist.Task;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;
/**
 *
 * @author Tomasz.Slota@Sun.COM
 */
public class EJBVerificationTaskProvider extends FileTaskScanner {
    private static final String TASKLIST_ERROR = "nb-tasklist-error"; //NOI18N
    private static final String TASKLIST_WARNING = "nb-tasklist-warning"; //NOI18N
    
    public EJBVerificationTaskProvider() {
        super(NbBundle.getMessage(EJBVerificationTaskProvider.class, "LBL_TaskList_DisplayName"),
                NbBundle.getMessage(EJBVerificationTaskProvider.class, "LBL_TaskList_Desc"),
                null);
    }
    
    public List<? extends Task> scan(FileObject file) {
        JavaSource javaSrc = JavaSource.forFileObject(file);
        ProblemFinderCompControl compControl = new ProblemFinderCompControl(file);
        
        if (javaSrc != null){
            try{
                javaSrc.runUserActionTask(compControl, true);
            } catch (IOException e){
                EJBProblemFinder.LOG.log(Level.WARNING, e.getMessage(), e);
            }
        }
        
        List<Task> tasks = new ArrayList<Task>();
        
        for (ErrorDescription error : compControl.getProblemsFound()){
            try{
                Task task = Task.create(file,
                        severityToTaskListString(error.getSeverity()),
                        error.getDescription(),
                        error.getRange().getBegin().getLine());
                
                tasks.add(task);
            } catch (IOException e){
                EJBProblemFinder.LOG.log(Level.SEVERE, e.getMessage(), e);
            }
        }
        
        return tasks;
    }
    
    public void attach(Callback callback) {
    }
    
    private static String severityToTaskListString(Severity severity){
        if (severity == Severity.ERROR){
            return TASKLIST_ERROR;
        }
        
        return TASKLIST_WARNING;
    }
}
