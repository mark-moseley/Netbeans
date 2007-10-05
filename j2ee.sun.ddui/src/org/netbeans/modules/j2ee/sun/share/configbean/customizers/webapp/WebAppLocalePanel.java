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
 * WebAppLocalePanel.java
 *
 * Created on November 5, 2003, 4:56 PM
 */

package org.netbeans.modules.j2ee.sun.share.configbean.customizers.webapp;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.SortedMap;

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import javax.swing.DefaultComboBoxModel;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import org.netbeans.modules.j2ee.sun.dd.api.ASDDVersion;
import org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean;
import org.netbeans.modules.j2ee.sun.dd.api.web.LocaleCharsetInfo;
import org.netbeans.modules.j2ee.sun.dd.api.web.LocaleCharsetMap;

import org.netbeans.modules.j2ee.sun.share.configbean.StorageBeanFactory;
import org.netbeans.modules.j2ee.sun.share.configbean.Utils;
import org.netbeans.modules.j2ee.sun.share.configbean.WebAppRoot;
import org.netbeans.modules.j2ee.sun.share.configbean.customizers.common.GenericTableModel;
import org.netbeans.modules.j2ee.sun.share.configbean.customizers.common.GenericTablePanel;
import org.netbeans.modules.j2ee.sun.share.configbean.customizers.common.HelpContext;
import org.netbeans.modules.j2ee.sun.share.CharsetMapping;
import org.openide.util.NbBundle;

/**
 *
 * @author Peter Williams
 */
public class WebAppLocalePanel extends javax.swing.JPanel implements TableModelListener {
	
	private final ResourceBundle webappBundle = NbBundle.getBundle(
		"org.netbeans.modules.j2ee.sun.share.configbean.customizers.webapp.Bundle");	// NOI18N

	private final ResourceBundle commonBundle = NbBundle.getBundle(
		"org.netbeans.modules.j2ee.sun.share.configbean.customizers.common.Bundle");	// NOI18N

	private WebAppRootCustomizer masterPanel;

	// Temporary storage for quick determination of presence of parameter encoding
	// entry
	private String defaultCharset;
	private String formHintField;
	
	private DefaultComboBoxModel defaultCharsetCbxModel;
	private DefaultComboBoxModel defaultLocaleCbxModel;

	// Table for editing locale-charset mapping entries
	private GenericTableModel localeCharsetMapModel;
	private GenericTablePanel localeCharsetMapPanel;	

