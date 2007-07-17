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
package org.netbeans.modules.vmd.api.model;

/**
 * Register an implementation of this interface into global lookup (META-INF/services/org.netbeans.modules.vmd.api.model.AccessControllerFactory file).
 * Registered factories are called for creating AccessControllers for each DesignDocument.
 *
 * @author David Kaspar
 */
public interface AccessControllerFactory {

    /**
     * Factory method for creating AccessController.
     * NOTE: It is called when a DesignDocument is creating, do not perform any action which may call/use DesignDocument since the document is not initialized yet.
     * @param document the document
     * @return the access controller
     */
    public AccessController createAccessController (DesignDocument document);

}
