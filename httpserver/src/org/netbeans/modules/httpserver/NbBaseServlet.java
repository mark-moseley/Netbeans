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

package org.netbeans.modules.httpserver;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Set;
import javax.servlet.*;
import javax.servlet.http.*;
import org.openide.util.Exceptions;

import org.openide.util.NbBundle;

/** Base servlet for servlets which access NetBeans Open APIs
*
* @author Petr Jiricka
* @version 0.11 May 5, 1999
*/
public abstract class NbBaseServlet extends HttpServlet {

    /** Initializes the servlet. */
    public void init() throws ServletException {
    }

    /** Processes the request for both HTTP GET and POST methods
    * @param request servlet request
    * @param response servlet response
    */
    protected abstract void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, java.io.IOException;

    /** Performs the HTTP GET operation.
    * @param request servlet request
    * @param response servlet response
    */
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, java.io.IOException {
        processRequest(request, response);
    }

    /** Performs the HTTP POST operation.
    * @param request servlet request
    * @param response servlet response
    */
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, java.io.IOException {
        processRequest(request, response);
    }

    /**
    * Returns a short description of the servlet.
    */
    public String getServletInfo() {
        return NbBundle.getBundle(NbBaseServlet.class).getString("MSG_BaseServletDescr");
    }

    /** Checks whether access should be permitted according to HTTP Server module access settings
    * (localhost/anyhost, granted addesses)
    *  @return true if access is granted
    */
    protected boolean checkAccess(HttpServletRequest request) throws IOException {

        HttpServerSettings settings = HttpServerSettings.getDefault();
        if (settings == null)
            return false;

        if (settings.getHostProperty ().getHost ().equals(HttpServerSettings.ANYHOST))
            return true;

        Set hs = settings.getGrantedAddressesSet();

        if (hs.contains(request.getRemoteAddr().trim()))
            return true;

        String pathI = request.getPathInfo();
        if (pathI == null)
            pathI = "";      // NOI18N
        // ask user
        try {
            String address = request.getRemoteAddr().trim();
            if (settings.allowAccess(InetAddress.getByName(address), pathI)) return true;
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
            return false;
        }

        return false;
    }

}
