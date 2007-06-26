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
 * The Original Software is the Accelerators module.
 * The Initial Developer of the Original Software is Andrei Badea.
 * Portions Copyright 2005-2006 Andrei Badea.
 * All Rights Reserved.
 *
 * Contributor(s): Andrei Badea
 *                 Petr Hrebejk 
 */

package org.netbeans.modules.jumpto.file;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Action;
import javax.swing.JLabel;
import javax.swing.KeyStroke;
import javax.swing.ListModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.api.project.ui.OpenProjects;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/**
 *
 * @author  Petr Hrebejk, Andrei Badea
 */
public class FileSearchPanel extends javax.swing.JPanel implements ActionListener, LazyListModel.Filter {
    
    private static final int BRIGHTER_COLOR_COMPONENT = 10;    
    private FileSearch search;
    private Project currentProject;
    private FileSearchAction action;
    private boolean containsScrollPane;
    
    private JLabel messageLabel;
    
            
    private FileDescription.Renderer renderer;
    
    public FileSearchPanel(FileSearchAction action) {
        this.action = action;
        this.search = new FileSearch( this );
        currentProject = FileSearchAction.findCurrentProject(); 
        if ( currentProject == null ) {
            currentProject = OpenProjects.getDefault().getMainProject();
        }
        
        initComponents();        
        
        this.containsScrollPane = true;
        Color bgColorBrighter = new Color(
                                    Math.min(getBackground().getRed() + BRIGHTER_COLOR_COMPONENT, 255),
                                    Math.min(getBackground().getGreen() + BRIGHTER_COLOR_COMPONENT, 255),
                                    Math.min(getBackground().getBlue() + BRIGHTER_COLOR_COMPONENT, 255)
                            );
        messageLabel = new JLabel();
        messageLabel.setBackground(bgColorBrighter);
        messageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        messageLabel.setEnabled(true);
        messageLabel.setText(NbBundle.getMessage(FileSearchPanel.class, "TXT_NoTypesFound")); // NOI18N
        messageLabel.setFont(resultList.getFont());
        
        caseSensitiveCheckBox.setSelected(FileSearchOptions.getCaseSensitive());
        hiddenFilesCheckBox.setSelected(FileSearchOptions.getShowHiddenFiles());
        mainProjectCheckBox.setSelected(FileSearchOptions.getPreferMainProject());

        if ( currentProject == null ) {
            mainProjectCheckBox.setEnabled(false);
            mainProjectCheckBox.setSelected(false);
        }
        else {
            ProjectInformation pi = currentProject.getLookup().lookup(ProjectInformation.class);
            mainProjectCheckBox.setText(NbBundle.getMessage(FileSearchPanel.class, "FMT_CurrentProjectLabel", pi.getDisplayName())); // NOI18N
        }
        
        mainProjectCheckBox.addActionListener(this);
        caseSensitiveCheckBox.addActionListener(this);
        hiddenFilesCheckBox.addActionListener(this);
        
        renderer = new FileDescription.Renderer( resultList );
        resultList.setCellRenderer( renderer );
                
        fileNameTextField.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
                updateFileName();
            }
            
            public void insertUpdate(DocumentEvent e) {
                updateFileName();
            }
            
