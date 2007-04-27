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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
/*
 * MessageSecurityEntryPanel.java
 *
 * Created on Apr 19, 2006, 6:28 PM
 */

package org.netbeans.modules.j2ee.sun.share.configbean.customizers.webservice;

import java.awt.Dimension;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ResourceBundle;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JPanel;

import org.openide.util.NbBundle;

import org.netbeans.modules.j2ee.sun.share.Constants;
import org.netbeans.modules.j2ee.sun.share.configbean.ASDDVersion;
import org.netbeans.modules.j2ee.sun.share.configbean.Utils;
import org.netbeans.modules.j2ee.sun.share.configbean.customizers.common.ValidationSupport;
import org.netbeans.modules.j2ee.sun.share.configbean.customizers.common.GenericTableDialogPanelAccessor;
import org.netbeans.modules.j2ee.sun.share.configbean.customizers.common.TextMapping;


/**
 *
 * @author Peter Williams
 */
public class MessageSecurityEntryPanel extends JPanel implements GenericTableDialogPanelAccessor {

	private static final ResourceBundle webserviceBundle = ResourceBundle.getBundle(
		"org.netbeans.modules.j2ee.sun.share.configbean.customizers.webservice.Bundle");	// NOI18N

    private static final ResourceBundle commonBundle = ResourceBundle.getBundle(
		"org.netbeans.modules.j2ee.sun.share.configbean.customizers.common.Bundle");	// NOI18N

	/** xml <--> ui mapping for request/response authorization settings */
	private static final TextMapping [] sourceTypes = {
		new TextMapping("", ""), // NOI18N
		new TextMapping("sender", webserviceBundle.getString("AUTH_SOURCE_SENDER")),	// NOI18N
		new TextMapping("content", webserviceBundle.getString("AUTH_SOURCE_CONTENT")),	// NOI18N
	};
    
	private static final TextMapping [] recipientTypes = {
		new TextMapping("", ""), // NOI18N
		new TextMapping("before-content", webserviceBundle.getString("AUTH_RECIPIENT_BEFORE_CONTENT")),	// NOI18N
		new TextMapping("after-content", webserviceBundle.getString("AUTH_RECIPIENT_AFTER_CONTENT")),	// NOI18N
	};
    
	// Field indices (maps to values[] handled by get/setValues()
	private static final int METHOD_FIELD = 0;
	private static final int REQ_SOURCE_FIELD = 1;
	private static final int REQ_RECIPIENT_FIELD = 2;
	private static final int RESP_SOURCE_FIELD = 3;
	private static final int RESP_RECIPIENT_FIELD = 4;
	private static final int NUM_FIELDS = 5;	// Number of objects expected in get/setValue methods.
    
    private DefaultComboBoxModel authReqSourceModel;
    private DefaultComboBoxModel authReqRecipientModel;
    private DefaultComboBoxModel authRespSourceModel;
    private DefaultComboBoxModel authRespRecipientModel;

    // Do we validate method name as an operation or as a java method?
    private boolean methodAsOperation;
    
	// Local storage for data entered by user
	private String methodName;
    private String reqSource;
    private String reqRecipient;
    private String respSource;
    private String respRecipient;

    private boolean initializingFields;

	/**
     * Creates new form MessageSecurityEntryPanel
     */
	public MessageSecurityEntryPanel() {
		initComponents();
		initUserComponents();
	}
	
	/** This method is called from within the constructor to
	 * initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is
	 * always regenerated by the Form Editor.
	 */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jLblMethodNameReqFlag = new javax.swing.JLabel();
        jLblMethodName = new javax.swing.JLabel();
        jTxtMethodName = new javax.swing.JTextField();
        jLblReqAuthSource = new javax.swing.JLabel();
        jCbxReqAuthSource = new javax.swing.JComboBox();
        jLblReqAuthRecip = new javax.swing.JLabel();
        jCbxReqAuthRecip = new javax.swing.JComboBox();
        jLblRespAuthSource = new javax.swing.JLabel();
        jCbxRespAuthSource = new javax.swing.JComboBox();
        jLblRespAuthRecip = new javax.swing.JLabel();
        jCbxRespAuthRecip = new javax.swing.JComboBox();

        setLayout(new java.awt.GridBagLayout());

