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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.autoupdate.ui;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.JViewport;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkEvent.EventType;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import org.openide.util.NbBundle;

/**
 *
 * @author  Jiri Rechtacek, Radek Matous
 */
public class DetailsPanel extends JTextPane  {
    private JScrollPane scrollPane;
    private HeaderPanel header;
    private JLabel title;
    private JButton button;
    private JButton button2;
    private JPanel rightCornerHeader;
    
    public DetailsPanel() {
        initComponents2();
        HTMLEditorKit htmlkit = new HTMLEditorKitEx();
        // override the Swing default CSS to make the HTMLEditorKit use the
        // same font as the rest of the UI.
        
        // XXX the style sheet is shared by all HTMLEditorKits.  We must
        // detect if it has been tweaked by ourselves or someone else
        // (code completion javadoc popup for example) and avoid doing the
        // same thing again
        
        StyleSheet css = htmlkit.getStyleSheet();
        
        if (css.getStyleSheets() == null) {
            StyleSheet css2 = new StyleSheet();
            Font f = new JList().getFont();
            int size = f.getSize();
            css2.addRule(new StringBuffer("body { font-size: ").append(size) // NOI18N
                    .append("; font-family: ").append(f.getName()).append("; }").toString()); // NOI18N
            css2.addStyleSheet(css);
            htmlkit.setStyleSheet(css2);
        }
        
        setEditorKit(htmlkit);
        addHyperlinkListener(new HyperlinkListener() {
            public void hyperlinkUpdate(HyperlinkEvent hlevt) {
                if (EventType.ACTIVATED == hlevt.getEventType()) {
                    assert hlevt.getURL() != null;
                    Utilities.showURL(hlevt.getURL());
                }
            }
        });
        setEditable(false);
        setPreferredSize(new Dimension(300, 80));
        getAccessibleContext ().setAccessibleName (
                NbBundle.getMessage (DetailsPanel.class, "ACN_DetailsPanel")); // NOI18N
    }

    @Override
    public void addNotify() {
        super.addNotify();
        getScrollPane();
    }
    
    
    
    JScrollPane getScrollPane() {
        if (scrollPane == null) {
            Container p = getParent();
            if (p instanceof JViewport) {
                Container gp = p.getParent();
                if (gp instanceof JScrollPane) {
                    scrollPane = (JScrollPane)gp;
                }
            }            
        }
        return scrollPane;
    }
    
    public void setTitle(String value) {
        getScrollPane().setColumnHeaderView(value != null ? header : null);
        getScrollPane().setCorner(JScrollPane.UPPER_RIGHT_CORNER, value != null ? rightCornerHeader : null);
        if (value != null) {                            
            title.setText("<html><h3>"+value+"</h3></html>"); // NOI18N
        }
    }
    public void setActionListener(Action action) {
        button.setVisible(action != null);
        button.setEnabled(action != null);
        if (action != null) {
            button.setAction(action);
            button.getAccessibleContext ().setAccessibleDescription (
                    NbBundle.getMessage (DetailsPanel.class, "ACN_DetailsPanel_Button", button.getName ())); // NOI18N
        }
    }
    
    public void setActionListener2(Action action) {
        button2.setVisible(action != null);
        button2.setEnabled(action != null);
        if (action != null) {
            button2.setAction(action);
            button2.getAccessibleContext ().setAccessibleDescription (
                    NbBundle.getMessage (DetailsPanel.class, "ACN_DetailsPanel_Button", button2.getName ())); // NOI18N
        }
    }
    

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        title.setEnabled(enabled);
        button.setEnabled(enabled);
    }
 
    
    javax.swing.JEditorPane getDetails() {
        return this;
    }
    
    HeaderPanel getHeader() {
        return header;
    }
    
    private void initComponents2() {
        header = new HeaderPanel();
        title = header.getTitle();
        button = header.getButton();
        button2 = header.getButton2();
        Border outsideBorder = BorderFactory.createMatteBorder(0, 0, 1, 0, Color.gray);        
        Border insideBorder = BorderFactory.createEmptyBorder(3, 3, 3, 3);
        CompoundBorder compoundBorder = BorderFactory.createCompoundBorder(outsideBorder, insideBorder);
        header.setBorder(compoundBorder);
        button.setVisible(false);
        button2.setVisible(false);        
        rightCornerHeader  = new JPanel();        
        rightCornerHeader.setBorder(compoundBorder);
        
        Color headerBgColor = UnitTable.getDarkerColor(getBackground());
        header.setBackground(headerBgColor);
        if (button != null) {
            button.setOpaque(false);
        }
        if (button2 != null) {
            button2.setOpaque(false);
        }        
        rightCornerHeader.setBackground(headerBgColor);
        setBorder(BorderFactory.createEmptyBorder(3, 3, 0, 0));
    }
}