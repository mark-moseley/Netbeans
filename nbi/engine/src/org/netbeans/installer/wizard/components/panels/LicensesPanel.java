/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU General
 * Public License Version 2 only ("GPL") or the Common Development and Distribution
 * License("CDDL") (collectively, the "License"). You may not use this file except in
 * compliance with the License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html or nbbuild/licenses/CDDL-GPL-2-CP. See the
 * License for the specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header Notice in
 * each file and include the License file at nbbuild/licenses/CDDL-GPL-2-CP.  Sun
 * designates this particular file as subject to the "Classpath" exception as
 * provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the License Header,
 * with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original Software
 * is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun Microsystems, Inc. All
 * Rights Reserved.
 * 
 * If you wish your version of this file to be governed by only the CDDL or only the
 * GPL Version 2, indicate your decision by adding "[Contributor] elects to include
 * this software in this distribution under the [CDDL or GPL Version 2] license." If
 * you do not indicate a single choice of license, a recipient has the option to
 * distribute your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above. However, if
 * you add GPL Version 2 code and therefore, elected the GPL Version 2 license, then
 * the option applies only if the new code is made subject to such option by the
 * copyright holder.
 */

package org.netbeans.installer.wizard.components.panels;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.LinkedList;
import java.util.List;
import javax.swing.BoundedRangeModel;
import javax.swing.JComponent;
import javax.swing.JScrollBar;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.installer.product.components.Product;
import org.netbeans.installer.product.Registry;
import org.netbeans.installer.utils.StringUtils;
import org.netbeans.installer.utils.ErrorManager;
import org.netbeans.installer.utils.SystemUtils;
import org.netbeans.installer.utils.exceptions.InitializationException;
import org.netbeans.installer.utils.helper.swing.NbiCheckBox;
import org.netbeans.installer.utils.helper.swing.NbiScrollPane;
import org.netbeans.installer.utils.helper.swing.NbiTextPane;
import org.netbeans.installer.utils.helper.Text;
import org.netbeans.installer.utils.ResourceUtils;
import org.netbeans.installer.wizard.ui.SwingUi;
import org.netbeans.installer.wizard.ui.WizardUi;
import org.netbeans.installer.wizard.components.WizardPanel;
import org.netbeans.installer.wizard.components.WizardPanel.WizardPanelSwingUi;
import org.netbeans.installer.wizard.components.WizardPanel.WizardPanelUi;
import org.netbeans.installer.wizard.containers.SwingContainer;

public class LicensesPanel extends WizardPanel {
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    public LicensesPanel() {
        setProperty(TITLE_PROPERTY,
                DEFAULT_TITLE);
        setProperty(DESCRIPTION_PROPERTY,
                DEFAULT_DESCRIPTION);
        
        setProperty(ACCEPT_CHECKBOX_TEXT_PROPERTY,
                DEFAULT_ACCEPT_CHECKBOX_TEXT);
        setProperty(ERROR_CANNOT_GET_LOGIC_PROPERTY,
                DEFAULT_ERROR_CANNOT_GET_LOGIC);
        setProperty(APPEND_LICENSE_FORMAT_PROPERTY, 
                DEFAULT_APPEND_LICENSE_FORMAT);
        setProperty(SINGLE_PRODUCT_LICENSE_FORMAT_PROPERTY,
                DEFAULT_SINGLE_PRODUCT_LICENSE_FORMAT);
        setProperty(OVERALL_LICENSE_FORMAT_PROPERTY, 
                DEFAULT_OVERALL_LICENSE_FORMAT);
    }
    
    @Override
    public boolean canExecuteForward() {
        return Registry.getInstance().getProductsToInstall().size()  > 0;
    }
    
    @Override
    public boolean canExecuteBackward() {
        return Registry.getInstance().getProductsToInstall().size()  > 0;
    }
    
