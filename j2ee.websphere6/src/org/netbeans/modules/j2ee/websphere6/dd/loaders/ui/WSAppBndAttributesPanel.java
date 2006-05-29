/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.j2ee.websphere6.dd.loaders.ui;

import org.netbeans.modules.j2ee.websphere6.dd.beans.WSAppBnd;
import org.netbeans.modules.j2ee.websphere6.dd.loaders.WSMultiViewDataObject;
import org.netbeans.modules.xml.multiview.ui.SectionInnerPanel;
import org.netbeans.modules.xml.multiview.ui.SectionView;
import org.netbeans.modules.xml.multiview.Error;
/**
 *
 * @author  dlm198383
 */
public class WSAppBndAttributesPanel extends SectionInnerPanel {
    WSAppBnd appbnd;
    WSMultiViewDataObject dObj;
    /** Creates new form WSAppBndAttributesPanel */
    public WSAppBndAttributesPanel(SectionView view,WSMultiViewDataObject dObj,WSAppBnd appbnd) {
        super(view);
        this.dObj=dObj;
        this.appbnd=appbnd;
        
        initComponents();
        
        nameField.setText(appbnd.getXmiId());
        hrefField.setText(appbnd.getApplicationHref());
        authTableIdField.setText(appbnd.getAuthTableId());
        runAsMapField.setText(appbnd.getRunAsMapId());
        
        addModifier(hrefField);
        addModifier(nameField);
        addModifier(runAsMapField);
        addModifier(authTableIdField);
        
        addValidatee(hrefField);
        addValidatee(nameField);
        addValidatee(authTableIdField);
    }
    
    public void setValue(javax.swing.JComponent source,Object value) {
        if (source==nameField) {
            appbnd.setXmiId((String)value);
        } else if (source==hrefField) {
            appbnd.setApplicationHref((String)value);
        } else if(source==authTableIdField) {
            appbnd.setAuthTableId((String)value);
        } else if(source==runAsMapField) {
            appbnd.setRunAsMapId((String)value);
        }
    }
    
    public void linkButtonPressed(Object ddBean, String ddProperty) {
    }
    
    public javax.swing.JComponent getErrorComponent(String errorId) {
        if ("ID".equals(errorId)) return nameField;
        if ("Name".equals(errorId)) return hrefField;
        if ("Run as Roles Map Id".equals(errorId)) return runAsMapField;
        if ("Authorization Table Id".equals(errorId)) return authTableIdField;
        return null;
    }
    public void stateChanged(javax.swing.event.ChangeEvent evt) {
        dObj.modelUpdatedFromUI();
    }
    public void documentChanged(javax.swing.text.JTextComponent comp, String value) {
        if (comp==nameField) {
            String val = (String)value;
            if (val.length()==0) {
                getSectionView()
                .getErrorPanel()
                .setError(new Error(Error.MISSING_VALUE_MESSAGE, "ID", comp));
                return;
            }
            getSectionView().getErrorPanel().clearError();
        }
        if (comp==hrefField) {
            String val = (String)value;
            if (val.length()==0) {
                getSectionView()
                .getErrorPanel()
                .setError(new Error(Error.MISSING_VALUE_MESSAGE, "Name", comp));
                return;
            }
            getSectionView().getErrorPanel().clearError();
        }
        if (comp==authTableIdField) {
            String val = (String)value;
            if (val.length()==0) {
                getSectionView()
                .getErrorPanel()
                .setError(new Error(Error.MISSING_VALUE_MESSAGE, "Authorization Table Id", comp));
                return;
            }
            getSectionView().getErrorPanel().clearError();
        }
        if (comp==runAsMapField) {
            String val = (String)value;
            if (val.length()==0) {
                getSectionView()
                .getErrorPanel()
                .setError(new Error(Error.MISSING_VALUE_MESSAGE, "Run as Roles Map Id", comp));
                return;
            }
            getSectionView().getErrorPanel().clearError();
        }
        
        
        
    }
    
    public void rollbackValue(javax.swing.text.JTextComponent source) {
        if (nameField==source) {
            nameField.setText(appbnd.getXmiId());
        } else if(hrefField==source) {
            hrefField.setText(appbnd.getApplicationHref());
        } else if(authTableIdField==source) {
            authTableIdField.setText(appbnd.getAuthTableId());
        } else if(runAsMapField==source) {
            runAsMapField.setText(appbnd.getRunAsMapId());
        }
        
    }
    
    /** This will be called before model is changed from this panel
     */
    protected void startUIChange() {
        dObj.setChangedFromUI(true);
    }
    
    /** This will be called after model is changed from this panel
     */
    protected void endUIChange() {
        dObj.modelUpdatedFromUI();
        dObj.setChangedFromUI(false);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        jLabel1 = new javax.swing.JLabel();
        hrefField = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        nameField = new javax.swing.JTextField();
        authTableIdField = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        runAsMapField = new javax.swing.JTextField();

        jLabel1.setText("Name:");

        jLabel2.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/websphere6/dd/loaders/ui/Bundle").getString("LBL_Id"));

        jLabel3.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/websphere6/dd/loaders/ui/Bundle").getString("LBL_AuthTableId"));

        jLabel4.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/websphere6/dd/loaders/ui/Bundle").getString("LBL_RunAsMap"));

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(jLabel4)
                    .add(jLabel1)
                    .add(jLabel2)
                    .add(jLabel3))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(hrefField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 292, Short.MAX_VALUE)
                    .add(nameField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 292, Short.MAX_VALUE)
                    .add(authTableIdField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 292, Short.MAX_VALUE)
                    .add(runAsMapField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 292, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(nameField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel2))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(hrefField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel1))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(authTableIdField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel3))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel4)
                    .add(runAsMapField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField authTableIdField;
    private javax.swing.JTextField hrefField;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JTextField nameField;
    private javax.swing.JTextField runAsMapField;
    // End of variables declaration//GEN-END:variables
    
}
