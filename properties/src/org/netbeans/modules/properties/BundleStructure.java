/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.modules.properties;


import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import org.openide.util.WeakListener;


/** 
 * Structure of bundle of .properties files. Provides structure of entries (which each corresponds
 * to one .properties file) for one <code>PropertiesDataObject</code>.
 * <p>
 * This structure provides support for sorting <code>entries</code> and fast mapping of integers to <code>entries</code>.
 * <p>
 * The sorting support in this class is design flaw and consider it deprecated.
 *
 * @author Petr Jiricka
 */
public class BundleStructure extends Object {
    
    /** <code>PropertiesDataObject</code> which structure is provided. */
    PropertiesDataObject obj;

    /** Array of <code>PropertiesFileEntry</code> entries. The first entry is always the primary entry. 
     * @see PropertiesFileEntry */
    private PropertiesFileEntry[] entries;

    /** Sorted list of non-escaped keys. */
    private ArrayList keyList;
    
    /** Compartor which sorts keylist.
     * Default set is sort according keys in file order.
     * */
    private KeyComparator comparator = new KeyComparator();

    /** Support for firing events when changes made on this bundle. */
    private PropertyBundleSupport propBundleSupport = new PropertyBundleSupport(this);

    /** Listens to changes on the underlying dataobject. */
    private PropertyChangeListener propListener;

    
    /** Create a data node for a given data object.
     * The provided children object will be used to hold all child nodes.
     * @param obj object to work with
     * @param ch children container for the node
     */
    public BundleStructure (PropertiesDataObject obj) {
        this.obj = obj;
        updateEntries();

        // Listen on the PropertiesDataObject.
        propListener = new PropertyChangeListener () {
            public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getPropertyName().equals(PropertiesDataObject.PROP_FILES)) {
                    updateEntries();
                    
                    propBundleSupport.fireBundleStructureChanged();
                }
            }
        };
        obj.addPropertyChangeListener(WeakListener.propertyChange(propListener, obj));
    }

    
    /** Retrieves n-th entry from the list, indexed from 0.
     * The first entry is always the primary entry.
     * @return n-th ntry or null if index is out of bounds */
    public PropertiesFileEntry getNthEntry(int i) {
        if (entries == null)
            throw new IllegalStateException("Resource Bundles: Entries not initialized"); // NOI18N

        try {
            return entries[i];
        } catch(ArrayIndexOutOfBoundsException aibe) {
            return null;
        }
    }

    /** Retrieves the index of a file entry (primary or secondary) by the name of its file
     *  @return index for entry with the given filename or -1 if not found
     */
    public int getEntryIndexByFileName(String fileName) {
        if(entries == null)
            throw new IllegalStateException("Resource Bundles: Entries not initialized"); // NOI18N
            
        for (int i = 0; i < getEntryCount(); i++) {
            if (entries[i].getFile().getName().equals(fileName))
                return i;
        }
            
        return -1;
    }

    /** Retrieves a file entry (primary or secondary) by the name of its file
     *  @return entry with the given filename or null if not found
     */
    public PropertiesFileEntry getEntryByFileName(String fileName) {
        int index = getEntryIndexByFileName(fileName);
        return ((index == -1) ? null : entries[index]);
    }

    /** Retrieves number of all entries */
    public int getEntryCount() {
        if(entries == null)
            throw new IllegalStateException("Resource Bundles: Entries not initialized"); // NOI18N

        return entries.length;
    }

    // Sorted keys management ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    /** Retrieves all un-escaped keys in bundle. */
    public String[] getKeys() {
        if (keyList == null)
            throw new IllegalStateException("Resource Bundles: KeyList not initialized"); // NOI18N
        
        Object keyArray[] = keyList.toArray();
        String stringArray[] = new String[keyArray.length];
        System.arraycopy(keyArray, 0, stringArray, 0, keyArray.length);
        
        return stringArray;
    }

    /**
     * Retrieves n-th key from the list, indexed from 0.
     * @param keyIndex index accrding to current sort order
     * @return unescaped key
     * */
    public String keyAt(int keyIndex) {
        if (keyList == null)
            throw new IllegalStateException("Resource Bundles: KeyList not initialized"); // NOI18N
        
        if ((keyIndex >= keyList.size()) || (keyIndex < 0)) {
            return null;
        } else {
            return (String)keyList.get(keyIndex);
        }
    }

    /**
     * Retrieves index for a key from the list, by name.
     * @param keyName non-escaped key name
     * @return 0-based position of key in current sort order or -1 it it does not exist
     */
    public int getKeyIndexByName(String keyName) {
        return keyList.indexOf(keyName);
    }

    /** Retrieves keyIndex-th key in the entryIndex-th entry from the list, indexed from 0
     * @return item for keyIndex-th key in the entryIndex-th entry 
     *  or null if the entry does not contain the key or entry doesn't exist
     */
    public Element.ItemElem getItem(int entryIndex, int keyIndex) {
        String key = keyAt(keyIndex);
        return getItem(entryIndex, key);
    }

    public Element.ItemElem getItem(int entryIndex, String key) {
        if (key == null) return null;
        PropertiesFileEntry pfe = getNthEntry(entryIndex);
        if(pfe == null) return null;

        PropertiesStructure ps = pfe.getHandler().getStructure();
        if (ps != null)
            return ps.getItem(key);
        else
            return null;

    }

    /** Retrieves number of all keys. */
    public int getKeyCount() {
        if (keyList != null)
            return keyList.size();
        else
            throw new IllegalStateException("Resource Bundles: KeyList not initialized"); // NOI18N
    }
    
    /** Sorts the keylist according the values of entry which index is given to this method.
     * @param index sorts accordinng nth-1 entry values, 0 means sort by keys,
     * if less than 0 it re-compares keylist with the same un-changed comparator.
     */
    public void sort(int index) {
        if(index >= 0)
            comparator.setIndex(index);
        Collections.sort(keyList, comparator);
        propBundleSupport.fireBundleDataChanged();
    }

    /** Gets index accoring which is bundle key list sorted.
     * @return index, 0 means according keys, -1 means sorting as in default
     * properties file
     */
    public int getSortIndex() {
        return comparator.getIndex();
    }
    
    /**
     * Gets current order of sort.
     * @return true if ascending, alse descending order (until sort index is -1, then unsorted)
     */
    public boolean getSortOrder() {
        return comparator.isAscending();
    }

    /** Helper method. Updates internal entries from the underlying <code>PropertiesDataObject<code>. */
    private void updateEntries() {
        TreeMap tm = new TreeMap(PropertiesDataObject.getSecondaryFilesComparator());
        PropertiesFileEntry pfe;
        for (Iterator it = obj.secondaryEntries().iterator(); it.hasNext(); ) {
            pfe = (PropertiesFileEntry)it.next();
            tm.put(pfe.getFile().getName(), pfe);
        }

        synchronized(this) {
            // Move the entries.
            entries = new PropertiesFileEntry[tm.size() + 1];
            entries[0] = (PropertiesFileEntry)obj.getPrimaryEntry();
            int index = 0;
            for (Iterator it = tm.keySet().iterator(); it.hasNext(); )
                entries[++index] = (PropertiesFileEntry)tm.get(it.next());
        }

        buildKeySet();
    }

    /** Helper method. Constructs a set of keys from the entries (from scratch). */
    private void buildKeySet() {
        keyList = new ArrayList() {
            public boolean equals(Object obj) {
                if(!(obj instanceof ArrayList))
                    return false;
                ArrayList list2 = (ArrayList)obj;
                
                if(this.size() != list2.size())
                    return false;
                for(int i=0; i<this.size(); i++) {
                    if(!this.contains(list2.get(i)) || !list2.contains(this.get(i)))
                        return false;
                }
                return true;
            }
        };

        // for all entries add all keys
        for (int index = 0; index < getEntryCount(); index++) {
            PropertiesFileEntry entry = getNthEntry(index);
            PropertiesStructure ps = entry.getHandler().getStructure();
            if(ps != null) {
                for(Iterator it = ps.allItems(); it.hasNext(); ) {
                    Element.ItemElem item = (Element.ItemElem)it.next();
                    if(item == null)
                        continue;
                    
                    String key = item.getKey();
                    if(key != null && !(keyList.contains(key)) )
                        keyList.add(item.getKey());
                }
            }
        }
        
        Collections.sort(keyList, comparator);
    }

    /** Adds listener to the list that's notified each time a change
     * to the property bundle occurs.
     * @param l the <code>PropertyBundleListener</code>
     */
    public void addPropertyBundleListener(PropertyBundleListener l) {
        propBundleSupport.addPropertyBundleListener(l);
    }

    /**
     * Removes listener from the list.
     * @param l the <code>PropertyBundleListener</code>
     */
    public void removePropertyBundleListener(PropertyBundleListener l) {
        propBundleSupport.removePropertyBundleListener(l);
    }

    /** Notification method.
     * One item in a properties file has changed. Fires a change event for this item.
     */
    void notifyItemChanged(PropertiesStructure struct, Element.ItemElem item) {
        propBundleSupport.fireItemChanged(
            struct.getParent().getEntry().getFile().getName(),
            item.getKey()
        );
    }

    /** Notification method.
     * One file in the bundle has changed - no further information.
     * Fires changes for a bundle or a file according to the changes in the keys.
     */
    void notifyOneFileChanged(StructHandler handler) {
        // PENDING - events should be finer
        // find out whether global key table has changed and fire a change according to that
        ArrayList oldKeyList = keyList;         
        
        buildKeySet();
        if (!keyList.equals(oldKeyList)) {
            propBundleSupport.fireBundleDataChanged();
        } else {
            propBundleSupport.fireFileChanged(handler.getEntry().getFile().getName());
        }
    }

    /** One file in the bundle has changed, carries information about what particular items have changed.
     * Fires changes for a bundle or a file according to the changes in the keys.
     */
    void notifyOneFileChanged(StructHandler handler, Map itemsChanged, Map itemsAdded, Map itemsDeleted) {
        // PENDING - events should be finer
        // find out whether global key table has changed
        // should use a faster algorithm of building the keyset
        buildKeySet();
        propBundleSupport.fireBundleDataChanged();
    }

    
    /** Inner class. Comparator which compares keys according which locale (column in table was selected). */
    private final class KeyComparator implements Comparator {

        /** Index of column to compare with. */
        private int index;
        
        /** Flag if ascending order should be performed. */
        private boolean ascending;

        
        /** Constructor. */
        public KeyComparator() {
            this.index = -1;
            ascending = false;
        }
        
        
        /**
         * Setter for <code>index</code> property.
         * ascending -> descending -> primary file key order -> ....
         * @param index interval 0 .. entry count
         * */
        public void setIndex(int index) {
            if (index == -1) throw new IllegalArgumentException();
            // if same column toggle order
            if(this.index == index) {
                if (ascending) {
                    ascending = false;
                } else {
                    // sort as in properties file
                    index = -1;
                    ascending = true;
                }
            } else {
                ascending = true;
            }
            this.index = index;
        }

        /**
         * Getter for <code>index</code> property.
         * @return -1 .. entry count, -1 means unsorted
         * */
        public int getIndex() {
            return index;
        }
        
        /** Getter for <code>ascending</code> property. */
        public boolean isAscending() {
            return ascending;
        }

        /**
         * Impements <code>Comparator</code>.
         * It;s strange as it access just being compared list
         */
        public int compare(Object o1, Object o2) {
            String str1;
            String str2;
            
            // sort as in default properties file
            if (index < 0) {
                Element.ItemElem item1 = getItem(0, (String)o1);
                Element.ItemElem item2 = getItem(0, (String)o2);
                if (item1 != null && item2 != null) {
                    int i1 = item1.getBounds().getBegin().getOffset();
                    int i2 = item2.getBounds().getBegin().getOffset();
                    return i1 - i2;
                } else if (item1 != null) {
                    return -1;
                } else if (item2 != null) {
                    return 1;
                } else {
                    // both keys are not in the default properties file
                    // order keys by name
                    str1 = (String)o1;
                    str2 = (String)o2;
                }
            }
            // key column
            if (index==0) {
                str1 = (String)o1;
                str2 = (String)o2;
            } else {
                Element.ItemElem item1 = getItem(index-1, (String)o1);
                Element.ItemElem item2 = getItem(index-1, (String)o2);
                if(item1 == null) {
                    if(item2 == null)
                        return 0;
                    else
                        return ascending ? 1 : -1;
                } else
                    if(item2 == null)
                        return ascending ? -1 : 1;
                
                str1 = item1.getValue();
                str2 = item2.getValue();
            }

            if(str1 == null) {
                if(str2 == null)
                    return 0;
                else
                    return ascending ? 1 : -1;
            } else if(str2 == null)
                return ascending ? -1 : 1;
            
            int res = str1.compareToIgnoreCase(str2);

            return ascending ? res : -res;
        }
        
    } // End of inner class KeyComparator.
    
}
