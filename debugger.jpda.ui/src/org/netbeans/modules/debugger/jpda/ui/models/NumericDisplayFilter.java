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

package org.netbeans.modules.debugger.jpda.ui.models;

import org.netbeans.spi.viewmodel.*;
import org.netbeans.api.debugger.jpda.*;
import org.openide.util.actions.Presenter;
import org.openide.util.NbBundle;

import javax.swing.*;
import java.util.*;
import java.awt.event.ActionEvent;

/**
 * Implements the "Display As Decimal/Hexadecimal/Octal/Binary/Char" 
 * option for numeric variables.
 * Provides the popup action and filters displayed values.
 *
 * @author Maros Sandor
 */
class NumericDisplayFilter implements TableModelFilter, 
NodeActionsProviderFilter {

    private final Map   variableToDisplaySettings;
    private String      columnID;
    private HashSet     listeners;

    /**
     *
     * @param columnID The column ID to filter, 
     * see {@link org.netbeans.spi.debugger.ui.Constants}
     */
    NumericDisplayFilter (String columnID) {
        this.columnID = columnID;
        variableToDisplaySettings = new HashMap (1);
    }

    public Object getValueAt (
        TableModel original, 
        Object node, 
        String columnID
    ) throws ComputingException, UnknownTypeException {
        if (columnID == this.columnID && 
            node instanceof Variable && 
            isIntegralType ((Variable) node)
        ) {
            Variable var = (Variable) node;
            return getValue (
                var, 
                (NumericDisplaySettings) variableToDisplaySettings.get (var)
            );
        }
        return original.getValueAt (node, columnID);
    }

    private Object getValue (Variable var, NumericDisplaySettings settings) {
        if (settings == null) return var.getValue ();
        String type = var.getType ();
        switch (settings.getDisplayAs ()) {
        case NumericDisplaySettings.DECIMAL:
            return var.getValue ();
        case NumericDisplaySettings.HEXADECIMAL:
            if (type.equals ("int"))
                return "0x" + Integer.toHexString (
                    Integer.parseInt (var.getValue ())
                );
            else
            if (type.equals ("short")) {
                String rv = "0x" + Integer.toHexString (
                    Short.parseShort (var.getValue ())
                );
                if (rv.length () > 6)
                    rv = rv.substring (0, 6);
                return rv;
            } else
                return "0x" + Long.toHexString (
                    Long.parseLong (var.getValue ())
                );
        case NumericDisplaySettings.OCTAL:
            if (type.equals ("int"))
                return "0" + Integer.toOctalString (
                    Integer.parseInt (var.getValue ())
                );
            else
            if (type.equals ("short"))
                return "0" + Integer.toOctalString (
                    Short.parseShort (var.getValue ())
                );
            else
                return "0" + Long.toOctalString (
                    Long.parseLong (var.getValue ())
                );
        case NumericDisplaySettings.BINARY:
            return Long.toBinaryString (Long.parseLong (var.getValue ()));
        case NumericDisplaySettings.CHAR:
            try {
                return "'" + new Character (
                    (char) Integer.parseInt (var.getValue ())
                ) + "'";
            } catch (Exception e) {
                return "?";
            }
        default:
            return var.getValue ();
        }
    }

    private boolean isIntegralType (Variable v) {
        String type = v.getType ();
        return type != null && 
            (type.equals ("int") || 
            type.equals ("char") || 
            type.equals ("long") || 
            type.equals ("short"));
    }

    public boolean isReadOnly (
        TableModel original, 
        Object node, 
        String columnID
    ) throws UnknownTypeException {
        return original.isReadOnly(node, columnID);
    }

    public void setValueAt (
        TableModel original, 
        Object node, 
        String columnID, 
        Object value
    ) throws UnknownTypeException {
        original.setValueAt(node, columnID, value);
    }

    public void perform(String action, Object node) {
    }

    public void performDefaultAction (
        NodeActionsProvider original, 
        Object node
    ) throws UnknownTypeException {
        original.performDefaultAction (node);
    }

    public Action[] getActions (
        NodeActionsProvider original, 
        Object node
    ) throws UnknownTypeException {
        if (!(node instanceof Variable)) return original.getActions(node);
        Action [] actions;
        try {
            actions = original.getActions(node);
        } catch (UnknownTypeException e) {
            actions = new Action[0];
        }
        List myActions = new ArrayList();
        if (node instanceof Variable) {
            Variable var = (Variable) node;
            if (isIntegralType(var)) {
                myActions.add(new DisplayAsAction((Variable) node));
            }
        }
        myActions.addAll(Arrays.asList(actions));
        return (Action[]) myActions.toArray(new Action[myActions.size()]);
    }

    private String localize(String s) {
        return NbBundle.getBundle(NumericDisplayFilter.class).getString(s);
    }

    private class DisplayAsAction extends AbstractAction 
    implements Presenter.Popup {

        private Variable variable;

        public DisplayAsAction(Variable variable) {
            this.variable = variable;
        }

        public void actionPerformed(ActionEvent e) {
        }

        public JMenuItem getPopupPresenter() {
            JMenu displayAsPopup = new JMenu(localize("CTL_Variable_DisplayAs_Popup"));

            JRadioButtonMenuItem decimalItem = new JRadioButtonMenuItem(new AbstractAction(localize("CTL_Variable_DisplayAs_Decimal")) {
                public void actionPerformed(ActionEvent e) {
                    onDisplayAs(NumericDisplaySettings.DECIMAL);
                }
            });
            JRadioButtonMenuItem hexadecimalItem = new JRadioButtonMenuItem(new AbstractAction(localize("CTL_Variable_DisplayAs_Hexadecimal")) {
                public void actionPerformed(ActionEvent e) {
                    onDisplayAs(NumericDisplaySettings.HEXADECIMAL);
                }
            });
            JRadioButtonMenuItem octalItem = new JRadioButtonMenuItem(new AbstractAction(localize("CTL_Variable_DisplayAs_Octal")) {
                public void actionPerformed(ActionEvent e) {
                    onDisplayAs(NumericDisplaySettings.OCTAL);
                }
            });
            JRadioButtonMenuItem binaryItem = new JRadioButtonMenuItem (new AbstractAction(localize("CTL_Variable_DisplayAs_Binary")) {
                public void actionPerformed(ActionEvent e) {
                    onDisplayAs(NumericDisplaySettings.BINARY);
                }
            });
            JRadioButtonMenuItem charItem = new JRadioButtonMenuItem (
                new AbstractAction (
                    localize ("CTL_Variable_DisplayAs_Character")
                ) {
                    public void actionPerformed (ActionEvent e) {
                        onDisplayAs (NumericDisplaySettings.CHAR);
                    }
                }
            );

            NumericDisplaySettings lds = (NumericDisplaySettings) 
                variableToDisplaySettings.get(variable);
            if (lds != null) {
                switch (lds.getDisplayAs ()) {
                case NumericDisplaySettings.DECIMAL:
                    decimalItem.setSelected (true);
                    break;
                case NumericDisplaySettings.HEXADECIMAL:
                    hexadecimalItem.setSelected (true);
                    break;
                case NumericDisplaySettings.OCTAL:
                    octalItem.setSelected (true);
                    break;
                case NumericDisplaySettings.BINARY:
                    binaryItem.setSelected (true);
                    break;
                case NumericDisplaySettings.CHAR:
                    charItem.setSelected (true);
                    break;
                }
            } else {
                decimalItem.setSelected (true);
            }

            displayAsPopup.add (decimalItem);
            displayAsPopup.add (hexadecimalItem);
            displayAsPopup.add (octalItem);
            displayAsPopup.add (binaryItem);
            displayAsPopup.add (charItem);
            return displayAsPopup;
        }

        private void onDisplayAs (int how) {
            NumericDisplaySettings lds = (NumericDisplaySettings) 
                variableToDisplaySettings.get (variable);
            if (lds == null) {
                lds = new NumericDisplaySettings 
                    (NumericDisplaySettings.DECIMAL);
            }
            if (lds.getDisplayAs () == how) return;
            variableToDisplaySettings.put 
                (variable, new NumericDisplaySettings (how));
            fireModelChanged ();
        }
    }


    private static class NumericDisplaySettings {

        public static final int DECIMAL        = 0;
        public static final int HEXADECIMAL    = 1;
        public static final int OCTAL          = 2;
        public static final int BINARY         = 3;
        public static final int CHAR           = 4;

        private int displayAs;

        public NumericDisplaySettings (int displayAs) {
            this.displayAs = displayAs;
        }

        public int getDisplayAs () {
            return displayAs;
        }
    }

    private void fireModelChanged() {
        if (listeners == null) return;
        for (Iterator i = listeners.iterator (); i.hasNext ();) {
            TreeModelListener listener = (TreeModelListener) i.next();
            listener.treeChanged ();
        }
    }

    public void addTreeModelListener (TreeModelListener l) {
        HashSet newListeners = (listeners == null) ? 
            new HashSet () : (HashSet) listeners.clone ();
        newListeners.add (l);
        listeners = newListeners;
    }

    public void removeTreeModelListener (TreeModelListener l) {
        if (listeners == null) return;
        HashSet newListeners = (HashSet) listeners.clone();
        newListeners.remove (l);
        listeners = newListeners;
    }
}
