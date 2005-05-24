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
 * LocaleCharsetMapEntryPanel.java
 *
 * Created on January 2, 2004, 2:33 PM
 */

package org.netbeans.modules.j2ee.sun.share.configbean.customizers.webapp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.List;
import java.util.ResourceBundle;
import java.util.SortedSet;
import java.util.SortedMap;
import java.text.MessageFormat;

import java.awt.Dimension;
import javax.swing.JPanel;
import javax.swing.DefaultComboBoxModel;

import java.util.Locale;
import java.nio.charset.Charset;

import org.netbeans.modules.j2ee.sun.share.Constants;
import org.netbeans.modules.j2ee.sun.share.configbean.customizers.common.ValidationSupport;
import org.netbeans.modules.j2ee.sun.share.configbean.customizers.common.GenericTableDialogPanelAccessor;

import org.netbeans.modules.j2ee.sun.api.CharsetMapping;


/**
 *
 * @author Peter Williams
 */
public class LocaleCharsetMapEntryPanel extends JPanel implements GenericTableDialogPanelAccessor {

	private static final ResourceBundle webappBundle = ResourceBundle.getBundle(
		"org.netbeans.modules.j2ee.sun.share.configbean.customizers.webapp.Bundle");	// NOI18N

	private static final ResourceBundle commonBundle = ResourceBundle.getBundle(
		"org.netbeans.modules.j2ee.sun.share.configbean.customizers.common.Bundle");	// NOI18N

	private static final String DEFAULT_CHARSET="UTF8";	// NOI18N
	
	// Field indices (maps to values[] handled by get/setValues()
	private static final int LOCALE_FIELD = 0;
	private static final int CHARSET_FIELD = 1;
	private static final int AGENT_FIELD = 2;
	private static final int DESCRIPTION_FIELD = 3;
	private static final int NUM_FIELDS = 4;	// Number of objects expected in get/setValue methods.

	// Local storage for data entered by user
	private LocaleMapping localeMap;
	private CharsetMapping charsetMap;
	private String agent;
	private String description;

	private DefaultComboBoxModel localeCbxModel;
	private DefaultComboBoxModel charsetCbxModel;

	/** Creates new form LocaleCharsetMapEntryPanel */
	public LocaleCharsetMapEntryPanel() {
		initComponents();
		initUserComponents();
	}
	