	private PropertyChangeListener charsetChangeListener;
	
	
	/** Creates new form WebAppLocalePanel */
	public WebAppLocalePanel(WebAppRootCustomizer src) {
		masterPanel = src;

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

        jBtnGrpCharsetAliases = new javax.swing.ButtonGroup();
        jLabel1 = new javax.swing.JLabel();
        jPnlLocaleSettings = new javax.swing.JPanel();
        jLblDefaultLocale = new javax.swing.JLabel();
        jCbxDefaultLocale = new javax.swing.JComboBox();
        jLblParameterEncoding = new javax.swing.JLabel();
        jPnlParameterEncoding = new javax.swing.JPanel();
        jLblDefaultCharset = new javax.swing.JLabel();
        jCbxDefaultCharset = new javax.swing.JComboBox();
        jLblFormHintField = new javax.swing.JLabel();
        jTxtFormHintField = new javax.swing.JTextField();

        setLayout(new java.awt.GridBagLayout());

        getAccessibleContext().setAccessibleName(webappBundle.getString("ACSN_WebAppLocaleTab"));
        getAccessibleContext().setAccessibleDescription(webappBundle.getString("ACSD_WebAppLocaleTab"));
        jLabel1.setText(webappBundle.getString("LBL_LocaleInfoPanelDescription"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 0);
        add(jLabel1, gridBagConstraints);

        jPnlLocaleSettings.setLayout(new java.awt.GridBagLayout());

        jLblDefaultLocale.setLabelFor(jCbxDefaultLocale);
        jLblDefaultLocale.setText(webappBundle.getString("LBL_DefaultLocaleMapping_1"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPnlLocaleSettings.add(jLblDefaultLocale, gridBagConstraints);

        jCbxDefaultLocale.setPrototypeDisplayValue("");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 0);
        jPnlLocaleSettings.add(jCbxDefaultLocale, gridBagConstraints);
        jCbxDefaultLocale.getAccessibleContext().setAccessibleName(webappBundle.getString("ACSN_DefaultLocaleMapping"));
        jCbxDefaultLocale.getAccessibleContext().setAccessibleDescription(webappBundle.getString("ACSD_DefaultLocaleMapping"));

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 5);
        add(jPnlLocaleSettings, gridBagConstraints);

        jLblParameterEncoding.setLabelFor(jPnlParameterEncoding);
        jLblParameterEncoding.setText(webappBundle.getString("LBL_ParameterEncoding"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 0);
        add(jLblParameterEncoding, gridBagConstraints);

        jPnlParameterEncoding.setLayout(new java.awt.GridBagLayout());

        jPnlParameterEncoding.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jLblDefaultCharset.setLabelFor(jCbxDefaultCharset);
        jLblDefaultCharset.setText(webappBundle.getString("LBL_DefaultCharset_1"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 0);
        jPnlParameterEncoding.add(jLblDefaultCharset, gridBagConstraints);

        jCbxDefaultCharset.setPrototypeDisplayValue("");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 5);
        jPnlParameterEncoding.add(jCbxDefaultCharset, gridBagConstraints);
        jCbxDefaultCharset.getAccessibleContext().setAccessibleName(webappBundle.getString("ACSN_DefaultCharset"));
        jCbxDefaultCharset.getAccessibleContext().setAccessibleDescription(webappBundle.getString("ACSD_DefaultCharset"));

        jLblFormHintField.setLabelFor(jTxtFormHintField);
        jLblFormHintField.setText(webappBundle.getString("LBL_FormHintField_1"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipady = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 5, 0);
        jPnlParameterEncoding.add(jLblFormHintField, gridBagConstraints);

        jTxtFormHintField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jTxtFormHintFieldKeyReleased(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 5, 5);
        jPnlParameterEncoding.add(jTxtFormHintField, gridBagConstraints);
        jTxtFormHintField.getAccessibleContext().setAccessibleName(webappBundle.getString("ACSN_FormHintField"));
        jTxtFormHintField.getAccessibleContext().setAccessibleDescription(webappBundle.getString("ACSD_FormHintField"));

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 5, 5);
        add(jPnlParameterEncoding, gridBagConstraints);

    }// </editor-fold>//GEN-END:initComponents

	private void jTxtFormHintFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTxtFormHintFieldKeyReleased
		// Add your handling code here:
		WebAppRoot bean = masterPanel.getBean();
		if(bean != null) {
			LocaleCharsetInfo localeInfo = bean.getLocaleCharsetInfo();
			formHintField = jTxtFormHintField.getText();
			localeInfo.setParameterEncodingFormHintField(formHintField);
			localeInfo.setParameterEncoding(hasParameterEncoding());
			bean.setDirty();
			
			masterPanel.validateField(WebAppRoot.FIELD_FORM_HINT);
		}
	}//GEN-LAST:event_jTxtFormHintFieldKeyReleased

	private void jCbxDefaultCharsetActionPerformed(java.awt.event.ActionEvent evt) {
		// Add your handling code here:
		WebAppRoot bean = masterPanel.getBean();
		if(bean != null) {
			LocaleCharsetInfo localeInfo = bean.getLocaleCharsetInfo();
			Object item = defaultCharsetCbxModel.getSelectedItem();
			if(item instanceof CharsetMapping) {
				defaultCharset = ((CharsetMapping) item).getAlias();
				localeInfo.setParameterEncodingDefaultCharset(defaultCharset);
			} else {
				defaultCharset = null;
				localeInfo.setParameterEncodingDefaultCharset(null);
			}
			localeInfo.setParameterEncoding(hasParameterEncoding());
			bean.setDirty();
		}
	}

	private void jCbxDefaultLocaleActionPerformed(java.awt.event.ActionEvent evt) {
		// Add your handling code here:
		WebAppRoot bean = masterPanel.getBean();
		if(bean != null) {
			LocaleCharsetInfo localeInfo = bean.getLocaleCharsetInfo();
			Object item = defaultLocaleCbxModel.getSelectedItem();
			if(item instanceof LocaleMapping) {
				localeInfo.setDefaultLocale(((LocaleMapping) item).getLocale().toString());
			} else {
				localeInfo.setDefaultLocale(null);
			}
			bean.setDirty();
		}
	}
	
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup jBtnGrpCharsetAliases;
    private javax.swing.JComboBox jCbxDefaultCharset;
    private javax.swing.JComboBox jCbxDefaultLocale;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLblDefaultCharset;
    private javax.swing.JLabel jLblDefaultLocale;
    private javax.swing.JLabel jLblFormHintField;
    private javax.swing.JLabel jLblParameterEncoding;
    private javax.swing.JPanel jPnlLocaleSettings;
    private javax.swing.JPanel jPnlParameterEncoding;
    private javax.swing.JTextField jTxtFormHintField;
    // End of variables declaration//GEN-END:variables

	private ActionListener defaultLocaleActionListener;
	private ActionListener defaultCharsetActionListener;

	private void initUserComponents() {
		// Default locale combo box is initialized in initFields, when bean
		// being edited is known.
		defaultLocaleActionListener = new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jCbxDefaultLocaleActionPerformed(evt);
			}
		};

