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


package org.netbeans.modules.uml.integration.finddialog.ui;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.prefs.Preferences;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.SoftBevelBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.diagrams.IProxyDiagram;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.ui.support.commondialogs.IErrorDialog;
import org.netbeans.modules.uml.ui.support.commondialogs.MessageIconKindEnum;
import org.netbeans.modules.uml.integration.finddialog.DefaultFindDialogResource;
import org.netbeans.modules.uml.integration.finddialog.FindController;
import org.netbeans.modules.uml.integration.finddialog.FindResults;
import org.netbeans.modules.uml.integration.finddialog.FindUtilities;
import org.netbeans.modules.uml.ui.support.ProductHelper;
import org.netbeans.modules.uml.ui.swing.commondialogs.JCenterDialog;
import org.netbeans.modules.uml.ui.swing.commondialogs.SwingErrorDialog;
import org.netbeans.modules.uml.util.DummyCorePreference;
import org.openide.awt.Mnemonics;
import org.openide.util.NbPreferences;

public class ReplaceDialogUI extends JCenterDialog
{
    private JScrollPane jScrollPane2;
    
    /** Creates new form finddialog */
    public ReplaceDialogUI(Frame parent, boolean modal, FindController controller)
    {
        super(parent, modal);
        
        setController(controller);
        initComponents();
        initTextFieldListeners();
        selectionListener = new SelectionListener();
        m_ResultsTable.getSelectionModel().addListSelectionListener(selectionListener);
        initDialog();
        center(parent);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents()
    {
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        
        mainPanel = new JPanel();
        searchPanelsPanel = new JPanel();
        findWhatFieldsPanel = new JPanel();
        statusFieldsPanel = new JPanel();
        replaceFielsPanel = new JPanel();
        textLabel = new JLabel();
        textLabel2 = new JLabel();
        m_FindCombo = new JComboBox();
        searchOptionsPanel = new JPanel();
        m_LoadExternalCheck = new JCheckBox();
        m_MatchCaseCheck = new JCheckBox();
        m_XpathCheck = new JCheckBox();
        m_WholeWordCheck = new JCheckBox();
        m_SearchAlias = new JRadioButton();
        projectFieldsPanel = new JPanel();
        projectListPanel = new JPanel();
        m_SearchElementsRadio = new JRadioButton();
        m_SearchDescriptionsRadio = new JRadioButton();
        m_ProjectList = new JList();
        searchInFieldsPanel = new JPanel();
        m_ProjectLabel = new JLabel();
        resultsFieldsPanel = new JPanel();
        m_ResultsLabel = new JLabel();
        FindTableModel model = new FindTableModel(this, null);
        m_ResultsTable = new JReplaceTable(model, this);
        navigateFieldsPanel = new JPanel();
        m_NavigateCheck = new JCheckBox();
        m_Status = new JLabel();
        replaceButtonsPanel = new JPanel();
        m_FindButton = new JButton();
        m_CloseButton = new JButton();
        m_ReplaceCombo = new JComboBox();
        m_ReplaceButton = new JButton();
        m_ReplaceAllButton = new JButton();
        replaceButtonsPositionPanel = new JPanel();
        
        
        setTitle(DefaultFindDialogResource.getString("IDS_REPLACETITLE"));
        
        addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowClosing(WindowEvent evt)
            {
                closeDialog(evt);
            }
        });
        
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(new EmptyBorder(new Insets(5, 5, 5, 5)));
        mainPanel.add(Box.createVerticalStrut(10));
        findWhatFieldsPanel.setLayout(new GridBagLayout());
        
        
        
        //CBeckham -  added to dynamicaly adjust panel size for larger fonts
        // Note...getFoint.getSize will not return the ide parm -fontsize
        //in most cases of localized version, the user will use the -fontsize to start the ide
        //regaqrdless of what the os font size setting is, however in some remote cases the user
        //may actaully have the OS fontsize setting high
        int fontsize;
        Font f = UIManager.getFont("controlFont"); //NOI18N
        if (f != null)
            fontsize = f.getSize();

        else
            fontsize = 12;

        int width  = 450;
        int height = 400;
        int multiplyer = 2;
        
        if (fontsize > 17)
            multiplyer =3;
        
        width  = width  + Math.round(width*(multiplyer*fontsize/100f));
        height = height + Math.round(height*(multiplyer*fontsize/100f));
        setSize(width,height);
        // CBeckham - end of add
        
        
        // text label
        textLabel.setText(DefaultFindDialogResource.determineText(
            DefaultFindDialogResource.getString("IDS_FINDWHAT")));
        
        textLabel.setLabelFor(m_FindCombo);
        
        DefaultFindDialogResource.setMnemonic(textLabel, 
            DefaultFindDialogResource.getString("IDS_FINDWHAT"));
        
        DefaultFindDialogResource.setFocusAccelerator(m_FindCombo, 
            DefaultFindDialogResource.getString("IDS_FINDWHAT"));
        
        textLabel.setName("findLabel");
        gridBagConstraints.gridx=0;
        gridBagConstraints.gridy=0;
        gridBagConstraints.weightx=0;
        gridBagConstraints.insets=new Insets(0,0,0,0);
        findWhatFieldsPanel.add(textLabel,gridBagConstraints);
        
        // combo box
        gridBagConstraints.gridx=1;
        gridBagConstraints.gridy=0;
        gridBagConstraints.weightx=0.9;
        gridBagConstraints.fill=GridBagConstraints.BOTH;
        gridBagConstraints.insets=new Insets(0,5,0,0);
        findWhatFieldsPanel.add(m_FindCombo,gridBagConstraints);
        m_FindCombo.setEditable(true);
        m_FindCombo.setMaximumRowCount(10);

