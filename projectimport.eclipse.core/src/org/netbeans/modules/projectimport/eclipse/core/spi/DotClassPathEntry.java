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

package org.netbeans.modules.projectimport.eclipse.core.spi;

import java.util.Map;
import java.util.Set;

/**
 * Represents one classpath entry of an Eclipse project's .classpath file.
 * Basically set of attributes (key/value pairs) and couple of helper methods for
 * some important attributes like 'kind' or 'path'.
 * 
 */
public final class DotClassPathEntry {

    public static final String ATTRIBUTE_KIND = "kind";
    public static final String ATTRIBUTE_PATH = "path";

    public static enum Kind {
        CONTAINER,
        LIBRARY,
        PROJECT,
        SOURCE,
        VARIABLE,
        OUTPUT
    };
    
    private Map<String,String> properties;
    
    private String absolutePath;
    
    private String containerMapping;
    
    private String linkName;
    
    private Boolean importSuccessful;
    
    public DotClassPathEntry(Map<String, String> properties, String linkName) {
        this.properties = properties;
        this.linkName = linkName;
    }
    
    public String getProperty(String key) {
        return properties.get(key);
    }
    
    public Set<String> getPropertyNames() {
        return properties.keySet();
    }
    
    public Kind getKind() {
        String value = getProperty(ATTRIBUTE_KIND);
        if ("con".equals(value)) { // NOI18N
            return Kind.CONTAINER;
        } else if ("lib".equals(value)) { // NOI18N
            return Kind.LIBRARY;
        } else if ("src".equals(value)) { // NOI18N
            // resolved SOURCE link has absolute URL:
            if (getRawPath().startsWith("/") && getLinkName() == null) {
                return Kind.PROJECT;
            } else {
                return Kind.SOURCE;
            }
        } else if ("var".equals(value)) { // NOI18N
            return Kind.VARIABLE;
        } else if ("output".equals(value)) { // NOI18N
            return Kind.OUTPUT;
        }
        throw new IllegalStateException("unknown kind: "+value);
    }
    /**
     * Value of path attribute. If path attribute was link then resolved link 
     * value is returned instead and link name can be retrieve via {@link #getLinkName}.
     */
    public String getRawPath() {
        return getProperty(ATTRIBUTE_PATH);
    }

    /**
     * Normalized path.
     */
    public String getAbsolutePath() {
        return absolutePath;
    }

    /**
     * Returns link name. 
     * @return null if entry was not a link
     */
    /*public*/ String getLinkName() {
        return linkName;
    }
    
    /**
     * Despite being public this method should not be called outside of 
     * eclipse.core module.
     */
    public void setAbsolutePath(String absolutePath) {
        this.absolutePath = absolutePath;
    }
    
    @Override
    public String toString() {
        return "DotClassPathEntry: "+properties;
    }

    /**
     * Returns Ant-like classpath where entries are separated by ":" or ";" and
     * entry can be or contain:
     *  <li>absolute path to a folder or jar
     *  <li>netbeans project relative paths to a folders or jars
     *  <li>references to existing variables from project properties, 
     *     private properties or build.properties.
     *  <li>perhaps more
     * 
     * <p>Valid only for entry of CONTAINER type.
     */
    public String getContainerMapping() {
        return containerMapping;
    }
    
    /**
     * Despite being public this method should not be called outside of 
     * eclipse.core module.
     */
    public void setContainerMapping(String containerMapping) {
        this.containerMapping = containerMapping;
    }

    public Boolean getImportSuccessful() {
        return importSuccessful;
    }

    /**
     * Despite being public this method should not be called outside of 
     * eclipse.core module.
     */
    public void setImportSuccessful(Boolean importSuccessful) {
        this.importSuccessful = importSuccessful;
    }
    
}
