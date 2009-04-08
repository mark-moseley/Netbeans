/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.dbgp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.php.project.api.PhpOptions;

/**
 * @author Radek Matous
 */
class ProxyClient {

    private static final Logger LOGGER = Logger.getLogger(ProxyClient.class.getName());
    private String proxyHost;
    private int proxyPort;
    private int idePort;
    private String ideKey;

    /**
     * @return instance or null if proxy isn't enabled
     */
    static ProxyClient getInstance(DebuggerOptions options) {
        return options.getDebugProxy() != null ? new ProxyClient(options) : null;
    }

    private ProxyClient(DebuggerOptions options) {
        assert options != null;
        assert options.getDebugProxy().first != null;
        assert options.getDebugProxy().second != null;

        this.proxyHost = options.getDebugProxy().first;
        this.proxyPort = options.getDebugProxy().second;
        this.idePort = PhpOptions.getInstance().getDebuggerPort();
        this.ideKey = PhpOptions.getInstance().getDebuggerSessionId();
    }

    boolean init() {
        boolean retval = false;
        String command = getProxyInitCommand(idePort, ideKey);
        try {
            Socket socket = new Socket(proxyHost, proxyPort);
            OutputStream outputStream = socket.getOutputStream();
            InputStream inputStream = socket.getInputStream();
            try {
                sendCommand(outputStream, command);
                String response = getResponse(inputStream, command);
                retval = response != null && response.indexOf("success=\"1\"") != -1;
            } finally {
                outputStream.close();
                inputStream.close();
                socket.close();
            }
        } catch (UnknownHostException ex) {
            if (LOGGER.isLoggable(Level.INFO)) {
                LOGGER.log(Level.INFO, ex.getLocalizedMessage(), ex);//NOI18N
            }
        } catch (IOException ex) {
            if (LOGGER.isLoggable(Level.INFO)) {
                LOGGER.log(Level.INFO, ex.getLocalizedMessage(), ex);//NOI18N
            }
        }
        return retval;
    }

    void close() {
        String command = getProxyStopCommand(ideKey);
        try {
            Socket socket = new Socket(proxyHost, proxyPort);
            OutputStream outputStream = socket.getOutputStream();
            try {
                sendCommand(outputStream, command);
            } finally {
                outputStream.close();
                socket.close();
            }
        } catch (UnknownHostException ex) {
            if (LOGGER.isLoggable(Level.INFO)) {
                LOGGER.log(Level.INFO, ex.getLocalizedMessage(), ex);//NOI18N
            }
        } catch (IOException ex) {
            if (LOGGER.isLoggable(Level.INFO)) {
                LOGGER.log(Level.INFO, ex.getLocalizedMessage(), ex);//NOI18N
            }
        }
    }

    private static String getProxyInitCommand(int idePort, String ideKey) {
        return String.format("proxyinit -p %d -k %s -m 0", idePort, ideKey);//NOI18N
    }

    private static String getProxyStopCommand(String ideKey) {
        return String.format("proxystop -k %s", ideKey);//NOI18N
    }

    private static void sendCommand(OutputStream outputStream, String command) throws IOException {
        outputStream.write(command.getBytes());
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.log(Level.FINE, String.format("send command: %s: ", command));//NOI18N
        }
    }

    private static String getResponse(InputStream inputStream, String command) throws IOException {
        String retval = null;
        int available = inputStream.available();
        if (available > 0) {
            byte[] responseBytes = new byte[available];
            int read = inputStream.read(responseBytes);
            if (read == available) {
                retval = new String(responseBytes);
            }
        }
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.log(Level.FINE, String.format("response(%s) is %s: ", command, retval));//NOI18N
        }
        return retval;
    }
}