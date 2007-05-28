/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.form.j2ee;

import com.sun.source.tree.*;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTargetDragEvent;
import java.util.*;
import javax.swing.ImageIcon;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.modules.db.api.explorer.DatabaseMetaDataTransfer;
import org.netbeans.modules.form.*;
import org.netbeans.modules.form.assistant.AssistantMessages;
import org.netbeans.modules.form.palette.PaletteItem;
import org.netbeans.modules.form.project.ClassPathUtils;
import org.netbeans.modules.form.project.ClassSource;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.persistence.api.PersistenceScope;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.EntityMappingsMetadata;
import org.netbeans.modules.j2ee.persistence.dd.persistence.model_1_0.PersistenceUnit;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 * Result of database table DnD.
 *
 * @author Jan Stola
 */
public class DBTableDrop extends DBConnectionDrop {
    /** Dropped table. */
    private DatabaseMetaDataTransfer.Table table;

    /**
     * Creates new <code>DBTableDrop</code>.
     *
     * @param model form model.
     * @param table dropped table.
     */
    public DBTableDrop(FormModel model, DatabaseMetaDataTransfer.Table table) {
        super(model, null);
        this.table = table;
    }

    /**
     * Returns <code>JTable</code> palette item.
     *
     * @param dtde corresponding drop target drag event.
     * @return <code>JTable</code> palette item.
     */
    public PaletteItem getPaletteItem(DropTargetDragEvent dtde) {
        PaletteItem pItem;
        setBindingOnly(dtde.getDropAction() == DnDConstants.ACTION_MOVE);
        if (isBindingOnly()) {
            if (!assistantInitialized) {
                initAssistant();
            }
            FormEditor.getAssistantModel(model).setContext("tableDropBinding", "tableDropComponent"); // NOI18N
            pItem = new PaletteItem(new ClassSource("javax.persistence.EntityManager", // NOI18N
                new String[] { ClassSource.LIBRARY_SOURCE },
                new String[] { "toplink" }), null); // NOI18N
            pItem.setIcon(new ImageIcon(
                Utilities.loadImage("org/netbeans/modules/form/j2ee/resources/binding.gif")).getImage()); // NOI18N
        } else {
            pItem = new PaletteItem(new ClassSource("javax.swing.JTable", null, null), null); // NOI18N
        }
        return pItem;
    }

    /**
     * Registers assistant messages related to DB table DnD.
     */
    private void initAssistant() {
        ResourceBundle bundle = NbBundle.getBundle(DBColumnDrop.class);
        String dropBindingMsg = bundle.getString("MSG_TableDropBinding"); // NOI18N
        String dropComponentMsg = bundle.getString("MSG_TableDropComponent"); // NOI18N
        AssistantMessages messages = AssistantMessages.getDefault();
        messages.setMessages("tableDropBinding", dropBindingMsg); // NOI18N
        messages.setMessages("tableDropComponent", dropComponentMsg); // NOI18N        
        assistantInitialized = true;
    }

