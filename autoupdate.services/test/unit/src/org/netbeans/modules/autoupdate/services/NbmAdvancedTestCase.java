/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.autoupdate.services;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.Arrays;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.autoupdate.updateprovider.AutoupdateCatalogProvider;

/**
 *
 * @author Jiri Rechtacek
 */
public class NbmAdvancedTestCase extends NbTestCase {
    
    protected NbmAdvancedTestCase (String name) {
        super (name);
    }
    
    @Override
    protected void setUp () throws IOException {
        clearWorkDir ();
        System.setProperty ("netbeans.user", getWorkDirPath ());
        File pf = new File (new File (getWorkDir(), "platform"), "installdir");
        pf.mkdirs ();
        new File (pf, "config").mkdir();
        System.setProperty ("netbeans.home", pf.toString ());
    }
    
    public static String generateModuleElement (String codeName, String version, String requires, boolean kit, boolean eager, String... deps) {
        String res = "<module codenamebase=\"" + codeName + "\" " +
                "homepage=\"http://au.netbeans.org/\" distribution=\"nbresloc:/org/netbeans/api/autoupdate/data/" + dot2dash (codeName) + ".nbm\" " +
                "license=\"standard-nbm-license.txt\" downloadsize=\"98765\" " +
                "needsrestart=\"false\" moduleauthor=\"\" " +
                "eager=\"" + eager + "\" " + 
                "releasedate=\"2006/02/23\">";
        res +=  "<manifest OpenIDE-Module=\"" + codeName + "\" " +
                (deps != null || deps.length == 0 ? "" : "OpenIDE-Module-Module-Dependencies=\"" + deps2ModuleModuleDependencies (deps) + "\" ") +
                "OpenIDE-Module-Name=\"" + codeName + "\" " +
                "AutoUpdate-Show-In-Client=\"" + kit + "\" " +
                (requires == null || requires.length () == 0 ? "" : "OpenIDE-Module-Requires=\"" + requires + "\" ") +
                "OpenIDE-Module-Specification-Version=\"" + version + "\"/>";
        res += "</module>";
        return res;
    }
    
    public AutoupdateCatalogProvider createUpdateProvider (String catalog) {
        AutoupdateCatalogProvider provider = null;
        try {
            provider = new MyProvider (catalog);
        } catch (IOException x) {
            fail (x.toString ());
        }
        return provider;
    }
    
    private static String dot2dash (String codeName) {
        return codeName.replace ('.', '-');
    }
    
    private static String deps2ModuleModuleDependencies (String... deps) {
        String res = "";
        for (String dep : Arrays.asList (deps)) {
            if (dep.indexOf (">") != -1) {
                dep = dep.replace (">", "&gt;");
            }
            res += res.length() == 0 ? dep : ", " + dep;
        }
        return res;
    }
    
    public static String generateCatalog (String... elements) {
        String res = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<!DOCTYPE module_updates PUBLIC \"-//NetBeans//DTD Autoupdate Catalog 2.5//EN\" \"http://www.netbeans.org/dtds/autoupdate-catalog-2_5.dtd\">" +
                "<module_updates timestamp=\"00/00/19/08/03/2006\">";
        for (String element : Arrays.asList (elements)) {
            res += element;
        }
        res += "</module_updates>";
        return res;
    }
    
    protected URL generateFile (String s) throws IOException {
        File res = new File (getWorkDir (), "test-catalog.xml");
        OutputStream os = new FileOutputStream (res);
        os.write (s.getBytes ());
        os.close ();
        return res.toURL ();
    }
    
    public class MyProvider extends AutoupdateCatalogProvider {
        public MyProvider (String s) throws IOException {
            super ("test-updates-with-os-provider", "test-updates-with-os-provider", generateFile (s));
        }
    }
    
}
