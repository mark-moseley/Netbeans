/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 
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

package org.netbeans.modules.cnd.makeproject.api.configurations;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.api.project.NativeFileItemSet;
import org.netbeans.modules.cnd.loaders.CndDataObject;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.NbBundle;

public class Folder {
    public static final String DEFAULT_FOLDER_NAME = "f"; // NOI18N
    public static final String DEFAULT_FOLDER_DISPLAY_NAME = getString("NewFolderName");
    
    private ConfigurationDescriptor configurationDescriptor;
    private final String name;
    private String displayName;
    private final Folder parent;
    private Vector items = null; // Folder or Item
    private Vector changeListenerList = null;
    private final boolean projectFiles;
    private String id = null;
    
    public Folder(ConfigurationDescriptor configurationDescriptor, Folder parent, String name, String displayName, boolean projectFiles) {
        this.configurationDescriptor = configurationDescriptor;
        this.parent = parent;
        this.name = name;
        this.displayName = displayName;
        this.projectFiles = projectFiles;
        this.items = new Vector();
        this.changeListenerList = new Vector();
    }
    
    public int size() {
        return items.size();
    }
    
    public Folder getParent() {
        return parent;
    }
    
    public Project getProject() {
        return ((MakeConfigurationDescriptor)getConfigurationDescriptor()).getProject();
    }
    
    public String getName() {
        return name;
    }
    
    public String getPath() {
        StringBuilder builder = new StringBuilder(getName()); 
        Folder parent = getParent();
        while (parent != null) {
            if (parent.getParent() != null) {
                builder.insert(0, '/'); // NOI18N
                builder.insert(0, parent.getName());
            }
            parent = parent.getParent();
        };
        return builder.toString();
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
        configurationDescriptor.setModified();
    }
    
    public ConfigurationDescriptor getConfigurationDescriptor() {
        return configurationDescriptor;
    }
    
    public void setConfigurationDescriptor(ConfigurationDescriptor configurationDescriptor) {
        this.configurationDescriptor = configurationDescriptor;
    }
    
    public boolean isProjectFiles() {
        return projectFiles;
    }
    
    public Vector getElements() {
        return items;
    }
    
    public void addElement(Object element) { // FIXUP: shopuld be private
        // Always keep the vector sorted
        int indexAt = -1;
        if (element instanceof Item) {
            String name1 = ((Item)element).getSortName();
            indexAt = 0;
            while (indexAt < items.size()) {
                Object o = items.elementAt(indexAt);
                if (!(o instanceof Item)) {
                    indexAt++;
                    continue;
                }
                String name2 = ((Item)o).getSortName();
                int compareRes = name1.compareTo(name2);
                if (compareRes > 0) {
                    indexAt++;
                    continue;
                }
                break;
            }
        } else if (element instanceof Folder) {
            if (((Folder)element).isProjectFiles()) {
                Object lastElement = null;
                if (items.size() > 0) {
                    lastElement = items.elementAt(items.size()-1);
                    if (lastElement instanceof Folder && !((Folder)lastElement).isProjectFiles()) {
                        indexAt = items.size() - 1;
                    }
                }
            }
        }
        if (indexAt >= 0) {
            items.add(indexAt, element);
        } else {
            items.add(element);
        }
        fireChangeEvent();
    }
    
    public Item addItemAction(Item item) {
        addItem(item);
        ArrayList list = new ArrayList(1);
        list.add(item);
        ((MakeConfigurationDescriptor)configurationDescriptor).fireFilesAdded(list);
        return item;
    }
    
    public Item addItem(Item item) {
        if (item == null)
            return item;
        // Check if already in project. Silently ignore if already there.
        if (isProjectFiles() && ((MakeConfigurationDescriptor)configurationDescriptor).findProjectItemByPath(item.getPath()) != null) {
            System.err.println("Folder - addItem - item ignored, already added: " + item); // NOI18N  // FIXUP: correct?
            return item;
        }
        // Add it to the folder
        item.setFolder(this);
        addElement(item);
        
        // Add item to the dataObject's lookup
        if (isProjectFiles()) {
            if (item.getDataObject() instanceof CndDataObject) {
                CndDataObject dataObject = (CndDataObject)item.getDataObject();
                MyNativeFileItemSet myNativeFileItemSet = (MyNativeFileItemSet)dataObject.getCookie(MyNativeFileItemSet.class);
                if (myNativeFileItemSet == null) {
                    myNativeFileItemSet = new MyNativeFileItemSet();
                    dataObject.addCookie(myNativeFileItemSet);
                }
                myNativeFileItemSet.add(item);
            }
        }
        
        // Add it to project Items
        if (isProjectFiles()) {
            ((MakeConfigurationDescriptor)configurationDescriptor).addProjectItem(item);
            // Add configuration to all configurations
            if (configurationDescriptor.getConfs() == null)
                return item;
            Configuration[] configurations = configurationDescriptor.getConfs().getConfs();
            for (int i = 0; i < configurations.length; i++) {
                FolderConfiguration folderConfiguration = getFolderConfiguration(configurations[i]);
                configurations[i].addAuxObject(new ItemConfiguration(configurations[i], item));
            }
        }
        
        return item;
    }
    
