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

package org.netbeans.modules.xml.schema.ui.nodes.categorized.customizer;

import java.util.Collection;
import org.netbeans.modules.xml.retriever.catalog.Utilities;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.schema.model.SchemaModelReference;
import org.netbeans.modules.xml.schema.ui.basic.NameGenerator;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.locator.CatalogModelException;
import org.netbeans.modules.xml.xam.ui.customizer.AbstractReferenceCustomizer;
import org.netbeans.modules.xml.xam.ui.customizer.ExternalReferenceDataNode;
import org.netbeans.modules.xml.xam.ui.customizer.ExternalReferenceDecorator;
import org.netbeans.modules.xml.xam.ui.customizer.ExternalReferenceNode;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

/**
 * The ExternalReferenceDecorator for schema components.
 *
 * @author  Nathan Fiedler
 */
public class SchemaReferenceDecorator implements ExternalReferenceDecorator {
    /** The customizer that created this decorator. */
    private AbstractReferenceCustomizer customizer;

    /**
     * Creates a new instance of SchemaReferenceDecorator.
     *
     * @param  customizer  provides information about the edited component.
     */
    public SchemaReferenceDecorator(AbstractReferenceCustomizer customizer) {
        this.customizer = customizer;
    }

    public String validate(ExternalReferenceNode node) {
        if (node.hasModel()) {
            Model model = node.getModel();
            if (model == null) {
                // If it is supposed to have a model, it must not be null.
                return NbBundle.getMessage(SchemaReferenceDecorator.class,
                        "LBL_SchemaReferenceDecorator_NoModel");
            }
            Model componentModel = customizer.getComponentModel();
            if (model.equals(componentModel)) {
                return NbBundle.getMessage(SchemaReferenceDecorator.class,
                        "LBL_SchemaReferenceDecorator_SameModel");
            }
            // It had better be a schema model, but check anyway.
            if (componentModel instanceof SchemaModel) {
                SchemaModel sm = (SchemaModel) componentModel;
                Collection<SchemaModelReference> references =
                        sm.getSchema().getSchemaReferences();
                // Ensure the selected document is not already among the
                // set that have been included.
                for (SchemaModelReference ref : references) {
                    try {
                        SchemaModel otherModel = ref.resolveReferencedModel();
                        if (model.equals(otherModel)) {
                            return NbBundle.getMessage(SchemaReferenceDecorator.class,
                                    "LBL_SchemaReferenceDecorator_AlreadyRefd");
                        }
                    } catch (CatalogModelException cme) {
                        // Ignore that one as it does not matter.
                    }
                }
            }
        }
        String ns = node.getNamespace();
        String namespace = customizer.getTargetNamespace();
        if (customizer.mustNamespaceDiffer()) {
            // This is an import, which must have no namespace, or a
            // different one than the customized component.
            if (ns != null && !Utilities.NO_NAME_SPACE.equals(ns) &&
                    namespace != null && namespace.equals(ns)) {
                return NbBundle.getMessage(SchemaReferenceDecorator.class,
                        "LBL_SchemaReferenceDecorator_SameNamespace");
            }
        } else {
            // This is an include or redefine, which must have no namespace,
            // or the same one as the customized component.
            if (ns != null && !Utilities.NO_NAME_SPACE.equals(ns) &&
                    namespace != null && !namespace.equals(ns)) {
                return NbBundle.getMessage(SchemaReferenceDecorator.class,
                        "LBL_SchemaReferenceDecorator_DifferentNamespace");
            }
        }
        return null;
    }

    public ExternalReferenceDataNode createExternalReferenceNode(Node original) {
        return customizer.createExternalReferenceNode(original);
    }

    public String generatePrefix(ExternalReferenceNode node) {
        if (node.hasModel()) {
            Model model = node.getModel();
            if (model != null && model instanceof SchemaModel) {
                return NameGenerator.getInstance().generateNamespacePrefix(
                        null, (SchemaModel) model);
            }
        }
        return "";
    }

    public Utilities.DocumentTypesEnum getDocumentType() {
        return Utilities.DocumentTypesEnum.schema;
    }

    public String getHtmlDisplayName(String name, ExternalReferenceNode node) {
        if (validate(node) != null) {
            return "<s>" + name + "</s>";
        }
        return name;
    }

    public String getNamespace(Model model) {
        if (model instanceof SchemaModel) {
            SchemaModel sm = (SchemaModel) model;
            Schema schema = sm.getSchema();
            if (schema != null) {
                return schema.getTargetNamespace();
            }
        }
        return null;
    }
}
