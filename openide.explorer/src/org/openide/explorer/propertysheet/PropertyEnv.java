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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.openide.explorer.propertysheet;

import java.beans.FeatureDescriptor;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.beans.VetoableChangeSupport;


/**
 * PropertyEnv is a class which allows an object (such as a
 * Node.Property instance) to communicate hints to property
 * editor instances that affect the behavior or visual
 * rendering of the property in a <code>PropertySheet</code>
 * or <code>PropertyPanel</code> component.  An instance of
 * PropertyEnv is passed to the <code>attachEnv()</code>
 * method of property editors implementing ExPropertyEditor.
 * Such property editors may then call <code>
 * env.getFeatureDescriptor().get("someHintString")</code>
 * to retrieve hints that affect their behavior from the
 * Node.Property object whose properties they are displaying.
 * <p>
 * The other purpose of the of this interface is to 
 * enable communication between the {@link java.beans.PropertyEditor} and
 * the {@link PropertySheet} or {@link PropertyPanel}. 
 * The custom property editor can get into a state when the visual state 
 * does not represent valid value and in such case needs to disable OK button. 
 * To handle that the property editor has to keep reference to its associated
 * {@link PropertyEnv} and switch it into invalid state by calling 
 * <code>setState({@link PropertyEnv#STATE_INVALID})</code>
 * as soon as the content of the custom property editor is invalid and then
 * back by 
 * <code>setState({@link PropertyEnv#STATE_VALID})</code>
 * when the custom property editor becomes valid once again. As a reaction
 * to these calls the displayer of the custom property editor (for example
 * dialog with OK button) is supposed to disable and re-enable the button.
 * <p>
 * Also it can happen that the property editor is complex and cannot decide whether 
 * the value is valid quickly. Just knows that it needs to be validated.
 * For that purpose it wants to be informed when user presses the OK button and 
 * do the complex validation then. For that purpose there is the {@link PropertyEnv#STATE_NEEDS_VALIDATION}
 * state. The property editor needs to use it and attach a listener to 
 * be notified when the user presses the OK button:
 * <pre>
 * class Validate implements VetoableChangeListener {
 *   public void vetoableChange(VetoableChangeEvent ev) throws PropertyVetoException {
 *     if (PROP_STATE.equals(ev.getPropertyName())) {
 *       if (!doTheComplexValidationOk()) {
 *         throw new PropertyVetoException(...);
 *       }
 *     }
 *     // otherwise allow the switch to ok state
 *   }
 * }
 * Validate v = new Validate();
 * env.setState(STATE_NEEDS_VALIDATION);
 * env.addVetoableChangeListener(v);
 * </pre>
 * When the state is {@link PropertyEnv#STATE_NEEDS_VALIDATION} the OK button
 * of the surrounding dialog shall be enabled and when pressed the vetoable
 * listener notified about the expected change from 
 * {@link PropertyEnv#STATE_NEEDS_VALIDATION} to {@link PropertyEnv#STATE_VALID}
 * which can either be accepted or vetoed.
 *
 *
 * @author  dstrupl
 */
public class PropertyEnv {
    /** Name of the state property. */
    public static final String PROP_STATE = "state"; //NOI18N

    /**
     * One possible value for the setState/getState methods. With this
     * value the editor is in a valid state.
     */
    public static final Object STATE_VALID = "valid"; //NOI18N

    /**
     * One possible value for the setState/getState methods.
     * This one means that the editor does not know its state and
     * it has to validate it later.
     */
    public static final Object STATE_NEEDS_VALIDATION = "needs_validation"; //NOI18N

    /**
     * One possible value for the setState/getState methods. With this
     * one the editor is in invalid state (Ok button on custom editor
     * panel is disabled and an invalid glyph shown on the property panel).
     */
    public static final Object STATE_INVALID = "invalid"; // NOI18N
    static final String PROP_CHANGE_IMMEDIATE = "changeImmediate"; // NOI18N
    static final FeatureDescriptor dummyDescriptor = new FeatureDescriptor();

    /** The value returned from getFeatureDescriptor. */
    private FeatureDescriptor featureDescriptor = dummyDescriptor;

    /** The value returned from getBeans.*/
    private Object[] beans;

    /** Current state. */
    private Object state = STATE_VALID;

    /** The support is lazy initialized in getSupport. */
    private VetoableChangeSupport support;

    /** change support here */
    private PropertyChangeSupport change;

    /**
     * The value of this field is basically taken from
     * the property panel. The property panel is responsible
     * for propagating the value to this field by calling
     * setchangeImmediate.
     */
    private boolean changeImmediate = true;
    InplaceEditor.Factory factory = null;
    boolean editable = true;

    /** Default constructor has package access -
     * we do not want the instances to be created outside
     * our package.
     */
    PropertyEnv() {
    }

    /**
     * Array of beans that the edited property belongs to.
     */
    public Object[] getBeans() {
        return beans;
    }

    /**
     * Array of nodes that the edited property belongs to.
     */
    void setBeans(Object[] beans) {
        this.beans = beans;
    }

    /**
     * Feature descritor that describes the property. It is feature
     * descriptor so one can plug in PropertyDescritor and also Node.Property
     * which both inherit from FeatureDescriptor
     */
    public FeatureDescriptor getFeatureDescriptor() {
        return featureDescriptor;
    }

    /**
     * Feature descritor that describes the property. It is feature
     * descriptor so one can plug in PropertyDescritor and also Node.Property
     * which both inherit from FeatureDescriptor
     */
    void setFeatureDescriptor(FeatureDescriptor desc) {
        if (desc == null) {
            throw new IllegalArgumentException("Cannot set FeatureDescriptor to null."); //NOI18N
        }

        this.featureDescriptor = desc;

        if (featureDescriptor != null) {
            Object obj = featureDescriptor.getValue(PROP_CHANGE_IMMEDIATE);

            if (obj instanceof Boolean) {
                setChangeImmediate(((Boolean) obj).booleanValue());
            }
        }
    }

