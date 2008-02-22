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
package org.netbeans.modules.bpel.mapper.palette;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Graphics;
import java.awt.event.ActionEvent;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseMotionAdapter;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;

import org.netbeans.modules.bpel.mapper.model.customitems.BpelXPathCustomFunction;
import org.netbeans.modules.bpel.mapper.model.customitems.WrapServiceRefHandler;
import org.netbeans.modules.soa.mappercore.Mapper;
import org.netbeans.modules.xml.xpath.ext.CoreFunctionType;
import org.netbeans.modules.xml.xpath.ext.CoreOperationType;
import org.netbeans.modules.bpel.model.api.support.BpelXPathExtFunctionMetadata;
import org.openide.util.NbBundle;
import static org.netbeans.modules.soa.ui.util.UI.*;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2007.10.29
 */
public final class Palette {

    public Palette(Mapper mapper) {
        myMapper = mapper;
        myIsCollapsed = true;
    }

    public JPanel getPanel() {
        final JPanel panel = new JPanel(new GridBagLayout());
        final GridBagConstraints c = new GridBagConstraints();
        c.weighty = 0.0;

        c.weightx = 1.0;
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.BOTH;
        panel.add(createMenuBar(), c);

        c.weightx = 0.0;
        c.anchor = GridBagConstraints.EAST;
        c.fill = GridBagConstraints.NONE;
        panel.add(createCollapseExpandAllButton(), c);

        panel.setBorder(new Border());
        panel.setOpaque(true);
        panel.addMouseMotionListener(new MouseMotionAdapter() {});

        return panel;
    }

    private JMenuBar createMenuBar() {
        myBar = new JMenuBar();
        myBar.setBorder(BorderFactory.createEmptyBorder());

        myBar.add(createOperatorMenu());
        myBar.add(createBooleanMenu());
        myBar.add(createStringMenu());
        myBar.add(createNodeMenu());
        myBar.add(createNumberMenu());
        myBar.add(createDateTimeMenu());
        myBar.add(createBPELMenu());

        return myBar;
    }

    private JButton createCollapseExpandAllButton() {
        final JButton button = createButton(
                new ButtonAction(
                icon(Palette.class, "expose"), // NOI18N
                i18n(Palette.class, "TLT_Collapse_Expand")) { // NOI18N

                    public void actionPerformed(ActionEvent event) {
//out("DO: " + myIsCollapsed);
                        if (myIsCollapsed) {
                            myMapper.expandNonEmptyGraphs();
                        } else {
                            myMapper.collapseAll(COLLAPSE_LEVEL);
                        }
                        myIsCollapsed = !myIsCollapsed;
                    }
                });
        setImageSize(button);
        button.getAccessibleContext().setAccessibleName(NbBundle
                .getMessage(Palette.class, "ACSN_ExpandCollapseButton")); // NOI18N
        button.getAccessibleContext().setAccessibleDescription(NbBundle
                .getMessage(Palette.class, "ACSD_ExpandCollapseButton")); // NOI18N

        return button;
    }

    public void hideMenu() {
        for (int i = 0; i < myBar.getMenuCount(); i++) {
            JMenu menu = myBar.getMenu(i);

            if (menu.isSelected()) {
                menu.setSelected(false);
                menu.getPopupMenu().setVisible(false);
                return;
            }
        }
    }

    public Mapper getMapper() {
        return myMapper;
    }

