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

package org.netbeans.modules.web.wizards;

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import org.openide.util.NbBundle;

/**
 * Wizard panel that collects deployment data for Servlets and Filters
 * @author Ana von Klopp 
 */
class InitParamPanel extends JPanel implements ActionListener, 
						ListSelectionListener { 

    private final static String ADD = "add"; 
    private final static String EDIT = "edit"; 
    private final static String REMOVE = "remove"; 
    private ServletData deployData; 
    private BaseWizardPanel parent; 
    private JLabel jLinitparams;
    private DDTable table; 
    private JButton jBnew; 
    private JButton jBedit; 
    private JButton jBdelete; 
    private JScrollPane scrollP; 

    private static final long serialVersionUID = -5803905591685582710L;
    
    public InitParamPanel(ServletData deployData, BaseWizardPanel parent) { 
	this.deployData = deployData; 
	this.parent = parent; 
	initComponents ();
    }

    private void initComponents () {
	// Layout description
	setLayout(new java.awt.GridBagLayout());

	// Entity covers entire row
	GridBagConstraints fullRowC = new GridBagConstraints();
	fullRowC.gridx = 0;
        fullRowC.gridy = 0;
	fullRowC.gridwidth = 2;
	fullRowC.anchor = GridBagConstraints.WEST;
	fullRowC.fill = GridBagConstraints.HORIZONTAL;
	fullRowC.insets = new Insets(4, 0, 4, 60);

	// Button
	GridBagConstraints bC = new GridBagConstraints();
	bC.gridx = 1;
        bC.gridy = 1;
	bC.weightx = 0.1;
	bC.fill = GridBagConstraints.HORIZONTAL;
	bC.insets = new Insets(4, 20, 4, 60);

	// Table panel 
	GridBagConstraints tableC = new GridBagConstraints();
	tableC.gridx = 0;
        tableC.gridy = 1; 
	tableC.gridheight = 4;
	tableC.fill = GridBagConstraints.BOTH; 
	tableC.weightx = 0.9;
	tableC.weighty = 1.0; 
	tableC.anchor = GridBagConstraints.WEST; 
	tableC.insets = new Insets(4, 0, 4, 0);

	// Filler panel 
	GridBagConstraints fillerC = new GridBagConstraints();
	fillerC.gridx = 1;
        fillerC.gridy = GridBagConstraints.RELATIVE; 
	fillerC.fill = GridBagConstraints.BOTH; 
	fillerC.weighty = 1.0; 
	fillerC.insets = new Insets(4, 0, 4, 0);
        
	// Component Initialization by row
	// 1. Init parameter
	jLinitparams = new JLabel(NbBundle.getMessage(InitParamPanel.class, "LBL_initparamsL"));
	jLinitparams.setDisplayedMnemonic(NbBundle.getMessage (InitParamPanel.class, "LBL_initparams_mnemonic").charAt(0));
	// PENDING 
	this.add(jLinitparams, fullRowC); 

	// 2. Table row

	String[] headers = { "paramname", "paramvalue" };
	table = new DDTable(headers, 
			    "LBL_initparams", 
			    Editable.BOTH); 

	jLinitparams.setLabelFor(table);

	// Enable the buttons according to the row selected
	table.getSelectionModel().addListSelectionListener(this); 
	table.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(InitParamPanel.class, "ACSD_initparams_desc")); // NOI18N
	table.getAccessibleContext().setAccessibleName(NbBundle.getMessage(InitParamPanel.class, "ACSD_initparams")); // NOI18N


	table.getModel().addTableModelListener(new TableModelListener() { 
	    public void tableChanged(TableModelEvent evt) {
		updateInitParams();
	    }}); 

	scrollP = new JScrollPane(table); 
	this.add(scrollP, tableC); 

	jBnew = new JButton(); 
	jBnew.setText(NbBundle.getMessage(InitParamPanel.class, 
					  "LBL_new")); 
	jBnew.setMnemonic(NbBundle.getMessage(InitParamPanel.class, "LBL_new_mnemonic").charAt(0));
	jBnew.addActionListener(this); 
	jBnew.setActionCommand(ADD); 
	jBnew.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(InitParamPanel.class, "ACSD_initparam_new")); // NOI18N
	this.add(jBnew, bC); 

	bC.gridy++; 
	jBedit = new JButton(); 
	jBedit.setText(NbBundle.getMessage(InitParamPanel.class,
					     "LBL_edit")); 
	jBedit.setMnemonic(NbBundle.getMessage(InitParamPanel.class, "LBL_edit_mnemonic").charAt(0));
	jBedit.addActionListener(this);
	jBedit.setActionCommand(EDIT); 
	jBedit.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(InitParamPanel.class, "ACSD_initparam_edit")); // NOI18N
	jBedit.setEnabled(false); 
	this.add(jBedit, bC); 

	bC.gridy++; 
	jBdelete = new JButton(); 
	jBdelete.setText(NbBundle.getMessage(InitParamPanel.class,
					     "LBL_delete")); 
	jBdelete.setMnemonic(NbBundle.getMessage(InitParamPanel.class, "LBL_delete_mnemonic").charAt(0));
	jBdelete.addActionListener(this);
	jBdelete.setActionCommand(REMOVE); 
	jBdelete.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(InitParamPanel.class, "ACSD_initparam_delete")); // NOI18N
	jBdelete.setEnabled(false); 
	this.add(jBdelete, bC);
        
        this.add(new javax.swing.JPanel(),fillerC);
    }

    public void setEnabled() { 
	boolean enable = deployData.makeEntry(); 
    
	jLinitparams.setEnabled(enable);
	jBnew.setEnabled(enable); 
	if(enable) { 
	    ListSelectionModel lsm = table.getSelectionModel(); 
	    if (lsm.isSelectionEmpty()) {
		jBdelete.setEnabled(false); 
		jBedit.setEnabled(false); 
	    }
	    else { 
		jBdelete.setEnabled(true);
		jBedit.setEnabled(true);  
	    }
	}
	else { 
	    jBdelete.setEnabled(false); 
	    jBedit.setEnabled(false); 
	}
	table.setEditable(enable ? Editable.BOTH : Editable.NEITHER); 
    } 

    public void actionPerformed(ActionEvent evt) { 
	int row = -1; 
	if(evt.getSource() instanceof JButton) { 
	    if(evt.getActionCommand() == ADD) { 
		String[] values= { 
		    NbBundle.getMessage(InitParamPanel.class, "LBL_paramname"), 
		    NbBundle.getMessage(InitParamPanel.class, "LBL_paramvalue"), 
		}; 
		row = table.addRow(values); 
		table.setRowSelectionInterval(row, row);
	    } 
	    else if (evt.getActionCommand() == REMOVE) { 
		row = table.getSelectedRow(); 
		table.removeRow(row); 
                setEnabled();
	    }
	    else if (evt.getActionCommand() == EDIT) { 
		row = table.getSelectedRow(); 
		String name = (String)(table.getValueAt(row, 0)); 
		String value = (String)(table.getValueAt(row, 1)); 
		String title =  NbBundle.getMessage(DDTable.class, "LBL_initparams_edit"); //NOI18N
		TableRowDialog d =
		    new TableRowDialog(name, value, Editable.BOTH, 
				       TableRowDialog.Condition.NONE, 
				       title);
		d.showDialog();
		if(d.getDialogOK()) {
		    table.setData(d.getName(), d.getValue(), row); 
		}
		else 
		    table.setData(name, value, row); 
	    }
	    scrollP.revalidate(); 
	} 
    }

    public void valueChanged(ListSelectionEvent e) {
	//Ignore extra messages.
	if (e.getValueIsAdjusting()) return;
	setEnabled(); 
	updateInitParams(); 
    } 

    private void updateInitParams() { 
	if(deployData.makeEntry()) { 
	    int numInitParams = table.getRowCount(); 
	    String[][] param = new String[numInitParams][2]; 
	    boolean isOK = true; 
	    for(int i=0; i<numInitParams; ++i) { 
		param[i][0] = (String)(table.getModel().getValueAt(i,0)); 
		if(param[i][0].length() == 0) isOK = false; 
		param[i][1] = (String)(table.getModel().getValueAt(i,1)); 
	    } 
	    deployData.setInitParams(param, isOK); 
	    parent.fireChangeEvent();
	}
    }	    
    
}
