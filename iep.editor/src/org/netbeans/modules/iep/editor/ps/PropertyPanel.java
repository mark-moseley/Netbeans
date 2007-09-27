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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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


package org.netbeans.modules.iep.editor.ps;

import org.netbeans.modules.iep.editor.designer.JTextFieldFilter;
import org.netbeans.modules.iep.editor.share.SharedConstants;
import org.netbeans.modules.iep.editor.tcg.model.TcgProperty;
import org.netbeans.modules.iep.editor.tcg.model.TcgPropertyType;
import org.netbeans.modules.iep.editor.tcg.model.TcgType;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.text.ParseException;
import java.util.Calendar;
import javax.swing.*;
import javax.swing.border.EtchedBorder;
import org.openide.util.NbBundle;

/**
 * PropertyPanel.java
 *
 * Created on November 1, 2006, 1:52 PM
 *
 * @author Bing Lu
 */
public class PropertyPanel {
    protected String mLabel;
    protected TcgProperty mProperty;
    protected TcgProperty mProperty2;
    protected Object mTemp;
    protected Object mTemp2;
    protected JPanel panel;
    public JComponent component[];
    public JComponent input[];
    
    public PropertyPanel(String label, TcgProperty prop) {
        mLabel = label;
        mProperty = prop;
    }
    
    public PropertyPanel(String label, TcgProperty prop, TcgProperty prop2) {
        mLabel = label;
        mProperty = prop;
        mProperty2 = prop2;
    }
    
    public JPanel getPanel() {
        return panel;
    }
    
    public String getStringValue() {
        return null;
    }
    
    public void setStringValue(String value) {
        return;
    }
    
    public boolean getBooleanValue() {
        return false;
    }
    
    public int getIntValue() {
        return 0;
    }
    
    public void store() {
        return;
    }
    
    public void validateContent(PropertyChangeEvent evt) throws PropertyVetoException {
        TcgPropertyType pt = mProperty.getType();
        String value =  getStringValue();
        // if value is required, it must be specified
        if (pt.isRequired()) {
            if ((value == null) || value.trim().equals("")) {
                String msg = NbBundle.getMessage(PropertyPanel.class,
                        "PropertyPanel.PROPERTY_CANNOT_BE_EMPTY",
                        mLabel);
                throw new PropertyVetoException(msg, evt);
            }
        }
        if ((value == null) || value.trim().equals("")) {
            return;
        }
        // if value is specified, then it must be valid
        if (pt.getType() == TcgType.INTEGER) {
            try {
                Integer.parseInt(value);
            } catch (NumberFormatException e) {
                String msg = NbBundle.getMessage(PropertyPanel.class,
                        "PropertyPanel.PROPERTY_IS_NOT_A_VALID_INTEGER",
                        mLabel);
                throw new PropertyVetoException(msg, evt);
            }
        }
        if (pt.getType() == TcgType.LONG) {
            try {
                Long.parseLong(value);
            } catch (NumberFormatException e) {
                String msg = NbBundle.getMessage(PropertyPanel.class,
                        "PropertyPanel.PROPERTY_IS_NOT_A_VALID_LONG_INTEGER",
                        mLabel);
                throw new PropertyVetoException(msg, evt);
            }
        }
        if (pt.getType() == TcgType.INTEGER) {
            try {
                Double.parseDouble(value);
            } catch (NumberFormatException e) {
                String msg = NbBundle.getMessage(PropertyPanel.class,
                        "PropertyPanel.PROPERTY_IS_NOT_A_VALID_NUMBER",
                        mLabel);
                throw new PropertyVetoException(msg, evt);
            }
        }
        if (pt.getType() == TcgType.DATE) {
            try {
                value = value + mTemp.toString();
                SharedConstants.DATE_FORMAT.parse(value);
            } catch (ParseException e) {
                String msg = NbBundle.getMessage(PropertyPanel.class,
                        "PropertyPanel.PROPERTY_IS_NOT_A_VALID_TIME",
                        mLabel);
                throw new PropertyVetoException(msg, evt);
            }
        }
    }
    
