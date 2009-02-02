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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.cnd.apt.support;

import antlr.TokenStreamException;
import java.io.File;
import java.util.logging.Level;
import org.netbeans.modules.cnd.apt.debug.DebugUtils;
import org.netbeans.modules.cnd.apt.structure.APT;
import org.netbeans.modules.cnd.apt.structure.APTDefine;
import org.netbeans.modules.cnd.apt.structure.APTFile;
import org.netbeans.modules.cnd.apt.structure.APTInclude;
import org.netbeans.modules.cnd.apt.structure.APTIncludeNext;
import org.netbeans.modules.cnd.apt.structure.APTUndefine;
import org.netbeans.modules.cnd.apt.support.APTMacro.Kind;
import org.netbeans.modules.cnd.apt.utils.APTUtils;

/**
 * abstract Tree walker for APT
 * @author Vladimir Voskresensky
 */
public abstract class APTAbstractWalker extends APTWalker {
    
    private final APTPreprocHandler preprocHandler;
    private final String startPath;
    
    protected APTAbstractWalker(APTFile apt, APTPreprocHandler preprocHandler) {
        super(apt, preprocHandler == null ? null: preprocHandler.getMacroMap());
        this.startPath = apt.getPath();
        this.preprocHandler = preprocHandler;
    }
    
    protected void onInclude(APT apt) {
        if (getIncludeHandler() != null) {
            APTIncludeResolver resolver = getIncludeHandler().getResolver(startPath);
            ResolvedPath resolvedPath = resolver.resolveInclude((APTInclude)apt, getMacroMap());
            if (resolvedPath == null) {
                if (DebugUtils.STANDALONE) {
                    if (APTUtils.LOG.getLevel().intValue() <= Level.SEVERE.intValue()) {
                        System.err.println("FAILED INCLUDE: from " + new File(startPath).getName() + " for:\n\t" + apt);// NOI18N
                    }
                } else {
                    APTUtils.LOG.log(Level.WARNING,
                            "failed resolving path from {0} for {1}", // NOI18N
                            new Object[] { startPath, apt });
                }
            }
            include(resolvedPath, (APTInclude)apt);
        }
    }
    
    protected void onIncludeNext(APT apt) {
        if (getIncludeHandler() != null) {
            APTIncludeResolver resolver = getIncludeHandler().getResolver(startPath);
            ResolvedPath resolvedPath = resolver.resolveIncludeNext((APTIncludeNext)apt, getMacroMap());
            if (resolvedPath == null) {
                if (DebugUtils.STANDALONE) {
                    if (APTUtils.LOG.getLevel().intValue() <= Level.SEVERE.intValue()) {
                        System.err.println("FAILED INCLUDE: from " + new File(startPath).getName() + " for:\n\t" + apt);// NOI18N
                    }
                } else {
                    APTUtils.LOG.log(Level.WARNING,
                            "failed resolving path from {0} for {1}", // NOI18N
                            new Object[] { startPath, apt });
                }
            }
            include(resolvedPath, (APTInclude) apt);
        }
    }
    
    abstract protected void include(ResolvedPath resolvedPath, APTInclude aPTInclude);
   
    protected void onDefine(APT apt) {
        APTDefine define = (APTDefine)apt;
        if (define.isValid()) {
            getMacroMap().define(getRootFile(), define.getName(), define.getParams(), define.getBody(), Kind.DEFINED);
        } else {
            if (DebugUtils.STANDALONE) {
                if (APTUtils.LOG.getLevel().intValue() <= Level.SEVERE.intValue()) {
                    System.err.println("INCORRECT #define directive: in " + new File(startPath).getName() + " for:\n\t" + apt);// NOI18N
                }
            } else {
                APTUtils.LOG.log(Level.SEVERE,
                        "INCORRECT #define directive: in {0} for:\n\t{1}", // NOI18N
                        new Object[] { new File(startPath).getName(), apt });
            }
        }
    }
    
    protected void onUndef(APT apt) {
        APTUndefine undef = (APTUndefine)apt;
        getMacroMap().undef(getRootFile(), undef.getName());
    }
    
    protected boolean onIf(APT apt) {
        return eval(apt);
    }
    
    protected boolean onIfdef(APT apt) {
        return eval(apt);
    }
    
    protected boolean onIfndef(APT apt) {
        return eval(apt);
    }
    
    protected boolean onElif(APT apt, boolean wasInPrevBranch) {
        return !wasInPrevBranch && eval(apt);
    }
    
    protected boolean onElse(APT apt, boolean wasInPrevBranch) {
        return !wasInPrevBranch;
    }
    
    protected void onEndif(APT apt, boolean wasInBranch) {
    }

    protected void onEval(APT apt, boolean result) {
    }

    protected APTPreprocHandler getPreprocHandler() {
        return preprocHandler;
    }
    
    protected APTIncludeHandler getIncludeHandler() {
        return getPreprocHandler() == null ? null: getPreprocHandler().getIncludeHandler();
    }   
 
    ////////////////////////////////////////////////////////////////////////////
    // implementation details
   
    private boolean eval(APT apt) {
        APTUtils.LOG.log(Level.FINE, "eval condition for {0}", new Object[] {apt});// NOI18N
        boolean res = false;
        try {
            res = APTConditionResolver.evaluate(apt, getMacroMap());
        } catch (TokenStreamException ex) {
            APTUtils.LOG.log(Level.SEVERE, "error on evaluating condition node " + apt, ex);// NOI18N
        }
        onEval(apt, res);
        return res;
    }
}