    private JMenu createOperatorMenu() {
        String menuName = i18n(Palette.class, "LBL_Operator"); // NOI18N
        JMenu menu = new MyMenu(menuName);
        menu.setToolTipText(menuName);
        menu.setIcon(icon(Palette.class, "operator")); // NOI18N

        menu.add(new Item(this, new Handler(CoreOperationType.OP_GT)));
        menu.add(new Item(this, new Handler(CoreOperationType.OP_GE)));
        menu.add(new Item(this, new Handler(CoreOperationType.OP_LT)));
        menu.add(new Item(this, new Handler(CoreOperationType.OP_LE)));
        menu.add(new Item(this, new Handler(CoreOperationType.OP_SUM)));
        menu.add(new Item(this, new Handler(CoreOperationType.OP_MINUS)));
        menu.add(new Item(this, new Handler(CoreOperationType.OP_MULT)));
        menu.add(new Item(this, new Handler(CoreOperationType.OP_DIV)));
        menu.add(new Item(this, new Handler(CoreOperationType.OP_MOD)));
        menu.add(new Item(this, new Handler(CoreOperationType.OP_NEGATIVE)));
        menu.add(new Item(this, new Handler(CoreOperationType.OP_NE)));
        menu.add(new Item(this, new Handler(CoreOperationType.OP_EQ)));

        return menu;
    }

    private JMenu createBooleanMenu() {
        String menuName = i18n(Palette.class, "LBL_Boolean"); // NOI18N
        JMenu menu = new MyMenu(menuName);
        menu.setToolTipText(menuName);
        menu.setIcon(icon(Palette.class, "boolean")); // NOI18N

        menu.add(new Item(this, new Handler(CoreOperationType.OP_AND)));
        menu.add(new Item(this, new Handler(CoreOperationType.OP_OR)));
        menu.add(new Item(this, new Handler(CoreFunctionType.FUNC_NOT)));
        menu.add(new Item(this, new Handler(CoreFunctionType.FUNC_LANG)));
        menu.add(new Item(this, new Handler(CoreFunctionType.FUNC_FALSE)));
        menu.add(new Item(this, new Handler(CoreFunctionType.FUNC_TRUE)));
        menu.add(new Item(this, new Handler(CoreFunctionType.FUNC_BOOLEAN)));

        return menu;
    }

    private JMenu createStringMenu() {
        String menuName = i18n(Palette.class, "LBL_String"); // NOI18N
        JMenu menu = new MyMenu(menuName);
        menu.setToolTipText(menuName);
        menu.setIcon(icon(Palette.class, "string")); // NOI18N

        menu.add(new Item(this, new Handler(CoreFunctionType.FUNC_CONTAINS)));
        menu.add(new Item(this, new Handler(CoreFunctionType.FUNC_NORMALIZE_SPACE)));
        menu.add(new Item(this, new Handler(CoreFunctionType.FUNC_STRING)));
        menu.add(new Item(this, new Handler(CoreFunctionType.FUNC_STARTS_WITH)));
        menu.add(new Item(this, new Handler(CoreFunctionType.FUNC_STRING_LENGTH)));
        menu.add(new Item(this, new Handler(CoreFunctionType.FUNC_SUBSTRING)));
        menu.add(new Item(this, new Handler(CoreFunctionType.FUNC_SUBSTRING_BEFORE)));
        menu.add(new Item(this, new Handler(CoreFunctionType.FUNC_SUBSTRING_AFTER)));
        menu.add(new Item(this, new Handler(CoreFunctionType.FUNC_TRANSLATE)));
        menu.add(new Item(this, new Handler(CoreFunctionType.FUNC_CONCAT)));
        menu.add(new Item(this, new Handler(new String())));

        return menu;
    }

    private JMenu createNodeMenu() {
        String menuName = i18n(Palette.class, "LBL_Node"); // NOI18N
        JMenu menu = new MyMenu(menuName);
        menu.setToolTipText(menuName);
        menu.setIcon(icon(Palette.class, "node")); // NOI18N

        menu.add(new Item(this, new Handler(CoreFunctionType.FUNC_LOCAL_NAME)));
        menu.add(new Item(this, new Handler(CoreFunctionType.FUNC_NAME)));
        menu.add(new Item(this, new Handler(CoreFunctionType.FUNC_NAMESPACE_URI)));
        menu.add(new Item(this, new Handler(CoreFunctionType.FUNC_POSITION)));
        menu.add(new Item(this, new Handler(CoreFunctionType.FUNC_LAST)));
        menu.add(new Item(this, new Handler(CoreFunctionType.FUNC_COUNT)));
        //
        // The following functions are not supported by the runtime
        // menu.add(new Item(this, new Handler(CoreOperationType.OP_UNION)));
        // menu.add(new Item(this, new Handler(CoreFunctionType.FUNC_ID)));
        // menu.add(new Item(this, new Handler(CoreFunctionType.FUNC_KEY)));

        return menu;
    }

