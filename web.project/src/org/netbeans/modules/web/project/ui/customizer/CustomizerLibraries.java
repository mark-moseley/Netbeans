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

package org.netbeans.modules.web.project.ui.customizer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.JTable;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.platform.PlatformsCustomizer;
import org.netbeans.api.java.platform.Specification;
import org.netbeans.modules.web.project.classpath.ClassPathSupport;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/** Customizer for general project attributes.
 *
 * @author  phrebejk
 */
public class CustomizerLibraries extends JPanel implements HelpCtx.Provider, ListDataListener {
    
    WebProjectProperties uiProperties;
    
    public CustomizerLibraries( WebProjectProperties uiProperties ) {
        this.uiProperties = uiProperties;
        initComponents();

        // Hide unused edit buttons
        jButtonEditC.setVisible( false );
        jButtonEditCT.setVisible( false );
        jButtonEditRT.setVisible( false );
        
        jTableCpC.setModel( uiProperties.JAVAC_CLASSPATH_MODEL );
        initTableVisualProperties(jTableCpC);
        uiProperties.CLASS_PATH_TABLE_ITEM_RENDERER.setBooleanRenderer(jTableCpC.getDefaultRenderer(Boolean.class));
        jTableCpC.setDefaultRenderer( ClassPathSupport.Item.class, uiProperties.CLASS_PATH_TABLE_ITEM_RENDERER );
        jTableCpC.setDefaultRenderer( Boolean.class, uiProperties.CLASS_PATH_TABLE_ITEM_RENDERER );
        
        WebClassPathUi.EditMediator.register( uiProperties.getProject(),
                                               WebClassPathUi.EditMediator.createListComponent( jTableCpC, uiProperties.JAVAC_CLASSPATH_MODEL.getDefaultListModel()),  
                                               jButtonAddJarC.getModel(), 
                                               jButtonAddLibraryC.getModel(), 
                                               jButtonAddArtifactC.getModel(), 
                                               jButtonRemoveC.getModel(), 
                                               jButtonMoveUpC.getModel(), 
                                               jButtonMoveDownC.getModel(),
                                               true );
        
        jListCpCT.setModel( uiProperties.JAVAC_TEST_CLASSPATH_MODEL);
        jListCpCT.setCellRenderer( uiProperties.CLASS_PATH_LIST_RENDERER );
        WebClassPathUi.EditMediator.register( uiProperties.getProject(),
                                               WebClassPathUi.EditMediator.createListComponent( jListCpCT) , 
                                               jButtonAddJarCT.getModel(), 
                                               jButtonAddLibraryCT.getModel(), 
                                               jButtonAddArtifactCT.getModel(), 
                                               jButtonRemoveCT.getModel(), 
                                               jButtonMoveUpCT.getModel(), 
                                               jButtonMoveDownCT.getModel(),
                                               false );
                
        jListCpRT.setModel( uiProperties.RUN_TEST_CLASSPATH_MODEL );
        jListCpRT.setCellRenderer( uiProperties.CLASS_PATH_LIST_RENDERER );
        WebClassPathUi.EditMediator.register( uiProperties.getProject(),
                                               WebClassPathUi.EditMediator.createListComponent( jListCpRT), 
                                               jButtonAddJarRT.getModel(), 
                                               jButtonAddLibraryRT.getModel(), 
                                               jButtonAddArtifactRT.getModel(), 
                                               jButtonRemoveRT.getModel(), 
                                               jButtonMoveUpRT.getModel(), 
                                               jButtonMoveDownRT.getModel(),
                                               false );
        
        jCheckBoxBuildSubprojects.setModel( uiProperties.NO_DEPENDENCIES_MODEL );                        
        jComboBoxTarget.setModel(uiProperties.PLATFORM_MODEL);               
        
        testBroken();
        
        uiProperties.JAVAC_CLASSPATH_MODEL.getDefaultListModel().addListDataListener( this );
        uiProperties.JAVAC_TEST_CLASSPATH_MODEL.addListDataListener( this );
        uiProperties.RUN_TEST_CLASSPATH_MODEL.addListDataListener( this );
    }
        
