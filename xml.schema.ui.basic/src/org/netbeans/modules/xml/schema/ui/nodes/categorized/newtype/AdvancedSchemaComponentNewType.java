/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

/*
 * AdvancedSchemaComponentNewType.java
 *
 * Created on January 19, 2006, 1:31 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.schema.ui.nodes.categorized.newtype;

// java imports
import java.awt.Dialog;
import java.io.IOException;

//netbeans imports
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import org.openide.util.datatransfer.NewType;
import org.openide.ErrorManager;

//local imports
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.SchemaComponentReference;
import org.netbeans.modules.xml.schema.ui.basic.UIUtilities;
import org.netbeans.modules.xml.xam.ui.cookies.ViewComponentCookie;
import org.netbeans.modules.xml.xam.ui.customizer.Customizer;
import org.openide.DialogDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;

/**
 *
 * @author Ajit Bhate (ajit.bhate@Sun.Com)
 */
public class AdvancedSchemaComponentNewType extends NewType {
    
    private SchemaComponentReference<? extends SchemaComponent> reference;
    private Class<? extends SchemaComponent> childType;
    private AdvancedSchemaComponentCreator creator;
    private SchemaComponent component;
    private SchemaComponent container;
    
    /**
     * Creates a new instance of AdvancedSchemaComponentNewType
     */
    public AdvancedSchemaComponentNewType(SchemaComponentReference<? extends SchemaComponent>
            reference, Class<? extends SchemaComponent> childType) {
        super();
        this.reference=reference;
        this.childType=childType;
        this.creator = new AdvancedSchemaComponentCreator();
    }
    
    public String getName() {
        return NbBundle.getMessage(AdvancedSchemaComponentNewType.class,
                "LBL_NewType_".concat(getChildType().getSimpleName()));
    }
    
    public void create() {
        if (!canCreate()) {
            showIncompleteDefinitionMessage();
            return;
        }
        SchemaModel model = getSchemaComponent().getModel();
        assert model != null;
        try {
            if(customize()) {
                model.startTransaction();
                addComponent(container);
            }
        } finally {
            if (model.isIntransaction()) {
                model.endTransaction();
            }
        }
        
        // select in UI
        try {
            FileObject fobj = (FileObject) model.getModelSource().
                    getLookup().lookup(FileObject.class);
            if (fobj != null) {
                DataObject dobj = DataObject.find(fobj);
                if (dobj != null) {
                    ViewComponentCookie svc = (ViewComponentCookie) dobj.getCookie(
                            ViewComponentCookie.class);
                    if(svc!=null) svc.view(ViewComponentCookie.View.SCHEMA,getComponent());
                }
            }
        } catch (DataObjectNotFoundException donfe) {
        }
    }
    
    public boolean canCreate() {
        if(getComponent()==null) {
            setComponent(createComponent());
            setContainer(findContainer());
        }
        return getContainer()!=null;
    }
    
    /**
     * The container of the new type.
     * In most cases it will be getSchemaComponent(), but need to ensure correct type.
     * It uses AdvancedSchemaComponentCreator to find appropriate container.
     */
    protected SchemaComponent findContainer() {
        return getCreator().findContainer(getSchemaComponent(), getComponent());
    }
    
    /**
     * This api adds required new type to the container.
     * This is called from create.
     * The create method ensures a transaction and does error reporting.
     */
    protected void addComponent(SchemaComponent container) {
        getCreator().add(container, getComponent());
    }
    
    /**
     * This api creates required new type.
     * uses SchemaComponentCreator to add
     */
    protected SchemaComponent createComponent() {
        return SchemaComponentCreator.createComponent(getSchemaComponent().
                getModel().getFactory(), getChildType());
    }
    
    protected SchemaComponent getSchemaComponent() {
        return getReference().get();
    }
    
    protected SchemaComponentReference<? extends SchemaComponent> getReference() {
        return reference;
    }
    
    protected Class<? extends SchemaComponent> getChildType() {
        return childType;
    }
    
    
    protected AdvancedSchemaComponentCreator getCreator() {
        return creator;
    }
    
    /**
     * getter for newly created component
     */
    protected SchemaComponent getComponent() {
        return component;
    }
    
    /**
     * setter for newly created component
     */
    protected void setComponent(SchemaComponent component) {
        this.component = component;
    }
    
    /**
     * getter for container
     */
    protected SchemaComponent getContainer() {
        return container;
    }
    
    /**
     * setter for container
     */
    protected void setContainer(SchemaComponent container) {
        this.container = container;
    }
    
    /**
     * This apis check if newtype needs a customizer and returns true,
     * if customizer is not needed or if user OKs customization, false otherwise.
     */
    protected boolean customize() {
        Customizer customizer = getCreator().createCustomizer(getComponent(),getContainer());
        if(customizer==null || customizer.getComponent()==null) return true;
        DialogDescriptor descriptor = UIUtilities.
                getCustomizerDialog(customizer,getName(),true);
        Dialog dlg = DialogDisplayer.getDefault().createDialog(descriptor);
        dlg.setTitle(NbBundle.getMessage(AdvancedSchemaComponentNewType.class,
                "LBL_Customizer_".concat(getChildType().getSimpleName())));
        dlg.getAccessibleContext().setAccessibleDescription(dlg.getTitle());
        dlg.setVisible(true);
        return descriptor.getValue()==DialogDescriptor.OK_OPTION;
    }
    /**
     * This will show a message to user if this newtype can't be created
     *
     */
    private void showIncompleteDefinitionMessage() {
        String message = NbBundle.getMessage(AdvancedSchemaComponentNewType.class,
                "MSG_NewType_IncompleteDefinition",	getName().toLowerCase());
        NotifyDescriptor.Message descriptor =
                new NotifyDescriptor.Message(message);
        DialogDisplayer.getDefault().notify(descriptor);
    }
}