		// Init default charset combo box
		defaultCharsetCbxModel = new DefaultComboBoxModel();
		defaultCharsetCbxModel.addElement(""); // NOI18N
		SortedMap charsets = CharsetMapping.getSortedAvailableCharsetMappings();
		for(Iterator iter = charsets.entrySet().iterator(); iter.hasNext(); ) {
			CharsetMapping cm = (CharsetMapping) ((Map.Entry) iter.next()).getValue();
			defaultCharsetCbxModel.addElement(cm);
		}
		jCbxDefaultCharset.setModel(defaultCharsetCbxModel);

		defaultCharsetActionListener = new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jCbxDefaultCharsetActionPerformed(evt);
			}
		};

		/* Add locale-charset mapping table panel :
		 * TableEntry list has four properties: locale, agent, charset, description
		 */
		ArrayList tableColumns = new ArrayList(4);
		tableColumns.add(new GenericTableModel.AttributeEntry(LocaleCharsetMap.LOCALE, 
			webappBundle.getString("LBL_Locale"), true));		// NOI18N
		tableColumns.add(new GenericTableModel.AttributeEntry(LocaleCharsetMap.CHARSET, 
			webappBundle.getString("LBL_CharacterSet"), true));	// NOI18N
		tableColumns.add(new GenericTableModel.AttributeEntry(LocaleCharsetMap.AGENT, 
			webappBundle.getString("LBL_Agent")));				// NOI18N
		tableColumns.add(new GenericTableModel.ValueEntry(LocaleCharsetMap.DESCRIPTION, 
			webappBundle.getString("LBL_LocaleDescription")));		// NOI18N

