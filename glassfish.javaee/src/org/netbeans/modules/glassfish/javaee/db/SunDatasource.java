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

package org.netbeans.modules.glassfish.javaee.db;

import java.io.File;
import org.netbeans.modules.j2ee.deployment.common.api.Datasource;

/**
 * Data model for a sun datasource (combined jdbc resource and connection pool).
 * 
 * @author Nitya Doraisamy
 */
public class SunDatasource implements Datasource {

    private final String jndiName;
    private final String url;
    private final String username;
    private final String password;
    private final String driverClassName;
    private File resourceDir;
    
    public SunDatasource(String jndiName, String url, String username, 
            String password, String driverClassName) {
        this(jndiName, url, username, password, driverClassName, null);
    }
    
    public SunDatasource(String jndiName, String url, String username, 
            String password, String driverClassName, File resourceDir) {
        this.jndiName = jndiName;
        this.url = url;
        this.username = username;
        this.password = password;
        this.driverClassName = driverClassName;
        this.resourceDir = resourceDir;
    }
    
    public String getDisplayName() {
        return jndiName;
    }

    public String getJndiName() {
        return jndiName;
    }

    public String getUrl() {
        return url;
    }
    
    public String getUsername() {
        return username;
    }
    
    public String getPassword() {
        return password;
    }
    
    public String getDriverClassName() {
        return driverClassName;
    }
    
    File getResourceDir() {
        return resourceDir;
    }
    
    void setResourceDir(File resourceDir) {
        this.resourceDir = resourceDir;
    }
    
    @Override
    public String toString() {
        return "[ " + jndiName + " : " + url 
                + " : " + username + " : " + password
                + " : " + driverClassName + " ]";
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SunDatasource other = (SunDatasource) obj;
        if (this.jndiName == null || !this.jndiName.equals(other.jndiName)) {
            return false;
        }
        if (this.url == null || !this.url.equals(other.url)) {
            return false;
        }
        if (this.username == null || !this.username.equals(other.username)) {
            return false;
        }
        if (this.password == null || !this.password.equals(other.password)) {
            return false;
        }
        if (this.driverClassName == null || !this.driverClassName.equals(other.driverClassName)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 41 * hash + (this.jndiName != null ? this.jndiName.hashCode() : 0);
        hash = 41 * hash + (this.url != null ? this.url.hashCode() : 0);
        hash = 41 * hash + (this.username != null ? this.username.hashCode() : 0);
        hash = 41 * hash + (this.password != null ? this.password.hashCode() : 0);
        hash = 41 * hash + (this.driverClassName != null ? this.driverClassName.hashCode() : 0);
        return hash;
    }

}
