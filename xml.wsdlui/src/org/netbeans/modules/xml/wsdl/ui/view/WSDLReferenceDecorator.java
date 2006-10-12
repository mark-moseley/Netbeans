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

package org.netbeans.modules.xml.wsdl.ui.view;

import java.util.Collection;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.xml.retriever.catalog.Utilities;
import org.netbeans.modules.xml.wsdl.model.Import;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.ui.wsdl.nodes.ImportViewNodes;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.locator.CatalogModelException;
import org.netbeans.modules.xml.xam.ui.customizer.ExternalReferenceCustomizer;
import org.netbeans.modules.xml.xam.ui.customizer.ExternalReferenceDecorator;
import org.netbeans.modules.xml.xam.ui.customizer.ExternalReferenceNode;
import org.openide.util.NbBundle;

/**
 * The ExternalReferenceDecorator for WSDL components.
 *
 * @author  Nathan Fiedler
 */
public class WSDLReferenceDecorator implements ExternalReferenceDecorator {
    /** The customizer that created this decorator. */
    private ExternalReferenceCustomizer customizer;

    /**
     * Creates a new instance of WSDLReferenceDecorator.
     *
     * @param  customizer  provides information about the edited component.
     */
    public WSDLReferenceDecorator(ExternalReferenceCustomizer customizer) {
        this.customizer = customizer;
    }

    public String annotate(ExternalReferenceNode node) {
        if (node.hasModel()) {
            Model model = node.getModel();
            if (model == null) {
                // If it is supposed to have a model, it must not be null.
                return NbBundle.getMessage(WSDLReferenceDecorator.class,
                        "LBL_ReferenceDecorator_NoModel");
            }
            Model componentModel = customizer.getComponentModel();
            if (model.equals(componentModel)) {
                return NbBundle.getMessage(WSDLReferenceDecorator.class,
                        "LBL_ReferenceDecorator_SameModel");
            }
            // It had better be a WSDL model, but check anyway.
            if (componentModel instanceof WSDLModel) {
                WSDLModel sm = (WSDLModel) componentModel;
                Collection<Import> references =
                        sm.getDefinitions().getImports();
                // Ensure the selected document is not already among the
                // set that have been included.
                for (Import ref : references) {
                    try {
                        WSDLModel otherModel = ref.getImportedWSDLModel();
                        if (model.equals(otherModel)) {
                            return NbBundle.getMessage(WSDLReferenceDecorator.class,
                                    "LBL_ReferenceDecorator_AlreadyRefd");
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
                    namespace.equals(ns)) {
                return NbBundle.getMessage(WSDLReferenceDecorator.class,
                        "LBL_ReferenceDecorator_SameNamespace");
            }
        } else {
            // This is an include or redefine, which must have no namespace,
            // or the same one as the customized component.
            if (ns != null && !Utilities.NO_NAME_SPACE.equals(ns) &&
                    !namespace.equals(ns)) {
                return NbBundle.getMessage(WSDLReferenceDecorator.class,
                        "LBL_ReferenceDecorator_DifferentNamespace");
            }
        }
        ImportViewNodes.DuplicateFileCookie cookie =
                (ImportViewNodes.DuplicateFileCookie) node.getCookie(
                ImportViewNodes.DuplicateFileCookie.class);
        if (cookie != null) {
            Project ownerProject = cookie.getPrecedingOwnerProject();
            String projectName = "";
            if (ownerProject != null) {
                projectName = ProjectUtils.getInformation(ownerProject).getDisplayName();
            }
            String fileName = cookie.getFileName();
            return NbBundle.getMessage(WSDLReferenceDecorator.class,
                    "LBL_ReferenceDecorator_DupeInDependentProject",
                    fileName, projectName);
        }
        return null;
    }

    public Utilities.DocumentTypesEnum getDocumentType() {
        return Utilities.DocumentTypesEnum.wsdl;
    }

    public String getHtmlDisplayName(String name, ExternalReferenceNode node) {
        String ns = node.getNamespace();
        boolean exclude = customizer.mustNamespaceDiffer();
        String namespace = customizer.getTargetNamespace();
        if (ns != null && !Utilities.NO_NAME_SPACE.equals(ns) &&
                exclude == ns.equals(namespace)) {
            return "<s>" + name + "</s>";
        }
        return name;
    }

    public String getNamespace(Model model) {
        if (model instanceof WSDLModel) {
            WSDLModel wm = (WSDLModel) model;
            return wm.getDefinitions().getTargetNamespace();
        }
        return null;
    }
}
