/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

/*
 * ListRendererPanel.java
 *
 * Created on Jan 20, 2009, 11:10:53 AM
 */

package org.netbeans.modules.kenai.ui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.image.BufferedImage;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JViewport;
import org.openide.util.NbBundle;

/**
 * TODO: use better layout mgr
 * 
 * @author Milan
 */
public class ListRendererPanel extends javax.swing.JPanel {

    private KenaiSearchPanel.KenaiProjectSearchInfo kenaiProject;

    /** Creates new form ListRendererPanel */
    public ListRendererPanel(JList jlist, KenaiSearchPanel.KenaiProjectSearchInfo kp, int index, 
            boolean isSelected, boolean hasFocus, KenaiSearchPanel.PanelType pType) {

        initComponents();
        kenaiProject = kp;

        projectNameLabel.setText("<html><b>" + kenaiProject.kenaiProject.getDisplayName() + " (" + kenaiProject.kenaiProject.getName() + ")</b></html>");
        projectDescLabel.setText(highlighthPattern(kenaiProject.kenaiProject.getDescription(), kenaiProject.searchPattern));

        if (isSelected) {
            setBackground(jlist.getSelectionBackground());
            innerDescPanel.setBackground(jlist.getSelectionBackground());
        } else {
            setBackground(jlist.getBackground());
            innerDescPanel.setBackground(jlist.getBackground());
        }

        Graphics2D g2d = (Graphics2D) new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB).getGraphics();
        FontMetrics fm = g2d.getFontMetrics(new JLabel().getFont());

        //descPane.setText(getSubstrWithElipsis(kenaiProject.kenaiProject.getDescription(), fm, getWidth(), 5.0f, g2d));
        //descArea.setText("<html>" + kenaiProject.kenaiProject.getDescription() + "</html>");

    }

    @Override
    public Dimension getPreferredSize() {
        Container parent = getParent();
        int width = 0;
        while (parent != null) {
            if (parent instanceof JViewport) {
                width = parent.getWidth();
                break;
            }
            parent = parent.getParent();
        }
        return new Dimension(width, super.getPreferredSize().height + 10);
        //return new Dimension(width - (int) (0.65 * width), super.getPreferredSize().height + 15);
    }

    private String highlighthPattern(String txt, String ptrn) {
        Pattern pattern = Pattern.compile(ptrn);
        Matcher matcher = pattern.matcher(txt);
        return "<html>" + matcher.replaceAll(makeBold(ptrn)) + "</html>";
    }

    private String makeBold(String txt) {
        return "<b>" + txt + "</b>";
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        GridBagConstraints gridBagConstraints;

        innerDescPanel = new JPanel();
        projectDescLabel = new JLabel();
        projectNameLabel = new JLabel();

        setLayout(new GridBagLayout());

        innerDescPanel.setLayout(new BorderLayout());

        projectDescLabel.setText(NbBundle.getMessage(ListRendererPanel.class, "ListRendererPanel.projectDescLabel.text")); // NOI18N
        innerDescPanel.add(projectDescLabel, BorderLayout.CENTER);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new Insets(4, 4, 3, 0);
        add(innerDescPanel, gridBagConstraints);

        projectNameLabel.setText(NbBundle.getMessage(ListRendererPanel.class, "ListRendererPanel.projectNameLabel.text")); // NOI18N
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new Insets(4, 4, 0, 0);
        add(projectNameLabel, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JPanel innerDescPanel;
    private JLabel projectDescLabel;
    private JLabel projectNameLabel;
    // End of variables declaration//GEN-END:variables

    private String getSubstrWithElipsis(String text, FontMetrics fm, int reqWidth, float charWidth, Graphics2D context) {

        int textCharLen = text.length();
        int mIndex = textCharLen;

        int textPixWidth = (int) fm.getStringBounds(text, 0, mIndex, context).getWidth();

        // text is already smaller than required width
        if (reqWidth > textPixWidth) {
            return text;
        }

        // find longest possible substring that would fit into the required
        // width by binary division over text length
        while (Math.abs(reqWidth - textPixWidth) > charWidth) {

            textCharLen = textCharLen == 1 ? 1 : textCharLen / 2;

            if (reqWidth - textPixWidth < 0) {
                mIndex = mIndex - textCharLen;
            } else {
                mIndex = mIndex + textCharLen;
            }

            textPixWidth = (int) fm.getStringBounds(text, 0, mIndex, context).getWidth();

        }

        return text.substring(0, mIndex) + "...";

    }

}
