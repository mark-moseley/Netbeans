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

package org.netbeans.modules.form;

import java.beans.*;
import org.openide.nodes.Node;
import org.openide.explorer.propertysheet.editors.NodePropertyEditor;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.explorer.propertysheet.ExPropertyEditor;

/** 
 * An interface for working with "context" of properties
 * (of FormProperty type). The interface has three methods.
 * First:
 *     boolean useMultipleEditors();
 * describes whether the FormPropertyEditor can be used for editing properties.
 * This property editor encapsulates multiple property editors which can be used
 * for given property - this feature is not suitable e.g. for event properties,
 * and sometimes not possible beacuase of restrictions in XML storage format
 * (which must stay compatible with previous versions).
 *
 * Second:
 *     void initPropertyEditor(PropertyEditor prEd);
 * initializes property editor for a property - beacause property editors
 * are usually constructed with no parameters, but often needs some
 * context (e.g. FormAwareEditor needs FormModel).
 *
 * Third:
 *     FormModel getFormModel();
 * provides the form the property belongs to. The context is needed for loading
 * classes of property editors (from the right classpath).
 *
 * @author Tomas Pavek
 */

public interface FormPropertyContext {

    public boolean useMultipleEditors();

    public void initPropertyEditor(PropertyEditor prEd);

    public FormModel getFormModel();

    /** 
     * Support for default implementation of FormPropertyContext interface.
     * One FormModel and one Node instances are needed for now.
     * Currently FormAwareEditor and NodePropertyEditor are recognized
     * and initialized. It may change in the future...
     * To use this class, implement methods getFormModel() and getNode().
     */
    public static abstract class DefaultSupport implements FormPropertyContext {

        public boolean useMultipleEditors() {
            FormModel formModel = getFormModel();
            return formModel != null;
        }
        
        public void initPropertyEditor(PropertyEditor prEd) {
            FormModel formModel = getFormModel();

            if (formModel != null && prEd instanceof FormAwareEditor)
                ((FormAwareEditor)prEd).setFormModel(formModel);
        }
    }

    /** Defualt implementation of FormPropertyContext interface. */
    public static class DefaultImpl extends DefaultSupport {

        FormModel formModel;

        public DefaultImpl(FormModel form) {
            formModel = form;
        }

        public FormModel getFormModel() {
            return formModel;
        }
    }

    /** "Empty" implementation of FormPropertyContext. */
    public static class EmptyImpl implements FormPropertyContext {

        public boolean useMultipleEditors() {
            return false;
        }

        public void initPropertyEditor(PropertyEditor prEd) {
        }

        public FormModel getFormModel() {
            return null;
        }

        // ------

        public static EmptyImpl getInstance() {
            if (theInstance == null)
                theInstance = new EmptyImpl();
            return theInstance;
        }

        static private EmptyImpl theInstance = null;
    }
}
