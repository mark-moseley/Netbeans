/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU General
 * Public License Version 2 only ("GPL") or the Common Development and Distribution
 * License("CDDL") (collectively, the "License"). You may not use this file except in
 * compliance with the License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html or nbbuild/licenses/CDDL-GPL-2-CP. See the
 * License for the specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header Notice in
 * each file and include the License file at nbbuild/licenses/CDDL-GPL-2-CP.  Sun
 * designates this particular file as subject to the "Classpath" exception as
 * provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the License Header,
 * with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original Software
 * is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun Microsystems, Inc. All
 * Rights Reserved.
 * 
 * If you wish your version of this file to be governed by only the CDDL or only the
 * GPL Version 2, indicate your decision by adding "[Contributor] elects to include
 * this software in this distribution under the [CDDL or GPL Version 2] license." If
 * you do not indicate a single choice of license, a recipient has the option to
 * distribute your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above. However, if
 * you add GPL Version 2 code and therefore, elected the GPL Version 2 license, then
 * the option applies only if the new code is made subject to such option by the
 * copyright holder.
 */

package org.netbeans.installer.utils;

import java.lang.Thread.UncaughtExceptionHandler;
import org.netbeans.installer.utils.helper.ErrorLevel;
import org.netbeans.installer.utils.helper.FinishHandler;

/**
 *
 * @author Kirill Sorokin
 */
public final class ErrorManager {
    /////////////////////////////////////////////////////////////////////////////////
    // Static
    private static UncaughtExceptionHandler exceptionHandler;
    private static FinishHandler finishHandler;
    
    public static synchronized void notifyDebug(String message) {
        notify(ErrorLevel.DEBUG, message);
    }
    
    public static synchronized void notifyDebug(String message, Throwable e) {
        notify(ErrorLevel.DEBUG, message, e);
    }
    
    public static synchronized void notify(String message) {
        notify(ErrorLevel.MESSAGE, message);
    }
    
    public static synchronized void notify(String message, Throwable e) {
        notify(ErrorLevel.MESSAGE, message, e);
    }
    
    public static synchronized void notifyWarning(String message) {
        notify(ErrorLevel.WARNING, message);
    }
    
    public static synchronized void notifyWarning(String message, Throwable e) {
        notify(ErrorLevel.WARNING, message, e);
    }
    
    public static synchronized void notifyError(String message) {
        notify(ErrorLevel.ERROR, message);
    }
    
    public static synchronized void notifyError(String message, Throwable e) {
        notify(ErrorLevel.ERROR, message, e);
    }
    
    public static synchronized void notifyCritical(String message) {
        notify(ErrorLevel.CRITICAL, message);
    }
    
    public static synchronized void notifyCritical(String message, Throwable e) {
        notify(ErrorLevel.CRITICAL, message, e);
    }
    
    public static synchronized void notify(int level, String message) {
        notify(level, message, null);
    }
    
    public static synchronized void notify(int level, Throwable exception) {
        notify(level, null, exception);
    }
    
    public static synchronized void notify(int level, String message, Throwable exception) {
        // parameters validation
        assert (message != null) || (exception != null);
        
        String dialogText = StringUtils.EMPTY_STRING;
        
        if (message != null) {
            LogManager.log(level, message);
            dialogText = message;
        } else {
            dialogText = ResourceUtils.getString(ErrorManager.class, 
                ERROR_UNEXPECTED_ERROR_KEY);
        }
        if (exception != null) {
            LogManager.log(level, exception);
            dialogText += ResourceUtils.getString(ErrorManager.class, 
                    ERROR_EXCEPTION_MESSAGE_KEY,
                    exception.getClass().getName(),
                    exception.getMessage());
        }
        if(LogManager.getLogFile()!=null) {
            dialogText += ResourceUtils.getString(ErrorManager.class,
                    ERROR_LOGFILE_INFO_KEY,
                    LogManager.getLogFile().getAbsolutePath());
        }
        switch (level) {
            case ErrorLevel.MESSAGE:
                UiUtils.showMessageDialog(
                        dialogText,
                        ResourceUtils.getString(
                        ErrorManager.class, ERROR_MESSAGE_KEY),
                        UiUtils.MessageType.INFORMATION);
                return;
            case ErrorLevel.WARNING:
                UiUtils.showMessageDialog(
                        dialogText,
                        ResourceUtils.getString(
                        ErrorManager.class, ERROR_WARNING_KEY),
                        UiUtils.MessageType.WARNING);
                return;
            case ErrorLevel.ERROR:
                UiUtils.showMessageDialog(
                        dialogText,
                        ResourceUtils.getString(
                        ErrorManager.class, ERROR_ERROR_KEY),
                        UiUtils.MessageType.ERROR);
                return;
            case ErrorLevel.CRITICAL:
                UiUtils.showMessageDialog(
                        dialogText,
                        ResourceUtils.getString(
                        ErrorManager.class, ERROR_CRITICAL_KEY),
                        UiUtils.MessageType.CRITICAL);
                finishHandler.criticalExit();
                return;
            default:
                return;
        }
    }
    
    public static UncaughtExceptionHandler getExceptionHandler() {
        return exceptionHandler;
    }
    
    public static void setExceptionHandler(final UncaughtExceptionHandler exceptionHandler) {
        ErrorManager.exceptionHandler = exceptionHandler;
    }
    
    public static FinishHandler getFinishHandler() {
        return finishHandler;
    }
    
    public static void setFinishHandler(final FinishHandler finishHandler) {
        ErrorManager.finishHandler = finishHandler;
    }
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    private ErrorManager() {
        // does nothing
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Inner Classes
    public static class ExceptionHandler implements UncaughtExceptionHandler {
        public void uncaughtException(
                final Thread thread,
                final Throwable exception) {
            ErrorManager.notifyCritical(
                    ResourceUtils.getString(ErrorManager.class,
                    ERROR_UNEXPECTED_EXCEPTION_KEY,thread.getName()),
                    exception);
        }
    }
    private static final String ERROR_UNEXPECTED_EXCEPTION_KEY =
            "EM.ununexpected.exception";//NOI18N
    private static final String ERROR_CRITICAL_KEY =
            "EM.errortype.critical";//NOI18N
    private static final String ERROR_WARNING_KEY =
            "EM.errortype.warning";//NOI18N
    private static final String ERROR_ERROR_KEY =
            "EM.errortype.error";//NOI18N
    private static final String ERROR_MESSAGE_KEY =
            "EM.errortype.message";//NOI18N
    private static final String ERROR_UNEXPECTED_ERROR_KEY = 
            "EM.unexpected.error";//NOI18N
    private static final String ERROR_EXCEPTION_MESSAGE_KEY =
            "EM.exception.message";//NOI18N
    private static final String ERROR_LOGFILE_INFO_KEY =
            "EM.exception.logfile.info";//NOI18N
}
