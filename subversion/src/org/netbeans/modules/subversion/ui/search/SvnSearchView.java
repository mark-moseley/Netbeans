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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.subversion.ui.search;

import javax.swing.event.ListSelectionListener;
import org.openide.ErrorManager;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.settings.FontColorSettings;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.event.*;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.text.DateFormat;
import org.openide.util.NbBundle;
import org.tigris.subversion.svnclientadapter.ISVNLogMessage;
import org.tigris.subversion.svnclientadapter.SVNRevision;

/**
 * Shows Search results in a JList.
 * 
 * @author Tomas Stupka
 */
class SvnSearchView implements ComponentListener {

    private JList resultsList;
    private ISVNLogMessage[] lm;
    private AttributeSet searchHiliteAttrs;
    private JScrollPane pane;
                            
                            
    public SvnSearchView() {
        FontColorSettings fcs = (FontColorSettings) MimeLookup.getMimeLookup("text/x-java").lookup(FontColorSettings.class); // NOI18N
        searchHiliteAttrs = fcs.getFontColors("highlight-search"); // NOI18N
        
        resultsList = new JList(new SvnSearchListModel());
        resultsList.setFixedCellHeight(-1);
        resultsList.setCellRenderer(new SvnSearchListCellRenderer());
        resultsList.getAccessibleContext().setAccessibleName(NbBundle.getMessage(SvnSearchView.class, "ACSN_SummaryView_ListName"));
        resultsList.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SvnSearchView.class, "ACSD_SummaryView_ListDesc"));
        resultsList.addComponentListener(this);        
        pane = new JScrollPane(resultsList, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    }

    JComponent getComponent() {
        return pane;
    }
    
    public void componentResized(ComponentEvent e) {
        int [] selection = resultsList.getSelectedIndices();
        resultsList.setModel(new SvnSearchListModel());
        resultsList.setSelectedIndices(selection);
    }

    public void componentHidden(ComponentEvent e) {
        // not interested
    }

    public void componentMoved(ComponentEvent e) {
        // not interested
    }

    public void componentShown(ComponentEvent e) {
        // not interested
    }
    
    public void setResults(ISVNLogMessage[] lm) {
        this.lm = lm;
        resultsList.setModel(new SvnSearchListModel());
    }

    SVNRevision getSelectedValue() {
        Object selection = resultsList.getSelectedValue();
        if(selection == null) {
            return null;
        }
        if(!(selection instanceof ISVNLogMessage)) {
            return null;
        }
        ISVNLogMessage message = (ISVNLogMessage) selection;
        return message.getRevision();
    }

    void addListSelectionListener(ListSelectionListener listener) {
        resultsList.addListSelectionListener(listener);
    }

    void removeListSelectionListener(ListSelectionListener listener) {
        resultsList.removeListSelectionListener(listener);
    }

    private class SvnSearchListModel extends AbstractListModel {

        public int getSize() {
            if(lm == null) {
                return 0;
            }
            return lm.length; 
        }

        public Object getElementAt(int index) {
            return lm[index];
        }
    }
    
    private class SvnSearchListCellRenderer extends JPanel implements ListCellRenderer {

        private static final String FIELDS_SEPARATOR = "        "; // NOI18N
        private static final double DARKEN_FACTOR = 0.95;

        private Style selectedStyle;
        private Style normalStyle;
        private Style boldStyle;
        private Style hiliteStyle;
        
        private JTextPane textPane = new JTextPane();
        
        private DateFormat defaultFormat;        

        public SvnSearchListCellRenderer() {
            selectedStyle = textPane.addStyle("selected", null); // NOI18N
            StyleConstants.setForeground(selectedStyle, UIManager.getColor("List.selectionForeground")); // NOI18N
            normalStyle = textPane.addStyle("normal", null); // NOI18N
            StyleConstants.setForeground(normalStyle, UIManager.getColor("List.foreground")); // NOI18N
            boldStyle = textPane.addStyle("filename", normalStyle); // NOI18N
            StyleConstants.setBold(boldStyle, true);
            defaultFormat = DateFormat.getDateTimeInstance();

            hiliteStyle = textPane.addStyle("hilite", normalStyle); // NOI18N
            StyleConstants.setBackground(hiliteStyle, (Color) searchHiliteAttrs.getAttribute(StyleConstants.Background));
            StyleConstants.setForeground(hiliteStyle, (Color) searchHiliteAttrs.getAttribute(StyleConstants.Foreground));
            
            setLayout(new BorderLayout());
            add(textPane);
            textPane.setBorder(null);
        }
        
        public Color darker(Color c) {
            return new Color(Math.max((int)(c.getRed() * DARKEN_FACTOR), 0), 
                 Math.max((int)(c.getGreen() * DARKEN_FACTOR), 0),
                 Math.max((int)(c.getBlue() * DARKEN_FACTOR), 0));
        }
        
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            if(value instanceof ISVNLogMessage) {
                ISVNLogMessage message = (ISVNLogMessage) value;
                StyledDocument sd = textPane.getStyledDocument();

                Style style;
                if (isSelected) {
                    textPane.setBackground(UIManager.getColor("List.selectionBackground")); // NOI18N
                    style = selectedStyle;
                } else {
                    Color c = UIManager.getColor("List.background"); // NOI18N
                    textPane.setBackground((index & 1) == 0 ? c : darker(c));
                    style = normalStyle;
                }

                try {
                    sd.remove(0, sd.getLength());
                    sd.setCharacterAttributes(0, Integer.MAX_VALUE, style, true);
                    sd.insertString(0, message.getRevision().toString(), null);
                    sd.setCharacterAttributes(0, sd.getLength(), boldStyle, false);
                    sd.insertString(sd.getLength(), FIELDS_SEPARATOR + message.getAuthor(), null);
                    sd.insertString(sd.getLength(), FIELDS_SEPARATOR +  defaultFormat.format(message.getDate()), null);
                    sd.insertString(sd.getLength(), "\n" + message.getMessage(), null); // NOI18N
                } catch (BadLocationException e) {
                    ErrorManager.getDefault().notify(e);
                }
                
                if (message.getMessage() != null) {
                    int width = resultsList.getWidth();
                    if (width > 0) {
                        FontMetrics fm = list.getFontMetrics(list.getFont());
                        Rectangle2D rect = fm.getStringBounds(message.getMessage(), textPane.getGraphics());
                        int nlc, i;
                        for (nlc = -1, i = 0; i != -1 ; i = message.getMessage().indexOf('\n', i + 1), nlc++);
                        //if (indentation == 0) nlc++;
                        int lines = (int) (rect.getWidth() / (width - 80) + 1);
                        int ph = fm.getHeight() * (lines + nlc + 1) + 0;
                        textPane.setPreferredSize(new Dimension(width - 50, ph));
                    }
                }
                
            }
            return this;
        }
    }
}
