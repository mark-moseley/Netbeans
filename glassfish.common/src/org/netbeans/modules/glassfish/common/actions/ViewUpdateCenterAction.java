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

package org.netbeans.modules.glassfish.common.actions;

import org.netbeans.modules.glassfish.common.SimpleIO;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.netbeans.spi.glassfish.GlassfishModule;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.execution.NbProcessDescriptor;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;
import org.openide.util.actions.NodeAction;

/** 
 * This action will run the update center tool for Glassfish.
 * 
 * @author Peter Williams
 */
public class ViewUpdateCenterAction extends NodeAction {

    private static final String SHOW_UPDATE_CENTER_ICONBASE = 
            "org/netbeans/modules/glassfish/common/resources/UpdateCenter.gif"; // NOI18N

    private static WeakHashMap<String, Process> taskMap = new WeakHashMap<String, Process>();
    
    @Override
    protected void performAction(Node[] nodes) {
        GlassfishModule commonSupport = nodes[0].getLookup().lookup(GlassfishModule.class);
        if(commonSupport != null) {
            // updatetool already running?  if yes, is there a crossplatform way
            // to set focus to it?

            // If not running, install it if necessary, then run it.
            Map<String, String> ip = commonSupport.getInstanceProperties();
            final String serverUrl = ip.get(GlassfishModule.URL_ATTR);
            final String serverName = ip.get(GlassfishModule.DISPLAY_NAME_ATTR);
            final String installRoot = ip.get(GlassfishModule.INSTALL_FOLDER_ATTR);
            final File installDir = new File(installRoot);
            final File launcher = getUpdateCenterLauncher(installDir);

            Process p = null;
            synchronized (taskMap) {
                p = taskMap.get(serverUrl);
            }
            
            if(p != null) {
                String message = NbBundle.getMessage(ViewUpdateCenterAction.class, 
                        "MSG_UpdateCenterDownloading", serverName);
                NotifyDescriptor nd = new NotifyDescriptor.Confirmation(message,
                        NotifyDescriptor.DEFAULT_OPTION, NotifyDescriptor.INFORMATION_MESSAGE);
                DialogDisplayer.getDefault().notify(nd);
                return;
            } 

            if(launcher != null) {
                RequestProcessor.getDefault().post(new Runnable() {
                    public void run() {
                        try {
                            if(isUCInstalled(serverName, serverUrl, installDir, launcher)) {
                                new NbProcessDescriptor(launcher.getPath(), "").exec();
                            }
                        } catch(Exception ex) {
                            Logger.getLogger("glassfish").log(Level.WARNING, ex.getLocalizedMessage(), ex);
                        }
                    }
                });
            } else {
                String message = NbBundle.getMessage(ViewUpdateCenterAction.class, 
                        "MSG_UpdateCenterNotFound", serverName);
                NotifyDescriptor nd = new NotifyDescriptor.Confirmation(message,
                        NotifyDescriptor.DEFAULT_OPTION, NotifyDescriptor.ERROR_MESSAGE);
                DialogDisplayer.getDefault().notify(nd);
            }
        }
    }
    
    /**
     * Locate update center launcher within the glassfish installation
     *   [installRoot]/updatecenter/bin/updatetool[.BAT]
     * 
     * @param asInstallRoot appserver install location
     * @return File reference to launcher, or null if not found.
     */
    private File getUpdateCenterLauncher(File installRoot) {
        File result = null;
        if(installRoot != null && installRoot.exists()) {
            File updateCenterBin = new File(installRoot, "bin"); // NOI18N
            if(updateCenterBin.exists()) {
                String launcher = "updatetool"; // NOI18N
                if(Utilities.isWindows()) {
                    launcher += ".BAT"; // NOI18N
                }
                File launcherPath = new File(updateCenterBin, launcher);
                result = (launcherPath.exists()) ? launcherPath : null;
            }
        }
        return result;
    }
    
    private boolean isUCInstalled(String serverName, String serverUrl, File installRoot, File launcher) {
        if(new File(installRoot, "updatetool/bin").exists()) {
            return true;
        }
        
        String message = NbBundle.getMessage(ViewUpdateCenterAction.class, 
                "MSG_QueryInstallUpdateCenter", serverName);
        NotifyDescriptor nd = new NotifyDescriptor.Confirmation(message,
                NotifyDescriptor.YES_NO_OPTION, NotifyDescriptor.QUESTION_MESSAGE);
        
        boolean result = false;
        if(DialogDisplayer.getDefault().notify(nd) == NotifyDescriptor.YES_OPTION) {
            Writer writer = null;
            try {
                Process process = new NbProcessDescriptor(launcher.getPath(), "").exec();
                synchronized(taskMap) {
                    taskMap.put(serverUrl, process);
                }
                
                SimpleIO ucIO = new SimpleIO("Update Center Installer", process);
                ucIO.readInputStreams(process.getInputStream(), process.getErrorStream());
                writer = new OutputStreamWriter(process.getOutputStream());
                writer.write("y\n");
                writer.flush();
                int exitCode = process.waitFor();
                Logger.getLogger("glassfish").log(Level.FINEST, "UC exit code = " + exitCode);
                if(exitCode == 0) {
                    writer.close();
                    writer = null;
                    ucIO.closeIO();
                    result = true;
                }
            } catch(InterruptedException ex) {
                Logger.getLogger("glassfish").log(Level.WARNING, ex.getLocalizedMessage(), ex);
            } catch(IOException ex) {
                Logger.getLogger("glassfish").log(Level.WARNING, ex.getLocalizedMessage(), ex);
            } finally {
                synchronized (taskMap) {
                    taskMap.remove(serverUrl);
                }
                
                if(writer != null) {
                    try { writer.close(); } catch(IOException ex) { }
                }
            }
        }
        return result;
    }
    
    @Override
    protected boolean enable(Node[] nodes) {
        if(nodes != null && nodes.length == 1 && nodes[0] != null) {
            GlassfishModule commonSupport = nodes[0].getLookup().lookup(GlassfishModule.class);
            if(commonSupport != null) {
                String installRoot = commonSupport.getInstanceProperties().get(GlassfishModule.INSTALL_FOLDER_ATTR);
                return installRoot != null;
            }
        }
        return false;
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(ViewUpdateCenterAction.class, "CTL_ViewUpdateCenterAction");
    }

    @Override
    protected String iconResource() {
        return SHOW_UPDATE_CENTER_ICONBASE;
    }
    
    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
    
}