    public void addFolder(Folder folder) {
        addElement(folder);
        if (isProjectFiles()) {
            // Add configuration to all configurations
            if (configurationDescriptor.getConfs() == null)
                return;
            Configuration[] configurations = configurationDescriptor.getConfs().getConfs();
            for (int i = 0; i < configurations.length; i++) {
                folder.getFolderConfiguration(configurations[i]);
            }
        }
    }
    
    /**
     * Returns an unique id (String) used to retrive this object from the
     * pool of aux objects
     */
    public String getId() {
        if (id == null) {
            id = "f-" + getPath(); // NOI18N
        }
        return id;
    }
    
    public FolderConfiguration getFolderConfiguration(Configuration configuration) {
        FolderConfiguration folderConfiguration = null;
        if (isProjectFiles()) {
            String id = getId();
            folderConfiguration = (FolderConfiguration)configuration.getAuxObject(getId());
            if (folderConfiguration == null) {
                CCompilerConfiguration parentCCompilerConfiguration;
                CCCompilerConfiguration parentCCCompilerConfiguration;
                FolderConfiguration parentFolderConfiguration = null;
                if (getParent() != null)
                    parentFolderConfiguration = getParent().getFolderConfiguration(configuration);
                if (parentFolderConfiguration != null) {
                    parentCCompilerConfiguration = parentFolderConfiguration.getCCompilerConfiguration();
                    parentCCCompilerConfiguration = parentFolderConfiguration.getCCCompilerConfiguration();
                }
                else {
                    parentCCompilerConfiguration = ((MakeConfiguration)configuration).getCCompilerConfiguration();
                    parentCCCompilerConfiguration = ((MakeConfiguration)configuration).getCCCompilerConfiguration();
                }
                folderConfiguration = new FolderConfiguration(configuration, parentCCompilerConfiguration, parentCCCompilerConfiguration, this);
                configuration.addAuxObject(folderConfiguration);
            }
        }
        return folderConfiguration;
    }
    
    public Folder addNewFolder(boolean projectFiles) {
        String name;
        String displayName;
        for (int i = 1;; i++) {
            name = DEFAULT_FOLDER_NAME + i;
            displayName = DEFAULT_FOLDER_DISPLAY_NAME + " " + i; // NOI18N
            if (findFolderByName(name) == null)
                break;
        }
        return addNewFolder(name, displayName, projectFiles); // NOI18N
    }
    
    public Folder addNewFolder(String name, String displayName, boolean projectFiles) {
        Folder newFolder = new Folder(getConfigurationDescriptor(), this, name, displayName, projectFiles);
        addFolder(newFolder);
        return newFolder;
    }
    
    public boolean removeItemAction(Item item) {
        ArrayList list = new ArrayList(1);
        list.add(item);
        ((MakeConfigurationDescriptor)configurationDescriptor).fireFilesRemoved(list);
        return removeItem(item);
    }
    
    public boolean removeItem(Item item) {
        boolean ret = false;
        if (item == null)
            return false;
        // Remove it from folder
        ret = items.removeElement(item);
        if (!ret)
            return ret;
        
        // Remove item from the dataObject's lookup
        if (isProjectFiles()) {
            if (item.getDataObject() instanceof CndDataObject) {
                CndDataObject dataObject = (CndDataObject)item.getDataObject();
                MyNativeFileItemSet myNativeFileItemSet = (MyNativeFileItemSet)dataObject.getCookie(MyNativeFileItemSet.class);
                if (myNativeFileItemSet != null) {
                    myNativeFileItemSet.remove(item);
                    if (myNativeFileItemSet.isEmpty())
                        dataObject.removeCookie(myNativeFileItemSet);
                }
            }
        }
        
//	item.setFolder(null);
        if (isProjectFiles()) {
            // Remove it from project Items
            ((MakeConfigurationDescriptor)configurationDescriptor).removeProjectItem(item);
            // Remove it form all configurations
            Configuration[] configurations = configurationDescriptor.getConfs().getConfs();
            for (int i = 0; i < configurations.length; i++)
                configurations[i].removeAuxObject(item.getId()/*ItemConfiguration.getId(item.getPath())*/);
        }
        item.removePropertyChangeListener();
        item.setFolder(null);
        fireChangeEvent();
        return ret;
    }
    
