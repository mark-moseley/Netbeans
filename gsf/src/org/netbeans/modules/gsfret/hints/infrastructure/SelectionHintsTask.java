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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.gsfret.hints.infrastructure;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.gsf.HintsProvider;
import org.netbeans.napi.gsfret.source.CompilationInfo;
import org.netbeans.napi.gsfret.source.support.CaretAwareSourceTaskFactory;
import org.netbeans.modules.gsfret.editor.semantic.ScanningCancellableTask;
import org.netbeans.napi.gsfret.source.support.SelectionAwareSourceTaskFactory;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.HintsController;

/**
 *
 * @author Tor Norbye
 */
public class SelectionHintsTask extends ScanningCancellableTask<CompilationInfo> {
    
    public SelectionHintsTask() {
    }
    
    public void run(CompilationInfo info) throws Exception {
        resume();
        
        int[] range = SelectionAwareSourceTaskFactory.getLastSelection(info.getFileObject());
        
        if (range == null || range.length != 2 || range[0] == -1 || range[1] == -1) {
            return;
        }

        HintsProvider provider = info.getLanguage().getHintsProvider();

        if (provider == null) {
            return;
        }
        
        List<ErrorDescription> result = new ArrayList<ErrorDescription>();
        
        if (range[0] != range[1]) {
            provider.computeSelectionHints(info, result, Math.min(range[0], range[1]), Math.max(range[0], range[1]));
        }
        
        if (isCancelled()) {
            return;
        }
        
        HintsController.setErrors(info.getFileObject(), SelectionHintsTask.class.getName(), result);
    }
}
