/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/**
 * EditPanelQuery.java
 *
 *
 * Created: Mon Feb  5 13:34:46 2001
 *
 * @author Ana von Klopp
 * @author Simran Gleason
 * @version
 */

/**
 * Contains the Query sub-panel for the EditPanel
 */


package org.netbeans.modules.web.monitor.client; 

import java.awt.event.*;
import java.awt.Component;
import java.util.ResourceBundle;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import org.openide.NotifyDescriptor;
import org.openide.TopManager;
import org.openide.util.NbBundle;

import org.netbeans.modules.web.monitor.data.*;

public class EditPanelQuery extends DataDisplay {

    private final static boolean debug = false;
    private static final ResourceBundle msgs =
	NbBundle.getBundle(TransactionView.class);
 
    private DisplayTable paramTable = null; 

    private MonitorData monitorData = null;
    private EditPanel editPanel;
    private boolean setParams = false;

    //
    // Widgets
    //
    JButton    newParamB;
    JButton    deleteParamB;
    JTextField queryStringText;
    JTextField uploadFileText;
 
    public EditPanelQuery(MonitorData md, EditPanel editPanel) {
	super();
	this.editPanel = editPanel;
	this.monitorData = md;
    }

    //
    // stoopid versio nof redisplayData: nuke it all & start again.
    //
    public void redisplayData() {
	setData(monitorData);
    }
    
    // We're treating these as if they are all strings at the
    // moment. In reality they can be of different types, though maybe 
    // that does not matter...
    public void setData(MonitorData md) {

	this.monitorData = md;
	
	if(debug) System.out.println("in EditPanelQuery.setData()"); // NOI18N
	this.removeAll();

	int fullGridWidth = java.awt.GridBagConstraints.REMAINDER;
	int gridy = -1;
	 

	addGridBagComponent(this, createTopSpacer(), 0, ++gridy,
			    fullGridWidth, 1, 0, 0, 
			    java.awt.GridBagConstraints.WEST,
			    java.awt.GridBagConstraints.NONE,
			    topSpacerInsets,
			    0, 0);
	
	// The query parameters: PENDING
	//
	// 1. Get request - add a parameter one at a time. We should not
	//    allow the user to edit the query string. 
	// 2. POST request. Allow the user to edit the query string, 
	//    and then there are two cases: 
        //    a) add a parameter one at a time
	//    b) add data from file
	// 3. PUT request. Allow the user to enter data from a file
	//    only. They can edit the query string as well. 
	
	final RequestData rd = monitorData.getRequestData();
	String method = rd.getAttributeValue(EditPanel.METHOD);
	if (EditPanel.POST.equals(method)) {
	    queryStringText =
		new JTextField(rd.getAttributeValue("queryString")); // NOI18N
	    queryStringText.addFocusListener(new FocusListener() {
		public void focusGained(FocusEvent evt) {
		}
		public void focusLost(FocusEvent evt) {
		    // PENDING - check that this works
		    rd.setAttributeValue("queryString", //NOI18N
					 queryStringText.getText());
		}
	    });

	    addGridBagComponent(this, createHeaderLabel(msgs.getString("MON_Querystring"), msgs.getString("MON_Querystring_Mnemonic").charAt(0), msgs.getString("ACS_MON_QuerystringA11yDesc"), paramTable),
                                0, ++gridy,
				1, 1, 0, 0, 
				java.awt.GridBagConstraints.WEST,
				java.awt.GridBagConstraints.NONE,
				labelInsets,
				0, 0);
	    addGridBagComponent(this, queryStringText, 0, ++gridy,
				fullGridWidth, 1, 1.0, 0, 
				java.awt.GridBagConstraints.WEST,
				java.awt.GridBagConstraints.HORIZONTAL,
				tableInsets,
				0, 0);
	}
	    

	String msg2 = null;
	Component msg2Label;

	if (EditPanel.PUT.equals(method)) {
	    msg2 = msgs.getString("MON_Upload_File");
	    msg2Label = createDataLabel(msg2);
	    addGridBagComponent(this, msg2Label, 0, ++gridy,
				fullGridWidth, 1, 0, 0, 
				java.awt.GridBagConstraints.WEST,
				java.awt.GridBagConstraints.NONE,
				labelInsets,
				0, 0);
	    
	    String uploadFileMsg = msgs.getString("MON_Upload_File_Not_Supported");
	    uploadFileText = new JTextField(uploadFileMsg);
	    uploadFileText.setEnabled(false);
	    addGridBagComponent(this, uploadFileText, 0, ++gridy,
				fullGridWidth, 1, 1.0, 0, 
				java.awt.GridBagConstraints.WEST,
				java.awt.GridBagConstraints.HORIZONTAL,
				labelInsets,
				0, 0);
	    addGridBagComponent(this, createGlue(), 0, ++gridy,
				1, 1, 1.0, 1.0, 
				java.awt.GridBagConstraints.WEST,
				java.awt.GridBagConstraints.BOTH,
				labelInsets,
				0, 0);

	} else if (method != null) {  // GET or POST

	    Param[] params2 = rd.getParam();
	    if (params2 == null) params2 = new Param[0];
	    setParameters(params2);
            char mnemonic = ' ';
            String ad = null;

	    if(method.equals(EditPanel.GET)) {
		msg2 = msgs.getString("MON_Query_parameters");
                mnemonic = msgs.getString("MON_Query_parameters_Mnemonic").charAt(0);
                ad = msgs.getString("ACS_MON_Query_parametersA11yDesc");
                paramTable.getAccessibleContext().setAccessibleName(msgs.getString("ACS_MON_ParametersTableA11yDesc"));
                paramTable.setToolTipText(msgs.getString("ACS_MON_ParametersTableA11yDesc"));
		
	    } else if(method.equals(EditPanel.POST)) {
		msg2 = msgs.getString("MON_Posted_data");
                mnemonic = msgs.getString("MON_Posted_data_Mnemonic").charAt(0);
                ad = msgs.getString("ACS_MON_Posted_dataA11yDesc");
                paramTable.getAccessibleContext().setAccessibleName(msgs.getString("ACS_MON_Posted_dataTableA11yName"));
                paramTable.setToolTipText(msgs.getString("ACS_MON_Posted_dataTableA11yDesc"));
	    }

	    msg2Label = createSortButtonLabel(msg2, paramTable, mnemonic, ad);

	    addGridBagComponent(this, msg2Label, 0, ++gridy,
				1, 1, 0, 0, 
				java.awt.GridBagConstraints.WEST,
				java.awt.GridBagConstraints.NONE,
				labelInsets,
				0, 0);

	    gridy = addParamTable(this, params2, gridy);
	}
	
	setEnablings();

	// Housekeeping
	this.setMaximumSize(this.getPreferredSize()); 
	this.repaint();

    }

