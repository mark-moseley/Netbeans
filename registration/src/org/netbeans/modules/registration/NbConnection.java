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

package org.netbeans.modules.registration;

import com.sun.servicetag.RegistrationData;
import com.sun.servicetag.ServiceTag;
import java.awt.Frame;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.netbeans.modules.reglib.BrowserSupport;
import org.netbeans.modules.reglib.NbConnectionSupport;
import org.netbeans.modules.reglib.NbServiceTagSupport;
import org.netbeans.modules.reglib.StatusData;
import org.openide.awt.HtmlBrowser;
import org.openide.util.SharedClassObject;
import org.openide.windows.WindowManager;

/**
 * NetBeans Connection Class for Product Registration.
 *
 * Registration Web Application Interface
 * 1) POST the product registry to the output stream of the registration
 *    relay service.
 * 2) Open the webapp URL from a browser with the following parameters:
 *    registry-urn
 *    product=jdk
 *    locale=<locale-lang>
 *
 * @see https://sn-tools.central.sun.com/twiki/pub/ServiceTags/RegistrationRelayService/
 * 
 */
class NbConnection {
    
    private static final Logger LOG = Logger.getLogger("org.netbeans.modules.registration.NbConnection"); // NOI18N
    
    private static final String STATUS_FILE = "status.xml";
    
    private static StatusData status = new StatusData(StatusData.STATUS_UNKNOWN,StatusData.DEFAULT_DELAY);
    
    private NbConnection() {
    }
    
