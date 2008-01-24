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
 * WebSvc.java
 *
 * Created on May 12, 2005, 3:45 PM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package org.netbeans.test.j2ee.lib;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.ejbjarproject.EjbJarProject;


/**
 *
 * @author jungi
 */
public final class WebSvc extends AbstractJ2eeFile {
    
    static final String SEI = "SEI";
    static final String BEAN_IMPL = "Bean";
    static final String IMPL = "Impl";
    static final String CONFIG = "-config.xml";
    static final String WS_XML = "webservices.xml";
    private String wsIntf;
    private String wsImpl;
    private String wsConfig;
    
    /** Creates a new instance of WebSvc */
    public WebSvc(String fqName, Project p) {
        super(fqName, p);
        wsIntf = name + SEI;
        wsImpl = name + ((isEjbMod) ? BEAN_IMPL : IMPL);
        wsConfig = name + CONFIG;
    }
    
    public WebSvc(String fqName, Project p, String srcRoot) {
        super(fqName, p, srcRoot);
        wsIntf = name + SEI;
        wsImpl = name + ((isEjbMod) ? BEAN_IMPL : IMPL);
        wsConfig = name + CONFIG;
    }
    
    private boolean implClassExists() {
        String res = pkgName.replace('.', File.separatorChar) + wsImpl + ".java";
        //System.err.println("name: " + name);
        //System.err.println("impl: " + res);
        return srcFileExist(res);
    }
    
    private boolean seiClassExists() {
        String res = pkgName.replace('.', File.separatorChar) + wsIntf + ".java";
        //System.err.println("intf: " + res);
        return srcFileExist(res);
    }
    
    private boolean configXmlExists() {
        String res = pkgName.replace('.', File.separatorChar) + wsConfig;
        //System.err.println("config: " + res);
        return srcFileExist(res);
    }
    
    private boolean websvcXmlExists() {
        //System.err.println("wsxml: " + WS_XML);
        return confFileExist(WS_XML);
    }

    public String[] checkExistingFiles() {
        List l = new ArrayList();
        if (!implClassExists()) {
            l.add(MESSAGE.replaceAll("\\$0", "WS impl class"));
        }
        if (!seiClassExists()) {
            l.add(MESSAGE.replaceAll("\\$0", "WS sei class"));
        }
        if (!configXmlExists()) {
            l.add(MESSAGE.replaceAll("\\$0", "WS config file"));
        }
        if (!websvcXmlExists()) {
            l.add(MESSAGE.replaceAll("\\$0", "Webservices.xml file"));
        }
        return (String[]) l.toArray(new String[l.size()]);
    }
}
