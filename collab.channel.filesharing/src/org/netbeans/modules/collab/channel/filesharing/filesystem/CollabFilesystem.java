/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.collab.channel.filesharing.filesystem;

import com.sun.collablet.CollabSession;

import org.openide.*;
import org.openide.filesystems.*;
import org.openide.loaders.*;
import org.openide.util.*;

import java.awt.Image;

import java.io.*;

import java.util.*;

import org.netbeans.modules.collab.channel.filesharing.FilesharingConstants;
import org.netbeans.modules.collab.channel.filesharing.FilesharingContext;
import org.netbeans.modules.collab.channel.filesharing.filehandler.CollabFileHandler;
import org.netbeans.modules.collab.channel.filesharing.ui.FilesharingCollabletFactorySettings;
import org.netbeans.modules.collab.channel.filesharing.ui.ProjectsRootNode;
import org.netbeans.modules.collab.channel.filesharing.util.FileshareUtil;
import org.netbeans.modules.collab.core.Debug;


/**
 *
 * @author  Todd Fast <todd.fast@sun.com>
 */
public class CollabFilesystem extends LocalFileSystem implements FileSystem.Status, FilesharingConstants {
    ////////////////////////////////////////////////////////////////////////////
    // Class variables
    ////////////////////////////////////////////////////////////////////////////
    private static final long serialVersionUID = 1L; // DO NOT CHANGE!
    public static final String SYSTEM_NAME = "Collaboration"; // NOI18N
    public static final String EDITOR_ANNOTATION_SOURCE = "editor.name.annotation.source";
    public static final String EDITOR_ANNOTATION = "editor.name.annotation";
    public static final String EDITOR_ANNOTATION_TEMPLATE = "editor.name.annotation.html.template";
    public static final String ANNOTATION_SOURCE_FILESHARING = "com.sun.tools.ide.collab.channel.filesharing"; // NOI18N

    ////////////////////////////////////////////////////////////////////////////
    // Instance variables
    ////////////////////////////////////////////////////////////////////////////
    private String filesystemID = null;
    private FilesharingContext context = null;
    private File collabFileRoot;
    private String rPath = null;

    /**
     *
     * @param context
     * @param filesystemID
     */
    public CollabFilesystem(FilesharingContext context, String filesystemID) {
        super();
        this.filesystemID = filesystemID;
        this.context = context;

        String collabFileRootDir = getCollabRoot(this.filesystemID);
        collabFileRoot = new File(collabFileRootDir);

        if (collabFileRoot.exists()) {
            collabFileRootDir += ("_" + System.currentTimeMillis());
            collabFileRoot = new File(collabFileRootDir);
        }

        Debug.log("CollabFilesystem", "collabFileRootDir is:" + collabFileRootDir);
        createCollabFileRoot(collabFileRoot);
        collabFileRoot.mkdirs();

        try {
            super.setRootDirectory(collabFileRoot);
        } catch (Exception e) {
            ErrorManager.getDefault().notify(e);
        }
    }

    /**
     *
     *
     */
    public FilesharingContext getContext() {
        return context;
    }

    /**
     *
     * @return filesystemID
     */
    public String getID() {
        return this.filesystemID; // NOI18N
    }

    /**
     *
     * @return filesystem root
     */
    public String getCollabRoot(String filesystemID) {
        return getSharedSystemFolder() + File.separator + filesystemID;
    }

    /**
     *
     * @return filesystem root
     */
    public File getCollabRoot() {
        return collabFileRoot;
    }

    /**
     *
     * @return display name
     */
    public String getDisplayName() {
        return getDisplayName(false);
    }

    /**
     *
     * @param isChannelView
     * @return displayName
     */
    public String getDisplayName(boolean isChannelView) {
        String rootDisplayName = NbBundle.getMessage(ProjectsRootNode.class, "LBL_ProjectsRootNode_DisplayName"); //NoI18n

        if (isChannelView) {
            return rootDisplayName;
        } else {
            return NbBundle.getMessage(
                CollabFilesystem.class, "LBL_CollabFilesystem_FilesystemDisplayName", //NoI18n
                rootDisplayName, this.filesystemID
            );
        }
    }

    /**
     *
     * @param name
     * @param files
     * @return file/folder annotation
     */
    public String annotateName(String name, Set files) {
        String annotation = NbBundle.getMessage(
                CollabFilesystem.class, "LBL_CollabFilesystem_SharedFileAnnotation", name
            );

        return annotation;
    }

    /**
     *
     * @param image
     * @param iconType
     * @param files
     * @return icon image
     */
    public Image annotateIcon(Image image, int iconType, Set files) {
        return image;
    }

    ////////////////////////////////////////////////////////////////////////////
    // File management methods
    ////////////////////////////////////////////////////////////////////////////

    /**
     *
     * @param name
     * @throws IOException
     */
    public void createData(String name) throws IOException {
        super.createData(name);

        if (name.startsWith(FILE_SEPERATOR)) {
            name = name.substring(1);
        }
    }

