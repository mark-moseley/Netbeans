/*
 * ErrorInfoPanel.java
 *
 * Created on September 10, 2008, 9:51 AM
 */

package org.netbeans.modules.db.util;

import java.awt.CardLayout;
import java.awt.Color;
import java.util.ResourceBundle;
import org.openide.util.NbBundle;

/**
 *
 * @author  rob
 */
public class ErrorInfoPanel extends javax.swing.JPanel {
    
    private static final String EMPTYCARD = "emptyCard";  //NOI18N
    private static final String CONTENTCARD = "contentCard";  //NOI18N
    private static final String BUNDLE = "org.netbeans.modules.db.resources.Bundle"; //NOI18N

    private static ResourceBundle bundle() {
        return NbBundle.getBundle(BUNDLE);
    }

    /** Creates new form ErrorInfoPanel */
    public ErrorInfoPanel() {
        initComponents();
        
        add(emptyPanel, EMPTYCARD);
    }

    public void clear()
    {
        ((CardLayout)getLayout()).show(this, EMPTYCARD);
    }
    
    public void setError(String msg)
    {
        ((CardLayout)getLayout()).show(this, CONTENTCARD);
        msgLabel.setText(msg);
        msgLabel.setForeground(Color.RED);
        iconLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource(bundle().getString("ErrorInfo_ErrorIcon"))));  //NOI18N        
    }
    
    public void setInfo(String msg)
    {
        ((CardLayout)getLayout()).show(this, CONTENTCARD);
        msgLabel.setText(msg);
        msgLabel.setForeground(Color.BLACK);
        iconLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource(bundle().getString("ErrorInfo_InfoIcon"))));  //NOI18N       
    }
    
    public void set(String msg, boolean isError)
    {
        if (isError)
        {
            setError(msg);
        }
        else
        {
            setInfo(msg);
        }
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        emptyPanel = new javax.swing.JPanel();
        contentPanel = new javax.swing.JPanel();
        iconLabel = new javax.swing.JLabel();
        msgLabel = new javax.swing.JLabel();

        org.jdesktop.layout.GroupLayout emptyPanelLayout = new org.jdesktop.layout.GroupLayout(emptyPanel);
        emptyPanel.setLayout(emptyPanelLayout);
        emptyPanelLayout.setHorizontalGroup(
            emptyPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 278, Short.MAX_VALUE)
        );
        emptyPanelLayout.setVerticalGroup(
            emptyPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 18, Short.MAX_VALUE)
        );

        setLayout(new java.awt.CardLayout());

        iconLabel.setText(org.openide.util.NbBundle.getMessage(ErrorInfoPanel.class, "ErrorInfoPanel.iconLabel.text")); // NOI18N

        msgLabel.setText(org.openide.util.NbBundle.getMessage(ErrorInfoPanel.class, "ErrorInfoPanel.msgLabel.text")); // NOI18N

        org.jdesktop.layout.GroupLayout contentPanelLayout = new org.jdesktop.layout.GroupLayout(contentPanel);
        contentPanel.setLayout(contentPanelLayout);
        contentPanelLayout.setHorizontalGroup(
            contentPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(contentPanelLayout.createSequentialGroup()
                .add(iconLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 20, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(msgLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 372, Short.MAX_VALUE))
        );
        contentPanelLayout.setVerticalGroup(
            contentPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(contentPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                .add(iconLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 16, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(msgLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 16, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );

        add(contentPanel, "contentCard");
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel contentPanel;
    private javax.swing.JPanel emptyPanel;
    private javax.swing.JLabel iconLabel;
    private javax.swing.JLabel msgLabel;
    // End of variables declaration//GEN-END:variables
    
}
