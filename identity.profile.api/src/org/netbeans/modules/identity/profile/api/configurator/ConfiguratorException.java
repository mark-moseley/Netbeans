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

package org.netbeans.modules.identity.profile.api.configurator;

/**
 * Exception used to wrap all low-level exceptions.
 *
 * Created on July 28, 2006, 10:55 PM
 *
 * @author ptliu
 */
public class ConfiguratorException extends RuntimeException {
    
    /** Creates a new instance of ConfiguratorException */
    public ConfiguratorException(Throwable cause) {
        super(cause);
    }
    
    public ConfiguratorException(String message) {
        super(message);
    }
    
    public static ConfiguratorException create(Exception ex) {
        Throwable cause = ex.getCause();
        
        return new ConfiguratorException((cause != null) ? cause : ex);
    }
    
    public String getMessage() {
        Throwable cause = getCause();
        
        return ((cause != null) ? cause.getMessage() : super.getMessage());
    }
}