    public static PropertyPanel createSingleLineTextPanel(String label, TcgProperty prop, JTextFieldFilter tff, boolean createPanel) {
        PropertyPanel panel = new PropertyPanel(label, prop) {
            public String getStringValue() {
                return ((JTextField)input[0]).getText();
            }
            public void setStringValue(String value) {
                ((JTextField)input[0]).setText(value);
            }
            public int getIntValue() {
                try {
                    return Integer.parseInt(getStringValue());
                } catch (NumberFormatException e) {
                    return 0;
                }
            }
            public void store() {
                String value = ((JTextField)input[0]).getText();
                if (!mProperty.getStringValue().equals(value)) {
                    mProperty.setStringValue(value);
                }
            }
        };
        JLabel nameLbl = new JLabel(label);
        JTextField tf = new JTextField();
        tf.setPreferredSize(new Dimension(140, 20));
        tf.setMinimumSize(new Dimension(50, 20));
        tf.setDocument(tff);
        tf.setText(prop.getStringValue());
        if (createPanel) {
            panel.panel = new JPanel();
            panel.panel.setLayout(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(4, 3, 4, 3);
            
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.gridwidth = 1;
            gbc.gridheight = 1;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.weightx = 0.0D;
            gbc.weighty = 0.0D;
            gbc.fill = GridBagConstraints.NONE;
            panel.panel.add(nameLbl, gbc);
            
            gbc.gridx = 1;
            gbc.gridy = 0;
            gbc.gridwidth = 2;
            gbc.gridheight = 1;
            gbc.weightx = 1.0D;
            gbc.weighty = 0.0D;
            //gbc.fill = GridBagConstraints.NONE;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            panel.panel.add(tf, gbc);
            /*
            gbc.gridx = 2;
            gbc.gridy = 0;
            gbc.gridwidth = 1;
            gbc.gridheight = 1;
            gbc.weightx = 0.0D;
            gbc.weighty = 0.0D;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            java.awt.Component glue = Box.createHorizontalGlue();
	    panel.panel.add(glue, gbc);
             */
            
            gbc.gridx = 2;
            gbc.gridy = 0;
            gbc.gridwidth = 1;
            gbc.gridheight = 1;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.weightx = 1.0D;
            gbc.weighty = 0.0D;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            java.awt.Component struct = Box.createHorizontalStrut(10);
            panel.panel.add(struct, gbc);
           
            
        }
        panel.component = new JComponent[2];
        panel.component[0] = nameLbl;
        panel.component[1] = tf;
        
        panel.input = new JComponent[1];
        panel.input[0] = tf;
        
        return panel;
    }
    
    public static PropertyPanel createSingleLineTextPanel(String label, TcgProperty prop, boolean createPanel) {
        return createSingleLineTextPanel(label, prop, JTextFieldFilter.newAlphaNumericUnderscore(), createPanel);
    }
    
    public static PropertyPanel createFloatNumberPanel(String label, TcgProperty prop, boolean createPanel) {
        return createSingleLineTextPanel(label, prop, JTextFieldFilter.newFloat(), createPanel);
    }
    
    public static PropertyPanel createIntNumberPanel(String label, TcgProperty prop, boolean createPanel) {
        return createSingleLineTextPanel(label, prop, JTextFieldFilter.newNumeric(), createPanel);
    }
    
    public static PropertyPanel createSmartSingleLineTextPanel(String label, TcgProperty prop, boolean truncateColumn, boolean createPanel) {
        PropertyPanel panel = new PropertyPanel(label, prop) {
            public String getStringValue() {
                return ((JTextField)input[0]).getText();
            }
            public void setStringValue(String value) {
                ((JTextField)input[0]).setText(value);
            }
            public void store() {
                String value = ((JTextField)input[0]).getText();
                if (mProperty.getType().isMultiple()) {
                    value = value.replace(',', '\\');
                }
                if (!mProperty.getStringValue().equals(value)) {
                    mProperty.setStringValue(value);
                }
            }
        };
        JLabel nameLbl = new JLabel(label);
        SmartTextField tf = new SmartTextField(truncateColumn);
        tf.setPreferredSize(new Dimension(200, 20));
        String value = prop.getStringValue();
        if (prop.getType().isMultiple()) {
            value = value.replace('\\', ',');
        }
        tf.setText(value);
        if (createPanel) {
            panel.panel = new JPanel();
            panel.panel.setLayout(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(4, 3, 4, 3);
            
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.gridwidth = 1;
            gbc.gridheight = 1;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.weightx = 0.0D;
            gbc.weighty = 0.0D;
            gbc.fill = GridBagConstraints.NONE;
            panel.panel.add(nameLbl, gbc);
            
            gbc.gridx = 1;
            gbc.gridy = 0;
            gbc.gridwidth = 1;
            gbc.gridheight = 1;
            gbc.weightx = 1.0D;
            gbc.weighty = 0.0D;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            panel.panel.add(tf, gbc);
            
            gbc.gridx = 2;
            gbc.gridy = 0;
            gbc.gridwidth = 1;
            gbc.gridheight = 1;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.weightx = 0.0D;
            gbc.weighty = 0.0D;
            gbc.fill = GridBagConstraints.NONE;
            java.awt.Component struct = Box.createHorizontalStrut(10);
            panel.panel.add(struct, gbc);
        }
        panel.component = new JComponent[2];
        panel.component[0] = nameLbl;
        panel.component[1] = tf;
        
        panel.input = new JComponent[1];
        panel.input[0] = tf;
        
        return panel;
    }
    
    public static PropertyPanel createSmartMultiLineTextPanel(String label, TcgProperty prop) {
        PropertyPanel panel = new PropertyPanel(label, prop) {
            public String getStringValue() {
                return ((JTextArea)input[0]).getText();
            }
            public void setStringValue(String value) {
                ((JTextArea)input[0]).setText(value);
            }
            public void store() {
                String value = ((JTextArea)input[0]).getText();
                if (!mProperty.getStringValue().equals(value)) {
                    mProperty.setStringValue(value);
                }
            }
        };
        panel.panel = new JPanel();
        panel.panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 3, 4, 3);
        
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.weightx = 0.0D;
        gbc.weighty = 0.0D;
        gbc.fill = GridBagConstraints.NONE;
        JLabel nameLbl = new JLabel(label);
        panel.panel.add(nameLbl, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.weightx = 1.0D;
        gbc.weighty = 1.0D;
        gbc.fill = GridBagConstraints.BOTH;
        JScrollPane scrollPane = new JScrollPane();
        panel.panel.add(scrollPane, gbc);
        SmartTextArea ta = new SmartTextArea(5, 40);
        ta.setBorder(new EtchedBorder());
        ta.setMargin(new Insets(3, 3, 3, 3));
        if (prop.getType().isMultiple()) {
            // Note that prop.getStringValue() returns
            // a "\" separated single line text. But we want
            // a newline separated multi-line text.
            String s = prop.getStringValue();
            s = s.replace("\\", "\n");
            ta.setText(s);
        } else {
            ta.setText(prop.getStringValue());
        }
        scrollPane.getViewport().add(ta);
        
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.weightx = 0.0D;
        gbc.weighty = 0.0D;
        gbc.fill = GridBagConstraints.NONE;
        java.awt.Component struct = Box.createHorizontalStrut(10);
        panel.panel.add(struct, gbc);
        
        panel.input = new JComponent[1];
        panel.input[0] = ta;
        
        return panel;
    }
    
    public static PropertyPanel createCheckBoxPanel(String label, TcgProperty prop) {
        PropertyPanel panel = new PropertyPanel(label, prop) {
            public boolean getBooleanValue() {
                return ((JCheckBox)input[0]).isSelected();
            }
            public void store() {
                boolean value = ((JCheckBox)input[0]).isSelected();
                if (!mProperty.getBoolValue() == value) {
                    mProperty.setValue(value? Boolean.TRUE : Boolean.FALSE);
                }
            }
        };
        panel.panel = new JPanel();
        panel.panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 3, 4, 3);
        
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.weightx = 0.0D;
        gbc.weighty = 0.0D;
        gbc.fill = GridBagConstraints.NONE;
        JCheckBox cb = new JCheckBox(label, prop.getBoolValue());
        panel.panel.add(cb, gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.weightx = 1.0D;
        gbc.weighty = 0.0D;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        java.awt.Component glue = Box.createHorizontalGlue();
        panel.panel.add(glue, gbc);
        
        panel.input = new JComponent[1];
        panel.input[0] = cb;
        
        return panel;
    }
    
    public static PropertyPanel createComboBoxPanel(String label, TcgProperty prop, String[] values, boolean createPanel) {
        PropertyPanel panel = new PropertyPanel(label, prop) {
            public String getStringValue() {
                return (String)((JComboBox)input[0]).getSelectedItem();
            }
            public void store() {
                String value = getStringValue();
                if (!mProperty.getStringValue().equals(value)) {
                    mProperty.setStringValue(value);
                }
            }
        };
        JLabel nameLabel = new JLabel(label);
        JComboBox cbb = new JComboBox(values);
        // PreferredSize must be set o.w. failed validation will resize this field.
        cbb.setPreferredSize(new Dimension(140, 20));
        cbb.setMinimumSize(new Dimension(50, 20));

        String value = prop.getStringValue();
        for (int i = 0; i < values.length; i++) {
            if (values[i].equals(value)) {
                cbb.setSelectedItem(value);
                break;
            }
        }
        if (createPanel) {
            panel.panel = new JPanel();
            panel.panel.setLayout(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(4, 3, 4, 3);
            
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.gridwidth = 1;
            gbc.gridheight = 1;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.weightx = 0.0D;
            gbc.weighty = 0.0D;
            gbc.fill = GridBagConstraints.NONE;
            panel.panel.add(nameLabel, gbc);
            
            gbc.gridx = 1;
            gbc.gridy = 0;
            gbc.gridwidth = 1;
            gbc.gridheight = 1;
            gbc.weightx = 0.0D;
            gbc.weighty = 0.0D;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            panel.panel.add(cbb, gbc);
            
            gbc.gridx = 2;
            gbc.gridy = 0;
            gbc.gridwidth = 1;
            gbc.gridheight = 1;
            gbc.weightx = 1.0D;
            gbc.weighty = 0.0D;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            java.awt.Component glue = Box.createHorizontalGlue();
            panel.panel.add(glue, gbc);
        }
        panel.component = new JComponent[2];
        panel.component[0] = nameLabel;
        panel.component[1] = cbb;
        
        panel.input = new JComponent[1];
        panel.input[0] = cbb;
        
        return panel;
    }
    
    private static Calendar CALENDAR = Calendar.getInstance();
    public static PropertyPanel createDatePanel(String label, TcgProperty prop, boolean createPanel) {
        PropertyPanel panel = new PropertyPanel(label, prop) {
            public String getStringValue() {
                return (String)((JTextField)input[0]).getText();
            }
            public void setStringValue(String value) {
                ((JTextField)input[0]).setText(value);
            }
            public void store() {
                String value = getStringValue() + mTemp.toString();
                if (!mProperty.getStringValue().equals(value)) {
                    mProperty.setStringValue(value);
                }
            }
        };
        JLabel nameLabel = new JLabel(label);
        JTextField tf = new JTextField();
        String value = prop.getStringValue();
        panel.mTemp = value.substring(value.length()-5);
        tf.setText(value.substring(0, value.length()-5));
        tf.setPreferredSize(new Dimension(140, 20));
        tf.setMinimumSize(new Dimension(50, 20));
        String timeZone = CALENDAR.getTimeZone().getDisplayName();
        JLabel zoneLbl = new JLabel(timeZone);
        if (createPanel) {
            panel.panel = new JPanel();
            panel.panel.setLayout(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(4, 3, 4, 3);
            
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.gridwidth = 1;
            gbc.gridheight = 1;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.weightx = 0.0D;
            gbc.weighty = 0.0D;
            gbc.fill = GridBagConstraints.NONE;
            panel.panel.add(nameLabel, gbc);
            
            gbc.gridx = 1;
            gbc.gridy = 0;
            gbc.gridwidth = 1;
            gbc.gridheight = 1;
            gbc.weightx = 0.0D;
            gbc.weighty = 0.0D;
            gbc.fill = GridBagConstraints.NONE;
            panel.panel.add(tf, gbc);
            
            gbc.gridx = 2;
            gbc.gridy = 0;
            gbc.gridwidth = 1;
            gbc.gridheight = 1;
            gbc.weightx = 0.0D;
            gbc.weighty = 0.0D;
            gbc.fill = GridBagConstraints.NONE;
            panel.panel.add(zoneLbl, gbc);
            
            gbc.gridx = 3;
            gbc.gridy = 0;
            gbc.gridwidth = 1;
            gbc.gridheight = 1;
            gbc.weightx = 1.0D;
            gbc.weighty = 0.0D;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            java.awt.Component glue = Box.createHorizontalGlue();
            panel.panel.add(glue, gbc);
        }
        panel.component = new JComponent[3];
        panel.component[0] = nameLabel;
        panel.component[1] = tf;
        panel.component[2] = zoneLbl;
        panel.input = new JComponent[1];
        panel.input[0] = tf;
        
        return panel;
    }
    
    public static PropertyPanel createDurationPanel(String label, TcgProperty size, TcgProperty unit, boolean createPanel) {
        PropertyPanel panel = new PropertyPanel(label, size, unit) {
            public String getStringValue() {
                return ((JTextField)input[0]).getText();
            }
            public void setStringValue(String value) {
                ((JTextField)input[0]).setText(value);
            }
            public void store() {
                String size = getStringValue();
                if (!mProperty.getStringValue().equals(size)) {
                    mProperty.setStringValue(size);
                }
                String unit = (String)((JComboBox)input[1]).getSelectedItem();
                if (!mProperty2.getStringValue().equals(unit)) {
                    mProperty2.setStringValue(unit);
                }
            }
        };
        JLabel nameLabel = new JLabel(label);
        JTextField tf = new JTextField();
        tf.setPreferredSize(new Dimension(140, 20));
        tf.setMinimumSize(new Dimension(50, 20));
        //tf.setDocument(JTextFieldFilter.newFloat());
        tf.setDocument(JTextFieldFilter.newFloatExp());
        
        tf.setText(size.getStringValue());
        String[] values = new String[] {
            SharedConstants.TIME_UNIT_SECOND,
            SharedConstants.TIME_UNIT_MINUTE,
            SharedConstants.TIME_UNIT_HOUR,
            SharedConstants.TIME_UNIT_DAY,
            SharedConstants.TIME_UNIT_WEEK,
        };
        JComboBox cbb = new JComboBox(values);
        // PreferredSize must be set o.w. failed validation will resize this field.
        cbb.setPreferredSize(new Dimension(80, 20));
        cbb.setMinimumSize(new Dimension(30, 20));
        
        String value = unit.getStringValue();
        for (int i = 0; i < values.length; i++) {
            if (values[i].equals(value)) {
                cbb.setSelectedItem(value);
                break;
            }
        }
        if (createPanel) {
            panel.panel = new JPanel();
            panel.panel.setLayout(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(4, 3, 4, 3);
            
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.gridwidth = 1;
            gbc.gridheight = 1;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.weightx = 0.0D;
            gbc.weighty = 0.0D;
            gbc.fill = GridBagConstraints.NONE;
            panel.panel.add(nameLabel, gbc);
            
            gbc.gridx = 1;
            gbc.gridy = 0;
            gbc.gridwidth = 1;
            gbc.gridheight = 1;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.weightx = 0.0D;
            gbc.weighty = 0.0D;
            gbc.fill = GridBagConstraints.NONE;
            panel.panel.add(tf, gbc);
            
            gbc.gridx = 2;
            gbc.gridy = 0;
            gbc.gridwidth = 1;
            gbc.gridheight = 1;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.weightx = 0.0D;
            gbc.weighty = 0.0D;
            gbc.fill = GridBagConstraints.NONE;
            panel.panel.add(cbb, gbc);
            
            gbc.gridx = 3;
            gbc.gridy = 0;
            gbc.gridwidth = 1;
            gbc.gridheight = 1;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.weightx = 1.0D;
            gbc.weighty = 0.0D;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            java.awt.Component glue = Box.createHorizontalGlue();
            panel.panel.add(glue, gbc);
        }
        panel.component = new JComponent[3];
        panel.component[0] = nameLabel;
        panel.component[1] = tf;
        panel.component[2] = cbb;

        panel.input = new JComponent[2];
        panel.input[0] = tf;
        panel.input[1] = cbb;
        return panel;
    }
}