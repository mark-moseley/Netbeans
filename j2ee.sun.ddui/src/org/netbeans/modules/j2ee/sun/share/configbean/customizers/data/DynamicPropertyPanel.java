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
 * DynamicPropertyPanel.java
 *
 * Created on January 29, 2004, 2:28 PM
 */

package org.netbeans.modules.j2ee.sun.share.configbean.customizers.data;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.SortedMap;
import java.util.regex.Pattern;
import java.text.MessageFormat;

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.Container;
import java.awt.Component;
import java.awt.Rectangle;

import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JComboBox;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.DefaultComboBoxModel;

import org.netbeans.modules.j2ee.sun.share.Constants;
import org.netbeans.modules.j2ee.sun.share.configbean.Utils;
import org.netbeans.modules.j2ee.sun.share.configbean.customizers.common.GenericTableDialogPanelAccessor;
import org.netbeans.modules.j2ee.sun.share.configbean.customizers.common.ValidationSupport;
import org.netbeans.modules.j2ee.sun.share.configbean.customizers.webapp.LocaleMapping;
import org.netbeans.modules.j2ee.sun.api.CharsetMapping;

/**
 *
 * @author Peter Williams
 */
public class DynamicPropertyPanel extends JPanel 
	implements GenericTableDialogPanelAccessor {
	
	// Panel state
	private boolean isEditPopup;	// <edit> versus <new>
		
	// Data support
	private PropertyList theList;
	private List paramMappings;
	private boolean hasDescription;
	private boolean isEditable;
	
	// Visual Components
	private JLabel nameRequiredMark;
	private JLabel nameLabel;
	private JLabel valueRequiredMark ;
	private JLabel valueLabel;
	private JComboBox propertiesCombo;
	private JComponent customEditor;
	private JTextField descriptionField;
	
	// Swappable components for custom editor
	private JTextField customTextField;
	private JComboBox customComboBox;
	private JCheckBox customCheckBox;
	private int normalizedHeight;
	
	// Data models
	private DefaultComboBoxModel propertyListModel;
	
	// Field data storage
	private String name;
	private ParamMapping nameMapping;
	private String value;
	private String description;
	
	// Standard resource bundle to use for non-property list fields
	private static final ResourceBundle localBundle = ResourceBundle.getBundle(
		"org.netbeans.modules.j2ee.sun.share.configbean.customizers.common.Bundle");	// NOI18N

	// Special resource bundle to use for this property list
	private ResourceBundle propertyListBundle;
	
	// Probably should put these in utils or something - text for boolean properties
	// that gets written to sun-xxx.xml.
	private static final String TEXT_TRUE="true";	// NOI18N
	private static final String TEXT_FALSE="false";	// NOI18N
	

	/** Creates new form DynamicPropertyPanel */
	public DynamicPropertyPanel() {
		// initialization is entirely performed in the init() method due to the
		// dynamic nature of the controls in this panel.
	}
	
	/** This method is called from within the constructor to
	 * initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is
	 * always regenerated by the Form Editor.
	 */
    private void initComponents() {//GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;

        setLayout(new java.awt.GridBagLayout());

    }//GEN-END:initComponents
	
	
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
	
	private void initUserComponents() {
		
		// Create the custom editors and cache some sizing values
		//
		customTextField = new JTextField();
		customComboBox = new JComboBox();
		customCheckBox = new JCheckBox();
		
		nameRequiredMark = new JLabel();
		nameLabel = new JLabel();
		propertiesCombo = new JComboBox();
		valueRequiredMark = new JLabel();
		valueLabel = new JLabel();
		customEditor = customTextField;  // Use text field initially.

		GridBagConstraints gridBagConstraints;
		
		int textFieldHeight = customTextField.getPreferredSize().height;
		int comboBoxHeight = customComboBox.getPreferredSize().height;
		int checkBoxHeight = customCheckBox.getPreferredSize().height;
		normalizedHeight = Math.max(textFieldHeight, Math.max(comboBoxHeight, checkBoxHeight));
		
		// Add controls
		//
        nameRequiredMark.setText(localBundle.getString("LBL_RequiredMark"));	// NOI18N
		nameRequiredMark.setLabelFor(propertiesCombo);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new Insets(4, 4, 4, 0);
		nameRequiredMark.getAccessibleContext().setAccessibleName(localBundle.getString("ACSN_RequiredMark"));	// NOI18N
		nameRequiredMark.getAccessibleContext().setAccessibleDescription(localBundle.getString("ACSD_RequiredMark"));	// NOI18N
        add(nameRequiredMark, gridBagConstraints);
		
        nameLabel.setText(localBundle.getString("LBL_Name_1"));	// NOI18N
		nameLabel.setDisplayedMnemonic(localBundle.getString("MNE_Name").charAt(0));	// NOI18N
		nameLabel.setLabelFor(propertiesCombo);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new Insets(4, 4, 4, 4);
        add(nameLabel, gridBagConstraints);
		
		propertiesCombo.setEditable(isEditable);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridwidth = GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new Insets(4, 4, 4, 4);
        gridBagConstraints.weightx = 1.0;
		propertiesCombo.getAccessibleContext().setAccessibleName(localBundle.getString("ACSN_Name"));	// NOI18N
		propertiesCombo.getAccessibleContext().setAccessibleDescription(localBundle.getString("ACSD_Name"));	// NOI18N
        add(propertiesCombo, gridBagConstraints);
		
        propertiesCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
				handlePropertiesComboActionPerformed(evt);
            }
        });
		
        valueRequiredMark.setText(localBundle.getString("LBL_RequiredMark"));	// NOI18N
		valueRequiredMark.setLabelFor(customEditor);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new Insets(4, 4, 4, 0);
		valueRequiredMark.getAccessibleContext().setAccessibleName(localBundle.getString("ACSN_RequiredMark"));	// NOI18N
		valueRequiredMark.getAccessibleContext().setAccessibleDescription(localBundle.getString("ACSD_RequiredMark"));	// NOI18N
        add(valueRequiredMark, gridBagConstraints);
		
		valueLabel.setText(localBundle.getString("LBL_Value_1"));	// NOI18N
		valueLabel.setDisplayedMnemonic(localBundle.getString("MNE_Value").charAt(0));	// NOI18N
		valueLabel.setLabelFor(customEditor);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new Insets(4, 4, 4, 4);
        add(valueLabel, gridBagConstraints);
		
		int extraHeight = normalizedHeight - textFieldHeight;
		int extraBelow = extraHeight/2;
		int extraAbove = extraHeight-extraBelow;
		
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridwidth = GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new Insets(extraAbove+4, 4, extraBelow+4, 4);
        gridBagConstraints.weightx = 1.0;
		customTextField.getAccessibleContext().setAccessibleName(localBundle.getString("ACSN_Value"));	// NOI18N
		customTextField.getAccessibleContext().setAccessibleDescription(localBundle.getString("ACSD_Value"));	// NOI18N
		customComboBox.getAccessibleContext().setAccessibleName(localBundle.getString("ACSN_Value"));	// NOI18N
		customComboBox.getAccessibleContext().setAccessibleDescription(localBundle.getString("ACSD_Value"));	// NOI18N
		customCheckBox.getAccessibleContext().setAccessibleName(localBundle.getString("ACSN_Value"));	// NOI18N
		customCheckBox.getAccessibleContext().setAccessibleDescription(localBundle.getString("ACSD_Value"));	// NOI18N
        add(customEditor, gridBagConstraints);
		
		if(hasDescription) {
			JLabel descRequiredMark = new JLabel();
			JLabel descLabel = new JLabel();
			descriptionField = new JTextField();

			// Description required mark is just filler, as description is never
			// required.  But we need it to make the layout look nice.
			gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints.insets = new Insets(4, 4, 4, 0);
			add(descRequiredMark, gridBagConstraints);
		
			descLabel.setText(localBundle.getString("LBL_Description_1"));	// NOI18N
			descLabel.setDisplayedMnemonic(localBundle.getString("MNE_Description").charAt(0));	// NOI18N
			descLabel.setLabelFor(descriptionField);
			gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints.insets = new Insets(4, 4, 4, 4);
			add(descLabel, gridBagConstraints);

			gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.gridwidth = GridBagConstraints.REMAINDER;
			gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints.insets = new Insets(4, 4, 4, 4);
			gridBagConstraints.weightx = 1.0;
			descriptionField.getAccessibleContext().setAccessibleName(localBundle.getString("ACSN_Description"));	// NOI18N
			descriptionField.getAccessibleContext().setAccessibleDescription(localBundle.getString("ACSD_Description"));	// NOI18N
			add(descriptionField, gridBagConstraints);
			
			descriptionField.addKeyListener(new java.awt.event.KeyAdapter() {
				public void keyReleased(java.awt.event.KeyEvent evt) {
					handleDescriptionFieldKeyReleased(evt);
				}
			});			
		}
		
		// Add listeners to custom editors
        customTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                handleCustomTextFieldKeyReleased(evt);
            }
        });
		
        customCheckBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                handleCustomCheckBoxItemStateChanged(evt);
            }
        });	
		
		customComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                handleCustomComboBoxActionPerformed(evt);
            }
        });
		
		// Create and set data model for property selector
		//
		buildMappings();
		propertyListModel = new DefaultComboBoxModel();
		for(Iterator iter = paramMappings.iterator(); iter.hasNext(); ) {
			propertyListModel.addElement((ParamMapping) iter.next());
		}
		propertiesCombo.setModel(propertyListModel);
	}
	
	private void handleCustomTextFieldKeyReleased(java.awt.event.KeyEvent evt) {
		if(customEditor == customTextField) {
			value = customTextField.getText();
			firePropertyChange(Constants.USER_DATA_CHANGED, null, null);
		}
	}

	private void handleCustomCheckBoxItemStateChanged(java.awt.event.ItemEvent evt) {
		if(customEditor == customCheckBox) {
			value = interpretCheckboxState(evt) ? TEXT_TRUE : TEXT_FALSE;
			firePropertyChange(Constants.USER_DATA_CHANGED, null, null);
		}	
	}
	
	private void handleCustomComboBoxActionPerformed(java.awt.event.ActionEvent evt) {
		if(customEditor == customComboBox) {
			if("comboBoxChanged".equals(evt.getActionCommand())) {	// NOI18N
				Object selectedItem = customComboBox.getSelectedItem();
				
				if(selectedItem instanceof String) {
					value = (String) selectedItem;
				} else if(selectedItem instanceof CharsetMapping) {
					value = ((CharsetMapping) selectedItem).getCharset().toString();
				} else if(selectedItem instanceof LocaleMapping) {
					value = ((LocaleMapping) selectedItem).getLocale().toString();
				}
				
				firePropertyChange(Constants.USER_DATA_CHANGED, null, null);
			}
		}
	}

	private void handleDescriptionFieldKeyReleased(java.awt.event.KeyEvent evt) {
		if(hasDescription) {
			description = descriptionField.getText();
			firePropertyChange(Constants.USER_DATA_CHANGED, null, null);
		}	
	}
	
	private void handlePropertiesComboActionPerformed(java.awt.event.ActionEvent evt) {
		if("comboBoxChanged".equals(evt.getActionCommand())) {	// NOI18N
			Object selectedItem = propertyListModel.getSelectedItem();
//			System.out.println("property selected: is type: " + selectedItem.getClass().getName());
			if(selectedItem instanceof ParamMapping) {
				// param item selected
				nameMapping = (ParamMapping) selectedItem;
				selectProperty(nameMapping);
			} else if(selectedItem instanceof String) { // && evt.getActionCommand().equals("comboBoxEdited")) {
				// typed in item selected
				nameMapping = null;
				name = (String) selectedItem;
				
				// add the selected item to the list.
				if(propertyListModel.getIndexOf(selectedItem) == -1) {
					propertyListModel.addElement(selectedItem);
				}
				
				enableTextFields(true);
				displayCustomEditor(customTextField);					
			}
			
			firePropertyChange(Constants.USER_DATA_CHANGED, null, null);
		}
	}
	
	private void selectProperty(ParamMapping pm) {
		PropertyParam param = pm.getParam();
		if(param != null) {
			ParamType paramType = param.getParamType();
			String type = paramType.getType();
			enableTextFields(true);
			
			valueRequiredMark.setText(paramType.getRequired().equals(TEXT_TRUE) ? 
				localBundle.getString("LBL_RequiredMark") : "");	// NOI18N
			String paramLabel = param.getParamLabel();
			if(paramLabel != null) {
				valueLabel.setText(propertyListBundle.getString(paramLabel) + " :");	// NOI18N
			} else {
				valueLabel.setText(localBundle.getString("LBL_Value_1"));	// NOI18N
			}

			value = param.getDefaultValue();
			
			if(type.equals("boolean")) {	// NOI18N
				customCheckBox.setSelected(value.equals(TEXT_TRUE));
				displayCustomEditor(customCheckBox);
			} else if(type.equals("text")) {	// NOI18N
				customTextField.setText(value);
				customTextField.setCaretPosition(0);
				displayCustomEditor(customTextField);
			} else if(type.equals("number")) {	// NOI18N
				customTextField.setText(value);
				customTextField.setCaretPosition(0);
				displayCustomEditor(customTextField);
			} else if(type.equals("list")) {	// NOI18N
				// get model list and set model to custom combo box
				DefaultComboBoxModel valueListModel = new DefaultComboBoxModel();
				Object defaultValue = value;
				
				if(paramType.isParamCharset()) {
					SortedMap charsets = CharsetMapping.getSortedAvailableCharsetMappings();
					for(Iterator iter = charsets.entrySet().iterator(); iter.hasNext(); ) {
						CharsetMapping cm = (CharsetMapping) ((Map.Entry) iter.next()).getValue();
						valueListModel.addElement(cm);
					}
					
					if(value != null) {
						defaultValue = CharsetMapping.getCharsetMapping(value);
					} else {
						defaultValue = CharsetMapping.getCharsetMapping("UTF8");
					}
				} else if(paramType.isParamLocale()) {
					SortedMap locales = LocaleMapping.getSortedAvailableLocaleMappings();
					for(Iterator iter = locales.entrySet().iterator(); iter.hasNext(); ) {
						LocaleMapping lm = (LocaleMapping) ((Map.Entry) iter.next()).getValue();
						valueListModel.addElement(lm);
					}
					
					if(value != null) {
						defaultValue = LocaleMapping.getLocaleMapping(value);
					} else {
						defaultValue = LocaleMapping.getLocaleMapping(Locale.getDefault());
					}
				} else {
					for(int i = 0, n = paramType.sizeParamValue(); i < n; i++) {
						valueListModel.addElement(paramType.getParamValue(i));
					}
					
					// Defaults to first item if none specified.
					if(defaultValue == null) {
						defaultValue = valueListModel.getElementAt(0);
					}
				}
				
				customComboBox.setModel(valueListModel);
				customComboBox.setEditable(paramType.getEditable().equals(TEXT_TRUE));
				customComboBox.setSelectedItem(defaultValue);
				
				displayCustomEditor(customComboBox);
			}
			
			// Show default description, if there is one.
			if(hasDescription) {
				String defaultDesc = param.getParamDescription();
				if(defaultDesc != null) {
					description = propertyListBundle.getString(defaultDesc);
					descriptionField.setText(description);
					descriptionField.setCaretPosition(0);
				}
			}
		} else {
			// null param means "blank" property is selected
			// use disabled readonly edit control for value entry component.
			enableTextFields(false);
			displayCustomEditor(customTextField);
		}
	}
	
	private boolean interpretCheckboxState(java.awt.event.ItemEvent e) {
		boolean state = false;
		
		if(e.getStateChange() == java.awt.event.ItemEvent.SELECTED) {
			state = true;
		} else if(e.getStateChange() == java.awt.event.ItemEvent.DESELECTED) {
			state = false;
		}
		
		return state;
	}
	
	private void buildMappings() {
		List properties = theList.fetchPropertyParamList();
		paramMappings = new ArrayList(properties.size()+1);
		
		// only non-editable property lists can use the null mapping.
		if(!isEditable) {
			paramMappings.add(new ParamMapping(null)); // Represents blank entry
		}
		
		for(Iterator iter = properties.iterator(); iter.hasNext(); ) {
			PropertyParam pp = (PropertyParam) iter.next();
			paramMappings.add(new ParamMapping(pp));
		}
	}
	
	private void displayCustomEditor(JComponent newCustomEditor) {
		if(customEditor != newCustomEditor) {
			int extraHeight = normalizedHeight - newCustomEditor.getPreferredSize().height;
			int extraBelow = extraHeight/2;
			int extraAbove = extraHeight-extraBelow;
			
			GridBagConstraints constraints = new GridBagConstraints();
			constraints.gridwidth = GridBagConstraints.REMAINDER;
			constraints.fill = GridBagConstraints.HORIZONTAL;
			constraints.insets = new Insets(extraAbove+4, 4, extraBelow+4, 4);
			constraints.weightx = 1.0;

			// Reset labels
			valueRequiredMark.setLabelFor(newCustomEditor);
			valueLabel.setLabelFor(newCustomEditor);	
			
			// Remove old control, add the new one, and display them.
			int customEditorIndex = componentIndexOf(customEditor);
			remove(customEditor);
			add(newCustomEditor, constraints, customEditorIndex);
			
			// Repack the controls if we're already displayed -- if parent is null
			// then there is no need to repack yet.
			Window parentWindow = getParentWindow();
			if(parentWindow != null) {
				parentWindow.pack();
			}
			
			// !PW I'm not sure if this is a hack, or the real fix for this.
			// For some reason, when the new control is smaller than the old 
			// control (though we've increased the insets to account for this),
			// there is a repainting issue where the non-overlapped parts of the
			// old control do not get erased.  So, after we've displayed the new
			// control (via pack()), we invalidate the region occupied by the 
			// entire control, including any extra space we added.
			//
			Rectangle bounds = newCustomEditor.getBounds(null);
			bounds.y -= extraAbove;
			bounds.height += extraHeight;
			repaint(bounds);
			
			customEditor = newCustomEditor;
		}
	}
	
	private int componentIndexOf(JComponent control) {
		int result = -1;
		
		Component [] children = getComponents();
		for(int i = 0; i < children.length; i++) {
			if(control == children[i]) {
				result = i;
				break;
			}
		}
		
		return result;
	}
	
	private void enableTextFields(boolean flag) {
		value = null;
		customTextField.setText(value);
		customTextField.setEditable(flag);
		customTextField.setEnabled(flag);		

		if(hasDescription) {
			description = null;
			descriptionField.setText(description);
			descriptionField.setEditable(flag);
			descriptionField.setEnabled(flag);		
		}
	}
	
	private Window getParentWindow() {
		for(Container parent = getParent(); parent != null; parent = parent.getParent()) {
			if(parent instanceof Window) {
				return (Window) parent;
			}
		}
		
		return null;
	}
	
	/** -----------------------------------------------------------------------
	 *  GenericTableDialogPanelAccessor implementation
	 */
	// Field indices (maps to values[] handled by get/setValues()
	private static final int NAME_FIELD = 0;
	private static final int VALUE_FIELD = 1;
	private static final int DESCRIPTION_FIELD = 2;
	private static final int NUM_FIELDS_NO_DESCRIPTION = 2;		// Number of objects expected in get/setValue methods.
	private static final int NUM_FIELDS_WITH_DESCRIPTION = 3;	// Number of objects expected in get/setValue methods.
	
	public void init(int preferredWidth, List entries, Object data) {
		theList = (PropertyList) data;
		hasDescription = theList.getDescription().equals(TEXT_TRUE);
		isEditable = theList.getEditable().equals(TEXT_TRUE);
		
		String bundlePath = theList.getBundlePath();
		if(bundlePath != null) {
			propertyListBundle = ResourceBundle.getBundle(bundlePath);
		}
		
		initComponents();
		initUserComponents();		
		
		setPreferredSize(new Dimension(preferredWidth, getPreferredSize().height));
		
		if(!isEditable) {
			// If this is a new operation, select the null property, disable the
			// entry fields and offer name selection hint.
			selectProperty((ParamMapping) paramMappings.get(0));
		}
	}
	
	public Object[] getValues() {
		Object [] result;
		
		if(hasDescription) {
			result = new Object[NUM_FIELDS_WITH_DESCRIPTION];
		} else {
			result = new Object[NUM_FIELDS_NO_DESCRIPTION];
		}
		
		if(nameMapping != null) {
			PropertyParam param = nameMapping.getParam();
			if(param != null) {
				result[NAME_FIELD] = param.getParamName();
			} else {
				result[NAME_FIELD] = ""; // NOI18N
			}
		} else {
			result[NAME_FIELD] = name;
		}

		result[VALUE_FIELD] = value;
		
		if(hasDescription) {
			result[DESCRIPTION_FIELD] = description;
		}
		
		return result;
	}
	
	public void setValues(Object[] values) {
		// Now calling this method with null if this is a <new> action, so
		// set values to default array and set <new> vs. <edit> flag.
		if(values == null) {
			values = new Object [hasDescription ? NUM_FIELDS_WITH_DESCRIPTION : NUM_FIELDS_NO_DESCRIPTION];
			values[NAME_FIELD] = null;
			values[VALUE_FIELD] = null;
			
			if(hasDescription) {
				values[DESCRIPTION_FIELD] = null;
			}
			
			isEditPopup = false;
		} else {
			isEditPopup = true;
		}
		
		// Normal, the pattern for this method is to translate each of the value
		// Objects into native form (string or combobox mapping, usually) and then
		// set all the components via a setComponentValues() method.  But due to
		// the dynamic nature of the name component, setting the name component
		// value causes the other data elements to be set to default values.  So
		// we initialize the name component ahead of setting the value and
		// description data members.
		name = (String) values[NAME_FIELD];
		nameMapping = null;
		
		// Matching name mapping is convoluted, partly due to the possibility of
		// ParamMappings with a null Param (which is assumed to be the first
		// entry if it exists, by the way.
		//
		if(name != null && name.length() > 0) {
			for(Iterator iter = paramMappings.iterator(); iter.hasNext(); ) {
				ParamMapping pm = (ParamMapping) iter.next();
				PropertyParam param = pm.getParam();
				if(param != null && name.equals(param.getParamName())) {
					nameMapping = pm;
					break;
				}
			}
		} else if(paramMappings.size() > 0) {
			ParamMapping pm = (ParamMapping) paramMappings.get(0);
			if(pm.getParam() == null) {
				nameMapping = pm;
			}
		}
		
		if(nameMapping != null) {
			propertiesCombo.setSelectedItem(nameMapping);
		} else {
			// FIXME should only happen with editable combo boxes, but could happen
			// if the user hand edits - we should ensure appropriate contingencies
			// in that case.
			propertiesCombo.setSelectedItem(name); 
		}
		
		// setValues is called with null for New popups and with a values array
		// for edit popups.
		//
		// For New popups, default value & description were set when the property
		// selection field was initialed (to blank, btw).
		// For Edit popups, we'll disable the name selection field, and set the
		// value and description fields to the passed in values, as appropriate.
		if(isEditPopup) {
			// Disable the name selection field in Edit mode.  This makes
			// validation significantly easier.
			propertiesCombo.setEnabled(false);
					
			value = (String) values[VALUE_FIELD];

			if(customEditor == customCheckBox) {
				customCheckBox.setSelected(Utils.booleanValueOf(value));
			} else if(customEditor == customTextField) {
				customTextField.setText(value);
				customTextField.setCaretPosition(0);
			} else if(customEditor == customComboBox) {
				// this combo is just a list of Strings, so no mapping object needed.
				customComboBox.setSelectedItem(value); 
			}		

			if(hasDescription) {
				description = (String) values[DESCRIPTION_FIELD];
				descriptionField.setText(description);
				descriptionField.setCaretPosition(0);
			}
		}
	}
	
	public boolean requiredFieldsFilled() {
		return true;
	}
	
	public Collection getErrors(ValidationSupport validationSupport) {
		ArrayList errorList = new ArrayList();
		
		if(hasEmptyName()) {
			errorList.add(localBundle.getString("ERR_NameFieldIsEmpty"));	// NOI18N
		} else if(nameMapping != null) {
			// Only values with a 'param-type' entry can have a validator
			if(value == null || value.length() == 0) {
//				errorList.add(localBundle.getString("ERR_ValueFieldIsEmpty"));
				Object [] args = new Object [1];
				args[0] = localBundle.getString("LBL_Value");
				errorList.add(MessageFormat.format(localBundle.getString("ERR_SpecifiedFieldIsEmpty"), args));
			} else {
				PropertyParam param = nameMapping.getParam();  // Not null here due to hasEmptyName() call, above
				ParamType paramType = param.getParamType();
				String type = paramType.getType();
				String name = param.getParamName();

				if(type.equals("text")) {	// NOI18N
					validate(value, param.getParamValidator(), name, errorList);
				} else if(type.equals("number")) {	// NOI18N
					validateNumber(value, paramType.getParamMin(), paramType.getParamMax(), errorList);
				} else if(type.equals("list")) {	// NOI18N
					validate(value, param.getParamValidator(), name, errorList);
				}
			}
		}
		
		return errorList;
	}
	
	/** Determine if the name field represents a blank value.  This is a bit 
	 *  complicated since the name could be a ParamMapping that may or may not
	 *  be null, or it could be a String (that may or may not be null).
	 */
	private boolean hasEmptyName() {
		boolean result = true;
		
		if(nameMapping != null && nameMapping.getParam() != null) {
			result = false; // param name is not null
		} else if(nameMapping == null && name != null && name.length() > 0) {
			result = false; // param name is null, but typed-in name is not empty
		}
		
		return result;
	}
	
	/** Validate the numeric string passed in with optional range, and add any
	 *  error messages to the list.
	 *
	 * @param theNumber The number (as string) to validate
	 * @param paramMin The minimum value (can be null)
	 * @param paramMax The maximum value (can be null)
	 * @param errorList The list to add any error messages to.
	 */
	private void validateNumber(String theNumber, String paramMin, String paramMax, List errorList) {
		try {
			long x = Long.parseLong(theNumber);
			long min = Long.MIN_VALUE;
			long max = Long.MAX_VALUE;

			if(paramMin != null) {
				min = Long.parseLong(paramMin);
				if(x < min) {
					Object [] args = new Object [] { theNumber, Long.toString(min) };
					errorList.add(MessageFormat.format(
						localBundle.getString("ERR_NumberTooLow"), args));	// NOI18N
				}
			}

			if(paramMax != null) {
				max = Long.parseLong(paramMax);
				if(x > max) {
					Object [] args = new Object [] { theNumber, Long.toString(max) };
					errorList.add(MessageFormat.format(
						localBundle.getString("ERR_NumberTooHigh"), args));	// NOI18N
				}
			}
		} catch(NumberFormatException ex) {
			Object [] args = new Object [] { theNumber };
			errorList.add(MessageFormat.format(
				localBundle.getString("ERR_NumberInvalid"), args));	// NOI18N
		}
	}
	
	/** Validate the string passed according the to validator specified.
	 *
	 * @param value The string to validate
	 * @param validatorName The name of the validator to look up.  (Must have
	 *   been defined in validator section of input XML file)
	 * @param proprtyName The name of the property being edited.  This is for
	 *   any resulting error message.
	 * @param errorList The list to add any error messages to.
	 */
	private void validate(String value, String validatorName, String propertyName, List errorList) {
		if(validatorName != null) {
			Pattern validatorPattern = PropertyListMapping.getValidator(validatorName);
			if(validatorPattern != null) {
				if(!validatorPattern.matcher(value).matches()) {
					Object [] args = new Object [] { propertyName, validatorName };
					String propInvalidMsg = localBundle.getString("ERR_PropertyValueInvalid");	// NOI18N
					errorList.add(MessageFormat.format(propInvalidMsg, args));
				}
			}
		}
	}
	
}