    static void init () {
        //As we need this code for NB 5.5 we cannot use new Winsys API method
        //WindowManager.invokeWhenUIReady(Runnable). Here we use old way how to
        //perform something after opening of main window
        if ((System.getProperty ("netbeans.full.hack") == null) && (System.getProperty ("netbeans.close") == null)) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    final Frame mainWindow = WindowManager.getDefault().getMainWindow();
                    mainWindow.addComponentListener(new ComponentAdapter() {
                        @Override
                        public void componentShown(ComponentEvent evt) {
                            mainWindow.removeComponentListener(this);
                            SwingUtilities.invokeLater(new Runnable() {
                                public void run() {
                                    checkStatus();
                                }
                            });
                        }
                    });
                }
            });
        }
    }
    
    private static void checkStatus () {
        LOG.log(Level.FINE,"Check registration status");
        File dir = NbServiceTagSupport.getServiceTagDirHome();
        File statusFile = new File(dir,STATUS_FILE);
        if (statusFile.exists()) {
            LOG.log(Level.FINE,"Load registration status from:" + statusFile);
            //Status file exists, check its content
            BufferedInputStream in = null;
            try {
                in = new BufferedInputStream(new FileInputStream(statusFile));
                status = StatusData.loadFromXML(in);
            } catch (IOException ex) {
                LOG.log(Level.INFO,"Error: Bad registration data \"" +
                statusFile + "\"",ex);
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException ex) {
                        LOG.log(Level.INFO,"Error: Cannot close reader",ex);
                    }
                }
            }
            if (status.getStatus().equals(StatusData.STATUS_REGISTERED)) {
                LOG.log(Level.FINE,"Status is STATUS_REGISTERED");
                checkProductRegistrationStatus();
            } else if (status.getStatus().equals(StatusData.STATUS_NEVER)) {
                LOG.log(Level.FINE,"Status is STATUS_NEVER");
            } else if (status.getStatus().equals(StatusData.STATUS_LATER)) {
                LOG.log(Level.FINE,"Status is STATUS_LATER");
                //Check current date
                //Date ts+delay
                Date next = new Date(status.getTimestamp().getTime() + (status.getDelay() * 24L * 60L * 60L * 1000L));
                LOG.log(Level.FINE,"      Date timestamp:" + status.getTimestamp());
                LOG.log(Level.FINE,"Date timestamp+delay:" + next);
                Date now = new Date();
                LOG.log(Level.FINE,"            Date now:" + now);
                if (now.after(next)) {
                    //Time is over, ask again
                    status.setDelay(StatusData.DEFAULT_DELAY);
                    storeStatus();
                    RegisterAction a = SharedClassObject.findObject(RegisterAction.class, true);
                    a.showDialog();
                }
            } else {
                LOG.log(Level.FINE,"Status is unknown");
                //Status is unknown, ask user
                RegisterAction a = SharedClassObject.findObject(RegisterAction.class, true);
                a.showDialog();
            }
        } else {
            //Status file does not exist so directly show dialog
            RegisterAction a = SharedClassObject.findObject(RegisterAction.class, true);
            a.showDialog();
        }
    }
    
    /**
     * Query web service if all products are registered. If there is confirmation that any product is
     * not registered show Reminder dialog.
     */
    private static void checkProductRegistrationStatus () {
        RegistrationData regData = null;
        try {
            regData = NbServiceTagSupport.getRegistrationData();
        } catch (IOException exc) {
            LOG.log(Level.INFO,"Error: Cannot get registration data",exc);
            return;
        }
        Collection<ServiceTag> svcTags = regData.getServiceTags();
        boolean registered = true;
        for (ServiceTag st : svcTags) {
            registered = NbConnectionSupport.isRegistered(NbConnectionSupport.getRegistrationQueryHost(), st.getInstanceURN());
            if (!registered) {
                //If one service tag is not registered show reminder dialog
                RegisterAction a = SharedClassObject.findObject(RegisterAction.class, true);
                a.showDialog();
                return;
            }
        }
    }
    
    /** This method updates registration status. It saves user selection.
     * If user selects Register registration is started
     * @param value User choice in reminder dialog
     */
    static void updateStatus (String value, String product) {
        LOG.log(Level.FINE,"updateStatus status:" + value);
        //Ignore null value ie. do not change status if null is passed
        if (value != null) {
            status.setStatus(value);
        }
        //Set time stamp to time when we showed reminder dialog.
        status.setTimestamp(new Date());
        
        storeStatus();
        
        if (StatusData.STATUS_REGISTERED.equals(status.getStatus())) {
            try {
                NbConnection.register(NbServiceTagSupport.getRegistrationData(), product);
            } catch (IOException ex) {
                LOG.log(Level.INFO,
                "Error: Cannot register product", ex);
            }
        }
    }
    
    static void storeStatus () {
        File dir = NbServiceTagSupport.getServiceTagDirHome();
        File statusFile = new File(dir,STATUS_FILE);
        
        LOG.log(Level.FINE,"Store registration status to: " + statusFile);
        BufferedOutputStream out = null;
        try {
            out = new BufferedOutputStream(new FileOutputStream(statusFile));
            status.storeToXML(out);
        } catch (IOException ex) {
            LOG.log(Level.INFO,
            "Error: Cannot save status data to \"" + statusFile, ex);
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException ex) {
                    LOG.log(Level.INFO,
                    "Error: Cannot close writer", ex);
                }
            }
        }
    }
    
    /**
     * Registers all products in the given product registry.  If it fails
     * to post the service tag registry, open the browser with the offline 
     * registration page.
     * 
     * @param regData registration data to be posted to the Sun Connection
     *             for registration.
     *
     * @throws IOException if I/O error occurs in this operation
     */
    static void register(RegistrationData regData, String product) throws IOException {
        // Gets the URL for SunConnection registration relay service
        LOG.log(Level.FINE,"Product registration");
        URL url = NbConnectionSupport.getRegistrationURL(regData.getRegistrationURN(), product);

        // Post the Product Registry to Sun Connection
        LOG.log(Level.FINE,"POST registration data to:" + url);
        boolean succeed = NbConnectionSupport.postRegistrationData(url, regData);
        if (succeed) {
            // service tags posted successfully
            // now prompt for registration
            LOG.log(Level.FINE,"Open browser with:" + url);
            openBrowser(url);
        } else {
            // open browser with the offline registration page
            openOfflineRegisterPage(product);
        }
    }

    /**
     * Opens a browser for JDK product registration.
     * @param url Registration Webapp URL
     */
    private static void openBrowser(URL url) throws IOException {
        if (BrowserSupport.isSupported()) {
            try {
                BrowserSupport.browse(url.toURI());
            } catch (URISyntaxException ex) {
                InternalError x = new InternalError("Error in registering: " + ex.getMessage());
                x.initCause(ex);
                throw x;
            } catch (IllegalArgumentException ex) {
                LOG.log(Level.FINE,"Cannot open browser:",ex);
            } catch (UnsupportedOperationException ex) {
                // ignore if not supported
                LOG.log(Level.FINE,"Cannot open browser:",ex);
            }
        } else {
            //Fallback to openide API in JDK 5
            HtmlBrowser.URLDisplayer.getDefault().showURL(url);
        }
    }
    
    /**
     * Opens the offline registratioin page in the browser.
     * 
     */
    private static void openOfflineRegisterPage (String product)
            throws IOException {
        File registerPage = NbServiceTagSupport.getRegistrationHtmlPage(product);
        if (BrowserSupport.isSupported()) {
            try {
                BrowserSupport.browse(registerPage.toURI());
            } catch (FileNotFoundException ex) {
                // should never reach here
                InternalError x = 
                    new InternalError("Error in launching " + registerPage + ": " + ex.getMessage());
                x.initCause(ex);
                throw x;
            } catch (IllegalArgumentException ex) {
                LOG.log(Level.FINE,"Cannot open browser:",ex);
            } catch (UnsupportedOperationException ex) {
                // ignore if not supported
                LOG.log(Level.FINE,"Cannot open browser:",ex);
            }
        } else {
            //Fallback to openide API in JDK 5
            HtmlBrowser.URLDisplayer.getDefault().showURL(registerPage.toURI().toURL());
        }
    }
    
}
