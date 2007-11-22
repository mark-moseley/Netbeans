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
package org.netbeans.modules.cnd.modelimpl.impl.services;

import antlr.Token;
import antlr.TokenStream;
import antlr.TokenStreamException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmFunctionDefinition;
import org.netbeans.modules.cnd.api.model.CsmMember;
import org.netbeans.modules.cnd.api.model.CsmMethod;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.api.model.services.CsmFileInfoQuery;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;
import org.netbeans.modules.cnd.api.model.xref.CsmReferenceResolver;
import org.netbeans.modules.cnd.api.project.NativeFileItem;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.netbeans.modules.cnd.apt.structure.APT;
import org.netbeans.modules.cnd.apt.structure.APTFile;
import org.netbeans.modules.cnd.apt.support.APTDriver;
import org.netbeans.modules.cnd.apt.support.APTToken;
import org.netbeans.modules.cnd.modelimpl.csm.FieldImpl;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileImpl;
import org.netbeans.modules.cnd.modelimpl.csm.core.ProjectBase;
import org.netbeans.modules.cnd.modelimpl.parser.apt.APTFindMacrosWalker;
import org.netbeans.modules.cnd.modelimpl.parser.apt.APTFindUnusedBlocksWalker;
import org.netbeans.modules.cnd.modelimpl.parser.generated.CPPTokenTypes;
import org.openide.util.Exceptions;

/**
 * implementaion of CsmFileInfoQuery
 * @author Vladimir Voskresenskky
 */
public class FileInfoQueryImpl extends CsmFileInfoQuery {

    public List<String> getSystemIncludePaths(CsmFile file) {
        return getIncludePaths(file, true);
    }

    public List<String> getUserIncludePaths(CsmFile file) {
        return getIncludePaths(file, false);
    }

    private List<String> getIncludePaths(CsmFile file, boolean system) {
        List<String> out = Collections.<String>emptyList();
        if (file instanceof FileImpl) {
            NativeFileItem item = ProjectBase.getCompiledFileItem((FileImpl) file);
            if (item != null) {
                if (item.getLanguage() == NativeFileItem.Language.C_HEADER) {
                    // It's an orphan (otherwise the getCompiledFileItem would return C or C++ item, not header).
                    // For headers, NativeFileItem does NOT contain necessary information
                    // (whe parsing, we use DefaultFileItem for headers)
                    // so for headers, we should use project iformation instead
                    NativeProject nativeProject = item.getNativeProject();
                    if (nativeProject != null) {
                        if (system) {
                            out = nativeProject.getSystemIncludePaths();
                        } else {
                            out = nativeProject.getUserIncludePaths();
                        }
                    }
                } else {
                    if (system) {
                        out = item.getSystemIncludePaths();
                    } else {
                        out = item.getUserIncludePaths();
                    }
                }
            }
        }
        return out;
    }

    public List<CsmOffsetable> getUnusedCodeBlocks(CsmFile file) {
        List<CsmOffsetable> out = Collections.<CsmOffsetable>emptyList();
        if (file instanceof FileImpl) {
            FileImpl fileImpl = (FileImpl) file;

            try {
                APTFile apt = APTDriver.getInstance().findAPTLight(fileImpl.getBuffer());

                if (hasConditionalsDirectives(apt)) {
                    APTFindUnusedBlocksWalker walker = new APTFindUnusedBlocksWalker(apt, fileImpl, fileImpl.getPreprocHandler());
                    walker.visit();
                    out = walker.getBlocks();
                }
            } catch (IOException ex) {
                System.err.println("skip getting unused blockes\nreason:" + ex.getMessage()); //NOI18N
            }
        }
        return out;
    }

    private static boolean hasConditionalsDirectives(APTFile apt) {
        if (apt == null) {
            return false;
        }
        APT node = apt.getFirstChild();
        while (node != null) {
            if (node.getType() == APT.Type.CONDITION_CONTAINER) {
                return true;
            }
            assert node.getFirstChild() == null;
            node = node.getNextSibling();
        }
        return false;
    }

    public List<CsmReference> getMacroUsages(CsmFile file) {
        List<CsmReference> out = Collections.<CsmReference>emptyList();
        if (file instanceof FileImpl) {
            FileImpl fileImpl = (FileImpl) file;

            try {
                APTFile apt = APTDriver.getInstance().findAPT(fileImpl.getBuffer());
                if (apt != null) {
                    APTFindMacrosWalker walker = new APTFindMacrosWalker(apt, fileImpl, fileImpl.getPreprocHandler());
                    walker.getTokenStream();
                    out = walker.getCollectedData();
                }
            } catch (IOException ex) {
                System.err.println("skip marking macros\nreason:" + ex.getMessage()); //NOI18N
            }
        }
        return out;
    }

}
