/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.core;


import java.awt.Dimension;
import java.beans.BeanInfo;
import java.util.Iterator;
import java.util.ResourceBundle;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.border.CompoundBorder;

import org.openide.awt.Actions;
import org.openide.cookies.SaveCookie;
import org.openide.DialogDescriptor;
import org.openide.ErrorManager;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

/** Dialog which lets the user select which open files to close.
 *
 * @author  Ian Formanek, Petr Hrebejk
 */

public class ExitDialog extends JPanel implements java.awt.event.ActionListener {


    private static Object[] exitOptions;

    /** The dialog */
    private static java.awt.Dialog exitDialog;

    /** Result of the dialog */
    private static boolean result = false;

    JList list;
    DefaultListModel listModel;

    static final long serialVersionUID = 6039058107124767512L;

    /** Constructs new dlg */
    public ExitDialog () {
        setLayout (new java.awt.BorderLayout ());

        listModel = new DefaultListModel();
        Iterator iter = DataObject.getRegistry ().getModifiedSet ().iterator();
        while (iter.hasNext()) {
            DataObject obj = (DataObject) iter.next();
            listModel.addElement(obj);
        }
        draw ();
    }
    
    /** Constructs new dlg for unsaved files in filesystems marked 
     * for unmount.
    */
    public ExitDialog (Node[] activatedNodes) {
        setLayout (new java.awt.BorderLayout ());

        listModel = new DefaultListModel();
        Iterator iter = getModifiedActSet (activatedNodes).iterator();
        while (iter.hasNext()) {
            DataObject obj = (DataObject) iter.next();
            listModel.addElement(obj);
        }
        draw ();
    }

    /** Constructs rest of dialog.
    */
    private void draw () {
        list = new JList(listModel);
        list.setBorder(new EmptyBorder(2, 2, 2, 2));
        list.addListSelectionListener (new javax.swing.event.ListSelectionListener () {
                                           public void valueChanged (javax.swing.event.ListSelectionEvent evt) {
                                               updateSaveButton ();
                                           }
                                       }
                                      );
        // bugfix 37941, select first item in list
        if (!listModel.isEmpty ()) {
            list.setSelectedIndex (0);
        } else {                              
            updateSaveButton ();
        }
        JScrollPane scroll = new JScrollPane (list);
        scroll.setBorder (new CompoundBorder (new EmptyBorder (12, 12, 11, 0), scroll.getBorder ()));
        add(scroll, java.awt.BorderLayout.CENTER);
        list.setCellRenderer(new ExitDlgListCellRenderer());
        list.getAccessibleContext().setAccessibleName((NbBundle.getBundle(ExitDialog.class)).getString("ACSN_ListOfChangedFiles"));
        list.getAccessibleContext().setAccessibleDescription((NbBundle.getBundle(ExitDialog.class)).getString("ACSD_ListOfChangedFiles"));
        this.getAccessibleContext().setAccessibleDescription((NbBundle.getBundle(ExitDialog.class)).getString("ACSD_ExitDialog"));
    }
    
    private void updateSaveButton () {
        ((JButton)exitOptions [0]).setEnabled (list.getSelectedIndex () != -1);
    }

    /** @return preffered size */
    public Dimension getPreferredSize() {
        Dimension prev = super.getPreferredSize();
        return new Dimension(Math.max(300, prev.width), Math.max(150, prev.height));
    }

    /** This method is called when is any of buttons pressed
    */
    public void actionPerformed(final java.awt.event.ActionEvent evt ) {
        if (exitOptions[0].equals (evt.getSource ())) {
            save(false);
        } else if (exitOptions[1].equals (evt.getSource ())) {
            save(true);
        } else if (exitOptions[2].equals (evt.getSource ())) {
            theEnd();
        } else if (NotifyDescriptor.CANCEL_OPTION.equals (evt.getSource ())) {
            exitDialog.setVisible (false);
        }
    }

