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


package org.netbeans.modules.form;

/**
 * FormDesignValue interface gives a way how to use special property
 * values that holds some additional or design specific information.
 * Objects implementing FormDesignValue are specially supported by
 * properties (@see FormProperty) in the form editor. An instance of
 * FormDesignValue can be set as a property value - but it is not set to
 * the real object (target bean) directly - some "design value" is derived
 * from it first. Method getDesignValue() is defined for this purpose.
 * The value returned  from getDesignValue() is used on the real instance
 * of the bean during design-time, while the object implementing
 * FormDesignValue will be used for persistence and for code generation.
 *
 * Various property editors may provide values implementing FormDesignValue.
 * For example, an internationalization string editor can work with
 * values holding the key of internationalized string and returning the 
 * content of the key (from a Bundle.properties file) as the "design value" 
 * (from getDesignValue() method). Such an editor can be used then on any
 * property of type String.
 *
 * @author Ian Formanek
 */
public interface FormDesignValue extends java.io.Serializable {

    /** A special value indicating (when returned from getDesignValue())
     * that no real value is available during design-time for this object.
     * @see #getDesignValue
     */
    public static final Object IGNORED_VALUE = new Object();

    static final long serialVersionUID =5993614134339828170L;

    /** Provides a value which should be used during design-time
     * as the real value of a property on the bean instance.
     * E.g. the ResourceBundle String would provide the real value
     * of the String from the resource bundle, so that the design-time
     * representation reflects the real code being generated.
     * @return the real property value to be used during design-time
     */
    public Object getDesignValue();
    
    public Object getDesignValue(Object target);

    /** Returns description of the design value. Can be useful when
     * the real value for design-time is not provided.
     */
    public String getDescription();

    /**
     * Returns a new FormDesignValue instance which should, as appropriate, 
     * correspond to the target form property.
     *
     * @param targetFormProperty the FormProperty the copied value will be set to
     * @return the new FormDesignValue instance or null if it's impossible to copy 
     *         the particular subtype; may also return the represented real value
     */
    public Object copy(FormProperty targetFormProperty);
    
    //
    // In the future, some methods for handling persistence
    // will be probably added here.
    //

//    /** Extended version of FormDesignValue which supports listening on
//     * changes of the design value. */
//    public interface Listener extends FormDesignValue {
//        static final long serialVersionUID =7127443991708952900L;
//
//        /** Attaches specified listener to the design value.
//         * The change event is fired whenever the design value (accessible
//         * via getDesignValue() method call) changes.
//         * @param listener the change listener to add
//         */
//        public void addChangeListener(javax.swing.event.ChangeListener listener);
//
//        /** Deattaches specified listener from the design value.
//         * @param listener the change listener to remove
//         */
//        public void removeChangeListener(javax.swing.event.ChangeListener listener);
//    }
}
