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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.spi.autoupdate;

import java.security.KeyStore;

/** Registers <code>java.security.KeyStore</code> into Autoupdate infrastructure.
 * This KeyStore is used for verification of downloaded NBMs. The system finds out
 * the KeyStoreProvider in <code>Lookup.getDefault()<code>.
 * The <code>KeyStoreProvider</code> should be registred via META-INF/services.
 *
 * @author Jiri Rechtacek
 */
public interface KeyStoreProvider {
    
    /**
     * @return KeyStore
     */
    public KeyStore getKeyStore ();
}
