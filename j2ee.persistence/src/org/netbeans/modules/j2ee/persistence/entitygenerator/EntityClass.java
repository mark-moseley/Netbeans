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
package org.netbeans.modules.j2ee.persistence.entitygenerator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.openide.filesystems.FileObject;

/**
 * Abstract description of an entity class
 * (either CMP or Java Persistence API).
 *
 * @author Chris Webster, Martin Adamek, Andrei Badea
 */
public class EntityClass {
    
    private final String tableName;
    private final FileObject rootFolder;
    private final String className;
    private final String packageName;
    
    private List<RelationshipRole> roles;
    private List<EntityMember> fields;
    private boolean usePkField;
    private String pkFieldName;
    private CMPMappingModel mappingModel;
    
    public EntityClass(String tableName, FileObject rootFolder, String packageName, String className) {
        this.tableName = tableName;
        this.rootFolder = rootFolder;
        this.packageName = packageName;
        this.className = className;
        
        roles = Collections.<RelationshipRole>emptyList();
        fields = new ArrayList<EntityMember>();
        mappingModel = new CMPMappingModel();
    }
    
    public void addRole(RelationshipRole role) {
        if (roles == Collections.<RelationshipRole>emptyList()) {
            roles = new ArrayList<RelationshipRole>();
        }
        roles.add(role);
    }
    
    public List<RelationshipRole> getRoles() {
        return roles;
    }
    
    public List<EntityMember> getFields() {
        return fields;
    }
    
    public void setFields(List<EntityMember> fields) {
        this.fields = fields;
    }
    
    public String toString() {
        String cmpFields = ""; // NOI18N
        for (EntityMember entityMember : getFields()) {
            cmpFields += " " + entityMember.getMemberName() + (entityMember.isPrimaryKey() ? " (PK) " : " "); // NOI18N
        }
        return "bean name " + getClassName() + // NOI18N
                "\ncmp-fields "+ cmpFields;  // NOI18N
    }
    
    public FileObject getRootFolder() {
        return rootFolder;
    }
    
    public String getPackage() {
        return packageName;
    }
    
    public String getTableName() {
        return tableName;
    }
    
    public String getClassName() {
        return className;
    }
    
    public FileObject getPackageFileObject() {
        String relative = packageName.replace('.', '/');
        return rootFolder.getFileObject(relative);
    }
    
    public CMPMappingModel getCMPMapping() {
        mappingModel.getCMPFieldMapping().clear();
        for (EntityMember entityMember : getFields()) {
            mappingModel.setTableName(entityMember.getTableName());
            mappingModel.getCMPFieldMapping().put(entityMember.getMemberName(), entityMember.getColumnName());
        }
        return mappingModel;
    }
    
    public void usePkField(boolean usePkField) {
        this.usePkField = usePkField;
    }
    
    public boolean isUsePkField() {
        return usePkField;
    }
    
    public String getPkFieldName() {
        return pkFieldName;
    }
    
    public void setPkFieldName(String pkFieldName) {
        this.pkFieldName = pkFieldName;
    }
}
