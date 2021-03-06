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
import java.io.IOException;
import java.util.Date;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.netbeans.installer.infra.build.ant.utils.Utils;

/**
 * This class is an ant task which creates a product package descriptor based on the
 * existing project properties and writes it to the specified file.
 *
 * @author Kirill Sorokin
 */
public class ProductDescriptor extends Task {
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    /**
     * File to which the product descriptor should be written.
     */
    private File file;
    
    // setters //////////////////////////////////////////////////////////////////////
    /**
     * Setter for the 'file' property.
     *
     * @param path The new value of the 'file' property.
     */
    public void setFile(final String path) {
        file = new File(path);
        if (!file.equals(file.getAbsoluteFile())) {
            file = new File(getProject().getBaseDir(), path);
        }
    }
    
    // execution ////////////////////////////////////////////////////////////////////
    /**
     * Executes the task. This method writes the product package descriptor xml code
     * to the specified file.
     *
     * @throws org.apache.tools.ant.BuildException if a I/O error occurs.
     */
    public void execute() throws BuildException {
        Utils.setProject(getProject());
        
        StringBuilder xml = new StringBuilder();
        
        // header ///////////////////////////////////////////////////////////////////
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"); // NOI18N
        xml.append("<registry " + // NOI18N
                "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +// NOI18N
                "xsi:noNamespaceSchemaLocation=\"registry.xsd\">\n"); // NOI18N
        xml.append("    <components>\n"); // NOI18N
        
        // core data ////////////////////////////////////////////////////////////////
        String uid = get("product.uid"); // NOI18N
        final String version = get("product.version"); // NOI18N
        final String platform = get("product.platforms"); // NOI18N
        final String status = get("product.status"); // NOI18N
        final String offset = get("product.offset"); // NOI18N
        final String expand = get("product.expand"); // NOI18N
        final String visible = get("product.visible"); // NOI18N
        final String features = get("product.features"); // NOI18N
        
        xml.append("        <product uid=\"" + uid + "\" " + // NOI18N
                "version=\"" + version + "\" " + // NOI18N
                "platforms=\"" + platform + "\" " + // NOI18N
                "status=\"" + status + "\" " + // NOI18N
                "offset=\"" + offset + "\" " + // NOI18N
                "expand=\"" + expand + "\" " + // NOI18N
                "built=\"" + new Date().getTime() + "\" " + // NOI18N
                "visible=\"" + visible + "\" " + // NOI18N
                "features=\"" + features + "\">\n"); // NOI18N
        
        // locales //////////////////////////////////////////////////////////////////
        final String locales = get("product.locales.list").trim(); // NOI18N
        
        // display name /////////////////////////////////////////////////////////////
        xml.append("            <display-name>\n"); // NOI18N
        xml.append("                <default><![CDATA[" + // NOI18N
                get("product.display.name.default") + "]]></default>\n"); // NOI18N
        if (!locales.equals("")) { // NOI18N
            for (String locale: locales.split(" ")) { // NOI18N
                xml.append("                <localized locale=\"" + // NOI18N
                        locale + "\"><![CDATA[" + // NOI18N
                        get("product.display.name." + locale) + // NOI18N
                        "]]></localized>\n"); // NOI18N
            }
        }
        xml.append("            </display-name>\n"); // NOI18N
        
        // description //////////////////////////////////////////////////////////////
        xml.append("            <description>\n"); // NOI18N
        xml.append("                <default><![CDATA[" + // NOI18N
                get("product.description.default") + "]]></default>\n"); // NOI18N
        if (!locales.equals("")) { // NOI18N
            for (String locale: locales.split(" ")) { // NOI18N
                xml.append("                <localized locale=\"" + // NOI18N
                        locale + "\"><![CDATA[" + // NOI18N
                        get("product.description." + locale) + // NOI18N
                        "]]></localized>\n"); // NOI18N
            }
        }
        xml.append("            </description>\n"); // NOI18N
        
        // icon /////////////////////////////////////////////////////////////////////
        String size = get("product.icon.size"); // NOI18N
        String md5 = get("product.icon.md5"); // NOI18N
        String uri = get("product.icon.correct.uri"); // NOI18N
        
        xml.append("            <icon " + // NOI18N
                "size=\"" + size + "\" " + // NOI18N
                "md5=\"" + md5 + "\">\n"); // NOI18N
        xml.append("                <default-uri>" + // NOI18N
                uri.replace(" ", "%20") + "</default-uri>\n"); // NOI18N
        xml.append("            </icon>\n"); // NOI18N
        
        // properties ///////////////////////////////////////////////////////////////
        if (getInt("product.properties.length") > 0) { // NOI18N
            xml.append("            <properties>\n"); // NOI18N
            for (int i = 1; i <=
                    getInt("product.properties.length"); i++) { // NOI18N
                String name = get("product.properties." + i + ".name"); // NOI18N
                String value = get("product.properties." + i + ".value"); // NOI18N
                xml.append("                <property name=\"" + name + // NOI18N
                        "\"><![CDATA[" + value + "]]></property>\n"); // NOI18N
            }
            xml.append("            </properties>\n"); // NOI18N
        }
        
        // configuration logic //////////////////////////////////////////////////////
        xml.append("            <configuration-logic>\n"); // NOI18N
        for (int i = 1; i <= getInt("product.logic.length"); i++) { // NOI18N
            size  = get("product.logic." + i + ".size"); // NOI18N
            md5   = get("product.logic." + i + ".md5"); // NOI18N
            uri   = get("product.logic." + i + ".correct.uri"); // NOI18N
            
            xml.append("                <file " + // NOI18N
                    "size=\"" + size + "\" " + // NOI18N
                    "md5=\"" + md5 + "\">\n"); // NOI18N
            
            xml.append(
                    "                    <default-uri>" + // NOI18N
                    uri.replace(" ", "%20") + // NOI18N
                    "</default-uri>\n"); // NOI18N
            xml.append("                </file>\n"); // NOI18N
        }
        xml.append("            </configuration-logic>\n"); // NOI18N
        
        // installation data ////////////////////////////////////////////////////////
        xml.append("            <installation-data>\n"); // NOI18N
        for (int i = 1; i <= getInt("product.data.length"); i++) { // NOI18N
            size  = get("product.data." + i + ".size"); // NOI18N
            md5   = get("product.data." + i + ".md5"); // NOI18N
            uri   = get("product.data." + i + ".correct.uri"); // NOI18N
            
            xml.append("                <file " + // NOI18N
                    "size=\"" + size + "\" " + // NOI18N
                    "md5=\"" + md5 + "\">\n"); // NOI18N
            xml.append(
                    "                    <default-uri>" + // NOI18N
                    uri.replace(" ", "%20") + // NOI18N
                    "</default-uri>\n"); // NOI18N
            xml.append("                </file>\n"); // NOI18N
        }
        xml.append("            </installation-data>\n"); // NOI18N
        
        // requirements /////////////////////////////////////////////////////////////
        xml.append("            <system-requirements>\n"); // NOI18N
        xml.append("                <disk-space>" + // NOI18N
                get("product.disk.space") + "</disk-space>\n"); // NOI18N
        xml.append("            </system-requirements>\n"); // NOI18N
        
        // dependencies /////////////////////////////////////////////////////////////
        if (getInt("product.requirements.length") + // NOI18N
                getInt("product.conflicts.length") + // NOI18N
                getInt("product.install-afters.length") > 0) { // NOI18N
            xml.append("            <dependencies>\n"); // NOI18N
            
            for (int i = 1; i <=
                    getInt("product.requirements.length"); i++) { // NOI18N
                uid = get("product.requirements." + i + ".uid"); // NOI18N
                
                String lower = get(
                        "product.requirements." + i + // NOI18N
                        ".version-lower"); // NOI18N
                String upper = get(
                        "product.requirements." + i + // NOI18N
                        ".version-upper"); // NOI18N
                int alternativeRequirements =
                        getInt(
                        "product.requirements." + i +// NOI18N
                        ".alternatives.length");// NOI18N
                
                if(alternativeRequirements==0) {
                    xml.append("                <requirement " + // NOI18N
                            "uid=\"" + uid + "\" " + // NOI18N
                            "version-lower=\"" + lower + "\" " + // NOI18N
                            "version-upper=\"" + upper + "\"/>\n"); // NOI18N
                } else {
                    xml.append("                <requirement " + // NOI18N
                            "uid=\"" + uid + "\" " + // NOI18N
                            "version-lower=\"" + lower + "\" " + // NOI18N
                            "version-upper=\"" + upper + "\">\n"); // NOI18N
                    
                    for(int j=1;j<=alternativeRequirements;j++) {
                        int reqs = getInt(
                                "product.requirements." + i + 
                                ".alternatives." + j + 
                                ".requirements.length");
                        
                        if (reqs > 0 ) {
                            xml.append("                    <or>\n");
                            for(int k = 1 ; k <= reqs; k++) {
                                String prefix = 
                                        "product.requirements." + i +
                                        ".alternatives." + j +
                                        ".requirements." + k + ".";
                                uid   = get(prefix  + "uid"); // NOI18N
                                lower = get(prefix + "version-lower"); // NOI18N
                                upper = get(prefix + "version-upper"); // NOI18N
                                
                                xml.append(
                                        "                        <requirement " + // NOI18N
                                        "uid=\"" + uid + "\" " + // NOI18N
                                        "version-lower=\"" + lower + "\" " + // NOI18N
                                        "version-upper=\"" + upper + "\"/>\n"); // NOI18N
                            }
                            xml.append("                    </or>\n");
                        }
                    }
                    
                    xml.append("                </requirement>\n");
                }
            }
            
            for (int i = 1; i <=
                    getInt("product.conflicts.length"); i++) { // NOI18N
                uid = get("product.conflicts." + i + ".uid"); // NOI18N
                
                String lower = get(
                        "product.conflicts." + i + ".version-lower"); // NOI18N
                String upper = get(
                        "product.conflicts." + i + ".version-upper"); // NOI18N
                
                xml.append("                <conflict " + // NOI18N
                        "uid=\"" + uid + "\" " + // NOI18N
                        "version-lower=\"" + lower + // NOI18N
                        "\" version-upper=\"" + upper + "\"/>\n"); // NOI18N
            }
            
            for (int i = 1; i <=
                    getInt("product.install-afters.length"); i++) { // NOI18N
                uid = get("product.install-afters." + i + ".uid"); // NOI18N
                
                xml.append("                <install-after " + "uid=\"" + // NOI18N
                        uid + "\"/>\n"); // NOI18N
            }
            
            xml.append("            </dependencies>\n"); // NOI18N
        }
        
        xml.append("        </product>\n"); // NOI18N
        xml.append("    </components>\n"); // NOI18N
        xml.append("</registry>\n"); // NOI18N
        
        try {
            Utils.write(file, xml);
        } catch (IOException e) {
            throw new BuildException(e);
        }
    }
    
    // private //////////////////////////////////////////////////////////////////////
    /**
     * Gets a project's property's string value.
     *
     * @param name Name of the property whose value is required.
     * @return The value of the property as a string.
     */
    private String get(String name) {
        return getProject().getProperty(name);
    }
    
    /**
     * Gets a project's property's integer value.
     *
     * @param name Name of the property whose value is required.
     * @return The value of the property as an integer.
     */
    private int getInt(String name) {
        if (get(name) == null) {
            return 0;
        } else {
            return Integer.parseInt(get(name));
        }
    }
}
