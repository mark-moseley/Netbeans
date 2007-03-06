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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.iep.editor.ps;

import org.netbeans.modules.iep.editor.model.AttributeMetadata;
import org.netbeans.modules.iep.editor.model.Plan;
import org.netbeans.modules.iep.editor.model.Schema;
import org.netbeans.modules.iep.editor.share.SharedConstants;
import org.netbeans.modules.iep.editor.tcg.model.TcgComponent;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import org.openide.util.NbBundle;

/**
 * InputSchemaSelectionPanel.java
 *
 * Created on November 15, 2006, 2:42 PM
 *
 * @author Bing Lu
 */
public class InputSchemaSelectionPanel extends JPanel implements SharedConstants {
    private static final Logger mLog = Logger.getLogger(InputSchemaSelectionPanel.class.getName());
    
    private List mCheckBoxList = new ArrayList();
    private List mAttributeList = new ArrayList();
    
    /** Creates a new instance of InputSchemaSelectionPanel */
    public InputSchemaSelectionPanel(Plan plan, TcgComponent component) {
        try {
            String msg = NbBundle.getMessage(InputSchemaSelectionPanel.class, "InputSchemaSelectionPanel.SELECTED_INPUT_ATTRIBUTES");
            setBorder(new TitledBorder(LineBorder.createGrayLineBorder(), msg, TitledBorder.LEFT, TitledBorder.TOP));
            setLayout(new BorderLayout(5, 5));
            JScrollPane scrollPane = new JScrollPane();
            add(scrollPane, BorderLayout.CENTER);
            
            //Put the check boxes in a column in a panel
            JPanel checkPanel = new JPanel(new GridBagLayout());
            scrollPane.getViewport().add(checkPanel);
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(3, 3, 3, 3);
            int gGridy = 0;
            
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.gridwidth = 1;
            gbc.gridheight = 1;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.weightx = 0.0D;
            gbc.weighty = 0.0D;
            gbc.fill = GridBagConstraints.NONE;

            List inputIdList = component.getProperty(INPUT_ID_LIST_KEY).getListValue();
            if (inputIdList.size() < 1) {
                msg = NbBundle.getMessage(InputSchemaSelectionPanel.class,
                        "InputSchemaSelectionPanel.INPUT_IS_NOT_SPECIFIED");
                JLabel label = new JLabel(msg);
                checkPanel.add(label, gbc);
                return;
            }
            String id = (String)inputIdList.get(0);
            TcgComponent input = plan.getOperatorById(id);
            String outputSchemaId = input.getProperty(OUTPUT_SCHEMA_ID_KEY).getStringValue();
            Schema schema = plan.getSchema(outputSchemaId);
            if (schema == null) {
                msg = NbBundle.getMessage(InputSchemaSelectionPanel.class,
                        "InputSchemaSelectionPanel.INPUT_DOES_NOT_HAVE_ANY_SCHEMA");
                JLabel label = new JLabel(msg);
                checkPanel.add(label, gbc);
                return;
            }
            List fromColumnList = component.getProperty(FROM_COLUMN_LIST_KEY).getListValue();
            for(int i = 0, I = schema.getAttributeCount(); i < I; i++) {
                org.netbeans.modules.iep.editor.model.AttributeMetadata cm = schema.getAttributeMetadata(i);
                mAttributeList.add(cm);
                String attributeName = cm.getName();
                JCheckBox cb = new JCheckBox(attributeName);
                mCheckBoxList.add(cb);
                if (fromColumnList.contains(attributeName)) {
                    cb.setSelected(true);
                } else {
                    cb.setSelected(false);
                }
                gbc.gridy = gGridy++;
                checkPanel.add(cb, gbc);
            }
            gbc.gridy = gGridy++;
            gbc.weighty = 1.0D;
            gbc.fill = GridBagConstraints.VERTICAL;
            checkPanel.add(Box.createHorizontalGlue(), gbc);
        } catch(Exception e) {
            mLog.log(Level.SEVERE, NbBundle.getMessage(InputSchemaSelectionPanel.class,
                    "InputSchemaSelectionPanel.FAIL_TO_BUILD_SELECTION_LIST_FOR", component.getTitle()), e);
        }
    }
    
    public List getSelectedAttributeList() {
        List ret = new ArrayList();
        for (int i = 0, I = mCheckBoxList.size(); i < I; i++) {
            if (((JCheckBox)mCheckBoxList.get(i)).isSelected()) {
                ret.add(mAttributeList.get(i));
            }
        }
        return ret;
    }

    public List getSelectedAttributeNameList() {
        List ret = new ArrayList();
        try {
            for (int i = 0, I = mCheckBoxList.size(); i < I; i++) {
                if (((JCheckBox)mCheckBoxList.get(i)).isSelected()) {
                    ret.add(((AttributeMetadata)mAttributeList.get(i)).getAttributeName());
                }
            }
        } catch (Exception e) {
        }
        return ret;
    }
    
    public void addItemListener(ItemListener listener) {
        for (int i = 0, I = mCheckBoxList.size(); i < I; i++) {
            ((JCheckBox)mCheckBoxList.get(i)).addItemListener(listener);
        }
    }
}
