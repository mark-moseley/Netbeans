/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.beaninfo.editors;

import java.beans.*;
import java.awt.Component;
import java.util.ArrayList;
import java.util.Iterator;

import org.openide.ErrorManager;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
/**
 * Defines editor for choosing of Web browser.
 *
 * @author  Radim Kubacki
 */
public class HtmlBrowser extends Object {

    public static class FactoryEditor extends PropertyEditorSupport {
        
        /** extended attribute that signals that this object should not be visible to the user */
        private static final String EA_HIDDEN = "hidden"; // NOI18N

        private static final String BROWSER_FOLDER = "Services/Browsers"; // NOI18N
        
        /** Creates new FactoryEditor */
        public FactoryEditor () {
        }
        
        public String getAsText () {
            try {
                org.openide.awt.HtmlBrowser.Factory f = (org.openide.awt.HtmlBrowser.Factory)getValue ();
                
                Lookup.Item i = Lookup.getDefault().lookupItem(
                    new Lookup.Template (org.openide.awt.HtmlBrowser.Factory.class, null, f)
                );
                if (i != null)
                    return i.getDisplayName();
            }
            catch (Exception ex) {
                ErrorManager.getDefault ().notify (ex);
            }
            return NbBundle.getMessage (FactoryEditor.class, "CTL_UnspecifiedBrowser");
        }
        
        public boolean supportsCustomEditor () {
            return false;
        }
        
        public void setAsText (java.lang.String str) throws java.lang.IllegalArgumentException {
            try {
                if (NbBundle.getMessage (FactoryEditor.class, "CTL_UnspecifiedBrowser").equals (str)
                ||  str == null) {
                    setValue (null);
                    return;
                }
                Lookup.Result r = Lookup.getDefault().lookup(
                    new Lookup.Template (org.openide.awt.HtmlBrowser.Factory.class)
                );
                Iterator it = r.allItems().iterator();
                while (it.hasNext()) {
                    Lookup.Item i = (Lookup.Item)it.next();
                    if (str.equals(i.getDisplayName())) {
                        setValue (i.getInstance());
                        return;
                    }
                }
            }
            catch (Exception ex) {
                ErrorManager.getDefault ().notify (ex);
                return;
            }
        }
        
        public java.lang.String[] getTags () {
            ArrayList list = new ArrayList (4);
            Lookup.Result r = Lookup.getDefault().lookup(
                new Lookup.Template (org.openide.awt.HtmlBrowser.Factory.class)
            );
            Iterator it = r.allItems().iterator();
            while (it.hasNext()) {
                Lookup.Item i = (Lookup.Item)it.next();
                list.add(i.getDisplayName());
            }
            
            // PENDING need to get rid of this filtering
            FileObject fo = Repository.getDefault ()
            .getDefaultFileSystem ().findResource (BROWSER_FOLDER);
            DataFolder folder = DataFolder.findFolder (fo);
            DataObject [] dobjs = folder.getChildren ();
            for (int i = 0; i<dobjs.length; i++) {
                // Must not be hidden and have to provide instances (we assume instance is HtmlBrowser.Factory)
                if (Boolean.TRUE.equals (dobjs[i].getPrimaryFile ().getAttribute (EA_HIDDEN))
                ||  dobjs[i].getCookie (InstanceCookie.class) == null)
                    list.remove (dobjs[i].getNodeDelegate ().getDisplayName ());
            }
            String[] retValue = new String[list.size ()];
            
            list.toArray (retValue);
            return retValue;
        }
        
    }
                
}