    @Override
    public WizardUi getWizardUi() {
        if (wizardUi == null) {
            wizardUi = new LicensesPanelUi(this);
        }
        
        return wizardUi;
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Inner Classes
    public static class LicensesPanelUi extends WizardPanelUi {
        protected LicensesPanel component;
        
        public LicensesPanelUi(LicensesPanel component) {
            super(component);
            
            this.component = component;
        }
        
        @Override
        public SwingUi getSwingUi(SwingContainer container) {
            if (swingUi == null) {
                swingUi = new LicensesPanelSwingUi(component, container);
            }
            
            return super.getSwingUi(container);
        }
    }
    
    public static class LicensesPanelSwingUi extends WizardPanelSwingUi {
        protected LicensesPanel component;
        
        private List<Product> acceptedProducts;
        
        private NbiTextPane licensePane;
        private NbiScrollPane licenseScrollPane;
        
        private NbiCheckBox acceptCheckBox;
        
        public LicensesPanelSwingUi(
                final LicensesPanel component,
                final SwingContainer container) {
            super(component, container);
            
            this.component = component;
            this.acceptedProducts = new LinkedList<Product>();
            
            initComponents();
        }
        
        @Override
        public JComponent getDefaultFocusOwner() {
            return acceptCheckBox;
        }
        
        // protected ////////////////////////////////////////////////////////////////
        @Override
        protected void initialize() {
            acceptCheckBox.setText(
                    component.getProperty(ACCEPT_CHECKBOX_TEXT_PROPERTY));
            
            final List<Product> currentProducts =
                    Registry.getInstance().getProductsToInstall();
            
            final StringBuilder text = new StringBuilder();
            
            boolean everythingAccepted = true;
            
            if(System.getProperty(OVERALL_LICENSE_RESOURCE_PROPERTY)!=null) {
                if(acceptedProducts.size()==0) {
                    everythingAccepted = false;
                }
                final String licenseValue = SystemUtils.resolveString(
                        System.getProperty(OVERALL_LICENSE_RESOURCE_PROPERTY));
                final String license = SystemUtils.resolveString("$R{" + licenseValue + ";" + StringUtils.ENCODING_UTF8 + "}");
                final String format = component.getProperty(OVERALL_LICENSE_FORMAT_PROPERTY);
                text.append(StringUtils.format(format, license));
            } else {
                final String format = (currentProducts.size() == 1) ? 
                    component.getProperty(SINGLE_PRODUCT_LICENSE_FORMAT_PROPERTY) :
                    component.getProperty(APPEND_LICENSE_FORMAT_PROPERTY);
                
                for (Product product: currentProducts) {
                    if (!acceptedProducts.contains(product)) {
                        everythingAccepted = false;
                    }
                    try {
                        Text license = product.getLogic().getLicense();
                        if(license!=null) {
                            text.append(
                                    StringUtils.format(format, 
                                    product.getDisplayName(),
                                    license.getText()));                            
                        }
                    } catch (InitializationException e) {
                        ErrorManager.notifyError(
                                component.getProperty(ERROR_CANNOT_GET_LOGIC_PROPERTY),
                                e);
                    }                    
                }
            }
            licensePane.setText(text);
            licensePane.setCaretPosition(0);
            licensePane.requestFocus();

            licensePane.addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    final int code = e.getKeyCode();
                    if (code == KeyEvent.VK_SPACE || code == KeyEvent.VK_ENTER) {
                        BoundedRangeModel brm = licenseScrollPane.getVerticalScrollBar().getModel();
                        brm.setValue(brm.getValue() + brm.getExtent());
                    } else if (code == KeyEvent.VK_N || code == KeyEvent.VK_Q) {
                        container.getCancelButton().doClick();
                    } else if (code == KeyEvent.VK_A || code == KeyEvent.VK_Y) {
                        if (acceptCheckBox.isEnabled()) {
                            acceptCheckBox.setSelected(true);
                            acceptCheckBoxToggled();
                        }
                    }
                }
            });

            if (System.getProperty(WHOLE_LICENSE_SCROLLING_REQUIRED) != null) {
                licenseScrollPane.getVerticalScrollBar().getModel().addChangeListener(new ChangeListener() {

                    public void stateChanged(ChangeEvent e) {
                        JScrollBar vsb = licenseScrollPane.getVerticalScrollBar();
                        if (vsb.getValue() >= vsb.getMaximum() - vsb.getModel().getExtent()) {
                            acceptCheckBox.setEnabled(true);
                        }
                    }
                });
                acceptCheckBox.setEnabled(false);
            }
            
