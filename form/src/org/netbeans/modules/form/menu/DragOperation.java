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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.form.menu;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Point;
import javax.swing.BorderFactory;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JLayeredPane;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSeparator;
import javax.swing.SwingUtilities;
import org.netbeans.modules.form.FormModel;
import org.netbeans.modules.form.HandleLayer;
import org.netbeans.modules.form.MetaComponentCreator;
import org.netbeans.modules.form.RADComponent;
import org.netbeans.modules.form.RADVisualComponent;
import org.netbeans.modules.form.RADVisualContainer;
import org.netbeans.modules.form.layoutdesign.LayoutComponent;
import org.netbeans.modules.form.palette.PaletteItem;
import org.netbeans.modules.form.palette.PaletteUtils;

/**
 * DragOperation handles all drag operations whether they are drag and drop or pick and plop. It
 * also deals with new components from the palette and rearranging existing menu items within the menu.
 * It does *not* handle the actual adding and removing of components. Instead that is delegated back
 * to the MenuEditLayer.
 * @author joshua.marinacci@sun.com
 */
class DragOperation {
    private static final boolean DEBUG = false;
    private MenuEditLayer menuEditLayer;
    private JComponent dragComponent;
    private boolean started = false;
    private JComponent targetComponent;
    private enum Op { PICK_AND_PLOP_FROM_PALETTE, INTER_MENU_DRAG, NO_MENUBAR };
    private Op op = Op.PICK_AND_PLOP_FROM_PALETTE;
    private JMenuItem payloadComponent;
    private PaletteItem currentItem;
    
    public DragOperation(MenuEditLayer menuEditLayer) {
        this.menuEditLayer = menuEditLayer;
        this.started = false;
    }
    
    // start a drag from one menu item to another
    void start(JMenuItem item, Point pt) {
        op = Op.INTER_MENU_DRAG;
        p("starting an inner menu drag for: " + item + " at " + pt);
        started = true;
        
        
        dragComponent = (JMenuItem) createDragFeedbackComponent(item, null);
        dragComponent.setSize(dragComponent.getPreferredSize());
        dragComponent.setLocation(pt);
        menuEditLayer.layers.add(dragComponent, JLayeredPane.DRAG_LAYER);
        menuEditLayer.repaint();
        payloadComponent = item;
    }
    
    private JComponent createDragFeedbackComponent(JMenuItem item, Class type) {
        // get the pre-created component for use as drag feedback
        PaletteItem paletteItem = PaletteUtils.getSelectedItem();
        if(paletteItem != null) {
            MetaComponentCreator creator = menuEditLayer.formDesigner.getFormModel().getComponentCreator();
            RADVisualComponent precreated = creator.precreateVisualComponent(
                    paletteItem.getComponentClassSource());
            if(precreated != null) {
                p("precreated: " + precreated.getBeanClass());
                Object comp = precreated.getBeanInstance();
                if(comp instanceof JComponent) {
                    JComponent jcomp = (JComponent) comp;
                    p("it's a jcomponent");
                    if(comp instanceof JMenuItem) {
                        jcomp.setBorder(MenuEditLayer.DRAG_MENU_BORDER);
                        ((JMenuItem)comp).setBorderPainted(true);
                    }
                    if(comp instanceof JSeparator) {
                        p("it's a separator");
                        jcomp.setBorder(BorderFactory.createLineBorder(Color.RED, 2));//, thickness)MenuEditLayer.DRAG_SEPARATOR_BORDER);
                        jcomp.setPreferredSize(new Dimension(80,5));
                        p("border = " + jcomp.getBorder());
                    }
                    return jcomp;
                }
            }
        }
        
        JComponent dragComponent = null;
        dragComponent = new JMenuItem();
        
        if(item == null && type != null && JComponent.class.isAssignableFrom(type)) {
            try {
                dragComponent = (JComponent)type.newInstance();
                p("created a drag component here: " + dragComponent);
            } catch (Exception ex) {
                System.out.println("exception: " + ex.getMessage());
                ex.printStackTrace();
            }
        }
        if(item instanceof JMenu) { 
            dragComponent = new JMenu(); 
        }
        if(item instanceof JCheckBoxMenuItem) { 
            dragComponent = new JCheckBoxMenuItem(); 
            ((JCheckBoxMenuItem)dragComponent).setSelected(true);
        }
        if(item instanceof JRadioButtonMenuItem) { 
            dragComponent = new JRadioButtonMenuItem(); 
            ((JRadioButtonMenuItem)dragComponent).setSelected(true);
        }
        if(dragComponent instanceof JMenuItem) {
            JMenuItem dragItem = (JMenuItem) dragComponent;
            if(item != null) {
                dragItem.setText(item.getText());
                dragItem.setIcon(item.getIcon());
                if(! (item instanceof JMenu)) {
                    dragItem.setAccelerator(item.getAccelerator());
                }
            } else {
                dragItem.setText("a new menu item");
            }
            dragItem.setMargin(new Insets(1,1,1,1));
            dragItem.setBorderPainted(true);
        }
        dragComponent.setBorder(MenuEditLayer.DRAG_MENU_BORDER);
        return dragComponent;

    }
    
