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
package org.netbeans.modules.refactoring.spi.impl;

import java.awt.*;
import javax.swing.*;
import javax.swing.tree.*;
import org.openide.awt.HtmlRenderer;
import org.openide.util.NbBundle;

/**
 * @author Pavel Flaska
 */
public class CheckRenderer extends JPanel implements TreeCellRenderer {

    protected JCheckBox check;
    protected HtmlRenderer.Renderer renderer = HtmlRenderer.createRenderer();
    private static Dimension checkDim;

    static Rectangle checkBounds;

    static {
        Dimension old = new JCheckBox().getPreferredSize();
        checkDim = new Dimension(old.width, old.height - 5);
    }
    
    public CheckRenderer(boolean isQuery) {
        setLayout(null);
        if (isQuery) {
            check = null;
        } else {
            add(check = new JCheckBox());
            Color c = UIManager.getColor("Tree.textBackground"); //NOI18N
            if (c == null) {
                //May be null on GTK L&F
                c = Color.WHITE;
            }
            check.setBackground(c); // NOI18N
            Dimension dim = check.getPreferredSize();
            check.setPreferredSize(checkDim);
        }
    }
    
    /** The component returned by HtmlRenderer.Renderer.getTreeCellRendererComponent() */
    private Component stringDisplayer = new JLabel(" "); //NOI18N
    
    public Component getTreeCellRendererComponent(JTree tree, Object value,
    boolean isSelected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        CheckNode node = (CheckNode) value;
        stringDisplayer = renderer.getTreeCellRendererComponent(tree, 
            getNodeText(node), isSelected, expanded, leaf, row, hasFocus);

        renderer.setIcon (node.getIcon());
        stringDisplayer.setEnabled(!node.isDisabled());
        String toolTip = node.getToolTip();
        if (toolTip!=null)
            setToolTipText("<html>"+node.getToolTip()+"</html>"); // NOI18N
        
        //HtmlRenderer does not tolerate null colors - real ones are needed to
        //ensure fg/bg always diverge enough to be readable
        if (stringDisplayer.getBackground() == null) {
            stringDisplayer.setBackground (tree.getBackground());
        }
        if (stringDisplayer.getForeground() == null) {
            stringDisplayer.setForeground (tree.getForeground());
        }

        if (check != null) {
            check.setSelected(node.isSelected());
            check.setEnabled(!node.isDisabled());
        }
        return this;
    }
    
    public void paintComponent (Graphics g) {
        Dimension d_check = check == null ? new Dimension(0, 0) : check.getSize();
        Dimension d_label = stringDisplayer == null ? new Dimension(0,0) : 
            stringDisplayer.getPreferredSize();
            
        int y_check = 0;
        int y_label = 0;
        
        if (d_check.height >= d_label.height) {
            y_label = (d_check.height - d_label.height) / 2;
        }
        if (check != null) {
            check.setBounds (0, 0, d_check.width, d_check.height);
            check.paint(g);
        }
        if (stringDisplayer != null) {
            int y = y_label-2;
            stringDisplayer.setBounds (d_check.width, y, 
                d_label.width, getHeight()-1);
            g.translate (d_check.width, y_label);
            stringDisplayer.paint(g);
            g.translate (-d_check.width, -y_label);
        }
    }
    
    private String getNodeText(CheckNode node) {
        String nodeLabel = node.getLabel() == null ? NbBundle.getMessage(CheckRenderer.class,"LBL_NotAvailable") : node.getLabel();
        nodeLabel = "<html>" + nodeLabel; // NOI18N
        if (node.needsRefresh()) {
            nodeLabel += " - " + NbBundle.getMessage(RefactoringPanel.class, "LBL_NeedsRefresh");
        }
        nodeLabel += "</html>"; // NOI18N
        int i = nodeLabel.indexOf("<br>"); // NOI18N
        if (i!=-1) {
            return nodeLabel.substring(0,i) +"</html>"; // NOI18N
        } else {
            return nodeLabel;
        }
    }
    
    public Dimension getPreferredSize() {
        if (stringDisplayer != null) {
            stringDisplayer.setFont(getFont());
        }
        Dimension d_check = check == null ? new Dimension(0, checkDim.height) : 
            check.getPreferredSize();
            
        Dimension d_label = stringDisplayer != null ? 
            stringDisplayer.getPreferredSize() : new Dimension(0,0);
            
        return new Dimension(d_check.width  + d_label.width, (d_check.height < d_label.height ? d_label.height : d_check.height));
    }
    
    public void doLayout() {
        Dimension d_check = check == null ? new Dimension(0, 0) : check.getPreferredSize();
        Dimension d_label = stringDisplayer == null ? new Dimension (0,0) : stringDisplayer.getPreferredSize();
        int y_check = 0;
        int y_label = 0;
        
        if (d_check.height < d_label.height)
            y_check = (d_label.height - d_check.height) / 2;
        else
            y_label = (d_check.height - d_label.height) / 2;

        if (check != null) {
            check.setLocation(0, y_check);
            check.setBounds(0, y_check, d_check.width, d_check.height);
            if (checkBounds == null)
                checkBounds = check.getBounds();
        }
    }

    public static Rectangle getCheckBoxRectangle() {
        return (Rectangle) checkBounds.clone();
    }
}
