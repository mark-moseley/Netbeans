/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.jellytools.modules.form;

import java.awt.Component;
import java.lang.reflect.Method;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jemmy.operators.JListOperator;
import org.netbeans.jemmy.operators.JListOperator.ListItemChooser;

/**
 * Keeps methods to access component palette of form editor.
 * <p>
 * Usage:<br>
 * <pre>
        ComponentPaletteOperator cpo = new ComponentPaletteOperator();
        cpo.expandAWT();
        cpo.selectComponent("Label");
 * </pre>
 *
 * @author Jiri.Skrivanek@sun.com
 */
public class ComponentPaletteOperator extends TopComponentOperator {
    
    private JCheckBoxOperator _cbSwingContainers;
    private JCheckBoxOperator _cbSwingControls;
    private JCheckBoxOperator _cbSwingMenus;
    private JCheckBoxOperator _cbSwingWindows;
    private JCheckBoxOperator _cbAWT;
    private JCheckBoxOperator _cbBeans;
    // "Palette"
    private static final String PALETTE_TITLE = 
            Bundle.getString("org.netbeans.modules.palette.Bundle", "CTL_Component_palette");

    /** Waits for the Component Palette appearence and creates operator for it.
     */
    public ComponentPaletteOperator() {
        super(waitTopComponent(null, PALETTE_TITLE, 0, new PaletteTopComponentChooser()));
    }

    //subcomponents
    
    /** Waits for "Swing Containers" check box button.
     * @return JCheckBoxOperator instance
     */
    public JCheckBoxOperator cbSwingContainers() {
        if(_cbSwingContainers == null) {
            _cbSwingContainers = new JCheckBoxOperator(
                                    this,
                                    Bundle.getString("org.netbeans.modules.form.resources.Bundle",
                                                     "FormDesignerPalette/SwingContainers"));  // NOI18N
        }
        return _cbSwingContainers;
    }
        
    /** Waits for ""Swing Controls check box button.
     * @return JCheckBoxOperator instance
     */
    public JCheckBoxOperator cbSwingControls() {
        if(_cbSwingControls == null) {
            _cbSwingControls = new JCheckBoxOperator(
                                    this,
                                    Bundle.getString("org.netbeans.modules.form.resources.Bundle",
                                                     "FormDesignerPalette/SwingControls"));  // NOI18N
        }
        return _cbSwingControls;
    }

    /** Waits for "Swing Menus" check box button.
     * @return JCheckBoxOperator instance
     */
    public JCheckBoxOperator cbSwingMenus() {
        if(_cbSwingMenus == null) {
            _cbSwingMenus = new JCheckBoxOperator(
                                    this,
                                    Bundle.getString("org.netbeans.modules.form.resources.Bundle",
                                                     "FormDesignerPalette/SwingMenus"));  // NOI18N
        }
        return _cbSwingMenus;
    }

    /** Waits for "Swing Windows" check box button.
     * @return JCheckBoxOperator instance
     */
    public JCheckBoxOperator cbSwingWindows() {
        if(_cbSwingWindows == null) {
            _cbSwingWindows = new JCheckBoxOperator(
                                    this,
                                    Bundle.getString("org.netbeans.modules.form.resources.Bundle",
                                                     "FormDesignerPalette/SwingWindows"));  // NOI18N
        }
        return _cbSwingWindows;
    }

    /** Waits for "AWT" check box button.
     * @return JCheckBoxOperator instance
     */
    public JCheckBoxOperator cbAWT() {
        if(_cbAWT == null) {
            _cbAWT = new JCheckBoxOperator(
                                    this, 
                                    Bundle.getString("org.netbeans.modules.form.resources.Bundle",
                                                     "FormDesignerPalette/AWT"));  // NOI18N
        }
        return _cbAWT;
    }
    
    /** Waits for "Beans" check box button.
     * @return JCheckBoxOperator instance
     */
    public JCheckBoxOperator cbBeans() {
        if(_cbBeans == null) {
            _cbBeans = new JCheckBoxOperator(
                                    this, 
                                    Bundle.getString("org.netbeans.modules.form.resources.Bundle",
                                                     "FormDesignerPalette/Beans"));  // NOI18N
        }
        return _cbBeans;
    }

    /** Getter for the component types list.
     * List really looks like a toolbar here.
     * @return JListOperator instance of a palette
     */
    public JListOperator lstComponents() {
        int i = 0;
        JListOperator jlo = new JListOperator(this, i++);
        // find only list which has size greater then 0
        while(jlo.getModel().getSize() == 0 && i < 10) {
            jlo = new JListOperator(this, i++);
        }
        return jlo;
    }

