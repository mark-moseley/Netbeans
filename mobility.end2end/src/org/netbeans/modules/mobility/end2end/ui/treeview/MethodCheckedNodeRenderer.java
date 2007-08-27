/*
 * MethodCheckedNodeRenderer.java
 *
 */

package org.netbeans.modules.mobility.end2end.ui.treeview;

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ItemListener;
import java.beans.BeanInfo;
import java.io.CharConversionException;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.tree.TreeCellRenderer;
import org.netbeans.modules.mobility.end2end.util.ServiceNodeManager;
import org.openide.explorer.view.Visualizer;
import org.openide.nodes.Node;
import org.openide.xml.XMLUtil;

/**
 *
 * @author  Adam
 */
public class MethodCheckedNodeRenderer extends JPanel implements TreeCellRenderer {

    private static final Color selectionForeground = UIManager.getColor("Tree.selectionForeground"); //NOI18N
    private static final Color selectionBackground = UIManager.getColor("Tree.selectionBackground"); //NOI18N
    private static final Color textForeground = UIManager.getColor("Tree.textForeground"); //NOI18N
    private Object defaultRenderer = null;
    private MethodCheckedTreeBeanView storage;
    private final CardLayout layout;

    /** Creates new form MethodCheckedNodeRenderer */
    public MethodCheckedNodeRenderer() {
        this(null);
    }

    /** Creates new form MethodCheckedNodeRenderer */
    public MethodCheckedNodeRenderer(Object defaultRenderer) {
        this.defaultRenderer = defaultRenderer;
        Font fontValue = UIManager.getFont("Tree.font"); //NOI18N
        if (fontValue != null) {
            setFont(fontValue);
        }
        initComponents();
        Boolean booleanValue = (Boolean) UIManager.get("Tree.drawsFocusBorderAroundIcon"); //NOI18N
        setFocusPainted((booleanValue != null) && (booleanValue.booleanValue()));
        jPanel2.setPreferredSize(jCheckBox1.getSize());
        layout = (CardLayout) jPanel1.getLayout();
    }
    
    public void setContentStorage(final MethodCheckedTreeBeanView storage) {
        this.storage = storage;
    }

    public Component getTreeCellRendererComponent(final JTree tree, final Object value, final boolean selected, final boolean expanded, final boolean leaf, final int row, final boolean hasFocus) {
        // System.out.println("in getTreeCellRendererComponent");
        Component returnValue = null;
        final Node node = Visualizer.findNode(value);
        /* sigal */

        if (node != null && storage != null && !node.equals(storage.getWaitNode())) {
            setText(node.getDisplayName());
            // System.out.println("node = " + node.getDisplayName() );
            setEnabled(tree.isEnabled());

            if (selected) {
                // System.out.println("SELECTED");
                setForeground(selectionForeground, textForeground);
                setBackground(selectionBackground, tree.getBackground());
            } else {
                // System.out.println("NOT  SELECTED");
                setForeground(textForeground);
                setBackground(tree.getBackground());
            }
            // setText(node.getDisplayName());
            if (storage == null) {
                // System.out.println("storage = null");
                setState(MethodCheckedTreeBeanView.UNSELECTED);
            } else {
                final Object state = storage.getState(node);
                //System.out.println("state = "+ state.toString());
                setState(state == null ? MethodCheckedTreeBeanView.UNSELECTED : state);
            }

            // Strikeout the line
            if (node.getValue(ServiceNodeManager.NODE_VALIDITY_ATTRIBUTE) != null && !((Boolean) node.getValue( ServiceNodeManager.NODE_VALIDITY_ATTRIBUTE )).booleanValue()) try {
                setText("<html><s>" + XMLUtil.toAttributeValue(node.getDisplayName()) + "</s></html>");
                setEnabled(false);
            } catch (CharConversionException cce) {} else {
                setEnabled(true);
            }

            setIcon(new ImageIcon(node.getIcon(BeanInfo.ICON_COLOR_16x16)));
            return this;
        }
        if (defaultRenderer != null) {
            returnValue = ((TreeCellRenderer) defaultRenderer).getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
        }
        return returnValue;
    }

    public void setForeground(final Color fg) {
        setForeground(fg, fg);
    }

    public void setBackground(final Color bg) {
        setBackground(bg, bg);
    }

    public void setForeground(final Color selection, final Color text) {
        if (jCheckBox1 == null || jLabel1 == null || jPanel1 == null) {
            return;
        }
        jCheckBox1.setForeground(text);
        jLabel1.setForeground(selection);
        super.setForeground(selection);
    }

    public void setBackground(final Color selection, final Color text) {
        if (jCheckBox1 == null || jLabel1 == null || jPanel1 == null) {
            return;
        }
        //System.err.println(jCheckBox1);
        jCheckBox1.setBackground(text);
        jPanel2.setBackground(text);
        jLabel1.setBackground(selection);
        super.setBackground(selection);
    }

    public void setFocusPainted(final boolean painted) {
        jCheckBox1.setFocusPainted(painted);
    }

    public void setText(final String text) {
        jLabel1.setText(text);
    }

    public String getText() {
        return jLabel1.getText();
    }

    public void setState(final Object state) {
        jCheckBox1.setState(state);
    }

    public void setIcon(final Icon icon) {
        //System.err.println(icon);
        jLabel1.setIcon(icon);
    }

    public void setEnabled(final boolean enabled) {
        layout.show(jPanel1, new Boolean(enabled).toString());
    }

    public void addItemListener(final ItemListener itemListener) {
        // System.out.println("in add item listener");
        jCheckBox1.addItemListener(itemListener);
    }

    public void removeItemListener(final ItemListener itemListener) {
        // System.out.println("remove item listener");
        jCheckBox1.removeItemListener(itemListener);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jCheckBox1 = new org.netbeans.modules.mobility.end2end.ui.treeview.MultiStateCheckBox();
        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();

        setLayout(new java.awt.BorderLayout(5, 0));

        jPanel1.setLayout(new java.awt.CardLayout());
        jPanel1.add(jCheckBox1, "true");

        org.jdesktop.layout.GroupLayout jPanel2Layout = new org.jdesktop.layout.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 620, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 374, Short.MAX_VALUE)
        );

        jPanel1.add(jPanel2, "false");

        add(jPanel1, java.awt.BorderLayout.CENTER);
        add(jLabel1, java.awt.BorderLayout.EAST);
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private org.netbeans.modules.mobility.end2end.ui.treeview.MultiStateCheckBox jCheckBox1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    // End of variables declaration//GEN-END:variables
}