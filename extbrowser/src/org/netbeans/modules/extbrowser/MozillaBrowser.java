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

package org.netbeans.modules.extbrowser;

import java.util.logging.Level;
import org.openide.awt.HtmlBrowser;
import org.openide.execution.NbProcessDescriptor;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 * @author Martin Grebac
 */
public class MozillaBrowser extends ExtWebBrowser {

//    /** storage for starting browser timeout property */
//    protected int browserStartTimeout = 6000;

    private static final long serialVersionUID = -3982770681461437966L;

    /** Creates new ExtWebBrowser */
    public MozillaBrowser() {
        ddeServer = ExtWebBrowser.MOZILLA;
        //browserStartTimeout = 6000;
    }

    /** Determines whether the browser should be visible or not
     *  @return true when OS is Windows.
     *          false in all other cases.
     */
    public static Boolean isHidden () {
        String detectedPath = null;
        if (Utilities.isWindows()) {
            try {
                detectedPath = NbDdeBrowserImpl.getBrowserPath("MOZILLA");      // NOI18N
            } catch (NbBrowserException e) {
                ExtWebBrowser.getEM().log(Level.INFO, "Cannot detect Mozilla : " + e);      // NOI18N
            }
            if ((detectedPath != null) && (detectedPath.trim().length() > 0)) {
                return Boolean.FALSE;
            }
            return Boolean.TRUE;
        }
        return (Utilities.isUnix() && !Utilities.isMac()) ? Boolean.FALSE : Boolean.TRUE;
    }
    
    /** Getter for browser name
     *  @return name of browser
     */
    public String getName () {
        if (name == null) {
            this.name = NbBundle.getMessage(MozillaBrowser.class, "CTL_MozillaBrowserName");
        }
        return name;
    }
    
    /**
     * Returns a new instance of BrowserImpl implementation.
     * @throws UnsupportedOperationException when method is called and OS is not Windows.
     * @return browserImpl implementation of browser.
     */
    public HtmlBrowser.Impl createHtmlBrowserImpl() {
        ExtBrowserImpl impl = null;

        if (Utilities.isWindows()) {
            impl = new NbDdeBrowserImpl(this);
        } else if (Utilities.isUnix() && !Utilities.isMac()) {
            impl = new UnixBrowserImpl(this);
        } else {
            throw new UnsupportedOperationException (NbBundle.getMessage (MozillaBrowser.class, "MSG_CannotUseBrowser"));
        }
        
        return impl;
    }
    
    /** Default command for browser execution.
     * Can be overriden to return browser that suits to platform and settings.
     *
     * @return process descriptor that allows to start browser.
     */
    protected NbProcessDescriptor defaultBrowserExecutable () {

        String prg;
        String params = "";                                                     // NOI18N
        NbProcessDescriptor retValue;
        
        //Windows
        if (Utilities.isWindows()) {
            params += "{" + ExtWebBrowser.UnixBrowserFormat.TAG_URL + "}";
            try {
                prg = NbDdeBrowserImpl.getBrowserPath(getDDEServer());
                return new NbProcessDescriptor (prg, params);
            } catch (NbBrowserException e) {
                    prg = "C:\\Program Files\\Mozilla.org\\Mozilla\\mozilla.exe";     // NOI18N
            } catch (UnsatisfiedLinkError e) {
                prg = "iexplore";                                     // NOI18N
            }
        
            retValue = new NbProcessDescriptor (prg, params);
            return retValue;            
        
        //Unix
        } else { 
            
            prg = "mozilla";                                                      // NOI18N
            if (Utilities.getOperatingSystem() == Utilities.OS_LINUX) {
                java.io.File f = new java.io.File ("/usr/bin/mozilla"); // NOI18N
                if (f.exists()) {
                    prg = f.getAbsolutePath();
                }
                f = new java.io.File ("/usr/local/mozilla/mozilla"); // NOI18N
                if (f.exists()) {
                    prg = f.getAbsolutePath();
                }
            } else if (Utilities.getOperatingSystem() == Utilities.OS_SOLARIS) {
                java.io.File f = new java.io.File ("/usr/sfw/lib/mozilla/mozilla"); // NOI18N
                if (f.exists()) {
                    prg = f.getAbsolutePath();
                } else {
                    f = new java.io.File ("/opt/csw/bin/mozilla"); // NOI18N
                    if (f.exists()) {
                        prg = f.getAbsolutePath();
                    }
                }
            }
            retValue = new NbProcessDescriptor(
                prg,
                "-remote \"openURL({" + ExtWebBrowser.UnixBrowserFormat.TAG_URL + "})\"", // NOI18N
                NbBundle.getMessage(MozillaBrowser.class, "MSG_BrowserExecutorHint")
            );
        }
        return retValue;    
    }
    
}