    private JMenu createNumberMenu() {
        String menuName = i18n(Palette.class, "LBL_Number"); // NOI18N
        JMenu menu = new MyMenu(menuName);
        menu.setToolTipText(menuName);
        menu.setIcon(icon(Palette.class, "number")); // NOI18N

        menu.add(new Item(this, new Handler(CoreFunctionType.FUNC_NUMBER)));
        menu.add(new Item(this, new Handler(new Integer(0))));
        menu.add(new Item(this, new Handler(CoreFunctionType.FUNC_ROUND)));
        menu.add(new Item(this, new Handler(CoreFunctionType.FUNC_SUM)));
        menu.add(new Item(this, new Handler(CoreFunctionType.FUNC_FLOOR)));
        menu.add(new Item(this, new Handler(CoreFunctionType.FUNC_CEILING)));
        //
        // Isn't supported by the runtime
        // menu.add(new Item(this, new Handler(CoreFunctionType.FUNC_FORMAT_NUMBER)));

        return menu;
    }

    private JMenu createDateTimeMenu() {
        String menuName = i18n(Palette.class, "LBL_Date_Time"); // NOI18N
        JMenu menu = new MyMenu(menuName);
        menu.setToolTipText(menuName);
        menu.setIcon(icon(Palette.class, "date_time")); // NOI18N

        menu.add(new Item(this, new Handler(BpelXPathExtFunctionMetadata.CURRENT_DATE_METADATA)));
        menu.add(new Item(this, new Handler(BpelXPathExtFunctionMetadata.CURRENT_TIME_METADATA)));
        menu.add(new Item(this, new Handler(BpelXPathExtFunctionMetadata.CURRENT_DATE_TIME_METADATA)));
        menu.add(new Item(this, new Handler.Duration()));

        return menu;
    }

    private JMenu createBPELMenu() {
        String menuName = i18n(Palette.class, "LBL_BPEL"); // NOI18N
        JMenu menu = new MyMenu(menuName);
        menu.setToolTipText(menuName);
        menu.setIcon(icon(Palette.class, "bpel")); // NOI18N

        menu.add(new Item(this, new Handler(BpelXPathExtFunctionMetadata.DO_XSL_TRANSFORM_METADATA)));
        //
        // Isn't supported by the runtime
        // menu.add(new Item(this, new Handler(BpelXPathExtFunctionMetadata.GET_VARIABLE_PROPERTY_METADATA)));
        //
        menu.add(new Item(this, new WrapServiceRefHandler(BpelXPathCustomFunction.WRAP_WITH_SERVICE_REF_METADATA)));
        menu.add(new Item(this, new Handler(BpelXPathExtFunctionMetadata.DO_MARSHAL_METADATA)));
        menu.add(new Item(this, new Handler(BpelXPathExtFunctionMetadata.DO_UNMARSHAL_METADATA)));

        return menu;
    }

    // ---------------------------------------------------------------
    private static class Border implements javax.swing.border.Border {

        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Color oldColor = g.getColor();
            g.setColor(c.getBackground().darker());
            y += height - 1;
            g.drawLine(x, y, x + width - 1, y);
            g.setColor(oldColor);
        }

        public Insets getBorderInsets(Component c) {
            return new Insets(0, 0, 1, 0);
        }

        public boolean isBorderOpaque() {
            return true;
        }
    }
    private JMenuBar myBar;
    private Mapper myMapper;
    private boolean myIsCollapsed;
    private static final int COLLAPSE_LEVEL = 2;

    private static class MyMenu extends JMenu {

        MyMenu(String name) {
            super(name);
        }

        @Override
        public String getToolTipText() {
            Dimension size = getPreferredSize();
            return (getWidth() < size.width) 
                    ? super.getToolTipText()
                    : null;
        }
    }
}
