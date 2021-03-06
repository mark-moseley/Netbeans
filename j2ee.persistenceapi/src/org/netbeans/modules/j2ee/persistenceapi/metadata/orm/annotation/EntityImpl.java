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

package org.netbeans.modules.j2ee.persistenceapi.metadata.orm.annotation;

import java.util.Map;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.TypeElement;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationModelHelper;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.parser.AnnotationParser;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.JavaContextListener;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.parser.ParseResult;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.PersistentObject;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.*;

public class EntityImpl extends PersistentObject implements Entity, JavaContextListener {

    private final EntityMappingsImpl root;

    // persistent
    private String name;
    private String class2;
    private Table table;

    // transient: set to null in javaContextLeft()
    private IdClassImpl idClass;
    private AttributesImpl attributes;

    public EntityImpl(AnnotationModelHelper helper, EntityMappingsImpl root, TypeElement typeElement) {
        super(helper, typeElement);
        this.root = root;
        helper.addJavaContextListener(this);
        boolean valid = refresh(typeElement);
        assert valid;
    }

    boolean refresh(TypeElement typeElement) {
        class2 = typeElement.getQualifiedName().toString();
        AnnotationModelHelper helper = getHelper();
        Map<String, ? extends AnnotationMirror> annByType = helper.getAnnotationsByType(typeElement.getAnnotationMirrors());
        AnnotationMirror entityAnn = annByType.get("javax.persistence.Entity"); // NOI18N
        if (entityAnn == null) {
            return false;
        }
        AnnotationParser parser = AnnotationParser.create(helper);
        parser.expectString("name", parser.defaultValue(typeElement.getSimpleName().toString())); // NOI18N
        ParseResult parseResult = parser.parse(entityAnn); // NOI18N
        name = parseResult.get("name", String.class); // NOI18N
        // also reading the table element to avoid initializing the whole model
        // when a client looking the entity mapped to a specific table iterates
        // over all entities calling getTable().
        // XXX locale?
        table = new TableImpl(helper, annByType.get("javax.persistence.Table"), name.toUpperCase()); // NOI18N
        return true;
    }

    EntityMappingsImpl getRoot() {
        return root;
    }

    public void javaContextLeft() {
        attributes = null;
        idClass = null;
    }

