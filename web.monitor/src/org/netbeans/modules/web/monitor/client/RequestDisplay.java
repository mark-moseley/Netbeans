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
 * RequestDisplay.java
 *
 *
 * Created: Wed Jan 31 18:04:22 2001
 *
 * @author Ana von Klopp
 * @version
 */

package org.netbeans.modules.web.monitor.client;

import javax.swing.table.*;     // widgets
import javax.swing.JLabel;
import javax.swing.JTextArea;
import org.netbeans.modules.web.monitor.data.*;
import org.openide.util.NbBundle;
import java.util.ResourceBundle;
import java.awt.Component;

public class RequestDisplay extends DataDisplay {
    
    private final static boolean debug = false;
    private static final ResourceBundle msgs =
	NbBundle.getBundle(TransactionView.class);
    
    private static final String[] requestCategories = { 
	msgs.getString("MON_Request_URI"),
	msgs.getString("MON_Method"),
	msgs.getString("MON_Querystring"),
	msgs.getString("MON_Protocol"),
	msgs.getString("MON_Remote_Address"),
	msgs.getString("MON_Scheme"),
	msgs.getString("MON_Status"),
    };


    private DisplayTable dt = null; 
        
    public RequestDisplay() {

	super();
    }


    // We're treating these as if they are all strings at the
    // moment. In reality they can be of different types, though maybe 
    // that does not matter...
    public void setData(MonitorData md) {

	if(debug) System.out.println("in RequestDisplay.setData()"); //NOI18N
	this.removeAll();
	if (md == null)
	    return;
	
	RequestData rd = md.getRequestData();
	dt = new DisplayTable(requestCategories);
	dt.setValueAt(rd.getAttributeValue("uri"), 0,1); //NOI18N
	dt.setValueAt(rd.getAttributeValue("method"),1,1); //NOI18N
	dt.setValueAt(rd.getAttributeValue("queryString"), 2,1); //NOI18N
	dt.setValueAt(rd.getAttributeValue("protocol"), 3,1); //NOI18N
	dt.setValueAt(rd.getAttributeValue("ipaddress"), 4,1); //NOI18N
	dt.setValueAt(rd.getAttributeValue("scheme"), 5,1); //NOI18N
	dt.setValueAt(rd.getAttributeValue("status"), 6,1); //NOI18N
        dt.getAccessibleContext().setAccessibleName(msgs.getString("ACS_MON_RequestTable_19A11yName")); 
        dt.setToolTipText(msgs.getString("ACS_MON_RequestTable_19A11yDesc"));

	int gridy = -1;
	addGridBagComponent(this, createTopSpacer(), 0, ++gridy,
			    fullGridWidth, 1, 0, 0, 
			    java.awt.GridBagConstraints.WEST,
			    java.awt.GridBagConstraints.NONE,
			    topSpacerInsets,
			    0, 0);

        JLabel requestHeaderLabel = createHeaderLabel(msgs.getString("MON_Request_19"));
        requestHeaderLabel.setDisplayedMnemonic(msgs.getString("MON_Request_19_Mnemonic").charAt(0));
        requestHeaderLabel.getAccessibleContext().setAccessibleDescription(msgs.getString("ACS_MON_Request_19A11yDesc"));
        requestHeaderLabel.setLabelFor(dt);
	addGridBagComponent(this, requestHeaderLabel, 0, ++gridy,
			    fullGridWidth, 1, 0, 0, 
			    java.awt.GridBagConstraints.WEST,
			    java.awt.GridBagConstraints.NONE,
			    labelInsets,
			    0, 0);

	addGridBagComponent(this, dt, 0, ++gridy,
			    fullGridWidth, 1, tableWeightX, tableWeightY, 
			    java.awt.GridBagConstraints.WEST,
			    java.awt.GridBagConstraints.BOTH,
			    tableInsets,
			    0, 0);


	String msg;
	
	// add the parameters

	Param[] params2 = rd.getParam();
	String msg2 = "";
	DisplayTable paramTable = null;
	Component queryDataLabel = null;
	boolean bad = false;
	
	if(params2 == null || params2.length == 0) {
	    if("POST".equals(rd.getAttributeValue("method"))) { //NOI18N

		String type = rd.getAttributeValue("urlencoded"); //NOI18N
		
		if(type != null) {

		    if (type.equals("false")) { //NOI18N
			msg2 = msgs.getString("MON_Unparameterized");
		    }
		    else if (type.equals("bad")) { //NOI18N
			msg2 = msgs.getString("MON_Warning_param"); 
			queryDataLabel =
			    createHeaderLabel(msg2); 
			bad = true;
		    }
		    else msg2 = msgs.getString("MON_No_posted_data");
		}
		else msg2 = msgs.getString("MON_No_posted_data");
	    } else {
		msg2 = msgs.getString("MON_No_querystring");
	    }
	    if(queryDataLabel == null) 
		queryDataLabel = createDataLabel(msg2);
	    
	} else {
	    msg2 = msgs.getString("MON_Parameters");
	    paramTable = new DisplayTable(params2);
            paramTable.getAccessibleContext().setAccessibleName(msgs.getString("ACS_MON_ParametersTableA11yName"));
            paramTable.setToolTipText(msgs.getString("ACS_MON_ParametersTableA11yDesc"));
	    queryDataLabel = createSortButtonLabel(msg2, paramTable, msgs.getString("MON_Parameters_Mnemonic").charAt(0), msgs.getString("ACS_MON_ParametersA11yDesc"));
	}
	
	addGridBagComponent(this, queryDataLabel, 0, ++gridy,
			    1, 1, 0, 0, 
			    java.awt.GridBagConstraints.WEST,
			    java.awt.GridBagConstraints.NONE,
			    labelInsets,
			    0, 0);

	if (params2 != null && params2.length > 0) {
	    addGridBagComponent(this, paramTable, 0, ++gridy,
				fullGridWidth, 1, tableWeightX, tableWeightY, 
				java.awt.GridBagConstraints.WEST,
				java.awt.GridBagConstraints.BOTH,
				tableInsets,
				0, 0);
	}
	else if(bad) {
	    JTextArea ta = new JTextArea(msgs.getString("MON_Unparameterized_bad"));
	    ta.setEditable(false);
	    ta.setLineWrap(true);
	    ta.setBackground(this.getBackground());
	    addGridBagComponent(this, ta, 0, ++gridy,
				fullGridWidth, 1, tableWeightX, tableWeightY, 
				java.awt.GridBagConstraints.WEST,
				java.awt.GridBagConstraints.BOTH,
				tableInsets,
				0, 0);
			
	}
	
	this.add(createRigidArea()); 

	Param[] param = null;
	try {
	    param = rd.getRequestAttributesIn().getParam();
	}
	catch(Exception ex) {
	}

	if(param != null && param.length > 0) {

	    JLabel requestAttrBeforeLabel =
		createHeaderLabel(msgs.getString("MON_Request_att_before")); 
	    requestAttrBeforeLabel.setDisplayedMnemonic(msgs.getString("MON_Request_att_before_Mnemonic").charAt(0));
	    requestAttrBeforeLabel.getAccessibleContext().setAccessibleDescription(msgs.getString("ACS_MON_Request_att_beforeA11yDesc"));
	    addGridBagComponent(this, requestAttrBeforeLabel, 0, ++gridy,
				fullGridWidth, 1, 0, 0, 
				java.awt.GridBagConstraints.WEST,
				java.awt.GridBagConstraints.NONE,
				labelInsets,
				0, 0);
	    dt = new DisplayTable(param);
	    requestAttrBeforeLabel.setLabelFor(dt);
	    dt.getAccessibleContext().setAccessibleName(msgs.getString("ACS_MON_Request_att_beforeTableA11yName"));
	    dt.setToolTipText(msgs.getString("ACS_MON_Request_att_beforeTableA11yDesc"));
	    addGridBagComponent(this, dt, 0, ++gridy,
				fullGridWidth, 1, tableWeightX, tableWeightY, 
				java.awt.GridBagConstraints.WEST,
				java.awt.GridBagConstraints.BOTH,
				tableInsets,
				0, 0);	
	    this.add(createRigidArea()); 
	}
    
	param = null;
	try {
	    param = rd.getRequestAttributesOut().getParam();
	}
	catch(Exception ex) {
	}

	if(param != null && param.length > 0) {

	    JLabel requestAttrAfterLabel =
		createHeaderLabel(msgs.getString("MON_Request_att_after")); 
	    requestAttrAfterLabel.setDisplayedMnemonic(msgs.getString("MON_Request_att_after_Mnemonic").charAt(0));
	    requestAttrAfterLabel.getAccessibleContext().setAccessibleDescription(msgs.getString("ACS_MON_Request_att_afterA11yDesc"));
	    addGridBagComponent(this, requestAttrAfterLabel, 0, ++gridy,
				fullGridWidth, 1, 0, 0, 
				java.awt.GridBagConstraints.WEST,
				java.awt.GridBagConstraints.NONE,
				labelInsets,
				0, 0);
	    dt = new DisplayTable(param);
	    requestAttrAfterLabel.setLabelFor(dt);
	    dt.getAccessibleContext().setAccessibleName(msgs.getString("ACS_MON_Request_att_afterTableA11yName"));
	    dt.setToolTipText(msgs.getString("ACS_MON_Request_att_afterTableA11yDesc"));
	    addGridBagComponent(this, dt, 0, ++gridy,
				fullGridWidth, 1, tableWeightX, tableWeightY, 
				java.awt.GridBagConstraints.WEST,
				java.awt.GridBagConstraints.BOTH,
				tableInsets,
				0, 0);	
	}
    
	addGridBagComponent(this, createGlue(), 0, ++gridy,
			    1, 1, 1.0, 1.0, 
			    java.awt.GridBagConstraints.WEST,
			    java.awt.GridBagConstraints.BOTH,
			    zeroInsets,
			    0, 0);



	this.setMaximumSize(this.getPreferredSize()); 
	this.repaint();
    }
} // RequestDisplay
