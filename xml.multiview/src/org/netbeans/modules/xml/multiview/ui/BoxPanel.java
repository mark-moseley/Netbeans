/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.xml.multiview.ui;

import javax.swing.*;
import java.awt.*;

/**
 * @author pfiala
 */
public class BoxPanel extends SectionNodeInnerPanel {

    /**
     * Creates new form BoxPanel
     *
     * @param sectionNodeView
     */
    public BoxPanel(SectionNodeView sectionNodeView) {
        super(sectionNodeView);
        initComponents();
    }

    /**
     * This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents

        setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.Y_AXIS));

    }//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables

    public JComponent getErrorComponent(String errorId) {
        final Component[] components = getComponents();
        for (int i = 0; i < components.length; i++) {
            final Component component = components[i];
            if (component instanceof SectionInnerPanel) {
                SectionInnerPanel panel = (SectionInnerPanel) component;
                final JComponent errorComponent = panel.getErrorComponent(errorId);
                if (errorComponent != null) {
                    return errorComponent;
                }
            }
        }
        return null;
    }

    public void setValue(JComponent source, Object value) {

    }

    public void linkButtonPressed(Object ddBean, String ddProperty) {
        final Component[] components = getComponents();
        for (int i = 0; i < components.length; i++) {
            final Component component = components[i];
            if (component instanceof SectionInnerPanel) {
                SectionInnerPanel panel = (SectionInnerPanel) component;
                panel.linkButtonPressed(ddBean, ddProperty);
            }
        }
    }

    public void setComponents(Component[] components) {
        int n1 = getComponentCount();
        for (int i = 0; i < components.length; i++) {
            Component component = components[i];
            if (i < n1) {
                remove(i);
            }
            add(component, i);
        }
        int n2 = components.length;
        while (getComponentCount() > n2) {
            remove(n2);
        }
    }
    /** This will be called before model is changed from this panel
     */
    protected void signalUIChange() {
    }
}
