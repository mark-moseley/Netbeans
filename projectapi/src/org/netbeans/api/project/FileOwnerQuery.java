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

package org.netbeans.api.project;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Iterator;
import org.netbeans.spi.project.FileOwnerQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.util.Lookup;

/**
 * Find the project which owns a file.
 * @see org.netbeans.spi.project.FileOwnerQueryImplementation
 * @author Jesse Glick
 */
public class FileOwnerQuery {

    private static final Lookup.Result/*<FileOwnerQueryImplementation>*/ implementations =
        Lookup.getDefault().lookup(new Lookup.Template(FileOwnerQueryImplementation.class));

    private FileOwnerQuery() {}

    /**
     * Find the project, if any, which "owns" the given file.
     * @param file the file (generally on disk)
     * @return a project which contains it, or null if there is no known project containing it
     */
    public static Project getOwner(FileObject file) {
        URL url = URLMapper.findURL(file, URLMapper.EXTERNAL);
        if (url == null) {
            return null;
        }
        URI u = URI.create(url.toString());
        return getOwner(u);
    }

    /**
     * Find the project, if any, which "owns" the given URI.
     * @param uri the uri to the file (generally on disk); must be absolute and not opaque
     * @return a project which contains it, or null if there is no known project containing it
     * @throws IllegalArgumentException if the URI is relative or opaque
     */
    public static Project getOwner(URI uri) {
        if (uri.isOpaque() && "jar".equalsIgnoreCase(uri.getScheme())) {    //NOI18N
            String schemaPart = uri.getSchemeSpecificPart();
            int index = schemaPart.lastIndexOf ('!');                       //NOI18N
            if (index>0) {
                schemaPart = schemaPart.substring(0,index);
            }
            // XXX: schemaPart can contains spaces. create File first and 
            // then convert it to URI.
            try {
                uri = URI.create(schemaPart);
            } catch (IllegalArgumentException ex) {
                try {
                    URL u = new URL(schemaPart);
                    uri = new File(u.getPath()).toURI();
                } catch (MalformedURLException ex2) {
                    ex2.printStackTrace();
                    assert false : schemaPart;
                    return null;
                }
            }
        }
        else if (!uri.isAbsolute() || uri.isOpaque()) {
            throw new IllegalArgumentException("Bad URI: " + uri); // NOI18N
        }
        Iterator it = implementations.allInstances().iterator();
        while (it.hasNext()) {
            FileOwnerQueryImplementation q = (FileOwnerQueryImplementation)it.next();
            Project p = q.getOwner(uri);
            if (p != null) {
                return p;
            }
        }
        return null;
    }

}