            public void removeUpdate(DocumentEvent e) {
                updateFileName();
            }
        });
        
    }
  
    //Good for setting model form any thread  
    public void setModel( final boolean keepSelection, final boolean stillSearching) {
        
        final ListModel m = search.createModel(isCaseSensitive(), isPreferedProject(), isShowHiddenFiles(), stillSearching);
        
        renderer.setColorPrefered( isPreferedProject() );
        
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                
                //final ListModel m = search.createModel(isCaseSensitive(), isPreferedProject(), isShowHiddenFiles(), stillSearching);
                int currentSelection = -1;
                Rectangle visibleRect = null;
                
                if ( keepSelection ) {
                    currentSelection = resultList.getSelectedIndex();
                    visibleRect = resultList.getVisibleRect();
                }

                resultList.setModel(m); // XXX Nothing found
                
                if ( m.getSize() > 0 ) {
                    setListPanelContent(null, false);
                    action.getOpenButton().setEnabled(true);
                    resultList.setSelectedIndex(0);
                    resultList.ensureIndexIsVisible(0);
                }
                else {
                    action.getOpenButton().setEnabled(false);
                    if ( fileNameTextField.getText().trim().length() == 0 || 
                         RegexpFileFilter.onlyContainsWildcards(fileNameTextField.getText().trim())) {
                        setListPanelContent(null, false);
                    }
                    else if (!stillSearching) {
                        setListPanelContent( NbBundle.getMessage(FileSearchPanel.class, "TXT_NoTypesFound"), false ); // NOI18N
                    }
                }
                
                if ( currentSelection != -1 && visibleRect != null) {
                    resultList.scrollRectToVisible(visibleRect);
                    resultList.setSelectedIndex(currentSelection);
                }
            }
        });        
    }

    public FileSearch getSearch() {
        return search;
    }
   
    public Project[] getProjects() {        
        return OpenProjects.getDefault().getOpenProjects();
    }
    
    public void openSelectedItems() {
        Object[] selectedValues = resultList.getSelectedValues();
        if ( selectedValues != null ) {
            for(Object v : selectedValues) {
                if ( v instanceof FileDescription) {
                    ((FileDescription)v).open();
                }
            }
        }        
    }

    public Project getPreferedProject() {
        if ( !isPreferedProject() ) {
            return null;
        }
        else {
            return currentProject; 
        }
        
    }
    
    void setListPanelContent( String message, boolean waitIcon ) {
        
        if ( message == null && !containsScrollPane ) {
           listPanel.remove( messageLabel );
           listPanel.add( resultScrollPane );
           containsScrollPane = true;
           revalidate();
           repaint();
        }        
        else if ( message != null ) { 
           jTextFieldLocation.setText(""); 
           messageLabel.setText(message);
           messageLabel.setIcon( waitIcon ? FileDescription.Renderer.WAIT_ICON : null);
           if ( containsScrollPane ) {
               listPanel.remove( resultScrollPane );
               listPanel.add( messageLabel );
               containsScrollPane = false;
           }
           revalidate();
           repaint();
       }                
    }
    
    private boolean isShowHiddenFiles() {
        return hiddenFilesCheckBox.isSelected();
    }
    
    private boolean isPreferedProject() {
        return mainProjectCheckBox.isSelected();
    }
    
    private boolean isCaseSensitive() {
        return caseSensitiveCheckBox.isSelected();
    }
    
    private void updateFileName() {
        
        String fieldText = fileNameTextField.getText().trim();
        
        if ( fieldText.length() == 0 || RegexpFileFilter.onlyContainsWildcards(fieldText)) {   // Empty reset everything
            search.newSearchResults(null);
            setModel(false, false);            
        }
        else {            
            if ( search.isNewSearchNeeded(fieldText) ) {
                // We have to do the search again
                // search.setCurrentPrefix( null );
                setListPanelContent(NbBundle.getMessage(FileSearchPanel.class, "TXT_Searching"), true); // NOI18N            
                search.search(fieldText, false);
            }
            else {
                // We don't need to restart the search reset the model
                search.setCurrentPrefix( fieldText );                
                //setModel( false, false );
            }
        }
    }
        
    void cleanup() {
        if ( search != null ) {
            search.cancel( true );
        }
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        fileNameLabel = new javax.swing.JLabel();
        fileNameTextField = new javax.swing.JTextField();
        resultLabel = new javax.swing.JLabel();
        listPanel = new javax.swing.JPanel();
        resultScrollPane = new javax.swing.JScrollPane();
        resultList = new javax.swing.JList();
        caseSensitiveCheckBox = new javax.swing.JCheckBox();
        hiddenFilesCheckBox = new javax.swing.JCheckBox();
        mainProjectCheckBox = new javax.swing.JCheckBox();
        jLabelLocation = new javax.swing.JLabel();
        jTextFieldLocation = new javax.swing.JTextField();

        setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 8, 8, 8));
        setPreferredSize(new java.awt.Dimension(540, 280));
        setLayout(new java.awt.GridBagLayout());

        fileNameLabel.setFont(fileNameLabel.getFont());
        fileNameLabel.setLabelFor(fileNameTextField);
        org.openide.awt.Mnemonics.setLocalizedText(fileNameLabel, org.openide.util.NbBundle.getMessage(FileSearchPanel.class, "CTL_FileName")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 4, 0);
        add(fileNameLabel, gridBagConstraints);

        fileNameTextField.setFont(new java.awt.Font("Monospaced", 0, 12));
        fileNameTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fileNameTextFieldActionPerformed(evt);
            }
        });
        fileNameTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                fileNameTextFieldKeyPressed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 7, 0);
        add(fileNameTextField, gridBagConstraints);

        resultLabel.setLabelFor(resultList);
        org.openide.awt.Mnemonics.setLocalizedText(resultLabel, org.openide.util.NbBundle.getMessage(FileSearchPanel.class, "CTL_MatchingFiles")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 4, 0);
        add(resultLabel, gridBagConstraints);

        listPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        listPanel.setLayout(new java.awt.BorderLayout());

        resultScrollPane.setBorder(null);

        resultList.setFont(new java.awt.Font("Monospaced", 0, 12));
        resultList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                resultListValueChanged(evt);
            }
        });
        resultList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                resultListMouseReleased(evt);
            }
        });
        resultScrollPane.setViewportView(resultList);

        listPanel.add(resultScrollPane, java.awt.BorderLayout.CENTER);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 8, 0);
        add(listPanel, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(caseSensitiveCheckBox, org.openide.util.NbBundle.getMessage(FileSearchPanel.class, "LBL_CaseSensitive")); // NOI18N
        caseSensitiveCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        caseSensitiveCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        add(caseSensitiveCheckBox, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(hiddenFilesCheckBox, org.openide.util.NbBundle.getMessage(FileSearchPanel.class, "LBL_HiddenFiles")); // NOI18N
        hiddenFilesCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        hiddenFilesCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 8, 0, 0);
        add(hiddenFilesCheckBox, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(mainProjectCheckBox, org.openide.util.NbBundle.getMessage(FileSearchPanel.class, "LBL_PreferMainProject")); // NOI18N
        mainProjectCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        mainProjectCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 8, 0, 0);
        add(mainProjectCheckBox, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jLabelLocation, org.openide.util.NbBundle.getMessage(FileSearchPanel.class, "LBL_Location")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(8, 0, 4, 0);
        add(jLabelLocation, gridBagConstraints);

        jTextFieldLocation.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 8, 0);
        add(jTextFieldLocation, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

private void fileNameTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fileNameTextFieldActionPerformed
    if (!resultList.isSelectionEmpty()) {
        action.closeDialog();
        openSelectedItems();        
    }
}//GEN-LAST:event_fileNameTextFieldActionPerformed

private void resultListMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_resultListMouseReleased
    if ( evt.getClickCount() == 2 ) {
        fileNameTextFieldActionPerformed(null);
    }
}//GEN-LAST:event_resultListMouseReleased

