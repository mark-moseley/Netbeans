/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.search;

import java.awt.EventQueue;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.search.MatchingObject.InvalidityStatus;
import org.openide.filesystems.FileAlreadyLockedException;
import org.openide.filesystems.FileLock;
import org.openide.util.NbBundle;
import org.openide.util.UserQuestionException;

/**
 * Task that checks validity of found files and then
 * (if everything is valid) replaces the matching substrings
 * with the replacement string/pattern.
 * 
 * @author  Tim Boudreau
 * @author  Marian Petras
 */
final class ReplaceTask implements Runnable {
    
    /**
     * maximum number of errors detected before replacing
     * and displayed to the user
     */
    private static final int MAX_ERRORS_CHECKED = 20;
    
    private final MatchingObject[] matchingObjects;
    private final ProgressHandle progressHandle;
    private final List<String> problems;
    
    /** */
    private ResultStatus resultStatus = null;
    
    enum ResultStatus {
        SUCCESS,
        PRE_CHECK_FAILED,
        PROBLEMS_ENCOUNTERED
    }
    
    /**
     */
    ReplaceTask(MatchingObject[] matchingObjects) {
        this.matchingObjects = matchingObjects;
        
        problems = new ArrayList<String>(4);
        progressHandle = ProgressHandleFactory.createHandle(
                NbBundle.getMessage(getClass(), "LBL_Replacing"));      //NOI18N
    }
    
    /**
     */
    public void run() {
        assert !EventQueue.isDispatchThread();
        
        progressHandle.start(matchingObjects.length * 2);
        try {
            replace();
            assert resultStatus != null;
        } finally {
            progressHandle.finish();
        }
    }
    
    /**
     */
    private void replace() {
        assert !EventQueue.isDispatchThread();
        
        checkForErrors();
        if (resultStatus == null) {       //the check passed
            doReplace();
        }
    }
    
    /**
     */
    private void checkForErrors() {
        assert !EventQueue.isDispatchThread();
        
        int errorsCount = 0;
        
        for (int i = 0; i < matchingObjects.length; i++) {
            InvalidityStatus status = matchingObjects[i].checkValidity();
            if (status != null) {
                problems.add(status.getDescription(
                                       matchingObjects[i].getFile().getPath()));
                if (++errorsCount > MAX_ERRORS_CHECKED) {
                    break;
                }
            }
        }
        if (!problems.isEmpty()) {
            resultStatus = ResultStatus.PRE_CHECK_FAILED;
        }
    }

    /**
     * 
     * @return  list of strings describing problems that happened during
     *          the replace, or {@code null} if no problem happened
     */
    private void doReplace() {
        assert !EventQueue.isDispatchThread();
        
        for (int i = 0; i < matchingObjects.length; i++) {
            final MatchingObject obj = matchingObjects[i];
            
            progressHandle.progress(obj.getName(),
                                    i + matchingObjects.length);
            if (!obj.isSelected() || !obj.isValid()) {
                continue;
            }
            
            String invDescription = obj.getInvalidityDescription();
            if (invDescription != null) {
                problems.add(invDescription);
                continue;
            }
            
            String errMessage = null;
            FileLock fileLock = null;
            try {
                fileLock = obj.lock();
                MatchingObject.InvalidityStatus status = obj.replace();
                if (status == null) {
                    obj.write(fileLock);
                } else {
                    errMessage = status.getDescription(obj.getFile().getPath());
                }
            } catch (FileAlreadyLockedException ex) {
                errMessage = createMsgFileLocked(obj);
            } catch (UserQuestionException ex) {
                errMessage = createMsgFileLocked(obj);
            } catch (IOException ex) {
                ex.printStackTrace();      //PENDING - ex.printStackTrace()?
                errMessage = ex.getLocalizedMessage();
                if (errMessage == null) {
                    errMessage = ex.getMessage();
                }
            } finally {
                if (fileLock != null) {
                    fileLock.releaseLock();
                }
            }
            if (errMessage != null) {
                problems.add(errMessage);
            }
        }
        resultStatus = problems.isEmpty() ? ResultStatus.SUCCESS
                                          : ResultStatus.PROBLEMS_ENCOUNTERED;
    }

    private static String createMsgFileLocked(MatchingObject matchingObj) {
        return NbBundle.getMessage(
                ReplaceTask.class,
                "MSG_cannot_access_file_already_locked",                //NOI18N
                matchingObj.getName());
    }
    
    /**
     * 
     * @see  #getProblems()
     */
    ResultStatus getResultStatus() {
        return resultStatus;
    }
    
    /**
     * Returns a list of problems encountered during the pre-check or 
     * during replacing. The type of problems (pre-check or replacing)
     * can be determined from the results status returned by method
     * {@link #getResultStatus()}.
     * 
     * @return  array of problems, or {@code null} if no problems have been
     *          encountered
     * @see  #getResultStatus()
     */
    String[] getProblems() {
        return problems.isEmpty()
               ? null
               : problems.toArray(new String[problems.size()]);
    }
    
}