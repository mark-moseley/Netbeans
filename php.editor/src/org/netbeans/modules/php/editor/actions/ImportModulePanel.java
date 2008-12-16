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
package org.netbeans.modules.php.editor.actions;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.KeyStroke;
import javax.swing.text.BadLocationException;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.gsf.api.CompilationInfo;
import org.netbeans.modules.gsf.api.EditList;
import org.netbeans.modules.gsf.api.ElementKind;
import org.netbeans.modules.php.editor.lexer.LexUtilities;
import org.netbeans.modules.php.editor.lexer.PHPTokenId;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 * This file is originally from Retouche, the Java Support
 * infrastructure in NetBeans. I have modified the file as little
 * as possible to make merging Retouche fixes back as simple as
 * possible.
 *
 * (This used to be ImportClassPanel in org.netbeans.modules.java.editor.imports)
 *
 * @author Jan Lahoda, Tor Norbye
 */
public class ImportModulePanel extends javax.swing.JPanel {

    private final String ident;
    private CompilationInfo info;
    private DefaultListModel model;
    private final int position;

    /** Creates new form ImportClassPanel */
    @SuppressWarnings("deprecation")
    public ImportModulePanel(String ident, List</*TypeElement*/String> priviledged,
            List</*TypeElement*/String> denied, Font font, CompilationInfo info, int position) {
        this.ident = ident;
        // System.err.println("priviledged=" + priviledged);
        // System.err.println("denied=" + denied);
        this.info = info;
        this.position = position;
        createModel(priviledged, denied);

        initComponents();
        setBackground(jList1.getBackground());

        if (model.size() > 0) {
            jList1.setModel(model);
            setFocusable(false);
            setNextFocusableComponent(jList1);
            jScrollPane1.setBackground(jList1.getBackground());
            setBackground(jList1.getBackground());
            if (font != null) {
                jList1.setFont(font);
            }
            int modelSize = jList1.getModel().getSize();
            if (modelSize > 0) {
                jList1.setSelectedIndex(0);
            }
            jList1.setVisibleRowCount(modelSize > 8 ? 8 : modelSize);
            jList1.setCellRenderer(new Renderer(jList1));
            jList1.grabFocus();
        } else {
            remove(jScrollPane1);
            JLabel nothingFoundJL = new JLabel(NbBundle.getMessage(ImportModulePanel.class, "NoModsFound"));
            if (font != null) {
                nothingFoundJL.setFont(font);
            }
            nothingFoundJL.setBorder(javax.swing.BorderFactory.createEmptyBorder(2, 4, 4, 4));
            nothingFoundJL.setEnabled(false);
            nothingFoundJL.setBackground(jList1.getBackground());
            //nothingFoundJL.setOpaque(true);
            add(nothingFoundJL);
        }

        setA11Y();
        updatePreview(null);
    }

