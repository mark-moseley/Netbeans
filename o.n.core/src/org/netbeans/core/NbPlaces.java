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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.core;

import java.io.IOException;
import javax.swing.event.ChangeListener;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.nodes.Node;
import org.openide.util.ChangeSupport;

/** Important places in the system.
*
* @author Jaroslav Tulach
*/
public final class NbPlaces extends Object {
    private final ChangeSupport cs = new ChangeSupport(this);
    
    /** No instance outside this class.
    */
    private NbPlaces() {
    }
    
    private static NbPlaces DEFAULT;
    
    /** Getter for default instance.
     */
    public static synchronized NbPlaces getDefault() {
        if (DEFAULT == null) {
            DEFAULT = new NbPlaces();
        }
        return DEFAULT;
    }
    
    public void addChangeListener(ChangeListener l) {
        cs.addChangeListener(l);
    }
    
    public void removeChangeListener(ChangeListener l) {
        cs.removeChangeListener(l);
    }
    
    void fireChange() {
        cs.fireChange();
    }

    /** Environment node. Place for all transient information about
    * the IDE.
    */
    public Node environment () {
        return EnvironmentNode.find(EnvironmentNode.TYPE_ENVIRONMENT);
    }


    /** Session node */
    public Node session () {
        return EnvironmentNode.find(EnvironmentNode.TYPE_SESSION); 
    }

    /** Root nodes.
    */
    public Node[] roots () {
        return EnvironmentNode.find(EnvironmentNode.TYPE_ROOTS).getChildren ().getNodes (); 
    }

    /** Default folder for toolbars.
    */
    public DataFolder toolbars () {
        return findSessionFolder ("Toolbars"); // NOI18N
    }

    /** Default folder for menus.
    */
    public DataFolder menus () {
        return findSessionFolder ("Menu"); // NOI18N
    }

    /** Default folder for actions pool.
    */
    public DataFolder actions () {
        return findSessionFolder ("Actions"); // NOI18N
    }

     /**
     * Returns a DataFolder subfolder of the session folder.  In the DataFolder
     * folders go first (sorted by name) followed by the rest of objects sorted
     * by name.
     */
     public static DataFolder findSessionFolder (String name) {
        try {
            FileSystem fs = Repository.getDefault().getDefaultFileSystem ();
            FileObject fo = fs.findResource(name);
            if (fo == null) {
                // resource not found, try to create new folder
                fo = FileUtil.createFolder(fs.getRoot(), name);
            }
            return DataFolder.findFolder(fo);
        } catch (IOException ex) {
            throw (IllegalStateException) new IllegalStateException("Folder not found and cannot be created: " + name).initCause(ex); // NOI18N
        }
    }

}
