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
/*
 * AquaToolBarButtonUI.java
 *
 * Created on January 17, 2004, 1:54 PM
 */

package org.netbeans.swing.plaf.aqua;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.ButtonUI;
import javax.swing.plaf.basic.BasicButtonListener;
import java.awt.*;

/** A finder-style aqua toolbar button UI
 *
 * @author  Tim Boudreau
 */
class AquaToolBarButtonUI extends ButtonUI implements ChangeListener {
    private static BasicButtonListener listener = 
        new BasicButtonListener(null);
    
    /** Creates a new instance of AquaToolBarButtonUI */
    public AquaToolBarButtonUI() {
    }
    
    public void installUI (JComponent c) {
        AbstractButton b = (AbstractButton) c;
        b.addMouseListener (listener);
        b.addChangeListener(this);
        b.setContentAreaFilled(false);
        b.setOpaque(false);
        b.setFocusable(false);
        b.setBorderPainted(false);
        b.setBorder (BorderFactory.createEmptyBorder());
        b.putClientProperty("hideActionText", Boolean.TRUE); //NOI18N
   }
    
    public void uninstallUI(JComponent c) {
        c.removeMouseListener (listener);
    }
    
    public void stateChanged(ChangeEvent e) {
        ((AbstractButton) e.getSource()).repaint();
    }
    
    private final Rectangle scratch = new Rectangle();
    public void paint (Graphics g, JComponent c) {
        Rectangle r = c.getBounds(scratch);
        AbstractButton b = (AbstractButton) c;
        r.x = 0;
        r.y = 0;
        Paint temp = ((Graphics2D) g).getPaint();
        paintBackground ((Graphics2D)g, b, r);
        paintIcon (g, b, r);
        ((Graphics2D) g).setPaint(temp);
    }
    
    private void paintBackground (Graphics2D g, AbstractButton b, Rectangle r) {
        if (!b.isEnabled()) {
        } else if (b.getModel().isPressed()) {
            compositeColor (g, r, Color.BLUE, 0.3f);
        } else if (b.getModel().isSelected()) {
            compositeColor (g, r, new Color (0, 120, 255), 0.2f);;
        }
    }
    
    private void compositeColor (Graphics2D g, Rectangle r, Color c, float alpha) {
        g.setColor (c);
        Composite comp = g.getComposite();

        g.setComposite(AlphaComposite.getInstance(
            AlphaComposite.SRC_OVER, alpha));

        g.fillRect (r.x, r.y, r.width, r.height);
        g.setComposite(comp);
    }
    
    
    private static boolean isFirst (AbstractButton b) {
        if (b.getParent() != null && b.getParent().getComponentCount() > 1) {
            //The grip is always component 0, so see if the button
            //is component 1
            return b == b.getParent().getComponent(1);
        } else {
            return false;
        }
    }
    
    private void paintIcon (Graphics g, AbstractButton b, Rectangle r) {
        Icon ic = getIconForState (b);
        if (ic != null) {
            int iconX = 0;
            int iconY = 0;
            int iconW = ic.getIconWidth();
            int iconH = ic.getIconHeight();
            
            if (iconW <= r.width) {
                iconX = (r.width / 2) - (iconW / 2);
            }
            if (iconH <= r.height) {
                iconY = (r.height / 2) - (iconH / 2);
            }
            if (iconH > 20) {
                iconY += 2;
            }
            ic.paintIcon(b, g, iconX, iconY);
        }
    }
    
    private Icon getIconForState (AbstractButton b) {
        ButtonModel mdl = b.getModel();
        Icon result = null;
        if (!b.isEnabled()) {
            result = mdl.isSelected() ? b.getDisabledSelectedIcon() : b.getDisabledIcon();
            if (result == null && mdl.isSelected()) {
                result = b.getDisabledIcon();
            }
        } else {
            if (mdl.isArmed() && !mdl.isPressed()) {
                result = mdl.isSelected() ? b.getRolloverSelectedIcon() : b.getRolloverIcon();
                if (result == null & mdl.isSelected()) {
                    result = b.getRolloverIcon();
                }
            }
            if (mdl.isPressed()) {
                result = b.getPressedIcon();
            } else if (mdl.isSelected()) {
                result = b.getSelectedIcon();
            }
        }
        if (result == null) {
            result = b.getIcon();
        }
        return result;
    }
    
    private static final int minButtonSize = 32;
    public Dimension getPreferredSize(JComponent c) {
        if (c instanceof AbstractButton) {
            Icon ic = getIconForState((AbstractButton) c);
            Dimension result;
            int minSize = isFirst((AbstractButton)c) ? 0 : minButtonSize;
            if (ic != null) {
                result = new Dimension(Math.max(minSize, ic.getIconWidth()+1), 
                    Math.max(minButtonSize,ic.getIconHeight() + 1));
            } else {
                result = new Dimension (minButtonSize, minButtonSize);
            }
            return result;
        } else {
            if (c.getLayout() != null) {
                return c.getLayout().preferredLayoutSize(c);
            }
        }
        return null;
    }    
}
