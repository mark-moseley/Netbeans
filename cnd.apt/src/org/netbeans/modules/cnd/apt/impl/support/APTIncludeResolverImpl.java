/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 
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

package org.netbeans.modules.cnd.apt.impl.support;

import java.util.List;
import org.netbeans.modules.cnd.apt.debug.APTTraceFlags;
import org.netbeans.modules.cnd.apt.structure.APTInclude;
import org.netbeans.modules.cnd.apt.structure.APTIncludeNext;
import org.netbeans.modules.cnd.apt.support.APTIncludeResolver;
import org.netbeans.modules.cnd.apt.support.APTMacroCallback;
import org.netbeans.modules.cnd.apt.utils.APTIncludeUtils;
import org.netbeans.modules.cnd.apt.support.ResolvedPath;

/**
 * implementation of include resolver
 * @author Vladimir Voskresensky
 */
public class APTIncludeResolverImpl implements APTIncludeResolver {
    private final int baseFileIncludeDirIndex;
    private final String baseFile;
    private final List<String> systemIncludePaths;
    private final List<String> userIncludePaths;  
    
    public APTIncludeResolverImpl(String path, int baseFileIncludeDirIndex, 
                                    List<String> systemIncludePaths,
                                    List<String> userIncludePaths) {
        this.baseFile = path;
        this.systemIncludePaths = systemIncludePaths;
        this.userIncludePaths = userIncludePaths;
        this.baseFileIncludeDirIndex = baseFileIncludeDirIndex;
    }       

    public ResolvedPath resolveInclude(APTInclude apt, APTMacroCallback callback) {
        return resolveFilePath(apt.getFileName(callback), apt.isSystem(callback), false);
    }

    public ResolvedPath resolveIncludeNext(APTIncludeNext apt, APTMacroCallback callback) {
        return resolveFilePath(apt.getFileName(callback), apt.isSystem(callback), true);
    }

    public String getBasePath() {
        return baseFile;
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // implementation details    
        
    private ResolvedPath resolveFilePath(String file, boolean system, boolean includeNext) {
        ResolvedPath result = null;
        if (file != null && (file.length() > 0)) {  
            result = APTIncludeUtils.resolveAbsFilePath(file);
            if (result != null && !system && !includeNext) {
                // for <system> "current dir" has lowest priority
                // for #include_next should start from another dir
                result = APTIncludeUtils.resolveFilePath(file, baseFile);
            }
            if ( result == null) {
                int startOffset = includeNext ? baseFileIncludeDirIndex+1 : 0;
                PathsCollectionIterator paths = 
                        new PathsCollectionIterator(userIncludePaths, systemIncludePaths, startOffset);
                result = APTIncludeUtils.resolveFilePath(paths, file, startOffset);
            }
            if ( result == null && system && !includeNext) {
                // <system> was skipped above, check now, but not for #include_next
                result = APTIncludeUtils.resolveFilePath(file, baseFile);
            }
        }
        return result;
    }  

//    private String resolveNextFilePath(String file, boolean system) {
//        String result = null;
//        if (result == null) {
//            result = APTIncludeUtils.resolveFilePath(file, system ? systemIncludePaths : userIncludePaths, baseFile, true);
//        }
//        if (result == null) {
//            result = APTIncludeUtils.resolveFilePath(file, system ? userIncludePaths : systemIncludePaths, baseFile, true);
//        }
//        return result;
//    }      
}