    public void setName(String value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public String getName() {
        return name;
    }

    public void setClass2(String value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public String getClass2() {
        return class2;
    }

    public void setAccess(String value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public String getAccess() {
        return getAttributes().hasFieldAccess() ? FIELD_ACCESS : PROPERTY_ACCESS;
    }

    public void setMetadataComplete(boolean value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public boolean isMetadataComplete() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public void setDescription(String value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public String getDescription() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public void setTable(Table value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public Table getTable() {
        return table;
    }

    public Table newTable() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public void setSecondaryTable(int index, SecondaryTable value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public SecondaryTable getSecondaryTable(int index) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public int sizeSecondaryTable() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public void setSecondaryTable(SecondaryTable[] value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public SecondaryTable[] getSecondaryTable() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public int addSecondaryTable(SecondaryTable value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public int removeSecondaryTable(SecondaryTable value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public SecondaryTable newSecondaryTable() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public void setPrimaryKeyJoinColumn(int index, PrimaryKeyJoinColumn value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public PrimaryKeyJoinColumn getPrimaryKeyJoinColumn(int index) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public int sizePrimaryKeyJoinColumn() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public void setPrimaryKeyJoinColumn(PrimaryKeyJoinColumn[] value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public PrimaryKeyJoinColumn[] getPrimaryKeyJoinColumn() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public int addPrimaryKeyJoinColumn(PrimaryKeyJoinColumn value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public int removePrimaryKeyJoinColumn(PrimaryKeyJoinColumn value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public PrimaryKeyJoinColumn newPrimaryKeyJoinColumn() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public void setIdClass(IdClass value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public IdClass getIdClass() {
        if (idClass == null) {
            TypeElement typeElement = getTypeElement();
            if (typeElement != null) {
                idClass = EntityMappingsUtilities.getIdClass(getRoot().getHelper(), typeElement);
            }
        }
        return idClass;
    }

    public IdClass newIdClass() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public void setInheritance(Inheritance value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public Inheritance getInheritance() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public Inheritance newInheritance() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public void setDiscriminatorValue(String value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public String getDiscriminatorValue() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public void setDiscriminatorColumn(DiscriminatorColumn value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public DiscriminatorColumn getDiscriminatorColumn() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public DiscriminatorColumn newDiscriminatorColumn() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public void setSequenceGenerator(SequenceGenerator value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public SequenceGenerator getSequenceGenerator() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public SequenceGenerator newSequenceGenerator() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public void setTableGenerator(TableGenerator value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public TableGenerator getTableGenerator() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public TableGenerator newTableGenerator() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public void setNamedQuery(int index, NamedQuery value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public NamedQuery getNamedQuery(int index) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public int sizeNamedQuery() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public void setNamedQuery(NamedQuery[] value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public NamedQuery[] getNamedQuery() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public int addNamedQuery(NamedQuery value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public int removeNamedQuery(NamedQuery value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public NamedQuery newNamedQuery() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public void setNamedNativeQuery(int index, NamedNativeQuery value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public NamedNativeQuery getNamedNativeQuery(int index) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public int sizeNamedNativeQuery() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public void setNamedNativeQuery(NamedNativeQuery[] value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public NamedNativeQuery[] getNamedNativeQuery() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public int addNamedNativeQuery(NamedNativeQuery value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public int removeNamedNativeQuery(NamedNativeQuery value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public NamedNativeQuery newNamedNativeQuery() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public void setSqlResultSetMapping(int index, SqlResultSetMapping value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public SqlResultSetMapping getSqlResultSetMapping(int index) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public int sizeSqlResultSetMapping() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public void setSqlResultSetMapping(SqlResultSetMapping[] value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public SqlResultSetMapping[] getSqlResultSetMapping() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public int addSqlResultSetMapping(SqlResultSetMapping value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public int removeSqlResultSetMapping(SqlResultSetMapping value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public SqlResultSetMapping newSqlResultSetMapping() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public void setExcludeDefaultListeners(EmptyType value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public EmptyType getExcludeDefaultListeners() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public EmptyType newEmptyType() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public void setExcludeSuperclassListeners(EmptyType value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public EmptyType getExcludeSuperclassListeners() {
        throw new UnsupportedOperationException("This operation is not implemented yet"); // NOI18N
    }

    public void setEntityListeners(EntityListeners value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public EntityListeners getEntityListeners() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public EntityListeners newEntityListeners() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public void setPrePersist(PrePersist value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public PrePersist getPrePersist() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public PrePersist newPrePersist() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public void setPostPersist(PostPersist value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public PostPersist getPostPersist() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public PostPersist newPostPersist() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public void setPreRemove(PreRemove value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public PreRemove getPreRemove() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public PreRemove newPreRemove() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public void setPostRemove(PostRemove value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public PostRemove getPostRemove() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public PostRemove newPostRemove() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public void setPreUpdate(PreUpdate value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public PreUpdate getPreUpdate() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public PreUpdate newPreUpdate() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public void setPostUpdate(PostUpdate value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public PostUpdate getPostUpdate() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public PostUpdate newPostUpdate() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public void setPostLoad(PostLoad value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public PostLoad getPostLoad() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public PostLoad newPostLoad() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public void setAttributeOverride(int index, AttributeOverride value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public AttributeOverride getAttributeOverride(int index) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public int sizeAttributeOverride() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public void setAttributeOverride(AttributeOverride[] value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public AttributeOverride[] getAttributeOverride() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public int addAttributeOverride(AttributeOverride value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public int removeAttributeOverride(AttributeOverride value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public AttributeOverride newAttributeOverride() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public void setAssociationOverride(int index, AssociationOverride value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public AssociationOverride getAssociationOverride(int index) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public int sizeAssociationOverride() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public void setAssociationOverride(AssociationOverride[] value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public AssociationOverride[] getAssociationOverride() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public int addAssociationOverride(AssociationOverride value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public int removeAssociationOverride(AssociationOverride value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public AssociationOverride newAssociationOverride() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public void setAttributes(Attributes value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public AttributesImpl getAttributes() {
        if (attributes == null) {
            attributes = new AttributesImpl(this);
        }
        return attributes;
    }

    public Attributes newAttributes() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public String toString() {
        return "EntityImpl[name='" + name + "',class2='" + class2 + "']"; // NOI18N
    }
}
