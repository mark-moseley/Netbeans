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
 * CacheHelperEntryPanel.java
 *
 * Created on January 9, 2004, 12:15 PM
 */

package org.netbeans.modules.j2ee.sun.share.configbean.customizers.webapp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ResourceBundle;
import java.text.MessageFormat;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Insets;

import javax.swing.JPanel;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import org.netbeans.modules.j2ee.sun.dd.api.ASDDVersion;
import org.netbeans.modules.j2ee.sun.dd.api.web.WebProperty;
import org.netbeans.modules.j2ee.sun.share.Constants;
import org.netbeans.modules.j2ee.sun.share.configbean.Utils;
import org.netbeans.modules.j2ee.sun.share.configbean.customizers.common.ListMapping;
import org.netbeans.modules.j2ee.sun.share.configbean.customizers.common.GenericTableModel;
import org.netbeans.modules.j2ee.sun.share.configbean.customizers.common.GenericTablePanel;
import org.netbeans.modules.j2ee.sun.share.configbean.customizers.common.GenericTableDialogPanelAccessor;
import org.netbeans.modules.j2ee.sun.share.configbean.customizers.common.HelpContext;
import org.netbeans.modules.j2ee.sun.share.configbean.customizers.common.ValidationSupport;
import org.netbeans.modules.j2ee.sun.share.configbean.customizers.data.DynamicPropertyPanel;
import org.netbeans.modules.j2ee.sun.share.configbean.customizers.data.PropertyListMapping;
import org.openide.util.NbBundle;

/**
 *
 * @author Peter Williams
 */
public class CacheHelperEntryPanel extends JPanel implements GenericTableDialogPanelAccessor, TableModelListener {

	// Standard resource bundle from common
	private final ResourceBundle commonBundle = NbBundle.getBundle(
		"org.netbeans.modules.j2ee.sun.share.configbean.customizers.common.Bundle");	// NOI18N
	
	// Resource bundle for webapp
	private final ResourceBundle webappBundle = NbBundle.getBundle(
		"org.netbeans.modules.j2ee.sun.share.configbean.customizers.webapp.Bundle");	// NOI18N
	
	// Field indices (maps to values[] handled by get/setValues()
	private static final int NAME_FIELD = 0;
	private static final int CLASSNAME_FIELD = 1;
	private static final int PROPERTIES_FIELD = 2;
	private static final int NUM_FIELDS = 3;	// Number of objects expected in get/setValue methods.

	// Appserver version current referenced
	private ASDDVersion appServerVersion;
    
	// Local storage for data entered by user
	private String name;
	private String className;
	private List properties;
	
	// Table for editing default helper web properties
	private GenericTableModel propertiesModel;
	private GenericTablePanel propertiesPanel;	
	
	private Dimension basicPreferredSize;
	
