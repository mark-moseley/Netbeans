/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
/*
 * CustomizerTitlePanel.java
 *
 * Created on February 19, 2004, 4:57 PM
 */

package org.netbeans.modules.j2ee.sun.share.configbean.customizers.common;

import java.util.ResourceBundle;

import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.Insets;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.ImageIcon;
import javax.swing.UIManager;
import javax.swing.plaf.metal.MetalLookAndFeel;

import org.openide.util.HelpCtx;

import org.netbeans.modules.j2ee.sun.share.Constants;

import org.netbeans.modules.j2ee.sun.share.configbean.Utils;

/**
 *
 * @author Peter Williams
 */
public class CustomizerTitlePanel extends JPanel {
	
	/** Reference to the resource bundle in customizers/common
	 */
	protected static final ResourceBundle commonBundle = ResourceBundle.getBundle(
		"org.netbeans.modules.j2ee.sun.share.configbean.customizers.common.Bundle");	// NOI18N
	
	/** Path for help button image resource
	 */
	private static final String helpButtonImagePath = 
		"org/netbeans/modules/j2ee/sun/share/configbean/customizers/common/resources/customizerHelp.gif"; // NOI18N
	
	/** We only want to load this one.  It's only used here, but in case someone
	 *  decides to use it elsewhere, we'll make it public.
	 */
	public static final ImageIcon helpIcon = 
		 new ImageIcon(Utils.getResourceURL(helpButtonImagePath, CustomizerTitlePanel.class));
	
	/** Creates new form CustomizerTitlePanel */
	public CustomizerTitlePanel() {
		initComponents();
		initUserComponents();
	}
	
	public void setCustomizerTitle(String title) {
		jLblDDType.setText(title);
	}
	
	public GridBagConstraints getConstraints() {
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.gridwidth = GridBagConstraints.REMAINDER;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.anchor = GridBagConstraints.NORTH;
		constraints.weightx = 1.0;
		
		return constraints;
	}
	
	/** This method is called from within the constructor to
	 * initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is
	 * always regenerated by the Form Editor.
	 */
    private void initComponents() {//GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;

        jLblDDType = new javax.swing.JLabel();
        jBtnHelp = new javax.swing.JButton();

        setLayout(new java.awt.GridBagLayout());

        jLblDDType.setFont(new java.awt.Font("Dialog", 0, 14));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        add(jLblDDType, gridBagConstraints);

        jBtnHelp.setBorder(BorderFactory.createEmptyBorder());
        jBtnHelp.setContentAreaFilled(false);
        jBtnHelp.setDefaultCapable(false);
        jBtnHelp.setFocusable(false);
        jBtnHelp.setName("CustomizerHelpButton");
        jBtnHelp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnHelpActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        add(jBtnHelp, gridBagConstraints);
        jBtnHelp.getAccessibleContext().setAccessibleName(commonBundle.getString("ACSN_Help"));
        jBtnHelp.getAccessibleContext().setAccessibleDescription(commonBundle.getString("ACSD_Help"));

    }//GEN-END:initComponents

	private void jBtnHelpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnHelpActionPerformed
		Container parent = getParent();
		if(parent instanceof HelpCtx.Provider) {
			Utils.invokeHelp(((HelpCtx.Provider) parent).getHelpCtx());
		} else {
			Constants.jsr88Logger.fine(commonBundle.getString("ERR_CustomizerHelpNotFound"));
		}
	}//GEN-LAST:event_jBtnHelpActionPerformed
	
	
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jBtnHelp;
    private javax.swing.JLabel jLblDDType;
    // End of variables declaration//GEN-END:variables
	
	private void initUserComponents() {
		jBtnHelp.setIcon(helpIcon);
		
		// !PW Taken verbatim from org\openide\explorer\propertysheet\DescriptionPanel.java
		//     Just a little something to make the button a little bit more polished under
		//     metal L&F I think (though personally, I was unable to see any effect myself.)
		//
		if (UIManager.getLookAndFeel() instanceof MetalLookAndFeel) {
			jBtnHelp.setBorderPainted(false);
			//issue 34159 Metal rollover buttons do not use rollover border
			jBtnHelp.addMouseListener(new MouseAdapter() {
				public void mouseEntered (MouseEvent me) {
					jBtnHelp.setBorderPainted(true);
				}
				public void mouseExited (MouseEvent me) {
					jBtnHelp.setBorderPainted(false);
				}
			});
		}
	}
}
