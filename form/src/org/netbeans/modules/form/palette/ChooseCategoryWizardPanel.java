/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.form.palette;

import javax.swing.*;
import javax.swing.event.*;
import java.beans.*;

import org.openide.WizardDescriptor;
import org.openide.explorer.*;

/**
 * The third panel in the wizard for adding new components to the palette.
 * Lets the user choose the palette category where to add the selected
 * components.
 *
 * @author Tomas Pavek
 */

class ChooseCategoryWizardPanel implements WizardDescriptor.FinishablePanel {

    private CategorySelector categorySelector;

    private EventListenerList listenerList;

    // ----------
    // WizardDescriptor.Panel implementation

    public java.awt.Component getComponent() {
        if (categorySelector == null) { // create the UI component for the wizard step
            categorySelector = new CategorySelector();

            // wizard API: set the caption and index of this panel
            categorySelector.setName(PaletteUtils.getBundleString("CTL_SelectCategory_Caption")); // NOI18N
            categorySelector.putClientProperty("WizardPanel_contentSelectedIndex", // NOI18N
                                               new Integer(2));

            categorySelector.getExplorerManager().addPropertyChangeListener(
                new PropertyChangeListener() {
                    public void propertyChange(PropertyChangeEvent ev) {
                        if (ExplorerManager.PROP_SELECTED_NODES.equals(ev.getPropertyName()))
                            fireStateChanged();
                    }
                });
        }

        return categorySelector;
    }

    public org.openide.util.HelpCtx getHelp() {
        // PENDING
        return new org.openide.util.HelpCtx("beans.adding"); // NOI18N
    }

    public boolean isValid() {
        return categorySelector != null
               && categorySelector.getSelectedCategory() != null;
    }

    public void readSettings(Object settings) {
    }

    public void storeSettings(Object settings) {
        if (categorySelector != null)
            ((AddToPaletteWizard)settings).setSelectedCategory(categorySelector.getSelectedCategory());
    }

    public void addChangeListener(ChangeListener listener) {
        if (listenerList == null)
            listenerList = new EventListenerList();
        listenerList.add(ChangeListener.class, listener);
    }

    public void removeChangeListener(ChangeListener listener) {
        if (listenerList != null)
            listenerList.remove(ChangeListener.class, listener);
    }

    // WizardDescriptor.FinishablePanel implementation
    public boolean isFinishPanel() {
        return true;
    }

    // -----

    void fireStateChanged() {
        if (listenerList == null)
            return;

        ChangeEvent e = null;
        Object[] listeners = listenerList.getListenerList();
        for (int i=listeners.length-2; i >= 0; i-=2) {
            if (listeners[i] == ChangeListener.class) {
                if (e == null)
                    e = new ChangeEvent(this);
                ((ChangeListener)listeners[i+1]).stateChanged(e);
            }
        }
    }
}
