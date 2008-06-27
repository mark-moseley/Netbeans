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

package org.netbeans.modules.jumpto.symbol;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.io.IOException;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.KeyStroke;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.BadLocationException;
import org.netbeans.modules.jumpto.type.UiOptions;
import org.netbeans.spi.jumpto.symbol.SymbolDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 *
 * @author  Petr Hrebejk
 */
public class GoToPanel extends javax.swing.JPanel {
            
    private static Icon WAIT_ICON = new ImageIcon( Utilities.loadImage("org/netbeans/modules/jumpto/resources/wait.gif") ); // NOI18N
    private static Icon WARN_ICON = new ImageIcon( Utilities.loadImage("org/netbeans/modules/jumpto/resources/warning.png") ); // NOI18N
        
    private static final int BRIGHTER_COLOR_COMPONENT = 10;
    private ContentProvider contentProvider;
    private boolean containsScrollPane;
    private JLabel messageLabel;
    private SymbolDescriptor selectedSymbol;
    
    private String oldText;
    
    // Time when the serach stared (for debugging purposes)
    long time = -1;
    
    
    /** Creates new form GoToPanel */
    public GoToPanel( ContentProvider contentProvider ) throws IOException {
        this.contentProvider = contentProvider;
        initComponents();
        containsScrollPane = true;
                
        matchesList.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
        //matchesList.setPrototypeCellValue("12345678901234567890123456789012345678901234567890123456789012345678901234567890");        
        matchesList.addListSelectionListener(null);
        
        Color bgColorBrighter = new Color(
                                    Math.min(getBackground().getRed() + BRIGHTER_COLOR_COMPONENT, 255),
                                    Math.min(getBackground().getGreen() + BRIGHTER_COLOR_COMPONENT, 255),
                                    Math.min(getBackground().getBlue() + BRIGHTER_COLOR_COMPONENT, 255)
                            );
        
        messageLabel = new JLabel();
        messageLabel.setBackground(bgColorBrighter);
        messageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        messageLabel.setEnabled(true);
        messageLabel.setText(NbBundle.getMessage(GoToPanel.class, "TXT_NoSymbolsFound")); // NOI18N
        messageLabel.setFont(matchesList.getFont());
        
        // matchesList.setBackground( bgColorBrighter );
        // matchesScrollPane1.setBackground( bgColorBrighter );
        matchesList.setCellRenderer( contentProvider.getListCellRenderer( matchesList ) );
        contentProvider.setListModel( this, null );
        
        PatternListener pl = new PatternListener( this );
        nameField.getDocument().addDocumentListener(pl);
        matchesList.addListSelectionListener(pl);   
        caseSensitive.setSelected(UiOptions.GoToSymbolDialog.getCaseSensitive());
        caseSensitive.addItemListener(pl);
                
    }
    
    public boolean isCaseSensitive () {
        return this.caseSensitive.isSelected();
    }
    
    /** Sets the model from different therad
     */
    public void setModel( final ListModel model ) { 
        // XXX measure time here
        SwingUtilities.invokeLater(new Runnable() {
           public void run() {
               if (model.getSize() > 0 || getText() == null || getText().trim().length() == 0 ) {
                   matchesList.setModel(model);
                   matchesList.setSelectedIndex(0);
                   setListPanelContent(null,false);
                   if ( time != -1 ) {
                       GoToSymbolAction.LOGGER.fine("Real search time " + (System.currentTimeMillis() - time) + " ms.");
                       time = -1;
                   }
               }
               else {
                   setListPanelContent( NbBundle.getMessage(GoToPanel.class, "TXT_NoSymbolsFound") ,false ); // NOI18N
               }
           }
       });
    }
    
    /** Sets the initial text to find in case the user did not start typing yet. */
    public void setInitialText( final String text ) {
        oldText = text;
        SwingUtilities.invokeLater( new Runnable() {
            public void run() {
                String textInField = nameField.getText();
                if ( textInField == null || textInField.trim().length() == 0 ) {
                    nameField.setText(text);
                    nameField.setCaretPosition(text.length());
                    nameField.setSelectionStart(0);
                    nameField.setSelectionEnd(text.length());
                }
            }
        });
    }
    
    public void setSelectedSymbol() {
        selectedSymbol = ((SymbolDescriptor) matchesList.getSelectedValue());
    }
    
    public SymbolDescriptor getSelectedSymbol() {
        return selectedSymbol;
    }

