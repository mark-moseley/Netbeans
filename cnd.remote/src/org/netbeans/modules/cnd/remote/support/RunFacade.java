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
import org.netbeans.modules.cnd.api.utils.RemoteUtils;
import org.openide.util.Exceptions;

/**
 * Execution facade for simple tasks.
 *
 * @author Sergey Grinev
 */
public abstract class RunFacade {

    public static RunFacade getInstance(String hkey) {
        if (RemoteUtils.isLocalhost(hkey)) {
            return new RunFacadeLocal();
        } else {
            return new RunFacadeRemote(hkey);
        }
    }

    public boolean run(String command) {
        return doRun(command) != -1;
    }

    protected abstract int doRun(String command);

    protected String output = null;

    public String getOutput() {
        return output;
    }

    private static class RunFacadeLocal extends RunFacade {

        @Override
        public int doRun(String command) {
            int exitValue = -1;
            try {
                Process process = Runtime.getRuntime().exec(command);
                InputStream outputStream = process.getInputStream();
                if (outputStream != null) {
                    output = outputStream.toString();
                }
                process.waitFor();
                exitValue = process.exitValue();
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
            return exitValue;
        }
    }

    private static class RunFacadeRemote extends RunFacade {

        private String hkey;

        public RunFacadeRemote(String hkey) {
            this.hkey = hkey;
        }

        @Override
        public int doRun(String command) {
            RemoteCommandSupport support = new RemoteCommandSupport(hkey, command);
            output = support.toString();
            return support.getExitStatus();
        }
    }
}
