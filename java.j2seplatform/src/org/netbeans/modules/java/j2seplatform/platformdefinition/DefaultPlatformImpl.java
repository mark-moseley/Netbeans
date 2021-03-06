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

package org.netbeans.modules.java.j2seplatform.platformdefinition;

import java.io.*;
import java.net.URL;
import java.util.*;
import java.net.MalformedURLException;
import org.openide.util.Exceptions;

import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.filesystems.FileUtil;
import org.netbeans.api.java.platform.*;
import org.netbeans.api.java.classpath.*;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.URLMapper;

/**
 * Implementation of the "Default" platform. The information here is extracted
 * from the NetBeans' own runtime.
 *
 * @author Svata Dedic
 */
public class DefaultPlatformImpl extends J2SEPlatformImpl {


    public static final String DEFAULT_PLATFORM_ANT_NAME = "default_platform";           //NOI18N

    private ClassPath standardLibs;
    
    static JavaPlatform create(Map properties, List sources, List javadoc) {
        if (properties == null) {
            properties = new HashMap ();
        }
        // XXX java.home??
        File javaHome = FileUtil.normalizeFile(new File(System.getProperty("jdk.home")));       //NOI18N
        List installFolders = new ArrayList ();
        try {
            installFolders.add (javaHome.toURI().toURL());
        } catch (MalformedURLException mue) {
            Exceptions.printStackTrace(mue);
        }
        if (sources == null) {
            sources = getSources (javaHome);
        }
        if (javadoc == null) {
            javadoc = getJavadoc (javaHome);
        }
        return new DefaultPlatformImpl(installFolders, properties, new HashMap(System.getProperties()), sources,javadoc);
    }
    
    private DefaultPlatformImpl(List installFolders, Map platformProperties, Map systemProperties, List sources, List javadoc) {
        super(null,DEFAULT_PLATFORM_ANT_NAME,
              installFolders, platformProperties, systemProperties, sources, javadoc);
    }

    public void setAntName(String antName) {
        throw new UnsupportedOperationException (); //Default platform ant name can not be changed
    }
    
    public String getDisplayName () {
        String displayName = super.getDisplayName();
        if (displayName == null) {
            displayName = NbBundle.getMessage(DefaultPlatformImpl.class,"TXT_DefaultPlatform", getSpecification().getVersion().toString());
            this.internalSetDisplayName (displayName);
        }
        return displayName;
    }
    
    public void setDisplayName(String name) {
        throw new UnsupportedOperationException (); //Default platform name can not be changed
    }

    public ClassPath getStandardLibraries() {
        if (standardLibs != null)
            return standardLibs;
        String s = System.getProperty(SYSPROP_JAVA_CLASS_PATH);       //NOI18N
        if (s == null) {
            s = ""; // NOI18N
        }
        return standardLibs = Util.createClassPath (s);
    }

    static List getSources (File javaHome) {
        if (javaHome != null) {
            try {
                File f;
                //On VMS, the root of the "src.zip" is "src", and this causes
                //problems with NetBeans 4.0. So use the modified "src.zip" shipped 
                //with the OpenVMS NetBeans 4.0 kit.
                if (Utilities.getOperatingSystem() == Utilities.OS_VMS) {
                    String srcHome = 
                        System.getProperty("netbeans.openvms.j2seplatform.default.srcdir");
                    if (srcHome != null)
                        f = new File(srcHome, "src.zip");
                    else
                        f = new File (javaHome, "src.zip");
                } else {
                    f = new File (javaHome, "src.zip");    //NOI18N
                    //If src.zip does not exist, try src.jar (it is on some platforms)
                    if (!f.exists()) {
                        f = new File (javaHome, "src.jar");    //NOI18N
                    }
                }
                if (f.exists() && f.canRead()) {
                    URL url = FileUtil.getArchiveRoot(f.toURI().toURL());
                    
                     //Test for src folder in the src.zip on Mac
                    if (Utilities.getOperatingSystem() == Utilities.OS_MAC) {
                         try {
                             FileObject fo = URLMapper.findFileObject(url);
                             if (fo != null) {
                                 fo = fo.getFileObject("src");    //NOI18N
                                 if (fo != null) {
                                     url = fo.getURL();
                                 }
                             }                             
                         } catch (FileStateInvalidException fileStateInvalidException) {
                             Exceptions.printStackTrace(fileStateInvalidException);
                         }
                    }
                    return Collections.singletonList (url);
                }
            } catch (MalformedURLException e) {
                Exceptions.printStackTrace(e);
            }              
        }
        return null;
    }
    
    
    static List getJavadoc (File javaHome) {
        if (javaHome != null ) {
            File f = new File (javaHome,"docs"); //NOI18N
            if (f.isDirectory() && f.canRead()) {
                try {
                    return Collections.singletonList(f.toURI().toURL());
                } catch (MalformedURLException mue) {
                    Exceptions.printStackTrace(mue);
                }
            }                        
        }
        return null;
    }

}