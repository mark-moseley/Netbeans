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

package org.netbeans.modules.compapp.casaeditor.properties.extension;

import java.beans.PropertyEditor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaComponent;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaExtensibilityElement;
import org.netbeans.modules.compapp.casaeditor.nodes.CasaNode;
import org.netbeans.modules.compapp.casaeditor.properties.ComboBoxEditor;
import org.netbeans.modules.compapp.casaeditor.properties.spi.ExtensionProperty;

/**
 * Extension poperty of enumerated strings for <code>JbiChoiceExtensionElement</code>.
 *
 * @author jqian
 */
public class ChoiceExtensionProperty extends ExtensionProperty<String> {

    private List<String> choices;
    // a map of possible child extensibility elements keyed by the element names
    private Map<String, CasaExtensibilityElement> choiceMap;
    private CasaNode node;

    public ChoiceExtensionProperty(
            CasaNode node,
            CasaComponent extensionPointComponent,
            CasaExtensibilityElement firstEE,
            CasaExtensibilityElement lastEE,
            String propertyType,
            String propertyName,
            String displayName,
            String description,
            Map<String, CasaExtensibilityElement> choiceMap) {

        super(node, extensionPointComponent, firstEE, lastEE, propertyType,
                String.class, propertyName, displayName, description);

        this.node = node;
        this.choiceMap = choiceMap;

        choices = new ArrayList<String>();
        choices.addAll(choiceMap.keySet());
        Collections.sort(choices);
    }

    @Override
    public PropertyEditor getPropertyEditor() {
        return new ComboBoxEditor(choices.toArray(new String[]{}));
    }

    @SuppressWarnings("unchecked")
    @Override
    public String getValue()
            throws IllegalAccessException, InvocationTargetException {
        CasaExtensibilityElement casaEE =
                (CasaExtensibilityElement) getComponent(); // e.x., redelivery:on-failure        
        List<CasaExtensibilityElement> children =
                casaEE.getChildren(CasaExtensibilityElement.class);
        if (children != null && children.size() == 1) {
            return children.get(0).getQName().getLocalPart();
        }
        return "";
    }

    @Override
    public void setValue(String value)
            throws IllegalAccessException, IllegalArgumentException,
            InvocationTargetException {
        CasaExtensibilityElement lastEE =
                (CasaExtensibilityElement) getComponent(); // e.x., redelivery:on-failure

        if (firstEE.getParent() == null) { // e.x., firstEE: redelivery:redelivery
            // Purge the non-choice elements from the pre-built 
            // extensibility element tree.
            for (CasaExtensibilityElement ee : lastEE.getExtensibilityElements()) {
                lastEE.removeExtensibilityElement(ee);
            }

            // Add the choice element to the extensibility element tree.
            CasaExtensibilityElement ee = choiceMap.get(value.toString());
            assert ee != null : "Failed to find " + value + " from " + choiceMap.keySet();
            lastEE.addExtensibilityElement(
                    (CasaExtensibilityElement) ee.copy(lastEE));

            // The extensibility element does not exist in the CASA model yet.
            getModel().addExtensibilityElement(extensionPointComponent, firstEE);

        } else {
            // Purge the non-choice elements from the pre-built 
            // extensibility element tree.
            for (CasaExtensibilityElement ee : lastEE.getExtensibilityElements()) {
                getModel().removeExtensibilityElement(lastEE, ee);
            }

            // Add the choice element to the extensibility element tree.
            CasaExtensibilityElement ee = choiceMap.get(value.toString());
            assert ee != null : "Failed to find " + value + " from " + choiceMap.keySet();
            getModel().addExtensibilityElement(lastEE,
                    (CasaExtensibilityElement) ee.copy(lastEE));
        }

        // rebuild property sheet
        node.refresh();
    }
}
