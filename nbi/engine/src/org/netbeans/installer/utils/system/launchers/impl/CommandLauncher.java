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


package org.netbeans.installer.utils.system.launchers.impl;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.installer.utils.helper.JavaCompatibleProperties;
import org.netbeans.installer.utils.system.launchers.LauncherProperties;

/**
 *
 * @author Dmitry Lipin
 */
public class CommandLauncher extends ShLauncher {
    private static final String COMMAND_EXT = ".command"; //NOI18N
    
    private static final String [] JAVA_MACOSX_LOCATION = {
        "/Library/Java", // NOI18N
        "/System/Library/Frameworks/JavaVM.framework/Versions/1.5", // NOI18N
        "/System/Library/Frameworks/JavaVM.framework/Versions/1.5.0", // NOI18N
        "/System/Library/Frameworks/JavaVM.framework/Versions/1.6", // NOI18N
        "/System/Library/Frameworks/JavaVM.framework/Versions/1.6.0", // NOI18N
        "/System/Library/Frameworks/JavaVM.framework/Versions/" // NOI18N
    };
    // the 1.5.0_02 is the first fcs release of J2SE5 for MacOSX according to
    // http://developer.apple.com/releasenotes/Java/Java50RN/index.html
    public static final String MIN_JAVA_VERSION_MACOSX = "1.5.0_02";
    
    public CommandLauncher(LauncherProperties props) {
        super(props);
    }
    @Override
    protected String [] getCommonSystemJavaLocations() {
        return JAVA_MACOSX_LOCATION;
    }
    @Override
    public String getExtension() {
        return COMMAND_EXT;
    }
    @Override
    public List <JavaCompatibleProperties> getDefaultCompatibleJava() {
        List <JavaCompatibleProperties> list = new ArrayList <JavaCompatibleProperties>();
        list.add(new JavaCompatibleProperties(
                MIN_JAVA_VERSION_MACOSX, null, null, null, null));
        return list;
    }
}