    private void testBroken() {
        
        DefaultListModel[] models = new DefaultListModel[] {
            uiProperties.JAVAC_CLASSPATH_MODEL.getDefaultListModel(),
            uiProperties.JAVAC_TEST_CLASSPATH_MODEL,
            uiProperties.RUN_TEST_CLASSPATH_MODEL,
        };
        
        boolean broken = false;
        
        for( int i = 0; i < models.length; i++ ) {
            for( Iterator it = ClassPathUiSupport.getIterator( models[i] ); it.hasNext(); ) {
                if ( ((ClassPathSupport.Item)it.next()).isBroken() ) {
                    broken = true;
                    break;
                }
            }
            if ( broken ) {
                break;
            }
        }
        
        if ( broken ) {
            jLabelErrorMessage.setText( NbBundle.getMessage( CustomizerLibraries.class, "LBL_CustomizeLibraries_Libraries_Error" ) ); // NOI18N
        }
        else {
            jLabelErrorMessage.setText( " " ); // NOI18N
        }
        
    }
    
    private void initTableVisualProperties(JTable table) {
        //table.setGridColor(jTableCpC.getBackground());
        table.setRowHeight(jTableCpC.getRowHeight() + 4);        
        table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);        
        table.setIntercellSpacing(new java.awt.Dimension(0, 0));        
        // set the color of the table's JViewport
        table.getParent().setBackground(table.getBackground());
        table.setShowHorizontalLines(false);
        table.setShowVerticalLines(false);
        