            if (!everythingAccepted) {
                acceptCheckBox.setSelected(false);
            }
            acceptCheckBoxToggled();
        }
        
        // private //////////////////////////////////////////////////////////////////
        private void initComponents() {
            // licensePane //////////////////////////////////////////////////////////
            licensePane = new NbiTextPane();
            licensePane.setOpaque(true);
            licensePane.setBackground(Color.WHITE);
            licensePane.setBorder(new EmptyBorder(new Insets(5, 5, 5, 5)));
            
            // licenseScrollPane ////////////////////////////////////////////////////
            licenseScrollPane = new NbiScrollPane(licensePane);
            
            // acceptCheckBox ///////////////////////////////////////////////////////
            acceptCheckBox = new NbiCheckBox();
            acceptCheckBox.setSelected(false);
            acceptCheckBox.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    acceptCheckBoxToggled();
                }
            });
            
            // this /////////////////////////////////////////////////////////////////
            add(licenseScrollPane, new GridBagConstraints(
                    0, 0,                             // x, y
                    1, 1,                             // width, height
                    1.0, 1.0,                         // weight-x, weight-y
                    GridBagConstraints.LINE_START,    // anchor
                    GridBagConstraints.BOTH,          // fill
                    new Insets(11, 11, 0, 11),        // padding
                    0, 0));                           // padx, pady - ???
            add(acceptCheckBox, new GridBagConstraints(
                    0, 2,                             // x, y
                    1, 1,                             // width, height
                    1.0, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.LINE_START,    // anchor
                    GridBagConstraints.HORIZONTAL,    // fill
                    new Insets(7, 11, 11, 11),        // padding
                    0, 0));                           // padx, pady - ???
        }
        
        private void acceptCheckBoxToggled() {
            if (acceptCheckBox.isSelected()) {
                for (Product product: Registry.
                        getInstance().getProductsToInstall()) {
                    if (!acceptedProducts.contains(product)) {
                        acceptedProducts.add(product);
                    }
                }
                
                container.getNextButton().setEnabled(true);
            } else {
                container.getNextButton().setEnabled(false);
            }
        }
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    public static final String ACCEPT_CHECKBOX_TEXT_PROPERTY =
            "accept.checkbox.text"; // NOI18N
    public static final String ERROR_CANNOT_GET_LOGIC_PROPERTY =
            "error.cannot.get.logic";//NOI18N
    public static final String OVERALL_LICENSE_RESOURCE_PROPERTY =
            "nbi.overall.license.resource";//NOI18N
    public static final String APPEND_LICENSE_FORMAT_PROPERTY =
            "append.license.format";//NOI18N
    public static final String OVERALL_LICENSE_FORMAT_PROPERTY =
            "overall.license.format";//NOI18N
    public static final String SINGLE_PRODUCT_LICENSE_FORMAT_PROPERTY =
            "single.product.license.format";//NOI18N
    private static final String WHOLE_LICENSE_SCROLLING_REQUIRED =
            "nbi.whole.license.scrolling.required";
    public static final String DEFAULT_TITLE =
            ResourceUtils.getString(LicensesPanel.class,
            "LP.title"); // NOI18N
    public static final String DEFAULT_DESCRIPTION =
            ResourceUtils.getString(LicensesPanel.class,
            "LP.description"); // NOI18N
    public static final String DEFAULT_APPEND_LICENSE_FORMAT =
            ResourceUtils.getString(LicensesPanel.class,
            "LP.append.license.format"); // NOI18N
    public static final String DEFAULT_OVERALL_LICENSE_FORMAT =
            ResourceUtils.getString(LicensesPanel.class,
            "LP.overall.license.format"); // NOI18N
    public static final String DEFAULT_SINGLE_PRODUCT_LICENSE_FORMAT =
            ResourceUtils.getString(LicensesPanel.class,
            "LP.single.product.license.format"); // NOI18N
    public static final String DEFAULT_ACCEPT_CHECKBOX_TEXT =
            ResourceUtils.getString(LicensesPanel.class,
            "LP.accept.checkbox.text"); // NOI18N
    public static final String DEFAULT_ERROR_CANNOT_GET_LOGIC =
            ResourceUtils.getString(LicensesPanel.class,
            "LP.error.cannot.get.logic"); // NOI18N
}
