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

package org.netbeans.modules.form.editors;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.lang.reflect.Modifier;
import java.util.StringTokenizer;
import javax.swing.JPanel;
import org.openide.ErrorManager;
import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.util.NbBundle;

/** Property editors for java modifiers.
*
* @author Petr Hamernik
*/
public class ModifierEditor extends JPanel implements ExPropertyEditor {

    public static final String PROP_MODIFIERS = "modifiers"; // NOI18N
    /**
     * attribute name for the feature descriptor of the property environment
     * @see #ACCESS_MODIFIERS_CUSTOM_EDITOR
     * @see #OTHERS_MODIFIERS_CUSTOM_EDITOR
     */ 
    public static final String CUSTOM_EDITOR_TYPE = "customEditorType"; // NOI18N
    /** if it is set as feature descriptor's attribute value in the environment then the getCustomComponent returns 
     * the combo box for access modifiers
     */
    public static final Integer ACCESS_MODIFIERS_CUSTOM_EDITOR = new Integer(0);
    /** if it is set as feature descriptor's attribute value in the environment then the getCustomComponent returns
     * the panel containing other modifiers than the access modifiers. 
     */
    public static final Integer OTHERS_MODIFIERS_CUSTOM_EDITOR = new Integer(1);
    /** if it is set as feature descriptor's attribute value in the environment then the getCustomComponent returns
     * the panel containing full range of modifiers 
     */
    public static final Integer FULL_CUSTOM_EDITOR = new Integer(2);

    /** Instance of custom property editor - visual panel. */
    private ModifierPanel panel;

    /** Current mask */
    private int mask;

    /** Current value */
    private int modifier;
    
    private PropertyEnv env;
    
    /**
     * @see #getType
     */ 
    private Object type;
    
    /** Creates new modifiers editor with full mask.
    */
    public ModifierEditor() {
        this(ModifierPanel.EDITABLE_MASK);
    }

    /** Creates new modifiers editor.
    * @param mask The mask of modifier values which should be possible to change.
    */
    public ModifierEditor(int mask) {
        modifier = 0;
        setMask(mask & ModifierPanel.EDITABLE_MASK);
    }
    
    private Component customComponent;
    
    public void addNotify() {
        setLayout(new BorderLayout());
        panel = new ModifierPanel(this);
        Object type = getType();
        if (ACCESS_MODIFIERS_CUSTOM_EDITOR == type) {
            customComponent = panel.getAccessComponent();
        } else if (OTHERS_MODIFIERS_CUSTOM_EDITOR == type) {
            customComponent = panel.getModifiersComponent();
        } else {
            customComponent = panel.getCompactComponent();
        }
        add(customComponent, BorderLayout.CENTER);
        
        super.addNotify();
    }

    public void removeNotify() {
        super.removeNotify();
        if (panel != null) {
            remove(customComponent);
            panel = null;
        }
    }
    
    /** Getter for property mask.
     *@return Value of property mask.
     */
    int getMask() {
        return mask;
    }

    /** Set the mask of editable modifiers.
     * @param mask new value of the mask.
     */
    public void setMask(int mask) {
        if (this.mask != mask) {
            int oldMask = this.mask;
            this.mask = mask & ModifierPanel.EDITABLE_MASK;
            firePropertyChange (ModifierPanel.PROP_MASK, new Integer (oldMask), new Integer (mask));
            setModifier(modifier & mask);
        }
    }
    
    /** Getter for property modifier.
     *@return Value of property modifier.
     */
    int getModifier() {
        return modifier;
    }

    /** Setter for property modifier.
     *@param modifier New value of property modifier.
     */
    void setModifier(int modifier) {
        if (this.modifier != modifier) {
            int oldModifier = this.modifier;
            this.modifier = modifier;
            // for our panel
            firePropertyChange (ModifierPanel.PROP_MODIFIER, new Integer (oldModifier), new Integer (modifier));
            // for the outside world
            firePropertyChange(PROP_MODIFIERS, new Integer (oldModifier), new Integer (modifier));
        }
    }
    
    /**
     * @return type of the editor
     * @see #ACCESS_MODIFIERS_CUSTOM_EDITOR
     * @see #OTHERS_MODIFIERS_CUSTOM_EDITOR
     * @see #FULL_CUSTOM_EDITOR
     */ 
    Object getType() {
        return type;
    }

