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

import java.awt.event.ActionEvent;
import java.util.*;

import javax.swing.Action;
import javax.swing.SwingUtilities;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.text.CloneableEditorSupport;
import org.openide.util.ContextAwareAction;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;


import org.openide.util.Utilities;
import org.openide.windows.Mode;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;



/** The util methods for projectui module.
 *
 * @author  Jiri Rechtacek
 */
public class ProjectUtilities {
    
    /** Creates a new instance of CloseAllProjectDocuments */
    private ProjectUtilities () {
    }
    
    /** Closes all documents in editor area which are owned by one of given projects.
     * If some documents are modified then an user is notified by Save/Discard/Cancel dialog.
     * Dialog is showed only once for all project's documents together.
     *
     * @param p project to close
     * @return false if an user cancel the Save/Discard/Cancel dialog, true otherwise
     */    
    final public static boolean closeAllDocuments (Project[] projects) {
        if (projects == null) {
            throw new IllegalArgumentException ("No proects are specified."); // NOI18N
        }
        
        if (projects.length == 0) {
            // no projects to close, no documents will be closed
            return true;
        }
        List/*Project*/ listOfProjects = Arrays.asList (projects);
        Set/*DataObject*/ modifiedFiles = new HashSet ();
        Set/*TopComponent*/ tc2close = new HashSet ();
        Mode editorMode = WindowManager.getDefault ().findMode (CloneableEditorSupport.EDITOR_MODE);
        TopComponent[] openTCs = editorMode.getTopComponents ();
        for (int i = 0; i < openTCs.length; i++) {
            DataObject dobj = (DataObject)openTCs[i].getLookup ().lookup (DataObject.class);
            if (dobj != null) {
              FileObject fobj = dobj.getPrimaryFile ();
              Project owner = FileOwnerQuery.getOwner (fobj);
              if (listOfProjects.contains (owner)) {
                  modifiedFiles.add (dobj);
                  tc2close.add (openTCs[i]);
              }
            }
        }
        
        if (modifiedFiles.isEmpty ()) {
            return true;
        }
        
        boolean result = ExitDialog.showDialog (modifiedFiles);
        
        if (result) {
            // close documents
            Iterator it = tc2close.iterator ();
            while (it.hasNext ()) {
                ((TopComponent)it.next ()).close ();
            }
        }
        
        return result;
    }
    
    /** Closes all documents of the given project in editor area. If some documents
     * are modified then an user is notified by Save/Discard/Cancel dialog.
     *
     * @param p project to close
     * @return false if an user cancel the Save/Discard/Cancel dialog, true otherwise
     */    
    final public static boolean closeAllDocuments (Project p) {
        if (p == null) {
            throw new IllegalArgumentException ("No specified project."); // NOI18N
        }
        return closeAllDocuments (new Project[] { p });
    }
    
    /** Invokes the preferred action on given object and tries to select it in
     * corresponding view, e.g. in logical view if possible otherwise
     * in physical project's view.
     * Note: execution this methods can invokes new threads to assure the action
     * is called in EQ.
     *
     * @param newDo new data object
     */   
    final public static void openAndSelectNewObject (final DataObject newDo) {
        // call the preferred action on main class
        Mutex.EVENT.writeAccess (new Runnable () {
            public void run () {
                final Node node = newDo.getNodeDelegate ();
                Action a = node.getPreferredAction();
                if (a instanceof ContextAwareAction) {
                    a = ((ContextAwareAction)a).createContextAwareInstance(node.getLookup ());
                }
                if (a != null) {
                    a.actionPerformed(new ActionEvent(node, ActionEvent.ACTION_PERFORMED, "")); // NOI18N
                }

                // next action -> expand && select main class in package view
                final ProjectTab ptLogial  = ProjectTab.findDefault (ProjectTab.ID_LOGICAL);
                final ProjectTab ptPhysical  = ProjectTab.findDefault (ProjectTab.ID_PHYSICAL);
                // invoke later, Mutex.EVENT.writeAccess isn't suffice to 
                // select && expand if the focus is outside ProjectTab
                SwingUtilities.invokeLater (new Runnable () {
                    public void run () {
                        boolean success = ptLogial.selectNode (newDo.getPrimaryFile ());
                        if (!success) {
                            ptPhysical.selectNode (newDo.getPrimaryFile ());
                        }
                    }
                });
            }
        });
    }
    
    /** Checks if the given file name can be created in the target folder.
     *
     * @param folder target folder
     * @param newObjectName name of created file
     * @param extension extension of created file
     * @return localized error message or null if all right
     */    
    final public static String canUseFileName (FileObject folder, String newObjectName, String extension) {
        if (extension != null && extension.length () > 0) {
            StringBuffer sb = new StringBuffer ();
            sb.append (newObjectName);
            sb.append ('.'); // NOI18N
            sb.append (extension);
            newObjectName = sb.toString ();
        }
        // test whether the selected folder on selected filesystem already exists
        if (folder == null) {
            return NbBundle.getMessage (ProjectUtilities.class, "MSG_fs_or_folder_does_not_exist"); // NOI18N
        }
        
        // target filesystem should be writable
        if (!folder.canWrite ()) {
            return NbBundle.getMessage (ProjectUtilities.class, "MSG_fs_is_readonly"); // NOI18N
        }
        
        if (folder.getFileObject (newObjectName) != null) {
            return NbBundle.getMessage (ProjectUtilities.class, "MSG_file_already_exist", newObjectName); // NOI18N
        }
        
        if (Utilities.isWindows ()) {
            if (checkCaseInsensitiveName (folder, newObjectName)) {
                return NbBundle.getMessage (ProjectUtilities.class, "MSG_file_already_exist", newObjectName); // NOI18N
            }
        }

        // all ok
        return null;
    }
    
    // helper check for windows, its filesystem is case insensitive (workaround the bug #33612)
    /** Check existence of file on case insensitive filesystem.
     * Returns true if folder contains file with given name and extension.
     * @param folder folder for search
     * @param name name of file
     * @param extension extension of file
     * @return true if file with name and extension exists, false otherwise.
     */    
    private static boolean checkCaseInsensitiveName (FileObject folder, String name) {
        // bugfix #41277, check only direct children
        Enumeration children = folder.getChildren (false);
        FileObject fo;
        while (children.hasMoreElements ()) {
            fo = (FileObject) children.nextElement ();
            if (name.equalsIgnoreCase (fo.getName ())) {
                return true;
            }
        }
        return false;
    }
    
}