        gridBagConstraints.gridx=0;
        gridBagConstraints.gridy=0;
        gridBagConstraints.insets=new Insets(5,0,5,0);
        gridBagConstraints.anchor=GridBagConstraints.PAGE_START;
        mainPanel.add(findWhatFieldsPanel,gridBagConstraints);
        mainPanel.add(findWhatFieldsPanel);
        

        searchPanelsPanel.setLayout(new GridBagLayout());

        ///////////////////////////////////////////////////////////////////////
        // Match Case/XPath Expr/Match Whole Word check boxes
        
        searchOptionsPanel.setLayout(new GridBagLayout());
        
        TitledBorder bord = new TitledBorder(
            DefaultFindDialogResource.getString("IDS_SEARCHOPTIONS"));
        
        searchOptionsPanel.setBorder(bord);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.anchor = GridBagConstraints.WEST;

//        m_LoadExternalCheck.setText(DefaultFindDialogResource.determineText(
//            DefaultFindDialogResource.getString("IDS_LOADEXTERNAL")));
//        
//        DefaultFindDialogResource.setMnemonic(m_LoadExternalCheck, 
//            DefaultFindDialogResource.getString("IDS_LOADEXTERNAL"));
//        
//        m_LoadExternalCheck.addActionListener(new ActionListener()
//        {
//            public void actionPerformed(ActionEvent evt)
//            {
//                onLoadExternalCheck(evt);
//            }
//        });
        
        // Match Case checkbox
        // default to checked to try and make the query faster
        m_MatchCaseCheck.setSelected(isMatchCase());
        m_Controller.setCaseSensitive(m_MatchCaseCheck.isSelected());
        
        m_MatchCaseCheck.setText(DefaultFindDialogResource.determineText(
            DefaultFindDialogResource.getString("IDS_MATCHCASE")));
        
        DefaultFindDialogResource.setMnemonic(m_MatchCaseCheck, 
            DefaultFindDialogResource.getString("IDS_MATCHCASE"));
        
        m_MatchCaseCheck.getAccessibleContext().setAccessibleDescription(
            DefaultFindDialogResource.getString("ACSD_MatchCaseCheck"));
        