    private int addParamTable(JPanel panel, Param[] params, int gridy) {
	
	JScrollPane scrollpane = new JScrollPane(paramTable);

	addGridBagComponent(panel, scrollpane, 0, ++gridy,
			    fullGridWidth, 1, 1.0, 1.0, 
			    java.awt.GridBagConstraints.WEST,
			    //java.awt.GridBagConstraints.HORIZONTAL, 
			    java.awt.GridBagConstraints.BOTH,
			    tableInsets,
			    0, 0);

	newParamB = new JButton(msgs.getString("MON_New_param"));
        newParamB.setMnemonic(msgs.getString("MON_New_param_Mnemonic").charAt(0));
        newParamB.setToolTipText(msgs.getString("ACS_MON_New_paramA11yDesc"));
	newParamB.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {

		    String title = msgs.getString("MON_New_param"); 
		    ParamEditor pe = new ParamEditor("", "", //NOI18N
						     true, true,
						     title, false);

		    if(debug) System.out.println("Now showing dialog"); // NOI18N
		    
		    pe.showDialog(true);

		    if(debug) System.out.println("Dialog closed"); // NOI18N

		    if (pe.getDialogOK()) {

			if(debug) System.out.println("Dialog returned OK"); // NOI18N
			String name = pe.getName();
			String value = pe.getValue();
			Param newParam = new Param(name, value);
			RequestData rd = monitorData.getRequestData();
			int nth = rd.addParam(newParam);
			redisplayData();
		    }
		}});

	deleteParamB = new JButton(msgs.getString("MON_Delete_param"));
        deleteParamB.setMnemonic(msgs.getString("MON_Delete_param_Mnemonic").charAt(0));
        deleteParamB.setToolTipText(msgs.getString("ACS_MON_Delete_paramA11yDesc"));

	deleteParamB.addActionListener(new ActionListener() {

		public void actionPerformed(ActionEvent e) {
		
		    int numRows = paramTable.getRowCount();
		    RequestData rd = monitorData.getRequestData();
		
		    StringBuffer buf = new StringBuffer
			(msgs.getString("MON_Confirm_Delete_Params")); 
		    buf.append("\n"); //NOI18N
		
		    for(int i=0; i<numRows; ++i) {
		    
			if(paramTable.isRowSelected(i)) {
			    buf.append(paramTable.getValueAt(i, 0));
			    buf.append("="); //NOI18N
			    buf.append(paramTable.getValueAt(i, 1));
			    buf.append("\n"); //NOI18N
			}
		    }
		
		    showConfirmDialog(buf.toString()); 
		
		    if(setParams) {

			for(int i=0; i<numRows; ++i) {
			
			    if(paramTable.isRowSelected(i)) {
				String name = 
				    (String)paramTable.getValueAt(i, 0);
				String value = 
				    (String)paramTable.getValueAt(i, 1);

				// Note that we get the params each
				// time through so that we don't run
				// into null pointer exceptions. 
				Param[] params2 = rd.getParam();
				Param param = findParam(params2, name, value);
				if (param != null) 
				    rd.removeParam(param);
			    }
			}
			redisplayData();
			repaint();
		    }
		}});
	int gridx = -1;
	addGridBagComponent(this, createGlue(), ++gridx, ++gridy,
			    1, 1, 1.0, 0, 
			    java.awt.GridBagConstraints.WEST,
			    java.awt.GridBagConstraints.NONE,
			    buttonInsets,
			    0, 0);
	addGridBagComponent(this, newParamB, ++gridx, gridy,
			    1, 1, 0, 0, 
			    java.awt.GridBagConstraints.EAST,
			    java.awt.GridBagConstraints.NONE,
			    buttonInsets,
			    0, 0);

	addGridBagComponent(this, deleteParamB, ++gridx, gridy,
			    1, 1, 0, 0, 
			    java.awt.GridBagConstraints.EAST,
			    java.awt.GridBagConstraints.NONE,
			    buttonInsets,
			    0, 0);
	return gridy;
    }

    /**
     * find the param with the given name and value from the list.
     */
    private Param findParam(Param [] myParams, String name, String value) {
	for (int i=0; i < myParams.length; i++) {
	    Param param = myParams[i];
	    if (name.equals(param.getName()) &&
		value.equals(param.getValue()) ) {
		return param;
	    }
	}
	return null;
    }

    public void setEnablings() {
	//
	// Always enable the Add button.
	//
	newParamB.setEnabled(true);

	int selectedRows[] = paramTable.getSelectedRows();
	//
	// The edit button is enabled if exactly one row is selected
	//
	//editParamB.setEnabled(selectedRows.length == 1);

	//
	// The delete row button is enabled if any rows are selected.
	//
	deleteParamB.setEnabled(selectedRows.length > 0);
    }

    public void repaint() {
	super.repaint();
	if (editPanel != null) 
	    editPanel.repaint();
    }

    boolean tableModelChanging = false;
    public void setParameters(Param[] newParams) {
	paramTable = new DisplayTable(newParams, DisplayTable.PARAMS);
        paramTable.getAccessibleContext().setAccessibleName(msgs.getString("ACS_MON_QuerystringTableA11yName"));
        paramTable.setToolTipText(msgs.getString("ACS_MON_QuerystringTableA11yDesc"));
	paramTable.sortByName(true);
	ListSelectionModel selma = paramTable.getSelectionModel();
	selma.addListSelectionListener(new ListSelectionListener() {
	    public void valueChanged(ListSelectionEvent evt) {
		if(debug) System.out.println("EditPanelQuery::paramTable list selection listener"); // NOI18N
		setEnablings();
	    }
	});

	paramTable.addTableModelListener(new TableModelListener() {
	    public void tableChanged(TableModelEvent evt) {
		if (!tableModelChanging) {
		    tableModelChanging = true;
		    //
		    // Loop through the rows and reset the params.
		    //
		    int num = paramTable.getRowCount();
		    RequestData rd = monitorData.getRequestData();
		    Param[] params = rd.getParam();
		    
		    for(int i=0; i < num; i++) {
			String name = (String)paramTable.getValueAt(i, 0);
			name = name.trim();
			if(name.equals("")) {
			    paramTable.setValueAt(params[i].getName(), 
						  i, 0);
			    showErrorDialog();
			    return;
			}
			String value = (String)paramTable.getValueAt(i, 1);
			value = value.trim();
			params[i].setName(name);
			params[i].setValue(value);
		    }
		    paramTable.sortByName();
		    tableModelChanging = false;
		}
	    }
	});
    }


    public void showConfirmDialog(String msg) {

	Object[] options = { NotifyDescriptor.OK_OPTION, 
			   NotifyDescriptor.CANCEL_OPTION 
	};
	
	NotifyDescriptor confirmDialog = 
	    new NotifyDescriptor((Object)msg, 
				 msgs.getString("MON_Confirmation_Required"),
				 NotifyDescriptor.OK_CANCEL_OPTION,
				 NotifyDescriptor.QUESTION_MESSAGE, 
				 options,
				 NotifyDescriptor.CANCEL_OPTION);

	TopManager.getDefault().notify(confirmDialog);
	if(confirmDialog.getValue().equals(NotifyDescriptor.OK_OPTION)) 
	    setParams = true;
	else 
	    setParams = false;
    }

    public void showErrorDialog() {

	Object[] options = { NotifyDescriptor.OK_OPTION };
	
	NotifyDescriptor errorDialog = 
	    new NotifyDescriptor((Object)msgs.getString("MON_Bad_param"),
				 msgs.getString("MON_Invalid_input"),
				 NotifyDescriptor.DEFAULT_OPTION,
				 NotifyDescriptor.ERROR_MESSAGE, 
				 options,
				 NotifyDescriptor.OK_OPTION);

	TopManager.getDefault().notify(errorDialog);
    }



} // EditPanel
