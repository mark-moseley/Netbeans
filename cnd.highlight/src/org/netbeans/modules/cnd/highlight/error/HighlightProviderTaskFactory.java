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

package org.netbeans.modules.cnd.highlight.error;

import javax.swing.text.Document;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.model.tasks.CsmFileTaskFactory.PhaseRunner;
import org.netbeans.modules.cnd.model.tasks.EditorAwareCsmFileTaskFactory;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;

/**
 *
 * @author Sergey Grinev
 */
public class HighlightProviderTaskFactory extends EditorAwareCsmFileTaskFactory {

    @Override
    protected PhaseRunner createTask(FileObject fo) {
        PhaseRunner pr = null;
        try {
            DataObject dobj = DataObject.find(fo);
            EditorCookie ec = dobj.getCookie(EditorCookie.class);
            final CsmFile file = CsmUtilities.getCsmFile(dobj, false);
            final Document doc = ec.getDocument();
            if (doc != null && file != null) {
                pr = new PhaseRunner() {
                    public void run(Phase phase) {
                        if (phase == Phase.PARSED || phase == Phase.INIT) {
                            HighlightProvider.getInstance().update(file, doc);
                        } else if (phase == Phase.CLEANUP) {
                            HighlightProvider.getInstance().clear(file, doc);
                        }
                    }
                };
            }
        } catch (DataObjectNotFoundException ex)  {
            ex.printStackTrace();
        }
        return pr != null ? pr : lazyRunner();
    }
}