    // [PENDING]

    /** The editor may be able to edit properties of different classes. It can decide to
     * be able to edit descendants of a base class.*/

    //Class propertyClass; // read-only property

    /**
     * A setter that should be used by the property editor
     * to change the state of the environment.
     * Even the state property is bound, changes made from the editor itself
     * are allowed without restrictions.
     */
    public void setState(Object newState) {
        if (getState().equals(newState)) {
            // no change, no fire vetoable and property change
            return;
        }

        try {
            getSupport().fireVetoableChange(PROP_STATE, getState(), newState);
            state = newState;

            // always notify state change
            getChange().firePropertyChange(PROP_STATE, null, newState);
        } catch (PropertyVetoException pve) {
            // and notify the user that the change cannot happen
            PropertyDialogManager.notify(pve);
        }
    }

    /** Allow setting of state without triggering a dialog. */
    String silentlySetState(Object newState, Object newValue) {
        if (getState().equals(newState)) {
            // no change, no fire vetoable and property change
            return null;
        }

        try {
            getSupport().fireVetoableChange(PROP_STATE, getState(), newState);
            state = newState;

            // always notify state change
            getChange().firePropertyChange(PROP_STATE, null, newState);
        } catch (PropertyVetoException pve) {
            // and notify the user that the change cannot happen
            pve.printStackTrace();

            String name = (getFeatureDescriptor() == null) ? null : getFeatureDescriptor().getDisplayName();

            return PropUtils.findLocalizedMessage(pve, newValue, name);
        }

        return null;
    }

    /**
     * A getter for the current state of the environment.
     * @return one of the constants STATE_VALID, STATE_INVALID,
     * STATE_NEEDS_VALIDATION.
     */
    public Object getState() {
        return state;
    }

    /**
     * Vetoable change listener: listenning here you will be notified
     * when the state of the environment is being changed (when the setState
     * method is being called). You can veto the change and provide
     * a displayable information in the thrown exception. Use
     * the ErrorManager annotaion feature for the your exception to modify
     * the message and severity.
     */
    public void addVetoableChangeListener(VetoableChangeListener l) {
        getSupport().addVetoableChangeListener(l);
    }

    /**
     * Property change listener: listenning here you will be notified
     * when the state of the environment is has been changed.
     * @since 2.20
     */
    public void addPropertyChangeListener(PropertyChangeListener l) {
        getChange().addPropertyChangeListener(l);
    }

    /**
     * Vetoable change listener removal.
     */
    public void removeVetoableChangeListener(VetoableChangeListener l) {
        getSupport().removeVetoableChangeListener(l);
    }

    /**
     * Removes Property change listener.
     * @since 2.20
     */
    public void removePropertyChangeListener(PropertyChangeListener l) {
        getChange().removePropertyChangeListener(l);
    }

    /** Getter for property changeImmediate.
     * @return Value of property changeImmediate.
     */
    boolean isChangeImmediate() {
        return changeImmediate;
    }

    /** Setter for property changeImmediate.
     * @param changeImmediate New value of property changeImmediate.
     */
    void setChangeImmediate(boolean changeImmediate) {
        this.changeImmediate = changeImmediate;
    }

    /**
     * Lazy initialization of the VetoableChangeSupport.
     */
    private synchronized VetoableChangeSupport getSupport() {
        if (support == null) {
            support = new VetoableChangeSupport(this);
        }

        return support;
    }

    /**
     * Lazy initialization of the PropertyChangeSupport.
     */
    private synchronized PropertyChangeSupport getChange() {
        if (change == null) {
            change = new PropertyChangeSupport(this);
        }

        return change;
    }

    /**Register a factory for InplaceEditor instances that the property
     * sheet should use as an inline editor for the property.
     * This allows modules to supply custom inline editors globally
     * for a type.  It can be overridden on a property-by-property
     * basis for properties that supply a hint for an inplace editor
     * using <code>getValue(String)</code>.
     * @see org.openide.nodes.Node.Property
     * @see org.openide.explorer.propertysheet.InplaceEditor
     */
    public void registerInplaceEditorFactory(InplaceEditor.Factory factory) {
        this.factory = factory;
    }

    InplaceEditor getInplaceEditor() {
        InplaceEditor result;

        if (factory != null) {
            result = factory.getInplaceEditor();
        } else {
            result = null;
        }

        return result;
    }

    void setEditable(boolean editable) {
        this.editable = editable;
    }

    boolean isEditable() {
        return editable;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(getClass().getName());
        sb.append("@"); //NOI18N
        sb.append(System.identityHashCode(this));
        sb.append("[state="); //NOI18N
        sb.append(
            (state == STATE_NEEDS_VALIDATION) ? "STATE_NEEDS_VALIDATION"
                                              : ((state == STATE_INVALID) ? "STATE_INVALID" : "STATE_VALID")
        ); //NOI18N
        sb.append(", "); //NOI18N

        if (factory != null) {
            sb.append("InplaceEditorFactory=" + factory.getClass().getName()); //NOI18N
            sb.append(", "); //NOI18N
        }

        sb.append("editable="); //NOI18N
        sb.append(editable);
        sb.append(", isChangeImmediate="); //NOI18N
        sb.append(isChangeImmediate());
        sb.append(", featureDescriptor="); //NOI18N
        sb.append(getFeatureDescriptor().getDisplayName());

        return sb.toString();
    }
}
