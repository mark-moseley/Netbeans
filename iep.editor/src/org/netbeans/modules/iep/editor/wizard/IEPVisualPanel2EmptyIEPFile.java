/*
 * IEPVisualPanel2EmptyIEPFile.java
 *
 * Created on January 7, 2008, 5:46 PM
 */

package org.netbeans.modules.iep.editor.wizard;

import org.openide.util.NbBundle;

/**
 *
 * @author  radval
 */
public class IEPVisualPanel2EmptyIEPFile extends javax.swing.JPanel {
    
    /** Creates new form IEPVisualPanel2EmptyIEPFile */
    public IEPVisualPanel2EmptyIEPFile() {
        initComponents();
    }
    
    @Override
    public String getName() {
        return NbBundle.getMessage(IEPVisualPanel2EmptyIEPFile.class, "IEPVisualPanel2EmptyIEPFile_title");
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 300, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
    
}
