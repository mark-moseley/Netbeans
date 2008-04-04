/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.php.project.api;

import java.util.prefs.Preferences;
import org.openide.util.NbPreferences;

/**
 * Helper class to get actual PHP properties like debugger port etc.
 * Use {@link #getInstance()} to get class instance.
 * @author Tomas Mysik
 * @since 1.2
 */
public final class PhpOptions {

    private static final PhpOptions INSTANCE = new PhpOptions();

    // php cli
    private static final String PHP_INTERPRETER = "phpInterpreter"; // NOI18N
    private static final String PHP_OPEN_IN_OUTPUT = "phpOpenInOutput"; // NOI18N
    private static final String PHP_OPEN_IN_BROWSER = "phpOpenInBrowser"; // NOI18N
    private static final String PHP_OPEN_IN_EDITOR = "phpOpenInEditor"; // NOI18N

    // debugger
    private static final String PHP_DEBUGGER_PORT = "phpDebuggerPort"; // NOI18N
    private static final String PHP_DEBUGGER_STOP_AT_FIRST_LINE = "phpDebuggerStopAtFirstLine"; // NOI18N

    // global include path
    private static final String PHP_GLOBAL_INCLUDE_PATH = "phpGlobalIncludePath"; // NOI18N

    private PhpOptions() {
    }

    public static PhpOptions getInstance() {
        return INSTANCE;
    }

    private Preferences getPreferences() {
        return NbPreferences.forModule(PhpOptions.class);
    }

    public String getPhpInterpreter() {
        return getPreferences().get(PHP_INTERPRETER, null);
    }

    public void setPhpInterpreter(String phpInterpreter) {
        getPreferences().put(PHP_INTERPRETER, phpInterpreter);
    }

    public boolean isOpenResultInOutputWindow() {
        return getPreferences().getBoolean(PHP_OPEN_IN_OUTPUT, true);
    }

    public void setOpenResultInOutputWindow(boolean openResultInOutputWindow) {
        getPreferences().putBoolean(PHP_OPEN_IN_OUTPUT, openResultInOutputWindow);
    }

    public boolean isOpenResultInBrowser() {
        return getPreferences().getBoolean(PHP_OPEN_IN_BROWSER, false);
    }

    public void setOpenResultInBrowser(boolean openResultInBrowser) {
        getPreferences().putBoolean(PHP_OPEN_IN_BROWSER, openResultInBrowser);
    }

    public boolean isOpenResultInEditor() {
        return getPreferences().getBoolean(PHP_OPEN_IN_EDITOR, false);
    }

    public void setOpenResultInEditor(boolean openResultInEditor) {
        getPreferences().putBoolean(PHP_OPEN_IN_EDITOR, openResultInEditor);
    }

    public int getDebuggerPort() {
        return getPreferences().getInt(PHP_DEBUGGER_PORT, 9000);
    }

    public void setDebuggerPort(int debuggerPort) {
        getPreferences().putInt(PHP_DEBUGGER_PORT, debuggerPort);
    }

    public boolean isDebuggerStoppedAtTheFirstLine() {
        return getPreferences().getBoolean(PHP_DEBUGGER_STOP_AT_FIRST_LINE, false);
    }

    public void setDebuggerStoppedAtTheFirstLine(boolean debuggerStoppedAtTheFirstLine) {
        getPreferences().putBoolean(PHP_DEBUGGER_STOP_AT_FIRST_LINE, debuggerStoppedAtTheFirstLine);
    }

    public String getPhpGlobalIncludePath() {
        return getPreferences().get(PHP_GLOBAL_INCLUDE_PATH, ""); // NOI18N
    }

    public void setPhpGlobalIncludePath(String phpGlobalIncludePath) {
        getPreferences().put(PHP_GLOBAL_INCLUDE_PATH, phpGlobalIncludePath);
    }
}