    public boolean removeItemByPath(String path) {
        boolean ret = false;
        Item item = findItemByPath(path);
        return removeItem(item);
    }
    
    public boolean removeFolderAction(Folder folder) {
        ((MakeConfigurationDescriptor)configurationDescriptor).fireFilesRemoved(folder.getAllItemsAsList());
        return removeFolder(folder);
    }
    
    public boolean removeFolder(Folder folder) {
        boolean ret = false;
        if (folder != null) {
            folder.removeAll();
            ret = items.removeElement(folder);
            if (isProjectFiles()) {
                // Remove it form all configurations
                Configuration[] configurations = configurationDescriptor.getConfs().getConfs();
                for (int i = 0; i < configurations.length; i++)
                    configurations[i].removeAuxObject(folder.getId());
            }
        }
        if (ret)
            fireChangeEvent();
        return ret;
    }
    
    /**
     * Remove all items and folders recursively
     */
    public void removeAll() {
        Item[] itemsToRemove = getItemsAsArray();
        Folder[] foldersToRemove = getFoldersAsArray();
        for (int i = 0; i < itemsToRemove.length; i++)
            removeItem(itemsToRemove[i]);
        for (int i = 0; i < foldersToRemove.length; i++)
            removeFolder(foldersToRemove[i]);
    }
    
    public void reset() {
        items = new Vector();
        fireChangeEvent();
    }
    
    public Item findItemByPath(String path) {
        if (path == null)
            return null;
        Item[] items = getItemsAsArray();
        for (int i = 0; i < items.length; i++) {
            if (path.equals(items[i].getPath()))
                return items[i];
        }
        return null;
    }
    
    public Folder findFolderByName(String name) {
        if (name == null)
            return null;
        Folder[] folders = getFoldersAsArray();
        for (int i = 0; i < folders.length; i++) {
            if (name.equals(folders[i].getName()))
                return folders[i];
        }
        return null;
    }
    
    public Folder findFolderByDisplayName(String name) {
        if (name == null)
            return null;
        Folder[] folders = getFoldersAsArray();
        for (int i = 0; i < folders.length; i++) {
            if (name.equals(folders[i].getDisplayName()))
                return folders[i];
        }
        return null;
    }
    
    public Folder findFolderByPath(String path) {
        int i = path.indexOf('/');
        if (i >= 0) {
            String name = path.substring(0, i);
            Folder folder = findFolderByName(name);
            if (folder == null)
                return null;
            return folder.findFolderByPath(path.substring(i+1));
        }
        else
            return findFolderByName(path);
    }
    
    public Item[] getItemsAsArray() {
        Vector found = new Vector();
        Iterator iter = new ArrayList(getElements()).iterator();
        while (iter.hasNext()) {
            Object o = iter.next();
            if (o instanceof Item)
                found.add(o);
        }
        return (Item[])found.toArray(new Item[found.size()]);
    }
    
    public List getAllItemsAsList() {
        ArrayList found = new ArrayList();
        Iterator iter = new ArrayList(getElements()).iterator();
        while (iter.hasNext()) {
            Object o = iter.next();
            if (o instanceof Item)
                found.add(o);
            if (o instanceof Folder) {
                List items = ((Folder)o).getAllItemsAsList();
                found.addAll(items);
            }
        }
        return found;
    }
    
    
    public Item[] getAllItemsAsArray() {
        List list = getAllItemsAsList();
        return (Item[])list.toArray(new Item[list.size()]);
    }
    
    /*
     * Returns a set of all files in this logical folder as FileObjetc's
     */
    public Set/*<FileObject>*/ getItemsAsFileObjectSet() {
        Vector files = new Vector();
        Iterator iter = new ArrayList(getElements()).iterator();
        while (iter.hasNext()) {
            Item item = (Item)iter.next();
            if (item instanceof Item) {
                FileObject fo  = item.getFileObject();
                if (fo != null)
                    files.add(fo);
            }
        }
        return new LinkedHashSet(files);
    }
    
