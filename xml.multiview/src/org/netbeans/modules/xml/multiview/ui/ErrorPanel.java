/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.xml.multiview.ui;

import javax.swing.UIManager;
import org.netbeans.modules.xml.multiview.Error;

/** ErrorPanel.java
 *
 * Created on November 19, 2004, 10:44 AM
 * @author  mkuchtiak
 */
public class ErrorPanel extends javax.swing.JPanel {
    
    private javax.swing.JButton errorButton;
    private javax.swing.JComponent focusableComponent;
    
    /** Creates new form ErrorPanel */
    public ErrorPanel() {
        initComponents();
        errorButton = new ErrorButton();
        errorButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                javax.swing.JComponent comp = ErrorPanel.this.getFocusableComponent();
                if (comp!=null) {
                    comp.requestFocus();
                    java.awt.Container cont = comp.getParent();
                    if (cont !=null ) cont = cont.getParent();
                    if (cont!=null && cont instanceof SectionPanel) {
                        ((SectionPanel)cont).open();
                        ((SectionPanel)cont).scroll();
                    }
                }
            }
        });
        add(errorButton,java.awt.BorderLayout.CENTER);
        
    }
    
    public javax.swing.JComponent getFocusableComponent() {
        return focusableComponent;
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents

        setLayout(new java.awt.BorderLayout());

    }//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
 
    
    public void setError(Error error) {
        switch (error.getErrorType()) {
            case Error.ERROR_MESSAGE : {
                errorButton.setText("Error: "+error.getErrorMessage());
                break;
            }
            case Error.WARNING_MESSAGE : {
                errorButton.setText("Warning: "+error.getErrorMessage());
                break;
            }
            case Error.MISSING_VALUE_MESSAGE : {
                System.out.println("MissingValue");
                errorButton.setText("Missing Value: "+error.getErrorMessage());
                break;
            }            
            case Error.DUPLICATE_VALUE_MESSAGE : {
                errorButton.setText("Duplicate Value: "+error.getErrorMessage());
                break;
            }
        }
        focusableComponent = error.getFocusableComponent();
        errorButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/xml/multiview/resources/error-glyph.gif")));
    }
    
    public void clearError() {
        //errorButton.setVisible(false);
        errorButton.setIcon(null);
        errorButton.setText("");
    }
    
    private class ErrorButton extends javax.swing.JButton {
    
        /** Creates a new instance of LinkButton */
        public ErrorButton() {
            super();
            //setForeground(SectionVisualTheme.hyperlinkColor);
            setForeground(UIManager.getDefaults().getColor("ToolBar.dockingForeground")); //NOI18N
            setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
            setMargin(new java.awt.Insets(2, 2, 2, 2));
            setText("");
            setFocusPainted(false);
            setOpaque(false);
        }

        public void setText(String text) {
            if (text.length()==0) {
                super.setText(" ");
            } else super.setText("<html><u>"+text+"</u></html>");
        }        
    }
    

}
