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

package org.netbeans.modules.cnd.editor.spi.cplusplus;

import javax.swing.Action;
import org.openide.util.Lookup;

/**
 * 
 * @author Vladimir Voskresensky
 */
public abstract class CndEditorActionsProvider {
    private static final CndEditorActionsProvider DEFAULT = new Default();
    
    public static CndEditorActionsProvider getDefault() {
        return DEFAULT;
    }
    
    /**
     * returns addional actions for asked mime type
     * @param mime
     * @return
     */
    public abstract Action[] getActions(String mime);
    
    private static final class Default extends CndEditorActionsProvider {
        private final Lookup.Result<CndEditorActionsProvider> res;
        private Default() {
            res = Lookup.getDefault().lookupResult(CndEditorActionsProvider.class);
        }
        
        @Override
        public Action[] getActions(String mime) {
            Action[] tmp = new Action[100];
            int pos = 0;
            for (CndEditorActionsProvider provider : res.allInstances()) {
                Action[] curActions = provider.getActions(mime);
                if (pos + curActions.length > tmp.length) {
                    // reallocate
                    int extra = Math.max(curActions.length + 1, 100);
                    Action[] copy = new Action[tmp.length + extra];
                    System.arraycopy(tmp, 0, copy, 0, pos);
                    tmp = null;
                    tmp = copy;
                }
                System.arraycopy(curActions, 0, tmp, pos, curActions.length);
                pos += curActions.length;
            }
            Action[] out = new Action[pos];
            System.arraycopy(tmp, 0, out, 0, pos);
            return out;
        }
    }
}
