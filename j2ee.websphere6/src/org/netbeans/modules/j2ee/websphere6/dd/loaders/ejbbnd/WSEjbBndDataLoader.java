/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.j2ee.websphere6.dd.loaders.ejbbnd;

import java.io.IOException;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.UniFileLoader;
import org.openide.util.NbBundle;

public class WSEjbBndDataLoader extends UniFileLoader {
    
    public static final String REQUIRED_MIME = "text/x-ejbbnd+xml";
    
    private static final long serialVersionUID = 1L;
    
    public WSEjbBndDataLoader() {
        super("org.netbeans.modules.j2ee.websphere6.ddloaders.ejbbnd.WSEjbBndDataObject");
    }
    
    protected String defaultDisplayName() {
        return NbBundle.getMessage(WSEjbBndDataLoader.class, "LBL_WSEjbBnd_loader_name");
    }
    
    protected void initialize() {
        super.initialize();
        getExtensions().addMimeType(REQUIRED_MIME);
    }
    
    protected MultiDataObject createMultiObject(FileObject primaryFile) throws DataObjectExistsException, IOException {
        return new WSEjbBndDataObject(primaryFile, this);
    }
    
    protected String actionsContext() {
        return "Loaders/" + REQUIRED_MIME + "/Actions";
    }
    
}
