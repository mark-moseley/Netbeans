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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.lib.cvsclient.connection;

import org.netbeans.lib.cvsclient.command.CommandAbortedException;
import org.netbeans.lib.cvsclient.util.LoggedDataInputStream;
import org.netbeans.lib.cvsclient.util.LoggedDataOutputStream;

import java.io.IOException;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;

/**
 * Provides support for the :ext: connection method.
 *
 * @author Maros Sandor
 */
public class ExtConnection extends AbstractConnection {

    private final String command;

    private Process process;

    /**
     * Creates new EXT connection method support class. Given command will be used for getting I/O
     * streams to CVS server. 
     * 
     * @param command command to execute
     */ 
    public ExtConnection(String command) {
        this.command = command;
    }

    public void open() throws AuthenticationException, CommandAbortedException {
        try {
            process = Runtime.getRuntime().exec(command);
            setInputStream(new LoggedDataInputStream(new BufferedInputStream(process.getInputStream())));
            setOutputStream(new LoggedDataOutputStream(new BufferedOutputStream(process.getOutputStream())));
        } catch (IOException e) {
            throw new AuthenticationException(e, "Failed to execute: " + command);
        }
    }

    public void verify() throws AuthenticationException {
        try {
            open();
            verifyProtocol();
            process.destroy();
        } catch (Exception e) {
            throw new AuthenticationException(e, "Failed to execute: " + command);
        }
    }

    public void close() throws IOException {
        if (isOpen()) {
            process.destroy();
        }
    }

    public boolean isOpen() {
        if (process == null) return false;
        try {
            process.exitValue();
            return false;
        } catch (IllegalThreadStateException e) {
            return true;
        }
    }

    public int getPort() {
        return 0;
    }

    public void modifyInputStream(ConnectionModifier modifier) throws IOException {
        modifier.modifyInputStream(getInputStream());
    }

    public void modifyOutputStream(ConnectionModifier modifier) throws IOException {
        modifier.modifyOutputStream(getOutputStream());
    }
}
