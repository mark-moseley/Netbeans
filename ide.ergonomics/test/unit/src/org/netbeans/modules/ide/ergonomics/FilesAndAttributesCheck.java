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
package org.netbeans.modules.ide.ergonomics;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.Repository;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class FilesAndAttributesCheck extends NbTestCase {

    public FilesAndAttributesCheck(String name) {
        super(name);
    }

    public void testGetAllTemplates() throws Exception {
        FileObject orig = Repository.getDefault().getDefaultFileSystem().findResource("Templates");

        Enumeration<? extends FileObject> allTemplates = orig.getChildren(true);
        StringBuilder errors = new StringBuilder();
        while (allTemplates.hasMoreElements()) {
            FileObject fo = allTemplates.nextElement();

            int cnt = 0;
            Enumeration<String> allAttributes = fo.getAttributes();
            while (allAttributes.hasMoreElements()) {
                String name = allAttributes.nextElement();
                Object attr = fo.getAttribute(name);
                if (attr == null) {
                    fail("fo: " + fo + " has null " + name + " attribute");
                }
                System.setProperty(dynAttr + fo.getPath() + "@" + name, attr.toString());
                cnt++;

                if (attr instanceof URL) {
                    URL u = (URL) attr;
                    int read = -1;
                    try {
                        read = u.openStream().read(new byte[4096]);
                    } catch (IOException ex) {
                        errors.append(ex.getMessage()).append('\n');
                    }
                    if (read <= 0) {
                        errors.append("Resource shall exist: " + fo + " attr: " + name + " value: " + attr + "\n");
                    }
                }
            }
            System.setProperty(dynVery + fo.getPath(), String.valueOf(cnt));
        }

        if (errors.length() > 0) {
            fail(errors.toString());
        }
    }
    private static final String dynVery = "dynamic/verify/";
    private static final String dynAttr = "dynamic/attr/";

    public void testCheckAllTemplatesPretest() throws Exception {
        testCheckAllTemplatesReal();
    }
    public void testCheckAllTemplatesReal() throws Exception {
        FileSystem mfs = Repository.getDefault().getDefaultFileSystem();

        Map<String, List<String>> filesAndAttribs = new TreeMap<String, List<String>>();
        for (Object o : System.getProperties().keySet()) {
            String f = (String)o;
            if (f.startsWith(dynVery)) {
                int cnt = Integer.parseInt(System.getProperties().getProperty(f));
                f = f.substring(dynVery.length());
                ArrayList<String> arr = new ArrayList<String>();
                String pref = dynAttr + f + "@";
                for (Map.Entry<Object, Object> entry : System.getProperties().entrySet()) {
                    if (entry.getKey() instanceof String) {
                        String a = (String) entry.getKey();
                        if (a.startsWith(pref)) {
                            a = a.substring(pref.length());
                            arr.add(a);
                        }
                    }
                }
                if (arr.size() != cnt) {
                    fail("There shall be " + cnt + " attributes for " + f);
                }
                filesAndAttribs.put(f, arr);
            }
        }
        if (filesAndAttribs.size() == 0) {
            fail("Some properties shall be set: " + System.getProperties());
        }

        Iterator<? extends String> allTemplates = filesAndAttribs.keySet().iterator();
        StringBuilder errors = new StringBuilder();
        while (allTemplates.hasNext()) {
            String fo = allTemplates.next();
            FileObject clone = mfs.findResource(fo);

            if (clone == null) {
                errors.append("Both files exist: " + fo + "\n");
                continue;
            }

            Enumeration<String> allAttributes = Collections.enumeration(filesAndAttribs.get(fo));
            while (allAttributes.hasMoreElements()) {
                String name = allAttributes.nextElement();
                Object attr = clone.getAttribute(name);
                if (attr == null && "templateWizardIterator".equals(name)) {
                    attr = clone.getAttribute("instantiatingIterator");
                }

                if (attr == null) {
                    errors.append("Attribute " + name + " present in orig on " + fo + " but null in clone\n");
                }

                if (attr instanceof URL) {
                    URL u = (URL) attr;
                    int read = -1;
                    try {
                        read = u.openStream().read(new byte[4096]);
                    } catch (IOException ex) {
                        errors.append(ex.getMessage()).append('\n');
                    }
                    if (read <= 0) {
                        errors.append("Resource shall exist: " + fo + " attr: " + name + " value: " + attr + "\n");
                    }
                }
            }
            allTemplates.remove();
        }

        if (errors.length() > 0) {
            fail(errors.toString());
        }

        if (!filesAndAttribs.isEmpty()) {
            fail("All should be empty: " + filesAndAttribs);
        }
    }
}
