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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.vmd.midp.propertyeditors.resource.elements;

import java.util.Map;
import javax.swing.JComponent;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.PropertyValue;
import org.netbeans.modules.vmd.api.model.TypeID;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.midp.components.resources.TickerCD;

/**
 *
 * @author Anton Chechel
 */
public class TickerEditorElement extends PropertyEditorResourceElement implements DocumentListener {
    
    private long componentID;
    private boolean doNotFireEvent;
    
    public TickerEditorElement() {
        initComponents();
        tickerTextField.getDocument().addDocumentListener(this);
    }
    
    public JComponent getJComponent() {
        return this;
    }

    public TypeID getTypeID() {
        return TickerCD.TYPEID;
    }

    public void setDesignComponentWrapper(final DesignComponentWrapper wrapper) {
        if (wrapper == null) {
            // UI stuff
            setText(null);
            setAllEnabled(false);
            return;
        }
        
        this.componentID = wrapper.getComponentID();
        final String[] _tickerText = new String[1];

        final DesignComponent component = wrapper.getComponent();
        if (component != null) { // existing component
            if (!component.getType().equals(getTypeID())) {
                throw new IllegalArgumentException("Passed component must have typeID " + getTypeID() + " instead passed " + component.getType()); // NOI18N
            }

            this.componentID = component.getComponentID();
            component.getDocument().getTransactionManager().readAccess(new Runnable() {
                public void run() {
                    _tickerText[0] = MidpTypes.getString(component.readProperty(TickerCD.PROP_STRING));
                }
            });
        }

        if (wrapper.hasChanges()) {
            Map<String, PropertyValue> changes = wrapper.getChanges();
            for (String propertyName : changes.keySet()) {
                final PropertyValue propertyValue = changes.get(propertyName);
                if (TickerCD.PROP_STRING.equals(propertyName)) {
                    _tickerText[0] = MidpTypes.getString(propertyValue);
                }
            }
        }

        // UI stuff
        setAllEnabled(true);
        setText(_tickerText[0]);
    }

    private synchronized void setText(String text) {
        doNotFireEvent = true;
        tickerTextField.setText(text);
        doNotFireEvent = false;
    }
    
    private void setAllEnabled(boolean isEnabled) {
        tickerLabel.setEnabled(isEnabled);
        tickerTextField.setEnabled(isEnabled);
    }
    
    public void insertUpdate(DocumentEvent e) {
        textChanged();
    }

    public void removeUpdate(DocumentEvent e) {
        textChanged();
    }

    public void changedUpdate(DocumentEvent e) {
        textChanged();
    }
    
    private synchronized void textChanged() {
        if (isShowing() && !doNotFireEvent) {
            fireElementChanged(componentID, TickerCD.PROP_STRING, MidpTypes.createStringValue(tickerTextField.getText()));
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {

        tickerLabel = new javax.swing.JLabel();
        tickerTextField = new javax.swing.JTextField();

        tickerLabel.setLabelFor(tickerTextField);
        org.openide.awt.Mnemonics.setLocalizedText(tickerLabel, org.openide.util.NbBundle.getMessage(TickerEditorElement.class, "TickerEditorElement.tickerLabel.text")); // NOI18N
        tickerLabel.setEnabled(false);

        tickerTextField.setEnabled(false);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(2, 2, 2)
                .add(tickerLabel)
                .addContainerGap())
            .add(tickerTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 320, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(tickerLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(tickerTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(110, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel tickerLabel;
    private javax.swing.JTextField tickerTextField;
    // End of variables declaration//GEN-END:variables
    
}
