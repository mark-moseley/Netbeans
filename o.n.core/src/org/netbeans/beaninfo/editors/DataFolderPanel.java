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

package org.netbeans.beaninfo.editors;

import java.awt.event.KeyEvent;
import java.io.IOException;
import java.lang.ref.*;
import java.util.StringTokenizer;
import java.beans.*;
import java.util.Enumeration;
import java.text.MessageFormat;

import javax.swing.tree.TreeSelectionModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.ListSelectionModel;
import javax.swing.JButton;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import org.openide.DialogDescriptor;
import org.openide.ErrorManager;
import org.openide.WizardDescriptor;
import org.openide.loaders.*;
import org.openide.nodes.*;
import org.openide.explorer.view.*;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.propertysheet.editors.EnhancedCustomPropertyEditor;
import org.openide.filesystems.*;
import org.openide.util.UserCancelException;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.enum.*;
import org.openide.windows.TopComponent;

/**
 * A panel for selecting an existing data folder. 
 * @author  Jaroslav Tulach, David Strupl
 * @version 
 */
class DataFolderPanel extends TopComponent implements
                    DocumentListener, DataFilter, EnhancedCustomPropertyEditor,
                    PropertyChangeListener, VetoableChangeListener {

    /** prefered dimmension of the panels */
    static java.awt.Dimension PREF_DIM = new java.awt.Dimension (450, 250);
                    
    /** format to for default package */
    private static MessageFormat defaultFolderName;

    /** listener to changes in the panel */
    private ChangeListener listener;

    /** system reference (FileSystem) */
    private Reference  system = new WeakReference (null);

    /** root node */
    private Node rootNode;

    /** last DataFolder object that can be returned */
    private DataFolder df;

    /** */
    private DataFolderEditor editor;
    
    private static final String PATH_TOKEN_DELIMITER = "/" + java.io.File.separatorChar; // NOI18N
    
    private String last_suggestion = "";
    
    public DataFolderPanel(DataFolderEditor ed) {
        this();
        editor = ed;
    }
    
    /** Creates new form DataFolderPanel */
    public DataFolderPanel() {
        initComponents ();

        setName (getString("LAB_TargetLocationPanelName"));

        setBorder (new javax.swing.border.EmptyBorder(new java.awt.Insets(12, 12, 0, 11)));
        /*
        packagesPanel.setBorder (new javax.swing.border.CompoundBorder(
                                     new javax.swing.border.TitledBorder(getString("LAB_SelectPackageBorder")),
                                     new javax.swing.border.EmptyBorder(new java.awt.Insets(8, 8, 8, 8)))
                                );
         */

        rootNode = createPackagesNode ();

        packagesPanel.getExplorerManager ().setRootContext (rootNode);
        packagesPanel.getExplorerManager ().addPropertyChangeListener (this);
        packagesPanel.getExplorerManager ().addVetoableChangeListener (this);

        // registers itself to listen to changes in the content of document
        packageName.getDocument().addDocumentListener(this);
        packageName.unregisterKeyboardAction(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0));
        
        descriptionLabel.setDisplayedMnemonic(getString("LAB_TargetLocationDescription_mnemonic").charAt(0));
        packageLabel.setDisplayedMnemonic(getString("LAB_package_mnemonic").charAt(0));
        dirLabel.setDisplayedMnemonic(getString("LAB_directory_mnemonic").charAt(0));
        createButton.setMnemonic(getString("CTL_Create_mnemonic").charAt(0));
        
        beanTreeView.getAccessibleContext().setAccessibleDescription(getString("ACSD_DataFolderTree"));
        packageName.getAccessibleContext().setAccessibleDescription(getString("ACSD_package"));
        directoryName.getAccessibleContext().setAccessibleDescription(getString("ACSD_directory"));
        createButton.getAccessibleContext().setAccessibleDescription(getString("ACSD_Create"));
        getAccessibleContext().setAccessibleDescription(getString("ACSD_DataFolderPanel"));
    }

    /** Preffered size */
    public java.awt.Dimension getPreferredSize() {
        return PREF_DIM;
    }

    /** Request focus.
    */
    public void requestFocus () {
        // TODO: set the focus
        // used to be :
//        className.requestFocus();
//        className.selectAll ();
    }

    /** Creates node that displays all packages.
    */
    private Node createPackagesNode () {
        return RepositoryNodeFactory.getDefault().repository(this);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the FormEditor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        packagesPanel = new org.openide.explorer.ExplorerPanel();
        beanTreeView = new org.openide.explorer.view.BeanTreeView();
        descriptionLabel = new javax.swing.JLabel();
        packageLabel = new javax.swing.JLabel();
        packageName = new javax.swing.JTextField();
        dirLabel = new javax.swing.JLabel();
        directoryName = new javax.swing.JTextField();
        createButton = new javax.swing.JButton();
        
        setLayout(new java.awt.BorderLayout());
        
        packagesPanel.setLayout(new java.awt.GridBagLayout());
        java.awt.GridBagConstraints gridBagConstraints1;
        
        beanTreeView.setPopupAllowed(false);
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.gridx = 0;
        gridBagConstraints1.gridy = 1;
        gridBagConstraints1.gridwidth = 3;
        gridBagConstraints1.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints1.insets = new java.awt.Insets(0, 0, 5, 0);
        gridBagConstraints1.weightx = 1.0;
        gridBagConstraints1.weighty = 1.0;
        packagesPanel.add(beanTreeView, gridBagConstraints1);
        
        descriptionLabel.setText(java.util.ResourceBundle.getBundle("org/netbeans/beaninfo/editors/Bundle").getString("LAB_TargetLocationDescription"));
        descriptionLabel.setLabelFor(beanTreeView);
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.gridx = 0;
        gridBagConstraints1.gridy = 0;
        gridBagConstraints1.gridwidth = 3;
        gridBagConstraints1.insets = new java.awt.Insets(0, 0, 2, 0);
        gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
        packagesPanel.add(descriptionLabel, gridBagConstraints1);
        
        packageLabel.setText(java.util.ResourceBundle.getBundle("org/netbeans/beaninfo/editors/Bundle").getString("LAB_package"));
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.gridx = 0;
        gridBagConstraints1.gridy = 2;
        gridBagConstraints1.insets = new java.awt.Insets(0, 0, 5, 12);
        gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
        packagesPanel.add(packageLabel, gridBagConstraints1);
        
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.gridx = 1;
        gridBagConstraints1.gridy = 2;
        gridBagConstraints1.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints1.insets = new java.awt.Insets(0, 0, 5, 5);
        gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints1.weightx = 1.0;
        packagesPanel.add(packageName, gridBagConstraints1);
        
        dirLabel.setText(java.util.ResourceBundle.getBundle("org/netbeans/beaninfo/editors/Bundle").getString("LAB_directory"));
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.gridx = 0;
        gridBagConstraints1.gridy = 3;
        gridBagConstraints1.insets = new java.awt.Insets(0, 0, 0, 12);
        gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
        packagesPanel.add(dirLabel, gridBagConstraints1);
        
        directoryName.setEnabled(false);
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.gridx = 1;
        gridBagConstraints1.gridy = 3;
        gridBagConstraints1.gridwidth = 2;
        gridBagConstraints1.fill = java.awt.GridBagConstraints.HORIZONTAL;
        packagesPanel.add(directoryName, gridBagConstraints1);
        
        createButton.setText(java.util.ResourceBundle.getBundle("org/netbeans/beaninfo/editors/Bundle").getString("CTL_Create"));
        createButton.setEnabled(false);
        createButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                createButtonActionPerformed(evt);
            }
        });
        
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.gridx = 2;
        gridBagConstraints1.gridy = 2;
        gridBagConstraints1.insets = new java.awt.Insets(0, 0, 5, 0);
        gridBagConstraints1.anchor = java.awt.GridBagConstraints.NORTHWEST;
        packagesPanel.add(createButton, gridBagConstraints1);
        
        add(packagesPanel, java.awt.BorderLayout.CENTER);
        
    }//GEN-END:initComponents

  private void createButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_createButtonActionPerformed
        try {
            // create the folder
            final DataFolder newDf = (DataFolder)getPropertyValue();
            // TODO: this line does not work - because the Node is not there yet
            setTargetFolder(newDf);
            updateDirectory ();
            updatePropertyEditor();
            enableCreateButton();
        } catch (IllegalStateException ex) {
              throw new RuntimeException(ex.getMessage());
        }
  }//GEN-LAST:event_createButtonActionPerformed


  // Variables declaration - do not modify//GEN-BEGIN:variables
  private org.openide.explorer.ExplorerPanel packagesPanel;
  private org.openide.explorer.view.BeanTreeView beanTreeView;
  private javax.swing.JLabel descriptionLabel;
  private javax.swing.JLabel packageLabel;
  private javax.swing.JTextField packageName;
  private javax.swing.JLabel dirLabel;
  private javax.swing.JTextField directoryName;
  private javax.swing.JButton createButton;
  // End of variables declaration//GEN-END:variables

  private static final java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/beaninfo/editors/Bundle"); // NOI18N

    //
    // Filter to accept only folders
    //

    /** Should the data object be displayed or not?
    * @param obj the data object
    * @return <CODE>true</CODE> if the object should be displayed,
    *    <CODE>false</CODE> otherwise
    */
    public boolean acceptDataObject(DataObject obj) {
        return obj instanceof DataFolder;
    }

    /** Allow only simple selection.
    */
    public void vetoableChange(PropertyChangeEvent ev)
    throws PropertyVetoException {
        if (ExplorerManager.PROP_SELECTED_NODES.equals (ev.getPropertyName ())) {
            Node[] arr = (Node[])ev.getNewValue();

            if (arr.length > 1) {
                throw new PropertyVetoException ("Only single selection allowed", ev); // NOI18N
            }
        }
    }

    /** Changes in selected node in packages.
    */
    public void propertyChange (PropertyChangeEvent ev) {
        if (ExplorerManager.PROP_SELECTED_NODES.equals (ev.getPropertyName ())) {
            Node[] arr = packagesPanel.getExplorerManager ().getSelectedNodes ();
            if (!isVisible()) {
                // in the case we are not shown don't update the panel's state
                return;
            }
            if (arr.length == 1) {
                DataFolder df = (DataFolder)arr[0].getCookie (DataFolder.class);
                if (df != null) {
                    setTargetFolder (df);
                    updatePropertyEditor();
                    enableCreateButton();
                    return;
                }
            }
            setTargetFolder ((DataFolder)null);
        }
    }

    /** Fires info to listener.
    */
    private void fireStateChanged () {
        if (listener != null) {
            listener.stateChanged (new ChangeEvent (this));
        }
    }

    //
    // Modification of package name
    //

    public void changedUpdate(final javax.swing.event.DocumentEvent p1) {
        if (p1.getDocument () == packageName.getDocument ()) {
            SwingUtilities.invokeLater (new Runnable () {
                                            public void run () {
                                                String text = packageName.getText ();
                                                if (text != null) {
                                                    if (isValid()) {
                                                        setTargetFolder (text, false);
                                                        updatePropertyEditor();
                                                    }
                                                    updateDirectory ();
                                                }
                                                enableCreateButton();
                                            }
                                        });
            return;
        }
    }

    public void removeUpdate(final javax.swing.event.DocumentEvent p1) {
        // when deleted => do no looking for folder
        // changedUpdate (p1);
        if (p1.getDocument () == packageName.getDocument ()) {
            SwingUtilities.invokeLater(new Runnable () {
                                            public void run () {
                                                if (packageName.getText ().length () == 0) {
                                                    FileSystem fs = (FileSystem)system.get ();
                                                    if (fs != null) {
                                                        DataFolder df = DataFolder.findFolder (fs.getRoot ());
                                                        setTargetFolder (df);
                                                        packageName.selectAll ();
                                                    }
                                                }
                                                String text = packageName.getText ();
                                                if (text != null) {
                                                    if (isValid()) {
                                                        setTargetFolder (text, true);
                                                        updatePropertyEditor();
                                                   }
                                                   updateDirectory ();
                                               }
                                               enableCreateButton();
                                            }
                                        });
        }
    }
    
    public void insertUpdate(final javax.swing.event.DocumentEvent p1) {
        changedUpdate (p1);
    }


    /** Help for this panel.
    * @return the help or <code>null</code> if no help is supplied
    */
    public org.openide.util.HelpCtx getHelp () {
        return new HelpCtx (DataFolderPanel.class);
    }

    /** Test whether the panel is finished and it is safe to proceed to the next one.
    * If the panel is valid, the "Next" (or "Finish") button will be enabled.
    * @return <code>true</code> if the user has entered satisfactory information
    */
    public boolean isValid () {
        String text = packageName.getText ();
        if (text.length () == 0) {
            Node[] arr = packagesPanel.getExplorerManager ().getSelectedNodes ();
            if (arr.length == 1 && arr[0] == rootNode) {
                return false;
            }
        }

        return true;
    }

    /** Add a listener to changes of the panel's validity.
    * @param l the listener to add
    * @see #isValid
    */
    public void addChangeListener (ChangeListener l) {
        if (listener != null) throw new IllegalStateException ();

        listener = l;
    }

    /** Remove a listener to changes of the panel's validity.
    * @param l the listener to remove
    */
    public void removeChangeListener (ChangeListener l) {
        listener = null;
    }

    /** Computes a suggestion for a given prefix and
    * a list of file objects.
    *
    * @param node the node to start with
    * @param pref prefix
    * @param first [0] is the first node that satisfies the suggestion
    * @return the longest continuation string for all folders that 
    *    starts with prefix
    */
    private static String computeSuggestion (
        Node node,
        String pref,
        Node[] first
    ) {
        Node[] arr = node.getChildren ().getNodes ();

        String match = null;

        for (int i = 0; i < arr.length; i++) {
            String name = arr[i].getName ();
            if (name.startsWith (pref)) {
                // ok, has the right prefix
                if (match == null) {
                    // first match
                    match = name;
                    if (first != null) {
                        first[0] = arr[i];
                    }
                } else {
                    // find common part of the names
                    int indx = pref.length ();
                    int end = Math.min (name.length (), match.length ());
                    while (indx < end && match.charAt (indx) == name.charAt (indx)) {
                        indx++;
                    }
                    match = match.substring (0, indx);
                }
            }
        }

        if (match == null ) {  // why? || match.length () == pref.length ()) {
            return null;
        } else {
            return match.substring (pref.length ());
        }
    }


    /** Presets a target folder.
    * @param f the folder
    * @return true if succeeded
    */
    boolean setTargetFolder (final DataFolder f) {
        boolean exact;
        Node n;
        String name;
        
        df = f;
        
        if (f != null) {
            FileObject fo = f.getPrimaryFile ();
            name = fo.getPath();

            StringTokenizer st = new StringTokenizer (name, PATH_TOKEN_DELIMITER);
            try {
                FileSystem fs = fo.getFileSystem ();

                if (fo.isRoot ()) {
                    // Fix 8492
                    //name = ""; // NOI18N
                    name = packageName.getText().trim();
                }

                system = new WeakReference (fs);

                Enumeration en = new SequenceEnumeration (
                                     new SingletonEnumeration (fs.getSystemName()),
                                     st
                                 );

                n = NodeOp.findPath (rootNode, en);
                exact = true;
            } catch (FileStateInvalidException ex) {
                // invalid state of file system => back to root
                n = rootNode;
                name = ""; // NOI18N
                exact = false;
            } catch (NodeNotFoundException ex) {
                n = ex.getClosestNode();
                DataFolder df = (DataFolder)n.getCookie (DataFolder.class);
                if (df != null) {
                    name = df.getPrimaryFile ().getPath ();
                } else {
                    name = ""; // NO-I18N // NOI18N
                }
                exact = false;
            }


        } else {
            // null folder => use root
            n = rootNode;
            name = null;
            exact = true;
        }

        // remove listener + do change + add listener
        ExplorerManager em = packagesPanel.getExplorerManager ();
        em.removePropertyChangeListener (this);
        packageName.getDocument ().removeDocumentListener (this);

        try {
            em.setSelectedNodes (new Node[] { n });
        } catch (PropertyVetoException ex) {
            throw new InternalError ();
        }

        packageName.setText (name);
        updateDirectory ();

        packageName.getDocument ().addDocumentListener (this);
        em.addPropertyChangeListener (this);

        fireStateChanged ();

        return exact;
    }
    
    /** Getter for target folder. If the folder does not
    * exists it is created at this point.
    * @param create true if the target folder should be created.
    * @return the target folder
    * @exception IOException if the possible creation of the folder fails
    */
    private DataFolder getTargetFolder(boolean create) throws IOException {
        if (create && isValid()) {
            FileSystem fs = (FileSystem)system.get ();
            if (fs != null) {
                DataFolder folder = DataFolder.findFolder (fs.getRoot ());
                String currentName = packageName.getText();
                if (currentName.length () > 0) {
                    folder = DataFolder.create (folder, currentName);
                }
                df = folder;
                return folder;
            }
        }
        return df;
    }

    /** Presets a target folder.
    * @param f the name of target folder
    * @return true if succeeded
    */
    private boolean setTargetFolder (final String f, boolean afterDelete) {
        Node n = null;
        NodeNotFoundException closest = null;

        // first of all test the currently selected nod
        // for location of closest
        java.util.Collection selected = new java.util.HashSet ();
        Node[] nodes = packagesPanel.getExplorerManager().getSelectedNodes();
        for ( int i = 0; i < nodes.length; i++ ) {
            Node n1 = nodes[i];
            if ( n1.getParentNode() == null ) {
                continue;
            }
            while ( n1.getParentNode().getParentNode() != null )
                n1 = n1.getParentNode();
            selected.add( n1 );
        }

        // scan
        Node[] arr = rootNode.getChildren ().getNodes ();

        for (int i = 0; i < arr.length; i++) {
            Node root = arr[i];

            StringTokenizer st = new StringTokenizer (f, PATH_TOKEN_DELIMITER);

            try {
                n = NodeOp.findPath (root, st);
                break;
            } catch (NodeNotFoundException ex) {
                if (!st.hasMoreElements ()) {
                    // a test for !hasMoreElements is here to be sure that
                    // all tokens has been read, so only the last item
                    // has not been found

                    // check whether we can continue from the nod
                    final String sugg = computeSuggestion (
                                            ex.getClosestNode (),
                                            ex.getMissingChildName(),
                                            null
                                        );

                    if ( ( closest == null || selected.contains (root) ) && sugg != null ) {
                        // if we can go on and there has been no suggestion o
                        // this is the current filesystem => go o
                        closest = ex;
                    } 
                }
            }
        }

        if (n != null) {
            // closest node not used
            closest = null;
        } else {

            if (closest == null) {
                // the node has not been even found
                return false;
            }

            // we will select the closest node found - old version
            n = closest.getClosestNode ();

            // new - try to build shadow nodes hierarchy
            
        }

        // remove listener + do change + add listener
        ExplorerManager em = packagesPanel.getExplorerManager ();
        em.removePropertyChangeListener (this);

        // change the text if we want to add suggestion
        if (closest != null) {
            Node[] first = new Node[1];
            String sugg = computeSuggestion (
                                    closest.getClosestNode (),
                                    closest.getMissingChildName(),
                                    first
                                );

            if ( afterDelete && sugg != null && sugg.equals( last_suggestion ) )
                sugg = null;
            
            last_suggestion = sugg;
            if (sugg != null) {
                packageName.getDocument ().removeDocumentListener (
                    DataFolderPanel.this
                );

                packageName.setText (f + sugg);
                updateDirectory ();

                javax.swing.text.Caret c = packageName.getCaret ();
                c.setDot (f.length () + sugg.length ());
                c.moveDot (f.length ());

                packageName.getDocument ().addDocumentListener (
                    DataFolderPanel.this
                );
            }

            if (first[0] != null) {
                // show the first node that fits
                n = first[0];
            }
        }


        // change the node
        try {
            em.setSelectedNodes(new Node[] { n });
            //beanTreeView.selectionChanged(new Node[] { n }, em);
        } catch (PropertyVetoException ex) {
            throw new InternalError ();
        }

        // change the selected filesystem
        df = (DataFolder)n.getCookie (DataFolder.class);
        if (df != null) {
            try {
                FileSystem fs = df.getPrimaryFile ().getFileSystem ();
                system = new WeakReference (fs);
            } catch (FileStateInvalidException ex) {
            }
        }


        em.addPropertyChangeListener (this);

        fireStateChanged ();

        return closest == null;
    }

    /** Creates default package name for given file system.
    * @param fs the file system
    * @return localized name of default package
    */
    private static String defaultFolderName (FileSystem fs) {
        if (defaultFolderName == null) {
            defaultFolderName = new MessageFormat(
                getString ("FMT_TemplateDefaultFolderName")
            );
        }

        String n = fs == null ? "" : fs.getDisplayName (); // NOI18N

        return defaultFolderName.format (new Object[] { n });
    }

    /** Updates directory name
    */
    private void updateDirectory () {
        StringBuffer sb = new StringBuffer ();
        FileSystem fs = (FileSystem)system.get ();
        if (fs != null) {
            sb.append (fs.getDisplayName ());
        }
        String name = packageName.getText ();
        if (name.equals (defaultFolderName (fs))) {
            name = ""; // NOI18N
        }
        if (name.length () > 0) {
            sb.append (java.io.File.separatorChar);
            sb.append (name.replace ('/', java.io.File.separatorChar));
        }
        directoryName.setText (sb.toString ());
    }

    /** Updates associated editor by calling setDataFolder(...) . */
    private void updatePropertyEditor() {
        if (editor != null) {
            try {
                DataFolder newF = getTargetFolder(false);
                String name = newF.getPrimaryFile ().getPath ();
                if (name.equals(packageName.getText())) {
                    editor.setDataFolder(df);
                } else {
                    editor.setDataFolder(null);
                }
            } catch (IOException ex) {
                 ErrorManager.getDefault().notify(ex);
            }
        }
    }
    
    /** Sets the state of the createButton */
    private void enableCreateButton() {
        String name = null;
        if (df != null) {
            name = df.getPrimaryFile ().getPath ();
        } else {
            name = ""; // NOI18N
        }
        if (name.equals(packageName.getText())) {
            // nothing to create
            createButton.setEnabled(false);
        } else {
            createButton.setEnabled(isValid());
        }
    }
        
    
    /** Get the customized property value.
     * @return the property value
     * @exception InvalidStateException when the custom property editor does not contain a valid property value
     *           (and thus it should not be set)
     */
    public Object getPropertyValue() throws IllegalStateException {
        if (isValid()) {
            DataFolder old = df;
            try {
                df = getTargetFolder(true);
                return df;
            } catch (IOException x) {
                ErrorManager.getDefault().notify(x);
                throw new IllegalStateException();
            }
        } else {
            throw new IllegalStateException();
        }
    }
    public static class ShadowDirNode extends AbstractNode {
        public ShadowDirNode(Children children) {
            super(children);
        }
    } 

    public static class ShadowLeafNode extends AbstractNode {
        public ShadowLeafNode() {
            super(Children.LEAF);
        }
    } 

    private static String getString (String s) {
        return org.openide.util.NbBundle.getBundle (DataFolderPanel.class).getString (s);
    }
}
