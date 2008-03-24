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

package org.netbeans.modules.cnd.modelimpl.parser.apt;

import java.util.HashMap;
import java.util.Map;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmUID;
import org.netbeans.modules.cnd.apt.structure.APT;
import org.netbeans.modules.cnd.apt.structure.APTDefine;
import org.netbeans.modules.cnd.apt.structure.APTFile;
import org.netbeans.modules.cnd.apt.structure.APTUndefine;
import org.netbeans.modules.cnd.apt.support.APTPreprocHandler;
import org.netbeans.modules.cnd.apt.support.APTWalker;
import org.netbeans.modules.cnd.modelimpl.uid.UIDCsmConverter;
//import org.netbeans.modules.cnd.modelutil.CsmUtilities;

/**
 *
 * @author Sergey Grinev
 */
public class APTDefinesCollectorWalker extends APTSelfWalker {

    /*package*/
    final Map<String, MacroInfo> macroRefMap;
    private final String includePath;

    protected APTDefinesCollectorWalker(APTFile apt, CsmFile csmFile, APTPreprocHandler preprocHandler) {
        this(apt, csmFile, preprocHandler, new HashMap<String, MacroInfo>(), null);
    }

    private APTDefinesCollectorWalker(APTFile apt, CsmFile csmFile, APTPreprocHandler preprocHandler, Map<String, MacroInfo> macroRefMap, String includePath) {
        super(apt, csmFile, preprocHandler);
        this.macroRefMap = macroRefMap;
        this.includePath = includePath;
    }

    @Override
    protected APTWalker createIncludeWalker(APTFile apt, APTSelfWalker parent, String includePath) {
        return new APTDefinesCollectorWalker(apt, parent.csmFile, ((APTDefinesCollectorWalker) parent).getPreprocHandler(), macroRefMap, includePath);
    }

    @Override
    protected void onDefine(APT apt) {
        super.onDefine(apt);
        APTDefine aptMacro = (APTDefine) apt;
        macroRefMap.put(aptMacro.getName().getText(), new MacroInfo(csmFile, apt.getOffset(), apt.getEndOffset(), includePath));
    }

    @Override
    protected void onUndef(APT apt) {
        super.onUndef(apt);
        APTUndefine aptUndef = (APTUndefine) apt;
        macroRefMap.remove(aptUndef.getName().getText());
    }
}

class MacroInfo {

    public MacroInfo(CsmFile file, int startOffest, int endOffset, String includePath) {
        this.targetFile = UIDCsmConverter.fileToUID(file);
        this.startOffset = startOffest;
        this.endOffset = endOffset;
        this.includePath = includePath;
    }
    public final CsmUID<CsmFile> targetFile;
    public final int startOffset;
    public final int endOffset;
    public final String includePath;
}