	/** This method is called from within the constructor to
	 * initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is
	 * always regenerated by the Form Editor.
	 */
    private void initComponents() {//GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;

        jLblLocaleReqFlag = new javax.swing.JLabel();
        jLblLocale = new javax.swing.JLabel();
        jCbxLocale = new javax.swing.JComboBox();
        jLblCharsetReqFlag = new javax.swing.JLabel();
        jLblCharset = new javax.swing.JLabel();
        jCbxCharset = new javax.swing.JComboBox();
        jLblFiller1 = new javax.swing.JLabel();
        jLblAgent = new javax.swing.JLabel();
        jTxtAgent = new javax.swing.JTextField();
        jLblFiller2 = new javax.swing.JLabel();
        jLblDescription = new javax.swing.JLabel();
        jTxtDescription = new javax.swing.JTextField();

        setLayout(new java.awt.GridBagLayout());

        jLblLocaleReqFlag.setLabelFor(jCbxLocale);
        jLblLocaleReqFlag.setText(commonBundle.getString("LBL_RequiredMark"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 0, 4);
        add(jLblLocaleReqFlag, gridBagConstraints);
        jLblLocaleReqFlag.getAccessibleContext().setAccessibleName(commonBundle.getString("ACSN_RequiredMark"));
        jLblLocaleReqFlag.getAccessibleContext().setAccessibleDescription(commonBundle.getString("ACSD_RequiredMark"));

        jLblLocale.setDisplayedMnemonic(webappBundle.getString("MNE_Locale").charAt(0));
        jLblLocale.setLabelFor(jCbxLocale);
        jLblLocale.setText(webappBundle.getString("LBL_Locale_1"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 0, 4);
        add(jLblLocale, gridBagConstraints);

        jCbxLocale.setPrototypeDisplayValue("");
        jCbxLocale.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCbxLocaleActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 0, 4);
        add(jCbxLocale, gridBagConstraints);
        jCbxLocale.getAccessibleContext().setAccessibleName(webappBundle.getString("ACSN_Locale"));
        jCbxLocale.getAccessibleContext().setAccessibleDescription(webappBundle.getString("ACSD_Locale"));

        jLblCharsetReqFlag.setLabelFor(jCbxCharset);
        jLblCharsetReqFlag.setText(commonBundle.getString("LBL_RequiredMark"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 0, 4);
        add(jLblCharsetReqFlag, gridBagConstraints);
        jLblCharsetReqFlag.getAccessibleContext().setAccessibleName(commonBundle.getString("ACSN_RequiredMark"));
        jLblCharsetReqFlag.getAccessibleContext().setAccessibleDescription(commonBundle.getString("ACSD_RequiredMark"));

        jLblCharset.setDisplayedMnemonic(webappBundle.getString("MNE_CharacterSet").charAt(0));
        jLblCharset.setLabelFor(jCbxCharset);
        jLblCharset.setText(webappBundle.getString("LBL_CharacterSet_1"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 0, 4);
        add(jLblCharset, gridBagConstraints);

        jCbxCharset.setPrototypeDisplayValue("");
        jCbxCharset.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCbxCharsetActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 0, 4);
        add(jCbxCharset, gridBagConstraints);
        jCbxCharset.getAccessibleContext().setAccessibleName(webappBundle.getString("ACSN_CharacterSet"));
        jCbxCharset.getAccessibleContext().setAccessibleDescription(webappBundle.getString("ACSD_CharacterSet"));

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 0, 4);
        add(jLblFiller1, gridBagConstraints);

        jLblAgent.setDisplayedMnemonic(webappBundle.getString("MNE_Agent").charAt(0));
        jLblAgent.setLabelFor(jTxtAgent);
        jLblAgent.setText(webappBundle.getString("LBL_Agent_1"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 0, 4);
        add(jLblAgent, gridBagConstraints);

        jTxtAgent.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jTxtAgentKeyReleased(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 0, 4);
        add(jTxtAgent, gridBagConstraints);
        jTxtAgent.getAccessibleContext().setAccessibleName(webappBundle.getString("ACSN_Agent"));
        jTxtAgent.getAccessibleContext().setAccessibleDescription(webappBundle.getString("ACSD_Agent"));

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        add(jLblFiller2, gridBagConstraints);

        jLblDescription.setDisplayedMnemonic(webappBundle.getString("MNE_LocaleDescription").charAt(0));
        jLblDescription.setLabelFor(jTxtDescription);
        jLblDescription.setText(webappBundle.getString("LBL_LocaleDescription_1"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 4, 4);
        add(jLblDescription, gridBagConstraints);

        jTxtDescription.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jTxtDescriptionKeyReleased(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        add(jTxtDescription, gridBagConstraints);
        jTxtDescription.getAccessibleContext().setAccessibleName(webappBundle.getString("ACSN_LocaleDescription"));
        jTxtDescription.getAccessibleContext().setAccessibleDescription(webappBundle.getString("ACSD_LocaleDescription"));

    }//GEN-END:initComponents

	private void jCbxCharsetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCbxCharsetActionPerformed
		// Add your handling code here:
		charsetMap = (CharsetMapping) charsetCbxModel.getSelectedItem();
		firePropertyChange(Constants.USER_DATA_CHANGED, null, null);
	}//GEN-LAST:event_jCbxCharsetActionPerformed

	private void jCbxLocaleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCbxLocaleActionPerformed
		// Add your handling code here:
		localeMap = (LocaleMapping) localeCbxModel.getSelectedItem();
		firePropertyChange(Constants.USER_DATA_CHANGED, null, null);
	}//GEN-LAST:event_jCbxLocaleActionPerformed

	private void jTxtDescriptionKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTxtDescriptionKeyReleased
		// Add your handling code here:
		description = jTxtDescription.getText();
		firePropertyChange(Constants.USER_DATA_CHANGED, null, null);
	}//GEN-LAST:event_jTxtDescriptionKeyReleased

	private void jTxtAgentKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTxtAgentKeyReleased
		// Add your handling code here:
		agent = jTxtAgent.getText();
		firePropertyChange(Constants.USER_DATA_CHANGED, null, null);
	}//GEN-LAST:event_jTxtAgentKeyReleased
	
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox jCbxCharset;
    private javax.swing.JComboBox jCbxLocale;
    private javax.swing.JLabel jLblAgent;
    private javax.swing.JLabel jLblCharset;
    private javax.swing.JLabel jLblCharsetReqFlag;
    private javax.swing.JLabel jLblDescription;
    private javax.swing.JLabel jLblFiller1;
    private javax.swing.JLabel jLblFiller2;
    private javax.swing.JLabel jLblLocale;
    private javax.swing.JLabel jLblLocaleReqFlag;
    private javax.swing.JTextField jTxtAgent;
    private javax.swing.JTextField jTxtDescription;
    // End of variables declaration//GEN-END:variables
	
