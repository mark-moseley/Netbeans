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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.visualweb.propertyeditors.binding.data;

import com.sun.rave.designtime.DesignBean;
import com.sun.rave.designtime.DesignContext;
import com.sun.rave.designtime.faces.FacesDesignProperty;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.JLabel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

import com.sun.rave.designtime.DesignProperty;
import com.sun.rave.designtime.faces.FacesDesignContext;
import org.netbeans.modules.visualweb.propertyeditors.binding.BindingTargetCallback;
import org.netbeans.modules.visualweb.propertyeditors.util.Bundle;

import com.sun.data.provider.DataProvider;
import com.sun.rave.designtime.DesignProject;
import com.sun.rave.designtime.faces.FacesDesignProject;

public class TabbedDataBindingPanel extends DataBindingPanel implements BindingTargetCallback {

    private static final Bundle bundle = Bundle.getBundle(TabbedDataBindingPanel.class);

    protected JTabbedPane tabs = new JTabbedPane();

    protected JTextField valueTextField = new JTextField(70); // resonable number of columns for value field to limit the preferred size
    protected GridBagLayout gridBagLayout1 = new GridBagLayout();
    protected JLabel valueLabel = new JLabel();

    public String getDataBindingTitle() {
        return bundle.getMessage("bindToData"); // NOI18N
    }

    public void refresh() {
        this.validate();
        this.doLayout();
        this.repaint(100);
        bindingCallback.refresh();
    }

    public void setNewExpressionText(String newExpr) {
        if (initializing) {
            return;
        }
        valueTextField.setText(newExpr);
        bindingCallback.setNewExpressionText(newExpr);
    }

    public String getNewExpressionText() {
        return valueTextField.getText();
    }

    private static Class[] BINDING_PANEL_CONSTRUCTOR_SIG = new Class[] {
        BindingTargetCallback.class,
                DesignProperty.class
    };

    public TabbedDataBindingPanel(BindingTargetCallback callback, DesignProperty prop, Class[] bindingPanelClasses, boolean showExpr) {
        super(callback, prop);

        // find the current value
        valueTextField.setText(prop.getValueSource());
        valueLabel.setLabelFor(valueTextField);
        valueLabel.setDisplayedMnemonic(bundle.getMessage("VALUE_EXP_DISPLAYED_MNEMONIC").charAt(0));
        valueTextField.getAccessibleContext().setAccessibleName(bundle.getMessage("VALUE_EXP_ACCESS_NAME"));
        valueTextField.getAccessibleContext().setAccessibleDescription(bundle.getMessage("VALUE_EXP_ACCESS_DESC"));

        ArrayList bindingPanels = new ArrayList();
        if (bindingPanelClasses != null && bindingPanelClasses.length > 0) {
            for (int i = 0; i < bindingPanelClasses.length; i++) {
                try {
                    Constructor con = bindingPanelClasses[i].getConstructor(BINDING_PANEL_CONSTRUCTOR_SIG);
                    DataBindingPanel bp = (DataBindingPanel)con.newInstance(new Object[] { this, prop });
                    bindingPanels.add(bp);
                } catch (Exception x) {
                    x.printStackTrace();
                }
            }
        } else {
            bindingPanels.add(new BindValueToObjectPanel(this, prop));
        }

        this.setLayout(gridBagLayout1);
        String lbl_current = bundle.getMessage("LBL_Current");
        String lbl_setting = bundle.getMessage("LBL_Setting");
        valueLabel.setText(lbl_current + " " + designProperty.getPropertyDescriptor().getDisplayName() + " " + lbl_setting); //NOI18N
        if (showExpr && bindingPanels.size() > 0) {
            this.add(valueLabel,
                    new GridBagConstraints(0, 0, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER,
                    GridBagConstraints.HORIZONTAL, new Insets(8, 8, 2, 8), 0, 0));
            this.add(valueTextField, new GridBagConstraints(0, 1, 1, 1, 1.0, 0.0,
                    GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 8, 4, 8), 0, 0));
        }
        
        if (bindingPanels.size() > 1) {
            boolean shouldShowBindToObjectPanel = showBindToObjectPanel();
            this.add(tabs, new GridBagConstraints(0, 2, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER,
                    GridBagConstraints.BOTH, new Insets(0, 8, 8, 8), 0, 0));
            int selectedIndex = 0;
            for (int i = 0; i < bindingPanels.size(); i++) {
                DataBindingPanel bp = (DataBindingPanel)bindingPanels.get(i);
                tabs.add(bp, bp.getDataBindingTitle());                
                tabs.getAccessibleContext().setAccessibleDescription(bp.getDataBindingTitle());
                tabs.getAccessibleContext().setAccessibleName(bp.getDataBindingTitle());
                if (shouldShowBindToObjectPanel &&  bp.getClass().isAssignableFrom(BindValueToObjectPanel.class)){
                    selectedIndex = i;
                }
            }
            tabs.setSelectedIndex(selectedIndex);
        } else if (bindingPanels.size() > 0){
            DataBindingPanel bp = (DataBindingPanel)bindingPanels.get(0);
            this.add(bp, new GridBagConstraints(0, 2, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER,
                    GridBagConstraints.BOTH, new Insets(0, 8, 8, 8), 0, 0));
        }
        
        initializing = false;
    }
    
    // For performance improvement. No need to get all the contexts in the project
    private DesignContext[] getDesignContexts(DesignBean designBean){
        DesignProject designProject = designBean.getDesignContext().getProject();
        DesignContext[] contexts;
        if (designProject instanceof FacesDesignProject) {
            contexts = ((FacesDesignProject)designProject).findDesignContexts(new String[] {
                "request",
                "session",
                "application"
            });
        } else {
            contexts = new DesignContext[0];
        }
        DesignContext[] designContexts = new DesignContext[contexts.length + 1];
        designContexts[0] = designBean.getDesignContext();
        System.arraycopy(contexts, 0, designContexts, 1, contexts.length);
        return designContexts;
    }
    
    private boolean showBindToObjectPanel(){
        // Show the data provider panel by default
        String valueExpression = null;
        if(designProperty instanceof FacesDesignProperty){
            FacesDesignProperty fprop = (FacesDesignProperty)designProperty;
            if (fprop.isBound() && fprop.getValueBinding() != null) {
                valueExpression = fprop.getValueBinding().getExpressionString();
            }
        }else{
            valueExpression = designProperty.getValueSource();
        }
        if(valueExpression != null){
            boolean dataProviderBound = false;
            //DesignContext[] contexts = designProperty.getDesignBean().getDesignContext().getProject().getDesignContexts();
            DesignContext[] contexts =  getDesignContexts(designProperty.getDesignBean());
            
            // Scan for all data providers
            for (int i = 0; i < contexts.length; i++) {
                DesignBean[] dpbs = contexts[i].getBeansOfType(DataProvider.class);
                for (int j = 0; j < dpbs.length; j++) {
                    String modelBindingExpr = ((FacesDesignContext)contexts[i]).getBindingExpr(dpbs[j]);
                    // Get only the qualified name
                    int startIndex = modelBindingExpr.indexOf('{');
                    int endIndex = modelBindingExpr.indexOf('}');
                    if ((startIndex != -1) && (endIndex != -1)){
                        modelBindingExpr =  modelBindingExpr.substring(startIndex + 2, endIndex - 1);
                    }
                    if(valueExpression.indexOf(modelBindingExpr) != -1){
                        dataProviderBound = true;
                        break;
                    }
                }
            }
            
            // Value bound but not to databound so assume bound to object
            if(!dataProviderBound) return true;
        }
        return false;
    }
    
    protected boolean initializing = true;
}