    /** Save the files from the listbox
    * @param all true- all files, false - just selected
    */
    private void save(boolean all) {
        Object array[] = ((all) ? listModel.toArray() : list.getSelectedValues());
        int i, count = ((array == null) ? 0 : array.length);
        int index = 0;	// index of last removed item

        for (i = 0; i < count; i++) {
            DataObject nextObject = (DataObject)array[i];
            index = listModel.indexOf(nextObject);
            save(nextObject);
        }

        if (listModel.isEmpty())
            theEnd();
        else {	// reset selection to new item at the same index if available
            if (index < 0)
                index = 0;
            else if (index > listModel.size() - 1) {
                index = listModel.size() - 1;
            }
            list.setSelectedIndex(index);
        }
    }

    /** Tries to save given data object using its save cookie.
     * Notifies user if excetions appear.
     */
    private void save (DataObject dataObject) {
        try {
            SaveCookie sc = (SaveCookie)dataObject.getCookie(SaveCookie.class);
            if (sc != null) {
                sc.save();
            }
            listModel.removeElement(dataObject);
        } catch (java.io.IOException exc) {
            ErrorManager em = ErrorManager.getDefault();
            Throwable t = em.annotate(
                exc, NbBundle.getBundle(ExitDialog.class).getString("EXC_Save")
            );
            em.notify(ErrorManager.EXCEPTION, t);
        }
    }
 
    /** Exit the IDE
    */
    private void theEnd() {
        // XXX(-ttran) result must be set before calling setVisible(false)
        // because this will unblock the thread which called Dialog.show()
        
        for (int i = listModel.size() - 1; i >= 0; i--) {            
            DataObject obj = (DataObject) listModel.getElementAt(i);
            obj.setModified(false);
        }

        result = true;
        exitDialog.setVisible (false);
        exitDialog.dispose();
    }

    /** Opens the ExitDialog for unsaved files in filesystems marked 
     * for unmount and blocks until it's closed. If dialog doesm't
     * exists it creates new one. Returns true if the IDE should be closed.
     */
    public static boolean showDialog(Node[] activatedNodes) {
        return innerShowDialog(activatedNodes);
    }
    
    /** Opens the ExitDialog and blocks until it's closed. If dialog doesm't
     * exists it creates new one. Returns true if the IDE should be closed.
     */
    public static boolean showDialog() {
        return innerShowDialog(null);        
    }

    /** Returns modified set of DataObjects in filesystems marked 
     * for unmount.
     */
    private static java.util.Set getModifiedActSet (Node[] activatedNodes) {
        Iterator iter = DataObject.getRegistry ().getModifiedSet ().iterator();
        java.util.Set set = new java.util.HashSet();
        while (iter.hasNext()) {
            DataObject obj = (DataObject) iter.next();
            try {
                FileSystem fs = obj.getPrimaryFile().getFileSystem();
                for (int i=0;i<activatedNodes.length;i++) {
                    DataFolder df = (DataFolder)activatedNodes[i].getCookie(DataFolder.class);
                    if (df != null)
                        if (df.getPrimaryFile().getFileSystem().equals(fs)) {
                            set.add(obj);
                            break;
                        }
                }
            } catch (FileStateInvalidException fe) {
            }
        }
        return set;
    }