	protected void initUserComponents() {
		localeCbxModel = new DefaultComboBoxModel();
		SortedMap locales = LocaleMapping.getSortedAvailableLocaleMappings();
		for(Iterator iter = locales.entrySet().iterator(); iter.hasNext(); ) {
			LocaleMapping lm = (LocaleMapping) ((Map.Entry) iter.next()).getValue();
			localeCbxModel.addElement(lm);
		}
		jCbxLocale.setModel(localeCbxModel);
		jCbxLocale.setSelectedItem(LocaleMapping.getLocaleMapping(Locale.getDefault()));
		
		charsetCbxModel = new DefaultComboBoxModel();
		SortedMap charsets = CharsetMapping.getSortedAvailableCharsetMappings();
		for(Iterator iter = charsets.entrySet().iterator(); iter.hasNext(); ) {
			CharsetMapping cm = (CharsetMapping) ((Map.Entry) iter.next()).getValue();
			charsetCbxModel.addElement(cm);
		}
		jCbxCharset.setModel(charsetCbxModel);
		jCbxCharset.setSelectedItem(CharsetMapping.getCharsetMapping(DEFAULT_CHARSET));
	}
	
	public Collection getErrors(ValidationSupport validationSupport) {
		ArrayList errorList = new ArrayList();
		
		if(localeMap == null) {
			errorList.add(webappBundle.getString("ERR_LocaleFieldRequired"));	// NOI18N
		} else if(charsetMap == null) {
			errorList.add(webappBundle.getString("ERR_CharsetFieldRequired"));	// NOI18N
		}
		
		return errorList;	
	}
	
	public Object[] getValues() {
		Object [] result = new Object[NUM_FIELDS];
		
		if(localeMap != null) {
			result[LOCALE_FIELD] = localeMap.getLocale().toString();
		}
		
		if(charsetMap != null) {
			result[CHARSET_FIELD] = charsetMap.getCharset().toString();
		}
		
		result[AGENT_FIELD] = agent;
		result[DESCRIPTION_FIELD] = description;
		return result;
	}
	
	public void init(int preferredWidth, List entries, Object data) {
		setPreferredSize(new Dimension(preferredWidth, getPreferredSize().height));
	}
	
	public void setValues(Object[] values) {
		if(values != null && values.length == NUM_FIELDS) {
			localeMap = LocaleMapping.getLocaleMapping((String) values[LOCALE_FIELD]);
			charsetMap = CharsetMapping.getCharsetMapping((String) values[CHARSET_FIELD]);
			agent = (String) values[AGENT_FIELD];
			description = (String) values[DESCRIPTION_FIELD];
		} else {
			if(values != null) {
				assert (values.length == NUM_FIELDS);	// Should fail
			}
			
			// default values
			localeMap = LocaleMapping.getLocaleMapping(Locale.getDefault());
			charsetMap = null; //CharsetMapping.getCharsetMapping(Charset.);
			agent = "";	// NOI18N
			description = "";	// NOI18N
		}
		
		setComponentValues();		
	}
	
	private void setComponentValues() {
		jCbxLocale.setSelectedItem(localeMap);
		jCbxCharset.setSelectedItem(charsetMap);
		jTxtAgent.setText(agent);
		jTxtDescription.setText(description);
	}
	
	public boolean requiredFieldsFilled() {
		return (localeMap != null && charsetMap != null);
	}
}
