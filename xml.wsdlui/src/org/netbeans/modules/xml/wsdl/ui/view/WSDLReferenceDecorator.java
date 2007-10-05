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

package org.netbeans.modules.xml.wsdl.ui.view;

import java.util.Collection;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.xml.retriever.catalog.Utilities;
import org.netbeans.modules.xml.wsdl.model.Import;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.ui.actions.NameGenerator;
import org.netbeans.modules.xml.wsdl.ui.wsdl.nodes.ImportViewNodes;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.locator.CatalogModelException;
import org.netbeans.modules.xml.xam.ui.customizer.AbstractReferenceCustomizer;
import org.netbeans.modules.xml.xam.ui.customizer.AbstractReferenceDecorator;
import org.netbeans.modules.xml.xam.ui.customizer.ExternalReferenceDataNode;
import org.netbeans.modules.xml.xam.ui.customizer.ExternalReferenceNode;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

/**
 * The ExternalReferenceDecorator for WSDL components.
 *
 * @author  Nathan Fiedler
 */
public class WSDLReferenceDecorator extends AbstractReferenceDecorator {
    /** The customizer that created this decorator. */
    private AbstractReferenceCustomizer customizer;
    /** Used to generate unique namespace prefixes. */
    private int prefixCounter;

    /**
     * Creates a new instance of WSDLReferenceDecorator.
     *
     * @param  customizer  provides information about the edited component.
     */
    public WSDLReferenceDecorator(AbstractReferenceCustomizer customizer) {
        this.customizer = customizer;
    }

    public String validate(ExternalReferenceNode node) {
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
                node.getCookie(ImportViewNodes.DuplicateFileCookie.class);
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

    public ExternalReferenceDataNode createExternalReferenceNode(Node original) {
        return customizer.createExternalReferenceNode(original);
    }

    protected String generatePrefix(Model model) {
        Model ourModel = customizer.getComponentModel();
        if (ourModel instanceof WSDLModel) {
            return NameGenerator.getInstance().generateNamespacePrefix(
                    null, (WSDLModel) ourModel, prefixCounter++);
        }
        return "";
    }

    public Utilities.DocumentTypesEnum getDocumentType() {
        return Utilities.DocumentTypesEnum.wsdl;
    }

    public String getHtmlDisplayName(String name, ExternalReferenceNode node) {
        if (validate(node) != null) {
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