    /**
     * Post-processing after placement of the dragged table.
     *
     * @param componentId ID of the table (in fact, it is ID of the inserted
     * component e.g. the enclosing scroll pane).
     * @param droppedOverId ID of a component the new component has been dropped over.
     */
    public void componentAdded(String componentId, String droppedOverId) {
        try {
            FileObject formFile = FormEditor.getFormDataObject(model).getFormFile();
            project = FileOwnerQuery.getOwner(formFile);

            // Make sure persistence.xml file exists
            FileObject persistenceXML = J2EEUtils.getPersistenceXML(project, true);
            
            // Initializes persistence unit and persistence descriptor
            PersistenceUnit unit = J2EEUtils.initPersistenceUnit(persistenceXML, table.getDatabaseConnection());

            // Initializes project's classpath
            J2EEUtils.updateProjectForUnit(persistenceXML, unit, table.getJDBCDriver());

            // Obtain description of entity mappings
            PersistenceScope scope = PersistenceScope.getPersistenceScope(formFile);
            MetadataModel<EntityMappingsMetadata> mappings = scope.getEntityMappingsModel(unit.getName());
            
            // Find entity that corresponds to the dragged table
            String[] entityInfo = J2EEUtils.findEntity(mappings, table.getTableName());
            
            // Create a new entity (if there isn't one that corresponds to the dragged table)
            if (entityInfo == null) {
                // Generates a Java class for the entity
                J2EEUtils.createEntity(formFile.getParent(), scope, unit, table.getDatabaseConnection(), table.getTableName(), null);

                mappings = scope.getEntityMappingsModel(unit.getName());
                entityInfo = J2EEUtils.findEntity(mappings, table.getTableName());
            }

            // Find (or create) entity manager "bean" for the persistence unit
            RADComponent entityManager;
            if (isBindingOnly()) {
                String unitName = unit.getName();
                entityManager = J2EEUtils.findEntityManager(model, unitName);
                if (entityManager == null) {
                    entityManager = model.getMetaComponent(componentId);
                    entityManager.getPropertyByName("persistenceUnit").setValue(unitName); // NOI18N
                    J2EEUtils.renameComponent(entityManager, true, unitName + "EntityManager", "entityManager"); // NOI18N
                } else {
                    // The entity manager was already there => remove the dragged one
                    model.removeComponent(model.getMetaComponent(componentId), true);
                }
            } else {
                entityManager = initEntityManagerBean(unit);
            }

            RADComponent queryBean = createQueryBean(model, entityManager, entityInfo[0]);

            // Create a meta-component for the collection of entities
            RADComponent resultList = createResultListBean(model, queryBean, entityInfo);

            Class beanClass = javax.swing.JTable.class;
            if (isBindingOnly()) {
                if (droppedOverId == null) return;
                RADComponent comp = model.getMetaComponent(droppedOverId);
                if (javax.swing.JScrollPane.class.isAssignableFrom(comp.getBeanClass())) {
                    if (comp instanceof RADVisualContainer) {
                        RADVisualContainer cont = (RADVisualContainer)comp;
                        if (cont.getSubComponents().length > 0) {
                            comp = cont.getSubComponent(0);
                            droppedOverId = comp.getId();
                        }
                    }
                }
                // PENDING subclasses
                beanClass = comp.getBeanClass();
                if (!javax.swing.JTable.class.equals(beanClass)
                    && !javax.swing.JList.class.equals(beanClass)
                    && !javax.swing.JComboBox.class.equals(beanClass)) return;
            }
            if (beanClass.equals(javax.swing.JTable.class)) {
                // Bind the table component to the result list
                bindTableComponent(isBindingOnly() ? droppedOverId : componentId,
                    resultList, mappings, entityInfo);
            } else {
                // JList and JComboBox
                bindListComponent(droppedOverId, resultList);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Creates query RAD component.
     *
     * @param model form model.
     * @param entityManager entity manager used for creation of the query.
     * @param entityName name of the entity whose instances will be in the result of the query.
     * @throws Exception when something goes wrong.
     * @return query RAD component.
     */
    public static RADComponent createQueryBean(FormModel model, RADComponent entityManager, String entityName) throws Exception {
        RADComponent query = new RADComponent();
        FileObject formFile = FormEditor.getFormDataObject(model).getFormFile();
        Class queryClass = ClassPathUtils.loadClass("javax.persistence.Query", formFile); // NOI18N

        query.initialize(model);
        query.initInstance(queryClass);

        char c = entityName.toLowerCase().charAt(0);
        String q = "SELECT " + c + " FROM " + entityName + " " + c;  // NOI18N
        query.getPropertyByName("query").setValue(q); // NOI18N
        query.getPropertyByName("entityManager").setValue(entityManager); // NOI18N
        query.setStoredName(c + entityName.substring(1) + "Query"); // NOI18N

        model.addComponent(query, null, true);
        return query;
    }

    /**
     * Creates query result list RAD component.
     *
     * @param model form model.
     * @param query query used to obtain the list of entities.
     * @param entityInfo information about the entity whose instances will be in the result list.
     * @throws Exception when something goes wrong.
     * @return query result list RAD component.
     */
    public static RADComponent createResultListBean(FormModel model, RADComponent query, String[] entityInfo) throws Exception {
        // Create a meta-component for the collection of entities
        RADComponent resultList = new RADComponent();
        resultList.setAuxValue("JavaCodeGenerator_TypeParameters", '<' + entityInfo[1] + '>'); // NOI18N
        resultList.initialize(model);
        resultList.initInstance(java.util.List.class);

        char c = entityInfo[0].toLowerCase().charAt(0);
        resultList.getPropertyByName("query").setValue(query); // NOI18N
        resultList.setStoredName(c + entityInfo[0].substring(1) + "List"); // NOI18N

        model.addComponent(resultList, null, true);
        return resultList;
    }

    /**
     * Binds table component to the result list.
     *
     * @param tableID ID of the table to bind.
     * @param resultList RAD component representing the result list to bind to table.
     * @param scope persistence scope.
     * @param entity persistence entity.
     */
    private void bindTableComponent(String tableID, RADComponent resultList, MetadataModel<EntityMappingsMetadata> mappings, String[] entityInfo) throws Exception {
        RADComponent table = model.getMetaComponent(tableID);
        if (javax.swing.JScrollPane.class.isAssignableFrom(table.getBeanClass())) {
            table = ((RADVisualContainer)table).getSubComponent(0);
        }

        // Bind the elements property
        BindingProperty prop = table.getBindingProperty("elements"); // NOI18N
        MetaBinding binding = new MetaBinding(resultList, null, table, "elements"); // NOI18N

        int count = 0;
        List<String> propertyNames = J2EEUtils.propertiesForColumns(mappings, entityInfo[0], null);
        FileObject formFile = FormEditor.getFormDataObject(model).getPrimaryFile();
        List<String> propertyTypes = J2EEUtils.typesOfProperties(formFile, entityInfo[1], propertyNames);
        Iterator<String> typeIter = propertyTypes.iterator();
        for (String column : propertyNames) {
            MetaBinding subBinding = binding.addSubBinding(BindingDesignSupport.elWrap(column), null);
            subBinding.setParameter(MetaBinding.TABLE_COLUMN_PARAMETER, ""+count++); // NOI18N
            String clazz = typeIter.next();
            if (clazz != null) {
                subBinding.setParameter(MetaBinding.TABLE_COLUMN_CLASS_PARAMETER, clazz);
            }
        }

        prop.setValue(binding);
    }

    /**
     * Binds list or combobox component to the result list.
     *
     * @param listID ID of the component to bind.
     * @param resultList RAD component representing the result list to bind to table.
     */
    private void bindListComponent(String listID, RADComponent resultList) throws Exception {
        RADComponent list = model.getMetaComponent(listID);
        if (javax.swing.JScrollPane.class.isAssignableFrom(list.getBeanClass())) {
            list = ((RADVisualContainer)list).getSubComponent(0);
        }

        // Bind the elements property
        BindingProperty prop = list.getBindingProperty("elements"); // NOI18N
        MetaBinding binding = new MetaBinding(resultList, null, list, "elements"); // NOI18N
        // should we create some display expression (e.g. primary key)?
        prop.setValue(binding);
    }
        
}