        jLblMethodNameReqFlag.setText(commonBundle.getString("LBL_RequiredMark"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 0);
        add(jLblMethodNameReqFlag, gridBagConstraints);
        jLblMethodNameReqFlag.getAccessibleContext().setAccessibleName(commonBundle.getString("ACSN_RequiredMark"));
        jLblMethodNameReqFlag.getAccessibleContext().setAccessibleDescription(commonBundle.getString("ACSD_RequiredMark"));

        jLblMethodName.setDisplayedMnemonic(webserviceBundle.getString("MNE_MethodName").charAt(0));
        jLblMethodName.setText(webserviceBundle.getString("LBL_MethodName_1"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 0);
        add(jLblMethodName, gridBagConstraints);

        jTxtMethodName.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jTxtMethodNameKeyReleased(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 5);
        add(jTxtMethodName, gridBagConstraints);

        jLblReqAuthSource.setDisplayedMnemonic(webserviceBundle.getString("MNE_ReqAuthSource").charAt(0));
        jLblReqAuthSource.setText(webserviceBundle.getString("LBL_ReqAuthSource_1"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 0);
        add(jLblReqAuthSource, gridBagConstraints);

        jCbxReqAuthSource.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCbxReqAuthSourceActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 5);
        add(jCbxReqAuthSource, gridBagConstraints);

        jLblReqAuthRecip.setDisplayedMnemonic(webserviceBundle.getString("MNE_ReqAuthRecipient").charAt(0));
        jLblReqAuthRecip.setText(webserviceBundle.getString("LBL_ReqAuthRecipient_1"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 0);
        add(jLblReqAuthRecip, gridBagConstraints);

        jCbxReqAuthRecip.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCbxReqAuthRecipActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 5);
        add(jCbxReqAuthRecip, gridBagConstraints);

        jLblRespAuthSource.setDisplayedMnemonic(webserviceBundle.getString("MNE_RespAuthSource").charAt(0));
        jLblRespAuthSource.setText(webserviceBundle.getString("LBL_RespAuthSource_1"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 0);
        add(jLblRespAuthSource, gridBagConstraints);

        jCbxRespAuthSource.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCbxRespAuthSourceActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 5);
        add(jCbxRespAuthSource, gridBagConstraints);

        jLblRespAuthRecip.setDisplayedMnemonic(webserviceBundle.getString("MNE_RespAuthRecipient").charAt(0));
        jLblRespAuthRecip.setText(webserviceBundle.getString("LBL_RespAuthRecipient_1"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 5, 0);
        add(jLblRespAuthRecip, gridBagConstraints);

        jCbxRespAuthRecip.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCbxRespAuthRecipActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 5, 5);
        add(jCbxRespAuthRecip, gridBagConstraints);

    }// </editor-fold>//GEN-END:initComponents

    private void jTxtMethodNameKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTxtMethodNameKeyReleased
        if(!initializingFields) {
            String oldMethodName = methodName;
            methodName = jTxtMethodName.getText().trim();
            if(!methodName.equals(oldMethodName)) {
                firePropertyChange(Constants.USER_DATA_CHANGED, null, null);
            }
        }
    }//GEN-LAST:event_jTxtMethodNameKeyReleased

    private void jCbxRespAuthRecipActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCbxRespAuthRecipActionPerformed
        if(!initializingFields) {
    		TextMapping authMapping = (TextMapping) authRespRecipientModel.getSelectedItem();
        	respRecipient = normalizeBlank(authMapping.getXMLString());
    		firePropertyChange(Constants.USER_DATA_CHANGED, null, null);		
        }
    }//GEN-LAST:event_jCbxRespAuthRecipActionPerformed

    private void jCbxRespAuthSourceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCbxRespAuthSourceActionPerformed
        if(!initializingFields) {
    		TextMapping authMapping = (TextMapping) authRespSourceModel.getSelectedItem();
        	respSource = normalizeBlank(authMapping.getXMLString());
    		firePropertyChange(Constants.USER_DATA_CHANGED, null, null);		
        }
    }//GEN-LAST:event_jCbxRespAuthSourceActionPerformed

    private void jCbxReqAuthRecipActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCbxReqAuthRecipActionPerformed
        if(!initializingFields) {
    		TextMapping authMapping = (TextMapping) authReqRecipientModel.getSelectedItem();
        	reqRecipient = normalizeBlank(authMapping.getXMLString());
    		firePropertyChange(Constants.USER_DATA_CHANGED, null, null);		
        }
    }//GEN-LAST:event_jCbxReqAuthRecipActionPerformed

    private void jCbxReqAuthSourceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCbxReqAuthSourceActionPerformed
        if(!initializingFields) {
    		TextMapping authMapping = (TextMapping) authReqSourceModel.getSelectedItem();
        	reqSource = normalizeBlank(authMapping.getXMLString());
    		firePropertyChange(Constants.USER_DATA_CHANGED, null, null);		
        }
    }//GEN-LAST:event_jCbxReqAuthSourceActionPerformed
	
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox jCbxReqAuthRecip;
    private javax.swing.JComboBox jCbxReqAuthSource;
    private javax.swing.JComboBox jCbxRespAuthRecip;
    private javax.swing.JComboBox jCbxRespAuthSource;
    private javax.swing.JLabel jLblMethodName;
    private javax.swing.JLabel jLblMethodNameReqFlag;
    private javax.swing.JLabel jLblReqAuthRecip;
    private javax.swing.JLabel jLblReqAuthSource;
    private javax.swing.JLabel jLblRespAuthRecip;
    private javax.swing.JLabel jLblRespAuthSource;
    private javax.swing.JTextField jTxtMethodName;
    // End of variables declaration//GEN-END:variables
	
	protected void initUserComponents() {
        // Setup authorization source comboboxes
        authReqSourceModel = new DefaultComboBoxModel();
        authRespSourceModel = new DefaultComboBoxModel();
        for(int i = 0; i < sourceTypes.length; i++) {
            authReqSourceModel.addElement(sourceTypes[i]);
            authRespSourceModel.addElement(sourceTypes[i]);
        }
        jCbxReqAuthSource.setModel(authReqSourceModel);
        jCbxRespAuthSource.setModel(authRespSourceModel);
        
        // Setup authorization recipient comboboxes
        authReqRecipientModel = new DefaultComboBoxModel();
        authRespRecipientModel = new DefaultComboBoxModel();
        for(int i = 0; i < recipientTypes.length; i++) {
            authReqRecipientModel.addElement(recipientTypes[i]);
            authRespRecipientModel.addElement(recipientTypes[i]);
        }
        jCbxReqAuthRecip.setModel(authReqRecipientModel);
        jCbxRespAuthRecip.setModel(authRespRecipientModel);
	}
    
    private String normalizeBlank(String value) {
        return (!value.equals("")) ? value : null;
    }
    
	private TextMapping getAuthMapping(String xmlKey, final TextMapping [] authMap) {
		TextMapping result = null;
		if(xmlKey == null) {
			xmlKey = ""; // NOI18N
		}
		for(int i = 0; i < authMap.length; i++) {
			if(authMap[i].getXMLString().compareTo(xmlKey) == 0) {
				result = authMap[i];
				break;
			}
		}
		return result;
	}
    
	public Collection getErrors(ValidationSupport validationSupport) {
		ArrayList errorList = new ArrayList();

        if(!Utils.notEmpty(methodName)) {
            String msgPattern = webserviceBundle.getString("ERR_MethodNameRequired"); // NOI18N
			errorList.add(MessageFormat.format(msgPattern, new Object [] { Integer.valueOf(methodAsOperation ? 1 : 0) } ));
        } else if(!"*".equals(methodName)) {
            if(methodAsOperation) {
                // validate methodName as just an operation name.
                if(!Utils.isJavaIdentifier(methodName)) {
                    errorList.add(NbBundle.getMessage(MessageSecurityEntryPanel.class,  
                            "ERR_OperationNameInvalid", methodName)); // NOI18N
                }
            } else {
                // validate methodName as a java method signature.
                String [] parts = MessageEntry.methodSplitter.split(methodName);
                if(parts.length > 0) {
                    if(!Utils.isJavaIdentifier(parts[0])) {
                        errorList.add(NbBundle.getMessage(MessageSecurityEntryPanel.class, 
                                "ERR_JavaMethodInvalid", parts[0])); // NOI18N
                    } else {
                        for(int i = 1; i < parts.length; i++) {
                            if(!Utils.isJavaClass(parts[i])) {
                                errorList.add(NbBundle.getMessage(MessageSecurityEntryPanel.class, 
                                        "ERR_MethodParamInvalid", parts[i])); // NOI18N
                                // Only show one parameter error at a time.
                                break;
                            }   
                        }
                    }
                }
            }
        }
		
		return errorList;	
	}
	
	public Object[] getValues() {
		Object [] result = new Object[NUM_FIELDS];
		
        result[METHOD_FIELD] = methodName;
        result[REQ_SOURCE_FIELD] = reqSource;
        result[REQ_RECIPIENT_FIELD] = reqRecipient;
        result[RESP_SOURCE_FIELD] = respSource;
        result[RESP_RECIPIENT_FIELD] = respRecipient;
        
		return result;
	}
	
	public void init(ASDDVersion asVersion, int preferredWidth, List entries, Object data) {
        if(data instanceof Boolean) {
            methodAsOperation = ((Boolean) data).booleanValue();
        }
        
		setPreferredSize(new Dimension(preferredWidth, getPreferredSize().height));
	}
	
	public void setValues(Object[] values) {
		if(values != null && values.length == NUM_FIELDS) {
            methodName = (String) values[METHOD_FIELD];
            reqSource = (String) values[REQ_SOURCE_FIELD];
            reqRecipient = (String) values[REQ_RECIPIENT_FIELD];
            respSource = (String) values[RESP_SOURCE_FIELD];
            respRecipient = (String) values[RESP_RECIPIENT_FIELD];
		} else {
			if(values != null) {
				assert (values.length == NUM_FIELDS);	// Should fail
			}
			
			// default values
			methodName = "*"; // NOI18N
            reqSource = null;
            reqRecipient = null;
            respSource = null;
            respRecipient = null;
		}
		
		setComponentValues();		
	}
	
	private void setComponentValues() {
        try {
            initializingFields = true;
            
            jTxtMethodName.setText(methodName);
            jCbxReqAuthSource.setSelectedItem(getAuthMapping(reqSource, sourceTypes));
            jCbxReqAuthRecip.setSelectedItem(getAuthMapping(reqRecipient, recipientTypes));
            jCbxRespAuthSource.setSelectedItem(getAuthMapping(respSource, sourceTypes));
            jCbxRespAuthRecip.setSelectedItem(getAuthMapping(respRecipient, recipientTypes));
        } finally {
            initializingFields = false;
        }
	}
	
	public boolean requiredFieldsFilled() {
		return Utils.notEmpty(methodName);
	}
}
