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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.lib.editor.codetemplates.textsync;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Group of text syncs that can be traversed by TAB (Shift-TAB) keys. ENTER may
 * be used to activate a special caret text sync.
 * <br/>
 * {@link TextRegionManager} can maintain multiple groups but only one of them
 * may be active at a given time.
 *
 * @author Miloslav Metelka
 */
public final class TextSyncGroup {
    
    private TextRegionManager manager;
    
    private List<TextSync> textSyncs;
    
    public TextSyncGroup(TextSync... textSyncs) {
        initTextSyncs(textSyncs.length);
        for (TextSync textSync : textSyncs)
            addTextSync(textSync);
    }
    
    public TextSyncGroup() {
        initTextSyncs(4);
    }

    private void initTextSyncs(int size) {
        this.textSyncs = new ArrayList<TextSync>(size);
    }
    
    /**
     * Get list of all text syncs managed by this group.
     *
     * @return non-null unmodifiable list of text syncs.
     */
    public List<TextSync> textSyncs() {
        return Collections.unmodifiableList(textSyncs);
    }
    
    public void addTextSync(TextSync textSync) {
        if (textSync == null)
            throw new IllegalArgumentException("textSync cannot be null");
        if (textSync.textSyncGroup() != null)
            throw new IllegalArgumentException("textSync " + textSync + // NOI18N
                    " already assigned to group " + textSync.textSyncGroup()); // NOI18N
        textSyncs.add(textSync);
        textSync.setTextSyncGroup(this);
        
    }

    public void removeTextSync(TextSync textSync) {
        if (textSyncs.remove(textSync)) {
            textSync.setTextSyncGroup(null);
        }
    }

    List<TextSync> textSyncsModifiable() {
        return textSyncs;
    }

    public TextRegionManager textRegionManager() {
        return manager;
    }
    
    void setTextRegionManager(TextRegionManager manager) {
        this.manager = manager;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(textSyncs.size() * 50 + 2);
        sb.append('{');
        if (textSyncs.size() > 0)
            sb.append(textSyncs.get(0));
        for (int i = 1; i < textSyncs.size(); i++)
            sb.append(", ").append(textSyncs.get(i));
        sb.append('}');
        return sb.toString();
    }

}