        m_MatchCaseCheck.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent evt)
            {
                onMatchCaseCheck(evt);
            }
        });

        gridBagConstraints.gridx=0;
        gridBagConstraints.gridy=0;
        searchOptionsPanel.add(m_MatchCaseCheck, gridBagConstraints);
     
        
        // Match Whole Word checkbox
        m_WholeWordCheck.setText(DefaultFindDialogResource.determineText(
            DefaultFindDialogResource.getString("IDS_MATCHWHOLE")));
        
        DefaultFindDialogResource.setMnemonic(m_WholeWordCheck, 
            DefaultFindDialogResource.getString("IDS_MATCHWHOLE"));
        
        m_WholeWordCheck.getAccessibleContext().setAccessibleDescription(
            DefaultFindDialogResource.getString("ACSD_WholeWordCheck"));
        
        m_WholeWordCheck.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent evt)
            {
                onWholeWordCheck(evt);
            }
        });
        
        gridBagConstraints.gridx=0;
        gridBagConstraints.gridy=1;
        searchOptionsPanel.add(m_WholeWordCheck, gridBagConstraints);

        
        // This is an XPath Expression checkbox
        m_XpathCheck.setText(DefaultFindDialogResource.determineText(
            DefaultFindDialogResource.getString("IDS_XPATHEXPRESSION")));
        
        DefaultFindDialogResource.setMnemonic(m_XpathCheck,
            DefaultFindDialogResource.getString("IDS_XPATHEXPRESSION"));

        m_XpathCheck.getAccessibleContext().setAccessibleDescription(
            DefaultFindDialogResource.getString("ACSD_XpathCheck"));

        m_XpathCheck.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent evt)
            {
                onXPathCheck(evt);
            }
        });

        gridBagConstraints.gridx=0;
        gridBagConstraints.gridy=2;
        searchOptionsPanel.add(m_XpathCheck, gridBagConstraints);
        
        
        // add "search options" panel to search panels panel
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx=0;
        gridBagConstraints.gridy=0;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets=new Insets(5,0,5,5);
        searchPanelsPanel.add(searchOptionsPanel, gridBagConstraints);
        
        // Match Case/XPath Expr/Match Whole Word check boxes
        ///////////////////////////////////////////////////////////////////////

        
        ///////////////////////////////////////////////////////////////////////
        // Elements/Descriptions/Alias radio buttons

        searchInFieldsPanel.setLayout(new GridBagLayout());
        
        bord = new TitledBorder(
            DefaultFindDialogResource.getString("IDS_SEARCHIN"));
        
        searchInFieldsPanel.setBorder(bord);
        
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        
        // Elements radio button
        // default the dialog to have the element radio button checked
        m_SearchElementsRadio.setSelected(true);
        
        m_SearchElementsRadio.setText(DefaultFindDialogResource.determineText(
            DefaultFindDialogResource.getString("IDS_ELEMENTS")));
        
        DefaultFindDialogResource.setMnemonic(m_SearchElementsRadio,
            DefaultFindDialogResource.getString("IDS_ELEMENTS"));
        
        m_SearchElementsRadio.getAccessibleContext().setAccessibleDescription(
            DefaultFindDialogResource.getString("ACSD_Search_Element"));
        
        m_SearchElementsRadio.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent evt)
            {
                onSearchElementsRadio(evt);
            }
        });
        
        gridBagConstraints.gridx=0;
        gridBagConstraints.gridy=0;
        searchInFieldsPanel.add(m_SearchElementsRadio,gridBagConstraints);

        
        // Descriptions radio button
        m_SearchDescriptionsRadio.setText(DefaultFindDialogResource.determineText(
            DefaultFindDialogResource.getString("IDS_DESCRIPTIONS")));
        
        DefaultFindDialogResource.setMnemonic(m_SearchDescriptionsRadio, 
            DefaultFindDialogResource.getString("IDS_DESCRIPTIONS"));
        
        m_SearchDescriptionsRadio.getAccessibleContext().setAccessibleDescription(
            DefaultFindDialogResource.getString("ACSD_Search_Description"));
        
        m_SearchDescriptionsRadio.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent evt)
            {
                onSearchDescriptionsRadio(evt);
            }
        });
        
        gridBagConstraints.gridx=0;
        gridBagConstraints.gridy=1;
        searchInFieldsPanel.add(m_SearchDescriptionsRadio,gridBagConstraints);
        
        // Alias radio button
        m_SearchAlias.setText(DefaultFindDialogResource.determineText(
            DefaultFindDialogResource.getString("IDS_ALIASTEXT")));
        
        DefaultFindDialogResource.setMnemonic(m_SearchAlias, 
            DefaultFindDialogResource.getString("IDS_ALIASTEXT"));
        
        m_SearchAlias.getAccessibleContext().setAccessibleDescription(
            DefaultFindDialogResource.getString("ACSD_SearchAliasCheck"));
        
        m_SearchAlias.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent evt)
            {
                onAliasCheck(evt);
            }
        });

        gridBagConstraints.gridx=0;
        gridBagConstraints.gridy=2;
        searchInFieldsPanel.add(m_SearchAlias,gridBagConstraints);

        
        // add "search in" panel to search panels panel
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx=1;
        gridBagConstraints.gridy=0;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets=new Insets(5,0,5,5);
        searchPanelsPanel.add(searchInFieldsPanel, gridBagConstraints);

        // Elements/Eescriptions/Alias radio buttons
        ///////////////////////////////////////////////////////////////////////

        mainPanel.add(searchPanelsPanel);

        
        projectFieldsPanel.setLayout(new GridBagLayout());
        projectListPanel.setLayout(new GridBagLayout());
        
        Mnemonics.setLocalizedText(m_ProjectLabel,
            DefaultFindDialogResource.getString("IDS_PROJECTS"));
        
        m_ProjectLabel.setLabelFor(m_ProjectList);
        
        m_ProjectLabel.getAccessibleContext().setAccessibleDescription(
            DefaultFindDialogResource.getString("ACSD_ProjectLabel"));
        
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets=new Insets(0,0,5,0);
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        projectListPanel.add(m_ProjectLabel, gridBagConstraints);
        
        m_ProjectList.setBorder(new LineBorder(new Color(0, 0, 0)));
        m_ProjectList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        jScrollPrjList = new JScrollPane(m_ProjectList);
        jScrollPrjList.setMinimumSize(new Dimension(30,80));
        jScrollPrjList.setPreferredSize(new Dimension(50,80));
        
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.insets = new Insets(0, 0, 0, 5);
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.weightx =9;
        projectListPanel.add(jScrollPrjList, gridBagConstraints);
        
        
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets=new Insets(0,0,5,0);
        projectFieldsPanel.add(projectListPanel, gridBagConstraints);
        
        mainPanel.add(projectFieldsPanel);
        
        // results grid
        Mnemonics.setLocalizedText(m_ResultsLabel,
            DefaultFindDialogResource.getString("LBL_SearchResult"));
        
        m_ResultsLabel.setLabelFor(m_ResultsTable);
        
        m_ResultsLabel.getAccessibleContext().setAccessibleDescription(
            DefaultFindDialogResource.getString("LBL_SearchResult"));
        
        jScrollPane2 = new JScrollPane(m_ResultsTable);
        resultsFieldsPanel.setLayout(new GridBagLayout());
        GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
        gridBagConstraints2.gridx=0;
        gridBagConstraints2.gridy=0;
        gridBagConstraints2.insets=new Insets(5,0,5,0);
        gridBagConstraints2.fill = GridBagConstraints.BOTH;
        resultsFieldsPanel.add(m_ResultsLabel, gridBagConstraints2);
        
        m_ResultsTable.setBorder(new SoftBevelBorder(BevelBorder.LOWERED));
        gridBagConstraints2.gridx=0;
        gridBagConstraints2.gridy=1;
        gridBagConstraints2.fill = GridBagConstraints.BOTH;
        gridBagConstraints2.weightx = 1.0;
        gridBagConstraints2.weighty = 1.0;
        resultsFieldsPanel.add(jScrollPane2, gridBagConstraints2);
        mainPanel.add(resultsFieldsPanel);
        
        // navigate check
        navigateFieldsPanel.setLayout(new GridBagLayout());
        // default the navigate button to true
        m_NavigateCheck.setSelected(true);
        
        m_NavigateCheck.setText(DefaultFindDialogResource.determineText(
            DefaultFindDialogResource.getString("IDS_NAVIGATE")));
        
        DefaultFindDialogResource.setMnemonic(m_NavigateCheck, 
            DefaultFindDialogResource.getString("IDS_NAVIGATE"));
        
        m_NavigateCheck.getAccessibleContext().setAccessibleDescription(
            DefaultFindDialogResource.getString("IDS_NAVIGATE"));
        
        gridBagConstraints.gridx=0;
        gridBagConstraints.gridy=0;
        gridBagConstraints.weightx=1;
        gridBagConstraints.anchor=GridBagConstraints.LINE_START;
        gridBagConstraints.fill=GridBagConstraints.HORIZONTAL;
        navigateFieldsPanel.add(m_NavigateCheck,gridBagConstraints);
        
        m_NavigateCheck.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent evt)
            {
                onNavigateCheck(evt);
            }
        });
        
        mainPanel.add(navigateFieldsPanel);
        
        // replace combo
        replaceFielsPanel.setLayout(new GridBagLayout());
        // text label
        textLabel2.setText(DefaultFindDialogResource.determineText(
            DefaultFindDialogResource.getString("IDS_REPLACEWITH")));
        
        textLabel2.setLabelFor(m_ReplaceCombo);
        
        DefaultFindDialogResource.setMnemonic(textLabel2, 
            DefaultFindDialogResource.getString("IDS_REPLACEWITH"));
        
        DefaultFindDialogResource.setFocusAccelerator(m_ReplaceCombo, 
            DefaultFindDialogResource.getString("IDS_REPLACEWITH"));
        
        textLabel2.setName("replaceLabel");
        gridBagConstraints.gridx=0;
        gridBagConstraints.gridy=0;
        gridBagConstraints.insets=new Insets(0,5,0,5);
        gridBagConstraints.weightx=0;
        replaceFielsPanel.add(textLabel2, gridBagConstraints);
        
        // combo box
        m_ReplaceCombo.setEditable(true);
        m_ReplaceCombo.setMaximumRowCount(10);
        gridBagConstraints.gridx=1;
        gridBagConstraints.gridy=0;
        gridBagConstraints.weightx=0.9;
        gridBagConstraints.fill=GridBagConstraints.BOTH;
        gridBagConstraints.insets=new Insets(0,5,0,0);
        replaceFielsPanel.add(m_ReplaceCombo,gridBagConstraints);

        mainPanel.add(replaceFielsPanel);
        
        // status
        statusFieldsPanel.setLayout(new GridBagLayout());
        m_Status.setMaximumSize(new Dimension(2147483647, 20));
        gridBagConstraints.gridx=0;
        gridBagConstraints.gridy=0;
        gridBagConstraints.fill=GridBagConstraints.BOTH;
        statusFieldsPanel.add(m_Status,gridBagConstraints);

        mainPanel.add(statusFieldsPanel);
        mainPanel.add(Box.createVerticalStrut(10));
        
        getContentPane().add(mainPanel, BorderLayout.CENTER);
        
        // find/close buttons
        replaceButtonsPanel.setLayout(
            new BoxLayout(replaceButtonsPanel, BoxLayout.Y_AXIS));
        
        replaceButtonsPanel.setBorder(
            new EmptyBorder(new Insets(5, 5, 5, 5)));
        
        m_FindButton.setEnabled(false);
        
        m_FindButton.setText(DefaultFindDialogResource.determineText(
            DefaultFindDialogResource.getString("IDS_FIND")));
        
        DefaultFindDialogResource.setMnemonic(m_FindButton, 
            DefaultFindDialogResource.getString("IDS_FIND"));
        
        m_FindButton.getAccessibleContext().setAccessibleDescription(
            DefaultFindDialogResource.getString("IDS_FIND"));
        
        m_FindButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent evt)
            {
                onFindButton(evt);
            }
        });
        
        getRootPane().setDefaultButton(m_FindButton);
        replaceButtonsPanel.add(Box.createVerticalStrut(9));
        replaceButtonsPanel.add(m_FindButton);
        
        m_CloseButton.setText(DefaultFindDialogResource.determineText(
            DefaultFindDialogResource.getString("IDS_CLOSE")));
        
        DefaultFindDialogResource.setMnemonic(m_CloseButton, 
            DefaultFindDialogResource.getString("IDS_CLOSE"));
        
        m_CloseButton.getAccessibleContext().setAccessibleDescription(
            DefaultFindDialogResource.getString("IDS_CLOSE"));
        
        m_CloseButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent evt)
            {
                setVisible(false);
                dispose();
            }
        });
        
        replaceButtonsPanel.add(Box.createVerticalStrut(3));
        replaceButtonsPanel.add(m_CloseButton);
        replaceButtonsPanel.add(replaceButtonsPositionPanel);
        
        m_ReplaceButton.setText(DefaultFindDialogResource.determineText(
            DefaultFindDialogResource.getString("IDS_REPLACE")));
        
        DefaultFindDialogResource.setMnemonic(m_ReplaceButton, 
            DefaultFindDialogResource.getString("IDS_REPLACE"));
        
        m_ReplaceButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent evt)
            {
                onReplaceButton(evt);
            }
        });
        
        replaceButtonsPanel.add(m_ReplaceButton);
        
        m_ReplaceAllButton.setText(DefaultFindDialogResource.determineText(
            DefaultFindDialogResource.getString("IDS_REPLACEALL")));
        
        DefaultFindDialogResource.setMnemonic(m_ReplaceAllButton, 
            DefaultFindDialogResource.getString("IDS_REPLACEALL"));
        
        m_ReplaceAllButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent evt)
            {
                onReplaceAllButton(evt);
            }
        });
        
        replaceButtonsPanel.add(Box.createVerticalStrut(3));
        replaceButtonsPanel.add(m_ReplaceAllButton);
        replaceButtonsPanel.add(Box.createVerticalStrut(10));
        getContentPane().add(replaceButtonsPanel, BorderLayout.EAST);
        
        // now figure out the button sizes
        Dimension buttonSize = getMaxButtonWidth();
        m_FindButton.setMaximumSize(buttonSize);
        m_FindButton.setPreferredSize(buttonSize);
        m_CloseButton.setPreferredSize(buttonSize);
        m_CloseButton.setMaximumSize(buttonSize);
        m_ReplaceButton.setPreferredSize(buttonSize);
        m_ReplaceButton.setMaximumSize(buttonSize);
        m_ReplaceAllButton.setPreferredSize(buttonSize);
        m_ReplaceAllButton.setMaximumSize(buttonSize);
        
        getAccessibleContext().setAccessibleDescription(
            DefaultFindDialogResource.getString(
            "Action.ReplaceSymbol.Description"));
    }
    
    private Dimension getMaxButtonWidth()
    {
        Dimension ret = null;
        Dimension d = m_FindButton.getPreferredSize();
        double max  = d.width;
        
        d = m_CloseButton.getPreferredSize();
        if(d.width > max)
        {
            max = d.width;
            ret = d;
        }
        d = m_ReplaceButton.getPreferredSize();
        if(d.width > max)
        {
            max = d.width;
            ret = d;
        }
        d = m_ReplaceAllButton.getPreferredSize();
        if(d.width > max)
        {
            max = d.width;
            ret = d;
        }
        
        return ret;
        
    }
    
    private void initTextFieldListeners()
    {
        class TextChangeListener implements DocumentListener
        {
            private JTextField textField;
            TextChangeListener(JTextField textField)
            {
                this.textField = textField;
            }
            public void changedUpdate(DocumentEvent e)
            {
                documentChanged();
            }
            public void insertUpdate(DocumentEvent e)
            {
                documentChanged();
            }
            public void removeUpdate(DocumentEvent e)
            {
                documentChanged();
            }
            private void documentChanged()
            {
                updateState(textField);
            }
        }
        
        ((JTextField)m_FindCombo.getEditor().getEditorComponent()).getDocument().addDocumentListener(
            new TextChangeListener((JTextField)m_FindCombo.getEditor().getEditorComponent()));
        
        ((JTextField)m_ReplaceCombo.getEditor().getEditorComponent()).getDocument().addDocumentListener(
            new TextChangeListener((JTextField)m_ReplaceCombo.getEditor().getEditorComponent()));
    }
    
    private void updateState(JTextField textField)
    {
        if (update == false)
            return;
        
        String text = textField.getText().trim();
        
        if (textField==(JTextField)m_FindCombo.getEditor().getEditorComponent())
        {
            m_FindButton.setEnabled(!"".equals(text));
        }
        
        else if (textField==(JTextField)m_ReplaceCombo.getEditor().getEditorComponent())
        {
            m_ReplaceButton.setEnabled(!"".equals(text) && m_ResultsTable.getSelectedRowCount()>0);
            m_ReplaceAllButton.setEnabled(!"".equals(text));
        }
    }
    
    