    /*
     * Returns a set of all files in this logical folder and subfolders as FileObjetc's
     */
    public Set/*<FileObject>*/ getAllItemsAsFileObjectSet(boolean projectFilesOnly) {
        Vector files = new Vector();
        
        if (!projectFilesOnly || isProjectFiles()) {
            Iterator iter = new ArrayList(getElements()).iterator();
            while (iter.hasNext()) {
                Object item = iter.next();
                if (item instanceof Item) {
                    FileObject fo  = ((Item)item).getFileObject();
                    if (fo != null)
                        files.add(fo);
                }
                if (item instanceof Folder) {
                    files.addAll(((Folder)item).getAllItemsAsFileObjectSet(projectFilesOnly));
                }
            }
        }
        
        return new LinkedHashSet(files);
    }
    
    /*
     * Returns a set of all files in this logical folder as FileObjetc's
     */
    public Set/*<DataObject>*/ getItemsAsDataObjectSet(String MIMETypeFilter) {
        Vector files = new Vector();
        Iterator iter = new ArrayList(getElements()).iterator();
        while (iter.hasNext()) {
            Item item = (Item)iter.next();
            if (item instanceof Item) {
                DataObject da  = item.getDataObject();
                if (da != null && (MIMETypeFilter == null || da.getPrimaryFile().getMIMEType().contains(MIMETypeFilter)))
                    files.add(da);
            }
        }
        return new LinkedHashSet(files);
    }
    
    /*
     * Returns a set of all files in this logical folder and subfolders as FileObjetc's
     */
    public Set/*<DataObject>*/ getAllItemsAsDataObjectSet(boolean projectFilesOnly, String MIMETypeFilter) {
        Vector files = new Vector();
        
        if (!projectFilesOnly || isProjectFiles()) {
            Iterator iter = new ArrayList(getElements()).iterator();
            while (iter.hasNext()) {
                Object item = iter.next();
                if (item instanceof Item) {
                    DataObject da  = ((Item)item).getDataObject();
                    if (da != null && (MIMETypeFilter == null || da.getPrimaryFile().getMIMEType().contains(MIMETypeFilter)))
                        files.add(da);
                }
                if (item instanceof Folder) {
                    files.addAll(((Folder)item).getAllItemsAsDataObjectSet(projectFilesOnly, MIMETypeFilter));
                }
            }
        }
        
        return new LinkedHashSet(files);
    }
    
    public String[] getItemNamesAsArray() {
        Item[] items = getItemsAsArray();
        String[] names = new String[items.length];
        for (int i = 0; i < items.length; i++)
            names[i] = items[i].getPath();
        return names;
    }
    
    /*
     * Returns a set of all logical folder in this folder as an array
     */
    public Folder[] getFoldersAsArray() {
        Vector folders = getFolders();
        return (Folder[])folders.toArray(new Folder[folders.size()]);
    }
    
    /*
     * Returns a set of all logical folder in this folder
     */
    public Vector getFolders() {
        Vector folders = new Vector();
        Iterator iter = new ArrayList(getElements()).iterator();
        while (iter.hasNext()) {
            Object item = iter.next();
            if (item instanceof Folder) {
                folders.add(item);
            }
        }
        return folders;
    }
    
    /*
     * Returns a set of all logical folder and subfolders in this folder
     */
    public Vector getAllFolders(boolean projectFilesOnly) {
        Vector folders = new Vector();
        
        if (!projectFilesOnly || isProjectFiles()) {
            Iterator iter = new ArrayList(getElements()).iterator();
            while (iter.hasNext()) {
                Object item = iter.next();
                if (item instanceof Folder) {
                    if (!projectFilesOnly || ((Folder)item).isProjectFiles()) {
                        folders.add(item);
                        folders.addAll(((Folder)item).getAllFolders(projectFilesOnly));
                    }
                }
            }
        }
        
        return folders;
    }
    
    public String[] getFolderNamesAsArray() {
        Folder[] items = getFoldersAsArray();
        String[] names = new String[items.length];
        for (int i = 0; i < items.length; i++)
            names[i] = items[i].getName();
        return names;
    }
    
    
    public void addChangeListener(ChangeListener cl) {
        synchronized (changeListenerList) {
            changeListenerList.add(cl);
        }
    }
    
    public void removeChangeListener(ChangeListener cl) {
        synchronized (changeListenerList) {
            changeListenerList.remove(cl);
        }
    }
    
    public void fireChangeEvent() {
        Iterator it;
        
        synchronized (changeListenerList) {
            it = new HashSet(changeListenerList).iterator();
        }
        ChangeEvent ev = new ChangeEvent(this);
        while (it.hasNext()) {
            ((ChangeListener)it.next()).stateChanged(ev);
        }
        configurationDescriptor.setModified();
    }
    
    class MyNativeFileItemSet extends HashSet implements NativeFileItemSet {
    }
    
    /** Look up i18n strings here */
    private static String getString(String s) {
        return NbBundle.getMessage(Folder.class, s);
    }
}
