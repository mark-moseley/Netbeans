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

package org.netbeans.modules.project.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.BeanInfo;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.ResourceBundle;
import java.util.Set;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.openide.DialogDescriptor;

import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.SaveCookie;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;


// XXX This code is stolen from core/ExitDialog.
/** Dialog which lets the user select which open files to close.
 *
 * @author  Ian Formanek, Petr Hrebejk
 */
final public class ExitDialog extends JPanel implements ActionListener {


    private static Object[] exitOptions;

    /** The dialog */
    private static Dialog exitDialog;

    /** Result of the dialog */
    private static boolean result = false;

    JList list;
    DefaultListModel listModel;

    /** Constructs new dlg for unsaved files in filesystems marked 
     * for unmount.
    */
    private ExitDialog (Set/*DataObject*/ openedFiles) {
        setLayout (new BorderLayout ());

        listModel = new DefaultListModel();
        
        Set set = getModifiedFiles (openedFiles);
        if (!set.isEmpty ()) {
            Iterator iter = set.iterator ();
            while (iter.hasNext ()) {
                DataObject obj = (DataObject) iter.next ();
                listModel.addElement(obj);
            }
            draw ();
        }
    }
    
    
    /** Constructs rest of dialog.
    */
    private void draw () {
        list = new JList(listModel);
        list.setBorder(new EmptyBorder(2, 2, 2, 2));
        list.addListSelectionListener (new ListSelectionListener () {
                                           public void valueChanged (ListSelectionEvent evt) {
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
        add(scroll, BorderLayout.CENTER);
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
    public void actionPerformed (final ActionEvent evt) {
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
        } catch (IOException exc) {
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
    public static boolean showDialog (Set/*DataObject*/ openedFiles) {
        return innerShowDialog (getModifiedFiles (openedFiles));
    }
    
    private static Set getModifiedFiles (Set/*DataObject*/ openedFiles) {
        Set set = new HashSet (openedFiles.size ());
        Iterator iter = openedFiles.iterator ();
        while (iter.hasNext()) {
            DataObject obj = (DataObject) iter.next ();
            EditorCookie ed = (EditorCookie)obj.getCookie (EditorCookie.class);
            if (ed != null && ed.isModified ()) {
                set.add (obj);
            }
        }
        return set;
    }


    /** Opens the ExitDialog for activated nodes or for
     * whole repository.
     */
    private static boolean innerShowDialog (Set/*DataObject*/ openedFiles) {
        if (!openedFiles.isEmpty()) {

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
                
                buttonSave.setText (bundle.getString("CTL_Save"));
                buttonSave.setMnemonic (bundle.getString ("CTL_Save_MNM").charAt (0));
                buttonSaveAll.setText (bundle.getString ("CTL_SaveAll"));
                buttonSaveAll.setMnemonic (bundle.getString ("CTL_SaveAll_MNM").charAt (0));
                buttonDiscardAll.setText (bundle.getString ("CTL_DiscardAll"));
                buttonDiscardAll.setMnemonic (bundle.getString ("CTL_DiscardAll_MNM").charAt (0));
                
                exitOptions = new Object[] {
                                  buttonSave,
                                  buttonSaveAll,
                                  buttonDiscardAll,
                              };
                ExitDialog exitComponent = null;
                exitComponent = new ExitDialog (openedFiles);
                DialogDescriptor exitDlgDescriptor = new DialogDescriptor (
                                                         exitComponent,                                                   // inside component
                                                         bundle.getString("CTL_ExitTitle"), // title
                                                         true,                                                            // modal
                                                         exitOptions,                                                     // options
                                                         NotifyDescriptor.CANCEL_OPTION,                                  // initial value
                                                         DialogDescriptor.RIGHT_ALIGN,                                    // option align
                                                         null,                                                            // no help
                                                         exitComponent                                                    // Action Listener
                                                     );
                exitDlgDescriptor.setAdditionalOptions (new Object[] {NotifyDescriptor.CANCEL_OPTION});
                exitDialog = DialogDisplayer.getDefault ().createDialog (exitDlgDescriptor);
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

        protected Border hasFocusBorder;
        protected Border noFocusBorder;

        public ExitDlgListCellRenderer() {
            this.setOpaque(true);
            this.setBorder(noFocusBorder);
            hasFocusBorder = new LineBorder(UIManager.getColor("List.focusCellHighlight")); // NOI18N
            noFocusBorder = new EmptyBorder(1, 1, 1, 1);
        }

        public Component getListCellRendererComponent (JList list, Object value, int index, boolean isSelected, boolean cellHasFocus)    // the list and the cell have the focus
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