    // start a pick and plop from the palette operation
    void start(PaletteItem item, Point pt) {
        // clean up prev is necessary
        if(dragComponent != null) {
            menuEditLayer.layers.remove(dragComponent);
            dragComponent = null;
        }
        
        if(!menuEditLayer.doesFormContainMenuBar()) {
            op = Op.NO_MENUBAR;
            //josh: use the invalid drop target cursor instead
            menuEditLayer.glassLayer.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            menuEditLayer.showMenubarWarning = true;
            menuEditLayer.repaint();
            return;
        }
        
        op = Op.PICK_AND_PLOP_FROM_PALETTE;
        p("starting drag op for : " + item.getComponentClassName() + " at " + pt);
        started = true;
        dragComponent = createDragFeedbackComponent(null, item.getComponentClass());
        p("created drag component = " + dragComponent);
        dragComponent.setSize(dragComponent.getPreferredSize());
        p("created drag component = " + dragComponent);
        dragComponent.setLocation(pt);
        menuEditLayer.layers.add(dragComponent, JLayeredPane.DRAG_LAYER);
        menuEditLayer.repaint();
        currentItem = item;
    }
    
    void move(Point pt) {
        if(dragComponent != null) {
            // move the drag component
            dragComponent.setLocation(pt);
            
            
            // look at the rad component under the cursor before checking the popups
            RADComponent rad = menuEditLayer.formDesigner.getHandleLayer().getMetaComponentAt(pt, HandleLayer.COMP_DEEPEST);
            
            // if dragging a JMenu over an open spot in the menu bar
            if(rad != null && JMenuBar.class.isAssignableFrom(rad.getBeanClass()) && JMenu.class.isAssignableFrom(dragComponent.getClass())) {
                p("over the menu bar");
                showMenuBarDropTarget(rad, pt);
                targetComponent = (JComponent) menuEditLayer.formDesigner.getComponent(rad);
            }
            
            // open any relevant top-level menus
            if(rad != null && JMenu.class.isAssignableFrom(rad.getBeanClass())) {
                //p("over a menu");
                targetComponent = (JComponent) menuEditLayer.formDesigner.getComponent(rad);
                menuEditLayer.openMenu(rad, targetComponent);
                if(JMenu.class.isAssignableFrom(dragComponent.getClass())) {
                    showMenuBarDropTarget(rad, pt);
                }
                return;
            }
            
            //show any drop target markers
            Component child = getDeepestComponentInPopups(pt);
            //p("child = " + child);
            if(child == null) {
                if(targetComponent != null) {
                    //targetComponent.setBorder(MenuEditLayer.UNSELECTED_BORDER);
                }
            }
            
            
            if(child instanceof JMenuItem && /*child != payloadComponent &&*/ child != dragComponent) {
                if(targetComponent != null) {
                    //targetComponent.setBorder(MenuEditLayer.UNSELECTED_BORDER);
                }
                targetComponent = (JComponent)child;
                if(targetComponent != null) {
                    menuEditLayer.dropTargetLayer.setDropTargetComponent(targetComponent, DropTargetLayer.DropTargetType.INTER_MENU, pt);
                }
                menuEditLayer.repaint();
            }
            
            if(child instanceof JMenu) {
                Point pt2 = SwingUtilities.convertPoint(menuEditLayer.glassLayer, pt, child);
                styleMenu((JMenu)child,pt2);
            }
            
        } else {
            p("DragOperation: dragComponent shouldn't be null when moving");
        }
    }
    
    private void showMenuBarDropTarget(RADComponent comp, Point pt) {
        menuEditLayer.setDrawMenuBarNewComponentTarget(comp, pt);
        menuEditLayer.repaint();
    }
    
