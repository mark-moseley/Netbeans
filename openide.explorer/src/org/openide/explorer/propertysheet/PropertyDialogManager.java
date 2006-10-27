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
package org.openide.explorer.propertysheet;

import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.explorer.propertysheet.editors.EnhancedCustomPropertyEditor;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

import java.awt.Component;
import java.awt.Window;
import java.awt.event.*;

import java.beans.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.UIManager;
import org.openide.awt.Mnemonics;
import org.openide.util.Exceptions;


/**
 * Helper dialog box manager for showing custom property editors.
 *
 * @author Jan Jancura, Dafe Simonek, David Strupl
 */
final class PropertyDialogManager implements VetoableChangeListener, ActionListener {
    /** Name of the custom property that can be passed in PropertyEnv. */
    private static final String PROPERTY_DESCRIPTION = "description"; // NOI18N
    private static Throwable doNotNotify;

    /* JST: Made package private because PropertyPanel should be used instead. */

    /** Listener to editor property changes. */
    private PropertyChangeListener listener;

    /** Cache for reverting on cancel. */
    private Object oldValue;

    /** Custom property editor. */
    private PropertyEditor editor;

    /** model of the edited property */
    private PropertyModel model;

    /** this is extracted from the model if possible, can be null */
    private Node.Property prop;

    /** Set true when property is changed. */
    private boolean changed = false;

    /** Given component stored for test on Enhance property ed. */
    private Component component;

    /** Dialog instance. */
    private Window dialog;

    /** Ok button can be enabled/disabled*/
    private JButton okButton;

    /** */
    private Runnable errorPerformer;
    private boolean okButtonState = true;
    private Object envStateBeforeDisplay = null;

    /** Environment passed to the property editor. */
    private PropertyEnv env;
    private Object lastValueFromEditor;
    private boolean cancelled = false;
    private boolean ok = false;
    private boolean reset = false;

    public PropertyDialogManager(
        String title, final boolean isModal, final PropertyEditor editor, PropertyModel model,
        final PropertyEnv env
    ) {
        this.editor = editor;

        if (env != null) {
            env.addVetoableChangeListener(this);
        }

        this.component = editor.getCustomEditor();

        if (component == null) {
            throw new NullPointerException(
                "Cannot create a dialog" + " for a null component. Offending property editor class:" +
                editor.getClass().getName()
            );
        }

        this.model = model;
        this.env = env;

        HelpCtx helpCtx = null;
        if (env != null) {
            Object helpID = env.getFeatureDescriptor().getValue(ExPropertyEditor.PROPERTY_HELP_ID);

            if ((helpID != null) && helpID instanceof String && (component != null) && component instanceof JComponent) {
                HelpCtx.setHelpIDString((JComponent) component, (String) helpID);
                helpCtx = new HelpCtx((String) helpID);
            }
        }

        //Docs say this works, but apparently hadn't worked for some time
        if (component instanceof JComponent) {
            Object compTitle = ((JComponent) component).getClientProperty("title");

            if (compTitle instanceof String) {
                title = (String) compTitle;
            }
        }

        // create dialog instance and initialize listeners
        createDialog(isModal, title, helpCtx);
        initializeListeners();
    }

    // public methods ............................................................

    /** Get the created dialog instance.
     * @return the dialog instance managed by this class.
     */
    public Window getDialog() {
        return dialog;
    }

    /**
     * PropertyDialogManager is attached to the PropertyEnv instance by
     * vetoableChangeListener.
     */
    public void vetoableChange(PropertyChangeEvent evt)
    throws PropertyVetoException {
        if ((env != null) && (PropertyEnv.PROP_STATE.equals(evt.getPropertyName()))) {
            okButtonState = evt.getNewValue() != PropertyEnv.STATE_INVALID;

            if (okButton != null) {
                okButton.setEnabled(okButtonState);
            }
        }
    }

    // other methods ............................................................