//		localeCharsetMapModel = new GenericTableModel(LocaleCharsetInfo.LOCALE_CHARSET_MAP, 
//			LocaleCharsetMap.class, tableColumns);
		localeCharsetMapModel = new LocaleCharsetTableModel(tableColumns);
		localeCharsetMapPanel = new GenericTablePanel(localeCharsetMapModel, 
			webappBundle, "LocaleCharsetMapping",	// NOI18N - property name
			LocaleCharsetMapEntryPanel.class, 
			HelpContext.HELP_WEBAPP_LOCALE_MAPPING_POPUP);

		GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridwidth = GridBagConstraints.REMAINDER;
		gridBagConstraints.fill = GridBagConstraints.BOTH;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new Insets(0, 6, 0, 5);
		add(localeCharsetMapPanel, gridBagConstraints);
		
		charsetChangeListener = new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent pce) {
				updateDefaultCharsetModel();
			}
		};
	}
	
	public void addListeners() {
		localeCharsetMapModel.addTableModelListener(this);
		jCbxDefaultLocale.addActionListener(defaultLocaleActionListener);
		jCbxDefaultCharset.addActionListener(defaultCharsetActionListener);
		CharsetMapping.addPropertyChangeListener(charsetChangeListener);
	}

	public void removeListeners() {
		localeCharsetMapModel.removeTableModelListener(this);
		jCbxDefaultLocale.removeActionListener(defaultLocaleActionListener);
		jCbxDefaultCharset.removeActionListener(defaultCharsetActionListener);
		CharsetMapping.removePropertyChangeListener(charsetChangeListener);
	}	
	
	/** Initialization of all the fields in this panel from the bean that
	 *  was passed in.
	 */
	public void initFields(WebAppRoot bean) {
		LocaleCharsetInfo localeInfo = bean.getLocaleCharsetInfo();

		if(enableDefaultFields(localeInfo.sizeLocaleCharsetMap() > 0)) {
			// init default locale combo
			defaultLocaleCbxModel = getDefaultLocales(localeInfo);
			jCbxDefaultLocale.setModel(defaultLocaleCbxModel);
			
			defaultLocaleCbxModel.setSelectedItem(
				LocaleMapping.getLocaleMapping(localeInfo.getDefaultLocale()));

			// init parameter encoding fields
			if(localeInfo.isParameterEncoding()) {
				defaultCharset = localeInfo.getParameterEncodingDefaultCharset();
				formHintField = localeInfo.getParameterEncodingFormHintField();

				defaultCharsetCbxModel.setSelectedItem(CharsetMapping.getCharsetMapping(defaultCharset));		
			} else {
				defaultCharset = null;	// NOI18N
				formHintField = "";		// NOI18N

				defaultCharsetCbxModel.setSelectedItem(defaultCharset);
			}

			jTxtFormHintField.setText(formHintField);
		} else {
			jCbxDefaultLocale.setSelectedItem(null);
			jCbxDefaultCharset.setSelectedItem(null);
			jTxtFormHintField.setText("");
		}

		localeCharsetMapPanel.setModel(localeInfo, bean.getAppServerVersion());
	}
	
	private boolean enableDefaultFields(boolean hasMappings) {
		jLblDefaultLocale.setEnabled(hasMappings);
		jCbxDefaultLocale.setEnabled(hasMappings);
		
		jLblParameterEncoding.setEnabled(hasMappings);
		jLblDefaultCharset.setEnabled(hasMappings);
		jCbxDefaultCharset.setEnabled(hasMappings);
		jLblFormHintField.setEnabled(hasMappings);
		jTxtFormHintField.setEnabled(hasMappings);
		jTxtFormHintField.setEditable(hasMappings);
		
		return hasMappings;
	}
	
	private boolean hasParameterEncoding() {
		return (Utils.notEmpty(defaultCharset) || Utils.notEmpty(formHintField));
	}
	
	private DefaultComboBoxModel getDefaultLocales(LocaleCharsetInfo localeInfo) {
		DefaultComboBoxModel defaultLocaleModel = new DefaultComboBoxModel();
		
		if(localeInfo.sizeLocaleCharsetMap() > 0) {
			LocaleCharsetMap [] maps = localeInfo.getLocaleCharsetMap();
			for(int i = 0; i < maps.length; i++) {
				LocaleMapping lm = LocaleMapping.getLocaleMapping(maps[i].getLocale());
				if(lm != null) {
					defaultLocaleModel.addElement(lm);
				}
			}
		}
		
		return defaultLocaleModel;
	}
	
	private void updateDefaultLocale(LocaleCharsetInfo localeInfo) {
		int oldSize = (defaultLocaleCbxModel != null) ? defaultLocaleCbxModel.getSize() : 0;
		defaultLocaleCbxModel = getDefaultLocales(localeInfo);
		jCbxDefaultLocale.setModel(defaultLocaleCbxModel);
		
		int newSize = defaultLocaleCbxModel.getSize();

		if(oldSize == 0 && newSize > 0) {
			LocaleMapping lm = (LocaleMapping) defaultLocaleCbxModel.getElementAt(0);
			localeInfo.setDefaultLocale(lm.getLocale().toString());
			defaultLocaleCbxModel.setSelectedItem(lm);
		} else {
			LocaleMapping lm = LocaleMapping.getLocaleMapping(localeInfo.getDefaultLocale());
			if(defaultLocaleCbxModel.getIndexOf(lm) >= 0) {
				localeInfo.setDefaultLocale(lm.getLocale().toString());
				defaultLocaleCbxModel.setSelectedItem(lm);
			} else {
				localeInfo.setDefaultLocale(null);
				
				defaultCharset = null;
				localeInfo.setParameterEncodingDefaultCharset(null);
				defaultCharsetCbxModel.setSelectedItem(null);
				
				formHintField = null;
				localeInfo.setParameterEncodingFormHintField(null);
				jTxtFormHintField.setText(null);
				
				// In case it was invalid before...
				masterPanel.validateField(WebAppRoot.FIELD_FORM_HINT);
			}
		}
		
		enableDefaultFields(localeInfo.sizeLocaleCharsetMap() > 0);
	}
	
	private void updateDefaultCharsetModel() {
		Object mapping = defaultCharsetCbxModel.getSelectedItem();
		CharsetMapping oldMapping;
		
		if(mapping instanceof CharsetMapping) {
			oldMapping = (CharsetMapping) mapping;
		} else {
			oldMapping = null;
		}
		
		defaultCharsetCbxModel = new DefaultComboBoxModel();
		defaultCharsetCbxModel.addElement(""); // NOI18N
		SortedMap charsets = CharsetMapping.getSortedAvailableCharsetMappings();
		for(Iterator iter = charsets.entrySet().iterator(); iter.hasNext(); ) {
			CharsetMapping cm = (CharsetMapping) ((Map.Entry) iter.next()).getValue();
			defaultCharsetCbxModel.addElement(cm);
		}
		jCbxDefaultCharset.setModel(defaultCharsetCbxModel);
		
		if(oldMapping != null) {
			oldMapping = CharsetMapping.getCharsetMapping(oldMapping.getCharset());
		}

		defaultCharsetCbxModel.setSelectedItem(oldMapping);
	}
	
	public void tableChanged(TableModelEvent e) {
		WebAppRoot bean = masterPanel.getBean();
		if(bean != null) {
//			LocaleCharsetInfo info = bean.getLocaleCharsetInfo();
//			info.setLocaleCharsetMap(localeCharsetMapModel.getData().toArray(new LocaleCharsetMap[0]));
			if(e.getSource() == localeCharsetMapModel) {
				updateDefaultLocale(bean.getLocaleCharsetInfo());
			}
			
			// Force property change to be issued by the bean
			bean.setDirty();
		}		
	}
	
	private static class LocaleCharsetTableModel extends GenericTableModel {
		public LocaleCharsetTableModel(List tableColumns) {
			super(LocaleCharsetInfo.LOCALE_CHARSET_MAP, localeCharsetMapFactory, tableColumns);
		}
		
		public boolean alreadyExists(Object[] values) {
			boolean exists = false;
			
			List children = getChildren();
			for(Iterator iter = children.iterator(); iter.hasNext(); ) {
				LocaleCharsetMap map = (LocaleCharsetMap) iter.next();
				if(match((String) values[0], map.getLocale()) &&
				   match((String) values[1], map.getCharset()) &&
				   match((String) values[2], map.getAgent())) {
					   exists = true;
					   break;
				}
			}

			return exists;
		}
		
		private final boolean match(String a, String b) {
			boolean result = false;
			
			if(a == b) {
				result = true;
			} else if(a != null && b != null && a.equals(b)) {
				result = true;
			}
			
			return result;
		}

		public boolean alreadyExists(String keyPropertyValue) {
			// FIXME we can't actually support this API properly with the current
			// design so just have it fail.
			return false;
		}
	}
    
    // New for migration to sun DD API model.  Factory instance to pass to generic table model
    // to allow it to create localeCharsetMap beans.
	static GenericTableModel.ParentPropertyFactory localeCharsetMapFactory =
        new GenericTableModel.ParentPropertyFactory() {
            public CommonDDBean newParentProperty(ASDDVersion asVersion) {
                return StorageBeanFactory.getStorageBeanFactory(asVersion).createLocaleCharsetMap();
            }
        };
}
