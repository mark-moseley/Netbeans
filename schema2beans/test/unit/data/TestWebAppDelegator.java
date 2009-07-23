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

/*
 *	TestWebAppDelegator - test the basic features.
 *
 *	The following test assumes that we know the content of the
 *	graph as we get elements, add and change them. Therefore, the TestWebAppDelegator.xml
 *	file and this java test should be kept in sync.
 *
 * 	Test the following:
 *
 *	single String: get/set/remove/set/get
 *	boolean (from true): get/set true/get/set false/get/set true/get
 *	boolean (from false): get/set false/get/set true/get/set false/get
 *	String[]: get/set (null & !null)/add/remove
 *	Bean: remove/set(null)/create bean and graph of beans/set/add
 *
 */

import java.io.*;
import java.util.*;
import org.w3c.dom.*;

import webapp.*;


public class TestWebAppDelegator extends BaseTest {
    public static void main(String[] argv) {
        TestWebAppDelegator o = new TestWebAppDelegator();
        if (argv.length > 0)
            o.setDocumentDir(argv[0]);
        try {
            o.run();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        System.exit(0);
    }
    
    public void run() throws Exception {
        WebAppDelegator webApp;

        this.readDocument();

        out("creating the bean graph");
        webApp = WebAppDelegator.read(doc);

        //	Check that we can read the graph an it is complete
        out("bean graph created");
        webApp.write(out);

        out("making some minor changes");
        webApp.setDescription(0, "Changed the description");
        webApp.getFilter(0).setFilterClass("foo");
        webApp.write(out);

        out("set some goodnesses");
        SecurityConstraintType sc = new SecurityConstraintType();
        webApp.addSecurityConstraint(sc);
        sc.setAuthConstraint(new AuthConstraintType());
        sc.getAuthConstraint().addGoodPresidentCandidate(true);
        sc.getAuthConstraint().addGoodPresidentCandidate(false);
        boolean firstCandiateGoodness = sc.getAuthConstraint().isGoodPresidentCandidate(0);
        check(firstCandiateGoodness == true, "firstCandiateGoodness is good");
        boolean[] allCandidateGoodnesses = sc.getAuthConstraint().getGoodPresidentCandidate();
        check(allCandidateGoodnesses.length == 2, "2 candidates goodness");
        check(allCandidateGoodnesses[0] == true, "candidate 0 is good");
        check(allCandidateGoodnesses[1] == false, "candidate 1 is not good");
        webApp.write(out);

        try {
            webApp.validate();
            check(false, "Failed to get validate exception");
        } catch (webapp.WebApp.ValidateException e) {
            check(true, "Got good validate exception: "+e.getMessage());
        }
        webApp.setVersion("2.4");
        try {
            webApp.validate();
            check(false, "Failed to get validate exception");
        } catch (webapp.WebApp.ValidateException e) {
            check(true, "Got good validate exception: "+e.getMessage());
        }
        sc.addWebResourceCollection(new WebResourceCollectionType());
        try {
            webApp.validate();
            check(false, "Failed to get validate exception");
        } catch (webapp.WebApp.ValidateException e) {
            check(true, "Got good validate exception: "+e.getMessage());
        }
        sc.getWebResourceCollection(0).setWebResourceName("blue");
        try {
            webApp.validate();
            check(false, "Failed to get validate exception");
        } catch (webapp.WebApp.ValidateException e) {
            check(true, "Got good validate exception: "+e.getMessage());
        }
        sc.getWebResourceCollection(0).addUrlPattern("*.html");
        ErrorPageType errorPage = new ErrorPageType("over there");
        webApp.addErrorPage(errorPage);
        try {
            webApp.validate();
            check(false, "Failed to get validate exception");
        } catch (webapp.WebApp.ValidateException e) {
            check(true, "Got good validate exception: "+e.getMessage());
        }
        errorPage.setErrorCode(new Long(3l));
        webApp.write(out);
        webApp.validate();

        FilterMappingType fm = new FilterMappingType();
        webApp.addFilterMapping(fm);
        fm.setServletName("Bob");
        fm.addDispatcher("I am a dispatcher.");
        try {
            webApp.validate();
            check(false, "Failed to get validate exception");
        } catch (webapp.WebApp.ValidateException e) {
            check(true, "Got good validate exception: "+e.getMessage());
        }
        fm.setDispatcher(0, "FORWARD");
        webApp.write(out);
        webApp.validate();

        check(webApp._hasChanged(), "Some changes were made correctly.");
        webApp._setChanged(false);
        check(!webApp._hasChanged(), "Was able to reset the dirty flag.");
        fm.setServletName("Robert");
        check(webApp._hasChanged(), "Some changes were made correctly.");
    }
}
