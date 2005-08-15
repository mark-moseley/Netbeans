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

package org.netbeans.spi.palette;

import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.MultiFileSystem;
import org.openide.filesystems.Repository;
import org.openide.filesystems.XMLFileSystem;
import org.xml.sax.SAXException;

/**
 * Repository whose getDefaultFileSystem() returns a writeable FS containing
 * the layer of the Core - Component Palette module. It is put in the default lookup,
 * thus it is returned by Repository.getDefault().
 *
 * @author Libor Kotouc
 */
public class RepositoryImpl extends Repository {
    
    private XMLFileSystem system;
    
    public RepositoryImpl() {
        super(createDefFs());
    }
    
    private static FileSystem createDefFs() {
        try
        {
            FileSystem writeFs = FileUtil.createMemoryFileSystem();
            FileSystem layerFs = new XMLFileSystem(RepositoryImpl.class.getClassLoader().getResource("org/netbeans/modules/palette/resources/layer.xml"));
            return new MultiFileSystem(new FileSystem[] { writeFs, layerFs });
        } catch (SAXException e) {
            return null;
        }
    }
}
