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

package org.netbeans.modules.masterfs.filebasedfs.naming;


import java.io.File;
import java.io.IOException;
import org.netbeans.modules.masterfs.providers.ProvidedExtensions;
import org.openide.ErrorManager;

/**
 * @author Radek Matous
 */
public class FileName implements FileNaming {
    private String name;
    private final FileNaming parent;
    private Integer id;
    
    protected FileName(final FileNaming parent, final File file) {
        this.parent = parent;
        this.name = parseName(file);
        id = NamingFactory.createID(file);
    }

    private static String parseName(final File file) {
        return (file.getParentFile() == null) ? file.getPath() : file.getName();
    }

    public boolean rename(String name, ProvidedExtensions.IOHandler handler) {
        boolean retVal = false;
        final File f = getFile();

        if (f.exists()) {
            File newFile = new File(f.getParentFile(), name);
            if (handler != null) {
                try {
                    handler.handle();
                    retVal = true;
                } catch (IOException ex) {
                    ErrorManager.getDefault().notify(ex);
                }
            } else {
                retVal = f.renameTo(newFile);
            }
            if (retVal) {
                this.name = name;
                id = NamingFactory.createID(newFile);
            }
        }
        FolderName.freeCaches();
        return retVal;
    }

    public final boolean rename(final String name) {        
        return rename(name, null);
    }

    public final boolean isRoot() {
        return (getParent() == null);
    }


    public File getFile() {
        final FileNaming parent = this.getParent();
        return (parent != null) ? new File(parent.getFile(), getName()) : new File(getName());
    }


    public final String getName() {
        return name;
    }

    public FileNaming getParent() {
        return parent;
    }

    public final Integer getId() {
        return getId(false);
    }

    public Integer getId(boolean recompute) {
        if (recompute) {
            id = NamingFactory.createID(getFile());
        }
        return id;
    }

    public final boolean equals(final Object obj) {
        return (obj instanceof FileNaming && obj.hashCode() == hashCode());
    }


    public final String toString() {
        return getFile().getAbsolutePath();
    }

    public final int hashCode() {
        return id.intValue();
    }

    public boolean isFile() {
        return true;
    }

    public boolean isDirectory() {
        return !isFile();
    }
}
