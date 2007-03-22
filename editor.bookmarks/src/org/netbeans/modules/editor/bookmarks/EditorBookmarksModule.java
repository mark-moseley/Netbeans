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

package org.netbeans.modules.editor.bookmarks;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import javax.swing.Action;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.Document;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.editor.AnnotationType;
import org.netbeans.editor.AnnotationTypes;
import org.netbeans.editor.BaseKit;
import org.netbeans.editor.Registry;
import org.netbeans.editor.Settings;
import org.netbeans.editor.SettingsNames;
import org.netbeans.editor.SettingsUtil;
import org.netbeans.lib.editor.bookmarks.actions.BookmarksKitInstallAction;
import org.netbeans.lib.editor.bookmarks.api.BookmarkList;
import org.openide.modules.ModuleInstall;

/**
 * Module installation class for editor.
 *
 * @author Miloslav Metelka
 */
public class EditorBookmarksModule extends ModuleInstall {

    private PropertyChangeListener openProjectsListener;
    private static List listeners = new ArrayList(); // List<ChangeListener>
    private PropertyChangeListener annotationTypesListener;
    private List lastOpenProjects;

    public void restored () {
        Settings.addInitializer(new BookmarksSettingsInitializer());
        
        // Start listening on project closing
        openProjectsListener = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                List openProjects = Arrays.asList(OpenProjects.getDefault().getOpenProjects());
                // lastOpenProjects will contain the just closed projects
                lastOpenProjects.removeAll(openProjects);
                for (Iterator it = lastOpenProjects.iterator(); it.hasNext();) {
                    Project prj = (Project)it.next();
                    PersistentBookmarks.saveProjectBookmarks(prj);
                }
                lastOpenProjects = new ArrayList(openProjects);
            }
        };
        OpenProjects op = OpenProjects.getDefault();
        lastOpenProjects = new ArrayList(Arrays.asList(op.getOpenProjects()));
        op.addPropertyChangeListener(openProjectsListener);
        
        SwingUtilities.invokeLater(new Runnable(){
            public void run(){
                final Iterator it = Registry.getDocumentIterator();
                if (!it.hasNext()){
                    return ;
                }
                
                AnnotationType type = AnnotationTypes.getTypes().getType(NbBookmarkImplementation.BOOKMARK_ANNOTATION_TYPE);
                if (type == null){
                    // bookmark type was not added into AnnotationTypes yet, wait for event
                    AnnotationTypes.getTypes().addPropertyChangeListener(annotationTypesListener = new PropertyChangeListener(){
                        public void propertyChange(PropertyChangeEvent evt) {
                            AnnotationType type = AnnotationTypes.getTypes().getType(NbBookmarkImplementation.BOOKMARK_ANNOTATION_TYPE);
                            if (type != null){
                                AnnotationTypes.getTypes().removePropertyChangeListener(annotationTypesListener);
                                while(it.hasNext()){
                                    Document doc = (Document)it.next();
                                    BookmarkList.get(doc); // Initialize the bookmark list
                                }
                            }
                        }
                    });
                } else {
                    while(it.hasNext()){
                        Document doc = (Document)it.next();
                        BookmarkList.get(doc); // Initialize the bookmark list
                    }
                }
            }
        });
    }
    
    /**
     * Called when all modules agreed with closing and the IDE will be closed.
     */
    public void close() {
        finish();
    }
    
    /**
     * Called when module is uninstalled.
     */
    public void uninstalled() {
        finish();
    }
    
    private void finish() {
        // Stop listening on projects closing
        OpenProjects.getDefault().removePropertyChangeListener(openProjectsListener);
        
        Settings.removeInitializer(BookmarksSettingsInitializer.NAME);
        Settings.reset();
        
        // Save bookmarks for all opened projects with touched bookmarks
        PersistentBookmarks.saveAllProjectBookmarks();
        
        // notify NbBookmarkManager that the module is uninstalled and BookmarkList can be removed
        // from doc.clientProperty
        fireChange();
    }

    private static final class BookmarksSettingsInitializer extends Settings.AbstractInitializer {
        
        static final String NAME = "bookmarks-settings-initializer"; // NOI18N
        
        BookmarksSettingsInitializer() {
            super(NAME);
        }

        public void updateSettingsMap(Class kitClass, java.util.Map settingsMap) {
            if (kitClass == BaseKit.class) {
                SettingsUtil.updateListSetting(settingsMap,
                        SettingsNames.CUSTOM_ACTION_LIST,
                        new Object[] { BookmarksKitInstallAction.INSTANCE }
                );
                SettingsUtil.updateListSetting(settingsMap,
                        SettingsNames.KIT_INSTALL_ACTION_NAME_LIST,
                        new Object[] { BookmarksKitInstallAction.INSTANCE.getValue(Action.NAME) }
                );
            }
        }
        
    }
    
    public static synchronized void addChangeListener(ChangeListener l) {
        listeners.add(l);
    }
    
    public static synchronized void removeChangeListener(ChangeListener l) {
        listeners.remove(l);
    }
    
    private static void fireChange() {
        ChangeEvent ev = new ChangeEvent(EditorBookmarksModule.class);
        ChangeListener[] ls;
        synchronized (EditorBookmarksModule.class) {
            ls = (ChangeListener[]) listeners.toArray(new ChangeListener[listeners.size()]);
        }
        for (int i = 0; i < ls.length; i++) {
            ls[i].stateChanged(ev);
        }
    }
}
