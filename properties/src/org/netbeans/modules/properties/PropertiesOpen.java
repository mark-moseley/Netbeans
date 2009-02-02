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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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

package org.netbeans.modules.properties;

import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.beans.VetoableChangeSupport;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import javax.swing.Action;
import javax.swing.event.ChangeListener;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.openide.actions.FindAction;
import org.openide.awt.UndoRedo;
import org.openide.cookies.CloseCookie;
import org.openide.cookies.OpenCookie;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileStatusEvent;
import org.openide.filesystems.FileStatusListener;
import org.openide.filesystems.FileSystem;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Node;
import org.openide.NotifyDescriptor;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.ImageUtilities;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;
import org.openide.util.WeakSet;
import org.openide.util.actions.SystemAction;
import org.openide.windows.CloneableOpenSupport;
import org.openide.windows.CloneableTopComponent;
import org.openide.DialogDescriptor;
import org.openide.filesystems.FileUtil;


/** 
 * Support for opening bundle of .properties files (OpenCookie) in table view editor. 
 * 
 * @author Petr Jiricka, Peter Zavadsky
 */
public class PropertiesOpen extends CloneableOpenSupport
                            implements OpenCookie, CloseCookie {

    /** Main properties dataobject */
    @Deprecated
    PropertiesDataObject propDataObject;


    private List<PropertiesDataObject> dataObjectList;

    private BundleStructure bundleStructure;
    /** Listener for modificationc on dataobject, adding and removing save cookie */
    PropertyChangeListener modifL;

    HashMap<PropertiesDataObject,PropertyChangeListener> weakModifiedListeners;

    /** UndoRedo manager for this properties open support */
    protected transient UndoRedo.Manager undoRedoManager;

    /** This object is used for marking all undoable edits performed as one atomic undoable action. */
    transient Object atomicUndoRedoFlag;
    

    /** Constructor */
    @Deprecated
    public PropertiesOpen(PropertiesDataObject propDataObject) {
        super(new Environment(propDataObject));
        
        this.propDataObject = propDataObject;

        //PENDING Add Listeners for all DataObject from this OpenSupport
        this.propDataObject.addPropertyChangeListener(WeakListeners.propertyChange(modifL =
            new ModifiedListener(), this.propDataObject));
    }

    public PropertiesOpen(BundleStructure structure) {
        super(new Environment(structure));

        this.bundleStructure = structure;
        addModifiedListeners();
    }

    private void addModifiedListeners() {
        BundleStructure structure = bundleStructure;
        dataObjectList = new ArrayList<PropertiesDataObject>();
        PropertiesDataObject dataObject;
        modifL = new ModifiedListener();
        weakModifiedListeners = new HashMap<PropertiesDataObject, PropertyChangeListener>();
        for (int i=0;i<structure.getEntryCount();i++) {
            dataObject = (PropertiesDataObject) structure.getNthEntry(i).getDataObject();
            weakModifiedListeners.put(dataObject, WeakListeners.propertyChange(modifL, dataObject));
            dataObject.addPropertyChangeListener(weakModifiedListeners.get(dataObject));
            dataObjectList.add(dataObject);
        }
    }

    protected void addDataObject(PropertiesDataObject dataObject) {
        //PENDING to remove PropertyChangeListener its need to be stored first
        if (weakModifiedListeners.get(dataObject)!=null) {
            dataObject.removePropertyChangeListener(weakModifiedListeners.get(dataObject));
        } else {
            weakModifiedListeners.put(dataObject,WeakListeners.propertyChange(modifL, dataObject));
        }
        dataObject.addPropertyChangeListener(weakModifiedListeners.get(dataObject));
        env = new Environment(bundleStructure);
        PropertiesCloneableTopComponent topComp = (PropertiesCloneableTopComponent) allEditors.getArbitraryComponent();
        if (topComp != null) {
            topComp.dataObjectAdded(dataObject);
        }
        dataObjectList.add(dataObject);
    }
    /** 
     * Tests whether all data is saved, and if not, prompts the user to save.
     *
     * @return {@code true} if everything can be closed
     */
    @Override
    protected boolean canClose() {
        PropertiesDataObject dataObject;
        SaveCookie saveCookie = null;
        HashMap<SaveCookie,PropertiesDataObject> map = new HashMap<SaveCookie,PropertiesDataObject>();
        for (int i=0;i<bundleStructure.getEntryCount();i++) {
            dataObject = (PropertiesDataObject) bundleStructure.getNthEntry(i).getDataObject();
            saveCookie = dataObject.getCookie(SaveCookie.class);
            //Need to find all saveCookie
            if (saveCookie != null) map.put(saveCookie, dataObject);
        }
        if (map.isEmpty()) {
            return true;
        }
        stopEditing();
        if (!shouldAskSave()) {
            return true;
        }
        
        /* Create and display a confirmation dialog - Save/Discard/Cancel: */
        String title = NbBundle.getMessage(PropertiesOpen.class,
                                           "CTL_Question");         //NOI18N
        String question = NbBundle.getMessage(PropertiesOpen.class,
                                              "MSG_SaveFile",       //NOI18N
                                              bundleStructure.getNthEntry(0).getName());
        String optionSave = NbBundle.getMessage(PropertiesOpen.class,
                                                "CTL_Save");        //NOI18N
        String optionDiscard = NbBundle.getMessage(PropertiesOpen.class,
                                                   "CTL_Discard");  //NOI18N
        NotifyDescriptor descr = new DialogDescriptor(
                question,
                title,                              //title
                true,                               //modal
                new Object[] {optionSave,
                              optionDiscard,
                              NotifyDescriptor.CANCEL_OPTION},
                optionSave,                         //default option
                DialogDescriptor.DEFAULT_ALIGN,     //alignment of the options
                null,                               //help context
                (ActionListener) null);
        descr.setMessageType(NotifyDescriptor.QUESTION_MESSAGE);
        Object answer = DialogDisplayer.getDefault().notify(descr);
        
        /* Save the file if the answer was "Save": */
        if (answer == optionSave) {
            try {
                for (SaveCookie save : map.keySet()) {
                    save.save();
                    map.get(save).updateModificationStatus();
                }
            }
            catch (IOException e) {
                ErrorManager.getDefault().notify(e);
                return false;
            }
        }
        dataObject = null;
        for (int i=0;i<bundleStructure.getEntryCount();i++) {
            dataObject = (PropertiesDataObject) bundleStructure.getNthEntry(i).getDataObject();
            dataObject.updateModificationStatus();
        }

        return (answer == optionSave || answer == optionDiscard);
    }

    private void stopEditing() {
        saveEditorValues(true);
    }
    
    /**
     * Saves values of cells being edited and optionally stops the cell editing.
     *
     * @param  saveValueOnly  if {@code true}, just the cell values will be
     *                        saved to the model but the editor remains active
     */
    private void saveEditorValues(boolean stopEditing) {
        Enumeration en = allEditors.getComponents();
        while (en.hasMoreElements()) {
            Object o = en.nextElement();
            if (o instanceof PropertiesCloneableTopComponent) {
                BundleEditPanel bep = (BundleEditPanel)((PropertiesCloneableTopComponent)o).getComponent(0);
                if (stopEditing) {
                    bep.stopEditing();
                } else {
                    bep.saveEditorValue(false);
                }
            }
        }
    }
    
    /** 
     * Overrides superclass abstract method. 
     * A method to create a new component.
     * @return the cloneable top component for this support
     */
    protected CloneableTopComponent createCloneableTopComponent() {
//        return new PropertiesCloneableTopComponent(propDataObject);
        return new PropertiesCloneableTopComponent(bundleStructure);
    }

    /**
     * Overrides superclass abstract method. 
     * Message to display when an object is being opened.
     * @return the message or null if nothing should be displayed
     */
    protected String messageOpening() {
        bundleStructure.updateEntries();
        return NbBundle.getMessage(PropertiesOpen.class, "LBL_ObjectOpen", // NOI18N
            bundleStructure.getNthEntry(0).getName(),
            bundleStructure.getNthEntry(0).getFile().toString()
        );
    }

    /** 
     * Overrides superclass abstract method.
     * Message to display when an object has been opened.
     * @return the message or null if nothing should be displayed
     */
    protected String messageOpened() {
        return NbBundle.getMessage(PropertiesOpen.class, "LBL_ObjectOpened"); // NOI18N
    }

    /** @return whether has open table view component. */
    public synchronized boolean hasOpenedTableComponent() {
        return !allEditors.isEmpty();
    }

    /** Gets UndoRedo manager for this OpenSupport. */
    public UndoRedo getUndoRedo () {
        if (undoRedoManager != null) {
            return undoRedoManager;
        } else {
            return new CompoundUndoRedoManager(bundleStructure);
        }
    }

    /** Helper method. Closes documents. */
    private synchronized void closeDocuments() {
        BundleStructure structure = bundleStructure;
        for (int i = 0; i< structure.getEntryCount(); i++) {
            closeEntry(structure.getNthEntry(i));
        }
    }

    /** Helper method. Closes entry. */
    private void closeEntry(PropertiesFileEntry entry) {
        PropertiesEditorSupport editorSupport = entry.getPropertiesEditor();
        if (editorSupport.hasOpenedEditorComponent()) {
            // Has opened editor view for this entry -> don't close document.
            return;
        } else {
            // Hasn't opened editor view for this entry -> close document.
            editorSupport.forceNotifyClosed();
            
            // #17221. Don't reparse invalid or virtual file.
            if(entry.getFile().isValid() && !entry.getFile().isVirtual()) {
                entry.getHandler().autoParse();
            }
        }
    }

    /**
     * Helper method. Should be called only if the object has SaveCookie
     * @return true if closing this editor whithout saving would result in loss of data
     *  because al least one of the modified files is not open in the code editor
     */
    private boolean shouldAskSave() {
        // for each entry : if there is a SaveCookie and no open editor component, return true.
        // if passed for all entries, return false
        BundleStructure structure = bundleStructure;
        PropertiesFileEntry entry;
        SaveCookie savec;
        for (int i = 0; i < structure.getEntryCount(); i++) {
            entry = structure.getNthEntry(i);
            savec = entry.getCookie(SaveCookie.class);
            if ((savec != null) && !entry.getPropertiesEditor().hasOpenedEditorComponent()) {
                return true;
            }
        }
        return false;
    }


    /** Environment that connects the open support together with {@code DataObject}. */
    private static class Environment implements CloneableOpenSupport.Env, Serializable,
        PropertyChangeListener, VetoableChangeListener {
            
        /** Generated Serialized Version UID */
        static final long serialVersionUID = -1934890789745432531L;
        
        /** Object to serialize and be connected to. */
        @Deprecated
        private DataObject dataObject;

        private BundleStructure bundleStructure;
        
        /** Support for firing of property changes. */
        private transient PropertyChangeSupport propSupp;
        
        /** Support for firing of vetoable changes. */
        private transient VetoableChangeSupport vetoSupp;

        
        /** 
         * Constructor. Attaches itself as listener to 
         * the data object so, all property changes of the data object
         * are also rethrown to own listeners.
         * @param dataObject data object to be attached to
         */
        @Deprecated
        public Environment(PropertiesDataObject dataObject) {
            this.dataObject = dataObject;
            dataObject.addPropertyChangeListener(WeakListeners.propertyChange(this, dataObject));
            dataObject.addVetoableChangeListener(WeakListeners.vetoableChange(this, dataObject));
        }

        public Environment(BundleStructure structure) {
            this.bundleStructure = structure;
            dataObject = bundleStructure.getNthEntry(0).getDataObject();
            addListeners();
        }


        private void addListeners() {
            BundleStructure structure = bundleStructure;
            PropertiesDataObject dataObj;
            for(int i=0;i<structure.getEntryCount();i++) {
                dataObj = (PropertiesDataObject) structure.getNthEntry(i).getDataObject();
                dataObj.addPropertyChangeListener(WeakListeners.propertyChange(this, dataObj));
                dataObj.addVetoableChangeListener(WeakListeners.vetoableChange(this, dataObj));
            }

        }
        /** Implements {@code CloneableOpenSupport.Env} interface. Adds property listener. */
        public void addPropertyChangeListener(PropertyChangeListener l) {
            prop().addPropertyChangeListener(l);
        }

        /** Implements {@code CloneableOpenSupport.Env} interface. Removes property listener. */
        public void removePropertyChangeListener(PropertyChangeListener l) {
            prop().removePropertyChangeListener(l);
        }

        /** Implements {@code CloneableOpenSupport.Env} interface. Adds veto listener. */
        public void addVetoableChangeListener(VetoableChangeListener l) {
            veto().addVetoableChangeListener(l);
        }

        /** Implements {@code CloneableOpenSupport.Env} interface. Removes veto listener. */
        public void removeVetoableChangeListener(VetoableChangeListener l) {
            veto().removeVetoableChangeListener(l);
        }

        /**
         * Implements {@code CloneableOpenSupport} interface.
         * Method that allows environment to find its cloneable open support.
         * @return the support or null if the environemnt is not in valid 
         * state and the CloneableOpenSupport cannot be found for associated
         * data object
         */
        @Deprecated
        //TODO PENDING Called from super class need to preserve
        public CloneableOpenSupport findCloneableOpenSupport() {
            return (CloneableOpenSupport) dataObject.getCookie(OpenCookie.class);
        }

        public CloneableOpenSupport findCloneableOpenSupport(PropertiesDataObject dataObject) {
            return (CloneableOpenSupport)dataObject.getCookie(OpenCookie.class);
        }

        /** 
         * Implements {@code CloneableOpenSupport.Env} interface.
         * Test whether the support is in valid state or not.
         * It could be invalid after deserialization when the object it
         * referenced to does not exist anymore.
         * @return true or false depending on its state
         */
        @Deprecated
        public boolean isValid() {
            return dataObject.isValid();
        }

        public boolean isValid(PropertiesDataObject dataObject) {
            return dataObject.isValid();
        }

        
        /**
         * Implements {@code CloneableOpenSupport.Env} interface. 
         * Test whether the object is modified or not.
         * @return true if the object is modified
         */
        public boolean isModified() {
            //if one dataObject is modified assume that everything modified
            PropertiesFileEntry entry;
            for (int i=0; i < bundleStructure.getEntryCount();i++) {
                entry = bundleStructure.getNthEntry(i);
                if ((entry !=null) && (entry.getDataObject().isModified()) ) {
                    return true;
                }
            }
            return false;
        }

        public boolean isModified(PropertiesDataObject dataObject) {
            return dataObject.isModified();
        }

        /**
         * Implements {@code CloneableOpenSupport.Env} interface. 
         * Support for marking the environement modified.
         * @exception IOException if the environment cannot be marked modified
         *   (for example when the file is readonly), when such exception
         *   is the support should discard all previous changes
         */
        @Deprecated
        public void markModified() throws java.io.IOException {
            dataObject.setModified(true);
        }

        public void markModified(PropertiesDataObject dataObject) throws java.io.IOException {
            dataObject.setModified(true);
        }
        /** 
         * Implements {@code CloneableOpenSupport.Env} interface.
         * Reverse method that can be called to make the environment unmodified.
         */
        @Deprecated
        public void unmarkModified() {
            dataObject.setModified(false);
        }

        public void unmarkModified(PropertiesDataObject dataObject) {
            dataObject.setModified(false);
        }
        
        /** 
         * Implements {@code PropertyChangeListener} interface.
         * Accepts property changes from {@code DataObject} and fires them to own listeners.
         */
        public void propertyChange(PropertyChangeEvent evt) {
            if(DataObject.PROP_MODIFIED.equals(evt.getPropertyName())) {
                PropertiesDataObject dataObj = null;
                if (evt.getSource() instanceof PropertiesDataObject) {
                    dataObj = (PropertiesDataObject) evt.getSource();
                }
                if (dataObj!=null) {
                    if(dataObj.isModified()) {
                        dataObj.addVetoableChangeListener(this);
                    } else {
                        dataObj.removeVetoableChangeListener(this);
                    }
                }
            } else if(DataObject.PROP_VALID.equals(evt.getPropertyName ())) { 
                // We will handle the object invalidation here.
                // Do not check it if old value is not true.
                if (Boolean.FALSE.equals(evt.getOldValue())) {
                    return;
                }

                PropertiesDataObject dataObj = null;
                if (evt.getSource() instanceof PropertiesDataObject) {
                    dataObj = (PropertiesDataObject) evt.getSource();
                }
                if (dataObj != null) {
                    // Mark the object as not being modified, so nobody
                    // will ask for save.
                    unmarkModified(dataObj);
                    bundleStructure.updateEntries();
                    if (bundleStructure.getEntryCount() == 0) {
                // Loosing validity.
//                PropertiesOpen support = (PropertiesOpen)findCloneableOpenSupport();
                        PropertiesOpen support = (PropertiesOpen)findCloneableOpenSupport();
                        if(support != null ) {
                    
//                            bundleStructure.updateEntries();
                            support.close(false);
                        }
                    } else {
                        bundleStructure.notifyOneFileChanged(dataObj.getPrimaryFile());
                    }
                }
            } else if (DataObject.PROP_PRIMARY_FILE.equals(evt.getPropertyName())) {
                PropertiesDataObject dataObj = null;
                if (evt.getSource() instanceof PropertiesDataObject) {
                    dataObj = (PropertiesDataObject) evt.getSource();
                }
                if (dataObj != null) {
                    bundleStructure.updateEntries();
                    bundleStructure.notifyOneFileChanged(dataObj.getPrimaryFile());
                }
            } else {
                firePropertyChange (
                    evt.getPropertyName(),
                    evt.getOldValue(),
                    evt.getNewValue()
                );
            }
        }
        
        /**
         * Implements <code>VetoAbleChangeListener</code> interface. 
         * Accepts vetoable changes and fires them to own listeners.
         */
        public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {
            fireVetoableChange (
                evt.getPropertyName(),
                evt.getOldValue(),
                evt.getNewValue()
            );
        }
        
        /** Fires property change.
        * @param name the name of property that changed
        * @param oldValue old value
        * @param newValue new value
        */
        private void firePropertyChange (String name, Object oldValue, Object newValue) {
            prop().firePropertyChange(name, oldValue, newValue);
        }
        
        /** Fires vetoable change.
        * @param name the name of property that changed
        * @param oldValue old value
        * @param newValue new value
        */
        private void fireVetoableChange (String name, Object oldValue, Object newValue) throws PropertyVetoException {
            veto().fireVetoableChange(name, oldValue, newValue);
        }
        
        /** Lazy gets property change support. */
        private PropertyChangeSupport prop() {
            synchronized (this) {
                if (propSupp == null) {
                    propSupp = new PropertyChangeSupport(this);
                }
            }
            return propSupp;
        }
        
        /** Lazy gets vetoable change support. */
        private VetoableChangeSupport veto() {
            synchronized (this) {
                if (vetoSupp == null) {
                    vetoSupp = new VetoableChangeSupport(this);
                }
            }
            return vetoSupp;
        }
    } // End of inner class Environment.
    
    
    /** Inner class. Listens to modifications and updates save cookie. */
    private final class ModifiedListener implements SaveCookie, PropertyChangeListener {


        /** Gives notification that the DataObject was changed.
        * @param ev PropertyChangeEvent
        */
        public void propertyChange(PropertyChangeEvent evt) {
            // Data object changed, reset the UndoRedo manager.
            if (DataObject.PROP_MODIFIED.equals(evt.getPropertyName())) {
                if (evt.getSource() instanceof  PropertiesDataObject) {
                    PropertiesDataObject dataObject = (PropertiesDataObject) evt.getSource();
                    if (!dataObject.isValid()) {
                            if (!((Boolean)evt.getNewValue()).booleanValue()) {
                                removeSaveCookie(dataObject);
                            }
                    } else
                    if(bundleStructure.getEntryByFileName(dataObject.getName())!=null) {
                        ((CompoundUndoRedoManager) PropertiesOpen.this.getUndoRedo()).reset(bundleStructure);
                            if (((Boolean)evt.getNewValue()).booleanValue()) {
                                addSaveCookie(dataObject);
                            } else {
                                removeSaveCookie(dataObject);
                            }
                     }
                }
            }
        }

        /** Implements {@code SaveCookie} interface. */
        public void save() throws IOException {
            /*
             * At first, save the value of the cell being edited,
             * without making any UI changes.
             */
            saveEditorValues(false);

            // do saving job
            saveDocument();

            /* Update the UI: */
            Mutex.EVENT.writeAccess(new Runnable() {
                public void run() {
                    stopEditing();
                }
            });
        }

        /** Save the document in this thread.
        * Create "orig" document for the case that the save would fail.
        * @exception IOException on I/O error
        */
        public void saveDocument() throws IOException {
            BundleStructure structure = bundleStructure;
            SaveCookie save;
            for (int i=0; i<structure.getEntryCount();i++) {
                save = structure.getNthEntry(i).getCookie(SaveCookie.class);
                if (save != null) {
                    save.save();
                }
            }
        }

        /** Adds save cookie to the dataobject. */
        @Deprecated
        private void addSaveCookie() {
            if(propDataObject.getCookie(SaveCookie.class) == null) {
                propDataObject.getCookieSet0().add(this);
            }
        }

        private void addSaveCookie(PropertiesDataObject propDataObject) {
            if(propDataObject.getCookie(SaveCookie.class) == null) {
                propDataObject.getCookieSet0().add(this);
            }
        }
        
        /** Removes save cookie from the dataobject. */
        @Deprecated
        private void removeSaveCookie() {
            if(propDataObject.getCookie(SaveCookie.class) == this) {
                propDataObject.getCookieSet0().remove(this);
            }
        }

        private void removeSaveCookie(PropertiesDataObject propDataObject) {
            if(propDataObject.getCookie(SaveCookie.class) == this) {
                propDataObject.getCookieSet0().remove(this);
            }
        }
    } // End of inner class ModifiedListener.

    
    /** Inner class for opening at a given key. */
    public class PropertiesOpenAt implements OpenCookie {

        /** Entry the key belongs to. */
        private PropertiesFileEntry entry;
        
        /** Key where to open at. */
        private String key;

        
        /** Construcor. */
        PropertiesOpenAt(PropertiesFileEntry entry, String key) {
            this.entry = entry;
            this.key   = key;
        }

        
        /** Setter for key property. */
        public void setKey(String key) {
            this.key = key;
        }

        /** Implements {@code OpenCookie}. Opens document. */
        public void open() {
            // Instead of PropertiesOpen.super.open() so we get reference to TopComponent.
            // Note: It is strange for me that calling PropetiesOpen.this.openCloneableTopComponent throw s exception at run-time.
            final PropertiesCloneableTopComponent editor = (PropertiesCloneableTopComponent)PropertiesOpen.super.openCloneableTopComponent();
            editor.requestActive();
            
            BundleStructure bs = bundleStructure;
            bs.updateEntries();
            // Find indexes.
            int entryIndex = bs.getEntryIndexByFileName(entry.getFile().getName());
            int rowIndex   = bs.getKeyIndexByName(key);
            
            if ((entryIndex != -1) && (rowIndex != -1)) {
                final int row = rowIndex;
                final int column = entryIndex + 1;

                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        JTable table = ((BundleEditPanel)editor.getComponent(0)).getTable();
                        // Autoscroll to cell if possible and necessary.
                        if (table.getAutoscrolls()) { 
                            Rectangle cellRect = table.getCellRect(row, column, false);
                            if (cellRect != null) {
                                table.scrollRectToVisible(cellRect);
                            }
                        }

                        // Update selection & edit.
//XXX This caused to open properties for editing with wrong values
//                        table.getColumnModel().getSelectionModel().setSelectionInterval(row, column);
//                        table.getSelectionModel().setSelectionInterval(row, column);

                        table.editCellAt(row, column);
                    }
                });
            }
        }
    } // End of inner class PropertiesOpenAt.


    /** Cloneable top component which represents table view of resource bundles. */
    public static class PropertiesCloneableTopComponent extends CloneableTopComponent {

        /** Reference to underlying {@code PropertiesDataObject}. */
        @Deprecated
        private PropertiesDataObject propDataObject;

        private List<PropertiesDataObject> dataObjectsList;
        /** Listener for changes on {@code propDataObject} name and cookie properties.
         * Changes display name of components accordingly. */
        private transient PropertyChangeListener dataObjectListener;

        private transient static HashMap<PropertiesDataObject,PropertyChangeListener> weakListeners;

        /** Generated serial version UID. */
        static final long serialVersionUID =2836248291419024296L;
        
        
        /** Default constructor for deserialization. */
        public PropertiesCloneableTopComponent() {
        }

        @Override
        protected void componentHidden() {
            ((BundleEditPanel)getComponent(0)).getTable().firePropertyChange("componentHidden", 0, 1);  //NOI18N
            super.componentHidden();
        }

        /** Constructor.
        * @param propDataObject data object we belong to */
        @Deprecated
        public PropertiesCloneableTopComponent (PropertiesDataObject propDataObject) {
            this.propDataObject  = propDataObject;

            initialize();
        }

        private MultiBundleStructure bundleStructure;

        public PropertiesCloneableTopComponent(BundleStructure structure) {
            this.bundleStructure = (MultiBundleStructure) structure;
            propDataObject = (PropertiesDataObject) bundleStructure.getNthEntry(0).getDataObject();
            dataObjectsList = new ArrayList<PropertiesDataObject>();
            for (int i=0; i<bundleStructure.getEntryCount();i++) {
                dataObjectsList.add((PropertiesDataObject)bundleStructure.getNthEntry(i).getDataObject());
            }
            weakListeners = new HashMap<PropertiesDataObject, PropertyChangeListener>();
            initialize();
        }
        /**
         */
        @Override
        public void open() {
            if (discard()) {
                return;
            }
            super.open();
        }

        @Override
        public void requestActive() {
            super.requestActive();
            getComponent(0).requestFocusInWindow();
        }
        
        @Override
        public boolean canClose () {
            ((BundleEditPanel)getComponent(0)).stopEditing();
            return super.canClose();
        }
        
        /** Initializes this instance. Used by construction and deserialization. */
        private void initialize() {
            initComponents();
            setupActions();
            BundleStructure structure = bundleStructure;
            PropertiesDataObject dataObject;

            Node[] node = new Node[structure.getEntryCount()];
            dataObjectListener = new NameUpdater();

            for( int i=0; i<structure.getEntryCount();i++) {
                    dataObject = dataObjectsList.get(i);//(PropertiesDataObject) structure.getNthEntry(i).getDataObject();
                    node[i] = dataObject.getNodeDelegate();
                    weakListeners.put(dataObject,WeakListeners.propertyChange(dataObjectListener, dataObject));
                    dataObject.addPropertyChangeListener(weakListeners.get(dataObject));
            }
            
            setActivatedNodes(node);

            updateName();
        }

        /**
         * Called from PropertiesOpen when new DataObject added
         * @param dataObject to add listener to
         */
        protected void dataObjectAdded(PropertiesDataObject dataObject) {
            if (weakListeners.get(dataObject)!=null) {
                dataObject.removePropertyChangeListener(weakListeners.get(dataObject));
            } else {
                weakListeners.put(dataObject, WeakListeners.propertyChange(dataObjectListener, dataObject));
            }
            dataObject.addPropertyChangeListener(weakListeners.get(dataObject));
            updateDataObjects();
            updateName();
        }

        /**
         * update dataObjectList with new data
         */
        private void updateDataObjects() {
            if (bundleStructure==null) {
                dataObjectsList = null;
                return;
            }

            int entryCount = bundleStructure.getEntryCount();
            if (entryCount == 0) {
                dataObjectsList = null;
                return;
            }
            for (PropertiesDataObject DO: dataObjectsList) {
                if (bundleStructure.getEntryIndexByFileName(DO.getName())==-1) {
                    dataObjectsList.remove(DO);
                }
            }
            PropertiesDataObject DO = null;
            for (int i=0;i<entryCount;i++) {
                DO = (PropertiesDataObject)bundleStructure.getNthEntry(i).getDataObject();
                if (!dataObjectsList.contains(DO)) {
                    dataObjectsList.add(DO);
                }
            }
        }
        /* Based on class DataNode.PropL. */
        final class NameUpdater implements PropertyChangeListener,
                                           FileStatusListener,
                                           Runnable {
            
            /** */
            private static final int NO_ACTION = 0;
            /** */
            private static final int ACTION_UPDATE_NAME = 1;
            /** */
            private static final int ACTION_UPDATE_DISPLAY_NAME = 2;
            
            /** weak version of this listener */
            private FileStatusListener weakL;
            /** previous filesystem we were attached to */
            private FileSystem previous;
            
            /** */
            private final int action;

            /**
             */
            NameUpdater() {
                this(NO_ACTION);
                updateStatusListener();
            }
            
            /**
             */
            NameUpdater(int action) {
                this.action = action;
            }
            
            /** Updates listening on a status of filesystem. */
            private void updateStatusListener() {
                if (previous != null) {
                    previous.removeFileStatusListener(weakL);
                }
                try {
                    previous = bundleStructure.getNthEntry(0).getFile().getFileSystem();
                    if (weakL == null) {
                        weakL = org.openide.filesystems.FileUtil
                                .weakFileStatusListener(this, previous);
                    }
                    previous.addFileStatusListener(weakL);
                } catch (FileStateInvalidException ex) {
                    previous = null;
                }
            }
            
            /**
             * Notifies listener about change in annotataion of a few files.
             */
            public void annotationChanged(FileStatusEvent ev) {
                if (!ev.isNameChange()) {
                    return;
                }
                
                boolean thisChanged = false;
                for (int i=0;i<bundleStructure.getEntryCount();i++) {
                    if(ev.hasChanged(bundleStructure.getNthEntry(i).getFile())) {
                        thisChanged = true;
                        break;
                    }
                }
                if (thisChanged) {
                    Mutex.EVENT.writeAccess(
                            new NameUpdater(ACTION_UPDATE_DISPLAY_NAME));
                }
            }
            
            /**
             */
            public void propertyChange(PropertyChangeEvent e) {
                //PENDING Add correct propDataObject
                if (bundleStructure.getEntryCount() == 0) {return;}
                if (e.getSource() instanceof PropertiesDataObject) {
                    PropertiesDataObject DO = (PropertiesDataObject) e.getSource();
                    try {
                        if ((DO == Util.findPrimaryDataObject(DO)) && (!DO.isValid())) {
                            return;
                        }
                    } catch (DataObjectNotFoundException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
                
                final String property = e.getPropertyName();
                if (property == null) {
                    return;
                }
                if (property.equals(DataObject.PROP_NAME)) {
                    Mutex.EVENT.writeAccess(
                            new NameUpdater(ACTION_UPDATE_NAME));
                } else if (property.equals(DataObject.PROP_PRIMARY_FILE)) {
                    updateStatusListener();
                    Mutex.EVENT.writeAccess(
                            new NameUpdater(ACTION_UPDATE_NAME));
                } else if (property.equals(DataObject.PROP_COOKIE)
                           || property.equals(DataObject.PROP_FILES)) {
                    Mutex.EVENT.writeAccess(
                            new NameUpdater(ACTION_UPDATE_DISPLAY_NAME));
                }
            }
            
            /**
             */
            public void run() {
                assert EventQueue.isDispatchThread();
                
                if (action == ACTION_UPDATE_NAME) {
                    updateName();
                } else if (action == ACTION_UPDATE_DISPLAY_NAME) {
                    updateDisplayName();
                } else {
                    assert false;
                }
            }
            
        }
        
        /**
         * Sets up action Find that it is activated/deactivated appropriately
         * and so that it does what it should do.
         */
        private void setupActions() {
            JTable bundleTable = ((BundleEditPanel) getComponent(0)).getTable();
            FindAction findAction = SystemAction.get(FindAction.class);
            Action action = FindPerformer.getFindPerformer(bundleTable);
            getActionMap().put(findAction.getActionMapKey(), action);
        }
        
        /**
         */
        private void updateName() {
            assert EventQueue.isDispatchThread();
            
            final String name = bundleStructure.getNthEntry(0).getName();
            final String displayName = displayName();
            final String htmlDisplayName = htmlDisplayName();
            final String toolTip = messageToolTip();
            
            Enumeration<CloneableTopComponent> en = getReference().getComponents();
            while (en.hasMoreElements()) {
                CloneableTopComponent tc = en.nextElement();
                tc.setName(name);
                tc.setDisplayName(displayName);
                tc.setHtmlDisplayName(htmlDisplayName);
                tc.setToolTipText(toolTip);
            }
        }
        
        /**
         */
        private void updateDisplayName() {
            assert EventQueue.isDispatchThread();
            
            final String displayName = displayName();
            final String htmlDisplayName = htmlDisplayName();
            
            Enumeration<CloneableTopComponent> en = getReference().getComponents();
            while (en.hasMoreElements()) {
                CloneableTopComponent tc = en.nextElement();
                tc.setDisplayName(displayName);
                tc.setHtmlDisplayName(htmlDisplayName);
            }
        }
        
        /**
         */
        private String addModifiedInfo(String name) {
            boolean modified = false;
            for (int i=0;i<bundleStructure.getEntryCount();i++) {
                if(bundleStructure.getNthEntry(i).getDataObject().getCookie(SaveCookie.class) != null) {
                    modified = true;
                    break;
                }
            }
            int version = modified ? 1 : 3;
            return NbBundle.getMessage(PropertiesCloneableTopComponent.class,
                                       "LBL_EditorName",                //NOI18N
                                       new Integer(version),
                                       name);
        }

        /**
         * Builds a display name for this component.
         *
         * @return  the created display name
         * @see  #htmlDisplayName
         */
        private String displayName() {
            //PENDING change to avoid call getNthEntry, in some cases it will throw an exception
            String nameBase = bundleStructure.getNthEntry(0).getDataObject().getNodeDelegate().getDisplayName();
            return addModifiedInfo(nameBase);
        }
        
        /**
         * Builds a HTML display name for this component.
         *
         * @return  the created display name
         * @see  #displayName()
         */
        private String htmlDisplayName() {
            final Node node = bundleStructure.getNthEntry(0).getDataObject().getNodeDelegate();
            String displayName = node.getHtmlDisplayName();
            if (displayName != null) {
                if (!displayName.startsWith("<html>")) {                //NOI18N
                    displayName = "<html>" + displayName;               //NOI18N
                }
            } else {
                displayName = node.getDisplayName();
            }
            return addModifiedInfo(displayName);
        }
        
        /** Gets string for tooltip. */
        private String messageToolTip() {
            FileObject fo = bundleStructure.getNthEntry(0).getFile();
            return FileUtil.getFileDisplayName(fo);
        }
        
        /**
         * 
         * Overrides superclass method. When closing last view, also close the document.
         * @return {@code true} if close succeeded
         */
        @Override
        protected boolean closeLast () {
            if (!bundleStructure.getOpenSupport().canClose ()) {
                // if we cannot close the last window
                return false;
            }
            bundleStructure.getOpenSupport().closeDocuments();
            PropertyChangeListener l;
            for (PropertiesDataObject dataObject:dataObjectsList) {
                l = weakListeners.get(dataObject);
                if (l!=null) {
                    dataObject.removePropertyChangeListener(l);
                    weakListeners.remove(dataObject);
                }
            }
            return true;
        }

        /**
         * Is called from the superclass {@code clone} method to create new component from this one.
         * This implementation only clones the object by calling super.clone method.
         * @return the copy of this object
         */
        @Override
        protected CloneableTopComponent createClonedObject () {
            return new PropertiesCloneableTopComponent(bundleStructure);
        }

        /** Gets {@code Icon}. */
        @Override
        public Image getIcon () {
            return ImageUtilities.loadImage("org/netbeans/modules/properties/propertiesEditorMode.gif"); // NOI18N
        }

        /** Gets help context. */
        @Override
        public HelpCtx getHelpCtx () {
            return new HelpCtx(Util.HELP_ID_MODIFYING);
        }
        
        @Override
        protected String preferredID() {
            return getName();
        }
        
        @Override
        public int getPersistenceType() {
            return PERSISTENCE_ONLY_OPENED;
        }
        
        /** 
         * Gets compound UndoRedo manager from all UndozRedo managers from all editor supports. 
         */
        @Override
        public UndoRedo getUndoRedo () {
            return  bundleStructure.getOpenSupport().getUndoRedo();
        }

        /** Inits the subcomponents. Sets layout for this top component and adds {@code BundleEditPanel} to it. 
         * @see BundleEditPanel */
        private void initComponents() {
            GridBagLayout gridbag = new GridBagLayout();
            setLayout(gridbag);

            GridBagConstraints c = new GridBagConstraints();
            c.fill = GridBagConstraints.BOTH;
            c.weightx = 1.0;
            c.weighty = 1.0;
            c.gridwidth = GridBagConstraints.REMAINDER;
            JPanel panel = new BundleEditPanel(bundleStructure, new PropertiesTableModel(bundleStructure));
            gridbag.setConstraints(panel, c);
            add(panel);
        }
        
        /** This component should be discarded if the associated environment
         *  is not valid.
         */
        private boolean discard () {
            return bundleStructure == null;
        }
        

        /**
         * Serialize this top component.
         * Subclasses wishing to store state must call the super method, then write to the stream.
         * @param out the stream to serialize to
         */
        @Override
        public void writeExternal (ObjectOutput out) throws IOException {
            super.writeExternal(out);
            out.writeObject(bundleStructure.getNthEntry(0).getDataObject());
        }

        /** 
         * Deserialize this top component.
         * Subclasses wishing to store state must call the super method, then read from the stream.
         * @param in the stream to deserialize from
         */
        @Override
        public void readExternal (ObjectInput in) throws IOException, ClassNotFoundException {
            super.readExternal(in);

            propDataObject = (PropertiesDataObject)in.readObject();
            bundleStructure = (MultiBundleStructure) propDataObject.getBundleStructure();
            dataObjectsList = new ArrayList<PropertiesDataObject>();
            for (int i=0;i<bundleStructure.getEntryCount();i++) {
                dataObjectsList.add((PropertiesDataObject) bundleStructure.getNthEntry(i).getDataObject());
            }
            weakListeners = new HashMap<PropertiesDataObject, PropertyChangeListener>();
            initialize();
        }
    } // End of nested class PropertiesCloneableTopComponent.


    /**
     * {@code UndoRedo} manager for {@code PropertiesOpen} support. It contains weak references
     * to all UndoRedo managers from all PropertiesEditor supports (for each entry of dataobject one manager).
     * It uses it's "timeStamp" methods to find out which one of these managers comes to play.
     */
    private static class CompoundUndoRedoManager implements UndoRedo {
        
        /** Set of weak references to all "underlying" editor support undoredo managers. */
        private WeakSet<Manager> managers = new WeakSet<Manager>(5);
        
        // Constructor
        
        /** Collects all UndoRedo managers from all editor support of all entries. */
        @Deprecated
        public CompoundUndoRedoManager(PropertiesDataObject obj) {
            init(obj);
        }
        public CompoundUndoRedoManager(BundleStructure structure) {
            init(structure);
        }

        /** Initialize set of managers. */
        @Deprecated
        private void init(PropertiesDataObject obj) {
            BundleStructure structure = obj.getBundleStructure();
            PropertiesEditorSupport editorSupport = null;
            for(int i=0; i< structure.getEntryCount(); i++) {
                editorSupport = structure.getNthEntry(i).getPropertiesEditor();
                if (editorSupport != null) {
                    managers.add(editorSupport.getUndoRedoManager());
                }
            }
        }

        private void init(BundleStructure structure) {
            for(int i=0; i< structure.getEntryCount(); i++) {
                managers.add(structure.getNthEntry(i).getPropertiesEditor().getUndoRedoManager());
            }
        }

        /** Resets the managers. Used when data object has changed. */
        @Deprecated
        public synchronized void reset(PropertiesDataObject obj) {
            managers.clear();
            init(obj);
        }

        public synchronized void reset(BundleStructure structure) {
            managers.clear();
            init(structure);
        }

        /** Gets manager which undo edit comes to play.*/
        private UndoRedo getNextUndo() {
            UndoRedo chosenManager = null;
            long time = 0L; // time to compare with
            long timeManager; // time of next undo of actual manager
            
            for (Iterator<Manager> it = managers.iterator(); it.hasNext(); ) {
                PropertiesEditorSupport.UndoRedoStampFlagManager manager = (PropertiesEditorSupport.UndoRedoStampFlagManager)it.next();
                timeManager = manager.getTimeStampOfEditToBeUndone();
                if(timeManager > time) {
                    time = timeManager;
                    chosenManager = manager;
                }
            }
            return chosenManager;
        }
        
        /** Gets manager which redo edit comes to play.*/
        private UndoRedo getNextRedo() {
            UndoRedo chosenManager = null;
            long time = 0L; // time to compare with
            long timeManager; // time of next redo of actual manager
            
            for (Iterator<Manager> it = managers.iterator(); it.hasNext(); ) {
                PropertiesEditorSupport.UndoRedoStampFlagManager manager = (PropertiesEditorSupport.UndoRedoStampFlagManager)it.next();
                timeManager = manager.getTimeStampOfEditToBeRedone();
                if(timeManager > time) {
                    time = timeManager;
                    chosenManager = manager;
                }
            }
            return chosenManager;
        }
        
        /** Implements {@code UndoRedo}. Test whether at least one of managers can Undo.
         * @return {@code true} if undo is allowed
         */
        public synchronized boolean canUndo () {
            for (Manager manager : managers) {
                if (manager.canUndo()) {
                    return true;
                }
            }
            return false;
        }

        /** Implements {@code UndoRedo}. Test whether at least one of managers can Redo.
         * @return {@code true} if redo is allowed
         */
        public synchronized boolean canRedo () {
            for (Manager manager : managers) {
                if (manager.canRedo()) {
                    return true;
                }
            }
            return false;
        }

        /** Implements {@code UndoRedo}. Undo an edit. It finds a manager which next undo edit has the highest 
         * time stamp and makes undo on it.
         * @exception CannotUndoException if it fails
         */
        public synchronized void undo () throws CannotUndoException {
            PropertiesEditorSupport.UndoRedoStampFlagManager chosenManager = (PropertiesEditorSupport.UndoRedoStampFlagManager)getNextUndo();

            if (chosenManager == null) {
                throw new CannotUndoException();
            } else {
                Object atomicFlag = chosenManager.getAtomicFlagOfEditToBeUndone();
                if (atomicFlag == null) {// not linked with other edits as one atomic action
                    chosenManager.undo();
                } else { // atomic undo compound from more edits in underlying managers
                    boolean undone;
                    do { // the atomic action can consists from more undo edits from same manager
                        undone = false;
                        for (Iterator<Manager> it = managers.iterator(); it.hasNext(); ) {
                            PropertiesEditorSupport.UndoRedoStampFlagManager manager = (PropertiesEditorSupport.UndoRedoStampFlagManager)it.next();
                            if(atomicFlag.equals(manager.getAtomicFlagOfEditToBeUndone())) {
                                manager.undo();
                                undone = true;
                            }
                        }
                    } while(undone);
                }
            }
        }

        /** Implements {@code UndoRedo}. Redo a previously undone edit. It finds a manager which next undo edit has the highest 
         * time stamp and makes undo on it.
         * @exception CannotRedoException if it fails
         */
        public synchronized void redo () throws CannotRedoException {
            PropertiesEditorSupport.UndoRedoStampFlagManager chosenManager = (PropertiesEditorSupport.UndoRedoStampFlagManager)getNextRedo();

            if (chosenManager == null) {
                throw new CannotRedoException();
            } else {
                Object atomicFlag = chosenManager.getAtomicFlagOfEditToBeRedone();
                if (atomicFlag == null) {// not linked with other edits as one atomic action
                    chosenManager.redo();
                } else { // atomic redo compound from more edits in underlying managers
                    boolean redone;
                    do { // the atomic action can consists from more redo edits from same manager
                        redone = false;
                        for (Iterator<Manager> it = managers.iterator(); it.hasNext(); ) {
                            PropertiesEditorSupport.UndoRedoStampFlagManager manager = (PropertiesEditorSupport.UndoRedoStampFlagManager)it.next();
                            if(atomicFlag.equals(manager.getAtomicFlagOfEditToBeRedone())) {
                                manager.redo();
                                redone = true;
                            }
                        }
                    } while(redone);
                }
            }
        }

        /** Implements {@code UndoRedo}. Empty implementation. Does nothing.
         * @param l the listener to add
         */
        public void addChangeListener (ChangeListener l) {
            // PENDING up to now listen on separate managers
        }

        /** Implements {@code UndoRedo}. Empty implementation. Does nothing.
         * @param l the listener to remove
         * @see #addChangeListener
         */
        public void removeChangeListener (ChangeListener l) {
            // PENDING
        }

        /** Implements {@code UndoRedo}. Get a human-presentable name describing the
         * undo operation.
         * @return the name
         */
        public synchronized String getUndoPresentationName () {
            UndoRedo chosenManager = getNextUndo();

            if (chosenManager == null) {
                return "Undo"; // NOI18N // AbstractUndoableEdit.UndoName is not accessible
            } else {
                return chosenManager.getUndoPresentationName();
            }
        }

        /** Implements {@code UndoRedo}. Get a human-presentable name describing the
         * redo operation.
         * @return the name
         */
        public synchronized String getRedoPresentationName () {
            UndoRedo chosenManager = getNextRedo();
            if (chosenManager == null) {
                return "Redo"; // NOI18N // AbstractUndoableEdit.RedoName is not accessible
            } else {
                return chosenManager.getRedoPresentationName();
            }
        }
        
    } // End of nested class CompoundUndoRedoManager.
    
}
