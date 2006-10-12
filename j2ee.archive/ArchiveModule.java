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


package org.netbeans.modules.j2ee.archive.project;

import java.io.IOException;
import java.util.Iterator;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.openide.filesystems.FileObject;

public class ArchiveModule implements J2eeModule {
    public String getModuleVersion() {
        return null;
    }

    public Object getModuleType() {
        return null;
    }

    public String getUrl() {
        return null;
    }

    public void setUrl(String url) {
        
    }

    public FileObject getArchive() throws IOException {
        return null;
    }

    public Iterator getArchiveContents() throws IOException {
        return null;
    }

    public FileObject getContentDirectory() throws IOException {
        return null;
    }

    public org.netbeans.modules.schema2beans.BaseBean getDeploymentDescriptor(String location) {
        return null;
    }

    public void addVersionListener(J2eeModule.VersionListener listener) {
    }

    public void removeVersionListener(J2eeModule.VersionListener listener) {
    }
    
    
}
