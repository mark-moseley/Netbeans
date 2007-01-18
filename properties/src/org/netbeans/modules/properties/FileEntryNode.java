/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.modules.properties;


import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import javax.swing.Action;

import org.openide.loaders.DataObject;
import org.openide.nodes.*;
import org.openide.util.actions.SystemAction;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;


/**
 * Standard node representing a <code>PresentableFileEntry</code>.
 *
 * @see  PresentableFileEntry
 * @author Petr Jiricka
 */
public class FileEntryNode extends AbstractNode {

    /** generated Serialized Version UID */
    static final long serialVersionUID = -7882925922830244768L;

    /** FileEntry of this node. */
    private PresentableFileEntry entry;


    /**
     * Creates a data node for a given file entry.
     * The provided children object will be used to hold all child nodes.
     *
     * @param entry entry to work with
     * @param ch children container for the node
     */
    public FileEntryNode (PresentableFileEntry entry, Children ch) {
        super (ch);
        this.entry = entry;
        
        PropL propListener = new PropL ();
        entry.addPropertyChangeListener(
                WeakListeners.propertyChange(propListener, entry));
        entry.getDataObject().addPropertyChangeListener (propListener);
        
        super.setName (entry.getName ());
    }
    
    private String getBundleString(String s){
        return NbBundle.getMessage(FileEntryNode.class, s);
    }


    /** Gets the represented entry.
     * @return the entry
     */
    public PresentableFileEntry getFileEntry() {
        return entry;
    }

    /** Indicate whether the node may be destroyed.
     * @return tests {@link DataObject#isDeleteAllowed}
     */
    public boolean canDestroy () {
        return entry.isDeleteAllowed ();
    }

    /** Destroyes the node. */
    public void destroy () throws IOException {
        entry.delete ();
        super.destroy ();
    }

    /** 
     * @returns true if this node allows copying.
     */
    public final boolean canCopy () {
        return entry.isCopyAllowed ();
    }

    /**
     * @returns true if this node allows cutting.
     */
    public final boolean canCut () {
        return entry.isMoveAllowed ();
    }

    /** Rename the data object.
     * @param name new name for the object
     * @exception IllegalArgumentException if the rename failed
     */
    public void setName (String name) {
        try {
            entry.renameEntry (name);
            super.setName (name);
        } catch (IOException ex) {
            throw new IllegalArgumentException (ex.getMessage ());
        }
    }

    /** Gets default action.
     * @deprecated
     * @return no action if the underlying entry is a template. Otherwise the abstract node's default action is returned, possibly <code>null</code>.
     */
    public SystemAction getDefaultAction () {
        if (entry.isTemplate ()) {
            return null;
        } else {
            Action a = getPreferredAction();
            if(a instanceof SystemAction){
                return (SystemAction) a;
            } else {
                return null;
            }            
        }
    }
 
    /** Gets default action.
     * @return no action if the underlying entry is a template. Otherwise the abstract node's default action is returned, possibly <code>null</code>.
     */ 
    public Action getPreferredAction() {
        if (entry.isTemplate ()) {
            return null;
        } else {
            return super.getPreferredAction();
        }
    }
    
    /** Get a cookie.
     * First of all {@link PresentableFileEntry#getCookie} is
     * called. If it produces non-<code>null</code> result, that is returned.
     * Otherwise the superclass is tried.
     * @return the cookie or <code>null</code>
     */
    public <T extends Node.Cookie> T getCookie(Class<T> cl) {
        T c = entry.getCookie(cl);
        if (c != null) {
            return c;
        } else {
            return super.getCookie (cl);
        }
    }

    /** Initializes sheet of properties. Allows subclasses to
     * overwrite it.
     * @return the default sheet to use
     */
    protected Sheet createSheet () {
        Sheet s = Sheet.createDefault ();
        Sheet.Set ss = s.get (Sheet.PROPERTIES);

        Node.Property p;

        p = new PropertySupport.ReadWrite<String>(
                PROP_NAME,
                String.class,
                getBundleString("PROP_name"),
                getBundleString("HINT_name")
            ) {
                public String getValue() {
                    return entry.getName();
                }

                public void setValue(String val) throws IllegalAccessException,
                    IllegalArgumentException, InvocationTargetException {
                    if (!canWrite()) {
                        throw new IllegalAccessException();
                    }
                    FileEntryNode.this.setName(val);
                }

                public boolean canWrite () {
                    return entry.isRenameAllowed();
                }
            };
        p.setName (DataObject.PROP_NAME);
        ss.put (p);

        try {
            p = new PropertySupport.Reflection<Boolean>(
                    entry, Boolean.TYPE, "isTemplate", "setTemplate" // NOI18N
                );
            p.setName (DataObject.PROP_TEMPLATE);
            p.setDisplayName (getBundleString("PROP_template"));
            p.setShortDescription (getBundleString("HINT_template"));
            ss.put (p);
        } catch(Exception ex) {
            throw new IllegalStateException();
        }
        return s;
    }


    /**
     * Support for firing property change.
     *
     * @param ev event describing the change
     */
    void fireChange (PropertyChangeEvent ev) {
        String propertyName = ev.getPropertyName();
        if (propertyName.equals(Node.PROP_COOKIE)) {
            fireCookieChange();
            return;
        }
        firePropertyChange(propertyName, ev.getOldValue(), ev.getNewValue());
        if (propertyName.equals(DataObject.PROP_NAME)) {
            super.setName (entry.getName ());
        }
    }

    /** Property listener on data object that delegates all changes of
     * properties to this node.
     */
    private class PropL extends Object implements PropertyChangeListener {
        public void propertyChange (PropertyChangeEvent ev) {
            fireChange (ev);
        }
    }
    
}
