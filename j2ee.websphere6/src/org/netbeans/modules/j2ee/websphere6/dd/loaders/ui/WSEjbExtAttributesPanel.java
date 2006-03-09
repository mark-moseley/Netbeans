/*
 * WSEjbExtAttributesPanel.java
 *
 */

package org.netbeans.modules.j2ee.websphere6.dd.loaders.ui;
import org.netbeans.modules.j2ee.websphere6.dd.beans.DDXmiConstants;
import org.netbeans.modules.xml.multiview.*;
import org.netbeans.modules.xml.multiview.ui.*;
import org.netbeans.modules.xml.multiview.Error;
import org.netbeans.modules.j2ee.websphere6.dd.loaders.ejbext.*;
import org.netbeans.modules.j2ee.websphere6.dd.beans.WSEjbExt;
/**
 *
 * @author  dlm198383
 */
public class WSEjbExtAttributesPanel extends SectionInnerPanel  {
    
    /** Creates new form WSEjbExtAttributesPanel */
    WSEjbExt ejbext;
    WSEjbExtDataObject dObj;
    
    public WSEjbExtAttributesPanel(SectionView view, WSEjbExtDataObject dObj,  WSEjbExt ejbext) {
        super(view);
        this.dObj=dObj;
        this.ejbext=ejbext;
        initComponents();
        nameField.setText(ejbext.getXmiId());        
        hrefField.setText(ejbext.getEjbJarHref());
        addModifier(nameField);
        addModifier(hrefField);
    }
    
    public void setValue(javax.swing.JComponent source,Object value) {
        if (source==nameField) {
            ejbext.setXmiId((String)value);
        } else if (source==hrefField) {            
            ejbext.setEjbJarHref((String)value);
        }
    }
    public void linkButtonPressed(Object ddBean, String ddProperty) {
    }
    public javax.swing.JComponent getErrorComponent(String errorId) {
        if ("ID".equals(errorId)) return nameField;
        if ("HREF".equals(errorId)) return hrefField;
        return null;
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
                .setError(new Error(Error.MISSING_VALUE_MESSAGE, "HREF", comp));
                return;
            }
            getSectionView().getErrorPanel().clearError();
        }
        
    }
    
    protected void startUIChange() {
        dObj.setChangedFromUI(true);
    }
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
        nameField = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        hrefField = new javax.swing.JTextField();

        jLabel1.setText("Name:");

        jLabel2.setText("ID:");

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(jLabel2)
                    .add(jLabel1))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(hrefField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 472, Short.MAX_VALUE)
                    .add(nameField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 472, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(nameField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel1))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel2)
                    .add(hrefField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField hrefField;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JTextField nameField;
    // End of variables declaration//GEN-END:variables
    
}