	/** Creates new form CacheHelperEntryPanel */
	public CacheHelperEntryPanel() {
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

        jLblNameReqFlag = new javax.swing.JLabel();
        jLblName = new javax.swing.JLabel();
        jTxtName = new javax.swing.JTextField();
        jLblClassNameReqFlag = new javax.swing.JLabel();
        jLblClassName = new javax.swing.JLabel();
        jTxtClassName = new javax.swing.JTextField();

        setLayout(new java.awt.GridBagLayout());

        jLblNameReqFlag.setLabelFor(jTxtName);
        jLblNameReqFlag.setText(commonBundle.getString("LBL_RequiredMark"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 0);
        add(jLblNameReqFlag, gridBagConstraints);
        jLblNameReqFlag.getAccessibleContext().setAccessibleName(commonBundle.getString("ACSN_RequiredMark"));
        jLblNameReqFlag.getAccessibleContext().setAccessibleDescription(commonBundle.getString("ACSD_RequiredMark"));

        jLblName.setDisplayedMnemonic(commonBundle.getString("MNE_Name").charAt(0));
        jLblName.setLabelFor(jTxtName);
        jLblName.setText(commonBundle.getString("LBL_Name_1"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 0);
        add(jLblName, gridBagConstraints);

        jTxtName.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jTxtNameKeyReleased(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 5);
        add(jTxtName, gridBagConstraints);
        jTxtName.getAccessibleContext().setAccessibleName(webappBundle.getString("ACSN_CacheHelperName"));
        jTxtName.getAccessibleContext().setAccessibleDescription(webappBundle.getString("ACSD_CacheHelperName"));

        jLblClassNameReqFlag.setLabelFor(jTxtClassName);
        jLblClassNameReqFlag.setText(commonBundle.getString("LBL_RequiredMark"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 0);
        add(jLblClassNameReqFlag, gridBagConstraints);
        jLblClassNameReqFlag.getAccessibleContext().setAccessibleName(commonBundle.getString("ACSN_RequiredMark"));
        jLblClassNameReqFlag.getAccessibleContext().setAccessibleDescription(commonBundle.getString("ACSD_RequiredMark"));

        jLblClassName.setDisplayedMnemonic(webappBundle.getString("MNE_Classname").charAt(0));
        jLblClassName.setLabelFor(jTxtClassName);
        jLblClassName.setText(webappBundle.getString("LBL_Classname_1"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 0);
        add(jLblClassName, gridBagConstraints);

        jTxtClassName.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jTxtClassNameKeyReleased(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 5);
        add(jTxtClassName, gridBagConstraints);
        jTxtClassName.getAccessibleContext().setAccessibleName(webappBundle.getString("ACSN_Classname"));
        jTxtClassName.getAccessibleContext().setAccessibleDescription(webappBundle.getString("ACSD_Classname"));

    }// </editor-fold>//GEN-END:initComponents

	private void jTxtClassNameKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTxtClassNameKeyReleased
		// Add your handling code here:
		className = jTxtClassName.getText();
		firePropertyChange(Constants.USER_DATA_CHANGED, null, null);
	}//GEN-LAST:event_jTxtClassNameKeyReleased

	private void jTxtNameKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTxtNameKeyReleased
		// Add your handling code here:
		name = jTxtName.getText();
		firePropertyChange(Constants.USER_DATA_CHANGED, null, null);
	}//GEN-LAST:event_jTxtNameKeyReleased
	
	
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLblClassName;
    private javax.swing.JLabel jLblClassNameReqFlag;
    private javax.swing.JLabel jLblName;
    private javax.swing.JLabel jLblNameReqFlag;
    private javax.swing.JTextField jTxtClassName;
    private javax.swing.JTextField jTxtName;
    // End of variables declaration//GEN-END:variables
	
	private void initUserComponents() {
		/* Save preferred size before adding table.  We have our own width and
		 * will add a constant of our own choosing for the height in init(), below.
		 */
		basicPreferredSize = getPreferredSize();
		
		/* Class helper properties table panel :
		 * TableEntry list has three properties: Name, Value, Description
		 */
		ArrayList tableColumns = new ArrayList(3);
		tableColumns.add(new GenericTableModel.AttributeEntry(
			WebProperty.NAME, commonBundle.getString("LBL_Name"), true));	// NOI18N
		tableColumns.add(new GenericTableModel.AttributeEntry(
			WebProperty.VALUE, commonBundle.getString("LBL_Value"), true));	// NOI18N
		tableColumns.add(new GenericTableModel.ValueEntry(
			WebProperty.DESCRIPTION, commonBundle.getString("LBL_Description")));	// NOI18N
		
		// Class helper property table
		propertiesModel = new GenericTableModel(WebAppRootCustomizer.webPropertyFactory, tableColumns);
		propertiesModel.addTableModelListener(this);
		propertiesPanel = new GenericTablePanel(propertiesModel, 
			webappBundle, "HelperDefinitionProperties",	// NOI18N - property name
			DynamicPropertyPanel.class, HelpContext.HELP_CACHE_HELPER_PROPERTY_POPUP,
			PropertyListMapping.getPropertyList(PropertyListMapping.CACHE_HELPER_PROPERTIES));
		propertiesPanel.setHeadingMnemonic(webappBundle.getString("MNE_HelperDefinitionProperties").charAt(0));	// NOI18N
		
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridwidth = GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new Insets(0, 6, 5, 5);
		add(propertiesPanel, gridBagConstraints);
	}
	
	/** -----------------------------------------------------------------------
	 *  Implementation of TableModelListener interface
	 */
	public void tableChanged(TableModelEvent e) {
		properties = propertiesModel.getData();
	}
	
	/** -----------------------------------------------------------------------
	 *  Implementation of GenericTableDialogPanelAccessor interface
	 */
	public Collection getErrors(ValidationSupport validationSupport) {
		ArrayList errorList = new ArrayList();
		
		if(!Utils.notEmpty(name)) {
			Object [] args = new Object [1];
			args[0] = commonBundle.getString("LBL_Name");
			errorList.add(MessageFormat.format(commonBundle.getString("ERR_SpecifiedFieldIsEmpty"), args));
		} else if(!Utils.isJavaIdentifier(name)) {
			Object [] args = new Object [1];
			args[0] = name;
			errorList.add(MessageFormat.format(commonBundle.getString("ERR_NotValidIdentifier"), args));
		}
		
		if(!Utils.notEmpty(className)) {
			Object [] args = new Object [1];
			args[0] = webappBundle.getString("LBL_Classname");
			errorList.add(MessageFormat.format(commonBundle.getString("ERR_SpecifiedFieldIsEmpty"), args));
		} else if(!Utils.isJavaPackage(className)) {
			Object [] args = new Object [1];
			args[0] = className;
			errorList.add(MessageFormat.format(commonBundle.getString("ERR_NotValidPackage"), args));
		}
		
		return errorList;	
	}
	
	public Object[] getValues() {
		Object [] result = new Object[NUM_FIELDS];
		
		result[NAME_FIELD] = name;
		result[CLASSNAME_FIELD] = className;
		result[PROPERTIES_FIELD] = new ListMapping(properties);
		
		return result;		
	}
	
	public void init(ASDDVersion asVersion, int preferredWidth, List entries, Object data) {
		/* Cache appserver version for use in setComponentValues.
		 */
		appServerVersion = asVersion;
        
		/* Set preferred size to pre-table saved height plus constant, width is
		 * precalculated to be 3/4 of width of parent table.
		 */
		setPreferredSize(new Dimension(preferredWidth, basicPreferredSize.height + 148));

		/* Initialize property list, in case this is a <New...> operation.
		 * 'properties' data member picks up the real List used by the model
		 * in the listener, tableChanged().
		 */
		propertiesPanel.setModel(new ArrayList(), asVersion);
	}
	
	public void setValues(Object[] values) {
		if(values != null && values.length == NUM_FIELDS) {
			name = (String) values[NAME_FIELD];
			className = (String) values[CLASSNAME_FIELD];
			ListMapping lm = (ListMapping) values[PROPERTIES_FIELD];
			properties = lm.getList();
		} else {
			if(values != null) {
				assert (values.length == NUM_FIELDS);	// Should fail
			}
			
			// default values
			name = "";	// NOI18N
			className = "";	// NOI18N
			properties = new ArrayList();
		}
		
		setComponentValues();
	}
	
	private void setComponentValues() {
		jTxtName.setText(name);
		jTxtClassName.setText(className);
		propertiesPanel.setModel(properties, appServerVersion);
	}	

	public boolean requiredFieldsFilled() {
		return (name != null && name.length() > 0 && 
			className != null && className.length() > 0);
	}
}
