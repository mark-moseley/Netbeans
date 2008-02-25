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

package org.netbeans.modules.glassfish.common;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.Manifest;


/**
 * Abstraction of commands for V3 server administration
 *
 * @author Peter Williams
 */
public abstract class ServerCommand {

    private ServerCommand() {
    }
    
    /**
     * Override to provide the server command represented by this object.  Caller
     * will prefix with http://host:port/__asadmin/ and open the server connection.
     * 
     * @return suffix to append to [host]/__asadmin/ for server command.
     */
    public abstract String getCommand();

    /**
     * Override to change the type of HTTP method used for this command.
     * Default is GET.
     * 
     * @return HTTP method (GET, PUT, etc.)
     */
    public String getRequestMethod() {
        return "GET"; // NOI18N
    }
    
    /**
     * Override and return true to send information to the server (HTTP PUT).
     * Default is false.
     * 
     * @return HTTP method (GET, PUT, etc.)
     */
    public boolean getDoOutput() {
        return false;
    }
    
    /**
     * Override to set the content-type of information sent to the server.
     * Default is null (not set).
     * 
     * @return HTTP method (GET, PUT, etc.)
     */
    public String getContentType() {
        return null;
    }
    
    /**
     * Override to provide a data stream for PUT requests.  Data will be read
     * from this stream [until EOF?] and sent to the server.
     * 
     * @return a new InputStream derivative that provides the data to send
     *  to the server.  Caller is responsible for closing the stream.  Can
     *  return null, in which case no data will be sent.
     */
    public InputStream getInputStream() {
        return null;
    }
    
    /**
     * Override for command specific failure checking.
     * 
     * @param responseCode code returned by http request
     * @return true if response was acceptable (e.g. 200) and handling of result
     * should proceed.
     */
    public boolean handleResponse(int responseCode) {
        return responseCode == 200;
    }
    
    /**
     * Override this to read the response data sent by the server (e.g. list-applications
     * sends the currently deployed applications.)  Do not close the stream parameter
     * when finished.  Caller will take care of that.
     * 
     * @param in Stream to read data from.
     * @return true if response was read correctly.
     * @throws java.io.IOException in case of stream error.
     */
    public boolean readResponse(InputStream in) throws IOException {
        return true;
    }
    
    /**
     * Override this to parse, validate, and/or format any data read from the 
     * server in readResponse().
     * 
     * @return true if data was processed correctly.
     */
    public boolean processResponse() {
        return true;
    }
    
    // ------------------------------------------------------------------------
    // Specific server commands.
    // ------------------------------------------------------------------------
    
    /**
     * Command to start a server domain
     */
    public static final ServerCommand START = new ServerCommand() {
        
        @Override
        public String getCommand() { 
            return "start-domain"; // NOI18N
        } 
        
    };
    
    /**
     * Command to stop a server domain
     */
    public static final ServerCommand STOP = new ServerCommand() {
        
        @Override
        public String getCommand() { 
            return "stop-domain"; // NOI18N
        } 
        
    };
    
    /**
     * Command to list applications current deployed on the server.
     */
    public static final class ListCommand extends ServerCommand {
        
        private Manifest list = null;
        private List<String> containerList = null;
        private List<String> appList = null; 
        
        public String [] getContainers() {
            if(containerList != null && containerList.size() > 0) {
                return containerList.toArray(new String [containerList.size()]);
            }
            return null;
        }
        
        public String [] getApplications() {
            if(appList != null && appList.size() > 0) {
                return appList.toArray(new String [appList.size()]);
            }
            return null;
        }
        
        @Override
        public String getCommand() { 
            return "list-applications"; // NOI18N
        }
        
        @Override
        public boolean readResponse(InputStream in) throws IOException {
            boolean result = false;

            Manifest m = new Manifest();
            m.read(in);
            String outputCode = m.getMainAttributes().getValue("exit-code"); // NOI18N
            if(outputCode.equalsIgnoreCase("Success")) { // NOI18N
                list = m;
                result = true;
            }
            
            return result;
        }
        
        @Override
        public boolean processResponse() {
            if(list == null) {
                return false;
            }
            
            String containerDesc = list.getMainAttributes().getValue("children"); // NOI18N
            if(containerDesc == null || containerDesc.length() == 0) {
                // no containers running...
                return true;
            }

            String [] containers = containerDesc.split(","); // NOI18N
            containerList = new ArrayList<String>(containers.length);
            for(String container: containers) {
                // get container attributes
                Attributes contAttr = list.getAttributes(container);
                String appDesc = contAttr.getValue("children"); // NOI18N
                if(appDesc == null) {
                    // no apps currently deployed in this container
                    continue;
                }

                // !PW XXX Do we want/need to show empty containers?
                // Only add the container if there are running apps...
                containerList.add(container);
                
                String [] apps = appDesc.split(","); // NOI18N
                appList = new ArrayList<String>(apps.length);
                for(String app: apps) {
                    Attributes appAttr = list.getAttributes(app);
                    appList.add(appAttr.getValue("message")); // NOI18N
                }
            }
            
            return true;
        }
    
    };
    
    /**
     * Command to deploy a directory
     */
    public static final class DeployCommand extends ServerCommand {
        
        private final String path;
        private final String name;
        
        public DeployCommand(final String path) {
            this(path, null);
        }
        
        public DeployCommand(final String path, final String name) {
            this.path = path;
            this.name = name;
        }
        
        @Override
        public String getCommand() { 
            StringBuilder cmd = new StringBuilder(128);
            cmd.append("deploy?path="); // NOI18N
            cmd.append(path);
            if(name != null && name.length() > 0) {
                cmd.append("?name="); // NOI18N
                cmd.append(name);
            }
            return cmd.toString();
        } 
        
    }
    
    /**
     * Command to redeploy a directory deployed app that is already deployed.
     */
    public static final class RedeployCommand extends ServerCommand {
        
        private final String name;
        
        public RedeployCommand(final String name) {
            this.name = name;
        }
        
        @Override
        public String getCommand() { 
            return "redeploy?name=" + name; // NOI18N
        }
        
    }
    
    /**
     * Command to undeploy a deployed application.
     */
    public static final class UndeployCommand extends ServerCommand {
        
        private final String name;
        
        public UndeployCommand(final String name) {
            this.name = name;
        }
        
        @Override
        public String getCommand() { 
            return "undeploy?name=" + name; // NOI18N
        }
        
    }
    
}