    //common
    
    /** Select a component in expanded category of components. Use one of
     * expand methods before using this method.
     * @param displayName display name of component to be selected (e.g. Button)
     * @see #expandBeans
     * @see #expandSwing
     * @see #expandAWT
     */
    public void selectComponent(final String displayName) {
        int index = lstComponents().findItemIndex(new ListItemChooser() {
            public boolean checkItem(JListOperator oper, int index) {
                try {
                    // call method org.netbeans.modules.palette.DefaultItem#getDisplayName
                    Object item = oper.getModel().getElementAt(index);
                    Method getDisplayNameMethod = item.getClass().getMethod("getDisplayName", new Class[] {}); // NOI18N
                    getDisplayNameMethod.setAccessible(true);
                    String indexDisplayName = (String)getDisplayNameMethod.invoke(item, new Object[] {});
                    return oper.getComparator().equals(indexDisplayName, displayName);
                } catch (Exception e) {
                    throw new JemmyException("getDisplayName failed.", e); // NOI18N
                }
            }
            public String getDescription() {
                return "display name equals "+displayName; // NOI18N
            }
        });
        lstComponents().selectItem(index);
    }

    //shortcuts

    /** Expands Swing Containers and collapses all others. */
    public void expandSwingContainers() {
        collapseSwingControls();
        collapseSwingMenus();
        collapseSwingWindows();
        collapseAWT();
        collapseBeans();
        expand(cbSwingContainers(), true);
    }
    
    /** Expands Swing Controls and collapses all others. */
    public void expandSwingControls() {
        collapseSwingContainers();
        collapseSwingMenus();
        collapseSwingWindows();
        collapseAWT();
        collapseBeans();
        expand(cbSwingControls(), true);
    }

    /** Expands Swing Menus and collapses all others. */
    public void expandSwingMenus() {
        collapseSwingContainers();
        collapseSwingControls();
        collapseSwingWindows();
        collapseAWT();
        collapseBeans();
        expand(cbSwingMenus(), true);
    }
    /** Expands Swing Windows and collapses all others. */
    public void expandSwingWindows() {
        collapseSwingContainers();
        collapseSwingControls();
        collapseSwingMenus();
        collapseAWT();
        collapseBeans();
        expand(cbSwingWindows(), true);
    }

    /** Expands AWT components palette and collapses all others. */
    public void expandAWT() {
        collapseSwingContainers();
        collapseSwingControls();
        collapseSwingMenus();
        collapseSwingWindows();
        collapseBeans();
        expand(cbAWT(), true);
    }
    
    /** Expands Beans components palette and collapses all others. */
    public void expandBeans() {
        collapseSwingContainers();
        collapseSwingControls();
        collapseSwingMenus();
        collapseSwingWindows();
        collapseAWT();
        expand(cbBeans(), true);
    }

    /** Collapses Swing Containers palette. */
    public void collapseSwingContainers() {
        expand(cbSwingContainers(), false);
    }

    /** Collapses Swing Controls palette. */
    public void collapseSwingControls() {
        expand(cbSwingControls(), false);
    }
 
    /** Collapses Swing Menus palette. */
    public void collapseSwingMenus() {
        expand(cbSwingMenus(), false);
    }

    /** Collapses Swing Windows palette. */
    public void collapseSwingWindows() {
        expand(cbSwingWindows(), false);
    }

    /** Collapses AWT components palette. */
    public void collapseAWT() {
        expand(cbAWT(), false);
    }
    
    /** Collapses Beans components palette. */
    public void collapseBeans() {
        expand(cbBeans(), false);
    }

    /** Expands or collapses category.
     * @param categoryOper JCheckBoxOperator of components category
     * @param expand true to expand, false to collapse
     */
    private void expand(JCheckBoxOperator categoryOper, boolean expand) {
        if(categoryOper.isSelected() != expand) {
            categoryOper.push();
            categoryOper.waitSelected(expand);
        }
    }

    private static class PaletteTopComponentChooser implements ComponentChooser {
        public boolean checkComponent(Component comp) {
            return(comp.getClass().getName().equals("org.netbeans.spi.palette.PaletteTopComponent"));
        }
        public String getDescription() {
            return("Any PaletteTopComponent");
        }
    }

    /** Performs verification by accessing all sub-components */    
    public void verify() {
        lstComponents();
        cbSwingContainers();
        cbSwingControls();
        cbSwingMenus();
        cbSwingWindows();
        cbAWT();
        cbBeans();
    }
}
