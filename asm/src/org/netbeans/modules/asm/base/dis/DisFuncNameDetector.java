/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.asm.base.dis;

import org.netbeans.modules.asm.base.att.ATTParser;


class DisFuncNameDetector implements ATTParser.FunctionNameDetector {

    private static final ATTParser.FunctionNameDetector instance =
            new DisFuncNameDetector();
        
    
    public static ATTParser.FunctionNameDetector getInstance() {
        return instance;
    }
    
    private DisFuncNameDetector() { }
            
    public boolean isFunctionLabel(String name) {
        return name.endsWith("()");  // NOI18N
    }

    public String getCleanName(String name) {
        if (name.endsWith("()")) {   // NOI18N
            return name.substring(0, name.length() - 2);
        }
        else if(name.endsWith(":")) {
            return name.substring(0, name.length() - 1);
        }
        return name;
    }                 
}