private void resultListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_resultListValueChanged
         
        Object svObject = resultList.getSelectedValue();
        if ( svObject != null && svObject instanceof FileDescription ) {
            FileDescription selectedValue = (FileDescription)svObject;
            jTextFieldLocation.setText(FileUtil.getFileDisplayName(selectedValue.getFileObject()));
        }
        else {
            jTextFieldLocation.setText("");
        }
}//GEN-LAST:event_resultListValueChanged

    private void fileNameTextFieldKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_fileNameTextFieldKeyPressed
        Object actionKey = resultList.getInputMap().get(KeyStroke.getKeyStrokeForEvent(evt));
        
        // see JavaFastOpen.boundScrollingKey()
        boolean isListScrollAction = 
            "selectPreviousRow".equals(actionKey) || // NOI18N
            "selectPreviousRowExtendSelection".equals(actionKey) || // NOI18N            
            "selectNextRow".equals(actionKey) || // NOI18N
            "selectNextRowExtendSelection".equals(actionKey) || // NOI18N
            // "selectFirstRow".equals(action) || // NOI18N
            // "selectLastRow".equals(action) || // NOI18N
            "scrollUp".equals(actionKey) || // NOI18N            
            "scrollUpExtendSelection".equals(actionKey) || // NOI18N            
            "scrollDown".equals(actionKey) || // NOI18N
            "scrollDownExtendSelection".equals(actionKey); // NOI18N
        
        
        int selectedIndex = resultList.getSelectedIndex();
        ListModel model = resultList.getModel();
        int modelSize = model.getSize();
        
        // Wrap around
        if ( "selectNextRow".equals(actionKey) && 
              ( selectedIndex == modelSize - 1 ||
                ( selectedIndex == modelSize - 2 && 
                  model.getElementAt(modelSize - 1) == FileDescription.SEARCH_IN_PROGRES )
             ) ) {
            resultList.setSelectedIndex(0);
            resultList.ensureIndexIsVisible(0);
            return;
        }
        else if ( "selectPreviousRow".equals(actionKey) &&
                   selectedIndex == 0 ) {
            int last = modelSize - 1;
            
            if ( model.getElementAt(last) == FileDescription.SEARCH_IN_PROGRES ) {
                last--;
            } 
            
            resultList.setSelectedIndex(last);
            resultList.ensureIndexIsVisible(last);
            return;
        }
        
        if (isListScrollAction) {
            Action action = resultList.getActionMap().get(actionKey);
            action.actionPerformed(new ActionEvent(resultList, 0, (String)actionKey));
            evt.consume();
        }
    }//GEN-LAST:event_fileNameTextFieldKeyPressed
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox caseSensitiveCheckBox;
    private javax.swing.JLabel fileNameLabel;
    private javax.swing.JTextField fileNameTextField;
    private javax.swing.JCheckBox hiddenFilesCheckBox;
    private javax.swing.JLabel jLabelLocation;
    private javax.swing.JTextField jTextFieldLocation;
    private javax.swing.JPanel listPanel;
    private javax.swing.JCheckBox mainProjectCheckBox;
    private javax.swing.JLabel resultLabel;
    private javax.swing.JList resultList;
    private javax.swing.JScrollPane resultScrollPane;
    // End of variables declaration//GEN-END:variables
    
    public void actionPerformed(ActionEvent e) {
        
        setModel( false, false );
        
        if ( e.getSource() == caseSensitiveCheckBox ) {
            FileSearchOptions.setCaseSensitive(caseSensitiveCheckBox.isSelected());
        }
        else if ( e.getSource() == hiddenFilesCheckBox ) {
            FileSearchOptions.setShowHiddenFiles(hiddenFilesCheckBox.isSelected());            
        }
        else if ( e.getSource() == mainProjectCheckBox ) {            
            FileSearchOptions.setPreferMainProject(isPreferedProject());            
        }
    }
   
    public boolean accept(Object obj) {
        if ( obj instanceof FileDescription ) {
            FileDescription fd = (FileDescription)obj;
            return isShowHiddenFiles() ? true : fd.isVisible();
        }            
        return true;
    }

    public void scheduleUpdate(Runnable run) {
        SwingUtilities.invokeLater( run );
    }
}
