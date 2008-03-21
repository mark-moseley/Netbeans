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
package org.netbeans.modules.hibernate.refactoring;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import org.netbeans.modules.hibernate.mapping.model.HibernateMapping;
import org.netbeans.modules.hibernate.mapping.model.MyClass;
import org.netbeans.modules.hibernate.mapping.model.Property;
import org.openide.ErrorManager;
import org.openide.filesystems.FileAlreadyLockedException;
import org.openide.filesystems.FileObject;

/**
 * Refactor the Java field names in the Hibernate mapping files
 * 
 * @author Dongmei Cao
 */
public class JavaFieldRenameTransaction extends RenameTransaction {

    private String className;

    public JavaFieldRenameTransaction(Set<FileObject> files, String className, String origFieldName, String newFieldName) {
        super(files, origFieldName, newFieldName);
        this.className = className;
    }

    /**
     * Do the actual changes
     * 
     */
    public void doChanges() {

        String oldName = getOriginalName();
        String newName = getNewName();

        for (FileObject mappingFileObject : getToBeModifiedFiles()) {

            OutputStream outs = null;
            try {
                InputStream is = mappingFileObject.getInputStream();
                HibernateMapping hbMapping = HibernateMapping.createGraph(is);
                MyClass[] myClazz = hbMapping.getMyClass();
                for (int ci = 0; ci < myClazz.length; ci++) {
                    String clsName = myClazz[ci].getAttributeValue("name"); // NOI18N
                    if (clsName.equals(className)) {

                        // Found the property element
                        Property[] clazzProps = myClazz[ci].getProperty2();
                        for (int pi = 0; pi < clazzProps.length; pi++) {
                            String propName = clazzProps[pi].getAttributeValue("name"); // NOI18N
                            if (propName.equals(oldName)) {
                                clazzProps[pi].setAttributeValue("name", newName); // NOI18N
                                break;
                            }
                        }

                    // TODO: need to search other elements, such as, <id>, etc
                    }
                }
                
                //HibernateMappingXmlConstants.ID_TAG, HibernateMappingXmlConstants.NAME_ATTRIB
                //HibernateMappingXmlConstants.SET_TAG, HibernateMappingXmlConstants.NAME_ATTRIB
                //HibernateMappingXmlConstants.COMPOSITE_ID_TAG, HibernateMappingXmlConstants.NAME_ATTRIB
                //HibernateMappingXmlConstants.KEY_PROPERTY_TAG, HibernateMappingXmlConstants.NAME_ATTRIB
                //HibernateMappingXmlConstants.KEY_MANY_TO_ONE_TAG, HibernateMappingXmlConstants.NAME_ATTRIB
                //HibernateMappingXmlConstants.VERSION_TAG, HibernateMappingXmlConstants.NAME_ATTRIB
                //HibernateMappingXmlConstants.TIMESTAMP_TAG, HibernateMappingXmlConstants.NAME_ATTRIB
                //HibernateMappingXmlConstants.MANY_TO_ONE_TAG, HibernateMappingXmlConstants.NAME_ATTRIB
                //HibernateMappingXmlConstants.ONE_TO_ONE_TAG, HibernateMappingXmlConstants.NAME_ATTRIB
                //HibernateMappingXmlConstants.COMPONENT_TAG, HibernateMappingXmlConstants.NAME_ATTRIB
                //HibernateMappingXmlConstants.ANY_TAG, HibernateMappingXmlConstants.NAME_ATTRIB
                //HibernateMappingXmlConstants.MAP_TAG, HibernateMappingXmlConstants.NAME_ATTRIB
                //HibernateMappingXmlConstants.LIST_TAG, HibernateMappingXmlConstants.NAME_ATTRIB
                
                outs = mappingFileObject.getOutputStream();
                hbMapping.write(outs);

            } catch (FileAlreadyLockedException ex) {
                ErrorManager.getDefault().notify(org.openide.ErrorManager.INFORMATIONAL, ex);
            } catch (IOException ex) {
                ErrorManager.getDefault().notify(org.openide.ErrorManager.INFORMATIONAL, ex);
            } finally {
                try {
                    if(outs != null)
                        outs.close();
                } catch (IOException ex) {
                    ErrorManager.getDefault().notify(org.openide.ErrorManager.INFORMATIONAL, ex);
                }
            }
        }
    }
}
