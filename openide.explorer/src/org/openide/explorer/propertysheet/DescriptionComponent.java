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
package org.openide.explorer.propertysheet;

import java.awt.BorderLayout;
import javax.accessibility.Accessible;
import javax.accessibility.AccessibleContext;
import org.openide.util.Utilities;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.accessibility.AccessibleRole;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JComponent.AccessibleJComponent;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import org.openide.util.NbBundle;


/**
 * A component which can display a description, a title and a button.
 *
 * @author  Tim Boudreau
 */
class DescriptionComponent extends JComponent implements ActionListener, MouseListener, Accessible {
    private static int fontHeight = -1;
    private JTextArea jta;
    private JLabel lbl;
    private JButton btn;
    private JToolBar toolbar;
    private JScrollPane jsc;

    /** Creates a new instance of SplitLowerComponent */
    public DescriptionComponent() {
        init();
    }

    private void init() {
        setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));

        jta = new JTextArea();
        jta.setLineWrap(true);
        jta.setWrapStyleWord(true);
        jta.setOpaque(false);
        jta.setBackground(getBackground());
        jta.setEditable(false);
        jta.setOpaque(false);
        jta.getAccessibleContext().setAccessibleName( NbBundle.getMessage(DescriptionComponent.class, "ACS_Description") );
        jta.getAccessibleContext().setAccessibleDescription( NbBundle.getMessage(DescriptionComponent.class, "ACSD_Description") );

        //We use a JScrollPane to suppress the changes in layout that will be
        //caused by adding the raw JTextArea directly - JTextAreas can fire
        //preferred size changes from within their paint methods, leading to
        //cyclic revalidation problems
        jsc = new JScrollPane(jta);
        jsc.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        jsc.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        jsc.setBorder(BorderFactory.createEmptyBorder());
        jsc.setViewportBorder(jsc.getBorder());
        jsc.setOpaque(false);
        jsc.setBackground(getBackground());
        jsc.getViewport().setOpaque(false);

        Font f = UIManager.getFont("Tree.font"); //NOI18N

        if (f != null) {
            jta.setFont(f);
        }

        if (!PropUtils.psNoHelpButton) {
            Image help = Utilities.loadImage("org/openide/resources/propertysheet/propertySheetHelp.png", true); //NOI18N

            btn = new JButton(new ImageIcon(help));
            btn.addActionListener(this);

            toolbar = new JToolBar ();
            toolbar.setRollover (true);
            toolbar.setFloatable (false);
            toolbar.setLayout (new BorderLayout (0, 0));
            toolbar.setBorder (BorderFactory.createEmptyBorder());
            toolbar.add (btn);
            btn.setFocusable(false);
        }

        lbl = new JLabel("Label"); //NOI18N

        lbl.setFont(new Font(null, Font.BOLD, lbl.getFont().getSize()));

        add(jsc);
        add(lbl);
        if (!PropUtils.psNoHelpButton) {
            add(toolbar);
        }
        jta.addMouseListener(this);
        jsc.addMouseListener(this);
        lbl.addMouseListener(this);
        if (!PropUtils.psNoHelpButton) {
            btn.addMouseListener(this);
        }
        jsc.getViewport().addMouseListener(this);
    }

    public void doLayout() {
        Insets ins = getInsets();
        Dimension lbll = lbl.getPreferredSize();
        int height = lbll.height;
        int right = getWidth() - ins.right;
        if (!PropUtils.psNoHelpButton) {
            Dimension bttn = toolbar.getPreferredSize();
            height = Math.max(bttn.height, lbll.height);
            right = getWidth() - (ins.right + bttn.width);
            toolbar.setBounds(right, ins.top, bttn.width, height);
        }
        lbl.setBounds(ins.left, ins.top, right, height);
        jsc.setBounds(ins.left, height, getWidth() - (ins.left + ins.right), getHeight() - height);
    }

    public void setDescription(String title, String txt) {
        if (title == null) {
            title = "";
        }

        if (txt == null) {
            txt = "";
        }

        lbl.setText(title);

        if (title.equals(txt)) {
            jta.setText("");
        } else {
            jta.setText(txt);
        }
    }

    public void setHelpEnabled(boolean val) {
        if (!PropUtils.psNoHelpButton) {
            btn.setEnabled(val);
        }
    }

    /**
     * Overridden to calculate a font height on the first paint
     */
    public void paint(Graphics g) {
        if (fontHeight == -1) {
            fontHeight = g.getFontMetrics(lbl.getFont()).getHeight();
        }

        super.paint(g);
    }

    /** Overridden to ensure the description area doesn't grow too big
     * with large amounts of text */
    public Dimension getPreferredSize() {
        Dimension d = new Dimension(super.getPreferredSize());

        if (fontHeight > 0) {
            Insets ins = getInsets();
            d.height = Math.max(50, Math.max(d.height, (4 * fontHeight) + ins.top + ins.bottom + 12));
        } else {
            d.height = Math.min(d.height, 64);
        }

        return d;
    }

    public Dimension getMinimumSize() {
        if (fontHeight < 0) {
            return super.getMinimumSize();
        }

        Dimension d = new Dimension(4 * fontHeight, 4 * fontHeight);

        return d;
    }

    public void actionPerformed(ActionEvent actionEvent) {
        PSheet sheet = (PSheet) SwingUtilities.getAncestorOfClass(PSheet.class, this);

        if (sheet != null) {
            sheet.helpRequested();
        }
    }

    private PSheet findSheet() {
        return (PSheet) SwingUtilities.getAncestorOfClass(PSheet.class, this);
    }

    public void mouseClicked(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    /**
     * Forward events that might invoke the popup menu
     */
    public void mousePressed(MouseEvent e) {
        PSheet sh = findSheet();

        if (sh != null) {
            sh.mousePressed(e);
        }
    }

    /**
     * Forward events that might invoke the popup menu
     */
    public void mouseReleased(MouseEvent e) {
        PSheet sh = findSheet();

        if (sh != null) {
            sh.mousePressed(e);
        }
    }

    public AccessibleContext getAccessibleContext() {

        if( null == accessibleContext ) {
            accessibleContext = new AccessibleJComponent() {
                        public AccessibleRole getAccessibleRole() {
                            return AccessibleRole.SWING_COMPONENT;
                        }
                    };
        
            accessibleContext.setAccessibleName( NbBundle.getMessage(DescriptionComponent.class, "ACS_Description") );
            accessibleContext.setAccessibleDescription( NbBundle.getMessage(DescriptionComponent.class, "ACSD_Description") );
        }
        
        return accessibleContext;
    }
}