    void setWarning(String warningMessage) {
        if (warningMessage != null) {
            jLabelWarning.setIcon(WARN_ICON);
            jLabelWarning.setBorder(BorderFactory.createEmptyBorder(3, 1, 1, 1));
        } else {
            jLabelWarning.setIcon(null);
            jLabelWarning.setBorder(null);
        }
        jLabelWarning.setText(warningMessage);
    }
            
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("deprecation")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jLabelText = new javax.swing.JLabel();
        nameField = new javax.swing.JTextField();
        jLabelList = new javax.swing.JLabel();
        listPanel = new javax.swing.JPanel();
        matchesScrollPane1 = new javax.swing.JScrollPane();
        matchesList = new javax.swing.JList();
        jLabelWarning = new javax.swing.JLabel();
        caseSensitive = new javax.swing.JCheckBox();
        jLabelLocation = new javax.swing.JLabel();
        jTextFieldLocation = new javax.swing.JTextField();

        setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 8, 8, 8));
        setFocusable(false);
        setNextFocusableComponent(nameField);
        setLayout(new java.awt.GridBagLayout());

        jLabelText.setLabelFor(nameField);
        org.openide.awt.Mnemonics.setLocalizedText(jLabelText, org.openide.util.NbBundle.getMessage(GoToPanel.class, "TXT_GoToSymbol_TypeName_Label")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 4, 0);
        add(jLabelText, gridBagConstraints);

        nameField.setFont(new java.awt.Font("Monospaced", 0, getFontSize()));
        nameField.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        nameField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nameFieldActionPerformed(evt);
            }
        });
        nameField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                nameFieldKeyPressed(evt);
            }
            public void keyReleased(java.awt.event.KeyEvent evt) {
                nameFieldKeyReleased(evt);
            }
            public void keyTyped(java.awt.event.KeyEvent evt) {
                nameFieldKeyTyped(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 8, 0);
        add(nameField, gridBagConstraints);

        jLabelList.setLabelFor(matchesScrollPane1);
        jLabelList.setText(org.openide.util.NbBundle.getMessage(GoToPanel.class, "TXT_GoToSymbol_MatchesList_Label")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 4, 0);
        add(jLabelList, gridBagConstraints);

        listPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        listPanel.setName("dataPanel"); // NOI18N
        listPanel.setLayout(new java.awt.BorderLayout());

        matchesScrollPane1.setBorder(null);
        matchesScrollPane1.setFocusable(false);

        matchesList.setFont(new java.awt.Font("Monospaced", 0, getFontSize()));
        matchesList.setVisibleRowCount(15);
        matchesList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                matchesListMouseReleased(evt);
            }
        });
        matchesScrollPane1.setViewportView(matchesList);

        listPanel.add(matchesScrollPane1, java.awt.BorderLayout.CENTER);

        jLabelWarning.setFocusable(false);
        listPanel.add(jLabelWarning, java.awt.BorderLayout.PAGE_END);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 8, 0);
        add(listPanel, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(caseSensitive, org.openide.util.NbBundle.getMessage(GoToPanel.class, "CTL_CaseSensitive")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 8, 0);
        add(caseSensitive, gridBagConstraints);
        caseSensitive.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(GoToPanel.class, "AD_CaseSensitive")); // NOI18N

        jLabelLocation.setText(org.openide.util.NbBundle.getMessage(GoToPanel.class, "LBL_GoToSymbol_LocationJLabel")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 4, 0);
        add(jLabelLocation, gridBagConstraints);

        jTextFieldLocation.setEditable(false);
        jTextFieldLocation.setFocusable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        add(jTextFieldLocation, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void matchesListMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_matchesListMouseReleased
        if ( evt.getClickCount() == 2 ) {
            nameFieldActionPerformed( null );
        }
    }//GEN-LAST:event_matchesListMouseReleased

    private void nameFieldKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_nameFieldKeyTyped
        if (boundScrollingKey(evt)) {
            delegateScrollingKey(evt);
        }
    }//GEN-LAST:event_nameFieldKeyTyped

    private void nameFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_nameFieldKeyReleased
        if (boundScrollingKey(evt)) {
            delegateScrollingKey(evt);
        }
    }//GEN-LAST:event_nameFieldKeyReleased

    private void nameFieldKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_nameFieldKeyPressed
        if (boundScrollingKey(evt)) {
            delegateScrollingKey(evt);
        }
    }//GEN-LAST:event_nameFieldKeyPressed

    private void nameFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nameFieldActionPerformed
        if (contentProvider.hasValidContent()) {
            contentProvider.closeDialog();
            setSelectedSymbol();        
        }
    }//GEN-LAST:event_nameFieldActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox caseSensitive;
    private javax.swing.JLabel jLabelList;
    private javax.swing.JLabel jLabelLocation;
    private javax.swing.JLabel jLabelText;
    private javax.swing.JLabel jLabelWarning;
    private javax.swing.JTextField jTextFieldLocation;
    private javax.swing.JPanel listPanel;
    private javax.swing.JList matchesList;
    private javax.swing.JScrollPane matchesScrollPane1;
    private javax.swing.JTextField nameField;
    // End of variables declaration//GEN-END:variables
        
    
    private String getText() {
        try {
            String text = nameField.getDocument().getText(0, nameField.getDocument().getLength());
            return text;
        }
        catch( BadLocationException ex ) {
            return null;
        }
    }
    
    private int getFontSize () {
        return this.jLabelList.getFont().getSize();
    }        
    
    void setListPanelContent( String message ,boolean waitIcon ) {
        
        if ( message == null && !containsScrollPane ) {
           listPanel.remove( messageLabel );
           listPanel.add( matchesScrollPane1 );
           containsScrollPane = true;
           revalidate();
           repaint();
        }        
        else if ( message != null ) { 
           jTextFieldLocation.setText(""); 
           messageLabel.setText(message);
           messageLabel.setIcon( waitIcon ? WAIT_ICON : null);
           if ( containsScrollPane ) {
               listPanel.remove( matchesScrollPane1 );
               listPanel.add( messageLabel );
               containsScrollPane = false;
           }
           revalidate();
           repaint();
       }                
    }
    
    private String listActionFor(KeyEvent ev) {
        InputMap map = matchesList.getInputMap();
        Object o = map.get(KeyStroke.getKeyStrokeForEvent(ev));
        if (o instanceof String) {
            return (String)o;
        } else {
            return null;
        }
    }

    private boolean boundScrollingKey(KeyEvent ev) {
        String action = listActionFor(ev);
        // See BasicListUI, MetalLookAndFeel:
        return "selectPreviousRow".equals(action) || // NOI18N
        "selectNextRow".equals(action) || // NOI18N
        // "selectFirstRow".equals(action) || // NOI18N
        // "selectLastRow".equals(action) || // NOI18N
        "scrollUp".equals(action) || // NOI18N
        "scrollDown".equals(action); // NOI18N
    }

    private void delegateScrollingKey(KeyEvent ev) {
        String action = listActionFor(ev);
        
        // Wrap around
        if ( "selectNextRow".equals(action) && 
            matchesList.getSelectedIndex() == matchesList.getModel().getSize() -1 ) {
            matchesList.setSelectedIndex(0);
            matchesList.ensureIndexIsVisible(0);
            return;
        }
        else if ( "selectPreviousRow".equals(action) &&
                  matchesList.getSelectedIndex() == 0 ) {
            int last = matchesList.getModel().getSize() - 1;
            matchesList.setSelectedIndex(last);
            matchesList.ensureIndexIsVisible(last);
            return;
        }
        
        // Plain delegation        
        Action a = matchesList.getActionMap().get(action);
        if (a != null) {
            a.actionPerformed(new ActionEvent(matchesList, 0, action));
        }
    }
    
    private static class PatternListener implements DocumentListener, ListSelectionListener, ItemListener {
               
        private final GoToPanel dialog;
        
        
        PatternListener( GoToPanel dialog  ) {
            this.dialog = dialog;
        }
        
        PatternListener( DocumentEvent e, GoToPanel dialog  ) {
            this.dialog = dialog;
        }
        
        // DocumentListener ----------------------------------------------------
        
        public void changedUpdate( DocumentEvent e ) {            
            update();
        }

        public void removeUpdate( DocumentEvent e ) {
            update();
        }

        public void insertUpdate( DocumentEvent e ) {
            update();
        }
        
        public void itemStateChanged(ItemEvent e) {
            UiOptions.GoToSymbolDialog.setCaseSensitive(dialog.isCaseSensitive());
            update();
        }
                
        // ListSelectionListener -----------------------------------------------
        
        public void valueChanged(ListSelectionEvent ev) {
            // got "Not computed yet" text sometimes
            Object obj = dialog.matchesList.getSelectedValue();
            
            if (obj instanceof SymbolDescriptor) {
                SymbolDescriptor selectedValue = ((SymbolDescriptor) obj);
                if ( selectedValue != null ) {
                    String fileName = "";
                    FileObject fo = selectedValue.getFileObject();
                    if (fo != null) {
                        fileName = FileUtil.getFileDisplayName(fo);
                    }
                    dialog.jTextFieldLocation.setText(fileName);
                }
                else {
                    dialog.jTextFieldLocation.setText("");
                }
            } else {
                dialog.jTextFieldLocation.setText("");
         }
        }
        
        private void update() {
            dialog.time = System.currentTimeMillis();
            String text = dialog.getText();
            if ( dialog.oldText == null || dialog.oldText.trim().length() == 0 || !text.startsWith(dialog.oldText) ) {
                dialog.setListPanelContent(NbBundle.getMessage(GoToPanel.class, "TXT_Searching"),true); // NOI18N
            }
            dialog.oldText = text;
            dialog.contentProvider.setListModel(dialog,text);            
        }                                         
    }
             
    
    public static interface ContentProvider {
        
        public ListCellRenderer getListCellRenderer( JList list );
        
        public void setListModel( GoToPanel panel, String text  );
        
        public void closeDialog();
        
        public boolean hasValidContent ();
                
    }
    
}
