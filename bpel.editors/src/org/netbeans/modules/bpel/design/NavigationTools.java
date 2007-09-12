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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.bpel.design;

import java.awt.Cursor;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import org.openide.util.NbBundle;

/**
 *
 * @author anjeleevich
 */
public class NavigationTools extends JPanel implements MouseListener, 
        MouseMotionListener, MouseWheelListener, KeyListener, FocusListener, 
        ActionListener {

    
    private DesignView designView; 
    
    private Mode mode = Mode.OFF; 
    
    private int toolX;
    private int toolY;
    
    private JToggleButton editingModeButton;
    private JToggleButton navigationModeButton;
    
    private ButtonGroup buttonGroup;

    private JComponent[] controllers;
    
    
    public NavigationTools(DesignView designView) {
        this.designView = designView;
        
        setFocusable(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setBackground(null);
        setOpaque(false);
        
        designView.addKeyListener(this);
        designView.addFocusListener(this);
        
        editingModeButton = new JToggleButton(SELECT_TOOL_ICON);
        editingModeButton.setSelected(true);
        editingModeButton.setFocusable(false);
        editingModeButton.setToolTipText(
                getMessage("LBL_NavigationTools_EditingMode")); // NOI18N
        
        navigationModeButton = new JToggleButton(HAND_TOOL_ICON);
        navigationModeButton.setSelected(false);
        navigationModeButton.setFocusable(false);
        navigationModeButton.setToolTipText(
                getMessage("LBL_NavigationTools_NavigationMode")); // NOI18N
        
        buttonGroup = new ButtonGroup();
        buttonGroup.add(editingModeButton);
        buttonGroup.add(navigationModeButton);
        
        addMouseListener(this);
        addMouseMotionListener(this);
        addMouseWheelListener(this);

        editingModeButton.addActionListener(this);
        navigationModeButton.addActionListener(this);
        
        controllers = new JComponent[] {
            editingModeButton,
            navigationModeButton
        };
    }
    
    
    public int getControllersCount() {
        return controllers.length;
    }
    
    
    public JComponent getController(int i) {
        return controllers[i];
    }

    
    public DesignView getDesignView() {
        return designView;
    }
    
    
    private void turnOn(Mode newMode) {
        this.mode = newMode;
        getDesignView().revalidate();
        getDesignView().repaint();
        getDesignView().add(this, 0);
    }
    
    
    private void turnOff() {
        this.mode = Mode.OFF;
        getDesignView().remove(this);
        getDesignView().revalidate();
        getDesignView().repaint();
    }
    
    
    public void setEnabled(boolean v) {
        editingModeButton.setEnabled(v);
        navigationModeButton.setEnabled(v);
    }
    
    
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE && mode == Mode.OFF) {
            turnOn(Mode.WEAK);
        }
    }

    
    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE && mode == Mode.WEAK) {
            turnOff();
        }
    }
    
    
    public void focusLost(FocusEvent e) {
        if (mode == Mode.WEAK) {
            turnOff();
        }
    }


    public void mouseWheelMoved(MouseWheelEvent e) {
        if (e.getWheelRotation() > 0) {
            getDesignView().getZoomManager().zoomIn(e.getX(), e.getY());
        } else {
            getDesignView().getZoomManager().zoomOut(e.getX(), e.getY());
        }
        
        Point p = getDesignView().getMousePosition(true);
        
        if (p != null) {
            toolX = p.x;
            toolY = p.y;
        }
    }

    
    public void mouseDragged(MouseEvent e) {
        int dx = e.getX() - toolX;
        int dy = e.getY() - toolY;
        
        Rectangle rect = getDesignView().getVisibleRect();
        
        rect.x -= dx;
        rect.y -= dy;
        
        getDesignView().scrollRectToVisible(rect);
    }
    
    
    public void mousePressed(MouseEvent e) {
        toolX = e.getX();
        toolY = e.getY();
    }
    
    
    public void mouseReleased(MouseEvent e) {
        
    }

    
    public void mouseClicked(MouseEvent e) {}
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}
    public void mouseMoved(MouseEvent e) {}
    public void keyTyped(KeyEvent e) {}
    public void focusGained(FocusEvent e) {}

    public void actionPerformed(ActionEvent e) {
        if (navigationModeButton.isSelected()) {
            turnOn(Mode.STRONG);
        }
        
        if (editingModeButton.isSelected()) {
            turnOff();
        }
    }

    private static enum Mode { OFF, WEAK, STRONG }
    
    
    private static String getMessage(String key) {
        return NbBundle.getBundle(NavigationTools.class).getString(key); 
    }
    
    
    private static Icon loadIcon(String name) {
        return new ImageIcon(NavigationTools.class
                .getResource("resources/" + name)); // NOI18N
    }       
    
    private static Icon HAND_TOOL_ICON = loadIcon("hand_tool.png"); // NOI18N
    private static Icon SELECT_TOOL_ICON = loadIcon("select_tool.png"); // NOI18N
}