    /** Creates proper DialogDescriptor and obtain dialog instance
     * via DialogDisplayer.createDialog() call.
     */
    private void createDialog(boolean isModal, String title, HelpCtx helpCtx) {
        if (component instanceof Window) {
            // custom component is already a window --> just return it
            // from getDialog
            dialog = (Window) component;
            dialog.pack();

            return;
        }

        // prepare our options (buttons)
        boolean cannotWrite = false;
        boolean defaultValue = false;

        if (model instanceof ExPropertyModel) {
            FeatureDescriptor fd = ((ExPropertyModel) model).getFeatureDescriptor();

            if (fd instanceof Node.Property) {
                prop = (Node.Property) fd;
                cannotWrite = !prop.canWrite();
                defaultValue = PropUtils.shallBeRDVEnabled(prop);
            }
        }

        Object defaultOption;
        Object[] options;

        if ((editor == null) || (cannotWrite)) {
            JButton closeButton = new JButton(getString("CTL_Close"));
            closeButton.getAccessibleContext().setAccessibleDescription(getString("ACSD_CTL_Close"));
            options = new Object[] { closeButton };
            defaultOption = closeButton;
        } else {
            okButton = new JButton(getString("CTL_OK"));
            okButton.getAccessibleContext().setAccessibleDescription(getString("ACSD_CTL_OK"));

            JButton cancelButton = new JButton(getString("CTL_Cancel"));
            cancelButton.setVerifyInputWhenFocusTarget(false);
            cancelButton.getAccessibleContext().setAccessibleDescription(getString("ACSD_CTL_Cancel"));
            cancelButton.setDefaultCapable(false);

            if (defaultValue) {
                JButton defaultButton = new JButton();
                String name = getString("CTL_Default");
                Mnemonics.setLocalizedText(defaultButton, name);
                defaultButton.setActionCommand(name);
                defaultButton.getAccessibleContext().setAccessibleDescription(getString("ACSD_CTL_Default"));
                defaultButton.setDefaultCapable(false);
                defaultButton.setVerifyInputWhenFocusTarget(false);

                if ("Aqua".equals(UIManager.getLookAndFeel().getID())) {
                    //Support Aqua button ordering
                    options = new Object[] { defaultButton, cancelButton, okButton };
                } else {
                    options = new Object[] { okButton, defaultButton, cancelButton };
                }
            } else {
                if ("Aqua".equals(UIManager.getLookAndFeel().getID())) {
                    options = new Object[] { cancelButton, okButton };
                } else {
                    options = new Object[] { okButton, cancelButton };
                }
            }

            defaultOption = okButton;
        }

        if ((env != null) && (okButton != null)) {
            okButtonState = env.getState() != PropertyEnv.STATE_INVALID;

            if (okButton != null) {
                okButton.setEnabled(okButtonState);
            }
        }

        if (env != null) {
            envStateBeforeDisplay = env.getState();
        }

        
        // create dialog descriptor, create & return the dialog
        // bugfix #24998, set helpCtx obtain from PropertyEnv.getFeatureDescriptor()
        DialogDescriptor descriptor = new DialogDescriptor(component, title,
                isModal, options, defaultOption, DialogDescriptor.DEFAULT_ALIGN,
                helpCtx, this);
        dialog = DialogDisplayer.getDefault().createDialog(descriptor);
    }

