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

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;
import java.util.logging.Logger;

/**
 *
 * @author gordonp
 */
public abstract class RemoteConnectionSupport {
    
    private JSch jsch;
    protected String key;
    protected Session session;
    protected Channel channel;
    private String user;
    private int exit_status;
    private boolean cancelled = false;
    protected static Logger log = Logger.getLogger("cnd.remote.logger"); // NOI18N
    
    public RemoteConnectionSupport(String key, int port) {
        this.key = key;
        int pos = key.indexOf('@');
        user = key.substring(0, pos);
        String host = key.substring(pos + 1);
        exit_status = -1; // this is what JSch initializes it to...
        
        try {
            jsch = new JSch();
            jsch.setKnownHosts(System.getProperty("user.home") + "/.ssh/known_hosts");
            session = jsch.getSession(user, host, port);

            RemoteUserInfo ui = RemoteUserInfo.getUserInfo(key);
            session.setUserInfo(ui);
            session.connect();
            if (!session.isConnected()) {
                System.err.println("");
            }
        } catch (JSchException jsce) {
            log.warning("RPB<Init>: Got JSchException [" + jsce.getMessage() + "]");
            String msg = jsce.getMessage();
            if (msg.equals("Auth cancel")) {
                cancelled = true;
            }
        }
    }
    
    public RemoteConnectionSupport(String key) {
        this(key, 22);
    }
    
    public Channel getChannel() {
        return channel;
    }
    
    public void setChannel(Channel channel) {
        this.channel = channel;
    }
    
    protected abstract Channel createChannel() throws JSchException;
    
    public int getExitStatus() {
        return !cancelled && channel != null ? channel.getExitStatus() : -1; // JSch initializes exit status to -1
    }
    
    public boolean isCancelled() {
        return cancelled;
    }
    
    protected void setExitStatus(int exit_status) {
        this.exit_status = exit_status;
    }
    
    protected void disconnect() {
        channel.disconnect();
        session.disconnect();
    }
    
    public String getUser() {
        return user;
    }
}