        TableColumn column = table.getColumnModel().getColumn(1);
        JTableHeader header = table.getTableHeader();
        column.setMaxWidth(8 + SwingUtilities.computeStringWidth(header.getFontMetrics(header.getFont()), String.valueOf(column.getHeaderValue())));
    }
    
    // Implementation of HelpCtx.Provider --------------------------------------
    
    public HelpCtx getHelpCtx() {
        return new HelpCtx( CustomizerLibraries.class );
    }        

    
    // Implementation of ListDataListener --------------------------------------
    
    
    public void intervalRemoved( ListDataEvent e ) {
        testBroken(); 
    }

    public void intervalAdded( ListDataEvent e ) {
        // NOP
    }

    public void contentsChanged( ListDataEvent e ) {
        // NOP
    }
    
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;

        jLabelTarget = new javax.swing.JLabel();
        jComboBoxTarget = new javax.swing.JComboBox();
        jButton1 = new javax.swing.JButton();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanelCompile = new javax.swing.JPanel();
        librariesJLabel1 = new javax.swing.JLabel();
        librariesJScrollPane = new javax.swing.JScrollPane();
        jTableCpC = new javax.swing.JTable();
        jButtonAddArtifactC = new javax.swing.JButton();
        jButtonAddLibraryC = new javax.swing.JButton();
        jButtonAddJarC = new javax.swing.JButton();
        jButtonEditC = new javax.swing.JButton();
        jButtonRemoveC = new javax.swing.JButton();
        jButtonMoveUpC = new javax.swing.JButton();
        jButtonMoveDownC = new javax.swing.JButton();
        jPanelCompileTests = new javax.swing.JPanel();
        librariesJLabel2 = new javax.swing.JLabel();
        librariesJScrollPane1 = new javax.swing.JScrollPane();
        jListCpCT = new javax.swing.JList();
        jButtonAddArtifactCT = new javax.swing.JButton();
        jButtonAddLibraryCT = new javax.swing.JButton();
        jButtonAddJarCT = new javax.swing.JButton();
        jButtonEditCT = new javax.swing.JButton();
        jButtonRemoveCT = new javax.swing.JButton();
        jButtonMoveUpCT = new javax.swing.JButton();
        jButtonMoveDownCT = new javax.swing.JButton();
        jPanelRunTests = new javax.swing.JPanel();
        librariesJLabel4 = new javax.swing.JLabel();
        librariesJScrollPane3 = new javax.swing.JScrollPane();
        jListCpRT = new javax.swing.JList();
        jButtonAddArtifactRT = new javax.swing.JButton();
        jButtonAddLibraryRT = new javax.swing.JButton();
        jButtonAddJarRT = new javax.swing.JButton();
        jButtonEditRT = new javax.swing.JButton();
        jButtonRemoveRT = new javax.swing.JButton();
        jButtonMoveUpRT = new javax.swing.JButton();
        jButtonMoveDownRT = new javax.swing.JButton();
        jCheckBoxBuildSubprojects = new javax.swing.JCheckBox();
        jLabelErrorMessage = new javax.swing.JLabel();

        setLayout(new java.awt.GridBagLayout());

        jLabelTarget.setLabelFor(jComboBoxTarget);
        org.openide.awt.Mnemonics.setLocalizedText(jLabelTarget, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeGeneral_Platform_JLabel"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 12);
        add(jLabelTarget, gridBagConstraints);
        jLabelTarget.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "ACSD_CustomizerGeneral_jLabelTarget"));

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        add(jComboBoxTarget, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jButton1, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeGeneral_Platform_JButton"));
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                createNewPlatform(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 12, 0);
        add(jButton1, gridBagConstraints);
        jButton1.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "ACSD_CustomizerGeneral_jButton1"));

        jPanelCompile.setLayout(new java.awt.GridBagLayout());

        jPanelCompile.setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(8, 8, 8, 8)));
        org.openide.awt.Mnemonics.setLocalizedText(librariesJLabel1, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeLibraries_LibrariesC_JLabel"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        jPanelCompile.add(librariesJLabel1, gridBagConstraints);

        jTableCpC.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        librariesJScrollPane.setViewportView(jTableCpC);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridheight = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 12);
        jPanelCompile.add(librariesJScrollPane, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jButtonAddArtifactC, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeLibraries_AddProject_JButton"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        jPanelCompile.add(jButtonAddArtifactC, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jButtonAddLibraryC, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeLibraries_AddLibary_JButton"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        jPanelCompile.add(jButtonAddLibraryC, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jButtonAddJarC, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeLibraries_AddJar_JButton"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        jPanelCompile.add(jButtonAddJarC, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jButtonEditC, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeLibraries_Edit_JButton"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        jPanelCompile.add(jButtonEditC, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jButtonRemoveC, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeLibraries_Remove_JButton"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        jPanelCompile.add(jButtonRemoveC, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jButtonMoveUpC, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeLibraries_MoveUp_JButton"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        jPanelCompile.add(jButtonMoveUpC, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jButtonMoveDownC, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeLibraries_MoveDown_JButton"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        jPanelCompile.add(jButtonMoveDownC, gridBagConstraints);

        jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeLibraries_LibrariesTab"), jPanelCompile);

        jPanelCompileTests.setLayout(new java.awt.GridBagLayout());

        jPanelCompileTests.setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(8, 8, 8, 8)));
        org.openide.awt.Mnemonics.setLocalizedText(librariesJLabel2, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeLibraries_LibrariesCT_JLabel"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        jPanelCompileTests.add(librariesJLabel2, gridBagConstraints);

        librariesJScrollPane1.setViewportView(jListCpCT);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridheight = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 12);
        jPanelCompileTests.add(librariesJScrollPane1, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jButtonAddArtifactCT, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeLibraries_AddProject_JButton"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        jPanelCompileTests.add(jButtonAddArtifactCT, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jButtonAddLibraryCT, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeLibraries_AddLibary_JButton"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        jPanelCompileTests.add(jButtonAddLibraryCT, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jButtonAddJarCT, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeLibraries_AddJar_JButton"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        jPanelCompileTests.add(jButtonAddJarCT, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jButtonEditCT, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeLibraries_Edit_JButton"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        jPanelCompileTests.add(jButtonEditCT, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jButtonRemoveCT, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeLibraries_Remove_JButton"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        jPanelCompileTests.add(jButtonRemoveCT, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jButtonMoveUpCT, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeLibraries_MoveUp_JButton"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        jPanelCompileTests.add(jButtonMoveUpCT, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jButtonMoveDownCT, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeLibraries_MoveDown_JButton"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        jPanelCompileTests.add(jButtonMoveDownCT, gridBagConstraints);

        jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeLibraries_TestLibrariesTab"), jPanelCompileTests);

        jPanelRunTests.setLayout(new java.awt.GridBagLayout());

        jPanelRunTests.setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(8, 8, 8, 8)));
        org.openide.awt.Mnemonics.setLocalizedText(librariesJLabel4, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeLibraries_LibrariesRT_JLabel"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        jPanelRunTests.add(librariesJLabel4, gridBagConstraints);

        librariesJScrollPane3.setViewportView(jListCpRT);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridheight = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 12);
        jPanelRunTests.add(librariesJScrollPane3, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jButtonAddArtifactRT, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeLibraries_AddProject_JButton"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        jPanelRunTests.add(jButtonAddArtifactRT, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jButtonAddLibraryRT, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeLibraries_AddLibary_JButton"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        jPanelRunTests.add(jButtonAddLibraryRT, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jButtonAddJarRT, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeLibraries_AddJar_JButton"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        jPanelRunTests.add(jButtonAddJarRT, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jButtonEditRT, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeLibraries_Edit_JButton"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        jPanelRunTests.add(jButtonEditRT, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jButtonRemoveRT, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeLibraries_Remove_JButton"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        jPanelRunTests.add(jButtonRemoveRT, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jButtonMoveUpRT, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeLibraries_MoveUp_JButton"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        jPanelRunTests.add(jButtonMoveUpRT, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jButtonMoveDownRT, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeLibraries_MoveDown_JButton"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        jPanelRunTests.add(jButtonMoveDownRT, gridBagConstraints);

        jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeLibraries_RunTests_Tab"), jPanelRunTests);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weighty = 1.0;
        add(jTabbedPane1, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jCheckBoxBuildSubprojects, org.openide.util.NbBundle.getMessage(CustomizerLibraries.class, "LBL_CustomizeLibraries_Build_Subprojects"));
        jCheckBoxBuildSubprojects.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        add(jCheckBoxBuildSubprojects, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jLabelErrorMessage, " ");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 0);
        add(jLabelErrorMessage, gridBagConstraints);

    }//GEN-END:initComponents

    private void createNewPlatform(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_createNewPlatform
        PlatformsCustomizer.showCustomizer(null);        
    }//GEN-LAST:event_createNewPlatform
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButtonAddArtifactC;
    private javax.swing.JButton jButtonAddArtifactCT;
    private javax.swing.JButton jButtonAddArtifactRT;
    private javax.swing.JButton jButtonAddJarC;
    private javax.swing.JButton jButtonAddJarCT;
    private javax.swing.JButton jButtonAddJarRT;
    private javax.swing.JButton jButtonAddLibraryC;
    private javax.swing.JButton jButtonAddLibraryCT;
    private javax.swing.JButton jButtonAddLibraryRT;
    private javax.swing.JButton jButtonEditC;
    private javax.swing.JButton jButtonEditCT;
    private javax.swing.JButton jButtonEditRT;
    private javax.swing.JButton jButtonMoveDownC;
    private javax.swing.JButton jButtonMoveDownCT;
    private javax.swing.JButton jButtonMoveDownRT;
    private javax.swing.JButton jButtonMoveUpC;
    private javax.swing.JButton jButtonMoveUpCT;
    private javax.swing.JButton jButtonMoveUpRT;
    private javax.swing.JButton jButtonRemoveC;
    private javax.swing.JButton jButtonRemoveCT;
    private javax.swing.JButton jButtonRemoveRT;
    private javax.swing.JCheckBox jCheckBoxBuildSubprojects;
    private javax.swing.JComboBox jComboBoxTarget;
    private javax.swing.JLabel jLabelErrorMessage;
    private javax.swing.JLabel jLabelTarget;
    private javax.swing.JList jListCpCT;
    private javax.swing.JList jListCpRT;
    private javax.swing.JPanel jPanelCompile;
    private javax.swing.JPanel jPanelCompileTests;
    private javax.swing.JPanel jPanelRunTests;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTable jTableCpC;
    private javax.swing.JLabel librariesJLabel1;
    private javax.swing.JLabel librariesJLabel2;
    private javax.swing.JLabel librariesJLabel4;
    private javax.swing.JScrollPane librariesJScrollPane;
    private javax.swing.JScrollPane librariesJScrollPane1;
    private javax.swing.JScrollPane librariesJScrollPane3;
    // End of variables declaration//GEN-END:variables
        
        
}
