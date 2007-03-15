/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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
package org.netbeans.modules.xml.refactoring.ui;
import java.awt.Component;
import java.awt.Dimension;
import java.text.MessageFormat;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import org.netbeans.modules.refactoring.spi.ui.CustomRefactoringPanel;
import org.netbeans.modules.xml.xam.NamedReferenceable;
import org.openide.util.NbBundle;


/**
 * Subclass of CustomRefactoringPanel representing the
 * Safe Delete refactoring UI
 * @author Bharath Ravikumar
 */
public class SafeDeletePanel extends JPanel implements CustomRefactoringPanel {

    private final transient NamedReferenceable target;    
    /**
     * Creates new form RenamePanelName
     * @param refactoring The SafeDelete refactoring used by this panel
     * @param selectedElements A Collection of selected elements
     */
    public SafeDeletePanel(NamedReferenceable target) {
        assert target != null:"Nameable target required.";
        setName(NbBundle.getMessage(SafeDeletePanel.class,"LBL_SafeDel")); // NOI18N
        this.target = target;
        initComponents();
    }
    
    private boolean initialized = false;
    /**
     * Initialization method. Creates appropriate labels in the panel.
     */
    public void initialize() {
        
        if (initialized) return;
        final String labelText;
       
        
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                System.out.println("INIT CALLED");
                label.setText(MessageFormat.format(NbBundle.getMessage(
                        SafeDeletePanel.class, "LBL_Delete_BOLD"), 
                        new Object[]{target.getName()}));
                validate();
            }
        });
        initialized = true;
    }
    
    public void requestFocus() {
        super.requestFocus();
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        buttonGroup = new javax.swing.ButtonGroup();
        jPanel3 = new javax.swing.JPanel();
        label = new javax.swing.JLabel();

        setLayout(new java.awt.BorderLayout());

        jPanel3.setLayout(new java.awt.BorderLayout());

        org.openide.awt.Mnemonics.setLocalizedText(label, "Delete myGlobalComponent");
        jPanel3.add(label, java.awt.BorderLayout.NORTH);

        add(jPanel3, java.awt.BorderLayout.NORTH);

    }// </editor-fold>//GEN-END:initComponents
        
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JLabel label;
    // End of variables declaration//GEN-END:variables
    
    
    public Dimension getPreferredSize() {
        Dimension orig = super.getPreferredSize();
        return new Dimension(orig.width + 30 , orig.height + 30);
    }
    
    
    
//--public utility methods--
    
    //This method has been made public so that another class might be able to reuse this.
    //This should be moved to a common utility class.
    /**
     * Returns the formatted string corresponding to the declaration
     * of a CallableFeature(a {@link org.netbeans.jmi.javamodel.Method}
     * or a {@link org.netbeans.jmi.javamodel.Constructor})
     * Copied from {@link org.netbeans.modules.refactoring.ui.WhereUsedPanel}
     */
//    public String getHeader(CallableFeature call) {
//        if (((CallableFeatureImpl) call).getParser() == null) {
//            if (call instanceof Method) {
//                return ((Method) call).getName();
//            } else if (call instanceof Constructor) {
//                return getSimpleName(call.getDeclaringClass());
//            }
//            return "";
//        }
//        int s = ((MetadataElement) call).getPartStartOffset(ElementPartKindEnum.HEADER);
//        int element = ((MetadataElement) call).getPartEndOffset(ElementPartKindEnum.HEADER);
//        String result =  call.getResource().getSourceText().substring(s,element);
//        if (result.length() > 50) {
//            result = result.substring(0,49) + "..."; // NOI18N
//        }
//        return CheckUtils.htmlize(result);
//    }
   
    
    public Component getComponent(){
        return this;
    }
    
}

