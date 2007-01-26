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

/*
 * BindingSubTypePanel.java
 *
 * Created on August 30, 2006, 7:04 PM
 */

package org.netbeans.modules.xml.wsdl.ui.view;

import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import org.netbeans.modules.xml.wsdl.ui.view.wizard.ExtensibilityElementTemplateFactory;

import org.netbeans.modules.xml.wsdl.ui.view.wizard.TemplateGroup;
import org.netbeans.modules.xml.wsdl.ui.view.wizard.TemplateType;
import org.netbeans.modules.xml.wsdl.ui.view.wizard.localized.LocalizedTemplate;
import org.netbeans.modules.xml.wsdl.ui.view.wizard.localized.LocalizedTemplateGroup;

/**
 *
 * @author  skini
 */
public class BindingSubTypePanel extends javax.swing.JPanel {
    
    private String namespace;
    private TemplateGroup group;
    
    private LocalizedTemplateGroup mLtg;
    
    private List<TemplatePanel> mPanels = new ArrayList();
    
    private ActionListener mButtonActionListener;
    
    /** Creates new form BindingSubTypePanel */
    public BindingSubTypePanel(LocalizedTemplateGroup ltg, ActionListener buttonActionListener) {
        this.mLtg = ltg;
        this.mButtonActionListener = buttonActionListener;
        initComponents();
        initGUI();
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        buttonGroup1 = new javax.swing.ButtonGroup();

        setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.Y_AXIS));

    }// </editor-fold>//GEN-END:initComponents
   
    private void initGUI() {
        boolean isDefaultAvailable = false;
        
        LocalizedTemplate[] templates = this.mLtg.getTemplate();
        for (int i = 0; i < templates.length; i++) {
            LocalizedTemplate template = templates[i];
            TemplatePanel panel = new TemplatePanel(template, buttonGroup1);
            panel.getRadioButton().addActionListener(this.mButtonActionListener);
            mPanels.add(panel);
            this.add(panel);
            if(template.getDelegate().isDefault()) {
                setSelectedTemplateName(templates[i].getName());
                isDefaultAvailable = true;
            }
        }
        if (!isDefaultAvailable && templates.length > 0) {
            setSelectedTemplateName(templates[0].getName());
        }
    }
        
    public LocalizedTemplate getBindingSubType() {
        Iterator<TemplatePanel> it = this.mPanels.iterator();
        while(it.hasNext()) {
            TemplatePanel p = it.next();
            if(p.isSelected()) {
                return p.getTemplate();
            }
        }
        return null;
    }
    
    public void reset(LocalizedTemplateGroup ltg) {
        this.removeAll();
        Iterator<TemplatePanel> it =  mPanels.iterator();
        while(it.hasNext()) {
        	TemplatePanel panel = it.next();
        	panel.getRadioButton().removeActionListener(this.mButtonActionListener);
        	
        }
        mPanels.clear();
        
        this.mLtg = ltg;
        initGUI();
        revalidate();
    }
    
    public void setSelectedTemplateName(String templateName) {
        Enumeration<AbstractButton> buttons = buttonGroup1.getElements();
        if (buttons != null) {
            while (buttons.hasMoreElements()) {
                AbstractButton button = buttons.nextElement();
                if (button.getActionCommand().equals(templateName)) {
                    button.setSelected(true);
                }
            }
        }
    }
    
    public ButtonGroup getButtonGroup() {
        return this.buttonGroup1;
    }
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    // End of variables declaration//GEN-END:variables
    
}
