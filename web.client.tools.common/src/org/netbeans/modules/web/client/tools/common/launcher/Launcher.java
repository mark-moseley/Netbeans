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

package org.netbeans.modules.web.client.tools.common.launcher;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import org.openide.util.Utilities;

public class Launcher {
    private static class ProcessWaiter extends Thread {
        private Process process;
        private String command;

        ProcessWaiter(String command, Process process) {
            this.command = command;
            this.process = process;
            setDaemon(true);
        }

        public void run() {
            try {
                int exitStatus = process.waitFor();               
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
    }

    public static Process launch(LaunchDescriptor launchDescriptor) throws IOException {
        List<String> command = new LinkedList<String>();
        command.addAll(Arrays.asList(launchDescriptor.getLaunchCommand()));
        
        for (String uri : launchDescriptor.getURIs()) {
            command.add(uri);
        }
        
        ProcessBuilder processBuilder = new ProcessBuilder(command);

        Process process = processBuilder.start();
        Thread processWaiter = new ProcessWaiter(command.toString(), process);
        processWaiter.start();
        return process;
    }

    public static class LaunchDescriptor {
        private static final String[] MACOS_FIREFOX_LAUNCH_COMMAND = new String[] {
            "/usr/bin/open",
            "-b",
            "org.mozilla.firefox"
        };

        private String executablePath;
        private String[] computedExecutablePath;
        private List<String> uriList;

        public LaunchDescriptor(String executablePath) {
            this.executablePath = executablePath;

            if (Utilities.isMac()) {
                computedExecutablePath = MACOS_FIREFOX_LAUNCH_COMMAND;
            } else {
                computedExecutablePath = new String[]{executablePath};
            }
        }

        public String[] getLaunchCommand() {
            return computedExecutablePath;
        }

        public String getURI() {
            if (uriList != null && uriList.size() > 0) {
                return uriList.get(0);
            }
            
            return null;
        }

        public void setURI(String uri) {
            this.uriList = new ArrayList<String>();
            this.uriList.add(uri);
        }
        
        public void setURI(List<String> uriList) {
            this.uriList = uriList;
        }
        
        public List<String> getURIs() {
            return uriList;
        }
    }
}

