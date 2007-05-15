/*
 * The contents of this file are subject to the terms of the Common Development and
 * Distribution License (the License). You may not use this file except in compliance
 * with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html or
 * http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file and
 * include the License file at http://www.netbeans.org/cddl.txt. If applicable, add
 * the following below the CDDL Header, with the fields enclosed by brackets []
 * replaced by your own identifying information:
 * 
 *     "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original Software
 * is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun Microsystems, Inc. All
 * Rights Reserved.
 */

package org.netbeans.installer.utils.helper;

/**
 *
 * @author Kirill Sorokin
 */
public final class EngineResources {
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    private EngineResources() {
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    public static final String LOCAL_ENGINE_PATH_PROPERTY = 
            "nbi.product.local.engine.path";
    
    /*public static final String LOCAL_ENGINE_UNINSTALL_COMMAND_PROPERTY =
            "nbi.product.local.engine.uninstall.command";
    
    public static final String LOCAL_ENGINE_MODIFY_COMMAND_PROPERTY =
            "nbi.product.local.engine.modify.command";
    */
    public static final String DATA_DIRECTORY = 
            "data";
    
    public static final String ENGINE_CONTENTS_LIST = 
            DATA_DIRECTORY + "/engine.list";
    
    public static final String ENGINE_PROPERTIES = 
            DATA_DIRECTORY + "/engine.properties";
}
