/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.spi.options;

import java.lang.reflect.Method;


/**
 * Implementation of this class represents one category (like "Ant" 
 * or "Form Editor") in Miscellaneous Panel of Options Dialog. It should 
 * be registerred in layers:
 *
 * <pre style="background-color: rgb(255, 255, 153);">
 * &lt;folder name="OptionsDialog"&gt;
 *     &lt;folder name="Advanced"&gt;
 *         &lt;file name="FooAdvancedPanel.instance"&gt;
 *             &lt;attr name="instanceClass" stringvalue="org.foo.FooAdvancedPanel"/&gt;
 *         &lt;/file&gt;
 *     &lt;/file&gt;
 * &lt;/folder&gt;</pre>
 * 
 * Use standard way how to sort items registered in layers:
 * 
 * <pre style="background-color: rgb(255, 255, 153);">
 * &lt;attr name="GeneralPanel/Advanced" boolvalue="true"/&gt;
 * </pre>
 *
 * @see OptionsCategory
 * @see OptionsCategory.PanelController 
 * @author Jan Jancura
 */
public abstract class AdvancedOption {
    
    /**
     * Returns name of category used in Advanced Panel of 
     * Options Dialog.
     *
     * @return name of category
     */
    public abstract String getDisplayName ();
    
    /**
     * Returns tooltip to be used on category name.
     *
     * @return tooltip for this category
     */
    public abstract String getTooltip ();
    
    /**
     * Returns {@link PanelController} for this category. PanelController 
     * creates visual component to be used inside of Advanced Panel.
     *
     * @return new instance of PanelController for this advanced options 
     *         category
     */
    public abstract OptionsPanelController create ();

    //compatibility hack, see core/options/build.xml for more details:
    private OptionsCategory.PanelController createOldImpl () {
        return null;
    }
    
    private OptionsPanelController createNewImpl () {
        Class clazz = getClass();
        Method[] methods = clazz.getDeclaredMethods();
        
        for (int cntr = 0; cntr < methods.length; cntr++) {
            Method m = methods[cntr];
            
            if ("create".equals(m.getName()) && m.getReturnType() == OptionsCategory.PanelController.class) {
                try {
                    return (OptionsCategory.PanelController) m.invoke(this, new Object[0]);
                } catch (RuntimeException e) {
                    throw e;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
        
        return null;
    }
    
}
