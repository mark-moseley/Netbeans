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

package org.netbeans.modules.hibernate.loaders.cfg.multiview;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.hibernate.cfg.HibernateCfgProperties;
import org.netbeans.modules.hibernate.cfg.model.SessionFactory;
import org.netbeans.modules.hibernate.loaders.cfg.HibernateCfgDataObject;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * 
 * @author Dongmei Cao
 */
public class Util {
    
    public static String[] getAllPropNames(String propCat) {
        if (propCat.equals(HibernateCfgToolBarMVElement.JDBC_PROPS)) {
            return HibernateCfgProperties.jdbcProps;
        } else if (propCat.equals(HibernateCfgToolBarMVElement.DATASOURCE_PROPS)) {
            return HibernateCfgProperties.datasourceProps;
        } else if (propCat.equals(HibernateCfgToolBarMVElement.CONFIGURATION_PROPS)) {
            return HibernateCfgProperties.optionalConfigProps;
        } else if (propCat.equals(HibernateCfgToolBarMVElement.JDBC_CONNECTION_PROPS)) {
            return HibernateCfgProperties.optionalJdbcConnProps;
        } else if (propCat.equals(HibernateCfgToolBarMVElement.CACHE_PROPS)) {
            return HibernateCfgProperties.optionalCacheProps;
        } else if (propCat.equals(HibernateCfgToolBarMVElement.TRANSACTION_PROPS)) {
            return HibernateCfgProperties.optionalTransactionProps;
        } else if (propCat.equals(HibernateCfgToolBarMVElement.MISCELLANEOUS_PROPS)) {
            return HibernateCfgProperties.optionalMiscProps;
        } else // Should never be here
            return new String[0];
    }
    
    /**
     * Gets the properties that are not defined in the configuration file yet
     * 
     * @param propCat The property category
     * @param sessionFactory The session factory that contains the properties
     * @return Array of property names
     */
    public static String[] getAvailPropNames(String propCat, SessionFactory sessionFactory) {

        List<String> propsList = Arrays.asList(getAllPropNames(propCat));
        
        if (sessionFactory != null) {
            ArrayList<String> availProps = new ArrayList<String>(propsList);
            for (int i = 0; i < sessionFactory.sizeProperty2(); i++) {

                String propName = sessionFactory.getAttributeValue(SessionFactory.PROPERTY2, i, "Name");
                if (availProps.contains(propName) ||
                        availProps.contains("hibernate." + propName)) {
                    availProps.remove(propName);
                }
            }

            return availProps.toArray(new String[0]);
        }

        return new String[0];
    }
    
    // Gets the list of mapping files from HibernateEnvironment.
    public static String[] getMappingFilesFromProject(FileObject fileObj) {
        org.netbeans.api.project.Project enclosingProject = org.netbeans.api.project.FileOwnerQuery.getOwner(fileObj);
        org.netbeans.modules.hibernate.service.HibernateEnvironment env = enclosingProject.getLookup().lookup(org.netbeans.modules.hibernate.service.HibernateEnvironment.class);
        return env.getAllHibernateMappings().toArray(new String[]{});
    }

    
    public static SourceGroup[] getJavaSourceGroups(HibernateCfgDataObject dObj) throws java.io.IOException {
        Project proj = FileOwnerQuery.getOwner(dObj.getPrimaryFile());
        if (proj==null) return new SourceGroup[]{};
        Sources sources = ProjectUtils.getSources(proj);
        return sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
    }
    
     public static String getResourcePath(SourceGroup[] groups, FileObject fo) {
        return getResourcePath(groups, fo, '.', false);
    }
     
     public static String getResourcePath(SourceGroup[] groups, FileObject fo, char separator) {
        return getResourcePath(groups, fo, separator, false);
    }
     
     public static String getResourcePath(SourceGroup[] groups, FileObject fo, char separator, boolean withExt) {
        for (int i=0;i<groups.length;i++) {
            FileObject root = groups[i].getRootFolder();
            if (FileUtil.isParentOf(root,fo)) {
                String relativePath = FileUtil.getRelativePath(root,fo);
                if (relativePath!=null) {
                    if (separator!='/') relativePath = relativePath.replace('/',separator);
                    if (!withExt) {
                        int index = relativePath.lastIndexOf((int)'.');
                        if (index>0) relativePath = relativePath.substring(0,index);
                    }
                    return relativePath;
                } else {
                    return "";
                }
            }
        }
        return "";
    }

}