    private void styleMenu(JMenu menu, Point point) {
        menu.setBorderPainted(true);
        // if on the right side: 
        p("point = " + point + "  widt = " + menu.getWidth());
        if(point.x > menu.getWidth()-30) {
            p("doing menu right");
            //menu.setBorder(MenuEditLayer.INSERTION_BORDER_MENU_RIGHT);
            menuEditLayer.dropTargetLayer.setDropTargetComponent(menu, DropTargetLayer.DropTargetType.INTO_SUBMENU, point);
            menu.repaint();
        } else {
            menuEditLayer.dropTargetLayer.setDropTargetComponent(menu, DropTargetLayer.DropTargetType.INTER_MENU, point);
            //menu.setBorder(MenuEditLayer.INSERTION_BORDER);
        }
        menuEditLayer.showMenuPopup(menu);
    }
    
    void end(Point pt) {
        started = false;
        currentItem = null;
        if(dragComponent == null) return;
        p("ending an operation at: " + pt);
        menuEditLayer.layers.remove(dragComponent);
        menuEditLayer.setDrawMenuBarNewComponentTarget(null, null);
        menuEditLayer.dropTargetLayer.setDropTargetComponent(null, DropTargetLayer.DropTargetType.NONE, null);
        
        p("op = " + op);
        switch (op) {
        case PICK_AND_PLOP_FROM_PALETTE: completePickAndPlopFromPalette(pt); break;
        case INTER_MENU_DRAG: completeInterMenuDrag(pt); break ;
        }
        
        payloadComponent = null;
        targetComponent = null;
        menuEditLayer.repaint();
        
    }
    
    void fastEnd() {
        started = false;
        if(dragComponent != null) {
            menuEditLayer.layers.remove(dragComponent);
        }
        if(targetComponent != null) {
            //targetComponent.setBorder(MenuEditLayer.UNSELECTED_BORDER);
        }
    }
    
    // only looks at JMenu and JMenubar RADComponents as well as anything in the popups
    JComponent getDeepestComponent(Point pt) {
        RADComponent rad = menuEditLayer.formDesigner.getHandleLayer().getMetaComponentAt(pt, HandleLayer.COMP_DEEPEST);
        if(rad != null && (JMenu.class.isAssignableFrom(rad.getBeanClass()) ||
                JMenuBar.class.isAssignableFrom(rad.getBeanClass()))) {
           return (JComponent) menuEditLayer.formDesigner.getComponent(rad);
        } else {
            return (JComponent) getDeepestComponentInPopups(pt);
        }
    }
    
    private void completeInterMenuDrag(Point pt) {
        p("complete inter menu drag: target comp = " + targetComponent);
        p("================\n\n\n\n==========\n\n========");
        if(targetComponent == null) return;
        //targetComponent.setBorder(MenuEditLayer.UNSELECTED_BORDER);
        
        //check if it's still a valid target
        JComponent tcomp = (JComponent) getDeepestComponent(pt);
        p("target = " + targetComponent);
        p("tcomp = " + tcomp);
        if(targetComponent != tcomp) {
            p("no longer over a valid target. bailing");
            menuEditLayer.formDesigner.toggleSelectionMode();
            return;
        }
        
        if(tcomp instanceof JMenu) {
            JMenu menu = (JMenu) tcomp;
            
            // conver to target menu's coords.
            Point pt2 = SwingUtilities.convertPoint(menuEditLayer.glassLayer, pt, menu);
            
            // if dragging a jmenu onto a toplevel jmenu
            if(menu.getParent() instanceof JMenuBar && payloadComponent instanceof JMenu) {
                p("dropping into a toplevel menu");
                if(pt2.x < 15) {  // if on the left edge
                    p("doing a left drop");
                    menuEditLayer.moveRadComponentToBefore(payloadComponent, menu);
                    return;
                } else if (pt2.x > menu.getWidth()-15) {  // if on the right edge
                    p("doing a right drop");
                    //menuEditLayer.moveRadComponentToAfter(payloadComponent, menu);
                    p("not doing a right drop yet");
                    return;
                } else {  // else must be in the center so just add to the menu instead of next to
                    menuEditLayer.moveRadComponentInto(payloadComponent, menu);
                    p("doing a center drop");
                    return;
                }
            }
            p("on a jmenu. could be in or above");
            p("converted point = " + pt2);
            if(pt2.x > menu.getWidth()-30) {
                p("doing 'in' menu drop");
                menuEditLayer.moveRadComponentInto(payloadComponent, menu);
            } else {
                p("doing above menu drop");
                menuEditLayer.moveRadComponentToBefore(payloadComponent, targetComponent);
            }
            return;
        }
        
        menuEditLayer.moveRadComponentToBefore(payloadComponent, targetComponent);
    }
    