    private void setA11Y() {
        this.getAccessibleContext().setAccessibleName(NbBundle.getMessage(ImportModulePanel.class, "ImportClassPanel_ACN"));
        this.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(ImportModulePanel.class, "ImportClassPanel_ACSD"));
        jList1.getAccessibleContext().setAccessibleName(NbBundle.getMessage(ImportModulePanel.class, "ImportClassPanel_JList1_ACN"));
        jList1.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(ImportModulePanel.class, "ImportClassPanel_JList1_ACSD"));
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        jList1 = new javax.swing.JList();
        jLabel1 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        ctrlLabel = new javax.swing.JLabel();
        importPreviewLabel = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();

        setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(64, 64, 64)));
        setLayout(new java.awt.BorderLayout());

        jScrollPane1.setBorder(javax.swing.BorderFactory.createEmptyBorder(2, 4, 4, 4));

        jList1.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jList1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                listMouseReleased(evt);
            }
        });
        jList1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                updatePreview(evt);
            }
            public void keyReleased(java.awt.event.KeyEvent evt) {
                listKeyReleased(evt);
                updatePreview(evt);
            }
        });
        jScrollPane1.setViewportView(jList1);

        add(jScrollPane1, java.awt.BorderLayout.CENTER);

        jLabel1.setLabelFor(jList1);
        jLabel1.setText(org.openide.util.NbBundle.getMessage(ImportModulePanel.class, "ImportChoices")); // NOI18N
        jLabel1.setBorder(javax.swing.BorderFactory.createEmptyBorder(4, 4, 4, 4));
        jLabel1.setOpaque(true);
        add(jLabel1, java.awt.BorderLayout.PAGE_START);

        jPanel1.setBorder(javax.swing.BorderFactory.createEmptyBorder(4, 4, 4, 4));

        ctrlLabel.setText(org.openide.util.NbBundle.getMessage(ImportModulePanel.class, "LBL_PackageImport")); // NOI18N

        importPreviewLabel.setText(" ");

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(ctrlLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 390, Short.MAX_VALUE)
            .add(importPreviewLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 390, Short.MAX_VALUE)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jSeparator1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 390, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .add(ctrlLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jSeparator1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 10, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(importPreviewLabel)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        add(jPanel1, java.awt.BorderLayout.PAGE_END);
    }// </editor-fold>//GEN-END:initComponents

    private void listMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_listMouseReleased
        boolean packageImport = (evt.getModifiers() & InputEvent.ALT_MASK) > 0;
        boolean useFqn = (evt.getModifiers() & InputEvent.SHIFT_MASK) > 0;
        importModule(getSelected(), packageImport, useFqn);
    }//GEN-LAST:event_listMouseReleased

    private void listKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_listKeyReleased
        KeyStroke ks = KeyStroke.getKeyStrokeForEvent(evt);
        if (ks.getKeyCode() == KeyEvent.VK_ENTER ||
                ks.getKeyCode() == KeyEvent.VK_SPACE) {
            boolean packageImport = (evt.getModifiers() & InputEvent.ALT_MASK) > 0;
            boolean useFqn = (evt.getModifiers() & InputEvent.SHIFT_MASK) > 0;
            importModule(getSelected(), packageImport, useFqn);
        }
    }//GEN-LAST:event_listKeyReleased

    private void updatePreview(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_updatePreview
        Object selected = jList1.getSelectedValue();
        if (selected != null) {
            String preview = "<html><code><b>" + ((TypeDescription)selected).qualifiedName + "</b></code></html>"; // NOI18N
            importPreviewLabel.setText(preview);
        }
    }//GEN-LAST:event_updatePreview

    // Variables declaration - do not modify//GEN-BEGIN:variables
    public javax.swing.JLabel ctrlLabel;
    public javax.swing.JLabel importPreviewLabel;
    public javax.swing.JLabel jLabel1;
    public javax.swing.JList jList1;
    public javax.swing.JPanel jPanel1;
    public javax.swing.JScrollPane jScrollPane1;
    public javax.swing.JSeparator jSeparator1;
    // End of variables declaration//GEN-END:variables


    public String getSelected() {
        TypeDescription typeDescription = ((TypeDescription) jList1.getSelectedValue());
        return typeDescription == null ? null : typeDescription.qualifiedName;
    }

    private void createModel(List<String> priviledged, List<String> denied) {

        List<TypeDescription> l = new ArrayList<TypeDescription>(priviledged.size());
        for (String typeElement : priviledged) {
            l.add(new TypeDescription(typeElement, false));
        }
        for (String typeElement : denied) {
            l.add(new TypeDescription(typeElement, true));
        }

        Collections.sort(l);

        model = new DefaultListModel();
        for (TypeDescription td : l) {
            model.addElement(td);
        }
    }

    /*
     *
     * TODO: @param packageImport, useFqn - both unused (maybe could be misused to decide whether
     * generate just one line before or at the top of the file after PHP_OPENTAG
     */
    private void importModule(String importCode, boolean importInPreviousLine, boolean useFqn) {
        PopupUtil.hidePopup();

        int basePosition = importInPreviousLine ? position : -1;
        BaseDocument document = (BaseDocument) info.getDocument();
        if (!importInPreviousLine) {
            TokenSequence<PHPTokenId> ts = LexUtilities.getPHPTokenSequence(document, position);
            ts.move(position);
            while (ts.movePrevious()) {
                Token<PHPTokenId> token = ts.token();
                if (token.id().equals(PHPTokenId.PHP_OPENTAG)) {
                    basePosition = ts.offset();
                }
            }
        }
        if (basePosition != -1) {
            EditList edits = new EditList(document);
            try {
                int rowEnd = importInPreviousLine ? Utilities.getRowStart(document, position) :
                    Utilities.getRowEnd(document, basePosition);
                if (importInPreviousLine) {
                    importCode = importCode +"\n";//NOI18N
                } else {
                    importCode = "\n" + importCode;//NOI18N
                }
                edits.replace(rowEnd, 0, importCode, true, 0);
                edits.setFormatAll(true);
                edits.apply();

            } catch (BadLocationException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    private static class Renderer extends DefaultListCellRenderer {

        private static int DARKER_COLOR_COMPONENT = 5;
        private static int LIGHTER_COLOR_COMPONENT = DARKER_COLOR_COMPONENT;
        private Color denidedColor = new Color(0x80, 0x80, 0x80);
        private Color fgColor;
        private Color bgColor;
        private Color bgColorDarker;
        private Color bgSelectionColor;
        private Color fgSelectionColor;

        public Renderer(JList list) {
            setFont(list.getFont());
            fgColor = list.getForeground();
            bgColor = list.getBackground();
            bgColorDarker = new Color(
                    Math.abs(bgColor.getRed() - DARKER_COLOR_COMPONENT),
                    Math.abs(bgColor.getGreen() - DARKER_COLOR_COMPONENT),
                    Math.abs(bgColor.getBlue() - DARKER_COLOR_COMPONENT));
            bgSelectionColor = list.getSelectionBackground();
            fgSelectionColor = list.getSelectionForeground();
        }

        @Override
        public Component getListCellRendererComponent(JList list,
                Object value,
                int index,
                boolean isSelected,
                boolean hasFocus) {

            if (isSelected) {
                setForeground(fgSelectionColor);
                setBackground(bgSelectionColor);
            } else {
                setForeground(fgColor);
                setBackground(index % 2 == 0 ? bgColor : bgColorDarker);
            }

            if (value instanceof TypeDescription) {
                TypeDescription td = (TypeDescription) value;
                // setIcon(td.getIcon());
                setText(td.qualifiedName);
                if (td.isDenied) {
                    setForeground(denidedColor);
                }
                // TODO - depend on gsf directly and get icons from there!
                //setIcon( ElementIcons.getElementIcon( td.kind, null ) );
                setIcon(new ImageIcon(ImageUtilities.loadImage("org/netbeans/modules/python/editor/imports/module.png"))); // NOI18N
            } else {
                setText(value.toString());
            }

            return this;
        }
    }

    private static class TypeDescription implements Comparable<TypeDescription> {

        private boolean isDenied;
        private final ElementKind kind;
        private final String qualifiedName;

        public TypeDescription(String typeElement, boolean isDenied) {
            this.isDenied = isDenied;
            //this.kind = typeElement.getKind();
            //this.qualifiedName = typeElement.getQualifiedName().toString();
            this.kind = ElementKind.MODULE;
            this.qualifiedName = typeElement;
        }

        public int compareTo(TypeDescription o) {

            if (isDenied && !o.isDenied) {
                return 1;
            } else if (!isDenied && o.isDenied) {
                return -1;
            } else {
                return qualifiedName.compareTo(o.qualifiedName);
            }
        }
    }
}
