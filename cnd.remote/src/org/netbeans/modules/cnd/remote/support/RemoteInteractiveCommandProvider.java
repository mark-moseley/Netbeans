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

package org.netbeans.modules.cnd.remote.support;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import org.netbeans.modules.cnd.api.remote.InteractiveCommandProvider;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;

/**
 * Run a remote command which requires interactive I/O. The caller is responsible for setting up the
 * reader and writers via the getInputStream() and getOutputStream() methods.
 * 
 * @author gordonp
 */
public class RemoteInteractiveCommandProvider implements InteractiveCommandProvider {

    public RemoteInteractiveCommandProvider(ExecutionEnvironment execEnv) {
        executionEnvironment = execEnv;
        log.finest(getClass().getSimpleName() + " .ctor " + execEnv);
    }
    
    private RemoteInteractiveCommandSupport support;
    private ExecutionEnvironment executionEnvironment;
    private Logger log = Logger.getLogger("cnd.remote.logger"); //NOI18N

    public boolean run(ExecutionEnvironment execEnv, String cmd, Map<String, String> env) {
        log.finest(getClass().getSimpleName() + " running (1) " + cmd + " on " + execEnv);
        support = new RemoteInteractiveCommandSupport(execEnv, cmd, env);
        return !support.isFailedOrCancelled();
    }

    public boolean run(List<String> commandAndArgs, String workingDirectory, Map<String, String> env) {
        assert executionEnvironment != null;
        log.finest(getClass().getSimpleName() + " running (2) " + commandAndArgs + " on " + executionEnvironment);
        StringBuilder plainCommand = new StringBuilder();
        
        for (String arg : commandAndArgs) {
            plainCommand.append(arg);
            plainCommand.append(' ');
        }
        support = new RemoteInteractiveCommandSupport(executionEnvironment, plainCommand.toString(), env);
        return !support.isFailedOrCancelled();
    }

    public InputStream getInputStream() throws IOException {
        return support == null ? null : support.getInputStream();
    }

    public OutputStream getOutputStream() throws IOException {
        return support == null ? null : support.getOutputStream();
    }
    
    public void disconnect() {
//        if (support != null) {
//            support.disconnect();
//        }
    }

    public int waitFor() {
        return support == null ? -1 : support.waitFor();
    }

    public int getExitStatus() {
        return support == null ? -1 : support.getExitStatus();
    }
}