    private void completePickAndPlopFromPalette(Point pt) {
        p("complete pick and plop from palette: target comp = " + targetComponent);
        PaletteItem paletteItem = PaletteUtils.getSelectedItem();
        
        if(targetComponent == null) return;
        
        //targetComponent.setBorder(MenuEditLayer.UNSELECTED_BORDER);
        
        //check if it's still a valid target
        JComponent tcomp = (JComponent) getDeepestComponent(pt);
        p("target = " + targetComponent);
        p("tcomp = " + tcomp);
        if(targetComponent != tcomp) {
            p("no longer over a valid target. bailing");
            menuEditLayer.formDesigner.toggleSelectionMode();
            return;
        }
        
        // get the pre-created component
        MetaComponentCreator creator = menuEditLayer.formDesigner.getFormModel().getComponentCreator();
        RADVisualComponent precreated = creator.precreateVisualComponent(
                paletteItem.getComponentClassSource());
        JComponent newComponent = (JComponent) precreated.getBeanInstance();
        // if pre-creation failed then make new component manually
        if(newComponent == null) {
            try {
                newComponent = (JComponent)paletteItem.getComponentClass().newInstance();
            } catch (Exception ex) {
                p("couldn't create new component!");
                ex.printStackTrace();
                return;
            }
        }
        
        // add new component reference to the form
        // i can probably remove both of these variables
        LayoutComponent layoutComponent = creator.getPrecreatedLayoutComponent();
        Object constraints = null;
        
        
        // dragged to a menu, add inside the menu instead of next to it
        if(targetComponent instanceof JMenu) {
            p("============== doing a new comp to a jmenu");
            Point pt2 = SwingUtilities.convertPoint(menuEditLayer.glassLayer, pt, targetComponent);
            if(pt2.x > targetComponent.getWidth()-30) {
                p("doing in menu drop");
                RADVisualContainer targetContainer = (RADVisualContainer) menuEditLayer.formDesigner.getMetaComponent(targetComponent);
                p("target container = " + targetContainer);
                boolean added = creator.addPrecreatedComponent(targetContainer, constraints);
            } else {
                p("doing above menu drop");
                RADVisualComponent newRad = creator.getPrecreatedMetaComponent();
                p("new rad = " + newRad);
                menuEditLayer.addRadComponentToBefore(newRad, targetComponent);
            }
        } else {
            if(targetComponent instanceof JMenuBar) {
                p("======= doing a new comp directly to the jmenubar");
                RADVisualContainer targetContainer = (RADVisualContainer) menuEditLayer.formDesigner.getMetaComponent(targetComponent);
                p("target container = " + targetContainer);
                boolean added = creator.addPrecreatedComponent(targetContainer, constraints);
            } else {
                /*
                // add the new component to the target's containing menu
                JComponent menuParent = menuEditLayer.getMenuParent(targetComponent);
                RADVisualContainer targetContainer = (RADVisualContainer) menuEditLayer.formDesigner.getMetaComponent(menuParent);
                boolean added = creator.addPrecreatedComponent(targetContainer, constraints);
                */
                p("doing the new kind of add");
                RADVisualComponent newRad = creator.getPrecreatedMetaComponent();
                p("new rad = " + newRad);
                menuEditLayer.addRadComponentToBefore(newRad, targetComponent);
            }
        }
        
        menuEditLayer.formDesigner.toggleSelectionMode();
        
    }
    
    
    //josh: this is a very slow way to find the component under the mouse cursor.
    //there must be a faster way to do it
    public JComponent getDeepestComponentInPopups(Point pt) {
        Component[] popups = menuEditLayer.layers.getComponentsInLayer(JLayeredPane.DEFAULT_LAYER);
        for(Component popup : popups) {
            //p("looking at popup: " + popup);
            if(popup.isVisible()) {
                Point pt2 = SwingUtilities.convertPoint(menuEditLayer, pt, popup);
                JComponent child = (JComponent) javax.swing.SwingUtilities.getDeepestComponentAt(popup, pt2.x, pt2.y);
                if(child != null) return child;
            }
        }
        return null;
    }
    
    
    public boolean isStarted() {
        return started;
    }
    
    public PaletteItem getCurrentItem() {
        return currentItem;
    }
    
    private static void p(String s) {
        if(DEBUG) {
            System.out.println(s);
        }
    }
}
