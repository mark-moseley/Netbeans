/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License(the License). You may not use this file except in
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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.webmodule;

import org.netbeans.modules.j2ee.dd.api.web.WebAppMetadata;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.web.spi.webmodule.WebModuleImplementation;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Andrei Badea
 */
public class SimpleWebModuleImpl implements WebModuleImplementation {

    public String getContextPath() {
        return null;
    }

    public FileObject getDocumentBase() {
        return null;
    }

    public String getJ2eePlatformVersion() {
        return null;
    }

    public FileObject getDeploymentDescriptor() {
        return null;
    }

    public FileObject getWebInf() {
        return null;
    }

    public FileObject[] getJavaSources() {
        return new FileObject[0];
    }

    public MetadataModel<WebAppMetadata> getMetadataModel() {
        return null;
    }
}
