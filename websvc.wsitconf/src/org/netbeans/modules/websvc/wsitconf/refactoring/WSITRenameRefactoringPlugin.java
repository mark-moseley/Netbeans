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

package org.netbeans.modules.websvc.wsitconf.refactoring;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.netbeans.modules.refactoring.api.RenameRefactoring;
import org.netbeans.modules.refactoring.spi.RefactoringPlugin;
import org.netbeans.modules.websvc.wsitconf.refactoring.WSITXmlRenameRefactoring;
import org.netbeans.modules.websvc.wsitconf.util.Util;
import org.openide.util.Lookup;

/**
 *
 * @author Martin Grebac
 */
public class WSITRenameRefactoringPlugin implements RefactoringPlugin {
    
    /** This one is important creature - makes sure that cycles between plugins won't appear */
    private static ThreadLocal semafor = new ThreadLocal();
    
    private WSITXmlRenameRefactoring wsitXmlRenameRefactor = new WSITXmlRenameRefactoring();
    
    private final RenameRefactoring renameRefactoring;
    
    private static final Logger logger = Logger.getLogger("org.netbeans.modules.websvc.wsitconf.refactoring.rename");
    
    /**
     * Creates a new instance of WSITRenameRefactoringPlugin
     */
    public WSITRenameRefactoringPlugin(RenameRefactoring refactoring) {
        this.renameRefactoring = refactoring;
    }
    
    /** Checks pre-conditions of the renameRefactoring and returns problems.
     * @return Problems found or null (if no problems were identified)
     */
    public Problem preCheck() {
        Problem problem = null;
        if (semafor.get() == null) {
            semafor.set(new Object());
            
            Lookup refO = renameRefactoring.getRefactoringSource();
            logger.log(Level.FINE, "refO: " + refO);
            
            Problem wsitXmlProblem = wsitXmlRenameRefactor.preCheck(refO);
            problem = Util.addProblemsToEnd(problem, wsitXmlProblem);

            semafor.set(null);
        }
        return problem;
    }
    
    
    public Problem fastCheckParameters() {
        Problem problem = null;
        if (semafor.get() == null) {
            semafor.set(new Object());
            
            Lookup refO = renameRefactoring.getRefactoringSource();
            logger.log(Level.FINE, "refO: " + refO);
            
            String newName = renameRefactoring.getNewName();
            logger.log(Level.FINE, "newname: " + newName);
                        
            semafor.set(null);
        }
        return problem;
    }
    
    /** Checks parameters of the renameRefactoring.
     * @return Problems found or null (if no problems were identified)
     */
    public Problem checkParameters() {
        Problem problem = null;
        if (semafor.get() == null) {
            semafor.set(new Object());
            
            Lookup refO = renameRefactoring.getRefactoringSource();
            logger.log(Level.FINE, "refO: " + refO);
            
            String newName = renameRefactoring.getNewName();
            logger.log(Level.FINE, "newname: " + newName);
            
            semafor.set(null);
        }
        return problem;
    }
    
    /** Collects renameRefactoring elements for a given renameRefactoring.
     * @param refactoringElements Collection of renameRefactoring elements - the implementation of this method
     * should add renameRefactoring elements to this collections. It should make no assumptions about the collection
     * content.
     * @return Problems found or null (if no problems were identified)
     */
    public Problem prepare(RefactoringElementsBag refactoringElements) {
        Problem problem = null;
        
        if (semafor.get() == null) {
            semafor.set(new Object());
            
            Lookup refO = renameRefactoring.getRefactoringSource();
            logger.log(Level.FINE, "refO: " + refO);
            
            String newName = renameRefactoring.getNewName();
            logger.log(Level.FINE, "newname: " + newName);
            
            Problem wsitXmlProblem = wsitXmlRenameRefactor.prepare(renameRefactoring, refO, newName, refactoringElements);
            problem = Util.addProblemsToEnd(problem, wsitXmlProblem);
            
            semafor.set(null);
        }
        
        logger.log(Level.FINE, "Gonna return problem: " + problem);
        return problem;
    }
    
    public void cancelRequest() { }
    
}
