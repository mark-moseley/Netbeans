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

package org.netbeans.swing.popupswitcher;

import javax.swing.Icon;

/**
 * Represents one item in <code>SwitcherTable</class>.
 *
 * @see SwitcherTable
 *
 * @author mkrauskopf
 */
public class SwitcherTableItem implements Comparable {
    
    /** Item's name. Base name used by the <code>SwitcherTable</code> */
    private String name;
    
    /** Item's html-based name. */
    private String htmlName;
    
    /** Item's description. Text which can be used for arbitrary purpose. */
    private String description;
    
    /** Item's icon */
    private Icon icon;
    
    /** Indicates whether this item is active or not */
    private boolean active;
    
    /**
     * Object to be activated. This is up to concrete <code>PopupSwitcher</code>
     * implementation.
     */
    private Activable activable;
    
    /** Creates a new instance of SwitcherTableItem */
    public SwitcherTableItem(Activable activable, String name) {
        this(activable, name, null);
    }
    
    /** Creates a new instance of SwitcherTableItem */
    public SwitcherTableItem(Activable activable, String name, Icon icon) {
        this(activable, name, name, icon, false);
    }
    
    /** Creates a new instance of SwitcherTableItem */
    public SwitcherTableItem(Activable activable, String name, String htmlName,
            Icon icon, boolean active) {
        this(activable, name, htmlName, icon, active, null);
    }
    
    /** Creates a new instance of SwitcherTableItem */
    public SwitcherTableItem(Activable activable, String name, String htmlName,
            Icon icon, boolean active, String description) {
        this.activable = activable;
        this.name = name;
        this.htmlName = htmlName;
        this.icon = icon;
        this.active = active;
        this.description = description;
    }
    
    /**
     * Calls <code>activate()</code> method of <code>Activable</code> interface
     * which has to be passed in a constructor.
     *
     * @see SwitcherTableItem.Activable#activate
     */
    public void activate() {
        activable.activate();
    }
    
    /** Returns item's name */
    public String getName() {
        return name;
    }
    
    /** Returns item's html name. */
    public String getHtmlName() {
        return htmlName;
    }
    
    /**
     * Return item's description - the text which can be used for arbitrary
     * purpose. E.g. </code>KeyboardPopupSwitcher</code> uses it for statusbar
     * text.
     */
    public String getDescription() {
        return description;
    }
    
    /** Returns item's icon */
    public Icon getIcon() {
        return icon;
    }
    
    /** Returns item's activable object */
    public Activable getActivable() {
        return activable;
    }
    
    /** Returns whether this item is active or not. */
    public boolean isActive() {
        return active;
    }
    
    /** Returns human readable description of this item */
    public String toString() {
        return super.toString() + "[name=" + name + ", icon=" + icon + "]"; // NOI18N
    }
    
    /**
     * Returns true if the <code>name</code> and <code>activable</code> are the
     * same as passed one.
     */
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (o instanceof SwitcherTableItem) {
            SwitcherTableItem item = (SwitcherTableItem) o;
            boolean result = item.getName().equals(name) &&
                    item.getActivable().equals(activable);
            return result;
        } else {
            return false;
        }
    }
    
    /**
     * Returns a hash code value for the item.
     *
     * @return int hashcode
     */
    public int hashCode() {
        return (name == null ? 1 : name.hashCode()) * activable.hashCode();
    }
    
    /**
     * Compares items based on theirs <code>name</code>s. Items which has
     * null-name will be last.
     */
    public int compareTo(Object o) {
        String name1 = getName();
        String name2 = null;
        if (o instanceof SwitcherTableItem) {
            name2 = ((SwitcherTableItem) o).getName();
        }
        if (name2 == null) {
            return (name1 == null ? 0 : -1);
        } else {
            return (name1 == null ? 1 : name1.compareToIgnoreCase(name2));
        }
    }
    
    /**
     * This interface has to be implemented and passed to the
     * <code>SwitcherTableItem</code> constructor.
     */
    public static interface Activable {
        /**
         * Here should be code witch <em>activate</em> this item. The method
         * <code>SwitcherTableItem.activate()</code> conveniently call this
         * method. So you never need to call this method directly.
         *
         * @see SwitcherTableItem#activate
         */
        void activate();
    }
}