    /** Initializes dialog listeners. Must be called after
     * createDialog method call. (dialog variable must not be null)
     */
    private void initializeListeners() {
        // dialog closing reactions
        dialog.addWindowListener(
            new WindowAdapter() {
                /** Ensure that values are reverted when user cancelles dialog
                 * by clicking on x image */
                public void windowClosing(WindowEvent e) {
                    if ((editor != null) && !(component instanceof Window)) {
                        // if someone provides a window (s)he has to handle
                        // the cancel him/herself
                        cancelValue();
                    }

                    // ensure that resources are released
                    if (env != null) {
                        env.removeVetoableChangeListener(PropertyDialogManager.this);
                    }

                    dialog.dispose();
                }

                /** Remove property listener on window close */
                public void windowClosed(WindowEvent e) {
                    if (component instanceof Window) {
                        // in this case we have to do similar thing as we do
                        // directly after the Ok button is pressed. The difference
                        // is in the fact that here we do not decide whether the
                        // dialog will be closed or not - it is simply being closed
                        // But we have to take care of propagating the value to
                        // the model
                        if (component instanceof EnhancedCustomPropertyEditor) {
                            try {
                                Object newValue = ((EnhancedCustomPropertyEditor) component).getPropertyValue();
                                model.setValue(newValue);
                            } catch (java.lang.reflect.InvocationTargetException ite) {
                                notifyUser(ite);
                            } catch (IllegalStateException ise) {
                                notifyUser(ise);
                            }
                        } else if ((env != null) && (!env.isChangeImmediate())) {
                            try {
                                model.setValue(lastValueFromEditor);
                            } catch (java.lang.reflect.InvocationTargetException ite) {
                                notifyUser(ite);
                            } catch (IllegalStateException ise) {
                                notifyUser(ise);
                            }
                        }
                    }

                    if (listener != null) {
                        editor.removePropertyChangeListener(listener);
                    }

                    dialog.removeWindowListener(this);
                }
            }
        );

        // reactions to editor property changes
        if (editor != null) {
            try {
                oldValue = model.getValue();
            } catch (Exception e) {
                // Ignored, there can be number of exceptions
                // when asking for old values...
            }

            lastValueFromEditor = editor.getValue();
            editor.addPropertyChangeListener(
                listener = new PropertyChangeListener() {
                        /** Notify displayer about property change in editor */
                        public void propertyChange(PropertyChangeEvent e) {
                            changed = true;
                            lastValueFromEditor = editor.getValue();

                            // enabling/disabling the okButton in response to
                            // firing PROP_VALUE_VALID --- usage of this firing
                            // has been deprecated in favor of directly calling
                            // PropertyEnv.setState(...)
                            if (ExPropertyEditor.PROP_VALUE_VALID.equals(e.getPropertyName())) {
                                if (okButton != null) {
                                    if (e.getNewValue() instanceof Boolean) {
                                        Boolean newButtonState = (Boolean) e.getNewValue();
                                        okButtonState = newButtonState.booleanValue();

                                        if (env != null) {
                                            env.setState(
                                                    okButtonState ? PropertyEnv.STATE_VALID : PropertyEnv.STATE_INVALID
                                                );
                                        } else {
                                            okButton.setEnabled(okButtonState);
                                        }

                                        if (e.getOldValue() instanceof Runnable) {
                                            errorPerformer = (Runnable) e.getOldValue();
                                        } else {
                                            errorPerformer = null;
                                        }
                                    }
                                }
                            }
                        }
                    }
            );

            if (component == null) {
                throw new NullPointerException(editor.getClass().getName() + " returned null from getCustomEditor()");
            }

            component.addPropertyChangeListener(
                listener = new PropertyChangeListener() {
                        /** forward possible help context change in custom editor */
                        public void propertyChange(PropertyChangeEvent e) {
                            if (DialogDescriptor.PROP_HELP_CTX.equals(e.getPropertyName())) {
                                if (dialog instanceof PropertyChangeListener) {
                                    ((PropertyChangeListener) dialog).propertyChange(e);
                                }
                            }
                        }
                    }
            );
        }
    }

    /**
     * Reverts to old values.
     */
    private void cancelValue() {
        if (
            (!changed) || (component instanceof EnhancedCustomPropertyEditor) ||
                ((env != null) && (!env.isChangeImmediate()))
        ) {
            if ((env != null) && (envStateBeforeDisplay != null)) {
                env.setState(envStateBeforeDisplay);
            }

            return;
        }

        try {
            model.setValue(oldValue);
        } catch (Exception e) {
            // Ignored, there can be number of exceptions
            // when asking for old values...
        }
    }