    /**
     *
     * @param name
     * @throws IOException
     */
    public void delete(String name) throws IOException {
        super.delete(name);

        if (name.startsWith(FILE_SEPERATOR)) {
            name = name.substring(1);
        }
    }

    /**
     *
     *
     */
    private String getSharedSystemFolder() {
        String userHome = System.getProperty("netbeans.user"); // NOI18N
        String sharedFiles = userHome + File.separator + "collab" + // NOI18N
            File.separator + "shared_files"; // NOI18N

        return sharedFiles;
    }

    /**
     *
     *
     */
    private void createCollabFileRoot(File collabFileRoot) {
        int max = FilesharingCollabletFactorySettings.getDefault().getMaxSharedFileFolders().intValue();

        if (max < 1) {
            max = Integer.MAX_VALUE;
        }

        CollabSession[] sessions = getContext().getConversation().getCollabSession().getManager().getSessions();
        int numOfSessions = sessions.length;
        int convs = 0;

        for (int i = 0; i < numOfSessions; i++) {
            convs += sessions[i].getConversations().length;
        }

        File shared = new File(getSharedSystemFolder());

        if (shared.exists()) {
            File[] dirs = shared.listFiles();

            while ((dirs != null) && (dirs.length >= max) && (max > convs)) {
                deleteOldestDirectory(dirs);
                dirs = shared.listFiles();
            }
        }

        collabFileRoot.mkdirs();
    }

    /**
     *
     *
     */
    private void deleteOldestDirectory(File[] dirs) {
        long lastModified = 0;
        long oldest = 0;
        File oldestFolder = new File("temp");

        for (int i = 0; i < dirs.length; i++) {
            lastModified = dirs[i].lastModified();

            if (lastModified == 0) {
                continue;
            }

            if (i == 0) {
                oldest = lastModified;
                oldestFolder = dirs[i];
            } else {
                if (lastModified < oldest) {
                    oldest = lastModified;
                    oldestFolder = dirs[i];
                }
            }
        }

        try {
            deleteFolder(oldestFolder);
        } catch (Exception e) {
            Debug.debugNotify(e);
            e.printStackTrace();
        }
    }

    /**
     * delete file/folder
     *
     * @param file/folder
     */
    public void deleteFolder(File file) {
        if (file == null) {
            return;
        }

        if (file.isFile()) {
            file.delete();
        } else {
            File[] files = file.listFiles();

            if (files != null) {
                for (int i = 0; i < files.length; i++) {
                    deleteFolder(files[i]);
                }
            }

            //Delete self
            file.delete();
        }
    }

    /**
     *
     *
     */
    public void cleanup() {
        if ((collabFileRoot.list() == null) || (collabFileRoot.list().length == 0)) {
            collabFileRoot.delete();
        }
    }

    public String getPath(DataFolder folder) {
        String path = folder.getPrimaryFile().getPath();

        if (path.startsWith("/") || path.startsWith("\\")) {
            path = path.substring(1);
        }

        String rootPath = getCollabRoot().getAbsolutePath();

        if (rootPath.startsWith("/") || rootPath.startsWith("\\")) {
            rootPath = rootPath.substring(1);
        }

        return path.substring(rootPath.length() + 1);
    }

    public String getPath() {
        if (rPath == null) {
            rPath = FileshareUtil.getNormalizedPath(getCollabRoot().getPath());
        }

        return rPath;
    }

    public String getPath(FileObject fObj) {
        if (fObj == null) {
            return null;
        }

        if (fObj.getPath().contains(getPath())) {
            DataFolder df = null;

            try {
                df = (DataFolder) DataFolder.find(fObj.getParent());
            } catch (DataObjectNotFoundException ddnf) {
                ddnf.printStackTrace(Debug.out);
            }

            if (df != null) {
                return FileshareUtil.getNormalizedPath(getPath(df) + FILE_SEPERATOR + fObj.getNameExt()); //NoI18n
            } else {
                return fObj.getPath(); //NoI18n
            }
        } else {
            try {
                CollabFileHandler fh = getContext().getSharedFileGroupManager().getFileHandler(fObj);

                if (fh != null) {
                    Debug.out.println("getPath: for file: " + fObj.getPath() + " is: " + fh.getName());

                    return fh.getName();
                }
            } catch (Throwable th) {
                th.printStackTrace(Debug.out);
            }
        }

        return null;
    }

    public String getPath(DataFolder folder, FileObject fObj) {
        return getPath(folder) + FILE_SEPERATOR + fObj.getNameExt(); //NoI18n
    }

    public String getPath(DataFolder folder, DataObject d) {
        return getPath(folder) + FILE_SEPERATOR + d.getName(); //NoI18n
    }

    public File getAbsoluteFile(String path) {
        return new File(getCollabRoot(), path); //NoI18n
    }
}