    /** Set new value */
    public void setValue(Object object) throws IllegalArgumentException {
        if (object == null) {
            setModifier(0);
            return;
        }
        if (object instanceof Integer) {
            setModifier(((Integer) object).intValue());
        }
        else {
            throw new IllegalArgumentException();
        }
    }

    /** @return the java source code representation
    * of the current value.
    */
    public String getJavaInitializationString() {
        return new Integer(getModifier()).toString();
    }

    /** Get the value */
    public Object getValue() {
        return new Integer(getModifier());
    }

    /** @return <CODE>false</CODE> */
    public boolean isPaintable() {
        return false;
    }

    /** Does nothing. */
    public void paintValue(Graphics g, Rectangle rectangle) {
    }

    /** @return textual representition of current value of the modifiers. */
    public String getAsText() {
        return Modifier.toString(getModifier());
    }

    /** Parse the text and sets the modifier editor value */
    public void setAsText(String string) throws IllegalArgumentException {
        int newValue = 0;
        int oldValue = modifier;

        StringTokenizer tukac = new StringTokenizer(string, ", ", false); // NOI18N
        while (tukac.hasMoreTokens()) {
            String token = tukac.nextToken();
            boolean known = false;
            for (int i = 0; i < ModifierPanel.MODIFIER_COUNT; i++) {
                if ((ModifierPanel.MODIFIER_VALUES[i] & mask) != 0) {
                    if (token.equals(ModifierPanel.MODIFIER_NAMES[i])) {
                        if (((ModifierPanel.MODIFIER_VALUES[i] == Modifier.FINAL) && ((newValue & Modifier.ABSTRACT) != 0)) ||
                                ((ModifierPanel.MODIFIER_VALUES[i] == Modifier.ABSTRACT) && ((newValue & Modifier.FINAL) != 0)))
                            break;
                        newValue |= ModifierPanel.MODIFIER_VALUES[i];
                        known = true;
                        break;
                    }
                }
            }
            if ((newValue & ModifierPanel.ACCESS_MASK) == 0) {
                for (int i = 1; i <= 3; i++) {
                    if ((ModifierPanel.ACCESS_VALUES[i] & mask) != 0) {
                        if (token.equals(ModifierPanel.ACCESS_NAMES[i])) {
                            newValue |= ModifierPanel.ACCESS_VALUES[i];
                            known = true;
                            break;
                        }
                    }
                }
            }
            if (!known) {
                IllegalArgumentException x = new IllegalArgumentException(
                    "Invalid modifier: " + token); // NOI18N
                String message = java.text.MessageFormat.format(
                    getString("MSG_IllegalModifierString"), // NOI18N
                    new Object[] { token });
                ErrorManager.getDefault().annotate(x,
			ErrorManager.USER, null, message, null, null);
                throw x;
            }
        }
        if (oldValue != newValue) {
            modifier = newValue;
            firePropertyChange(ModifierPanel.PROP_MODIFIER,
                    new Integer(oldValue), new Integer(modifier));
        }
    }

    /** @return <CODE>null</CODE> */
    public String[] getTags() {
        return null;
    }

    /** @return <CODE>this</CODE> */
    public Component getCustomEditor() {
        return this;
    }
    
    /** @return <CODE>true</CODE> */
    public boolean supportsCustomEditor() {
        return true;
    }

    /** Get the customized property value.
     * @return the property value
     * @exception IllegalStateException when the custom property editor does not contain a valid property value
     *           (and thus it should not be set)
     */
    public Object getPropertyValue() throws IllegalStateException {
        return getValue();
    }

    /**
     * This method is called by the IDE to pass
     * the environment to the property editor.
     */
    public void attachEnv(PropertyEnv env) {
        this.env = env;
        type = env.getFeatureDescriptor().getValue(CUSTOM_EDITOR_TYPE);
        if (type == null) {
            type = FULL_CUSTOM_EDITOR;
        } else if (ACCESS_MODIFIERS_CUSTOM_EDITOR.equals(type)) {
            type = ACCESS_MODIFIERS_CUSTOM_EDITOR;
        } else if (OTHERS_MODIFIERS_CUSTOM_EDITOR.equals(type)) {
            type = OTHERS_MODIFIERS_CUSTOM_EDITOR;
        } else {
            type = FULL_CUSTOM_EDITOR;
        }
        
    }
     
    private static String getString(String key) {
        return NbBundle.getMessage(ModifierEditor.class, key);
    }
}
