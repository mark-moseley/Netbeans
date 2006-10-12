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

package org.netbeans.modules.db.sql.loader;

import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.UniFileLoader;
import org.openide.nodes.CookieSet;
import org.openide.nodes.Node;


/**
 *
 * @author Andrei Badea
 */
public class SQLDataObject extends MultiDataObject {

    public SQLDataObject(FileObject primaryFile, UniFileLoader loader) throws DataObjectExistsException {
        super(primaryFile, loader);
        CookieSet cookies = getCookieSet();
        cookies.add(new SQLEditorSupport(this));
    }

    protected Node createNodeDelegate() {
        return new SQLNode(this);
    }
    
    void addCookie(Node.Cookie cookie) {
        getCookieSet().add(cookie);
    }
    
    void removeCookie(Node.Cookie cookie) {
        getCookieSet().remove(cookie);
    }
}
