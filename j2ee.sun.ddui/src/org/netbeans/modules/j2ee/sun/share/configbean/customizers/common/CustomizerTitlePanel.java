/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
/*
 * CustomizerTitlePanel.java
 *
 * Created on February 19, 2004, 4:57 PM
 */
package org.netbeans.modules.j2ee.sun.share.configbean.customizers.common;

import java.awt.Container;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.Insets;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import java.util.ResourceBundle;
import javax.swing.JPanel;
import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.plaf.metal.MetalLookAndFeel;
import org.netbeans.modules.j2ee.sun.share.config.ConfigDataObject;
import org.netbeans.modules.j2ee.sun.share.config.ConfigurationStorage;
import org.netbeans.modules.j2ee.sun.share.configbean.SunONEDeploymentConfiguration;
import org.openide.cookies.EditCookie;

import org.openide.util.HelpCtx;

import org.netbeans.modules.j2ee.sun.share.Constants;
import org.netbeans.modules.j2ee.sun.share.configbean.Base;
import org.netbeans.modules.j2ee.sun.share.configbean.Utils;

/**
 *
 * @author Peter Williams
 */
public class CustomizerTitlePanel extends JPanel {
	
	/** Reference to the resource bundle in customizers/common
	 */
	protected final ResourceBundle commonBundle = ResourceBundle.getBundle(
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
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jLblDDType = new javax.swing.JLabel();
        jBtnEditXml = new javax.swing.JButton();
        jBtnDocType = new javax.swing.JButton();
        jBtnHelp = new javax.swing.JButton();

        setLayout(new java.awt.GridBagLayout());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 5, 5);
        add(jLblDDType, gridBagConstraints);

        jBtnEditXml.setText(commonBundle.getString("LBL_EditAsXml"));
        jBtnEditXml.setMargin(new java.awt.Insets(0, 10, 0, 10));
        jBtnEditXml.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnEditXmlActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 5, 0);
        add(jBtnEditXml, gridBagConstraints);
        jBtnEditXml.getAccessibleContext().setAccessibleName(commonBundle.getString("ACSN_EditAsXml"));
        jBtnEditXml.getAccessibleContext().setAccessibleDescription(commonBundle.getString("ACSD_EditAsXml"));

        jBtnDocType.setText(commonBundle.getString("LBL_DocType"));
        jBtnDocType.setMargin(new java.awt.Insets(0, 10, 0, 10));
        jBtnDocType.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnDocTypeActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 5, 0);
        add(jBtnDocType, gridBagConstraints);
        jBtnDocType.getAccessibleContext().setAccessibleName(commonBundle.getString("ACSN_Doctype"));
        jBtnDocType.getAccessibleContext().setAccessibleDescription(commonBundle.getString("ACSD_Doctype"));

        jBtnHelp.setBorder(javax.swing.BorderFactory.createEmptyBorder(2, 2, 1, 1));
        jBtnHelp.setContentAreaFilled(false);
        jBtnHelp.setDefaultCapable(false);
        jBtnHelp.setFocusPainted(false);
        jBtnHelp.setName("CustomizerHelpButton");
        jBtnHelp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnHelpActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 5, 5);
        add(jBtnHelp, gridBagConstraints);
        jBtnHelp.getAccessibleContext().setAccessibleName(commonBundle.getString("ACSN_Help"));
        jBtnHelp.getAccessibleContext().setAccessibleDescription(commonBundle.getString("ACSD_Help"));

    }// </editor-fold>//GEN-END:initComponents

    private void jBtnEditXmlActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnEditXmlActionPerformed
        // TODO add your handling code here:
        Base theBean = null;
        Container parent = getParent();
        if(parent instanceof BaseCustomizer) {
            BaseCustomizer masterPanel = (BaseCustomizer) parent;
            theBean = masterPanel.getBean();
        } 

        if(theBean != null) {
            switchToXml(theBean.getConfig());
        }
    }//GEN-LAST:event_jBtnEditXmlActionPerformed

    private void jBtnDocTypeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnDocTypeActionPerformed
        Base theBean = null;
        Container p = getParent();
        if(p instanceof JPanel) {
            JPanel parent = (JPanel) p;
            if(parent instanceof BaseCustomizer) {
                BaseCustomizer masterPanel = (BaseCustomizer) parent;
                theBean = masterPanel.getBean();
            } 

            if(theBean != null) {
                ChangeDocTypePanel.editASVersion(parent, theBean);
            }
        }
    }//GEN-LAST:event_jBtnDocTypeActionPerformed

	private void jBtnHelpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnHelpActionPerformed
		Container parent = getParent();
		if(parent instanceof HelpCtx.Provider) {
			Utils.invokeHelp(((HelpCtx.Provider) parent).getHelpCtx());
		} else {
			Constants.jsr88Logger.fine(commonBundle.getString("ERR_CustomizerHelpNotFound"));
		}
	}//GEN-LAST:event_jBtnHelpActionPerformed
	
	
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jBtnDocType;
    private javax.swing.JButton jBtnEditXml;
    private javax.swing.JButton jBtnHelp;
    private javax.swing.JLabel jLblDDType;
    // End of variables declaration//GEN-END:variables
	
	private void initUserComponents() {
        // Set the title font to be bold and +4 size to regular font, whatever that is.
		Font labelFont = jLblDDType.getFont();
        jLblDDType.setFont(labelFont.deriveFont(Font.BOLD, labelFont.getSize()+4));
        
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
    
    private boolean switchToXml(SunONEDeploymentConfiguration config) {
        // first get primary dataobject
        ConfigDataObject primaryDO = null;
        ConfigurationStorage storage = config.getStorage();
        if(storage != null) {
            primaryDO = storage.getPrimaryDataObject();
        }
        
        if(primaryDO == null) {
//            System.out.println("Can't find primary dataobject for this configuration.");
            return false;
        }
        
        if(!primaryDO.closeConfigEditors()) {
//            System.out.println("Failed to close editor(s).");
            return false;
        }
        
        // process closing & possible autosave.
        try {
            // hack - wait for autosave() to post it's event.
            Thread.currentThread().sleep(100);
        } catch (InterruptedException ex) {
            // shouldn't happen, but just ignore it if it does.
        }
        
        final ConfigDataObject theDataObject = primaryDO;
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                // reopen editor
                EditCookie editCookie = (EditCookie) theDataObject.getCookie(EditCookie.class);
                if(editCookie != null) {
                    editCookie.edit();
                } 
            }
        });

        return true;
    }
}
