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
package org.netbeans.jellytools;

import java.awt.Component;

import java.lang.reflect.Method;

import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jellytools.actions.PaletteViewAction;

import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jemmy.operators.JListOperator;
import org.netbeans.jemmy.operators.JListOperator.ListItemChooser;

/**
 * Keeps methods to access component palette.
 * <p>
 * Usage:<br>
 * <pre>
        ComponentPaletteOperator cpo = new ComponentPaletteOperator();
        cpo.expand("Some Category Name");
        cpo.selectComponent("Some Component Name");
 * </pre>
 *
 * @author Jiri.Skrivanek@sun.com, mmirilovic@netbeans.org
 */
public class PaletteOperator extends TopComponentOperator {
    
    private static final PaletteViewAction paletteAction = new PaletteViewAction();
    
    // "Palette"
    private static final String PALETTE_TITLE = 
            Bundle.getString("org.netbeans.modules.palette.Bundle", "CTL_Component_palette");

    /** Waits for the Component Palette appearence and creates operator for it. */
    public PaletteOperator() {
        super(waitTopComponent(null, PALETTE_TITLE, 0, new PaletteTopComponentChooser()));
    }

    /** invokes palette and returns new instance of PaletteOperator
     * @return new instance of PaletteOperator */    
    public static PaletteOperator invoke() {
        paletteAction.perform();
        return new PaletteOperator();
    }
    
    //subcomponents
    
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
     * @see #expandAWT
     * @see #expandSwingContainers
     * @see #expandSwingControls
     * @see #expandSwingMenus
     * @see #expandSwingWindows
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

    /** Expands or collapses category.
     * @param categoryOper JCheckBoxOperator of components category
     * @param expand true to expand, false to collapse
     */
    public void expand(JCheckBoxOperator categoryOper, boolean expand) {
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
    }
}
