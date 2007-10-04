/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU General
 * Public License Version 2 only ("GPL") or the Common Development and Distribution
 * License("CDDL") (collectively, the "License"). You may not use this file except in
 * compliance with the License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html or nbbuild/licenses/CDDL-GPL-2-CP. See the
 * License for the specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header Notice in
 * each file and include the License file at nbbuild/licenses/CDDL-GPL-2-CP.  Sun
 * designates this particular file as subject to the "Classpath" exception as
 * provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the License Header,
 * with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original Software
 * is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun Microsystems, Inc. All
 * Rights Reserved.
 * 
 * If you wish your version of this file to be governed by only the CDDL or only the
 * GPL Version 2, indicate your decision by adding "[Contributor] elects to include
 * this software in this distribution under the [CDDL or GPL Version 2] license." If
 * you do not indicate a single choice of license, a recipient has the option to
 * distribute your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above. However, if
 * you add GPL Version 2 code and therefore, elected the GPL Version 2 license, then
 * the option applies only if the new code is made subject to such option by the
 * copyright holder.
 */

package org.netbeans.installer.infra.build.ant;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.netbeans.installer.infra.build.ant.utils.Utils;

/**
 * This class is an ant task which parses a given input file and devises the
 * netbeans c/c++ pack build number from it.
 *
 * @author Kirill Sorokin
 */
public class BuildNumberNbUml extends Task {
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    /**
     * The input file.
     */
    private File file;
    
    /**
     * The properties' names prefix.
     */
    private String prefix;
    
    // setters //////////////////////////////////////////////////////////////////////
    /**
     * Setter for the <code>file</code> attribute.
     *
     * @param path The value of the <code>file</code> attribute.
     */
    public void setFile(String path) {
        this.file = new File(path);
    }
    
    /**
     * Setter for the <code>prefix</code> attribute.
     *
     * @param prefix The value of the <code>prefix</code> attribute.
     */
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }
    
    // execution ////////////////////////////////////////////////////////////////////
    /**
     * Executes the task. The input file is parsed and three properties identifying
     * the netbeans c/c++ pack build are set.
     *
     * @throws org.apache.tools.ant.BuildException if the input file cannot be
     *      parsed for whatever reason.
     */
    @Override
    public void execute() throws BuildException {
        try {
            final FileInputStream in = new FileInputStream(file);
            final CharSequence contents = Utils.read(in);
            
            in.close();
            
            final Matcher matcher = PATTERN.matcher(contents);
            
            if (matcher.find()) {
                String buildNumber = matcher.group(1);                      // NOMAGI
                
                getProject().setProperty(
                        prefix + BUILD_NUMBER_REAL_SUFFIX,
                        buildNumber);
                
                if (buildNumber.indexOf('_') == -1) {
                    getProject().setProperty(
                            prefix + BUILD_NUMBER_SUFFIX,
                            "20" + buildNumber + "00"); // NOI18N
                } else {
                    final int index = buildNumber.indexOf('_');             // NOMAGI
                    
                    final String bnPrefix = 
                            buildNumber.substring(0, index);                // NOMAGI
                    final String bnSuffix = 
                            buildNumber.substring(index + 1);               // NOMAGI
                    
                    buildNumber = 
                            bnPrefix + 
                            (bnSuffix.length() < 2 ? "0" : "") + // NOI18N NOMAGI
                            bnSuffix;
                    
                    getProject().setProperty(
                            prefix + BUILD_NUMBER_SUFFIX,
                            "20" + buildNumber); // NOI18N NOMAGI
                }
            } else {
                throw new BuildException(
                        "Cannot find build number"); // NOI18N
            }
        } catch (IOException e) {
            throw new BuildException(e);
        }
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    /**
     * Pattern for which to look in the input file.
     */
    private static final Pattern PATTERN = Pattern.compile(
            "uml_cluster-hydra-([0-9_]+)\\.zip"); // NOI18N
    
    /**
     * Build number property suffix.
     */
    private static final String BUILD_NUMBER_SUFFIX =
            ".build.number"; // NOI18N
    
    /**
     * Real build number property suffix.
     */
    private static final String BUILD_NUMBER_REAL_SUFFIX =
            ".build.number.real"; // NOI18N
}