    public boolean wasCancelled() {
        return cancelled;
    }

    public boolean wasOK() {
        return ok;
    }

    public boolean wasReset() {
        return reset;
    }

    /** Called when user presses a button on some option (button) in the
     * dialog.
     * @param evt The button press event.
     */
    public void actionPerformed(ActionEvent evt) {
        String label = evt.getActionCommand();
        if (label.equals(getString("CTL_Cancel"))) {
            cancelled = true; // XXX shouldn't this be reset otherwise?
            cancelValue();
        }

        ok = false;
        reset = false;

        if (label.equals(getString("CTL_Default"))) {
            reset = true;

            if (prop != null) {
                try {
                    prop.restoreDefaultValue();
                } catch (IllegalAccessException iae) {
                    notifyUser(iae);
                } catch (java.lang.reflect.InvocationTargetException ite) {
                    notifyUser(ite);
                }
            }
        }

        if (label.equals(getString("CTL_OK"))) {
            ok = true;

            if ((env != null) && (env.getState() == PropertyEnv.STATE_NEEDS_VALIDATION)) {
                env.setState(PropertyEnv.STATE_VALID);

                if (env.getState() != PropertyEnv.STATE_VALID) {
                    // if the change was vetoed do nothing and return
                    return;
                }
            }

            if (component instanceof EnhancedCustomPropertyEditor) {
                try {
                    Object newValue = ((EnhancedCustomPropertyEditor) component).getPropertyValue();
                    model.setValue(newValue);
                } catch (java.lang.reflect.InvocationTargetException ite) {
                    notifyUser(ite);
                    return;
                } catch (IllegalStateException ise) {
                    notifyUser(ise);
                    return;
                } catch (IllegalArgumentException iae) {
                    notifyUser(iae);
                    return;
                }
            } else if ((env != null) && (!env.isChangeImmediate())) {
                try {
                    model.setValue(lastValueFromEditor);
                } catch (java.lang.reflect.InvocationTargetException ite) {
                    notifyUser(ite);
                    return;
                } catch (IllegalStateException ise) {
                    notifyUser(ise);
                    return;
                }
            }

            // this is an old hack allowing to notify a cached value
            // obtained via propertyChangeEvent from the editor
            if (!okButtonState) {
                if (errorPerformer != null) {
                    errorPerformer.run();
                }

                return;
            }
        }

        // close the dialog
        changed = false;

        if (env != null) {
            env.removeVetoableChangeListener(this);
        }

        dialog.dispose();
    }

    private static String getString(String key) {
        return NbBundle.getBundle(PropertyDialogManager.class).getString(key);
    }

    /** For testing purposes we need to _not_ notify some exceptions.
     * That is why here is a package private method to register an exception
     * that should not be fired.
     */
    static void doNotNotify(Throwable ex) {
        doNotNotify = ex;
    }

    /** Notifies an exception to error manager or prints its it to stderr.
     * @param ex exception to notify
     */
    static void notify(Throwable ex) {
        Throwable d = doNotNotify;
        doNotNotify = null;

        if (d == ex) {
            return;
        }

        Exceptions.printStackTrace(ex);
    }

    /**
     * Tries to find an annotation with localized message(primarily) or general
     * message in all exception annotations. If the annotation with the message
     * is found it is notified with original severity. If the message is not
     * found the exception is notified with informational severity.
     *
     * @param e exception to notify
     */
    private static void notifyUser(Exception e) {
        String userMessage = Exceptions.findLocalizedMessage(e);

        if (userMessage != null) {
            Exceptions.printStackTrace(e);
        } else {
            // if there is not user message don't bother user, just log an
            // exception
            Logger.global.log(Level.INFO, null, e);
        }
    }

    Component getComponent() {
        return component;
    }

    PropertyEditor getEditor() {
        return editor;
    }

}
