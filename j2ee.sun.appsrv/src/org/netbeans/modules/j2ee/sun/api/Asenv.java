// <editor-fold defaultstate="collapsed" desc=" License Header ">
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
// </editor-fold>

package org.netbeans.modules.j2ee.sun.api;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.Utilities;

/**
 * Parser for asenv.conf and asenv.bat
 */
public class Asenv {
    
    transient private final java.util.Properties props = new java.util.Properties();
    
    /**
     * key for path to jdk stored in asenv file
     */
    public static final String AS_JAVA = "AS_JAVA";
    /**
     * key for path to jdk stored in asenv file
     */
    public static final String AS_NS_BIN = "AS_NSS_BIN";
    /**
     * key for path to jdk stored in asenv file
     */
    public static final String AS_HADB = "AS_HADB";
    /**
     * key to path of default domains in asenv file
     */
    public static final String AS_DEF_DOMAINS_PATH = "AS_DEF_DOMAINS_PATH";
    
    /**
     * Creates a new instance of Asenv
     * @param platformRoot root of the platform
     */
    public Asenv(File platformRoot) {
        String ext = (Utilities.isWindows() ? "bat" : "conf");          // NOI18N
        File asenv = new File(platformRoot,"config/asenv."+ext);            // NOI18N
        FileReader fReader = null;
        BufferedReader bReader = null;
        try {
            fReader = new FileReader(asenv);
            bReader = new BufferedReader(fReader);
            
            String line = bReader.readLine();
            while (line != null) {
                StringTokenizer strtok = new StringTokenizer(line,"=");
                if (strtok.countTokens() == 2) {
                    String key = strtok.nextToken();
                    String val = strtok.nextToken();
                    if (key.startsWith("set ")) {
                        key = key.substring(3).trim();
                    }
                    if (val.startsWith("\"")) {
                        val = val.substring(1,val.length()-1);
                    }
                    props.put(key,val);
                }
                line = bReader.readLine(); 
            } 
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Asenv.class.getName()).log(Level.INFO,null,ex);
        } catch (IOException ex) {
            Logger.getLogger(Asenv.class.getName()).log(Level.INFO,null,ex);
        } finally {
            if (null != bReader) {
                try {
                    bReader.close();
                } catch (IOException ioe) {
                    Logger.getLogger(Asenv.class.getName()).log(Level.INFO,null,ioe);
                }
            }
            if (null != fReader) {
                try {
                    fReader.close();
                } catch (IOException ioe) {
                    Logger.getLogger(Asenv.class.getName()).log(Level.INFO,null,ioe);
                }
            }
        }
    }
    
    /**
     * Get values from asenv file
     * @param key variable defined in asenv
     * @return associated value    
     */
    public String get(final String key) {
        return (String) props.getProperty(key);
    }
    
}