    /** Opens the ExitDialog for activated nodes or for
     * whole repository.
     */
    private static boolean innerShowDialog(Node[] activatedNodes) {
        java.util.Set set = null;
        if (activatedNodes != null)
            set = getModifiedActSet (activatedNodes);
        else
            set = org.openide.loaders.DataObject.getRegistry ().getModifiedSet ();
        if (!set.isEmpty()) {

            // XXX(-ttran) caching this dialog is fatal.  If the user
            // cancels the Exit action, modifies some more files and tries to
            // Exit again the list of modified DataObject's is not updated,
            // changes made by the user after the first aborted Exit will be
            // lost.
            exitDialog = null;
            
            if (exitDialog == null) {
                ResourceBundle bundle = NbBundle.getBundle(ExitDialog.class);
                JButton buttonSave = new JButton();
                buttonSave.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_Save"));
                JButton buttonSaveAll = new JButton();
                buttonSaveAll.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_SaveAll"));
                JButton buttonDiscardAll = new JButton();
                buttonDiscardAll.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_DiscardAll"));

                // special handling to handle a button title with mnemonic 
                // and to allow enable/disable control of the option
                Actions.setMenuText(buttonSave, bundle.getString("CTL_Save"), true);
                Actions.setMenuText(buttonSaveAll, bundle.getString("CTL_SaveAll"), true);
                Actions.setMenuText(buttonDiscardAll, bundle.getString("CTL_DiscardAll"), true);

                exitOptions = new Object[] {
                                  buttonSave,
                                  buttonSaveAll,
                                  buttonDiscardAll,
                              };
                ExitDialog exitComponent = null;
                if (activatedNodes != null)
                    exitComponent = new ExitDialog (activatedNodes);
                else
                    exitComponent = new ExitDialog ();
                DialogDescriptor exitDlgDescriptor = new DialogDescriptor (
                                                         exitComponent,                                                   // inside component
                                                         bundle.getString("CTL_ExitTitle"), // title
                                                         true,                                                            // modal
                                                         exitOptions,                                                     // options
                                                         NotifyDescriptor.CANCEL_OPTION,                                        // initial value
                                                         DialogDescriptor.RIGHT_ALIGN,                                    // option align
                                                         new org.openide.util.HelpCtx (ExitDialog.class.getName () + ".dialog"), // HelpCtx // NOI18N
                                                         exitComponent                                                    // Action Listener
                                                     );
                exitDlgDescriptor.setAdditionalOptions (new Object[] {NotifyDescriptor.CANCEL_OPTION});
                exitDialog = org.openide.DialogDisplayer.getDefault ().createDialog (exitDlgDescriptor);
            }

            result = false;
            exitDialog.show(); // Show the modal Save dialog
            return result;

        }
        else
            return true;
    }

    /** Renderer used in list box of exit dialog
     */
    private class ExitDlgListCellRenderer extends JLabel implements ListCellRenderer {
        /** generated Serialized Version UID */
        static final long serialVersionUID = 1877692790854373689L;

        protected Border hasFocusBorder;
        protected Border noFocusBorder;

        public ExitDlgListCellRenderer() {
            this.setOpaque(true);
            this.setBorder(noFocusBorder);
            hasFocusBorder = new LineBorder(UIManager.getColor("List.focusCellHighlight")); // NOI18N
            noFocusBorder = new EmptyBorder(1, 1, 1, 1);
        }

        public java.awt.Component getListCellRendererComponent(JList list,
                Object value,            // value to display
                int index,               // cell index
                boolean isSelected,      // is the cell selected
                boolean cellHasFocus)    // the list and the cell have the focus
        {
            final DataObject obj = (DataObject)value;
            if (!obj.isValid()) {
                // #17059: it might be invalid already.
                // #18886: but if so, remove it later, otherwise BasicListUI gets confused.
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        listModel.removeElement(obj);
                    }
                });
                setText("");
                return this;
            }

            Node node = obj.getNodeDelegate();

            ImageIcon icon = new ImageIcon(node.getIcon(BeanInfo.ICON_COLOR_16x16));
            super.setIcon(icon);

            setText(node.getDisplayName());
            if (isSelected){
                this.setBackground(UIManager.getColor("List.selectionBackground")); // NOI18N
                this.setForeground(UIManager.getColor("List.selectionForeground")); // NOI18N
            }
            else {
                this.setBackground(list.getBackground());
                this.setForeground(list.getForeground());
            }

            this.setBorder(cellHasFocus ? hasFocusBorder : noFocusBorder);

            return this;
        }
    }
}
