/*
 * AbstractPopup.java
 *
 * Created on April 20, 2005, 2:55 PM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package org.netbeans.modules.jmx.mbeanwizard.popup;

import org.netbeans.modules.jmx.mbeanwizard.tablemodel.AbstractJMXTableModel;
import java.awt.Dialog;
import java.awt.GridLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import org.openide.util.NbBundle;
import java.awt.Insets;
import java.awt.event.*;
import org.openide.awt.Mnemonics;

/**
 *
 * @author an156382
 */
public abstract class AbstractPopup extends JDialog implements ActionListener {
    
    protected JTable popupTable;
    protected AbstractJMXTableModel popupTableModel;
    
    protected JTextField textFieldToFill;
    
    protected JButton addJButton;
    protected JButton removeJButton;
    protected JButton closeJButton;
    private JButton cancelJButton;
    private int winWidth;
    
    public AbstractPopup(Dialog d) {
        super(d);
    }
    
    protected void setDimensions(String str) {
        setTitle(str);
        setModal(true);
        setResizable(false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setBounds(350,250,winWidth,250);
        setVisible(true);
    }
    
    protected JTextField instanciateJTextField() {
        
        JTextField text = new JTextField();
        text.setDragEnabled(true);
        text.setEditable(true);
        
        return text;
    }
    
    public void actionPerformed(ActionEvent e) {
        setVisible(false);
    }
    
    protected JButton instanciatePopupButton(java.lang.Class c, String s) {
        
        //return new JButton(NbBundle.getMessage(c,s));
        JButton button = new JButton();
        Mnemonics.setLocalizedText(button,
                     NbBundle.getMessage(c,s));//NOI18N
        
        return button;
    }
    
    protected void definePanels(JButton[] popupButtons, JTable table) {
        java.awt.GridBagLayout mainGridBag = new java.awt.GridBagLayout();
        
        getContentPane().setLayout(mainGridBag);
        java.awt.GridBagConstraints c1 = new java.awt.GridBagConstraints();
        c1.fill = java.awt.GridBagConstraints.BOTH;
        c1.gridwidth = 2;
        c1.weightx = 1.0;
        c1.weighty = 1.0;
        c1.gridx = 1;
        c1.gridy = 1;
        c1.insets = new Insets(12,12,12,12);
        
        // panel definition
        JScrollPane tablePanel = new JScrollPane(table);
        mainGridBag.setConstraints(tablePanel, c1);
        
        winWidth = 12+5+17+5+12 + 100;
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(1, 1, 5, 0));

        cancelJButton = new JButton(NbBundle.getMessage(AbstractPopup.class, "LBL_Generic_Cancel"));
        cancelJButton.setMargin(new Insets(cancelJButton.getMargin().top, 12, cancelJButton.getMargin().bottom, 12));
        winWidth = 
                winWidth + (int) cancelJButton.getPreferredSize().getWidth();
        
        
        cancelJButton.addActionListener(this);
        for (int i = 0; i < popupButtons.length; i++) {
            ((JButton)popupButtons[i]).setMargin(new Insets(((JButton)popupButtons[i]).getMargin().top, 12, ((JButton)popupButtons[i]).getMargin().bottom, 12));
            winWidth = 
                winWidth + (int) ((JButton)popupButtons[i]).getPreferredSize().getWidth();
            if(popupButtons[i] != closeJButton) {
                buttonPanel.add((JButton)popupButtons[i]);
            }
        }
        
        JPanel standardButtonPanel = new JPanel();
        standardButtonPanel.setLayout(new GridLayout(1,1, 5, 0));
        standardButtonPanel.add(closeJButton);
        standardButtonPanel.add(cancelJButton);
        
        //JPanel extStandardButtonPanel = new JPanel();
        //extStandardButtonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        
        //extStandardButtonPanel.add(standardButtonPanel);
        //JPanel allButtons = new JPanel();
        //java.awt.GridBagLayout gridbag = new java.awt.GridBagLayout();
        //java.awt.GridBagConstraints c = new java.awt.GridBagConstraints();
        //c.fill = java.awt.GridBagConstraints.BOTH;
        //c.weightx = 1.5;
        //allButtons.setLayout(gridbag);
        //gridbag.setConstraints(buttonPanel, c);
        //allButtons.add(buttonPanel);
        //c.weightx = 1;
        //gridbag.setConstraints(extStandardButtonPanel, c);
        //allButtons.add(extStandardButtonPanel);
        
        add(tablePanel);
        
        c1.gridwidth = 1;
        c1.fill = java.awt.GridBagConstraints.BOTH;
        c1.weightx = 0.5;
        c1.weighty = 0.0;
        c1.gridx = 1;
        c1.gridy = 2;
        c1.insets = new Insets(0,12,12,12);
        
        mainGridBag.setConstraints(buttonPanel, c1);
        add(buttonPanel);
        
        c1.gridx = 2;
        mainGridBag.setConstraints(standardButtonPanel, c1);
        add(standardButtonPanel);
    }
    
    protected abstract void initJTable();
    protected abstract void initComponents();
    protected abstract void readSettings();
    public abstract String storeSettings();
    
}
