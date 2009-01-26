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
package org.netbeans.modules.cnd.refactoring.codegen.ui;

import java.util.List;
import javax.swing.JPanel;
import org.netbeans.modules.cnd.api.model.CsmField;
import org.netbeans.modules.cnd.refactoring.support.GeneratorUtils;
import org.netbeans.modules.cnd.refactoring.codegen.GetterSetterGenerator;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;

/**
 *
 * @author  Dusan Balek
 * @author Vladimir Voskresensky
 */
public class GetterSetterPanel extends JPanel {

    private final ElementSelectorPanel elementSelector;

    /** Creates new form GetterSetterPanel */
    public GetterSetterPanel(ElementNode.Description description, GeneratorUtils.Kind type) {
        initComponents();
        elementSelector = new ElementSelectorPanel(description, false, true);
        java.awt.GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 0, 12);
        add(elementSelector, gridBagConstraints);
        if (type == GeneratorUtils.Kind.GETTERS_ONLY) {
            Mnemonics.setLocalizedText(selectorLabel, NbBundle.getMessage(GetterSetterGenerator.class, "LBL_getter_field_select")); //NOI18N
        } else if (type == GeneratorUtils.Kind.SETTERS_ONLY) {
            Mnemonics.setLocalizedText(selectorLabel, NbBundle.getMessage(GetterSetterGenerator.class, "LBL_setter_field_select")); //NOI18N
        } else {
            Mnemonics.setLocalizedText(selectorLabel, NbBundle.getMessage(GetterSetterGenerator.class, "LBL_getter_and_setter_field_select")); //NOI18N
        }
        selectorLabel.setLabelFor(elementSelector);

        elementSelector.doInitialExpansion(1);
        this.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(GetterSetterGenerator.class, "A11Y_Generate_GetterSetter")); // NOI18N
    }

    public List<CsmField> getVariables() {
        List<?> decls = elementSelector.getSelectedElements();
        // we know that list contains only fields
        @SuppressWarnings("unchecked")
        List<CsmField> fields = (List<CsmField>) decls;
        return fields;
    }

    public final boolean isMethodInline() {
        return elementSelector.isMethodInline();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        selectorLabel = new javax.swing.JLabel();

        setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 6, 12);
        add(selectorLabel, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel selectorLabel;
    // End of variables declaration//GEN-END:variables
}