//    private void onLoadExternalCheck(ActionEvent evt)
//    {
//        Object obj = evt.getSource();
//
//        if (obj instanceof JCheckBox)
//        {
//            JCheckBox box = (JCheckBox)obj;
//            boolean checkboxState = box.isSelected();
//
//            if (checkboxState)
//                m_Controller.setExternalLoad(true);
//            
//            else
//                m_Controller.setExternalLoad(false);
//        }
//    }
    
    private void onXPathCheck(ActionEvent evt)
    {
        Object obj = evt.getSource();

        if (obj instanceof JCheckBox)
        {
            JCheckBox box = (JCheckBox)obj;
            boolean checkboxState = box.isSelected();
            
            if (checkboxState)
            {
                m_Controller.setKind(1);
                m_Controller.setCaseSensitive(true);
                m_MatchCaseCheck.setEnabled(false);
                m_SearchDescriptionsRadio.setEnabled(false);
                m_SearchElementsRadio.setEnabled(false);
                m_SearchAlias.setEnabled(false);
                m_WholeWordCheck.setEnabled(false);
            }
            
            else
            {
                m_Controller.setKind(0);
                m_Controller.setCaseSensitive(m_MatchCaseCheck.isSelected());
                m_MatchCaseCheck.setEnabled(true);
                m_SearchDescriptionsRadio.setEnabled(true);
                m_SearchElementsRadio.setEnabled(true);
                m_SearchAlias.setEnabled(true);
                m_WholeWordCheck.setEnabled(true);
            }
        }
    }
    
    private void onAliasCheck(ActionEvent evt)
    {
        Object obj = evt.getSource();
        
        if (obj instanceof JRadioButton)
        {
            m_Controller.setResultType(-1);
            m_Controller.setSearchAlias(true);
            m_SearchElementsRadio.setSelected(false);
            m_SearchDescriptionsRadio.setSelected(false);
            m_SearchAlias.setSelected(true);
        }
    }
    
    private void onWholeWordCheck(ActionEvent evt)
    {
        Object obj = evt.getSource();

        if (obj instanceof JCheckBox)
        {
            JCheckBox box = (JCheckBox)obj;
            boolean checkboxState = box.isSelected();
            
            if (checkboxState)
                m_Controller.setWholeWordSearch(true);

            else
                m_Controller.setWholeWordSearch(false);
        }
    }
    
    private void onMatchCaseCheck(ActionEvent evt)
    {
        Object obj = evt.getSource();
        
        if (obj instanceof JCheckBox)
        {
            Preferences prefs = NbPreferences.forModule (DummyCorePreference.class) ;
            JCheckBox box = (JCheckBox)obj;
            boolean checkboxState = box.isSelected();
        
            if (checkboxState)
            {
                prefs.put ("UML_ShowMe_Allow_Lengthy_Searches", "PSK_NEVER") ;
            }

            else
            {
                m_Controller.setCaseSensitive(false);
                String find = prefs.get ("UML_ShowMe_Allow_Lengthy_Searches", "PSK_ASK");
                
                if (find.equals("PSK_NEVER"))
                    prefs.put("UML_ShowMe_Allow_Lengthy_Searches", "PSK_ALWAYS");
            }
        }
    }
    
    private void onSearchElementsRadio(ActionEvent evt)
    {
        Object obj = evt.getSource();
        if (obj instanceof JRadioButton)
        {
            m_Controller.setResultType(0);
            m_SearchElementsRadio.setSelected(true);
            m_SearchDescriptionsRadio.setSelected(false);
            m_SearchAlias.setSelected(false);
            m_Controller.setSearchAlias(false);
        }
    }
    
    private void onSearchDescriptionsRadio(ActionEvent evt)
    {
        Object obj = evt.getSource();
        if (obj instanceof JRadioButton)
        {
            m_Controller.setResultType(1);
            m_SearchDescriptionsRadio.setSelected(true);
            m_SearchElementsRadio.setSelected(false);
            m_SearchAlias.setSelected(false);
            m_Controller.setSearchAlias(false);
        }
    }
    
    private void onNavigateCheck(ActionEvent evt)
    {
        Object obj = evt.getSource();

        if (obj instanceof JCheckBox)
        {
            JCheckBox box = (JCheckBox)obj;
            boolean checkboxState = box.isSelected();
            if (checkboxState)
                m_Controller.setDiagramNavigate(true);

            else
                m_Controller.setDiagramNavigate(false);
        }
    }
    
    private void onFindButton(ActionEvent evt)
    {
        Object obj = evt.getSource();
        try
        {
            FindUtilities.startWaitCursor(getContentPane());
            onFindButton();
        }

        catch (Exception ex)
        {
            String msg;
            
            if (m_XpathCheck.isSelected())
                msg = FindUtilities.translateString("IDS_ERROR1");

            else
                msg = FindUtilities.translateString("IDS_NONEFOUND");
            
            m_Status.setText(msg);
        }

        finally
        {
            FindUtilities.endWaitCursor(getContentPane());
        }
    }
    
    private void onFindButton() throws Exception
    {
        m_Status.setText("");
        update = false;
        String searchStr = (String)(m_FindCombo.getSelectedItem());
        
        boolean continueFlag = true;
        // Save the values of the search combo
        FindUtilities.saveSearchString("LastSearchStrings", m_FindCombo);
        // reset what is in the search combo
        
        FindUtilities.populateComboBoxes("LastSearchStrings", m_FindCombo);
        // if they have project selected, make sure there is a project selected
        
        int count = m_ProjectList.getSelectedIndex();
        if (count == -1)
        {
            continueFlag = false;
            String msg = FindUtilities.translateString("IDS_ERROR2");
            String title = FindUtilities.translateString("IDS_PROJNAME2");
            IErrorDialog pTemp = new SwingErrorDialog(this);
            if (pTemp != null)
            {
                pTemp.display(msg, MessageIconKindEnum.EDIK_ICONINFORMATION, title);
            }
        }
        
        if (continueFlag)
        {
            m_Controller.setSearchString(searchStr);
            FindUtilities.loadProjectListOfController(m_ProjectList, m_Controller);
            // do the search
            FindResults pResults = new FindResults();
            m_Controller.search(pResults);
            if (pResults != null)
            {
                ETList<IElement> pElements = pResults.getElements();
                ETList<IProxyDiagram> pDiagrams = pResults.getDiagrams();
                if ( (pElements != null) && (pDiagrams != null))
                {
                    int countD = pDiagrams.size();
                    if (pElements.size() > 0 || countD > 0)
                    {
                        // show the results
                        ETList< Object > findResults = FindUtilities.loadResultsIntoArray(pResults);
                        FindTableModel model = new FindTableModel(this, findResults);
                        m_ResultsTable.setModel(model);
                        m_ReplaceCombo.setEnabled(true);
                        
                        long totalC = pElements.size() + countD;
                        String strMsg = totalC + " ";
                        strMsg += FindUtilities.translateString("IDS_NUMFOUND");
                        m_Status.setText(strMsg);
                        //
                        // This is special code to aid in the automating testing.  We had no way to access
                        // the information in the grid from the automated scripts and/or VisualTest, so
                        // if a flag is set in the registry, we will dump the results of the grid to a
                        // specified file
                        //
                            /* TODO
                            if( GETDEBUGFLAG_RELEASE(_T("DumpGridResults"), 0))
                            {
                             CComBSTR file = CRegistry::GetItem( CString(_T("DumpGridResultsFile")), CString(_T("")));
                                 if (file.Length())
                                 {
                                     m_FlexGrid->SaveGrid(file, flexFileCommaText, CComVariant(FALSE));
                                 }
                             }
                             */
                    }
                    else
                    {
                        clearGrid();
                        String noneStr = FindUtilities.translateString("IDS_NONEFOUND");
                        m_Status.setText(noneStr);
                    }
                }
                else
                {
                    String canStr = FindUtilities.translateString("IDS_CANCELLED");
                    m_Status.setText(canStr);
                }
            }
            else
            {
                String str2 = FindUtilities.translateString("IDS_NONEFOUND2");
                m_Status.setText(str2);
            }
            
        }
        m_FindCombo.setSelectedItem(searchStr);

        update = true;
        if (m_ReplaceCombo.isEnabled())
            updateState((JTextField)m_ReplaceCombo.getEditor().getEditorComponent());
        m_FindCombo.getEditor().selectAll();
    }
    
    
    private void onReplaceButton(ActionEvent evt)
    {
        Object obj = evt.getSource();
        if (obj instanceof JButton)
        {
            m_Status.setText("");
            String str;
            str = (String)(m_ReplaceCombo.getSelectedItem());
            if (str != null && str.length() > 0)
            {
                FindUtilities.startWaitCursor(getContentPane());
                // Save the values of the search combo
                FindUtilities.saveSearchString("LastReplaceStrings", m_ReplaceCombo);
                // reset what is in the search combo
                FindUtilities.populateComboBoxes("LastReplaceStrings", m_ReplaceCombo);
                if (m_Controller != null)
                {
                    m_Controller.setReplaceString(str);
                    FindResults pResults = new FindResults();
                    if (pResults != null)
                    {
                        loadResultsFromGrid(pResults, true);
                        ETList<IElement> pElements = pResults.getElements();
                        ETList<IProxyDiagram> pDiagrams = pResults.getDiagrams();
                        if ( (pElements != null) && (pDiagrams != null))
                        {
                            int count = pElements.size();
                            int countD = pDiagrams.size();
                            if (count > 0 || countD > 0)
                            {
                                // clear the grid
                                m_Controller.replace(pResults);
                                ETList< Object > findResults = FindUtilities.loadResultsIntoArray(pResults);
                                FindTableModel model = new FindTableModel(this, findResults);
                                m_ResultsTable.setModel(model);
                                
                                //
                                // This is special code to aid in the automating testing.  We had no way to access
                                // the information in the grid from the automated scripts and/or VisualTest, so
                                // if a flag is set in the registry, we will dump the results of the grid to a
                                // specified file
                                //
                                // TODO
                                //if( GETDEBUGFLAG_RELEASE(_T("DumpGridResults"), 0))
                                //{
                                //	CComBSTR file = CRegistry::GetItem( CString(_T("DumpGridResultsFile")), CString(_T("")));
                                //	if (file.Length())
                                //	{
                                //		m_FlexGrid->SaveGrid(file, flexFileCommaText, CComVariant(FALSE));
                                //	}
                                //}
                            }
                            else
                            {
                                // no items selected in the grid
                                String noneStr = FindUtilities.translateString("IDS_NOITEMSSELECTED");
                                String str2 = FindUtilities.translateString("IDS_PROJNAME2");
                                IErrorDialog pTemp = new SwingErrorDialog(this);
                                if (pTemp != null)
                                {
                                    pTemp.display(noneStr, MessageIconKindEnum.EDIK_ICONINFORMATION, str2);
                                }
                            }
                        }
                    }
                }
                m_ReplaceCombo.setSelectedItem(str);
                FindUtilities.endWaitCursor(getContentPane());
                disableReplaceSection();
            }
            else
            {
                String strNo = FindUtilities.translateString("IDS_NOREPLACESTR");
                String str2 = FindUtilities.translateString("IDS_PROJNAME2");
                IErrorDialog pTemp = new SwingErrorDialog(this);
                if (pTemp != null)
                {
                    pTemp.display(strNo, MessageIconKindEnum.EDIK_ICONINFORMATION, str2);
                }
            }
        }
    }
    
    public void onDblClickFindResults(int row, FindTableModel model, boolean isShift)
    {
        m_Status.setText("");
        boolean hr = FindUtilities.onDblClickFindResults(row, model, m_Controller, isShift);
        if (!hr)
        {
            String msg =  FindUtilities.translateString("IDS_NOPRESELEMENTS");
            m_Status.setText(msg);
        }
    }
    
    private void onReplaceAllButton(ActionEvent evt)
    {
        Object obj = evt.getSource();
        if (obj instanceof JButton)
        {
            m_Status.setText("");
            String str;
            str = (String)(m_ReplaceCombo.getSelectedItem());
            if (str != null && str.length() > 0)
            {
                FindUtilities.startWaitCursor(getContentPane());
                // Save the values of the search combo
                FindUtilities.saveSearchString("LastReplaceStrings", m_ReplaceCombo);
                // reset what is in the search combo
                FindUtilities.populateComboBoxes("LastReplaceStrings", m_ReplaceCombo);
                if (m_Controller != null)
                {
                    m_Controller.setReplaceString(str);
                    FindResults pResults = new FindResults();
                    if (pResults != null)
                    {
                        loadResultsFromGrid(pResults, false);
                        // clear the grid
                        m_Controller.replace(pResults);
                        ETList< Object > findResults = FindUtilities.loadResultsIntoArray(pResults);
                        FindTableModel model = new FindTableModel(this, findResults);
                        m_ResultsTable.setModel(model);
                        //
                        // This is special code to aid in the automating testing.  We had no way to access
                        // the information in the grid from the automated scripts and/or VisualTest, so
                        // if a flag is set in the registry, we will dump the results of the grid to a
                        // specified file
                        //
                        // TODO
                        //if( GETDEBUGFLAG_RELEASE(_T("DumpGridResults"), 0))
                        //{
                        //	CComBSTR file = CRegistry::GetItem( CString(_T("DumpGridResultsFile")), CString(_T("")));
                        //	if (file.Length())
                        //	{
                        //		m_FlexGrid->SaveGrid(file, flexFileCommaText, CComVariant(FALSE));
                        //	}
                        //}
                    }
                }
                FindUtilities.endWaitCursor(getContentPane());
                disableReplaceSection();
            }
            else
            {
                String strNo = FindUtilities.translateString("IDS_NOREPLACESTR");
                String str2 = FindUtilities.translateString("IDS_PROJNAME2");
                IErrorDialog pTemp = new SwingErrorDialog(this);
                if (pTemp != null)
                {
                    pTemp.display(strNo, MessageIconKindEnum.EDIK_ICONINFORMATION, str2);
                }
            }
        }
    }
    
    private void loadResultsFromGrid(FindResults pResults, boolean bSelect)
    {
        if (pResults != null)
        {
            // get the elements array from the results object
            ETList<IElement> pElements = pResults.getElements();
            // get the diagrams array from the results object
            ETList<IProxyDiagram> pDiagrams = pResults.getDiagrams();
            if ( (pElements != null) && (pDiagrams != null))
            {
                if (bSelect)
                {
                    // loop through the information in the table
                    int[] selRows = m_ResultsTable.getSelectedRows();
                    for (int x = 0; x < selRows.length; x++)
                    {
                        int selRow = selRows[x];
                        FindTableModel model = (FindTableModel)m_ResultsTable.getModel();
                        
                        if (model != null)
                        {
                            IElement pElement = model.getElementAtRow(selRow);
                            if (pElement != null)
                            {
                                pElements.add(pElement);
                            }
                            else
                            {
                                IProxyDiagram pDiagram = model.getDiagramAtRow(selRow);
                                if (pDiagram != null)
                                {
                                    pDiagrams.add(pDiagram);
                                }
                            }
                        }
                    }
                }
                else
                {
                    int rows = m_ResultsTable.getRowCount();
                    for (int x = 0; x < rows; x++)
                    {
                        FindTableModel model = (FindTableModel)m_ResultsTable.getModel();
                        if (model != null)
                        {
                            IElement pElement = model.getElementAtRow(x);
                            if (pElement != null)
                            {
                                pElements.add(pElement);
                            }
                            else
                            {
                                IProxyDiagram pDiagram = model.getDiagramAtRow(x);
                                if (pDiagram != null)
                                {
                                    pDiagrams.add(pDiagram);
                                }
                            }
                        }
                    }
                    
                }
            }
        }
    }
    
    private void clearGrid()
    {
        // clear the results
        FindTableModel model = new FindTableModel(this, null);
        m_ResultsTable.setModel(model);
        disableReplaceSection();
    }
    
    private void disableReplaceSection()
    {
        m_ReplaceCombo.setEnabled(false);
        m_ReplaceButton.setEnabled(false);
        m_ReplaceAllButton.setEnabled(false);
    }
    
    
    private void initDialog()
    {
        m_Status.setText("");
        FindUtilities.populateProjectList(m_ProjectList);
        FindUtilities.selectProjectInList( m_ProjectList );
        FindUtilities.populateComboBoxes("LastSearchStrings", m_FindCombo);
        FindUtilities.populateComboBoxes("LastReplaceStrings", m_ReplaceCombo);
        disableReplaceSection();
        m_FindCombo.getEditor().selectAll();
    }
    
    public void setController(FindController controller)
    {
        m_Controller = controller;
        m_Controller.setDialog(this);
    }
    
    
    /** Closes the dialog */
    private void closeDialog(WindowEvent evt)
    {
        setVisible(false);
        dispose();
    }
    
    private boolean isMatchCase()
    {
        return !"PSK_ALWAYS".equals(ProductHelper.getPreferenceManager().getPreferenceValue("FindDialog", "LongSearch"));
    }
    
    private class SelectionListener implements ListSelectionListener
    {
        public void valueChanged(ListSelectionEvent e)
        {
            updateState((JTextField)m_ReplaceCombo.getEditor().getEditorComponent());
        }
    }
    
    // Variables declaration - do not modify
    private JButton m_FindButton;
    private JButton m_CloseButton;
    private JLabel textLabel;
    private JLabel textLabel2;
    private JComboBox m_FindCombo;
    private JCheckBox m_LoadExternalCheck;
    private JCheckBox m_MatchCaseCheck;
    private JCheckBox m_NavigateCheck;
    private JList m_ProjectList;
    private JRadioButton m_ProjectRadio;
    private JLabel m_ProjectLabel;
    private JLabel m_ResultsLabel;
    private JTable m_ResultsTable;
    private JRadioButton m_SearchAlias;
    private JRadioButton m_SearchDescriptionsRadio;
    private JRadioButton m_SearchElementsRadio;
    private JLabel m_Status;
    private JCheckBox m_WholeWordCheck;
    private JCheckBox m_XpathCheck;
    private JPanel mainPanel;
    private JPanel resultsFieldsPanel;
    private JPanel navigateFieldsPanel;
    private JPanel replaceButtonsPositionPanel;
    private JPanel replaceButtonsPanel;
    private JPanel searchPanelsPanel;
    private JPanel searchOptionsPanel;
    private JPanel searchInFieldsPanel;
    private JPanel findWhatFieldsPanel;
    private JPanel replaceFielsPanel;
    private JPanel statusFieldsPanel;
    private JPanel projectListPanel;
    private JPanel projectFieldsPanel;
    private JButton m_ReplaceButton;
    private JButton m_ReplaceAllButton;
    private JComboBox m_ReplaceCombo;
    private JScrollPane jScrollPrjList;
    private SelectionListener selectionListener;
    private boolean update = true;
    
    
    // End of variables declaration
    private org.netbeans.modules.uml.integration.finddialog.FindController m_Controller = null;
    private boolean m_Done = false;
    
}
