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

package org.netbeans.modules.glassfish.common;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.logging.Logger;
import java.util.logging.Level;
import javax.swing.SwingUtilities;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.glassfish.spi.GlassfishModule;
import org.netbeans.modules.glassfish.spi.ServerUtilities;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Cancellable;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

public class CreateDomain extends Thread {

    static final String PORTBASE = "portbase";
    final private String uname;
    final private String pword;
    final private File platformLocation;
    final private Map<String, String> map;
    final private Map<String, String> ip;
    private GlassfishInstanceProvider gip;

    public CreateDomain(String uname, String pword, File platformLocation, 
            Map<String, String> ip, GlassfishInstanceProvider gip) {
        this.uname = uname;
        this.pword = pword;
        this.platformLocation = platformLocation;
        this.ip = ip;
        this.map = new HashMap<String,String>();
        this.gip = gip;
        map.putAll(ip);
        computePorts(ip,map);
    }

    static private void computePorts(Map<String, String> ip, Map<String, String> createProps) {
        int portBase = 8900;
        int kicker = ((new Date()).toString() + ip.get(GlassfishModule.DOMAINS_FOLDER_ATTR)+ip.get(GlassfishModule.DOMAIN_NAME_ATTR)).hashCode() % 40000;
        kicker = kicker < 0 ? -kicker : kicker;
        
        int httpPort = portBase + kicker + 80;
        int adminPort = portBase + kicker + 48;
        ip.put(GlassfishModule.HTTPPORT_ATTR, Integer.toString(httpPort));
        ip.put(GlassfishModule.ADMINPORT_ATTR, Integer.toString(adminPort));
        createProps.put(GlassfishModule.HTTPPORT_ATTR, Integer.toString(httpPort));
        createProps.put(GlassfishModule.ADMINPORT_ATTR, Integer.toString(adminPort));
        createProps.put(CreateDomain.PORTBASE, Integer.toString(portBase+kicker));
    }

