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

package org.openide.loaders;

import java.util.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

import javax.swing.event.ChangeListener;

import org.openide.ErrorManager;
import org.openide.filesystems.*;
import org.openide.util.RequestProcessor;
import java.lang.ref.*;
import org.openide.util.WeakSet;
import org.openide.util.Lookup;

/** Registraction list of all data objects in the system.
* Maps data objects to its handlers.
*
* @author Jaroslav Tulach
*/
final class DataObjectPool extends Object
implements ChangeListener, RepositoryListener, PropertyChangeListener {
    /** set to null if the constructor is called from somewhere else than DataObject.find 
     * Otherwise contains Collection<Item> that have just been created in this thread and
     * shall be notified.
     */
    private static final ThreadLocal FIND = new ThreadLocal ();
    /** validator */
    private static final Validator VALIDATOR = new Validator ();

    private static final List TOKEN = Collections.unmodifiableList(new ArrayList());
    
    /** hashtable that maps FileObject to DataObjectPool.Item */
    private HashMap map = new HashMap (512);
    
    /** Set<FileSystem> covering all FileSystems we're listening on */
    private WeakSet knownFileSystems = new WeakSet();
    
    /** error manager to log what is happening here */
    private static final ErrorManager err = ErrorManager.getDefault().getInstance("org.openide.loaders.DataObject.find"); // NOI18N
    private static final boolean errLog = err.isLoggable(err.INFORMATIONAL);
    
    /** the pool for all objects. Use getPOOL method instead of direct referencing
     * this field.
     */
    private static DataObjectPool POOL;

    /** Lock for creating POOL instance */
    private static Object lockPOOL = new Object();
    
    /** Get the instance of DataObjectPool - value of static field 'POOL'.
     * Initialize the field if necessary.
     *
     * @return The DataObjectPool.
     */
    static DataObjectPool getPOOL() {
        synchronized (lockPOOL) {
            if (POOL != null)
                return POOL;
            POOL = new DataObjectPool ();
        }
        
        lp.addChangeListener(POOL);
        Repository.getDefault().addRepositoryListener (POOL);

        return POOL;
    }
    
    /** Allows DataObject constructors to be called.
     * @return a key to pass to exitAllowConstructor
     */
    private static Collection enterAllowContructor () {
        Collection prev = (Collection)FIND.get ();
        FIND.set (TOKEN);
        return prev;
    }
    
    /** Disallows DataObject constructors to be called and notifies 
     * all created DataObjects.
     */
    private static void exitAllowConstructor (Collection previous) {
        List l = (List)FIND.get ();
        FIND.set (previous);
        if (l != TOKEN) getPOOL ().notifyCreationAll(l);
    }
    
    /** Calls into one loader. Setups security condition to allow DataObject ocnstructor
     * to succeed.
     */
    public static DataObject handleFindDataObject (DataLoader loader, FileObject fo, DataLoader.RecognizedFiles rec) 
    throws java.io.IOException {
        DataObject ret;
        
        Collection prev = enterAllowContructor ();
        try {
            // make sure this thread is allowed to recognize
            getPOOL ().enterRecognition(fo);
            
            ret = loader.handleFindDataObject (fo, rec);
        } finally {
            exitAllowConstructor (prev);
        }
        
        return ret;
    }

    /** Creates and finishes registration of MultiDataObject.
     */
    public static MultiDataObject createMultiObject (MultiFileLoader loader, FileObject fo)
    throws java.io.IOException {
        MultiDataObject ret;
        
        Collection prev = enterAllowContructor ();
        try {
            ret = loader.createMultiObject (fo);
        } finally {
            exitAllowConstructor (prev);
        }
        
        return ret;
     }
    
    /** Calls into FolderLoader. Setups security condition to allow DataObject constructor
     * to succeed.
     */
    public static MultiDataObject createMultiObject(DataLoaderPool$FolderLoader loader, FileObject fo, DataFolder original) throws java.io.IOException {
        MultiDataObject ret;
        
        Collection prev = enterAllowContructor ();
        try {
            ret = loader.createMultiObject (fo, original);
        } finally {
            exitAllowConstructor (prev);
        }
        
        return ret;
     }
    
        
    
    /** Executes atomic action with priviledge to create DataObjects.
     */
    public void runAtomicActionSimple (FileObject fo, FileSystem.AtomicAction action) 
    throws java.io.IOException {
        Collection prev = enterAllowContructor ();
        try {
            fo.getFileSystem ().runAtomicAction(action);
        } finally {
            exitAllowConstructor (prev);
        }
    }
    
    //
    // Support for running really atomic actions
    //
    private Thread atomic;
    private RequestProcessor priviledged;
    /** the folder that is being modified */
    private FileObject blocked;
    public void runAtomicAction (final FileObject target, final FileSystem.AtomicAction action) 
    throws java.io.IOException {
        
        class WrapAtomicAction implements FileSystem.AtomicAction {
            public void run () throws java.io.IOException {
                Thread prev;
                FileObject prevBlocked;
                synchronized (DataObjectPool.this) {
                    // make sure that we are the ones that own 
                    // the recognition process
                    enterRecognition (null);
                    prev = atomic;
                    prevBlocked = blocked;
                    atomic = Thread.currentThread ();
                    blocked = target;
                }

                Collection findPrev = enterAllowContructor ();
                try {
                    action.run ();
                } finally {
                    synchronized (DataObjectPool.this) {
                        atomic = prev;
                        blocked = prevBlocked;
                        DataObjectPool.this.notifyAll ();
                    }
                    exitAllowConstructor (findPrev);
                }
            }
        } // end of WrapAtomicAction
        
        target.getFileSystem ().runAtomicAction(new WrapAtomicAction ());
    }
    
    /** The thread that runs in atomic action wants to delegate its priviledia
     * to somebody else. Used in DataFolder.getChildren that blocks on 
     * Folder Recognizer thread.
     *
     * @param delegate the priviledged processor
     */
    public synchronized void enterPriviledgedProcessor (RequestProcessor delegate) {
        if (atomic == Thread.currentThread()) {
            if (priviledged != null) throw new IllegalStateException ("Previous priviledged is not null: " + priviledged + " now: " + delegate); // NOI18N
            priviledged = delegate;
        }
        // wakeup everyone in enterRecognition, as this changes the conditions there
        notifyAll ();
    }
    
    /** Exits the priviledged processor.
     */
    public synchronized void exitPriviledgedProcessor (RequestProcessor delegate) {
        if (atomic == Thread.currentThread ()) {
            if (priviledged != delegate) throw new IllegalStateException ("Trying to unregister wrong priviledged. Prev: " + priviledged + " now: " + delegate); // NOI18N
            priviledged = null;
        }
        // wakeup everyone in enterRecognition, as this changes the conditions there
        notifyAll ();
    }
    
    /** Ensures it is safe to enter the recognition. 
     * @param fo file object we want to recognize or null if we do not know it
     */
    private synchronized void enterRecognition (FileObject fo) {
        // wait till nobody else stops the recognition
        for (;;) {
            if (atomic == null) {
                // ok, I am the one who can enter
                break;
            }
            if (atomic == Thread.currentThread()) {
                // ok, reentering again
                break;
            }
            
            if (priviledged != null && priviledged.isRequestProcessorThread()) {
                // ok, we have priviledged request processor thread
                break;
            }
            
            if (fo != null && blocked != null && !blocked.equals (fo.getParent ())) {
                // access to a file in different folder than it is blocked
                // => go on
                break;
            }
            
            if (errLog) {
                err.log (ErrorManager.INFORMATIONAL, "Enter recognition block: " + Thread.currentThread()); // NOI18N
                err.log (ErrorManager.INFORMATIONAL, "            waiting for: " + fo); // NOI18N
                err.log (ErrorManager.INFORMATIONAL, "        blocking thread: " + atomic); // NOI18N
                err.log (ErrorManager.INFORMATIONAL, "             blocked on: " + blocked); // NOI18N
            }
            try {
                wait ();
            } catch (InterruptedException ex) {
                // means nothing, go on
            }
        } 
    }
    
    /** Collection of all objects that has been created but their
    * creation has not been yet notified to OperationListener.postCreate
    * method.
    *
    * Set<Item>
    */
    private HashSet toNotify = new HashSet();
    
    private static final Integer ONE = new Integer(1);
    
    /** Constructor.
     */
    private DataObjectPool () {
    }

    

    /** Checks whether there is a data object with primary file
    * passed thru the parameter.
    *
    * @param fo the file to check
    * @return data object with fo as primary file or null
    */
    public DataObject find (FileObject fo) {
        synchronized (this) {
            Item doh = (Item)map.get (fo);
            if (doh == null) {
                return null;
            }
            
            // do not return DOs before their creation were notified to OperationListeners
            if (toNotify.contains (doh)) {
                // special test for data objects calling this method from 
                // their own constructor, those are ok to be returned if
                // they exist
                List l = (List)FIND.get ();
                if (l == null || !l.contains (doh)) {
                    return null;
                }
            }

            return doh.getDataObjectOrNull ();
        }
    }
    
    /** mapping of files to registration count */
    private final Map registrationCounts = new WeakHashMap(); // Map<FileObject,int>
    void countRegistration(FileObject fo) {
        Integer i = (Integer)registrationCounts.get(fo);
        Integer i2;
        if (i == null) {
            i2 = ONE;
        } else {
            i2 = new Integer(i.intValue() + 1);
        }
        registrationCounts.put(fo, i2);
    }
    /** For use from FolderChildren. @see "#20699" */
    int registrationCount(FileObject fo) {
        Integer i = (Integer)registrationCounts.get(fo);
        if (i == null) {
            return 0;
        } else {
            return i.intValue();
        }
    }
    
    /** Refresh of all folders.
    */
    private void refreshAllFolders () {
        Set files;
        synchronized (this) {
            files = new HashSet (map.keySet ());
        }

        Iterator it = files.iterator ();
        while (it.hasNext ()) {
            FileObject fo = (FileObject)it.next ();
            if (fo.isFolder ()) {
                DataObject obj = find (fo);
                if (obj instanceof DataFolder) {
                    DataFolder df = (DataFolder)obj;
                    FileObject file = df.getPrimaryFile ();
                    synchronized (this) {
                        if (toNotify.isEmpty() || !toNotify.contains((Item)map.get(file))) {
                            FolderList.changedDataSystem (file);
                        }
                    }
                }
            }
        }
    }

    /** Rescans all fileobjects in given set.
    * @param s mutable set of FileObjects
    * @return set of DataObjects that refused to be revalidated
    */
    public Set revalidate (Set s) {
        return VALIDATOR.revalidate (s);
    }

    /** Rescan all primary files of currently existing data
    * objects.
    *
    * @return set of DataObjects that refused to be revalidated
    */
    public Set revalidate () {
        Set files;
        synchronized (this) {
            files = createSetOfAllFiles (map.values ());
        }

        return revalidate (files);
    }

    /** Notifies that an object has been created.
     * @param obj the object that was created
    */
    public void notifyCreation (DataObject obj) {
        notifyCreation (obj.item);
    }

    private static final DataLoaderPool lp = DataLoaderPool.getDefault();
    
    /** Notifies the creation of an item*/
    private void notifyCreation (Item item) {
        synchronized (this) {
            if (errLog) {
                err.log (ErrorManager.INFORMATIONAL, "Notify created: " + item + " by " + Thread.currentThread()); // NOI18N
            }
            
            if (toNotify.isEmpty()) {
                if (errLog) {
                    err.log (ErrorManager.INFORMATIONAL, "  but toNotify is empty"); // NOI18N
                }
                return;
            }
            
            if (!toNotify.remove (item)) {
                if (errLog) {
                    err.log (ErrorManager.INFORMATIONAL, "  the item is not there: " + toNotify); // NOI18N
                }
                return;
            }
            
            // if somebody is caught in waitNotified then wake him up
            notifyAll ();
        }
        
        DataObject obj = item.getDataObjectOrNull ();
        if (obj != null) {
            lp.fireOperationEvent (
                new OperationEvent (obj), OperationEvent.CREATE
            );
        }
    }
    
    /** Notifies all objects in the list */
    private void notifyCreationAll (List l) {
        if (l.isEmpty()) return;
        
        Iterator iter = l.iterator();

        // iter has a lot of objects
        while (iter.hasNext ()) {
            DataObjectPool.Item i = (DataObjectPool.Item)iter.next ();
            notifyCreation (i);
        }
    }
    /** Wait till the data object will be notified. But wait limited amount
     * of time so we will not deadlock
     *
     * @param obj data object to check
     */
    public void waitNotified (DataObject obj) {
        for (;;) {
            try {
                synchronized (this) {
                    enterRecognition (obj.getPrimaryFile().getParent());

                    if (toNotify.isEmpty()) {
                        return;
                    }

                    List l = (List)FIND.get ();
                    if (l != null && l.contains (obj.item)) {
                        return;
                    }

                    if (!toNotify.contains (obj.item)) {
                        return;
                    }

                    if (errLog) {
                        err.log (ErrorManager.INFORMATIONAL, "waitTillNotified: " + Thread.currentThread()); // NOI18N
                        err.log (ErrorManager.INFORMATIONAL, "      waitingFor: " + obj.getPrimaryFile()); // NOI18N
                    }

                    wait ();
                }
            } catch (InterruptedException ex) {
                // never mind
            }
        }
    }
        
    
    /** Add to list of created objects.
     */
    private void notifyAdd (Item item) {
        toNotify.add (item);
        List l = (List)FIND.get ();
        if (l == TOKEN) FIND.set (l = new ArrayList());
        l.add (item);
    }

    
    /** Listener used to distribute the File events to their DOs.
     * [pnejedly] A little bit about its internals/motivation:
     * Originally, every created DO have hooked its onw listener to the primary
     * FO's parent folder for listening on primary FO changes. The listener
     * was enhanced in MDO to also cover secondaries.
     * Now there is one FSListener per FileSystem which have to distribute
     * the events to the DOs using limited DOPool's knowledge about FO->DO
     * mapping. Because the mapping knowledge is limited to primary FOs only,
     * it have to resort to notifying all known DOs for given folder
     * if the changed file is not known. Although it is not as good as direct
     * notification used for known primaries, it is still no worse than
     * all DOs listening on their folder themselves as it spares at least
     * the zillions of WeakListener instances.
     */
    private final class FSListener extends FileChangeAdapter {
        FSListener() {}
        /**
         * @return Iterator<Item>
         */
        private Iterator getTargets(FileEvent fe) {
            FileObject fo = fe.getFile();
            List toNotify = new LinkedList();
            // The FileSystem notifying us about the changes should
            // not hold any lock so we're safe here
            synchronized (DataObjectPool.this) {
                Item itm = (Item)map.get (fo);
                if (itm != null) { // the file was someones' primary
                    toNotify.add(itm); // so notify only owner
                } else { // unknown file or someone secondary
                    FileObject parent = fo.getParent();
                    if (parent != null) { // the fo is not root
                        FileObject[] siblings = parent.getChildren();
                        // notify all in folder
                        for (int i=0; i<siblings.length; i++) { 
                            itm = (Item)map.get (siblings[i]);
                            if (itm != null) toNotify.add(itm);
                        }
                    }
                }
            }
            return toNotify.iterator();
        }

        public void fileRenamed (FileRenameEvent fe) {
            for( Iterator it = getTargets(fe); it.hasNext(); ) {
                DataObject dobj = ((Item)it.next()).getDataObjectOrNull();
                if (dobj != null) dobj.notifyFileRenamed(fe);
            }
        }

        public void fileDeleted (FileEvent fe) {
            for( Iterator it = getTargets(fe); it.hasNext(); ) {
                DataObject dobj = ((Item)it.next()).getDataObjectOrNull();
                if (dobj != null) dobj.notifyFileDeleted(fe);
            }
        }

        public void fileDataCreated (FileEvent fe) {
            for( Iterator it = getTargets(fe); it.hasNext(); ) {
                DataObject dobj = ((Item)it.next()).getDataObjectOrNull();
                if (dobj != null) dobj.notifyFileDataCreated(fe);
            }
        }
        
        public void fileAttributeChanged (FileAttributeEvent fe) {
            for( Iterator it = getTargets(fe); it.hasNext(); ) {
                DataObject dobj = ((Item)it.next()).getDataObjectOrNull();
                if (dobj != null) dobj.notifyAttributeChanged(fe);
            }
        }
    }
    
    /** Registers new DataObject instance.
    * @param fo primary file for obj
    * @param loader the loader of the object to be created
    *
    * @return object with common information for this <CODE>DataObject</CODE>
    * @exception DataObjectExistsException if the file object is already registered
    */
    public Item register (FileObject fo, DataLoader loader) throws DataObjectExistsException {
        if (FIND.get () == null) throw new IllegalStateException ("DataObject constructor can be called only thru DataObject.find - use that method"); // NOI18N
        
        // here we're registering a listener on fo's FileSystem so we can deliver
        // fo changes to DO without lots of tiny listeners on folders
        // The new DS bound to a repository can simply place a single listener
        // on its repository instead of registering listeners on FileSystems. 
        try { // to register a listener of fo's FileSystem
            FileSystem fs = fo.getFileSystem();
            synchronized (knownFileSystems) {
                if (! knownFileSystems.contains(fs)) {
                    fs.addFileChangeListener (new FSListener());
                    knownFileSystems.add(fs);
                }
            }
        } catch (FileStateInvalidException e ) {
            // no need to listen then
        }
        
        Item doh;
        DataObject obj;
        synchronized (this) {
            doh = (Item)map.get (fo);
            // if Item for this file has not been created yet
            if (doh == null) {
                doh = new Item (fo);
                map.put (fo, doh);
                countRegistration(fo);
                notifyAdd (doh);

                VALIDATOR.notifyRegistered (fo);

                return doh;
            }
            
            obj = doh.getDataObjectOrNull ();

            if (obj == null) {
                // the item is to be finalize => create new
                doh = new Item (fo);
                map.put (fo, doh);
                countRegistration(fo);
                notifyAdd (doh);

                return doh;
            }
            
            if (!VALIDATOR.reregister (obj, loader)) {
                throw new DataObjectExistsException (obj);
            }
        }
        
        try {
            obj.setValid (false);
            synchronized (this) {
                // check if there isn't any new data object registered 
                // when this thread left synchronization block.
                Item doh2 = (Item)map.get (fo);
                if (doh2 == null) {
                    doh = new Item (fo);
                    map.put (fo, doh);
                    countRegistration(fo);
                    notifyAdd (doh);

                    return doh;
                }
            }
        } catch (java.beans.PropertyVetoException ex) {
            VALIDATOR.refusingObjects.add (obj);
        }
        throw new DataObjectExistsException (obj);
    }

    /** Notifies all newly created objects to

    /** Deregister.
    * @param item the item with common information to deregister
    * @param refresh true if the parent folder should be refreshed
    */
    private synchronized void deregister (Item item, boolean refresh) {
        FileObject fo = item.primaryFile;

        Item previous = (Item)map.remove (fo);

        if (previous != null && previous != item) {
            // ops, mistake,
            // return back the original
            map.put (fo, previous);
            countRegistration(fo);
            // Furthermore, item is probably in toNotify by mistake.
            // Observed in DataFolderTest.testMove: after vetoing the move
            // of a data folder, the bogus item for the temporary new folder
            // (e.g. BB/AAA/A1) is left in the toNotify pool forever. This
            // point is reached; remove it now. -jglick
            if (toNotify.remove(item)) {
                notifyAll();
            }
            return;
        }

        // refresh of parent folder
        if (refresh) {
            fo = fo.getParent ();
            if (fo != null) {
                Item item2 = (Item)map.get (fo);
                if (item2 != null) {
                    DataFolder df = (DataFolder) item2.getDataObjectOrNull();
                    if (df != null) {
                        VALIDATOR.refreshFolderOf (df);
                    }
                }
            }
        }
    }

    /** Changes the primary file to new one.
    * @param item the item to change
    * @param newFile new primary file to set
    */
    private synchronized void changePrimaryFile (
        Item item, FileObject newFile
    ) {
        map.remove (item.primaryFile);
        item.primaryFile = newFile;
        map.put (newFile, item);
        countRegistration(newFile);
    }

    /** When the loader pool is changed, then all objects are rescanned.
    */
    public void stateChanged (javax.swing.event.ChangeEvent ev) {
        Set set;
        synchronized (this) {
            // copy the values synchronously
            set = new HashSet (map.values ());
        }
        set = createSetOfAllFiles (set);
        revalidate (set);
    }
    
    /** Create list of all files for given collection of data objects.
    * @param c collection of DataObjectPool.Item
    * @return set of files
    */
    private static Set createSetOfAllFiles (Collection c) {
        HashSet set = new HashSet (c.size () * 7);
        
        Iterator it = c.iterator();
        while (it.hasNext()) {
            Item item = (Item)it.next ();
            DataObject obj = item.getDataObjectOrNull ();
            if (obj != null) {
                getPOOL ().waitNotified (obj);
                set.addAll (obj.files ());
            }
        }
        return set;
    }
    
    /** Remove DataObjects which became invalid thanks
     * to unmounting a FileSystem
     */
    private void removeInvalidObjects() {
        Set files;
        synchronized (this) {
            files = new HashSet (map.values ());
        }
        files = createSetOfAllFiles (files);
        
        VALIDATOR.removeInvalidObject(files);
    }

    //
    // Repository listener changes
    //
    
    /** All filesystems removed from repository, until GCed */
    private static WeakSet fsRemovedFromRepository = new WeakSet ();
    
    /** Called when new file system is added to the pool.
     * @param ev event describing the action
     */
    public void fileSystemAdded(RepositoryEvent ev) {
        ev.getFileSystem().addPropertyChangeListener( getPOOL() );
        fsRemovedFromRepository.remove (ev.getFileSystem ());
    }
    /** Called when a file system is removed from the pool.
     * @param ev event describing the action
     */
    public void fileSystemRemoved(RepositoryEvent ev) {
        ev.getFileSystem().removePropertyChangeListener( getPOOL() );
        fsRemovedFromRepository.add (ev.getFileSystem ());
        removeInvalidObjects();
    }

    /** Called when a file system pool is reordered. */
    public void fileSystemPoolReordered(RepositoryReorderedEvent ev) {
    }
    
    /** Called when a file system property changed.
     * If it's property root, check validity.
     * @param ev event describing the action     
    */
    public void propertyChange (final PropertyChangeEvent ev) {
        if (FileSystem.PROP_SYSTEM_NAME.equals (ev.getPropertyName ())) {
            removeInvalidObjects();
        }        
        if (FileSystem.PROP_ROOT.equals (ev.getPropertyName ())) {
            removeInvalidObjects();
        }        
    }

    /** Returns all currently existing data
    * objects.
    *
    * @return iterator of DataObjectPool.Item
    */    
    Iterator getActiveDataObjects () {
        synchronized (this) {
            ArrayList alist = new ArrayList(map.values());
            return alist.iterator();
        }
    }

    /** One item in object pool.
    */
    static final class Item extends Object {
        /** initial value of obj field. */
        private static final Reference REFERENCE_NOT_SET = new WeakReference(null);

        /** weak reference data object with this primary file */
        private Reference obj = REFERENCE_NOT_SET;
        
        /** primary file */
        FileObject primaryFile;
        
        // [PENDING] hack to check the stack when the DataObject has been created
        //    private Exception stack;

        /** @param fo primary file
        * @param pool object pool
        */
        public Item (FileObject fo) {
            this.primaryFile = fo;

            // [PENDING] // stores stack
            /*      java.io.StringWriter sw = new java.io.StringWriter ();
                  stack = new Exception ();
                }

                // [PENDING] toString returns original stack
                public String toString () {
                  return stack.toString ();*/
        }

        /** Setter for the data object. Called immediatelly as possible.
        * @param obj the data object for this item
        */
        public void setDataObject (DataObject obj) {
            this.obj = new ItemReference (obj, this);
            
            if (obj != null && !obj.getPrimaryFile ().isValid ()) {
                // if the primary file is already invalid =>
                // mark the object as invalid
                deregister (false);
            }
            
            synchronized (DataObjectPool.getPOOL()) {
                DataObjectPool.getPOOL().notifyAll();
            }
        }

        /** Getter for the data object.
        * @return the data object or null
        */
        DataObject getDataObjectOrNull () {
            synchronized (DataObjectPool.getPOOL()) {
                while (this.obj == REFERENCE_NOT_SET) {
                    try {
                        DataObjectPool.getPOOL().wait ();
                    }
                    catch (InterruptedException exc) {
                    }
                }
            }
            
            return this.obj == null ? null : (DataObject)this.obj.get ();
        }
        
        /** Getter for the data object.
        * @return the data object
        * @exception IllegalStateException if the data object has been lost
        *   due to weak references (should not happen)
        */
        public DataObject getDataObject () {
            DataObject obj = getDataObjectOrNull ();
            if (obj == null) {
                throw new IllegalStateException ();
            }
            return obj;
        }

        /** Deregister one reference.
        * @param refresh true if the parent folder should be refreshed
        */
        public void deregister (boolean refresh) {
            getPOOL().deregister (this, refresh);
        }

        /** Changes the primary file to new one.
        * @param newFile new primary file to set
        */
        public void changePrimaryFile (FileObject newFile) {
            getPOOL().changePrimaryFile (this, newFile);
        }

        /** Is the item valid?
        */
        public boolean isValid () {
            if (getPOOL().map.get (primaryFile) == this) {
                return primaryFile.isValid();
            } else {
                return false;
            }
            
        }
        
        public String toString () {
            DataObject obj = (DataObject)this.obj.get ();
            if (obj == null) {
                return "nothing[" + primaryFile + "]"; // NOI18N
            }
            return obj.toString ();
        }
    }

    /** WeakReference - references a DataObject, strongly references an Item */
    static final class ItemReference extends WeakReference 
    implements Runnable {
        /** Reference to an Item */
        private Item item;
        
        ItemReference(DataObject dobject, Item item) {
            super(dobject, org.openide.util.Utilities.activeReferenceQueue());
            this.item = item;
        }

        /** Does the cleanup of the reference */
        public void run () {
            item.deregister(false);
            item = null;
        }
        
    }
    
    /** Validator to allow rescan of files.
    */
    private static final class Validator extends Object
        implements DataLoader.RecognizedFiles {
        /** set of all files that should be revalidated (FileObject) */
        private Set files;
        /** current thread that is in the validator */
        private Thread current;
        /** number of threads waiting to enter the validation */
        private int waiters;
        /** Number of calls to enter by current thread minus 1 */
        private int reenterCount;
        /** set of files that has been marked recognized (FileObject) */
        private HashSet recognizedFiles;
        /** set with all objects that refused to be discarded (DataObject) */
        private HashSet refusingObjects;
        /** set of files that has been registered during revalidation */
        private HashSet createdFiles;

	Validator() {}

        /** Enters the section.
        * @param set mutable set of files that should be processed
        * @return the set of files concatenated with any previous sets
        */
        private synchronized Set enter (Set set) {
            if (current == Thread.currentThread ()) {
                reenterCount++;
            } else {
                waiters++;
                while (current != null) {
                    try {
                        wait ();
                    } catch (InterruptedException ex) {
                    }
                }
                current = Thread.currentThread ();
                waiters--;
            }
            
            if (files == null) {
                files = set;
            } else {
                files.addAll (set);
            }

            return files;
        }

        /** Leaves the critical section.
        */
        private synchronized void exit () {
            if (reenterCount == 0) {
                current = null;
                if (waiters == 0) {
                    files = null;
                }
                notify ();
            } else {
                reenterCount--;
            }
        }

        /** If there is another waiting thread, then I can
        * cancel my computation.
        */
        private synchronized boolean goOn () {
            return waiters == 0;
        }

        /** Called to either refresh folder, or register the folder to be
        * refreshed later is validation is in progress.
        */
        public void refreshFolderOf (DataFolder df) {
            if (createdFiles == null) {
                // no validator in progress
                FolderList.changedDataSystem (df.getPrimaryFile ());
            }
        }

        /** Mark this file as being recognized. It will be excluded
        * from further processing.
        *
        * @param fo file object to exclude
        */
        public void markRecognized (FileObject fo) {
            recognizedFiles.add (fo);
        }

        public void notifyRegistered (FileObject fo) {
            if (createdFiles != null) {
                createdFiles.add (fo);
            }
        }

        /** Reregister new object for already existing file object.
        * @param obj old object existing
        * @param loader loader of new object to create
        * @return true if the old object has been discarded and new one can
        *    be created
        */
        public boolean reregister (DataObject obj, DataLoader loader) {
            if (recognizedFiles == null) {
                // revalidation not in progress
                return false;
            }

            if (obj.getLoader () == loader) {
                // no change in loader =>
                return false;
            }

            if (createdFiles.contains (obj.getPrimaryFile ())) {
                // if the file already has been created
                return false;
            }

            if (refusingObjects.contains (obj)) {
                // the object has been refused before
                return false;
            }

            return true;
        }

        /** Rescans all fileobjects in given set.
        * @param s mutable set of FileObjects
        * @return set of objects that refused to be revalidated
        */
        public Set revalidate (Set s) {
            
            // ----------------- fix of #30559 START
            if ((s.size() == 1) && (current == Thread.currentThread ())) {
                if (files != null && files.contains(s.iterator().next())) {
                    return new HashSet();
                }
            }
            // ----------------- fix of #30559 END
            
            // holds all created object, so they are not garbage
            // collected till this method ends
            LinkedList createObjects = new LinkedList ();
            try {
                
                s = enter (s);
                
                recognizedFiles = new HashSet ();
                refusingObjects = new HashSet ();
                createdFiles = new HashSet ();

                DataLoaderPool pool = lp;
                Iterator it = s.iterator ();
                while (it.hasNext () && goOn ()) {
                    try {
                        FileObject fo = (FileObject)it.next ();
                        if (!recognizedFiles.contains (fo)) {
                            // first of all test if the file is on a valid filesystem
                            boolean invalidate;
                            try {
                                FileSystem fs = fo.getFileSystem ();
                                invalidate = fsRemovedFromRepository.contains (fs);
                            } catch (FileStateInvalidException ex) {
                                invalidate = true;
                            }

                            // the previous data object should be canceled
                            DataObject orig = getPOOL().find (fo);
                            if (orig == null) {
                                // go on
                                continue;
                            }

                            if (!invalidate) {
                                // findDataObject
                                // is not using method DataObjectPool.find to locate data object
                                // directly for primary file, that is good
                                DataObject obj = pool.findDataObject (fo, this);
                                createObjects.add (obj);

                                invalidate = obj != orig;
                            }

                            if (invalidate) {
                                it.remove();                                
                                try {
                                    orig.setValid (false);
                                } catch (java.beans.PropertyVetoException ex) {
                                    refusingObjects.add (orig);
                                }
                            }
                        }
                    } catch (DataObjectExistsException ex) {
                        // this should be no problem here
                    } catch (java.io.IOException ioe) {
                        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ioe);
                    } catch (ConcurrentModificationException cme) {
                        // not very nice but the only way I could come up to handle this:
                        // java.util.ConcurrentModificationException
                        //   at java.util.HashMap$HashIterator.remove(HashMap.java:755)
                        //   at org.openide.loaders.DataObjectPool$Validator.revalidate(DataObjectPool.java:916)
                        //   at org.openide.loaders.DataObjectPool.revalidate(DataObjectPool.java:203)
                        //   at org.openide.loaders.DataObjectPool.stateChanged(DataObjectPool.java:527)
                        //   at org.openide.loaders.DataLoaderPool$1.run(DataLoaderPool.java:128)
                        //   at org.openide.util.Task.run(Task.java:136)
                        //[catch] at org.openide.util.RequestProcessor$Processor.run(RequestProcessor.java:635)
                        // is to ignore the exception and continue
                        it = s.iterator();
                    }
                }
                return refusingObjects;
            } finally {
                recognizedFiles = null;
                refusingObjects = null;
                createdFiles = null;

                exit ();

                if ( s.size() > 1 )
                    getPOOL().refreshAllFolders ();
            }
        }
        
        /** Remove DataObjects which became invalid thanks
         * to unmounting a FileSystem
         */
        void removeInvalidObject(Set files) {
            try {
                files = enter(files);
                
                Iterator it = files.iterator();
                while (it.hasNext() && goOn()) {
                    FileObject fo = (FileObject) it.next();
                    
                    boolean invalidate = !fo.isValid();
                    if ( !invalidate ) {
                        try {
                            FileSystem fs = fo.getFileSystem ();
                            invalidate = fsRemovedFromRepository.contains (fs);
                        } catch (FileStateInvalidException ex) {
                            invalidate = true;
                        }
                    }

                    DataObject orig = null;
                    synchronized (getPOOL()) {
                        Item itm = (Item)getPOOL().map.get (fo);
                        if (itm == null) {
                            continue;
                        }
                        orig = itm.getDataObjectOrNull ();
                    }

                    if (invalidate && orig != null) {
                        it.remove();
                        try {                            
                            orig.setValid (false);
                        } catch (java.beans.PropertyVetoException ex) {
                            // silently ignore?
                            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                        }
                    }
                }
            } finally {
                exit();
            }
        }
    } // end of Validator
}
