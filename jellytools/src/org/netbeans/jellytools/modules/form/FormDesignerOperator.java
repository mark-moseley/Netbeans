/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.jellytools.modules.form;

import java.awt.Component;
import java.awt.Container;
import java.awt.Point;

import org.netbeans.jellytools.TopComponentOperator;

import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.ContainerOperator;

import org.openide.windows.TopComponent;

/**
 * Handles access to org.netbeans.modules.form.FormDesigner component.
 */
public class FormDesignerOperator extends TopComponentOperator {
    private ComponentOperator _handleLayer;
    private ContainerOperator _componentLayer;
    private ContainerOperator _fakePane;

    /** Searches for FormDesigner in the specified ContainerOperator. Usually
     * it is FormEditorOperator. In such case it returns currenly selected pane,
     * if there are more than one form being edited. But FormDesigner can be
     * docked to any window.     
     * @param contOper ContainerOperator where to find FormDesigner
     */
    public FormDesignerOperator(ContainerOperator contOper) {
        super((TopComponent)contOper.waitSubComponent(new FormDesignerChooser()));
    }

    /** Returns component which actually handles all events happening
     * on components inside designer.
     * During reproducing, all events should be posted to this component.
     * @see #convertCoords(java.awt.Component, java.awt.Point)
     * @see #convertCoords(java.awt.Component)
     * @return ComponentOperator for handle layer
     */
    public ComponentOperator handleLayer() {
        if(_handleLayer == null) {
            _handleLayer = createSubOperator(new HandleLayerChooser());
        }
        return(_handleLayer);
    }
    
    /** Return ContainerOperator for a component which contains all the designing components.
     * @see #findComponent(org.netbeans.jemmy.ComponentChooser, int)
     * @see #findComponent(org.netbeans.jemmy.ComponentChooser)
     * @see #findComponent(java.lang.Class, int)
     * @see #findComponent(java.lang.Class)
     * @return ContainerOperator for component layer
     */
    public ContainerOperator componentLayer() {
        if(_componentLayer == null) {
            _componentLayer = new ContainerOperator((Container)waitSubComponent(new ComponentLayerChooser()));
        }
        return(_componentLayer);
    }
    
    /** Returns ContainerOperator for component which represents designing form 
     * (like JFrame, JDialog, ...).
     * @return ContainerOperator for fake pane
     */
    public ContainerOperator fakePane() {
        if(_fakePane == null) {
            _fakePane = new ContainerOperator((Container)componentLayer().waitSubComponent(new FakePaneChooser()));
        }
        return(_fakePane);
    }

    /** Converts relative coordinates inside one of the components
     * laying on the designer to coordinates relative to handleLayer()
     * @see #handleLayer()
     * @see #componentLayer()
     * @see #findComponent(org.netbeans.jemmy.ComponentChooser, int)
     * @see #findComponent(org.netbeans.jemmy.ComponentChooser)
     * @see #findComponent(java.lang.Class, int)
     * @see #findComponent(java.lang.Class)
     * @param subComponent Component in designer.
     * @param localCoords Local <code>subComponent</code>'s coordinates
     * @return coordinates relative to handle layer
     */
    public Point convertCoords(Component subComponent, Point localCoords) {
        Point subLocation = subComponent.getLocationOnScreen();
        Point location = handleLayer().getLocationOnScreen();
        return(new Point(subLocation.x - location.x + localCoords.x,
                         subLocation.y - location.y + localCoords.y));
    }

    /** Converts components center coordinates
     * to coordinates relative to handleLayer()
     * @see #handleLayer()
     * @see #findComponent(org.netbeans.jemmy.ComponentChooser, int)
     * @see #findComponent(org.netbeans.jemmy.ComponentChooser)
     * @see #findComponent(java.lang.Class, int)
     * @see #findComponent(java.lang.Class)
     * @param subComponent Component in designer.
     * @return coordinates of the center of the subComponent relative to handle layer
     */
    public Point convertCoords(Component subComponent) {
        return(convertCoords(subComponent, new Point(subComponent.getWidth() / 2, 
                                                     subComponent.getHeight() / 2)));
    }

    /**
     * Clicks on component. Events are really sent to handleLayer()
     * @param subComponent Component in designer.
     * @param localCoords Local <code>subComponent</code>'s coordinates
     * @see #handleLayer()
     */
    public void clickOnComponent(Component subComponent, Point localCoords) {
        Point pointToClick = convertCoords(subComponent, localCoords);
        handleLayer().clickMouse(pointToClick.x, pointToClick.y, 1);
    }

    /**
     * Clicks on the component center. Events are really sent to handleLayer()
     * @param subComponent Component in designer.
     * @see #handleLayer()
     */
    public void clickOnComponent(Component subComponent) {
        Point pointToClick = convertCoords(subComponent);
        handleLayer().makeComponentVisible();
        handleLayer().clickMouse(pointToClick.x, pointToClick.y, 1);
    }

    /** Searches a component inside fakePane().
     * @see #fakePane()
     * @param chooser chooser specifying criteria to find a component
     * @param index index of component
     * @return index-th component from fake pane matching chooser's criteria
     */
    public Component findComponent(ComponentChooser chooser, int index) {
        return(fakePane().waitSubComponent(chooser, index));
    }
    
    /** Searches a component inside fakePane().
     * @see #fakePane()
     * @param chooser chooser specifying criteria to find a component
     * @return component from fake pane matching chooser's criteria
     */
    public Component findComponent(ComponentChooser chooser) {
        return(findComponent(chooser, 0));
    }
    
    /** Searches <code>index</code>'s instance of a <code>clzz</code> class inside fakePane().
     * @see #fakePane()
     * @param clzz class of component to be find (e.g. <code>JButton.class</code>)
     * @param index index of component
     * @return index-th component from fake pane of the given class
     */
    public Component findComponent(final Class clzz, int index) {
        return(findComponent(new ComponentChooser() {
                public boolean checkComponent(Component comp) {
                    return(clzz.isInstance(comp) &&
                           comp.isShowing());
                }
                public String getDescription() {
                    return("Any " + clzz.getName());
                }
            }, index));
    }

    /** Searches first instance of a <code>clzz</code> class inside fakePane().
     * @see #fakePane()
     * @param clzz class of component to be find (e.g. <code>JButton.class</code>)
     * @return first component from fake pane of the given class
     */
    public Component findComponent(Class clzz) {
        return(findComponent(clzz, 0));
    }

    private static class FormDesignerChooser implements ComponentChooser {
        public boolean checkComponent(Component comp) {
            if(comp.getClass().getName().equals("org.netbeans.modules.form.FormDesigner")) {
                return comp.isShowing();
            }
            return false;
        }
        public String getDescription() {
            return("Any FormDesigner");
        }
    }
    
    private static class HandleLayerChooser implements ComponentChooser {
        public boolean checkComponent(Component comp) {
            return(comp.getClass().getName().equals("org.netbeans.modules.form.HandleLayer"));
        }
        public String getDescription() {
            return("Any HandleLayer");
        }
    }
    
    private static class ComponentLayerChooser implements ComponentChooser {
        public boolean checkComponent(Component comp) {
            return(comp.getClass().getName().equals("org.netbeans.modules.form.ComponentLayer"));
        }
        public String getDescription() {
            return("Any ComponentLayer");
        }
    }
    
    private static class FakePaneChooser implements ComponentChooser {
        public boolean checkComponent(Component comp) {
            return(comp.getClass().getName().equals("org.netbeans.modules.form.fakepeer.FakePeerContainer"));
        }
        public String getDescription() {
            return("Any FakePeerContainer");
        }
    }
}