    @Override
    public void run() {
        Process process = null;
        // attempt to do the domian/instance create HERE
        File irf = platformLocation;
        int retVal = 0;
        if (null != irf && irf.exists()) {
            PDCancel pdcan;
            String installRoot = irf.getAbsolutePath();
            String asadminCmd = installRoot + File.separator +
                    "bin" + //NOI18N
                    File.separator +
                    "asadmin";                                                  //NOI18N
            if ("\\".equals(File.separator)) {                                  //NOI18N
                asadminCmd = asadminCmd + ".bat";                               //NOI18N
            }
            String domain = map.get(GlassfishModule.DOMAIN_NAME_ATTR);
            String domainDir = map.get(GlassfishModule.DOMAINS_FOLDER_ATTR);
            File passWordFile = createTempPasswordFile(pword, "changeit");//NOI18N

            if (passWordFile == null) {
                return;
            }
            String arrnd[];
            
            if ("".equals(pword)) {
                arrnd = new String[] {asadminCmd,
                    "create-domain", //NOI18N
                    "--domaindir", //NOI18N
                    domainDir,
                    "--portbase", //NOI18N
                    map.get(PORTBASE),
                    "--user", //NOI18N
                    uname,
                    domain
                };            
            } else {
                arrnd = new String[] {asadminCmd,
                    "create-domain", //NOI18N
                    "--domaindir", //NOI18N
                    domainDir,
                    "--portbase", //NOI18N
                    map.get(PORTBASE),
                    "--user", //NOI18N
                    uname,
                    "--passwordfile", //NOI18N
                    passWordFile.getAbsolutePath(),
                    domain
                };                
            }

            ProgressHandle ph = null;
            try {
                ExecSupport ee = new ExecSupport();
                process = Runtime.getRuntime().exec(arrnd);
                pdcan = new PDCancel(process, domainDir + File.separator + domain);
                ph = ProgressHandleFactory.createHandle(
                        NbBundle.getMessage(this.getClass(), "LBL_Creating_personal_domain"), // NI18N
                        pdcan);
                ph.start();

                ee.displayProcessOutputs(process,
                        NbBundle.getMessage(this.getClass(), "LBL_outputtab"),//NOI18N
                        NbBundle.getMessage(this.getClass(), "LBL_RunningCreateDomainCommand")//NOI18N
                        );
            } catch (MissingResourceException ex) {
                showInformation(ex.getLocalizedMessage());
            } catch (IOException ex) {
                showInformation(ex.getLocalizedMessage());
            } catch (InterruptedException ex) {
                showInformation(ex.getLocalizedMessage());
            } catch (RuntimeException ex) {
                showInformation(ex.getLocalizedMessage());
                // this is more interesting
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
                        ex);
            }
            if (null != process) {
                try {
                    retVal = process.waitFor();
                    if (!passWordFile.delete()) {
                        showInformation(NbBundle.getMessage(this.getClass(), "MSG_delete_password_failed", passWordFile.getAbsolutePath()));
                    }
                } catch (InterruptedException ie) {
                    retVal = -1;
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
                            ie);
                }
            } else {
                retVal = -1;
            }
            if (null != ph) {
                ph.finish();
            }
            if (0 == retVal) {
                // The create was successful... create the instance and register it.
                GlassfishInstance gi = GlassfishInstance.create(ip,gip);
                gip.addServerInstance(gi);
                NbPreferences.forModule(this.getClass()).putBoolean(ServerUtilities.PROP_FIRST_RUN, true);
            }
        }
    }

    static class PDCancel implements Cancellable {

        final private Process p;
        final private String dirname;
        private boolean notFired = true;

        PDCancel(Process p, String newDirName) {
            this.p = p;
            this.dirname = newDirName;
        }

        synchronized public boolean isNotFired() {
            return notFired;
        }

        synchronized public boolean cancel() {
            notFired = false;
            p.destroy();
            File domainDir = new File(dirname);
            if (domainDir.exists()) {
                FileObject fo = FileUtil.toFileObject(FileUtil.normalizeFile(domainDir));
                try {
                    fo.delete();
                } catch (IOException ex) {
                    Logger.getLogger(this.getClass().getName()).log(Level.FINER,"",ex);
                    showError(NbBundle.getMessage(GlassfishInstanceProvider.class, "ERR_Failed_cleanup", dirname));
                }
            }
            return true;
        }
    }

    /*
     * This create a temporary file, deleted at exit, that contains
     * the necessary password infos for starting or creating a domain
     * bot admu and master password are there.
     * @returns the temporary file
     * or null if for some reason, this file cannot be created.
     */
    private static File createTempPasswordFile(String password, String masterPassword) {
        OutputStream output;
        PrintWriter p = null;
        File retVal = null;
        try {
            retVal = File.createTempFile("admin", null);//NOI18N

            retVal.deleteOnExit();
            output = new FileOutputStream(retVal);
            p = new PrintWriter(output);
            p.println("AS_ADMIN_ADMINPASSWORD=" + password);//NOI18N for create domains

            p.println("AS_ADMIN_PASSWORD=" + password);//NOI18N for start domains

            p.println("AS_ADMIN_MASTERPASSWORD=" + masterPassword);//NOI18N

        } catch (IOException e) {
            // this should not happen... If it does we should at least log it
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
        } finally {
            if (p != null) {
                p.close();
            }
        }
        return retVal;
    }
    
    private static void showInformation(final String msg){
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                NotifyDescriptor d = new NotifyDescriptor.Message(msg, NotifyDescriptor.INFORMATION_MESSAGE);
                DialogDisplayer.getDefault().notify(d);
            }
        });       
    }
    
    private static void showError(final String msg){
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                NotifyDescriptor d = new NotifyDescriptor.Message(msg, NotifyDescriptor.ERROR_MESSAGE);
                DialogDisplayer.getDefault().notify(d);
            }
        });        
    }
}
